package com.quickcode.service.impl;

import com.quickcode.config.FileUploadConfig;
import com.quickcode.entity.ProjectFile;
import com.quickcode.service.FileExtractionService;
import com.quickcode.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipFile;

/**
 * 文件解压和结构分析服务实现类
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileExtractionServiceImpl implements FileExtractionService {

    private final FileUploadConfig fileUploadConfig;
    private final FileStorageService fileStorageService;

    // 最大解压文件数量
    private static final int MAX_EXTRACT_FILES = 1000;
    // 最大解压大小 (1GB)
    private static final long MAX_EXTRACT_SIZE = 1024L * 1024 * 1024;

    @Override
    public ExtractionResult extractProjectFile(ProjectFile projectFile) {
        log.info("开始解压项目文件: fileId={}, fileName={}", projectFile.getId(), projectFile.getFileName());

        try {
            // 获取文件路径
            String filePath = projectFile.getFilePath();
            if (!fileStorageService.exists(filePath)) {
                return new ExtractionResult(false, null, 0, 0, "源文件不存在", new ArrayList<>());
            }

            // 创建解压目录
            String extractPath = createExtractionDirectory(projectFile);
            
            // 根据文件类型选择解压方法
            String fileName = projectFile.getFileName().toLowerCase();
            ExtractionResult result;
            
            if (fileName.endsWith(".zip")) {
                result = extractZipFile(filePath, extractPath);
            } else if (fileName.endsWith(".rar")) {
                result = extractRarFile(filePath, extractPath);
            } else if (fileName.endsWith(".7z")) {
                result = extract7zFile(filePath, extractPath);
            } else if (fileName.endsWith(".tar.gz") || fileName.endsWith(".tar")) {
                result = extractTarFile(filePath, extractPath);
            } else {
                return new ExtractionResult(false, null, 0, 0, "不支持的压缩格式", new ArrayList<>());
            }

            if (result.isSuccess()) {
                log.info("文件解压成功: fileId={}, extractedFiles={}, extractedSize={}", 
                    projectFile.getId(), result.getFileCount(), result.getExtractedSize());
            } else {
                log.warn("文件解压失败: fileId={}, message={}", projectFile.getId(), result.getMessage());
            }

            return result;

        } catch (Exception e) {
            log.error("解压文件时发生异常: fileId={}", projectFile.getId(), e);
            return new ExtractionResult(false, null, 0, 0, "解压过程中发生错误: " + e.getMessage(), new ArrayList<>());
        }
    }

    @Override
    public ProjectStructureAnalysis analyzeProjectStructure(String extractedPath) {
        log.info("开始分析项目结构: path={}", extractedPath);

        try {
            Path rootPath = Paths.get(extractedPath);
            if (!Files.exists(rootPath)) {
                return createEmptyAnalysis();
            }

            // 收集文件信息
            Map<String, Integer> fileTypeCount = new HashMap<>();
            List<String> techStack = new ArrayList<>();
            List<String> configFiles = new ArrayList<>();
            AtomicBoolean hasReadme = new AtomicBoolean(false);
            AtomicBoolean hasLicense = new AtomicBoolean(false);
            AtomicReference<String> mainEntryPoint = new AtomicReference<>(null);

            // 遍历文件
            Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    String fileName = file.getFileName().toString().toLowerCase();
                    String extension = getFileExtension(fileName);

                    // 统计文件类型
                    fileTypeCount.merge(extension, 1, Integer::sum);

                    // 检查特殊文件
                    if (fileName.startsWith("readme")) {
                        hasReadme.set(true);
                    } else if (fileName.startsWith("license")) {
                        hasLicense.set(true);
                    }

                    // 检查配置文件
                    if (isConfigFile(fileName)) {
                        configFiles.add(fileName);
                    }

                    // 检查入口文件
                    if (isMainEntryFile(fileName) && mainEntryPoint.get() == null) {
                        mainEntryPoint.set(fileName);
                    }
                });

            // 分析技术栈
            techStack = analyzeTechStack(fileTypeCount, configFiles);
            
            // 判断项目类型
            ProjectType projectType = determineProjectType(fileTypeCount, configFiles);

            return new ProjectStructureAnalysis(projectType, techStack, fileTypeCount,
                hasReadme.get(), hasLicense.get(), mainEntryPoint.get(), configFiles);

        } catch (Exception e) {
            log.error("分析项目结构时发生异常: path={}", extractedPath, e);
            return createEmptyAnalysis();
        }
    }

    @Override
    public DockerConfigDetection detectDockerConfig(String extractedPath) {
        log.info("开始检测Docker配置: path={}", extractedPath);

        try {
            Path rootPath = Paths.get(extractedPath);
            Path dockerfilePath = rootPath.resolve("Dockerfile");
            Path dockerComposePath = rootPath.resolve("docker-compose.yml");
            
            boolean hasDockerfile = Files.exists(dockerfilePath);
            boolean hasDockerCompose = Files.exists(dockerComposePath);
            
            String dockerfileContent = null;
            String baseImage = null;
            List<String> exposedPorts = new ArrayList<>();
            List<String> volumes = new ArrayList<>();
            
            if (hasDockerfile) {
                dockerfileContent = Files.readString(dockerfilePath);
                baseImage = extractBaseImage(dockerfileContent);
                exposedPorts = extractExposedPorts(dockerfileContent);
                volumes = extractVolumes(dockerfileContent);
            }
            
            boolean isDockerized = hasDockerfile || hasDockerCompose;
            
            return new DockerConfigDetection(hasDockerfile, hasDockerCompose, dockerfileContent,
                baseImage, exposedPorts, volumes, isDockerized);

        } catch (Exception e) {
            log.error("检测Docker配置时发生异常: path={}", extractedPath, e);
            return new DockerConfigDetection(false, false, null, null, 
                new ArrayList<>(), new ArrayList<>(), false);
        }
    }

    @Override
    public ProjectIntegrityCheck checkProjectIntegrity(String extractedPath) {
        log.info("开始检查项目完整性: path={}", extractedPath);

        try {
            Path rootPath = Paths.get(extractedPath);
            List<String> missingFiles = new ArrayList<>();
            List<String> issues = new ArrayList<>();
            int qualityScore = 100;

            // 检查基本文件
            if (!hasReadmeFile(rootPath)) {
                missingFiles.add("README文件");
                qualityScore -= 10;
            }

            if (!hasLicenseFile(rootPath)) {
                missingFiles.add("LICENSE文件");
                qualityScore -= 5;
            }

            // 检查项目结构
            if (!hasValidProjectStructure(rootPath)) {
                issues.add("项目结构不规范");
                qualityScore -= 15;
            }

            // 检查代码质量
            if (hasLargeFiles(rootPath)) {
                issues.add("包含过大的文件");
                qualityScore -= 10;
            }

            if (hasTooManyFiles(rootPath)) {
                issues.add("文件数量过多");
                qualityScore -= 5;
            }

            boolean isComplete = missingFiles.isEmpty() && issues.isEmpty();
            qualityScore = Math.max(0, qualityScore);

            return new ProjectIntegrityCheck(isComplete, missingFiles, issues, qualityScore);

        } catch (Exception e) {
            log.error("检查项目完整性时发生异常: path={}", extractedPath, e);
            return new ProjectIntegrityCheck(false, Arrays.asList("检查过程中发生错误"), 
                Arrays.asList(e.getMessage()), 0);
        }
    }

    @Override
    public boolean cleanupExtractedFiles(String extractedPath) {
        log.info("开始清理解压文件: path={}", extractedPath);

        try {
            Path path = Paths.get(extractedPath);
            if (Files.exists(path)) {
                Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
                
                log.info("解压文件清理成功: path={}", extractedPath);
                return true;
            }
            return true;
        } catch (Exception e) {
            log.error("清理解压文件时发生异常: path={}", extractedPath, e);
            return false;
        }
    }

    /**
     * 创建解压目录
     */
    private String createExtractionDirectory(ProjectFile projectFile) throws IOException {
        String basePath = fileUploadConfig.getUploadPath();
        String extractPath = basePath + "/extracted/" + projectFile.getProjectId() + "/" + projectFile.getId();
        
        Path path = Paths.get(extractPath);
        Files.createDirectories(path);
        
        return extractPath;
    }

    /**
     * 解压ZIP文件
     */
    private ExtractionResult extractZipFile(String filePath, String extractPath) {
        List<String> extractedFiles = new ArrayList<>();
        long totalSize = 0;
        int fileCount = 0;

        try (ZipInputStream zis = new ZipInputStream(
                fileStorageService.loadAsResource(filePath).getInputStream())) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // 安全检查
                if (fileCount >= MAX_EXTRACT_FILES) {
                    return new ExtractionResult(false, null, 0, 0, "解压文件数量超过限制", extractedFiles);
                }
                
                if (totalSize >= MAX_EXTRACT_SIZE) {
                    return new ExtractionResult(false, null, 0, 0, "解压文件大小超过限制", extractedFiles);
                }

                // 防止路径遍历攻击
                String entryName = entry.getName();
                if (entryName.contains("..") || entryName.startsWith("/")) {
                    continue;
                }

                Path targetPath = Paths.get(extractPath, entryName);
                
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    extractedFiles.add(entryName);
                    totalSize += Files.size(targetPath);
                    fileCount++;
                }
                
                zis.closeEntry();
            }

            return new ExtractionResult(true, extractPath, totalSize, fileCount, "解压成功", extractedFiles);

        } catch (Exception e) {
            log.error("解压ZIP文件失败: filePath={}", filePath, e);
            return new ExtractionResult(false, null, 0, 0, "解压失败: " + e.getMessage(), extractedFiles);
        }
    }

    /**
     * 解压RAR文件（简化实现，实际项目中可能需要第三方库）
     */
    private ExtractionResult extractRarFile(String filePath, String extractPath) {
        log.warn("RAR格式解压暂不支持: filePath={}", filePath);
        return new ExtractionResult(false, null, 0, 0, "RAR格式暂不支持", new ArrayList<>());
    }

    /**
     * 解压7Z文件（简化实现，实际项目中可能需要第三方库）
     */
    private ExtractionResult extract7zFile(String filePath, String extractPath) {
        log.warn("7Z格式解压暂不支持: filePath={}", filePath);
        return new ExtractionResult(false, null, 0, 0, "7Z格式暂不支持", new ArrayList<>());
    }

    /**
     * 解压TAR文件（简化实现）
     */
    private ExtractionResult extractTarFile(String filePath, String extractPath) {
        log.warn("TAR格式解压暂不支持: filePath={}", filePath);
        return new ExtractionResult(false, null, 0, 0, "TAR格式暂不支持", new ArrayList<>());
    }

    /**
     * 创建空的分析结果
     */
    private ProjectStructureAnalysis createEmptyAnalysis() {
        return new ProjectStructureAnalysis(ProjectType.UNKNOWN, new ArrayList<>(),
            new HashMap<>(), false, false, null, new ArrayList<>());
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    /**
     * 判断是否为配置文件
     */
    private boolean isConfigFile(String fileName) {
        return fileName.equals("package.json") || fileName.equals("pom.xml") ||
               fileName.equals("build.gradle") || fileName.equals("requirements.txt") ||
               fileName.equals("composer.json") || fileName.equals("cargo.toml") ||
               fileName.equals("go.mod") || fileName.endsWith(".config") ||
               fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }

    /**
     * 判断是否为主入口文件
     */
    private boolean isMainEntryFile(String fileName) {
        return fileName.equals("index.html") || fileName.equals("index.js") ||
               fileName.equals("main.js") || fileName.equals("app.js") ||
               fileName.equals("main.py") || fileName.equals("app.py") ||
               fileName.equals("main.java") || fileName.equals("application.java");
    }

    /**
     * 分析技术栈
     */
    private List<String> analyzeTechStack(Map<String, Integer> fileTypeCount, List<String> configFiles) {
        List<String> techStack = new ArrayList<>();

        // 根据文件扩展名判断技术栈
        if (fileTypeCount.containsKey("js") || fileTypeCount.containsKey("jsx")) {
            techStack.add("JavaScript");
        }
        if (fileTypeCount.containsKey("ts") || fileTypeCount.containsKey("tsx")) {
            techStack.add("TypeScript");
        }
        if (fileTypeCount.containsKey("java")) {
            techStack.add("Java");
        }
        if (fileTypeCount.containsKey("py")) {
            techStack.add("Python");
        }
        if (fileTypeCount.containsKey("php")) {
            techStack.add("PHP");
        }
        if (fileTypeCount.containsKey("go")) {
            techStack.add("Go");
        }
        if (fileTypeCount.containsKey("rs")) {
            techStack.add("Rust");
        }

        // 根据配置文件判断框架
        for (String configFile : configFiles) {
            if (configFile.equals("package.json")) {
                techStack.add("Node.js");
            } else if (configFile.equals("pom.xml")) {
                techStack.add("Maven");
            } else if (configFile.equals("build.gradle")) {
                techStack.add("Gradle");
            } else if (configFile.equals("requirements.txt")) {
                techStack.add("Python");
            } else if (configFile.equals("composer.json")) {
                techStack.add("Composer");
            }
        }

        return techStack;
    }

    /**
     * 判断项目类型
     */
    private ProjectType determineProjectType(Map<String, Integer> fileTypeCount, List<String> configFiles) {
        // 前端项目判断
        if (fileTypeCount.containsKey("html") || fileTypeCount.containsKey("css") ||
            fileTypeCount.containsKey("js") || fileTypeCount.containsKey("jsx") ||
            fileTypeCount.containsKey("ts") || fileTypeCount.containsKey("tsx")) {

            // 如果同时有后端文件，则为全栈项目
            if (fileTypeCount.containsKey("java") || fileTypeCount.containsKey("py") ||
                fileTypeCount.containsKey("php") || fileTypeCount.containsKey("go")) {
                return ProjectType.FULL_STACK;
            }
            return ProjectType.WEB_FRONTEND;
        }

        // 后端项目判断
        if (fileTypeCount.containsKey("java") || fileTypeCount.containsKey("py") ||
            fileTypeCount.containsKey("php") || fileTypeCount.containsKey("go") ||
            fileTypeCount.containsKey("rs")) {
            return ProjectType.WEB_BACKEND;
        }

        // 移动应用判断
        if (fileTypeCount.containsKey("swift") || fileTypeCount.containsKey("kt") ||
            fileTypeCount.containsKey("dart")) {
            return ProjectType.MOBILE_APP;
        }

        return ProjectType.UNKNOWN;
    }

    /**
     * 提取Docker基础镜像
     */
    private String extractBaseImage(String dockerfileContent) {
        String[] lines = dockerfileContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.toUpperCase().startsWith("FROM ")) {
                return line.substring(5).trim();
            }
        }
        return null;
    }

    /**
     * 提取暴露的端口
     */
    private List<String> extractExposedPorts(String dockerfileContent) {
        List<String> ports = new ArrayList<>();
        String[] lines = dockerfileContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.toUpperCase().startsWith("EXPOSE ")) {
                String portStr = line.substring(7).trim();
                ports.add(portStr);
            }
        }
        return ports;
    }

    /**
     * 提取数据卷
     */
    private List<String> extractVolumes(String dockerfileContent) {
        List<String> volumes = new ArrayList<>();
        String[] lines = dockerfileContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.toUpperCase().startsWith("VOLUME ")) {
                String volumeStr = line.substring(7).trim();
                volumes.add(volumeStr);
            }
        }
        return volumes;
    }

    /**
     * 检查是否有README文件
     */
    private boolean hasReadmeFile(Path rootPath) {
        try {
            return Files.walk(rootPath, 1)
                .anyMatch(path -> path.getFileName().toString().toLowerCase().startsWith("readme"));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查是否有LICENSE文件
     */
    private boolean hasLicenseFile(Path rootPath) {
        try {
            return Files.walk(rootPath, 1)
                .anyMatch(path -> path.getFileName().toString().toLowerCase().startsWith("license"));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查项目结构是否有效
     */
    private boolean hasValidProjectStructure(Path rootPath) {
        try {
            long fileCount = Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .count();
            return fileCount > 0 && fileCount < MAX_EXTRACT_FILES;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查是否有过大的文件
     */
    private boolean hasLargeFiles(Path rootPath) {
        try {
            return Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .anyMatch(path -> {
                    try {
                        return Files.size(path) > 50 * 1024 * 1024; // 50MB
                    } catch (IOException e) {
                        return false;
                    }
                });
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查文件数量是否过多
     */
    private boolean hasTooManyFiles(Path rootPath) {
        try {
            long fileCount = Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .count();
            return fileCount > 500;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public ValidationResult validateArchiveIntegrity(ProjectFile projectFile) {
        log.info("开始验证压缩文件完整性: fileId={}, fileName={}", projectFile.getId(), projectFile.getFileName());

        List<String> issues = new ArrayList<>();
        boolean isValid = true;
        String message = "文件验证通过";

        try {
            // 构建文件路径
            String filePath = fileStorageService.getFilePath(projectFile.getFileName(),
                "projects/" + projectFile.getProjectId()).toString();
            File file = new File(filePath);

            if (!file.exists()) {
                return new ValidationResult(false, "文件不存在",
                    Arrays.asList("文件路径无效"), 0, null);
            }

            long actualSize = file.length();

            // 1. 文件大小验证
            if (actualSize != projectFile.getFileSize()) {
                issues.add("文件大小不匹配，预期: " + projectFile.getFileSize() + ", 实际: " + actualSize);
                isValid = false;
            }

            // 2. 文件格式验证
            String fileName = projectFile.getFileName().toLowerCase();
            if (!isValidArchiveFormat(fileName)) {
                issues.add("不支持的压缩文件格式");
                isValid = false;
            }

            // 3. 文件头部验证
            if (!validateArchiveHeader(filePath, fileName)) {
                issues.add("文件头部验证失败，可能文件已损坏");
                isValid = false;
            }

            // 4. 计算文件校验和
            String checksum = calculateFileChecksum(filePath);

            // 5. 压缩文件结构验证
            if (isValid && !validateArchiveStructure(filePath, fileName)) {
                issues.add("压缩文件结构验证失败");
                isValid = false;
            }

            if (!isValid) {
                message = "文件验证失败: " + String.join(", ", issues);
            }

            return new ValidationResult(isValid, message, issues, actualSize, checksum);

        } catch (Exception e) {
            log.error("验证压缩文件完整性时发生异常: fileId={}", projectFile.getId(), e);
            return new ValidationResult(false, "验证过程中发生错误: " + e.getMessage(),
                Arrays.asList("系统异常"), 0, null);
        }
    }

    @Override
    public PreExtractionCheck preCheckExtraction(ProjectFile projectFile) {
        log.info("开始预检查解压操作: fileId={}, fileName={}", projectFile.getId(), projectFile.getFileName());

        List<String> warnings = new ArrayList<>();
        List<String> securityIssues = new ArrayList<>();
        boolean canExtract = true;
        String reason = "可以安全解压";

        try {
            String filePath = fileStorageService.getFilePath(projectFile.getFileName(),
                "projects/" + projectFile.getProjectId()).toString();
            String fileName = projectFile.getFileName().toLowerCase();

            // 1. 文件大小预检查
            long fileSize = projectFile.getFileSize();
            if (fileSize > MAX_EXTRACT_SIZE) {
                canExtract = false;
                reason = "文件过大，超过解压限制";
                securityIssues.add("文件大小超过限制: " + fileSize + " bytes");
            }

            // 2. 估算解压后大小和文件数量
            long estimatedSize = 0;
            int estimatedFileCount = 0;

            if (fileName.endsWith(".zip")) {
                EstimationResult estimation = estimateZipExtraction(filePath);
                estimatedSize = estimation.getEstimatedSize();
                estimatedFileCount = estimation.getEstimatedFileCount();

                if (estimation.hasSecurityIssues()) {
                    securityIssues.addAll(estimation.getSecurityIssues());
                    if (estimation.isCritical()) {
                        canExtract = false;
                        reason = "检测到安全风险";
                    }
                }
            } else {
                // 对于其他格式，使用保守估算
                estimatedSize = fileSize * 3; // 假设压缩比为1:3
                estimatedFileCount = 100; // 保守估算
                warnings.add("无法精确估算解压大小，使用保守估算");
            }

            // 3. 检查解压限制
            if (estimatedSize > MAX_EXTRACT_SIZE) {
                warnings.add("估算解压大小可能超过限制: " + estimatedSize + " bytes");
            }

            if (estimatedFileCount > MAX_EXTRACT_FILES) {
                warnings.add("估算文件数量可能超过限制: " + estimatedFileCount + " files");
            }

            // 4. 磁盘空间检查
            if (!hasEnoughDiskSpace(estimatedSize)) {
                canExtract = false;
                reason = "磁盘空间不足";
                securityIssues.add("可用磁盘空间不足");
            }

            return new PreExtractionCheck(canExtract, reason, estimatedSize,
                estimatedFileCount, warnings, securityIssues);

        } catch (Exception e) {
            log.error("预检查解压操作时发生异常: fileId={}", projectFile.getId(), e);
            return new PreExtractionCheck(false, "预检查失败: " + e.getMessage(),
                0, 0, warnings, Arrays.asList("系统异常"));
        }
    }

    @Override
    public ExtractionResult safeExtractProjectFile(ProjectFile projectFile, int maxFiles, long maxSize) {
        log.info("开始安全解压项目文件: fileId={}, maxFiles={}, maxSize={}",
            projectFile.getId(), maxFiles, maxSize);

        try {
            // 1. 预检查
            PreExtractionCheck preCheck = preCheckExtraction(projectFile);
            if (!preCheck.canExtract()) {
                return new ExtractionResult(false, null, 0, 0,
                    "预检查失败: " + preCheck.getReason(), new ArrayList<>());
            }

            // 2. 验证文件完整性
            ValidationResult validation = validateArchiveIntegrity(projectFile);
            if (!validation.isValid()) {
                return new ExtractionResult(false, null, 0, 0,
                    "文件验证失败: " + validation.getMessage(), new ArrayList<>());
            }

            // 3. 使用自定义限制进行解压
            String filePath = fileStorageService.getFilePath(projectFile.getFileName(),
                "projects/" + projectFile.getProjectId()).toString();
            String extractPath = createExtractionDirectory(projectFile);
            String fileName = projectFile.getFileName().toLowerCase();

            ExtractionResult result;
            if (fileName.endsWith(".zip")) {
                result = safeExtractZipFile(filePath, extractPath, maxFiles, maxSize);
            } else {
                return new ExtractionResult(false, null, 0, 0, "不支持的压缩格式", new ArrayList<>());
            }

            // 4. 后处理验证
            if (result.isSuccess()) {
                if (!postExtractionValidation(result.getExtractedPath())) {
                    // 清理已解压的文件
                    cleanupExtractedFiles(result.getExtractedPath());
                    return new ExtractionResult(false, null, 0, 0,
                        "解压后验证失败，已清理文件", new ArrayList<>());
                }
            }

            return result;

        } catch (Exception e) {
            log.error("安全解压文件时发生异常: fileId={}", projectFile.getId(), e);
            return new ExtractionResult(false, null, 0, 0,
                "解压过程中发生错误: " + e.getMessage(), new ArrayList<>());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 验证压缩文件格式
     */
    private boolean isValidArchiveFormat(String fileName) {
        return fileName.endsWith(".zip") || fileName.endsWith(".rar") ||
               fileName.endsWith(".7z") || fileName.endsWith(".tar.gz") ||
               fileName.endsWith(".tar");
    }

    /**
     * 验证压缩文件头部
     */
    private boolean validateArchiveHeader(String filePath, String fileName) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] header = new byte[10];
            int bytesRead = fis.read(header);

            if (bytesRead < 4) {
                return false;
            }

            if (fileName.endsWith(".zip")) {
                // ZIP文件头: 50 4B 03 04 或 50 4B 05 06 或 50 4B 07 08
                return (header[0] == 0x50 && header[1] == 0x4B &&
                       (header[2] == 0x03 || header[2] == 0x05 || header[2] == 0x07));
            } else if (fileName.endsWith(".rar")) {
                // RAR文件头: 52 61 72 21
                return (header[0] == 0x52 && header[1] == 0x61 &&
                       header[2] == 0x72 && header[3] == 0x21);
            } else if (fileName.endsWith(".7z")) {
                // 7Z文件头: 37 7A BC AF 27 1C
                return (header[0] == 0x37 && header[1] == 0x7A &&
                       header[2] == (byte)0xBC && header[3] == (byte)0xAF);
            }

            return true; // 其他格式暂时认为有效
        } catch (IOException e) {
            log.warn("验证文件头部失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 计算文件校验和
     */
    private String calculateFileChecksum(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (FileInputStream fis = new FileInputStream(filePath);
                 DigestInputStream dis = new DigestInputStream(fis, md)) {

                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {
                    // 读取文件内容计算MD5
                }

                byte[] digest = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            }
        } catch (Exception e) {
            log.warn("计算文件校验和失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 验证压缩文件结构
     */
    private boolean validateArchiveStructure(String filePath, String fileName) {
        if (fileName.endsWith(".zip")) {
            return validateZipStructure(filePath);
        }
        // 其他格式暂时返回true
        return true;
    }

    /**
     * 验证ZIP文件结构
     */
    private boolean validateZipStructure(String filePath) {
        try (ZipFile zipFile = new ZipFile(filePath)) {
            // 检查ZIP文件是否可以正常打开和读取
            zipFile.entries();
            return true;
        } catch (Exception e) {
            log.warn("ZIP文件结构验证失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 估算ZIP解压结果
     */
    private EstimationResult estimateZipExtraction(String filePath) {
        List<String> securityIssues = new ArrayList<>();
        long estimatedSize = 0;
        int estimatedFileCount = 0;
        boolean isCritical = false;

        try (ZipFile zipFile = new ZipFile(filePath)) {
            var entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 检查路径遍历攻击
                if (entryName.contains("..") || entryName.startsWith("/")) {
                    securityIssues.add("检测到路径遍历攻击: " + entryName);
                    isCritical = true;
                }

                // 检查文件名长度
                if (entryName.length() > 255) {
                    securityIssues.add("文件名过长: " + entryName);
                }

                // 累计大小和数量
                estimatedSize += entry.getSize();
                estimatedFileCount++;

                // 检查单个文件大小
                if (entry.getSize() > 100 * 1024 * 1024) { // 100MB
                    securityIssues.add("单个文件过大: " + entryName + " (" + entry.getSize() + " bytes)");
                }
            }

        } catch (Exception e) {
            log.warn("估算ZIP解压失败: {}", filePath, e);
            securityIssues.add("无法读取ZIP文件结构");
            isCritical = true;
        }

        return new EstimationResult(estimatedSize, estimatedFileCount, securityIssues, isCritical);
    }

    /**
     * 检查磁盘空间
     */
    private boolean hasEnoughDiskSpace(long requiredSpace) {
        try {
            File uploadDir = new File(fileUploadConfig.getUploadPath());
            long freeSpace = uploadDir.getFreeSpace();
            // 保留10%的缓冲空间
            return freeSpace > requiredSpace * 1.1;
        } catch (Exception e) {
            log.warn("检查磁盘空间失败", e);
            return false;
        }
    }

    /**
     * 安全解压ZIP文件
     */
    private ExtractionResult safeExtractZipFile(String filePath, String extractPath, int maxFiles, long maxSize) {
        List<String> extractedFiles = new ArrayList<>();
        long totalSize = 0;
        int fileCount = 0;

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(filePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // 安全检查
                if (fileCount >= maxFiles) {
                    return new ExtractionResult(false, null, 0, 0,
                        "解压文件数量超过限制: " + maxFiles, extractedFiles);
                }

                if (totalSize >= maxSize) {
                    return new ExtractionResult(false, null, 0, 0,
                        "解压文件大小超过限制: " + maxSize, extractedFiles);
                }

                // 防止路径遍历攻击
                String entryName = entry.getName();
                if (entryName.contains("..") || entryName.startsWith("/")) {
                    log.warn("跳过危险路径: {}", entryName);
                    continue;
                }

                Path targetPath = Paths.get(extractPath, entryName);

                // 确保目标路径在解压目录内
                if (!targetPath.normalize().startsWith(Paths.get(extractPath).normalize())) {
                    log.warn("跳过路径遍历攻击: {}", entryName);
                    continue;
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());

                    // 限制单个文件大小
                    long entrySize = entry.getSize();
                    if (entrySize > 100 * 1024 * 1024) { // 100MB
                        log.warn("跳过过大文件: {} ({}bytes)", entryName, entrySize);
                        continue;
                    }

                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    extractedFiles.add(entryName);
                    totalSize += Files.size(targetPath);
                    fileCount++;
                }

                zis.closeEntry();
            }

            return new ExtractionResult(true, extractPath, totalSize, fileCount, "安全解压成功", extractedFiles);

        } catch (Exception e) {
            log.error("安全解压ZIP文件失败: filePath={}", filePath, e);
            return new ExtractionResult(false, null, 0, 0, "解压失败: " + e.getMessage(), extractedFiles);
        }
    }

    /**
     * 解压后验证
     */
    private boolean postExtractionValidation(String extractedPath) {
        try {
            Path rootPath = Paths.get(extractedPath);

            // 检查解压路径是否存在
            if (!Files.exists(rootPath)) {
                return false;
            }

            // 检查是否有可执行文件
            try (Stream<Path> paths = Files.walk(rootPath)) {
                boolean hasExecutable = paths
                    .filter(Files::isRegularFile)
                    .anyMatch(this::isExecutableFile);

                if (hasExecutable) {
                    log.warn("解压后发现可执行文件: {}", extractedPath);
                    // 可以选择是否允许可执行文件
                }
            }

            return true;
        } catch (Exception e) {
            log.error("解压后验证失败: {}", extractedPath, e);
            return false;
        }
    }

    /**
     * 检查是否为可执行文件
     */
    private boolean isExecutableFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".exe") || fileName.endsWith(".bat") ||
               fileName.endsWith(".cmd") || fileName.endsWith(".sh") ||
               fileName.endsWith(".com") || fileName.endsWith(".scr");
    }

    /**
     * 解压估算结果内部类
     */
    private static class EstimationResult {
        private final long estimatedSize;
        private final int estimatedFileCount;
        private final List<String> securityIssues;
        private final boolean isCritical;

        public EstimationResult(long estimatedSize, int estimatedFileCount,
                              List<String> securityIssues, boolean isCritical) {
            this.estimatedSize = estimatedSize;
            this.estimatedFileCount = estimatedFileCount;
            this.securityIssues = securityIssues;
            this.isCritical = isCritical;
        }

        public long getEstimatedSize() { return estimatedSize; }
        public int getEstimatedFileCount() { return estimatedFileCount; }
        public List<String> getSecurityIssues() { return securityIssues; }
        public boolean hasSecurityIssues() { return !securityIssues.isEmpty(); }
        public boolean isCritical() { return isCritical; }
    }

    @Override
    public QualityAssessmentResult performQualityAssessment(String extractedPath) {
        log.info("开始执行全面项目质量评估: path={}", extractedPath);

        try {
            // 1. 代码质量检查
            CodeQualityCheck codeQuality = checkCodeQuality(extractedPath);

            // 2. 文档检查
            DocumentationCheck documentation = checkDocumentation(extractedPath);

            // 3. 配置检查
            ConfigurationCheck configuration = checkConfiguration(extractedPath);

            // 4. 项目完整性检查
            ProjectIntegrityCheck integrity = checkProjectIntegrity(extractedPath);

            // 5. 计算综合评分
            int overallScore = calculateOverallScore(codeQuality, documentation, configuration, integrity);

            // 6. 确定质量等级
            String qualityLevel = determineQualityLevel(overallScore);

            // 7. 生成建议和关键问题
            List<String> recommendations = generateRecommendations(codeQuality, documentation, configuration, integrity);
            List<String> criticalIssues = identifyCriticalIssues(codeQuality, documentation, configuration, integrity);

            return new QualityAssessmentResult(overallScore, qualityLevel, codeQuality,
                documentation, configuration, integrity, recommendations, criticalIssues);

        } catch (Exception e) {
            log.error("执行项目质量评估时发生异常: path={}", extractedPath, e);
            // 返回默认的低质量评估结果
            return createDefaultQualityAssessment(e.getMessage());
        }
    }

    @Override
    public CodeQualityCheck checkCodeQuality(String extractedPath) {
        log.info("开始检查代码质量: path={}", extractedPath);

        try {
            Path rootPath = Paths.get(extractedPath);
            Map<String, Integer> languageStats = new HashMap<>();
            List<String> codeIssues = new ArrayList<>();
            List<String> bestPractices = new ArrayList<>();
            int codeScore = 100;

            // 1. 统计编程语言
            analyzeLanguageDistribution(rootPath, languageStats);

            // 2. 检查代码结构
            if (!hasGoodCodeStructure(rootPath)) {
                codeIssues.add("代码结构不规范");
                codeScore -= 15;
            }

            // 3. 检查命名规范
            if (!hasGoodNamingConventions(rootPath)) {
                codeIssues.add("命名规范不一致");
                codeScore -= 10;
            }

            // 4. 检查代码复杂度
            int complexity = calculateCodeComplexity(rootPath);
            if (complexity > 80) {
                codeIssues.add("代码复杂度过高");
                codeScore -= 20;
            }

            // 5. 检查可维护性
            int maintainability = calculateMaintainability(rootPath);
            if (maintainability < 60) {
                codeIssues.add("代码可维护性较低");
                codeScore -= 15;
            }

            // 6. 检查测试覆盖率
            boolean hasTests = hasTestFiles(rootPath);
            double testCoverage = estimateTestCoverage(rootPath);

            if (!hasTests) {
                codeIssues.add("缺少测试文件");
                codeScore -= 25;
            } else if (testCoverage < 50) {
                codeIssues.add("测试覆盖率较低");
                codeScore -= 15;
            }

            // 7. 检查最佳实践
            checkBestPractices(rootPath, bestPractices);

            codeScore = Math.max(0, codeScore);

            return new CodeQualityCheck(codeScore, languageStats, codeIssues, bestPractices,
                complexity, maintainability, hasTests, testCoverage);

        } catch (Exception e) {
            log.error("检查代码质量时发生异常: path={}", extractedPath, e);
            return new CodeQualityCheck(0, new HashMap<>(),
                Arrays.asList("代码质量检查失败: " + e.getMessage()),
                new ArrayList<>(), 100, 0, false, 0.0);
        }
    }

    @Override
    public DocumentationCheck checkDocumentation(String extractedPath) {
        log.info("开始检查文档完整性: path={}", extractedPath);

        try {
            Path rootPath = Paths.get(extractedPath);
            List<String> missingDocs = new ArrayList<>();
            List<String> documentationIssues = new ArrayList<>();
            int documentationScore = 100;

            // 1. 检查基本文档文件
            boolean hasReadme = hasReadmeFile(rootPath);
            boolean hasLicense = hasLicenseFile(rootPath);
            boolean hasChangelog = hasChangelogFile(rootPath);
            boolean hasContributing = hasContributingFile(rootPath);
            boolean hasApiDocs = hasApiDocumentation(rootPath);

            if (!hasReadme) {
                missingDocs.add("README文件");
                documentationScore -= 30;
            }

            if (!hasLicense) {
                missingDocs.add("LICENSE文件");
                documentationScore -= 15;
            }

            if (!hasChangelog) {
                missingDocs.add("CHANGELOG文件");
                documentationScore -= 10;
            }

            if (!hasContributing) {
                missingDocs.add("CONTRIBUTING文件");
                documentationScore -= 10;
            }

            if (!hasApiDocs) {
                missingDocs.add("API文档");
                documentationScore -= 15;
            }

            // 2. 评估README质量
            String readmeQuality = evaluateReadmeQuality(rootPath);
            if ("POOR".equals(readmeQuality)) {
                documentationIssues.add("README内容质量较低");
                documentationScore -= 20;
            } else if ("FAIR".equals(readmeQuality)) {
                documentationIssues.add("README内容可以改进");
                documentationScore -= 10;
            }

            // 3. 检查文档一致性
            if (!hasConsistentDocumentation(rootPath)) {
                documentationIssues.add("文档内容不一致");
                documentationScore -= 10;
            }

            documentationScore = Math.max(0, documentationScore);

            return new DocumentationCheck(documentationScore, hasReadme, hasLicense,
                hasChangelog, hasContributing, hasApiDocs, missingDocs,
                documentationIssues, readmeQuality);

        } catch (Exception e) {
            log.error("检查文档完整性时发生异常: path={}", extractedPath, e);
            return new DocumentationCheck(0, false, false, false, false, false,
                Arrays.asList("文档检查失败"), Arrays.asList(e.getMessage()), "UNKNOWN");
        }
    }

    @Override
    public ConfigurationCheck checkConfiguration(String extractedPath) {
        log.info("开始检查项目配置: path={}", extractedPath);

        try {
            Path rootPath = Paths.get(extractedPath);
            Map<String, Boolean> configFiles = new HashMap<>();
            List<String> configIssues = new ArrayList<>();
            List<String> securityConfigIssues = new ArrayList<>();
            int configScore = 100;

            // 1. 检查构建配置文件
            boolean hasValidBuildConfig = false;
            String buildTool = "UNKNOWN";

            // Maven
            if (Files.exists(rootPath.resolve("pom.xml"))) {
                configFiles.put("pom.xml", true);
                hasValidBuildConfig = true;
                buildTool = "Maven";
            }

            // Gradle
            if (Files.exists(rootPath.resolve("build.gradle")) || Files.exists(rootPath.resolve("build.gradle.kts"))) {
                configFiles.put("build.gradle", true);
                hasValidBuildConfig = true;
                buildTool = "Gradle";
            }

            // NPM
            if (Files.exists(rootPath.resolve("package.json"))) {
                configFiles.put("package.json", true);
                hasValidBuildConfig = true;
                buildTool = "NPM";
            }

            // 其他构建工具
            if (Files.exists(rootPath.resolve("Makefile"))) {
                configFiles.put("Makefile", true);
                if (!hasValidBuildConfig) {
                    hasValidBuildConfig = true;
                    buildTool = "Make";
                }
            }

            if (!hasValidBuildConfig) {
                configIssues.add("缺少构建配置文件");
                configScore -= 30;
            }

            // 2. 检查依赖管理
            boolean hasDependencyManagement = checkDependencyManagement(rootPath, buildTool);
            if (!hasDependencyManagement) {
                configIssues.add("依赖管理配置不完整");
                configScore -= 20;
            }

            // 3. 检查环境配置
            boolean hasEnvironmentConfig = checkEnvironmentConfig(rootPath);
            configFiles.put("environment_config", hasEnvironmentConfig);
            if (!hasEnvironmentConfig) {
                configIssues.add("缺少环境配置文件");
                configScore -= 15;
            }

            // 4. 检查安全配置
            checkSecurityConfiguration(rootPath, securityConfigIssues);
            if (!securityConfigIssues.isEmpty()) {
                configScore -= securityConfigIssues.size() * 10;
            }

            // 5. 检查其他重要配置文件
            checkOtherConfigFiles(rootPath, configFiles, configIssues);

            configScore = Math.max(0, configScore);

            return new ConfigurationCheck(configScore, configFiles, configIssues,
                hasValidBuildConfig, hasDependencyManagement, hasEnvironmentConfig,
                securityConfigIssues, buildTool);

        } catch (Exception e) {
            log.error("检查项目配置时发生异常: path={}", extractedPath, e);
            return new ConfigurationCheck(0, new HashMap<>(),
                Arrays.asList("配置检查失败: " + e.getMessage()),
                false, false, false, Arrays.asList("检查异常"), "UNKNOWN");
        }
    }

    // ==================== 质量评估辅助方法 ====================

    /**
     * 计算综合评分
     */
    private int calculateOverallScore(CodeQualityCheck codeQuality, DocumentationCheck documentation,
                                    ConfigurationCheck configuration, ProjectIntegrityCheck integrity) {
        // 权重分配：代码质量40%，文档20%，配置20%，完整性20%
        double score = codeQuality.getCodeScore() * 0.4 +
                      documentation.getDocumentationScore() * 0.2 +
                      configuration.getConfigScore() * 0.2 +
                      integrity.getQualityScore() * 0.2;

        return (int) Math.round(score);
    }

    /**
     * 确定质量等级
     */
    private String determineQualityLevel(int overallScore) {
        if (overallScore >= 90) {
            return "EXCELLENT";
        } else if (overallScore >= 80) {
            return "GOOD";
        } else if (overallScore >= 70) {
            return "FAIR";
        } else if (overallScore >= 60) {
            return "POOR";
        } else {
            return "VERY_POOR";
        }
    }

    /**
     * 生成改进建议
     */
    private List<String> generateRecommendations(CodeQualityCheck codeQuality, DocumentationCheck documentation,
                                               ConfigurationCheck configuration, ProjectIntegrityCheck integrity) {
        List<String> recommendations = new ArrayList<>();

        // 代码质量建议
        if (codeQuality.getCodeScore() < 80) {
            recommendations.add("改进代码结构和命名规范");
        }
        if (!codeQuality.hasTests()) {
            recommendations.add("添加单元测试和集成测试");
        }
        if (codeQuality.getComplexity() > 70) {
            recommendations.add("降低代码复杂度，拆分大型函数和类");
        }

        // 文档建议
        if (!documentation.hasReadme()) {
            recommendations.add("添加详细的README文件");
        }
        if (!documentation.hasLicense()) {
            recommendations.add("添加开源许可证文件");
        }
        if (documentation.getDocumentationScore() < 70) {
            recommendations.add("完善项目文档和API文档");
        }

        // 配置建议
        if (!configuration.hasValidBuildConfig()) {
            recommendations.add("添加标准的构建配置文件");
        }
        if (!configuration.hasDependencyManagement()) {
            recommendations.add("完善依赖管理配置");
        }
        if (!configuration.getSecurityConfigIssues().isEmpty()) {
            recommendations.add("修复安全配置问题");
        }

        // 完整性建议
        if (integrity.getQualityScore() < 80) {
            recommendations.add("完善项目结构和基础文件");
        }

        return recommendations;
    }

    /**
     * 识别关键问题
     */
    private List<String> identifyCriticalIssues(CodeQualityCheck codeQuality, DocumentationCheck documentation,
                                               ConfigurationCheck configuration, ProjectIntegrityCheck integrity) {
        List<String> criticalIssues = new ArrayList<>();

        // 代码关键问题
        if (codeQuality.getCodeScore() < 50) {
            criticalIssues.add("代码质量严重不达标");
        }
        if (codeQuality.getComplexity() > 90) {
            criticalIssues.add("代码复杂度过高，难以维护");
        }

        // 安全关键问题
        if (!configuration.getSecurityConfigIssues().isEmpty()) {
            criticalIssues.addAll(configuration.getSecurityConfigIssues());
        }

        // 构建关键问题
        if (!configuration.hasValidBuildConfig()) {
            criticalIssues.add("缺少构建配置，无法正常构建项目");
        }

        return criticalIssues;
    }

    /**
     * 创建默认质量评估结果
     */
    private QualityAssessmentResult createDefaultQualityAssessment(String errorMessage) {
        CodeQualityCheck codeQuality = new CodeQualityCheck(0, new HashMap<>(),
            Arrays.asList("代码质量检查失败"), new ArrayList<>(), 100, 0, false, 0.0);

        DocumentationCheck documentation = new DocumentationCheck(0, false, false, false, false, false,
            Arrays.asList("文档检查失败"), Arrays.asList(errorMessage), "UNKNOWN");

        ConfigurationCheck configuration = new ConfigurationCheck(0, new HashMap<>(),
            Arrays.asList("配置检查失败"), false, false, false, Arrays.asList(errorMessage), "UNKNOWN");

        ProjectIntegrityCheck integrity = new ProjectIntegrityCheck(false,
            Arrays.asList("完整性检查失败"), Arrays.asList(errorMessage), 0);

        return new QualityAssessmentResult(0, "VERY_POOR", codeQuality, documentation,
            configuration, integrity, Arrays.asList("修复系统错误后重新评估"),
            Arrays.asList("系统错误: " + errorMessage));
    }

    /**
     * 分析编程语言分布
     */
    private void analyzeLanguageDistribution(Path rootPath, Map<String, Integer> languageStats) {
        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                 .forEach(path -> {
                     String fileName = path.getFileName().toString().toLowerCase();
                     String language = detectLanguage(fileName);
                     if (language != null) {
                         languageStats.merge(language, 1, Integer::sum);
                     }
                 });
        } catch (IOException e) {
            log.warn("分析编程语言分布失败", e);
        }
    }

    /**
     * 检测编程语言
     */
    private String detectLanguage(String fileName) {
        if (fileName.endsWith(".java")) return "Java";
        if (fileName.endsWith(".js") || fileName.endsWith(".ts")) return "JavaScript/TypeScript";
        if (fileName.endsWith(".py")) return "Python";
        if (fileName.endsWith(".cpp") || fileName.endsWith(".c") || fileName.endsWith(".h")) return "C/C++";
        if (fileName.endsWith(".cs")) return "C#";
        if (fileName.endsWith(".php")) return "PHP";
        if (fileName.endsWith(".rb")) return "Ruby";
        if (fileName.endsWith(".go")) return "Go";
        if (fileName.endsWith(".rs")) return "Rust";
        if (fileName.endsWith(".kt")) return "Kotlin";
        if (fileName.endsWith(".swift")) return "Swift";
        if (fileName.endsWith(".html") || fileName.endsWith(".css")) return "Web";
        if (fileName.endsWith(".sql")) return "SQL";
        if (fileName.endsWith(".sh") || fileName.endsWith(".bash")) return "Shell";
        return null;
    }

    /**
     * 检查代码结构
     */
    private boolean hasGoodCodeStructure(Path rootPath) {
        try {
            // 检查是否有合理的目录结构
            boolean hasSourceDir = Files.exists(rootPath.resolve("src")) ||
                                  Files.exists(rootPath.resolve("lib")) ||
                                  Files.exists(rootPath.resolve("app"));

            // 检查文件组织
            long totalFiles = Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .count();

            long rootLevelFiles = Files.list(rootPath)
                .filter(Files::isRegularFile)
                .count();

            // 如果根目录文件过多，认为结构不好
            return hasSourceDir && (rootLevelFiles < totalFiles * 0.3);

        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查命名规范
     */
    private boolean hasGoodNamingConventions(Path rootPath) {
        try (Stream<Path> paths = Files.walk(rootPath)) {
            long totalFiles = paths.filter(Files::isRegularFile).count();

            try (Stream<Path> paths2 = Files.walk(rootPath)) {
                long goodNamedFiles = paths2.filter(Files::isRegularFile)
                    .filter(this::hasGoodFileName)
                    .count();

                return goodNamedFiles > totalFiles * 0.8; // 80%以上文件命名规范
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查文件名是否规范
     */
    private boolean hasGoodFileName(Path filePath) {
        String fileName = filePath.getFileName().toString();

        // 检查是否包含特殊字符
        if (fileName.matches(".*[\\s\\u4e00-\\u9fa5].*")) {
            return false; // 包含空格或中文
        }

        // 检查命名风格一致性
        boolean isCamelCase = fileName.matches("^[a-z][a-zA-Z0-9]*\\.[a-z]+$");
        boolean isSnakeCase = fileName.matches("^[a-z][a-z0-9_]*\\.[a-z]+$");
        boolean isKebabCase = fileName.matches("^[a-z][a-z0-9-]*\\.[a-z]+$");

        return isCamelCase || isSnakeCase || isKebabCase;
    }

    /**
     * 计算代码复杂度
     */
    private int calculateCodeComplexity(Path rootPath) {
        // 简化的复杂度计算，基于文件数量和嵌套深度
        try (Stream<Path> paths = Files.walk(rootPath)) {
            long fileCount = paths.filter(Files::isRegularFile).count();
            int maxDepth = getMaxDirectoryDepth(rootPath);

            // 复杂度 = 文件数量 + 目录深度 * 10
            return (int) (fileCount + maxDepth * 10);

        } catch (IOException e) {
            return 100; // 默认高复杂度
        }
    }

    /**
     * 获取最大目录深度
     */
    private int getMaxDirectoryDepth(Path rootPath) {
        try (Stream<Path> paths = Files.walk(rootPath)) {
            return paths.mapToInt(path -> path.getNameCount() - rootPath.getNameCount())
                       .max()
                       .orElse(0);
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * 计算可维护性
     */
    private int calculateMaintainability(Path rootPath) {
        int score = 100;

        try {
            // 基于文件大小分布计算可维护性
            try (Stream<Path> paths = Files.walk(rootPath)) {
                List<Long> fileSizes = paths.filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .boxed()
                    .collect(Collectors.toList());

                // 如果有过大的文件，降低可维护性分数
                long largeFiles = fileSizes.stream()
                    .filter(size -> size > 100 * 1024) // 大于100KB
                    .count();

                score -= largeFiles * 5;
            }

        } catch (IOException e) {
            score = 50; // 默认中等可维护性
        }

        return Math.max(0, score);
    }

    /**
     * 检查是否有测试文件
     */
    private boolean hasTestFiles(Path rootPath) {
        try (Stream<Path> paths = Files.walk(rootPath)) {
            return paths.filter(Files::isRegularFile)
                       .anyMatch(this::isTestFile);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 判断是否为测试文件
     */
    private boolean isTestFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        String pathStr = filePath.toString().toLowerCase();

        return fileName.contains("test") ||
               fileName.contains("spec") ||
               pathStr.contains("/test/") ||
               pathStr.contains("/tests/") ||
               pathStr.contains("/spec/") ||
               pathStr.contains("__tests__");
    }

    /**
     * 估算测试覆盖率
     */
    private double estimateTestCoverage(Path rootPath) {
        try (Stream<Path> paths = Files.walk(rootPath)) {
            long totalCodeFiles = paths.filter(Files::isRegularFile)
                                     .filter(this::isCodeFile)
                                     .count();

            try (Stream<Path> paths2 = Files.walk(rootPath)) {
                long testFiles = paths2.filter(Files::isRegularFile)
                                      .filter(this::isTestFile)
                                      .count();

                if (totalCodeFiles == 0) return 0.0;

                // 简化的覆盖率估算：测试文件数 / 代码文件数 * 100
                return Math.min(100.0, (double) testFiles / totalCodeFiles * 100);
            }
        } catch (IOException e) {
            return 0.0;
        }
    }

    /**
     * 判断是否为代码文件
     */
    private boolean isCodeFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".java") || fileName.endsWith(".js") || fileName.endsWith(".ts") ||
               fileName.endsWith(".py") || fileName.endsWith(".cpp") || fileName.endsWith(".c") ||
               fileName.endsWith(".cs") || fileName.endsWith(".php") || fileName.endsWith(".rb") ||
               fileName.endsWith(".go") || fileName.endsWith(".rs") || fileName.endsWith(".kt");
    }

    /**
     * 检查最佳实践
     */
    private void checkBestPractices(Path rootPath, List<String> bestPractices) {
        // 检查是否有版本控制
        if (Files.exists(rootPath.resolve(".git"))) {
            bestPractices.add("使用Git版本控制");
        }

        // 检查是否有CI配置
        if (Files.exists(rootPath.resolve(".github/workflows")) ||
            Files.exists(rootPath.resolve(".gitlab-ci.yml")) ||
            Files.exists(rootPath.resolve("Jenkinsfile"))) {
            bestPractices.add("配置了持续集成");
        }

        // 检查是否有代码格式化配置
        if (Files.exists(rootPath.resolve(".editorconfig")) ||
            Files.exists(rootPath.resolve(".prettierrc")) ||
            Files.exists(rootPath.resolve(".eslintrc"))) {
            bestPractices.add("配置了代码格式化");
        }

        // 检查是否有依赖锁定文件
        if (Files.exists(rootPath.resolve("package-lock.json")) ||
            Files.exists(rootPath.resolve("yarn.lock")) ||
            Files.exists(rootPath.resolve("Pipfile.lock"))) {
            bestPractices.add("锁定了依赖版本");
        }
    }

    /**
     * 检查是否有变更日志文件
     */
    private boolean hasChangelogFile(Path rootPath) {
        return Files.exists(rootPath.resolve("CHANGELOG.md")) ||
               Files.exists(rootPath.resolve("CHANGELOG.txt")) ||
               Files.exists(rootPath.resolve("HISTORY.md")) ||
               Files.exists(rootPath.resolve("RELEASES.md"));
    }

    /**
     * 检查是否有贡献指南文件
     */
    private boolean hasContributingFile(Path rootPath) {
        return Files.exists(rootPath.resolve("CONTRIBUTING.md")) ||
               Files.exists(rootPath.resolve("CONTRIBUTING.txt")) ||
               Files.exists(rootPath.resolve(".github/CONTRIBUTING.md"));
    }

    /**
     * 检查是否有API文档
     */
    private boolean hasApiDocumentation(Path rootPath) {
        return Files.exists(rootPath.resolve("docs")) ||
               Files.exists(rootPath.resolve("doc")) ||
               Files.exists(rootPath.resolve("documentation")) ||
               Files.exists(rootPath.resolve("api-docs"));
    }

    /**
     * 评估README质量
     */
    private String evaluateReadmeQuality(Path rootPath) {
        try {
            Path readmePath = null;
            if (Files.exists(rootPath.resolve("README.md"))) {
                readmePath = rootPath.resolve("README.md");
            } else if (Files.exists(rootPath.resolve("README.txt"))) {
                readmePath = rootPath.resolve("README.txt");
            } else if (Files.exists(rootPath.resolve("README"))) {
                readmePath = rootPath.resolve("README");
            }

            if (readmePath == null) {
                return "NONE";
            }

            String content = Files.readString(readmePath);
            int score = 0;

            // 检查内容长度
            if (content.length() > 500) score += 20;
            if (content.length() > 1000) score += 10;

            // 检查关键部分
            if (content.toLowerCase().contains("installation")) score += 15;
            if (content.toLowerCase().contains("usage")) score += 15;
            if (content.toLowerCase().contains("example")) score += 10;
            if (content.toLowerCase().contains("license")) score += 10;
            if (content.toLowerCase().contains("contribute")) score += 10;
            if (content.toLowerCase().contains("api")) score += 10;

            // 检查格式
            if (content.contains("#")) score += 10; // 有标题

            if (score >= 80) return "EXCELLENT";
            if (score >= 60) return "GOOD";
            if (score >= 40) return "FAIR";
            if (score >= 20) return "POOR";
            return "VERY_POOR";

        } catch (IOException e) {
            return "UNKNOWN";
        }
    }

    /**
     * 检查文档一致性
     */
    private boolean hasConsistentDocumentation(Path rootPath) {
        // 简化检查：如果有多个文档文件，检查它们是否都存在
        int docCount = 0;
        if (hasReadmeFile(rootPath)) docCount++;
        if (hasLicenseFile(rootPath)) docCount++;
        if (hasChangelogFile(rootPath)) docCount++;
        if (hasContributingFile(rootPath)) docCount++;

        // 如果有3个以上文档文件，认为文档比较完整一致
        return docCount >= 3;
    }

    /**
     * 检查依赖管理
     */
    private boolean checkDependencyManagement(Path rootPath, String buildTool) {
        switch (buildTool) {
            case "Maven":
                return checkMavenDependencies(rootPath);
            case "Gradle":
                return checkGradleDependencies(rootPath);
            case "NPM":
                return checkNpmDependencies(rootPath);
            default:
                return false;
        }
    }

    /**
     * 检查Maven依赖
     */
    private boolean checkMavenDependencies(Path rootPath) {
        try {
            Path pomPath = rootPath.resolve("pom.xml");
            if (!Files.exists(pomPath)) return false;

            String content = Files.readString(pomPath);
            return content.contains("<dependencies>") && content.contains("<dependency>");
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查Gradle依赖
     */
    private boolean checkGradleDependencies(Path rootPath) {
        try {
            Path gradlePath = rootPath.resolve("build.gradle");
            if (!Files.exists(gradlePath)) {
                gradlePath = rootPath.resolve("build.gradle.kts");
            }
            if (!Files.exists(gradlePath)) return false;

            String content = Files.readString(gradlePath);
            return content.contains("dependencies") &&
                   (content.contains("implementation") || content.contains("compile"));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查NPM依赖
     */
    private boolean checkNpmDependencies(Path rootPath) {
        try {
            Path packagePath = rootPath.resolve("package.json");
            if (!Files.exists(packagePath)) return false;

            String content = Files.readString(packagePath);
            return content.contains("\"dependencies\"") || content.contains("\"devDependencies\"");
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查环境配置
     */
    private boolean checkEnvironmentConfig(Path rootPath) {
        return Files.exists(rootPath.resolve(".env")) ||
               Files.exists(rootPath.resolve(".env.example")) ||
               Files.exists(rootPath.resolve("config")) ||
               Files.exists(rootPath.resolve("application.properties")) ||
               Files.exists(rootPath.resolve("application.yml")) ||
               Files.exists(rootPath.resolve("application.yaml"));
    }

    /**
     * 检查安全配置
     */
    private void checkSecurityConfiguration(Path rootPath, List<String> securityIssues) {
        // 检查是否有敏感文件
        if (Files.exists(rootPath.resolve(".env")) && !Files.exists(rootPath.resolve(".env.example"))) {
            securityIssues.add("包含.env文件但缺少.env.example模板");
        }

        // 检查是否有.gitignore
        if (!Files.exists(rootPath.resolve(".gitignore"))) {
            securityIssues.add("缺少.gitignore文件");
        } else {
            try {
                String gitignoreContent = Files.readString(rootPath.resolve(".gitignore"));
                if (!gitignoreContent.contains(".env")) {
                    securityIssues.add(".gitignore未忽略环境配置文件");
                }
                if (!gitignoreContent.contains("node_modules") && Files.exists(rootPath.resolve("package.json"))) {
                    securityIssues.add(".gitignore未忽略node_modules");
                }
            } catch (IOException e) {
                securityIssues.add("无法读取.gitignore文件");
            }
        }

        // 检查是否有密钥文件
        try (Stream<Path> paths = Files.walk(rootPath)) {
            boolean hasKeyFiles = paths.filter(Files::isRegularFile)
                .anyMatch(path -> {
                    String fileName = path.getFileName().toString().toLowerCase();
                    return fileName.contains("key") || fileName.contains("secret") ||
                           fileName.contains("password") || fileName.endsWith(".pem") ||
                           fileName.endsWith(".p12") || fileName.endsWith(".jks");
                });

            if (hasKeyFiles) {
                securityIssues.add("检测到可能的密钥或证书文件");
            }
        } catch (IOException e) {
            securityIssues.add("安全检查过程中发生错误");
        }
    }

    /**
     * 检查其他配置文件
     */
    private void checkOtherConfigFiles(Path rootPath, Map<String, Boolean> configFiles, List<String> configIssues) {
        // Docker配置
        configFiles.put("Dockerfile", Files.exists(rootPath.resolve("Dockerfile")));
        configFiles.put("docker-compose.yml", Files.exists(rootPath.resolve("docker-compose.yml")));

        // CI/CD配置
        configFiles.put("github_actions", Files.exists(rootPath.resolve(".github/workflows")));
        configFiles.put("gitlab_ci", Files.exists(rootPath.resolve(".gitlab-ci.yml")));
        configFiles.put("jenkins", Files.exists(rootPath.resolve("Jenkinsfile")));

        // 代码质量配置
        configFiles.put("eslint", Files.exists(rootPath.resolve(".eslintrc")) ||
                                 Files.exists(rootPath.resolve(".eslintrc.json")));
        configFiles.put("prettier", Files.exists(rootPath.resolve(".prettierrc")));
        configFiles.put("editorconfig", Files.exists(rootPath.resolve(".editorconfig")));

        // 检查配置完整性
        if (!configFiles.get("Dockerfile") && !configFiles.get("docker-compose.yml")) {
            configIssues.add("缺少Docker配置，不利于部署");
        }

        if (!configFiles.get("github_actions") && !configFiles.get("gitlab_ci") && !configFiles.get("jenkins")) {
            configIssues.add("缺少CI/CD配置");
        }
    }
}