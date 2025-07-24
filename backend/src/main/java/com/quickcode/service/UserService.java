package com.quickcode.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.quickcode.entity.User;

/**
 * 用户Service接口 提供用户相关的业务逻辑方法
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface UserService extends BaseService<User, Long> {

  /**
   * 用户注册
   */
  User register(String username, String email, String password);

  /**
   * 用户登录
   */
  User login(String usernameOrEmail, String password);

  /**
   * 根据用户名查找用户
   */
  Optional<User> findByUsername(String username);

  /**
   * 根据邮箱查找用户
   */
  Optional<User> findByEmail(String email);

  /**
   * 根据用户名或邮箱查找用户
   */
  Optional<User> findByUsernameOrEmail(String usernameOrEmail);

  /**
   * 根据用户名或邮箱查找用户（包含角色和权限信息）
   * 用于认证和权限验证场景
   */
  Optional<User> findByUsernameOrEmailWithRoles(String usernameOrEmail);

  /**
   * 检查用户名是否可用
   */
  boolean isUsernameAvailable(String username);

  /**
   * 检查邮箱是否可用
   */
  boolean isEmailAvailable(String email);

  /**
   * 更新用户信息
   */
  User updateUserInfo(Long userId, String nickname, String bio, String avatar);

  /**
   * 更新用户头像
   */
  User updateAvatar(Long userId, String avatarUrl);

  /**
   * 修改密码
   */
  void changePassword(Long userId, String oldPassword, String newPassword);

  /**
   * 重置密码
   */
  void resetPassword(String email, String newPassword);

  /**
   * 验证邮箱
   */
  void verifyEmail(Long userId);

  /**
   * 生成双因素认证设置信息（不立即启用）
   */
  String generateTwoFactorSecret(Long userId);

  /**
   * 启用双因素认证（需要验证TOTP代码）
   */
  void enableTwoFactor(Long userId, String totpCode);

  /**
   * 禁用双因素认证（需要验证TOTP代码）
   */
  void disableTwoFactor(Long userId, String totpCode);

  /**
   * 验证双因素认证码
   */
  boolean verifyTwoFactorCode(Long userId, String code);

  /**
   * 锁定用户
   */
  void lockUser(Long userId, LocalDateTime lockUntil);

  /**
   * 解锁用户
   */
  void unlockUser(Long userId);

  /**
   * 禁用用户
   */
  void disableUser(Long userId);

  /**
   * 启用用户
   */
  void enableUser(Long userId);

  /**
   * 记录登录成功
   */
  void recordLoginSuccess(Long userId, String loginIp);

  /**
   * 记录登录失败
   */
  void recordLoginFailure(Long userId);

  /**
   * 为用户分配角色
   */
  void assignRole(Long userId, String roleCode);

  /**
   * 移除用户角色
   */
  void removeRole(Long userId, String roleCode);

  /**
   * 检查用户是否拥有角色
   */
  boolean hasRole(Long userId, String roleCode);

  /**
   * 检查用户是否拥有权限
   */
  boolean hasPermission(Long userId, String permissionCode);

  /**
   * 获取用户的所有权限
   */
  List<String> getUserPermissions(Long userId);

  /**
   * 根据角色查找用户
   */
  List<User> findByRole(String roleCode);

  /**
   * 根据状态查找用户
   */
  List<User> findByStatus(Integer status);

  /**
   * 分页查找用户
   */
  Page<User> findUsers(String keyword, Integer status, Pageable pageable);

  /**
   * 查找新注册用户
   */
  List<User> findNewUsers(LocalDateTime since);

  /**
   * 查找长时间未登录的用户
   */
  List<User> findInactiveUsers(LocalDateTime cutoffTime);

  /**
   * 查找被锁定的用户
   */
  List<User> findLockedUsers();

  /**
   * 统计用户数量
   */
  long countUsers();

  /**
   * 根据状态统计用户数量
   */
  long countUsersByStatus(Integer status);

  /**
   * 统计新注册用户数量
   */
  long countNewUsers(LocalDateTime since);

  /**
   * 统计已验证邮箱的用户数量
   */
  long countVerifiedUsers();

  /**
   * 统计启用双因素认证的用户数量
   */
  long countTwoFactorEnabledUsers();

  /**
   * 根据邮箱验证令牌查找用户
   */
  Optional<User> findByEmailVerificationToken(String token);

  /**
   * 根据密码重置令牌查找用户
   */
  Optional<User> findByPasswordResetToken(String token);

  /**
   * 保存用户
   */
  User save(User user);
}
