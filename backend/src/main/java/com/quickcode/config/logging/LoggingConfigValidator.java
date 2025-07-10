package com.quickcode.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 日志配置验证器
 * 在应用启动完成后验证日志配置是否正确
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class LoggingConfigValidator {
    
    /**
     * 应用启动完成后验证日志配置
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateLoggingConfiguration() {
        log.info("========================================");
        log.info("🔍 开始验证日志配置...");
        log.info("========================================");
        
        // 测试不同级别的日志
        testLogLevels();
        
        // 测试路径转换器
        testPathConverter();
        
        // 验证完成
        log.info("========================================");
        log.info("✅ 日志配置验证完成！");
        log.info("📝 请检查上述日志输出格式是否包含文件路径和行号");
        log.info("🔗 在IDE中点击文件路径应该能够快速跳转到对应代码位置");
        log.info("📁 日志文件位置: logs/quick-code.log");
        log.info("🚨 错误日志文件位置: logs/quick-code-error.log");
        log.info("🧪 测试接口: GET /api/test/logs");
        log.info("========================================");
    }
    
    /**
     * 测试不同级别的日志输出
     */
    private void testLogLevels() {
        log.debug("🐛 DEBUG级别日志测试 - 调试信息");
        log.info("ℹ️ INFO级别日志测试 - 一般信息");
        log.warn("⚠️ WARN级别日志测试 - 警告信息");
        log.error("❌ ERROR级别日志测试 - 错误信息");
    }
    
    /**
     * 测试路径转换器功能
     */
    private void testPathConverter() {
        log.info("🛠️ 测试自定义路径转换器功能");
        log.info("📍 当前方法位置: LoggingConfigValidator.testPathConverter()");
        log.info("📂 期望的文件路径格式: backend/src/main/java/com/quickcode/config/logging/LoggingConfigValidator.java:行号");
    }
}
