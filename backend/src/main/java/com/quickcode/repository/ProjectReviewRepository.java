package com.quickcode.repository;

import com.quickcode.entity.ProjectReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目审核记录Repository接口
 * 提供项目审核记录的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface ProjectReviewRepository extends JpaRepository<ProjectReview, Long> {

    /**
     * 根据项目ID查找审核记录
     */
    List<ProjectReview> findByProjectIdOrderByCreatedTimeDesc(Long projectId);

    /**
     * 分页查找项目的审核记录
     */
    Page<ProjectReview> findByProjectIdOrderByCreatedTimeDesc(Long projectId, Pageable pageable);

    /**
     * 根据审核员ID查找审核记录
     */
    List<ProjectReview> findByReviewerIdOrderByCreatedTimeDesc(Long reviewerId);

    /**
     * 分页查找审核员的审核记录
     */
    Page<ProjectReview> findByReviewerIdOrderByCreatedTimeDesc(Long reviewerId, Pageable pageable);

    /**
     * 根据审核动作查找记录
     */
    List<ProjectReview> findByReviewActionOrderByCreatedTimeDesc(ProjectReview.ReviewAction reviewAction);

    /**
     * 分页查找指定审核动作的记录
     */
    Page<ProjectReview> findByReviewActionOrderByCreatedTimeDesc(ProjectReview.ReviewAction reviewAction, Pageable pageable);

    /**
     * 查找指定时间范围内的审核记录
     */
    @Query("SELECT pr FROM ProjectReview pr WHERE pr.createdTime BETWEEN :startTime AND :endTime ORDER BY pr.createdTime DESC")
    List<ProjectReview> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 分页查找指定时间范围内的审核记录
     */
    @Query("SELECT pr FROM ProjectReview pr WHERE pr.createdTime BETWEEN :startTime AND :endTime ORDER BY pr.createdTime DESC")
    Page<ProjectReview> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime, 
                                               Pageable pageable);

    /**
     * 统计审核员的审核数量
     */
    @Query("SELECT COUNT(pr) FROM ProjectReview pr WHERE pr.reviewerId = :reviewerId")
    long countByReviewerId(@Param("reviewerId") Long reviewerId);

    /**
     * 统计指定审核动作的数量
     */
    @Query("SELECT COUNT(pr) FROM ProjectReview pr WHERE pr.reviewAction = :reviewAction")
    long countByReviewAction(@Param("reviewAction") ProjectReview.ReviewAction reviewAction);

    /**
     * 统计指定时间范围内的审核数量
     */
    @Query("SELECT COUNT(pr) FROM ProjectReview pr WHERE pr.createdTime BETWEEN :startTime AND :endTime")
    long countByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查找项目的最新审核记录
     */
    @Query("SELECT pr FROM ProjectReview pr WHERE pr.projectId = :projectId ORDER BY pr.createdTime DESC LIMIT 1")
    ProjectReview findLatestByProjectId(@Param("projectId") Long projectId);

    /**
     * 查找审核员今日的审核记录
     */
    @Query("SELECT pr FROM ProjectReview pr WHERE pr.reviewerId = :reviewerId " +
           "AND DATE(pr.createdTime) = CURRENT_DATE ORDER BY pr.createdTime DESC")
    List<ProjectReview> findTodayReviewsByReviewerId(@Param("reviewerId") Long reviewerId);

    /**
     * 统计审核员今日的审核数量
     */
    @Query("SELECT COUNT(pr) FROM ProjectReview pr WHERE pr.reviewerId = :reviewerId " +
           "AND DATE(pr.createdTime) = CURRENT_DATE")
    long countTodayReviewsByReviewerId(@Param("reviewerId") Long reviewerId);

    /**
     * 查找需要重新审核的项目（被拒绝后重新提交的）
     */
    @Query("SELECT DISTINCT pr.projectId FROM ProjectReview pr WHERE pr.reviewAction = 'REJECT' " +
           "AND pr.projectId IN (SELECT p.id FROM Project p WHERE p.status = 0)")
    List<Long> findProjectsNeedingReReview();

    /**
     * 查找活跃的审核员（最近30天有审核记录）
     */
    @Query("SELECT DISTINCT pr.reviewerId FROM ProjectReview pr " +
           "WHERE pr.createdTime >= :thirtyDaysAgo")
    List<Long> findActiveReviewers(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
}
