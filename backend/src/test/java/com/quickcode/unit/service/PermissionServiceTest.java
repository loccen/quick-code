package com.quickcode.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import com.quickcode.service.impl.PermissionServiceImpl;
import com.quickcode.testutil.TestDataFactory;

/**
 * PermissionService单元测试 验证权限服务层的业务逻辑
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService单元测试")
class PermissionServiceTest {

  @Mock
  private PermissionRepository permissionRepository;

  @InjectMocks
  private PermissionServiceImpl permissionService;

  private Permission testPermission;
  private Role testRole;

  @BeforeEach
  void setUp() {
    testPermission = TestDataFactory.createTestPermission();
    testPermission.setId(1L);
    testRole = TestDataFactory.createTestRole();
    testRole.setId(1L);
  }

  @Nested
  @DisplayName("权限创建测试")
  class CreatePermissionTests {

    @Test
    @DisplayName("应该成功创建权限")
    void shouldCreatePermissionSuccessfully() {
      // Arrange
      Permission newPermission = TestDataFactory.createTestPermission();
      newPermission.setPermissionCode("admin:manage");

      when(permissionRepository.existsByPermissionCode(newPermission.getPermissionCode()))
          .thenReturn(false);
      when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
        Permission permission = invocation.getArgument(0);
        permission.setId(1L);
        return permission;
      });

      // Act
      Permission result = permissionService.createPermission(newPermission);

      // Assert
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getPermissionCode()).isEqualTo("admin:manage");
      verify(permissionRepository).existsByPermissionCode(newPermission.getPermissionCode());
      verify(permissionRepository).save(newPermission);
    }

    @Test
    @DisplayName("应该在权限代码已存在时抛出异常")
    void shouldThrowExceptionWhenPermissionCodeExists() {
      // Arrange
      Permission newPermission = TestDataFactory.createTestPermission();
      newPermission.setPermissionCode("user:read");

      when(permissionRepository.existsByPermissionCode(newPermission.getPermissionCode()))
          .thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> permissionService.createPermission(newPermission))
          .isInstanceOf(com.quickcode.common.exception.DuplicateResourceException.class)
          .hasMessage("权限代码已存在: user:read");

      verify(permissionRepository).existsByPermissionCode(newPermission.getPermissionCode());
      verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    @DisplayName("应该成功批量创建权限")
    void shouldCreatePermissionsBatchSuccessfully() {
      // Arrange
      Permission permission1 = TestDataFactory.createTestPermission();
      permission1.setPermissionCode("user:create");
      Permission permission2 = TestDataFactory.createTestPermission();
      permission2.setPermissionCode("user:update");
      List<Permission> permissions = List.of(permission1, permission2);

      when(permissionRepository.existsByPermissionCode("user:create")).thenReturn(false);
      when(permissionRepository.existsByPermissionCode("user:update")).thenReturn(false);
      when(permissionRepository.saveAll(anyList())).thenAnswer(invocation -> {
        List<Permission> perms = invocation.getArgument(0);
        for (int i = 0; i < perms.size(); i++) {
          perms.get(i).setId((long) (i + 1));
        }
        return perms;
      });

      // Act
      List<Permission> result = permissionService.createPermissions(permissions);

      // Assert
      assertThat(result).hasSize(2);
      assertThat(result.get(0).getId()).isEqualTo(1L);
      assertThat(result.get(1).getId()).isEqualTo(2L);
      verify(permissionRepository).existsByPermissionCode("user:create");
      verify(permissionRepository).existsByPermissionCode("user:update");
      verify(permissionRepository).saveAll(permissions);
    }
  }

  @Nested
  @DisplayName("权限查询测试")
  class FindPermissionTests {

    @Test
    @DisplayName("应该根据ID成功查找权限")
    void shouldFindPermissionByIdSuccessfully() {
      // Arrange
      Long permissionId = testPermission.getId();

      when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));

      // Act
      Permission result = permissionService.findById(permissionId);

      // Assert
      assertThat(result.getId()).isEqualTo(permissionId);
      assertThat(result.getPermissionCode()).isEqualTo(testPermission.getPermissionCode());
      verify(permissionRepository).findById(permissionId);
    }

    @Test
    @DisplayName("应该在权限不存在时抛出异常")
    void shouldThrowExceptionWhenPermissionNotFound() {
      // Arrange
      Long permissionId = 999L;

      when(permissionRepository.findById(permissionId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> permissionService.findById(permissionId))
          .isInstanceOf(com.quickcode.common.exception.ResourceNotFoundException.class)
          .hasMessage("权限 (ID: " + permissionId + ") 不存在");

      verify(permissionRepository).findById(permissionId);
    }

    @Test
    @DisplayName("应该根据权限代码成功查找权限")
    void shouldFindPermissionByCodeSuccessfully() {
      // Arrange
      String permissionCode = testPermission.getPermissionCode();

      when(permissionRepository.findByPermissionCode(permissionCode))
          .thenReturn(Optional.of(testPermission));

      // Act
      Optional<Permission> result = permissionService.findByPermissionCode(permissionCode);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getPermissionCode()).isEqualTo(permissionCode);
      verify(permissionRepository).findByPermissionCode(permissionCode);
    }

    @Test
    @DisplayName("应该分页查询所有权限")
    void shouldFindAllPermissionsWithPagination() {
      // Arrange
      Pageable pageable = PageRequest.of(0, 10);
      Page<Permission> expectedPage = new PageImpl<>(List.of(testPermission), pageable, 1);

      when(permissionRepository.findAll(pageable)).thenReturn(expectedPage);

      // Act
      Page<Permission> result = permissionService.findAllPermissions(pageable);

      // Assert
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getId()).isEqualTo(testPermission.getId());
      verify(permissionRepository).findAll(pageable);
    }

    @Test
    @DisplayName("应该查询所有权限")
    void shouldFindAllPermissions() {
      // Arrange
      List<Permission> expectedPermissions = List.of(testPermission);

      when(permissionRepository.findAll()).thenReturn(expectedPermissions);

      // Act
      List<Permission> result = permissionService.findAllPermissions();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testPermission.getId());
      verify(permissionRepository).findAll();
    }

    @Test
    @DisplayName("应该根据状态查找权限")
    void shouldFindPermissionsByStatus() {
      // Arrange
      Integer status = 1;
      List<Permission> expectedPermissions = List.of(testPermission);

      when(permissionRepository.findByStatus(status)).thenReturn(expectedPermissions);

      // Act
      List<Permission> result = permissionService.findByStatus(status);

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testPermission.getId());
      verify(permissionRepository).findByStatus(status);
    }

    @Test
    @DisplayName("应该根据权限类型查找权限")
    void shouldFindPermissionsByType() {
      // Arrange
      String permissionType = "MENU";
      List<Permission> expectedPermissions = List.of(testPermission);

      when(permissionRepository.findByResourceType(permissionType)).thenReturn(expectedPermissions);

      // Act
      List<Permission> result = permissionService.findByPermissionType(permissionType);

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testPermission.getId());
      verify(permissionRepository).findByResourceType(permissionType);
    }

    @Test
    @DisplayName("应该根据资源查找权限")
    void shouldFindPermissionsByResource() {
      // Arrange
      String resource = "user";
      List<Permission> expectedPermissions = List.of(testPermission);

      when(permissionRepository.findByResourceType(resource)).thenReturn(expectedPermissions);

      // Act
      List<Permission> result = permissionService.findByResource(resource);

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testPermission.getId());
      verify(permissionRepository).findByResourceType(resource);
    }

    @Test
    @DisplayName("应该根据权限代码列表查找权限")
    void shouldFindPermissionsByCodes() {
      // Arrange
      List<String> permissionCodes = List.of("user:read", "user:write");
      List<Permission> expectedPermissions = List.of(testPermission);

      when(permissionRepository.findByPermissionCodeIn(permissionCodes))
          .thenReturn(expectedPermissions);

      // Act
      List<Permission> result = permissionService.findByPermissionCodes(permissionCodes);

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(testPermission.getId());
      verify(permissionRepository).findByPermissionCodeIn(permissionCodes);
    }
  }

  @Nested
  @DisplayName("权限更新测试")
  class UpdatePermissionTests {

    @Test
    @DisplayName("应该成功更新权限")
    void shouldUpdatePermissionSuccessfully() {
      // Arrange
      Long permissionId = testPermission.getId();
      Permission updatePermission = TestDataFactory.createTestPermission();
      updatePermission.setPermissionCode("updated:permission");
      updatePermission.setPermissionName("Updated Permission");

      when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));
      when(permissionRepository.existsByPermissionCode(updatePermission.getPermissionCode()))
          .thenReturn(false);
      when(permissionRepository.save(any(Permission.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      Permission result = permissionService.updatePermission(permissionId, updatePermission);

      // Assert
      assertThat(result.getPermissionCode()).isEqualTo("updated:permission");
      assertThat(result.getPermissionName()).isEqualTo("Updated Permission");
      verify(permissionRepository).findById(permissionId);
      verify(permissionRepository).existsByPermissionCode(updatePermission.getPermissionCode());
      verify(permissionRepository).save(testPermission);
    }

    @Test
    @DisplayName("应该在更新时权限代码已存在时抛出异常")
    void shouldThrowExceptionWhenUpdatingToExistingPermissionCode() {
      // Arrange
      Long permissionId = testPermission.getId();
      Permission updatePermission = TestDataFactory.createTestPermission();
      updatePermission.setPermissionCode("existing:code");

      when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));
      when(permissionRepository.existsByPermissionCode(updatePermission.getPermissionCode()))
          .thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> permissionService.updatePermission(permissionId, updatePermission))
          .isInstanceOf(com.quickcode.common.exception.DuplicateResourceException.class)
          .hasMessage("权限代码已存在: existing:code");

      verify(permissionRepository).findById(permissionId);
      verify(permissionRepository).existsByPermissionCode(updatePermission.getPermissionCode());
      verify(permissionRepository, never()).save(any(Permission.class));
    }
  }

  @Nested
  @DisplayName("权限删除测试")
  class DeletePermissionTests {

    @Test
    @DisplayName("应该成功删除权限")
    void shouldDeletePermissionSuccessfully() {
      // Arrange
      Long permissionId = testPermission.getId();

      when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));
      when(permissionRepository.countRolesByPermissionId(permissionId)).thenReturn(0L);

      // Act
      permissionService.deletePermission(permissionId);

      // Assert
      verify(permissionRepository).findById(permissionId);
      verify(permissionRepository).countRolesByPermissionId(permissionId);
      verify(permissionRepository).delete(testPermission);
    }

    @Test
    @DisplayName("应该在权限被使用时抛出异常")
    void shouldThrowExceptionWhenPermissionInUse() {
      // Arrange
      Long permissionId = testPermission.getId();

      when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));
      when(permissionRepository.countRolesByPermissionId(permissionId)).thenReturn(3L);

      // Act & Assert
      assertThatThrownBy(() -> permissionService.deletePermission(permissionId))
          .isInstanceOf(com.quickcode.common.exception.InvalidStateException.class)
          .hasMessage("权限正在被角色使用，无法删除: " + testPermission.getPermissionCode());

      verify(permissionRepository).findById(permissionId);
      verify(permissionRepository).countRolesByPermissionId(permissionId);
      verify(permissionRepository, never()).delete(any(Permission.class));
    }
  }

  @Nested
  @DisplayName("权限角色管理测试")
  class PermissionRoleTests {

    @Test
    @DisplayName("应该正确获取权限的角色")
    void shouldGetPermissionRolesCorrectly() {
      // Arrange
      Long permissionId = testPermission.getId();
      testPermission.getRoles().add(testRole);

      when(permissionRepository.findById(permissionId)).thenReturn(Optional.of(testPermission));

      // Act
      List<String> roles = permissionService.getPermissionRoles(permissionId);

      // Assert
      assertThat(roles).contains(testRole.getRoleCode());
      verify(permissionRepository).findById(permissionId);
    }
  }

  @Nested
  @DisplayName("权限统计测试")
  class PermissionStatisticsTests {

    @Test
    @DisplayName("应该正确统计权限总数")
    void shouldCountPermissionsCorrectly() {
      // Arrange
      long expectedCount = 15L;

      when(permissionRepository.count()).thenReturn(expectedCount);

      // Act
      long result = permissionService.countPermissions();

      // Assert
      assertThat(result).isEqualTo(expectedCount);
      verify(permissionRepository).count();
    }

    @Test
    @DisplayName("应该根据状态正确统计权限数量")
    void shouldCountPermissionsByStatusCorrectly() {
      // Arrange
      Integer status = 1;
      long expectedCount = 10L;

      when(permissionRepository.countByStatus(status)).thenReturn(expectedCount);

      // Act
      long result = permissionService.countPermissionsByStatus(status);

      // Assert
      assertThat(result).isEqualTo(expectedCount);
      verify(permissionRepository).countByStatus(status);
    }

    @Test
    @DisplayName("应该根据类型正确统计权限数量")
    void shouldCountPermissionsByTypeCorrectly() {
      // Arrange
      String permissionType = "MENU";
      long expectedCount = 5L;

      when(permissionRepository.countByPermissionType(permissionType)).thenReturn(expectedCount);

      // Act
      long result = permissionService.countPermissionsByType(permissionType);

      // Assert
      assertThat(result).isEqualTo(expectedCount);
      verify(permissionRepository).countByPermissionType(permissionType);
    }

    @Test
    @DisplayName("应该正确检查权限代码是否存在")
    void shouldCheckPermissionCodeExistsCorrectly() {
      // Arrange
      String permissionCode = "user:read";

      when(permissionRepository.existsByPermissionCode(permissionCode)).thenReturn(true);

      // Act
      boolean exists = permissionService.existsByPermissionCode(permissionCode);

      // Assert
      assertThat(exists).isTrue();
      verify(permissionRepository).existsByPermissionCode(permissionCode);
    }

    @Test
    @DisplayName("应该正确检查权限是否被使用")
    void shouldCheckPermissionInUseCorrectly() {
      // Arrange
      Long permissionId = testPermission.getId();

      when(permissionRepository.countRolesByPermissionId(permissionId)).thenReturn(2L);

      // Act
      boolean inUse = permissionService.isPermissionInUse(permissionId);

      // Assert
      assertThat(inUse).isTrue();
      verify(permissionRepository).countRolesByPermissionId(permissionId);
    }
  }
}
