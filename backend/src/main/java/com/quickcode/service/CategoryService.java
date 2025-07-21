package com.quickcode.service;

import com.quickcode.dto.category.CategoryCreateRequest;
import com.quickcode.dto.category.CategoryDTO;
import com.quickcode.dto.category.CategoryUpdateRequest;
import com.quickcode.dto.common.PageResponse;
import com.quickcode.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 分类Service接口
 * 提供分类相关的业务逻辑方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface CategoryService extends BaseService<Category, Long> {

    /**
     * 创建分类
     */
    CategoryDTO createCategory(CategoryCreateRequest request);

    /**
     * 更新分类
     */
    CategoryDTO updateCategory(CategoryUpdateRequest request);

    /**
     * 根据ID获取分类详情
     */
    CategoryDTO getCategoryDetail(Long categoryId);

    /**
     * 根据代码获取分类
     */
    Optional<CategoryDTO> getCategoryByCode(String code);

    /**
     * 获取所有激活的分类
     */
    List<CategoryDTO> getActiveCategories();

    /**
     * 分页获取分类列表
     */
    PageResponse<CategoryDTO> getCategories(Pageable pageable);

    /**
     * 分页获取激活的分类列表
     */
    PageResponse<CategoryDTO> getActiveCategories(Pageable pageable);

    /**
     * 获取根分类列表
     */
    List<CategoryDTO> getRootCategories();

    /**
     * 获取激活的根分类列表
     */
    List<CategoryDTO> getActiveRootCategories();

    /**
     * 根据父分类ID获取子分类列表
     */
    List<CategoryDTO> getChildrenByParentId(Long parentId);

    /**
     * 根据父分类ID获取激活的子分类列表
     */
    List<CategoryDTO> getActiveChildrenByParentId(Long parentId);

    /**
     * 获取分类树形结构
     */
    List<CategoryDTO> getCategoryTree();

    /**
     * 获取激活分类的树形结构
     */
    List<CategoryDTO> getActiveCategoryTree();

    /**
     * 根据分类ID获取完整的分类树
     */
    CategoryDTO getCategoryTreeById(Long categoryId);

    /**
     * 获取分类路径（从根到指定分类）
     */
    List<CategoryDTO> getCategoryPath(Long categoryId);

    /**
     * 搜索分类
     */
    List<CategoryDTO> searchCategories(String keyword);

    /**
     * 分页搜索分类
     */
    PageResponse<CategoryDTO> searchCategories(String keyword, Pageable pageable);

    /**
     * 启用分类
     */
    void enableCategory(Long categoryId);

    /**
     * 禁用分类
     */
    void disableCategory(Long categoryId);

    /**
     * 移动分类到新的父分类下
     */
    void moveCategory(Long categoryId, Long newParentId);

    /**
     * 调整分类排序
     */
    void updateSortOrder(Long categoryId, Integer sortOrder);

    /**
     * 批量更新分类排序
     */
    void batchUpdateSortOrder(List<Long> categoryIds, List<Integer> sortOrders);

    /**
     * 检查分类代码是否可用
     */
    boolean isCategoryCodeAvailable(String code);

    /**
     * 检查分类名称是否可用
     */
    boolean isCategoryNameAvailable(String name);

    /**
     * 检查分类是否可以删除
     */
    boolean canDeleteCategory(Long categoryId);

    /**
     * 检查分类是否有子分类
     */
    boolean hasChildren(Long categoryId);

    /**
     * 检查分类是否有项目
     */
    boolean hasProjects(Long categoryId);

    /**
     * 获取分类下的项目数量
     */
    long getProjectCount(Long categoryId);

    /**
     * 获取分类及其子分类下的项目总数
     */
    long getTotalProjectCount(Long categoryId);

    /**
     * 获取热门分类列表（按项目数量排序）
     */
    List<CategoryDTO> getPopularCategories(int limit);

    /**
     * 获取空分类列表（没有项目的分类）
     */
    List<CategoryDTO> getEmptyCategories();

    /**
     * 获取可删除的分类列表
     */
    List<CategoryDTO> getDeletableCategories();

    /**
     * 根据状态统计分类数量
     */
    long countCategoriesByStatus(Integer status);

    /**
     * 统计激活分类数量
     */
    long countActiveCategories();

    /**
     * 统计根分类数量
     */
    long countRootCategories();

    /**
     * 根据父分类ID统计子分类数量
     */
    long countChildrenByParentId(Long parentId);

    /**
     * 获取分类统计信息
     */
    CategoryStatistics getCategoryStatistics();

    /**
     * 批量删除分类
     */
    void batchDeleteCategories(List<Long> categoryIds);

    /**
     * 初始化默认分类
     */
    void initializeDefaultCategories();

    /**
     * 重建分类树缓存
     */
    void rebuildCategoryTreeCache();

    /**
     * 分类统计信息接口
     */
    interface CategoryStatistics {
        long getTotalCategories();
        long getActiveCategories();
        long getRootCategories();
        long getEmptyCategories();
        long getTotalProjects();
        double getAverageProjectsPerCategory();
        CategoryDTO getMostPopularCategory();
    }
}
