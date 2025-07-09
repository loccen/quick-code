#!/bin/bash

# 速码网开发环境 - 容器启动后执行脚本
# 此脚本在每次容器启动时执行，用于启动必要的服务

set -e

echo "🔄 容器启动后初始化..."

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# 等待数据库服务启动
wait_for_mysql() {
    log_info "等待MySQL服务启动..."

    max_attempts=30
    attempt=1

    while [ $attempt -le $max_attempts ]; do
        if mysql -h mysql -u quick_code_user -pquick_code_pass -e "SELECT 1" >/dev/null 2>&1; then
            log_success "MySQL服务已就绪"
            return 0
        fi

        echo "等待MySQL启动... (尝试 $attempt/$max_attempts)"
        sleep 2
        attempt=$((attempt + 1))
    done

    echo "MySQL服务启动超时"
    return 1
}

# 等待Redis服务启动
wait_for_redis() {
    log_info "等待Redis服务启动..."

    max_attempts=30
    attempt=1

    while [ $attempt -le $max_attempts ]; do
        if redis-cli -h redis -a redis_pass ping >/dev/null 2>&1; then
            log_success "Redis服务已就绪"
            return 0
        fi

        echo "等待Redis启动... (尝试 $attempt/$max_attempts)"
        sleep 2
        attempt=$((attempt + 1))
    done

    echo "Redis服务启动超时"
    return 1
}

# 配置MinIO
configure_minio() {
    log_info "配置MinIO..."

    # 等待MinIO启动
    max_attempts=30
    attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -f http://minio:9000/minio/health/live >/dev/null 2>&1; then
            log_success "MinIO服务已就绪"
            break
        fi

        echo "等待MinIO启动... (尝试 $attempt/$max_attempts)"
        sleep 2
        attempt=$((attempt + 1))
    done

    # 配置MinIO客户端
    mc alias set local http://minio:9000 minioadmin minioadmin123 >/dev/null 2>&1 || true

    # 创建存储桶
    mc mb local/quick-code-files >/dev/null 2>&1 || true
    mc policy set public local/quick-code-files >/dev/null 2>&1 || true

    log_success "MinIO配置完成"
}

# 主执行流程
main() {
    log_info "开始容器启动后初始化..."

    # 等待服务启动
    wait_for_mysql
    wait_for_redis
    configure_minio

    log_success "✅ 容器启动初始化完成！"
    log_info "开发环境已就绪，可以开始开发工作"
}

# 执行主函数
main "$@"