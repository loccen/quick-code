package com.quickcode.config;

import com.quickcode.entity.Category;
import com.quickcode.entity.Project;
import com.quickcode.entity.User;
import com.quickcode.repository.CategoryRepository;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 数据初始化配置
 * 在开发环境下自动初始化测试数据
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Component
@Profile({"dev", "test"}) // 仅在开发和测试环境下执行
@RequiredArgsConstructor
public class DataInitializationConfig implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("开始初始化测试数据...");
        
        try {
            initializeUsers();
            initializeCategories();
            initializeProjects();
            log.info("测试数据初始化完成");
        } catch (Exception e) {
            log.error("测试数据初始化失败", e);
        }
    }

    /**
     * 初始化用户数据
     */
    private void initializeUsers() {
        if (userRepository.count() > 0) {
            log.info("用户数据已存在，跳过初始化");
            return;
        }

        log.info("初始化用户数据...");
        
        // 创建管理员用户
        User admin = User.builder()
                .username("admin")
                .email("admin@quickcode.com")
                .password(passwordEncoder.encode("admin123"))
                .nickname("系统管理员")
                .status(1)
                .emailVerified(true)
                .isAdmin(true)
                .build();

        // 创建普通用户
        User user1 = User.builder()
                .username("testuser1")
                .email("user1@test.com")
                .password(passwordEncoder.encode("user123"))
                .nickname("测试用户1")
                .status(1)
                .emailVerified(true)
                .isAdmin(false)
                .build();

        User user2 = User.builder()
                .username("testuser2")
                .email("user2@test.com")
                .password(passwordEncoder.encode("user123"))
                .nickname("测试用户2")
                .status(1)
                .emailVerified(true)
                .isAdmin(false)
                .build();

        // 创建审核员（管理员权限）
        User reviewer = User.builder()
                .username("reviewer")
                .email("reviewer@quickcode.com")
                .password(passwordEncoder.encode("reviewer123"))
                .nickname("审核员")
                .status(1)
                .emailVerified(true)
                .isAdmin(true)
                .build();

        userRepository.saveAll(Arrays.asList(admin, user1, user2, reviewer));
        log.info("用户数据初始化完成，创建了 {} 个用户", 4);
    }

    /**
     * 初始化分类数据
     */
    private void initializeCategories() {
        if (categoryRepository.count() > 0) {
            log.info("分类数据已存在，跳过初始化");
            return;
        }

        log.info("初始化分类数据...");

        // 创建主分类
        Category frontend = createCategory("前端项目", "FRONTEND", "前端相关项目", null, 1);
        Category backend = createCategory("后端项目", "BACKEND", "后端相关项目", null, 2);
        Category fullstack = createCategory("全栈项目", "FULLSTACK", "全栈项目", null, 3);
        Category mobile = createCategory("移动应用", "MOBILE", "移动应用项目", null, 4);
        Category tools = createCategory("工具脚本", "TOOLS", "工具和脚本", null, 5);

        List<Category> mainCategories = categoryRepository.saveAll(
                Arrays.asList(frontend, backend, fullstack, mobile, tools));

        // 创建子分类
        List<Category> subCategories = Arrays.asList(
                // 前端子分类
                createCategory("Vue.js项目", "VUE", "Vue.js相关项目", mainCategories.get(0).getId(), 1),
                createCategory("React项目", "REACT", "React相关项目", mainCategories.get(0).getId(), 2),
                createCategory("Angular项目", "ANGULAR", "Angular相关项目", mainCategories.get(0).getId(), 3),
                createCategory("小程序", "MINIPROGRAM", "微信小程序等", mainCategories.get(0).getId(), 4),
                
                // 后端子分类
                createCategory("Spring Boot", "SPRINGBOOT", "Spring Boot项目", mainCategories.get(1).getId(), 1),
                createCategory("Node.js", "NODEJS", "Node.js项目", mainCategories.get(1).getId(), 2),
                createCategory("Python", "PYTHON", "Python项目", mainCategories.get(1).getId(), 3),
                createCategory("Go语言", "GOLANG", "Go语言项目", mainCategories.get(1).getId(), 4),
                
                // 移动应用子分类
                createCategory("Android", "ANDROID", "Android应用", mainCategories.get(3).getId(), 1),
                createCategory("iOS", "IOS", "iOS应用", mainCategories.get(3).getId(), 2),
                createCategory("Flutter", "FLUTTER", "Flutter应用", mainCategories.get(3).getId(), 3),
                createCategory("React Native", "REACTNATIVE", "React Native应用", mainCategories.get(3).getId(), 4)
        );

        categoryRepository.saveAll(subCategories);
        log.info("分类数据初始化完成，创建了 {} 个主分类和 {} 个子分类", 
                mainCategories.size(), subCategories.size());
    }

    /**
     * 初始化项目数据
     */
    private void initializeProjects() {
        if (projectRepository.count() > 0) {
            log.info("项目数据已存在，跳过初始化");
            return;
        }

        log.info("初始化项目数据...");

        // 获取用户ID（假设用户已经创建）
        User user1 = userRepository.findByUsername("testuser1").orElse(null);
        User user2 = userRepository.findByUsername("testuser2").orElse(null);

        if (user1 == null || user2 == null) {
            log.warn("用户数据不存在，跳过项目初始化");
            return;
        }

        // 获取分类ID，使用默认分类如果特定分类不存在
        Category vueCategory = categoryRepository.findByCode("VUE").orElse(null);
        Category springBootCategory = categoryRepository.findByCode("SPRINGBOOT").orElse(null);
        Category reactNativeCategory = categoryRepository.findByCode("REACTNATIVE").orElse(null);

        // 如果子分类不存在，使用主分类
        Category frontendCategory = categoryRepository.findByCode("FRONTEND").orElse(null);
        Category backendCategory = categoryRepository.findByCode("BACKEND").orElse(null);

        if (frontendCategory == null || backendCategory == null) {
            log.warn("主分类数据不存在，跳过项目初始化");
            return;
        }

        List<Project> projects = Arrays.asList(
                createProject("Vue3管理后台模板",
                        "基于Vue3 + Element Plus的现代化管理后台模板，包含用户管理、权限控制、数据统计等功能",
                        vueCategory != null ? vueCategory.getId() : frontendCategory.getId(),
                        user1.getId(), new BigDecimal("100.00"),
                        Arrays.asList("Vue 3", "TypeScript", "Element Plus", "Vite", "Pinia"),
                        Arrays.asList("管理后台", "Vue3", "TypeScript", "响应式")),

                createProject("Spring Boot电商API",
                        "完整的电商后端API，包含商品管理、订单处理、支付集成等功能",
                        springBootCategory != null ? springBootCategory.getId() : backendCategory.getId(),
                        user1.getId(), new BigDecimal("200.00"),
                        Arrays.asList("Spring Boot", "MySQL", "Redis", "JWT", "Swagger"),
                        Arrays.asList("电商", "API", "Spring Boot", "微服务")),

                createProject("React Native购物App",
                        "跨平台购物应用，支持商品浏览、购物车、订单管理等功能",
                        reactNativeCategory != null ? reactNativeCategory.getId() : frontendCategory.getId(),
                        user2.getId(), new BigDecimal("150.00"),
                        Arrays.asList("React Native", "Redux", "AsyncStorage", "React Navigation"),
                        Arrays.asList("移动应用", "购物", "跨平台", "React Native"))
        );

        projectRepository.saveAll(projects);
        log.info("项目数据初始化完成，创建了 {} 个项目", projects.size());
    }

    private Category createCategory(String name, String code, String description, Long parentId, Integer sortOrder) {
        return Category.builder()
                .name(name)
                .code(code)
                .description(description)
                .parentId(parentId)
                .sortOrder(sortOrder)
                .status(1)
                .build();
    }

    private Project createProject(String title, String description, Long categoryId, Long userId, 
                                BigDecimal price, List<String> techStack, List<String> tags) {
        return Project.builder()
                .title(title)
                .description(description)
                .categoryId(categoryId)
                .userId(userId)
                .price(price)
                .techStack(techStack)
                .tags(tags)
                .status(1) // 已发布
                .build();
    }
}
