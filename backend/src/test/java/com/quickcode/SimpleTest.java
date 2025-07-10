package com.quickcode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 简单测试类
 * 验证测试框架基础功能
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@DisplayName("简单测试")
class SimpleTest {

    @Test
    @DisplayName("基础断言测试")
    void shouldPassBasicAssertion() {
        // Arrange
        String expected = "Hello World";
        
        // Act
        String actual = "Hello World";
        
        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("数字计算测试")
    void shouldCalculateCorrectly() {
        // Arrange
        int a = 5;
        int b = 3;
        
        // Act
        int result = a + b;
        
        // Assert
        assertThat(result).isEqualTo(8);
    }

    @Test
    @DisplayName("布尔值测试")
    void shouldReturnTrue() {
        // Arrange & Act
        boolean result = true;
        
        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("字符串包含测试")
    void shouldContainSubstring() {
        // Arrange
        String text = "速码网后端测试框架";
        
        // Act & Assert
        assertThat(text).contains("测试框架");
    }

    @Test
    @DisplayName("集合测试")
    void shouldContainElements() {
        // Arrange
        java.util.List<String> list = java.util.Arrays.asList("Java", "Spring", "JUnit");
        
        // Act & Assert
        assertThat(list)
            .hasSize(3)
            .contains("Java", "Spring", "JUnit");
    }
}
