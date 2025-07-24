package com.quickcode.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置类
 * 启用定时任务和异步执行
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {
    
    public SchedulingConfig() {
        log.info("定时任务和异步执行配置已启用");
    }
}
