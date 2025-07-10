-- 测试环境数据库初始化脚本
-- 这个脚本会在每次测试运行时执行，用于创建测试所需的表结构

-- 注意：由于使用了JPA的create-drop模式，这个脚本主要用于创建JPA不会自动创建的表或视图

-- 创建测试用的序列（如果需要）
-- CREATE SEQUENCE IF NOT EXISTS test_sequence START WITH 1000;

-- 创建测试用的视图（如果需要）
-- CREATE VIEW IF NOT EXISTS user_stats AS 
-- SELECT u.id, u.username, COUNT(p.id) as project_count 
-- FROM users u LEFT JOIN projects p ON u.id = p.user_id 
-- GROUP BY u.id, u.username;

-- 创建测试用的索引（如果JPA没有自动创建）
-- CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
-- CREATE INDEX IF NOT EXISTS idx_project_status ON projects(status);

-- 测试环境特殊配置
SET REFERENTIAL_INTEGRITY FALSE; -- H2数据库允许在测试时禁用外键约束检查
