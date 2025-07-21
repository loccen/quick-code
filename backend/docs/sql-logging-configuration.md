# SQL日志配置说明

## 概述

为了提高开发环境中日志的可读性，项目已默认屏蔽了Hibernate和MyBatis Plus的SQL日志输出。本文档说明如何在需要时重新启用这些日志。

## 当前配置状态

### 已屏蔽的SQL日志
- Hibernate SQL语句日志 (`org.hibernate.SQL`)
- Hibernate参数绑定日志 (`org.hibernate.type.descriptor.sql.BasicBinder`)
- MyBatis Plus SQL日志

### 保持正常输出的日志
- 应用业务日志 (`com.quickcode`)
- Spring Security日志
- Spring Web日志
- Redis操作日志
- 错误和警告日志

## 如何重新启用SQL日志

### 方法1：修改logback-spring.xml（推荐用于调试特定问题）

编辑 `backend/src/main/resources/logback-spring.xml`：

```xml
<!-- 将WARN改为DEBUG启用Hibernate SQL日志 -->
<logger name="org.hibernate.SQL" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
</logger>

<!-- 将WARN改为TRACE启用参数绑定日志 -->
<logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE" additivity="false">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
</logger>
```

### 方法2：修改MyBatis Plus配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
mybatis-plus:
  configuration:
    # 启用MyBatis Plus SQL日志
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 方法3：通过环境变量临时启用

在启动应用时设置环境变量：

```bash
# 启用JPA SQL日志
export DEBUG_SQL=true

# 或者通过Spring Boot参数
java -jar app.jar --spring.jpa.show-sql=true
```

### 方法4：仅在特定环境启用

创建专门的配置文件 `application-debug.yml`：

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

然后使用 `--spring.profiles.active=dev,debug` 启动应用。

## 测试环境配置

测试环境的SQL日志也已被屏蔽。如需在测试时查看SQL，可以：

1. 修改 `backend/src/test/resources/application-test.yml`
2. 修改 `backend/src/test/resources/logback-test.xml`

## 注意事项

1. **热更新兼容性**：所有配置修改都与项目的热更新功能兼容，修改后会自动生效
2. **性能影响**：启用SQL日志会轻微影响性能，建议仅在调试时使用
3. **日志文件大小**：启用SQL日志会显著增加日志文件大小
4. **生产环境**：生产环境配置不受影响，SQL日志始终处于关闭状态

## 快速切换脚本

可以创建简单的脚本来快速切换SQL日志状态：

```bash
# 启用SQL日志
sed -i 's/level="WARN"/level="DEBUG"/g' backend/src/main/resources/logback-spring.xml

# 禁用SQL日志
sed -i 's/level="DEBUG"/level="WARN"/g' backend/src/main/resources/logback-spring.xml
```

## 故障排除

如果修改配置后SQL日志仍然显示：
1. 检查是否有多个配置文件冲突
2. 确认当前激活的Spring Profile
3. 重启应用以确保配置生效
4. 检查是否有代码中的硬编码日志配置
