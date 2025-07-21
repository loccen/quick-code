package com.quickcode.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * 文件存储服务接口
 * 提供文件上传、下载、删除等核心功能
 * 支持本地存储和云存储
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface FileStorageService {

    /**
     * 文件存储结果
     */
    class StorageResult {
        private final String fileName;
        private final String filePath;
        private final String fileUrl;
        private final Long fileSize;
        private final String mimeType;
        private final String fileHash;

        public StorageResult(String fileName, String filePath, String fileUrl, 
                           Long fileSize, String mimeType, String fileHash) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.fileUrl = fileUrl;
            this.fileSize = fileSize;
            this.mimeType = mimeType;
            this.fileHash = fileHash;
        }

        // Getters
        public String getFileName() { return fileName; }
        public String getFilePath() { return filePath; }
        public String getFileUrl() { return fileUrl; }
        public Long getFileSize() { return fileSize; }
        public String getMimeType() { return mimeType; }
        public String getFileHash() { return fileHash; }
    }

    /**
     * 文件存储配置
     */
    class StorageConfig {
        private final String basePath;
        private final String baseUrl;
        private final long maxFileSize;
        private final List<String> allowedTypes;

        public StorageConfig(String basePath, String baseUrl, long maxFileSize, List<String> allowedTypes) {
            this.basePath = basePath;
            this.baseUrl = baseUrl;
            this.maxFileSize = maxFileSize;
            this.allowedTypes = allowedTypes;
        }

        // Getters
        public String getBasePath() { return basePath; }
        public String getBaseUrl() { return baseUrl; }
        public long getMaxFileSize() { return maxFileSize; }
        public List<String> getAllowedTypes() { return allowedTypes; }
    }

    /**
     * 存储文件
     * 
     * @param file 要存储的文件
     * @param category 文件分类（如：avatar, project, cover等）
     * @return 存储结果
     * @throws IOException 存储失败时抛出
     */
    StorageResult store(MultipartFile file, String category) throws IOException;

    /**
     * 存储文件到指定路径
     * 
     * @param file 要存储的文件
     * @param category 文件分类
     * @param customPath 自定义路径
     * @return 存储结果
     * @throws IOException 存储失败时抛出
     */
    StorageResult store(MultipartFile file, String category, String customPath) throws IOException;

    /**
     * 存储输入流
     * 
     * @param inputStream 输入流
     * @param fileName 文件名
     * @param category 文件分类
     * @param contentType 内容类型
     * @return 存储结果
     * @throws IOException 存储失败时抛出
     */
    StorageResult store(InputStream inputStream, String fileName, String category, String contentType) throws IOException;

    /**
     * 加载文件作为资源
     * 
     * @param filePath 文件路径
     * @return 文件资源
     * @throws IOException 加载失败时抛出
     */
    Resource loadAsResource(String filePath) throws IOException;

    /**
     * 获取文件路径
     * 
     * @param fileName 文件名
     * @param category 文件分类
     * @return 文件路径
     */
    Path getFilePath(String fileName, String category);

    /**
     * 获取文件URL
     * 
     * @param fileName 文件名
     * @param category 文件分类
     * @return 文件URL
     */
    String getFileUrl(String fileName, String category);

    /**
     * 检查文件是否存在
     * 
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean exists(String filePath);

    /**
     * 删除文件
     * 
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean delete(String filePath);

    /**
     * 批量删除文件
     * 
     * @param filePaths 文件路径列表
     * @return 删除成功的文件数量
     */
    int deleteBatch(List<String> filePaths);

    /**
     * 复制文件
     * 
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 是否复制成功
     * @throws IOException 复制失败时抛出
     */
    boolean copy(String sourcePath, String targetPath) throws IOException;

    /**
     * 移动文件
     * 
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 是否移动成功
     * @throws IOException 移动失败时抛出
     */
    boolean move(String sourcePath, String targetPath) throws IOException;

    /**
     * 获取文件大小
     * 
     * @param filePath 文件路径
     * @return 文件大小（字节）
     * @throws IOException 获取失败时抛出
     */
    long getFileSize(String filePath) throws IOException;

    /**
     * 计算文件哈希值
     * 
     * @param filePath 文件路径
     * @return MD5哈希值
     * @throws IOException 计算失败时抛出
     */
    String calculateFileHash(String filePath) throws IOException;

    /**
     * 计算文件哈希值
     * 
     * @param inputStream 输入流
     * @return MD5哈希值
     * @throws IOException 计算失败时抛出
     */
    String calculateFileHash(InputStream inputStream) throws IOException;

    /**
     * 验证文件类型
     * 
     * @param file 文件
     * @param allowedTypes 允许的文件类型
     * @return 是否有效
     */
    boolean validateFileType(MultipartFile file, List<String> allowedTypes);

    /**
     * 验证文件大小
     * 
     * @param file 文件
     * @param maxSize 最大大小（字节）
     * @return 是否有效
     */
    boolean validateFileSize(MultipartFile file, long maxSize);

    /**
     * 生成唯一文件名
     * 
     * @param originalFileName 原始文件名
     * @return 唯一文件名
     */
    String generateUniqueFileName(String originalFileName);

    /**
     * 获取文件扩展名
     * 
     * @param fileName 文件名
     * @return 文件扩展名
     */
    String getFileExtension(String fileName);

    /**
     * 获取MIME类型
     * 
     * @param fileName 文件名
     * @return MIME类型
     */
    String getMimeType(String fileName);

    /**
     * 清理临时文件
     * 
     * @param olderThanHours 清理多少小时前的文件
     * @return 清理的文件数量
     */
    int cleanupTempFiles(int olderThanHours);

    /**
     * 获取存储统计信息
     * 
     * @return 存储统计信息
     */
    StorageStatistics getStorageStatistics();

    /**
     * 存储统计信息
     */
    class StorageStatistics {
        private final long totalFiles;
        private final long totalSize;
        private final long availableSpace;
        private final long usedSpace;

        public StorageStatistics(long totalFiles, long totalSize, long availableSpace, long usedSpace) {
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
            this.availableSpace = availableSpace;
            this.usedSpace = usedSpace;
        }

        // Getters
        public long getTotalFiles() { return totalFiles; }
        public long getTotalSize() { return totalSize; }
        public long getAvailableSpace() { return availableSpace; }
        public long getUsedSpace() { return usedSpace; }
    }

    /**
     * 初始化存储目录
     * 
     * @throws IOException 初始化失败时抛出
     */
    void initializeStorage() throws IOException;

    /**
     * 检查存储健康状态
     * 
     * @return 是否健康
     */
    boolean checkStorageHealth();
}
