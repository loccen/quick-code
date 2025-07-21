package com.quickcode.service;

import com.quickcode.dto.review.ReviewCreateRequest;
import com.quickcode.dto.review.ReviewDTO;
import com.quickcode.dto.review.ReviewSummaryDTO;
import com.quickcode.dto.common.PageResponse;
import com.quickcode.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 评价Service接口
 * 提供评价相关的业务逻辑方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface ReviewService extends BaseService<Review, Long> {

    /**
     * 创建评价
     */
    ReviewDTO createReview(ReviewCreateRequest request, Long userId);

    /**
     * 更新评价
     */
    ReviewDTO updateReview(Long reviewId, ReviewCreateRequest request, Long userId);

    /**
     * 根据ID获取评价详情
     */
    ReviewDTO getReviewDetail(Long reviewId);

    /**
     * 根据项目ID获取评价列表
     */
    PageResponse<ReviewDTO> getReviewsByProject(Long projectId, Pageable pageable);

    /**
     * 根据项目ID获取已发布的评价列表
     */
    PageResponse<ReviewDTO> getPublishedReviewsByProject(Long projectId, Pageable pageable);

    /**
     * 根据用户ID获取评价列表
     */
    PageResponse<ReviewDTO> getReviewsByUser(Long userId, Pageable pageable);

    /**
     * 根据用户ID获取已发布的评价列表
     */
    PageResponse<ReviewDTO> getPublishedReviewsByUser(Long userId, Pageable pageable);

    /**
     * 获取最新评价列表
     */
    List<ReviewDTO> getLatestReviews(int limit);

    /**
     * 根据项目ID获取最新评价列表
     */
    List<ReviewDTO> getLatestReviewsByProject(Long projectId, int limit);

    /**
     * 获取热门评价列表（按点赞数排序）
     */
    List<ReviewDTO> getPopularReviews(int limit);

    /**
     * 根据项目ID获取热门评价列表
     */
    List<ReviewDTO> getPopularReviewsByProject(Long projectId, int limit);

    /**
     * 获取高评分评价列表
     */
    List<ReviewDTO> getHighRatedReviews(BigDecimal minRating, int limit);

    /**
     * 根据评分范围获取评价列表
     */
    PageResponse<ReviewDTO> getReviewsByRatingRange(BigDecimal minRating, BigDecimal maxRating, Pageable pageable);

    /**
     * 根据项目ID和评分范围获取评价列表
     */
    List<ReviewDTO> getReviewsByProjectAndRatingRange(Long projectId, BigDecimal minRating, BigDecimal maxRating);

    /**
     * 获取有内容的评价列表
     */
    List<ReviewDTO> getReviewsWithContent();

    /**
     * 根据项目ID获取有内容的评价列表
     */
    List<ReviewDTO> getReviewsWithContentByProject(Long projectId);

    /**
     * 获取匿名评价列表
     */
    List<ReviewDTO> getAnonymousReviews();

    /**
     * 根据内容搜索评价
     */
    PageResponse<ReviewDTO> searchReviewsByContent(String keyword, Pageable pageable);

    /**
     * 根据创建时间范围获取评价列表
     */
    List<ReviewDTO> getReviewsByDateRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 发布评价
     */
    void publishReview(Long reviewId, Long adminUserId);

    /**
     * 隐藏评价
     */
    void hideReview(Long reviewId, Long adminUserId);

    /**
     * 删除评价
     */
    void deleteReview(Long reviewId, Long userId);

    /**
     * 增加评价点赞数
     */
    void incrementLikeCount(Long reviewId, Long userId);

    /**
     * 减少评价点赞数
     */
    void decrementLikeCount(Long reviewId, Long userId);

    /**
     * 检查用户是否已点赞评价
     */
    boolean isLikedByUser(Long reviewId, Long userId);

    /**
     * 检查用户是否已评价项目
     */
    boolean hasUserReviewedProject(Long projectId, Long userId);

    /**
     * 获取用户对项目的评价
     */
    Optional<ReviewDTO> getUserReviewForProject(Long projectId, Long userId);

    /**
     * 检查用户是否可以编辑评价
     */
    boolean canEditReview(Long reviewId, Long userId);

    /**
     * 检查用户是否可以删除评价
     */
    boolean canDeleteReview(Long reviewId, Long userId);

    /**
     * 获取项目评价摘要
     */
    ReviewSummaryDTO getProjectReviewSummary(Long projectId);

    /**
     * 计算项目平均评分
     */
    BigDecimal calculateAverageRating(Long projectId);

    /**
     * 根据项目ID统计评价数量
     */
    long countReviewsByProject(Long projectId);

    /**
     * 根据用户ID统计评价数量
     */
    long countReviewsByUser(Long userId);

    /**
     * 根据状态统计评价数量
     */
    long countReviewsByStatus(Integer status);

    /**
     * 统计已发布评价数量
     */
    long countPublishedReviews();

    /**
     * 统计匿名评价数量
     */
    long countAnonymousReviews();

    /**
     * 统计有内容的评价数量
     */
    long countReviewsWithContent();

    /**
     * 获取待审核评价列表
     */
    PageResponse<ReviewDTO> getPendingReviews(Pageable pageable);

    /**
     * 获取需要审核的评价数量
     */
    long countPendingReviews();

    /**
     * 根据项目ID和评分统计评价数量
     */
    long countReviewsByProjectAndRating(Long projectId, BigDecimal rating);

    /**
     * 根据项目ID获取评分分布
     */
    List<RatingDistribution> getRatingDistributionByProject(Long projectId);

    /**
     * 批量审核评价
     */
    void batchApproveReviews(List<Long> reviewIds, Long adminUserId);

    /**
     * 批量隐藏评价
     */
    void batchHideReviews(List<Long> reviewIds, Long adminUserId);

    /**
     * 批量删除评价
     */
    void batchDeleteReviews(List<Long> reviewIds, Long adminUserId);

    /**
     * 根据项目ID删除所有评价
     */
    void deleteAllReviewsByProject(Long projectId);

    /**
     * 根据用户ID删除所有评价
     */
    void deleteAllReviewsByUser(Long userId);

    /**
     * 获取评价统计信息
     */
    ReviewStatistics getReviewStatistics();

    /**
     * 获取用户评价统计信息
     */
    UserReviewStatistics getUserReviewStatistics(Long userId);

    /**
     * 获取项目评价统计信息
     */
    ProjectReviewStatistics getProjectReviewStatistics(Long projectId);

    /**
     * 评分分布接口
     */
    interface RatingDistribution {
        BigDecimal getRating();
        Long getCount();
        Double getPercentage();
    }

    /**
     * 评价统计信息接口
     */
    interface ReviewStatistics {
        long getTotalReviews();
        long getPublishedReviews();
        long getPendingReviews();
        long getAnonymousReviews();
        long getReviewsWithContent();
        BigDecimal getAverageRating();
        long getTotalLikes();
    }

    /**
     * 用户评价统计信息接口
     */
    interface UserReviewStatistics {
        long getTotalReviews();
        long getPublishedReviews();
        long getPendingReviews();
        BigDecimal getAverageRating();
        long getTotalLikes();
        long getProjectsReviewed();
    }

    /**
     * 项目评价统计信息接口
     */
    interface ProjectReviewStatistics {
        long getTotalReviews();
        long getPublishedReviews();
        BigDecimal getAverageRating();
        long getTotalLikes();
        long getReviewsWithContent();
        BigDecimal getPositiveRate();
        List<RatingDistribution> getRatingDistribution();
    }
}
