# 速码网开发环境故障排除指南

## 🚨 常见问题及解决方案

### 1. 容器相关问题

#### 1.1 Dev Container 启动失败

**问题描述**: VSCode 提示无法启动 Dev Container

**可能原因**:
- Docker Desktop 未运行
- Docker 权限不足
- 端口被占用
- 磁盘空间不足

**解决方案**:

```bash
# 1. 检查 Docker 状态
docker --version
docker ps

# 2. 重启 Docker Desktop
# Windows/Mac: 重启 Docker Desktop 应用

# 3. 检查端口占用
netstat -tulpn | grep :3000
netstat -tulpn | grep :8080
netstat -tulpn | grep :3306

# 4. 清理 Docker 资源
docker system prune -a
docker volume prune

# 5. 重新构建容器
# 在 VSCode 中: Ctrl+Shift+P -> "Dev Containers: Rebuild Container"
```

#### 1.2 容器构建缓慢

**问题描述**: 容器构建时间过长

**解决方案**:

```bash
# 1. 使用国内镜像源
# 在 Dockerfile 中添加：
RUN npm config set registry https://registry.npmmirror.com

# 2. 启用 Docker BuildKit
export DOCKER_BUILDKIT=1

# 3. 清理构建缓存
docker builder prune
```

#### 1.3 容器内存不足

**问题描述**: 容器运行时内存不足

**解决方案**:

```bash
# 1. 增加 Docker Desktop 内存限制
# Settings -> Resources -> Memory -> 调整到 8GB 或更高

# 2. 优化 Java 堆内存
export JAVA_OPTS="-Xmx1g -Xms512m"

# 3. 清理不必要的进程
ps aux | grep java
kill <process-id>
```

### 2. 数据库连接问题

#### 2.1 MySQL 连接失败

**问题描述**: 应用无法连接到 MySQL 数据库

**错误信息**:
```
Communications link failure
Access denied for user
Unknown database
```

**解决方案**:

```bash
# 1. 检查 MySQL 容器状态
docker ps | grep mysql
docker logs quick-code-mysql-1

# 2. 验证数据库连接
mysql -h localhost -P 3306 -u quick_code_user -pquick_code_pass

# 3. 重置数据库密码
docker exec -it quick-code-mysql-1 mysql -u root -p
ALTER USER 'quick_code_user'@'%' IDENTIFIED BY 'quick_code_pass';
FLUSH PRIVILEGES;

# 4. 重新初始化数据库
docker exec -it quick-code-mysql-1 mysql -u root -p quick_code < .devcontainer/scripts/mysql-init/01-init-database.sql
```

#### 2.2 Redis 连接失败

**问题描述**: 应用无法连接到 Redis

**解决方案**:

```bash
# 1. 检查 Redis 容器状态
docker ps | grep redis
docker logs quick-code-redis-1

# 2. 测试 Redis 连接
redis-cli -h localhost -p 6379 -a redis_pass ping

# 3. 重启 Redis 容器
docker restart quick-code-redis-1

# 4. 清理 Redis 数据
redis-cli -h localhost -p 6379 -a redis_pass FLUSHALL
```

### 3. 前端开发问题

#### 3.1 Vite 热重载不工作

**问题描述**: 修改前端代码后页面不自动刷新

**解决方案**:

```bash
# 1. 检查 Vite 配置
# 在 vite.config.ts 中确保：
export default defineConfig({
  server: {
    host: '0.0.0.0',
    port: 3000,
    watch: {
      usePolling: true
    }
  }
})

# 2. 重启前端服务
cd user-frontend
npm run dev

# 3. 清理 node_modules 缓存
rm -rf node_modules/.cache
npm install
```

#### 3.2 npm 安装依赖失败

**问题描述**: npm install 报错或安装缓慢

**解决方案**:

```bash
# 1. 清理 npm 缓存
npm cache clean --force

# 2. 删除 node_modules 重新安装
rm -rf node_modules package-lock.json
npm install

# 3. 使用国内镜像源
npm config set registry https://registry.npmmirror.com

# 4. 使用 yarn 替代 npm
npm install -g yarn
yarn install
```

#### 3.3 TypeScript 编译错误

**问题描述**: TypeScript 类型检查失败

**解决方案**:

```bash
# 1. 更新 TypeScript 版本
npm install -D typescript@latest

# 2. 重新生成类型声明
npm run build

# 3. 检查 tsconfig.json 配置
{
  "compilerOptions": {
    "strict": true,
    "skipLibCheck": true
  }
}

# 4. 重启 TypeScript 服务
# 在 VSCode 中: Ctrl+Shift+P -> "TypeScript: Restart TS Server"
```

### 4. 后端开发问题

#### 4.1 Spring Boot 启动失败

**问题描述**: Spring Boot 应用无法启动

**常见错误**:
```
Port 8080 was already in use
Failed to configure a DataSource
Bean creation exception
```

**解决方案**:

```bash
# 1. 检查端口占用
lsof -i :8080
kill -9 <PID>

# 2. 检查数据库配置
# 在 application.yml 中确保：
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/quick_code
    username: quick_code_user
    password: quick_code_pass

# 3. 清理 Maven 缓存
mvn clean install

# 4. 重新下载依赖
rm -rf ~/.m2/repository
mvn dependency:resolve
```

#### 4.2 Maven 依赖下载失败

**问题描述**: Maven 无法下载依赖包

**解决方案**:

```bash
# 1. 使用阿里云镜像
# 在 ~/.m2/settings.xml 中配置：
<mirrors>
  <mirror>
    <id>aliyunmaven</id>
    <mirrorOf>*</mirrorOf>
    <name>阿里云公共仓库</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>

# 2. 清理本地仓库
mvn dependency:purge-local-repository

# 3. 强制更新依赖
mvn clean install -U
```

#### 4.3 JPA/Hibernate 问题

**问题描述**: 数据库实体映射错误

**解决方案**:

```bash
# 1. 启用 SQL 日志
# 在 application.yml 中：
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

# 2. 检查实体类注解
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}

# 3. 验证数据库表结构
DESCRIBE users;
```

### 5. 网络和端口问题

#### 5.1 端口冲突

**问题描述**: 端口已被其他应用占用

**解决方案**:

```bash
# 1. 查找占用端口的进程
# Windows:
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# macOS/Linux:
lsof -i :3000
kill -9 <PID>

# 2. 修改端口配置
# 在 docker-compose.yml 中修改端口映射：
ports:
  - "3001:3000"  # 将主机端口改为 3001
```

#### 5.2 跨域问题

**问题描述**: 前端请求后端 API 时出现 CORS 错误

**解决方案**:

```java
// 在 Spring Boot 中配置 CORS
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 6. 性能问题

#### 6.1 应用响应缓慢

**问题描述**: 应用加载或响应时间过长

**解决方案**:

```bash
# 1. 检查系统资源使用
docker stats
htop

# 2. 优化数据库查询
# 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

# 3. 使用 Redis 缓存
@Cacheable("users")
public User findById(Long id) {
    return userRepository.findById(id);
}

# 4. 前端代码分割
// 在 Vue 中使用懒加载
const UserProfile = () => import('./components/UserProfile.vue');
```

#### 6.2 内存泄漏

**问题描述**: 应用内存使用持续增长

**解决方案**:

```bash
# 1. 监控内存使用
docker stats --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}"

# 2. 分析 Java 堆内存
jmap -histo <java-pid>
jstack <java-pid>

# 3. 前端内存分析
# 在浏览器开发者工具中使用 Memory 面板

# 4. 优化代码
// 及时清理事件监听器
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});
```

### 7. 文件和权限问题

#### 7.1 文件权限错误

**问题描述**: 容器内无法读写文件

**解决方案**:

```bash
# 1. 检查文件权限
ls -la

# 2. 修改文件权限
chmod 755 scripts/dev-tools.sh
chown vscode:vscode /workspace

# 3. 在 Dockerfile 中设置正确的用户
USER vscode
WORKDIR /workspace
```

#### 7.2 文件同步问题

**问题描述**: 主机和容器间文件同步异常

**解决方案**:

```bash
# 1. 检查挂载配置
docker inspect <container-id> | grep Mounts

# 2. 重新挂载卷
docker-compose down
docker-compose up -d

# 3. 使用 rsync 手动同步
rsync -av /host/path/ /container/path/
```

## 🔧 调试工具和技巧

### 1. 日志查看

```bash
# 查看所有容器日志
docker-compose logs

# 查看特定服务日志
docker-compose logs mysql
docker-compose logs redis

# 实时跟踪日志
docker-compose logs -f app
```

### 2. 容器内调试

```bash
# 进入容器
docker exec -it quick-code-app-1 bash

# 检查网络连接
ping mysql
ping redis
telnet mysql 3306

# 查看进程状态
ps aux | grep java
ps aux | grep node
```

### 3. 数据库调试

```bash
# 连接 MySQL
mysql -h localhost -P 3306 -u quick_code_user -p

# 查看数据库状态
SHOW PROCESSLIST;
SHOW ENGINE INNODB STATUS;

# 连接 Redis
redis-cli -h localhost -p 6379 -a redis_pass
INFO memory
MONITOR
```

## 📞 获取帮助

如果以上解决方案都无法解决问题，可以：

1. **查看详细日志**:
   ```bash
   ./scripts/dev-tools.sh logs
   ```

2. **重置开发环境**:
   ```bash
   ./scripts/dev-tools.sh clean
   docker-compose down -v
   docker-compose up -d
   ```

3. **提交 Issue**:
   - 描述问题现象
   - 提供错误日志
   - 说明操作步骤
   - 附上环境信息

4. **联系开发团队**:
   - 邮箱: dev@quickcode.com
   - 微信群: [扫码加入]

## 📋 问题报告模板

```markdown
### 问题描述
[简要描述遇到的问题]

### 环境信息
- 操作系统: [Windows/macOS/Linux]
- Docker 版本: [docker --version]
- VSCode 版本: [Help -> About]

### 重现步骤
1. [第一步]
2. [第二步]
3. [第三步]

### 期望结果
[描述期望的正常行为]

### 实际结果
[描述实际发生的情况]

### 错误日志
```
[粘贴相关的错误日志]
```

### 已尝试的解决方案
[列出已经尝试过的解决方法]
```

记住：大多数问题都有解决方案，保持耐心并系统性地排查问题！
