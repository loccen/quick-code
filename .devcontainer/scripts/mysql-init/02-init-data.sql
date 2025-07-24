-- 速码网测试数据初始化脚本
-- 插入基础数据和测试数据

USE quick_code;

-- 插入测试用户
INSERT INTO `users` (`username`, `email`, `password`, `nickname`, `status`, `email_verified`, `is_admin`) VALUES
('admin', 'admin@quickcode.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrvi.6eGIa', '系统管理员', 1, 1, 1),
('testuser1', 'user1@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrvi.6eGIa', '测试用户1', 1, 1, 0),
('testuser2', 'user2@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrvi.6eGIa', '测试用户2', 1, 1, 0),
('reviewer', 'reviewer@quickcode.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrvi.6eGIa', '审核员', 1, 1, 1);



-- 插入项目分类
INSERT INTO `project_categories` (`name`, `code`, `description`, `parent_id`, `sort_order`) VALUES
('前端项目', 'FRONTEND', '前端相关项目', NULL, 1),
('后端项目', 'BACKEND', '后端相关项目', NULL, 2),
('全栈项目', 'FULLSTACK', '全栈项目', NULL, 3),
('移动应用', 'MOBILE', '移动应用项目', NULL, 4),
('工具脚本', 'TOOLS', '工具和脚本', NULL, 5),

-- 前端子分类
('Vue.js项目', 'VUE', 'Vue.js相关项目', 1, 1),
('React项目', 'REACT', 'React相关项目', 1, 2),
('Angular项目', 'ANGULAR', 'Angular相关项目', 1, 3),
('小程序', 'MINIPROGRAM', '微信小程序等', 1, 4),

-- 后端子分类
('Spring Boot', 'SPRINGBOOT', 'Spring Boot项目', 2, 1),
('Node.js', 'NODEJS', 'Node.js项目', 2, 2),
('Python', 'PYTHON', 'Python项目', 2, 3),
('Go语言', 'GOLANG', 'Go语言项目', 2, 4),

-- 移动应用子分类
('Android', 'ANDROID', 'Android应用', 4, 1),
('iOS', 'IOS', 'iOS应用', 4, 2),
('Flutter', 'FLUTTER', 'Flutter应用', 4, 3),
('React Native', 'REACTNATIVE', 'React Native应用', 4, 4);

-- 插入测试项目
INSERT INTO `projects` (`title`, `description`, `category_id`, `user_id`, `price`, `tech_stack`, `tags`, `status`) VALUES
('Vue3管理后台模板', '基于Vue3 + Element Plus的现代化管理后台模板，包含用户管理、权限控制、数据统计等功能', 6, 2, 100.00, 
 '["Vue 3", "TypeScript", "Element Plus", "Vite", "Pinia"]', 
 '["管理后台", "Vue3", "TypeScript", "响应式"]', 1),

('Spring Boot电商API', '完整的电商后端API，包含商品管理、订单处理、支付集成等功能', 10, 2, 200.00,
 '["Spring Boot", "MySQL", "Redis", "JWT", "Swagger"]',
 '["电商", "API", "Spring Boot", "微服务"]', 1),

('React Native购物App', '跨平台购物应用，支持商品浏览、购物车、订单管理等功能', 17, 3, 300.00,
 '["React Native", "Redux", "TypeScript", "Expo"]',
 '["移动应用", "购物", "跨平台", "React Native"]', 1),

('Vue3 + Spring Boot博客系统', '全栈博客系统，前后端分离架构，支持文章发布、评论、标签等功能', 3, 3, 250.00,
 '["Vue 3", "Spring Boot", "MySQL", "Redis", "Docker"]',
 '["博客", "全栈", "前后端分离", "Docker"]', 1),

('Python数据分析工具', '数据分析和可视化工具集，包含数据清洗、统计分析、图表生成等功能', 12, 2, 150.00,
 '["Python", "Pandas", "Matplotlib", "Jupyter", "FastAPI"]',
 '["数据分析", "Python", "可视化", "工具"]', 0);

-- 为用户创建积分账户
INSERT INTO `point_accounts` (`user_id`, `total_points`, `available_points`) VALUES
(1, 10000.00, 10000.00),
(2, 500.00, 500.00),
(3, 800.00, 800.00),
(4, 200.00, 200.00);

-- 插入积分交易记录
INSERT INTO `point_transactions` (`user_id`, `type`, `amount`, `balance_before`, `balance_after`, `description`) VALUES
(1, 1, 10000.00, 0.00, 10000.00, '管理员初始积分'),
(2, 1, 500.00, 0.00, 500.00, '新用户注册奖励'),
(3, 1, 500.00, 0.00, 500.00, '新用户注册奖励'),
(4, 1, 200.00, 0.00, 200.00, '新用户注册奖励'),
(2, 3, 100.00, 500.00, 600.00, '项目上传奖励'),
(2, 3, 200.00, 600.00, 800.00, '项目上传奖励'),
(3, 3, 300.00, 500.00, 800.00, '项目上传奖励');

-- 插入系统配置
INSERT INTO `system_configs` (`config_key`, `config_value`, `description`, `type`) VALUES
('site.name', '速码网', '网站名称', 'STRING'),
('site.description', '专业的源码交易平台', '网站描述', 'STRING'),
('upload.max_file_size', '100', '最大上传文件大小(MB)', 'NUMBER'),
('point.register_reward', '100', '注册奖励积分', 'NUMBER'),
('point.upload_reward', '100', '上传项目奖励积分', 'NUMBER'),
('point.docker_bonus', '100', 'Docker化项目额外奖励', 'NUMBER'),
('review.auto_approve', 'false', '是否自动审核通过', 'BOOLEAN'),
('deploy.demo_expire_hours', '24', '演示站过期时间(小时)', 'NUMBER'),
('payment.platform_rate', '0.3', '平台分成比例', 'NUMBER'),
('security.jwt_expire_hours', '24', 'JWT过期时间(小时)', 'NUMBER'),
('security.max_login_attempts', '5', '最大登录尝试次数', 'NUMBER'),
('feature.enable_search', 'true', '是否启用搜索功能', 'BOOLEAN'),
('feature.enable_deploy', 'true', '是否启用一键部署', 'BOOLEAN'),
('feature.enable_points_withdraw', 'false', '是否启用积分提现', 'BOOLEAN');

-- 更新项目统计数据
UPDATE `projects` SET 
  `view_count` = FLOOR(RAND() * 1000) + 100,
  `download_count` = FLOOR(RAND() * 100) + 10,
  `like_count` = FLOOR(RAND() * 50) + 5,
  `rating` = ROUND(RAND() * 2 + 3, 2),
  `rating_count` = FLOOR(RAND() * 20) + 5
WHERE `status` = 1;
