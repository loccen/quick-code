package com.quickcode.unit.service;

import com.quickcode.entity.User;
import com.quickcode.repository.UserRepository;
import com.quickcode.repository.RoleRepository;
import com.quickcode.service.impl.UserServiceImpl;
import com.quickcode.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService纯单元测试
 * 不依赖Spring上下文，使用Mockito进行Mock
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService纯单元测试")
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser();
        testUser.setId(1L); // 设置ID用于测试
    }

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
    @DisplayName("应该在用户名不存在时返回空Optional")
    void shouldReturnEmptyOptionalWhenUsernameNotFound() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByUsername(username);

        // Assert
        assertThat(result).isEmpty();

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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名已存在");

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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("邮箱已存在");

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

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
    @DisplayName("应该在用户不存在时抛出异常")
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        String usernameOrEmail = "nonexistent";
        String password = "Password123!";

        when(userRepository.findByUsernameOrEmail(usernameOrEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.login(usernameOrEmail, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");

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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("密码错误");

        verify(userRepository).findByUsernameOrEmail(username);
        verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    @DisplayName("应该成功更新用户信息")
    void shouldUpdateUserInfoSuccessfully() {
        // Arrange
        Long userId = testUser.getId();
        String newNickname = "新昵称";
        String newPhone = "13900139000";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}
