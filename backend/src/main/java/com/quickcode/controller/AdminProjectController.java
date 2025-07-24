package com.quickcode.controller;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.dto.common.PageResponse;
import com.quickcode.dto.project.ProjectDTO;
import com.quickcode.service.ProjectService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员项目管理控制器
 * 提供项目审核、管理等功能
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
@Validated
public class AdminProjectController {

    private final ProjectService projectService;

    /**
     * 获取当前用户ID
     */
    protected Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User user =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            return Long.parseLong(user.getUsername());
        }
        throw new RuntimeException("用户未登录");
    }

    /**
     * 成功响应
     */
    protected <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    /**
     * 成功响应带消息
     */
    protected <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.success(data, message);
    }

    /**
     * 错误响应
     */
    protected <T> ApiResponse<T> error(String message) {
        return ApiResponse.error(message);
    }

    /**
     * 检查是否包含敏感词
     */
    private boolean containsSensitiveWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        // 简单的敏感词列表，实际项目中应该从配置文件或数据库读取
        String[] sensitiveWords = {
            "垃圾", "废物", "傻逼", "操你", "去死", "滚蛋"
        };

        String lowerText = text.toLowerCase();
        for (String word : sensitiveWords) {
            if (lowerText.contains(word.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取待审核项目列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ProjectDTO>> getPendingReviewProjects(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("获取待审核项目列表: page={}, size={}", page, size);

        try {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            com.quickcode.dto.common.PageResponse<ProjectDTO> serviceResponse = 
                projectService.getPendingReviewProjects(pageable);

            // 转换为Controller层的PageResponse格式
            PageResponse<ProjectDTO> pageResponse = PageResponse.<ProjectDTO>builder()
                    .content(serviceResponse.getContent())
                    .page(serviceResponse.getPage() + 1) // 前端页码从1开始
                    .size(serviceResponse.getSize())
                    .totalElements(serviceResponse.getTotalElements())
                    .totalPages(serviceResponse.getTotalPages())
                    .first(serviceResponse.getFirst())
                    .last(serviceResponse.getLast())
                    .build();

            return success(pageResponse);
        } catch (Exception e) {
            log.error("获取待审核项目列表失败", e);
            return error("获取待审核项目列表失败: " + e.getMessage());
        }
    }

    /**
     * 审核通过项目
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> approveProject(@PathVariable @Min(1) Long id) {
        log.info("审核通过项目: id={}", id);

        try {
            Long adminUserId = getCurrentUserId();
            projectService.approveProject(id, adminUserId);
            return success(null, "项目审核通过");
        } catch (RuntimeException e) {
            log.warn("审核通过项目失败: id={}, error={}", id, e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("审核通过项目失败: id={}", id, e);
            return error("审核通过项目失败: " + e.getMessage());
        }
    }

    /**
     * 审核拒绝项目
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> rejectProject(
            @PathVariable @Min(1) Long id,
            @RequestParam @NotBlank(message = "拒绝理由不能为空")
            @Size(max = 1000, message = "拒绝理由长度不能超过1000个字符") String reason) {
        
        log.info("审核拒绝项目: id={}, reason={}", id, reason);

        // 验证理由不能包含敏感词
        if (containsSensitiveWords(reason)) {
            return error("拒绝理由包含敏感词汇");
        }

        try {
            Long adminUserId = getCurrentUserId();
            projectService.rejectProject(id, adminUserId, reason);
            return success(null, "项目审核拒绝");
        } catch (RuntimeException e) {
            log.warn("审核拒绝项目失败: id={}, error={}", id, e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("审核拒绝项目失败: id={}", id, e);
            return error("审核拒绝项目失败: " + e.getMessage());
        }
    }

    /**
     * 设置为精选项目
     */
    @PostMapping("/{id}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> setAsFeatured(@PathVariable Long id) {
        log.info("设置精选项目: id={}", id);

        try {
            Long adminUserId = getCurrentUserId();
            projectService.setAsFeatured(id, adminUserId);
            return success(null, "设置精选成功");
        } catch (RuntimeException e) {
            log.warn("设置精选项目失败: id={}, error={}", id, e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("设置精选项目失败: id={}", id, e);
            return error("设置精选项目失败: " + e.getMessage());
        }
    }

    /**
     * 取消精选项目
     */
    @DeleteMapping("/{id}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unsetFeatured(@PathVariable Long id) {
        log.info("取消精选项目: id={}", id);

        try {
            Long adminUserId = getCurrentUserId();
            projectService.unsetFeatured(id, adminUserId);
            return success(null, "取消精选成功");
        } catch (RuntimeException e) {
            log.warn("取消精选项目失败: id={}, error={}", id, e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("取消精选项目失败: id={}", id, e);
            return error("取消精选项目失败: " + e.getMessage());
        }
    }

    /**
     * 下架项目
     */
    @PostMapping("/{id}/offline")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> takeOfflineProject(@PathVariable Long id) {
        log.info("下架项目: id={}", id);

        try {
            Long adminUserId = getCurrentUserId();
            projectService.takeOfflineProject(id, adminUserId);
            return success(null, "项目下架成功");
        } catch (RuntimeException e) {
            log.warn("下架项目失败: id={}, error={}", id, e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("下架项目失败: id={}", id, e);
            return error("下架项目失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProjectStatsDTO> getProjectStats() {
        log.info("获取项目统计信息");

        try {
            ProjectStatsDTO stats = ProjectStatsDTO.builder()
                    .pendingCount(projectService.countPendingReviewProjects())
                    .publishedCount(projectService.countPublishedProjects())
                    .featuredCount(projectService.countFeaturedProjects())
                    .offlineCount(projectService.countProjectsByStatus(2))
                    .rejectedCount(projectService.countProjectsByStatus(3))
                    .build();

            return success(stats);
        } catch (Exception e) {
            log.error("获取项目统计信息失败", e);
            return error("获取项目统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 项目统计信息DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProjectStatsDTO {
        /** 待审核项目数量 */
        private Long pendingCount;
        /** 已发布项目数量 */
        private Long publishedCount;
        /** 精选项目数量 */
        private Long featuredCount;
        /** 已下架项目数量 */
        private Long offlineCount;
        /** 审核拒绝项目数量 */
        private Long rejectedCount;
    }
}
