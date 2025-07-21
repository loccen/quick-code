package com.quickcode.dto;

import com.quickcode.service.ProjectDownloadService.DownloadStatistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 项目下载统计响应DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDownloadStatisticsResponse {

    /**
     * 总下载次数
     */
    private Long totalDownloads;

    /**
     * 独立下载用户数
     */
    private Long uniqueDownloaders;

    /**
     * 总下载大小（字节）
     */
    private Long totalSize;

    /**
     * 总下载大小（可读格式）
     */
    private String readableTotalSize;

    /**
     * 平均下载耗时（毫秒）
     */
    private Double averageDuration;

    /**
     * 平均下载耗时（可读格式）
     */
    private String readableAverageDuration;

    /**
     * 按来源统计下载次数
     */
    private Map<String, Long> downloadsBySource;

    /**
     * 按日期统计下载次数
     */
    private Map<String, Long> downloadsByDate;

    /**
     * 从DownloadStatistics转换
     */
    public static ProjectDownloadStatisticsResponse fromDownloadStatistics(DownloadStatistics statistics) {
        if (statistics == null) {
            return null;
        }

        return ProjectDownloadStatisticsResponse.builder()
                .totalDownloads(statistics.getTotalDownloads())
                .uniqueDownloaders(statistics.getUniqueDownloaders())
                .totalSize(statistics.getTotalSize())
                .readableTotalSize(formatFileSize(statistics.getTotalSize()))
                .averageDuration(statistics.getAverageDuration())
                .readableAverageDuration(formatDuration(statistics.getAverageDuration()))
                .downloadsBySource(statistics.getDownloadsBySource())
                .downloadsByDate(statistics.getDownloadsByDate())
                .build();
    }

    /**
     * 格式化文件大小
     */
    private static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 格式化耗时
     */
    private static String formatDuration(Double duration) {
        if (duration == null) {
            return "未知";
        }
        
        long durationMs = duration.longValue();
        if (durationMs < 1000) {
            return durationMs + " ms";
        } else if (durationMs < 60000) {
            return String.format("%.1f s", durationMs / 1000.0);
        } else {
            return String.format("%.1f min", durationMs / 60000.0);
        }
    }
}
