package com.quickcode.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.quickcode.entity.Role;

/**
 * 角色服务接口
 * 提供角色管理的核心业务逻辑
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface RoleService {

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @return 创建的角色
     * @throws IllegalArgumentException 当角色代码已存在时
     */
    Role createRole(Role role);

    /**
     * 根据ID查找角色
     *
     * @param id 角色ID
     * @return 角色信息
     * @throws IllegalArgumentException 当角色不存在时
     */
    Role findById(Long id);

    /**
     * 根据角色代码查找角色
     *
     * @param roleCode 角色代码
     * @return 角色信息（可能为空）
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 更新角色信息
     *
     * @param id 角色ID
     * @param role 更新的角色信息
     * @return 更新后的角色
     * @throws IllegalArgumentException 当角色不存在时
     */
    Role updateRole(Long id, Role role);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @throws IllegalArgumentException 当角色不存在时
     * @throws IllegalStateException 当角色仍被用户使用时
     */
    void deleteRole(Long id);

    /**
     * 分页查询所有角色
     *
     * @param pageable 分页参数
     * @return 角色分页结果
     */
    Page<Role> findAllRoles(Pageable pageable);

    /**
     * 查询所有角色
     *
     * @return 所有角色列表
     */
    List<Role> findAllRoles();

    /**
     * 根据状态查找角色
     *
     * @param status 角色状态
     * @return 角色列表
     */
    List<Role> findByStatus(Integer status);

    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @throws IllegalArgumentException 当角色或权限不存在时
     */
    void assignPermission(Long roleId, Long permissionId);

    /**
     * 移除角色权限
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @throws IllegalArgumentException 当角色或权限不存在时
     */
    void removePermission(Long roleId, Long permissionId);

    /**
     * 获取角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     * @throws IllegalArgumentException 当角色不存在时
     */
    List<String> getRolePermissions(Long roleId);

    /**
     * 检查角色是否拥有指定权限
     *
     * @param roleId 角色ID
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     * @throws IllegalArgumentException 当角色不存在时
     */
    boolean hasPermission(Long roleId, String permissionCode);

    /**
     * 统计角色数量
     *
     * @return 角色总数
     */
    long countRoles();

    /**
     * 根据状态统计角色数量
     *
     * @param status 角色状态
     * @return 角色数量
     */
    long countRolesByStatus(Integer status);

    /**
     * 检查角色代码是否存在
     *
     * @param roleCode 角色代码
     * @return 是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 检查角色是否被用户使用
     *
     * @param roleId 角色ID
     * @return 是否被使用
     */
    boolean isRoleInUse(Long roleId);
}
