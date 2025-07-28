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
            "exe", "bat", "cmd", "scr", "vbs", "js", "jar", "com", "pif", "msi", "dll",
            "app", "deb", "dmg", "pkg", "rpm", "run", "bin", "elf", "so", "dylib",
            "ps1", "psm1", "psd1", "ps1xml", "psc1", "psc2", "msh", "msh1", "msh2",
            "mshxml", "msh1xml", "msh2xml", "scf", "lnk", "inf", "reg", "cpl",
            "gadget", "msc", "msp", "mst", "application", "appx", "msix", "xap"
        );

        /**
         * 危险MIME类型黑名单
         */
        private List<String> dangerousMimeTypes = Arrays.asList(
            "application/x-executable", "application/x-msdownload",
            "application/x-msdos-program", "application/x-winexe",
            "application/x-dosexec", "application/x-exe", "application/x-winexe",
            "application/x-msi", "application/x-msdownload", "application/octet-stream",
            "application/x-sharedlib", "application/x-object", "application/x-coredump",
            "application/x-shellscript", "text/x-shellscript", "application/x-perl",
            "application/x-python-code", "application/x-ruby", "application/javascript"
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
            private Integer headerBytesToCheck = 2048;

            /**
             * 最大文本文件扫描大小（字节）
             */
            private Long maxTextScanSize = 10L * 1024 * 1024; // 10MB

            /**
             * 恶意文件特征码（十六进制）
             */
            private List<String> maliciousSignatures = Arrays.asList(
                "4D5A",     // PE executable (Windows)
                "7F454C46", // ELF executable (Linux)
                "CAFEBABE", // Java class file
                "FEEDFACE", // Mach-O binary (macOS)
                "FEEDFACF", // Mach-O 64-bit binary
                "CE4E5446", // .NET assembly
                "504B0304", // ZIP file (potential executable in disguise)
                "504B0506", // ZIP file end
                "504B0708", // ZIP file spanned
                "52617221", // RAR archive
                "377ABCAF", // 7-Zip archive
                "1F8B08",   // GZIP compressed
                "425A68",   // BZIP2 compressed
                "FD377A58", // XZ compressed
                "213C617263683E", // Unix archive
                "EDABEEDB"  // RPM package
            );

            /**
             * 可执行文件扩展名模式
             */
            private List<String> executablePatterns = Arrays.asList(
                ".*\\.exe$", ".*\\.bat$", ".*\\.cmd$", ".*\\.scr$", ".*\\.com$",
                ".*\\.pif$", ".*\\.msi$", ".*\\.dll$", ".*\\.so$", ".*\\.dylib$",
                ".*\\.app$", ".*\\.deb$", ".*\\.rpm$", ".*\\.dmg$", ".*\\.pkg$",
                ".*\\.run$", ".*\\.bin$", ".*\\.elf$", ".*\\.out$"
            );

            /**
             * 敏感关键词检查
             */
            private List<String> sensitiveKeywords = Arrays.asList(
                // 密码相关
                "password", "passwd", "pwd", "secret", "private_key", "private-key",
                "api_key", "api-key", "apikey", "token", "access_token", "refresh_token",
                "auth_token", "bearer", "jwt", "session_id", "session-id",

                // 数据库连接
                "database_url", "db_password", "db_user", "connection_string",
                "mysql_password", "postgres_password", "mongodb_uri", "redis_password",

                // 云服务密钥
                "aws_access_key", "aws_secret_key", "azure_client_secret",
                "google_api_key", "gcp_service_account", "alibaba_access_key",

                // 加密相关
                "private_key_file", "certificate", "ssl_key", "tls_key", "rsa_private",
                "encryption_key", "decrypt", "cipher", "hash_salt",

                // 敏感配置
                "admin_password", "root_password", "master_key", "signing_key",
                "webhook_secret", "oauth_secret", "client_secret"
            );

            /**
             * 敏感文件名模式
             */
            private List<String> sensitiveFilePatterns = Arrays.asList(
                ".*\\.key$", ".*\\.pem$", ".*\\.p12$", ".*\\.pfx$", ".*\\.jks$",
                ".*\\.keystore$", ".*\\.truststore$", ".*\\.crt$", ".*\\.cer$",
                ".*\\.der$", ".*\\.csr$", ".*\\.p7b$", ".*\\.p7c$", ".*\\.p7s$",
                ".*id_rsa.*", ".*id_dsa.*", ".*id_ecdsa.*", ".*id_ed25519.*",
                ".*\\.ssh.*", ".*\\.gpg$", ".*\\.pgp$", ".*\\.asc$",
                ".*config.*", ".*secret.*", ".*credential.*", ".*password.*"
            );

            /**
             * 恶意代码模式
             */
            private List<String> maliciousCodePatterns = Arrays.asList(
                // 系统命令执行
                "Runtime\\.getRuntime\\(\\)\\.exec", "ProcessBuilder", "system\\(",
                "shell_exec", "exec\\(", "eval\\(", "assert\\(", "passthru\\(",
                "popen\\(", "proc_open\\(", "file_get_contents\\(.*http",

                // 网络请求
                "curl_exec", "file_get_contents.*://", "fopen.*://", "include.*://",
                "require.*://", "HttpURLConnection", "URLConnection", "Socket\\(",

                // 文件操作
                "file_put_contents", "fwrite", "fputs", "move_uploaded_file",
                "copy\\(", "unlink\\(", "rmdir\\(", "mkdir\\(", "chmod\\(",

                // 数据库操作
                "mysql_query", "mysqli_query", "pg_query", "sqlite_query",
                "execute\\(", "prepare\\(", "query\\(", "SELECT.*FROM.*WHERE",

                // 反序列化
                "unserialize\\(", "pickle\\.loads", "yaml\\.load", "json\\.loads",
                "ObjectInputStream", "readObject\\(", "deserialize\\("
            );
        }
    }
}
