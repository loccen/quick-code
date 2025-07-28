package com.quickcode.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * 更新项目请求DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateRequest {

    /**
     * 项目ID（由URL路径参数提供，请求体中可选）
     */
    private Long id;

    /**
     * 项目标题
     */
    @NotBlank(message = "项目标题不能为空")
    @Size(max = 100, message = "项目标题长度不能超过100个字符")
    private String title;

    /**
     * 项目描述
     */
    @Size(max = 5000, message = "项目描述长度不能超过5000个字符")
    private String description;

    /**
     * 项目分类ID
     */
    @NotNull(message = "项目分类不能为空")
    private Long categoryId;

    /**
     * 项目价格（积分）
     */
    @DecimalMin(value = "0.00", message = "项目价格不能为负数")
    private BigDecimal price;

    /**
     * 封面图片URL
     */
    @Size(max = 255, message = "封面图片URL长度不能超过255个字符")
    private String coverImage;

    /**
     * 演示地址
     */
    @Size(max = 255, message = "演示地址长度不能超过255个字符")
    private String demoUrl;

    /**
     * 源码文件URL
     */
    @Size(max = 255, message = "源码文件URL长度不能超过255个字符")
    private String sourceFileUrl;

    /**
     * Docker镜像名称
     */
    @Size(max = 255, message = "Docker镜像名称长度不能超过255个字符")
    private String dockerImage;

    /**
     * 技术栈
     */
    private List<String> techStack;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 验证请求数据
     */
    public void validate() {
        // 验证技术栈
        if (techStack != null) {
            if (techStack.size() > 20) {
                throw new IllegalArgumentException("技术栈数量不能超过20个");
            }
            for (String tech : techStack) {
                if (tech == null || tech.trim().isEmpty()) {
                    throw new IllegalArgumentException("技术栈不能包含空值");
                }
                if (tech.length() > 50) {
                    throw new IllegalArgumentException("技术栈名称长度不能超过50个字符");
                }
            }
        }

        // 验证标签
        if (tags != null) {
            if (tags.size() > 10) {
                throw new IllegalArgumentException("标签数量不能超过10个");
            }
            for (String tag : tags) {
                if (tag == null || tag.trim().isEmpty()) {
                    throw new IllegalArgumentException("标签不能包含空值");
                }
                if (tag.length() > 30) {
                    throw new IllegalArgumentException("标签长度不能超过30个字符");
                }
            }
        }

        // 验证价格
        if (price != null && price.compareTo(new BigDecimal("999999.99")) > 0) {
            throw new IllegalArgumentException("项目价格不能超过999999.99积分");
        }
    }

    /**
     * 检查是否为免费项目
     */
    public boolean isFree() {
        return price == null || price.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 检查是否有演示地址
     */
    public boolean hasDemoUrl() {
        return demoUrl != null && !demoUrl.trim().isEmpty();
    }

    /**
     * 检查是否有源码文件
     */
    public boolean hasSourceFile() {
        return sourceFileUrl != null && !sourceFileUrl.trim().isEmpty();
    }

    /**
     * 检查是否有Docker镜像
     */
    public boolean hasDockerImage() {
        return dockerImage != null && !dockerImage.trim().isEmpty();
    }

    /**
     * 检查是否有技术栈
     */
    public boolean hasTechStack() {
        return techStack != null && !techStack.isEmpty();
    }

    /**
     * 检查是否有标签
     */
    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    /**
     * 清理和标准化数据
     */
    public void normalize() {
        // 清理标题
        if (title != null) {
            title = title.trim();
        }

        // 清理描述
        if (description != null) {
            description = description.trim();
            if (description.isEmpty()) {
                description = null;
            }
        }

        // 清理URL字段
        coverImage = normalizeUrl(coverImage);
        demoUrl = normalizeUrl(demoUrl);
        sourceFileUrl = normalizeUrl(sourceFileUrl);

        // 清理Docker镜像名称
        if (dockerImage != null) {
            dockerImage = dockerImage.trim().toLowerCase();
            if (dockerImage.isEmpty()) {
                dockerImage = null;
            }
        }

        // 清理技术栈
        if (techStack != null) {
            techStack = techStack.stream()
                    .filter(tech -> tech != null && !tech.trim().isEmpty())
                    .map(String::trim)
                    .distinct()
                    .toList();
            if (techStack.isEmpty()) {
                techStack = null;
            }
        }

        // 清理标签
        if (tags != null) {
            tags = tags.stream()
                    .filter(tag -> tag != null && !tag.trim().isEmpty())
                    .map(String::trim)
                    .distinct()
                    .toList();
            if (tags.isEmpty()) {
                tags = null;
            }
        }

        // 设置默认价格
        if (price == null) {
            price = BigDecimal.ZERO;
        }
    }

    /**
     * 标准化URL
     */
    private String normalizeUrl(String url) {
        if (url == null) {
            return null;
        }
        url = url.trim();
        if (url.isEmpty()) {
            return null;
        }
        // 确保URL以http://或https://开头
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }
}
