package com.quickcode.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件安全检查服务接口
 * 提供文件上传安全检查功能
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface FileSecurityService {

    /**
     * 执行完整的文件安全检查
     * 
     * @param file 要检查的文件
     * @param fileType 文件类型
     * @return 安全检查结果
     */
    SecurityCheckResult performSecurityCheck(MultipartFile file, String fileType);

    /**
     * 验证文件类型
     * 
     * @param file 文件
     * @param fileType 文件类型
     * @return 是否通过验证
     */
    boolean validateFileType(MultipartFile file, String fileType);

    /**
     * 验证文件大小
     * 
     * @param file 文件
     * @return 是否通过验证
     */
    boolean validateFileSize(MultipartFile file);

    /**
     * 检查文件扩展名
     * 
     * @param fileName 文件名
     * @return 是否安全
     */
    boolean checkFileExtension(String fileName);

    /**
     * 检查MIME类型
     * 
     * @param contentType MIME类型
     * @return 是否安全
     */
    boolean checkMimeType(String contentType);

    /**
     * 检查文件内容
     * 
     * @param file 文件
     * @return 检查结果
     * @throws IOException 读取文件失败
     */
    ContentCheckResult checkFileContent(MultipartFile file) throws IOException;

    /**
     * 检查文件头部特征
     * 
     * @param fileBytes 文件字节数组
     * @return 是否安全
     */
    boolean checkFileSignature(byte[] fileBytes);

    /**
     * 检查敏感信息
     * 
     * @param content 文件内容
     * @return 敏感信息检查结果
     */
    SensitiveInfoCheckResult checkSensitiveInfo(String content);

    /**
     * 计算文件风险等级
     * 
     * @param file 文件
     * @param issues 发现的问题列表
     * @return 风险等级
     */
    RiskLevel calculateRiskLevel(MultipartFile file, List<String> issues);

    /**
     * 安全检查结果
     */
    class SecurityCheckResult {
        private final boolean isSafe;
        private final RiskLevel riskLevel;
        private final List<String> issues;
        private final String recommendation;
        private final ContentCheckResult contentCheckResult;

        public SecurityCheckResult(boolean isSafe, RiskLevel riskLevel, List<String> issues, 
                                 String recommendation, ContentCheckResult contentCheckResult) {
            this.isSafe = isSafe;
            this.riskLevel = riskLevel;
            this.issues = issues;
            this.recommendation = recommendation;
            this.contentCheckResult = contentCheckResult;
        }

        // Getters
        public boolean isSafe() { return isSafe; }
        public RiskLevel getRiskLevel() { return riskLevel; }
        public List<String> getIssues() { return issues; }
        public String getRecommendation() { return recommendation; }
        public ContentCheckResult getContentCheckResult() { return contentCheckResult; }
    }

    /**
     * 文件内容检查结果
     */
    class ContentCheckResult {
        private final boolean hasExecutableContent;
        private final boolean hasSuspiciousContent;
        private final List<String> detectedSignatures;
        private final SensitiveInfoCheckResult sensitiveInfoResult;

        public ContentCheckResult(boolean hasExecutableContent, boolean hasSuspiciousContent,
                                List<String> detectedSignatures, SensitiveInfoCheckResult sensitiveInfoResult) {
            this.hasExecutableContent = hasExecutableContent;
            this.hasSuspiciousContent = hasSuspiciousContent;
            this.detectedSignatures = detectedSignatures;
            this.sensitiveInfoResult = sensitiveInfoResult;
        }

        // Getters
        public boolean hasExecutableContent() { return hasExecutableContent; }
        public boolean hasSuspiciousContent() { return hasSuspiciousContent; }
        public List<String> getDetectedSignatures() { return detectedSignatures; }
        public SensitiveInfoCheckResult getSensitiveInfoResult() { return sensitiveInfoResult; }
    }

    /**
     * 敏感信息检查结果
     */
    class SensitiveInfoCheckResult {
        private final boolean hasSensitiveInfo;
        private final List<String> detectedKeywords;
        private final int sensitiveCount;

        public SensitiveInfoCheckResult(boolean hasSensitiveInfo, List<String> detectedKeywords, int sensitiveCount) {
            this.hasSensitiveInfo = hasSensitiveInfo;
            this.detectedKeywords = detectedKeywords;
            this.sensitiveCount = sensitiveCount;
        }

        // Getters
        public boolean hasSensitiveInfo() { return hasSensitiveInfo; }
        public List<String> getDetectedKeywords() { return detectedKeywords; }
        public int getSensitiveCount() { return sensitiveCount; }
    }

    /**
     * 风险等级枚举
     */
    enum RiskLevel {
        LOW("LOW", "低风险"),
        MEDIUM("MEDIUM", "中风险"),
        HIGH("HIGH", "高风险"),
        CRITICAL("CRITICAL", "严重风险"),
        UNKNOWN("UNKNOWN", "未知风险");

        private final String code;
        private final String description;

        RiskLevel(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }
}
