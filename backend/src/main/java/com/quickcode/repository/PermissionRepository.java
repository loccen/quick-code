package com.quickcode.repository;

import com.quickcode.entity.Permission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 权限Repository接口
 * 提供权限相关的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface PermissionRepository extends BaseRepository<Permission, Long> {

    /**
     * 根据权限代码查找权限
     */
    Optional<Permission> findByPermissionCode(String permissionCode);

    /**
     * 根据权限名称查找权限
     */
    Optional<Permission> findByPermissionName(String permissionName);

    /**
     * 检查权限代码是否存在
     */
    boolean existsByPermissionCode(String permissionCode);

    /**
     * 检查权限名称是否存在
     */
    boolean existsByPermissionName(String permissionName);

    /**
     * 根据状态查找权限
     */
    List<Permission> findByStatus(Integer status);

    /**
     * 查找激活状态的权限
     */
    @Query("SELECT p FROM Permission p WHERE p.status = 1")
    List<Permission> findActivePermissions();

    /**
     * 根据资源类型查找权限
     */
    List<Permission> findByResourceType(String resourceType);

    /**
     * 根据操作类型查找权限
     */
    List<Permission> findByActionType(String actionType);

    /**
     * 根据资源类型和操作类型查找权限
     */
    List<Permission> findByResourceTypeAndActionType(String resourceType, String actionType);

    /**
     * 根据权限代码列表查找权限
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionCode IN :permissionCodes")
    List<Permission> findByPermissionCodeIn(@Param("permissionCodes") Set<String> permissionCodes);

    /**
     * 查找角色拥有的权限
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 查找用户拥有的权限（通过角色）
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * 查找没有角色的权限
     */
    @Query("SELECT p FROM Permission p WHERE p.roles IS EMPTY")
    List<Permission> findPermissionsWithoutRoles();

    /**
     * 根据资源类型分组统计权限数量
     */
    @Query("SELECT p.resourceType, COUNT(p) FROM Permission p GROUP BY p.resourceType")
    List<Object[]> countByResourceType();

    /**
     * 根据操作类型分组统计权限数量
     */
    @Query("SELECT p.actionType, COUNT(p) FROM Permission p GROUP BY p.actionType")
    List<Object[]> countByActionType();

    /**
     * 统计权限总数
     */
    @Query("SELECT COUNT(p) FROM Permission p")
    Long countAllPermissions();

    /**
     * 根据状态统计权限数量
     */
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.status = :status")
    Long countByStatus(@Param("status") Integer status);

    /**
     * 统计拥有角色的权限数量
     */
    @Query("SELECT COUNT(DISTINCT p) FROM Permission p WHERE p.roles IS NOT EMPTY")
    Long countPermissionsWithRoles();
}
