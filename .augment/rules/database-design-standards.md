---
type: "agent_requested"
description: "## 适用场景 本规范适用于速码网项目的所有数据库相关工作，包括MySQL数据库表结构设计和创建、数据库索引设计和优化、数据迁移脚本编写、Redis缓存设计和使用、数据库性能优化、数据备份和恢复策略。AI在进行任何数据库相关的设计、开发或优化时，必须严格遵循本规范。"
---
# 速码网数据库设计规范指南

## 1. MySQL数据库设计原则

### 1.1 命名规范
```sql
-- 数据库命名：小写字母+下划线
CREATE DATABASE quickcode_dev;

-- 表命名：小写字母+下划线，使用复数形式
CREATE TABLE users;           -- 用户表
CREATE TABLE projects;        -- 项目表
CREATE TABLE orders;          -- 订单表

-- 字段命名：小写字母+下划线
user_id, created_at, updated_at, is_deleted

-- 索引命名规范
idx_users_email              -- 普通索引
uk_users_username            -- 唯一索引
fk_orders_user_id           -- 外键索引
```

### 1.2 字段类型规范
```sql
-- 主键设计
id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID'

-- 字符串类型
username VARCHAR(50) NOT NULL COMMENT '用户名'
email VARCHAR(100) NOT NULL COMMENT '邮箱地址'
description TEXT COMMENT '项目描述'

-- 数值类型
price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格'
downloads INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '下载次数'

-- 时间类型
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'

-- 布尔和JSON类型
is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除'
tech_stack JSON COMMENT '技术栈'
```

### 1.3 表结构设计
```sql
-- 用户表
CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    points DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status TINYINT UNSIGNED NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_users_email (email),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 项目表
CREATE TABLE projects (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    tech_stack JSON,
    price DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    status TINYINT UNSIGNED NOT NULL DEFAULT 0,
    downloads INT UNSIGNED NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_projects_status (status),
    FULLTEXT INDEX ft_projects_search (title, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE orders (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    buyer_id BIGINT UNSIGNED NOT NULL,
    project_id BIGINT UNSIGNED NOT NULL,
    amount DECIMAL(8,2) NOT NULL,
    status TINYINT UNSIGNED NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    INDEX idx_orders_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 1.4 索引设计策略
```sql
-- 主键索引（自动创建）
PRIMARY KEY (id)

-- 唯一索引和普通索引
UNIQUE KEY uk_users_email (email)
INDEX idx_projects_status (status)
INDEX idx_projects_status_created (status, created_at)  -- 复合索引

-- 全文索引
FULLTEXT INDEX ft_projects_search (title, description)
```

## 2. 数据库性能优化

### 2.1 查询优化
```sql
-- 使用EXPLAIN分析查询计划
EXPLAIN SELECT id, title FROM projects WHERE status = 1;

-- 避免SELECT *，明确指定字段
SELECT id, title, price FROM projects WHERE status = 1;

-- 避免在WHERE子句中使用函数
SELECT * FROM users WHERE created_at >= '2023-01-01' AND created_at < '2023-01-02';

-- 使用EXISTS代替IN（大数据集）
SELECT * FROM users u WHERE EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id);
```

### 2.2 分页优化
```sql
-- 游标分页（推荐）
SELECT * FROM projects WHERE id > 1000 ORDER BY id LIMIT 20;
```

### 2.3 批量操作优化
```sql
-- 批量插入
INSERT INTO point_transactions (user_id, type, amount) VALUES
(1, 1, 100.00), (2, 1, 200.00), (3, 1, 150.00);

-- 批量更新
UPDATE projects SET downloads = downloads + 1 WHERE id IN (1, 2, 3);
```

## 3. Redis缓存设计规范

### 3.1 缓存键命名规范
```bash
# 命名格式：项目名:模块:类型:标识
quickcode:user:info:123              # 用户信息缓存
quickcode:project:detail:456         # 项目详情缓存
quickcode:user:session:token:abc123  # 用户会话缓存
```

### 3.2 缓存策略设计
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("user:info", config.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("project:detail", config.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}

@Service
public class ProjectService {
    @Cacheable(value = "project:detail", key = "#id")
    public ProjectDetailResponse getProjectById(Long id) {
        return projectRepository.findById(id)
            .map(projectMapper::toDetailResponse)
            .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    @CacheEvict(value = "project:detail", key = "#id")
    public void updateProject(Long id, ProjectUpdateRequest request) {
        // 更新项目信息，自动清除缓存
    }
}
```

### 3.3 缓存数据结构
```java
// 字符串缓存
@Component
public class UserCacheService {
    @Autowired private StringRedisTemplate redisTemplate;

    public void cacheUserToken(String token, Long userId, Duration expiration) {
        String key = "quickcode:user:token:" + token;
        redisTemplate.opsForValue().set(key, userId.toString(), expiration);
    }
}

// Hash缓存
@Component
public class ProjectCacheService {
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    public void incrementDownloads(Long projectId) {
        String key = "quickcode:project:stats:" + projectId;
        redisTemplate.opsForHash().increment(key, "downloads", 1);
    }
}
```

## 4. 数据迁移和版本控制

### 4.1 Flyway迁移脚本
```sql
-- V1__Create_users_table.sql
CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- V2__Add_user_points_column.sql
ALTER TABLE users ADD COLUMN points DECIMAL(10,2) NOT NULL DEFAULT 0.00;
```

### 4.2 数据迁移最佳实践
```sql
-- 安全的列添加
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500) DEFAULT NULL;

-- 安全的索引添加
CREATE INDEX CONCURRENTLY idx_projects_created_at ON projects(created_at);
```

## 5. 数据备份和监控

### 5.1 备份策略
```bash
# 全量备份
mysqldump --single-transaction quickcode_prod > backup_$(date +%Y%m%d).sql

# Redis备份
redis-cli --rdb /backup/redis_$(date +%Y%m%d).rdb
```

### 5.2 性能监控
```sql
-- 慢查询监控
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

-- 查看表大小
SELECT table_name, ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables WHERE table_schema = 'quickcode_prod';
```
