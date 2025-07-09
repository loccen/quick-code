---
type: "agent_requested"
description: "## 适用场景 本规范适用于速码网项目的所有测试相关工作，包括编写单元测试、集成测试、端到端测试、测试用例设计和实现、测试覆盖率分析和改进、自动化测试流程配置、性能测试和安全测试、测试代码审查和质量检查。AI在编写任何测试代码或配置测试环境时，必须严格遵循本规范。"
---
# 速码网测试规范指南


## 1. 测试策略概述

### 1.1 测试金字塔
- **单元测试 (70%)**：函数/方法测试、组件测试
- **集成测试 (20%)**：API接口测试、服务间集成测试
- **端到端测试 (10%)**：关键用户场景、浏览器自动化测试

### 1.2 测试覆盖率要求
- 单元测试覆盖率：≥ 80%
- 集成测试覆盖率：≥ 60%
- 端到端测试覆盖率：≥ 90%（关键业务流程）
- 代码分支覆盖率：≥ 75%

### 1.3 测试质量标准
- 测试用例必须独立运行
- 测试结果必须可重复
- 测试执行时间合理（单元测试<1s，集成测试<10s）
- 测试失败时提供清晰的错误信息

## 2. 前端测试规范

### 2.1 Vue3组件单元测试

#### 测试框架配置
```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    coverage: {
      thresholds: { global: { branches: 75, functions: 80, lines: 80 } }
    }
  }
})
```

#### 组件测试示例
```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProjectCard from '@/components/business/ProjectCard.vue'

describe('ProjectCard', () => {
  const mockProject = {
    id: 1,
    title: '测试项目',
    price: 99.99
  }

  it('应该正确渲染项目信息', () => {
    const wrapper = mount(ProjectCard, { props: { project: mockProject } })
    expect(wrapper.find('.project-card__title').text()).toBe('测试项目')
  })

  it('应该在点击时触发事件', async () => {
    const wrapper = mount(ProjectCard, { props: { project: mockProject } })
    await wrapper.find('.project-card').trigger('click')
    expect(wrapper.emitted('click')).toBeTruthy()
  })
})
```

#### Pinia状态管理测试
```typescript
import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('User Store', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('应该正确初始化用户状态', () => {
    const userStore = useUserStore()
    expect(userStore.isLoggedIn).toBe(false)
  })

  it('应该正确处理用户登录', async () => {
    const userStore = useUserStore()
    await userStore.login('test@example.com', 'password')
    expect(userStore.isLoggedIn).toBe(true)
  })
})
```

### 2.2 API服务测试
```typescript
import { describe, it, expect, vi } from 'vitest'
import { userApi } from '@/services/userApi'
import { http } from '@/utils/request'

vi.mock('@/utils/request')
const mockHttp = vi.mocked(http)

describe('User API', () => {
  it('应该正确调用登录API', async () => {
    const mockResponse = { code: 200, data: { token: 'mock-token' } }
    mockHttp.post.mockResolvedValue(mockResponse)
    const result = await userApi.login('test@example.com', 'password')
    expect(result).toEqual(mockResponse.data)
  })
})
```

### 2.3 端到端测试
```typescript
import { test, expect } from '@playwright/test'

test('用户注册和登录流程', async ({ page }) => {
  await page.goto('/register')
  await page.fill('[data-testid="email-input"]', 'test@example.com')
  await page.fill('[data-testid="password-input"]', 'password123')
  await page.click('[data-testid="register-button"]')
  await expect(page.locator('[data-testid="success-message"]')).toBeVisible()

  await page.goto('/login')
  await page.fill('[data-testid="email-input"]', 'test@example.com')
  await page.fill('[data-testid="password-input"]', 'password123')
  await page.click('[data-testid="login-button"]')
  await expect(page).toHaveURL('/market')
})
```

## 3. 后端测试规范

### 3.1 单元测试
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl userService;

    @Test
    void shouldCreateUserSuccessfully() {
        UserCreateRequest request = UserCreateRequest.builder()
            .username("testuser").email("test@example.com").build();
        User savedUser = User.builder().id(1L).username("testuser").build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserInfoResponse result = userService.createUser(request);

        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(EmailAlreadyExistsException.class);
    }
}
```

### 3.2 集成测试
```java
@SpringBootTest
@Testcontainers
class UserControllerIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private UserRepository userRepository;

    @Test
    void shouldCreateUserSuccessfully() {
        UserCreateRequest request = UserCreateRequest.builder()
            .username("testuser").email("test@example.com").build();

        ResponseEntity<ApiResponse<UserInfoResponse>> response =
            restTemplate.postForEntity("/api/users", request, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(userRepository.findByEmail("test@example.com")).isPresent();
    }
}
```

## 4. 测试用例编写规范

### 4.1 测试命名规范
```typescript
// 前端测试命名
describe('ComponentName', () => {
  it('应该在特定条件下执行特定行为', () => {})
})
```

```java
// 后端测试命名
class ServiceNameTest {
    @Test
    void shouldSuccessfullyExecuteOperation() {}
}
```

### 4.2 测试结构规范（AAA模式）
```typescript
it('应该正确计算总价', () => {
  // Arrange - 准备测试数据
  const items = [{ price: 10, quantity: 2 }]

  // Act - 执行被测试的操作
  const total = calculateTotal(items)

  // Assert - 验证结果
  expect(total).toBe(20)
})
```

### 4.3 测试数据管理
```typescript
// 使用工厂函数创建测试数据
const createMockUser = (overrides = {}) => ({
  id: 1,
  username: 'testuser',
  email: 'test@example.com',
  ...overrides
})
```

## 5. 自动化测试流程

### 5.1 CI/CD集成
```yaml
# .github/workflows/test.yml
name: Test
on: [push, pull_request]
jobs:
  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
      - run: npm ci
      - run: npm run test:unit
      - run: npm run test:e2e

  backend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
      - run: ./mvnw test
```

### 5.2 测试报告生成
```bash
npm run test:coverage        # 前端覆盖率报告
./mvnw jacoco:report        # 后端覆盖率报告
```

## 6. 性能测试和安全测试

### 6.1 性能测试
```javascript
// k6性能测试脚本
import http from 'k6/http'
export let options = {
  stages: [{ duration: '2m', target: 100 }],
  thresholds: { http_req_duration: ['p(95)<500'] }
}
export default function() {
  http.post('http://localhost:8080/api/auth/login', {
    email: 'test@example.com', password: 'password123'
  })
}
```

### 6.2 安全测试
```bash
npm audit                    # 前端依赖扫描
./mvnw dependency-check:check # 后端依赖扫描
```
