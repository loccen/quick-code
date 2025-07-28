package com.quickcode.service;

import com.quickcode.entity.ProjectFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 文件解压和结构分析服务接口
 * 提供压缩文件解压、项目结构分析等功能
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface FileExtractionService {

    /**
     * 解压项目文件
     * 
     * @param projectFile 项目文件
     * @return 解压结果
     */
    ExtractionResult extractProjectFile(ProjectFile projectFile);

    /**
     * 分析项目结构
     * 
     * @param extractedPath 解压后的路径
     * @return 项目结构分析结果
     */
    ProjectStructureAnalysis analyzeProjectStructure(String extractedPath);

    /**
     * 检测Docker配置
     * 
     * @param extractedPath 解压后的路径
     * @return Docker配置检测结果
     */
    DockerConfigDetection detectDockerConfig(String extractedPath);

    /**
     * 验证项目完整性
     * 
     * @param extractedPath 解压后的路径
     * @return 完整性验证结果
     */
    ProjectIntegrityCheck checkProjectIntegrity(String extractedPath);

    /**
     * 清理解压文件
     *
     * @param extractedPath 解压后的路径
     * @return 是否清理成功
     */
    boolean cleanupExtractedFiles(String extractedPath);

    /**
     * 验证压缩文件完整性
     *
     * @param projectFile 项目文件
     * @return 验证结果
     */
    ValidationResult validateArchiveIntegrity(ProjectFile projectFile);

    /**
     * 预检查解压操作
     *
     * @param projectFile 项目文件
     * @return 预检查结果
     */
    PreExtractionCheck preCheckExtraction(ProjectFile projectFile);

    /**
     * 安全解压文件
     *
     * @param projectFile 项目文件
     * @param maxFiles 最大文件数量
     * @param maxSize 最大解压大小
     * @return 解压结果
     */
    ExtractionResult safeExtractProjectFile(ProjectFile projectFile, int maxFiles, long maxSize);

    /**
     * 解压结果
     */
    class ExtractionResult {
        private final boolean success;
        private final String extractedPath;
        private final long extractedSize;
        private final int fileCount;
        private final String message;
        private final List<String> extractedFiles;

        public ExtractionResult(boolean success, String extractedPath, long extractedSize, 
                              int fileCount, String message, List<String> extractedFiles) {
            this.success = success;
            this.extractedPath = extractedPath;
            this.extractedSize = extractedSize;
            this.fileCount = fileCount;
            this.message = message;
            this.extractedFiles = extractedFiles;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getExtractedPath() { return extractedPath; }
        public long getExtractedSize() { return extractedSize; }
        public int getFileCount() { return fileCount; }
        public String getMessage() { return message; }
        public List<String> getExtractedFiles() { return extractedFiles; }
    }

    /**
     * 项目结构分析结果
     */
    class ProjectStructureAnalysis {
        private final ProjectType projectType;
        private final List<String> techStack;
        private final Map<String, Integer> fileTypeCount;
        private final boolean hasReadme;
        private final boolean hasLicense;
        private final String mainEntryPoint;
        private final List<String> configFiles;

        public ProjectStructureAnalysis(ProjectType projectType, List<String> techStack,
                                      Map<String, Integer> fileTypeCount, boolean hasReadme,
                                      boolean hasLicense, String mainEntryPoint, List<String> configFiles) {
            this.projectType = projectType;
            this.techStack = techStack;
            this.fileTypeCount = fileTypeCount;
            this.hasReadme = hasReadme;
            this.hasLicense = hasLicense;
            this.mainEntryPoint = mainEntryPoint;
            this.configFiles = configFiles;
        }

        // Getters
        public ProjectType getProjectType() { return projectType; }
        public List<String> getTechStack() { return techStack; }
        public Map<String, Integer> getFileTypeCount() { return fileTypeCount; }
        public boolean hasReadme() { return hasReadme; }
        public boolean hasLicense() { return hasLicense; }
        public String getMainEntryPoint() { return mainEntryPoint; }
        public List<String> getConfigFiles() { return configFiles; }
    }

    /**
     * Docker配置检测结果
     */
    class DockerConfigDetection {
        private final boolean hasDockerfile;
        private final boolean hasDockerCompose;
        private final String dockerfileContent;
        private final String baseImage;
        private final List<String> exposedPorts;
        private final List<String> volumes;
        private final boolean isDockerized;

        public DockerConfigDetection(boolean hasDockerfile, boolean hasDockerCompose,
                                   String dockerfileContent, String baseImage,
                                   List<String> exposedPorts, List<String> volumes, boolean isDockerized) {
            this.hasDockerfile = hasDockerfile;
            this.hasDockerCompose = hasDockerCompose;
            this.dockerfileContent = dockerfileContent;
            this.baseImage = baseImage;
            this.exposedPorts = exposedPorts;
            this.volumes = volumes;
            this.isDockerized = isDockerized;
        }

        // Getters
        public boolean hasDockerfile() { return hasDockerfile; }
        public boolean hasDockerCompose() { return hasDockerCompose; }
        public String getDockerfileContent() { return dockerfileContent; }
        public String getBaseImage() { return baseImage; }
        public List<String> getExposedPorts() { return exposedPorts; }
        public List<String> getVolumes() { return volumes; }
        public boolean isDockerized() { return isDockerized; }
    }

    /**
     * 项目完整性检查结果
     */
    class ProjectIntegrityCheck {
        private final boolean isComplete;
        private final List<String> missingFiles;
        private final List<String> issues;
        private final int qualityScore;

        public ProjectIntegrityCheck(boolean isComplete, List<String> missingFiles,
                                   List<String> issues, int qualityScore) {
            this.isComplete = isComplete;
            this.missingFiles = missingFiles;
            this.issues = issues;
            this.qualityScore = qualityScore;
        }

        // Getters
        public boolean isComplete() { return isComplete; }
        public List<String> getMissingFiles() { return missingFiles; }
        public List<String> getIssues() { return issues; }
        public int getQualityScore() { return qualityScore; }
    }

    /**
     * 项目类型枚举
     */
    enum ProjectType {
        WEB_FRONTEND("WEB_FRONTEND", "前端项目"),
        WEB_BACKEND("WEB_BACKEND", "后端项目"),
        FULL_STACK("FULL_STACK", "全栈项目"),
        MOBILE_APP("MOBILE_APP", "移动应用"),
        DESKTOP_APP("DESKTOP_APP", "桌面应用"),
        LIBRARY("LIBRARY", "库/框架"),
        SCRIPT("SCRIPT", "脚本工具"),
        UNKNOWN("UNKNOWN", "未知类型");

        private final String code;
        private final String description;

        ProjectType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }

    /**
     * 压缩文件验证结果
     */
    class ValidationResult {
        private final boolean isValid;
        private final String message;
        private final List<String> issues;
        private final long actualSize;
        private final String checksum;

        public ValidationResult(boolean isValid, String message, List<String> issues,
                              long actualSize, String checksum) {
            this.isValid = isValid;
            this.message = message;
            this.issues = issues;
            this.actualSize = actualSize;
            this.checksum = checksum;
        }

        // Getters
        public boolean isValid() { return isValid; }
        public String getMessage() { return message; }
        public List<String> getIssues() { return issues; }
        public long getActualSize() { return actualSize; }
        public String getChecksum() { return checksum; }
    }

    /**
     * 解压预检查结果
     */
    class PreExtractionCheck {
        private final boolean canExtract;
        private final String reason;
        private final long estimatedSize;
        private final int estimatedFileCount;
        private final List<String> warnings;
        private final List<String> securityIssues;

        public PreExtractionCheck(boolean canExtract, String reason, long estimatedSize,
                                int estimatedFileCount, List<String> warnings, List<String> securityIssues) {
            this.canExtract = canExtract;
            this.reason = reason;
            this.estimatedSize = estimatedSize;
            this.estimatedFileCount = estimatedFileCount;
            this.warnings = warnings;
            this.securityIssues = securityIssues;
        }

        // Getters
        public boolean canExtract() { return canExtract; }
        public String getReason() { return reason; }
        public long getEstimatedSize() { return estimatedSize; }
        public int getEstimatedFileCount() { return estimatedFileCount; }
        public List<String> getWarnings() { return warnings; }
        public List<String> getSecurityIssues() { return securityIssues; }
    }
}
