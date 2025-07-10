package com.quickcode.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.quickcode.entity.Role;
import com.quickcode.entity.User;
import com.quickcode.repository.RoleRepository;
import com.quickcode.repository.UserRepository;
import com.quickcode.service.impl.UserServiceImpl;
import com.quickcode.testutil.BaseUnitTest;
import com.quickcode.testutil.TestDataFactory;

/**
 * UserService单元测试 验证用户服务层的业务逻辑
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@DisplayName("UserService单元测试")
class UserServiceTest extends BaseUnitTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserServiceImpl userService;

  private User testUser;
  private Role testRole;

  @BeforeEach
  @Override
  protected void setupUnitTest() {
    super.setupUnitTest();
    testUser = TestDataFactory.createTestUser();
    testUser.setId(1L); // 设置ID用于测试
    testRole = TestDataFactory.createTestRole();
    testRole.setId(1L); // 设置ID用于测试
  }

  @Nested
  @DisplayName("用户注册相关测试")
  class UserRegistrationTests {

    @Test
    @DisplayName("应该成功注册新用户")
    void shouldRegisterNewUserSuccessfully() {
      // Arrange
      String username = "newuser";
      String email = "newuser@example.com";
      String password = "Password123!";
      String encodedPassword = "$2a$10$encoded.password";

      when(userRepository.existsByUsername(username)).thenReturn(false);
      when(userRepository.existsByEmail(email)).thenReturn(false);
      when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
      when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
        User user = invocation.getArgument(0);
        user.setId(1L);
        return user;
      });

      // Act
      User result = userService.register(username, email, password);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo(username);
      assertThat(result.getEmail()).isEqualTo(email);
      assertThat(result.getPassword()).isEqualTo(encodedPassword);
      assertThat(result.getStatus()).isEqualTo(User.Status.PENDING.getCode());
      assertThat(result.getEmailVerified()).isFalse();

      verify(userRepository).existsByUsername(username);
      verify(userRepository).existsByEmail(email);
      verify(passwordEncoder).encode(password);
      verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("应该在用户名已存在时抛出异常")
    void shouldThrowExceptionWhenUsernameExists() {
      // Arrange
      String username = "existinguser";
      String email = "new@example.com";
      String password = "Password123!";

      when(userRepository.existsByUsername(username)).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> userService.register(username, email, password))
          .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("用户名已存在");

      verify(userRepository).existsByUsername(username);
      verify(userRepository, never()).existsByEmail(anyString());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("应该在邮箱已存在时抛出异常")
    void shouldThrowExceptionWhenEmailExists() {
      // Arrange
      String username = "newuser";
      String email = "existing@example.com";
      String password = "Password123!";

      when(userRepository.existsByUsername(username)).thenReturn(false);
      when(userRepository.existsByEmail(email)).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> userService.register(username, email, password))
          .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("邮箱已存在");

      verify(userRepository).existsByUsername(username);
      verify(userRepository).existsByEmail(email);
      verify(userRepository, never()).save(any(User.class));
    }
  }

  @Nested
  @DisplayName("用户登录相关测试")
  class UserLoginTests {

    @Test
    @DisplayName("应该成功使用用户名登录")
    void shouldLoginWithUsernameSuccessfully() {
      // Arrange
      String username = testUser.getUsername();
      String password = "Password123!";
      String encodedPassword = "$2a$10$encoded.password";
      testUser.setPassword(encodedPassword);
      testUser.setStatus(User.Status.ACTIVE.getCode());

      when(userRepository.findByUsernameOrEmail(username)).thenReturn(Optional.of(testUser));
      when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

      // Act
      User result = userService.login(username, password);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo(username);

      verify(userRepository).findByUsernameOrEmail(username);
      verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    @DisplayName("应该成功使用邮箱登录")
    void shouldLoginWithEmailSuccessfully() {
      // Arrange
      String email = testUser.getEmail();
      String password = "Password123!";
      String encodedPassword = "$2a$10$encoded.password";
      testUser.setPassword(encodedPassword);
      testUser.setStatus(User.Status.ACTIVE.getCode());

      when(userRepository.findByUsernameOrEmail(email)).thenReturn(Optional.of(testUser));
      when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

      // Act
      User result = userService.login(email, password);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getEmail()).isEqualTo(email);

      verify(userRepository).findByUsernameOrEmail(email);
      verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    @DisplayName("应该在用户不存在时抛出异常")
    void shouldThrowExceptionWhenUserNotFound() {
      // Arrange
      String usernameOrEmail = "nonexistent";
      String password = "Password123!";

      when(userRepository.findByUsernameOrEmail(usernameOrEmail)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.login(usernameOrEmail, password))
          .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("用户不存在");

      verify(userRepository).findByUsernameOrEmail(usernameOrEmail);
      verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("应该在密码错误时抛出异常")
    void shouldThrowExceptionWhenPasswordIncorrect() {
      // Arrange
      String username = testUser.getUsername();
      String password = "WrongPassword";
      String encodedPassword = "$2a$10$encoded.password";
      testUser.setPassword(encodedPassword);

      when(userRepository.findByUsernameOrEmail(username)).thenReturn(Optional.of(testUser));
      when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

      // Act & Assert
      assertThatThrownBy(() -> userService.login(username, password))
          .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("密码错误");

      verify(userRepository).findByUsernameOrEmail(username);
      verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    @DisplayName("应该在用户被禁用时抛出异常")
    void shouldThrowExceptionWhenUserDisabled() {
      // Arrange
      String username = testUser.getUsername();
      String password = "Password123!";
      String encodedPassword = "$2a$10$encoded.password";
      testUser.setPassword(encodedPassword);
      testUser.setStatus(User.Status.DISABLED.getCode());

      when(userRepository.findByUsernameOrEmail(username)).thenReturn(Optional.of(testUser));
      when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> userService.login(username, password))
          .isInstanceOf(IllegalStateException.class).hasMessageContaining("用户已被禁用");

      verify(userRepository).findByUsernameOrEmail(username);
      verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    @DisplayName("应该在用户被锁定时抛出异常")
    void shouldThrowExceptionWhenUserLocked() {
      // Arrange
      String username = testUser.getUsername();
      String password = "Password123!";
      String encodedPassword = "$2a$10$encoded.password";
      testUser.setPassword(encodedPassword);
      testUser.setStatus(User.Status.ACTIVE.getCode());
      testUser.setLockedUntil(LocalDateTime.now().plusMinutes(30));

      when(userRepository.findByUsernameOrEmail(username)).thenReturn(Optional.of(testUser));
      when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> userService.login(username, password))
          .isInstanceOf(IllegalStateException.class).hasMessageContaining("用户已被锁定");

      verify(userRepository).findByUsernameOrEmail(username);
      verify(passwordEncoder).matches(password, encodedPassword);
    }
  }

  @Nested
  @DisplayName("用户查询相关测试")
  class UserQueryTests {

    @Test
    @DisplayName("应该能够根据用户名查找用户")
    void shouldFindUserByUsername() {
      // Arrange
      String username = testUser.getUsername();
      when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

      // Act
      Optional<User> result = userService.findByUsername(username);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getUsername()).isEqualTo(username);

      verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("应该能够根据邮箱查找用户")
    void shouldFindUserByEmail() {
      // Arrange
      String email = testUser.getEmail();
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

      // Act
      Optional<User> result = userService.findByEmail(email);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getEmail()).isEqualTo(email);

      verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("应该能够根据用户名或邮箱查找用户")
    void shouldFindUserByUsernameOrEmail() {
      // Arrange
      String usernameOrEmail = testUser.getUsername();
      when(userRepository.findByUsernameOrEmail(usernameOrEmail)).thenReturn(Optional.of(testUser));

      // Act
      Optional<User> result = userService.findByUsernameOrEmail(usernameOrEmail);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getUsername()).isEqualTo(usernameOrEmail);

      verify(userRepository).findByUsernameOrEmail(usernameOrEmail);
    }

    @Test
    @DisplayName("应该正确检查用户名是否可用")
    void shouldCheckUsernameAvailability() {
      // Arrange
      String availableUsername = "available";
      String unavailableUsername = "unavailable";

      when(userRepository.existsByUsername(availableUsername)).thenReturn(false);
      when(userRepository.existsByUsername(unavailableUsername)).thenReturn(true);

      // Act & Assert
      assertThat(userService.isUsernameAvailable(availableUsername)).isTrue();
      assertThat(userService.isUsernameAvailable(unavailableUsername)).isFalse();

      verify(userRepository).existsByUsername(availableUsername);
      verify(userRepository).existsByUsername(unavailableUsername);
    }

    @Test
    @DisplayName("应该正确检查邮箱是否可用")
    void shouldCheckEmailAvailability() {
      // Arrange
      String availableEmail = "available@example.com";
      String unavailableEmail = "unavailable@example.com";

      when(userRepository.existsByEmail(availableEmail)).thenReturn(false);
      when(userRepository.existsByEmail(unavailableEmail)).thenReturn(true);

      // Act & Assert
      assertThat(userService.isEmailAvailable(availableEmail)).isTrue();
      assertThat(userService.isEmailAvailable(unavailableEmail)).isFalse();

      verify(userRepository).existsByEmail(availableEmail);
      verify(userRepository).existsByEmail(unavailableEmail);
    }
  }

  @Nested
  @DisplayName("用户信息更新相关测试")
  class UserUpdateTests {

    @Test
    @DisplayName("应该成功更新用户信息")
    void shouldUpdateUserInfoSuccessfully() {
      // Arrange
      Long userId = testUser.getId();
      String newNickname = "新昵称";
      String newPhone = "13900139000";

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      User result = userService.updateUserInfo(userId, newNickname, newPhone);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getNickname()).isEqualTo(newNickname);
      assertThat(result.getPhone()).isEqualTo(newPhone);

      verify(userRepository).findById(userId);
      verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("应该在用户不存在时抛出异常")
    void shouldThrowExceptionWhenUserNotFoundForUpdate() {
      // Arrange
      Long userId = 999L;
      String newNickname = "新昵称";
      String newPhone = "13900139000";

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.updateUserInfo(userId, newNickname, newPhone))
          .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("用户不存在");

      verify(userRepository).findById(userId);
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("应该成功更新用户头像")
    void shouldUpdateAvatarSuccessfully() {
      // Arrange
      Long userId = testUser.getId();
      String newAvatarUrl = "https://example.com/avatar.jpg";

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      User result = userService.updateAvatar(userId, newAvatarUrl);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getAvatarUrl()).isEqualTo(newAvatarUrl);

      verify(userRepository).findById(userId);
      verify(userRepository).save(testUser);
    }
  }

  @Nested
  @DisplayName("密码相关测试")
  class PasswordTests {

    @Test
    @DisplayName("应该成功修改密码")
    void shouldChangePasswordSuccessfully() {
      // Arrange
      Long userId = testUser.getId();
      String oldPassword = "OldPassword123!";
      String newPassword = "NewPassword123!";
      String encodedOldPassword = "$2a$10$encoded.old.password";
      String encodedNewPassword = "$2a$10$encoded.new.password";

      testUser.setPassword(encodedOldPassword);

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(true);
      when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      userService.changePassword(userId, oldPassword, newPassword);

      // Assert
      assertThat(testUser.getPassword()).isEqualTo(encodedNewPassword);

      verify(userRepository).findById(userId);
      verify(passwordEncoder).matches(oldPassword, encodedOldPassword);
      verify(passwordEncoder).encode(newPassword);
      verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("应该在旧密码错误时抛出异常")
    void shouldThrowExceptionWhenOldPasswordIncorrect() {
      // Arrange
      Long userId = testUser.getId();
      String oldPassword = "WrongPassword";
      String newPassword = "NewPassword123!";
      String encodedOldPassword = "$2a$10$encoded.old.password";

      testUser.setPassword(encodedOldPassword);

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(false);

      // Act & Assert
      assertThatThrownBy(() -> userService.changePassword(userId, oldPassword, newPassword))
          .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("原密码错误");

      verify(userRepository).findById(userId);
      verify(passwordEncoder).matches(oldPassword, encodedOldPassword);
      verify(passwordEncoder, never()).encode(anyString());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("应该成功重置密码")
    void shouldResetPasswordSuccessfully() {
      // Arrange
      String email = testUser.getEmail();
      String newPassword = "NewPassword123!";
      String encodedNewPassword = "$2a$10$encoded.new.password";

      when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
      when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      userService.resetPassword(email, newPassword);

      // Assert
      assertThat(testUser.getPassword()).isEqualTo(encodedNewPassword);

      verify(userRepository).findByEmail(email);
      verify(passwordEncoder).encode(newPassword);
      verify(userRepository).save(testUser);
    }
  }

  @Nested
  @DisplayName("用户状态管理相关测试")
  class UserStatusTests {

    @Test
    @DisplayName("应该成功锁定用户")
    void shouldLockUserSuccessfully() {
      // Arrange
      Long userId = testUser.getId();
      LocalDateTime lockUntil = LocalDateTime.now().plusHours(1);

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      userService.lockUser(userId, lockUntil);

      // Assert
      assertThat(testUser.getLockedUntil()).isEqualTo(lockUntil);

      verify(userRepository).findById(userId);
      verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("应该成功解锁用户")
    void shouldUnlockUserSuccessfully() {
      // Arrange
      Long userId = testUser.getId();
      testUser.setLockedUntil(LocalDateTime.now().plusHours(1));

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      userService.unlockUser(userId);

      // Assert
      assertThat(testUser.getLockedUntil()).isNull();

      verify(userRepository).findById(userId);
      verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("应该成功禁用用户")
    void shouldDisableUserSuccessfully() {
      // Arrange
      Long userId = testUser.getId();

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      userService.disableUser(userId);

      // Assert
      assertThat(testUser.getStatus()).isEqualTo(User.Status.DISABLED.getCode());

      verify(userRepository).findById(userId);
      verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("应该成功启用用户")
    void shouldEnableUserSuccessfully() {
      // Arrange
      Long userId = testUser.getId();
      testUser.setStatus(User.Status.DISABLED.getCode());

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      userService.enableUser(userId);

      // Assert
      assertThat(testUser.getStatus()).isEqualTo(User.Status.ACTIVE.getCode());

      verify(userRepository).findById(userId);
      verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("应该成功验证邮箱")
    void shouldVerifyEmailSuccessfully() {
      // Arrange
      Long userId = testUser.getId();
      testUser.setEmailVerified(false);

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      userService.verifyEmail(userId);

      // Assert
      assertThat(testUser.getEmailVerified()).isTrue();

      verify(userRepository).findById(userId);
      verify(userRepository).save(testUser);
    }
  }
}
