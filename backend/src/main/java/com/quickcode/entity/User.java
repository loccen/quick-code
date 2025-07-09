package com.quickcode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 * 对应数据库表：users
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
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_time", columnList = "created_time")
})
public class User extends BaseEntity {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * 密码（加密后）
     */
    @NotBlank(message = "密码不能为空")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Column(name = "nickname", length = 50)
    private String nickname;

    /**
     * 头像URL
     */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /**
     * 手机号
     */
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 用户状态
     * 0: 禁用
     * 1: 正常
     * 2: 待验证
     */
    @Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 邮箱是否已验证
     */
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    /**
     * 是否启用双因素认证
     */
    @Builder.Default
    @Column(name = "two_factor_enabled", nullable = false)
    private Boolean twoFactorEnabled = false;

    /**
     * 双因素认证密钥
     */
    @Column(name = "two_factor_secret", length = 32)
    private String twoFactorSecret;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    @Builder.Default
    @Column(name = "login_failure_count", nullable = false)
    private Integer loginFailureCount = 0;

    /**
     * 账户锁定时间
     */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    /**
     * 用户角色关联
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_role_relations",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * 用户状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        ACTIVE(1, "正常"),
        PENDING(2, "待验证");

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
            throw new IllegalArgumentException("未知的用户状态代码: " + code);
        }
    }

    /**
     * 检查用户是否被锁定
     */
    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * 检查用户是否激活
     */
    public boolean isActive() {
        return Status.ACTIVE.getCode().equals(this.status) && !isLocked();
    }

    /**
     * 添加角色
     */
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    /**
     * 移除角色
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    /**
     * 检查是否拥有指定角色
     */
    public boolean hasRole(String roleCode) {
        return roles.stream()
                .anyMatch(role -> role.getRoleCode().equals(roleCode));
    }

    /**
     * 重置登录失败次数
     */
    public void resetLoginFailureCount() {
        this.loginFailureCount = 0;
        this.lockedUntil = null;
    }

    /**
     * 增加登录失败次数
     */
    public void incrementLoginFailureCount() {
        this.loginFailureCount++;
        // 如果失败次数达到5次，锁定账户30分钟
        if (this.loginFailureCount >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }
}
