package com.quickcode.service.impl;

import com.quickcode.dto.category.CategoryCreateRequest;
import com.quickcode.dto.category.CategoryDTO;
import com.quickcode.dto.category.CategoryUpdateRequest;
import com.quickcode.dto.common.PageResponse;
import com.quickcode.entity.Category;
import com.quickcode.repository.CategoryRepository;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 分类服务实现类
 * 提供分类相关的业务逻辑实现
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;

    @Override
    public CategoryDTO createCategory(CategoryCreateRequest request) {
        log.debug("创建分类: name={}, code={}", request.getName(), request.getCode());

        // 验证请求数据
        request.validate();
        request.normalize();

        // 检查分类代码是否重复
        if (!isCategoryCodeAvailable(request.getCode())) {
            throw new RuntimeException("分类代码已存在: " + request.getCode());
        }

        // 检查分类名称是否重复
        if (!isCategoryNameAvailable(request.getName())) {
            throw new RuntimeException("分类名称已存在: " + request.getName());
        }

        // 验证父分类是否存在
        if (request.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父分类不存在: " + request.getParentId()));
            
            // 检查父分类是否激活
            if (!parentCategory.isActive()) {
                throw new RuntimeException("父分类未激活，无法创建子分类");
            }
        }

        // 获取排序顺序
        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            // 自动分配排序顺序
            if (request.getParentId() != null) {
                Integer maxOrder = categoryRepository.findMaxSortOrderByParentId(request.getParentId());
                sortOrder = (maxOrder != null ? maxOrder : 0) + 1;
            } else {
                Integer maxOrder = categoryRepository.findMaxSortOrderForRootCategories();
                sortOrder = (maxOrder != null ? maxOrder : 0) + 1;
            }
        }

        // 创建分类实体
        Category category = Category.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .sortOrder(sortOrder)
                .status(1) // 默认激活状态
                .build();

        // 保存分类
        category = categoryRepository.save(category);

        log.info("分类创建成功: id={}, name={}, code={}", category.getId(), category.getName(), category.getCode());
        return CategoryDTO.fromCategory(category);
    }

    @Override
    public CategoryDTO updateCategory(CategoryUpdateRequest request) {
        log.debug("更新分类: id={}, name={}, code={}", request.getId(), request.getName(), request.getCode());

        // 验证请求数据
        request.validate();
        request.normalize();

        // 获取分类
        Category category = getById(request.getId());

        // 检查分类代码是否重复（排除自己）
        if (!request.getCode().equals(category.getCode()) && !isCategoryCodeAvailable(request.getCode())) {
            throw new RuntimeException("分类代码已存在: " + request.getCode());
        }

        // 检查分类名称是否重复（排除自己）
        if (!request.getName().equals(category.getName()) && !isCategoryNameAvailable(request.getName())) {
            throw new RuntimeException("分类名称已存在: " + request.getName());
        }

        // 验证父分类变更
        if (request.isParentChanged(category.getParentId())) {
            if (request.getParentId() != null) {
                // 检查新父分类是否存在
                Category newParent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new RuntimeException("父分类不存在: " + request.getParentId()));
                
                // 检查是否会形成循环引用
                if (wouldCreateCircularReference(request.getId(), request.getParentId())) {
                    throw new RuntimeException("不能将分类移动到其子分类下，会形成循环引用");
                }
            }
        }

        // 更新分类信息
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder());
        category.setStatus(request.getStatus());

        // 保存更新
        category = categoryRepository.save(category);

        log.info("分类更新成功: id={}, name={}, code={}", category.getId(), category.getName(), category.getCode());
        return CategoryDTO.fromCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryDetail(Long categoryId) {
        log.debug("获取分类详情: categoryId={}", categoryId);

        Category category = getById(categoryId);
        return CategoryDTO.fromCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> getCategoryByCode(String code) {
        log.debug("根据代码获取分类: code={}", code);

        return categoryRepository.findByCode(code)
                .map(CategoryDTO::fromCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getActiveCategories() {
        log.debug("获取所有激活的分类");

        List<Category> categories = categoryRepository.findActiveCategories();
        return categories.stream()
                .map(CategoryDTO::fromCategory)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryDTO> getCategories(Pageable pageable) {
        log.debug("分页获取分类列表");

        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return PageResponse.fromPage(categoryPage.map(CategoryDTO::fromCategory));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryDTO> getActiveCategories(Pageable pageable) {
        log.debug("分页获取激活的分类列表");

        Page<Category> categoryPage = categoryRepository.findActiveCategories(pageable);
        return PageResponse.fromPage(categoryPage.map(CategoryDTO::fromCategory));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getRootCategories() {
        log.debug("获取根分类列表");

        List<Category> categories = categoryRepository.findRootCategories();
        return categories.stream()
                .map(CategoryDTO::fromCategory)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getActiveRootCategories() {
        log.debug("获取激活的根分类列表");

        List<Category> categories = categoryRepository.findActiveRootCategories();
        return categories.stream()
                .map(CategoryDTO::fromCategory)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getChildrenByParentId(Long parentId) {
        log.debug("根据父分类ID获取子分类列表: parentId={}", parentId);

        List<Category> categories = categoryRepository.findByParentId(parentId);
        return categories.stream()
                .map(CategoryDTO::fromCategory)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getActiveChildrenByParentId(Long parentId) {
        log.debug("根据父分类ID获取激活的子分类列表: parentId={}", parentId);

        List<Category> categories = categoryRepository.findActiveChildrenByParentId(parentId);
        return categories.stream()
                .map(CategoryDTO::fromCategory)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryTree() {
        log.debug("获取分类树形结构");

        List<Category> rootCategories = categoryRepository.findRootCategories();
        return buildCategoryTree(rootCategories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getActiveCategoryTree() {
        log.debug("获取激活分类的树形结构");

        List<Category> rootCategories = categoryRepository.findActiveRootCategories();
        return buildActiveCategoryTree(rootCategories);
    }

    /**
     * 构建分类树
     */
    private List<CategoryDTO> buildCategoryTree(List<Category> rootCategories) {
        return rootCategories.stream()
                .map(this::buildCategoryNode)
                .toList();
    }

    /**
     * 构建激活分类树
     */
    private List<CategoryDTO> buildActiveCategoryTree(List<Category> rootCategories) {
        return rootCategories.stream()
                .map(this::buildActiveCategoryNode)
                .toList();
    }

    /**
     * 构建分类节点（包含所有子分类）
     */
    private CategoryDTO buildCategoryNode(Category category) {
        CategoryDTO dto = CategoryDTO.fromCategory(category);
        
        List<Category> children = categoryRepository.findByParentId(category.getId());
        if (!children.isEmpty()) {
            List<CategoryDTO> childrenDTOs = children.stream()
                    .map(this::buildCategoryNode)
                    .toList();
            dto.setChildren(childrenDTOs);
        }
        
        return dto;
    }

    /**
     * 构建激活分类节点（只包含激活的子分类）
     */
    private CategoryDTO buildActiveCategoryNode(Category category) {
        CategoryDTO dto = CategoryDTO.fromCategory(category);
        
        List<Category> children = categoryRepository.findActiveChildrenByParentId(category.getId());
        if (!children.isEmpty()) {
            List<CategoryDTO> childrenDTOs = children.stream()
                    .map(this::buildActiveCategoryNode)
                    .toList();
            dto.setChildren(childrenDTOs);
        }
        
        return dto;
    }

    /**
     * 检查是否会形成循环引用
     */
    private boolean wouldCreateCircularReference(Long categoryId, Long newParentId) {
        if (newParentId == null) {
            return false;
        }
        
        // 检查新父分类是否是当前分类的子分类
        Category currentParent = categoryRepository.findById(newParentId).orElse(null);
        while (currentParent != null) {
            if (currentParent.getId().equals(categoryId)) {
                return true;
            }
            currentParent = currentParent.getParentId() != null ? 
                    categoryRepository.findById(currentParent.getParentId()).orElse(null) : null;
        }
        
        return false;
    }

    // BaseService接口的方法实现
    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Category save(Category entity) {
        return categoryRepository.save(entity);
    }

    @Override
    public List<Category> saveAll(List<Category> entities) {
        return categoryRepository.saveAll(entities);
    }

    @Override
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public void delete(Category entity) {
        categoryRepository.delete(entity);
    }

    @Override
    public void deleteAll(List<Category> entities) {
        categoryRepository.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        categoryRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return categoryRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryTreeById(Long categoryId) {
        log.debug("根据分类ID获取完整的分类树: categoryId={}", categoryId);

        Category category = getById(categoryId);
        return buildCategoryNode(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryPath(Long categoryId) {
        log.debug("获取分类路径: categoryId={}", categoryId);

        List<Category> pathCategories = categoryRepository.findCategoryPath(categoryId);
        return pathCategories.stream()
                .map(CategoryDTO::fromCategory)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> searchCategories(String keyword) {
        log.debug("搜索分类: keyword={}", keyword);

        List<Category> categories = categoryRepository.searchByName(keyword);
        return categories.stream()
                .map(CategoryDTO::fromCategory)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryDTO> searchCategories(String keyword, Pageable pageable) {
        log.debug("分页搜索分类: keyword={}", keyword);

        Page<Category> categoryPage = categoryRepository.searchByName(keyword, pageable);
        return PageResponse.fromPage(categoryPage.map(CategoryDTO::fromCategory));
    }

    @Override
    public void enableCategory(Long categoryId) {
        log.debug("启用分类: categoryId={}", categoryId);

        Category category = getById(categoryId);
        category.enable();
        categoryRepository.save(category);

        log.info("分类启用成功: categoryId={}", categoryId);
    }

    @Override
    public void disableCategory(Long categoryId) {
        log.debug("禁用分类: categoryId={}", categoryId);

        Category category = getById(categoryId);

        // 检查是否有子分类
        if (hasChildren(categoryId)) {
            throw new RuntimeException("该分类下有子分类，无法禁用");
        }

        // 检查是否有项目
        if (hasProjects(categoryId)) {
            throw new RuntimeException("该分类下有项目，无法禁用");
        }

        category.disable();
        categoryRepository.save(category);

        log.info("分类禁用成功: categoryId={}", categoryId);
    }

    @Override
    public void moveCategory(Long categoryId, Long newParentId) {
        log.debug("移动分类: categoryId={}, newParentId={}", categoryId, newParentId);

        Category category = getById(categoryId);

        // 验证新父分类
        if (newParentId != null) {
            Category newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new RuntimeException("新父分类不存在: " + newParentId));

            // 检查是否会形成循环引用
            if (wouldCreateCircularReference(categoryId, newParentId)) {
                throw new RuntimeException("不能将分类移动到其子分类下，会形成循环引用");
            }
        }

        category.setParentId(newParentId);
        categoryRepository.save(category);

        log.info("分类移动成功: categoryId={}, newParentId={}", categoryId, newParentId);
    }

    @Override
    public void updateSortOrder(Long categoryId, Integer sortOrder) {
        log.debug("调整分类排序: categoryId={}, sortOrder={}", categoryId, sortOrder);

        Category category = getById(categoryId);
        category.setSortOrder(sortOrder);
        categoryRepository.save(category);

        log.info("分类排序调整成功: categoryId={}, sortOrder={}", categoryId, sortOrder);
    }

    @Override
    public void batchUpdateSortOrder(List<Long> categoryIds, List<Integer> sortOrders) {
        log.debug("批量更新分类排序: categoryIds={}, sortOrders={}", categoryIds, sortOrders);

        if (categoryIds.size() != sortOrders.size()) {
            throw new RuntimeException("分类ID列表和排序列表长度不匹配");
        }

        for (int i = 0; i < categoryIds.size(); i++) {
            try {
                updateSortOrder(categoryIds.get(i), sortOrders.get(i));
            } catch (Exception e) {
                log.warn("更新分类排序失败: categoryId={}, sortOrder={}", categoryIds.get(i), sortOrders.get(i), e);
            }
        }

        log.info("批量更新分类排序完成: count={}", categoryIds.size());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCategoryCodeAvailable(String code) {
        return !categoryRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCategoryNameAvailable(String name) {
        return !categoryRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteCategory(Long categoryId) {
        return !hasChildren(categoryId) && !hasProjects(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasChildren(Long categoryId) {
        return categoryRepository.hasChildren(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasProjects(Long categoryId) {
        return categoryRepository.hasProjects(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getProjectCount(Long categoryId) {
        return projectRepository.countByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalProjectCount(Long categoryId) {
        Category category = getById(categoryId);
        return category.getTotalProjectCount();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getPopularCategories(int limit) {
        log.debug("获取热门分类列表: limit={}", limit);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, limit);
        List<Object[]> results = categoryRepository.findPopularCategories(pageable);

        return results.stream()
                .map(result -> CategoryDTO.fromCategory((Category) result[0]))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getEmptyCategories() {
        log.debug("获取空分类列表");

        List<Category> categories = categoryRepository.findEmptyCategories();
        return categories.stream()
                .map(CategoryDTO::fromCategory)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getDeletableCategories() {
        log.debug("获取可删除的分类列表");

        List<Category> categories = categoryRepository.findDeletableCategories();
        return categories.stream()
                .map(CategoryDTO::fromCategory)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countCategoriesByStatus(Integer status) {
        return categoryRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveCategories() {
        return categoryRepository.countActiveCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public long countRootCategories() {
        return categoryRepository.countRootCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public long countChildrenByParentId(Long parentId) {
        return categoryRepository.countByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryStatistics getCategoryStatistics() {
        return new CategoryStatisticsImpl();
    }

    @Override
    public void batchDeleteCategories(List<Long> categoryIds) {
        log.debug("批量删除分类: categoryIds={}", categoryIds);

        for (Long categoryId : categoryIds) {
            try {
                if (canDeleteCategory(categoryId)) {
                    categoryRepository.deleteById(categoryId);
                } else {
                    log.warn("分类无法删除（有子分类或项目）: categoryId={}", categoryId);
                }
            } catch (Exception e) {
                log.warn("删除分类失败: categoryId={}", categoryId, e);
            }
        }

        log.info("批量删除分类完成: count={}", categoryIds.size());
    }

    @Override
    public void initializeDefaultCategories() {
        log.debug("初始化默认分类");

        // 检查是否已经有分类
        if (categoryRepository.count() > 0) {
            log.info("分类已存在，跳过初始化");
            return;
        }

        // 创建默认分类
        createDefaultCategory("Web开发", "WEB", "Web应用开发相关项目", null, 1);
        createDefaultCategory("移动开发", "MOBILE", "移动应用开发相关项目", null, 2);
        createDefaultCategory("桌面应用", "DESKTOP", "桌面应用开发相关项目", null, 3);
        createDefaultCategory("后端开发", "BACKEND", "后端服务开发相关项目", null, 4);
        createDefaultCategory("前端开发", "FRONTEND", "前端界面开发相关项目", null, 5);
        createDefaultCategory("全栈开发", "FULLSTACK", "全栈开发相关项目", null, 6);
        createDefaultCategory("游戏开发", "GAME", "游戏开发相关项目", null, 7);
        createDefaultCategory("人工智能", "AI_ML", "AI和机器学习相关项目", null, 8);
        createDefaultCategory("区块链", "BLOCKCHAIN", "区块链技术相关项目", null, 9);
        createDefaultCategory("物联网", "IOT", "物联网技术相关项目", null, 10);
        createDefaultCategory("DevOps", "DEVOPS", "DevOps和运维相关项目", null, 11);
        createDefaultCategory("工具软件", "TOOL", "开发工具和实用软件", null, 12);
        createDefaultCategory("模板主题", "TEMPLATE", "网站模板和主题", null, 13);
        createDefaultCategory("组件库", "COMPONENT", "UI组件和功能组件", null, 14);
        createDefaultCategory("插件扩展", "PLUGIN", "各种插件和扩展", null, 15);
        createDefaultCategory("其他", "OTHER", "其他类型项目", null, 16);

        log.info("默认分类初始化完成");
    }

    /**
     * 创建默认分类
     */
    private void createDefaultCategory(String name, String code, String description, Long parentId, Integer sortOrder) {
        Category category = Category.builder()
                .name(name)
                .code(code)
                .description(description)
                .parentId(parentId)
                .sortOrder(sortOrder)
                .status(1)
                .build();

        categoryRepository.save(category);
        log.debug("创建默认分类: name={}, code={}", name, code);
    }

    @Override
    public void rebuildCategoryTreeCache() {
        log.debug("重建分类树缓存");
        // TODO: 实现分类树缓存重建逻辑
        log.info("分类树缓存重建完成");
    }

    /**
     * 分类统计信息实现类
     */
    private class CategoryStatisticsImpl implements CategoryStatistics {
        @Override
        public long getTotalCategories() {
            return categoryRepository.countAllCategories();
        }

        @Override
        public long getActiveCategories() {
            return categoryRepository.countActiveCategories();
        }

        @Override
        public long getRootCategories() {
            return categoryRepository.countRootCategories();
        }

        @Override
        public long getEmptyCategories() {
            return categoryRepository.findEmptyCategories().size();
        }

        @Override
        public long getTotalProjects() {
            // TODO: 统计所有分类下的项目总数
            return 0L;
        }

        @Override
        public double getAverageProjectsPerCategory() {
            long totalCategories = getActiveCategories();
            long totalProjects = getTotalProjects();
            return totalCategories > 0 ? (double) totalProjects / totalCategories : 0.0;
        }

        @Override
        public CategoryDTO getMostPopularCategory() {
            List<CategoryDTO> popularCategories = getPopularCategories(1);
            return popularCategories.isEmpty() ? null : popularCategories.get(0);
        }
    }
}
