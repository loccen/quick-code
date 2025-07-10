package com.quickcode.testutil;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 集成测试基类 提供集成测试的通用配置，使用H2内存数据库
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("集成测试")
@Transactional
public abstract class BaseIntegrationTest extends BaseTest {

  @Override
  protected void initializeTest() {
    super.initializeTest();
    setupIntegrationTest();
  }

  /**
   * 集成测试特定的初始化 子类可以重写此方法进行特定的初始化
   */
  protected void setupIntegrationTest() {
    // 默认实现为空，子类可以重写
  }

  /**
   * 获取测试数据库连接信息
   */
  protected String getTestDatabaseUrl() {
    return "jdbc:h2:mem:testdb";
  }

  /**
   * 获取测试Redis配置信息
   */
  protected String getTestRedisHost() {
    return "localhost";
  }

  /**
   * 获取测试Redis端口
   */
  protected Integer getTestRedisPort() {
    return 6379;
  }

  /**
   * 清理测试数据 在需要时可以调用此方法清理测试数据
   */
  protected void cleanupTestData() {
    // 由于使用了@Transactional，测试数据会自动回滚
    // H2内存数据库在测试结束后会自动清理
  }
}
