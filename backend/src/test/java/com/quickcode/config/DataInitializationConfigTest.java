package com.quickcode.config;

import com.quickcode.repository.CategoryRepository;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

/**
 * 数据初始化配置单元测试
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DataInitializationConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private DataInitializationConfig dataInitializationConfig;

    @Test
    void shouldSkipInitializationWhenDataExists() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(5L);
        when(categoryRepository.count()).thenReturn(10L);
        when(projectRepository.count()).thenReturn(3L);

        // Act
        dataInitializationConfig.run(applicationArguments);

        // Assert
        verify(userRepository, times(1)).count();
        verify(categoryRepository, times(1)).count();
        verify(projectRepository, times(1)).count();
        
        // 验证没有保存新数据
        verify(userRepository, never()).saveAll(any());
        verify(categoryRepository, never()).saveAll(any());
        verify(projectRepository, never()).saveAll(any());
    }

    @Test
    void shouldInitializeDataWhenEmpty() throws Exception {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");

        // Act
        dataInitializationConfig.run(applicationArguments);

        // Assert
        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).saveAll(any());
        verify(passwordEncoder, atLeast(1)).encode(anyString()); // 至少编码一次密码
    }
}
