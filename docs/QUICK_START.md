# 🚀 速码网开发环境快速上手指南

## 📋 5分钟快速启动

### 第一步：环境准备 (2分钟)

1. **确保 Docker Desktop 正在运行**
   ```bash
   docker --version
   # 应该显示 Docker 版本信息
   ```

2. **确保 VSCode 已安装 Dev Containers 扩展**
   - 打开 VSCode
   - 按 `Ctrl+Shift+X` 打开扩展面板
   - 搜索 "Dev Containers" 并安装

### 第二步：启动开发环境 (3分钟)

1. **在 VSCode 中打开项目**
   ```bash
   code /path/to/quick-code
   ```

2. **启动 Dev Container**
   - 按 `Ctrl+Shift+P` (Windows/Linux) 或 `Cmd+Shift+P` (Mac)
   - 输入 "Dev Containers: Reopen in Container"
   - 等待容器构建完成（首次需要几分钟）

3. **验证环境**
   - 容器启动后，打开终端
   - 运行验证命令：
   ```bash
   ./scripts/dev-tools.sh status
   ```

### 第三步：访问应用

环境启动成功后，可以访问：

- **用户端前端**: http://localhost:3000
- **管理后台**: http://localhost:3001
- **后端API**: http://localhost:8080
- **MinIO控制台**: http://localhost:9001 (用户名/密码: minioadmin/minioadmin123)

## 🎯 核心功能演示

### 1. 前端开发演示

#### 启动用户端前端
```bash
cd user-frontend
npm run dev
```

#### 启动管理后台
```bash
cd admin-frontend
npm run dev -- --port 3001
```

#### 热重载测试
1. 修改 `user-frontend/src/App.vue` 文件
2. 保存文件
3. 浏览器应该自动刷新显示更改

### 2. 后端开发演示

#### 启动 Spring Boot 应用
```bash
cd backend
mvn spring-boot:run
```

#### 测试 API 接口
```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 测试用户API（如果已实现）
curl http://localhost:8080/api/users
```

#### 自动重启测试
1. 修改任意 Java 文件
2. 保存文件
3. Spring Boot DevTools 会自动重启应用

### 3. 数据库操作演示

#### 连接 MySQL
```bash
mysql -h localhost -P 3306 -u quick_code_user -pquick_code_pass
```

#### 查看测试数据
```sql
USE quick_code;
SHOW TABLES;
SELECT * FROM users LIMIT 5;
SELECT * FROM projects LIMIT 5;
```

#### 连接 Redis
```bash
redis-cli -h localhost -p 6379 -a redis_pass
```

#### Redis 基本操作
```redis
PING
SET test_key "Hello World"
GET test_key
KEYS *
```

## 🛠️ 开发工作流

### 日常开发流程

1. **启动开发环境**
   ```bash
   ./scripts/dev-tools.sh start
   ```

2. **开发代码**
   - 前端：修改 Vue 组件，实时预览
   - 后端：修改 Java 代码，自动重启
   - 数据库：使用 VSCode 扩展管理

3. **测试功能**
   ```bash
   # 前端测试
   cd user-frontend && npm run test
   
   # 后端测试
   cd backend && mvn test
   ```

4. **提交代码**
   ```bash
   git add .
   git commit -m "feat: 添加新功能"
   git push
   ```

### 常用开发命令

```bash
# 查看服务状态
./scripts/dev-tools.sh status

# 查看日志
./scripts/dev-tools.sh logs

# 重启服务
./scripts/dev-tools.sh restart

# 清理缓存
./scripts/dev-tools.sh clean
```

## 📁 项目结构快速导览

```
quick-code/
├── 📁 user-frontend/          # 用户端前端 (Vue3)
│   ├── src/components/        # Vue 组件
│   ├── src/views/            # 页面组件
│   ├── src/stores/           # Pinia 状态管理
│   └── src/api/              # API 接口
├── 📁 admin-frontend/         # 管理后台 (Vue3)
│   ├── src/components/        # 管理组件
│   ├── src/views/            # 管理页面
│   └── src/utils/            # 工具函数
├── 📁 backend/               # 后端服务 (Spring Boot)
│   ├── src/main/java/        # Java 源码
│   ├── src/main/resources/   # 配置文件
│   └── src/test/             # 测试代码
├── 📁 shared/                # 共享代码
├── 📁 scripts/               # 开发脚本
└── 📁 docs/                  # 项目文档
```

## 🎨 开发技巧

### VSCode 快捷键

| 功能 | Windows/Linux | macOS |
|------|---------------|-------|
| 命令面板 | `Ctrl+Shift+P` | `Cmd+Shift+P` |
| 快速打开文件 | `Ctrl+P` | `Cmd+P` |
| 全局搜索 | `Ctrl+Shift+F` | `Cmd+Shift+F` |
| 终端 | `Ctrl+`` | `Cmd+`` |
| 侧边栏 | `Ctrl+B` | `Cmd+B` |

### 代码片段

#### Vue 3 组件模板
```vue
<template>
  <div class="component-name">
    {{ message }}
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const message = ref('Hello World')
</script>

<style scoped>
.component-name {
  /* 样式 */
}
</style>
```

#### Spring Boot Controller
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        // 实现逻辑
        return ResponseEntity.ok(users);
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // 实现逻辑
        return ResponseEntity.ok(savedUser);
    }
}
```

### 调试技巧

#### 前端调试
1. 在 Chrome 中按 F12 打开开发者工具
2. 在 Sources 面板设置断点
3. 使用 Vue DevTools 查看组件状态

#### 后端调试
1. 在 VSCode 中设置断点
2. 使用 Spring Boot Dashboard 启动调试模式
3. 查看变量值和调用栈

## 🔧 自定义配置

### 修改端口

如果默认端口冲突，可以修改 `.devcontainer/docker-compose.yml`：

```yaml
services:
  app:
    ports:
      - "3002:3000"  # 用户端前端改为 3002
      - "3003:3001"  # 管理后台改为 3003
      - "8081:8080"  # 后端API改为 8081
```

### 添加新的依赖

#### 前端依赖
```bash
cd user-frontend
npm install axios
npm install -D @types/axios
```

#### 后端依赖
在 `backend/pom.xml` 中添加：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 环境变量配置

修改 `.env` 文件：
```bash
# 修改数据库配置
DB_NAME=my_custom_db
DB_USERNAME=my_user

# 修改应用配置
APP_NAME=我的应用
JWT_SECRET=my-secret-key
```

## 🚨 常见问题快速解决

### 问题1：容器启动失败
```bash
# 重新构建容器
# VSCode: Ctrl+Shift+P -> "Dev Containers: Rebuild Container"
```

### 问题2：端口被占用
```bash
# 查看端口占用
lsof -i :3000
# 杀死占用进程
kill -9 <PID>
```

### 问题3：数据库连接失败
```bash
# 重启数据库容器
docker restart quick-code-mysql-1
# 检查连接
mysql -h localhost -P 3306 -u quick_code_user -p
```

### 问题4：前端热重载不工作
```bash
# 重启前端服务
cd user-frontend
npm run dev
```

## 📚 下一步学习

1. **阅读详细文档**
   - [完整 README](../README.md)
   - [故障排除指南](./TROUBLESHOOTING.md)

2. **学习项目架构**
   - [系统架构设计](./architecture/system-architecture-design.md)
   - [需求分析报告](./requirements/requirement-analysis-report.md)

3. **参与开发**
   - 查看 Issues 列表
   - 提交 Pull Request
   - 参与代码审查

## 🎉 恭喜！

您已经成功搭建了速码网开发环境！现在可以开始愉快的开发工作了。

如果遇到任何问题，请查看 [故障排除指南](./TROUBLESHOOTING.md) 或联系开发团队。

Happy Coding! 🚀
