package com.quickcode.service.impl;

import com.quickcode.dto.common.PageResponse;
import com.quickcode.dto.project.ProjectDTO;
import com.quickcode.entity.Project;
import com.quickcode.entity.UserFavorite;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.repository.UserFavoriteRepository;
import com.quickcode.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏服务实现类
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserFavoriteRepository userFavoriteRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public UserFavorite favoriteProject(Long userId, Long projectId) {
        log.debug("用户收藏项目: userId={}, projectId={}", userId, projectId);

        // 检查项目是否存在
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));

        // 检查项目状态，只有已发布的项目才能收藏
        if (project.getStatus() != 1) {
            throw new RuntimeException("只能收藏已发布的项目");
        }

        // 检查是否已收藏
        if (userFavoriteRepository.existsByUserIdAndProjectId(userId, projectId)) {
            throw new RuntimeException("已收藏该项目");
        }

        // 创建收藏记录
        UserFavorite favorite = UserFavorite.create(userId, projectId);
        UserFavorite savedFavorite = userFavoriteRepository.save(favorite);

        // 更新项目收藏数量
        updateProjectFavoriteCount(projectId);

        log.info("用户收藏项目成功: userId={}, projectId={}, favoriteId={}", 
                userId, projectId, savedFavorite.getId());

        return savedFavorite;
    }

    @Override
    @Transactional
    public void unfavoriteProject(Long userId, Long projectId) {
        log.debug("用户取消收藏项目: userId={}, projectId={}", userId, projectId);

        // 查找收藏记录
        UserFavorite favorite = userFavoriteRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new RuntimeException("未收藏该项目"));

        // 删除收藏记录
        userFavoriteRepository.delete(favorite);

        // 更新项目收藏数量
        updateProjectFavoriteCount(projectId);

        log.info("用户取消收藏项目成功: userId={}, projectId={}", userId, projectId);
    }

    @Override
    public boolean isFavorited(Long userId, Long projectId) {
        return userFavoriteRepository.existsByUserIdAndProjectId(userId, projectId);
    }

    @Override
    public PageResponse<ProjectDTO> getUserFavoriteProjects(Long userId, Pageable pageable) {
        return getUserFavoriteProjects(userId, null, pageable);
    }

    @Override
    public PageResponse<ProjectDTO> getUserFavoriteProjects(Long userId, String keyword, Pageable pageable) {
        log.debug("获取用户收藏项目列表: userId={}, keyword={}, page={}, size={}", 
                userId, keyword, pageable.getPageNumber(), pageable.getPageSize());

        Page<UserFavorite> favoritePage = userFavoriteRepository.findByUserIdOrderByCreatedTimeDesc(userId, pageable);
        
        // 转换为ProjectDTO
        Page<ProjectDTO> projectPage = favoritePage.map(favorite -> {
            Project project = favorite.getProject();
            if (project != null) {
                ProjectDTO dto = ProjectDTO.fromProject(project);
                // 设置收藏时间
                dto.setFavoriteTime(favorite.getCreatedTime());
                return dto;
            }
            return null;
        }).map(dto -> dto); // 过滤null值

        // 如果有搜索关键词，进行过滤
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<ProjectDTO> filteredContent = projectPage.getContent().stream()
                    .filter(dto -> dto != null &&
                           (dto.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                            (dto.getDescription() != null && dto.getDescription().toLowerCase().contains(keyword.toLowerCase()))))
                    .collect(Collectors.toList());

            // 创建自定义的PageResponse
            return PageResponse.<ProjectDTO>builder()
                    .content(filteredContent)
                    .page(projectPage.getNumber())
                    .size(projectPage.getSize())
                    .totalPages(projectPage.getTotalPages())
                    .totalElements(projectPage.getTotalElements())
                    .first(projectPage.isFirst())
                    .last(projectPage.isLast())
                    .hasPrevious(projectPage.hasPrevious())
                    .hasNext(projectPage.hasNext())
                    .numberOfElements(filteredContent.size())
                    .empty(filteredContent.isEmpty())
                    .build();
        }

        return PageResponse.fromPage(projectPage);
    }

    @Override
    public long countUserFavorites(Long userId) {
        return userFavoriteRepository.countByUserId(userId);
    }

    @Override
    public long countProjectFavorites(Long projectId) {
        return userFavoriteRepository.countByProjectId(projectId);
    }

    @Override
    public List<ProjectDTO> getUserRecentFavorites(Long userId, int limit) {
        log.debug("获取用户最近收藏项目: userId={}, limit={}", userId, limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<UserFavorite> recentFavorites = userFavoriteRepository.findRecentFavoritesByUserId(userId, pageable);

        return recentFavorites.stream()
                .map(favorite -> {
                    Project project = favorite.getProject();
                    if (project != null) {
                        ProjectDTO dto = ProjectDTO.fromProject(project);
                        dto.setFavoriteTime(favorite.getCreatedTime());
                        return dto;
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> getPopularFavoriteProjects(int limit) {
        log.debug("获取热门收藏项目: limit={}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> popularProjects = userFavoriteRepository.findPopularFavoriteProjects(pageable);

        return popularProjects.stream()
                .map(result -> {
                    Long projectId = (Long) result[0];
                    Long favoriteCount = (Long) result[1];

                    return projectRepository.findById(projectId)
                            .map(project -> {
                                ProjectDTO dto = ProjectDTO.fromProject(project);
                                dto.setFavoriteCount(favoriteCount.intValue());
                                return dto;
                            })
                            .orElse(null);
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, Boolean> batchCheckFavoriteStatus(Long userId, List<Long> projectIds) {
        log.debug("批量检查收藏状态: userId={}, projectIds={}", userId, projectIds);

        List<UserFavorite> favorites = userFavoriteRepository.findByUserIdAndProjectIdIn(userId, projectIds);

        Map<Long, Boolean> favoriteStatusMap = projectIds.stream()
                .collect(Collectors.toMap(id -> id, id -> false));

        favorites.forEach(favorite ->
                favoriteStatusMap.put(favorite.getProjectId(), true));

        return favoriteStatusMap;
    }

    @Override
    @Transactional
    public int deleteUserAllFavorites(Long userId) {
        log.debug("删除用户所有收藏: userId={}", userId);

        int deletedCount = userFavoriteRepository.deleteByUserId(userId);

        log.info("删除用户所有收藏成功: userId={}, deletedCount={}", userId, deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional
    public int deleteProjectAllFavorites(Long projectId) {
        log.debug("删除项目所有收藏: projectId={}", projectId);

        int deletedCount = userFavoriteRepository.deleteByProjectId(projectId);

        // 更新项目收藏数量
        updateProjectFavoriteCount(projectId);

        log.info("删除项目所有收藏成功: projectId={}, deletedCount={}", projectId, deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional
    public void updateProjectFavoriteCount(Long projectId) {
        log.debug("更新项目收藏数量: projectId={}", projectId);

        long favoriteCount = userFavoriteRepository.countByProjectId(projectId);

        projectRepository.findById(projectId).ifPresent(project -> {
            project.setFavoriteCount((int) favoriteCount);
            projectRepository.save(project);
            log.debug("项目收藏数量更新成功: projectId={}, favoriteCount={}", projectId, favoriteCount);
        });
    }
}
