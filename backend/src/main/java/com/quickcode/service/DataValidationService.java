package com.quickcode.service;

import com.quickcode.repository.CategoryRepository;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 数据验证服务
 * 在应用启动完成后验证测试数据是否正确加载
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@Profile({"dev", "test"}) // 仅在开发和测试环境下执行
@RequiredArgsConstructor
public class DataValidationService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;

    /**
     * 应用启动完成后验证数据
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateDataAfterStartup() {
        log.info("开始验证测试数据...");
        
        try {
            validateUsers();
            validateCategories();
            validateProjects();
            log.info("测试数据验证完成，所有数据正常");
        } catch (Exception e) {
            log.error("测试数据验证失败", e);
        }
    }

    /**
     * 验证用户数据
     */
    private void validateUsers() {
        long userCount = userRepository.count();
        long adminCount = userRepository.countByIsAdmin(true);
        long regularUserCount = userRepository.countByIsAdmin(false);
        
        log.info("用户数据验证 - 总用户数: {}, 管理员数: {}, 普通用户数: {}", 
                userCount, adminCount, regularUserCount);
        
        if (userCount == 0) {
            log.warn("警告：没有找到任何用户数据");
            return;
        }
        
        // 验证管理员用户
        boolean hasAdmin = userRepository.findByUsername("admin").isPresent();
        if (!hasAdmin) {
            log.warn("警告：没有找到管理员用户");
        } else {
            log.info("✓ 管理员用户存在");
        }
        
        // 验证测试用户
        boolean hasTestUser = userRepository.findByUsername("testuser1").isPresent();
        if (!hasTestUser) {
            log.warn("警告：没有找到测试用户");
        } else {
            log.info("✓ 测试用户存在");
        }
    }

    /**
     * 验证分类数据
     */
    private void validateCategories() {
        long categoryCount = categoryRepository.count();
        long mainCategoryCount = categoryRepository.countByParentIdIsNull();
        long subCategoryCount = categoryRepository.countByParentIdIsNotNull();
        
        log.info("分类数据验证 - 总分类数: {}, 主分类数: {}, 子分类数: {}", 
                categoryCount, mainCategoryCount, subCategoryCount);
        
        if (categoryCount == 0) {
            log.warn("警告：没有找到任何分类数据");
            return;
        }
        
        // 验证主要分类
        String[] mainCategories = {"FRONTEND", "BACKEND", "FULLSTACK", "MOBILE", "TOOLS"};
        for (String code : mainCategories) {
            boolean exists = categoryRepository.existsByCode(code);
            if (exists) {
                log.info("✓ 主分类 {} 存在", code);
            } else {
                log.warn("警告：主分类 {} 不存在", code);
            }
        }
        
        // 验证子分类
        String[] subCategories = {"VUE", "REACT", "SPRINGBOOT", "NODEJS", "ANDROID", "IOS"};
        for (String code : subCategories) {
            boolean exists = categoryRepository.existsByCode(code);
            if (exists) {
                log.info("✓ 子分类 {} 存在", code);
            } else {
                log.warn("警告：子分类 {} 不存在", code);
            }
        }
    }

    /**
     * 验证项目数据
     */
    private void validateProjects() {
        long projectCount = projectRepository.count();
        long publishedProjectCount = projectRepository.countByStatus(1);
        
        log.info("项目数据验证 - 总项目数: {}, 已发布项目数: {}", 
                projectCount, publishedProjectCount);
        
        if (projectCount == 0) {
            log.warn("警告：没有找到任何项目数据");
            return;
        }
        
        // 验证项目分布
        long frontendProjects = projectRepository.countByCategoryId(
                categoryRepository.findByCode("VUE").map(c -> c.getId()).orElse(-1L));
        long backendProjects = projectRepository.countByCategoryId(
                categoryRepository.findByCode("SPRINGBOOT").map(c -> c.getId()).orElse(-1L));
        
        log.info("项目分布 - Vue项目: {}, Spring Boot项目: {}", 
                frontendProjects, backendProjects);
        
        if (frontendProjects > 0) {
            log.info("✓ 前端项目数据存在");
        }
        
        if (backendProjects > 0) {
            log.info("✓ 后端项目数据存在");
        }
    }
}
