package com.quickcode.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.quickcode.entity.Permission;
import com.quickcode.entity.Role;
import com.quickcode.entity.User;
import com.quickcode.repository.RoleRepository;
import com.quickcode.repository.UserRepository;
import com.quickcode.service.impl.UserServiceImpl;
import com.quickcode.testutil.TestDataFactory;

/**
 * UserService单元测试 验证用户服务层的业务逻辑
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService单元测试")
class UserServiceTest {

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
  void setUp() {
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

  @Nested
  @DisplayName("角色权限相关测试")
  class RolePermissionTests {

    @Test
    @DisplayName("应该成功为用户分配角色")
    void shouldAssignRoleToUserSuccessfully() {
      // Arrange
      Long userId = testUser.getId();
      String roleCode = "USER";

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(roleRepository.findByRoleCode(roleCode)).thenReturn(Optional.of(testRole));
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      userService.assignRole(userId, roleCode);

      // Assert
      verify(userRepository).findById(userId);
      verify(roleRepository).findByRoleCode(roleCode);
      verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("应该在用户不存在时抛出异常")
    void shouldThrowExceptionWhenUserNotFoundForRoleAssignment() {
      // Arrange
      Long userId = 999L;
      String roleCode = "USER";

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.assignRole(userId, roleCode))
          .isInstanceOf(IllegalArgumentException.class).hasMessage("用户不存在: " + userId);

      verify(userRepository).findById(userId);
      verify(roleRepository, never()).findByRoleCode(anyString());
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("应该在角色不存在时抛出异常")
    void shouldThrowExceptionWhenRoleNotFoundForAssignment() {
      // Arrange
      Long userId = testUser.getId();
      String roleCode = "NONEXISTENT";

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(roleRepository.findByRoleCode(roleCode)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> userService.assignRole(userId, roleCode))
          .isInstanceOf(IllegalArgumentException.class).hasMessage("角色不存在: " + roleCode);

      verify(userRepository).findById(userId);
      verify(roleRepository).findByRoleCode(roleCode);
      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("应该成功移除用户角色")
    void shouldRemoveRoleFromUserSuccessfully() {
      // Arrange
      Long userId = testUser.getId();
      String roleCode = "USER";
      testUser.addRole(testRole); // 先添加角色

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(roleRepository.findByRoleCode(roleCode)).thenReturn(Optional.of(testRole));
      when(userRepository.save(any(User.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      userService.removeRole(userId, roleCode);

      // Assert
      verify(userRepository).findById(userId);
      verify(roleRepository).findByRoleCode(roleCode);
      verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("应该正确检查用户是否拥有角色")
    void shouldCheckUserHasRoleCorrectly() {
      // Arrange
      Long userId = testUser.getId();
      String roleCode = "USER";
      testUser.addRole(testRole); // 添加角色

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      // Act
      boolean hasRole = userService.hasRole(userId, roleCode);

      // Assert
      assertThat(hasRole).isTrue();
      verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("应该在用户没有角色时返回false")
    void shouldReturnFalseWhenUserDoesNotHaveRole() {
      // Arrange
      Long userId = testUser.getId();
      String roleCode = "ADMIN";

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      // Act
      boolean hasRole = userService.hasRole(userId, roleCode);

      // Assert
      assertThat(hasRole).isFalse();
      verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("应该正确检查用户是否拥有权限")
    void shouldCheckUserHasPermissionCorrectly() {
      // Arrange
      Long userId = testUser.getId();
      String permissionCode = "user:read";

      // 创建权限并添加到角色
      Permission permission = TestDataFactory.createTestPermission();
      permission.setPermissionCode(permissionCode);
      testRole.addPermission(permission);
      testUser.addRole(testRole);

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      // Act
      boolean hasPermission = userService.hasPermission(userId, permissionCode);

      // Assert
      assertThat(hasPermission).isTrue();
      verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("应该在用户没有权限时返回false")
    void shouldReturnFalseWhenUserDoesNotHavePermission() {
      // Arrange
      Long userId = testUser.getId();
      String permissionCode = "admin:manage";

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      // Act
      boolean hasPermission = userService.hasPermission(userId, permissionCode);

      // Assert
      assertThat(hasPermission).isFalse();
      verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("应该正确获取用户的所有权限")
    void shouldGetUserPermissionsCorrectly() {
      // Arrange
      Long userId = testUser.getId();

      // 创建多个权限并添加到角色
      Permission permission1 = TestDataFactory.createTestPermission();
      permission1.setPermissionCode("user:read");
      Permission permission2 = TestDataFactory.createTestPermission();
      permission2.setPermissionCode("user:update");

      testRole.addPermission(permission1);
      testRole.addPermission(permission2);
      testUser.addRole(testRole);

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      // Act
      List<String> permissions = userService.getUserPermissions(userId);

      // Assert
      assertThat(permissions).hasSize(2);
      assertThat(permissions).containsExactlyInAnyOrder("user:read", "user:update");
      verify(userRepository).findById(userId);
    }
  }

  @Nested
  @DisplayName("查询统计相关测试")
  class QueryStatisticsTests {

    @Test
    @DisplayName("应该能够根据角色查找用户")
    void shouldFindUsersByRole() {
      // Arrange
      String roleCode = "USER";
      List<User> expectedUsers = List.of(testUser);

      when(userRepository.findByRoleCode(roleCode)).thenReturn(expectedUsers);

      // Act
      List<User> result = userService.findByRole(roleCode);

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testUser.getId());
      verify(userRepository).findByRoleCode(roleCode);
    }

    @Test
    @DisplayName("应该能够根据状态查找用户")
    void shouldFindUsersByStatus() {
      // Arrange
      Integer status = 1;
      List<User> expectedUsers = List.of(testUser);

      when(userRepository.findByStatus(status)).thenReturn(expectedUsers);

      // Act
      List<User> result = userService.findByStatus(status);

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testUser.getId());
      verify(userRepository).findByStatus(status);
    }

    @Test
    @DisplayName("应该能够分页查找用户")
    void shouldFindUsersWithPagination() {
      // Arrange
      String keyword = "test";
      Integer status = 1;
      Pageable pageable = PageRequest.of(0, 10);
      Page<User> expectedPage = new PageImpl<>(List.of(testUser), pageable, 1);

      when(userRepository.findAll(pageable)).thenReturn(expectedPage);

      // Act
      Page<User> result = userService.findUsers(keyword, status, pageable);

      // Assert
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getId()).isEqualTo(testUser.getId());
      verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("应该能够查找新注册用户")
    void shouldFindNewUsers() {
      // Arrange
      LocalDateTime startTime = LocalDateTime.now().minusDays(7);
      List<User> expectedUsers = List.of(testUser);

      when(userRepository.findByCreatedTimeBetween(any(LocalDateTime.class),
          any(LocalDateTime.class))).thenReturn(expectedUsers);

      // Act
      List<User> result = userService.findNewUsers(startTime);

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testUser.getId());
      verify(userRepository).findByCreatedTimeBetween(any(LocalDateTime.class),
          any(LocalDateTime.class));
    }

    @Test
    @DisplayName("应该能够查找长时间未登录的用户")
    void shouldFindInactiveUsers() {
      // Arrange
      LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
      List<User> expectedUsers = List.of(testUser);

      when(userRepository.findInactiveUsers(cutoffTime)).thenReturn(expectedUsers);

      // Act
      List<User> result = userService.findInactiveUsers(cutoffTime);

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testUser.getId());
      verify(userRepository).findInactiveUsers(cutoffTime);
    }

    @Test
    @DisplayName("应该能够查找被锁定的用户")
    void shouldFindLockedUsers() {
      // Arrange
      List<User> expectedUsers = List.of(testUser);

      when(userRepository.findLockedUsers(any(LocalDateTime.class))).thenReturn(expectedUsers);

      // Act
      List<User> result = userService.findLockedUsers();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testUser.getId());
      verify(userRepository).findLockedUsers(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("应该能够统计用户总数")
    void shouldCountUsers() {
      // Arrange
      long expectedCount = 100L;

      when(userRepository.count()).thenReturn(expectedCount);

      // Act
      long result = userService.countUsers();

      // Assert
      assertThat(result).isEqualTo(expectedCount);
      verify(userRepository).count();
    }

    @Test
    @DisplayName("应该能够根据状态统计用户数量")
    void shouldCountUsersByStatus() {
      // Arrange
      Integer status = 1;
      long expectedCount = 50L;

      when(userRepository.countByStatus(status)).thenReturn(expectedCount);

      // Act
      long result = userService.countUsersByStatus(status);

      // Assert
      assertThat(result).isEqualTo(expectedCount);
      verify(userRepository).countByStatus(status);
    }

    @Test
    @DisplayName("应该能够统计新注册用户数量")
    void shouldCountNewUsers() {
      // Arrange
      LocalDateTime since = LocalDateTime.now().minusDays(7);
      long expectedCount = 10L;

      when(userRepository.countNewUsers(since)).thenReturn(expectedCount);

      // Act
      long result = userService.countNewUsers(since);

      // Assert
      assertThat(result).isEqualTo(expectedCount);
      verify(userRepository).countNewUsers(since);
    }

    @Test
    @DisplayName("应该能够统计已验证邮箱的用户数量")
    void shouldCountVerifiedUsers() {
      // Arrange
      long expectedCount = 80L;

      when(userRepository.countVerifiedUsers()).thenReturn(expectedCount);

      // Act
      long result = userService.countVerifiedUsers();

      // Assert
      assertThat(result).isEqualTo(expectedCount);
      verify(userRepository).countVerifiedUsers();
    }

    @Test
    @DisplayName("应该能够统计启用双因素认证的用户数量")
    void shouldCountTwoFactorEnabledUsers() {
      // Arrange
      long expectedCount = 20L;

      when(userRepository.countTwoFactorEnabledUsers()).thenReturn(expectedCount);

      // Act
      long result = userService.countTwoFactorEnabledUsers();

      // Assert
      assertThat(result).isEqualTo(expectedCount);
      verify(userRepository).countTwoFactorEnabledUsers();
    }
  }
}
