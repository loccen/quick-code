package com.quickcode.service.impl;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quickcode.entity.Permission;
import com.quickcode.entity.Role;
import com.quickcode.repository.PermissionRepository;
import com.quickcode.repository.RoleRepository;
import com.quickcode.service.RoleService;

/**
 * 角色服务实现类 提供角色管理的核心业务逻辑实现
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Service
@Transactional
public class RoleServiceImpl implements RoleService {

  private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
  }

  @Override
  public Role createRole(Role role) {
    log.debug("创建角色: roleCode={}, roleName={}", role.getRoleCode(), role.getRoleName());

    if (existsByRoleCode(role.getRoleCode())) {
      throw new IllegalArgumentException("角色代码已存在: " + role.getRoleCode());
    }

    Role savedRole = roleRepository.save(role);
    log.info("角色创建成功: id={}, roleCode={}", savedRole.getId(), savedRole.getRoleCode());
    return savedRole;
  }

  @Override
  @Transactional(readOnly = true)
  public Role findById(Long id) {
    log.debug("根据ID查找角色: id={}", id);
    return roleRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Role> findByRoleCode(String roleCode) {
    log.debug("根据角色代码查找角色: roleCode={}", roleCode);
    return roleRepository.findByRoleCode(roleCode);
  }

  @Override
  public Role updateRole(Long id, Role role) {
    log.debug("更新角色: id={}, roleCode={}", id, role.getRoleCode());

    Role existingRole = findById(id);

    // 如果角色代码发生变化，检查新代码是否已存在
    if (!existingRole.getRoleCode().equals(role.getRoleCode())
        && existsByRoleCode(role.getRoleCode())) {
      throw new IllegalArgumentException("角色代码已存在: " + role.getRoleCode());
    }

    existingRole.setRoleCode(role.getRoleCode());
    existingRole.setRoleName(role.getRoleName());
    existingRole.setDescription(role.getDescription());
    existingRole.setStatus(role.getStatus());

    Role updatedRole = roleRepository.save(existingRole);
    log.info("角色更新成功: id={}, roleCode={}", updatedRole.getId(), updatedRole.getRoleCode());
    return updatedRole;
  }

  @Override
  public void deleteRole(Long id) {
    log.debug("删除角色: id={}", id);

    Role role = findById(id);

    if (isRoleInUse(id)) {
      throw new IllegalStateException("角色正在被用户使用，无法删除: " + role.getRoleCode());
    }

    roleRepository.delete(role);
    log.info("角色删除成功: id={}, roleCode={}", id, role.getRoleCode());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Role> findAllRoles(Pageable pageable) {
    log.debug("分页查询角色: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return roleRepository.findAll(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Role> findAllRoles() {
    log.debug("查询所有角色");
    return roleRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Role> findByStatus(Integer status) {
    log.debug("根据状态查找角色: status={}", status);
    return roleRepository.findByStatus(status);
  }

  @Override
  public void assignPermission(Long roleId, Long permissionId) {
    log.debug("为角色分配权限: roleId={}, permissionId={}", roleId, permissionId);

    Role role = findById(roleId);
    Permission permission = permissionRepository.findById(permissionId)
        .orElseThrow(() -> new IllegalArgumentException("权限不存在: " + permissionId));

    role.addPermission(permission);
    roleRepository.save(role);

    log.info("角色权限分配成功: roleId={}, permissionId={}", roleId, permissionId);
  }

  @Override
  public void removePermission(Long roleId, Long permissionId) {
    log.debug("移除角色权限: roleId={}, permissionId={}", roleId, permissionId);

    Role role = findById(roleId);
    Permission permission = permissionRepository.findById(permissionId)
        .orElseThrow(() -> new IllegalArgumentException("权限不存在: " + permissionId));

    role.removePermission(permission);
    roleRepository.save(role);

    log.info("角色权限移除成功: roleId={}, permissionId={}", roleId, permissionId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> getRolePermissions(Long roleId) {
    log.debug("获取角色权限: roleId={}", roleId);

    Role role = findById(roleId);
    return role.getPermissionCodes().stream().toList();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasPermission(Long roleId, String permissionCode) {
    log.debug("检查角色权限: roleId={}, permissionCode={}", roleId, permissionCode);

    Role role = findById(roleId);
    return role.hasPermission(permissionCode);
  }

  @Override
  @Transactional(readOnly = true)
  public long countRoles() {
    log.debug("统计角色数量");
    return roleRepository.count();
  }

  @Override
  @Transactional(readOnly = true)
  public long countRolesByStatus(Integer status) {
    log.debug("根据状态统计角色数量: status={}", status);
    return roleRepository.countByStatus(status);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByRoleCode(String roleCode) {
    log.debug("检查角色代码是否存在: roleCode={}", roleCode);
    return roleRepository.existsByRoleCode(roleCode);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isRoleInUse(Long roleId) {
    log.debug("检查角色是否被使用: roleId={}", roleId);
    return roleRepository.countUsersByRoleId(roleId) > 0;
  }
}
