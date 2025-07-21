package com.quickcode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目下载记录实体类
 * 对应数据库表：project_downloads
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "project_downloads", indexes = {
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_download_time", columnList = "download_time"),
    @Index(name = "idx_download_status", columnList = "download_status"),
    @Index(name = "idx_user_project", columnList = "user_id,project_id")
})
public class ProjectDownload extends BaseEntity {

    /**
     * 关联的项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 下载用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 下载的文件ID
     */
    @Column(name = "file_id")
    private Long fileId;

    /**
     * 下载时间
     */
    @Column(name = "download_time")
    private LocalDateTime downloadTime;

    /**
     * 下载状态
     * 0: 下载中
     * 1: 下载完成
     * 2: 下载失败
     * 3: 下载取消
     */
    @Builder.Default
    @Column(name = "download_status", nullable = false)
    private Integer downloadStatus = 0;

    /**
     * 下载IP地址
     */
    @Size(max = 45, message = "IP地址长度不能超过45个字符")
    @Column(name = "download_ip", length = 45)
    private String downloadIp;

    /**
     * 用户代理
     */
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 下载来源
     * WEB: 网页下载
     * API: API下载
     * MOBILE: 移动端下载
     */
    @Size(max = 20, message = "下载来源长度不能超过20个字符")
    @Column(name = "download_source", length = 20)
    private String downloadSource;

    /**
     * 下载文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 下载耗时（毫秒）
     */
    @Column(name = "download_duration")
    private Long downloadDuration;

    /**
     * 是否为重复下载
     */
    @Builder.Default
    @Column(name = "is_repeat", nullable = false)
    private Boolean isRepeat = false;

    /**
     * 下载备注
     */
    @Size(max = 500, message = "下载备注长度不能超过500个字符")
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 关联的项目实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    /**
     * 关联的用户实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 关联的文件实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", insertable = false, updatable = false)
    private ProjectFile projectFile;

    /**
     * 下载状态枚举
     */
    public enum DownloadStatus {
        DOWNLOADING(0, "下载中"),
        COMPLETED(1, "下载完成"),
        FAILED(2, "下载失败"),
        CANCELLED(3, "下载取消");

        private final Integer code;
        private final String description;

        DownloadStatus(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static DownloadStatus fromCode(Integer code) {
            for (DownloadStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("未知的下载状态代码: " + code);
        }
    }

    /**
     * 下载来源枚举
     */
    public enum DownloadSource {
        WEB("WEB", "网页下载"),
        API("API", "API下载"),
        MOBILE("MOBILE", "移动端下载");

        private final String code;
        private final String description;

        DownloadSource(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static DownloadSource fromCode(String code) {
            for (DownloadSource source : values()) {
                if (source.code.equals(code)) {
                    return source;
                }
            }
            throw new IllegalArgumentException("未知的下载来源代码: " + code);
        }
    }

    /**
     * 开始下载
     */
    public void startDownload() {
        this.downloadStatus = DownloadStatus.DOWNLOADING.getCode();
        this.downloadTime = LocalDateTime.now();
    }

    /**
     * 完成下载
     */
    public void completeDownload(Long duration) {
        this.downloadStatus = DownloadStatus.COMPLETED.getCode();
        this.downloadDuration = duration;
    }

    /**
     * 下载失败
     */
    public void failDownload() {
        this.downloadStatus = DownloadStatus.FAILED.getCode();
    }

    /**
     * 取消下载
     */
    public void cancelDownload() {
        this.downloadStatus = DownloadStatus.CANCELLED.getCode();
    }

    /**
     * 检查是否下载完成
     */
    public boolean isCompleted() {
        return DownloadStatus.COMPLETED.getCode().equals(this.downloadStatus);
    }

    /**
     * 检查是否下载失败
     */
    public boolean isFailed() {
        return DownloadStatus.FAILED.getCode().equals(this.downloadStatus);
    }

    /**
     * 获取下载耗时的可读格式
     */
    public String getReadableDuration() {
        if (downloadDuration == null) {
            return "未知";
        }
        
        long duration = downloadDuration;
        if (duration < 1000) {
            return duration + " ms";
        } else if (duration < 60000) {
            return String.format("%.1f s", duration / 1000.0);
        } else {
            return String.format("%.1f min", duration / 60000.0);
        }
    }

    /**
     * 获取文件大小的可读格式
     */
    public String getReadableFileSize() {
        if (fileSize == null) {
            return "未知";
        }
        
        long size = fileSize;
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
}
