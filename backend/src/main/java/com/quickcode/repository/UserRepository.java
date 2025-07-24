package com.quickcode.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.quickcode.entity.User;

/**
 * 用户Repository接口 提供用户相关的数据访问方法
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends BaseRepository<User, Long> {

  /**
   * 根据用户名查找用户（包含角色和权限信息）
   */
  @EntityGraph(attributePaths = {"roles", "roles.permissions"})
  Optional<User> findByUsername(String username);

  /**
   * 根据邮箱查找用户（包含角色和权限信息）
   */
  @EntityGraph(attributePaths = {"roles", "roles.permissions"})
  Optional<User> findByEmail(String email);

  /**
   * 根据用户名或邮箱查找用户（包含角色和权限信息）
   */
  @EntityGraph(attributePaths = {"roles", "roles.permissions"})
  @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
  Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

  /**
   * 根据ID查找用户（包含角色和权限信息）
   */
  @EntityGraph(attributePaths = {"roles", "roles.permissions"})
  Optional<User> findById(Long id);

  /**
   * 检查用户名是否存在
   */
  boolean existsByUsername(String username);

  /**
   * 检查邮箱是否存在
   */
  boolean existsByEmail(String email);

  /**
   * 根据状态查找用户
   */
  List<User> findByStatus(Integer status);

  /**
   * 查找已验证邮箱的用户
   */
  List<User> findByEmailVerified(Boolean emailVerified);

  /**
   * 查找启用双因素认证的用户
   */
  List<User> findByTwoFactorEnabled(Boolean twoFactorEnabled);

  /**
   * 查找被锁定的用户
   */
  @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > :now")
  List<User> findLockedUsers(@Param("now") LocalDateTime now);

  /**
   * 查找登录失败次数超过指定次数的用户
   */
  List<User> findByLoginFailureCountGreaterThan(Integer count);

  /**
   * 根据管理员状态查找用户
   */
  List<User> findByIsAdmin(Boolean isAdmin);

  /**
   * 根据创建时间范围查找用户
   */
  @Query("SELECT u FROM User u WHERE u.createdTime BETWEEN :startTime AND :endTime")
  List<User> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  /**
   * 根据最后登录时间范围查找用户
   */
  @Query("SELECT u FROM User u WHERE u.lastLoginTime BETWEEN :startTime AND :endTime")
  List<User> findByLastLoginTimeBetween(@Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  /**
   * 查找长时间未登录的用户
   */
  @Query("SELECT u FROM User u WHERE u.lastLoginTime < :cutoffTime OR u.lastLoginTime IS NULL")
  List<User> findInactiveUsers(@Param("cutoffTime") LocalDateTime cutoffTime);

  /**
   * 统计用户总数
   */
  @Query("SELECT COUNT(u) FROM User u")
  Long countAllUsers();

  /**
   * 根据状态统计用户数量
   */
  @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
  Long countByStatus(@Param("status") Integer status);

  /**
   * 统计已验证邮箱的用户数量
   */
  @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true")
  Long countVerifiedUsers();

  /**
   * 统计启用双因素认证的用户数量
   */
  @Query("SELECT COUNT(u) FROM User u WHERE u.twoFactorEnabled = true")
  Long countTwoFactorEnabledUsers();

  /**
   * 根据注册时间统计用户数量
   */
  @Query("SELECT COUNT(u) FROM User u WHERE u.createdTime >= :startTime")
  Long countNewUsers(@Param("startTime") LocalDateTime startTime);

  /**
   * 根据邮箱验证令牌查找用户
   */
  Optional<User> findByEmailVerificationToken(String emailVerificationToken);

  /**
   * 根据密码重置令牌查找用户
   */
  Optional<User> findByPasswordResetToken(String passwordResetToken);
}
