package com.quickcode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目文件上传请求DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFileUploadRequest {

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 文件类型
     * SOURCE: 源码文件
     * COVER: 封面图片
     * DEMO: 演示文件
     * DOCUMENT: 文档文件
     */
    @NotBlank(message = "文件类型不能为空")
    @Size(max = 20, message = "文件类型长度不能超过20个字符")
    private String fileType;

    /**
     * 文件描述
     */
    @Size(max = 500, message = "文件描述长度不能超过500个字符")
    private String description;

    /**
     * 是否设为主文件
     */
    @Builder.Default
    private Boolean isPrimary = false;

    /**
     * 自定义文件路径
     */
    @Size(max = 200, message = "自定义路径长度不能超过200个字符")
    private String customPath;
}
