package com.quickcode.testutil;

import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import com.quickcode.security.jwt.UserPrincipal;

/**
 * 测试安全配置 提供测试环境下的安全相关配置
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@TestConfiguration
public class TestSecurityConfig {

  /**
   * 创建测试用的UserPrincipal
   */
  public static UserPrincipal createTestUserPrincipal() {
    return new UserPrincipal(1L, "testuser", "test@example.com", "encoded_password", 1, // status:
                                                                                        // active
        true, // emailVerified
        false, // twoFactorEnabled
        null, // lockedUntil
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }

  /**
   * 创建测试用的管理员UserPrincipal
   */
  public static UserPrincipal createTestAdminPrincipal() {
    return new UserPrincipal(1L, "admin", "admin@example.com", "encoded_password", 1, // status:
                                                                                      // active
        true, // emailVerified
        false, // twoFactorEnabled
        null, // lockedUntil
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
  }

  /**
   * 设置测试用户认证上下文
   */
  public static void setTestUserAuthentication() {
    UserPrincipal userPrincipal = createTestUserPrincipal();
    Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null,
        userPrincipal.getAuthorities());

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }

  /**
   * 设置测试管理员认证上下文
   */
  public static void setTestAdminAuthentication() {
    UserPrincipal userPrincipal = createTestAdminPrincipal();
    Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null,
        userPrincipal.getAuthorities());

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }

  /**
   * 清除认证上下文
   */
  public static void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }
}
