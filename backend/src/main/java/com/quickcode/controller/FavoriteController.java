package com.quickcode.controller;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.dto.common.PageResponse;
import com.quickcode.dto.project.ProjectDTO;
import com.quickcode.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 收藏功能控制器
 * 提供项目收藏相关的API端点
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class FavoriteController extends BaseController {

    private final FavoriteService favoriteService;

    /**
     * 收藏项目
     */
    @PostMapping("/{id}/favorite")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> favoriteProject(@PathVariable Long id) {
        log.info("收藏项目: projectId={}", id);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return error("用户未登录");
            }
            favoriteService.favoriteProject(userId, id);
            return success(null, "收藏成功");
        } catch (RuntimeException e) {
            log.warn("收藏项目失败: projectId={}, error={}", id, e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("收藏项目失败: projectId={}", id, e);
            return error("收藏失败: " + e.getMessage());
        }
    }

    /**
     * 取消收藏项目
     */
    @DeleteMapping("/{id}/favorite")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> unfavoriteProject(@PathVariable Long id) {
        log.info("取消收藏项目: projectId={}", id);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return error("用户未登录");
            }
            favoriteService.unfavoriteProject(userId, id);
            return success(null, "取消收藏成功");
        } catch (RuntimeException e) {
            log.warn("取消收藏项目失败: projectId={}, error={}", id, e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("取消收藏项目失败: projectId={}", id, e);
            return error("取消收藏失败: " + e.getMessage());
        }
    }

    /**
     * 检查项目收藏状态
     */
    @GetMapping("/{id}/favorite/status")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Boolean> checkFavoriteStatus(@PathVariable Long id) {
        log.info("检查项目收藏状态: projectId={}", id);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return error("用户未登录");
            }
            boolean isFavorited = favoriteService.isFavorited(userId, id);
            return success(isFavorited);
        } catch (Exception e) {
            log.error("检查项目收藏状态失败: projectId={}", id, e);
            return error("检查收藏状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏的项目列表
     */
    @GetMapping("/favorites")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResponse<ProjectDTO>> getUserFavoriteProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("获取用户收藏项目列表: page={}, size={}, keyword={}, sortBy={}, sortDir={}",
                page, size, keyword, sortBy, sortDir);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return error("用户未登录");
            }

            Sort.Direction direction = Sort.Direction.fromString(sortDir);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            PageResponse<ProjectDTO> favoriteProjects = favoriteService.getUserFavoriteProjects(userId, keyword, pageable);
            return success(favoriteProjects);
        } catch (Exception e) {
            log.error("获取用户收藏项目列表失败", e);
            return error("获取收藏列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户最近收藏的项目
     */
    @GetMapping("/favorites/recent")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<ProjectDTO>> getUserRecentFavorites(
            @RequestParam(defaultValue = "5") int limit) {

        log.info("获取用户最近收藏项目: limit={}", limit);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return error("用户未登录");
            }
            List<ProjectDTO> recentFavorites = favoriteService.getUserRecentFavorites(userId, limit);
            return success(recentFavorites);
        } catch (Exception e) {
            log.error("获取用户最近收藏项目失败", e);
            return error("获取最近收藏失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门收藏项目（公开接口）
     */
    @GetMapping("/favorites/popular")
    public ApiResponse<List<ProjectDTO>> getPopularFavoriteProjects(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("获取热门收藏项目: limit={}", limit);

        try {
            List<ProjectDTO> popularProjects = favoriteService.getPopularFavoriteProjects(limit);
            return success(popularProjects);
        } catch (Exception e) {
            log.error("获取热门收藏项目失败", e);
            return error("获取热门收藏项目失败: " + e.getMessage());
        }
    }

    /**
     * 批量检查项目收藏状态
     */
    @PostMapping("/favorites/batch-check")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Map<Long, Boolean>> batchCheckFavoriteStatus(
            @RequestBody List<Long> projectIds) {

        log.info("批量检查项目收藏状态: projectIds={}", projectIds);

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return error("用户未登录");
            }
            Map<Long, Boolean> favoriteStatusMap = favoriteService.batchCheckFavoriteStatus(userId, projectIds);
            return success(favoriteStatusMap);
        } catch (Exception e) {
            log.error("批量检查项目收藏状态失败", e);
            return error("批量检查收藏状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏统计
     */
    @GetMapping("/favorites/stats")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Map<String, Object>> getUserFavoriteStats() {
        log.info("获取用户收藏统计");

        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return error("用户未登录");
            }
            long favoriteCount = favoriteService.countUserFavorites(userId);

            Map<String, Object> stats = Map.of(
                    "totalFavorites", favoriteCount,
                    "userId", userId
            );

            return success(stats);
        } catch (Exception e) {
            log.error("获取用户收藏统计失败", e);
            return error("获取收藏统计失败: " + e.getMessage());
        }
    }
}
