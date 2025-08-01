package com.quickcode.service.impl;

import com.quickcode.dto.project.ProjectCreateRequest;
import com.quickcode.dto.project.ProjectDTO;
import com.quickcode.dto.project.ProjectDetailDTO;
import com.quickcode.dto.project.ProjectSearchRequest;
import com.quickcode.dto.project.ProjectUpdateRequest;
import com.quickcode.dto.project.UserProjectStats;
import com.quickcode.dto.common.PageResponse;
import com.quickcode.entity.Project;
import com.quickcode.entity.User;
import com.quickcode.entity.Category;
import com.quickcode.entity.ProjectReview;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.repository.UserRepository;
import com.quickcode.repository.CategoryRepository;
import com.quickcode.repository.ProjectReviewRepository;
import com.quickcode.service.ProjectService;
import com.quickcode.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 项目服务实现类
 * 提供项目相关的业务逻辑实现
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectReviewRepository projectReviewRepository;
    private final RedisService redisService;

    @Override
    public ProjectDTO createProject(ProjectCreateRequest request, Long userId) {
        log.debug("创建项目: title={}, userId={}", request.getTitle(), userId);

        // 验证请求数据
        request.validate();
        request.normalize();

        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 验证分类是否存在
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("分类不存在: " + request.getCategoryId()));

        // 检查标题是否重复
        if (projectRepository.existsByTitle(request.getTitle())) {
            throw new RuntimeException("项目标题已存在: " + request.getTitle());
        }

        // 创建项目实体
        Project project = Project.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .categoryId(request.getCategoryId())
                .userId(userId)
                .price(request.getPrice())
                .coverImage(request.getCoverImage())
                .demoUrl(request.getDemoUrl())
                .sourceFileUrl(request.getSourceFileUrl())
                .dockerImage(request.getDockerImage())
                .techStack(request.getTechStack())
                .tags(request.getTags())
                .status(0) // 待审核状态
                .build();

        // 保存项目
        project = projectRepository.save(project);

        log.info("项目创建成功: id={}, title={}, userId={}", project.getId(), project.getTitle(), userId);
        return ProjectDTO.fromProject(project);
    }

    @Override
    public ProjectDTO updateProject(ProjectUpdateRequest request, Long userId) {
        log.debug("更新项目: id={}, userId={}", request.getId(), userId);

        // 验证请求数据
        request.validate();
        request.normalize();

        // 获取项目
        Project project = getById(request.getId());

        // 检查权限
        if (!canEditProject(project.getId(), userId)) {
            throw new RuntimeException("无权限编辑此项目");
        }

        // 验证分类是否存在
        if (!request.getCategoryId().equals(project.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("分类不存在: " + request.getCategoryId()));
        }

        // 检查标题是否重复（排除自己）
        if (!request.getTitle().equals(project.getTitle()) && 
            projectRepository.existsByTitle(request.getTitle())) {
            throw new RuntimeException("项目标题已存在: " + request.getTitle());
        }

        // 更新项目信息
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setCategoryId(request.getCategoryId());
        project.setPrice(request.getPrice());
        project.setCoverImage(request.getCoverImage());
        project.setDemoUrl(request.getDemoUrl());
        project.setSourceFileUrl(request.getSourceFileUrl());
        project.setDockerImage(request.getDockerImage());
        project.setTechStack(request.getTechStack());
        project.setTags(request.getTags());

        // 如果项目已发布，更新后需要重新审核
        if (project.isPublished()) {
            project.setStatus(0); // 重新设为待审核状态
            log.info("已发布项目更新后重新进入审核状态: id={}", project.getId());
        }

        // 保存更新
        project = projectRepository.save(project);

        log.info("项目更新成功: id={}, title={}, userId={}", project.getId(), project.getTitle(), userId);
        return ProjectDTO.fromProject(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailDTO getProjectDetail(Long projectId) {
        log.debug("获取项目详情: projectId={}", projectId);

        Project project = getById(projectId);
        
        // 增加浏览次数（异步处理，避免影响查询性能）
        incrementViewCountAsync(projectId);

        ProjectDetailDTO detailDTO = ProjectDetailDTO.fromProject(project);
        
        // TODO: 加载评价信息
        // TODO: 加载相关项目推荐
        
        return detailDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailDTO getProjectDetail(Long projectId, Long userId) {
        log.debug("获取项目详情: projectId={}, userId={}", projectId, userId);

        ProjectDetailDTO detailDTO = getProjectDetail(projectId);
        
        // TODO: 添加用户相关信息（是否已点赞、是否已购买等）
        
        return detailDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectDTO> searchProjects(ProjectSearchRequest request) {
        log.debug("搜索项目: keyword={}, categoryId={}, sortBy={}", request.getKeyword(), request.getCategoryId(), request.getSortBy());

        // 验证和标准化搜索参数
        request.setDefaults();
        request.normalize();
        request.validate();

        // 构建分页参数
        Pageable pageable = request.toPageable();

        // 执行搜索
        Page<Project> projectPage = projectRepository.searchProjects(
                request.getKeyword(),
                request.getCategoryId(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getMinRating(),
                pageable
        );

        // 转换为DTO
        List<ProjectDTO> projectDTOs = projectPage.getContent().stream()
                .map(ProjectDTO::fromProject)
                .toList();

        return PageResponse.<ProjectDTO>builder()
                .content(projectDTOs)
                .page(projectPage.getNumber())
                .size(projectPage.getSize())
                .totalPages(projectPage.getTotalPages())
                .totalElements(projectPage.getTotalElements())
                .first(projectPage.isFirst())
                .last(projectPage.isLast())
                .hasPrevious(projectPage.hasPrevious())
                .hasNext(projectPage.hasNext())
                .numberOfElements(projectPage.getNumberOfElements())
                .empty(projectPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectDTO> getPublishedProjects(Pageable pageable) {
        log.debug("获取已发布项目列表: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Project> projectPage = projectRepository.findPublishedProjects(pageable);
        return PageResponse.fromPage(projectPage.map(ProjectDTO::fromProject));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getFeaturedProjects(int limit) {
        log.debug("获取精选项目列表: limit={}", limit);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        Page<Project> projectPage = projectRepository.findFeaturedProjects(pageable);

        return projectPage.getContent().stream()
                .map(ProjectDTO::fromProject)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getPopularProjects(int limit) {
        log.debug("获取热门项目列表: limit={}", limit);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Project> projects = projectRepository.findPopularProjects(pageable);
        
        return projects.stream()
                .map(ProjectDTO::fromProject)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getLatestProjects(int limit) {
        log.debug("获取最新项目列表: limit={}", limit);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Project> projects = projectRepository.findLatestProjects(pageable);
        
        return projects.stream()
                .map(ProjectDTO::fromProject)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getHighRatedProjects(BigDecimal minRating, int limit) {
        log.debug("获取高评分项目列表: minRating={}, limit={}", minRating, limit);

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Project> projects = projectRepository.findHighRatedProjects(minRating, pageable);
        
        return projects.stream()
                .map(ProjectDTO::fromProject)
                .toList();
    }

    /**
     * 异步增加浏览次数
     * 使用Redis计数器实现，避免在只读事务中进行数据库写操作
     */
    private void incrementViewCountAsync(Long projectId) {
        try {
            redisService.incrementProjectViewCount(projectId);
            log.debug("异步增加浏览次数成功: projectId={}", projectId);
        } catch (Exception e) {
            log.warn("异步增加浏览次数失败: projectId={}", projectId, e);
        }
    }

    // BaseService接口的方法实现
    @Override
    @Transactional(readOnly = true)
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    @Override
    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> findAll(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    @Override
    public Project save(Project entity) {
        return projectRepository.save(entity);
    }

    @Override
    public List<Project> saveAll(List<Project> entities) {
        return projectRepository.saveAll(entities);
    }

    @Override
    public void deleteById(Long id) {
        projectRepository.deleteById(id);
    }

    @Override
    public void delete(Project entity) {
        projectRepository.delete(entity);
    }

    @Override
    public void deleteAll(List<Project> entities) {
        projectRepository.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        projectRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return projectRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return projectRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectDTO> getProjectsByCategory(Long categoryId, Pageable pageable) {
        log.debug("根据分类获取项目列表: categoryId={}", categoryId);

        Page<Project> projectPage = projectRepository.findByCategoryId(categoryId, pageable);
        return PageResponse.fromPage(projectPage.map(ProjectDTO::fromProject));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectDTO> getProjectsByUser(Long userId, Pageable pageable) {
        log.debug("根据用户获取项目列表: userId={}", userId);

        Page<Project> projectPage = projectRepository.findByUserId(userId, pageable);
        return PageResponse.fromPage(projectPage.map(ProjectDTO::fromProject));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectDTO> getProjectsByUser(Long userId, Integer status, Pageable pageable) {
        log.debug("根据用户和状态获取项目列表: userId={}, status={}", userId, status);

        // TODO: 需要在Repository中添加按用户和状态查询的方法
        Page<Project> projectPage = projectRepository.findByUserId(userId, pageable);
        return PageResponse.fromPage(projectPage.map(ProjectDTO::fromProject));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsByTechStack(String techStack, int limit) {
        log.debug("根据技术栈搜索项目: techStack={}, limit={}", techStack, limit);

        List<Project> projects = projectRepository.findByTechStack(techStack);
        return projects.stream()
                .limit(limit)
                .map(ProjectDTO::fromProject)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsByTag(String tag, int limit) {
        log.debug("根据标签搜索项目: tag={}, limit={}", tag, limit);

        List<Project> projects = projectRepository.findByTag(tag);
        return projects.stream()
                .limit(limit)
                .map(ProjectDTO::fromProject)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getRelatedProjects(Long projectId, int limit) {
        log.debug("获取相关项目推荐: projectId={}, limit={}", projectId, limit);

        Project project = getById(projectId);

        // 基于分类和技术栈推荐相关项目
        List<Project> relatedProjects = projectRepository.findByCategoryId(project.getCategoryId());

        return relatedProjects.stream()
                .filter(p -> !p.getId().equals(projectId)) // 排除自己
                .filter(p -> p.getStatus().equals(1)) // 只包含已发布的项目
                .limit(limit)
                .map(ProjectDTO::fromProject)
                .toList();
    }

    @Override
    public void publishProject(Long projectId, Long userId) {
        log.debug("发布项目: projectId={}, userId={}", projectId, userId);

        Project project = getById(projectId);

        // 检查权限
        if (!canEditProject(projectId, userId)) {
            throw new RuntimeException("无权限发布此项目");
        }

        // 检查项目状态
        if (project.getStatus().equals(1)) {
            throw new RuntimeException("项目已经是发布状态");
        }

        project.publish();
        projectRepository.save(project);

        log.info("项目发布成功: projectId={}, userId={}", projectId, userId);
    }

    @Override
    public void takeOfflineProject(Long projectId, Long userId) {
        log.debug("下架项目: projectId={}, userId={}", projectId, userId);

        Project project = getById(projectId);
        Integer previousStatus = project.getStatus();

        // 检查权限
        if (!canEditProject(projectId, userId)) {
            throw new RuntimeException("无权限下架此项目");
        }

        // 检查项目状态，只有已发布的项目才能下架
        if (previousStatus != 1) {
            throw new RuntimeException("只有已发布的项目才能下架");
        }

        project.takeOffline();
        projectRepository.save(project);

        // 记录审核历史
        ProjectReview reviewRecord = ProjectReview.createReviewRecord(
                projectId, userId, previousStatus, 2,
                ProjectReview.ReviewAction.OFFLINE, "项目下架"
        );
        projectReviewRepository.save(reviewRecord);

        log.info("项目下架成功: projectId={}, userId={}", projectId, userId);
    }

    @Override
    @Transactional
    public void approveProject(Long projectId, Long adminUserId) {
        log.debug("审核通过项目: projectId={}, adminUserId={}", projectId, adminUserId);

        // 使用悲观锁防止并发审核
        Project project = projectRepository.findByIdForUpdate(projectId)
            .orElseThrow(() -> new RuntimeException("项目不存在: " + projectId));

        Integer previousStatus = project.getStatus();

        // 检查项目状态，只有待审核的项目才能审核通过
        if (previousStatus != 0) {
            throw new RuntimeException("只有待审核的项目才能审核通过，当前状态: " +
                getStatusDescription(previousStatus));
        }

        // 验证管理员权限（这里简化处理，实际应该检查用户角色）
        if (adminUserId == null || adminUserId <= 0) {
            throw new RuntimeException("无效的管理员用户ID");
        }

        try {
            // 审核通过，设置为已发布状态
            project.publish();
            projectRepository.save(project);

            // 记录审核历史
            ProjectReview reviewRecord = ProjectReview.createReviewRecord(
                    projectId, adminUserId, previousStatus, 1,
                    ProjectReview.ReviewAction.APPROVE, "管理员审核通过"
            );
            projectReviewRepository.save(reviewRecord);

            log.info("项目审核通过: projectId={}, adminUserId={}", projectId, adminUserId);
        } catch (Exception e) {
            log.error("审核通过项目失败: projectId={}, adminUserId={}", projectId, adminUserId, e);
            throw new RuntimeException("审核操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取状态描述
     */
    private String getStatusDescription(Integer status) {
        if (status == null) return "未知状态";

        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已发布";
            case 2 -> "已下架";
            case 3 -> "审核拒绝";
            default -> "未知状态";
        };
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailDTO getPublishedProjectDetail(Long projectId) {
        log.debug("获取已发布项目详情: projectId={}", projectId);

        try {
            // 直接查询已发布状态的项目
            Project project = projectRepository.findByIdAndStatus(projectId, 1)
                .orElse(null);

            if (project == null) {
                log.debug("项目不存在或未发布: projectId={}", projectId);
                return null;
            }

            // 转换为DTO
            ProjectDetailDTO dto = ProjectDetailDTO.fromProject(project);

            // 异步增加浏览次数（不影响查询性能）
            incrementViewCountAsync(projectId);

            return dto;
        } catch (Exception e) {
            log.error("获取已发布项目详情失败: projectId={}", projectId, e);
            return null;
        }
    }

    @Override
    @Transactional
    public void rejectProject(Long projectId, Long adminUserId, String reason) {
        log.debug("审核拒绝项目: projectId={}, adminUserId={}, reason={}", projectId, adminUserId, reason);

        // 使用悲观锁防止并发审核
        Project project = projectRepository.findByIdForUpdate(projectId)
            .orElseThrow(() -> new RuntimeException("项目不存在: " + projectId));

        Integer previousStatus = project.getStatus();

        // 检查项目状态，只有待审核的项目才能审核拒绝
        if (previousStatus != 0) {
            throw new RuntimeException("只有待审核的项目才能审核拒绝，当前状态: " +
                getStatusDescription(previousStatus));
        }

        // 验证管理员权限和拒绝理由
        if (adminUserId == null || adminUserId <= 0) {
            throw new RuntimeException("无效的管理员用户ID");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("拒绝理由不能为空");
        }

        try {
            // 审核拒绝
            project.reject();
            projectRepository.save(project);

            // 记录审核历史
            ProjectReview reviewRecord = ProjectReview.createReviewRecord(
                    projectId, adminUserId, previousStatus, 3,
                    ProjectReview.ReviewAction.REJECT, reason
            );
            projectReviewRepository.save(reviewRecord);

            log.info("项目审核拒绝: projectId={}, adminUserId={}, reason={}", projectId, adminUserId, reason);
        } catch (Exception e) {
            log.error("审核拒绝项目失败: projectId={}, adminUserId={}", projectId, adminUserId, e);
            throw new RuntimeException("审核操作失败: " + e.getMessage());
        }
    }

    @Override
    public void setAsFeatured(Long projectId, Long adminUserId) {
        log.debug("设置为精选项目: projectId={}, adminUserId={}", projectId, adminUserId);

        Project project = getById(projectId);

        // 检查项目状态，只有已发布的项目才能设为精选
        if (project.getStatus() != 1) {
            throw new RuntimeException("只有已发布的项目才能设为精选");
        }

        if (project.getIsFeatured()) {
            throw new RuntimeException("项目已经是精选项目");
        }

        project.setAsFeatured();
        projectRepository.save(project);

        // 记录审核历史
        ProjectReview reviewRecord = ProjectReview.createReviewRecord(
                projectId, adminUserId, 1, 1,
                ProjectReview.ReviewAction.FEATURE, "管理员设置为精选项目"
        );
        projectReviewRepository.save(reviewRecord);

        log.info("设置精选项目成功: projectId={}, adminUserId={}", projectId, adminUserId);
    }

    @Override
    public void unsetFeatured(Long projectId, Long adminUserId) {
        log.debug("取消精选: projectId={}, adminUserId={}", projectId, adminUserId);

        Project project = getById(projectId);

        if (!project.getIsFeatured()) {
            throw new RuntimeException("项目不是精选项目");
        }

        project.unsetFeatured();
        projectRepository.save(project);

        // 记录审核历史
        ProjectReview reviewRecord = ProjectReview.createReviewRecord(
                projectId, adminUserId, 1, 1,
                ProjectReview.ReviewAction.UNFEATURE, "管理员取消精选项目"
        );
        projectReviewRepository.save(reviewRecord);

        log.info("取消精选成功: projectId={}, adminUserId={}", projectId, adminUserId);
    }

    @Override
    public void incrementViewCount(Long projectId) {
        log.debug("增加项目浏览次数: projectId={}", projectId);

        try {
            projectRepository.incrementViewCount(projectId);
        } catch (Exception e) {
            log.warn("增加浏览次数失败: projectId={}", projectId, e);
        }
    }

    @Override
    public void incrementDownloadCount(Long projectId) {
        log.debug("增加项目下载次数: projectId={}", projectId);

        try {
            projectRepository.incrementDownloadCount(projectId);
        } catch (Exception e) {
            log.warn("增加下载次数失败: projectId={}", projectId, e);
        }
    }

    @Override
    public void incrementLikeCount(Long projectId, Long userId) {
        log.debug("增加项目点赞次数: projectId={}, userId={}", projectId, userId);

        // TODO: 检查用户是否已经点赞过
        if (isLikedByUser(projectId, userId)) {
            throw new RuntimeException("用户已经点赞过此项目");
        }

        try {
            projectRepository.incrementLikeCount(projectId);
            // TODO: 记录用户点赞记录
        } catch (Exception e) {
            log.warn("增加点赞次数失败: projectId={}, userId={}", projectId, userId, e);
            throw new RuntimeException("点赞失败");
        }

        log.info("项目点赞成功: projectId={}, userId={}", projectId, userId);
    }

    @Override
    public void decrementLikeCount(Long projectId, Long userId) {
        log.debug("减少项目点赞次数: projectId={}, userId={}", projectId, userId);

        // TODO: 检查用户是否已经点赞过
        if (!isLikedByUser(projectId, userId)) {
            throw new RuntimeException("用户尚未点赞此项目");
        }

        try {
            projectRepository.decrementLikeCount(projectId);
            // TODO: 删除用户点赞记录
        } catch (Exception e) {
            log.warn("减少点赞次数失败: projectId={}, userId={}", projectId, userId, e);
            throw new RuntimeException("取消点赞失败");
        }

        log.info("取消项目点赞成功: projectId={}, userId={}", projectId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long projectId, Long userId) {
        // TODO: 实现用户点赞状态检查
        // 需要创建用户点赞记录表或使用Redis缓存
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEditProject(Long projectId, Long userId) {
        Project project = getById(projectId);

        // 项目作者可以编辑
        if (project.getUserId().equals(userId)) {
            return true;
        }

        // TODO: 检查是否为管理员

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteProject(Long projectId, Long userId) {
        Project project = getById(projectId);

        // 项目作者可以删除（仅限未发布状态）
        if (project.getUserId().equals(userId) && !project.isPublished()) {
            return true;
        }

        // TODO: 检查是否为管理员

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canPurchaseProject(Long projectId, Long userId) {
        Project project = getById(projectId);

        // 项目必须已发布
        if (!project.isPublished()) {
            return false;
        }

        // 项目作者不能购买自己的项目
        if (project.getUserId().equals(userId)) {
            return false;
        }

        // TODO: 检查用户是否已经购买过

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectDTO> getPendingReviewProjects(Pageable pageable) {
        log.debug("获取待审核项目列表");

        Page<Project> projectPage = projectRepository.findPendingReviewProjects(pageable);
        return PageResponse.fromPage(projectPage.map(ProjectDTO::fromProject));
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingReviewProjects() {
        return projectRepository.countByStatus(0);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProjectsByStatus(Integer status) {
        return projectRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPublishedProjects() {
        return projectRepository.countPublishedProjects();
    }

    @Override
    @Transactional(readOnly = true)
    public long countFeaturedProjects() {
        return projectRepository.countFeaturedProjects();
    }

    @Override
    @Transactional(readOnly = true)
    public long countProjectsByUser(Long userId) {
        return projectRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProjectsByCategory(Long categoryId) {
        return projectRepository.countByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalDownloads() {
        Long total = projectRepository.sumTotalDownloads();
        return total != null ? total : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalDownloadsByUser(Long userId) {
        Long total = projectRepository.sumDownloadsByUserId(userId);
        return total != null ? total : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectStatistics getProjectStatistics() {
        return new ProjectStatisticsImpl();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProjectStatistics getUserProjectStatistics(Long userId) {
        return new UserProjectStatisticsImpl(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("根据创建时间范围查找项目: startTime={}, endTime={}", startTime, endTime);

        List<Project> projects = projectRepository.findByCreatedTimeBetween(startTime, endTime);
        return projects.stream()
                .map(ProjectDTO::fromProject)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getStaleProjects(LocalDateTime cutoffTime) {
        log.debug("查找长时间未更新的项目: cutoffTime={}", cutoffTime);

        List<Project> projects = projectRepository.findStaleProjects(cutoffTime);
        return projects.stream()
                .map(ProjectDTO::fromProject)
                .toList();
    }

    @Override
    public void batchUpdateProjectStatus(List<Long> projectIds, Integer status, Long adminUserId) {
        log.debug("批量更新项目状态: projectIds={}, status={}, adminUserId={}", projectIds, status, adminUserId);

        // TODO: 检查管理员权限

        for (Long projectId : projectIds) {
            try {
                Project project = getById(projectId);
                project.setStatus(status);
                projectRepository.save(project);
            } catch (Exception e) {
                log.warn("更新项目状态失败: projectId={}", projectId, e);
            }
        }

        log.info("批量更新项目状态完成: count={}, status={}, adminUserId={}", projectIds.size(), status, adminUserId);
    }

    @Override
    public void batchDeleteProjects(List<Long> projectIds, Long adminUserId) {
        log.debug("批量删除项目: projectIds={}, adminUserId={}", projectIds, adminUserId);

        // TODO: 检查管理员权限

        for (Long projectId : projectIds) {
            try {
                if (canDeleteProject(projectId, adminUserId)) {
                    projectRepository.deleteById(projectId);
                }
            } catch (Exception e) {
                log.warn("删除项目失败: projectId={}", projectId, e);
            }
        }

        log.info("批量删除项目完成: count={}, adminUserId={}", projectIds.size(), adminUserId);
    }

    /**
     * 项目统计信息实现类
     */
    private class ProjectStatisticsImpl implements ProjectStatistics {
        @Override
        public long getTotalProjects() {
            return projectRepository.countAllProjects();
        }

        @Override
        public long getPublishedProjects() {
            return projectRepository.countPublishedProjects();
        }

        @Override
        public long getPendingProjects() {
            return projectRepository.countByStatus(0);
        }

        @Override
        public long getFeaturedProjects() {
            return projectRepository.countFeaturedProjects();
        }

        @Override
        public long getTotalDownloads() {
            Long total = projectRepository.sumTotalDownloads();
            return total != null ? total : 0L;
        }

        @Override
        public long getTotalViews() {
            // TODO: 实现总浏览量统计
            return 0L;
        }

        @Override
        public long getTotalLikes() {
            // TODO: 实现总点赞量统计
            return 0L;
        }

        @Override
        public BigDecimal getAverageRating() {
            // TODO: 实现平均评分统计
            return BigDecimal.ZERO;
        }
    }

    /**
     * 用户项目统计信息实现类
     */
    private class UserProjectStatisticsImpl implements UserProjectStatistics {
        private final Long userId;

        public UserProjectStatisticsImpl(Long userId) {
            this.userId = userId;
        }

        @Override
        public long getTotalProjects() {
            return projectRepository.countByUserId(userId);
        }

        @Override
        public long getPublishedProjects() {
            // TODO: 需要在Repository中添加按用户和状态统计的方法
            return 0L;
        }

        @Override
        public long getPendingProjects() {
            // TODO: 需要在Repository中添加按用户和状态统计的方法
            return 0L;
        }

        @Override
        public long getTotalDownloads() {
            Long total = projectRepository.sumDownloadsByUserId(userId);
            return total != null ? total : 0L;
        }

        @Override
        public long getTotalViews() {
            // TODO: 实现用户项目总浏览量统计
            return 0L;
        }

        @Override
        public long getTotalLikes() {
            // TODO: 实现用户项目总点赞量统计
            return 0L;
        }

        @Override
        public BigDecimal getAverageRating() {
            // TODO: 实现用户项目平均评分统计
            return BigDecimal.ZERO;
        }
    }

    @Override
    public UserProjectStats getUserProjectStats(Long userId) {
        log.debug("获取用户项目统计信息: userId={}", userId);

        try {
            // 统计上传的项目数量
            long uploadedCount = projectRepository.countByUserId(userId);

            // 统计已发布的项目数量
            long publishedCount = projectRepository.countByUserIdAndStatus(userId, 1);

            // 统计待审核的项目数量
            long pendingCount = projectRepository.countByUserIdAndStatus(userId, 0);

            // 统计收藏的项目数量（需要FavoriteService）
            long favoritesCount = 0;
            try {
                // 这里需要注入FavoriteService，暂时设为0
                // favoritesCount = favoriteService.countUserFavorites(userId);
            } catch (Exception e) {
                log.warn("获取用户收藏数量失败: userId={}", userId, e);
            }

            // 统计购买的项目数量（需要OrderService）
            long purchasedCount = 0;
            try {
                // 这里需要注入OrderService，暂时设为0
                // purchasedCount = orderService.countUserPurchases(userId);
            } catch (Exception e) {
                log.warn("获取用户购买数量失败: userId={}", userId, e);
            }

            // 统计总收益（需要OrderService）
            BigDecimal totalEarnings = BigDecimal.ZERO;
            try {
                // 这里需要注入OrderService，暂时设为0
                // totalEarnings = orderService.getUserTotalEarnings(userId);
            } catch (Exception e) {
                log.warn("获取用户总收益失败: userId={}", userId, e);
            }

            // 统计项目总下载次数
            long totalDownloads = projectRepository.findByUserId(userId).stream()
                    .mapToLong(project -> project.getDownloadCount() != null ? project.getDownloadCount() : 0)
                    .sum();

            // 统计项目总浏览次数
            long totalViews = projectRepository.findByUserId(userId).stream()
                    .mapToLong(project -> project.getViewCount() != null ? project.getViewCount() : 0)
                    .sum();

            // 统计项目总点赞次数
            long totalLikes = projectRepository.findByUserId(userId).stream()
                    .mapToLong(project -> project.getLikeCount() != null ? project.getLikeCount() : 0)
                    .sum();

            // 计算平均评分
            BigDecimal averageRating = projectRepository.findByUserId(userId).stream()
                    .filter(project -> project.getRating() != null)
                    .map(Project::getRating)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(Math.max(1, publishedCount)), 2, BigDecimal.ROUND_HALF_UP);

            UserProjectStats stats = UserProjectStats.builder()
                    .uploadedCount(uploadedCount)
                    .purchasedCount(purchasedCount)
                    .favoritesCount(favoritesCount)
                    .totalEarnings(totalEarnings)
                    .publishedCount(publishedCount)
                    .pendingCount(pendingCount)
                    .totalDownloads(totalDownloads)
                    .totalViews(totalViews)
                    .totalLikes(totalLikes)
                    .averageRating(averageRating)
                    .build();

            log.info("获取用户项目统计成功: userId={}, stats={}", userId, stats);
            return stats;

        } catch (Exception e) {
            log.error("获取用户项目统计失败: userId={}", userId, e);
            return UserProjectStats.empty();
        }
    }
}
