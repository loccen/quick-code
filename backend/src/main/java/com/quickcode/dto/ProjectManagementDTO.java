package com.quickcode.dto;

import com.quickcode.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目管理DTO
 * 用于项目管理界面显示项目信息
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectManagementDTO {

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
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 项目价格
     */
    private Double price;

    /**
     * 是否免费
     */
    private Boolean isFree;

    /**
     * 项目状态
     */
    private Integer status;

    /**
     * 项目状态描述
     */
    private String statusDesc;

    /**
     * 是否精选
     */
    private Boolean isFeatured;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 评分
     */
    private Double rating;

    /**
     * 评分人数
     */
    private Integer ratingCount;

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
     * 项目标签
     */
    private List<String> tags;

    /**
     * 技术栈
     */
    private List<String> techStack;

    /**
     * 项目文件统计
     */
    private FileStatistics fileStatistics;

    /**
     * 最近下载统计
     */
    private DownloadStatistics downloadStatistics;

    /**
     * 文件统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileStatistics {
        private Long totalFiles;
        private Long totalSize;
        private String readableTotalSize;
        private Long sourceFiles;
        private Long coverImages;
        private Long demoFiles;
        private Long documentFiles;
    }

    /**
     * 下载统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DownloadStatistics {
        private Long totalDownloads;
        private Long uniqueDownloaders;
        private Long todayDownloads;
        private Long weekDownloads;
        private Long monthDownloads;
    }

    /**
     * 从Project实体转换
     */
    public static ProjectManagementDTO fromProject(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectManagementDTO.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .categoryId(project.getCategoryId())
                .categoryName(getCategoryName(project))
                .userId(project.getUserId())
                .username(getUsername(project))
                .coverImage(project.getCoverImage())
                .price(project.getPrice() != null ? project.getPrice().doubleValue() : null)
                .isFree(project.getPrice() != null && project.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0)
                .status(project.getStatus())
                .statusDesc(getStatusDescription(project.getStatus()))
                .isFeatured(project.getIsFeatured())
                .viewCount(project.getViewCount())
                .downloadCount(project.getDownloadCount())
                .likeCount(project.getLikeCount())
                .rating(project.getRating() != null ? project.getRating().doubleValue() : null)
                .ratingCount(project.getRatingCount())
                .publishedTime(project.getPublishedTime())
                .createdTime(project.getCreatedTime())
                .updatedTime(project.getUpdatedTime())
                .tags(project.getTags())
                .techStack(project.getTechStack())
                .build();
    }

    /**
     * 获取分类名称
     */
    private static String getCategoryName(Project project) {
        // 这里应该通过关联查询获取分类名称
        // 暂时返回默认值
        return "分类-" + project.getCategoryId();
    }

    /**
     * 获取用户名
     */
    private static String getUsername(Project project) {
        // 这里应该通过关联查询获取用户名
        // 暂时返回默认值
        return "用户-" + project.getUserId();
    }

    /**
     * 获取状态描述
     */
    private static String getStatusDescription(Integer status) {
        if (status == null) {
            return "未知";
        }

        try {
            Project.Status projectStatus = Project.Status.fromCode(status);
            return projectStatus.getDescription();
        } catch (IllegalArgumentException e) {
            return "未知状态";
        }
    }



    /**
     * 检查项目是否已发布
     */
    public boolean isPublished() {
        return status != null && status.equals(2);
    }

    /**
     * 检查项目是否为草稿
     */
    public boolean isDraft() {
        return status != null && status.equals(0);
    }

    /**
     * 检查项目是否待审核
     */
    public boolean isPendingReview() {
        return status != null && status.equals(1);
    }

    /**
     * 检查项目是否已下架
     */
    public boolean isOffline() {
        return status != null && status.equals(3);
    }

    /**
     * 检查项目是否被拒绝
     */
    public boolean isRejected() {
        return status != null && status.equals(4);
    }

    /**
     * 获取项目状态颜色
     */
    public String getStatusColor() {
        if (status == null) {
            return "default";
        }

        switch (status) {
            case 0: return "info";      // 草稿 - 蓝色
            case 1: return "warning";   // 待审核 - 橙色
            case 2: return "success";   // 已发布 - 绿色
            case 3: return "default";   // 已下架 - 灰色
            case 4: return "danger";    // 已拒绝 - 红色
            default: return "default";
        }
    }

    /**
     * 获取价格显示文本
     */
    public String getPriceText() {
        if (Boolean.TRUE.equals(isFree)) {
            return "免费";
        } else if (price != null && price > 0) {
            return price + " 积分";
        } else {
            return "价格待定";
        }
    }

    /**
     * 获取评分显示文本
     */
    public String getRatingText() {
        if (rating != null && rating > 0) {
            return String.format("%.1f", rating);
        } else {
            return "暂无评分";
        }
    }
}
