package com.quickcode.service;

import com.quickcode.dto.common.PageResponse;
import com.quickcode.dto.project.ProjectDTO;
import com.quickcode.entity.UserFavorite;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 收藏服务接口
 * 提供项目收藏相关的业务逻辑
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface FavoriteService {

    /**
     * 收藏项目
     * 
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 收藏记录
     * @throws RuntimeException 如果项目不存在或已收藏
     */
    UserFavorite favoriteProject(Long userId, Long projectId);

    /**
     * 取消收藏项目
     * 
     * @param userId 用户ID
     * @param projectId 项目ID
     * @throws RuntimeException 如果收藏记录不存在
     */
    void unfavoriteProject(Long userId, Long projectId);

    /**
     * 检查用户是否收藏了指定项目
     * 
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 是否收藏
     */
    boolean isFavorited(Long userId, Long projectId);

    /**
     * 获取用户收藏的项目列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 收藏的项目列表
     */
    PageResponse<ProjectDTO> getUserFavoriteProjects(Long userId, Pageable pageable);

    /**
     * 获取用户收藏的项目列表（带搜索）
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 收藏的项目列表
     */
    PageResponse<ProjectDTO> getUserFavoriteProjects(Long userId, String keyword, Pageable pageable);

    /**
     * 统计用户收藏的项目数量
     * 
     * @param userId 用户ID
     * @return 收藏数量
     */
    long countUserFavorites(Long userId);

    /**
     * 统计项目被收藏的次数
     * 
     * @param projectId 项目ID
     * @return 收藏次数
     */
    long countProjectFavorites(Long projectId);

    /**
     * 获取用户最近收藏的项目
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 最近收藏的项目列表
     */
    List<ProjectDTO> getUserRecentFavorites(Long userId, int limit);

    /**
     * 获取热门收藏项目
     * 
     * @param limit 限制数量
     * @return 热门收藏项目列表
     */
    List<ProjectDTO> getPopularFavoriteProjects(int limit);

    /**
     * 批量检查用户对多个项目的收藏状态
     * 
     * @param userId 用户ID
     * @param projectIds 项目ID列表
     * @return 项目ID到收藏状态的映射
     */
    java.util.Map<Long, Boolean> batchCheckFavoriteStatus(Long userId, List<Long> projectIds);

    /**
     * 删除用户的所有收藏记录
     * 
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteUserAllFavorites(Long userId);

    /**
     * 删除项目的所有收藏记录
     * 
     * @param projectId 项目ID
     * @return 删除的记录数
     */
    int deleteProjectAllFavorites(Long projectId);

    /**
     * 更新项目的收藏数量缓存
     * 
     * @param projectId 项目ID
     */
    void updateProjectFavoriteCount(Long projectId);
}
