package com.quickcode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

/**
 * 权限实体类
 * 对应数据库表：permissions
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
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_code", columnList = "permission_code"),
    @Index(name = "idx_resource_type", columnList = "resource_type"),
    @Index(name = "idx_status", columnList = "status")
})
public class Permission extends BaseEntity {

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    @Column(name = "permission_name", nullable = false, length = 50)
    private String permissionName;

    /**
     * 权限代码（唯一标识）
     */
    @NotBlank(message = "权限代码不能为空")
    @Size(max = 100, message = "权限代码长度不能超过100个字符")
    @Column(name = "permission_code", nullable = false, unique = true, length = 100)
    private String permissionCode;

    /**
     * 权限描述
     */
    @Size(max = 255, message = "权限描述长度不能超过255个字符")
    @Column(name = "description")
    private String description;

    /**
     * 资源类型
     */
    @Size(max = 50, message = "资源类型长度不能超过50个字符")
    @Column(name = "resource_type", length = 50)
    private String resourceType;

    /**
     * 操作类型
     */
    @Size(max = 50, message = "操作类型长度不能超过50个字符")
    @Column(name = "action_type", length = 50)
    private String actionType;

    /**
     * 权限状态
     * 0: 禁用
     * 1: 正常
     */
    @Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 拥有此权限的角色
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * 权限状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        ACTIVE(1, "正常");

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
            throw new IllegalArgumentException("未知的权限状态代码: " + code);
        }
    }

    /**
     * 预定义权限代码
     */
    public static class PermissionCode {
        // 用户管理权限
        public static final String USER_CREATE = "user:create";
        public static final String USER_READ = "user:read";
        public static final String USER_UPDATE = "user:update";
        public static final String USER_DELETE = "user:delete";
        public static final String USER_MANAGE = "user:manage";

        // 项目管理权限
        public static final String PROJECT_CREATE = "project:create";
        public static final String PROJECT_READ = "project:read";
        public static final String PROJECT_UPDATE = "project:update";
        public static final String PROJECT_DELETE = "project:delete";
        public static final String PROJECT_REVIEW = "project:review";
        public static final String PROJECT_MANAGE = "project:manage";

        // 积分管理权限
        public static final String POINT_VIEW = "point:view";
        public static final String POINT_MANAGE = "point:manage";
        public static final String POINT_TRANSACTION = "point:transaction";

        // 系统管理权限
        public static final String SYSTEM_CONFIG = "system:config";
        public static final String SYSTEM_MONITOR = "system:monitor";
        public static final String SYSTEM_LOG = "system:log";

        // 角色权限管理
        public static final String ROLE_CREATE = "role:create";
        public static final String ROLE_READ = "role:read";
        public static final String ROLE_UPDATE = "role:update";
        public static final String ROLE_DELETE = "role:delete";
        public static final String ROLE_ASSIGN = "role:assign";
    }

    /**
     * 资源类型枚举
     */
    public static class ResourceType {
        public static final String USER = "USER";
        public static final String PROJECT = "PROJECT";
        public static final String POINT = "POINT";
        public static final String SYSTEM = "SYSTEM";
        public static final String ROLE = "ROLE";
    }

    /**
     * 操作类型枚举
     */
    public static class ActionType {
        public static final String CREATE = "CREATE";
        public static final String READ = "READ";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String MANAGE = "MANAGE";
        public static final String REVIEW = "REVIEW";
        public static final String ASSIGN = "ASSIGN";
    }

    /**
     * 检查权限是否激活
     */
    public boolean isActive() {
        return Status.ACTIVE.getCode().equals(this.status);
    }

    /**
     * 构建权限代码
     */
    public static String buildPermissionCode(String resourceType, String actionType) {
        return resourceType.toLowerCase() + ":" + actionType.toLowerCase();
    }
}
