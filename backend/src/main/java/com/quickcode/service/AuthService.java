package com.quickcode.service;

import com.quickcode.dto.auth.JwtResponse;
import com.quickcode.dto.auth.LoginRequest;
import com.quickcode.dto.auth.RegisterRequest;

/**
 * 认证服务接口
 * 提供用户认证相关的业务逻辑方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 用户注册
     */
    JwtResponse register(RegisterRequest request);

    /**
     * 用户登录
     */
    JwtResponse login(LoginRequest request);

    /**
     * 刷新令牌
     */
    JwtResponse refreshToken(String refreshToken);

    /**
     * 用户登出
     */
    void logout(String accessToken);

    /**
     * 验证邮箱
     */
    void verifyEmail(String token);

    /**
     * 发送邮箱验证码
     */
    void sendEmailVerification(String email);

    /**
     * 发送密码重置邮件
     */
    void sendPasswordResetEmail(String email);

    /**
     * 重置密码
     */
    void resetPassword(String token, String newPassword);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 启用双因素认证
     */
    String enableTwoFactor(Long userId);

    /**
     * 禁用双因素认证
     */
    void disableTwoFactor(Long userId, String code);

    /**
     * 验证双因素认证码
     */
    boolean verifyTwoFactorCode(Long userId, String code);

    /**
     * 检查用户名是否可用
     */
    boolean isUsernameAvailable(String username);

    /**
     * 检查邮箱是否可用
     */
    boolean isEmailAvailable(String email);
}
