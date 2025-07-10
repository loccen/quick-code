package com.quickcode.controller;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.common.response.PageResponse;
import com.quickcode.dto.user.UserInfoRequest;
import com.quickcode.dto.user.UserProfileResponse;
import com.quickcode.dto.user.ChangePasswordRequest;
import com.quickcode.entity.User;
import com.quickcode.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> getCurrentUserProfile() {
        Long userId = getCurrentUserId();
        log.info("获取用户信息: userId={}", userId);
        
        User user = userService.getById(userId);
        UserProfileResponse response = UserProfileResponse.fromUser(user);
        
        return success(response, "获取用户信息成功");
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> updateCurrentUserProfile(@Valid @RequestBody UserInfoRequest request) {
        Long userId = getCurrentUserId();
        log.info("更新用户信息: userId={}, nickname={}", userId, request.getNickname());
        
        User user = userService.updateUserInfo(userId, request.getNickname(), request.getPhone());
        UserProfileResponse response = UserProfileResponse.fromUser(user);
        
        return success(response, "更新用户信息成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getCurrentUserId();
        log.info("修改密码: userId={}", userId);
        
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        
        return success(null, "密码修改成功");
    }

    /**
     * 更新用户头像
     */
    @PutMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> updateAvatar(@RequestParam String avatarUrl) {
        Long userId = getCurrentUserId();
        log.info("更新用户头像: userId={}, avatarUrl={}", userId, avatarUrl);
        
        User user = userService.updateAvatar(userId, avatarUrl);
        UserProfileResponse response = UserProfileResponse.fromUser(user);
        
        return success(response, "头像更新成功");
    }

    /**
     * 获取指定用户信息（管理员或用户本人）
     */
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> getUserById(@PathVariable Long userId) {
        checkUserAccess(userId);
        log.info("获取指定用户信息: userId={}", userId);
        
        User user = userService.getById(userId);
        UserProfileResponse response = UserProfileResponse.fromUser(user);
        
        return success(response, "获取用户信息成功");
    }

    /**
     * 分页查询用户列表（管理员权限）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<UserProfileResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("分页查询用户列表: keyword={}, status={}, page={}, size={}", keyword, status, page, size);
        
        validatePageParams(page, size);
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        
        Page<User> userPage = userService.findUsers(keyword, status, pageable);
        PageResponse<UserProfileResponse> response = PageResponse.fromPage(
            userPage, user -> UserProfileResponse.fromUser(user)
        );
        
        return success(response, "查询用户列表成功");
    }

    /**
     * 禁用用户（管理员权限）
     */
    @PutMapping("/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> disableUser(@PathVariable Long userId) {
        log.info("禁用用户: userId={}", userId);
        
        userService.disableUser(userId);
        
        return success(null, "用户禁用成功");
    }

    /**
     * 启用用户（管理员权限）
     */
    @PutMapping("/{userId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> enableUser(@PathVariable Long userId) {
        log.info("启用用户: userId={}", userId);
        
        userService.enableUser(userId);
        
        return success(null, "用户启用成功");
    }

    /**
     * 锁定用户（管理员权限）
     */
    @PutMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> lockUser(@PathVariable Long userId, 
                                     @RequestParam Integer hours) {
        log.info("锁定用户: userId={}, hours={}", userId, hours);
        
        java.time.LocalDateTime lockUntil = java.time.LocalDateTime.now().plusHours(hours);
        userService.lockUser(userId, lockUntil);
        
        return success(null, "用户锁定成功");
    }

    /**
     * 解锁用户（管理员权限）
     */
    @PutMapping("/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unlockUser(@PathVariable Long userId) {
        log.info("解锁用户: userId={}", userId);
        
        userService.unlockUser(userId);
        
        return success(null, "用户解锁成功");
    }

    /**
     * 验证用户邮箱（管理员权限）
     */
    @PutMapping("/{userId}/verify-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> verifyUserEmail(@PathVariable Long userId) {
        log.info("验证用户邮箱: userId={}", userId);
        
        userService.verifyEmail(userId);
        
        return success(null, "邮箱验证成功");
    }

    /**
     * 获取用户统计信息（管理员权限）
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> getUserStats() {
        log.info("获取用户统计信息");
        
        // TODO: 实现用户统计逻辑
        Object stats = new Object(); // 临时返回空对象
        
        return success(stats, "获取用户统计信息成功");
    }

    /**
     * 根据角色查找用户（管理员权限）
     */
    @GetMapping("/by-role/{roleCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserProfileResponse>> getUsersByRole(@PathVariable String roleCode) {
        log.info("根据角色查找用户: roleCode={}", roleCode);
        
        List<User> users = userService.findByRole(roleCode);
        List<UserProfileResponse> response = users.stream()
                .map(UserProfileResponse::fromUser)
                .toList();
        
        return success(response, "查询成功");
    }

    /**
     * 根据状态查找用户（管理员权限）
     */
    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserProfileResponse>> getUsersByStatus(@PathVariable Integer status) {
        log.info("根据状态查找用户: status={}", status);
        
        List<User> users = userService.findByStatus(status);
        List<UserProfileResponse> response = users.stream()
                .map(UserProfileResponse::fromUser)
                .toList();
        
        return success(response, "查询成功");
    }
}
