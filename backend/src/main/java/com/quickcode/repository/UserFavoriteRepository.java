package com.quickcode.repository;

import com.quickcode.entity.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户收藏Repository接口
 * 提供用户收藏相关的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    /**
     * 根据用户ID和项目ID查找收藏记录
     * 
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 收藏记录
     */
    Optional<UserFavorite> findByUserIdAndProjectId(Long userId, Long projectId);

    /**
     * 检查用户是否收藏了指定项目
     * 
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 是否收藏
     */
    boolean existsByUserIdAndProjectId(Long userId, Long projectId);

    /**
     * 根据用户ID分页查询收藏列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 收藏列表
     */
    Page<UserFavorite> findByUserIdOrderByCreatedTimeDesc(Long userId, Pageable pageable);

    /**
     * 根据项目ID查询收藏该项目的用户列表
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 收藏用户列表
     */
    Page<UserFavorite> findByProjectIdOrderByCreatedTimeDesc(Long projectId, Pageable pageable);

    /**
     * 统计用户收藏的项目数量
     * 
     * @param userId 用户ID
     * @return 收藏数量
     */
    long countByUserId(Long userId);

    /**
     * 统计项目被收藏的次数
     * 
     * @param projectId 项目ID
     * @return 收藏次数
     */
    long countByProjectId(Long projectId);

    /**
     * 根据用户ID删除所有收藏记录
     * 
     * @param userId 用户ID
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM UserFavorite uf WHERE uf.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据项目ID删除所有收藏记录
     * 
     * @param projectId 项目ID
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM UserFavorite uf WHERE uf.projectId = :projectId")
    int deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 获取用户最近收藏的项目
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近收藏的项目列表
     */
    @Query("SELECT uf FROM UserFavorite uf WHERE uf.userId = :userId ORDER BY uf.createdTime DESC")
    List<UserFavorite> findRecentFavoritesByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 获取热门收藏项目（按收藏数量排序）
     * 
     * @param limit 限制数量
     * @return 热门收藏项目列表
     */
    @Query("SELECT uf.projectId, COUNT(uf) as favoriteCount FROM UserFavorite uf " +
           "GROUP BY uf.projectId ORDER BY favoriteCount DESC")
    List<Object[]> findPopularFavoriteProjects(Pageable pageable);

    /**
     * 获取指定时间范围内的收藏统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 收藏统计
     */
    @Query("SELECT COUNT(uf) FROM UserFavorite uf WHERE uf.createdTime BETWEEN :startTime AND :endTime")
    long countByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 批量查询用户对多个项目的收藏状态
     * 
     * @param userId 用户ID
     * @param projectIds 项目ID列表
     * @return 收藏记录列表
     */
    @Query("SELECT uf FROM UserFavorite uf WHERE uf.userId = :userId AND uf.projectId IN :projectIds")
    List<UserFavorite> findByUserIdAndProjectIdIn(@Param("userId") Long userId, 
                                                   @Param("projectIds") List<Long> projectIds);
}
