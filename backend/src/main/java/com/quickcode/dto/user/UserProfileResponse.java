package com.quickcode.dto.user;

import com.quickcode.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

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
     * 手机号
     */
    private String phone;

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
     * 是否为VIP
     */
    private Boolean isVip;

    /**
     * VIP到期时间
     */
    private LocalDateTime vipExpiresAt;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 所在地
     */
    private String location;

    /**
     * 个人网站
     */
    private String website;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 从User实体转换为UserProfileResponse
     */
    public static UserProfileResponse fromUser(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .lastLoginTime(user.getLastLoginTime())
                .isVip(user.getIsVip())
                .vipExpiresAt(user.getVipExpiresAt())
                .bio(user.getBio())
                .location(user.getLocation())
                .website(user.getWebsite())
                .createdTime(user.getCreatedTime())
                .updatedTime(user.getUpdatedTime())
                .build();
    }
}
