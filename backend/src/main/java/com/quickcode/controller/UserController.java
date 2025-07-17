package com.quickcode.controller;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.common.response.PageResponse;
import com.quickcode.dto.user.UserInfoRequest;
import com.quickcode.dto.user.UserProfileResponse;
import com.quickcode.dto.user.ChangePasswordRequest;
import com.quickcode.dto.user.TwoFactorStatusResponse;
import com.quickcode.dto.user.TwoFactorSetupResponse;
import com.quickcode.dto.user.TwoFactorVerifyRequest;
import com.quickcode.entity.User;
import com.quickcode.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Value("${app.file.upload-path:/uploads}")
    private String uploadPath;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

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
        log.info("更新用户信息: userId={}, nickname={}, bio={}, avatar={}",
                userId, request.getNickname(), request.getBio(), request.getAvatar());

        User user = userService.updateUserInfo(userId, request.getNickname(), request.getBio(), request.getAvatar());
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
     * 上传用户头像
     */
    @PostMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        log.info("上传头像请求: userId={}, fileName={}, fileSize={}",
                userId, file.getOriginalFilename(), file.getSize());

        try {
            // 验证文件
            validateAvatarFile(file);

            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());

            // 确保上传目录存在
            Path uploadDir;
            if (uploadPath.startsWith("/")) {
                // 绝对路径
                uploadDir = Paths.get(uploadPath, "avatars");
            } else {
                // 相对路径，基于当前工作目录
                uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath, "avatars");
            }
            Files.createDirectories(uploadDir);

            // 保存文件
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 生成访问URL
            String fileUrl = baseUrl + "/uploads/avatars/" + fileName;

            // 更新用户头像URL
            userService.updateAvatar(userId, fileUrl);

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileName", fileName);

            log.info("头像上传成功: userId={}, fileUrl={}", userId, fileUrl);
            return success(result, "头像上传成功");

        } catch (IOException e) {
            log.error("头像上传失败: userId={}, error={}", userId, e.getMessage(), e);
            return error("文件上传失败");
        } catch (IllegalArgumentException e) {
            log.warn("头像上传验证失败: userId={}, error={}", userId, e.getMessage());
            return error(e.getMessage());
        }
    }

    /**
     * 更新用户头像URL
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

    /**
     * 验证头像文件
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小 (2MB)
        long maxSize = 2L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过2MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只支持图片文件");
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)$")) {
            throw new IllegalArgumentException("只支持 JPG、PNG、GIF、WebP 格式的图片");
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    // ==================== 双因素认证相关API ====================

    /**
     * 获取2FA状态
     */
    @GetMapping("/2fa/status")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TwoFactorStatusResponse> getTwoFactorStatus() {
        Long userId = getCurrentUserId();
        log.info("获取2FA状态: userId={}", userId);

        User user = userService.getById(userId);
        TwoFactorStatusResponse response = TwoFactorStatusResponse.builder()
                .enabled(user.getTwoFactorEnabled())
                .build();

        return success(response, "获取2FA状态成功");
    }

    /**
     * 生成2FA设置信息
     */
    @GetMapping("/2fa/setup")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<TwoFactorSetupResponse> getTwoFactorSetup() {
        Long userId = getCurrentUserId();
        log.info("生成2FA设置信息: userId={}", userId);

        String secret = userService.generateTwoFactorSecret(userId);

        // 生成QR码URL (简化实现，实际项目中可能需要更复杂的QR码生成)
        String issuer = "QuickCode";
        User user = userService.getById(userId);
        String qrCodeUrl = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, user.getEmail(), secret, issuer);

        TwoFactorSetupResponse response = TwoFactorSetupResponse.builder()
                .secret(secret)
                .qrCodeUrl(qrCodeUrl)
                .build();

        return success(response, "2FA设置信息生成成功");
    }

    /**
     * 启用2FA（需要验证TOTP代码）
     */
    @PutMapping("/2fa/enable")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> enableTwoFactor(@Valid @RequestBody TwoFactorVerifyRequest request) {
        Long userId = getCurrentUserId();
        log.info("启用2FA: userId={}", userId);

        userService.enableTwoFactor(userId, request.getTotpCode());

        return success(null, "2FA启用成功");
    }

    /**
     * 禁用2FA（需要验证TOTP代码）
     */
    @PutMapping("/2fa/disable")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> disableTwoFactor(@Valid @RequestBody TwoFactorVerifyRequest request) {
        Long userId = getCurrentUserId();
        log.info("禁用2FA: userId={}", userId);

        userService.disableTwoFactor(userId, request.getTotpCode());

        return success(null, "2FA禁用成功");
    }

    /**
     * 验证2FA代码
     */
    @PutMapping("/2fa/verify")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> verifyTwoFactor(@Valid @RequestBody TwoFactorVerifyRequest request) {
        Long userId = getCurrentUserId();
        log.info("验证2FA代码: userId={}", userId);

        boolean isValid = userService.verifyTwoFactorCode(userId, request.getTotpCode());

        if (isValid) {
            return success(null, "2FA验证成功");
        } else {
            return error(400, "验证码错误");
        }
    }
}
