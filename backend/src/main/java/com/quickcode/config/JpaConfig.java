package com.quickcode.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA配置类
 * 启用JPA审计、Repository扫描和事务管理
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.quickcode.repository")
@EnableTransactionManagement
public class JpaConfig {
    
    // JPA配置已通过application.yml完成
    // 这里主要是启用相关功能
}
