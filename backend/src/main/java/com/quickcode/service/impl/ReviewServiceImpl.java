package com.quickcode.service.impl;

import com.quickcode.dto.review.ReviewCreateRequest;
import com.quickcode.dto.review.ReviewDTO;
import com.quickcode.dto.review.ReviewSummaryDTO;
import com.quickcode.dto.common.PageResponse;
import com.quickcode.entity.Review;
import com.quickcode.entity.Project;
import com.quickcode.entity.User;
import com.quickcode.repository.ReviewRepository;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.repository.UserRepository;
import com.quickcode.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 评价服务实现类
 * 提供评价相关的业务逻辑实现
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewDTO createReview(ReviewCreateRequest request, Long userId) {
        log.debug("创建评价: projectId={}, userId={}, rating={}", request.getProjectId(), userId, request.getRating());

        // 验证请求数据
        request.validate();
        request.normalize();

        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 验证项目是否存在
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("项目不存在: " + request.getProjectId()));

        // 检查项目是否已发布
        if (!project.isPublished()) {
            throw new RuntimeException("只能评价已发布的项目");
        }

        // 检查用户是否已经评价过该项目
        if (hasUserReviewedProject(request.getProjectId(), userId)) {
            throw new RuntimeException("用户已经评价过该项目");
        }

        // 检查用户是否为项目作者（项目作者不能评价自己的项目）
        if (project.getUserId().equals(userId)) {
            throw new RuntimeException("项目作者不能评价自己的项目");
        }

        // 创建评价实体
        Review review = Review.builder()
                .projectId(request.getProjectId())
                .userId(userId)
                .rating(request.getRating())
                .content(request.getContent())
                .isAnonymous(request.getIsAnonymous())
                .status(0) // 待审核状态
                .build();

        // 保存评价
        review = reviewRepository.save(review);

        // 更新项目评分
        updateProjectRating(request.getProjectId());

        log.info("评价创建成功: id={}, projectId={}, userId={}, rating={}", 
                review.getId(), request.getProjectId(), userId, request.getRating());
        
        return ReviewDTO.fromReview(review);
    }

    @Override
    public ReviewDTO updateReview(Long reviewId, ReviewCreateRequest request, Long userId) {
        log.debug("更新评价: reviewId={}, userId={}", reviewId, userId);

        // 验证请求数据
        request.validate();
        request.normalize();

        // 获取评价
        Review review = getById(reviewId);

        // 检查权限
        if (!canEditReview(reviewId, userId)) {
            throw new RuntimeException("无权限编辑此评价");
        }

        // 更新评价信息
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setIsAnonymous(request.getIsAnonymous());

        // 如果评价已发布，更新后需要重新审核
        if (review.isPublished()) {
            review.setStatus(0); // 重新设为待审核状态
            log.info("已发布评价更新后重新进入审核状态: id={}", review.getId());
        }

        // 保存更新
        review = reviewRepository.save(review);

        // 更新项目评分
        updateProjectRating(review.getProjectId());

        log.info("评价更新成功: id={}, userId={}, rating={}", reviewId, userId, request.getRating());
        return ReviewDTO.fromReview(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDTO getReviewDetail(Long reviewId) {
        log.debug("获取评价详情: reviewId={}", reviewId);

        Review review = getById(reviewId);
        return ReviewDTO.fromReview(review);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewDTO> getReviewsByProject(Long projectId, Pageable pageable) {
        log.debug("根据项目ID获取评价列表: projectId={}", projectId);

        Page<Review> reviewPage = reviewRepository.findByProjectId(projectId, pageable);
        return PageResponse.fromPage(reviewPage.map(ReviewDTO::fromReview));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewDTO> getPublishedReviewsByProject(Long projectId, Pageable pageable) {
        log.debug("根据项目ID获取已发布的评价列表: projectId={}", projectId);

        Page<Review> reviewPage = reviewRepository.findPublishedReviewsByProjectId(projectId, pageable);
        return PageResponse.fromPage(reviewPage.map(ReviewDTO::fromReviewAnonymous));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewDTO> getReviewsByUser(Long userId, Pageable pageable) {
        log.debug("根据用户ID获取评价列表: userId={}", userId);

        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);
        return PageResponse.fromPage(reviewPage.map(ReviewDTO::fromReview));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewDTO> getPublishedReviewsByUser(Long userId, Pageable pageable) {
        log.debug("根据用户ID获取已发布的评价列表: userId={}", userId);

        Page<Review> reviewPage = reviewRepository.findPublishedReviewsByUserId(userId, pageable);
        return PageResponse.fromPage(reviewPage.map(ReviewDTO::fromReview));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getLatestReviews(int limit) {
        log.debug("获取最新评价列表: limit={}", limit);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findLatestReviews(pageable);
        
        return reviews.stream()
                .map(ReviewDTO::fromReviewAnonymous)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getLatestReviewsByProject(Long projectId, int limit) {
        log.debug("根据项目ID获取最新评价列表: projectId={}, limit={}", projectId, limit);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findLatestReviewsByProjectId(projectId, pageable);
        
        return reviews.stream()
                .map(ReviewDTO::fromReviewAnonymous)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getPopularReviews(int limit) {
        log.debug("获取热门评价列表: limit={}", limit);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findPopularReviews(pageable);
        
        return reviews.stream()
                .map(ReviewDTO::fromReviewAnonymous)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getPopularReviewsByProject(Long projectId, int limit) {
        log.debug("根据项目ID获取热门评价列表: projectId={}, limit={}", projectId, limit);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findPopularReviewsByProjectId(projectId, pageable);
        
        return reviews.stream()
                .map(ReviewDTO::fromReviewAnonymous)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getHighRatedReviews(BigDecimal minRating, int limit) {
        log.debug("获取高评分评价列表: minRating={}, limit={}", minRating, limit);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findHighRatedReviews(minRating, pageable);
        
        return reviews.stream()
                .map(ReviewDTO::fromReviewAnonymous)
                .toList();
    }

    /**
     * 更新项目评分
     */
    private void updateProjectRating(Long projectId) {
        try {
            BigDecimal averageRating = reviewRepository.calculateAverageRatingByProjectId(projectId);
            Long reviewCount = reviewRepository.countByProjectId(projectId);
            
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project != null) {
                project.setRating(averageRating != null ? averageRating : BigDecimal.ZERO);
                project.setRatingCount(reviewCount != null ? reviewCount.intValue() : 0);
                projectRepository.save(project);
            }
        } catch (Exception e) {
            log.warn("更新项目评分失败: projectId={}", projectId, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedProject(Long projectId, Long userId) {
        return reviewRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewDTO> getUserReviewForProject(Long projectId, Long userId) {
        return reviewRepository.findByProjectIdAndUserId(projectId, userId)
                .map(ReviewDTO::fromReview);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEditReview(Long reviewId, Long userId) {
        Review review = getById(reviewId);
        
        // 评价作者可以编辑
        if (review.getUserId().equals(userId)) {
            return true;
        }

        // TODO: 检查是否为管理员
        
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteReview(Long reviewId, Long userId) {
        Review review = getById(reviewId);
        
        // 评价作者可以删除
        if (review.getUserId().equals(userId)) {
            return true;
        }

        // TODO: 检查是否为管理员
        
        return false;
    }

    // BaseService接口的方法实现
    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    @Override
    public Review getById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评价不存在: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findAll(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    @Override
    public Review save(Review entity) {
        return reviewRepository.save(entity);
    }

    @Override
    public List<Review> saveAll(List<Review> entities) {
        return reviewRepository.saveAll(entities);
    }

    @Override
    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }

    @Override
    public void delete(Review entity) {
        reviewRepository.delete(entity);
    }

    @Override
    public void deleteAll(List<Review> entities) {
        reviewRepository.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        reviewRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return reviewRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return reviewRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewDTO> getReviewsByRatingRange(BigDecimal minRating, BigDecimal maxRating, Pageable pageable) {
        log.debug("根据评分范围获取评价列表: minRating={}, maxRating={}", minRating, maxRating);

        Page<Review> reviewPage = reviewRepository.findByRatingRange(minRating, maxRating, pageable);
        return PageResponse.fromPage(reviewPage.map(ReviewDTO::fromReviewAnonymous));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByProjectAndRatingRange(Long projectId, BigDecimal minRating, BigDecimal maxRating) {
        log.debug("根据项目ID和评分范围获取评价列表: projectId={}, minRating={}, maxRating={}", projectId, minRating, maxRating);

        List<Review> reviews = reviewRepository.findByProjectIdAndRatingRange(projectId, minRating, maxRating);
        return reviews.stream()
                .map(ReviewDTO::fromReviewAnonymous)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsWithContent() {
        log.debug("获取有内容的评价列表");

        List<Review> reviews = reviewRepository.findReviewsWithContent();
        return reviews.stream()
                .map(ReviewDTO::fromReviewAnonymous)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsWithContentByProject(Long projectId) {
        log.debug("根据项目ID获取有内容的评价列表: projectId={}", projectId);

        List<Review> reviews = reviewRepository.findReviewsWithContentByProjectId(projectId);
        return reviews.stream()
                .map(ReviewDTO::fromReviewAnonymous)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getAnonymousReviews() {
        log.debug("获取匿名评价列表");

        List<Review> reviews = reviewRepository.findAnonymousReviews();
        return reviews.stream()
                .map(ReviewDTO::fromReviewAnonymous)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewDTO> searchReviewsByContent(String keyword, Pageable pageable) {
        log.debug("根据内容搜索评价: keyword={}", keyword);

        Page<Review> reviewPage = reviewRepository.searchByContent(keyword, pageable);
        return PageResponse.fromPage(reviewPage.map(ReviewDTO::fromReviewAnonymous));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("根据创建时间范围获取评价列表: startTime={}, endTime={}", startTime, endTime);

        List<Review> reviews = reviewRepository.findByCreatedTimeBetween(startTime, endTime);
        return reviews.stream()
                .map(ReviewDTO::fromReview)
                .toList();
    }

    @Override
    public void publishReview(Long reviewId, Long adminUserId) {
        log.debug("发布评价: reviewId={}, adminUserId={}", reviewId, adminUserId);

        Review review = getById(reviewId);

        // TODO: 检查管理员权限

        review.publish();
        reviewRepository.save(review);

        // 更新项目评分
        updateProjectRating(review.getProjectId());

        log.info("评价发布成功: reviewId={}, adminUserId={}", reviewId, adminUserId);
    }

    @Override
    public void hideReview(Long reviewId, Long adminUserId) {
        log.debug("隐藏评价: reviewId={}, adminUserId={}", reviewId, adminUserId);

        Review review = getById(reviewId);

        // TODO: 检查管理员权限

        review.hide();
        reviewRepository.save(review);

        // 更新项目评分
        updateProjectRating(review.getProjectId());

        log.info("评价隐藏成功: reviewId={}, adminUserId={}", reviewId, adminUserId);
    }

    @Override
    public void deleteReview(Long reviewId, Long userId) {
        log.debug("删除评价: reviewId={}, userId={}", reviewId, userId);

        Review review = getById(reviewId);

        // 检查权限
        if (!canDeleteReview(reviewId, userId)) {
            throw new RuntimeException("无权限删除此评价");
        }

        Long projectId = review.getProjectId();
        reviewRepository.delete(review);

        // 更新项目评分
        updateProjectRating(projectId);

        log.info("评价删除成功: reviewId={}, userId={}", reviewId, userId);
    }

    @Override
    public void incrementLikeCount(Long reviewId, Long userId) {
        log.debug("增加评价点赞数: reviewId={}, userId={}", reviewId, userId);

        // TODO: 检查用户是否已经点赞过
        if (isLikedByUser(reviewId, userId)) {
            throw new RuntimeException("用户已经点赞过此评价");
        }

        try {
            reviewRepository.incrementLikeCount(reviewId);
            // TODO: 记录用户点赞记录
        } catch (Exception e) {
            log.warn("增加点赞数失败: reviewId={}, userId={}", reviewId, userId, e);
            throw new RuntimeException("点赞失败");
        }

        log.info("评价点赞成功: reviewId={}, userId={}", reviewId, userId);
    }

    @Override
    public void decrementLikeCount(Long reviewId, Long userId) {
        log.debug("减少评价点赞数: reviewId={}, userId={}", reviewId, userId);

        // TODO: 检查用户是否已经点赞过
        if (!isLikedByUser(reviewId, userId)) {
            throw new RuntimeException("用户尚未点赞此评价");
        }

        try {
            reviewRepository.decrementLikeCount(reviewId);
            // TODO: 删除用户点赞记录
        } catch (Exception e) {
            log.warn("减少点赞数失败: reviewId={}, userId={}", reviewId, userId, e);
            throw new RuntimeException("取消点赞失败");
        }

        log.info("取消评价点赞成功: reviewId={}, userId={}", reviewId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long reviewId, Long userId) {
        // TODO: 实现用户点赞状态检查
        // 需要创建用户点赞记录表或使用Redis缓存
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryDTO getProjectReviewSummary(Long projectId) {
        log.debug("获取项目评价摘要: projectId={}", projectId);

        Long totalReviews = reviewRepository.countByProjectId(projectId);
        BigDecimal averageRating = reviewRepository.calculateAverageRatingByProjectId(projectId);
        List<Object[]> ratingDistribution = reviewRepository.getRatingDistributionByProjectId(projectId);

        // TODO: 实现完整的评价摘要统计

        return ReviewSummaryDTO.builder()
                .projectId(projectId)
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateAverageRating(Long projectId) {
        BigDecimal average = reviewRepository.calculateAverageRatingByProjectId(projectId);
        return average != null ? average : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByProject(Long projectId) {
        return reviewRepository.countByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByUser(Long userId) {
        return reviewRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByStatus(Integer status) {
        return reviewRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPublishedReviews() {
        return reviewRepository.countPublishedReviews();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAnonymousReviews() {
        return reviewRepository.countAnonymousReviews();
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsWithContent() {
        return reviewRepository.countReviewsWithContent();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewDTO> getPendingReviews(Pageable pageable) {
        log.debug("获取待审核评价列表");

        Page<Review> reviewPage = reviewRepository.findPendingReviews(pageable);
        return PageResponse.fromPage(reviewPage.map(ReviewDTO::fromReview));
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingReviews() {
        return reviewRepository.countByStatus(0);
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByProjectAndRating(Long projectId, BigDecimal rating) {
        return reviewRepository.countByProjectIdAndRating(projectId, rating);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingDistribution> getRatingDistributionByProject(Long projectId) {
        log.debug("根据项目ID获取评分分布: projectId={}", projectId);

        List<Object[]> results = reviewRepository.getRatingDistributionByProjectId(projectId);
        Long totalReviews = reviewRepository.countByProjectId(projectId);

        return results.stream()
                .map(result -> (RatingDistribution) new RatingDistributionImpl(
                        (BigDecimal) result[0],
                        (Long) result[1],
                        totalReviews))
                .toList();
    }

    /**
     * 评分分布实现类
     */
    private static class RatingDistributionImpl implements RatingDistribution {
        private final BigDecimal rating;
        private final Long count;
        private final Double percentage;

        public RatingDistributionImpl(BigDecimal rating, Long count, Long totalReviews) {
            this.rating = rating;
            this.count = count;
            this.percentage = totalReviews > 0 ? (count.doubleValue() / totalReviews.doubleValue()) * 100 : 0.0;
        }

        @Override
        public BigDecimal getRating() {
            return rating;
        }

        @Override
        public Long getCount() {
            return count;
        }

        @Override
        public Double getPercentage() {
            return percentage;
        }
    }

    @Override
    public void batchApproveReviews(List<Long> reviewIds, Long adminUserId) {
        log.debug("批量审核评价: reviewIds={}, adminUserId={}", reviewIds, adminUserId);

        // TODO: 检查管理员权限

        for (Long reviewId : reviewIds) {
            try {
                publishReview(reviewId, adminUserId);
            } catch (Exception e) {
                log.warn("审核评价失败: reviewId={}", reviewId, e);
            }
        }

        log.info("批量审核评价完成: count={}, adminUserId={}", reviewIds.size(), adminUserId);
    }

    @Override
    public void batchHideReviews(List<Long> reviewIds, Long adminUserId) {
        log.debug("批量隐藏评价: reviewIds={}, adminUserId={}", reviewIds, adminUserId);

        // TODO: 检查管理员权限

        for (Long reviewId : reviewIds) {
            try {
                hideReview(reviewId, adminUserId);
            } catch (Exception e) {
                log.warn("隐藏评价失败: reviewId={}", reviewId, e);
            }
        }

        log.info("批量隐藏评价完成: count={}, adminUserId={}", reviewIds.size(), adminUserId);
    }

    @Override
    public void batchDeleteReviews(List<Long> reviewIds, Long adminUserId) {
        log.debug("批量删除评价: reviewIds={}, adminUserId={}", reviewIds, adminUserId);

        // TODO: 检查管理员权限

        for (Long reviewId : reviewIds) {
            try {
                deleteReview(reviewId, adminUserId);
            } catch (Exception e) {
                log.warn("删除评价失败: reviewId={}", reviewId, e);
            }
        }

        log.info("批量删除评价完成: count={}, adminUserId={}", reviewIds.size(), adminUserId);
    }

    @Override
    public void deleteAllReviewsByProject(Long projectId) {
        log.debug("根据项目ID删除所有评价: projectId={}", projectId);

        try {
            reviewRepository.deleteByProjectId(projectId);
            log.info("项目评价删除成功: projectId={}", projectId);
        } catch (Exception e) {
            log.warn("删除项目评价失败: projectId={}", projectId, e);
            throw new RuntimeException("删除项目评价失败");
        }
    }

    @Override
    public void deleteAllReviewsByUser(Long userId) {
        log.debug("根据用户ID删除所有评价: userId={}", userId);

        try {
            reviewRepository.deleteByUserId(userId);
            log.info("用户评价删除成功: userId={}", userId);
        } catch (Exception e) {
            log.warn("删除用户评价失败: userId={}", userId, e);
            throw new RuntimeException("删除用户评价失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewStatistics getReviewStatistics() {
        return new ReviewStatisticsImpl();
    }

    @Override
    @Transactional(readOnly = true)
    public UserReviewStatistics getUserReviewStatistics(Long userId) {
        return new UserReviewStatisticsImpl(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectReviewStatistics getProjectReviewStatistics(Long projectId) {
        return new ProjectReviewStatisticsImpl(projectId);
    }

    /**
     * 评价统计信息实现类
     */
    private class ReviewStatisticsImpl implements ReviewStatistics {
        @Override
        public long getTotalReviews() {
            return reviewRepository.countAllReviews();
        }

        @Override
        public long getPublishedReviews() {
            return reviewRepository.countPublishedReviews();
        }

        @Override
        public long getPendingReviews() {
            return reviewRepository.countByStatus(0);
        }

        @Override
        public long getAnonymousReviews() {
            return reviewRepository.countAnonymousReviews();
        }

        @Override
        public long getReviewsWithContent() {
            return reviewRepository.countReviewsWithContent();
        }

        @Override
        public BigDecimal getAverageRating() {
            // TODO: 实现全局平均评分统计
            return BigDecimal.ZERO;
        }

        @Override
        public long getTotalLikes() {
            // TODO: 实现总点赞量统计
            return 0L;
        }
    }

    /**
     * 用户评价统计信息实现类
     */
    private class UserReviewStatisticsImpl implements UserReviewStatistics {
        private final Long userId;

        public UserReviewStatisticsImpl(Long userId) {
            this.userId = userId;
        }

        @Override
        public long getTotalReviews() {
            return reviewRepository.countByUserId(userId);
        }

        @Override
        public long getPublishedReviews() {
            // TODO: 需要在Repository中添加按用户和状态统计的方法
            return 0L;
        }

        @Override
        public long getPendingReviews() {
            // TODO: 需要在Repository中添加按用户和状态统计的方法
            return 0L;
        }

        @Override
        public BigDecimal getAverageRating() {
            BigDecimal average = reviewRepository.calculateAverageRatingByUserId(userId);
            return average != null ? average : BigDecimal.ZERO;
        }

        @Override
        public long getTotalLikes() {
            // TODO: 实现用户评价总点赞量统计
            return 0L;
        }

        @Override
        public long getProjectsReviewed() {
            // TODO: 实现用户评价过的项目数量统计
            return 0L;
        }
    }

    /**
     * 项目评价统计信息实现类
     */
    private class ProjectReviewStatisticsImpl implements ProjectReviewStatistics {
        private final Long projectId;

        public ProjectReviewStatisticsImpl(Long projectId) {
            this.projectId = projectId;
        }

        @Override
        public long getTotalReviews() {
            return reviewRepository.countByProjectId(projectId);
        }

        @Override
        public long getPublishedReviews() {
            // TODO: 需要在Repository中添加按项目和状态统计的方法
            return 0L;
        }

        @Override
        public BigDecimal getAverageRating() {
            BigDecimal average = reviewRepository.calculateAverageRatingByProjectId(projectId);
            return average != null ? average : BigDecimal.ZERO;
        }

        @Override
        public long getTotalLikes() {
            // TODO: 实现项目评价总点赞量统计
            return 0L;
        }

        @Override
        public long getReviewsWithContent() {
            return reviewRepository.findReviewsWithContentByProjectId(projectId).size();
        }

        @Override
        public BigDecimal getPositiveRate() {
            // TODO: 实现好评率计算（4星及以上）
            return BigDecimal.ZERO;
        }

        @Override
        public List<RatingDistribution> getRatingDistribution() {
            return getRatingDistributionByProject(projectId);
        }
    }
}
