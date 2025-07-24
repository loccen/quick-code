package com.quickcode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 项目审核记录实体
 * 记录项目的审核历史和状态变更
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Entity
@Table(name = "project_reviews", indexes = {
    @Index(name = "idx_project_reviews_project_id", columnList = "project_id"),
    @Index(name = "idx_project_reviews_reviewer_id", columnList = "reviewer_id"),
    @Index(name = "idx_project_reviews_created_time", columnList = "created_time"),
    @Index(name = "idx_project_reviews_action", columnList = "review_action"),
    @Index(name = "idx_project_reviews_project_action", columnList = "project_id, review_action"),
    @Index(name = "idx_project_reviews_reviewer_time", columnList = "reviewer_id, created_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectReview extends BaseEntity {

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 审核员用户ID
     */
    @NotNull(message = "审核员ID不能为空")
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    /**
     * 审核前状态
     * 0: 待审核, 1: 已发布, 2: 已下架, 3: 审核拒绝
     */
    @NotNull(message = "审核前状态不能为空")
    @Column(name = "previous_status", nullable = false)
    private Integer previousStatus;

    /**
     * 审核后状态
     * 0: 待审核, 1: 已发布, 2: 已下架, 3: 审核拒绝
     */
    @NotNull(message = "审核后状态不能为空")
    @Column(name = "new_status", nullable = false)
    private Integer newStatus;

    /**
     * 审核动作
     * APPROVE: 审核通过, REJECT: 审核拒绝, OFFLINE: 下架, FEATURE: 设为精选, UNFEATURE: 取消精选
     */
    @NotNull(message = "审核动作不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "review_action", nullable = false, length = 20)
    private ReviewAction reviewAction;

    /**
     * 审核理由或备注
     */
    @Size(max = 1000, message = "审核理由长度不能超过1000个字符")
    @Column(name = "review_reason", columnDefinition = "TEXT")
    private String reviewReason;

    /**
     * 关联的项目实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    /**
     * 关联的审核员用户实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", insertable = false, updatable = false)
    private User reviewer;

    /**
     * 审核动作枚举
     */
    public enum ReviewAction {
        APPROVE("审核通过"),
        REJECT("审核拒绝"),
        OFFLINE("下架"),
        FEATURE("设为精选"),
        UNFEATURE("取消精选"),
        REPUBLISH("重新发布");

        private final String description;

        ReviewAction(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 获取审核动作描述
     */
    public String getReviewActionDescription() {
        return reviewAction != null ? reviewAction.getDescription() : "";
    }

    /**
     * 获取审核前状态描述
     */
    public String getPreviousStatusDescription() {
        return getStatusDescription(previousStatus);
    }

    /**
     * 获取审核后状态描述
     */
    public String getNewStatusDescription() {
        return getStatusDescription(newStatus);
    }

    /**
     * 获取状态描述
     */
    private String getStatusDescription(Integer status) {
        if (status == null) return "";
        
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已发布";
            case 2 -> "已下架";
            case 3 -> "审核拒绝";
            default -> "未知状态";
        };
    }

    /**
     * 创建审核记录
     */
    public static ProjectReview createReviewRecord(Long projectId, Long reviewerId, 
                                                  Integer previousStatus, Integer newStatus,
                                                  ReviewAction action, String reason) {
        return ProjectReview.builder()
                .projectId(projectId)
                .reviewerId(reviewerId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reviewAction(action)
                .reviewReason(reason)
                .build();
    }
}
