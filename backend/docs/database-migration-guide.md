# 数据库迁移指南

## 概述

本文档描述了速码网项目的数据库迁移策略和最佳实践。

## 环境配置

### 开发环境
- **DDL模式**: `update` - 自动根据实体变更更新数据库结构
- **优点**: 开发便利，无需手动维护SQL脚本
- **适用场景**: 本地开发、测试环境

### 生产环境
- **DDL模式**: `validate` - 仅验证数据库结构与实体匹配
- **优点**: 安全可控，避免意外的结构变更
- **适用场景**: 生产环境、预发布环境

## 权限架构简化迁移

### 背景
项目从复杂的Role-Permission多对多关系简化为User实体的isAdmin布尔字段。

### 迁移步骤

#### 1. 开发环境自动迁移
开发环境配置了`ddl-auto: update`，会自动执行以下操作：
- 在users表中添加`is_admin`字段
- 删除不再使用的表：`user_roles`, `permissions`, `role_permission_relations`, `user_role_relations`

#### 2. 生产环境手动迁移
生产环境需要手动执行迁移脚本：

```sql
-- 1. 备份现有数据
CREATE TABLE users_backup AS SELECT * FROM users;
CREATE TABLE user_roles_backup AS SELECT * FROM user_roles;
CREATE TABLE user_role_relations_backup AS SELECT * FROM user_role_relations;

-- 2. 添加isAdmin字段
ALTER TABLE users ADD COLUMN is_admin TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为管理员：0-否，1-是';

-- 3. 迁移现有角色数据到isAdmin字段
UPDATE users u 
SET is_admin = 1 
WHERE u.id IN (
    SELECT DISTINCT urr.user_id 
    FROM user_role_relations urr 
    JOIN user_roles ur ON urr.role_id = ur.id 
    WHERE ur.role_code = 'ADMIN'
);

-- 4. 添加索引
ALTER TABLE users ADD INDEX idx_is_admin (is_admin);

-- 5. 验证数据迁移
SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN is_admin = 1 THEN 1 ELSE 0 END) as admin_users,
    SUM(CASE WHEN is_admin = 0 THEN 1 ELSE 0 END) as regular_users
FROM users;

-- 6. 删除旧表（确认数据正确后执行）
-- DROP TABLE role_permission_relations;
-- DROP TABLE user_role_relations;
-- DROP TABLE permissions;
-- DROP TABLE user_roles;
```

#### 3. 回滚计划
如需回滚到原有架构：

```sql
-- 1. 恢复备份表
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS user_role_relations;

RENAME TABLE users_backup TO users;
RENAME TABLE user_roles_backup TO user_roles;
RENAME TABLE user_role_relations_backup TO user_role_relations;

-- 2. 重新创建权限相关表
-- 执行原始的01-init-database.sql中的权限表创建语句
```

## 最佳实践

### 1. 迁移前准备
- [ ] 备份生产数据库
- [ ] 在测试环境验证迁移脚本
- [ ] 准备回滚方案
- [ ] 通知相关团队成员

### 2. 迁移执行
- [ ] 在维护窗口期执行
- [ ] 监控应用日志
- [ ] 验证功能正常
- [ ] 确认性能指标

### 3. 迁移后验证
- [ ] 检查数据完整性
- [ ] 验证用户权限功能
- [ ] 运行自动化测试
- [ ] 监控错误日志

## 配置说明

### JPA DDL模式说明
- `create`: 每次启动时删除并重新创建表（仅用于测试）
- `create-drop`: 启动时创建，关闭时删除（仅用于测试）
- `update`: 根据实体变更更新表结构（开发环境推荐）
- `validate`: 仅验证表结构与实体匹配（生产环境推荐）
- `none`: 不执行任何DDL操作

### 环境变量配置
生产环境通过环境变量配置敏感信息：
```bash
export DB_URL="jdbc:mysql://prod-db:3306/quick_code"
export DB_USERNAME="quick_code_user"
export DB_PASSWORD="secure_password"
export JWT_SECRET="your_jwt_secret"
```

## 故障排除

### 常见问题
1. **字段类型不匹配**: 检查实体定义与数据库字段类型
2. **外键约束错误**: 确保关联数据存在
3. **索引冲突**: 检查唯一约束和索引定义

### 监控指标
- 数据库连接池状态
- SQL执行时间
- 错误日志频率
- 应用响应时间

## 联系方式
如有问题，请联系开发团队或查看项目文档。
