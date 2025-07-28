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
}