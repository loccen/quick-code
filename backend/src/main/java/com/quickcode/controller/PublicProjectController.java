package com.quickcode.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.quickcode.common.response.ApiResponse;
import com.quickcode.common.response.PageResponse;
import com.quickcode.dto.project.ProjectDTO;
import com.quickcode.dto.project.ProjectDetailDTO;
import com.quickcode.dto.project.ProjectSearchRequest;
import com.quickcode.dto.category.CategoryDTO;
import com.quickcode.service.ProjectService;
import com.quickcode.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 公开项目控制器
 * 提供项目市场的公开访问接口，无需认证
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/public/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PublicProjectController extends BaseController {

    private final ProjectService projectService;
    private final CategoryService categoryService;

    /**
     * 获取项目列表（公开接口）
     */
    @GetMapping
    public ApiResponse<PageResponse<ProjectDTO>> getPublicProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "created_time") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("获取公开项目列表: page={}, size={}, category={}, keyword={}",
                page, size, category, keyword);

        try {
            // 构建搜索请求
            ProjectSearchRequest searchRequest = ProjectSearchRequest.builder()
                    .keyword(keyword)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDir)
                    .status(1) // 只查询已发布的项目
                    .build();

            // 如果指定了分类，查找分类ID
            if (category != null && !category.isEmpty()) {
                categoryService.getCategoryByCode(category)
                        .ifPresent(categoryDTO -> searchRequest.setCategoryId(categoryDTO.getId()));
            }

            // 搜索项目
            com.quickcode.dto.common.PageResponse<ProjectDTO> serviceResponse = projectService.searchProjects(searchRequest);

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
            log.error("获取项目列表失败", e);
            return error("获取项目列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目详情（公开接口）
     */
    @GetMapping("/{id}")
    public ApiResponse<ProjectDetailDTO> getPublicProjectDetail(@PathVariable Long id) {
        log.info("获取公开项目详情: id={}", id);

        try {
            ProjectDetailDTO project = projectService.getProjectDetail(id);

            // 增加浏览次数
            projectService.incrementViewCount(id);

            return success(project);
        } catch (RuntimeException e) {
            log.warn("获取项目详情失败: id={}, error={}", id, e.getMessage());
            return error("项目不存在或已下架");
        } catch (Exception e) {
            log.error("获取项目详情失败: id={}", id, e);
            return error("获取项目详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目分类（公开接口）
     */
    @GetMapping("/categories")
    public ApiResponse<List<CategoryDTO>> getProjectCategories() {
        log.info("获取项目分类列表");

        try {
            List<CategoryDTO> categories = categoryService.getActiveCategoryTree();
            return success(categories);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return error("获取分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 搜索项目（公开接口）
     */
    @GetMapping("/search")
    public ApiResponse<List<ProjectDTO>> searchProjects(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("搜索项目: keyword={}, limit={}", keyword, limit);

        try {
            // 构建搜索请求
            ProjectSearchRequest searchRequest = ProjectSearchRequest.builder()
                    .keyword(keyword)
                    .page(0)
                    .size(limit)
                    .sortBy("createdTime")
                    .sortDirection("desc")
                    .status(1) // 只查询已发布的项目
                    .build();

            // 搜索项目
            com.quickcode.dto.common.PageResponse<ProjectDTO> serviceResponse = projectService.searchProjects(searchRequest);

            return success(serviceResponse.getContent());
        } catch (Exception e) {
            log.error("搜索项目失败", e);
            return error("搜索项目失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门项目（公开接口）
     */
    @GetMapping("/popular")
    public ApiResponse<List<ProjectDTO>> getPopularProjects(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("获取热门项目列表: limit={}", limit);

        try {
            List<ProjectDTO> projects = projectService.getPopularProjects(limit);
            return success(projects);
        } catch (Exception e) {
            log.error("获取热门项目失败", e);
            return error("获取热门项目失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新项目（公开接口）
     */
    @GetMapping("/latest")
    public ApiResponse<List<ProjectDTO>> getLatestProjects(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("获取最新项目列表: limit={}", limit);

        try {
            List<ProjectDTO> projects = projectService.getLatestProjects(limit);
            return success(projects);
        } catch (Exception e) {
            log.error("获取最新项目失败", e);
            return error("获取最新项目失败: " + e.getMessage());
        }
    }

    /**
     * 获取精选项目（公开接口）
     */
    @GetMapping("/featured")
    public ApiResponse<List<ProjectDTO>> getFeaturedProjects(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("获取精选项目列表: limit={}", limit);

        try {
            List<ProjectDTO> projects = projectService.getFeaturedProjects(limit);
            return success(projects);
        } catch (Exception e) {
            log.error("获取精选项目失败", e);
            return error("获取精选项目失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类获取项目（公开接口）
     */
    @GetMapping("/category/{categoryId}")
    public ApiResponse<PageResponse<ProjectDTO>> getProjectsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        log.info("根据分类获取项目: categoryId={}, page={}, size={}", categoryId, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
            com.quickcode.dto.common.PageResponse<ProjectDTO> serviceResponse = projectService.getProjectsByCategory(categoryId, pageable);

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
            log.error("根据分类获取项目失败", e);
            return error("根据分类获取项目失败: " + e.getMessage());
        }
    }
}
