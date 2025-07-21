package com.quickcode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目审核日志实体类
 * 对应数据库表：project_audit_logs
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
@Table(name = "project_audit_logs", indexes = {
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_auditor_id", columnList = "auditor_id"),
    @Index(name = "idx_audit_time", columnList = "audit_time"),
    @Index(name = "idx_audit_action", columnList = "audit_action"),
    @Index(name = "idx_audit_result", columnList = "audit_result")
})
public class ProjectAuditLog extends BaseEntity {

    /**
     * 关联的项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 审核人员ID
     */
    @Column(name = "auditor_id")
    private Long auditorId;

    /**
     * 审核动作
     * SUBMIT: 提交审核
     * APPROVE: 审核通过
     * REJECT: 审核拒绝
     * PUBLISH: 发布项目
     * OFFLINE: 下架项目
     * FEATURE: 设为精选
     * UNFEATURE: 取消精选
     */
    @NotBlank(message = "审核动作不能为空")
    @Size(max = 20, message = "审核动作长度不能超过20个字符")
    @Column(name = "audit_action", nullable = false, length = 20)
    private String auditAction;

    /**
     * 审核结果
     * PENDING: 待审核
     * APPROVED: 已通过
     * REJECTED: 已拒绝
     * PUBLISHED: 已发布
     * OFFLINE: 已下架
     */
    @Size(max = 20, message = "审核结果长度不能超过20个字符")
    @Column(name = "audit_result", length = 20)
    private String auditResult;

    /**
     * 审核时间
     */
    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核意见
     */
    @Column(name = "audit_comment", columnDefinition = "TEXT")
    private String auditComment;

    /**
     * 审核前状态
     */
    @Column(name = "previous_status")
    private Integer previousStatus;

    /**
     * 审核后状态
     */
    @Column(name = "new_status")
    private Integer newStatus;

    /**
     * 审核IP地址
     */
    @Size(max = 45, message = "IP地址长度不能超过45个字符")
    @Column(name = "audit_ip", length = 45)
    private String auditIp;

    /**
     * 用户代理
     */
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 是否为系统自动审核
     */
    @Builder.Default
    @Column(name = "is_auto", nullable = false)
    private Boolean isAuto = false;

    /**
     * 审核耗时（毫秒）
     */
    @Column(name = "audit_duration")
    private Long auditDuration;

    /**
     * 附加数据（JSON格式）
     */
    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;

    /**
     * 关联的项目实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    /**
     * 关联的审核人员实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditor_id", insertable = false, updatable = false)
    private User auditor;

    /**
     * 审核动作枚举
     */
    public enum AuditAction {
        SUBMIT("SUBMIT", "提交审核"),
        APPROVE("APPROVE", "审核通过"),
        REJECT("REJECT", "审核拒绝"),
        PUBLISH("PUBLISH", "发布项目"),
        OFFLINE("OFFLINE", "下架项目"),
        FEATURE("FEATURE", "设为精选"),
        UNFEATURE("UNFEATURE", "取消精选"),
        UPDATE("UPDATE", "更新项目"),
        DELETE("DELETE", "删除项目");

        private final String code;
        private final String description;

        AuditAction(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static AuditAction fromCode(String code) {
            for (AuditAction action : values()) {
                if (action.code.equals(code)) {
                    return action;
                }
            }
            throw new IllegalArgumentException("未知的审核动作代码: " + code);
        }
    }

    /**
     * 审核结果枚举
     */
    public enum AuditResult {
        PENDING("PENDING", "待审核"),
        APPROVED("APPROVED", "已通过"),
        REJECTED("REJECTED", "已拒绝"),
        PUBLISHED("PUBLISHED", "已发布"),
        OFFLINE("OFFLINE", "已下架"),
        FEATURED("FEATURED", "已精选"),
        UNFEATURED("UNFEATURED", "已取消精选");

        private final String code;
        private final String description;

        AuditResult(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static AuditResult fromCode(String code) {
            for (AuditResult result : values()) {
                if (result.code.equals(code)) {
                    return result;
                }
            }
            throw new IllegalArgumentException("未知的审核结果代码: " + code);
        }
    }

    /**
     * 创建审核日志
     */
    public static ProjectAuditLog createLog(Long projectId, Long auditorId, AuditAction action, 
                                          AuditResult result, String comment) {
        return ProjectAuditLog.builder()
                .projectId(projectId)
                .auditorId(auditorId)
                .auditAction(action.getCode())
                .auditResult(result.getCode())
                .auditComment(comment)
                .auditTime(LocalDateTime.now())
                .isAuto(false)
                .build();
    }

    /**
     * 创建系统自动审核日志
     */
    public static ProjectAuditLog createAutoLog(Long projectId, AuditAction action, 
                                              AuditResult result, String comment) {
        return ProjectAuditLog.builder()
                .projectId(projectId)
                .auditAction(action.getCode())
                .auditResult(result.getCode())
                .auditComment(comment)
                .auditTime(LocalDateTime.now())
                .isAuto(true)
                .build();
    }

    /**
     * 设置状态变更
     */
    public void setStatusChange(Integer previousStatus, Integer newStatus) {
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    /**
     * 设置审核耗时
     */
    public void setAuditDuration(LocalDateTime startTime) {
        if (startTime != null && this.auditTime != null) {
            this.auditDuration = java.time.Duration.between(startTime, this.auditTime).toMillis();
        }
    }

    /**
     * 检查是否为通过审核
     */
    public boolean isApproved() {
        return AuditResult.APPROVED.getCode().equals(this.auditResult);
    }

    /**
     * 检查是否为拒绝审核
     */
    public boolean isRejected() {
        return AuditResult.REJECTED.getCode().equals(this.auditResult);
    }

    /**
     * 检查是否为系统自动审核
     */
    public boolean isAutoAudit() {
        return Boolean.TRUE.equals(this.isAuto);
    }

    /**
     * 获取审核耗时的可读格式
     */
    public String getReadableDuration() {
        if (auditDuration == null) {
            return "未知";
        }
        
        long duration = auditDuration;
        if (duration < 1000) {
            return duration + " ms";
        } else if (duration < 60000) {
            return String.format("%.1f s", duration / 1000.0);
        } else if (duration < 3600000) {
            return String.format("%.1f min", duration / 60000.0);
        } else {
            return String.format("%.1f h", duration / 3600000.0);
        }
    }
}
