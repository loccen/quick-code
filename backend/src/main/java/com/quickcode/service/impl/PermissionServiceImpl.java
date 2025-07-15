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
import com.quickcode.repository.PermissionRepository;
import com.quickcode.service.PermissionService;

/**
 * 权限服务实现类 提供权限管理的核心业务逻辑实现
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

  private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

  private final PermissionRepository permissionRepository;

  public PermissionServiceImpl(PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  @Override
  public Permission createPermission(Permission permission) {
    log.debug("创建权限: permissionCode={}, permissionName={}", permission.getPermissionCode(),
        permission.getPermissionName());

    if (existsByPermissionCode(permission.getPermissionCode())) {
      throw com.quickcode.common.exception.DuplicateResourceException.permissionCodeExists(permission.getPermissionCode());
    }

    Permission savedPermission = permissionRepository.save(permission);
    log.info("权限创建成功: id={}, permissionCode={}", savedPermission.getId(),
        savedPermission.getPermissionCode());
    return savedPermission;
  }

  @Override
  @Transactional(readOnly = true)
  public Permission findById(Long id) {
    log.debug("根据ID查找权限: id={}", id);
    return permissionRepository.findById(id)
        .orElseThrow(() -> com.quickcode.common.exception.ResourceNotFoundException.permission(id));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Permission> findByPermissionCode(String permissionCode) {
    log.debug("根据权限代码查找权限: permissionCode={}", permissionCode);
    return permissionRepository.findByPermissionCode(permissionCode);
  }

  @Override
  public Permission updatePermission(Long id, Permission permission) {
    log.debug("更新权限: id={}, permissionCode={}", id, permission.getPermissionCode());

    Permission existingPermission = findById(id);

    // 如果权限代码发生变化，检查新代码是否已存在
    if (!existingPermission.getPermissionCode().equals(permission.getPermissionCode())
        && existsByPermissionCode(permission.getPermissionCode())) {
      throw com.quickcode.common.exception.DuplicateResourceException.permissionCodeExists(permission.getPermissionCode());
    }

    existingPermission.setPermissionCode(permission.getPermissionCode());
    existingPermission.setPermissionName(permission.getPermissionName());
    existingPermission.setDescription(permission.getDescription());
    existingPermission.setResourceType(permission.getResourceType());
    existingPermission.setActionType(permission.getActionType());
    existingPermission.setStatus(permission.getStatus());

    Permission updatedPermission = permissionRepository.save(existingPermission);
    log.info("权限更新成功: id={}, permissionCode={}", updatedPermission.getId(),
        updatedPermission.getPermissionCode());
    return updatedPermission;
  }

  @Override
  public void deletePermission(Long id) {
    log.debug("删除权限: id={}", id);

    Permission permission = findById(id);

    if (isPermissionInUse(id)) {
      throw com.quickcode.common.exception.InvalidStateException
          .withCode(400, "权限正在被角色使用，无法删除: " + permission.getPermissionCode());
    }

    permissionRepository.delete(permission);
    log.info("权限删除成功: id={}, permissionCode={}", id, permission.getPermissionCode());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Permission> findAllPermissions(Pageable pageable) {
    log.debug("分页查询权限: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return permissionRepository.findAll(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Permission> findAllPermissions() {
    log.debug("查询所有权限");
    return permissionRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Permission> findByStatus(Integer status) {
    log.debug("根据状态查找权限: status={}", status);
    return permissionRepository.findByStatus(status);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Permission> findByPermissionType(String permissionType) {
    log.debug("根据权限类型查找权限: permissionType={}", permissionType);
    return permissionRepository.findByResourceType(permissionType);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Permission> findByResource(String resource) {
    log.debug("根据资源查找权限: resource={}", resource);
    return permissionRepository.findByResourceType(resource);
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> getPermissionRoles(Long permissionId) {
    log.debug("获取权限的角色: permissionId={}", permissionId);

    Permission permission = findById(permissionId);
    return permission.getRoles().stream().map(role -> role.getRoleCode()).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public long countPermissions() {
    log.debug("统计权限数量");
    return permissionRepository.count();
  }

  @Override
  @Transactional(readOnly = true)
  public long countPermissionsByStatus(Integer status) {
    log.debug("根据状态统计权限数量: status={}", status);
    return permissionRepository.countByStatus(status);
  }

  @Override
  @Transactional(readOnly = true)
  public long countPermissionsByType(String permissionType) {
    log.debug("根据类型统计权限数量: permissionType={}", permissionType);
    return permissionRepository.countByPermissionType(permissionType);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByPermissionCode(String permissionCode) {
    log.debug("检查权限代码是否存在: permissionCode={}", permissionCode);
    return permissionRepository.existsByPermissionCode(permissionCode);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isPermissionInUse(Long permissionId) {
    log.debug("检查权限是否被使用: permissionId={}", permissionId);
    return permissionRepository.countRolesByPermissionId(permissionId) > 0;
  }

  @Override
  public List<Permission> createPermissions(List<Permission> permissions) {
    log.debug("批量创建权限: count={}", permissions.size());

    // 检查权限代码是否重复
    for (Permission permission : permissions) {
      if (existsByPermissionCode(permission.getPermissionCode())) {
        throw com.quickcode.common.exception.DuplicateResourceException.permissionCodeExists(permission.getPermissionCode());
      }
    }

    List<Permission> savedPermissions = permissionRepository.saveAll(permissions);
    log.info("批量权限创建成功: count={}", savedPermissions.size());
    return savedPermissions;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Permission> findByPermissionCodes(List<String> permissionCodes) {
    log.debug("根据权限代码列表查找权限: codes={}", permissionCodes);
    return permissionRepository.findByPermissionCodeIn(permissionCodes);
  }
}
