package com.quickcode.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quickcode.entity.Permission;

/**
 * 权限服务接口
 * 提供权限管理的核心业务逻辑
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface PermissionService {

    /**
     * 创建权限
     *
     * @param permission 权限信息
     * @return 创建的权限
     * @throws IllegalArgumentException 当权限代码已存在时
     */
    Permission createPermission(Permission permission);

    /**
     * 根据ID查找权限
     *
     * @param id 权限ID
     * @return 权限信息
     * @throws IllegalArgumentException 当权限不存在时
     */
    Permission findById(Long id);

    /**
     * 根据权限代码查找权限
     *
     * @param permissionCode 权限代码
     * @return 权限信息（可能为空）
     */
    Optional<Permission> findByPermissionCode(String permissionCode);

    /**
     * 更新权限信息
     *
     * @param id 权限ID
     * @param permission 更新的权限信息
     * @return 更新后的权限
     * @throws IllegalArgumentException 当权限不存在时
     */
    Permission updatePermission(Long id, Permission permission);

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @throws IllegalArgumentException 当权限不存在时
     * @throws IllegalStateException 当权限仍被角色使用时
     */
    void deletePermission(Long id);

    /**
     * 分页查询所有权限
     *
     * @param pageable 分页参数
     * @return 权限分页结果
     */
    Page<Permission> findAllPermissions(Pageable pageable);

    /**
     * 查询所有权限
     *
     * @return 所有权限列表
     */
    List<Permission> findAllPermissions();

    /**
     * 根据状态查找权限
     *
     * @param status 权限状态
     * @return 权限列表
     */
    List<Permission> findByStatus(Integer status);

    /**
     * 根据权限类型查找权限
     *
     * @param permissionType 权限类型
     * @return 权限列表
     */
    List<Permission> findByPermissionType(String permissionType);

    /**
     * 根据资源查找权限
     *
     * @param resource 资源名称
     * @return 权限列表
     */
    List<Permission> findByResource(String resource);

    /**
     * 获取权限的所有角色
     *
     * @param permissionId 权限ID
     * @return 角色代码列表
     * @throws IllegalArgumentException 当权限不存在时
     */
    List<String> getPermissionRoles(Long permissionId);

    /**
     * 统计权限数量
     *
     * @return 权限总数
     */
    long countPermissions();

    /**
     * 根据状态统计权限数量
     *
     * @param status 权限状态
     * @return 权限数量
     */
    long countPermissionsByStatus(Integer status);

    /**
     * 根据类型统计权限数量
     *
     * @param permissionType 权限类型
     * @return 权限数量
     */
    long countPermissionsByType(String permissionType);

    /**
     * 检查权限代码是否存在
     *
     * @param permissionCode 权限代码
     * @return 是否存在
     */
    boolean existsByPermissionCode(String permissionCode);

    /**
     * 检查权限是否被角色使用
     *
     * @param permissionId 权限ID
     * @return 是否被使用
     */
    boolean isPermissionInUse(Long permissionId);

    /**
     * 批量创建权限
     *
     * @param permissions 权限列表
     * @return 创建的权限列表
     */
    List<Permission> createPermissions(List<Permission> permissions);

    /**
     * 根据权限代码列表查找权限
     *
     * @param permissionCodes 权限代码列表
     * @return 权限列表
     */
    List<Permission> findByPermissionCodes(List<String> permissionCodes);
}
