package com.quickcode.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.quickcode.entity.Review;

/**
 * 项目评价Repository接口
 * 提供评价相关的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface ReviewRepository extends BaseRepository<Review, Long> {

    /**
     * 根据项目ID查找评价
     */
    List<Review> findByProjectId(Long projectId);

    /**
     * 根据项目ID分页查找评价
     */
    Page<Review> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 根据用户ID查找评价
     */
    List<Review> findByUserId(Long userId);

    /**
     * 根据用户ID分页查找评价
     */
    Page<Review> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据项目ID和用户ID查找评价
     */
    Optional<Review> findByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * 检查用户是否已评价项目
     */
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    /**
     * 根据状态查找评价
     */
    List<Review> findByStatus(Integer status);

    /**
     * 根据状态分页查找评价
     */
    Page<Review> findByStatus(Integer status, Pageable pageable);

    /**
     * 查找已发布的评价
     */
    @Query("SELECT r FROM Review r WHERE r.status = 1")
    List<Review> findPublishedReviews();

    /**
     * 分页查找已发布的评价
     */
    @Query("SELECT r FROM Review r WHERE r.status = 1")
    Page<Review> findPublishedReviews(Pageable pageable);

    /**
     * 根据项目ID查找已发布的评价
     */
    @Query("SELECT r FROM Review r WHERE r.projectId = :projectId AND r.status = 1")
    List<Review> findPublishedReviewsByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID分页查找已发布的评价
     */
    @Query("SELECT r FROM Review r WHERE r.projectId = :projectId AND r.status = 1")
    Page<Review> findPublishedReviewsByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    /**
     * 根据用户ID查找已发布的评价
     */
    @Query("SELECT r FROM Review r WHERE r.userId = :userId AND r.status = 1")
    List<Review> findPublishedReviewsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID分页查找已发布的评价
     */
    @Query("SELECT r FROM Review r WHERE r.userId = :userId AND r.status = 1")
    Page<Review> findPublishedReviewsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据评分范围查找评价
     */
    @Query("SELECT r FROM Review r WHERE r.rating BETWEEN :minRating AND :maxRating AND r.status = 1")
    List<Review> findByRatingRange(@Param("minRating") BigDecimal minRating, 
                                   @Param("maxRating") BigDecimal maxRating);

    /**
     * 根据评分范围分页查找评价
     */
    @Query("SELECT r FROM Review r WHERE r.rating BETWEEN :minRating AND :maxRating AND r.status = 1")
    Page<Review> findByRatingRange(@Param("minRating") BigDecimal minRating, 
                                   @Param("maxRating") BigDecimal maxRating, 
                                   Pageable pageable);

    /**
     * 根据项目ID和评分范围查找评价
     */
    @Query("SELECT r FROM Review r WHERE r.projectId = :projectId AND r.rating BETWEEN :minRating AND :maxRating AND r.status = 1")
    List<Review> findByProjectIdAndRatingRange(@Param("projectId") Long projectId,
                                               @Param("minRating") BigDecimal minRating, 
                                               @Param("maxRating") BigDecimal maxRating);

    /**
     * 查找高评分评价
     */
    @Query("SELECT r FROM Review r WHERE r.rating >= :minRating AND r.status = 1 ORDER BY r.rating DESC")
    List<Review> findHighRatedReviews(@Param("minRating") BigDecimal minRating, Pageable pageable);

    /**
     * 查找最新评价
     */
    @Query("SELECT r FROM Review r WHERE r.status = 1 ORDER BY r.createdTime DESC")
    List<Review> findLatestReviews(Pageable pageable);

    /**
     * 根据项目ID查找最新评价
     */
    @Query("SELECT r FROM Review r WHERE r.projectId = :projectId AND r.status = 1 ORDER BY r.createdTime DESC")
    List<Review> findLatestReviewsByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    /**
     * 查找热门评价（按点赞数排序）
     */
    @Query("SELECT r FROM Review r WHERE r.status = 1 ORDER BY r.likeCount DESC")
    List<Review> findPopularReviews(Pageable pageable);

    /**
     * 根据项目ID查找热门评价
     */
    @Query("SELECT r FROM Review r WHERE r.projectId = :projectId AND r.status = 1 ORDER BY r.likeCount DESC")
    List<Review> findPopularReviewsByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    /**
     * 查找有内容的评价
     */
    @Query("SELECT r FROM Review r WHERE r.content IS NOT NULL AND r.content != '' AND r.status = 1")
    List<Review> findReviewsWithContent();

    /**
     * 根据项目ID查找有内容的评价
     */
    @Query("SELECT r FROM Review r WHERE r.projectId = :projectId AND r.content IS NOT NULL AND r.content != '' AND r.status = 1")
    List<Review> findReviewsWithContentByProjectId(@Param("projectId") Long projectId);

    /**
     * 查找匿名评价
     */
    @Query("SELECT r FROM Review r WHERE r.isAnonymous = true AND r.status = 1")
    List<Review> findAnonymousReviews();

    /**
     * 根据创建时间范围查找评价
     */
    @Query("SELECT r FROM Review r WHERE r.createdTime BETWEEN :startTime AND :endTime AND r.status = 1")
    List<Review> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 根据内容搜索评价
     */
    @Query("SELECT r FROM Review r WHERE r.content LIKE %:keyword% AND r.status = 1")
    List<Review> searchByContent(@Param("keyword") String keyword);

    /**
     * 根据内容搜索评价（分页）
     */
    @Query("SELECT r FROM Review r WHERE r.content LIKE %:keyword% AND r.status = 1")
    Page<Review> searchByContent(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计评价总数
     */
    @Query("SELECT COUNT(r) FROM Review r")
    Long countAllReviews();

    /**
     * 根据状态统计评价数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.status = :status")
    Long countByStatus(@Param("status") Integer status);

    /**
     * 统计已发布评价数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.status = 1")
    Long countPublishedReviews();

    /**
     * 根据项目ID统计评价数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.projectId = :projectId AND r.status = 1")
    Long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据用户ID统计评价数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.userId = :userId AND r.status = 1")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 计算项目平均评分
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.projectId = :projectId AND r.status = 1")
    BigDecimal calculateAverageRatingByProjectId(@Param("projectId") Long projectId);

    /**
     * 计算用户平均评分
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.userId = :userId AND r.status = 1")
    BigDecimal calculateAverageRatingByUserId(@Param("userId") Long userId);

    /**
     * 根据项目ID和评分统计评价数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.projectId = :projectId AND r.rating = :rating AND r.status = 1")
    Long countByProjectIdAndRating(@Param("projectId") Long projectId, @Param("rating") BigDecimal rating);

    /**
     * 根据项目ID获取评分分布
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.projectId = :projectId AND r.status = 1 GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计匿名评价数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.isAnonymous = true AND r.status = 1")
    Long countAnonymousReviews();

    /**
     * 统计有内容的评价数量
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.content IS NOT NULL AND r.content != '' AND r.status = 1")
    Long countReviewsWithContent();

    /**
     * 查找需要审核的评价
     */
    @Query("SELECT r FROM Review r WHERE r.status = 0 ORDER BY r.createdTime ASC")
    List<Review> findPendingReviews();

    /**
     * 分页查找需要审核的评价
     */
    @Query("SELECT r FROM Review r WHERE r.status = 0 ORDER BY r.createdTime ASC")
    Page<Review> findPendingReviews(Pageable pageable);

    /**
     * 增加评价点赞数
     */
    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    void incrementLikeCount(@Param("reviewId") Long reviewId);

    /**
     * 减少评价点赞数
     */
    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :reviewId AND r.likeCount > 0")
    void decrementLikeCount(@Param("reviewId") Long reviewId);

    /**
     * 根据项目ID删除所有评价
     */
    @Modifying
    @Query("DELETE FROM Review r WHERE r.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据用户ID删除所有评价
     */
    @Modifying
    @Query("DELETE FROM Review r WHERE r.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
