package com.quickcode.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.quickcode.entity.Role;

/**
 * 角色Repository接口 提供角色相关的数据访问方法
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface RoleRepository extends BaseRepository<Role, Long> {

  /**
   * 根据角色代码查找角色
   */
  Optional<Role> findByRoleCode(String roleCode);

  /**
   * 根据角色名称查找角色
   */
  Optional<Role> findByRoleName(String roleName);

  /**
   * 检查角色代码是否存在
   */
  boolean existsByRoleCode(String roleCode);

  /**
   * 检查角色名称是否存在
   */
  boolean existsByRoleName(String roleName);

  /**
   * 根据状态查找角色
   */
  List<Role> findByStatus(Integer status);

  /**
   * 查找激活状态的角色
   */
  @Query("SELECT r FROM Role r WHERE r.status = 1")
  List<Role> findActiveRoles();

  /**
   * 根据角色代码列表查找角色
   */
  @Query("SELECT r FROM Role r WHERE r.roleCode IN :roleCodes")
  List<Role> findByRoleCodeIn(@Param("roleCodes") Set<String> roleCodes);

  /**
   * 查找拥有指定权限的角色
   */
  @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.permissionCode = :permissionCode")
  List<Role> findByPermissionCode(@Param("permissionCode") String permissionCode);

  /**
   * 查找用户拥有的角色
   */
  @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
  List<Role> findByUserId(@Param("userId") Long userId);

  /**
   * 查找没有用户的角色
   */
  @Query("SELECT r FROM Role r WHERE r.users IS EMPTY")
  List<Role> findRolesWithoutUsers();

  /**
   * 查找没有权限的角色
   */
  @Query("SELECT r FROM Role r WHERE r.permissions IS EMPTY")
  List<Role> findRolesWithoutPermissions();

  /**
   * 统计角色总数
   */
  @Query("SELECT COUNT(r) FROM Role r")
  Long countAllRoles();

  /**
   * 根据状态统计角色数量
   */
  @Query("SELECT COUNT(r) FROM Role r WHERE r.status = :status")
  Long countByStatus(@Param("status") Integer status);

  /**
   * 统计拥有用户的角色数量
   */
  @Query("SELECT COUNT(DISTINCT r) FROM Role r WHERE r.users IS NOT EMPTY")
  Long countRolesWithUsers();

  /**
   * 统计拥有权限的角色数量
   */
  @Query("SELECT COUNT(DISTINCT r) FROM Role r WHERE r.permissions IS NOT EMPTY")
  Long countRolesWithPermissions();

  /**
   * 统计使用指定角色的用户数量
   */
  @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
  Long countUsersByRoleId(@Param("roleId") Long roleId);
}
