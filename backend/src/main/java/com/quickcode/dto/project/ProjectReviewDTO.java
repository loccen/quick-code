package com.quickcode.dto.project;

import com.quickcode.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 项目评价DTO
 * 用于项目详情页面的评价展示
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectReviewDTO {

    /**
     * 评价ID
     */
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 评价用户ID
     */
    private Long userId;

    /**
     * 用户名
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
     * 评分
     */
    private BigDecimal rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 是否匿名评价
     */
    private Boolean isAnonymous;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评价状态
     */
    private Integer status;

    /**
     * 评价状态描述
     */
    private String statusText;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 从Review实体转换为ProjectReviewDTO
     */
    public static ProjectReviewDTO fromReview(Review review) {
        return ProjectReviewDTO.builder()
                .id(review.getId())
                .projectId(review.getProjectId())
                .userId(review.getUserId())
                .username(review.getUser() != null ? review.getUser().getUsername() : null)
                .userNickname(review.getUser() != null ? review.getUser().getNickname() : null)
                .userAvatar(review.getUser() != null ? review.getUser().getAvatarUrl() : null)
                .rating(review.getRating())
                .content(review.getContent())
                .isAnonymous(review.getIsAnonymous())
                .likeCount(review.getLikeCount())
                .status(review.getStatus())
                .statusText(getStatusText(review.getStatus()))
                .createdTime(review.getCreatedTime())
                .updatedTime(review.getUpdatedTime())
                .build();
    }

    /**
     * 从Review实体转换为匿名ProjectReviewDTO
     */
    public static ProjectReviewDTO fromReviewAnonymous(Review review) {
        ProjectReviewDTO dto = fromReview(review);
        if (Boolean.TRUE.equals(review.getIsAnonymous())) {
            dto.setUsername("匿名用户");
            dto.setUserNickname("匿名用户");
            dto.setUserAvatar(null);
        }
        return dto;
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
            case 2 -> "已隐藏";
            case 3 -> "已删除";
            default -> "未知";
        };
    }

    /**
     * 检查评价是否已发布
     */
    public boolean isPublished() {
        return Integer.valueOf(1).equals(this.status);
    }

    /**
     * 检查是否为匿名评价
     */
    public boolean isAnonymousReview() {
        return Boolean.TRUE.equals(this.isAnonymous);
    }

    /**
     * 获取评分星级（整数）
     */
    public int getStarRating() {
        return this.rating != null ? this.rating.intValue() : 0;
    }

    /**
     * 检查是否有评价内容
     */
    public boolean hasContent() {
        return this.content != null && !this.content.trim().isEmpty();
    }

    /**
     * 获取显示用户名
     */
    public String getDisplayUsername() {
        if (isAnonymousReview()) {
            return "匿名用户";
        }
        return userNickname != null && !userNickname.trim().isEmpty() ? userNickname : username;
    }

    /**
     * 获取显示头像
     */
    public String getDisplayAvatar() {
        if (isAnonymousReview()) {
            return null; // 匿名用户不显示头像
        }
        return userAvatar;
    }

    /**
     * 获取评分文本
     */
    public String getRatingText() {
        if (rating == null) {
            return "未评分";
        }
        return rating.stripTrailingZeros().toPlainString() + "分";
    }

    /**
     * 获取评价内容摘要（限制长度）
     */
    public String getContentSummary(int maxLength) {
        if (!hasContent()) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    /**
     * 获取评价内容摘要（默认100字符）
     */
    public String getContentSummary() {
        return getContentSummary(100);
    }

    /**
     * 检查是否有点赞
     */
    public boolean hasLikes() {
        return likeCount != null && likeCount > 0;
    }

    /**
     * 获取点赞数文本
     */
    public String getLikeCountText() {
        if (!hasLikes()) {
            return "0";
        }
        if (likeCount >= 1000) {
            return String.format("%.1fk", likeCount / 1000.0);
        }
        return likeCount.toString();
    }

    /**
     * 获取相对时间文本
     */
    public String getRelativeTimeText() {
        if (createdTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdTime, now).toMinutes();
        
        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (minutes < 1440) { // 24小时
            return (minutes / 60) + "小时前";
        } else if (minutes < 43200) { // 30天
            return (minutes / 1440) + "天前";
        } else {
            return createdTime.toLocalDate().toString();
        }
    }
}
