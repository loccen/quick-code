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
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
}