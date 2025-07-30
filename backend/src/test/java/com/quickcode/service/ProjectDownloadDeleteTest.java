package com.quickcode.service;

import com.quickcode.entity.ProjectDownload;
import com.quickcode.repository.ProjectDownloadRepository;
import com.quickcode.service.impl.ProjectDownloadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 下载记录删除功能测试类
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ProjectDownloadDeleteTest {

    @Mock
    private ProjectDownloadRepository projectDownloadRepository;

    @InjectMocks
    private ProjectDownloadServiceImpl projectDownloadService;

    private ProjectDownload testDownloadRecord;
    private Long userId = 1L;
    private Long recordId = 1L;
    private Long otherUserId = 2L;

    @BeforeEach
    void setUp() {
        testDownloadRecord = new ProjectDownload();
        testDownloadRecord.setId(recordId);
        testDownloadRecord.setUserId(userId);
        testDownloadRecord.setProjectId(1L);
        testDownloadRecord.setDownloadTime(LocalDateTime.now());
        testDownloadRecord.setDownloadStatus(1);
    }

    @Test
    void testDeleteDownloadRecord_Success() {
        // Given
        when(projectDownloadRepository.findById(recordId)).thenReturn(Optional.of(testDownloadRecord));

        // When
        projectDownloadService.deleteDownloadRecord(recordId, userId);

        // Then
        verify(projectDownloadRepository).delete(testDownloadRecord);
    }

    @Test
    void testDeleteDownloadRecord_RecordNotFound() {
        // Given
        when(projectDownloadRepository.findById(recordId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> projectDownloadService.deleteDownloadRecord(recordId, userId));
        assertEquals("下载记录不存在", exception.getMessage());
    }

    @Test
    void testDeleteDownloadRecord_NoPermission() {
        // Given
        when(projectDownloadRepository.findById(recordId)).thenReturn(Optional.of(testDownloadRecord));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> projectDownloadService.deleteDownloadRecord(recordId, otherUserId));
        assertEquals("无权限删除此下载记录", exception.getMessage());
    }

    @Test
    void testBatchDeleteDownloadRecords_Success() {
        // Given
        List<Long> recordIds = Arrays.asList(1L, 2L, 3L);
        
        ProjectDownload record1 = new ProjectDownload();
        record1.setId(1L);
        record1.setUserId(userId);
        
        ProjectDownload record2 = new ProjectDownload();
        record2.setId(2L);
        record2.setUserId(userId);
        
        ProjectDownload record3 = new ProjectDownload();
        record3.setId(3L);
        record3.setUserId(userId);
        
        List<ProjectDownload> userRecords = Arrays.asList(record1, record2, record3);
        
        when(projectDownloadRepository.findByIdInAndUserId(recordIds, userId))
                .thenReturn(userRecords);

        // When
        int deletedCount = projectDownloadService.batchDeleteDownloadRecords(recordIds, userId);

        // Then
        assertEquals(3, deletedCount);
        verify(projectDownloadRepository).deleteAll(userRecords);
    }

    @Test
    void testBatchDeleteDownloadRecords_EmptyList() {
        // Given
        List<Long> recordIds = Arrays.asList();

        // When
        int deletedCount = projectDownloadService.batchDeleteDownloadRecords(recordIds, userId);

        // Then
        assertEquals(0, deletedCount);
        verify(projectDownloadRepository, never()).deleteAll(any());
    }

    @Test
    void testBatchDeleteDownloadRecords_NoValidRecords() {
        // Given
        List<Long> recordIds = Arrays.asList(1L, 2L, 3L);
        when(projectDownloadRepository.findByIdInAndUserId(recordIds, userId))
                .thenReturn(Arrays.asList());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> projectDownloadService.batchDeleteDownloadRecords(recordIds, userId));
        assertEquals("未找到可删除的下载记录", exception.getMessage());
    }

    @Test
    void testClearUserDownloadRecords_Success() {
        // Given
        when(projectDownloadRepository.countByUserId(userId)).thenReturn(5L);
        when(projectDownloadRepository.deleteByUserId(userId)).thenReturn(5);

        // When
        int deletedCount = projectDownloadService.clearUserDownloadRecords(userId);

        // Then
        assertEquals(5, deletedCount);
        verify(projectDownloadRepository).deleteByUserId(userId);
    }

    @Test
    void testClearUserDownloadRecords_NoRecords() {
        // Given
        when(projectDownloadRepository.countByUserId(userId)).thenReturn(0L);

        // When
        int deletedCount = projectDownloadService.clearUserDownloadRecords(userId);

        // Then
        assertEquals(0, deletedCount);
        verify(projectDownloadRepository, never()).deleteByUserId(any());
    }

    @Test
    void testBatchDeleteDownloadRecords_PartialPermission() {
        // Given
        List<Long> recordIds = Arrays.asList(1L, 2L, 3L);
        
        // 只有前两个记录属于当前用户
        ProjectDownload record1 = new ProjectDownload();
        record1.setId(1L);
        record1.setUserId(userId);
        
        ProjectDownload record2 = new ProjectDownload();
        record2.setId(2L);
        record2.setUserId(userId);
        
        List<ProjectDownload> userRecords = Arrays.asList(record1, record2);
        
        when(projectDownloadRepository.findByIdInAndUserId(recordIds, userId))
                .thenReturn(userRecords);

        // When
        int deletedCount = projectDownloadService.batchDeleteDownloadRecords(recordIds, userId);

        // Then
        assertEquals(2, deletedCount); // 只删除了属于用户的记录
        verify(projectDownloadRepository).deleteAll(userRecords);
    }
}
