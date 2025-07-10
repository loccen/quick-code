# 日志配置说明

## 概述

本项目采用了支持IDE快速跳转的日志配置方案，通过自定义的日志格式，可以在IDE中直接点击日志中的文件路径和行号快速跳转到对应的代码位置。

## 配置文件

### 1. logback-spring.xml
主要的日志配置文件，定义了：
- 开发环境和生产环境的不同日志格式
- 控制台输出格式（支持IDE跳转）
- 文件输出格式
- 错误日志单独文件
- SQL日志配置

### 2. RelativePathConverter.java
自定义的Logback转换器，用于：
- 将完整类名转换为相对于项目根目录的文件路径
- 支持IDE编辑器的快速跳转功能

## 日志格式说明

### 开发环境控制台输出格式
```
2024-01-15 10:30:45.123  INFO 12345 --- [main] backend/src/main/java/com/quickcode/controller/LogTestController.java:25 c.q.controller.LogTestController : 这是INFO级别的日志
```

格式组成：
- `2024-01-15 10:30:45.123`: 时间戳
- `INFO`: 日志级别
- `12345`: 进程ID
- `[main]`: 线程名
- `backend/src/main/java/com/quickcode/controller/LogTestController.java:25`: **文件相对路径:行号** (支持IDE跳转)
- `c.q.controller.LogTestController`: 简化的类名
- `这是INFO级别的日志`: 日志消息

### 文件输出格式
```
2024-01-15 10:30:45.123 [main] INFO  backend/src/main/java/com/quickcode/controller/LogTestController.java:25 com.quickcode.controller.LogTestController - 这是INFO级别的日志
```

## IDE跳转功能

### 支持的IDE
- IntelliJ IDEA
- Visual Studio Code
- Eclipse
- 其他支持文件路径跳转的IDE

### 使用方法
1. 在IDE的控制台或日志查看器中查看日志输出
2. 点击日志中的文件路径部分（如：`backend/src/main/java/com/quickcode/controller/LogTestController.java:25`）
3. IDE会自动跳转到对应的文件和行号

## 日志级别配置

### 开发环境 (dev profile)
- 应用日志: DEBUG级别
- Spring Security: DEBUG级别
- Spring Web: DEBUG级别
- Hibernate SQL: DEBUG级别
- Redis: DEBUG级别

### 生产环境 (prod profile)
- 应用日志: INFO级别
- 其他框架日志: WARN级别

## 日志文件

### 主日志文件
- 位置: `logs/quick-code.log`
- 滚动策略: 按日期和大小滚动
- 最大文件大小: 100MB
- 保留天数: 30天
- 总大小限制: 3GB

### 错误日志文件
- 位置: `logs/quick-code-error.log`
- 只记录ERROR级别的日志
- 滚动策略: 按日期和大小滚动
- 最大文件大小: 100MB
- 保留天数: 30天
- 总大小限制: 1GB

## 测试日志配置

### 测试接口
访问以下接口来测试日志输出：
- `GET /api/test/logs` - 测试不同级别的日志
- `GET /api/test/business-logs` - 测试业务日志

### 验证步骤
1. 启动应用
2. 访问测试接口
3. 查看控制台输出，确认格式正确
4. 在IDE中点击日志中的文件路径，验证跳转功能
5. 检查日志文件是否正确生成

## 自定义配置

### 修改日志格式
如需修改日志格式，请编辑 `logback-spring.xml` 中的 `<pattern>` 配置。

### 修改路径转换逻辑
如需修改路径转换逻辑，请编辑 `RelativePathConverter.java` 文件。

### 添加新的日志级别
在 `logback-spring.xml` 中添加新的 `<logger>` 配置。

## 注意事项

1. **路径格式**: 使用正斜杠 `/` 作为路径分隔符，确保跨平台兼容性
2. **性能影响**: 自定义转换器会有轻微的性能开销，但在开发环境中可以接受
3. **生产环境**: 生产环境使用简化的日志格式，减少性能开销
4. **文件编码**: 所有日志文件使用UTF-8编码

## 故障排除

### 路径跳转不工作
1. 检查IDE是否支持文件路径跳转
2. 确认项目根目录设置正确
3. 检查文件路径格式是否正确

### 日志文件未生成
1. 检查日志目录权限
2. 确认磁盘空间充足
3. 查看应用启动日志是否有错误

### 自定义转换器异常
1. 检查 `RelativePathConverter` 类是否正确编译
2. 确认类路径配置正确
3. 查看应用启动时的错误日志
