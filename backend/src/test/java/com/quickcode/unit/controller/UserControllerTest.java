package com.quickcode.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickcode.controller.UserController;
import com.quickcode.dto.user.ChangePasswordRequest;
import com.quickcode.dto.user.UserInfoRequest;
import com.quickcode.entity.User;
import com.quickcode.service.UserService;
import com.quickcode.testutil.TestDataFactory;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController单元测试
 * 使用MockMvc测试REST API端点
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
@DisplayName("UserController单元测试")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

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
        @WithMockUser(username = "testuser", authorities = {"USER"})
        void shouldGetCurrentUserProfileSuccessfully() throws Exception {
            // Arrange
            when(userService.getById(anyLong())).thenReturn(testUser);

            // Act & Assert
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("获取用户信息成功"))
                    .andExpect(jsonPath("$.data.username").value(testUser.getUsername()))
                    .andExpect(jsonPath("$.data.email").value(testUser.getEmail()));

            verify(userService).getById(anyLong());
        }

        @Test
        @DisplayName("应该在未认证时返回401")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isUnauthorized());

            verify(userService, never()).getById(anyLong());
        }

        @Test
        @DisplayName("应该成功更新当前用户信息")
        @WithMockUser(username = "testuser", authorities = {"USER"})
        void shouldUpdateCurrentUserProfileSuccessfully() throws Exception {
            // Arrange
            UserInfoRequest request = UserInfoRequest.builder()
                    .nickname("新昵称")
                    .phone("13900139000")
                    .bio("这是我的个人简介")
                    .build();

            User updatedUser = TestDataFactory.createTestUser();
            updatedUser.setId(1L);
            updatedUser.setNickname(request.getNickname());
            updatedUser.setPhone(request.getPhone());

            when(userService.updateUserInfo(anyLong(), anyString(), anyString())).thenReturn(updatedUser);

            // Act & Assert
            mockMvc.perform(put("/api/users/profile")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("更新用户信息成功"))
                    .andExpect(jsonPath("$.data.nickname").value(request.getNickname()))
                    .andExpect(jsonPath("$.data.phone").value(request.getPhone()));

            verify(userService).updateUserInfo(anyLong(), eq(request.getNickname()), eq(request.getPhone()));
        }

        @Test
        @DisplayName("应该在昵称过长时返回400")
        @WithMockUser(username = "testuser", authorities = {"USER"})
        void shouldReturn400WhenNicknameTooLong() throws Exception {
            // Arrange
            UserInfoRequest request = UserInfoRequest.builder()
                    .nickname("这是一个非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常长的昵称")
                    .build();

            // Act & Assert
            mockMvc.perform(put("/api/users/profile")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).updateUserInfo(anyLong(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("密码相关测试")
    class PasswordTests {

        @Test
        @DisplayName("应该成功修改密码")
        @WithMockUser(username = "testuser", authorities = {"USER"})
        void shouldChangePasswordSuccessfully() throws Exception {
            // Arrange
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .oldPassword("oldPassword123!")
                    .newPassword("newPassword123!")
                    .confirmPassword("newPassword123!")
                    .build();

            doNothing().when(userService).changePassword(anyLong(), anyString(), anyString());

            // Act & Assert
            mockMvc.perform(put("/api/users/password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("密码修改成功"));

            verify(userService).changePassword(anyLong(), eq(request.getOldPassword()), eq(request.getNewPassword()));
        }

        @Test
        @DisplayName("应该在原密码为空时返回400")
        @WithMockUser(username = "testuser", authorities = {"USER"})
        void shouldReturn400WhenOldPasswordEmpty() throws Exception {
            // Arrange
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .oldPassword("")
                    .newPassword("newPassword123!")
                    .confirmPassword("newPassword123!")
                    .build();

            // Act & Assert
            mockMvc.perform(put("/api/users/password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).changePassword(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("应该在新密码过短时返回400")
        @WithMockUser(username = "testuser", authorities = {"USER"})
        void shouldReturn400WhenNewPasswordTooShort() throws Exception {
            // Arrange
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .oldPassword("oldPassword123!")
                    .newPassword("123")
                    .confirmPassword("123")
                    .build();

            // Act & Assert
            mockMvc.perform(put("/api/users/password")
                            .with(csrf())
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
        @WithMockUser(username = "testuser", authorities = {"USER"})
        void shouldUpdateAvatarSuccessfully() throws Exception {
            // Arrange
            String avatarUrl = "https://example.com/avatar.jpg";
            User updatedUser = TestDataFactory.createTestUser();
            updatedUser.setId(1L);
            updatedUser.setAvatarUrl(avatarUrl);

            when(userService.updateAvatar(anyLong(), anyString())).thenReturn(updatedUser);

            // Act & Assert
            mockMvc.perform(put("/api/users/avatar")
                            .with(csrf())
                            .param("avatarUrl", avatarUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("头像更新成功"))
                    .andExpect(jsonPath("$.data.avatarUrl").value(avatarUrl));

            verify(userService).updateAvatar(anyLong(), eq(avatarUrl));
        }
    }

    @Nested
    @DisplayName("用户查询相关测试")
    class UserQueryTests {

        @Test
        @DisplayName("应该成功获取指定用户信息")
        @WithMockUser(username = "testuser", authorities = {"USER"})
        void shouldGetUserByIdSuccessfully() throws Exception {
            // Arrange
            Long userId = 1L;
            when(userService.getById(userId)).thenReturn(testUser);

            // Act & Assert
            mockMvc.perform(get("/api/users/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("获取用户信息成功"))
                    .andExpect(jsonPath("$.data.username").value(testUser.getUsername()));

            verify(userService).getById(userId);
        }

        @Test
        @DisplayName("应该成功分页查询用户列表")
        @WithMockUser(username = "admin", authorities = {"ADMIN"})
        void shouldGetUsersSuccessfully() throws Exception {
            // Arrange
            List<User> users = Arrays.asList(testUser);
            Page<User> userPage = new PageImpl<>(users);
            when(userService.findUsers(anyString(), anyInt(), any(Pageable.class))).thenReturn(userPage);

            // Act & Assert
            mockMvc.perform(get("/api/users")
                            .param("keyword", "test")
                            .param("status", "1")
                            .param("page", "1")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("查询用户列表成功"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));

            verify(userService).findUsers(eq("test"), eq(1), any(Pageable.class));
        }

        @Test
        @DisplayName("应该在非管理员访问用户列表时返回403")
        @WithMockUser(username = "user", authorities = {"USER"})
        void shouldReturn403WhenNonAdminAccessUserList() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isForbidden());

            verify(userService, never()).findUsers(anyString(), anyInt(), any(Pageable.class));
        }
    }
}
