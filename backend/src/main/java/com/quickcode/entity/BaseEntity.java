package com.quickcode.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 基础实体类
 * 包含所有实体的公共字段：ID、创建时间、更新时间
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 版本号（用于乐观锁）
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * 逻辑删除标记
     * 0: 未删除
     * 1: 已删除
     */
    @Column(name = "deleted", nullable = false)
    protected Integer deleted = 0;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdTime == null) {
            createdTime = now;
        }
        if (updatedTime == null) {
            updatedTime = now;
        }
        if (deleted == null) {
            deleted = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    /**
     * 标记为已删除
     */
    public void markDeleted() {
        this.deleted = 1;
    }

    /**
     * 恢复删除状态
     */
    public void unmarkDeleted() {
        this.deleted = 0;
    }

    /**
     * 检查是否已被逻辑删除
     */
    public boolean isDeleted() {
        return this.deleted != null && this.deleted == 1;
    }

    /**
     * 检查是否可用（未删除）
     */
    public boolean isAvailable() {
        return !isDeleted();
    }
}
