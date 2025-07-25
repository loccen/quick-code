package com.quickcode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 文件上传配置属性
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.file")
public class FileUploadConfig {

    /**
     * 文件上传路径
     */
    private String uploadPath = "/uploads";

    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize = 100L * 1024 * 1024; // 100MB

    /**
     * 最大请求大小（字节）
     */
    private Long maxRequestSize = 200L * 1024 * 1024; // 200MB

    /**
     * 基础URL
     */
    private String baseUrl = "http://localhost:8080";

    /**
     * 是否启用文件压缩
     */
    private Boolean enableCompression = true;

    /**
     * 压缩质量（0.0-1.0）
     */
    private Double compressionQuality = 0.8;

    /**
     * 安全检查配置
     */
    private SecurityConfig security = new SecurityConfig();

    @Data
    public static class SecurityConfig {
        /**
         * 是否启用安全检查
         */
        private Boolean enabled = true;

        /**
         * 最大文件大小限制（字节）
         */
        private Long maxFileSize = 500L * 1024 * 1024; // 500MB

        /**
         * 单个项目最大文件数量
         */
        private Integer maxFilesPerProject = 50;

        /**
         * 允许的源码文件类型
         */
        private List<String> allowedSourceTypes = Arrays.asList(
            "zip", "rar", "7z", "tar.gz", "tar", "gz"
        );

        /**
         * 允许的图片文件类型
         */
        private List<String> allowedImageTypes = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "svg"
        );

        /**
         * 允许的文档文件类型
         */
        private List<String> allowedDocumentTypes = Arrays.asList(
            "pdf", "doc", "docx", "txt", "md", "rtf"
        );

        /**
         * 危险文件扩展名黑名单
         */
        private List<String> dangerousExtensions = Arrays.asList(
            "exe", "bat", "cmd", "scr", "vbs", "js", "jar", "com", "pif", "msi", "dll"
        );

        /**
         * 危险MIME类型黑名单
         */
        private List<String> dangerousMimeTypes = Arrays.asList(
            "application/x-executable", "application/x-msdownload", 
            "application/x-msdos-program", "application/x-winexe"
        );

        /**
         * 文件内容检查配置
         */
        private ContentCheck contentCheck = new ContentCheck();

        @Data
        public static class ContentCheck {
            /**
             * 是否启用文件内容检查
             */
            private Boolean enabled = true;

            /**
             * 检查文件头部字节数
             */
            private Integer headerBytesToCheck = 1024;

            /**
             * 恶意文件特征码（十六进制）
             */
            private List<String> maliciousSignatures = Arrays.asList(
                "4D5A", // PE executable
                "7F454C46" // ELF executable
            );

            /**
             * 敏感关键词检查
             */
            private List<String> sensitiveKeywords = Arrays.asList(
                "password", "secret", "private_key", "api_key", "token"
            );
        }
    }
}
