package com.quickcode.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于验证前后端通信和数据库连接
 * 
 * @author QuickCode Team
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class TestController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 基础连接测试
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 数据库连接测试
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            response.put("status", "success");
            response.put("message", "数据库连接正常");
            response.put("database", connection.getCatalog());
            response.put("url", connection.getMetaData().getURL());
            response.put("timestamp", LocalDateTime.now());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "数据库连接失败: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(500).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Redis连接测试
     */
    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> testRedis() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String testKey = "test:connection:" + System.currentTimeMillis();
            String testValue = "Hello Redis!";
            
            // 写入测试
            redisTemplate.opsForValue().set(testKey, testValue);
            
            // 读取测试
            String retrievedValue = redisTemplate.opsForValue().get(testKey);
            
            // 删除测试数据
            redisTemplate.delete(testKey);
            
            response.put("status", "success");
            response.put("message", "Redis连接正常");
            response.put("testKey", testKey);
            response.put("testValue", testValue);
            response.put("retrievedValue", retrievedValue);
            response.put("timestamp", LocalDateTime.now());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Redis连接失败: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(500).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 综合连接测试
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> testAll() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> results = new HashMap<>();
        
        // 测试数据库
        try (Connection connection = dataSource.getConnection()) {
            Map<String, Object> dbResult = new HashMap<>();
            dbResult.put("status", "success");
            dbResult.put("database", connection.getCatalog());
            results.put("database", dbResult);
        } catch (Exception e) {
            Map<String, Object> dbResult = new HashMap<>();
            dbResult.put("status", "error");
            dbResult.put("error", e.getMessage());
            results.put("database", dbResult);
        }
        
        // 测试Redis
        try {
            String testKey = "test:all:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "test");
            redisTemplate.delete(testKey);
            
            Map<String, Object> redisResult = new HashMap<>();
            redisResult.put("status", "success");
            results.put("redis", redisResult);
        } catch (Exception e) {
            Map<String, Object> redisResult = new HashMap<>();
            redisResult.put("status", "error");
            redisResult.put("error", e.getMessage());
            results.put("redis", redisResult);
        }
        
        response.put("message", "连接测试完成");
        response.put("results", results);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST请求测试
     */
    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Echo test successful");
        response.put("receivedData", request);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}
