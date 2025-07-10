# 速码网后端测试指南

## 概述

本文档描述了速码网后端项目的测试框架、测试策略和最佳实践。

## 测试框架

### 技术栈
- **JUnit 5**: 测试框架
- **Mockito**: Mock框架
- **AssertJ**: 断言库
- **Testcontainers**: 集成测试容器
- **Spring Boot Test**: Spring Boot测试支持
- **JaCoCo**: 代码覆盖率工具

### 测试分层
```
测试金字塔
    /\
   /  \
  /E2E \ (10%)
 /______\
/        \
/Integration\ (20%)
/____________\
/            \
/  Unit Tests  \ (70%)
/________________\
```

## 测试目录结构

```
src/test/java/com/quickcode/
├── unit/                    # 单元测试
│   ├── service/            # 服务层测试
│   ├── controller/         # 控制器测试
│   └── repository/         # 仓库层测试
├── integration/            # 集成测试
│   ├── api/               # API集成测试
│   └── database/          # 数据库集成测试
├── testutil/              # 测试工具类
│   ├── BaseTest.java      # 基础测试类
│   ├── BaseUnitTest.java  # 单元测试基类
│   ├── BaseIntegrationTest.java # 集成测试基类
│   ├── TestDataFactory.java # 测试数据工厂
│   └── TestUtils.java     # 测试工具方法
└── resources/
    ├── application-test.yml # 测试配置
    ├── logback-test.xml    # 测试日志配置
    ├── schema-test.sql     # 测试数据库结构
    └── data-test.sql       # 测试基础数据
```

## 运行测试

### 使用Maven命令

```bash
# 运行所有测试
mvn clean verify

# 运行单元测试
mvn clean test

# 运行集成测试
mvn clean verify -Pintegration-tests

# 运行测试并生成覆盖率报告
mvn clean verify jacoco:report

# 检查覆盖率阈值
mvn clean verify jacoco:check
```

### 使用Make命令

```bash
# 运行所有测试
make test

# 运行单元测试
make test-unit

# 运行集成测试
make test-integration

# 运行测试并生成覆盖率报告
make test-coverage

# 清理测试文件
make clean-test
```

### 使用脚本

```bash
# 运行测试脚本
./scripts/run-tests.sh all

# 运行单元测试
./scripts/run-tests.sh unit

# 运行集成测试
./scripts/run-tests.sh integration

# 生成覆盖率报告
./scripts/run-tests.sh coverage
```

## 测试编写规范

### 单元测试

```java
@DisplayName("用户服务测试")
class UserServiceTest extends BaseUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("根据ID查找用户 - 成功")
    void shouldFindUserById() {
        // Arrange
        Long userId = 1L;
        User expectedUser = TestDataFactory.createTestUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Act
        Optional<User> result = userService.findById(userId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }
}
```

### 集成测试

```java
@DisplayName("用户集成测试")
class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("保存用户到数据库 - 成功")
    @Transactional
    void shouldSaveUserToDatabase() {
        // Arrange
        User user = TestDataFactory.createTestUser();

        // Act
        User savedUser = userRepository.save(user);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(user.getUsername());
    }
}
```

### 控制器测试

```java
@DisplayName("认证控制器测试")
class AuthControllerTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @Override
    protected void initTestData() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        LoginRequest request = TestDataFactory.createLoginRequest();
        JwtResponse response = new JwtResponse();
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        TestUtils.performPost(mockMvc, "/api/auth/login", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

## 测试配置

### 测试环境配置

测试使用独立的配置文件 `application-test.yml`：

- 使用H2内存数据库进行单元测试
- 使用Testcontainers进行集成测试
- 简化的日志配置
- Mock外部服务

### 覆盖率要求

- 行覆盖率：≥ 80%
- 分支覆盖率：≥ 75%
- 方法覆盖率：≥ 80%

### 测试数据管理

使用 `TestDataFactory` 创建测试数据：

```java
// 创建测试用户
User user = TestDataFactory.createTestUser();

// 创建特定用户
User admin = TestDataFactory.createAdminUser();

// 创建登录请求
LoginRequest request = TestDataFactory.createLoginRequest();
```

## 最佳实践

### 1. 测试命名
- 使用 `@DisplayName` 注解提供中文描述
- 方法名使用 `should...When...` 格式
- 测试类名以 `Test` 结尾

### 2. AAA模式
```java
@Test
void shouldReturnUserWhenValidIdProvided() {
    // Arrange - 准备测试数据
    Long userId = 1L;
    User expectedUser = TestDataFactory.createTestUser();
    
    // Act - 执行被测试的操作
    User result = userService.findById(userId);
    
    // Assert - 验证结果
    assertThat(result).isEqualTo(expectedUser);
}
```

### 3. 测试隔离
- 每个测试方法独立运行
- 使用 `@Transactional` 自动回滚数据库操作
- 清理共享资源

### 4. Mock使用
- 只Mock外部依赖
- 验证重要的交互
- 使用 `@MockBean` 替换Spring容器中的Bean

### 5. 断言优化
- 使用AssertJ的流式断言
- 提供清晰的错误消息
- 验证所有重要属性

## 持续集成

### GitHub Actions
项目配置了自动化测试流水线：

1. **代码检出**
2. **环境准备** (JDK, MySQL, Redis)
3. **依赖缓存**
4. **单元测试**
5. **集成测试**
6. **覆盖率报告**
7. **安全扫描**
8. **性能测试** (仅主分支)

### 覆盖率报告
- 自动上传到Codecov
- 生成HTML报告
- 检查覆盖率阈值

## 故障排除

### 常见问题

1. **测试数据库连接失败**
   - 检查Testcontainers是否正常启动
   - 确认Docker环境可用

2. **覆盖率不达标**
   - 添加缺失的测试用例
   - 检查测试是否实际执行

3. **集成测试超时**
   - 增加等待时间
   - 检查容器启动状态

4. **Mock不生效**
   - 确认使用正确的注解
   - 检查Mock对象的配置

### 调试技巧

1. **启用详细日志**
   ```yaml
   logging:
     level:
       com.quickcode: DEBUG
   ```

2. **使用测试切片**
   ```java
   @WebMvcTest(AuthController.class)
   @DataJpaTest
   @JsonTest
   ```

3. **条件测试**
   ```java
   @EnabledIf("#{environment.acceptsProfiles('integration')}")
   @DisabledOnOs(OS.WINDOWS)
   ```

## 参考资源

- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ 文档](https://assertj.github.io/doc/)
- [Testcontainers 文档](https://www.testcontainers.org/)
- [Spring Boot 测试指南](https://spring.io/guides/gs/testing-web/)
