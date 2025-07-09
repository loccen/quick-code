# VSCode Dev Container 配置说明

## 📋 配置优化总结

本文档说明了为速码网项目优化的VSCode Dev Container配置，确保与实际项目结构完全匹配。

## 🔧 主要优化内容

### 1. 环境变量统一管理

**问题**: 原配置中docker-compose.yml使用硬编码的环境变量，没有使用根目录的`.env`文件。

**解决方案**: 
- 在docker-compose.yml中为所有服务添加了`env_file: - ../../.env`配置
- 将所有硬编码的环境变量改为使用`${VARIABLE_NAME:-default_value}`格式
- 确保根目录的`.env`文件能够被正确读取和使用

**影响的服务**:
- `app` (主开发容器)
- `mysql` (数据库服务)
- `redis` (缓存服务)
- `minio` (对象存储服务)

### 2. 项目结构适配

**优化内容**:
- 更新了`post-create.sh`脚本，适配已存在的三个子项目
- 移除了自动创建项目的逻辑，改为检查和验证现有项目
- 优化了依赖安装逻辑，避免重复安装

### 3. 镜像源配置移除

**变更**:
- 移除了Dockerfile中的npm阿里云镜像配置
- 移除了Maven阿里云镜像配置
- 使用默认的官方镜像源，提高兼容性

### 4. 配置验证脚本

**新增**:
- 创建了`scripts/validate-config.sh`验证脚本
- 可以在容器启动前验证所有配置是否正确
- 提供详细的错误信息和修复建议

## 📁 配置文件结构

```
.devcontainer/
├── devcontainer.json          # 主配置文件
├── docker-compose.yml         # 服务编排（已优化环境变量）
├── Dockerfile                 # 开发环境镜像（已移除镜像源配置）
└── scripts/
    ├── post-create.sh         # 容器创建后脚本（已优化）
    └── post-start.sh          # 容器启动后脚本
```

## 🔗 环境变量映射

### 根目录 `.env` 文件变量

| 变量名 | 用途 | 默认值 |
|--------|------|--------|
| `DB_HOST` | 数据库主机 | `mysql` |
| `DB_PORT` | 数据库端口 | `3306` |
| `DB_NAME` | 数据库名称 | `quick_code` |
| `DB_USERNAME` | 数据库用户名 | `quick_code_user` |
| `DB_PASSWORD` | 数据库密码 | `quick_code_pass` |
| `REDIS_HOST` | Redis主机 | `redis` |
| `REDIS_PORT` | Redis端口 | `6379` |
| `REDIS_PASSWORD` | Redis密码 | `redis_pass` |
| `MINIO_ACCESS_KEY` | MinIO访问密钥 | `minioadmin` |
| `MINIO_SECRET_KEY` | MinIO秘密密钥 | `minioadmin123` |
| `JWT_SECRET` | JWT密钥 | `dev-jwt-secret-key-change-in-production` |

### Docker Compose 服务映射

所有服务现在都会读取根目录的`.env`文件，并使用以下格式：
```yaml
environment:
  - VARIABLE_NAME=${ENV_VARIABLE:-default_value}
```

这确保了：
1. 如果`.env`文件中定义了变量，使用该值
2. 如果未定义，使用默认值
3. 配置的一致性和可维护性

## 🚀 启动流程

### 1. 验证配置
```bash
./scripts/validate-config.sh
```

### 2. 启动Dev Container
1. 在VSCode中打开项目
2. 按`Ctrl+Shift+P` (Windows/Linux) 或 `Cmd+Shift+P` (Mac)
3. 选择"Dev Containers: Reopen in Container"
4. 等待容器构建和启动

### 3. 启动开发环境
```bash
./scripts/dev-tools.sh start
```

## 🔍 配置验证

使用验证脚本检查配置：

```bash
# 验证所有配置
./scripts/validate-config.sh

# 检查服务状态
./scripts/dev-tools.sh status

# 查看服务日志
./scripts/dev-tools.sh logs
```

## 📊 端口映射

| 服务 | 容器端口 | 主机端口 | 说明 |
|------|----------|----------|------|
| 用户端前端 | 3000 | 3000 | Vue3 用户界面 |
| 管理后台 | 3001 | 3001 | Vue3 管理界面 |
| 后端API | 8080 | 8080 | Spring Boot API |
| MySQL | 3306 | 3306 | 数据库服务 |
| Redis | 6379 | 6379 | 缓存服务 |
| MinIO API | 9000 | 9000 | 对象存储API |
| MinIO控制台 | 9001 | 9001 | 存储管理界面 |

## 🛠️ 开发工具

### 预安装的VSCode扩展
- Vue Language Features (Vue3支持)
- TypeScript Vue Plugin
- Extension Pack for Java
- Spring Boot Extension Pack
- MySQL Client
- Redis Client
- Docker
- GitLens
- REST Client

### 预安装的开发工具
- Node.js 18+
- Java 17
- Maven 3.9+
- Docker CLI
- MySQL Client
- Redis CLI
- MinIO Client (mc)

## 🔧 自定义配置

### 修改环境变量
编辑根目录的`.env`文件，重启容器生效。

### 添加新的依赖
- 前端：在对应项目目录运行`npm install package-name`
- 后端：在`backend/pom.xml`中添加依赖

### 修改端口
1. 更新`.env`文件中的端口配置
2. 更新对应项目的配置文件
3. 重启容器

## ❗ 注意事项

1. **首次启动**: 容器首次构建可能需要几分钟时间
2. **网络连接**: 确保网络连接良好，用于下载依赖
3. **磁盘空间**: 确保有足够的磁盘空间（建议至少5GB）
4. **Docker资源**: 建议分配至少8GB内存给Docker Desktop

## 🆘 故障排除

如果遇到问题，请参考：
1. [故障排除指南](./TROUBLESHOOTING.md)
2. [快速上手指南](./QUICK_START.md)
3. 运行验证脚本：`./scripts/validate-config.sh`

## 📝 更新日志

- **2024-01-09**: 优化环境变量管理，统一使用根目录`.env`文件
- **2024-01-09**: 移除镜像源配置，使用默认官方源
- **2024-01-09**: 优化项目结构适配，支持已存在的项目
- **2024-01-09**: 添加配置验证脚本
