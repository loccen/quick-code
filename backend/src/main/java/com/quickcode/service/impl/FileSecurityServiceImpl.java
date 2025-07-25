package com.quickcode.service.impl;

import com.quickcode.config.FileUploadConfig;
import com.quickcode.service.FileSecurityService;
import com.quickcode.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件安全检查服务实现类
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileSecurityServiceImpl implements FileSecurityService {

    private final FileUploadConfig fileUploadConfig;
    private final FileStorageService fileStorageService;

    @Override
    public SecurityCheckResult performSecurityCheck(MultipartFile file, String fileType) {
        List<String> issues = new ArrayList<>();
        boolean isSafe = true;

        try {
            // 1. 基础验证
            if (!validateFileSize(file)) {
                issues.add("文件大小超过限制");
                isSafe = false;
            }

            if (!validateFileType(file, fileType)) {
                issues.add("不支持的文件类型");
                isSafe = false;
            }

            // 2. 文件名和扩展名检查
            String fileName = file.getOriginalFilename();
            if (fileName != null && !checkFileExtension(fileName)) {
                issues.add("危险的文件扩展名");
                isSafe = false;
            }

            // 3. MIME类型检查
            String contentType = file.getContentType();
            if (contentType != null && !checkMimeType(contentType)) {
                issues.add("危险的MIME类型");
                isSafe = false;
            }

            // 4. 文件内容检查
            ContentCheckResult contentResult = null;
            if (fileUploadConfig.getSecurity().getContentCheck().getEnabled()) {
                try {
                    contentResult = checkFileContent(file);
                    if (contentResult.hasExecutableContent()) {
                        issues.add("检测到可执行文件内容");
                        isSafe = false;
                    }
                    if (contentResult.hasSuspiciousContent()) {
                        issues.add("检测到可疑内容");
                    }
                    if (contentResult.getSensitiveInfoResult().hasSensitiveInfo()) {
                        issues.add("检测到敏感信息");
                    }
                } catch (IOException e) {
                    log.warn("文件内容检查失败: {}", fileName, e);
                    issues.add("文件内容检查失败");
                }
            }

            // 5. 计算风险等级
            RiskLevel riskLevel = calculateRiskLevel(file, issues);

            // 6. 生成建议
            String recommendation = generateRecommendation(isSafe, riskLevel, issues);

            return new SecurityCheckResult(isSafe, riskLevel, issues, recommendation, contentResult);

        } catch (Exception e) {
            String fileName = file.getOriginalFilename();
            log.error("安全检查过程中发生异常: {}", fileName, e);
            issues.add("安全检查过程中发生错误");
            return new SecurityCheckResult(false, RiskLevel.UNKNOWN, issues,
                "建议不要上传此文件", null);
        }
    }

    @Override
    public boolean validateFileType(MultipartFile file, String fileType) {
        if (!fileUploadConfig.getSecurity().getEnabled()) {
            return true;
        }

        List<String> allowedTypes;
        switch (fileType.toUpperCase()) {
            case "SOURCE":
                allowedTypes = fileUploadConfig.getSecurity().getAllowedSourceTypes();
                break;
            case "COVER":
                allowedTypes = fileUploadConfig.getSecurity().getAllowedImageTypes();
                break;
            case "DOCUMENT":
                allowedTypes = fileUploadConfig.getSecurity().getAllowedDocumentTypes();
                break;
            default:
                return true; // 其他类型暂时允许
        }

        return fileStorageService.validateFileType(file, allowedTypes);
    }

    @Override
    public boolean validateFileSize(MultipartFile file) {
        if (!fileUploadConfig.getSecurity().getEnabled()) {
            return true;
        }

        long maxSize = fileUploadConfig.getSecurity().getMaxFileSize();
        return file.getSize() <= maxSize;
    }

    @Override
    public boolean checkFileExtension(String fileName) {
        if (!fileUploadConfig.getSecurity().getEnabled()) {
            return true;
        }

        String extension = fileStorageService.getFileExtension(fileName).toLowerCase();
        List<String> dangerousExtensions = fileUploadConfig.getSecurity().getDangerousExtensions();
        
        return !dangerousExtensions.contains(extension);
    }

    @Override
    public boolean checkMimeType(String contentType) {
        if (!fileUploadConfig.getSecurity().getEnabled()) {
            return true;
        }

        List<String> dangerousMimeTypes = fileUploadConfig.getSecurity().getDangerousMimeTypes();
        
        return dangerousMimeTypes.stream()
            .noneMatch(dangerous -> contentType.toLowerCase().contains(dangerous.toLowerCase()));
    }

    @Override
    public ContentCheckResult checkFileContent(MultipartFile file) throws IOException {
        if (!fileUploadConfig.getSecurity().getContentCheck().getEnabled()) {
            return new ContentCheckResult(false, false, new ArrayList<>(), 
                new SensitiveInfoCheckResult(false, new ArrayList<>(), 0));
        }

        // 读取文件头部字节
        int bytesToCheck = fileUploadConfig.getSecurity().getContentCheck().getHeaderBytesToCheck();
        byte[] headerBytes = new byte[Math.min(bytesToCheck, (int) file.getSize())];
        file.getInputStream().read(headerBytes);

        // 检查文件签名
        boolean hasExecutableContent = !checkFileSignature(headerBytes);
        List<String> detectedSignatures = detectSignatures(headerBytes);

        // 检查敏感信息（对于文本文件）
        SensitiveInfoCheckResult sensitiveResult = new SensitiveInfoCheckResult(false, new ArrayList<>(), 0);
        if (isTextFile(file.getContentType())) {
            try {
                String content = new String(file.getBytes());
                sensitiveResult = checkSensitiveInfo(content);
            } catch (Exception e) {
                log.warn("敏感信息检查失败", e);
            }
        }

        boolean hasSuspiciousContent = !detectedSignatures.isEmpty() || sensitiveResult.hasSensitiveInfo();

        return new ContentCheckResult(hasExecutableContent, hasSuspiciousContent, 
            detectedSignatures, sensitiveResult);
    }

    @Override
    public boolean checkFileSignature(byte[] fileBytes) {
        if (fileBytes.length < 4) {
            return true; // 文件太小，认为安全
        }

        List<String> maliciousSignatures = fileUploadConfig.getSecurity()
            .getContentCheck().getMaliciousSignatures();

        String fileHeader = bytesToHex(fileBytes, Math.min(8, fileBytes.length));
        
        return maliciousSignatures.stream()
            .noneMatch(signature -> fileHeader.startsWith(signature.toUpperCase()));
    }

    @Override
    public SensitiveInfoCheckResult checkSensitiveInfo(String content) {
        List<String> sensitiveKeywords = fileUploadConfig.getSecurity()
            .getContentCheck().getSensitiveKeywords();
        
        List<String> detectedKeywords = new ArrayList<>();
        String lowerContent = content.toLowerCase();
        
        for (String keyword : sensitiveKeywords) {
            if (lowerContent.contains(keyword.toLowerCase())) {
                detectedKeywords.add(keyword);
            }
        }
        
        boolean hasSensitiveInfo = !detectedKeywords.isEmpty();
        return new SensitiveInfoCheckResult(hasSensitiveInfo, detectedKeywords, detectedKeywords.size());
    }

    @Override
    public RiskLevel calculateRiskLevel(MultipartFile file, List<String> issues) {
        if (issues.isEmpty()) {
            return RiskLevel.LOW;
        }

        // 根据问题严重程度计算风险等级
        boolean hasHighRiskIssue = issues.stream().anyMatch(issue -> 
            issue.contains("可执行") || issue.contains("危险") || issue.contains("恶意"));
        
        boolean hasMediumRiskIssue = issues.stream().anyMatch(issue -> 
            issue.contains("敏感") || issue.contains("可疑") || issue.contains("超过限制"));

        if (hasHighRiskIssue) {
            return RiskLevel.HIGH;
        } else if (hasMediumRiskIssue) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    /**
     * 生成安全建议
     */
    private String generateRecommendation(boolean isSafe, RiskLevel riskLevel, List<String> issues) {
        if (isSafe) {
            return "文件安全检查通过，可以上传";
        }

        switch (riskLevel) {
            case HIGH:
            case CRITICAL:
                return "文件存在严重安全风险，强烈建议不要上传";
            case MEDIUM:
                return "文件存在一定安全风险，建议谨慎处理";
            default:
                return "文件存在轻微问题，请检查后再上传";
        }
    }

    /**
     * 检测文件签名
     */
    private List<String> detectSignatures(byte[] fileBytes) {
        List<String> detected = new ArrayList<>();
        List<String> signatures = fileUploadConfig.getSecurity()
            .getContentCheck().getMaliciousSignatures();

        String fileHeader = bytesToHex(fileBytes, Math.min(8, fileBytes.length));
        
        for (String signature : signatures) {
            if (fileHeader.startsWith(signature.toUpperCase())) {
                detected.add(signature);
            }
        }
        
        return detected;
    }

    /**
     * 判断是否为文本文件
     */
    private boolean isTextFile(String contentType) {
        return contentType != null && (
            contentType.startsWith("text/") || 
            contentType.equals("application/json") ||
            contentType.equals("application/xml")
        );
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length && i < bytes.length; i++) {
            sb.append(String.format("%02X", bytes[i]));
        }
        return sb.toString();
    }
}
