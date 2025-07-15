# 异常处理指南

## 概述

速码网项目采用统一的业务异常处理机制，通过具体的业务异常类替代通用异常，提供更清晰的错误信息和更好的用户体验。

## 异常类层次结构

```
BusinessException (基础业务异常)
├── AuthenticationFailedException (认证失败异常)
├── DuplicateResourceException (资源重复异常)
├── InvalidParameterException (参数无效异常)
├── InvalidStateException (状态无效异常)
├── InsufficientResourceException (资源不足异常)
├── ResourceNotFoundException (资源不存在异常)
└── ValidationException (验证异常)
```

## 错误码分类

### 1. 认证错误 (1xxx)
- `1001` - 密码错误
- `1002` - 用户名或邮箱错误
- `1003` - 验证码错误
- `1004` - 令牌无效
- `1005` - 令牌过期

### 2. 授权错误 (2xxx)
- `2001` - 权限不足
- `2002` - 角色权限不足
- `2003` - 资源访问被拒绝

### 3. 验证错误 (3xxx)
- `3001` - 参数验证失败
- `3002` - 数据格式错误
- `3003` - 必填参数缺失
- `3004` - 参数值超出范围
- `3005` - 分页参数无效

### 4. 资源错误 (4xxx)
- `4001` - 用户不存在
- `4002` - 用户名已存在
- `4003` - 邮箱已存在
- `4004` - 角色不存在
- `4005` - 角色代码已存在
- `4006` - 权限不存在
- `4007` - 权限代码已存在
- `4008` - 积分账户不存在

### 5. 状态错误 (5xxx)
- `5001` - 用户已被禁用
- `5002` - 用户已被锁定
- `5003` - 邮箱已验证
- `5004` - 验证令牌已过期
- `5005` - 功能尚未实现

### 6. 业务逻辑错误 (6xxx)
- `6001` - 积分不足
- `6002` - 冻结积分不足
- `6003` - 积分金额无效

## 使用指南

### 1. 抛出业务异常

```java
// ✅ 推荐：使用具体的业务异常
throw DuplicateResourceException.usernameExists(username);
throw ResourceNotFoundException.user(userId);
throw AuthenticationFailedException.passwordIncorrect();

// ❌ 不推荐：使用通用异常
throw new IllegalArgumentException("用户名已存在");
throw new RuntimeException("用户不存在");
```

### 2. 静态工厂方法

每个业务异常类都提供了静态工厂方法，便于创建常见的异常实例：

```java
// 资源重复异常
DuplicateResourceException.usernameExists(username);
DuplicateResourceException.emailExists(email);
DuplicateResourceException.roleCodeExists(roleCode);

// 资源不存在异常
ResourceNotFoundException.user(userId);
ResourceNotFoundException.role(roleId);
ResourceNotFoundException.permission(permissionId);

// 认证失败异常
AuthenticationFailedException.passwordIncorrect();
AuthenticationFailedException.invalidCredentials();
```

### 3. 自定义错误码和消息

```java
// 使用自定义错误码
throw new InvalidStateException(5005, "功能尚未实现");

// 使用预定义错误码
throw new InvalidStateException(ErrorCode.USER_DISABLED, 
    ErrorCode.getDefaultMessage(ErrorCode.USER_DISABLED));
```

## 测试最佳实践

### 1. 测试异常类型

```java
// ✅ 正确：测试具体的业务异常类型
assertThatThrownBy(() -> userService.register(username, email, password))
    .isInstanceOf(DuplicateResourceException.class)
    .hasMessageContaining("用户名已存在");

// ❌ 错误：测试通用异常类型
assertThatThrownBy(() -> userService.register(username, email, password))
    .isInstanceOf(IllegalArgumentException.class);
```

### 2. 测试错误消息

```java
// 精确匹配错误消息
.hasMessage("用户 (ID: " + userId + ") 不存在");

// 包含关键字匹配
.hasMessageContaining("用户名已存在");
```

## 前端错误处理

### 1. HTTP 状态码映射

业务异常会自动映射到相应的 HTTP 状态码：

- `AuthenticationFailedException` → 401 Unauthorized
- `DuplicateResourceException` → 409 Conflict
- `ResourceNotFoundException` → 404 Not Found
- `InvalidParameterException` → 400 Bad Request
- `InvalidStateException` → 400 Bad Request
- `InsufficientResourceException` → 400 Bad Request

### 2. 错误响应格式

```json
{
  "success": false,
  "code": 4002,
  "message": "用户名已存在: testuser",
  "data": null,
  "timestamp": "2025-07-15T16:00:00Z"
}
```

## 监控和日志

### 1. 异常日志记录

所有业务异常都会在 `GlobalExceptionHandler` 中统一记录：

```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<Object>> handleBusinessException(
    BusinessException e, HttpServletRequest request) {
    log.warn("业务异常: code={}, message={}, path={}", 
        e.getCode(), e.getMessage(), request.getRequestURI());
    // ...
}
```

### 2. 异常统计

可以通过日志分析工具统计各类异常的发生频率，用于系统优化和问题排查。

## 迁移指南

### 从通用异常迁移到业务异常

1. **识别异常场景**：分析现有代码中的异常抛出场景
2. **选择合适的业务异常类**：根据错误类型选择对应的业务异常
3. **更新测试用例**：修改测试用例以期望正确的异常类型
4. **验证错误消息**：确保错误消息格式与实际抛出的异常匹配

### 常见迁移模式

```java
// 迁移前
throw new IllegalArgumentException("用户名已存在");

// 迁移后
throw DuplicateResourceException.usernameExists(username);

// 测试迁移前
assertThatThrownBy(() -> service.method())
    .isInstanceOf(IllegalArgumentException.class);

// 测试迁移后
assertThatThrownBy(() -> service.method())
    .isInstanceOf(DuplicateResourceException.class);
```

## 总结

通过使用统一的业务异常处理机制，我们实现了：

1. **更清晰的错误分类**：通过错误码和异常类型明确区分不同类型的错误
2. **更好的用户体验**：提供具体、有意义的错误消息
3. **更容易的问题排查**：通过结构化的异常信息快速定位问题
4. **更高的代码质量**：遵循最佳实践，提高代码的可维护性

在后续开发中，请严格遵循本指南，使用具体的业务异常类替代通用异常，确保项目的异常处理机制保持一致性和高质量。
