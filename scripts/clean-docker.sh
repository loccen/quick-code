#!/bin/bash

# 清理Docker缓存和容器脚本
# 用于解决devcontainer构建问题

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_header() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

# 清理devcontainer相关的Docker资源
clean_devcontainer() {
    log_header "清理DevContainer相关资源"
    
    # 停止并删除相关容器
    log_info "停止并删除quick-code相关容器..."
    docker ps -a --filter "name=quick-code" --format "{{.ID}}" | xargs -r docker rm -f
    
    # 删除相关镜像
    log_info "删除quick-code相关镜像..."
    docker images --filter "reference=*quick-code*" --format "{{.ID}}" | xargs -r docker rmi -f
    
    # 删除devcontainer相关镜像
    docker images --filter "reference=*devcontainer*" --format "{{.ID}}" | xargs -r docker rmi -f
    
    log_success "DevContainer资源清理完成"
}

# 清理Docker构建缓存
clean_build_cache() {
    log_header "清理Docker构建缓存"
    
    log_info "清理构建缓存..."
    docker builder prune -f
    
    log_info "清理未使用的镜像..."
    docker image prune -f
    
    log_success "构建缓存清理完成"
}

# 清理Docker卷
clean_volumes() {
    log_header "清理Docker卷"
    
    log_info "列出quick-code相关卷..."
    docker volume ls --filter "name=quick-code" --format "{{.Name}}"
    
    read -p "是否要删除这些卷？这将删除所有数据 (y/N): " confirm
    if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
        log_info "删除quick-code相关卷..."
        docker volume ls --filter "name=quick-code" --format "{{.Name}}" | xargs -r docker volume rm
        log_success "卷清理完成"
    else
        log_info "跳过卷清理"
    fi
}

# 显示Docker状态
show_docker_status() {
    log_header "Docker状态"
    
    log_info "Docker版本:"
    docker --version
    
    log_info "Docker Compose版本:"
    docker compose version
    
    log_info "运行中的容器:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    log_info "Docker磁盘使用:"
    docker system df
}

# 主函数
main() {
    echo "🧹 Docker清理工具 - 解决DevContainer构建问题"
    echo ""
    
    show_docker_status
    echo ""
    
    clean_devcontainer
    echo ""
    
    clean_build_cache
    echo ""
    
    clean_volumes
    echo ""
    
    log_success "✅ Docker清理完成！"
    echo ""
    echo "🚀 现在可以重新尝试启动DevContainer:"
    echo "  1. 在VSCode中按 Ctrl+Shift+P"
    echo "  2. 选择 'Dev Containers: Rebuild Container'"
    echo "  3. 等待重新构建完成"
}

# 执行主函数
main "$@"
