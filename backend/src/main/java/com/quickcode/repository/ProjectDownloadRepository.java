package com.quickcode.repository;

import com.quickcode.entity.ProjectDownload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 项目下载记录Repository接口
 * 提供项目下载记录相关的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface ProjectDownloadRepository extends BaseRepository<ProjectDownload, Long> {

    /**
     * 根据项目ID查找下载记录
     */
    List<ProjectDownload> findByProjectId(Long projectId);

    /**
     * 根据项目ID分页查找下载记录
     */
    Page<ProjectDownload> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 根据用户ID查找下载记录
     */
    List<ProjectDownload> findByUserId(Long userId);

    /**
     * 根据用户ID分页查找下载记录
     */
    Page<ProjectDownload> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据用户ID和项目ID查找下载记录
     */
    List<ProjectDownload> findByUserIdAndProjectId(Long userId, Long projectId);

    /**
     * 根据用户ID和项目ID查找最新的下载记录
     */
    Optional<ProjectDownload> findFirstByUserIdAndProjectIdOrderByDownloadTimeDesc(Long userId, Long projectId);

    /**
     * 根据下载状态查找记录
     */
    List<ProjectDownload> findByDownloadStatus(Integer downloadStatus);

    /**
     * 根据下载状态分页查找记录
     */
    Page<ProjectDownload> findByDownloadStatus(Integer downloadStatus, Pageable pageable);

    /**
     * 根据下载来源查找记录
     */
    List<ProjectDownload> findByDownloadSource(String downloadSource);

    /**
     * 根据下载来源分页查找记录
     */
    Page<ProjectDownload> findByDownloadSource(String downloadSource, Pageable pageable);

    /**
     * 查找指定时间范围内的下载记录
     */
    @Query("SELECT pd FROM ProjectDownload pd WHERE pd.downloadTime BETWEEN :startTime AND :endTime")
    List<ProjectDownload> findByDownloadTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定时间范围内的下载记录（分页）
     */
    @Query("SELECT pd FROM ProjectDownload pd WHERE pd.downloadTime BETWEEN :startTime AND :endTime")
    Page<ProjectDownload> findByDownloadTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime, Pageable pageable);

    /**
     * 查找重复下载记录
     */
    @Query("SELECT pd FROM ProjectDownload pd WHERE pd.isRepeat = true")
    List<ProjectDownload> findRepeatDownloads();

    /**
     * 查找完成的下载记录
     */
    @Query("SELECT pd FROM ProjectDownload pd WHERE pd.downloadStatus = 1")
    List<ProjectDownload> findCompletedDownloads();

    /**
     * 查找失败的下载记录
     */
    @Query("SELECT pd FROM ProjectDownload pd WHERE pd.downloadStatus = 2")
    List<ProjectDownload> findFailedDownloads();

    /**
     * 查找进行中的下载记录
     */
    @Query("SELECT pd FROM ProjectDownload pd WHERE pd.downloadStatus = 0")
    List<ProjectDownload> findOngoingDownloads();

    /**
     * 根据IP地址查找下载记录
     */
    List<ProjectDownload> findByDownloadIp(String downloadIp);

    /**
     * 根据IP地址和时间范围查找下载记录
     */
    @Query("SELECT pd FROM ProjectDownload pd WHERE pd.downloadIp = :ip AND pd.downloadTime BETWEEN :startTime AND :endTime")
    List<ProjectDownload> findByDownloadIpAndTimeRange(@Param("ip") String downloadIp, 
                                                      @Param("startTime") LocalDateTime startTime, 
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计项目的下载次数
     */
    @Query("SELECT COUNT(pd) FROM ProjectDownload pd WHERE pd.projectId = :projectId AND pd.downloadStatus = 1")
    Long countCompletedDownloadsByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计用户的下载次数
     */
    @Query("SELECT COUNT(pd) FROM ProjectDownload pd WHERE pd.userId = :userId AND pd.downloadStatus = 1")
    Long countCompletedDownloadsByUserId(@Param("userId") Long userId);

    /**
     * 统计指定时间范围内的下载次数
     */
    @Query("SELECT COUNT(pd) FROM ProjectDownload pd WHERE pd.downloadTime BETWEEN :startTime AND :endTime AND pd.downloadStatus = 1")
    Long countCompletedDownloadsByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计项目的总下载大小
     */
    @Query("SELECT COALESCE(SUM(pd.fileSize), 0) FROM ProjectDownload pd WHERE pd.projectId = :projectId AND pd.downloadStatus = 1")
    Long sumDownloadSizeByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计用户的总下载大小
     */
    @Query("SELECT COALESCE(SUM(pd.fileSize), 0) FROM ProjectDownload pd WHERE pd.userId = :userId AND pd.downloadStatus = 1")
    Long sumDownloadSizeByUserId(@Param("userId") Long userId);

    /**
     * 统计所有完成下载的总大小
     */
    @Query("SELECT COALESCE(SUM(pd.fileSize), 0) FROM ProjectDownload pd WHERE pd.downloadStatus = 1")
    Long sumTotalDownloadSize();

    /**
     * 计算平均下载耗时
     */
    @Query("SELECT AVG(pd.downloadDuration) FROM ProjectDownload pd WHERE pd.downloadStatus = 1 AND pd.downloadDuration IS NOT NULL")
    Double averageDownloadDuration();

    /**
     * 根据项目ID计算平均下载耗时
     */
    @Query("SELECT AVG(pd.downloadDuration) FROM ProjectDownload pd WHERE pd.projectId = :projectId AND pd.downloadStatus = 1 AND pd.downloadDuration IS NOT NULL")
    Double averageDownloadDurationByProjectId(@Param("projectId") Long projectId);

    /**
     * 查找下载耗时最长的记录
     */
    @Query("SELECT pd FROM ProjectDownload pd WHERE pd.downloadStatus = 1 ORDER BY pd.downloadDuration DESC")
    Page<ProjectDownload> findSlowestDownloads(Pageable pageable);

    /**
     * 查找下载最频繁的项目
     */
    @Query("SELECT pd.projectId, COUNT(pd) as downloadCount FROM ProjectDownload pd WHERE pd.downloadStatus = 1 GROUP BY pd.projectId ORDER BY downloadCount DESC")
    Page<Object[]> findMostDownloadedProjects(Pageable pageable);

    /**
     * 查找下载最活跃的用户
     */
    @Query("SELECT pd.userId, COUNT(pd) as downloadCount FROM ProjectDownload pd WHERE pd.downloadStatus = 1 GROUP BY pd.userId ORDER BY downloadCount DESC")
    Page<Object[]> findMostActiveDownloaders(Pageable pageable);

    /**
     * 检查用户是否已下载过项目
     */
    boolean existsByUserIdAndProjectIdAndDownloadStatus(Long userId, Long projectId, Integer downloadStatus);

    /**
     * 检查用户是否已成功下载过项目
     */
    @Query("SELECT COUNT(pd) > 0 FROM ProjectDownload pd WHERE pd.userId = :userId AND pd.projectId = :projectId AND pd.downloadStatus = 1")
    boolean hasUserDownloadedProject(@Param("userId") Long userId, @Param("projectId") Long projectId);

    /**
     * 更新下载状态
     */
    @Modifying
    @Query("UPDATE ProjectDownload pd SET pd.downloadStatus = :status WHERE pd.id = :downloadId")
    void updateDownloadStatus(@Param("downloadId") Long downloadId, @Param("status") Integer status);

    /**
     * 更新下载耗时
     */
    @Modifying
    @Query("UPDATE ProjectDownload pd SET pd.downloadDuration = :duration WHERE pd.id = :downloadId")
    void updateDownloadDuration(@Param("downloadId") Long downloadId, @Param("duration") Long duration);

    /**
     * 标记为重复下载
     */
    @Modifying
    @Query("UPDATE ProjectDownload pd SET pd.isRepeat = true WHERE pd.id = :downloadId")
    void markAsRepeat(@Param("downloadId") Long downloadId);

    /**
     * 清理过期的下载记录
     */
    @Modifying
    @Query("DELETE FROM ProjectDownload pd WHERE pd.downloadTime < :cutoffTime AND pd.downloadStatus IN (0, 2, 3)")
    void cleanupExpiredDownloads(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 查找最近的下载记录
     */
    @Query("SELECT pd FROM ProjectDownload pd ORDER BY pd.downloadTime DESC")
    Page<ProjectDownload> findRecentDownloads(Pageable pageable);

    /**
     * 根据下载来源统计下载次数
     */
    @Query("SELECT pd.downloadSource, COUNT(pd) FROM ProjectDownload pd WHERE pd.downloadStatus = 1 GROUP BY pd.downloadSource")
    List<Object[]> countDownloadsBySource();

    /**
     * 按日期统计下载次数
     */
    @Query("SELECT DATE(pd.downloadTime), COUNT(pd) FROM ProjectDownload pd WHERE pd.downloadStatus = 1 AND pd.downloadTime BETWEEN :startTime AND :endTime GROUP BY DATE(pd.downloadTime) ORDER BY DATE(pd.downloadTime)")
    List<Object[]> countDownloadsByDate(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 按小时统计下载次数
     */
    @Query("SELECT HOUR(pd.downloadTime), COUNT(pd) FROM ProjectDownload pd WHERE pd.downloadStatus = 1 AND pd.downloadTime BETWEEN :startTime AND :endTime GROUP BY HOUR(pd.downloadTime) ORDER BY HOUR(pd.downloadTime)")
    List<Object[]> countDownloadsByHour(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 查找用户最近下载的项目
     */
    @Query("SELECT pd FROM ProjectDownload pd WHERE pd.userId = :userId AND pd.downloadStatus = 1 ORDER BY pd.downloadTime DESC")
    Page<ProjectDownload> findUserRecentDownloads(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找项目的最新下载记录
     */
    @Query("SELECT pd FROM ProjectDownload pd WHERE pd.projectId = :projectId AND pd.downloadStatus = 1 ORDER BY pd.downloadTime DESC")
    Page<ProjectDownload> findProjectRecentDownloads(@Param("projectId") Long projectId, Pageable pageable);

    /**
     * 查找下载排行用户
     */
    @Query("SELECT pd.userId, COUNT(pd) as downloadCount FROM ProjectDownload pd WHERE pd.downloadStatus = 1 AND pd.downloadTime >= :startTime GROUP BY pd.userId ORDER BY downloadCount DESC")
    Page<Object[]> findTopDownloaders(@Param("startTime") LocalDateTime startTime, Pageable pageable);

    /**
     * 获取下载趋势数据
     */
    @Query("SELECT DATE(pd.downloadTime) as downloadDate, COUNT(pd) as downloadCount FROM ProjectDownload pd WHERE pd.downloadStatus = 1 AND pd.downloadTime >= :startTime GROUP BY DATE(pd.downloadTime) ORDER BY downloadDate")
    List<Object[]> findDownloadTrends(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取项目下载趋势数据
     */
    @Query("SELECT DATE(pd.downloadTime) as downloadDate, COUNT(pd) as downloadCount FROM ProjectDownload pd WHERE pd.projectId = :projectId AND pd.downloadStatus = 1 AND pd.downloadTime >= :startTime GROUP BY DATE(pd.downloadTime) ORDER BY downloadDate")
    List<Object[]> findProjectDownloadTrends(@Param("projectId") Long projectId, @Param("startTime") LocalDateTime startTime);

    /**
     * 检测异常下载行为
     */
    @Query("SELECT COUNT(pd) FROM ProjectDownload pd WHERE (pd.userId = :userId OR pd.downloadIp = :clientIp) AND pd.downloadTime >= :startTime")
    Long countDownloadsInTimeWindow(@Param("userId") Long userId, @Param("clientIp") String clientIp, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取下载来源统计
     */
    @Query("SELECT pd.downloadSource, COUNT(pd) as downloadCount FROM ProjectDownload pd WHERE pd.downloadStatus = 1 AND pd.downloadTime >= :startTime GROUP BY pd.downloadSource ORDER BY downloadCount DESC")
    List<Object[]> findDownloadsBySourceInPeriod(@Param("startTime") LocalDateTime startTime);

    /**
     * 获取用户下载统计
     */
    @Query("SELECT COUNT(DISTINCT pd.projectId) as uniqueProjects, COUNT(pd) as totalDownloads, SUM(pd.fileSize) as totalSize FROM ProjectDownload pd WHERE pd.userId = :userId AND pd.downloadStatus = 1")
    Object[] getUserDownloadStatistics(@Param("userId") Long userId);

    /**
     * 获取项目下载统计
     */
    @Query("SELECT COUNT(DISTINCT pd.userId) as uniqueDownloaders, COUNT(pd) as totalDownloads, SUM(pd.fileSize) as totalSize, AVG(pd.downloadDuration) as avgDuration FROM ProjectDownload pd WHERE pd.projectId = :projectId AND pd.downloadStatus = 1")
    Object[] getProjectDownloadStatistics(@Param("projectId") Long projectId);

    /**
     * 根据用户ID查找下载记录，按下载时间倒序
     */
    Page<ProjectDownload> findByUserIdOrderByDownloadTimeDesc(Long userId, Pageable pageable);

    /**
     * 统计指定时间后的总下载数
     */
    @Query("SELECT COUNT(pd) FROM ProjectDownload pd WHERE pd.downloadTime >= :startTime AND pd.downloadStatus = :status")
    Long countByDownloadTimeGreaterThanEqualAndDownloadStatus(@Param("startTime") LocalDateTime startTime, @Param("status") Integer status);

    /**
     * 统计指定时间后的唯一用户数
     */
    @Query("SELECT COUNT(DISTINCT pd.userId) FROM ProjectDownload pd WHERE pd.downloadTime >= :startTime AND pd.downloadStatus = 1")
    Long countDistinctUsersByDownloadTimeGreaterThanEqual(@Param("startTime") LocalDateTime startTime);

    /**
     * 统计指定时间后的唯一项目数
     */
    @Query("SELECT COUNT(DISTINCT pd.projectId) FROM ProjectDownload pd WHERE pd.downloadTime >= :startTime AND pd.downloadStatus = 1")
    Long countDistinctProjectsByDownloadTimeGreaterThanEqual(@Param("startTime") LocalDateTime startTime);

    /**
     * 根据ID列表和用户ID查找下载记录
     */
    List<ProjectDownload> findByIdInAndUserId(List<Long> ids, Long userId);

    /**
     * 统计用户的下载记录数量
     */
    long countByUserId(Long userId);

    /**
     * 删除用户的所有下载记录
     */
    @Modifying
    @Query("DELETE FROM ProjectDownload pd WHERE pd.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
