---
type: "agent_requested"
description: "## 适用场景 本规范适用于速码网项目的所有API接口设计和开发工作，包括： - RESTful API接口设计和实现 - API文档编写和维护 - 请求/响应格式定义 - 错误处理和状态码规范 - API版本控制和兼容性管理 - 接口安全性设计  AI在进行任何API相关的设计、开发或文档编写时，必须严格遵循本规范。"
---
# 速码网API接口设计规范指南

## 1. RESTful API设计原则

### 1.1 资源命名规范

```bash
# 资源命名使用名词复数形式
GET    /api/users              # 获取用户列表
GET    /api/users/{id}         # 获取特定用户
POST   /api/users              # 创建用户
PUT    /api/users/{id}         # 更新用户
DELETE /api/users/{id}         # 删除用户

# 嵌套资源
GET    /api/users/{id}/projects        # 获取用户的项目列表
POST   /api/users/{id}/projects        # 为用户创建项目
GET    /api/projects/{id}/comments     # 获取项目评论
POST   /api/projects/{id}/comments     # 为项目添加评论

# 避免深层嵌套（最多2层）
# ❌ 错误示例
GET /api/users/{id}/projects/{projectId}/comments/{commentId}/replies

# ✅ 正确示例
GET /api/comments/{id}/replies
```

### 1.2 HTTP动词使用规范

## 2. 统一响应格式

### 2.1 成功响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1640995200000
}
```

```java
@Data @Builder
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .code(200).message("操作成功").data(data)
            .timestamp(System.currentTimeMillis()).build();
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
            .code(code).message(message)
            .timestamp(System.currentTimeMillis()).build();
    }
}
```

### 2.2 分页响应格式
```json
{
  "code": 200,
  "data": {
    "content": [{"id": 1, "title": "项目标题"}],
    "page": {"current": 1, "size": 20, "total": 100, "hasNext": true}
  }
}
```

```java
@Data @Builder
public class PageResult<T> {
    private List<T> content;
    private PageInfo page;

    @Data @Builder
    public static class PageInfo {
        private Integer current;
        private Integer size;
        private Long total;
        private Boolean hasNext;
    }
}
```

### 2.3 错误响应格式
```json
{
  "code": 400,
  "message": "请求参数错误",
  "errors": [{"field": "email", "message": "邮箱格式不正确"}]
}
```

## 3. HTTP状态码规范

### 3.1 标准状态码使用
```bash
200 OK              # 请求成功
201 Created         # 资源创建成功
400 Bad Request     # 请求参数错误
401 Unauthorized    # 未授权访问
403 Forbidden       # 权限不足
404 Not Found       # 资源不存在
500 Internal Server Error  # 服务器内部错误
```

### 3.2 业务错误码定义
```java
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(1000, "系统内部错误"),
    USER_NOT_FOUND(2001, "用户不存在"),
    PROJECT_NOT_FOUND(3001, "项目不存在"),
    INSUFFICIENT_POINTS(4001, "积分不足"),
    DEPLOY_FAILED(5001, "部署失败");

    private final Integer code;
    private final String message;
}
```

## 4. 请求/响应数据传输对象

### 4.1 请求DTO设计
```java
@Data @Builder
public class UserRegisterRequest {
    @NotBlank @Size(min = 3, max = 20)
    private String username;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, max = 20)
    private String password;
}

@Data @Builder
public class ProjectQueryRequest {
    @Min(1) private Integer page = 1;
    @Min(1) @Max(100) private Integer size = 20;
    private String search;
    private String category;
    private List<String> techStack;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
```

### 4.2 响应DTO设计
```java
@Data @Builder
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private BigDecimal points;
    private UserStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

@Data @Builder
public class ProjectDetailResponse {
    private Long id;
    private String title;
    private String description;
    private List<String> techStack;
    private BigDecimal price;
    private String demoUrl;
    private ProjectStatus status;
    private UserInfoResponse author;
}
```

## 5. API文档标准（OpenAPI 3.0）

### 5.1 控制器注解
```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理")
public class UserController {

    @Operation(summary = "用户注册")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "注册成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    @PostMapping
    public ApiResponse<UserInfoResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/{id}")
    public ApiResponse<UserInfoResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }
}
```

### 5.2 Schema定义
```java
@Schema(description = "用户信息")
@Data
public class UserInfoResponse {
    @Schema(description = "用户ID", example = "1")
    private Long id;
    @Schema(description = "用户名", example = "john_doe")
    private String username;
    @Schema(description = "邮箱地址", example = "john@example.com")
    private String email;
}
```

## 6. API版本控制

### 6.1 URL版本控制
```java
@RestController
@RequestMapping("/api/v1/users")  // 版本1
public class UserV1Controller {}

@RestController
@RequestMapping("/api/v2/users")  // 版本2
public class UserV2Controller {}
```

### 6.2 向后兼容性
```java
@Data
public class UserInfoResponse {
    private Long id;
    private String username;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String phoneNumber;  // v2新增
}
```

## 7. API安全性设计

### 7.1 认证和授权
```java
@RestController
public class AuthController {
    @PostMapping("/api/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.getEmail(), request.getPassword());
        String token = jwtService.generateToken(user);
        return ApiResponse.success(LoginResponse.builder().token(token).build());
    }
}

@PreAuthorize("hasRole('USER')")
@GetMapping("/api/users/profile")
public ApiResponse<UserInfoResponse> getProfile() {}
```

### 7.2 请求限流
```java
@RateLimiter(name = "api")
@GetMapping("/api/projects")
public ApiResponse<PageResult<ProjectResponse>> getProjects() {}
```

### 7.3 输入验证
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        return ApiResponse.error(400, "请求参数验证失败");
    }
}
```
