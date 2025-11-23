package com.quickcode.dto;

import com.quickcode.entity.ProjectDownload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目下载历史响应DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDownloadHistoryResponse {

    /**
     * 下载记录ID
     */
    private Long downloadId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目标题
     */
    private String projectTitle;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 下载时间
     */
    private LocalDateTime downloadTime;

    /**
     * 下载状态
     */
    private Integer downloadStatus;

    /**
     * 下载状态描述
     */
    private String downloadStatusDesc;

    /**
     * 下载来源
     */
    private String downloadSource;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件大小（可读格式）
     */
    private String readableFileSize;

    /**
     * 下载耗时（毫秒）
     */
    private Long downloadDuration;

    /**
     * 下载耗时（可读格式）
     */
    private String readableDuration;

    /**
     * 是否为重复下载
     */
    private Boolean isRepeat;

    /**
     * 项目下载次数（仅在按下载次数排序时使用）
     */
    private Integer projectDownloadCount;

    /**
     * 从ProjectDownload实体转换
     */
    public static ProjectDownloadHistoryResponse fromProjectDownload(ProjectDownload download) {
        if (download == null) {
            return null;
        }

        return ProjectDownloadHistoryResponse.builder()
                .downloadId(download.getId())
                .projectId(download.getProjectId())
                .projectTitle(getProjectTitle(download)) // 需要关联查询项目信息
                .fileId(download.getFileId())
                .fileName(getFileName(download)) // 需要关联查询文件信息
                .downloadTime(download.getDownloadTime())
                .downloadStatus(download.getDownloadStatus())
                .downloadStatusDesc(getDownloadStatusDescription(download.getDownloadStatus()))
                .downloadSource(download.getDownloadSource())
                .fileSize(download.getFileSize())
                .readableFileSize(download.getReadableFileSize())
                .downloadDuration(download.getDownloadDuration())
                .readableDuration(download.getReadableDuration())
                .isRepeat(download.getIsRepeat())
                .build();
    }

    /**
     * 获取项目标题
     */
    private static String getProjectTitle(ProjectDownload download) {
        // 暂时返回默认值，后续可以通过服务层注入项目信息
        return "项目-" + download.getProjectId();
    }

    /**
     * 获取文件名
     */
    private static String getFileName(ProjectDownload download) {
        // 暂时返回默认值，后续可以通过服务层注入文件信息
        return "文件-" + download.getFileId();
    }

    /**
     * 获取下载状态描述
     */
    private static String getDownloadStatusDescription(Integer status) {
        if (status == null) {
            return "未知";
        }

        try {
            ProjectDownload.DownloadStatus downloadStatus = ProjectDownload.DownloadStatus.fromCode(status);
            return downloadStatus.getDescription();
        } catch (IllegalArgumentException e) {
            return "未知状态";
        }
    }
}
