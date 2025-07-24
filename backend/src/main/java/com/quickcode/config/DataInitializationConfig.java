package com.quickcode.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickcode.dto.CategoryTestData;
import com.quickcode.dto.ProjectTestData;
import com.quickcode.dto.UserTestData;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
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
    private final ObjectMapper objectMapper;

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

        try {
            // 从JSON文件加载用户数据
            List<UserTestData> userDataList = loadUserDataFromJson();

            // 转换为User实体并保存
            List<User> users = userDataList.stream()
                    .map(this::convertToUser)
                    .toList();

            if (!users.isEmpty()) {
                userRepository.saveAll(users);
                log.info("用户数据初始化完成，创建了 {} 个用户", users.size());
            } else {
                log.warn("没有有效的用户数据可以初始化");
            }
        } catch (Exception e) {
            log.error("用户数据初始化失败", e);
        }
    }

    /**
     * 从JSON文件加载用户测试数据
     */
    private List<UserTestData> loadUserDataFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/test-users.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<UserTestData>>() {});
        }
    }

    /**
     * 将UserTestData转换为User实体
     */
    private User convertToUser(UserTestData data) {
        return User.builder()
                .username(data.getUsername())
                .email(data.getEmail())
                .password(passwordEncoder.encode(data.getPassword()))
                .nickname(data.getNickname())
                .status(data.getStatus())
                .emailVerified(data.getEmailVerified())
                .isAdmin(data.getIsAdmin())
                .build();
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

        try {
            // 从JSON文件加载分类数据
            List<CategoryTestData> categoryDataList = loadCategoryDataFromJson();

            // 先创建主分类（没有父分类的）
            List<Category> mainCategories = categoryDataList.stream()
                    .filter(data -> data.getParentCode() == null)
                    .map(this::convertToCategory)
                    .toList();

            if (!mainCategories.isEmpty()) {
                categoryRepository.saveAll(mainCategories);
                log.info("主分类初始化完成，创建了 {} 个主分类", mainCategories.size());
            }

            // 再创建子分类（有父分类的）
            List<Category> subCategories = categoryDataList.stream()
                    .filter(data -> data.getParentCode() != null)
                    .map(this::convertToCategory)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            if (!subCategories.isEmpty()) {
                categoryRepository.saveAll(subCategories);
                log.info("子分类初始化完成，创建了 {} 个子分类", subCategories.size());
            }

            log.info("分类数据初始化完成，总共创建了 {} 个分类",
                    mainCategories.size() + subCategories.size());
        } catch (Exception e) {
            log.error("分类数据初始化失败", e);
        }
    }

    /**
     * 从JSON文件加载分类测试数据
     */
    private List<CategoryTestData> loadCategoryDataFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/test-categories.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<CategoryTestData>>() {});
        }
    }

    /**
     * 将CategoryTestData转换为Category实体
     */
    private Category convertToCategory(CategoryTestData data) {
        try {
            Long parentId = null;
            if (data.getParentCode() != null) {
                Category parentCategory = categoryRepository.findByCode(data.getParentCode()).orElse(null);
                if (parentCategory == null) {
                    log.warn("父分类 {} 不存在，跳过子分类: {}", data.getParentCode(), data.getName());
                    return null;
                }
                parentId = parentCategory.getId();
            }

            return Category.builder()
                    .name(data.getName())
                    .code(data.getCode())
                    .description(data.getDescription())
                    .parentId(parentId)
                    .sortOrder(data.getSortOrder())
                    .status(data.getStatus())
                    .build();
        } catch (Exception e) {
            log.error("转换分类数据失败: {}", data.getName(), e);
            return null;
        }
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

        try {
            // 从JSON文件加载项目数据
            List<ProjectTestData> projectDataList = loadProjectDataFromJson();

            // 转换为Project实体并保存
            List<Project> projects = projectDataList.stream()
                    .map(this::convertToProject)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            if (!projects.isEmpty()) {
                projectRepository.saveAll(projects);
                log.info("项目数据初始化完成，创建了 {} 个项目", projects.size());
            } else {
                log.warn("没有有效的项目数据可以初始化");
            }
        } catch (Exception e) {
            log.error("项目数据初始化失败", e);
        }
    }

    /**
     * 从JSON文件加载项目测试数据
     */
    private List<ProjectTestData> loadProjectDataFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/test-projects.json");
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<ProjectTestData>>() {});
        }
    }

    /**
     * 将ProjectTestData转换为Project实体
     */
    private Project convertToProject(ProjectTestData data) {
        try {
            // 获取用户
            User user = userRepository.findByUsername(data.getUsername()).orElse(null);
            if (user == null) {
                log.warn("用户 {} 不存在，跳过项目: {}", data.getUsername(), data.getTitle());
                return null;
            }

            // 获取分类
            Category category = categoryRepository.findByCode(data.getCategoryCode()).orElse(null);
            if (category == null) {
                // 尝试使用备用分类
                category = categoryRepository.findByCode(data.getFallbackCategoryCode()).orElse(null);
                if (category == null) {
                    log.warn("分类 {} 和备用分类 {} 都不存在，跳过项目: {}",
                            data.getCategoryCode(), data.getFallbackCategoryCode(), data.getTitle());
                    return null;
                }
            }

            return Project.builder()
                    .title(data.getTitle())
                    .description(data.getDescription())
                    .categoryId(category.getId())
                    .userId(user.getId())
                    .price(data.getPrice())
                    .techStack(data.getTechStack())
                    .tags(data.getTags())
                    .downloadCount(data.getDownloadCount())
                    .viewCount(data.getViewCount())
                    .likeCount(data.getLikeCount())
                    .rating(data.getRating())
                    .ratingCount(data.getRatingCount())
                    .status(data.getStatus())
                    .build();
        } catch (Exception e) {
            log.error("转换项目数据失败: {}", data.getTitle(), e);
            return null;
        }
    }
}
