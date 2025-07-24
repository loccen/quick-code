-- 测试环境数据初始化脚本
-- 适用于H2内存数据库

-- 插入测试用户
INSERT INTO users (username, email, password, nickname, status, email_verified, is_admin) VALUES
('admin', 'admin@quickcode.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrvi.6eGIa', '系统管理员', 1, true, true),
('testuser1', 'user1@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrvi.6eGIa', '测试用户1', 1, true, false),
('testuser2', 'user2@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrvi.6eGIa', '测试用户2', 1, true, false),
('reviewer', 'reviewer@quickcode.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrvi.6eGIa', '审核员', 1, true, true);

-- 插入项目分类
INSERT INTO project_categories (name, code, description, parent_id, sort_order) VALUES
('前端项目', 'FRONTEND', '前端相关项目', NULL, 1),
('后端项目', 'BACKEND', '后端相关项目', NULL, 2),
('全栈项目', 'FULLSTACK', '全栈项目', NULL, 3),
('移动应用', 'MOBILE', '移动应用项目', NULL, 4),
('工具脚本', 'TOOLS', '工具和脚本', NULL, 5);

-- 插入前端子分类
INSERT INTO project_categories (name, code, description, parent_id, sort_order) VALUES
('Vue.js项目', 'VUE', 'Vue.js相关项目', 1, 1),
('React项目', 'REACT', 'React相关项目', 1, 2),
('Angular项目', 'ANGULAR', 'Angular相关项目', 1, 3),
('小程序', 'MINIPROGRAM', '微信小程序等', 1, 4);

-- 插入后端子分类
INSERT INTO project_categories (name, code, description, parent_id, sort_order) VALUES
('Spring Boot', 'SPRINGBOOT', 'Spring Boot项目', 2, 1),
('Node.js', 'NODEJS', 'Node.js项目', 2, 2),
('Python', 'PYTHON', 'Python项目', 2, 3),
('Go语言', 'GOLANG', 'Go语言项目', 2, 4);

-- 插入移动应用子分类
INSERT INTO project_categories (name, code, description, parent_id, sort_order) VALUES
('Android', 'ANDROID', 'Android应用', 4, 1),
('iOS', 'IOS', 'iOS应用', 4, 2),
('Flutter', 'FLUTTER', 'Flutter应用', 4, 3),
('React Native', 'REACTNATIVE', 'React Native应用', 4, 4);

-- 插入测试项目
INSERT INTO projects (title, description, category_id, user_id, price, tech_stack, tags, status) VALUES
('Vue3管理后台模板', '基于Vue3 + Element Plus的现代化管理后台模板，包含用户管理、权限控制、数据统计等功能', 6, 2, 100.00, 
 '["Vue 3", "TypeScript", "Element Plus", "Vite", "Pinia"]', 
 '["管理后台", "Vue3", "TypeScript", "响应式"]', 1),

('Spring Boot电商API', '完整的电商后端API，包含商品管理、订单处理、支付集成等功能', 10, 2, 200.00,
 '["Spring Boot", "MySQL", "Redis", "JWT", "Swagger"]',
 '["电商", "API", "Spring Boot", "微服务"]', 1),

('React Native购物App', '跨平台购物应用，支持商品浏览、购物车、订单管理等功能', 17, 3, 150.00,
 '["React Native", "Redux", "AsyncStorage", "React Navigation"]',
 '["移动应用", "购物", "跨平台", "React Native"]', 1);
