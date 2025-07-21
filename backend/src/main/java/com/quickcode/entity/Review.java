package com.quickcode.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 项目评价实体类
 * 对应数据库表：project_reviews
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "project_reviews", indexes = {
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_rating", columnList = "rating"),
    @Index(name = "idx_created_time", columnList = "created_time"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "uk_project_user", columnList = "project_id,user_id", unique = true)
})
public class Review extends BaseEntity {

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 评价用户ID
     */
    @NotNull(message = "评价用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 评分（1-5分）
     */
    @NotNull(message = "评分不能为空")
    @DecimalMin(value = "1.0", message = "评分不能低于1分")
    @DecimalMax(value = "5.0", message = "评分不能超过5分")
    @Column(name = "rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    /**
     * 评价内容
     */
    @Size(max = 1000, message = "评价内容长度不能超过1000个字符")
    @Column(name = "content", length = 1000)
    private String content;

    /**
     * 评价状态
     * 0: 待审核
     * 1: 已发布
     * 2: 已隐藏
     * 3: 已删除
     */
    @Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 是否匿名评价
     */
    @Builder.Default
    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;

    /**
     * 点赞数
     */
    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    /**
     * 关联的项目实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    /**
     * 关联的用户实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 评价状态枚举
     */
    public enum Status {
        PENDING(0, "待审核"),
        PUBLISHED(1, "已发布"),
        HIDDEN(2, "已隐藏"),
        DELETED(3, "已删除");

        private final Integer code;
        private final String description;

        Status(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static Status fromCode(Integer code) {
            for (Status status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("未知的评价状态代码: " + code);
        }
    }

    /**
     * 检查评价是否已发布
     */
    public boolean isPublished() {
        return Status.PUBLISHED.getCode().equals(this.status);
    }

    /**
     * 检查是否为匿名评价
     */
    public boolean isAnonymousReview() {
        return Boolean.TRUE.equals(this.isAnonymous);
    }

    /**
     * 发布评价
     */
    public void publish() {
        this.status = Status.PUBLISHED.getCode();
    }

    /**
     * 隐藏评价
     */
    public void hide() {
        this.status = Status.HIDDEN.getCode();
    }

    /**
     * 删除评价
     */
    public void delete() {
        this.status = Status.DELETED.getCode();
    }

    /**
     * 增加点赞数
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 减少点赞数
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 设置为匿名评价
     */
    public void setAsAnonymous() {
        this.isAnonymous = true;
    }

    /**
     * 取消匿名
     */
    public void unsetAnonymous() {
        this.isAnonymous = false;
    }

    /**
     * 检查评分是否有效
     */
    public boolean isValidRating() {
        return this.rating != null 
            && this.rating.compareTo(new BigDecimal("1.0")) >= 0 
            && this.rating.compareTo(new BigDecimal("5.0")) <= 0;
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
}
