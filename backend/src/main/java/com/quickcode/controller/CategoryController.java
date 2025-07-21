package com.quickcode.controller;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.common.response.PageResponse;
import com.quickcode.dto.category.CategoryCreateRequest;
import com.quickcode.dto.category.CategoryDTO;
import com.quickcode.dto.category.CategoryUpdateRequest;
import com.quickcode.service.CategoryService;
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
 * 分类管理控制器
 * 提供分类管理功能的API端点
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class CategoryController extends BaseController {

    private final CategoryService categoryService;

    // ==================== 公开接口 ====================

    /**
     * 获取激活分类树形结构（公开接口）
     */
    @GetMapping("/tree")
    public ApiResponse<List<CategoryDTO>> getActiveCategoryTree() {
        log.info("获取激活分类树形结构");

        try {
            List<CategoryDTO> categories = categoryService.getActiveCategoryTree();
            return success(categories);
        } catch (Exception e) {
            log.error("获取激活分类树形结构失败", e);
            return error("获取分类树失败: " + e.getMessage());
        }
    }

    /**
     * 获取激活根分类列表（公开接口）
     */
    @GetMapping("/roots")
    public ApiResponse<List<CategoryDTO>> getActiveRootCategories() {
        log.info("获取激活根分类列表");

        try {
            List<CategoryDTO> categories = categoryService.getActiveRootCategories();
            return success(categories);
        } catch (Exception e) {
            log.error("获取激活根分类列表失败", e);
            return error("获取根分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据父分类ID获取激活子分类（公开接口）
     */
    @GetMapping("/{parentId}/children")
    public ApiResponse<List<CategoryDTO>> getActiveChildrenByParentId(@PathVariable Long parentId) {
        log.info("根据父分类ID获取激活子分类: parentId={}", parentId);

        try {
            List<CategoryDTO> categories = categoryService.getActiveChildrenByParentId(parentId);
            return success(categories);
        } catch (Exception e) {
            log.error("根据父分类ID获取激活子分类失败", e);
            return error("获取子分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类代码获取分类（公开接口）
     */
    @GetMapping("/code/{code}")
    public ApiResponse<CategoryDTO> getCategoryByCode(@PathVariable String code) {
        log.info("根据分类代码获取分类: code={}", code);

        try {
            return categoryService.getCategoryByCode(code)
                    .map(this::success)
                    .orElse(error("分类不存在"));
        } catch (Exception e) {
            log.error("根据分类代码获取分类失败", e);
            return error("获取分类失败: " + e.getMessage());
        }
    }

    /**
     * 搜索分类（公开接口）
     */
    @GetMapping("/search")
    public ApiResponse<List<CategoryDTO>> searchCategories(@RequestParam String keyword) {
        log.info("搜索分类: keyword={}", keyword);

        try {
            List<CategoryDTO> categories = categoryService.searchCategories(keyword);
            return success(categories);
        } catch (Exception e) {
            log.error("搜索分类失败", e);
            return error("搜索分类失败: " + e.getMessage());
        }
    }

    // ==================== 管理员接口 ====================

    /**
     * 创建分类（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryDTO> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        log.info("创建分类: name={}, code={}", request.getName(), request.getCode());

        try {
            CategoryDTO category = categoryService.createCategory(request);
            return success(category, "分类创建成功");
        } catch (RuntimeException e) {
            log.warn("创建分类失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("创建分类失败", e);
            return error("创建分类失败: " + e.getMessage());
        }
    }

    /**
     * 更新分类（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        
        log.info("更新分类: id={}, name={}, code={}", id, request.getName(), request.getCode());

        try {
            request.setId(id);
            CategoryDTO category = categoryService.updateCategory(request);
            return success(category, "分类更新成功");
        } catch (RuntimeException e) {
            log.warn("更新分类失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("更新分类失败", e);
            return error("更新分类失败: " + e.getMessage());
        }
    }

    /**
     * 删除分类（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        log.info("删除分类: id={}", id);

        try {
            // 检查是否可以删除
            if (!categoryService.canDeleteCategory(id)) {
                return error("该分类下有子分类或项目，无法删除");
            }
            
            categoryService.deleteById(id);
            return success(null, "分类删除成功");
        } catch (RuntimeException e) {
            log.warn("删除分类失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("删除分类失败", e);
            return error("删除分类失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类详情（管理员）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryDTO> getCategoryDetail(@PathVariable Long id) {
        log.info("获取分类详情: id={}", id);

        try {
            CategoryDTO category = categoryService.getCategoryDetail(id);
            return success(category);
        } catch (RuntimeException e) {
            log.warn("获取分类详情失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("获取分类详情失败", e);
            return error("获取分类详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取分类列表（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<CategoryDTO>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("分页获取分类列表: page={}, size={}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sortOrder"));
            com.quickcode.dto.common.PageResponse<CategoryDTO> serviceResponse = categoryService.getCategories(pageable);
            
            // 转换为Controller层的PageResponse格式
            PageResponse<CategoryDTO> pageResponse = PageResponse.<CategoryDTO>builder()
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
            log.error("分页获取分类列表失败", e);
            return error("获取分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取完整分类树形结构（管理员）
     */
    @GetMapping("/admin/tree")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CategoryDTO>> getCategoryTree() {
        log.info("获取完整分类树形结构");

        try {
            List<CategoryDTO> categories = categoryService.getCategoryTree();
            return success(categories);
        } catch (Exception e) {
            log.error("获取完整分类树形结构失败", e);
            return error("获取分类树失败: " + e.getMessage());
        }
    }

    /**
     * 启用分类（管理员）
     */
    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> enableCategory(@PathVariable Long id) {
        log.info("启用分类: id={}", id);

        try {
            categoryService.enableCategory(id);
            return success(null, "分类启用成功");
        } catch (RuntimeException e) {
            log.warn("启用分类失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("启用分类失败", e);
            return error("启用分类失败: " + e.getMessage());
        }
    }

    /**
     * 禁用分类（管理员）
     */
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> disableCategory(@PathVariable Long id) {
        log.info("禁用分类: id={}", id);

        try {
            categoryService.disableCategory(id);
            return success(null, "分类禁用成功");
        } catch (RuntimeException e) {
            log.warn("禁用分类失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("禁用分类失败", e);
            return error("禁用分类失败: " + e.getMessage());
        }
    }

    /**
     * 移动分类（管理员）
     */
    @PostMapping("/{id}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> moveCategory(
            @PathVariable Long id,
            @RequestParam(required = false) Long newParentId) {
        
        log.info("移动分类: id={}, newParentId={}", id, newParentId);

        try {
            categoryService.moveCategory(id, newParentId);
            return success(null, "分类移动成功");
        } catch (RuntimeException e) {
            log.warn("移动分类失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("移动分类失败", e);
            return error("移动分类失败: " + e.getMessage());
        }
    }

    /**
     * 更新分类排序（管理员）
     */
    @PostMapping("/{id}/sort")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateSortOrder(
            @PathVariable Long id,
            @RequestParam Integer sortOrder) {
        
        log.info("更新分类排序: id={}, sortOrder={}", id, sortOrder);

        try {
            categoryService.updateSortOrder(id, sortOrder);
            return success(null, "分类排序更新成功");
        } catch (RuntimeException e) {
            log.warn("更新分类排序失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("更新分类排序失败", e);
            return error("更新分类排序失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新分类排序（管理员）
     */
    @PostMapping("/batch/sort")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchUpdateSortOrder(
            @RequestParam List<Long> categoryIds,
            @RequestParam List<Integer> sortOrders) {

        log.info("批量更新分类排序: categoryIds={}, sortOrders={}", categoryIds, sortOrders);

        try {
            categoryService.batchUpdateSortOrder(categoryIds, sortOrders);
            return success(null, "批量更新分类排序成功");
        } catch (RuntimeException e) {
            log.warn("批量更新分类排序失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("批量更新分类排序失败", e);
            return error("批量更新分类排序失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除分类（管理员）
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> batchDeleteCategories(@RequestParam List<Long> categoryIds) {
        log.info("批量删除分类: categoryIds={}", categoryIds);

        try {
            categoryService.batchDeleteCategories(categoryIds);
            return success(null, "批量删除分类成功");
        } catch (RuntimeException e) {
            log.warn("批量删除分类失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("批量删除分类失败", e);
            return error("批量删除分类失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门分类列表（管理员）
     */
    @GetMapping("/popular")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CategoryDTO>> getPopularCategories(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("获取热门分类列表: limit={}", limit);

        try {
            List<CategoryDTO> categories = categoryService.getPopularCategories(limit);
            return success(categories);
        } catch (Exception e) {
            log.error("获取热门分类列表失败", e);
            return error("获取热门分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取空分类列表（管理员）
     */
    @GetMapping("/empty")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CategoryDTO>> getEmptyCategories() {
        log.info("获取空分类列表");

        try {
            List<CategoryDTO> categories = categoryService.getEmptyCategories();
            return success(categories);
        } catch (Exception e) {
            log.error("获取空分类列表失败", e);
            return error("获取空分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取可删除的分类列表（管理员）
     */
    @GetMapping("/deletable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CategoryDTO>> getDeletableCategories() {
        log.info("获取可删除的分类列表");

        try {
            List<CategoryDTO> categories = categoryService.getDeletableCategories();
            return success(categories);
        } catch (Exception e) {
            log.error("获取可删除的分类列表失败", e);
            return error("获取可删除的分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类统计信息（管理员）
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CategoryService.CategoryStatistics> getCategoryStatistics() {
        log.info("获取分类统计信息");

        try {
            CategoryService.CategoryStatistics statistics = categoryService.getCategoryStatistics();
            return success(statistics);
        } catch (Exception e) {
            log.error("获取分类统计信息失败", e);
            return error("获取分类统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 初始化默认分类（管理员）
     */
    @PostMapping("/init")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> initializeDefaultCategories() {
        log.info("初始化默认分类");

        try {
            categoryService.initializeDefaultCategories();
            return success(null, "默认分类初始化成功");
        } catch (Exception e) {
            log.error("初始化默认分类失败", e);
            return error("初始化默认分类失败: " + e.getMessage());
        }
    }

    /**
     * 重建分类树缓存（管理员）
     */
    @PostMapping("/cache/rebuild")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> rebuildCategoryTreeCache() {
        log.info("重建分类树缓存");

        try {
            categoryService.rebuildCategoryTreeCache();
            return success(null, "分类树缓存重建成功");
        } catch (Exception e) {
            log.error("重建分类树缓存失败", e);
            return error("重建分类树缓存失败: " + e.getMessage());
        }
    }

    /**
     * 检查分类代码是否可用（管理员）
     */
    @GetMapping("/check/code")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Boolean> checkCategoryCodeAvailable(@RequestParam String code) {
        log.info("检查分类代码是否可用: code={}", code);

        try {
            boolean available = categoryService.isCategoryCodeAvailable(code);
            return success(available);
        } catch (Exception e) {
            log.error("检查分类代码可用性失败", e);
            return error("检查分类代码可用性失败: " + e.getMessage());
        }
    }

    /**
     * 检查分类名称是否可用（管理员）
     */
    @GetMapping("/check/name")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Boolean> checkCategoryNameAvailable(@RequestParam String name) {
        log.info("检查分类名称是否可用: name={}", name);

        try {
            boolean available = categoryService.isCategoryNameAvailable(name);
            return success(available);
        } catch (Exception e) {
            log.error("检查分类名称可用性失败", e);
            return error("检查分类名称可用性失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类路径（管理员）
     */
    @GetMapping("/{id}/path")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CategoryDTO>> getCategoryPath(@PathVariable Long id) {
        log.info("获取分类路径: id={}", id);

        try {
            List<CategoryDTO> path = categoryService.getCategoryPath(id);
            return success(path);
        } catch (Exception e) {
            log.error("获取分类路径失败", e);
            return error("获取分类路径失败: " + e.getMessage());
        }
    }
}
