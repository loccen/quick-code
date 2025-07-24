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

        // 获取用户
        User user1 = userRepository.findByUsername("testuser1").orElse(null);
        User user2 = userRepository.findByUsername("testuser2").orElse(null);
        User admin = userRepository.findByUsername("admin").orElse(null);

        if (user1 == null || user2 == null || admin == null) {
            log.warn("用户数据不存在，跳过项目初始化");
            return;
        }

        // 获取分类
        Category vueCategory = categoryRepository.findByCode("VUE").orElse(null);
        Category reactCategory = categoryRepository.findByCode("REACT").orElse(null);
        Category angularCategory = categoryRepository.findByCode("ANGULAR").orElse(null);
        Category miniprogramCategory = categoryRepository.findByCode("MINIPROGRAM").orElse(null);
        Category springBootCategory = categoryRepository.findByCode("SPRINGBOOT").orElse(null);
        Category nodejsCategory = categoryRepository.findByCode("NODEJS").orElse(null);
        Category pythonCategory = categoryRepository.findByCode("PYTHON").orElse(null);
        Category golangCategory = categoryRepository.findByCode("GOLANG").orElse(null);
        Category androidCategory = categoryRepository.findByCode("ANDROID").orElse(null);
        Category iosCategory = categoryRepository.findByCode("IOS").orElse(null);
        Category flutterCategory = categoryRepository.findByCode("FLUTTER").orElse(null);
        Category reactNativeCategory = categoryRepository.findByCode("REACTNATIVE").orElse(null);

        // 使用主分类作为备选
        Category frontendCategory = categoryRepository.findByCode("FRONTEND").orElse(null);
        Category backendCategory = categoryRepository.findByCode("BACKEND").orElse(null);
        Category mobileCategory = categoryRepository.findByCode("MOBILE").orElse(null);

        if (frontendCategory == null || backendCategory == null) {
            log.warn("主分类数据不存在，跳过项目初始化");
            return;
        }

        // 创建20个丰富的项目数据
        List<Project> projects = createRichProjectData(
                Arrays.asList(user1, user2, admin),
                Arrays.asList(vueCategory, reactCategory, angularCategory, miniprogramCategory,
                        springBootCategory, nodejsCategory, pythonCategory, golangCategory,
                        androidCategory, iosCategory, flutterCategory, reactNativeCategory),
                Arrays.asList(frontendCategory, backendCategory, mobileCategory)
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

    /**
     * 创建丰富的项目测试数据
     */
    private List<Project> createRichProjectData(List<User> users, List<Category> categories, List<Category> fallbackCategories) {
        List<Project> projects = Arrays.asList(
                // Vue.js项目
                createProjectWithStats("Vue3企业级管理系统",
                        "基于Vue3 + TypeScript + Element Plus的现代化企业管理系统，包含完整的权限管理、数据统计、工作流等功能模块",
                        getValidCategoryId(categories.get(0), fallbackCategories.get(0)),
                        users.get(0).getId(), new BigDecimal("299.00"),
                        Arrays.asList("Vue 3", "TypeScript", "Element Plus", "Vite", "Pinia", "Vue Router"),
                        Arrays.asList("企业管理", "Vue3", "TypeScript", "权限系统"),
                        150, 1200, 89, new BigDecimal("4.8"), 45, 1),

                createProjectWithStats("Vue3电商前端模板",
                        "完整的电商前端解决方案，支持商品展示、购物车、订单管理、支付集成等功能",
                        getValidCategoryId(categories.get(0), fallbackCategories.get(0)),
                        users.get(1).getId(), new BigDecimal("199.00"),
                        Arrays.asList("Vue 3", "Vuex", "Vue Router", "Axios", "Element UI"),
                        Arrays.asList("电商", "前端", "Vue", "响应式"),
                        89, 856, 67, new BigDecimal("4.6"), 32, 1),

                // React项目
                createProjectWithStats("React企业级CRM系统",
                        "基于React + Ant Design的客户关系管理系统，包含客户管理、销售跟进、数据分析等功能",
                        getValidCategoryId(categories.get(1), fallbackCategories.get(0)),
                        users.get(0).getId(), new BigDecimal("399.00"),
                        Arrays.asList("React", "TypeScript", "Ant Design", "Redux Toolkit", "React Router"),
                        Arrays.asList("CRM", "React", "企业应用", "客户管理"),
                        234, 1567, 123, new BigDecimal("4.9"), 78, 1),

                createProjectWithStats("React Native跨平台App",
                        "完整的跨平台移动应用解决方案，支持iOS和Android，包含用户认证、数据同步等功能",
                        getValidCategoryId(categories.get(11), fallbackCategories.get(2)),
                        users.get(2).getId(), new BigDecimal("349.00"),
                        Arrays.asList("React Native", "Redux", "React Navigation", "AsyncStorage"),
                        Arrays.asList("移动应用", "跨平台", "React Native", "原生"),
                        178, 934, 78, new BigDecimal("4.7"), 56, 1),

                // Spring Boot项目
                createProjectWithStats("Spring Boot微服务架构",
                        "完整的微服务架构解决方案，包含用户服务、订单服务、支付服务等，支持分布式部署",
                        getValidCategoryId(categories.get(4), fallbackCategories.get(1)),
                        users.get(0).getId(), new BigDecimal("599.00"),
                        Arrays.asList("Spring Boot", "Spring Cloud", "MySQL", "Redis", "RabbitMQ", "Docker"),
                        Arrays.asList("微服务", "Spring Boot", "分布式", "云原生"),
                        312, 2134, 156, new BigDecimal("4.9"), 89, 1),

                createProjectWithStats("Spring Boot电商后端API",
                        "完整的电商后端API系统，包含商品管理、订单处理、支付集成、库存管理等功能",
                        getValidCategoryId(categories.get(4), fallbackCategories.get(1)),
                        users.get(1).getId(), new BigDecimal("459.00"),
                        Arrays.asList("Spring Boot", "MyBatis Plus", "MySQL", "Redis", "JWT"),
                        Arrays.asList("电商", "API", "后端", "Spring Boot"),
                        267, 1789, 134, new BigDecimal("4.8"), 67, 1),

                // Node.js项目
                createProjectWithStats("Node.js实时聊天系统",
                        "基于Socket.io的实时聊天系统，支持群聊、私聊、文件传输、消息推送等功能",
                        getValidCategoryId(categories.get(5), fallbackCategories.get(1)),
                        users.get(2).getId(), new BigDecimal("289.00"),
                        Arrays.asList("Node.js", "Express", "Socket.io", "MongoDB", "JWT"),
                        Arrays.asList("聊天系统", "实时通信", "Node.js", "WebSocket"),
                        145, 987, 89, new BigDecimal("4.5"), 43, 1),

                createProjectWithStats("Node.js博客管理系统",
                        "完整的博客管理系统，支持文章发布、评论管理、用户权限、SEO优化等功能",
                        getValidCategoryId(categories.get(5), fallbackCategories.get(1)),
                        users.get(0).getId(), new BigDecimal("199.00"),
                        Arrays.asList("Node.js", "Express", "MongoDB", "EJS", "Bootstrap"),
                        Arrays.asList("博客", "内容管理", "Node.js", "SEO"),
                        98, 654, 45, new BigDecimal("4.3"), 28, 1),

                // Python项目
                createProjectWithStats("Python数据分析平台",
                        "基于Django的数据分析平台，支持数据导入、清洗、分析、可视化等功能",
                        getValidCategoryId(categories.get(6), fallbackCategories.get(1)),
                        users.get(1).getId(), new BigDecimal("399.00"),
                        Arrays.asList("Python", "Django", "Pandas", "NumPy", "Matplotlib", "Plotly"),
                        Arrays.asList("数据分析", "Python", "可视化", "机器学习"),
                        189, 1234, 98, new BigDecimal("4.7"), 67, 1),

                createProjectWithStats("Python爬虫框架",
                        "高效的分布式爬虫框架，支持多线程、反反爬、数据存储、监控等功能",
                        getValidCategoryId(categories.get(6), fallbackCategories.get(1)),
                        users.get(2).getId(), new BigDecimal("259.00"),
                        Arrays.asList("Python", "Scrapy", "Redis", "MongoDB", "Celery"),
                        Arrays.asList("爬虫", "数据采集", "Python", "分布式"),
                        134, 876, 67, new BigDecimal("4.4"), 45, 1),

                // 移动应用项目
                createProjectWithStats("Flutter跨平台商城App",
                        "基于Flutter的跨平台电商应用，支持商品浏览、购物车、支付、物流跟踪等功能",
                        getValidCategoryId(categories.get(10), fallbackCategories.get(2)),
                        users.get(0).getId(), new BigDecimal("449.00"),
                        Arrays.asList("Flutter", "Dart", "Provider", "HTTP", "SQLite"),
                        Arrays.asList("Flutter", "电商", "跨平台", "移动应用"),
                        223, 1456, 112, new BigDecimal("4.8"), 78, 1),

                createProjectWithStats("Android原生社交App",
                        "完整的Android社交应用，包含动态发布、好友系统、即时通讯、位置服务等功能",
                        getValidCategoryId(categories.get(8), fallbackCategories.get(2)),
                        users.get(1).getId(), new BigDecimal("389.00"),
                        Arrays.asList("Android", "Java", "Retrofit", "Room", "Firebase"),
                        Arrays.asList("Android", "社交", "原生应用", "即时通讯"),
                        167, 1123, 89, new BigDecimal("4.6"), 56, 1),

                createProjectWithStats("iOS Swift健身App",
                        "基于Swift的健身应用，支持运动记录、健身计划、数据统计、社区分享等功能",
                        getValidCategoryId(categories.get(9), fallbackCategories.get(2)),
                        users.get(2).getId(), new BigDecimal("329.00"),
                        Arrays.asList("iOS", "Swift", "Core Data", "HealthKit", "MapKit"),
                        Arrays.asList("iOS", "健身", "Swift", "健康"),
                        145, 967, 78, new BigDecimal("4.7"), 45, 1),

                // 小程序项目
                createProjectWithStats("微信小程序商城",
                        "完整的微信小程序商城解决方案，支持商品展示、下单支付、会员系统等功能",
                        getValidCategoryId(categories.get(3), fallbackCategories.get(0)),
                        users.get(0).getId(), new BigDecimal("299.00"),
                        Arrays.asList("微信小程序", "云开发", "WeUI", "云函数", "云数据库"),
                        Arrays.asList("小程序", "商城", "微信", "云开发"),
                        178, 1234, 98, new BigDecimal("4.5"), 67, 1),

                createProjectWithStats("小程序点餐系统",
                        "餐厅点餐小程序，支持菜品展示、在线点餐、支付结算、订单管理等功能",
                        getValidCategoryId(categories.get(3), fallbackCategories.get(0)),
                        users.get(1).getId(), new BigDecimal("199.00"),
                        Arrays.asList("微信小程序", "云开发", "支付API", "地图API"),
                        Arrays.asList("小程序", "点餐", "餐饮", "O2O"),
                        89, 567, 45, new BigDecimal("4.2"), 34, 1),

                // Go语言项目
                createProjectWithStats("Go微服务网关",
                        "高性能的API网关服务，支持路由转发、负载均衡、限流熔断、监控等功能",
                        getValidCategoryId(categories.get(7), fallbackCategories.get(1)),
                        users.get(2).getId(), new BigDecimal("399.00"),
                        Arrays.asList("Go", "Gin", "Redis", "Consul", "Prometheus"),
                        Arrays.asList("Go", "微服务", "网关", "高性能"),
                        156, 1089, 78, new BigDecimal("4.6"), 56, 1),

                // Angular项目
                createProjectWithStats("Angular企业门户",
                        "基于Angular的企业门户网站，包含新闻发布、产品展示、在线客服等功能",
                        getValidCategoryId(categories.get(2), fallbackCategories.get(0)),
                        users.get(0).getId(), new BigDecimal("259.00"),
                        Arrays.asList("Angular", "TypeScript", "Angular Material", "RxJS"),
                        Arrays.asList("Angular", "企业门户", "TypeScript", "响应式"),
                        123, 789, 56, new BigDecimal("4.3"), 34, 1),

                // 全栈项目
                createProjectWithStats("全栈在线教育平台",
                        "完整的在线教育解决方案，包含课程管理、视频播放、在线考试、学习进度跟踪等功能",
                        getValidCategoryId(categories.get(0), fallbackCategories.get(0)),
                        users.get(1).getId(), new BigDecimal("799.00"),
                        Arrays.asList("Vue 3", "Spring Boot", "MySQL", "Redis", "FFmpeg", "WebRTC"),
                        Arrays.asList("在线教育", "全栈", "视频", "考试系统"),
                        345, 2567, 189, new BigDecimal("4.9"), 123, 1),

                createProjectWithStats("全栈项目管理系统",
                        "敏捷项目管理系统，支持任务管理、团队协作、进度跟踪、文档管理等功能",
                        getValidCategoryId(categories.get(4), fallbackCategories.get(1)),
                        users.get(2).getId(), new BigDecimal("459.00"),
                        Arrays.asList("React", "Node.js", "MongoDB", "Socket.io", "Docker"),
                        Arrays.asList("项目管理", "团队协作", "敏捷开发", "全栈"),
                        234, 1678, 134, new BigDecimal("4.7"), 89, 1),

                createProjectWithStats("智能客服系统",
                        "基于AI的智能客服系统，支持自动回复、人工客服、知识库管理、数据分析等功能",
                        getValidCategoryId(categories.get(6), fallbackCategories.get(1)),
                        users.get(0).getId(), new BigDecimal("699.00"),
                        Arrays.asList("Python", "TensorFlow", "Django", "Redis", "Elasticsearch"),
                        Arrays.asList("AI", "客服系统", "机器学习", "自然语言处理"),
                        289, 1934, 145, new BigDecimal("4.8"), 98, 1)
        );

        return projects;
    }

    /**
     * 获取有效的分类ID
     */
    private Long getValidCategoryId(Category category, Category fallback) {
        return category != null ? category.getId() : fallback.getId();
    }

    /**
     * 创建带统计数据的项目
     */
    private Project createProjectWithStats(String title, String description, Long categoryId, Long userId,
                                         BigDecimal price, List<String> techStack, List<String> tags,
                                         int downloadCount, int viewCount, int likeCount,
                                         BigDecimal rating, int ratingCount, int status) {
        return Project.builder()
                .title(title)
                .description(description)
                .categoryId(categoryId)
                .userId(userId)
                .price(price)
                .techStack(techStack)
                .tags(tags)
                .downloadCount(downloadCount)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .rating(rating)
                .ratingCount(ratingCount)
                .status(status)
                .build();
    }
}
