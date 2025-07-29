package com.quickcode.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.quickcode.entity.Project;

/**
 * 项目Repository接口
 * 提供项目相关的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface ProjectRepository extends BaseRepository<Project, Long> {

    /**
     * 根据标题查找项目
     */
    Optional<Project> findByTitle(String title);

    /**
     * 检查标题是否存在
     */
    boolean existsByTitle(String title);

    /**
     * 根据用户ID查找项目
     */
    List<Project> findByUserId(Long userId);

    /**
     * 根据用户ID分页查找项目
     */
    Page<Project> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据分类ID查找项目
     */
    List<Project> findByCategoryId(Long categoryId);

    /**
     * 根据分类ID分页查找项目
     */
    Page<Project> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * 根据状态查找项目
     */
    List<Project> findByStatus(Integer status);

    /**
     * 根据状态分页查找项目
     */
    Page<Project> findByStatus(Integer status, Pageable pageable);

    /**
     * 查找已发布的项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = 1")
    List<Project> findPublishedProjects();

    /**
     * 分页查找已发布的项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = 1")
    Page<Project> findPublishedProjects(Pageable pageable);

    /**
     * 查找精选项目
     */
    @Query("SELECT p FROM Project p WHERE p.isFeatured = true AND p.status = 1")
    List<Project> findFeaturedProjects();

    /**
     * 分页查找精选项目
     */
    @Query("SELECT p FROM Project p WHERE p.isFeatured = true AND p.status = 1")
    Page<Project> findFeaturedProjects(Pageable pageable);

    /**
     * 根据价格范围查找项目
     */
    @Query("SELECT p FROM Project p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.status = 1")
    List<Project> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                   @Param("maxPrice") BigDecimal maxPrice);

    /**
     * 根据价格范围分页查找项目
     */
    @Query("SELECT p FROM Project p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.status = 1")
    Page<Project> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                   @Param("maxPrice") BigDecimal maxPrice, 
                                   Pageable pageable);

    /**
     * 根据评分范围查找项目
     */
    @Query("SELECT p FROM Project p WHERE p.rating BETWEEN :minRating AND :maxRating AND p.status = 1")
    List<Project> findByRatingRange(@Param("minRating") BigDecimal minRating, 
                                    @Param("maxRating") BigDecimal maxRating);

    /**
     * 根据评分范围分页查找项目
     */
    @Query("SELECT p FROM Project p WHERE p.rating BETWEEN :minRating AND :maxRating AND p.status = 1")
    Page<Project> findByRatingRange(@Param("minRating") BigDecimal minRating, 
                                    @Param("maxRating") BigDecimal maxRating, 
                                    Pageable pageable);

    /**
     * 全文搜索项目（标题和描述）
     */
    @Query("SELECT p FROM Project p WHERE (p.title LIKE %:keyword% OR p.description LIKE %:keyword%) AND p.status = 1")
    List<Project> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 全文搜索项目（分页）
     */
    @Query("SELECT p FROM Project p WHERE (p.title LIKE %:keyword% OR p.description LIKE %:keyword%) AND p.status = 1")
    Page<Project> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据技术栈搜索项目
     */
    @Query(value = "SELECT * FROM projects p WHERE JSON_CONTAINS(p.tech_stack, JSON_QUOTE(:techStack)) AND p.status = 1", nativeQuery = true)
    List<Project> findByTechStack(@Param("techStack") String techStack);

    /**
     * 根据标签搜索项目
     */
    @Query(value = "SELECT * FROM projects p WHERE JSON_CONTAINS(p.tags, JSON_QUOTE(:tag)) AND p.status = 1", nativeQuery = true)
    List<Project> findByTag(@Param("tag") String tag);

    /**
     * 复合条件搜索项目
     */
    @Query("SELECT p FROM Project p WHERE " +
           "(:keyword IS NULL OR p.title LIKE %:keyword% OR p.description LIKE %:keyword%) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR p.rating >= :minRating) AND " +
           "p.status = 1")
    Page<Project> searchProjects(@Param("keyword") String keyword,
                                 @Param("categoryId") Long categoryId,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("minRating") BigDecimal minRating,
                                 Pageable pageable);

    /**
     * 查找热门项目（按下载量排序）
     */
    @Query("SELECT p FROM Project p WHERE p.status = 1 ORDER BY p.downloadCount DESC")
    List<Project> findPopularProjects(Pageable pageable);

    /**
     * 查找最新项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = 1 ORDER BY p.publishedTime DESC")
    List<Project> findLatestProjects(Pageable pageable);

    /**
     * 查找高评分项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = 1 AND p.rating >= :minRating ORDER BY p.rating DESC")
    List<Project> findHighRatedProjects(@Param("minRating") BigDecimal minRating, Pageable pageable);

    /**
     * 根据创建时间范围查找项目
     */
    @Query("SELECT p FROM Project p WHERE p.createdTime BETWEEN :startTime AND :endTime")
    List<Project> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 根据发布时间范围查找项目
     */
    @Query("SELECT p FROM Project p WHERE p.publishedTime BETWEEN :startTime AND :endTime")
    List<Project> findByPublishedTimeBetween(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计项目总数
     */
    @Query("SELECT COUNT(p) FROM Project p")
    Long countAllProjects();

    /**
     * 根据状态统计项目数量
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    Long countByStatus(@Param("status") Integer status);

    /**
     * 统计已发布项目数量
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = 1")
    Long countPublishedProjects();

    /**
     * 统计精选项目数量
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.isFeatured = true AND p.status = 1")
    Long countFeaturedProjects();

    /**
     * 根据用户ID统计项目数量
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 根据分类ID统计项目数量
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.categoryId = :categoryId AND p.status = 1")
    Long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 统计总下载量
     */
    @Query("SELECT SUM(p.downloadCount) FROM Project p WHERE p.status = 1")
    Long sumTotalDownloads();

    /**
     * 统计用户项目总下载量
     */
    @Query("SELECT SUM(p.downloadCount) FROM Project p WHERE p.userId = :userId AND p.status = 1")
    Long sumDownloadsByUserId(@Param("userId") Long userId);

    /**
     * 查找需要审核的项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = 0 ORDER BY p.createdTime ASC")
    List<Project> findPendingReviewProjects();

    /**
     * 分页查找需要审核的项目
     */
    @Query("SELECT p FROM Project p WHERE p.status = 0 ORDER BY p.createdTime ASC")
    Page<Project> findPendingReviewProjects(Pageable pageable);

    /**
     * 查找长时间未更新的项目
     */
    @Query("SELECT p FROM Project p WHERE p.updatedTime < :cutoffTime AND p.status = 1")
    List<Project> findStaleProjects(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 增加项目浏览次数
     */
    @Modifying
    @Query("UPDATE Project p SET p.viewCount = p.viewCount + 1 WHERE p.id = :projectId")
    void incrementViewCount(@Param("projectId") Long projectId);

    /**
     * 增加项目下载次数
     */
    @Modifying
    @Query("UPDATE Project p SET p.downloadCount = p.downloadCount + 1 WHERE p.id = :projectId")
    void incrementDownloadCount(@Param("projectId") Long projectId);

    /**
     * 增加项目点赞次数
     */
    @Modifying
    @Query("UPDATE Project p SET p.likeCount = p.likeCount + 1 WHERE p.id = :projectId")
    void incrementLikeCount(@Param("projectId") Long projectId);

    /**
     * 减少项目点赞次数
     */
    @Modifying
    @Query("UPDATE Project p SET p.likeCount = p.likeCount - 1 WHERE p.id = :projectId AND p.likeCount > 0")
    void decrementLikeCount(@Param("projectId") Long projectId);

    /**
     * 按指定数量增加项目浏览次数
     */
    @Modifying
    @Query("UPDATE Project p SET p.viewCount = p.viewCount + :amount WHERE p.id = :projectId")
    int incrementViewCountByAmount(@Param("projectId") Long projectId, @Param("amount") Integer amount);

    /**
     * 使用悲观锁查询项目（防止并发审核）
     */
    @Query("SELECT p FROM Project p WHERE p.id = :projectId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Project> findByIdForUpdate(@Param("projectId") Long projectId);

    /**
     * 根据ID和状态查询项目
     */
    Optional<Project> findByIdAndStatus(Long id, Integer status);

    /**
     * 根据创建者查找项目列表
     */
    List<Project> findByCreatedBy(Long createdBy);

    /**
     * 统计用户创建的项目数量
     */
    long countByCreatedBy(Long createdBy);

    /**
     * 统计用户指定状态的项目数量
     */
    long countByCreatedByAndStatus(Long createdBy, Integer status);

    /**
     * 根据创建者和状态查找项目列表
     */
    List<Project> findByCreatedByAndStatus(Long createdBy, Integer status);

    /**
     * 分页查询用户创建的项目
     */
    Page<Project> findByCreatedBy(Long createdBy, Pageable pageable);

    /**
     * 分页查询用户指定状态的项目
     */
    Page<Project> findByCreatedByAndStatus(Long createdBy, Integer status, Pageable pageable);
}
