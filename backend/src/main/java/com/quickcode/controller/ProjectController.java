package com.quickcode.controller;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.common.response.PageResponse;
import com.quickcode.dto.project.ProjectCreateRequest;
import com.quickcode.dto.project.ProjectDTO;
import com.quickcode.dto.project.ProjectDetailDTO;
import com.quickcode.dto.project.ProjectSearchRequest;
import com.quickcode.dto.project.ProjectUpdateRequest;
import com.quickcode.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 项目管理控制器
 * 提供项目管理功能的API端点
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ProjectController extends BaseController {

    private final ProjectService projectService;

    /**
     * 创建项目
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ProjectDTO> createProject(
            @Valid @RequestBody ProjectCreateRequest request) {
        
        log.info("创建项目: title={}", request.getTitle());

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            ProjectDTO project = projectService.createProject(request, userId);
            return success(project, "项目创建成功");
        } catch (RuntimeException e) {
            log.warn("创建项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("创建项目失败", e);
            return error("创建项目失败: " + e.getMessage());
        }
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request) {
        
        log.info("更新项目: id={}, title={}", id, request.getTitle());

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            request.setId(id);
            ProjectDTO project = projectService.updateProject(request, userId);
            return success(project, "项目更新成功");
        } catch (RuntimeException e) {
            log.warn("更新项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("更新项目失败", e);
            return error("更新项目失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ProjectDetailDTO> getProjectDetail(@PathVariable Long id) {
        log.info("获取项目详情: id={}", id);

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            ProjectDetailDTO project = projectService.getProjectDetail(id, userId);
            return success(project);
        } catch (RuntimeException e) {
            log.warn("获取项目详情失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("获取项目详情失败", e);
            return error("获取项目详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        log.info("删除项目: id={}", id);

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            // 检查权限
            if (!projectService.canDeleteProject(id, userId)) {
                return error("无权限删除此项目");
            }
            
            projectService.deleteById(id);
            return success(null, "项目删除成功");
        } catch (RuntimeException e) {
            log.warn("删除项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("删除项目失败", e);
            return error("删除项目失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户项目列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResponse<ProjectDTO>> getMyProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Integer status) {
        
        log.info("获取用户项目列表: page={}, size={}, status={}", page, size, status);

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
            com.quickcode.dto.common.PageResponse<ProjectDTO> serviceResponse;
            
            if (status != null) {
                serviceResponse = projectService.getProjectsByUser(userId, status, pageable);
            } else {
                serviceResponse = projectService.getProjectsByUser(userId, pageable);
            }
            
            // 转换为Controller层的PageResponse格式
            PageResponse<ProjectDTO> pageResponse = PageResponse.<ProjectDTO>builder()
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
            log.error("获取用户项目列表失败", e);
            return error("获取用户项目列表失败: " + e.getMessage());
        }
    }

    /**
     * 搜索项目
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResponse<ProjectDTO>> searchProjects(
            @Valid @RequestBody ProjectSearchRequest request) {
        
        log.info("搜索项目: keyword={}, categoryId={}", request.getKeyword(), request.getCategoryId());

        try {
            com.quickcode.dto.common.PageResponse<ProjectDTO> serviceResponse = projectService.searchProjects(request);
            
            // 转换为Controller层的PageResponse格式
            PageResponse<ProjectDTO> pageResponse = PageResponse.<ProjectDTO>builder()
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
            log.error("搜索项目失败", e);
            return error("搜索项目失败: " + e.getMessage());
        }
    }

    /**
     * 发布项目
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> publishProject(@PathVariable Long id) {
        log.info("发布项目: id={}", id);

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            projectService.publishProject(id, userId);
            return success(null, "项目发布成功");
        } catch (RuntimeException e) {
            log.warn("发布项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("发布项目失败", e);
            return error("发布项目失败: " + e.getMessage());
        }
    }

    /**
     * 下架项目
     */
    @PostMapping("/{id}/takeOffline")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> takeOfflineProject(@PathVariable Long id) {
        log.info("下架项目: id={}", id);

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            projectService.takeOfflineProject(id, userId);
            return success(null, "项目下架成功");
        } catch (RuntimeException e) {
            log.warn("下架项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("下架项目失败", e);
            return error("下架项目失败: " + e.getMessage());
        }
    }

    /**
     * 点赞项目
     */
    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> likeProject(@PathVariable Long id) {
        log.info("点赞项目: id={}", id);

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            projectService.incrementLikeCount(id, userId);
            return success(null, "点赞成功");
        } catch (RuntimeException e) {
            log.warn("点赞项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("点赞项目失败", e);
            return error("点赞项目失败: " + e.getMessage());
        }
    }

    /**
     * 取消点赞项目
     */
    @DeleteMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> unlikeProject(@PathVariable Long id) {
        log.info("取消点赞项目: id={}", id);

        try {
            // TODO: 从认证上下文获取用户ID
            Long userId = getCurrentUserId();
            
            projectService.decrementLikeCount(id, userId);
            return success(null, "取消点赞成功");
        } catch (RuntimeException e) {
            log.warn("取消点赞项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("取消点赞项目失败", e);
            return error("取消点赞项目失败: " + e.getMessage());
        }
    }

    // ==================== 管理员功能 ====================

    /**
     * 获取待审核项目列表（管理员）
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ProjectDTO>> getPendingProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("获取待审核项目列表: page={}, size={}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdTime"));
            com.quickcode.dto.common.PageResponse<ProjectDTO> serviceResponse = projectService.getPendingReviewProjects(pageable);

            // 转换为Controller层的PageResponse格式
            PageResponse<ProjectDTO> pageResponse = PageResponse.<ProjectDTO>builder()
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
            log.error("获取待审核项目列表失败", e);
            return error("获取待审核项目列表失败: " + e.getMessage());
        }
    }

    /**
     * 审核通过项目（管理员）
     */
    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> approveProject(@PathVariable Long id) {
        log.info("审核通过项目: id={}", id);

        try {
            // TODO: 从认证上下文获取管理员用户ID
            Long adminUserId = getCurrentUserId();

            projectService.approveProject(id, adminUserId);
            return success(null, "项目审核通过");
        } catch (RuntimeException e) {
            log.warn("审核通过项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("审核通过项目失败", e);
            return error("审核通过项目失败: " + e.getMessage());
        }
    }

    /**
     * 审核拒绝项目（管理员）
     */
    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> rejectProject(
            @PathVariable Long id,
            @RequestParam String reason) {

        log.info("审核拒绝项目: id={}, reason={}", id, reason);

        try {
            // TODO: 从认证上下文获取管理员用户ID
            Long adminUserId = getCurrentUserId();

            projectService.rejectProject(id, adminUserId, reason);
            return success(null, "项目审核拒绝");
        } catch (RuntimeException e) {
            log.warn("审核拒绝项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("审核拒绝项目失败", e);
            return error("审核拒绝项目失败: " + e.getMessage());
        }
    }

    /**
     * 设置为精选项目（管理员）
     */
    @PostMapping("/admin/{id}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> setAsFeatured(@PathVariable Long id) {
        log.info("设置为精选项目: id={}", id);

        try {
            // TODO: 从认证上下文获取管理员用户ID
            Long adminUserId = getCurrentUserId();

            projectService.setAsFeatured(id, adminUserId);
            return success(null, "设置精选项目成功");
        } catch (RuntimeException e) {
            log.warn("设置精选项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("设置精选项目失败", e);
            return error("设置精选项目失败: " + e.getMessage());
        }
    }

    /**
     * 取消精选项目（管理员）
     */
    @DeleteMapping("/admin/{id}/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unsetFeatured(@PathVariable Long id) {
        log.info("取消精选项目: id={}", id);

        try {
            // TODO: 从认证上下文获取管理员用户ID
            Long adminUserId = getCurrentUserId();

            projectService.unsetFeatured(id, adminUserId);
            return success(null, "取消精选项目成功");
        } catch (RuntimeException e) {
            log.warn("取消精选项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("取消精选项目失败", e);
            return error("取消精选项目失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新项目状态（管理员）
     */
    @PostMapping("/admin/batch/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchUpdateStatus(
            @RequestParam List<Long> projectIds,
            @RequestParam Integer status) {

        log.info("批量更新项目状态: projectIds={}, status={}", projectIds, status);

        try {
            // TODO: 从认证上下文获取管理员用户ID
            Long adminUserId = getCurrentUserId();

            projectService.batchUpdateProjectStatus(projectIds, status, adminUserId);
            return success(null, "批量更新项目状态成功");
        } catch (RuntimeException e) {
            log.warn("批量更新项目状态失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("批量更新项目状态失败", e);
            return error("批量更新项目状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除项目（管理员）
     */
    @DeleteMapping("/admin/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteProjects(@RequestParam List<Long> projectIds) {
        log.info("批量删除项目: projectIds={}", projectIds);

        try {
            // TODO: 从认证上下文获取管理员用户ID
            Long adminUserId = getCurrentUserId();

            projectService.batchDeleteProjects(projectIds, adminUserId);
            return success(null, "批量删除项目成功");
        } catch (RuntimeException e) {
            log.warn("批量删除项目失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("批量删除项目失败", e);
            return error("批量删除项目失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目统计信息（管理员）
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProjectService.ProjectStatistics> getProjectStatistics() {
        log.info("获取项目统计信息");

        try {
            ProjectService.ProjectStatistics statistics = projectService.getProjectStatistics();
            return success(statistics);
        } catch (Exception e) {
            log.error("获取项目统计信息失败", e);
            return error("获取项目统计信息失败: " + e.getMessage());
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
