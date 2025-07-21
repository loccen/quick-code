package com.quickcode.service.impl;

import com.quickcode.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储服务实现类
 * 基于本地文件系统的存储实现
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.file.max-size:104857600}") // 100MB
    private long maxFileSize;

    private Path rootLocation;

    /**
     * 初始化存储服务
     */
    @Override
    public void initializeStorage() throws IOException {
        this.rootLocation = Paths.get(uploadPath).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.rootLocation);
            log.info("文件存储目录初始化成功: {}", this.rootLocation);
        } catch (IOException e) {
            log.error("无法创建文件存储目录: {}", this.rootLocation, e);
            throw new IOException("无法创建文件存储目录", e);
        }
    }

    @Override
    public StorageResult store(MultipartFile file, String category) throws IOException {
        return store(file, category, null);
    }

    @Override
    public StorageResult store(MultipartFile file, String category, String customPath) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 验证文件大小
        if (!validateFileSize(file, maxFileSize)) {
            throw new IllegalArgumentException("文件大小超过限制: " + maxFileSize + " 字节");
        }

        // 确保存储目录已初始化
        if (rootLocation == null) {
            initializeStorage();
        }

        // 生成唯一文件名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFileName = generateUniqueFileName(originalFileName);

        // 构建存储路径
        Path categoryPath = rootLocation.resolve(category);
        Files.createDirectories(categoryPath);

        Path targetPath;
        if (customPath != null && !customPath.trim().isEmpty()) {
            Path customDir = categoryPath.resolve(customPath);
            Files.createDirectories(customDir);
            targetPath = customDir.resolve(uniqueFileName);
        } else {
            targetPath = categoryPath.resolve(uniqueFileName);
        }

        // 检查路径安全性
        if (!targetPath.normalize().startsWith(rootLocation.normalize())) {
            throw new SecurityException("不安全的文件路径");
        }

        try {
            // 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 计算文件哈希值
            String fileHash = calculateFileHash(targetPath.toString());
            
            // 构建文件URL
            String relativePath = rootLocation.relativize(targetPath).toString().replace("\\", "/");
            String fileUrl = baseUrl + "/uploads/" + relativePath;

            log.info("文件存储成功: {} -> {}", originalFileName, targetPath);

            return new StorageResult(
                uniqueFileName,
                relativePath,
                fileUrl,
                file.getSize(),
                file.getContentType(),
                fileHash
            );

        } catch (IOException e) {
            log.error("文件存储失败: {}", originalFileName, e);
            throw new IOException("文件存储失败", e);
        }
    }

    @Override
    public StorageResult store(InputStream inputStream, String fileName, String category, String contentType) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("输入流不能为空");
        }

        // 确保存储目录已初始化
        if (rootLocation == null) {
            initializeStorage();
        }

        // 生成唯一文件名
        String uniqueFileName = generateUniqueFileName(fileName);

        // 构建存储路径
        Path categoryPath = rootLocation.resolve(category);
        Files.createDirectories(categoryPath);
        Path targetPath = categoryPath.resolve(uniqueFileName);

        // 检查路径安全性
        if (!targetPath.normalize().startsWith(rootLocation.normalize())) {
            throw new SecurityException("不安全的文件路径");
        }

        try {
            // 保存文件
            long fileSize = Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 计算文件哈希值
            String fileHash = calculateFileHash(targetPath.toString());
            
            // 构建文件URL
            String relativePath = rootLocation.relativize(targetPath).toString().replace("\\", "/");
            String fileUrl = baseUrl + "/uploads/" + relativePath;

            log.info("输入流存储成功: {} -> {}", fileName, targetPath);

            return new StorageResult(
                uniqueFileName,
                relativePath,
                fileUrl,
                fileSize,
                contentType,
                fileHash
            );

        } catch (IOException e) {
            log.error("输入流存储失败: {}", fileName, e);
            throw new IOException("输入流存储失败", e);
        }
    }

    @Override
    public Resource loadAsResource(String filePath) throws IOException {
        try {
            Path file = rootLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("文件不存在或不可读: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new IOException("文件路径格式错误: " + filePath, e);
        }
    }

    @Override
    public Path getFilePath(String fileName, String category) {
        return rootLocation.resolve(category).resolve(fileName);
    }

    @Override
    public String getFileUrl(String fileName, String category) {
        return baseUrl + "/uploads/" + category + "/" + fileName;
    }

    @Override
    public boolean exists(String filePath) {
        try {
            Path file = rootLocation.resolve(filePath).normalize();
            return Files.exists(file);
        } catch (Exception e) {
            log.warn("检查文件存在性失败: {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean delete(String filePath) {
        try {
            Path file = rootLocation.resolve(filePath).normalize();
            
            // 检查路径安全性
            if (!file.startsWith(rootLocation)) {
                log.warn("尝试删除不安全的文件路径: {}", filePath);
                return false;
            }
            
            boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
                log.info("文件删除成功: {}", filePath);
            } else {
                log.warn("文件不存在，无法删除: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("文件删除失败: {}", filePath, e);
            return false;
        }
    }

    @Override
    public int deleteBatch(List<String> filePaths) {
        int deletedCount = 0;
        for (String filePath : filePaths) {
            if (delete(filePath)) {
                deletedCount++;
            }
        }
        log.info("批量删除文件完成: 成功删除 {} / {} 个文件", deletedCount, filePaths.size());
        return deletedCount;
    }

    @Override
    public boolean copy(String sourcePath, String targetPath) throws IOException {
        try {
            Path source = rootLocation.resolve(sourcePath).normalize();
            Path target = rootLocation.resolve(targetPath).normalize();
            
            // 检查路径安全性
            if (!source.startsWith(rootLocation) || !target.startsWith(rootLocation)) {
                throw new SecurityException("不安全的文件路径");
            }
            
            // 确保目标目录存在
            Files.createDirectories(target.getParent());
            
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件复制成功: {} -> {}", sourcePath, targetPath);
            return true;
        } catch (IOException e) {
            log.error("文件复制失败: {} -> {}", sourcePath, targetPath, e);
            throw e;
        }
    }

    @Override
    public boolean move(String sourcePath, String targetPath) throws IOException {
        try {
            Path source = rootLocation.resolve(sourcePath).normalize();
            Path target = rootLocation.resolve(targetPath).normalize();
            
            // 检查路径安全性
            if (!source.startsWith(rootLocation) || !target.startsWith(rootLocation)) {
                throw new SecurityException("不安全的文件路径");
            }
            
            // 确保目标目录存在
            Files.createDirectories(target.getParent());
            
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件移动成功: {} -> {}", sourcePath, targetPath);
            return true;
        } catch (IOException e) {
            log.error("文件移动失败: {} -> {}", sourcePath, targetPath, e);
            throw e;
        }
    }

    @Override
    public long getFileSize(String filePath) throws IOException {
        Path file = rootLocation.resolve(filePath).normalize();
        return Files.size(file);
    }

    @Override
    public String calculateFileHash(String filePath) throws IOException {
        Path file = rootLocation.resolve(filePath).normalize();
        try (InputStream inputStream = Files.newInputStream(file)) {
            return calculateFileHash(inputStream);
        }
    }

    @Override
    public String calculateFileHash(InputStream inputStream) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            
            byte[] hashBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("MD5算法不可用", e);
        }
    }

    @Override
    public boolean validateFileType(MultipartFile file, List<String> allowedTypes) {
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            return true;
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        return allowedTypes.stream().anyMatch(type -> type.toLowerCase().equals(extension));
    }

    @Override
    public boolean validateFileSize(MultipartFile file, long maxSize) {
        return file.getSize() <= maxSize;
    }

    @Override
    public String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        if (extension.isEmpty()) {
            return timestamp + "_" + uuid;
        } else {
            return timestamp + "_" + uuid + "." + extension;
        }
    }

    @Override
    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1);
    }

    @Override
    public String getMimeType(String fileName) {
        try {
            Path path = Paths.get(fileName);
            return Files.probeContentType(path);
        } catch (IOException e) {
            log.warn("无法获取文件MIME类型: {}", fileName, e);
            return "application/octet-stream";
        }
    }

    @Override
    public int cleanupTempFiles(int olderThanHours) {
        // 实现临时文件清理逻辑
        // 这里可以根据需要实现具体的清理策略
        log.info("清理 {} 小时前的临时文件", olderThanHours);
        return 0;
    }

    @Override
    public StorageStatistics getStorageStatistics() {
        try {
            // 计算存储统计信息
            long totalFiles = Files.walk(rootLocation)
                    .filter(Files::isRegularFile)
                    .count();
            
            long totalSize = Files.walk(rootLocation)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
            
            long availableSpace = Files.getFileStore(rootLocation).getUsableSpace();
            long usedSpace = Files.getFileStore(rootLocation).getTotalSpace() - availableSpace;
            
            return new StorageStatistics(totalFiles, totalSize, availableSpace, usedSpace);
        } catch (IOException e) {
            log.error("获取存储统计信息失败", e);
            return new StorageStatistics(0, 0, 0, 0);
        }
    }

    @Override
    public boolean checkStorageHealth() {
        try {
            // 检查根目录是否存在且可写
            if (!Files.exists(rootLocation)) {
                log.warn("存储根目录不存在: {}", rootLocation);
                return false;
            }
            
            if (!Files.isWritable(rootLocation)) {
                log.warn("存储根目录不可写: {}", rootLocation);
                return false;
            }
            
            // 检查可用空间
            long availableSpace = Files.getFileStore(rootLocation).getUsableSpace();
            long minRequiredSpace = 100 * 1024 * 1024; // 100MB
            
            if (availableSpace < minRequiredSpace) {
                log.warn("存储空间不足: 可用空间 {} 字节，最小需要 {} 字节", availableSpace, minRequiredSpace);
                return false;
            }
            
            return true;
        } catch (IOException e) {
            log.error("检查存储健康状态失败", e);
            return false;
        }
    }
}
