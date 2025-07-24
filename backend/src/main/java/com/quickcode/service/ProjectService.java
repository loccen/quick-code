package com.quickcode.service;

import com.quickcode.dto.project.ProjectCreateRequest;
import com.quickcode.dto.project.ProjectDTO;
import com.quickcode.dto.project.ProjectDetailDTO;
import com.quickcode.dto.project.ProjectSearchRequest;
import com.quickcode.dto.project.ProjectUpdateRequest;
import com.quickcode.dto.common.PageResponse;
import com.quickcode.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 项目Service接口
 * 提供项目相关的业务逻辑方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface ProjectService extends BaseService<Project, Long> {

    /**
     * 创建项目
     */
    ProjectDTO createProject(ProjectCreateRequest request, Long userId);

    /**
     * 更新项目
     */
    ProjectDTO updateProject(ProjectUpdateRequest request, Long userId);

    /**
     * 根据ID获取项目详情
     */
    ProjectDetailDTO getProjectDetail(Long projectId);

    /**
     * 根据ID获取项目详情（包含用户权限检查）
     */
    ProjectDetailDTO getProjectDetail(Long projectId, Long userId);

    /**
     * 获取已发布项目的详情（仅用于公开访问）
     */
    ProjectDetailDTO getPublishedProjectDetail(Long projectId);

    /**
     * 搜索项目
     */
    PageResponse<ProjectDTO> searchProjects(ProjectSearchRequest request);

    /**
     * 获取已发布的项目列表
     */
    PageResponse<ProjectDTO> getPublishedProjects(Pageable pageable);

    /**
     * 获取精选项目列表
     */
    List<ProjectDTO> getFeaturedProjects(int limit);

    /**
     * 获取热门项目列表
     */
    List<ProjectDTO> getPopularProjects(int limit);

    /**
     * 获取最新项目列表
     */
    List<ProjectDTO> getLatestProjects(int limit);

    /**
     * 获取高评分项目列表
     */
    List<ProjectDTO> getHighRatedProjects(BigDecimal minRating, int limit);

    /**
     * 根据分类获取项目列表
     */
    PageResponse<ProjectDTO> getProjectsByCategory(Long categoryId, Pageable pageable);

    /**
     * 根据用户获取项目列表
     */
    PageResponse<ProjectDTO> getProjectsByUser(Long userId, Pageable pageable);

    /**
     * 根据用户获取项目列表（包含状态筛选）
     */
    PageResponse<ProjectDTO> getProjectsByUser(Long userId, Integer status, Pageable pageable);

    /**
     * 根据技术栈搜索项目
     */
    List<ProjectDTO> getProjectsByTechStack(String techStack, int limit);

    /**
     * 根据标签搜索项目
     */
    List<ProjectDTO> getProjectsByTag(String tag, int limit);

    /**
     * 获取相关项目推荐
     */
    List<ProjectDTO> getRelatedProjects(Long projectId, int limit);

    /**
     * 发布项目
     */
    void publishProject(Long projectId, Long userId);

    /**
     * 下架项目
     */
    void takeOfflineProject(Long projectId, Long userId);

    /**
     * 审核通过项目
     */
    void approveProject(Long projectId, Long adminUserId);

    /**
     * 审核拒绝项目
     */
    void rejectProject(Long projectId, Long adminUserId, String reason);

    /**
     * 设置为精选项目
     */
    void setAsFeatured(Long projectId, Long adminUserId);

    /**
     * 取消精选
     */
    void unsetFeatured(Long projectId, Long adminUserId);

    /**
     * 增加项目浏览次数
     */
    void incrementViewCount(Long projectId);

    /**
     * 增加项目下载次数
     */
    void incrementDownloadCount(Long projectId);

    /**
     * 增加项目点赞次数
     */
    void incrementLikeCount(Long projectId, Long userId);

    /**
     * 减少项目点赞次数
     */
    void decrementLikeCount(Long projectId, Long userId);

    /**
     * 检查用户是否已点赞项目
     */
    boolean isLikedByUser(Long projectId, Long userId);

    /**
     * 检查用户是否可以编辑项目
     */
    boolean canEditProject(Long projectId, Long userId);

    /**
     * 检查用户是否可以删除项目
     */
    boolean canDeleteProject(Long projectId, Long userId);

    /**
     * 检查项目是否可以购买
     */
    boolean canPurchaseProject(Long projectId, Long userId);

    /**
     * 获取待审核项目列表
     */
    PageResponse<ProjectDTO> getPendingReviewProjects(Pageable pageable);

    /**
     * 获取需要审核的项目数量
     */
    long countPendingReviewProjects();

    /**
     * 根据状态统计项目数量
     */
    long countProjectsByStatus(Integer status);

    /**
     * 统计已发布项目数量
     */
    long countPublishedProjects();

    /**
     * 统计精选项目数量
     */
    long countFeaturedProjects();

    /**
     * 根据用户统计项目数量
     */
    long countProjectsByUser(Long userId);

    /**
     * 根据分类统计项目数量
     */
    long countProjectsByCategory(Long categoryId);

    /**
     * 统计总下载量
     */
    long getTotalDownloads();

    /**
     * 根据用户统计总下载量
     */
    long getTotalDownloadsByUser(Long userId);

    /**
     * 获取项目统计信息
     */
    ProjectStatistics getProjectStatistics();

    /**
     * 获取用户项目统计信息
     */
    UserProjectStatistics getUserProjectStatistics(Long userId);

    /**
     * 根据创建时间范围查找项目
     */
    List<ProjectDTO> getProjectsByDateRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查找长时间未更新的项目
     */
    List<ProjectDTO> getStaleProjects(LocalDateTime cutoffTime);

    /**
     * 批量更新项目状态
     */
    void batchUpdateProjectStatus(List<Long> projectIds, Integer status, Long adminUserId);

    /**
     * 批量删除项目
     */
    void batchDeleteProjects(List<Long> projectIds, Long adminUserId);

    /**
     * 项目统计信息DTO
     */
    interface ProjectStatistics {
        long getTotalProjects();
        long getPublishedProjects();
        long getPendingProjects();
        long getFeaturedProjects();
        long getTotalDownloads();
        long getTotalViews();
        long getTotalLikes();
        BigDecimal getAverageRating();
    }

    /**
     * 用户项目统计信息DTO
     */
    interface UserProjectStatistics {
        long getTotalProjects();
        long getPublishedProjects();
        long getPendingProjects();
        long getTotalDownloads();
        long getTotalViews();
        long getTotalLikes();
        BigDecimal getAverageRating();
    }
}
