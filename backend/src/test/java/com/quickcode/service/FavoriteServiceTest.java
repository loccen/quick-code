package com.quickcode.service;

import com.quickcode.dto.common.PageResponse;
import com.quickcode.dto.project.ProjectDTO;
import com.quickcode.entity.Project;
import com.quickcode.entity.UserFavorite;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.repository.UserFavoriteRepository;
import com.quickcode.service.impl.FavoriteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 收藏服务测试类
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private UserFavoriteRepository userFavoriteRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private Project testProject;
    private UserFavorite testFavorite;
    private Long userId = 1L;
    private Long projectId = 1L;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setTitle("测试项目");
        testProject.setDescription("测试项目描述");
        testProject.setStatus(1); // 已发布
        testProject.setPrice(BigDecimal.valueOf(100));
        testProject.setFavoriteCount(0);

        testFavorite = UserFavorite.builder()
                .id(1L)
                .userId(userId)
                .projectId(projectId)
                .createdTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testFavoriteProject_Success() {
        // Given
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userFavoriteRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(false);
        when(userFavoriteRepository.save(any(UserFavorite.class))).thenReturn(testFavorite);
        when(userFavoriteRepository.countByProjectId(projectId)).thenReturn(1L);

        // When
        UserFavorite result = favoriteService.favoriteProject(userId, projectId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(projectId, result.getProjectId());
        verify(userFavoriteRepository).save(any(UserFavorite.class));
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void testFavoriteProject_ProjectNotFound() {
        // Given
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> favoriteService.favoriteProject(userId, projectId));
        assertEquals("项目不存在", exception.getMessage());
    }

    @Test
    void testFavoriteProject_ProjectNotPublished() {
        // Given
        testProject.setStatus(0); // 待审核状态
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> favoriteService.favoriteProject(userId, projectId));
        assertEquals("只能收藏已发布的项目", exception.getMessage());
    }

    @Test
    void testFavoriteProject_AlreadyFavorited() {
        // Given
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userFavoriteRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> favoriteService.favoriteProject(userId, projectId));
        assertEquals("已收藏该项目", exception.getMessage());
    }

    @Test
    void testUnfavoriteProject_Success() {
        // Given
        when(userFavoriteRepository.findByUserIdAndProjectId(userId, projectId))
                .thenReturn(Optional.of(testFavorite));
        when(userFavoriteRepository.countByProjectId(projectId)).thenReturn(0L);

        // When
        favoriteService.unfavoriteProject(userId, projectId);

        // Then
        verify(userFavoriteRepository).delete(testFavorite);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void testUnfavoriteProject_NotFavorited() {
        // Given
        when(userFavoriteRepository.findByUserIdAndProjectId(userId, projectId))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> favoriteService.unfavoriteProject(userId, projectId));
        assertEquals("未收藏该项目", exception.getMessage());
    }

    @Test
    void testIsFavorited() {
        // Given
        when(userFavoriteRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(true);

        // When
        boolean result = favoriteService.isFavorited(userId, projectId);

        // Then
        assertTrue(result);
    }

    @Test
    void testGetUserFavoriteProjects() {
        // Given
        testFavorite.setProject(testProject);
        Page<UserFavorite> favoritePage = new PageImpl<>(List.of(testFavorite));
        when(userFavoriteRepository.findByUserIdOrderByCreatedTimeDesc(eq(userId), any(Pageable.class)))
                .thenReturn(favoritePage);

        // When
        Pageable pageable = PageRequest.of(0, 10);
        PageResponse<ProjectDTO> result = favoriteService.getUserFavoriteProjects(userId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("测试项目", result.getContent().get(0).getTitle());
    }

    @Test
    void testCountUserFavorites() {
        // Given
        when(userFavoriteRepository.countByUserId(userId)).thenReturn(5L);

        // When
        long result = favoriteService.countUserFavorites(userId);

        // Then
        assertEquals(5L, result);
    }

    @Test
    void testCountProjectFavorites() {
        // Given
        when(userFavoriteRepository.countByProjectId(projectId)).thenReturn(10L);

        // When
        long result = favoriteService.countProjectFavorites(projectId);

        // Then
        assertEquals(10L, result);
    }
}
