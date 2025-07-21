package com.quickcode.service;

import com.quickcode.entity.ProjectDownload;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 项目下载服务接口
 * 提供项目下载功能，包括权限验证、下载统计、下载历史记录等
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface ProjectDownloadService extends BaseService<ProjectDownload, Long> {

    /**
     * 下载结果
     */
    class DownloadResult {
        private final boolean success;
        private final String message;
        private final Resource resource;
        private final ProjectDownload downloadRecord;

        public DownloadResult(boolean success, String message, Resource resource, ProjectDownload downloadRecord) {
            this.success = success;
            this.message = message;
            this.resource = resource;
            this.downloadRecord = downloadRecord;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Resource getResource() { return resource; }
        public ProjectDownload getDownloadRecord() { return downloadRecord; }
    }

    /**
     * 下载统计信息
     */
    class DownloadStatistics {
        private final long totalDownloads;
        private final long uniqueDownloaders;
        private final long totalSize;
        private final double averageDuration;
        private final Map<String, Long> downloadsBySource;
        private final Map<String, Long> downloadsByDate;

        public DownloadStatistics(long totalDownloads, long uniqueDownloaders, long totalSize, 
                                double averageDuration, Map<String, Long> downloadsBySource, 
                                Map<String, Long> downloadsByDate) {
            this.totalDownloads = totalDownloads;
            this.uniqueDownloaders = uniqueDownloaders;
            this.totalSize = totalSize;
            this.averageDuration = averageDuration;
            this.downloadsBySource = downloadsBySource;
            this.downloadsByDate = downloadsByDate;
        }

        // Getters
        public long getTotalDownloads() { return totalDownloads; }
        public long getUniqueDownloaders() { return uniqueDownloaders; }
        public long getTotalSize() { return totalSize; }
        public double getAverageDuration() { return averageDuration; }
        public Map<String, Long> getDownloadsBySource() { return downloadsBySource; }
        public Map<String, Long> getDownloadsByDate() { return downloadsByDate; }
    }

    /**
     * 下载项目
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param downloadSource 下载来源
     * @param userAgent 用户代理
     * @param clientIp 客户端IP
     * @return 下载结果
     * @throws IOException 下载失败时抛出
     */
    DownloadResult downloadProject(Long projectId, Long userId, String downloadSource, 
                                 String userAgent, String clientIp) throws IOException;

    /**
     * 下载指定文件
     * 
     * @param projectId 项目ID
     * @param fileId 文件ID
     * @param userId 用户ID
     * @param downloadSource 下载来源
     * @param userAgent 用户代理
     * @param clientIp 客户端IP
     * @return 下载结果
     * @throws IOException 下载失败时抛出
     */
    DownloadResult downloadProjectFile(Long projectId, Long fileId, Long userId, 
                                     String downloadSource, String userAgent, String clientIp) throws IOException;

    /**
     * 检查下载权限
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否有下载权限
     */
    boolean hasDownloadPermission(Long projectId, Long userId);

    /**
     * 检查用户是否已下载过项目
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否已下载过
     */
    boolean hasUserDownloaded(Long projectId, Long userId);

    /**
     * 获取用户下载历史
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 下载历史
     */
    Page<ProjectDownload> getUserDownloadHistory(Long userId, Pageable pageable);

    /**
     * 获取项目下载记录
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 下载记录
     */
    Page<ProjectDownload> getProjectDownloadHistory(Long projectId, Pageable pageable);

    /**
     * 获取下载统计信息
     * 
     * @param projectId 项目ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 下载统计信息
     */
    DownloadStatistics getDownloadStatistics(Long projectId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 记录下载开始
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param fileId 文件ID（可选）
     * @param downloadSource 下载来源
     * @param userAgent 用户代理
     * @param clientIp 客户端IP
     * @return 下载记录
     */
    ProjectDownload recordDownloadStart(Long projectId, Long userId, Long fileId, 
                                      String downloadSource, String userAgent, String clientIp);

    /**
     * 记录下载完成
     * 
     * @param downloadId 下载记录ID
     * @param fileSize 文件大小
     * @param duration 下载耗时（毫秒）
     * @return 是否记录成功
     */
    boolean recordDownloadComplete(Long downloadId, Long fileSize, Long duration);

    /**
     * 记录下载失败
     * 
     * @param downloadId 下载记录ID
     * @param reason 失败原因
     * @return 是否记录成功
     */
    boolean recordDownloadFailed(Long downloadId, String reason);

    /**
     * 记录下载取消
     * 
     * @param downloadId 下载记录ID
     * @return 是否记录成功
     */
    boolean recordDownloadCancelled(Long downloadId);

    /**
     * 更新项目下载次数
     * 
     * @param projectId 项目ID
     * @return 是否更新成功
     */
    boolean updateProjectDownloadCount(Long projectId);

    /**
     * 获取热门下载项目
     * 
     * @param limit 限制数量
     * @param days 统计天数
     * @return 热门项目列表
     */
    List<Map<String, Object>> getPopularDownloads(int limit, int days);

    /**
     * 获取用户下载排行
     * 
     * @param limit 限制数量
     * @param days 统计天数
     * @return 用户排行列表
     */
    List<Map<String, Object>> getTopDownloaders(int limit, int days);

    /**
     * 获取下载趋势数据
     * 
     * @param projectId 项目ID（可选）
     * @param days 统计天数
     * @return 趋势数据
     */
    List<Map<String, Object>> getDownloadTrends(Long projectId, int days);

    /**
     * 清理过期的下载记录
     * 
     * @param olderThanDays 清理多少天前的记录
     * @return 清理的记录数量
     */
    int cleanupExpiredDownloads(int olderThanDays);

    /**
     * 检测异常下载行为
     * 
     * @param userId 用户ID
     * @param clientIp 客户端IP
     * @param timeWindowMinutes 时间窗口（分钟）
     * @return 是否存在异常行为
     */
    boolean detectAbnormalDownloadBehavior(Long userId, String clientIp, int timeWindowMinutes);

    /**
     * 限制下载频率
     * 
     * @param userId 用户ID
     * @param clientIp 客户端IP
     * @return 是否允许下载
     */
    boolean checkDownloadRateLimit(Long userId, String clientIp);

    /**
     * 生成下载令牌
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param expirationMinutes 过期时间（分钟）
     * @return 下载令牌
     */
    String generateDownloadToken(Long projectId, Long userId, int expirationMinutes);

    /**
     * 验证下载令牌
     * 
     * @param token 下载令牌
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否有效
     */
    boolean validateDownloadToken(String token, Long projectId, Long userId);

    /**
     * 获取下载进度
     * 
     * @param downloadId 下载记录ID
     * @return 下载进度（0-100）
     */
    int getDownloadProgress(Long downloadId);

    /**
     * 更新下载进度
     * 
     * @param downloadId 下载记录ID
     * @param progress 进度（0-100）
     * @return 是否更新成功
     */
    boolean updateDownloadProgress(Long downloadId, int progress);

    /**
     * 暂停下载
     * 
     * @param downloadId 下载记录ID
     * @return 是否暂停成功
     */
    boolean pauseDownload(Long downloadId);

    /**
     * 恢复下载
     * 
     * @param downloadId 下载记录ID
     * @return 是否恢复成功
     */
    boolean resumeDownload(Long downloadId);

    /**
     * 获取断点续传信息
     * 
     * @param downloadId 下载记录ID
     * @return 断点位置
     */
    long getResumePosition(Long downloadId);

    /**
     * 支持断点续传的下载
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param rangeStart 开始位置
     * @param rangeEnd 结束位置
     * @return 下载结果
     * @throws IOException 下载失败时抛出
     */
    DownloadResult downloadWithRange(Long projectId, Long userId, long rangeStart, long rangeEnd) throws IOException;
}
