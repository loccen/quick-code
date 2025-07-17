package com.quickcode.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickcode.controller.UserController;
import com.quickcode.dto.user.ChangePasswordRequest;
import com.quickcode.dto.user.UserInfoRequest;
import com.quickcode.entity.User;
import com.quickcode.security.jwt.JwtUtils;
import com.quickcode.security.jwt.UserPrincipal;
import com.quickcode.security.service.CustomUserDetailsService;
import com.quickcode.service.UserService;
import com.quickcode.testutil.TestDataFactory;
import com.quickcode.testutil.TestSecurityConfig;

/**
 * UserController单元测试 使用MockMvc测试REST API端点
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
@EnableMethodSecurity(prePostEnabled = true)
@DisplayName("UserController单元测试")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @MockBean
  private JwtUtils jwtUtils;

  @MockBean
  private CustomUserDetailsService userDetailsService;

  @Autowired
  private ObjectMapper objectMapper;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = TestDataFactory.createTestUser();
    testUser.setId(1L);
  }

  @Nested
  @DisplayName("用户信息相关测试")
  class UserProfileTests {

    @Test
    @DisplayName("应该成功获取当前用户信息")
    void shouldGetCurrentUserProfileSuccessfully() throws Exception {
      // Arrange
      UserPrincipal userPrincipal = TestSecurityConfig.createTestUserPrincipal();
      when(userService.getById(1L)).thenReturn(testUser);

      // Act & Assert
      mockMvc
          .perform(get("/api/users/profile").with(SecurityMockMvcRequestPostProcessors
              .authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null,
                  userPrincipal.getAuthorities()))))
          .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("获取用户信息成功"))
          .andExpect(jsonPath("$.data.username").value(testUser.getUsername()))
          .andExpect(jsonPath("$.data.email").value(testUser.getEmail()));

      verify(userService).getById(1L);
    }

    @Test
    @DisplayName("应该在未认证时返回401")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
      // Act & Assert
      mockMvc.perform(get("/api/users/profile")).andExpect(status().isUnauthorized());

      verify(userService, never()).getById(anyLong());
    }

    @Test
    @DisplayName("应该成功更新当前用户信息")
    void shouldUpdateCurrentUserProfileSuccessfully() throws Exception {
      // Arrange
      UserPrincipal userPrincipal = TestSecurityConfig.createTestUserPrincipal();
      UserInfoRequest request =
          UserInfoRequest.builder()
              .nickname("新昵称")
              .bio("这是我的个人简介")
              .avatar("https://example.com/avatar.jpg")
              .build();

      User updatedUser = TestDataFactory.createTestUser();
      updatedUser.setId(1L);
      updatedUser.setNickname(request.getNickname());
      updatedUser.setBio(request.getBio());
      updatedUser.setAvatarUrl(request.getAvatar());

      when(userService.updateUserInfo(1L, request.getNickname(), request.getBio(), request.getAvatar()))
          .thenReturn(updatedUser);

      // Act & Assert
      mockMvc
          .perform(put("/api/users/profile")
              .with(SecurityMockMvcRequestPostProcessors
                  .authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null,
                      userPrincipal.getAuthorities())))
              .with(SecurityMockMvcRequestPostProcessors.csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("更新用户信息成功"))
          .andExpect(jsonPath("$.data.nickname").value(request.getNickname()))
          .andExpect(jsonPath("$.data.bio").value(request.getBio()))
          .andExpect(jsonPath("$.data.avatarUrl").value(request.getAvatar()));

      verify(userService).updateUserInfo(1L, request.getNickname(), request.getBio(), request.getAvatar());
    }

    @Test
    @DisplayName("应该在昵称过长时返回400")
    void shouldReturn400WhenNicknameTooLong() throws Exception {
      // Arrange
      UserPrincipal userPrincipal = TestSecurityConfig.createTestUserPrincipal();
      // 创建一个确实超过50个字符的昵称（51个字符）
      String longNickname = "这是一个非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常长的昵称";
      UserInfoRequest request = UserInfoRequest.builder().nickname(longNickname).build();

      // 这个测试应该在参数验证阶段就失败，不会调用UserService
      // 但为了防止意外调用，我们提供一个Mock
      when(userService.updateUserInfo(anyLong(), anyString(), anyString(), anyString())).thenReturn(testUser);

      // Act & Assert
      mockMvc
          .perform(put("/api/users/profile")
              .with(SecurityMockMvcRequestPostProcessors
                  .authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null,
                      userPrincipal.getAuthorities())))
              .with(SecurityMockMvcRequestPostProcessors.csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      // 验证参数验证在UserService调用之前就拦截了请求
      verify(userService, never()).updateUserInfo(anyLong(), anyString(), anyString(), anyString());
    }
  }

  @Nested
  @DisplayName("密码相关测试")
  class PasswordTests {

    @Test
    @DisplayName("应该成功修改密码")
    void shouldChangePasswordSuccessfully() throws Exception {
      // Arrange
      UserPrincipal userPrincipal = TestSecurityConfig.createTestUserPrincipal();
      ChangePasswordRequest request = ChangePasswordRequest.builder().oldPassword("oldPassword123!")
          .newPassword("newPassword123!").confirmPassword("newPassword123!").build();

      doNothing().when(userService).changePassword(1L, request.getOldPassword(),
          request.getNewPassword());

      // Act & Assert
      mockMvc
          .perform(put("/api/users/password")
              .with(SecurityMockMvcRequestPostProcessors
                  .authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null,
                      userPrincipal.getAuthorities())))
              .with(SecurityMockMvcRequestPostProcessors.csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("密码修改成功"));

      verify(userService).changePassword(1L, request.getOldPassword(), request.getNewPassword());
    }

    @Test
    @DisplayName("应该在原密码为空时返回400")
    void shouldReturn400WhenOldPasswordEmpty() throws Exception {
      // Arrange
      UserPrincipal userPrincipal = TestSecurityConfig.createTestUserPrincipal();
      ChangePasswordRequest request = ChangePasswordRequest.builder().oldPassword("")
          .newPassword("newPassword123!").confirmPassword("newPassword123!").build();

      // Act & Assert
      mockMvc
          .perform(put("/api/users/password")
              .with(SecurityMockMvcRequestPostProcessors
                  .authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null,
                      userPrincipal.getAuthorities())))
              .with(SecurityMockMvcRequestPostProcessors.csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).changePassword(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("应该在新密码过短时返回400")
    void shouldReturn400WhenNewPasswordTooShort() throws Exception {
      // Arrange
      UserPrincipal userPrincipal = TestSecurityConfig.createTestUserPrincipal();
      ChangePasswordRequest request = ChangePasswordRequest.builder().oldPassword("oldPassword123!")
          .newPassword("123").confirmPassword("123").build();

      // Act & Assert
      mockMvc
          .perform(put("/api/users/password")
              .with(SecurityMockMvcRequestPostProcessors
                  .authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null,
                      userPrincipal.getAuthorities())))
              .with(SecurityMockMvcRequestPostProcessors.csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(userService, never()).changePassword(anyLong(), anyString(), anyString());
    }
  }

  @Nested
  @DisplayName("头像相关测试")
  class AvatarTests {

    @Test
    @DisplayName("应该成功更新头像")
    void shouldUpdateAvatarSuccessfully() throws Exception {
      // Arrange
      UserPrincipal userPrincipal = TestSecurityConfig.createTestUserPrincipal();
      String avatarUrl = "https://example.com/avatar.jpg";
      User updatedUser = TestDataFactory.createTestUser();
      updatedUser.setId(1L);
      updatedUser.setAvatarUrl(avatarUrl);

      when(userService.updateAvatar(1L, avatarUrl)).thenReturn(updatedUser);

      // Act & Assert
      mockMvc
          .perform(put("/api/users/avatar")
              .with(SecurityMockMvcRequestPostProcessors
                  .authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null,
                      userPrincipal.getAuthorities())))
              .with(SecurityMockMvcRequestPostProcessors.csrf()).param("avatarUrl", avatarUrl))
          .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("头像更新成功"))
          .andExpect(jsonPath("$.data.avatarUrl").value(avatarUrl));

      verify(userService).updateAvatar(1L, avatarUrl);
    }
  }

  @Nested
  @DisplayName("用户查询相关测试")
  class UserQueryTests {

    @Test
    @DisplayName("应该成功获取指定用户信息")
    void shouldGetUserByIdSuccessfully() throws Exception {
      // Arrange
      UserPrincipal userPrincipal = TestSecurityConfig.createTestUserPrincipal();
      Long userId = 1L;
      when(userService.getById(userId)).thenReturn(testUser);

      // Act & Assert
      mockMvc
          .perform(get("/api/users/{userId}", userId).with(SecurityMockMvcRequestPostProcessors
              .authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null,
                  userPrincipal.getAuthorities()))))
          .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("获取用户信息成功"))
          .andExpect(jsonPath("$.data.username").value(testUser.getUsername()));

      verify(userService).getById(userId);
    }

    @Test
    @DisplayName("应该成功分页查询用户列表")
    void shouldGetUsersSuccessfully() throws Exception {
      // Arrange
      UserPrincipal adminPrincipal = TestSecurityConfig.createTestAdminPrincipal();
      List<User> users = Arrays.asList(testUser);
      Page<User> userPage = new PageImpl<>(users);
      when(userService.findUsers(eq("test"), eq(1), any(Pageable.class))).thenReturn(userPage);

      // Act & Assert
      mockMvc
          .perform(get("/api/users")
              .with(SecurityMockMvcRequestPostProcessors
                  .authentication(new UsernamePasswordAuthenticationToken(adminPrincipal, null,
                      adminPrincipal.getAuthorities())))
              .param("keyword", "test").param("status", "1").param("page", "1").param("size", "20"))
          .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("查询用户列表成功"))
          .andExpect(jsonPath("$.data.content").isArray())
          .andExpect(jsonPath("$.data.total").value(1)); // 修复JSON路径：total而不是totalElements

      verify(userService).findUsers(eq("test"), eq(1), any(Pageable.class));
    }

    @Test
    @DisplayName("应该在非管理员访问用户列表时返回403")
    void shouldReturn403WhenNonAdminAccessUserList() throws Exception {
      // Arrange
      UserPrincipal userPrincipal = TestSecurityConfig.createTestUserPrincipal();

      // 在@WebMvcTest中，方法级安全注解可能不会自动生效，
      // 但我们可以通过不提供Mock来模拟权限检查失败的情况
      // 如果权限检查正常工作，应该返回403而不是调用service方法

      // Act & Assert
      mockMvc.perform(get("/api/users").with(SecurityMockMvcRequestPostProcessors
          .authentication(new UsernamePasswordAuthenticationToken(userPrincipal, null,
              userPrincipal.getAuthorities()))))
          .andExpect(status().isForbidden());

      verify(userService, never()).findUsers(anyString(), anyInt(), any(Pageable.class));
    }
  }
}
