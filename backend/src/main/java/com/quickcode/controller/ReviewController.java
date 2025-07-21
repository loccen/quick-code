package com.quickcode.controller;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.common.response.PageResponse;
import com.quickcode.dto.review.ReviewCreateRequest;
import com.quickcode.dto.review.ReviewDTO;
import com.quickcode.dto.review.ReviewSummaryDTO;
import com.quickcode.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 评价管理控制器
 * 提供评价管理功能的API端点
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ReviewController extends BaseController {

    private final ReviewService reviewService;

    // ==================== 公开接口 ====================

    /**
     * 根据项目ID获取已发布的评价列表（公开接口）
     */
    @GetMapping("/project/{projectId}")
    public ApiResponse<PageResponse<ReviewDTO>> getPublishedReviewsByProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("根据项目ID获取已发布的评价列表: projectId={}, page={}, size={}", projectId, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
            com.quickcode.dto.common.PageResponse<ReviewDTO> serviceResponse =
                    reviewService.getPublishedReviewsByProject(projectId, pageable);
            
            // 转换为Controller层的PageResponse格式
            PageResponse<ReviewDTO> pageResponse = PageResponse.<ReviewDTO>builder()
                    .content(serviceResponse.getContent())
                    .page(serviceResponse.getPage() + 1) // 前端页码从1开始
                    .size(serviceResponse.getSize())
                    .total(serviceResponse.getTotalElements())
                    .totalPages(serviceResponse.getTotalPages())
                    .first(serviceResponse.getFirst())
                    .last(serviceResponse.getLast())
                    .build();

            return success(pageResponse);
        } catch (Exception e) {
            log.error("根据项目ID获取已发布的评价列表失败", e);
            return error("获取评价列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目评价摘要（公开接口）
     */
    @GetMapping("/project/{projectId}/summary")
    public ApiResponse<ReviewSummaryDTO> getProjectReviewSummary(@PathVariable Long projectId) {
        log.info("获取项目评价摘要: projectId={}", projectId);

        try {
            ReviewSummaryDTO summary = reviewService.getProjectReviewSummary(projectId);
            return success(summary);
        } catch (Exception e) {
            log.error("获取项目评价摘要失败", e);
            return error("获取项目评价摘要失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新评价列表（公开接口）
     */
    @GetMapping("/latest")
    public ApiResponse<List<ReviewDTO>> getLatestReviews(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("获取最新评价列表: limit={}", limit);

        try {
            List<ReviewDTO> reviews = reviewService.getLatestReviews(limit);
            return success(reviews);
        } catch (Exception e) {
            log.error("获取最新评价列表失败", e);
            return error("获取最新评价列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门评价列表（公开接口）
     */
    @GetMapping("/popular")
    public ApiResponse<List<ReviewDTO>> getPopularReviews(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("获取热门评价列表: limit={}", limit);

        try {
            List<ReviewDTO> reviews = reviewService.getPopularReviews(limit);
            return success(reviews);
        } catch (Exception e) {
            log.error("获取热门评价列表失败", e);
            return error("获取热门评价列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取高评分评价列表（公开接口）
     */
    @GetMapping("/high-rated")
    public ApiResponse<List<ReviewDTO>> getHighRatedReviews(
            @RequestParam(defaultValue = "4.0") BigDecimal minRating,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("获取高评分评价列表: minRating={}, limit={}", minRating, limit);

        try {
            List<ReviewDTO> reviews = reviewService.getHighRatedReviews(minRating, limit);
            return success(reviews);
        } catch (Exception e) {
            log.error("获取高评分评价列表失败", e);
            return error("获取高评分评价列表失败: " + e.getMessage());
        }
    }

    // ==================== 用户接口 ====================

    /**
     * 创建评价
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ReviewDTO> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        log.info("创建评价: projectId={}, rating={}", request.getProjectId(), request.getRating());

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            ReviewDTO review = reviewService.createReview(request, userId);
            return success(review, "评价创建成功");
        } catch (RuntimeException e) {
            log.warn("创建评价失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("创建评价失败", e);
            return error("创建评价失败: " + e.getMessage());
        }
    }

    /**
     * 更新评价
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ReviewDTO> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewCreateRequest request) {
        
        log.info("更新评价: id={}, rating={}", id, request.getRating());

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            ReviewDTO review = reviewService.updateReview(id, request, userId);
            return success(review, "评价更新成功");
        } catch (RuntimeException e) {
            log.warn("更新评价失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("更新评价失败", e);
            return error("更新评价失败: " + e.getMessage());
        }
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {
        log.info("删除评价: id={}", id);

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            reviewService.deleteReview(id, userId);
            return success(null, "评价删除成功");
        } catch (RuntimeException e) {
            log.warn("删除评价失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("删除评价失败", e);
            return error("删除评价失败: " + e.getMessage());
        }
    }

    /**
     * 获取评价详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ReviewDTO> getReviewDetail(@PathVariable Long id) {
        log.info("获取评价详情: id={}", id);

        try {
            ReviewDTO review = reviewService.getReviewDetail(id);
            return success(review);
        } catch (RuntimeException e) {
            log.warn("获取评价详情失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("获取评价详情失败", e);
            return error("获取评价详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户评价列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResponse<ReviewDTO>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("获取用户评价列表: page={}, size={}", page, size);

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
            com.quickcode.dto.common.PageResponse<ReviewDTO> serviceResponse =
                    reviewService.getReviewsByUser(userId, pageable);
            
            // 转换为Controller层的PageResponse格式
            PageResponse<ReviewDTO> pageResponse = PageResponse.<ReviewDTO>builder()
                    .content(serviceResponse.getContent())
                    .page(serviceResponse.getPage() + 1) // 前端页码从1开始
                    .size(serviceResponse.getSize())
                    .total(serviceResponse.getTotalElements())
                    .totalPages(serviceResponse.getTotalPages())
                    .first(serviceResponse.getFirst())
                    .last(serviceResponse.getLast())
                    .build();

            return success(pageResponse);
        } catch (Exception e) {
            log.error("获取用户评价列表失败", e);
            return error("获取用户评价列表失败: " + e.getMessage());
        }
    }

    /**
     * 点赞评价
     */
    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> likeReview(@PathVariable Long id) {
        log.info("点赞评价: id={}", id);

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            reviewService.incrementLikeCount(id, userId);
            return success(null, "点赞成功");
        } catch (RuntimeException e) {
            log.warn("点赞评价失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("点赞评价失败", e);
            return error("点赞评价失败: " + e.getMessage());
        }
    }

    /**
     * 取消点赞评价
     */
    @DeleteMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> unlikeReview(@PathVariable Long id) {
        log.info("取消点赞评价: id={}", id);

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            reviewService.decrementLikeCount(id, userId);
            return success(null, "取消点赞成功");
        } catch (RuntimeException e) {
            log.warn("取消点赞评价失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("取消点赞评价失败", e);
            return error("取消点赞评价失败: " + e.getMessage());
        }
    }

    // ==================== 管理员接口 ====================

    /**
     * 获取待审核评价列表（管理员）
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ReviewDTO>> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("获取待审核评价列表: page={}, size={}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdTime"));
            com.quickcode.dto.common.PageResponse<ReviewDTO> serviceResponse =
                    reviewService.getPendingReviews(pageable);

            // 转换为Controller层的PageResponse格式
            PageResponse<ReviewDTO> pageResponse = PageResponse.<ReviewDTO>builder()
                    .content(serviceResponse.getContent())
                    .page(serviceResponse.getPage() + 1) // 前端页码从1开始
                    .size(serviceResponse.getSize())
                    .total(serviceResponse.getTotalElements())
                    .totalPages(serviceResponse.getTotalPages())
                    .first(serviceResponse.getFirst())
                    .last(serviceResponse.getLast())
                    .build();

            return success(pageResponse);
        } catch (Exception e) {
            log.error("获取待审核评价列表失败", e);
            return error("获取待审核评价列表失败: " + e.getMessage());
        }
    }

    /**
     * 审核通过评价（管理员）
     */
    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> approveReview(@PathVariable Long id) {
        log.info("审核通过评价: id={}", id);

        try {
            // TODO: 从认证上下文获取管理员用户ID
            Long adminUserId = getCurrentUserId();

            reviewService.publishReview(id, adminUserId);
            return success(null, "评价审核通过");
        } catch (RuntimeException e) {
            log.warn("审核通过评价失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("审核通过评价失败", e);
            return error("审核通过评价失败: " + e.getMessage());
        }
    }

    /**
     * 隐藏评价（管理员）
     */
    @PostMapping("/admin/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> hideReview(@PathVariable Long id) {
        log.info("隐藏评价: id={}", id);

        try {
            // TODO: 从认证上下文获取管理员用户ID
            Long adminUserId = getCurrentUserId();

            reviewService.hideReview(id, adminUserId);
            return success(null, "评价隐藏成功");
        } catch (RuntimeException e) {
            log.warn("隐藏评价失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("隐藏评价失败", e);
            return error("隐藏评价失败: " + e.getMessage());
        }
    }

    /**
     * 批量审核评价（管理员）
     */
    @PostMapping("/admin/batch/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchApproveReviews(@RequestParam List<Long> reviewIds) {
        log.info("批量审核评价: reviewIds={}", reviewIds);

        try {
            // TODO: 从认证上下文获取管理员用户ID
            Long adminUserId = getCurrentUserId();

            reviewService.batchApproveReviews(reviewIds, adminUserId);
            return success(null, "批量审核评价成功");
        } catch (RuntimeException e) {
            log.warn("批量审核评价失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("批量审核评价失败", e);
            return error("批量审核评价失败: " + e.getMessage());
        }
    }

    /**
     * 批量隐藏评价（管理员）
     */
    @PostMapping("/admin/batch/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchHideReviews(@RequestParam List<Long> reviewIds) {
        log.info("批量隐藏评价: reviewIds={}", reviewIds);

        try {
            // TODO: 从认证上下文获取管理员用户ID
            Long adminUserId = getCurrentUserId();

            reviewService.batchHideReviews(reviewIds, adminUserId);
            return success(null, "批量隐藏评价成功");
        } catch (RuntimeException e) {
            log.warn("批量隐藏评价失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("批量隐藏评价失败", e);
            return error("批量隐藏评价失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除评价（管理员）
     */
    @DeleteMapping("/admin/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteReviews(@RequestParam List<Long> reviewIds) {
        log.info("批量删除评价: reviewIds={}", reviewIds);

        try {
            // TODO: 从认证上下文获取管理员用户ID
            Long adminUserId = getCurrentUserId();

            reviewService.batchDeleteReviews(reviewIds, adminUserId);
            return success(null, "批量删除评价成功");
        } catch (RuntimeException e) {
            log.warn("批量删除评价失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("批量删除评价失败", e);
            return error("批量删除评价失败: " + e.getMessage());
        }
    }

    /**
     * 搜索评价（管理员）
     */
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ReviewDTO>> searchReviews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("搜索评价: keyword={}, page={}, size={}", keyword, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
            com.quickcode.dto.common.PageResponse<ReviewDTO> serviceResponse =
                    reviewService.searchReviewsByContent(keyword, pageable);

            // 转换为Controller层的PageResponse格式
            PageResponse<ReviewDTO> pageResponse = PageResponse.<ReviewDTO>builder()
                    .content(serviceResponse.getContent())
                    .page(serviceResponse.getPage() + 1) // 前端页码从1开始
                    .size(serviceResponse.getSize())
                    .total(serviceResponse.getTotalElements())
                    .totalPages(serviceResponse.getTotalPages())
                    .first(serviceResponse.getFirst())
                    .last(serviceResponse.getLast())
                    .build();

            return success(pageResponse);
        } catch (Exception e) {
            log.error("搜索评价失败", e);
            return error("搜索评价失败: " + e.getMessage());
        }
    }

    /**
     * 获取评价统计信息（管理员）
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReviewService.ReviewStatistics> getReviewStatistics() {
        log.info("获取评价统计信息");

        try {
            ReviewService.ReviewStatistics statistics = reviewService.getReviewStatistics();
            return success(statistics);
        } catch (Exception e) {
            log.error("获取评价统计信息失败", e);
            return error("获取评价统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户评价统计信息（管理员）
     */
    @GetMapping("/admin/user/{userId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReviewService.UserReviewStatistics> getUserReviewStatistics(@PathVariable Long userId) {
        log.info("获取用户评价统计信息: userId={}", userId);

        try {
            ReviewService.UserReviewStatistics statistics = reviewService.getUserReviewStatistics(userId);
            return success(statistics);
        } catch (Exception e) {
            log.error("获取用户评价统计信息失败", e);
            return error("获取用户评价统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目评价统计信息（管理员）
     */
    @GetMapping("/admin/project/{projectId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReviewService.ProjectReviewStatistics> getProjectReviewStatistics(@PathVariable Long projectId) {
        log.info("获取项目评价统计信息: projectId={}", projectId);

        try {
            ReviewService.ProjectReviewStatistics statistics = reviewService.getProjectReviewStatistics(projectId);
            return success(statistics);
        } catch (Exception e) {
            log.error("获取项目评价统计信息失败", e);
            return error("获取项目评价统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据项目ID获取评分分布（管理员）
     */
    @GetMapping("/admin/project/{projectId}/rating-distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ReviewService.RatingDistribution>> getRatingDistribution(@PathVariable Long projectId) {
        log.info("根据项目ID获取评分分布: projectId={}", projectId);

        try {
            List<ReviewService.RatingDistribution> distribution = reviewService.getRatingDistributionByProject(projectId);
            return success(distribution);
        } catch (Exception e) {
            log.error("根据项目ID获取评分分布失败", e);
            return error("获取评分分布失败: " + e.getMessage());
        }
    }

    /**
     * 临时方法：获取当前用户ID
     * TODO: 实现真正的用户认证和授权
     */
    protected Long getCurrentUserId() {
        // 临时返回固定用户ID，实际应该从Spring Security上下文获取
        return 1L;
    }
}
