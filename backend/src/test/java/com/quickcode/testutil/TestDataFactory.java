package com.quickcode.testutil;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import com.quickcode.dto.auth.LoginRequest;
import com.quickcode.dto.auth.RegisterRequest;
import com.quickcode.entity.Permission;
import com.quickcode.entity.PointAccount;
import com.quickcode.entity.PointTransaction;
import com.quickcode.entity.Role;
import com.quickcode.entity.User;

/**
 * 测试数据工厂 提供创建测试数据的便捷方法
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
public class TestDataFactory {

  // 默认测试数据常量
  public static final String DEFAULT_USERNAME = "testuser";
  public static final String DEFAULT_EMAIL = "test@example.com";
  public static final String DEFAULT_PASSWORD = "Test123456!";
  public static final String DEFAULT_NICKNAME = "测试用户";
  public static final String DEFAULT_PHONE = "13800138000";
  public static final BigDecimal DEFAULT_POINTS = new BigDecimal("100.00");

  /**
   * 创建测试用户
   */
  public static User createTestUser() {
    return User.builder().username(DEFAULT_USERNAME).email(DEFAULT_EMAIL)
        .password("$2a$10$encrypted.password.hash") // 模拟加密后的密码
        .nickname(DEFAULT_NICKNAME).phone(DEFAULT_PHONE).status(1) // 激活状态
        .emailVerified(true).twoFactorEnabled(false).points(DEFAULT_POINTS).build();
  }

  /**
   * 创建指定用户名的测试用户
   */
  public static User createTestUser(String username) {
    return User.builder().username(username).email(username + "@example.com")
        .password("$2a$10$encrypted.password.hash").nickname(DEFAULT_NICKNAME).phone(DEFAULT_PHONE)
        .status(1).emailVerified(true).twoFactorEnabled(false).points(DEFAULT_POINTS).build();
  }

  /**
   * 创建指定ID的测试用户（已弃用，建议使用无参方法让JPA自动生成ID）
   */
  @Deprecated
  public static User createTestUser(Long id) {
    // 不再手动设置ID，让JPA自动生成
    return createTestUser();
  }

  /**
   * 创建管理员用户
   */
  public static User createAdminUser() {
    return User.builder().username("admin").email("admin@example.com")
        .password("$2a$10$encrypted.password.hash").nickname("管理员").phone(DEFAULT_PHONE).status(1)
        .emailVerified(true).twoFactorEnabled(false).points(DEFAULT_POINTS).build();
  }

  /**
   * 创建带有角色的测试用户
   */
  public static User createTestUserWithRoles() {
    User user = createTestUser();
    Role userRole = createTestRole();
    user.addRole(userRole);
    return user;
  }

  /**
   * 创建带有管理员角色的用户
   */
  public static User createAdminUserWithRoles() {
    User user = createAdminUser();
    Role adminRole = createAdminRole();
    user.addRole(adminRole);
    return user;
  }

  /**
   * 创建未激活的用户
   */
  public static User createInactiveUser() {
    return User.builder().username(DEFAULT_USERNAME + "_inactive")
        .email("inactive_" + DEFAULT_EMAIL).password("$2a$10$encrypted.password.hash")
        .nickname(DEFAULT_NICKNAME).phone(DEFAULT_PHONE).status(0) // 未激活状态
        .emailVerified(false).twoFactorEnabled(false).points(BigDecimal.ZERO).build();
  }

  /**
   * 创建登录请求
   */
  public static LoginRequest createLoginRequest() {
    LoginRequest request = new LoginRequest();
    request.setUsernameOrEmail(DEFAULT_USERNAME);
    request.setPassword(DEFAULT_PASSWORD);
    return request;
  }

  /**
   * 创建指定用户名的登录请求
   */
  public static LoginRequest createLoginRequest(String usernameOrEmail, String password) {
    LoginRequest request = new LoginRequest();
    request.setUsernameOrEmail(usernameOrEmail);
    request.setPassword(password);
    return request;
  }

  /**
   * 创建注册请求
   */
  public static RegisterRequest createRegisterRequest() {
    RegisterRequest request = new RegisterRequest();
    request.setUsername(DEFAULT_USERNAME);
    request.setEmail(DEFAULT_EMAIL);
    request.setPassword(DEFAULT_PASSWORD);
    request.setConfirmPassword(DEFAULT_PASSWORD);
    return request;
  }

  /**
   * 创建指定参数的注册请求
   */
  public static RegisterRequest createRegisterRequest(String username, String email,
      String password) {
    RegisterRequest request = new RegisterRequest();
    request.setUsername(username);
    request.setEmail(email);
    request.setPassword(password);
    request.setConfirmPassword(password);
    return request;
  }

  /**
   * 创建测试积分账户
   */
  public static PointAccount createTestPointAccount() {
    return PointAccount.builder().userId(1L).availablePoints(DEFAULT_POINTS)
        .frozenPoints(BigDecimal.ZERO).totalEarned(DEFAULT_POINTS).totalSpent(BigDecimal.ZERO)
        .build();
  }

  /**
   * 创建指定用户ID的积分账户
   */
  public static PointAccount createTestPointAccount(Long userId) {
    return PointAccount.builder().userId(userId).availablePoints(DEFAULT_POINTS)
        .frozenPoints(BigDecimal.ZERO).totalEarned(DEFAULT_POINTS).totalSpent(BigDecimal.ZERO)
        .build();
  }

  /**
   * 创建指定积分余额的积分账户
   */
  public static PointAccount createTestPointAccount(BigDecimal availablePoints) {
    return PointAccount.builder().userId(1L).availablePoints(availablePoints)
        .frozenPoints(BigDecimal.ZERO).totalEarned(availablePoints).totalSpent(BigDecimal.ZERO)
        .build();
  }

  /**
   * 创建测试积分交易记录
   */
  public static PointTransaction createTestPointTransaction() {
    return PointTransaction.builder().userId(1L).type(1) // 收入
        .amount(new BigDecimal("50.00")).balanceBefore(new BigDecimal("50.00"))
        .balanceAfter(new BigDecimal("100.00")).description("测试积分交易").status(1) // 成功
        .build();
  }

  /**
   * 创建测试角色
   */
  public static Role createTestRole() {
    Role role =
        Role.builder().roleName("普通用户").roleCode("USER").description("普通用户").status(1).build();
    // 确保users集合被初始化
    if (role.getUsers() == null) {
      role.setUsers(new java.util.HashSet<>());
    }
    // 添加基本权限
    role.addPermission(createUserReadPermission());
    role.addPermission(createProjectReadPermission());
    return role;
  }

  /**
   * 创建管理员角色
   */
  public static Role createAdminRole() {
    Role role = Role.builder().roleName("管理员").roleCode("ADMIN").description("管理员").status(1).build();
    // 添加管理员权限
    role.addPermission(createUserManagePermission());
    role.addPermission(createProjectManagePermission());
    role.addPermission(createSystemConfigPermission());
    return role;
  }

  /**
   * 创建测试权限
   */
  public static Permission createTestPermission() {
    return Permission.builder().permissionName("用户读取权限").permissionCode("USER_READ")
        .description("用户读取权限").resourceType("USER").actionType("READ").status(1).build();
  }

  /**
   * 创建用户读取权限
   */
  public static Permission createUserReadPermission() {
    return Permission.builder().permissionName("查看用户").permissionCode("user:read")
        .description("查看用户信息").resourceType("USER").actionType("READ").status(1).build();
  }

  /**
   * 创建项目读取权限
   */
  public static Permission createProjectReadPermission() {
    return Permission.builder().permissionName("查看项目").permissionCode("project:read")
        .description("查看项目信息").resourceType("PROJECT").actionType("READ").status(1).build();
  }

  /**
   * 创建用户管理权限
   */
  public static Permission createUserManagePermission() {
    return Permission.builder().permissionName("管理用户").permissionCode("user:manage")
        .description("完整的用户管理权限").resourceType("USER").actionType("MANAGE").status(1).build();
  }

  /**
   * 创建项目管理权限
   */
  public static Permission createProjectManagePermission() {
    return Permission.builder().permissionName("管理项目").permissionCode("project:manage")
        .description("完整的项目管理权限").resourceType("PROJECT").actionType("MANAGE").status(1).build();
  }

  /**
   * 创建系统配置权限
   */
  public static Permission createSystemConfigPermission() {
    return Permission.builder().permissionName("系统配置").permissionCode("system:config")
        .description("系统配置管理").resourceType("SYSTEM").actionType("MANAGE").status(1).build();
  }

  /**
   * 创建测试权限列表
   */
  public static List<Permission> createTestPermissions() {
    Permission permission2 =
        Permission.builder().permissionName("用户写入权限").permissionCode("USER_WRITE")
            .description("用户写入权限").resourceType("USER").actionType("WRITE").status(1).build();


    return Arrays.asList(createTestPermission(), permission2);
  }

  /**
   * 创建无效的登录请求（用于测试验证）
   */
  public static LoginRequest createInvalidLoginRequest() {
    LoginRequest request = new LoginRequest();
    request.setUsernameOrEmail(""); // 空用户名
    request.setPassword(""); // 空密码
    return request;
  }

  /**
   * 创建无效的注册请求（用于测试验证）
   */
  public static RegisterRequest createInvalidRegisterRequest() {
    RegisterRequest request = new RegisterRequest();
    request.setUsername(""); // 空用户名
    request.setEmail("invalid-email"); // 无效邮箱
    request.setPassword("123"); // 弱密码
    request.setConfirmPassword("456"); // 密码不匹配
    return request;
  }
}
