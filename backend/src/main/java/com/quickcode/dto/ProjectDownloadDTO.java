package com.quickcode.dto;

import com.quickcode.entity.ProjectDownload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目下载DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDownloadDTO {

    /**
     * 下载记录ID
     */
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目标题
     */
    private String projectTitle;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

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
     * 下载IP地址
     */
    private String downloadIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 下载来源
     */
    private String downloadSource;

    /**
     * 下载来源描述
     */
    private String downloadSourceDesc;

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
     * 下载备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 从ProjectDownload实体转换
     */
    public static ProjectDownloadDTO fromProjectDownload(ProjectDownload download) {
        if (download == null) {
            return null;
        }

        return ProjectDownloadDTO.builder()
                .id(download.getId())
                .projectId(download.getProjectId())
                .projectTitle(getProjectTitle(download))
                .userId(download.getUserId())
                .username(getUsername(download))
                .fileId(download.getFileId())
                .fileName(getFileName(download))
                .downloadTime(download.getDownloadTime())
                .downloadStatus(download.getDownloadStatus())
                .downloadStatusDesc(getDownloadStatusDescription(download.getDownloadStatus()))
                .downloadIp(download.getDownloadIp())
                .userAgent(download.getUserAgent())
                .downloadSource(download.getDownloadSource())
                .downloadSourceDesc(getDownloadSourceDescription(download.getDownloadSource()))
                .fileSize(download.getFileSize())
                .readableFileSize(download.getReadableFileSize())
                .downloadDuration(download.getDownloadDuration())
                .readableDuration(download.getReadableDuration())
                .isRepeat(download.getIsRepeat())
                .remark(download.getRemark())
                .createdTime(download.getCreatedTime())
                .updatedTime(download.getUpdatedTime())
                .build();
    }

    /**
     * 获取项目标题
     */
    private static String getProjectTitle(ProjectDownload download) {
        if (download.getProject() != null) {
            return download.getProject().getTitle();
        }
        return "项目-" + download.getProjectId();
    }

    /**
     * 获取用户名
     */
    private static String getUsername(ProjectDownload download) {
        if (download.getUser() != null) {
            return download.getUser().getUsername();
        }
        return "用户-" + download.getUserId();
    }

    /**
     * 获取文件名
     */
    private static String getFileName(ProjectDownload download) {
        if (download.getProjectFile() != null) {
            return download.getProjectFile().getOriginalName();
        }
        return download.getFileId() != null ? "文件-" + download.getFileId() : "项目主文件";
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

    /**
     * 获取下载来源描述
     */
    private static String getDownloadSourceDescription(String source) {
        if (source == null) {
            return "未知";
        }

        try {
            ProjectDownload.DownloadSource downloadSource = ProjectDownload.DownloadSource.fromCode(source);
            return downloadSource.getDescription();
        } catch (IllegalArgumentException e) {
            return "未知来源";
        }
    }

    /**
     * 检查是否下载完成
     */
    public boolean isCompleted() {
        return downloadStatus != null && downloadStatus.equals(1);
    }

    /**
     * 检查是否下载失败
     */
    public boolean isFailed() {
        return downloadStatus != null && downloadStatus.equals(2);
    }

    /**
     * 检查是否正在下载
     */
    public boolean isDownloading() {
        return downloadStatus != null && downloadStatus.equals(0);
    }

    /**
     * 检查是否已取消
     */
    public boolean isCancelled() {
        return downloadStatus != null && downloadStatus.equals(3);
    }

    /**
     * 检查是否为网页下载
     */
    public boolean isWebDownload() {
        return "WEB".equals(downloadSource);
    }

    /**
     * 检查是否为API下载
     */
    public boolean isApiDownload() {
        return "API".equals(downloadSource);
    }

    /**
     * 检查是否为移动端下载
     */
    public boolean isMobileDownload() {
        return "MOBILE".equals(downloadSource);
    }
}
