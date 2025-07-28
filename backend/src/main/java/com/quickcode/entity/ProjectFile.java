package com.quickcode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目文件实体类
 * 对应数据库表：project_files
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
@Table(name = "project_files", indexes = {
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_file_type", columnList = "file_type"),
    @Index(name = "idx_upload_time", columnList = "upload_time"),
    @Index(name = "idx_file_status", columnList = "file_status")
})
public class ProjectFile extends BaseEntity {

    /**
     * 关联的项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 文件名称
     */
    @NotBlank(message = "文件名称不能为空")
    @Size(max = 255, message = "文件名称长度不能超过255个字符")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * 原始文件名
     */
    @NotBlank(message = "原始文件名不能为空")
    @Size(max = 255, message = "原始文件名长度不能超过255个字符")
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    /**
     * 文件路径
     */
    @NotBlank(message = "文件路径不能为空")
    @Size(max = 500, message = "文件路径长度不能超过500个字符")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 文件URL
     */
    @Size(max = 500, message = "文件URL长度不能超过500个字符")
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    /**
     * 文件类型
     * SOURCE: 源码文件
     * COVER: 封面图片
     * DEMO: 演示文件
     * DOCUMENT: 文档文件
     */
    @NotBlank(message = "文件类型不能为空")
    @Size(max = 20, message = "文件类型长度不能超过20个字符")
    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;

    /**
     * MIME类型
     */
    @Size(max = 100, message = "MIME类型长度不能超过100个字符")
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文件MD5哈希值
     */
    @Size(max = 32, message = "MD5哈希值长度不能超过32个字符")
    @Column(name = "file_hash", length = 32)
    private String fileHash;

    /**
     * 文件状态
     * 0: 上传中
     * 1: 上传完成
     * 2: 处理中
     * 3: 处理完成
     * 4: 处理失败
     * 5: 已删除
     */
    @Builder.Default
    @Column(name = "file_status", nullable = false)
    private Integer fileStatus = 0;

    /**
     * 上传时间
     */
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    /**
     * 处理时间
     */
    @Column(name = "process_time")
    private LocalDateTime processTime;

    /**
     * 是否为主文件
     */
    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    /**
     * 文件描述
     */
    @Size(max = 500, message = "文件描述长度不能超过500个字符")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 关联的项目实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    /**
     * 文件类型枚举
     */
    public enum FileType {
        SOURCE("SOURCE", "源码文件"),
        COVER("COVER", "封面图片"),
        DEMO("DEMO", "演示文件"),
        DOCUMENT("DOCUMENT", "文档文件");

        private final String code;
        private final String description;

        FileType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static FileType fromCode(String code) {
            for (FileType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("未知的文件类型代码: " + code);
        }
    }

    /**
     * 文件状态枚举
     */
    public enum FileStatus {
        UPLOADING(0, "上传中"),
        UPLOADED(1, "上传完成"),
        PROCESSING(2, "处理中"),
        PROCESSED(3, "处理完成"),
        FAILED(4, "处理失败"),
        DELETED(5, "已删除");

        private final Integer code;
        private final String description;

        FileStatus(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static FileStatus fromCode(Integer code) {
            for (FileStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("未知的文件状态代码: " + code);
        }
    }

    /**
     * 标记文件上传完成
     */
    public void markUploaded() {
        this.fileStatus = FileStatus.UPLOADED.getCode();
        this.uploadTime = LocalDateTime.now();
    }

    /**
     * 标记文件处理中
     */
    public void markProcessing() {
        this.fileStatus = FileStatus.PROCESSING.getCode();
    }

    /**
     * 标记文件处理完成
     */
    public void markProcessed() {
        this.fileStatus = FileStatus.PROCESSED.getCode();
        this.processTime = LocalDateTime.now();
    }

    /**
     * 标记文件处理失败
     */
    public void markFailed() {
        this.fileStatus = FileStatus.FAILED.getCode();
        this.processTime = LocalDateTime.now();
    }

    /**
     * 标记文件已删除
     * 重写父类方法，同时设置文件状态
     */
    @Override
    public void markDeleted() {
        super.markDeleted(); // 调用父类方法设置逻辑删除标记
        this.fileStatus = FileStatus.DELETED.getCode(); // 设置文件状态
    }

    /**
     * 检查文件是否已上传完成
     */
    public boolean isUploaded() {
        return FileStatus.UPLOADED.getCode().equals(this.fileStatus) ||
               FileStatus.PROCESSING.getCode().equals(this.fileStatus) ||
               FileStatus.PROCESSED.getCode().equals(this.fileStatus);
    }

    /**
     * 检查文件是否可用（未删除且已上传）
     * 重写父类方法，添加文件特定的逻辑
     */
    @Override
    public boolean isAvailable() {
        return super.isAvailable() && isUploaded();
    }

    /**
     * 检查文件是否处理完成
     */
    public boolean isProcessed() {
        return FileStatus.PROCESSED.getCode().equals(this.fileStatus);
    }

    /**
     * 检查文件是否为源码文件
     */
    public boolean isSourceFile() {
        return FileType.SOURCE.getCode().equals(this.fileType);
    }

    /**
     * 检查文件是否为封面图片
     */
    public boolean isCoverImage() {
        return FileType.COVER.getCode().equals(this.fileType);
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
