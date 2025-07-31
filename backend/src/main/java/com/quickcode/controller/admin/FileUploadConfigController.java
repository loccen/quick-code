package com.quickcode.controller.admin;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.config.FileUploadConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传配置管理控制器
 * 提供配置查看和管理功能
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/config/file-upload")
@RequiredArgsConstructor
@Tag(name = "文件上传配置管理", description = "文件上传配置查看和管理接口")
public class FileUploadConfigController {

    private final FileUploadConfig fileUploadConfig;

    /**
     * 获取文件上传配置概览
     */
    @GetMapping("/overview")
    @Operation(summary = "获取配置概览", description = "获取文件上传配置的概览信息")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getConfigOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 基础配置
        Map<String, Object> basic = new HashMap<>();
        basic.put("uploadPath", fileUploadConfig.getUploadPath());
        basic.put("maxFileSize", fileUploadConfig.getMaxFileSize());
        basic.put("maxRequestSize", fileUploadConfig.getMaxRequestSize());
        basic.put("baseUrl", fileUploadConfig.getBaseUrl());
        basic.put("enableCompression", fileUploadConfig.getEnableCompression());
        basic.put("compressionQuality", fileUploadConfig.getCompressionQuality());
        overview.put("basic", basic);
        
        // 安全配置概览
        if (fileUploadConfig.getSecurity() != null) {
            Map<String, Object> security = new HashMap<>();
            FileUploadConfig.SecurityConfig securityConfig = fileUploadConfig.getSecurity();
            
            security.put("enabled", securityConfig.getEnabled());
            security.put("maxFileSize", securityConfig.getMaxFileSize());
            security.put("maxFilesPerProject", securityConfig.getMaxFilesPerProject());
            security.put("allowedSourceTypesCount", 
                securityConfig.getAllowedSourceTypes() != null ? securityConfig.getAllowedSourceTypes().size() : 0);
            security.put("allowedImageTypesCount", 
                securityConfig.getAllowedImageTypes() != null ? securityConfig.getAllowedImageTypes().size() : 0);
            security.put("allowedDocumentTypesCount", 
                securityConfig.getAllowedDocumentTypes() != null ? securityConfig.getAllowedDocumentTypes().size() : 0);
            security.put("dangerousExtensionsCount", 
                securityConfig.getDangerousExtensions() != null ? securityConfig.getDangerousExtensions().size() : 0);
            security.put("dangerousMimeTypesCount", 
                securityConfig.getDangerousMimeTypes() != null ? securityConfig.getDangerousMimeTypes().size() : 0);
            
            overview.put("security", security);
            
            // 内容检查配置概览
            if (securityConfig.getContentCheck() != null) {
                Map<String, Object> contentCheck = new HashMap<>();
                FileUploadConfig.SecurityConfig.ContentCheck contentCheckConfig = securityConfig.getContentCheck();
                
                contentCheck.put("enabled", contentCheckConfig.getEnabled());
                contentCheck.put("headerBytesToCheck", contentCheckConfig.getHeaderBytesToCheck());
                contentCheck.put("maxTextScanSize", contentCheckConfig.getMaxTextScanSize());
                contentCheck.put("maliciousSignaturesCount", 
                    contentCheckConfig.getMaliciousSignatures() != null ? contentCheckConfig.getMaliciousSignatures().size() : 0);
                contentCheck.put("executablePatternsCount", 
                    contentCheckConfig.getExecutablePatterns() != null ? contentCheckConfig.getExecutablePatterns().size() : 0);
                contentCheck.put("sensitiveKeywordsCount", 
                    contentCheckConfig.getSensitiveKeywords() != null ? contentCheckConfig.getSensitiveKeywords().size() : 0);
                contentCheck.put("sensitiveFilePatternsCount", 
                    contentCheckConfig.getSensitiveFilePatterns() != null ? contentCheckConfig.getSensitiveFilePatterns().size() : 0);
                contentCheck.put("maliciousCodePatternsCount", 
                    contentCheckConfig.getMaliciousCodePatterns() != null ? contentCheckConfig.getMaliciousCodePatterns().size() : 0);
                
                overview.put("contentCheck", contentCheck);
            }
        }
        
        return ApiResponse.success(overview);
    }

    /**
     * 获取完整的文件上传配置
     */
    @GetMapping("/full")
    @Operation(summary = "获取完整配置", description = "获取完整的文件上传配置信息")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FileUploadConfig> getFullConfig() {
        return ApiResponse.success(fileUploadConfig);
    }

    /**
     * 获取安全配置详情
     */
    @GetMapping("/security")
    @Operation(summary = "获取安全配置", description = "获取文件上传安全配置详情")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FileUploadConfig.SecurityConfig> getSecurityConfig() {
        return ApiResponse.success(fileUploadConfig.getSecurity());
    }

    /**
     * 获取内容检查配置详情
     */
    @GetMapping("/content-check")
    @Operation(summary = "获取内容检查配置", description = "获取文件内容检查配置详情")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FileUploadConfig.SecurityConfig.ContentCheck> getContentCheckConfig() {
        if (fileUploadConfig.getSecurity() != null) {
            return ApiResponse.success(fileUploadConfig.getSecurity().getContentCheck());
        }
        return ApiResponse.error("安全配置未找到");
    }

    /**
     * 获取配置统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取配置统计", description = "获取文件上传配置的统计信息")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getConfigStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 基础统计
        stats.put("configurationComplete", isConfigurationComplete());
        stats.put("securityEnabled", fileUploadConfig.getSecurity() != null && 
            Boolean.TRUE.equals(fileUploadConfig.getSecurity().getEnabled()));
        stats.put("contentCheckEnabled", fileUploadConfig.getSecurity() != null && 
            fileUploadConfig.getSecurity().getContentCheck() != null &&
            Boolean.TRUE.equals(fileUploadConfig.getSecurity().getContentCheck().getEnabled()));
        
        // 配置项统计
        if (fileUploadConfig.getSecurity() != null) {
            FileUploadConfig.SecurityConfig security = fileUploadConfig.getSecurity();
            stats.put("totalAllowedTypes", 
                (security.getAllowedSourceTypes() != null ? security.getAllowedSourceTypes().size() : 0) +
                (security.getAllowedImageTypes() != null ? security.getAllowedImageTypes().size() : 0) +
                (security.getAllowedDocumentTypes() != null ? security.getAllowedDocumentTypes().size() : 0));
            stats.put("totalBlacklistItems", 
                (security.getDangerousExtensions() != null ? security.getDangerousExtensions().size() : 0) +
                (security.getDangerousMimeTypes() != null ? security.getDangerousMimeTypes().size() : 0));
            
            if (security.getContentCheck() != null) {
                FileUploadConfig.SecurityConfig.ContentCheck contentCheck = security.getContentCheck();
                stats.put("totalSecurityPatterns", 
                    (contentCheck.getMaliciousSignatures() != null ? contentCheck.getMaliciousSignatures().size() : 0) +
                    (contentCheck.getExecutablePatterns() != null ? contentCheck.getExecutablePatterns().size() : 0) +
                    (contentCheck.getSensitiveKeywords() != null ? contentCheck.getSensitiveKeywords().size() : 0) +
                    (contentCheck.getSensitiveFilePatterns() != null ? contentCheck.getSensitiveFilePatterns().size() : 0) +
                    (contentCheck.getMaliciousCodePatterns() != null ? contentCheck.getMaliciousCodePatterns().size() : 0));
            }
        }
        
        return ApiResponse.success(stats);
    }

    /**
     * 检查配置是否完整
     */
    private boolean isConfigurationComplete() {
        if (fileUploadConfig.getUploadPath() == null || fileUploadConfig.getUploadPath().trim().isEmpty()) {
            return false;
        }
        if (fileUploadConfig.getMaxFileSize() == null || fileUploadConfig.getMaxFileSize() <= 0) {
            return false;
        }
        if (fileUploadConfig.getMaxRequestSize() == null || fileUploadConfig.getMaxRequestSize() <= 0) {
            return false;
        }
        if (fileUploadConfig.getSecurity() == null) {
            return false;
        }
        
        FileUploadConfig.SecurityConfig security = fileUploadConfig.getSecurity();
        if (security.getAllowedSourceTypes() == null || security.getAllowedSourceTypes().isEmpty()) {
            return false;
        }
        if (security.getDangerousExtensions() == null || security.getDangerousExtensions().isEmpty()) {
            return false;
        }
        
        return true;
    }
}
