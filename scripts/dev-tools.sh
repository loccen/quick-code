#!/bin/bash

# 速码网开发工具脚本
# 提供各种开发便利功能

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
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
    echo -e "${PURPLE}=== $1 ===${NC}"
}

# 显示帮助信息
show_help() {
    echo -e "${CYAN}速码网开发工具${NC}"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "可用命令:"
    echo "  start          启动开发环境"
    echo "  stop           停止开发环境"
    echo "  restart        重启开发环境"
    echo "  status         查看服务状态"
    echo "  logs           查看服务日志"
    echo "  clean          清理缓存和临时文件"
    echo "  test           运行测试"
    echo "  build          构建项目"
    echo "  db-reset       重置数据库"
    echo "  db-backup      备份数据库"
    echo "  install        安装/更新依赖"
    echo "  lint           代码检查"
    echo "  format         代码格式化"
    echo "  help           显示此帮助信息"
    echo ""
}

# 启动开发环境
start_dev() {
    log_header "启动开发环境"

    # 检查是否在容器内
    if [ ! -f /.dockerenv ]; then
        log_error "请在devcontainer内运行此脚本"
        exit 1
    fi

    # 启动用户端前端
    if [ -f "user-frontend/package.json" ]; then
        log_info "启动用户端前端 (端口: 3000)..."
        cd user-frontend
        npm run dev > ../logs/user-frontend.log 2>&1 &
        echo $! > ../logs/user-frontend.pid
        cd ..
    fi

    # 启动管理后台前端
    if [ -f "admin-frontend/package.json" ]; then
        log_info "启动管理后台前端 (端口: 3001)..."
        cd admin-frontend
        npm run dev -- --port 3001 > ../logs/admin-frontend.log 2>&1 &
        echo $! > ../logs/admin-frontend.pid
        cd ..
    fi

    # 启动后端服务
    if [ -f "backend/pom.xml" ]; then
        log_info "启动后端服务 (端口: 8080)..."
        cd backend
        # 确保JAVA_HOME环境变量正确设置并传递给子进程
        JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64 PATH=$JAVA_HOME/bin:$PATH mvn spring-boot:run > ../logs/backend.log 2>&1 &
        echo $! > ../logs/backend.pid
        cd ..
    fi

    # 等待服务启动
    sleep 5

    log_success "开发环境启动完成！"
    echo ""
    echo "服务地址:"
    echo "  用户端前端:     http://localhost:3000"
    echo "  管理后台前端:   http://localhost:3001"
    echo "  后端API:       http://localhost:8080"
    echo "  MySQL:         localhost:3306"
    echo "  Redis:         localhost:6379"
    echo "  MinIO:         http://localhost:9000"
    echo "  MinIO控制台:   http://localhost:9001"
    echo ""
}

# 停止开发环境
stop_dev() {
    log_header "停止开发环境"

    # 创建logs目录
    mkdir -p logs

    # 停止前端服务
    if [ -f "logs/user-frontend.pid" ]; then
        log_info "停止用户端前端..."
        kill $(cat logs/user-frontend.pid) 2>/dev/null || true
        rm -f logs/user-frontend.pid
    fi

    if [ -f "logs/admin-frontend.pid" ]; then
        log_info "停止管理后台前端..."
        kill $(cat logs/admin-frontend.pid) 2>/dev/null || true
        rm -f logs/admin-frontend.pid
    fi

    # 停止后端服务
    if [ -f "logs/backend.pid" ]; then
        log_info "停止后端服务..."
        kill $(cat logs/backend.pid) 2>/dev/null || true
        rm -f logs/backend.pid
    fi

    # 杀死可能残留的进程
    pkill -f "vite" 2>/dev/null || true
    pkill -f "spring-boot:run" 2>/dev/null || true

    log_success "开发环境已停止"
}

# 重启开发环境
restart_dev() {
    log_header "重启开发环境"
    stop_dev
    sleep 2
    start_dev
}

# 查看服务状态
show_status() {
    log_header "服务状态"

    # 检查前端服务
    if [ -f "logs/user-frontend.pid" ] && kill -0 $(cat logs/user-frontend.pid) 2>/dev/null; then
        echo -e "用户端前端:     ${GREEN}运行中${NC}"
    else
        echo -e "用户端前端:     ${RED}已停止${NC}"
    fi

    if [ -f "logs/admin-frontend.pid" ] && kill -0 $(cat logs/admin-frontend.pid) 2>/dev/null; then
        echo -e "管理后台前端:   ${GREEN}运行中${NC}"
    else
        echo -e "管理后台前端:   ${RED}已停止${NC}"
    fi

    # 检查后端服务
    if [ -f "logs/backend.pid" ] && kill -0 $(cat logs/backend.pid) 2>/dev/null; then
        echo -e "后端服务:       ${GREEN}运行中${NC}"
    else
        echo -e "后端服务:       ${RED}已停止${NC}"
    fi

    # 检查数据库连接
    if mysql -h mysql -u quick_code_user -pquick_code_pass -e "SELECT 1" >/dev/null 2>&1; then
        echo -e "MySQL数据库:    ${GREEN}连接正常${NC}"
    else
        echo -e "MySQL数据库:    ${RED}连接失败${NC}"
    fi

    # 检查Redis连接
    if redis-cli -h redis -a redis_pass ping >/dev/null 2>&1; then
        echo -e "Redis缓存:      ${GREEN}连接正常${NC}"
    else
        echo -e "Redis缓存:      ${RED}连接失败${NC}"
    fi

    # 检查MinIO连接
    if curl -f http://minio:9000/minio/health/live >/dev/null 2>&1; then
        echo -e "MinIO存储:      ${GREEN}连接正常${NC}"
    else
        echo -e "MinIO存储:      ${RED}连接失败${NC}"
    fi
}

# 查看日志
show_logs() {
    log_header "服务日志"

    echo "选择要查看的日志:"
    echo "1) 用户端前端"
    echo "2) 管理后台前端"
    echo "3) 后端服务"
    echo "4) 全部日志"
    read -p "请选择 (1-4): " choice

    case $choice in
        1)
            if [ -f "logs/user-frontend.log" ]; then
                tail -f logs/user-frontend.log
            else
                log_warning "用户端前端日志文件不存在"
            fi
            ;;
        2)
            if [ -f "logs/admin-frontend.log" ]; then
                tail -f logs/admin-frontend.log
            else
                log_warning "管理后台前端日志文件不存在"
            fi
            ;;
        3)
            if [ -f "logs/backend.log" ]; then
                tail -f logs/backend.log
            else
                log_warning "后端服务日志文件不存在"
            fi
            ;;
        4)
            if [ -d "logs" ]; then
                tail -f logs/*.log
            else
                log_warning "日志目录不存在"
            fi
            ;;
        *)
            log_error "无效选择"
            ;;
    esac
}

# 清理缓存和临时文件
clean_cache() {
    log_header "清理缓存和临时文件"

    # 清理前端缓存
    if [ -d "user-frontend/node_modules/.cache" ]; then
        log_info "清理用户端前端缓存..."
        rm -rf user-frontend/node_modules/.cache
    fi

    if [ -d "admin-frontend/node_modules/.cache" ]; then
        log_info "清理管理后台前端缓存..."
        rm -rf admin-frontend/node_modules/.cache
    fi

    # 清理构建文件
    if [ -d "user-frontend/dist" ]; then
        log_info "清理用户端前端构建文件..."
        rm -rf user-frontend/dist
    fi

    if [ -d "admin-frontend/dist" ]; then
        log_info "清理管理后台前端构建文件..."
        rm -rf admin-frontend/dist
    fi

    # 清理后端构建文件
    if [ -d "backend/target" ]; then
        log_info "清理后端构建文件..."
        rm -rf backend/target
    fi

    # 清理日志文件
    if [ -d "logs" ]; then
        log_info "清理日志文件..."
        rm -f logs/*.log
        rm -f logs/*.pid
    fi

    log_success "缓存清理完成"
}

# 主函数
main() {
    # 创建必要的目录
    mkdir -p logs

    case "${1:-help}" in
        start)
            start_dev
            ;;
        stop)
            stop_dev
            ;;
        restart)
            restart_dev
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        clean)
            clean_cache
            ;;
        help|*)
            show_help
            ;;
    esac
}

# 执行主函数
main "$@"
