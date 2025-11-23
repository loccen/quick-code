package com.quickcode.dto.project;

import com.quickcode.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目基础信息DTO
 * 用于项目列表展示
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {

    /**
     * 项目ID
     */
    private Long id;

    /**
     * 项目标题
     */
    private String title;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 项目分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 上传用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 项目价格（积分）
     */
    private BigDecimal price;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 演示地址
     */
    private String demoUrl;

    /**
     * 是否容器化
     */
    private boolean hasDocker;

    /**
     * 技术栈
     */
    private List<String> techStack;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 评分人数
     */
    private Integer ratingCount;

    /**
     * 项目状态
     */
    private Integer status;

    /**
     * 项目状态描述
     */
    private String statusText;

    /**
     * 是否精选
     */
    private Boolean isFeatured;

    /**
     * 发布时间
     */
    private LocalDateTime publishedTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 收藏时间（仅在收藏列表中使用）
     */
    private LocalDateTime favoriteTime;

    /**
     * 购买时间（仅在购买列表中使用）
     */
    private LocalDateTime purchaseTime;

    /**
     * 购买价格（仅在购买列表中使用）
     */
    private BigDecimal purchasePrice;

    /**
     * 从Project实体转换为ProjectDTO
     */
    public static ProjectDTO fromProject(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .categoryId(project.getCategoryId())
                .categoryName(project.getCategory() != null ? project.getCategory().getName() : null)
                .userId(project.getUserId())
                .username(project.getUser() != null ? project.getUser().getUsername() : null)
                .userNickname(project.getUser() != null ? project.getUser().getNickname() : null)
                .userAvatar(project.getUser() != null ? project.getUser().getAvatarUrl() : null)
                .price(project.getPrice())
                .coverImage(project.getCoverImage())
                .demoUrl(project.getDemoUrl())
                .hasDocker(project.getDockerImage() != null && !project.getDockerImage().isEmpty())
                .techStack(project.getTechStack())
                .tags(project.getTags())
                .downloadCount(project.getDownloadCount())
                .viewCount(project.getViewCount())
                .likeCount(project.getLikeCount())
                .favoriteCount(project.getFavoriteCount())
                .rating(project.getRating())
                .ratingCount(project.getRatingCount())
                .status(project.getStatus())
                .statusText(getStatusText(project.getStatus()))
                .isFeatured(project.getIsFeatured())
                .publishedTime(project.getPublishedTime())
                .createdTime(project.getCreatedTime())
                .updatedTime(project.getUpdatedTime())
                .build();
    }

    /**
     * 获取状态文本描述
     */
    private static String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已发布";
            case 2 -> "已下架";
            case 3 -> "审核拒绝";
            default -> "未知";
        };
    }

    /**
     * 检查项目是否已发布
     */
    public boolean isPublished() {
        return Integer.valueOf(1).equals(this.status);
    }

    /**
     * 检查项目是否为精选项目
     */
    public boolean isFeaturedProject() {
        return Boolean.TRUE.equals(this.isFeatured);
    }

    /**
     * 获取评分星级（整数）
     */
    public int getStarRating() {
        return this.rating != null ? this.rating.intValue() : 0;
    }

    /**
     * 检查是否有演示地址
     */
    public boolean hasDemoUrl() {
        return this.demoUrl != null && !this.demoUrl.trim().isEmpty();
    }

    /**
     * 检查是否有技术栈信息
     */
    public boolean hasTechStack() {
        return this.techStack != null && !this.techStack.isEmpty();
    }

    /**
     * 检查是否有标签信息
     */
    public boolean hasTags() {
        return this.tags != null && !this.tags.isEmpty();
    }

    /**
     * 获取技术栈字符串（用逗号分隔）
     */
    public String getTechStackString() {
        return hasTechStack() ? String.join(", ", this.techStack) : "";
    }

    /**
     * 获取标签字符串（用逗号分隔）
     */
    public String getTagsString() {
        return hasTags() ? String.join(", ", this.tags) : "";
    }

    /**
     * 检查是否为免费项目
     */
    public boolean isFree() {
        return this.price == null || this.price.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 获取格式化的价格字符串
     */
    public String getFormattedPrice() {
        if (isFree()) {
            return "免费";
        }
        return this.price.stripTrailingZeros().toPlainString() + " 积分";
    }
}
