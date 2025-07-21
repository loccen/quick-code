package com.quickcode.repository;

import com.quickcode.entity.ProjectAuditLog;
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
 * 项目审核日志Repository接口
 * 提供项目审核日志相关的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface ProjectAuditLogRepository extends BaseRepository<ProjectAuditLog, Long> {

    /**
     * 根据项目ID查找审核日志
     */
    List<ProjectAuditLog> findByProjectId(Long projectId);

    /**
     * 根据项目ID分页查找审核日志
     */
    Page<ProjectAuditLog> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 根据项目ID按时间倒序查找审核日志
     */
    List<ProjectAuditLog> findByProjectIdOrderByAuditTimeDesc(Long projectId);

    /**
     * 根据审核人员ID查找审核日志
     */
    List<ProjectAuditLog> findByAuditorId(Long auditorId);

    /**
     * 根据审核人员ID分页查找审核日志
     */
    Page<ProjectAuditLog> findByAuditorId(Long auditorId, Pageable pageable);

    /**
     * 根据审核动作查找日志
     */
    List<ProjectAuditLog> findByAuditAction(String auditAction);

    /**
     * 根据审核动作分页查找日志
     */
    Page<ProjectAuditLog> findByAuditAction(String auditAction, Pageable pageable);

    /**
     * 根据审核结果查找日志
     */
    List<ProjectAuditLog> findByAuditResult(String auditResult);

    /**
     * 根据审核结果分页查找日志
     */
    Page<ProjectAuditLog> findByAuditResult(String auditResult, Pageable pageable);

    /**
     * 查找指定时间范围内的审核日志
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.auditTime BETWEEN :startTime AND :endTime")
    List<ProjectAuditLog> findByAuditTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定时间范围内的审核日志（分页）
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.auditTime BETWEEN :startTime AND :endTime")
    Page<ProjectAuditLog> findByAuditTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime, Pageable pageable);

    /**
     * 查找自动审核日志
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.isAuto = true")
    List<ProjectAuditLog> findAutoAuditLogs();

    /**
     * 查找人工审核日志
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.isAuto = false")
    List<ProjectAuditLog> findManualAuditLogs();

    /**
     * 根据项目ID查找最新的审核日志
     */
    Optional<ProjectAuditLog> findFirstByProjectIdOrderByAuditTimeDesc(Long projectId);

    /**
     * 根据项目ID和审核动作查找最新的日志
     */
    Optional<ProjectAuditLog> findFirstByProjectIdAndAuditActionOrderByAuditTimeDesc(Long projectId, String auditAction);

    /**
     * 根据项目ID和审核结果查找最新的日志
     */
    Optional<ProjectAuditLog> findFirstByProjectIdAndAuditResultOrderByAuditTimeDesc(Long projectId, String auditResult);

    /**
     * 查找审核通过的日志
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.auditResult = 'APPROVED'")
    List<ProjectAuditLog> findApprovedLogs();

    /**
     * 查找审核拒绝的日志
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.auditResult = 'REJECTED'")
    List<ProjectAuditLog> findRejectedLogs();

    /**
     * 查找发布的日志
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.auditResult = 'PUBLISHED'")
    List<ProjectAuditLog> findPublishedLogs();

    /**
     * 查找下架的日志
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.auditResult = 'OFFLINE'")
    List<ProjectAuditLog> findOfflineLogs();

    /**
     * 根据IP地址查找审核日志
     */
    List<ProjectAuditLog> findByAuditIp(String auditIp);

    /**
     * 根据IP地址和时间范围查找审核日志
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.auditIp = :ip AND pal.auditTime BETWEEN :startTime AND :endTime")
    List<ProjectAuditLog> findByAuditIpAndTimeRange(@Param("ip") String auditIp, 
                                                   @Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计项目的审核次数
     */
    @Query("SELECT COUNT(pal) FROM ProjectAuditLog pal WHERE pal.projectId = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计审核人员的审核次数
     */
    @Query("SELECT COUNT(pal) FROM ProjectAuditLog pal WHERE pal.auditorId = :auditorId")
    Long countByAuditorId(@Param("auditorId") Long auditorId);

    /**
     * 统计指定动作的审核次数
     */
    @Query("SELECT COUNT(pal) FROM ProjectAuditLog pal WHERE pal.auditAction = :action")
    Long countByAuditAction(@Param("action") String auditAction);

    /**
     * 统计指定结果的审核次数
     */
    @Query("SELECT COUNT(pal) FROM ProjectAuditLog pal WHERE pal.auditResult = :result")
    Long countByAuditResult(@Param("result") String auditResult);

    /**
     * 统计指定时间范围内的审核次数
     */
    @Query("SELECT COUNT(pal) FROM ProjectAuditLog pal WHERE pal.auditTime BETWEEN :startTime AND :endTime")
    Long countByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 计算平均审核耗时
     */
    @Query("SELECT AVG(pal.auditDuration) FROM ProjectAuditLog pal WHERE pal.auditDuration IS NOT NULL AND pal.isAuto = false")
    Double averageAuditDuration();

    /**
     * 根据审核人员计算平均审核耗时
     */
    @Query("SELECT AVG(pal.auditDuration) FROM ProjectAuditLog pal WHERE pal.auditorId = :auditorId AND pal.auditDuration IS NOT NULL")
    Double averageAuditDurationByAuditor(@Param("auditorId") Long auditorId);

    /**
     * 查找审核耗时最长的记录
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.auditDuration IS NOT NULL ORDER BY pal.auditDuration DESC")
    Page<ProjectAuditLog> findSlowestAudits(Pageable pageable);

    /**
     * 查找最活跃的审核人员
     */
    @Query("SELECT pal.auditorId, COUNT(pal) as auditCount FROM ProjectAuditLog pal WHERE pal.auditorId IS NOT NULL GROUP BY pal.auditorId ORDER BY auditCount DESC")
    Page<Object[]> findMostActiveAuditors(Pageable pageable);

    /**
     * 按审核动作统计数量
     */
    @Query("SELECT pal.auditAction, COUNT(pal) FROM ProjectAuditLog pal GROUP BY pal.auditAction")
    List<Object[]> countByAuditActions();

    /**
     * 按审核结果统计数量
     */
    @Query("SELECT pal.auditResult, COUNT(pal) FROM ProjectAuditLog pal GROUP BY pal.auditResult")
    List<Object[]> countByAuditResults();

    /**
     * 按日期统计审核次数
     */
    @Query("SELECT DATE(pal.auditTime), COUNT(pal) FROM ProjectAuditLog pal WHERE pal.auditTime BETWEEN :startTime AND :endTime GROUP BY DATE(pal.auditTime) ORDER BY DATE(pal.auditTime)")
    List<Object[]> countAuditsByDate(@Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 按小时统计审核次数
     */
    @Query("SELECT HOUR(pal.auditTime), COUNT(pal) FROM ProjectAuditLog pal WHERE pal.auditTime BETWEEN :startTime AND :endTime GROUP BY HOUR(pal.auditTime) ORDER BY HOUR(pal.auditTime)")
    List<Object[]> countAuditsByHour(@Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 查找审核人员的最近审核记录
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.auditorId = :auditorId ORDER BY pal.auditTime DESC")
    Page<ProjectAuditLog> findAuditorRecentAudits(@Param("auditorId") Long auditorId, Pageable pageable);

    /**
     * 查找最近的审核记录
     */
    @Query("SELECT pal FROM ProjectAuditLog pal ORDER BY pal.auditTime DESC")
    Page<ProjectAuditLog> findRecentAudits(Pageable pageable);

    /**
     * 查找待审核的项目（最近提交审核但未处理的）
     */
    @Query("SELECT pal.projectId FROM ProjectAuditLog pal WHERE pal.auditAction = 'SUBMIT' AND pal.projectId NOT IN " +
           "(SELECT pal2.projectId FROM ProjectAuditLog pal2 WHERE pal2.auditAction IN ('APPROVE', 'REJECT') AND pal2.auditTime > pal.auditTime)")
    List<Long> findPendingAuditProjectIds();

    /**
     * 检查项目是否有指定的审核记录
     */
    boolean existsByProjectIdAndAuditAction(Long projectId, String auditAction);

    /**
     * 检查项目是否有指定的审核结果
     */
    boolean existsByProjectIdAndAuditResult(Long projectId, String auditResult);

    /**
     * 清理过期的审核日志
     */
    @Modifying
    @Query("DELETE FROM ProjectAuditLog pal WHERE pal.auditTime < :cutoffTime")
    void cleanupExpiredLogs(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 查找项目的审核历史（按时间正序）
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.projectId = :projectId ORDER BY pal.auditTime ASC")
    List<ProjectAuditLog> findProjectAuditHistory(@Param("projectId") Long projectId);

    /**
     * 查找项目状态变更历史
     */
    @Query("SELECT pal FROM ProjectAuditLog pal WHERE pal.projectId = :projectId AND pal.previousStatus IS NOT NULL AND pal.newStatus IS NOT NULL ORDER BY pal.auditTime ASC")
    List<ProjectAuditLog> findProjectStatusHistory(@Param("projectId") Long projectId);
}
