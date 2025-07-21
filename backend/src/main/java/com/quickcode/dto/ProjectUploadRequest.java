package com.quickcode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 项目上传请求DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUploadRequest {

    /**
     * 项目标题
     */
    @NotBlank(message = "项目标题不能为空")
    @Size(max = 100, message = "项目标题长度不能超过100个字符")
    private String title;

    /**
     * 项目描述
     */
    @NotBlank(message = "项目描述不能为空")
    @Size(max = 2000, message = "项目描述长度不能超过2000个字符")
    private String description;

    /**
     * 项目分类ID
     */
    @NotNull(message = "项目分类不能为空")
    private Long categoryId;

    /**
     * 项目标签
     */
    private List<String> tags;

    /**
     * 项目价格（积分）
     */
    @Builder.Default
    private Integer price = 0;

    /**
     * 是否免费
     */
    @Builder.Default
    private Boolean isFree = true;

    /**
     * 项目演示URL
     */
    @Size(max = 500, message = "演示URL长度不能超过500个字符")
    private String demoUrl;

    /**
     * 项目文档URL
     */
    @Size(max = 500, message = "文档URL长度不能超过500个字符")
    private String documentUrl;

    /**
     * 项目源码仓库URL
     */
    @Size(max = 500, message = "源码仓库URL长度不能超过500个字符")
    private String repositoryUrl;

    /**
     * 技术栈
     */
    private List<String> techStack;

    /**
     * 项目特性
     */
    private List<String> features;

    /**
     * 安装说明
     */
    @Size(max = 5000, message = "安装说明长度不能超过5000个字符")
    private String installInstructions;

    /**
     * 使用说明
     */
    @Size(max = 5000, message = "使用说明长度不能超过5000个字符")
    private String usageInstructions;

    /**
     * 更新日志
     */
    @Size(max = 3000, message = "更新日志长度不能超过3000个字符")
    private String changelog;

    /**
     * 许可证类型
     */
    @Size(max = 50, message = "许可证类型长度不能超过50个字符")
    private String licenseType;

    /**
     * 最低系统要求
     */
    @Size(max = 1000, message = "系统要求长度不能超过1000个字符")
    private String systemRequirements;

    /**
     * 项目版本
     */
    @Size(max = 20, message = "项目版本长度不能超过20个字符")
    private String version;

    /**
     * 是否开源
     */
    @Builder.Default
    private Boolean isOpenSource = false;

    /**
     * 是否支持商业使用
     */
    @Builder.Default
    private Boolean isCommercialUse = true;

    /**
     * 联系方式
     */
    @Size(max = 200, message = "联系方式长度不能超过200个字符")
    private String contactInfo;

    /**
     * 项目截图URL列表
     */
    private List<String> screenshots;

    /**
     * 封面图片URL
     */
    @Size(max = 500, message = "封面图片URL长度不能超过500个字符")
    private String coverImageUrl;

    /**
     * 是否立即发布
     */
    @Builder.Default
    private Boolean publishImmediately = false;

    /**
     * 备注信息
     */
    @Size(max = 1000, message = "备注信息长度不能超过1000个字符")
    private String remarks;

    /**
     * 验证项目价格
     */
    public boolean isValidPrice() {
        if (Boolean.TRUE.equals(isFree)) {
            return price == null || price == 0;
        } else {
            return price != null && price > 0;
        }
    }

    /**
     * 验证技术栈
     */
    public boolean isValidTechStack() {
        return techStack != null && !techStack.isEmpty() && techStack.size() <= 10;
    }

    /**
     * 验证标签
     */
    public boolean isValidTags() {
        return tags == null || (tags.size() <= 5 && 
               tags.stream().allMatch(tag -> tag != null && tag.length() <= 20));
    }

    /**
     * 验证特性列表
     */
    public boolean isValidFeatures() {
        return features == null || (features.size() <= 10 && 
               features.stream().allMatch(feature -> feature != null && feature.length() <= 100));
    }

    /**
     * 验证截图数量
     */
    public boolean isValidScreenshots() {
        return screenshots == null || screenshots.size() <= 8;
    }

    /**
     * 获取标签字符串
     */
    public String getTagsAsString() {
        return tags != null ? String.join(",", tags) : "";
    }

    /**
     * 获取技术栈字符串
     */
    public String getTechStackAsString() {
        return techStack != null ? String.join(",", techStack) : "";
    }

    /**
     * 获取特性字符串
     */
    public String getFeaturesAsString() {
        return features != null ? String.join(",", features) : "";
    }

    /**
     * 获取截图URL字符串
     */
    public String getScreenshotsAsString() {
        return screenshots != null ? String.join(",", screenshots) : "";
    }
}
