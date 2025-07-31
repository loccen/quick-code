package com.quickcode.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件上传配置验证器
 * 在应用启动时验证配置的完整性和合理性
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadConfigValidator {

    private final FileUploadConfig fileUploadConfig;

    /**
     * 应用启动后验证配置
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        log.info("开始验证文件上传配置...");
        
        validateBasicConfig();
        validateSecurityConfig();
        validateContentCheckConfig();
        
        log.info("文件上传配置验证完成");
        logConfigurationSummary();
    }

    /**
     * 验证基础配置
     */
    private void validateBasicConfig() {
        if (fileUploadConfig.getUploadPath() == null || fileUploadConfig.getUploadPath().trim().isEmpty()) {
            log.warn("上传路径未配置，使用默认值: /uploads");
        }

        if (fileUploadConfig.getMaxFileSize() == null || fileUploadConfig.getMaxFileSize() <= 0) {
            log.warn("最大文件大小配置无效，使用默认值: 100MB");
        }

        if (fileUploadConfig.getMaxRequestSize() == null || fileUploadConfig.getMaxRequestSize() <= 0) {
            log.warn("最大请求大小配置无效，使用默认值: 200MB");
        }

        if (fileUploadConfig.getMaxFileSize() != null && fileUploadConfig.getMaxRequestSize() != null) {
            if (fileUploadConfig.getMaxRequestSize() < fileUploadConfig.getMaxFileSize()) {
                log.warn("最大请求大小({})小于最大文件大小({})，可能导致上传失败", 
                    formatFileSize(fileUploadConfig.getMaxRequestSize()),
                    formatFileSize(fileUploadConfig.getMaxFileSize()));
            }
        }

        if (fileUploadConfig.getCompressionQuality() != null) {
            double quality = fileUploadConfig.getCompressionQuality();
            if (quality < 0.0 || quality > 1.0) {
                log.warn("压缩质量配置无效({}), 应在0.0-1.0之间", quality);
            }
        }
    }

    /**
     * 验证安全配置
     */
    private void validateSecurityConfig() {
        FileUploadConfig.SecurityConfig security = fileUploadConfig.getSecurity();
        if (security == null) {
            log.warn("安全配置未找到，将使用默认配置");
            return;
        }

        if (security.getEnabled() == null) {
            log.warn("安全检查开关未配置，默认启用");
        }

        validateFileTypeLists(security);
        validateBlacklists(security);
    }

    /**
     * 验证文件类型列表
     */
    private void validateFileTypeLists(FileUploadConfig.SecurityConfig security) {
        validateList(security.getAllowedSourceTypes(), "允许的源码文件类型");
        validateList(security.getAllowedImageTypes(), "允许的图片文件类型");
        validateList(security.getAllowedDocumentTypes(), "允许的文档文件类型");
    }

    /**
     * 验证黑名单配置
     */
    private void validateBlacklists(FileUploadConfig.SecurityConfig security) {
        validateList(security.getDangerousExtensions(), "危险文件扩展名黑名单");
        validateList(security.getDangerousMimeTypes(), "危险MIME类型黑名单");
    }

    /**
     * 验证内容检查配置
     */
    private void validateContentCheckConfig() {
        FileUploadConfig.SecurityConfig security = fileUploadConfig.getSecurity();
        if (security == null || security.getContentCheck() == null) {
            log.warn("内容检查配置未找到");
            return;
        }

        FileUploadConfig.SecurityConfig.ContentCheck contentCheck = security.getContentCheck();
        
        if (contentCheck.getHeaderBytesToCheck() != null && contentCheck.getHeaderBytesToCheck() <= 0) {
            log.warn("文件头检查字节数配置无效: {}", contentCheck.getHeaderBytesToCheck());
        }

        if (contentCheck.getMaxTextScanSize() != null && contentCheck.getMaxTextScanSize() <= 0) {
            log.warn("最大文本扫描大小配置无效: {}", contentCheck.getMaxTextScanSize());
        }

        validateList(contentCheck.getMaliciousSignatures(), "恶意文件特征码");
        validateList(contentCheck.getExecutablePatterns(), "可执行文件模式");
        validateList(contentCheck.getSensitiveKeywords(), "敏感关键词");
        validateList(contentCheck.getSensitiveFilePatterns(), "敏感文件名模式");
        validateList(contentCheck.getMaliciousCodePatterns(), "恶意代码模式");
    }

    /**
     * 验证列表配置
     */
    private void validateList(List<String> list, String configName) {
        if (list == null || list.isEmpty()) {
            log.warn("{}配置为空", configName);
        } else {
            log.debug("{}配置了{}项", configName, list.size());
        }
    }

    /**
     * 记录配置摘要
     */
    private void logConfigurationSummary() {
        log.info("=== 文件上传配置摘要 ===");
        log.info("上传路径: {}", fileUploadConfig.getUploadPath());
        log.info("最大文件大小: {}", formatFileSize(fileUploadConfig.getMaxFileSize()));
        log.info("最大请求大小: {}", formatFileSize(fileUploadConfig.getMaxRequestSize()));
        log.info("启用压缩: {}", fileUploadConfig.getEnableCompression());
        log.info("压缩质量: {}", fileUploadConfig.getCompressionQuality());
        
        FileUploadConfig.SecurityConfig security = fileUploadConfig.getSecurity();
        if (security != null) {
            log.info("安全检查: {}", security.getEnabled() ? "启用" : "禁用");
            log.info("单项目最大文件数: {}", security.getMaxFilesPerProject());
            
            if (security.getContentCheck() != null) {
                log.info("内容检查: {}", security.getContentCheck().getEnabled() ? "启用" : "禁用");
                log.info("文件头检查字节数: {}", security.getContentCheck().getHeaderBytesToCheck());
                log.info("最大文本扫描大小: {}", formatFileSize(security.getContentCheck().getMaxTextScanSize()));
            }
        }
        log.info("========================");
    }

    /**
     * 格式化文件大小显示
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null) {
            return "未配置";
        }
        
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
