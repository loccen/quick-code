package com.quickcode.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quickcode.entity.Role;
import com.quickcode.entity.User;
import com.quickcode.repository.RoleRepository;
import com.quickcode.repository.UserRepository;
import com.quickcode.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户服务实现类 提供用户相关的业务逻辑实现
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public User register(String username, String email, String password) {
    log.debug("开始注册用户: username={}, email={}", username, email);

    // 检查用户名是否已存在
    if (userRepository.existsByUsername(username)) {
      throw com.quickcode.common.exception.DuplicateResourceException.usernameExists(username);
    }

    // 检查邮箱是否已存在
    if (userRepository.existsByEmail(email)) {
      throw com.quickcode.common.exception.DuplicateResourceException.emailExists(email);
    }

    // 创建新用户
    User user = User.builder().username(username).email(email)
        .password(passwordEncoder.encode(password)).status(User.Status.PENDING.getCode())
        .emailVerified(false).twoFactorEnabled(false).loginFailureCount(0).build();

    User savedUser = userRepository.save(user);
    log.info("用户注册成功: username={}, id={}", username, savedUser.getId());

    return savedUser;
  }

  @Override
  public User login(String usernameOrEmail, String password) {
    log.debug("用户登录尝试: {}", usernameOrEmail);

    // 查找用户
    User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
        .orElseThrow(() -> new com.quickcode.common.exception.ResourceNotFoundException(
            "用户不存在: " + usernameOrEmail));

    // 验证密码
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw com.quickcode.common.exception.AuthenticationFailedException
          .invalidCredentials("密码错误");
    }

    // 检查用户状态
    if (User.Status.DISABLED.getCode().equals(user.getStatus())) {
      throw com.quickcode.common.exception.InvalidStateException.userDisabled();
    }

    // 检查是否被锁定
    if (user.isLocked()) {
      throw com.quickcode.common.exception.InvalidStateException
          .userLocked("用户已被锁定，请稍后再试");
    }

    log.info("用户登录成功: username={}, id={}", user.getUsername(), user.getId());
    return user;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
    return userRepository.findByUsernameOrEmail(usernameOrEmail);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isUsernameAvailable(String username) {
    return !userRepository.existsByUsername(username);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isEmailAvailable(String email) {
    return !userRepository.existsByEmail(email);
  }

  @Override
  public User updateUserInfo(Long userId, String nickname, String bio, String avatar) {
    log.debug("更新用户信息: userId={}, nickname={}, bio={}, avatar={}", userId, nickname, bio, avatar);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    if (nickname != null) {
      user.setNickname(nickname);
    }
    if (bio != null) {
      user.setBio(bio);
    }
    if (avatar != null) {
      user.setAvatarUrl(avatar);
    }

    User updatedUser = userRepository.save(user);
    log.info("用户信息更新成功: userId={}", userId);

    return updatedUser;
  }

  @Override
  public User updateAvatar(Long userId, String avatarUrl) {
    log.debug("更新用户头像: userId={}, avatarUrl={}", userId, avatarUrl);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    user.setAvatarUrl(avatarUrl);

    User updatedUser = userRepository.save(user);
    log.info("用户头像更新成功: userId={}", userId);

    return updatedUser;
  }

  @Override
  public void changePassword(Long userId, String oldPassword, String newPassword) {
    log.debug("修改用户密码: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    // 验证旧密码
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw com.quickcode.common.exception.AuthenticationFailedException
          .invalidCredentials("原密码错误");
    }

    // 设置新密码
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    log.info("用户密码修改成功: userId={}", userId);
  }

  @Override
  public void resetPassword(String email, String newPassword) {
    log.debug("重置用户密码: email={}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.userByEmail(email));

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    log.info("用户密码重置成功: email={}, userId={}", email, user.getId());
  }

  @Override
  public void verifyEmail(Long userId) {
    log.debug("验证用户邮箱: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    user.setEmailVerified(true);
    userRepository.save(user);

    log.info("用户邮箱验证成功: userId={}", userId);
  }

  @Override
  public String enableTwoFactor(Long userId) {
    // TODO: 实现启用双因素认证逻辑
    throw com.quickcode.common.exception.InvalidStateException
        .withCode(5005, "启用双因素认证功能尚未实现");
  }

  @Override
  public void disableTwoFactor(Long userId) {
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
  public void lockUser(Long userId, LocalDateTime lockUntil) {
    log.debug("锁定用户: userId={}, lockUntil={}", userId, lockUntil);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    user.setLockedUntil(lockUntil);
    userRepository.save(user);

    log.info("用户锁定成功: userId={}, lockUntil={}", userId, lockUntil);
  }

  @Override
  public void unlockUser(Long userId) {
    log.debug("解锁用户: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    user.setLockedUntil(null);
    userRepository.save(user);

    log.info("用户解锁成功: userId={}", userId);
  }

  @Override
  public void disableUser(Long userId) {
    log.debug("禁用用户: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    user.setStatus(User.Status.DISABLED.getCode());
    userRepository.save(user);

    log.info("用户禁用成功: userId={}", userId);
  }

  @Override
  public void enableUser(Long userId) {
    log.debug("启用用户: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    user.setStatus(User.Status.ACTIVE.getCode());
    userRepository.save(user);

    log.info("用户启用成功: userId={}", userId);
  }

  @Override
  public void recordLoginSuccess(Long userId, String loginIp) {
    // TODO: 实现记录登录成功逻辑
    throw com.quickcode.common.exception.InvalidStateException
        .withCode(5005, "记录登录成功功能尚未实现");
  }

  @Override
  public void recordLoginFailure(Long userId) {
    // TODO: 实现记录登录失败逻辑
    throw com.quickcode.common.exception.InvalidStateException
        .withCode(5005, "记录登录失败功能尚未实现");
  }

  @Override
  public void assignRole(Long userId, String roleCode) {
    log.debug("为用户分配角色: userId={}, roleCode={}", userId, roleCode);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    Role role = roleRepository.findByRoleCode(roleCode)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.roleByCode(roleCode));

    user.addRole(role);
    userRepository.save(user);

    log.info("用户角色分配成功: userId={}, roleCode={}", userId, roleCode);
  }

  @Override
  public void removeRole(Long userId, String roleCode) {
    log.debug("移除用户角色: userId={}, roleCode={}", userId, roleCode);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    Role role = roleRepository.findByRoleCode(roleCode)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.roleByCode(roleCode));

    user.removeRole(role);
    userRepository.save(user);

    log.info("用户角色移除成功: userId={}, roleCode={}", userId, roleCode);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasRole(Long userId, String roleCode) {
    log.debug("检查用户角色: userId={}, roleCode={}", userId, roleCode);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    return user.hasRole(roleCode);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasPermission(Long userId, String permissionCode) {
    log.debug("检查用户权限: userId={}, permissionCode={}", userId, permissionCode);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    return user.getRoles().stream().anyMatch(role -> role.hasPermission(permissionCode));
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> getUserPermissions(Long userId) {
    log.debug("获取用户权限: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(userId));

    return user.getRoles().stream().flatMap(role -> role.getPermissionCodes().stream()).distinct()
        .sorted().toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findByRole(String roleCode) {
    log.debug("根据角色查找用户: roleCode={}", roleCode);
    return userRepository.findByRoleCode(roleCode);
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findByStatus(Integer status) {
    log.debug("根据状态查找用户: status={}", status);
    return userRepository.findByStatus(status);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<User> findUsers(String keyword, Integer status, Pageable pageable) {
    log.debug("分页查找用户: keyword={}, status={}, page={}", keyword, status, pageable.getPageNumber());

    // 简化实现：直接返回所有用户的分页结果
    // 在实际项目中，这里应该根据keyword和status进行过滤
    return userRepository.findAll(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findNewUsers(LocalDateTime since) {
    log.debug("查找新注册用户: since={}", since);
    LocalDateTime endTime = LocalDateTime.now();
    return userRepository.findByCreatedTimeBetween(since, endTime);
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findInactiveUsers(LocalDateTime cutoffTime) {
    log.debug("查找长时间未登录用户: cutoffTime={}", cutoffTime);
    return userRepository.findInactiveUsers(cutoffTime);
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findLockedUsers() {
    log.debug("查找被锁定用户");
    LocalDateTime now = LocalDateTime.now();
    return userRepository.findLockedUsers(now);
  }

  @Override
  @Transactional(readOnly = true)
  public long countUsers() {
    log.debug("统计用户总数");
    return userRepository.count();
  }

  @Override
  @Transactional(readOnly = true)
  public long countUsersByStatus(Integer status) {
    log.debug("根据状态统计用户数量: status={}", status);
    return userRepository.countByStatus(status);
  }

  @Override
  @Transactional(readOnly = true)
  public long countNewUsers(LocalDateTime since) {
    log.debug("统计新注册用户数量: since={}", since);
    return userRepository.countNewUsers(since);
  }

  @Override
  @Transactional(readOnly = true)
  public long countVerifiedUsers() {
    log.debug("统计已验证邮箱用户数量");
    return userRepository.countVerifiedUsers();
  }

  @Override
  @Transactional(readOnly = true)
  public long countTwoFactorEnabledUsers() {
    log.debug("统计启用双因素认证用户数量");
    return userRepository.countTwoFactorEnabledUsers();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEmailVerificationToken(String token) {
    log.debug("根据邮箱验证令牌查找用户: token={}", token);
    return userRepository.findByEmailVerificationToken(token);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByPasswordResetToken(String token) {
    log.debug("根据密码重置令牌查找用户: token={}", token);
    return userRepository.findByPasswordResetToken(token);
  }

  // BaseService接口的方法实现
  @Override
  @Transactional(readOnly = true)
  public Optional<User> findById(Long id) {
    return userRepository.findById(id);
  }

  @Override
  public User getById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.user(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<User> findAll(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  @Override
  public User save(User entity) {
    return userRepository.save(entity);
  }

  @Override
  public List<User> saveAll(List<User> entities) {
    return userRepository.saveAll(entities);
  }

  @Override
  public void deleteById(Long id) {
    userRepository.deleteById(id);
  }

  @Override
  public void delete(User entity) {
    userRepository.delete(entity);
  }

  @Override
  public void deleteAll(List<User> entities) {
    userRepository.deleteAll(entities);
  }

  @Override
  public void deleteAll() {
    userRepository.deleteAll();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsById(Long id) {
    return userRepository.existsById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public long count() {
    return userRepository.count();
  }
}
