package com.quickcode.service;

import com.quickcode.entity.ProjectFile;
import com.quickcode.service.FileStorageService.StorageResult;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 项目文件服务接口
 * 提供项目文件管理的业务逻辑
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface ProjectFileService extends BaseService<ProjectFile, Long> {

    /**
     * 文件上传结果
     */
    class FileUploadResult {
        private final ProjectFile projectFile;
        private final StorageResult storageResult;
        private final boolean isSuccess;
        private final String message;

        public FileUploadResult(ProjectFile projectFile, StorageResult storageResult, boolean isSuccess, String message) {
            this.projectFile = projectFile;
            this.storageResult = storageResult;
            this.isSuccess = isSuccess;
            this.message = message;
        }

        // Getters
        public ProjectFile getProjectFile() { return projectFile; }
        public StorageResult getStorageResult() { return storageResult; }
        public boolean isSuccess() { return isSuccess; }
        public String getMessage() { return message; }
    }

    /**
     * 文件安全检查结果
     */
    class SecurityCheckResult {
        private final boolean isSafe;
        private final String riskLevel;
        private final List<String> issues;
        private final String recommendation;

        public SecurityCheckResult(boolean isSafe, String riskLevel, List<String> issues, String recommendation) {
            this.isSafe = isSafe;
            this.riskLevel = riskLevel;
            this.issues = issues;
            this.recommendation = recommendation;
        }

        // Getters
        public boolean isSafe() { return isSafe; }
        public String getRiskLevel() { return riskLevel; }
        public List<String> getIssues() { return issues; }
        public String getRecommendation() { return recommendation; }
    }

    /**
     * 上传项目文件
     * 
     * @param projectId 项目ID
     * @param file 文件
     * @param fileType 文件类型
     * @param description 文件描述
     * @param userId 上传用户ID
     * @return 上传结果
     * @throws IOException 上传失败时抛出
     */
    FileUploadResult uploadProjectFile(Long projectId, MultipartFile file, String fileType, 
                                     String description, Long userId) throws IOException;

    /**
     * 批量上传项目文件
     * 
     * @param projectId 项目ID
     * @param files 文件列表
     * @param fileType 文件类型
     * @param userId 上传用户ID
     * @return 上传结果列表
     */
    List<FileUploadResult> uploadProjectFiles(Long projectId, List<MultipartFile> files, 
                                            String fileType, Long userId);

    /**
     * 替换项目文件
     * 
     * @param fileId 原文件ID
     * @param newFile 新文件
     * @param userId 操作用户ID
     * @return 上传结果
     * @throws IOException 替换失败时抛出
     */
    FileUploadResult replaceProjectFile(Long fileId, MultipartFile newFile, Long userId) throws IOException;

    /**
     * 下载项目文件
     * 
     * @param fileId 文件ID
     * @param userId 下载用户ID
     * @return 文件资源
     * @throws IOException 下载失败时抛出
     */
    Resource downloadProjectFile(Long fileId, Long userId) throws IOException;

    /**
     * 获取项目文件列表
     * 
     * @param projectId 项目ID
     * @param fileType 文件类型（可选）
     * @param pageable 分页参数
     * @return 文件列表
     */
    Page<ProjectFile> getProjectFiles(Long projectId, String fileType, Pageable pageable);

    /**
     * 获取项目的主文件
     * 
     * @param projectId 项目ID
     * @param fileType 文件类型
     * @return 主文件
     */
    Optional<ProjectFile> getPrimaryFile(Long projectId, String fileType);

    /**
     * 设置主文件
     * 
     * @param fileId 文件ID
     * @param userId 操作用户ID
     * @return 是否设置成功
     */
    boolean setPrimaryFile(Long fileId, Long userId);

    /**
     * 删除项目文件
     * 
     * @param fileId 文件ID
     * @param userId 操作用户ID
     * @return 是否删除成功
     */
    boolean deleteProjectFile(Long fileId, Long userId);

    /**
     * 批量删除项目文件
     * 
     * @param fileIds 文件ID列表
     * @param userId 操作用户ID
     * @return 删除成功的文件数量
     */
    int deleteProjectFiles(List<Long> fileIds, Long userId);

    /**
     * 删除项目的所有文件
     * 
     * @param projectId 项目ID
     * @param userId 操作用户ID
     * @return 删除的文件数量
     */
    int deleteAllProjectFiles(Long projectId, Long userId);

    /**
     * 文件安全检查
     * 
     * @param file 文件
     * @return 安全检查结果
     */
    SecurityCheckResult performSecurityCheck(MultipartFile file);

    /**
     * 文件安全检查（已存储的文件）
     * 
     * @param fileId 文件ID
     * @return 安全检查结果
     */
    SecurityCheckResult performSecurityCheck(Long fileId);

    /**
     * 处理文件（解压、分析等）
     * 
     * @param fileId 文件ID
     * @return 是否处理成功
     */
    boolean processFile(Long fileId);

    /**
     * 获取文件处理状态
     * 
     * @param fileId 文件ID
     * @return 处理状态
     */
    ProjectFile.FileStatus getFileStatus(Long fileId);

    /**
     * 更新文件状态
     * 
     * @param fileId 文件ID
     * @param status 新状态
     * @param userId 操作用户ID
     * @return 是否更新成功
     */
    boolean updateFileStatus(Long fileId, ProjectFile.FileStatus status, Long userId);

    /**
     * 检查用户是否有文件访问权限
     * 
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasFileAccess(Long fileId, Long userId);

    /**
     * 检查用户是否有文件编辑权限
     * 
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasFileEditPermission(Long fileId, Long userId);

    /**
     * 获取文件统计信息
     * 
     * @param projectId 项目ID
     * @return 文件统计信息
     */
    FileStatistics getFileStatistics(Long projectId);

    /**
     * 文件统计信息
     */
    class FileStatistics {
        private final long totalFiles;
        private final long totalSize;
        private final long sourceFiles;
        private final long coverImages;
        private final long demoFiles;
        private final long documentFiles;

        public FileStatistics(long totalFiles, long totalSize, long sourceFiles, 
                            long coverImages, long demoFiles, long documentFiles) {
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
            this.sourceFiles = sourceFiles;
            this.coverImages = coverImages;
            this.demoFiles = demoFiles;
            this.documentFiles = documentFiles;
        }

        // Getters
        public long getTotalFiles() { return totalFiles; }
        public long getTotalSize() { return totalSize; }
        public long getSourceFiles() { return sourceFiles; }
        public long getCoverImages() { return coverImages; }
        public long getDemoFiles() { return demoFiles; }
        public long getDocumentFiles() { return documentFiles; }
    }

    /**
     * 清理过期的临时文件
     * 
     * @param olderThanHours 清理多少小时前的文件
     * @return 清理的文件数量
     */
    int cleanupExpiredFiles(int olderThanHours);

    /**
     * 查找重复文件
     * 
     * @param projectId 项目ID（可选）
     * @return 重复文件列表
     */
    List<List<ProjectFile>> findDuplicateFiles(Long projectId);

    /**
     * 验证文件完整性
     * 
     * @param fileId 文件ID
     * @return 是否完整
     */
    boolean validateFileIntegrity(Long fileId);

    /**
     * 获取文件下载URL
     * 
     * @param fileId 文件ID
     * @param userId 用户ID
     * @param expirationMinutes 过期时间（分钟）
     * @return 下载URL
     */
    String generateDownloadUrl(Long fileId, Long userId, int expirationMinutes);

    /**
     * 获取文件预览URL
     * 
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 预览URL
     */
    String generatePreviewUrl(Long fileId, Long userId);

    /**
     * 根据文件哈希查找文件
     * 
     * @param fileHash 文件哈希值
     * @return 文件列表
     */
    List<ProjectFile> findFilesByHash(String fileHash);

    /**
     * 根据文件类型统计文件数量
     * 
     * @param projectId 项目ID
     * @param fileType 文件类型
     * @return 文件数量
     */
    long countFilesByType(Long projectId, String fileType);

    /**
     * 获取项目文件总大小
     * 
     * @param projectId 项目ID
     * @return 总大小（字节）
     */
    long getProjectFilesTotalSize(Long projectId);

    /**
     * 查找失败的文件
     * 
     * @param pageable 分页参数
     * @return 失败的文件列表
     */
    Page<ProjectFile> findFailedFiles(Pageable pageable);

    /**
     * 查找待处理的文件
     * 
     * @param pageable 分页参数
     * @return 待处理的文件列表
     */
    Page<ProjectFile> findPendingFiles(Pageable pageable);

    /**
     * 重新处理失败的文件
     * 
     * @param fileId 文件ID
     * @return 是否重新处理成功
     */
    boolean reprocessFailedFile(Long fileId);

    /**
     * 批量重新处理失败的文件
     * 
     * @param fileIds 文件ID列表
     * @return 重新处理成功的文件数量
     */
    int reprocessFailedFiles(List<Long> fileIds);
}
