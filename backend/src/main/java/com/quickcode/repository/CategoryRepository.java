package com.quickcode.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.quickcode.entity.Category;

/**
 * 项目分类Repository接口
 * 提供分类相关的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface CategoryRepository extends BaseRepository<Category, Long> {

    /**
     * 根据分类代码查找分类
     */
    Optional<Category> findByCode(String code);

    /**
     * 根据分类名称查找分类
     */
    Optional<Category> findByName(String name);

    /**
     * 检查分类代码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据状态查找分类
     */
    List<Category> findByStatus(Integer status);

    /**
     * 根据状态分页查找分类
     */
    Page<Category> findByStatus(Integer status, Pageable pageable);

    /**
     * 查找激活状态的分类
     */
    @Query("SELECT c FROM Category c WHERE c.status = 1")
    List<Category> findActiveCategories();

    /**
     * 分页查找激活状态的分类
     */
    @Query("SELECT c FROM Category c WHERE c.status = 1")
    Page<Category> findActiveCategories(Pageable pageable);

    /**
     * 根据父分类ID查找子分类
     */
    List<Category> findByParentId(Long parentId);

    /**
     * 根据父分类ID分页查找子分类
     */
    Page<Category> findByParentId(Long parentId, Pageable pageable);

    /**
     * 查找根分类（无父分类）
     */
    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL")
    List<Category> findRootCategories();

    /**
     * 分页查找根分类
     */
    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL")
    Page<Category> findRootCategories(Pageable pageable);

    /**
     * 查找激活状态的根分类
     */
    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL AND c.status = 1")
    List<Category> findActiveRootCategories();

    /**
     * 根据父分类ID查找激活状态的子分类
     */
    @Query("SELECT c FROM Category c WHERE c.parentId = :parentId AND c.status = 1")
    List<Category> findActiveChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 查找有子分类的分类
     */
    @Query("SELECT DISTINCT c FROM Category c WHERE c.id IN (SELECT DISTINCT p.parentId FROM Category p WHERE p.parentId IS NOT NULL)")
    List<Category> findCategoriesWithChildren();

    /**
     * 查找没有子分类的分类（叶子节点）
     */
    @Query("SELECT c FROM Category c WHERE c.id NOT IN (SELECT DISTINCT p.parentId FROM Category p WHERE p.parentId IS NOT NULL)")
    List<Category> findLeafCategories();

    /**
     * 根据排序顺序查找分类
     */
    @Query("SELECT c FROM Category c WHERE c.status = 1 ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findActiveCategoriesOrderBySortOrder();

    /**
     * 根据父分类ID和排序顺序查找子分类
     */
    @Query("SELECT c FROM Category c WHERE c.parentId = :parentId AND c.status = 1 ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findActiveChildrenByParentIdOrderBySortOrder(@Param("parentId") Long parentId);

    /**
     * 统计主分类数量（父分类为空）
     */
    long countByParentIdIsNull();

    /**
     * 统计子分类数量（父分类不为空）
     */
    long countByParentIdIsNotNull();

    /**
     * 查找分类层级结构（递归查询所有子分类）
     */
    @Query(value = "WITH RECURSIVE category_tree AS (" +
           "SELECT id, name, code, parent_id, 0 as level FROM project_categories WHERE id = :categoryId " +
           "UNION ALL " +
           "SELECT c.id, c.name, c.code, c.parent_id, ct.level + 1 " +
           "FROM project_categories c " +
           "INNER JOIN category_tree ct ON c.parent_id = ct.id" +
           ") SELECT * FROM category_tree", nativeQuery = true)
    List<Category> findCategoryTree(@Param("categoryId") Long categoryId);

    /**
     * 查找分类路径（从根到指定分类）
     */
    @Query(value = "WITH RECURSIVE category_path AS (" +
           "SELECT id, name, code, parent_id, 0 as level FROM project_categories WHERE id = :categoryId " +
           "UNION ALL " +
           "SELECT c.id, c.name, c.code, c.parent_id, cp.level - 1 " +
           "FROM project_categories c " +
           "INNER JOIN category_path cp ON c.id = cp.parent_id" +
           ") SELECT * FROM category_path ORDER BY level", nativeQuery = true)
    List<Category> findCategoryPath(@Param("categoryId") Long categoryId);

    /**
     * 根据名称模糊搜索分类
     */
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:keyword% AND c.status = 1")
    List<Category> searchByName(@Param("keyword") String keyword);

    /**
     * 根据名称模糊搜索分类（分页）
     */
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:keyword% AND c.status = 1")
    Page<Category> searchByName(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据代码模糊搜索分类
     */
    @Query("SELECT c FROM Category c WHERE c.code LIKE %:keyword% AND c.status = 1")
    List<Category> searchByCode(@Param("keyword") String keyword);

    /**
     * 统计分类总数
     */
    @Query("SELECT COUNT(c) FROM Category c")
    Long countAllCategories();

    /**
     * 根据状态统计分类数量
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.status = :status")
    Long countByStatus(@Param("status") Integer status);

    /**
     * 统计激活状态的分类数量
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.status = 1")
    Long countActiveCategories();

    /**
     * 统计根分类数量
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parentId IS NULL")
    Long countRootCategories();

    /**
     * 根据父分类ID统计子分类数量
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parentId = :parentId")
    Long countByParentId(@Param("parentId") Long parentId);

    /**
     * 统计激活状态的根分类数量
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parentId IS NULL AND c.status = 1")
    Long countActiveRootCategories();

    /**
     * 根据父分类ID统计激活状态的子分类数量
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parentId = :parentId AND c.status = 1")
    Long countActiveChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 查找分类的最大排序顺序
     */
    @Query("SELECT MAX(c.sortOrder) FROM Category c WHERE c.parentId = :parentId")
    Integer findMaxSortOrderByParentId(@Param("parentId") Long parentId);

    /**
     * 查找根分类的最大排序顺序
     */
    @Query("SELECT MAX(c.sortOrder) FROM Category c WHERE c.parentId IS NULL")
    Integer findMaxSortOrderForRootCategories();

    /**
     * 检查分类是否有项目
     */
    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE p.categoryId = :categoryId")
    boolean hasProjects(@Param("categoryId") Long categoryId);

    /**
     * 检查分类是否有子分类
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.parentId = :categoryId")
    boolean hasChildren(@Param("categoryId") Long categoryId);

    /**
     * 查找可以删除的分类（没有子分类且没有项目）
     */
    @Query("SELECT c FROM Category c WHERE c.id NOT IN (" +
           "SELECT DISTINCT p.parentId FROM Category p WHERE p.parentId IS NOT NULL" +
           ") AND c.id NOT IN (" +
           "SELECT DISTINCT pr.categoryId FROM Project pr WHERE pr.categoryId IS NOT NULL" +
           ")")
    List<Category> findDeletableCategories();

    /**
     * 根据分类代码列表查找分类
     */
    @Query("SELECT c FROM Category c WHERE c.code IN :codes AND c.status = 1")
    List<Category> findByCodes(@Param("codes") List<String> codes);

    /**
     * 查找热门分类（根据项目数量排序）
     */
    @Query("SELECT c, COUNT(p) as projectCount FROM Category c " +
           "LEFT JOIN Project p ON c.id = p.categoryId AND p.status = 1 " +
           "WHERE c.status = 1 " +
           "GROUP BY c.id " +
           "ORDER BY projectCount DESC")
    List<Object[]> findPopularCategories(Pageable pageable);

    /**
     * 查找空分类（没有项目的分类）
     */
    @Query("SELECT c FROM Category c WHERE c.status = 1 AND c.id NOT IN (" +
           "SELECT DISTINCT p.categoryId FROM Project p WHERE p.categoryId IS NOT NULL AND p.status = 1" +
           ")")
    List<Category> findEmptyCategories();
}
