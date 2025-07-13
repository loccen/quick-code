package com.quickcode.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.quickcode.common.response.ApiResponse;
import com.quickcode.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 公开项目控制器
 * 提供项目市场的公开访问接口，无需认证
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/public/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PublicProjectController extends BaseController {

    /**
     * 获取项目列表（公开接口）
     */
    @GetMapping
    public ApiResponse<PageResponse<Map<String, Object>>> getPublicProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("获取公开项目列表: page={}, size={}, category={}, keyword={}",
                page, size, category, keyword);

        // 创建分页参数
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 模拟项目数据（实际应该从数据库查询）
        List<Map<String, Object>> projects = createMockProjects();

        // 根据条件过滤
        if (category != null && !category.isEmpty()) {
            projects = projects.stream()
                    .filter(p -> category.equals(p.get("category")))
                    .toList();
        }

        if (keyword != null && !keyword.isEmpty()) {
            projects = projects.stream()
                    .filter(p -> {
                        String title = (String) p.get("title");
                        String description = (String) p.get("description");
                        return title.toLowerCase().contains(keyword.toLowerCase()) ||
                               description.toLowerCase().contains(keyword.toLowerCase());
                    })
                    .toList();
        }

        // 创建分页响应
        int start = page * size;
        int end = Math.min(start + size, projects.size());
        List<Map<String, Object>> pageContent = projects.subList(start, end);

        PageResponse<Map<String, Object>> pageResponse = PageResponse.<Map<String, Object>>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .total((long) projects.size())
                .totalPages((int) Math.ceil((double) projects.size() / size))
                .first(page == 0)
                .last(end >= projects.size())
                .build();

        return success(pageResponse, "获取项目列表成功");
    }

    /**
     * 获取项目详情（公开接口）
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getPublicProjectDetail(@PathVariable Long id) {
        log.info("获取公开项目详情: id={}", id);

        // 模拟项目详情数据（实际应该从数据库查询）
        Map<String, Object> project = createMockProjectDetail(id);

        if (project == null) {
            return error("项目不存在");
        }

        return success(project, "获取项目详情成功");
    }

    /**
     * 获取项目分类（公开接口）
     */
    @GetMapping("/categories")
    public ApiResponse<List<Map<String, Object>>> getProjectCategories() {
        log.info("获取项目分类列表");

        List<Map<String, Object>> categories = List.of(
                Map.of("id", 1L, "name", "Web应用", "code", "web", "count", 25),
                Map.of("id", 2L, "name", "移动应用", "code", "mobile", "count", 18),
                Map.of("id", 3L, "name", "桌面应用", "code", "desktop", "count", 12),
                Map.of("id", 4L, "name", "小程序", "code", "miniapp", "count", 15),
                Map.of("id", 5L, "name", "游戏", "code", "game", "count", 8),
                Map.of("id", 6L, "name", "工具类", "code", "tool", "count", 22)
        );

        return success(categories, "获取分类列表成功");
    }

    /**
     * 搜索项目（公开接口）
     */
    @GetMapping("/search")
    public ApiResponse<List<Map<String, Object>>> searchProjects(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("搜索项目: keyword={}, limit={}", keyword, limit);

        List<Map<String, Object>> projects = createMockProjects();

        // 搜索过滤
        List<Map<String, Object>> results = projects.stream()
                .filter(p -> {
                    String title = (String) p.get("title");
                    String description = (String) p.get("description");
                    return title.toLowerCase().contains(keyword.toLowerCase()) ||
                           description.toLowerCase().contains(keyword.toLowerCase());
                })
                .limit(limit)
                .toList();

        return success(results, "搜索完成");
    }

    /**
     * 创建模拟项目数据
     */
    private List<Map<String, Object>> createMockProjects() {
        List<Map<String, Object>> projects = new ArrayList<>();

        // 项目1: Vue3 + TypeScript 管理后台
        Map<String, Object> project1 = new HashMap<>();
        project1.put("id", 1L);
        project1.put("title", "Vue3 + TypeScript 管理后台");
        project1.put("description", "基于Vue3、TypeScript、Element Plus的现代化管理后台模板");
        project1.put("category", "web");
        project1.put("price", 299);
        project1.put("thumbnail", "/images/projects/vue-admin.jpg");
        project1.put("author", "开发者A");
        project1.put("rating", 4.8);
        project1.put("downloads", 1250);
        project1.put("tags", List.of("Vue3", "TypeScript", "Element Plus"));
        project1.put("createdAt", "2024-01-15");
        projects.add(project1);

        // 项目2: React Native 电商App
        Map<String, Object> project2 = new HashMap<>();
        project2.put("id", 2L);
        project2.put("title", "React Native 电商App");
        project2.put("description", "功能完整的电商移动应用，支持iOS和Android");
        project2.put("category", "mobile");
        project2.put("price", 599);
        project2.put("thumbnail", "/images/projects/rn-ecommerce.jpg");
        project2.put("author", "开发者B");
        project2.put("rating", 4.6);
        project2.put("downloads", 890);
        project2.put("tags", List.of("React Native", "电商", "移动端"));
        project2.put("createdAt", "2024-01-10");
        projects.add(project2);

        // 项目3: Spring Boot 微服务架构
        Map<String, Object> project3 = new HashMap<>();
        project3.put("id", 3L);
        project3.put("title", "Spring Boot 微服务架构");
        project3.put("description", "企业级微服务架构解决方案，包含网关、配置中心等");
        project3.put("category", "web");
        project3.put("price", 899);
        project3.put("thumbnail", "/images/projects/springboot-microservice.jpg");
        project3.put("author", "开发者C");
        project3.put("rating", 4.9);
        project3.put("downloads", 2100);
        project3.put("tags", List.of("Spring Boot", "微服务", "Docker"));
        project3.put("createdAt", "2024-01-05");
        projects.add(project3);

        // 添加更多模拟数据...
        for (int i = 4; i <= 20; i++) {
            Map<String, Object> project = new HashMap<>();
            project.put("id", (long) i);
            project.put("title", "项目模板 " + i);
            project.put("description", "这是第 " + i + " 个项目模板的描述");
            project.put("category", i % 2 == 0 ? "web" : "mobile");
            project.put("price", 100 + i * 50);
            project.put("thumbnail", "/images/projects/default-" + i + ".jpg");
            project.put("author", "开发者" + (char)('A' + i % 5));
            project.put("rating", 4.0 + (i % 10) * 0.1);
            project.put("downloads", 100 + i * 50);
            project.put("tags", List.of("标签1", "标签2"));
            project.put("createdAt", "2024-01-" + String.format("%02d", i));
            projects.add(project);
        }

        return projects;
    }

    /**
     * 创建模拟项目详情数据
     */
    private Map<String, Object> createMockProjectDetail(Long id) {
        if (id == null || id <= 0 || id > 20) {
            return null;
        }

        Map<String, Object> project = new HashMap<>();
        project.put("id", id);
        project.put("title", "项目模板 " + id);
        project.put("description", "这是第 " + id + " 个项目模板的详细描述，包含了完整的功能介绍和使用说明。");
        project.put("category", id % 2 == 0 ? "web" : "mobile");
        project.put("price", 100 + id * 50);
        project.put("thumbnail", "/images/projects/default-" + id + ".jpg");
        project.put("author", "开发者" + (char)('A' + id % 5));
        project.put("rating", 4.0 + (id % 10) * 0.1);
        project.put("downloads", 100 + id * 50);
        project.put("tags", List.of("标签1", "标签2", "标签3"));
        project.put("createdAt", "2024-01-" + String.format("%02d", id.intValue()));
        project.put("updatedAt", "2024-01-" + String.format("%02d", id.intValue() + 5));

        // 添加详细信息
        project.put("features", List.of(
                "功能特性1：完整的用户管理系统",
                "功能特性2：响应式设计支持",
                "功能特性3：多语言国际化",
                "功能特性4：完善的权限控制"
        ));

        project.put("techStack", List.of("Vue3", "TypeScript", "Element Plus", "Vite"));
        project.put("demoUrl", "https://demo.example.com/project-" + id);
        project.put("sourceSize", "15.6 MB");
        project.put("license", "MIT");

        return project;
    }
}
