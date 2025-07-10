package com.quickcode.testutil;

import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 单元测试基类
 * 提供单元测试的通用配置和Mock支持
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("单元测试")
public abstract class BaseUnitTest extends BaseTest {

    @Override
    protected void initializeTest() {
        // 初始化Mockito注解
        MockitoAnnotations.openMocks(this);
        
        // 调用父类初始化
        super.initializeTest();
        
        // 单元测试特定的初始化
        setupUnitTest();
    }

    /**
     * 单元测试特定的初始化
     * 子类可以重写此方法进行特定的初始化
     */
    protected void setupUnitTest() {
        // 默认实现为空，子类可以重写
    }

    /**
     * 验证Mock对象的交互次数
     */
    protected void verifyNoMoreInteractions(Object... mocks) {
        org.mockito.Mockito.verifyNoMoreInteractions(mocks);
    }

    /**
     * 重置Mock对象
     */
    protected void resetMocks(Object... mocks) {
        org.mockito.Mockito.reset(mocks);
    }

    /**
     * 验证Mock方法被调用的次数
     */
    protected void verifyTimes(Object mock, int times) {
        // 这个方法需要在具体的验证中使用
        // 例如: verify(mock, times(times)).someMethod();
    }

    /**
     * 创建Mock对象
     */
    protected <T> T createMock(Class<T> classToMock) {
        return org.mockito.Mockito.mock(classToMock);
    }

    /**
     * 创建Spy对象
     */
    protected <T> T createSpy(T object) {
        return org.mockito.Mockito.spy(object);
    }
}
