package com.quickcode.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author QuickCode Team
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${app.name:速码网}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", appName);
        response.put("version", appVersion);
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "速码网后端服务运行正常");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 版本信息接口
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> version() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", appName);
        response.put("version", appVersion);
        response.put("buildTime", LocalDateTime.now());
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("springBootVersion", org.springframework.boot.SpringBootVersion.getVersion());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from QuickCode Backend!");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }
}
