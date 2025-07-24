package com.quickcode.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JWT响应DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 访问令牌过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserInfo user;

    /**
     * 用户信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        
        /**
         * 用户ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 头像URL
         */
        private String avatarUrl;

        /**
         * 用户状态
         */
        private Integer status;

        /**
         * 邮箱是否已验证
         */
        private Boolean emailVerified;

        /**
         * 是否启用双因素认证
         */
        private Boolean twoFactorEnabled;

        /**
         * 最后登录时间
         */
        private LocalDateTime lastLoginTime;

        /**
         * 是否为管理员
         */
        private Boolean isAdmin;

        /**
         * 权限列表
         */
        private List<String> permissions;
    }
}
