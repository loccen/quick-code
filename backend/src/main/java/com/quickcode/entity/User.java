package com.quickcode.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户实体类 对应数据库表：users
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"roles"})
@Entity
@Table(name = "users",
    indexes = {@Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_user_status", columnList = "status"),
        @Index(name = "idx_created_time", columnList = "created_time")})
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
   * 用户状态 0: 禁用 1: 正常 2: 待验证
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
   * 用户积分余额
   */
  @Builder.Default
  @Column(name = "points", nullable = false, precision = 10, scale = 2)
  private BigDecimal points = BigDecimal.ZERO;

  /**
   * 是否为永久会员
   */
  @Builder.Default
  @Column(name = "is_vip", nullable = false)
  private Boolean isVip = false;

  /**
   * 会员到期时间
   */
  @Column(name = "vip_expires_at")
  private LocalDateTime vipExpiresAt;

  /**
   * 用户简介
   */
  @Size(max = 500, message = "用户简介长度不能超过500个字符")
  @Column(name = "bio", length = 500)
  private String bio;

  /**
   * 用户网站
   */
  @Size(max = 200, message = "网站URL长度不能超过200个字符")
  @Column(name = "website", length = 200)
  private String website;

  /**
   * 用户位置
   */
  @Size(max = 100, message = "位置长度不能超过100个字符")
  @Column(name = "location", length = 100)
  private String location;

  /**
   * 邮箱验证令牌
   */
  @Column(name = "email_verification_token", length = 64)
  private String emailVerificationToken;

  /**
   * 邮箱验证令牌过期时间
   */
  @Column(name = "email_verification_expires_at")
  private LocalDateTime emailVerificationExpiresAt;

  /**
   * 密码重置令牌
   */
  @Column(name = "password_reset_token", length = 64)
  private String passwordResetToken;

  /**
   * 密码重置令牌过期时间
   */
  @Column(name = "password_reset_expires_at")
  private LocalDateTime passwordResetExpiresAt;

  /**
   * 用户角色关联
   */
  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "user_role_relations", joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  /**
   * 用户状态枚举
   */
  public enum Status {
    DISABLED(0, "禁用"), ACTIVE(1, "正常"), PENDING(2, "待验证");

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
    return roles.stream().anyMatch(role -> role.getRoleCode().equals(roleCode));
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

  /**
   * 检查是否为VIP用户
   */
  public boolean isVipUser() {
    return isVip && (vipExpiresAt == null || vipExpiresAt.isAfter(LocalDateTime.now()));
  }

  /**
   * 检查邮箱验证令牌是否有效
   */
  public boolean isEmailVerificationTokenValid(String token) {
    return emailVerificationToken != null && emailVerificationToken.equals(token)
        && emailVerificationExpiresAt != null
        && emailVerificationExpiresAt.isAfter(LocalDateTime.now());
  }

  /**
   * 检查密码重置令牌是否有效
   */
  public boolean isPasswordResetTokenValid(String token) {
    return passwordResetToken != null && passwordResetToken.equals(token)
        && passwordResetExpiresAt != null && passwordResetExpiresAt.isAfter(LocalDateTime.now());
  }

  /**
   * 生成邮箱验证令牌
   */
  public void generateEmailVerificationToken() {
    this.emailVerificationToken = java.util.UUID.randomUUID().toString().replace("-", "");
    this.emailVerificationExpiresAt = LocalDateTime.now().plusHours(24);
  }

  /**
   * 生成密码重置令牌
   */
  public void generatePasswordResetToken() {
    this.passwordResetToken = java.util.UUID.randomUUID().toString().replace("-", "");
    this.passwordResetExpiresAt = LocalDateTime.now().plusHours(1);
  }

  /**
   * 清除邮箱验证令牌
   */
  public void clearEmailVerificationToken() {
    this.emailVerificationToken = null;
    this.emailVerificationExpiresAt = null;
  }

  /**
   * 清除密码重置令牌
   */
  public void clearPasswordResetToken() {
    this.passwordResetToken = null;
    this.passwordResetExpiresAt = null;
  }

  /**
   * 增加积分
   */
  public void addPoints(BigDecimal amount) {
    if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
      this.points = this.points.add(amount);
    }
  }

  /**
   * 扣除积分
   */
  public boolean deductPoints(BigDecimal amount) {
    if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0
        && this.points.compareTo(amount) >= 0) {
      this.points = this.points.subtract(amount);
      return true;
    }
    return false;
  }

  /**
   * 设置VIP会员
   */
  public void setVipMembership(LocalDateTime expiresAt) {
    this.isVip = true;
    this.vipExpiresAt = expiresAt;
  }

  /**
   * 取消VIP会员
   */
  public void cancelVipMembership() {
    this.isVip = false;
    this.vipExpiresAt = null;
  }
}
