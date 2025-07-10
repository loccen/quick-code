-- 速码网数据库初始化脚本
-- 创建数据库和基础表结构

-- 设置字符集
SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;

-- 使用数据库
USE quick_code;

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `email` varchar(100) NOT NULL COMMENT '邮箱',
    `password` varchar(255) NOT NULL COMMENT '密码（加密）',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-正常，2-待验证',
    `email_verified` tinyint DEFAULT '0' COMMENT '邮箱是否验证：0-未验证，1-已验证',
    `two_factor_enabled` tinyint DEFAULT '0' COMMENT '是否启用双因素认证',
    `two_factor_secret` varchar(32) DEFAULT NULL COMMENT '双因素认证密钥',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(45) DEFAULT NULL COMMENT '最后登录IP',
    `login_failure_count` int DEFAULT '0' COMMENT '登录失败次数',
    `locked_until` datetime DEFAULT NULL COMMENT '账户锁定到期时间',
    `points` decimal(10, 2) DEFAULT '0.00' COMMENT '用户积分余额',
    `is_vip` tinyint DEFAULT '0' COMMENT '是否为永久会员：0-否，1-是',
    `vip_expires_at` datetime DEFAULT NULL COMMENT '会员到期时间',
    `bio` varchar(500) DEFAULT NULL COMMENT '用户简介',
    `website` varchar(200) DEFAULT NULL COMMENT '用户网站',
    `location` varchar(100) DEFAULT NULL COMMENT '用户位置',
    `email_verification_token` varchar(64) DEFAULT NULL COMMENT '邮箱验证令牌',
    `email_verification_expires_at` datetime DEFAULT NULL COMMENT '邮箱验证令牌过期时间',
    `password_reset_token` varchar(64) DEFAULT NULL COMMENT '密码重置令牌',
    `password_reset_expires_at` datetime DEFAULT NULL COMMENT '密码重置令牌过期时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_email_verification_token` (`email_verification_token`),
    KEY `idx_password_reset_token` (`password_reset_token`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';

-- 用户角色表
CREATE TABLE IF NOT EXISTS `user_roles` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` varchar(50) NOT NULL COMMENT '角色名称',
    `role_code` varchar(50) NOT NULL COMMENT '角色代码',
    `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
    `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role_relations` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `fk_user_role_user` (`user_id`),
    KEY `fk_user_role_role` (`role_id`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `user_roles` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表';

-- 项目分类表
CREATE TABLE IF NOT EXISTS `project_categories` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` varchar(50) NOT NULL COMMENT '分类名称',
    `code` varchar(50) NOT NULL COMMENT '分类代码',
    `description` varchar(255) DEFAULT NULL COMMENT '分类描述',
    `parent_id` bigint DEFAULT NULL COMMENT '父分类ID',
    `sort_order` int DEFAULT '0' COMMENT '排序顺序',
    `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目分类表';

-- 项目表
CREATE TABLE IF NOT EXISTS `projects` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    `title` varchar(100) NOT NULL COMMENT '项目标题',
    `description` text COMMENT '项目描述',
    `category_id` bigint NOT NULL COMMENT '分类ID',
    `user_id` bigint NOT NULL COMMENT '上传用户ID',
    `price` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '项目价格（积分）',
    `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片URL',
    `demo_url` varchar(255) DEFAULT NULL COMMENT '演示地址',
    `source_file_url` varchar(255) DEFAULT NULL COMMENT '源码文件URL',
    `docker_image` varchar(255) DEFAULT NULL COMMENT 'Docker镜像名称',
    `tech_stack` json DEFAULT NULL COMMENT '技术栈（JSON格式）',
    `tags` json DEFAULT NULL COMMENT '标签（JSON格式）',
    `download_count` int DEFAULT '0' COMMENT '下载次数',
    `view_count` int DEFAULT '0' COMMENT '浏览次数',
    `like_count` int DEFAULT '0' COMMENT '点赞次数',
    `rating` decimal(3, 2) DEFAULT '0.00' COMMENT '评分（0-5）',
    `rating_count` int DEFAULT '0' COMMENT '评分人数',
    `status` tinyint DEFAULT '0' COMMENT '状态：0-待审核，1-已发布，2-已下架，3-审核拒绝',
    `is_featured` tinyint DEFAULT '0' COMMENT '是否精选：0-否，1-是',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `published_time` datetime DEFAULT NULL COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY `fk_project_category` (`category_id`),
    KEY `fk_project_user` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_price` (`price`),
    KEY `idx_download_count` (`download_count`),
    CONSTRAINT `fk_project_category` FOREIGN KEY (`category_id`) REFERENCES `project_categories` (`id`),
    CONSTRAINT `fk_project_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目表';

-- 积分账户表
CREATE TABLE IF NOT EXISTS `point_accounts` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '账户ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `total_points` decimal(15, 2) DEFAULT '0.00' COMMENT '总积分',
    `available_points` decimal(15, 2) DEFAULT '0.00' COMMENT '可用积分',
    `frozen_points` decimal(15, 2) DEFAULT '0.00' COMMENT '冻结积分',
    `total_earned` decimal(15, 2) DEFAULT '0.00' COMMENT '累计获得积分',
    `total_spent` decimal(15, 2) DEFAULT '0.00' COMMENT '累计消费积分',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    CONSTRAINT `fk_point_account_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分账户表';

-- 积分交易记录表
CREATE TABLE IF NOT EXISTS `point_transactions` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '交易ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `type` tinyint NOT NULL COMMENT '交易类型：1-充值，2-消费，3-奖励，4-退款，5-提现',
    `amount` decimal(15, 2) NOT NULL COMMENT '交易金额',
    `balance_before` decimal(15, 2) NOT NULL COMMENT '交易前余额',
    `balance_after` decimal(15, 2) NOT NULL COMMENT '交易后余额',
    `description` varchar(255) DEFAULT NULL COMMENT '交易描述',
    `reference_id` bigint DEFAULT NULL COMMENT '关联ID（订单ID、项目ID等）',
    `reference_type` varchar(50) DEFAULT NULL COMMENT '关联类型',
    `status` tinyint DEFAULT '1' COMMENT '状态：0-失败，1-成功，2-处理中',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `fk_point_transaction_user` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_reference` (
        `reference_type`,
        `reference_id`
    ),
    CONSTRAINT `fk_point_transaction_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分交易记录表';

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` varchar(32) NOT NULL COMMENT '订单号',
    `user_id` bigint NOT NULL COMMENT '购买用户ID',
    `project_id` bigint NOT NULL COMMENT '项目ID',
    `seller_id` bigint NOT NULL COMMENT '卖家用户ID',
    `amount` decimal(10, 2) NOT NULL COMMENT '订单金额（积分）',
    `status` tinyint DEFAULT '0' COMMENT '订单状态：0-待支付，1-已支付，2-已取消，3-已退款',
    `payment_method` varchar(20) DEFAULT 'POINTS' COMMENT '支付方式',
    `paid_time` datetime DEFAULT NULL COMMENT '支付时间',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `fk_order_user` (`user_id`),
    KEY `fk_order_project` (`project_id`),
    KEY `fk_order_seller` (`seller_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_order_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`),
    CONSTRAINT `fk_order_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_configs` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` varchar(100) NOT NULL COMMENT '配置键',
    `config_value` text COMMENT '配置值',
    `description` varchar(255) DEFAULT NULL COMMENT '配置描述',
    `type` varchar(20) DEFAULT 'STRING' COMMENT '配置类型：STRING, NUMBER, BOOLEAN, JSON',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表';

SET FOREIGN_KEY_CHECKS = 1;
