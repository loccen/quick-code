package com.quickcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 速码网主应用启动类
 * 
 * @author QuickCode Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class QuickCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickCodeApplication.class, args);
        System.out.println("""
            
            ========================================
            🚀 速码网后端服务启动成功！
            ========================================
            📖 API文档: http://localhost:8080/swagger-ui.html
            🔍 健康检查: http://localhost:8080/actuator/health
            📊 监控面板: http://localhost:8080/actuator
            ========================================
            
            """);
    }
}
