package com.quickcode.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.quickcode.common.response.ApiResponse;
import com.quickcode.dto.auth.JwtResponse;
import com.quickcode.dto.auth.LoginRequest;
import com.quickcode.dto.auth.LoginResponse;
import com.quickcode.dto.auth.RegisterRequest;
import com.quickcode.dto.auth.TwoFactorLoginRequest;
import com.quickcode.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证控制器
 * 处理用户认证相关的HTTP请求
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求: {}", request.getUsername());

        if (!request.isPasswordMatched()) {
            return error("密码和确认密码不一致");
        }

        JwtResponse response = authService.register(request);
        return success(response, "注册成功");
    }

    /**
     * 用户登录（第一步：用户名密码验证）
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                           HttpServletRequest httpRequest) {
        log.info("用户登录请求: {}", request.getUsernameOrEmail());

        LoginResponse response = authService.login(request);

        if (response.isRequiresTwoFactor()) {
            return success(response, "需要双因素认证验证");
        } else {
            return success(response, "登录成功");
        }
    }

    /**
     * 双因素认证登录（第二步：TOTP验证码验证）
     */
    @PostMapping("/login/2fa")
    public ApiResponse<JwtResponse> loginWithTwoFactor(@Valid @RequestBody TwoFactorLoginRequest request) {
        log.info("双因素认证登录请求: userId={}", request.getUserId());

        JwtResponse response = authService.loginWithTwoFactor(request);
        return success(response, "登录成功");
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public ApiResponse<JwtResponse> refreshToken(@RequestParam String refreshToken) {
        log.info("刷新令牌请求");

        JwtResponse response = authService.refreshToken(refreshToken);
        return success(response, "令牌刷新成功");
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            authService.logout(token);
        }

        return success(null, "登出成功");
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        boolean available = authService.isUsernameAvailable(username);
        return success(available, available ? "用户名可用" : "用户名已被使用");
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        boolean available = authService.isEmailAvailable(email);
        return success(available, available ? "邮箱可用" : "邮箱已被使用");
    }

    /**
     * 发送邮箱验证码（注册前）
     */
    @PostMapping("/send-email-verification")
    public ApiResponse<Void> sendEmailVerification(@RequestParam String email) {
        // 在开发环境中，直接返回成功，不实际发送邮件
        log.info("发送邮箱验证码: email={}, 开发环境固定验证码: 123456", email);
        return success(null, "验证码已发送，开发环境验证码：123456");
    }

    /**
     * 验证邮箱
     */
    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return success(null, "邮箱验证成功");
    }

    /**
     * 发送密码重置邮件
     */
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestParam String email) {
        authService.sendPasswordResetEmail(email);
        return success(null, "密码重置邮件已发送");
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestParam String token,
                                          @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return success(null, "密码重置成功");
    }

    /**
     * 从请求中提取JWT令牌
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
