package com.quickcode.testutil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

/**
 * 基础测试类
 * 提供所有测试的通用配置和工具方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseTest {

    /**
     * 测试前置设置
     * 在每个测试方法执行前调用
     */
    @BeforeEach
    void setUp() {
        // 子类可以重写此方法进行特定的初始化
        initializeTest();
    }

    /**
     * 初始化测试
     * 子类可以重写此方法进行特定的初始化
     */
    protected void initializeTest() {
        // 默认实现为空，子类可以重写
    }

    /**
     * 获取测试用户ID
     */
    protected Long getTestUserId() {
        return 1L;
    }

    /**
     * 获取测试用户名
     */
    protected String getTestUsername() {
        return "testuser";
    }

    /**
     * 获取测试邮箱
     */
    protected String getTestEmail() {
        return "test@example.com";
    }

    /**
     * 获取测试密码
     */
    protected String getTestPassword() {
        return "Test123456!";
    }

    /**
     * 获取测试JWT令牌
     */
    protected String getTestJwtToken() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    }

    /**
     * 获取测试刷新令牌
     */
    protected String getTestRefreshToken() {
        return "refresh.test.token";
    }

    /**
     * 睡眠指定毫秒数（用于测试时间相关功能）
     */
    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("测试睡眠被中断", e);
        }
    }

    /**
     * 断言异常消息包含指定文本
     */
    protected void assertExceptionMessage(Exception exception, String expectedMessage) {
        if (exception.getMessage() == null || !exception.getMessage().contains(expectedMessage)) {
            throw new AssertionError(
                String.format("期望异常消息包含 '%s'，但实际消息为: '%s'", 
                    expectedMessage, exception.getMessage())
            );
        }
    }

    /**
     * 创建测试用的时间戳
     */
    protected long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 验证对象不为空
     */
    protected void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new AssertionError(message);
        }
    }

    /**
     * 验证字符串不为空
     */
    protected void assertNotEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new AssertionError(message);
        }
    }
}
