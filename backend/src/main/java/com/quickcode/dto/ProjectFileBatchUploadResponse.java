package com.quickcode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 项目文件批量上传响应DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFileBatchUploadResponse {

    /**
     * 总文件数
     */
    private Integer totalFiles;

    /**
     * 成功上传数
     */
    private Integer successCount;

    /**
     * 失败上传数
     */
    private Integer failureCount;

    /**
     * 成功上传的文件列表
     */
    private List<ProjectFileUploadResponse> successFiles;

    /**
     * 失败上传的文件列表
     */
    private List<FailedFileInfo> failedFiles;

    /**
     * 失败文件信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedFileInfo {
        /**
         * 原始文件名
         */
        private String originalName;

        /**
         * 失败原因
         */
        private String reason;

        /**
         * 错误代码
         */
        private String errorCode;
    }
}
