package com.quickcode.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
      throw new IllegalArgumentException("用户名已存在: " + username);
    }

    // 检查邮箱是否已存在
    if (userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("邮箱已存在: " + email);
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
        .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + usernameOrEmail));

    // 验证密码
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalArgumentException("密码错误");
    }

    // 检查用户状态
    if (User.Status.DISABLED.getCode().equals(user.getStatus())) {
      throw new IllegalStateException("用户已被禁用");
    }

    // 检查是否被锁定
    if (user.isLocked()) {
      throw new IllegalStateException("用户已被锁定，请稍后再试");
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
  public User updateUserInfo(Long userId, String nickname, String phone) {
    log.debug("更新用户信息: userId={}, nickname={}, phone={}", userId, nickname, phone);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

    user.setNickname(nickname);
    user.setPhone(phone);

    User updatedUser = userRepository.save(user);
    log.info("用户信息更新成功: userId={}", userId);

    return updatedUser;
  }

  @Override
  public User updateAvatar(Long userId, String avatarUrl) {
    log.debug("更新用户头像: userId={}, avatarUrl={}", userId, avatarUrl);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

    user.setAvatarUrl(avatarUrl);

    User updatedUser = userRepository.save(user);
    log.info("用户头像更新成功: userId={}", userId);

    return updatedUser;
  }

  @Override
  public void changePassword(Long userId, String oldPassword, String newPassword) {
    log.debug("修改用户密码: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

    // 验证旧密码
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new IllegalArgumentException("原密码错误");
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
        .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    log.info("用户密码重置成功: email={}, userId={}", email, user.getId());
  }

  @Override
  public void verifyEmail(Long userId) {
    log.debug("验证用户邮箱: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

    user.setEmailVerified(true);
    userRepository.save(user);

    log.info("用户邮箱验证成功: userId={}", userId);
  }

  @Override
  public String enableTwoFactor(Long userId) {
    // TODO: 实现启用双因素认证逻辑
    throw new UnsupportedOperationException("启用双因素认证功能尚未实现");
  }

  @Override
  public void disableTwoFactor(Long userId) {
    // TODO: 实现禁用双因素认证逻辑
    throw new UnsupportedOperationException("禁用双因素认证功能尚未实现");
  }

  @Override
  public boolean verifyTwoFactorCode(Long userId, String code) {
    // TODO: 实现验证双因素认证码逻辑
    throw new UnsupportedOperationException("验证双因素认证码功能尚未实现");
  }

  @Override
  public void lockUser(Long userId, LocalDateTime lockUntil) {
    log.debug("锁定用户: userId={}, lockUntil={}", userId, lockUntil);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

    user.setLockedUntil(lockUntil);
    userRepository.save(user);

    log.info("用户锁定成功: userId={}, lockUntil={}", userId, lockUntil);
  }

  @Override
  public void unlockUser(Long userId) {
    log.debug("解锁用户: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

    user.setLockedUntil(null);
    userRepository.save(user);

    log.info("用户解锁成功: userId={}", userId);
  }

  @Override
  public void disableUser(Long userId) {
    log.debug("禁用用户: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

    user.setStatus(User.Status.DISABLED.getCode());
    userRepository.save(user);

    log.info("用户禁用成功: userId={}", userId);
  }

  @Override
  public void enableUser(Long userId) {
    log.debug("启用用户: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));

    user.setStatus(User.Status.ACTIVE.getCode());
    userRepository.save(user);

    log.info("用户启用成功: userId={}", userId);
  }

  @Override
  public void recordLoginSuccess(Long userId, String loginIp) {
    // TODO: 实现记录登录成功逻辑
    throw new UnsupportedOperationException("记录登录成功功能尚未实现");
  }

  @Override
  public void recordLoginFailure(Long userId) {
    // TODO: 实现记录登录失败逻辑
    throw new UnsupportedOperationException("记录登录失败功能尚未实现");
  }

  @Override
  public void assignRole(Long userId, String roleCode) {
    // TODO: 实现分配角色逻辑
    throw new UnsupportedOperationException("分配角色功能尚未实现");
  }

  @Override
  public void removeRole(Long userId, String roleCode) {
    // TODO: 实现移除角色逻辑
    throw new UnsupportedOperationException("移除角色功能尚未实现");
  }

  @Override
  public boolean hasRole(Long userId, String roleCode) {
    // TODO: 实现检查角色逻辑
    throw new UnsupportedOperationException("检查角色功能尚未实现");
  }

  @Override
  public boolean hasPermission(Long userId, String permissionCode) {
    // TODO: 实现检查权限逻辑
    throw new UnsupportedOperationException("检查权限功能尚未实现");
  }

  @Override
  public List<String> getUserPermissions(Long userId) {
    // TODO: 实现获取用户权限逻辑
    throw new UnsupportedOperationException("获取用户权限功能尚未实现");
  }

  @Override
  public List<User> findByRole(String roleCode) {
    // TODO: 实现根据角色查找用户逻辑
    throw new UnsupportedOperationException("根据角色查找用户功能尚未实现");
  }

  @Override
  public List<User> findByStatus(Integer status) {
    // TODO: 实现根据状态查找用户逻辑
    throw new UnsupportedOperationException("根据状态查找用户功能尚未实现");
  }

  @Override
  public Page<User> findUsers(String keyword, Integer status, Pageable pageable) {
    // TODO: 实现分页查找用户逻辑
    throw new UnsupportedOperationException("分页查找用户功能尚未实现");
  }

  @Override
  public List<User> findNewUsers(LocalDateTime since) {
    // TODO: 实现查找新注册用户逻辑
    throw new UnsupportedOperationException("查找新注册用户功能尚未实现");
  }

  @Override
  public List<User> findInactiveUsers(LocalDateTime cutoffTime) {
    // TODO: 实现查找长时间未登录用户逻辑
    throw new UnsupportedOperationException("查找长时间未登录用户功能尚未实现");
  }

  @Override
  public List<User> findLockedUsers() {
    // TODO: 实现查找被锁定用户逻辑
    throw new UnsupportedOperationException("查找被锁定用户功能尚未实现");
  }

  @Override
  public long countUsers() {
    // TODO: 实现统计用户数量逻辑
    throw new UnsupportedOperationException("统计用户数量功能尚未实现");
  }

  @Override
  public long countUsersByStatus(Integer status) {
    // TODO: 实现根据状态统计用户数量逻辑
    throw new UnsupportedOperationException("根据状态统计用户数量功能尚未实现");
  }

  @Override
  public long countNewUsers(LocalDateTime since) {
    // TODO: 实现统计新注册用户数量逻辑
    throw new UnsupportedOperationException("统计新注册用户数量功能尚未实现");
  }

  @Override
  public long countVerifiedUsers() {
    // TODO: 实现统计已验证邮箱用户数量逻辑
    throw new UnsupportedOperationException("统计已验证邮箱用户数量功能尚未实现");
  }

  @Override
  public long countTwoFactorEnabledUsers() {
    // TODO: 实现统计启用双因素认证用户数量逻辑
    throw new UnsupportedOperationException("统计启用双因素认证用户数量功能尚未实现");
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
        .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));
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
