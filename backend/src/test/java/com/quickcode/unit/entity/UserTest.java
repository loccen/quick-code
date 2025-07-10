package com.quickcode.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.quickcode.entity.Role;
import com.quickcode.entity.User;
import com.quickcode.testutil.TestDataFactory;

/**
 * User实体类单元测试 验证User实体的业务逻辑和约束条件
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@DisplayName("User实体类测试")
class UserTest {

  private User user;

  @BeforeEach
  void setUp() {
    user = TestDataFactory.createTestUser();
  }

  @Nested
  @DisplayName("用户状态相关测试")
  class UserStatusTests {

    @Test
    @DisplayName("应该正确检查用户是否被锁定")
    void shouldCheckIfUserIsLocked() {
      // Arrange - 设置用户未被锁定
      user.setLockedUntil(null);

      // Act & Assert - 验证用户未被锁定
      assertThat(user.isLocked()).isFalse();

      // Arrange - 设置用户被锁定到未来时间
      user.setLockedUntil(LocalDateTime.now().plusMinutes(30));

      // Act & Assert - 验证用户被锁定
      assertThat(user.isLocked()).isTrue();

      // Arrange - 设置用户锁定时间已过期
      user.setLockedUntil(LocalDateTime.now().minusMinutes(30));

      // Act & Assert - 验证用户未被锁定
      assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("应该正确检查用户是否激活")
    void shouldCheckIfUserIsActive() {
      // Arrange - 设置用户状态为激活且未被锁定
      user.setStatus(User.Status.ACTIVE.getCode());
      user.setLockedUntil(null);

      // Act & Assert
      assertThat(user.isActive()).isTrue();

      // Arrange - 设置用户状态为禁用
      user.setStatus(User.Status.DISABLED.getCode());

      // Act & Assert
      assertThat(user.isActive()).isFalse();

      // Arrange - 设置用户状态为激活但被锁定
      user.setStatus(User.Status.ACTIVE.getCode());
      user.setLockedUntil(LocalDateTime.now().plusMinutes(30));

      // Act & Assert
      assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("应该正确处理登录失败次数")
    void shouldHandleLoginFailureCount() {
      // Arrange
      user.setLoginFailureCount(0);
      user.setLockedUntil(null);

      // Act - 增加登录失败次数（未达到锁定阈值）
      user.incrementLoginFailureCount();

      // Assert
      assertThat(user.getLoginFailureCount()).isEqualTo(1);
      assertThat(user.getLockedUntil()).isNull();

      // Act - 继续增加失败次数直到达到锁定阈值
      for (int i = 1; i < 5; i++) {
        user.incrementLoginFailureCount();
      }

      // Assert - 验证账户被锁定
      assertThat(user.getLoginFailureCount()).isEqualTo(5);
      assertThat(user.getLockedUntil()).isNotNull();
      assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now());

      // Act - 重置登录失败次数
      user.resetLoginFailureCount();

      // Assert
      assertThat(user.getLoginFailureCount()).isEqualTo(0);
      assertThat(user.getLockedUntil()).isNull();
    }
  }

  @Nested
  @DisplayName("VIP会员相关测试")
  class VipMembershipTests {

    @Test
    @DisplayName("应该正确检查VIP会员状态")
    void shouldCheckVipMembershipStatus() {
      // Arrange - 设置为非VIP用户
      user.setIsVip(false);
      user.setVipExpiresAt(null);

      // Act & Assert
      assertThat(user.isVipUser()).isFalse();

      // Arrange - 设置为永久VIP用户
      user.setIsVip(true);
      user.setVipExpiresAt(null);

      // Act & Assert
      assertThat(user.isVipUser()).isTrue();

      // Arrange - 设置为有期限的VIP用户（未过期）
      user.setIsVip(true);
      user.setVipExpiresAt(LocalDateTime.now().plusDays(30));

      // Act & Assert
      assertThat(user.isVipUser()).isTrue();

      // Arrange - 设置为有期限的VIP用户（已过期）
      user.setIsVip(true);
      user.setVipExpiresAt(LocalDateTime.now().minusDays(1));

      // Act & Assert
      assertThat(user.isVipUser()).isFalse();
    }

    @Test
    @DisplayName("应该正确设置VIP会员")
    void shouldSetVipMembership() {
      // Arrange
      LocalDateTime expiresAt = LocalDateTime.now().plusDays(365);

      // Act
      user.setVipMembership(expiresAt);

      // Assert
      assertThat(user.getIsVip()).isTrue();
      assertThat(user.getVipExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("应该正确取消VIP会员")
    void shouldCancelVipMembership() {
      // Arrange - 先设置为VIP用户
      user.setVipMembership(LocalDateTime.now().plusDays(30));

      // Act
      user.cancelVipMembership();

      // Assert
      assertThat(user.getIsVip()).isFalse();
      assertThat(user.getVipExpiresAt()).isNull();
    }
  }

  @Nested
  @DisplayName("积分相关测试")
  class PointsTests {

    @Test
    @DisplayName("应该正确增加积分")
    void shouldAddPoints() {
      // Arrange
      BigDecimal initialPoints = new BigDecimal("100.00");
      BigDecimal addAmount = new BigDecimal("50.00");
      user.setPoints(initialPoints);

      // Act
      user.addPoints(addAmount);

      // Assert
      assertThat(user.getPoints()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("应该忽略null或负数积分增加")
    void shouldIgnoreNullOrNegativePointsAddition() {
      // Arrange
      BigDecimal initialPoints = new BigDecimal("100.00");
      user.setPoints(initialPoints);

      // Act & Assert - 测试null值
      user.addPoints(null);
      assertThat(user.getPoints()).isEqualTo(initialPoints);

      // Act & Assert - 测试负数
      user.addPoints(new BigDecimal("-10.00"));
      assertThat(user.getPoints()).isEqualTo(initialPoints);

      // Act & Assert - 测试零
      user.addPoints(BigDecimal.ZERO);
      assertThat(user.getPoints()).isEqualTo(initialPoints);
    }

    @Test
    @DisplayName("应该正确扣除积分")
    void shouldDeductPoints() {
      // Arrange
      BigDecimal initialPoints = new BigDecimal("100.00");
      BigDecimal deductAmount = new BigDecimal("30.00");
      user.setPoints(initialPoints);

      // Act
      boolean result = user.deductPoints(deductAmount);

      // Assert
      assertThat(result).isTrue();
      assertThat(user.getPoints()).isEqualTo(new BigDecimal("70.00"));
    }

    @Test
    @DisplayName("应该拒绝扣除超过余额的积分")
    void shouldRejectDeductingMoreThanBalance() {
      // Arrange
      BigDecimal initialPoints = new BigDecimal("50.00");
      BigDecimal deductAmount = new BigDecimal("100.00");
      user.setPoints(initialPoints);

      // Act
      boolean result = user.deductPoints(deductAmount);

      // Assert
      assertThat(result).isFalse();
      assertThat(user.getPoints()).isEqualTo(initialPoints);
    }

    @Test
    @DisplayName("应该拒绝扣除null或负数积分")
    void shouldRejectDeductingNullOrNegativePoints() {
      // Arrange
      BigDecimal initialPoints = new BigDecimal("100.00");
      user.setPoints(initialPoints);

      // Act & Assert - 测试null值
      boolean result1 = user.deductPoints(null);
      assertThat(result1).isFalse();
      assertThat(user.getPoints()).isEqualTo(initialPoints);

      // Act & Assert - 测试负数
      boolean result2 = user.deductPoints(new BigDecimal("-10.00"));
      assertThat(result2).isFalse();
      assertThat(user.getPoints()).isEqualTo(initialPoints);
    }
  }

  @Nested
  @DisplayName("令牌相关测试")
  class TokenTests {

    @Test
    @DisplayName("应该正确生成邮箱验证令牌")
    void shouldGenerateEmailVerificationToken() {
      // Act
      user.generateEmailVerificationToken();

      // Assert
      assertThat(user.getEmailVerificationToken()).isNotNull();
      assertThat(user.getEmailVerificationToken()).hasSize(32); // UUID去掉横线后的长度
      assertThat(user.getEmailVerificationExpiresAt()).isNotNull();
      assertThat(user.getEmailVerificationExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("应该正确验证邮箱验证令牌")
    void shouldValidateEmailVerificationToken() {
      // Arrange
      user.generateEmailVerificationToken();
      String validToken = user.getEmailVerificationToken();

      // Act & Assert - 验证有效令牌
      assertThat(user.isEmailVerificationTokenValid(validToken)).isTrue();

      // Act & Assert - 验证无效令牌
      assertThat(user.isEmailVerificationTokenValid("invalid-token")).isFalse();

      // Act & Assert - 验证null令牌
      assertThat(user.isEmailVerificationTokenValid(null)).isFalse();
    }

    @Test
    @DisplayName("应该正确清除邮箱验证令牌")
    void shouldClearEmailVerificationToken() {
      // Arrange
      user.generateEmailVerificationToken();

      // Act
      user.clearEmailVerificationToken();

      // Assert
      assertThat(user.getEmailVerificationToken()).isNull();
      assertThat(user.getEmailVerificationExpiresAt()).isNull();
    }

    @Test
    @DisplayName("应该正确生成密码重置令牌")
    void shouldGeneratePasswordResetToken() {
      // Act
      user.generatePasswordResetToken();

      // Assert
      assertThat(user.getPasswordResetToken()).isNotNull();
      assertThat(user.getPasswordResetToken()).hasSize(32);
      assertThat(user.getPasswordResetExpiresAt()).isNotNull();
      assertThat(user.getPasswordResetExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("应该正确验证密码重置令牌")
    void shouldValidatePasswordResetToken() {
      // Arrange
      user.generatePasswordResetToken();
      String validToken = user.getPasswordResetToken();

      // Act & Assert
      assertThat(user.isPasswordResetTokenValid(validToken)).isTrue();
      assertThat(user.isPasswordResetTokenValid("invalid-token")).isFalse();
      assertThat(user.isPasswordResetTokenValid(null)).isFalse();
    }

    @Test
    @DisplayName("应该正确清除密码重置令牌")
    void shouldClearPasswordResetToken() {
      // Arrange
      user.generatePasswordResetToken();

      // Act
      user.clearPasswordResetToken();

      // Assert
      assertThat(user.getPasswordResetToken()).isNull();
      assertThat(user.getPasswordResetExpiresAt()).isNull();
    }
  }

  @Nested
  @DisplayName("角色相关测试")
  class RoleTests {

    @Test
    @DisplayName("应该正确添加角色")
    void shouldAddRole() {
      // Arrange
      Role role = TestDataFactory.createTestRole();

      // Act
      user.addRole(role);

      // Assert
      assertThat(user.getRoles()).contains(role);
      assertThat(role.getUsers()).contains(user);
    }

    @Test
    @DisplayName("应该正确移除角色")
    void shouldRemoveRole() {
      // Arrange
      Role role = TestDataFactory.createTestRole();
      user.addRole(role);

      // 验证角色已添加
      assertThat(user.getRoles()).hasSize(1);
      assertThat(role.getUsers()).hasSize(1);

      // Act
      user.removeRole(role);

      // Assert
      assertThat(user.getRoles()).isEmpty();
      assertThat(role.getUsers()).isEmpty();
    }

    @Test
    @DisplayName("应该正确检查是否拥有指定角色")
    void shouldCheckIfUserHasRole() {
      // Arrange
      Role userRole = TestDataFactory.createTestRole();
      userRole.setRoleCode("USER");
      Role adminRole = TestDataFactory.createAdminRole();
      adminRole.setRoleCode("ADMIN");

      user.addRole(userRole);

      // Act & Assert
      assertThat(user.hasRole("USER")).isTrue();
      assertThat(user.hasRole("ADMIN")).isFalse();
      assertThat(user.hasRole("NONEXISTENT")).isFalse();
    }
  }

  @Nested
  @DisplayName("用户状态枚举测试")
  class UserStatusEnumTests {

    @Test
    @DisplayName("应该正确从代码获取状态")
    void shouldGetStatusFromCode() {
      // Act & Assert
      assertThat(User.Status.fromCode(0)).isEqualTo(User.Status.DISABLED);
      assertThat(User.Status.fromCode(1)).isEqualTo(User.Status.ACTIVE);
      assertThat(User.Status.fromCode(2)).isEqualTo(User.Status.PENDING);
    }

    @Test
    @DisplayName("应该在无效状态代码时抛出异常")
    void shouldThrowExceptionForInvalidStatusCode() {
      // Act & Assert
      assertThatThrownBy(() -> User.Status.fromCode(999))
          .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("未知的用户状态代码: 999");
    }

    @Test
    @DisplayName("应该正确获取状态代码和描述")
    void shouldGetStatusCodeAndDescription() {
      // Act & Assert
      assertThat(User.Status.DISABLED.getCode()).isEqualTo(0);
      assertThat(User.Status.DISABLED.getDescription()).isEqualTo("禁用");

      assertThat(User.Status.ACTIVE.getCode()).isEqualTo(1);
      assertThat(User.Status.ACTIVE.getDescription()).isEqualTo("正常");

      assertThat(User.Status.PENDING.getCode()).isEqualTo(2);
      assertThat(User.Status.PENDING.getDescription()).isEqualTo("待验证");
    }
  }

  @Nested
  @DisplayName("用户构建器测试")
  class UserBuilderTests {

    @Test
    @DisplayName("应该使用默认值创建用户")
    void shouldCreateUserWithDefaultValues() {
      // Act
      User newUser = User.builder().username("testuser").email("test@example.com")
          .password("password").build();

      // Assert
      assertThat(newUser.getStatus()).isEqualTo(1); // 默认激活状态
      assertThat(newUser.getEmailVerified()).isFalse(); // 默认未验证
      assertThat(newUser.getTwoFactorEnabled()).isFalse(); // 默认未启用2FA
      assertThat(newUser.getLoginFailureCount()).isEqualTo(0); // 默认失败次数为0
      assertThat(newUser.getPoints()).isEqualTo(BigDecimal.ZERO); // 默认积分为0
      assertThat(newUser.getIsVip()).isFalse(); // 默认非VIP
      assertThat(newUser.getRoles()).isNotNull().isEmpty(); // 默认空角色集合
    }

    @Test
    @DisplayName("应该正确设置所有字段")
    void shouldSetAllFields() {
      // Arrange
      LocalDateTime now = LocalDateTime.now();

      // Act
      User newUser = User.builder().username("testuser").email("test@example.com")
          .password("password").nickname("Test User").phone("13800138000").status(1)
          .emailVerified(true).twoFactorEnabled(true).lastLoginTime(now).lastLoginIp("192.168.1.1")
          .points(new BigDecimal("100.00")).isVip(true).vipExpiresAt(now.plusDays(365))
          .bio("Test bio").website("https://example.com").location("Test Location").build();

      // Assert
      assertThat(newUser.getUsername()).isEqualTo("testuser");
      assertThat(newUser.getEmail()).isEqualTo("test@example.com");
      assertThat(newUser.getPassword()).isEqualTo("password");
      assertThat(newUser.getNickname()).isEqualTo("Test User");
      assertThat(newUser.getPhone()).isEqualTo("13800138000");
      assertThat(newUser.getStatus()).isEqualTo(1);
      assertThat(newUser.getEmailVerified()).isTrue();
      assertThat(newUser.getTwoFactorEnabled()).isTrue();
      assertThat(newUser.getLastLoginTime()).isEqualTo(now);
      assertThat(newUser.getLastLoginIp()).isEqualTo("192.168.1.1");
      assertThat(newUser.getPoints()).isEqualTo(new BigDecimal("100.00"));
      assertThat(newUser.getIsVip()).isTrue();
      assertThat(newUser.getVipExpiresAt()).isEqualTo(now.plusDays(365));
      assertThat(newUser.getBio()).isEqualTo("Test bio");
      assertThat(newUser.getWebsite()).isEqualTo("https://example.com");
      assertThat(newUser.getLocation()).isEqualTo("Test Location");
    }
  }
}
