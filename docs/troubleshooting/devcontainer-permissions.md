# DevContainer 权限问题解决方案

## 问题描述

在启动DevContainer时遇到以下错误：
```
mkdir: cannot create directory '/home/vscode/.vscode-server/bin': Permission denied
```

这个错误通常是由于Docker卷的权限配置不正确导致的。

## 解决方案

### 方案一：使用自动修复脚本（推荐）

1. 在项目根目录运行重建脚本：
```bash
./.devcontainer/scripts/rebuild-container.sh
```

2. 等待脚本执行完成后，在VSCode中重新打开DevContainer

### 方案二：手动修复

1. 停止现有容器：
```bash
docker-compose -f .devcontainer/docker-compose.yml down
```

2. 删除有问题的Docker卷：
```bash
docker volume rm quick-code-vscode-server
docker volume rm quick-code-vscode-config
docker volume rm quick-code-augment-context
```

3. 重新构建容器：
```bash
docker-compose -f .devcontainer/docker-compose.yml build --no-cache app
```

4. 在VSCode中选择 "Rebuild and Reopen in Container"

### 方案三：清理所有Docker资源

如果上述方案都不起作用，可以尝试完全清理Docker环境：

1. 停止所有容器：
```bash
docker stop $(docker ps -aq)
```

2. 删除所有容器和卷：
```bash
docker system prune -a --volumes
```

3. 重启Docker Desktop

4. 重新打开DevContainer

## 预防措施

1. **确保Docker Desktop有足够权限**：在macOS上，确保Docker Desktop在系统偏好设置中有完整的磁盘访问权限。

2. **检查文件共享设置**：在Docker Desktop设置中，确保项目目录被包含在文件共享列表中。

3. **避免手动修改Docker卷**：不要手动修改Docker卷中的文件权限，这可能导致不一致的状态。

## 技术原理

这个问题的根本原因是：
- VSCode Server需要在容器内的 `/home/vscode/.vscode-server` 目录创建文件
- 该目录通过Docker卷挂载，可能存在权限不匹配
- 我们的修复方案确保了容器内的vscode用户对这些目录有正确的读写权限

## 相关文件

- `.devcontainer/Dockerfile` - 容器镜像定义，包含用户和权限设置
- `.devcontainer/docker-compose.yml` - 容器编排配置，包含卷挂载
- `.devcontainer/scripts/rebuild-container.sh` - 自动修复脚本
- `.devcontainer/scripts/fix-permissions.sh` - 权限修复脚本
