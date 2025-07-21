package com.quickcode.dto;

import com.quickcode.entity.ProjectFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目文件DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFileDTO {

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件类型描述
     */
    private String fileTypeDesc;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件大小（可读格式）
     */
    private String readableFileSize;

    /**
     * 文件哈希值
     */
    private String fileHash;

    /**
     * 文件状态
     */
    private Integer fileStatus;

    /**
     * 文件状态描述
     */
    private String fileStatusDesc;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 是否为主文件
     */
    private Boolean isPrimary;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 从ProjectFile实体转换
     */
    public static ProjectFileDTO fromProjectFile(ProjectFile projectFile) {
        if (projectFile == null) {
            return null;
        }

        return ProjectFileDTO.builder()
                .id(projectFile.getId())
                .projectId(projectFile.getProjectId())
                .fileName(projectFile.getFileName())
                .originalName(projectFile.getOriginalName())
                .filePath(projectFile.getFilePath())
                .fileUrl(projectFile.getFileUrl())
                .fileType(projectFile.getFileType())
                .fileTypeDesc(getFileTypeDescription(projectFile.getFileType()))
                .mimeType(projectFile.getMimeType())
                .fileSize(projectFile.getFileSize())
                .readableFileSize(projectFile.getReadableFileSize())
                .fileHash(projectFile.getFileHash())
                .fileStatus(projectFile.getFileStatus())
                .fileStatusDesc(getFileStatusDescription(projectFile.getFileStatus()))
                .uploadTime(projectFile.getUploadTime())
                .processTime(projectFile.getProcessTime())
                .isPrimary(projectFile.getIsPrimary())
                .description(projectFile.getDescription())
                .createdTime(projectFile.getCreatedTime())
                .updatedTime(projectFile.getUpdatedTime())
                .build();
    }

    /**
     * 获取文件类型描述
     */
    private static String getFileTypeDescription(String fileType) {
        if (fileType == null) {
            return "未知";
        }

        try {
            ProjectFile.FileType type = ProjectFile.FileType.fromCode(fileType);
            return type.getDescription();
        } catch (IllegalArgumentException e) {
            return "未知类型";
        }
    }

    /**
     * 获取文件状态描述
     */
    private static String getFileStatusDescription(Integer status) {
        if (status == null) {
            return "未知";
        }

        try {
            ProjectFile.FileStatus fileStatus = ProjectFile.FileStatus.fromCode(status);
            return fileStatus.getDescription();
        } catch (IllegalArgumentException e) {
            return "未知状态";
        }
    }

    /**
     * 检查文件是否已上传完成
     */
    public boolean isUploaded() {
        return fileStatus != null && fileStatus >= 1;
    }

    /**
     * 检查文件是否处理完成
     */
    public boolean isProcessed() {
        return fileStatus != null && fileStatus.equals(3);
    }

    /**
     * 检查文件是否为源码文件
     */
    public boolean isSourceFile() {
        return "SOURCE".equals(fileType);
    }

    /**
     * 检查文件是否为封面图片
     */
    public boolean isCoverImage() {
        return "COVER".equals(fileType);
    }

    /**
     * 检查文件是否为演示文件
     */
    public boolean isDemoFile() {
        return "DEMO".equals(fileType);
    }

    /**
     * 检查文件是否为文档文件
     */
    public boolean isDocumentFile() {
        return "DOCUMENT".equals(fileType);
    }
}
