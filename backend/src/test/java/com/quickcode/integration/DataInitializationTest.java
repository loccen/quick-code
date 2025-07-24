package com.quickcode.integration;

import com.quickcode.entity.Category;
import com.quickcode.entity.Project;
import com.quickcode.entity.User;
import com.quickcode.repository.CategoryRepository;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 数据初始化集成测试
 * 验证测试数据是否正确加载
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataInitializationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldInitializeUsersCorrectly() {
        // 验证用户总数
        long userCount = userRepository.count();
        assertThat(userCount).isGreaterThan(0);

        // 验证管理员用户
        Optional<User> admin = userRepository.findByUsername("admin");
        assertThat(admin).isPresent();
        assertThat(admin.get().isAdmin()).isTrue();
        assertThat(admin.get().getEmailVerified()).isTrue();

        // 验证普通用户
        Optional<User> testUser = userRepository.findByUsername("testuser1");
        assertThat(testUser).isPresent();
        assertThat(testUser.get().isAdmin()).isFalse();
        assertThat(testUser.get().getEmailVerified()).isTrue();

        // 验证管理员和普通用户数量
        long adminCount = userRepository.countByIsAdmin(true);
        long regularUserCount = userRepository.countByIsAdmin(false);
        assertThat(adminCount).isGreaterThan(0);
        assertThat(regularUserCount).isGreaterThan(0);
    }

    @Test
    void shouldInitializeCategoriesCorrectly() {
        // 验证分类总数
        long categoryCount = categoryRepository.count();
        assertThat(categoryCount).isGreaterThan(0);

        // 验证主分类
        long mainCategoryCount = categoryRepository.countByParentIdIsNull();
        assertThat(mainCategoryCount).isGreaterThanOrEqualTo(5); // 至少5个主分类

        // 验证子分类
        long subCategoryCount = categoryRepository.countByParentIdIsNotNull();
        assertThat(subCategoryCount).isGreaterThan(0);

        // 验证特定分类存在
        assertThat(categoryRepository.existsByCode("FRONTEND")).isTrue();
        assertThat(categoryRepository.existsByCode("BACKEND")).isTrue();
        assertThat(categoryRepository.existsByCode("VUE")).isTrue();
        assertThat(categoryRepository.existsByCode("SPRINGBOOT")).isTrue();

        // 验证分类层级关系
        Optional<Category> frontend = categoryRepository.findByCode("FRONTEND");
        Optional<Category> vue = categoryRepository.findByCode("VUE");
        assertThat(frontend).isPresent();
        assertThat(vue).isPresent();
        assertThat(vue.get().getParentId()).isEqualTo(frontend.get().getId());
    }

    @Test
    void shouldInitializeProjectsCorrectly() {
        // 验证项目总数
        long projectCount = projectRepository.count();
        assertThat(projectCount).isGreaterThan(0);

        // 验证已发布项目数量
        long publishedProjectCount = projectRepository.countByStatus(1);
        assertThat(publishedProjectCount).isEqualTo(projectCount); // 所有测试项目都应该是已发布状态

        // 验证项目与分类的关联
        Optional<Category> vueCategory = categoryRepository.findByCode("VUE");
        if (vueCategory.isPresent()) {
            long vueProjectCount = projectRepository.countByCategoryId(vueCategory.get().getId());
            assertThat(vueProjectCount).isGreaterThanOrEqualTo(0);
        }

        // 验证项目与用户的关联
        Optional<User> testUser = userRepository.findByUsername("testuser1");
        if (testUser.isPresent()) {
            long userProjectCount = projectRepository.countByUserId(testUser.get().getId());
            assertThat(userProjectCount).isGreaterThanOrEqualTo(0);
        }

        // 验证项目数据完整性
        projectRepository.findAll().forEach(project -> {
            assertThat(project.getTitle()).isNotBlank();
            assertThat(project.getDescription()).isNotBlank();
            assertThat(project.getCategoryId()).isNotNull();
            assertThat(project.getUserId()).isNotNull();
            assertThat(project.getPrice()).isNotNull();
            assertThat(project.getStatus()).isEqualTo(1); // 已发布
        });
    }

    @Test
    void shouldHaveConsistentDataRelationships() {
        // 验证用户-项目关系一致性
        projectRepository.findAll().forEach(project -> {
            Optional<User> user = userRepository.findById(project.getUserId());
            assertThat(user).isPresent();
        });

        // 验证项目-分类关系一致性
        projectRepository.findAll().forEach(project -> {
            Optional<Category> category = categoryRepository.findById(project.getCategoryId());
            assertThat(category).isPresent();
        });

        // 验证分类层级关系一致性
        categoryRepository.findAll().forEach(category -> {
            if (category.getParentId() != null) {
                Optional<Category> parent = categoryRepository.findById(category.getParentId());
                assertThat(parent).isPresent();
            }
        });
    }

    @Test
    void shouldHaveUniqueConstraints() {
        // 验证用户名唯一性
        assertThat(userRepository.findByUsername("admin")).isPresent();
        assertThat(userRepository.findByEmail("admin@quickcode.com")).isPresent();

        // 验证分类代码唯一性
        assertThat(categoryRepository.findByCode("FRONTEND")).isPresent();
        assertThat(categoryRepository.findByCode("VUE")).isPresent();

        // 验证邮箱唯一性
        long uniqueEmailCount = userRepository.findAll().stream()
                .map(User::getEmail)
                .distinct()
                .count();
        long totalUserCount = userRepository.count();
        assertThat(uniqueEmailCount).isEqualTo(totalUserCount);
    }
}
