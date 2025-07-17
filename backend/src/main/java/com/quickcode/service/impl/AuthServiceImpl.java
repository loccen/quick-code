package com.quickcode.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import com.quickcode.config.EmailProperties;
import com.quickcode.dto.auth.JwtResponse;
import com.quickcode.dto.auth.LoginRequest;
import com.quickcode.dto.auth.LoginResponse;
import com.quickcode.dto.auth.RegisterRequest;
import com.quickcode.dto.auth.TwoFactorLoginRequest;
import com.quickcode.dto.auth.TwoFactorRequiredResponse;
import com.quickcode.entity.User;
import com.quickcode.security.jwt.JwtUtils;
import com.quickcode.security.jwt.UserPrincipal;
import com.quickcode.service.AuthService;
import com.quickcode.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务实现类 提供用户认证相关的业务逻辑实现
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserService userService;
  private final JwtUtils jwtUtils;
  private final EmailProperties emailProperties;

  @Override
  public JwtResponse register(RegisterRequest request) {
    log.debug("开始用户注册: username={}, email={}", request.getUsername(), request.getEmail());

    // 验证密码是否一致
    if (!request.isPasswordMatched()) {
      throw com.quickcode.common.exception.AuthenticationFailedException.passwordMismatch();
    }

    // 验证是否同意用户协议和隐私政策
    if (!request.isAgreeTerms()) {
      throw com.quickcode.common.exception.AuthenticationFailedException.termsNotAgreed();
    }

    // 验证邮箱验证码
    validateEmailVerificationCode(request.getEmail(), request.getEmailCode());

    // 调用UserService进行注册
    User user =
        userService.register(request.getUsername(), request.getEmail(), request.getPassword());

    // 创建认证对象
    Authentication authentication = createAuthentication(user);

    // 生成JWT令牌
    String accessToken = jwtUtils.generateAccessToken(authentication);
    String refreshToken = jwtUtils.generateRefreshToken(authentication);

    // 构建用户信息
    JwtResponse.UserInfo userInfo = buildUserInfo(user);

    log.info("用户注册成功: username={}, id={}", user.getUsername(), user.getId());

    return JwtResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
        .expiresIn(24 * 3600L) // 24小时过期
        .user(userInfo).build();
  }

  @Override
  public LoginResponse login(LoginRequest request) {
    log.debug("用户登录尝试: {}", request.getUsernameOrEmail());

    // 调用UserService进行登录验证
    User user = userService.login(request.getUsernameOrEmail(), request.getPassword());

    // 检查是否启用了2FA
    if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
      log.info("用户启用了2FA，需要进行二次验证: userId={}", user.getId());

      TwoFactorRequiredResponse twoFactorResponse = TwoFactorRequiredResponse.builder()
          .userId(user.getId())
          .message("请输入双因素认证验证码")
          .requiresTwoFactor(true)
          .build();

      return LoginResponse.twoFactor(twoFactorResponse);
    }

    // 创建认证对象
    Authentication authentication = createAuthentication(user);

    // 生成JWT令牌
    String accessToken = jwtUtils.generateAccessToken(authentication);
    String refreshToken = jwtUtils.generateRefreshToken(authentication);

    // 构建用户信息
    JwtResponse.UserInfo userInfo = buildUserInfo(user);

    log.info("用户登录成功: username={}, id={}", user.getUsername(), user.getId());

    JwtResponse jwtResponse = JwtResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
        .expiresIn(24 * 3600L) // 24小时过期
        .user(userInfo).build();

    return LoginResponse.jwt(jwtResponse);
  }

  @Override
  public JwtResponse loginWithTwoFactor(TwoFactorLoginRequest request) {
    log.debug("双因素认证登录验证: userId={}", request.getUserId());

    // 获取用户信息
    User user = userService.getById(request.getUserId());

    // 验证用户是否启用了2FA
    if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
      throw com.quickcode.common.exception.InvalidStateException
          .withCode(5008, "双因素认证未启用");
    }

    // 验证TOTP代码
    boolean isValid = userService.verifyTwoFactorCode(request.getUserId(), request.getTotpCode());
    if (!isValid) {
      throw com.quickcode.common.exception.AuthenticationFailedException
          .invalidCredentials("验证码错误");
    }

    // 创建认证对象
    Authentication authentication = createAuthentication(user);

    // 生成JWT令牌
    String accessToken = jwtUtils.generateAccessToken(authentication);
    String refreshToken = jwtUtils.generateRefreshToken(authentication);

    // 构建用户信息
    JwtResponse.UserInfo userInfo = buildUserInfo(user);

    log.info("双因素认证登录成功: username={}, id={}", user.getUsername(), user.getId());

    return JwtResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
        .expiresIn(24 * 3600L) // 24小时过期
        .user(userInfo).build();
  }

  @Override
  public JwtResponse refreshToken(String refreshToken) {
    // TODO: 实现令牌刷新逻辑
    throw com.quickcode.common.exception.InvalidStateException
        .withCode(5005, "令牌刷新功能尚未实现");
  }

  @Override
  public void logout(String accessToken) {
    // TODO: 实现用户登出逻辑
    log.info("用户登出");
  }

  @Override
  public void verifyEmail(String token) {
    log.debug("验证邮箱: token={}", token);

    // 查找具有该验证令牌的用户
    User user = userService.findByEmailVerificationToken(token)
        .orElseThrow(() -> com.quickcode.common.exception.AuthenticationFailedException
            .invalidEmailCode("无效的验证令牌"));

    // 检查令牌是否过期
    if (user.getEmailVerificationExpiresAt() == null
        || user.getEmailVerificationExpiresAt().isBefore(LocalDateTime.now())) {
      throw com.quickcode.common.exception.InvalidStateException.verificationTokenExpired();
    }

    // 验证邮箱
    user.setEmailVerified(true);
    user.clearEmailVerificationToken();

    // 保存用户
    userService.save(user);

    log.info("邮箱验证成功: userId={}, email={}", user.getId(), user.getEmail());
  }

  @Override
  public void sendEmailVerification(String email) {
    log.debug("发送邮箱验证码: email={}", email);

    // 查找用户
    User user = userService.findByEmail(email)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException
            .userByEmail(email));

    // 检查邮箱是否已验证
    if (user.getEmailVerified()) {
      throw com.quickcode.common.exception.InvalidStateException.emailAlreadyVerified();
    }

    // 生成验证令牌（开发和测试环境使用固定验证码）
    user.setEmailVerificationToken(emailProperties.getVerificationCode());
    user.setEmailVerificationExpiresAt(
        LocalDateTime.now().plusMinutes(emailProperties.getVerificationExpireMinutes()));

    // 保存用户
    userService.save(user);

    log.info("邮箱验证码发送成功: email={}, code={}", email, emailProperties.getVerificationCode());
  }

  @Override
  public void sendPasswordResetEmail(String email) {
    log.debug("发送密码重置邮件: email={}", email);

    // 查找用户
    User user = userService.findByEmail(email)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException
            .userByEmail(email));

    // 生成密码重置令牌（开发和测试环境使用固定验证码）
    user.setPasswordResetToken(emailProperties.getPasswordResetCode());
    user.setPasswordResetExpiresAt(
        LocalDateTime.now().plusMinutes(emailProperties.getPasswordResetExpireMinutes()));

    // 保存用户
    userService.save(user);

    log.info("密码重置邮件发送成功: email={}, code={}", email, emailProperties.getPasswordResetCode());
  }

  @Override
  public void resetPassword(String token, String newPassword) {
    log.debug("重置密码: token={}", token);

    // 查找具有该重置令牌的用户
    User user = userService.findByPasswordResetToken(token)
        .orElseThrow(() -> com.quickcode.common.exception.AuthenticationFailedException
            .invalidEmailCode("无效的重置令牌"));

    // 检查令牌是否过期
    if (user.getPasswordResetExpiresAt() == null
        || user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
      throw com.quickcode.common.exception.InvalidStateException
          .verificationTokenExpired("重置令牌已过期");
    }

    // 重置密码
    userService.changePassword(user.getId(), null, newPassword);

    // 清除重置令牌
    user.clearPasswordResetToken();
    userService.save(user);

    log.info("密码重置成功: userId={}, email={}", user.getId(), user.getEmail());
  }

  @Override
  public void changePassword(Long userId, String oldPassword, String newPassword) {
    log.debug("用户修改密码: userId={}", userId);

    // 调用UserService进行密码修改
    userService.changePassword(userId, oldPassword, newPassword);

    log.info("用户密码修改成功: userId={}", userId);
  }

  @Override
  public String enableTwoFactor(Long userId) {
    // TODO: 实现启用双因素认证逻辑
    throw com.quickcode.common.exception.InvalidStateException
        .withCode(5005, "启用双因素认证功能尚未实现");
  }

  @Override
  public void disableTwoFactor(Long userId, String code) {
    // TODO: 实现禁用双因素认证逻辑
    throw com.quickcode.common.exception.InvalidStateException
        .withCode(5005, "禁用双因素认证功能尚未实现");
  }

  @Override
  public boolean verifyTwoFactorCode(Long userId, String code) {
    // TODO: 实现验证双因素认证码逻辑
    throw com.quickcode.common.exception.InvalidStateException
        .withCode(5005, "验证双因素认证码功能尚未实现");
  }

  @Override
  public boolean isUsernameAvailable(String username) {
    return userService.isUsernameAvailable(username);
  }

  @Override
  public boolean isEmailAvailable(String email) {
    return userService.isEmailAvailable(email);
  }

  /**
   * 验证邮箱验证码（注册时使用）
   */
  private void validateEmailVerificationCode(String email, String code) {
    log.debug("验证邮箱验证码: email={}, code={}", email, code);

    // 在开发和测试环境中，直接验证固定验证码
    if (!emailProperties.getVerificationCode().equals(code)) {
      throw com.quickcode.common.exception.AuthenticationFailedException.invalidEmailCode();
    }

    log.debug("邮箱验证码验证成功: email={}", email);
  }

  /**
   * 创建认证对象
   */
  private Authentication createAuthentication(User user) {
    UserPrincipal userPrincipal = UserPrincipal.create(user);

    // 获取用户权限
    List<String> permissions = userService.getUserPermissions(user.getId());
    List<SimpleGrantedAuthority> authorities =
        permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

    return new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
  }

  /**
   * 构建用户信息
   */
  private JwtResponse.UserInfo buildUserInfo(User user) {
    List<String> permissions = userService.getUserPermissions(user.getId());

    return JwtResponse.UserInfo.builder().id(user.getId()).username(user.getUsername())
        .email(user.getEmail()).nickname(user.getNickname()).avatarUrl(user.getAvatarUrl())
        .status(user.getStatus()).emailVerified(user.getEmailVerified())
        .twoFactorEnabled(user.getTwoFactorEnabled()).lastLoginTime(user.getLastLoginTime())
        .permissions(permissions).build();
  }
}
