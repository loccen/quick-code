package com.quickcode.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import com.quickcode.config.EmailProperties;
import com.quickcode.dto.auth.JwtResponse;
import com.quickcode.dto.auth.LoginRequest;
import com.quickcode.dto.auth.LoginResponse;
import com.quickcode.dto.auth.RegisterRequest;
import com.quickcode.entity.User;
import com.quickcode.security.jwt.JwtUtils;
import com.quickcode.service.UserService;
import com.quickcode.service.impl.AuthServiceImpl;

/**
 * AuthService单元测试
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService单元测试")
class AuthServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private JwtUtils jwtUtils;

  @Mock
  private EmailProperties emailProperties;

  @InjectMocks
  private AuthServiceImpl authService;

  private User testUser;
  private RegisterRequest registerRequest;
  private LoginRequest loginRequest;

  @BeforeEach
  void setUp() {
    testUser = User.builder().username("testuser").email("test@example.com")
        .password("$2a$10$encoded.password").nickname("测试用户").status(User.Status.ACTIVE.getCode())
        .emailVerified(true).twoFactorEnabled(false).lastLoginTime(LocalDateTime.now()).build();
    testUser.setId(1L); // 手动设置ID

    registerRequest = new RegisterRequest();
    registerRequest.setUsername("newuser");
    registerRequest.setEmail("new@example.com");
    registerRequest.setPassword("Password123!");
    registerRequest.setConfirmPassword("Password123!");
    registerRequest.setEmailCode("123456"); // 使用固定验证码
    registerRequest.setAgreeTerms(true); // 同意用户协议

    loginRequest = new LoginRequest();
    loginRequest.setUsernameOrEmail("testuser");
    loginRequest.setPassword("Password123!");
  }

  @Nested
  @DisplayName("用户注册测试")
  class RegisterTests {

    @Test
    @DisplayName("应该成功注册新用户")
    void shouldRegisterNewUserSuccessfully() {
      // Arrange
      String accessToken = "access.token.here";
      String refreshToken = "refresh.token.here";
      List<String> permissions = Arrays.asList("USER_READ", "USER_WRITE");

      when(emailProperties.getVerificationCode()).thenReturn("123456"); // Mock 邮箱验证码
      when(userService.register(registerRequest.getUsername(), registerRequest.getEmail(),
          registerRequest.getPassword())).thenReturn(testUser);
      when(jwtUtils.generateAccessToken(any(Authentication.class))).thenReturn(accessToken);
      when(jwtUtils.generateRefreshToken(any(Authentication.class))).thenReturn(refreshToken);
      when(userService.getUserPermissions(testUser.getId())).thenReturn(permissions);

      // Act
      JwtResponse result = authService.register(registerRequest);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getAccessToken()).isEqualTo(accessToken);
      assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
      assertThat(result.getTokenType()).isEqualTo("Bearer");
      assertThat(result.getExpiresIn()).isEqualTo(24 * 3600L);

      assertThat(result.getUser()).isNotNull();
      assertThat(result.getUser().getId()).isEqualTo(testUser.getId());
      assertThat(result.getUser().getUsername()).isEqualTo(testUser.getUsername());
      assertThat(result.getUser().getEmail()).isEqualTo(testUser.getEmail());
      assertThat(result.getUser().getPermissions()).isEqualTo(permissions);

      verify(userService).register(registerRequest.getUsername(), registerRequest.getEmail(),
          registerRequest.getPassword());
      verify(jwtUtils).generateAccessToken(any(Authentication.class));
      verify(jwtUtils).generateRefreshToken(any(Authentication.class));
      verify(userService, times(2)).getUserPermissions(testUser.getId()); // 调用两次：createAuthentication和buildUserInfo
    }

    @Test
    @DisplayName("密码不一致时应该抛出异常")
    void shouldThrowExceptionWhenPasswordsDoNotMatch() {
      // Arrange
      registerRequest.setConfirmPassword("DifferentPassword123!");

      // Act & Assert
      assertThatThrownBy(() -> authService.register(registerRequest))
          .isInstanceOf(com.quickcode.common.exception.AuthenticationFailedException.class)
          .hasMessage("密码和确认密码不一致");
    }
  }

  @Nested
  @DisplayName("用户登录测试")
  class LoginTests {

    @Test
    @DisplayName("应该成功登录用户")
    void shouldLoginUserSuccessfully() {
      // Arrange
      String accessToken = "access.token.here";
      String refreshToken = "refresh.token.here";
      List<String> permissions = Arrays.asList("USER_READ", "USER_WRITE");

      when(userService.login(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()))
          .thenReturn(testUser);
      when(jwtUtils.generateAccessToken(any(Authentication.class))).thenReturn(accessToken);
      when(jwtUtils.generateRefreshToken(any(Authentication.class))).thenReturn(refreshToken);
      when(userService.getUserPermissions(testUser.getId())).thenReturn(permissions);

      // Act
      LoginResponse result = authService.login(loginRequest);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.isRequiresTwoFactor()).isFalse();
      assertThat(result.getJwtResponse()).isNotNull();

      JwtResponse jwtResponse = result.getJwtResponse();
      assertThat(jwtResponse.getAccessToken()).isEqualTo(accessToken);
      assertThat(jwtResponse.getRefreshToken()).isEqualTo(refreshToken);
      assertThat(jwtResponse.getTokenType()).isEqualTo("Bearer");
      assertThat(jwtResponse.getExpiresIn()).isEqualTo(24 * 3600L);

      assertThat(jwtResponse.getUser()).isNotNull();
      assertThat(jwtResponse.getUser().getId()).isEqualTo(testUser.getId());
      assertThat(jwtResponse.getUser().getUsername()).isEqualTo(testUser.getUsername());
      assertThat(jwtResponse.getUser().getEmail()).isEqualTo(testUser.getEmail());
      assertThat(jwtResponse.getUser().getPermissions()).isEqualTo(permissions);

      verify(userService).login(loginRequest.getUsernameOrEmail(), loginRequest.getPassword());
      verify(jwtUtils).generateAccessToken(any(Authentication.class));
      verify(jwtUtils).generateRefreshToken(any(Authentication.class));
      verify(userService, times(2)).getUserPermissions(testUser.getId()); // 调用两次：createAuthentication和buildUserInfo
    }

    @Test
    @DisplayName("应该在用户启用2FA时返回需要2FA验证的响应")
    void shouldReturnTwoFactorRequiredWhenUserHas2FAEnabled() {
      // Arrange
      testUser.setTwoFactorEnabled(true);

      when(userService.login(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()))
          .thenReturn(testUser);

      // Act
      LoginResponse result = authService.login(loginRequest);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.isRequiresTwoFactor()).isTrue();
      assertThat(result.getTwoFactorResponse()).isNotNull();
      assertThat(result.getTwoFactorResponse().getUserId()).isEqualTo(testUser.getId());
      assertThat(result.getTwoFactorResponse().getMessage()).isEqualTo("请输入双因素认证验证码");
      assertThat(result.getTwoFactorResponse().isRequiresTwoFactor()).isTrue();

      verify(userService).login(loginRequest.getUsernameOrEmail(), loginRequest.getPassword());
      // 不应该生成JWT令牌
      verify(jwtUtils, never()).generateAccessToken(any(Authentication.class));
      verify(jwtUtils, never()).generateRefreshToken(any(Authentication.class));
    }
  }

  @Nested
  @DisplayName("用户名和邮箱可用性测试")
  class AvailabilityTests {

    @Test
    @DisplayName("应该检查用户名是否可用")
    void shouldCheckUsernameAvailability() {
      // Arrange
      String username = "newuser";
      when(userService.isUsernameAvailable(username)).thenReturn(true);

      // Act
      boolean result = authService.isUsernameAvailable(username);

      // Assert
      assertThat(result).isTrue();
      verify(userService).isUsernameAvailable(username);
    }

    @Test
    @DisplayName("应该检查邮箱是否可用")
    void shouldCheckEmailAvailability() {
      // Arrange
      String email = "new@example.com";
      when(userService.isEmailAvailable(email)).thenReturn(true);

      // Act
      boolean result = authService.isEmailAvailable(email);

      // Assert
      assertThat(result).isTrue();
      verify(userService).isEmailAvailable(email);
    }
  }

  @Nested
  @DisplayName("密码修改测试")
  class ChangePasswordTests {

    @Test
    @DisplayName("应该成功修改密码")
    void shouldChangePasswordSuccessfully() {
      // Arrange
      Long userId = 1L;
      String oldPassword = "OldPassword123!";
      String newPassword = "NewPassword123!";

      // Act
      authService.changePassword(userId, oldPassword, newPassword);

      // Assert
      verify(userService).changePassword(userId, oldPassword, newPassword);
    }
  }

  @Nested
  @DisplayName("邮箱验证测试")
  class EmailVerificationTests {

    @Test
    @DisplayName("应该成功发送邮箱验证码")
    void shouldSendEmailVerificationSuccessfully() {
      // Arrange
      String email = "test@example.com";
      User user = User.builder().email(email).emailVerified(false).build();
      user.setId(1L);

      when(userService.findByEmail(email)).thenReturn(Optional.of(user));
      when(emailProperties.getVerificationCode()).thenReturn("123456");
      when(emailProperties.getVerificationExpireMinutes()).thenReturn(30);

      // Act
      authService.sendEmailVerification(email);

      // Assert
      verify(userService).findByEmail(email);
      verify(userService).save(user);
      verify(emailProperties, times(2)).getVerificationCode(); // 调用两次：设置令牌和记录日志
      verify(emailProperties).getVerificationExpireMinutes();
    }

    @Test
    @DisplayName("邮箱不存在时应该抛出异常")
    void shouldThrowExceptionWhenEmailNotExists() {
      // Arrange
      String email = "nonexistent@example.com";
      when(userService.findByEmail(email)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> authService.sendEmailVerification(email))
          .isInstanceOf(com.quickcode.common.exception.ResourceNotFoundException.class)
          .hasMessage("用户 (邮箱: " + email + ") 不存在");
    }

    @Test
    @DisplayName("邮箱已验证时应该抛出异常")
    void shouldThrowExceptionWhenEmailAlreadyVerified() {
      // Arrange
      String email = "verified@example.com";
      User user = User.builder().email(email).emailVerified(true).build();

      when(userService.findByEmail(email)).thenReturn(Optional.of(user));

      // Act & Assert
      assertThatThrownBy(() -> authService.sendEmailVerification(email))
          .isInstanceOf(com.quickcode.common.exception.InvalidStateException.class).hasMessage("邮箱已验证，无需重复验证");
    }

    @Test
    @DisplayName("应该成功验证邮箱")
    void shouldVerifyEmailSuccessfully() {
      // Arrange
      String token = "123456";
      User user = User.builder().email("test@example.com").emailVerified(false)
          .emailVerificationToken(token)
          .emailVerificationExpiresAt(LocalDateTime.now().plusMinutes(30)).build();
      user.setId(1L);

      when(userService.findByEmailVerificationToken(token)).thenReturn(Optional.of(user));

      // Act
      authService.verifyEmail(token);

      // Assert
      assertThat(user.getEmailVerified()).isTrue();
      assertThat(user.getEmailVerificationToken()).isNull();
      assertThat(user.getEmailVerificationExpiresAt()).isNull();
      verify(userService).findByEmailVerificationToken(token);
      verify(userService).save(user);
    }

    @Test
    @DisplayName("无效令牌时应该抛出异常")
    void shouldThrowExceptionWhenTokenInvalid() {
      // Arrange
      String token = "invalid-token";
      when(userService.findByEmailVerificationToken(token)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> authService.verifyEmail(token))
          .isInstanceOf(com.quickcode.common.exception.AuthenticationFailedException.class)
          .hasMessage("无效的验证令牌");
    }

    @Test
    @DisplayName("令牌过期时应该抛出异常")
    void shouldThrowExceptionWhenTokenExpired() {
      // Arrange
      String token = "123456";
      User user = User.builder().email("test@example.com").emailVerified(false)
          .emailVerificationToken(token)
          .emailVerificationExpiresAt(LocalDateTime.now().minusMinutes(1)) // 已过期
          .build();

      when(userService.findByEmailVerificationToken(token)).thenReturn(Optional.of(user));

      // Act & Assert
      assertThatThrownBy(() -> authService.verifyEmail(token))
          .isInstanceOf(com.quickcode.common.exception.InvalidStateException.class).hasMessage("验证令牌已过期");
    }
  }

  @Nested
  @DisplayName("密码重置测试")
  class PasswordResetTests {

    @Test
    @DisplayName("应该成功发送密码重置邮件")
    void shouldSendPasswordResetEmailSuccessfully() {
      // Arrange
      String email = "test@example.com";
      User user = User.builder().email(email).build();
      user.setId(1L);

      when(userService.findByEmail(email)).thenReturn(Optional.of(user));
      when(emailProperties.getPasswordResetCode()).thenReturn("888888");
      when(emailProperties.getPasswordResetExpireMinutes()).thenReturn(15);

      // Act
      authService.sendPasswordResetEmail(email);

      // Assert
      verify(userService).findByEmail(email);
      verify(userService).save(user);
      verify(emailProperties, times(2)).getPasswordResetCode(); // 调用两次：设置令牌和记录日志
      verify(emailProperties).getPasswordResetExpireMinutes();
    }

    @Test
    @DisplayName("邮箱不存在时应该抛出异常")
    void shouldThrowExceptionWhenEmailNotExistsForPasswordReset() {
      // Arrange
      String email = "nonexistent@example.com";
      when(userService.findByEmail(email)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> authService.sendPasswordResetEmail(email))
          .isInstanceOf(com.quickcode.common.exception.ResourceNotFoundException.class)
          .hasMessage("用户 (邮箱: " + email + ") 不存在");
    }

    @Test
    @DisplayName("应该成功重置密码")
    void shouldResetPasswordSuccessfully() {
      // Arrange
      String token = "888888";
      String newPassword = "NewPassword123!";
      User user = User.builder().email("test@example.com").passwordResetToken(token)
          .passwordResetExpiresAt(LocalDateTime.now().plusMinutes(15)).build();
      user.setId(1L);

      when(userService.findByPasswordResetToken(token)).thenReturn(Optional.of(user));

      // Act
      authService.resetPassword(token, newPassword);

      // Assert
      assertThat(user.getPasswordResetToken()).isNull();
      assertThat(user.getPasswordResetExpiresAt()).isNull();
      verify(userService).findByPasswordResetToken(token);
      verify(userService).changePassword(user.getId(), null, newPassword);
      verify(userService).save(user);
    }

    @Test
    @DisplayName("无效重置令牌时应该抛出异常")
    void shouldThrowExceptionWhenResetTokenInvalid() {
      // Arrange
      String token = "invalid-token";
      when(userService.findByPasswordResetToken(token)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> authService.resetPassword(token, "NewPassword123!"))
          .isInstanceOf(com.quickcode.common.exception.AuthenticationFailedException.class)
          .hasMessage("无效的重置令牌");
    }

    @Test
    @DisplayName("重置令牌过期时应该抛出异常")
    void shouldThrowExceptionWhenResetTokenExpired() {
      // Arrange
      String token = "888888";
      User user = User.builder().email("test@example.com").passwordResetToken(token)
          .passwordResetExpiresAt(LocalDateTime.now().minusMinutes(1)) // 已过期
          .build();

      when(userService.findByPasswordResetToken(token)).thenReturn(Optional.of(user));

      // Act & Assert
      assertThatThrownBy(() -> authService.resetPassword(token, "NewPassword123!"))
          .isInstanceOf(com.quickcode.common.exception.InvalidStateException.class).hasMessage("重置令牌已过期");
    }
  }
}
