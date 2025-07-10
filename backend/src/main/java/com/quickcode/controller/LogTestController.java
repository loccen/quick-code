package com.quickcode.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志测试控制器
 * 用于测试新的日志格式配置
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class LogTestController {
    
    /**
     * 测试不同级别的日志输出
     */
    @GetMapping("/logs")
    public String testLogs() {
        log.trace("这是TRACE级别的日志 - 通常用于最详细的调试信息");
        log.debug("这是DEBUG级别的日志 - 用于调试信息");
        log.info("这是INFO级别的日志 - 用于一般信息");
        log.warn("这是WARN级别的日志 - 用于警告信息");
        log.error("这是ERROR级别的日志 - 用于错误信息");
        
        // 测试带参数的日志
        String username = "testUser";
        int userId = 12345;
        log.info("用户登录成功: username={}, userId={}", username, userId);
        
        // 测试异常日志
        try {
            throw new RuntimeException("这是一个测试异常");
        } catch (Exception e) {
            log.error("捕获到异常", e);
        }
        
        return "日志测试完成，请查看控制台和日志文件输出";
    }
    
    /**
     * 测试业务日志
     */
    @GetMapping("/business-logs")
    public String testBusinessLogs() {
        log.info("开始处理业务请求");
        
        // 模拟业务处理
        processOrder();
        processPayment();
        processNotification();
        
        log.info("业务请求处理完成");
        return "业务日志测试完成";
    }
    
    private void processOrder() {
        log.debug("正在处理订单信息");
        log.info("订单创建成功，订单号: ORDER-{}", System.currentTimeMillis());
    }
    
    private void processPayment() {
        log.debug("正在处理支付信息");
        log.info("支付处理成功，支付金额: {}", 99.99);
    }
    
    private void processNotification() {
        log.debug("正在发送通知");
        log.info("通知发送成功");
    }
}
