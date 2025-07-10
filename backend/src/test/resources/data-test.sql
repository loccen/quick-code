-- 测试环境基础数据脚本
-- 这个脚本会在每次测试运行时执行，用于插入测试所需的基础数据

-- 插入测试角色数据
INSERT INTO roles (id, name, description, created_at, updated_at) VALUES 
(1, 'USER', '普通用户', NOW(), NOW()),
(2, 'ADMIN', '管理员', NOW(), NOW()),
(3, 'MODERATOR', '审核员', NOW(), NOW());

-- 插入测试权限数据
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at) VALUES 
(1, 'USER_READ', '查看用户信息', 'USER', 'READ', NOW(), NOW()),
(2, 'USER_WRITE', '修改用户信息', 'USER', 'WRITE', NOW(), NOW()),
(3, 'PROJECT_READ', '查看项目信息', 'PROJECT', 'READ', NOW(), NOW()),
(4, 'PROJECT_WRITE', '修改项目信息', 'PROJECT', 'WRITE', NOW(), NOW()),
(5, 'PROJECT_ADMIN', '管理项目', 'PROJECT', 'ADMIN', NOW(), NOW());

-- 插入测试用户数据（密码为 'password123' 的BCrypt哈希值）
INSERT INTO users (id, username, email, password_hash, avatar_url, points, status, created_at, updated_at) VALUES 
(1, 'testuser', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe5ldjoiKDpjIsIQaAJDSG', NULL, 1000.00, 1, NOW(), NOW()),
(2, 'admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe5ldjoiKDpjIsIQaAJDSG', NULL, 5000.00, 1, NOW(), NOW()),
(3, 'testuser2', 'test2@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFe5ldjoiKDpjIsIQaAJDSG', NULL, 500.00, 1, NOW(), NOW());

-- 插入用户角色关联数据
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), -- testuser -> USER
(2, 1), -- admin -> USER
(2, 2), -- admin -> ADMIN
(3, 1); -- testuser2 -> USER

-- 插入角色权限关联数据
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(1, 1), -- USER -> USER_READ
(1, 3), -- USER -> PROJECT_READ
(2, 1), -- ADMIN -> USER_READ
(2, 2), -- ADMIN -> USER_WRITE
(2, 3), -- ADMIN -> PROJECT_READ
(2, 4), -- ADMIN -> PROJECT_WRITE
(2, 5); -- ADMIN -> PROJECT_ADMIN

-- 插入测试积分账户数据
INSERT INTO point_accounts (id, user_id, balance, frozen_balance, total_earned, total_spent, created_at, updated_at) VALUES 
(1, 1, 1000.00, 0.00, 1000.00, 0.00, NOW(), NOW()),
(2, 2, 5000.00, 0.00, 5000.00, 0.00, NOW(), NOW()),
(3, 3, 500.00, 0.00, 500.00, 0.00, NOW(), NOW());

-- 插入测试积分交易记录
INSERT INTO point_transactions (id, user_id, type, amount, balance_after, description, reference_id, reference_type, created_at) VALUES 
(1, 1, 'REWARD', 1000.00, 1000.00, '注册奖励', NULL, 'REGISTER', NOW()),
(2, 2, 'REWARD', 5000.00, 5000.00, '管理员初始积分', NULL, 'ADMIN_INIT', NOW()),
(3, 3, 'REWARD', 500.00, 500.00, '注册奖励', NULL, 'REGISTER', NOW());
