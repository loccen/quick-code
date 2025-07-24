package com.quickcode.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 项目上传请求DTO
 * 简化版本，只包含项目上传必需的核心字段
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
    @Size(max = 10, message = "标签数量不能超过10个")
    private List<@NotBlank(message = "标签不能为空") @Size(max = 50, message = "标签长度不能超过50个字符") String> tags;

    /**
     * 项目价格（积分）
     */
    @Builder.Default
    @DecimalMin(value = "0.00", message = "项目价格不能为负数")
    @DecimalMax(value = "999999.99", message = "项目价格不能超过999999.99")
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * 项目演示URL
     */
    @Size(max = 255, message = "演示URL长度不能超过255个字符")
    @Pattern(regexp = "^$|^https?://.*", message = "演示URL格式不正确，必须以http://或https://开头")
    private String demoUrl;

    /**
     * 技术栈
     */
    @Size(max = 20, message = "技术栈数量不能超过20个")
    @NotEmpty(message = "请至少选择一个技术栈")
    private List<@NotBlank(message = "技术栈不能为空") @Size(max = 50, message = "技术栈名称长度不能超过50个字符") String> techStack;

    /**
     * 封面图片URL
     */
    @Size(max = 255, message = "封面图片URL长度不能超过255个字符")
    @Pattern(regexp = "^$|^https?://.*", message = "封面图片URL格式不正确，必须以http://或https://开头")
    private String coverImage;

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
        return tags == null || (tags.size() <= 10 &&
               tags.stream().allMatch(tag -> tag != null && tag.length() <= 50));
    }

    /**
     * 验证项目价格
     */
    public boolean isValidPrice() {
        return price != null && price.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * 验证所有字段
     */
    public boolean isValid() {
        return isValidPrice() && isValidTechStack() && isValidTags();
    }

    /**
     * 标准化数据
     */
    public void normalize() {
        if (title != null) {
            title = title.trim();
        }
        if (description != null) {
            description = description.trim();
        }
        if (demoUrl != null && demoUrl.trim().isEmpty()) {
            demoUrl = null;
        }
        if (coverImage != null && coverImage.trim().isEmpty()) {
            coverImage = null;
        }
    }
}
