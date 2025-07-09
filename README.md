# 速码网 - VSCode Dev Containers 开发环境

## 📋 项目概述

速码网是一个专业的源码交易平台，采用Vue3前端 + Spring Boot 3后端的双前端架构。本项目使用VSCode Dev Containers提供完整的容器化开发环境，支持一键启动和热重载开发。

## 🏗️ 技术架构

### 前端技术栈
- **框架**: Vue 3 + Composition API
- **语言**: TypeScript
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **UI组件**: Element Plus
- **样式**: SCSS + CSS Modules

### 后端技术栈
- **框架**: Spring Boot 3
- **语言**: Java 17
- **安全**: Spring Security 6
- **数据访问**: Spring Data JPA + MyBatis Plus
- **缓存**: Redis 7.0
- **API文档**: SpringDoc OpenAPI 3

### 数据存储
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.0
- **对象存储**: MinIO
- **消息队列**: RabbitMQ (可选)

## 🚀 快速开始

### 前置要求

1. **安装 Docker Desktop**
   - [Windows/Mac 下载地址](https://www.docker.com/products/docker-desktop)
   - 确保 Docker 正常运行

2. **安装 VSCode**
   - [VSCode 下载地址](https://code.visualstudio.com/)

3. **安装 Dev Containers 扩展**
   - 在 VSCode 中搜索并安装 "Dev Containers" 扩展

### 环境搭建步骤

#### 1. 克隆项目
```bash
git clone <repository-url>
cd quick-code
```

#### 2. 启动 Dev Container
1. 在 VSCode 中打开项目文件夹
2. 按 `Ctrl+Shift+P` (Windows/Linux) 或 `Cmd+Shift+P` (Mac)
3. 输入 "Dev Containers: Reopen in Container"
4. 等待容器构建和启动（首次启动需要几分钟）

#### 3. 验证环境
容器启动后，所有服务应该自动可用：

- **用户端前端**: http://localhost:3000
- **管理后台前端**: http://localhost:3001  
- **后端API**: http://localhost:8080
- **MySQL数据库**: localhost:3306
- **Redis缓存**: localhost:6379
- **MinIO存储**: http://localhost:9000
- **MinIO控制台**: http://localhost:9001

## 🛠️ 开发工具使用

### 开发脚本

项目提供了便利的开发脚本：

```bash
# 启动开发环境
./scripts/dev-tools.sh start

# 停止开发环境  
./scripts/dev-tools.sh stop

# 重启开发环境
./scripts/dev-tools.sh restart

# 查看服务状态
./scripts/dev-tools.sh status

# 查看服务日志
./scripts/dev-tools.sh logs

# 清理缓存
./scripts/dev-tools.sh clean

# 查看帮助
./scripts/dev-tools.sh help
```

### VSCode 集成功能

开发环境已预配置以下VSCode扩展：

#### 前端开发
- **Vue Language Features**: Vue 3 语法支持
- **TypeScript Vue Plugin**: Vue TypeScript 支持
- **ESLint**: 代码检查
- **Prettier**: 代码格式化

#### 后端开发
- **Extension Pack for Java**: Java 开发套件
- **Spring Boot Extension Pack**: Spring Boot 支持
- **Spring Boot Dashboard**: Spring Boot 应用管理

#### 数据库工具
- **MySQL Client**: MySQL 数据库管理
- **Redis Client**: Redis 缓存管理

#### 通用工具
- **GitLens**: Git 增强工具
- **Docker**: Docker 容器管理
- **REST Client**: API 测试工具

## 📁 项目结构

```
quick-code/
├── .devcontainer/              # Dev Container 配置
│   ├── devcontainer.json      # 主配置文件
│   ├── docker-compose.yml     # 服务编排
│   ├── Dockerfile             # 开发环境镜像
│   └── scripts/               # 初始化脚本
├── user-frontend/             # 用户端前端应用
├── admin-frontend/            # 管理后台前端应用
├── backend/                   # Spring Boot 后端
├── shared/                    # 共享组件和工具
├── scripts/                   # 开发脚本
├── docs/                      # 项目文档
├── .env                       # 环境配置
├── .env.example              # 环境配置模板
└── README.md                 # 项目说明
```

## 🔧 配置说明

### 环境变量

主要配置文件：
- `.env` - 开发环境配置
- `.env.example` - 配置模板

关键配置项：
```bash
# 数据库配置
DB_HOST=mysql
DB_NAME=quick_code
DB_USERNAME=quick_code_user
DB_PASSWORD=quick_code_pass

# Redis配置
REDIS_HOST=redis
REDIS_PASSWORD=redis_pass

# MinIO配置
MINIO_ENDPOINT=minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123

# 应用配置
APP_BASE_URL=http://localhost:8080
JWT_SECRET=dev-jwt-secret-key
```

### 端口映射

| 服务 | 容器端口 | 主机端口 | 说明 |
|------|----------|----------|------|
| 用户端前端 | 3000 | 3000 | Vue3 用户界面 |
| 管理后台 | 3001 | 3001 | Vue3 管理界面 |
| 后端API | 8080 | 8080 | Spring Boot API |
| MySQL | 3306 | 3306 | 数据库服务 |
| Redis | 6379 | 6379 | 缓存服务 |
| MinIO | 9000 | 9000 | 对象存储API |
| MinIO控制台 | 9001 | 9001 | 存储管理界面 |

## 🧪 开发流程

### 1. 前端开发

#### 用户端前端
```bash
cd user-frontend
npm run dev          # 启动开发服务器
npm run build        # 构建生产版本
npm run test         # 运行测试
npm run lint         # 代码检查
```

#### 管理后台前端
```bash
cd admin-frontend
npm run dev          # 启动开发服务器 (端口3001)
npm run build        # 构建生产版本
npm run test         # 运行测试
npm run lint         # 代码检查
```

### 2. 后端开发

```bash
cd backend
mvn spring-boot:run  # 启动开发服务器
mvn clean package    # 构建项目
mvn test            # 运行测试
```

### 3. 数据库操作

#### 连接数据库
```bash
# 使用MySQL客户端连接
mysql -h localhost -P 3306 -u quick_code_user -p

# 使用Redis客户端连接
redis-cli -h localhost -p 6379 -a redis_pass
```

#### 重置数据库
```bash
# 重置数据库到初始状态
./scripts/dev-tools.sh db-reset
```

## 🔍 调试指南

### 前端调试
1. 在 VSCode 中打开前端文件
2. 设置断点
3. 按 F5 启动调试
4. 在浏览器中访问应用

### 后端调试
1. 在 VSCode 中打开 Java 文件
2. 设置断点
3. 使用 Spring Boot Dashboard 启动应用
4. 调试模式会自动激活

### 数据库调试
1. 使用 MySQL Client 扩展连接数据库
2. 执行 SQL 查询
3. 查看表结构和数据

## 📊 性能优化

### 开发环境优化
- 使用 Volume 挂载提高文件 I/O 性能
- 缓存 node_modules 和 Maven 依赖
- 启用热重载减少重启时间

### 内存使用优化
- 合理配置 JVM 堆内存
- 优化 Docker 容器资源限制
- 使用 Redis 缓存减少数据库查询

## 🔒 安全注意事项

### 开发环境安全
- 默认密码仅用于开发环境
- 生产环境必须修改所有默认密码
- JWT 密钥必须使用强随机字符串

### 数据安全
- 敏感数据通过环境变量配置
- 不要将 .env 文件提交到版本控制
- 定期备份开发数据库

## 🆘 故障排除

### 常见问题

#### 1. 容器启动失败
```bash
# 检查 Docker 是否运行
docker --version

# 重新构建容器
# 在 VSCode 中: Ctrl+Shift+P -> "Dev Containers: Rebuild Container"
```

#### 2. 端口冲突
```bash
# 检查端口占用
lsof -i :3000
lsof -i :8080

# 修改 docker-compose.yml 中的端口映射
```

#### 3. 数据库连接失败
```bash
# 检查 MySQL 容器状态
docker ps | grep mysql

# 查看 MySQL 日志
docker logs <mysql-container-id>

# 重启 MySQL 容器
docker restart <mysql-container-id>
```

#### 4. 前端热重载不工作
```bash
# 检查 Vite 配置
# 确保 vite.config.ts 中配置了正确的 host
server: {
  host: '0.0.0.0',
  port: 3000
}
```

### 获取帮助

如果遇到问题，可以：
1. 查看服务日志：`./scripts/dev-tools.sh logs`
2. 检查服务状态：`./scripts/dev-tools.sh status`
3. 重启开发环境：`./scripts/dev-tools.sh restart`
4. 清理缓存：`./scripts/dev-tools.sh clean`

## 📚 相关文档

- [Vue 3 官方文档](https://vuejs.org/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [VSCode Dev Containers 文档](https://code.visualstudio.com/docs/devcontainers/containers)
- [Docker 官方文档](https://docs.docker.com/)

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
