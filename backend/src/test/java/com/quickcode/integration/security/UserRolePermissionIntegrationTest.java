package com.quickcode.integration.security;

import com.quickcode.entity.Permission;
import com.quickcode.entity.Role;
import com.quickcode.entity.User;
import com.quickcode.repository.PermissionRepository;
import com.quickcode.repository.RoleRepository;
import com.quickcode.repository.UserRepository;
import com.quickcode.security.jwt.UserPrincipal;
import com.quickcode.security.service.CustomUserDetailsService;
import com.quickcode.service.UserService;
import com.quickcode.testutil.BaseIntegrationTest;
import com.quickcode.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户角色权限集成测试
 * 验证用户角色权限加载和验证功能
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@DisplayName("用户角色权限集成测试")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserRolePermissionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private Role userRole;
    private Permission userReadPermission;
    private Permission projectReadPermission;

    @BeforeEach
    void setUp() {
        // 创建权限
        userReadPermission = TestDataFactory.createUserReadPermission();
        projectReadPermission = TestDataFactory.createProjectReadPermission();
        userReadPermission = permissionRepository.save(userReadPermission);
        projectReadPermission = permissionRepository.save(projectReadPermission);

        // 创建角色并分配权限
        userRole = TestDataFactory.createTestRole();
        userRole.getPermissions().clear(); // 清除TestDataFactory中添加的权限
        userRole.addPermission(userReadPermission);
        userRole.addPermission(projectReadPermission);
        userRole = roleRepository.save(userRole);

        // 创建用户并分配角色
        testUser = TestDataFactory.createTestUser();
        testUser = userRepository.save(testUser);
        testUser.addRole(userRole);
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("应该能够通过用户名或邮箱加载用户及其角色权限信息")
    @Transactional
    void shouldLoadUserWithRolesAndPermissionsByUsernameOrEmail() {
        // Act - 通过用户名查找
        Optional<User> userByUsername = userService.findByUsernameOrEmailWithRoles(testUser.getUsername());
        
        // Assert
        assertThat(userByUsername).isPresent();
        User foundUser = userByUsername.get();
        
        assertThat(foundUser.getRoles()).isNotEmpty();
        assertThat(foundUser.getRoles()).hasSize(1);
        
        Role foundRole = foundUser.getRoles().iterator().next();
        assertThat(foundRole.getRoleCode()).isEqualTo("USER");
        assertThat(foundRole.getPermissions()).isNotEmpty();
        assertThat(foundRole.getPermissions()).hasSize(2);
        
        // 验证权限
        assertThat(foundRole.getPermissions())
                .extracting(Permission::getPermissionCode)
                .containsExactlyInAnyOrder("user:read", "project:read");

        // Act - 通过邮箱查找
        Optional<User> userByEmail = userService.findByUsernameOrEmailWithRoles(testUser.getEmail());
        
        // Assert
        assertThat(userByEmail).isPresent();
        assertThat(userByEmail.get().getRoles()).hasSize(1);
    }

    @Test
    @DisplayName("应该能够通过CustomUserDetailsService正确加载用户权限")
    void shouldLoadUserDetailsWithCorrectAuthorities() {
        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());
        
        // Assert
        assertThat(userDetails).isInstanceOf(UserPrincipal.class);
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        
        // 验证基本信息
        assertThat(userPrincipal.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(userPrincipal.getEmail()).isEqualTo(testUser.getEmail());
        
        // 验证权限
        assertThat(userPrincipal.getAuthorities()).isNotEmpty();
        assertThat(userPrincipal.getAuthorities()).hasSize(3); // 2个权限 + 1个角色
        
        // 验证角色权限
        assertThat(userPrincipal.hasRole("USER")).isTrue();
        assertThat(userPrincipal.hasRole("ADMIN")).isFalse();
        
        // 验证具体权限
        assertThat(userPrincipal.hasPermission("user:read")).isTrue();
        assertThat(userPrincipal.hasPermission("project:read")).isTrue();
        assertThat(userPrincipal.hasPermission("user:manage")).isFalse();
        
        // 验证权限列表
        assertThat(userPrincipal.getPermissions())
                .containsExactlyInAnyOrder("user:read", "project:read");
        
        // 验证角色列表
        assertThat(userPrincipal.getRoles())
                .containsExactly("USER");
    }

    @Test
    @DisplayName("应该能够通过ID加载用户及其角色权限信息")
    void shouldLoadUserWithRolesAndPermissionsById() {
        // Act
        UserDetails userDetails = userDetailsService.loadUserById(testUser.getId());
        
        // Assert
        assertThat(userDetails).isInstanceOf(UserPrincipal.class);
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        
        assertThat(userPrincipal.getId()).isEqualTo(testUser.getId());
        assertThat(userPrincipal.hasRole("USER")).isTrue();
        assertThat(userPrincipal.hasPermission("user:read")).isTrue();
        assertThat(userPrincipal.hasPermission("project:read")).isTrue();
    }

    @Test
    @DisplayName("没有角色的用户应该获得默认USER角色")
    void shouldGetDefaultUserRoleWhenNoRolesAssigned() {
        // Arrange - 创建没有角色的用户
        User userWithoutRoles = TestDataFactory.createTestUser("noroles");
        userWithoutRoles = userRepository.save(userWithoutRoles);
        
        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(userWithoutRoles.getUsername());
        
        // Assert
        assertThat(userDetails).isInstanceOf(UserPrincipal.class);
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        
        assertThat(userPrincipal.hasRole("USER")).isTrue();
        assertThat(userPrincipal.getAuthorities()).hasSize(1); // 只有默认的USER角色
    }

    @Test
    @DisplayName("应该正确处理非激活的角色和权限")
    @Transactional
    void shouldHandleInactiveRolesAndPermissions() {
        // Arrange - 创建非激活的权限和角色
        Permission inactivePermission = TestDataFactory.createTestPermission();
        inactivePermission.setStatus(0); // 设置为非激活
        inactivePermission = permissionRepository.save(inactivePermission);
        
        Role inactiveRole = TestDataFactory.createAdminRole();
        inactiveRole.setStatus(0); // 设置为非激活
        inactiveRole.addPermission(inactivePermission);
        inactiveRole = roleRepository.save(inactiveRole);
        
        testUser.addRole(inactiveRole);
        testUser = userRepository.save(testUser);
        
        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getUsername());
        
        // Assert
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        
        // 非激活的角色和权限不应该被包含
        assertThat(userPrincipal.hasRole("ADMIN")).isFalse();
        assertThat(userPrincipal.hasPermission("test:read")).isFalse();
        
        // 但激活的角色和权限应该存在
        assertThat(userPrincipal.hasRole("USER")).isTrue();
        assertThat(userPrincipal.hasPermission("user:read")).isTrue();
    }
}
