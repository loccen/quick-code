package com.quickcode.dto;

import com.quickcode.entity.ProjectFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目文件上传响应DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFileUploadResponse {

    /**
     * 文件ID
     */
    private Long fileId;

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
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件状态
     */
    private Integer fileStatus;

    /**
     * 文件状态描述
     */
    private String fileStatusDesc;

    /**
     * 是否为主文件
     */
    private Boolean isPrimary;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 从ProjectFile实体转换
     */
    public static ProjectFileUploadResponse fromProjectFile(ProjectFile projectFile) {
        if (projectFile == null) {
            return null;
        }

        return ProjectFileUploadResponse.builder()
                .fileId(projectFile.getId())
                .projectId(projectFile.getProjectId())
                .fileName(projectFile.getFileName())
                .originalName(projectFile.getOriginalName())
                .fileUrl(projectFile.getFileUrl())
                .fileType(projectFile.getFileType())
                .fileSize(projectFile.getFileSize())
                .fileStatus(projectFile.getFileStatus())
                .fileStatusDesc(getFileStatusDescription(projectFile.getFileStatus()))
                .isPrimary(projectFile.getIsPrimary())
                .uploadTime(projectFile.getUploadTime())
                .description(projectFile.getDescription())
                .build();
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
}
