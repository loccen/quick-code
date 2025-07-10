package com.quickcode.integration.repository;

import com.quickcode.entity.User;
import com.quickcode.entity.Role;
import com.quickcode.repository.UserRepository;
import com.quickcode.repository.RoleRepository;
import com.quickcode.testutil.BaseIntegrationTest;
import com.quickcode.testutil.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * UserRepository集成测试
 * 验证数据访问层的查询和持久化功能
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@DisplayName("UserRepository集成测试")
class UserRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // 清理数据
        userRepository.deleteAll();
        roleRepository.deleteAll();
        
        // 创建测试数据
        testUser = TestDataFactory.createTestUser();
        testRole = TestDataFactory.createTestRole();
    }

    @Nested
    @DisplayName("基础CRUD操作测试")
    class BasicCrudTests {

        @Test
        @DisplayName("应该能够保存和查找用户")
        @Rollback
        void shouldSaveAndFindUser() {
            // Act
            User savedUser = userRepository.save(testUser);
            Optional<User> foundUser = userRepository.findById(savedUser.getId());

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo(testUser.getUsername());
            assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("应该能够更新用户信息")
        @Rollback
        void shouldUpdateUser() {
            // Arrange
            User savedUser = userRepository.save(testUser);
            String newNickname = "新昵称";

            // Act
            savedUser.setNickname(newNickname);
            User updatedUser = userRepository.save(savedUser);

            // Assert
            assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
        }

        @Test
        @DisplayName("应该能够删除用户")
        @Rollback
        void shouldDeleteUser() {
            // Arrange
            User savedUser = userRepository.save(testUser);

            // Act
            userRepository.delete(savedUser);

            // Assert
            Optional<User> foundUser = userRepository.findById(savedUser.getId());
            assertThat(foundUser).isEmpty();
        }
    }

    @Nested
    @DisplayName("自定义查询方法测试")
    class CustomQueryTests {

        @Test
        @DisplayName("应该能够根据用户名查找用户")
        @Rollback
        void shouldFindByUsername() {
            // Arrange
            userRepository.save(testUser);

            // Act
            Optional<User> foundUser = userRepository.findByUsername(testUser.getUsername());

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo(testUser.getUsername());
        }

        @Test
        @DisplayName("应该能够根据邮箱查找用户")
        @Rollback
        void shouldFindByEmail() {
            // Arrange
            userRepository.save(testUser);

            // Act
            Optional<User> foundUser = userRepository.findByEmail(testUser.getEmail());

            // Assert
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("应该能够根据用户名或邮箱查找用户")
        @Rollback
        void shouldFindByUsernameOrEmail() {
            // Arrange
            userRepository.save(testUser);

            // Act - 使用用户名查找
            Optional<User> foundByUsername = userRepository.findByUsernameOrEmail(testUser.getUsername());
            // Act - 使用邮箱查找
            Optional<User> foundByEmail = userRepository.findByUsernameOrEmail(testUser.getEmail());

            // Assert
            assertThat(foundByUsername).isPresent();
            assertThat(foundByEmail).isPresent();
            assertThat(foundByUsername.get().getId()).isEqualTo(foundByEmail.get().getId());
        }

        @Test
        @DisplayName("应该能够检查用户名是否存在")
        @Rollback
        void shouldCheckUsernameExists() {
            // Arrange
            userRepository.save(testUser);

            // Act & Assert
            assertThat(userRepository.existsByUsername(testUser.getUsername())).isTrue();
            assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("应该能够检查邮箱是否存在")
        @Rollback
        void shouldCheckEmailExists() {
            // Arrange
            userRepository.save(testUser);

            // Act & Assert
            assertThat(userRepository.existsByEmail(testUser.getEmail())).isTrue();
            assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("状态相关查询测试")
    class StatusQueryTests {

        @Test
        @DisplayName("应该能够根据状态查找用户")
        @Rollback
        void shouldFindByStatus() {
            // Arrange
            User activeUser = TestDataFactory.createTestUser();
            activeUser.setStatus(1); // 激活状态
            User inactiveUser = TestDataFactory.createInactiveUser();
            inactiveUser.setStatus(0); // 禁用状态

            userRepository.save(activeUser);
            userRepository.save(inactiveUser);

            // Act
            List<User> activeUsers = userRepository.findByStatus(1);
            List<User> inactiveUsers = userRepository.findByStatus(0);

            // Assert
            assertThat(activeUsers).hasSize(1);
            assertThat(inactiveUsers).hasSize(1);
            assertThat(activeUsers.get(0).getStatus()).isEqualTo(1);
            assertThat(inactiveUsers.get(0).getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("应该能够查找已验证邮箱的用户")
        @Rollback
        void shouldFindByEmailVerified() {
            // Arrange
            User verifiedUser = TestDataFactory.createTestUser();
            verifiedUser.setEmailVerified(true);
            User unverifiedUser = TestDataFactory.createTestUser("unverified");
            unverifiedUser.setEmailVerified(false);

            userRepository.save(verifiedUser);
            userRepository.save(unverifiedUser);

            // Act
            List<User> verifiedUsers = userRepository.findByEmailVerified(true);
            List<User> unverifiedUsers = userRepository.findByEmailVerified(false);

            // Assert
            assertThat(verifiedUsers).hasSize(1);
            assertThat(unverifiedUsers).hasSize(1);
            assertThat(verifiedUsers.get(0).getEmailVerified()).isTrue();
            assertThat(unverifiedUsers.get(0).getEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("应该能够查找被锁定的用户")
        @Rollback
        void shouldFindLockedUsers() {
            // Arrange
            User lockedUser = TestDataFactory.createTestUser();
            lockedUser.setLockedUntil(LocalDateTime.now().plusHours(1)); // 锁定1小时
            User normalUser = TestDataFactory.createTestUser("normal");
            normalUser.setLockedUntil(null);

            userRepository.save(lockedUser);
            userRepository.save(normalUser);

            // Act
            List<User> lockedUsers = userRepository.findLockedUsers(LocalDateTime.now());

            // Assert
            assertThat(lockedUsers).hasSize(1);
            assertThat(lockedUsers.get(0).getLockedUntil()).isAfter(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("角色相关查询测试")
    class RoleQueryTests {

        @Test
        @DisplayName("应该能够根据角色代码查找用户")
        @Rollback
        void shouldFindByRoleCode() {
            // Arrange
            Role savedRole = roleRepository.save(testRole);
            testUser.addRole(savedRole);
            userRepository.save(testUser);

            // Act
            List<User> usersWithRole = userRepository.findByRoleCode(testRole.getRoleCode());

            // Assert
            assertThat(usersWithRole).hasSize(1);
            assertThat(usersWithRole.get(0).getId()).isEqualTo(testUser.getId());
        }
    }

    @Nested
    @DisplayName("统计查询测试")
    class CountQueryTests {

        @Test
        @DisplayName("应该能够统计用户总数")
        @Rollback
        void shouldCountAllUsers() {
            // Arrange
            userRepository.save(testUser);
            userRepository.save(TestDataFactory.createTestUser("user2"));

            // Act
            Long count = userRepository.countAllUsers();

            // Assert
            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("应该能够根据状态统计用户数量")
        @Rollback
        void shouldCountByStatus() {
            // Arrange
            User activeUser = TestDataFactory.createTestUser();
            activeUser.setStatus(1);
            User inactiveUser = TestDataFactory.createInactiveUser();
            inactiveUser.setStatus(0);

            userRepository.save(activeUser);
            userRepository.save(inactiveUser);

            // Act
            Long activeCount = userRepository.countByStatus(1);
            Long inactiveCount = userRepository.countByStatus(0);

            // Assert
            assertThat(activeCount).isEqualTo(1L);
            assertThat(inactiveCount).isEqualTo(1L);
        }

        @Test
        @DisplayName("应该能够统计已验证邮箱的用户数量")
        @Rollback
        void shouldCountVerifiedUsers() {
            // Arrange
            User verifiedUser = TestDataFactory.createTestUser();
            verifiedUser.setEmailVerified(true);
            User unverifiedUser = TestDataFactory.createTestUser("unverified");
            unverifiedUser.setEmailVerified(false);

            userRepository.save(verifiedUser);
            userRepository.save(unverifiedUser);

            // Act
            Long verifiedCount = userRepository.countVerifiedUsers();

            // Assert
            assertThat(verifiedCount).isEqualTo(1L);
        }
    }
}
