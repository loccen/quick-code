package com.quickcode.security.jwt;

import com.quickcode.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 用户主体类
 * 实现Spring Security的UserDetails接口
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";

    private Long id;
    private String username;
    private String email;
    private String password;
    private Integer status;
    private Boolean emailVerified;
    private Boolean twoFactorEnabled;
    private LocalDateTime lockedUntil;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * 从User实体创建UserPrincipal
     */
    public static UserPrincipal create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("用户对象不能为空");
        }

        // 根据用户的isAdmin字段设置权限
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(ROLE_PREFIX + (user.isAdmin() ? ADMIN_ROLE : USER_ROLE))
        );

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getStatus(),
                user.getEmailVerified(),
                user.getTwoFactorEnabled(),
                user.getLockedUntil(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 账户不会过期
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 凭证不会过期
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1; // 状态为1表示启用
    }

    /**
     * 检查是否拥有指定权限
     */
    public boolean hasPermission(String permission) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(permission));
    }

    /**
     * 检查是否拥有指定角色
     */
    public boolean hasRole(String role) {
        String roleWithPrefix = role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleWithPrefix));
    }

    /**
     * 获取所有权限代码（不包括角色）
     */
    public List<String> getPermissions() {
        // 简化版本：管理员拥有所有权限，普通用户只有基本权限
        if (hasRole(ADMIN_ROLE)) {
            return List.of("user:manage", "project:manage", "system:manage");
        } else {
            return List.of("project:create", "project:read");
        }
    }

    /**
     * 获取所有角色代码（不包括ROLE_前缀）
     */
    public List<String> getRoles() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith(ROLE_PREFIX))
                .map(auth -> auth.substring(ROLE_PREFIX.length())) // 移除ROLE_前缀
                .toList();
    }

    /**
     * 检查是否为管理员
     */
    public boolean isAdmin() {
        return hasRole(ADMIN_ROLE);
    }

    /**
     * 检查是否为普通用户
     */
    public boolean isUser() {
        return hasRole("USER");
    }

    /**
     * 检查是否为审核员
     */
    public boolean isReviewer() {
        return hasRole("REVIEWER");
    }
}
