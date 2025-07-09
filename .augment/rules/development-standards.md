---
type: "agent_requested"
description: "## 适用场景 本规范适用于速码网项目的所有代码开发工作，包括前端代码开发（用户端和管理后台Vue3应用）、后端代码开发（Spring Boot 3微服务）、代码重构和优化、代码审查和质量检查、Git提交和分支管理。AI在进行任何代码编写、修改或审查时，必须严格遵循本规范。"
---
# 速码网开发规范指南



## 1. 前端代码规范（Vue3 + TypeScript）

### 1.1 项目结构规范
```
user-frontend/src/          # 用户端前端
├── components/             # 组件库
├── views/                  # 页面组件
├── stores/                 # Pinia状态管理
├── services/               # API服务
├── utils/                  # 工具函数
├── types/                  # TypeScript类型定义
└── shared/                 # 共享资源

admin-frontend/src/         # 管理后台前端
├── components/             # 管理组件
├── views/                  # 管理页面
└── shared/                 # 与用户端共享的资源
```

### 1.2 命名约定
```typescript
// 文件命名
UserProfile.vue          // 组件：PascalCase
userApi.ts              // 工具：camelCase

// 变量和函数
const userName = 'john'  // 变量：camelCase
const getUserInfo = () => {}  // 函数：camelCase，动词开头
const API_BASE_URL = 'https://api.quickcode.com'  // 常量：SCREAMING_SNAKE_CASE

// 类型定义
interface UserInfo {     // 接口：PascalCase
  id: number
  username: string
}
type ProjectStatus = 'draft' | 'published' | 'archived'
```

### 1.3 Vue3组件规范
```vue
<template>
  <div class="project-card">
    <h3 class="project-card__title">{{ project.title }}</h3>
    <p class="project-card__description">{{ project.description }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Project } from '@/types/project'

// Props定义
interface Props {
  project: Project
  showActions?: boolean
}
const props = withDefaults(defineProps<Props>(), { showActions: true })

// Emits定义
const emit = defineEmits<{
  (e: 'click', project: Project): void
}>()

// 响应式数据
const isLoading = ref(false)

// 计算属性
const formattedPrice = computed(() => `¥${props.project.price.toFixed(2)}`)

// 方法
const handleClick = () => emit('click', props.project)
</script>

<style scoped lang="scss">
.project-card {
  padding: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;

  &__title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 8px;
  }
}
</style>
```

### 1.4 TypeScript类型定义规范
```typescript
// types/user.ts
export interface UserInfo {
  id: number
  username: string
  email: string
  avatarUrl?: string
  points: number
  status: UserStatus
  createdAt: string
}

export enum UserStatus {
  ACTIVE = 1,
  FROZEN = 2,
  BANNED = 3
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}
```

## 2. 后端代码规范（Spring Boot 3 + Java 17）

### 2.1 项目结构规范
```
backend/src/main/java/com/quickcode/
├── controller/         # 控制器层
├── service/           # 业务逻辑层
├── repository/        # 数据访问层
├── entity/            # 实体类
├── dto/               # 数据传输对象
├── config/            # 配置类
├── common/            # 通用工具
└── QuickCodeApplication.java
```

### 2.2 命名约定
```java
// 类命名
@RestController
public class UserController {}        // 控制器：Controller后缀

@Service
public class UserServiceImpl implements UserService {}  // 服务实现：ServiceImpl后缀

@Entity
public class User {}                  // 实体类：无后缀，PascalCase

public class UserCreateRequest {}     // DTO：明确的后缀
public class UserInfoResponse {}

// 方法命名
public UserInfoResponse getUserById(Long id) {}              // 查询：get/find开头
public UserInfoResponse createUser(UserCreateRequest request) {}  // 创建：create开头
public UserInfoResponse updateUser(Long id, UserUpdateRequest request) {}  // 更新：update开头
public void deleteUser(Long id) {}                           // 删除：delete开头
public boolean validateUserPermission(Long userId, String permission) {}  // 验证：validate开头
```

### 2.3 代码注释规范
```java
/**
 * 用户管理控制器
 * @author AI Assistant
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    /**
     * 获取用户信息
     * @param id 用户ID
     * @return 用户信息响应
     */
    @GetMapping("/{id}")
    public ApiResponse<UserInfoResponse> getUserById(@PathVariable Long id) {
        UserInfoResponse userInfo = userService.getUserById(id);
        return ApiResponse.success(userInfo);
    }
}
```

### 2.4 异常处理规范
```java
// 自定义异常
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long userId) {
        super(ErrorCode.USER_NOT_FOUND, "用户不存在，ID: " + userId);
    }
}

// 全局异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ApiResponse<Void> handleUserNotFoundException(UserNotFoundException e) {
        return ApiResponse.error(e.getErrorCode(), e.getMessage());
    }
}
```

## 3. Git提交规范和分支策略

### 3.1 提交消息规范
```bash
# 格式：<type>(<scope>): <subject>
feat(user): 添加用户注册功能
fix(project): 修复项目上传失败问题
docs(api): 更新API文档
refactor(service): 重构用户服务层
test(user): 添加用户服务单元测试
```

### 3.2 分支策略
```bash
main                    # 生产环境分支
develop                 # 开发环境分支
feature/user-auth       # 功能分支
hotfix/login-bug        # 修复分支
release/v1.0.0          # 发布分支
```

### 3.3 提交前检查清单
```bash
□ 代码可以正常编译
□ 所有测试用例通过
□ 代码符合规范要求
□ 移除了调试代码
□ 功能按需求实现
□ 处理了异常情况
```

## 4. 代码审查检查清单

### 4.1 前端代码审查
```bash
□ 组件结构清晰，职责单一
□ TypeScript类型定义完整
□ 使用Composition API
□ 响应式数据正确使用
□ 样式使用scoped
□ 错误处理完善
```

### 4.2 后端代码审查
```bash
□ 遵循RESTful API设计
□ 异常处理完善
□ 日志记录合理
□ 数据验证充分
□ 安全性考虑周全
□ 单元测试覆盖
```

### 4.3 通用代码审查
```bash
□ 命名清晰有意义
□ 函数职责单一
□ 代码复用合理
□ 无重复代码
□ 输入验证充分
□ 权限控制到位
```
