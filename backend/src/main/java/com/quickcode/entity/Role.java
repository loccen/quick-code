package com.quickcode.entity;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 角色实体类 对应数据库表：user_roles
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"users", "permissions"})
@Entity
@Table(name = "user_roles", indexes = {@Index(name = "idx_role_code", columnList = "role_code"),
    @Index(name = "idx_role_status", columnList = "status")})
public class Role extends BaseEntity {

  /**
   * 角色名称
   */
  @NotBlank(message = "角色名称不能为空")
  @Size(max = 50, message = "角色名称长度不能超过50个字符")
  @Column(name = "role_name", nullable = false, length = 50)
  private String roleName;

  /**
   * 角色代码（唯一标识）
   */
  @NotBlank(message = "角色代码不能为空")
  @Size(max = 50, message = "角色代码长度不能超过50个字符")
  @Column(name = "role_code", nullable = false, unique = true, length = 50)
  private String roleCode;

  /**
   * 角色描述
   */
  @Size(max = 255, message = "角色描述长度不能超过255个字符")
  @Column(name = "description")
  private String description;

  /**
   * 角色状态 0: 禁用 1: 正常
   */
  @Builder.Default
  @Column(name = "status", nullable = false)
  private Integer status = 1;

  /**
   * 拥有此角色的用户
   */
  @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
  @Builder.Default
  private Set<User> users = new HashSet<>();

  /**
   * 角色拥有的权限
   */
  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "role_permission_relations", joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  @Builder.Default
  private Set<Permission> permissions = new HashSet<>();

  /**
   * 角色状态枚举
   */
  public enum Status {
    DISABLED(0, "禁用"), ACTIVE(1, "正常");

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
      throw new IllegalArgumentException("未知的角色状态代码: " + code);
    }
  }

  /**
   * 预定义角色代码
   */
  public static class RoleCode {
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String REVIEWER = "REVIEWER";
  }

  /**
   * 检查角色是否激活
   */
  public boolean isActive() {
    return Status.ACTIVE.getCode().equals(this.status);
  }

  /**
   * 添加权限
   */
  public void addPermission(Permission permission) {
    this.permissions.add(permission);
    permission.getRoles().add(this);
  }

  /**
   * 移除权限
   */
  public void removePermission(Permission permission) {
    this.permissions.remove(permission);
    permission.getRoles().remove(this);
  }

  /**
   * 检查是否拥有指定权限
   */
  public boolean hasPermission(String permissionCode) {
    return permissions.stream()
        .anyMatch(permission -> permission.getPermissionCode().equals(permissionCode));
  }

  /**
   * 获取所有权限代码
   */
  public Set<String> getPermissionCodes() {
    Set<String> codes = new HashSet<>();
    for (Permission permission : permissions) {
      codes.add(permission.getPermissionCode());
    }
    return codes;
  }
}
