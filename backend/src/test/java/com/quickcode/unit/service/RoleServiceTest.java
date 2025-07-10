package com.quickcode.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.quickcode.entity.Permission;
import com.quickcode.entity.Role;
import com.quickcode.repository.PermissionRepository;
import com.quickcode.repository.RoleRepository;
import com.quickcode.service.impl.RoleServiceImpl;
import com.quickcode.testutil.TestDataFactory;

/**
 * RoleService单元测试 验证角色服务层的业务逻辑
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService单元测试")
class RoleServiceTest {

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PermissionRepository permissionRepository;

  @InjectMocks
  private RoleServiceImpl roleService;

  private Role testRole;
  private Permission testPermission;

  @BeforeEach
  void setUp() {
    testRole = TestDataFactory.createTestRole();
    testRole.setId(1L);
    testPermission = TestDataFactory.createTestPermission();
    testPermission.setId(1L);
  }

  @Nested
  @DisplayName("角色创建测试")
  class CreateRoleTests {

    @Test
    @DisplayName("应该成功创建角色")
    void shouldCreateRoleSuccessfully() {
      // Arrange
      Role newRole = TestDataFactory.createTestRole();
      newRole.setRoleCode("ADMIN");

      when(roleRepository.existsByRoleCode(newRole.getRoleCode())).thenReturn(false);
      when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
        Role role = invocation.getArgument(0);
        role.setId(1L);
        return role;
      });

      // Act
      Role result = roleService.createRole(newRole);

      // Assert
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getRoleCode()).isEqualTo("ADMIN");
      verify(roleRepository).existsByRoleCode(newRole.getRoleCode());
      verify(roleRepository).save(newRole);
    }

    @Test
    @DisplayName("应该在角色代码已存在时抛出异常")
    void shouldThrowExceptionWhenRoleCodeExists() {
      // Arrange
      Role newRole = TestDataFactory.createTestRole();
      newRole.setRoleCode("USER");

      when(roleRepository.existsByRoleCode(newRole.getRoleCode())).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> roleService.createRole(newRole))
          .isInstanceOf(IllegalArgumentException.class).hasMessage("角色代码已存在: USER");

      verify(roleRepository).existsByRoleCode(newRole.getRoleCode());
      verify(roleRepository, never()).save(any(Role.class));
    }
  }

  @Nested
  @DisplayName("角色查询测试")
  class FindRoleTests {

    @Test
    @DisplayName("应该根据ID成功查找角色")
    void shouldFindRoleByIdSuccessfully() {
      // Arrange
      Long roleId = testRole.getId();

      when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));

      // Act
      Role result = roleService.findById(roleId);

      // Assert
      assertThat(result.getId()).isEqualTo(roleId);
      assertThat(result.getRoleCode()).isEqualTo(testRole.getRoleCode());
      verify(roleRepository).findById(roleId);
    }

    @Test
    @DisplayName("应该在角色不存在时抛出异常")
    void shouldThrowExceptionWhenRoleNotFound() {
      // Arrange
      Long roleId = 999L;

      when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> roleService.findById(roleId))
          .isInstanceOf(IllegalArgumentException.class).hasMessage("角色不存在: " + roleId);

      verify(roleRepository).findById(roleId);
    }

    @Test
    @DisplayName("应该根据角色代码成功查找角色")
    void shouldFindRoleByCodeSuccessfully() {
      // Arrange
      String roleCode = testRole.getRoleCode();

      when(roleRepository.findByRoleCode(roleCode)).thenReturn(Optional.of(testRole));

      // Act
      Optional<Role> result = roleService.findByRoleCode(roleCode);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getRoleCode()).isEqualTo(roleCode);
      verify(roleRepository).findByRoleCode(roleCode);
    }

    @Test
    @DisplayName("应该分页查询所有角色")
    void shouldFindAllRolesWithPagination() {
      // Arrange
      Pageable pageable = PageRequest.of(0, 10);
      Page<Role> expectedPage = new PageImpl<>(List.of(testRole), pageable, 1);

      when(roleRepository.findAll(pageable)).thenReturn(expectedPage);

      // Act
      Page<Role> result = roleService.findAllRoles(pageable);

      // Assert
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getId()).isEqualTo(testRole.getId());
      verify(roleRepository).findAll(pageable);
    }

    @Test
    @DisplayName("应该查询所有角色")
    void shouldFindAllRoles() {
      // Arrange
      List<Role> expectedRoles = List.of(testRole);

      when(roleRepository.findAll()).thenReturn(expectedRoles);

      // Act
      List<Role> result = roleService.findAllRoles();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testRole.getId());
      verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("应该根据状态查找角色")
    void shouldFindRolesByStatus() {
      // Arrange
      Integer status = 1;
      List<Role> expectedRoles = List.of(testRole);

      when(roleRepository.findByStatus(status)).thenReturn(expectedRoles);

      // Act
      List<Role> result = roleService.findByStatus(status);

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testRole.getId());
      verify(roleRepository).findByStatus(status);
    }
  }

  @Nested
  @DisplayName("角色更新测试")
  class UpdateRoleTests {

    @Test
    @DisplayName("应该成功更新角色")
    void shouldUpdateRoleSuccessfully() {
      // Arrange
      Long roleId = testRole.getId();
      Role updateRole = TestDataFactory.createTestRole();
      updateRole.setRoleCode("UPDATED_USER");
      updateRole.setRoleName("Updated User");

      when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
      when(roleRepository.existsByRoleCode(updateRole.getRoleCode())).thenReturn(false);
      when(roleRepository.save(any(Role.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      Role result = roleService.updateRole(roleId, updateRole);

      // Assert
      assertThat(result.getRoleCode()).isEqualTo("UPDATED_USER");
      assertThat(result.getRoleName()).isEqualTo("Updated User");
      verify(roleRepository).findById(roleId);
      verify(roleRepository).existsByRoleCode(updateRole.getRoleCode());
      verify(roleRepository).save(testRole);
    }

    @Test
    @DisplayName("应该在更新时角色代码已存在时抛出异常")
    void shouldThrowExceptionWhenUpdatingToExistingRoleCode() {
      // Arrange
      Long roleId = testRole.getId();
      Role updateRole = TestDataFactory.createTestRole();
      updateRole.setRoleCode("EXISTING_CODE");

      when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
      when(roleRepository.existsByRoleCode(updateRole.getRoleCode())).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> roleService.updateRole(roleId, updateRole))
          .isInstanceOf(IllegalArgumentException.class).hasMessage("角色代码已存在: EXISTING_CODE");

      verify(roleRepository).findById(roleId);
      verify(roleRepository).existsByRoleCode(updateRole.getRoleCode());
      verify(roleRepository, never()).save(any(Role.class));
    }
  }

  @Nested
  @DisplayName("角色删除测试")
  class DeleteRoleTests {

    @Test
    @DisplayName("应该成功删除角色")
    void shouldDeleteRoleSuccessfully() {
      // Arrange
      Long roleId = testRole.getId();

      when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
      when(roleRepository.countUsersByRoleId(roleId)).thenReturn(0L);

      // Act
      roleService.deleteRole(roleId);

      // Assert
      verify(roleRepository).findById(roleId);
      verify(roleRepository).countUsersByRoleId(roleId);
      verify(roleRepository).delete(testRole);
    }

    @Test
    @DisplayName("应该在角色被使用时抛出异常")
    void shouldThrowExceptionWhenRoleInUse() {
      // Arrange
      Long roleId = testRole.getId();

      when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
      when(roleRepository.countUsersByRoleId(roleId)).thenReturn(5L);

      // Act & Assert
      assertThatThrownBy(() -> roleService.deleteRole(roleId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("角色正在被用户使用，无法删除: " + testRole.getRoleCode());

      verify(roleRepository).findById(roleId);
      verify(roleRepository).countUsersByRoleId(roleId);
      verify(roleRepository, never()).delete(any(Role.class));
    }
  }

  @Nested
  @DisplayName("角色权限管理测试")
  class RolePermissionTests {

    @Test
    @DisplayName("应该成功为角色分配权限")
    void shouldAssignPermissionToRoleSuccessfully() {
      // Arrange
      Long roleId = testRole.getId();
      Long permissionId = testPermission.getId();

      when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
      when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));
      when(roleRepository.save(any(Role.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      roleService.assignPermission(roleId, permissionId);

      // Assert
      verify(roleRepository).findById(roleId);
      verify(permissionRepository).findById(permissionId);
      verify(roleRepository).save(testRole);
    }

    @Test
    @DisplayName("应该在权限不存在时抛出异常")
    void shouldThrowExceptionWhenPermissionNotFoundForAssignment() {
      // Arrange
      Long roleId = testRole.getId();
      Long permissionId = 999L;

      when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
      when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> roleService.assignPermission(roleId, permissionId))
          .isInstanceOf(IllegalArgumentException.class).hasMessage("权限不存在: " + permissionId);

      verify(roleRepository).findById(roleId);
      verify(permissionRepository).findById(permissionId);
      verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("应该成功移除角色权限")
    void shouldRemovePermissionFromRoleSuccessfully() {
      // Arrange
      Long roleId = testRole.getId();
      Long permissionId = testPermission.getId();
      testRole.addPermission(testPermission); // 先添加权限

      when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
      when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));
      when(roleRepository.save(any(Role.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      roleService.removePermission(roleId, permissionId);

      // Assert
      verify(roleRepository).findById(roleId);
      verify(permissionRepository).findById(permissionId);
      verify(roleRepository).save(testRole);
    }

    @Test
    @DisplayName("应该正确获取角色权限")
    void shouldGetRolePermissionsCorrectly() {
      // Arrange
      Long roleId = testRole.getId();
      testRole.addPermission(testPermission);

      when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));

      // Act
      List<String> permissions = roleService.getRolePermissions(roleId);

      // Assert
      assertThat(permissions).contains(testPermission.getPermissionCode());
      verify(roleRepository).findById(roleId);
    }

    @Test
    @DisplayName("应该正确检查角色权限")
    void shouldCheckRolePermissionCorrectly() {
      // Arrange
      Long roleId = testRole.getId();
      String permissionCode = testPermission.getPermissionCode();
      testRole.addPermission(testPermission);

      when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));

      // Act
      boolean hasPermission = roleService.hasPermission(roleId, permissionCode);

      // Assert
      assertThat(hasPermission).isTrue();
      verify(roleRepository).findById(roleId);
    }
  }

  @Nested
  @DisplayName("角色统计测试")
  class RoleStatisticsTests {

    @Test
    @DisplayName("应该正确统计角色总数")
    void shouldCountRolesCorrectly() {
      // Arrange
      long expectedCount = 10L;

      when(roleRepository.count()).thenReturn(expectedCount);

      // Act
      long result = roleService.countRoles();

      // Assert
      assertThat(result).isEqualTo(expectedCount);
      verify(roleRepository).count();
    }

    @Test
    @DisplayName("应该根据状态正确统计角色数量")
    void shouldCountRolesByStatusCorrectly() {
      // Arrange
      Integer status = 1;
      long expectedCount = 5L;

      when(roleRepository.countByStatus(status)).thenReturn(expectedCount);

      // Act
      long result = roleService.countRolesByStatus(status);

      // Assert
      assertThat(result).isEqualTo(expectedCount);
      verify(roleRepository).countByStatus(status);
    }

    @Test
    @DisplayName("应该正确检查角色代码是否存在")
    void shouldCheckRoleCodeExistsCorrectly() {
      // Arrange
      String roleCode = "USER";

      when(roleRepository.existsByRoleCode(roleCode)).thenReturn(true);

      // Act
      boolean exists = roleService.existsByRoleCode(roleCode);

      // Assert
      assertThat(exists).isTrue();
      verify(roleRepository).existsByRoleCode(roleCode);
    }

    @Test
    @DisplayName("应该正确检查角色是否被使用")
    void shouldCheckRoleInUseCorrectly() {
      // Arrange
      Long roleId = testRole.getId();

      when(roleRepository.countUsersByRoleId(roleId)).thenReturn(3L);

      // Act
      boolean inUse = roleService.isRoleInUse(roleId);

      // Assert
      assertThat(inUse).isTrue();
      verify(roleRepository).countUsersByRoleId(roleId);
    }
  }
}
