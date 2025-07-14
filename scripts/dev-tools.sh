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

# 服务定义
declare -A SERVICES=(
    ["user-frontend"]="用户端前端"
    ["admin-frontend"]="管理后台前端"
    ["backend"]="后端服务"
    ["mysql"]="MySQL数据库"
    ["redis"]="Redis缓存"
    ["minio"]="MinIO存储"
)

declare -A SERVICE_PORTS=(
    ["user-frontend"]="3000"
    ["admin-frontend"]="3001"
    ["backend"]="8080"
    ["mysql"]="3306"
    ["redis"]="6379"
    ["minio"]="9000"
)

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

# 验证服务名称
validate_service() {
    local service="$1"
    if [[ -z "$service" ]]; then
        return 0  # 空服务名表示所有服务
    fi

    if [[ ! -v SERVICES["$service"] ]]; then
        log_error "未知服务: $service"
        echo ""
        list_services
        return 1
    fi
    return 0
}

# 列出所有可用服务
list_services() {
    echo -e "${CYAN}可用服务:${NC}"
    for service in "${!SERVICES[@]}"; do
        echo "  $service - ${SERVICES[$service]} (端口: ${SERVICE_PORTS[$service]})"
    done
}

# 检查服务运行状态
check_service_status() {
    local service="$1"

    case "$service" in
        "user-frontend")
            # 检查PID文件和进程状态
            if [ -f "logs/user-frontend.pid" ] && kill -0 $(cat logs/user-frontend.pid) 2>/dev/null; then
                # 进一步检查端口是否被占用
                if lsof -i:3000 >/dev/null 2>&1; then
                    return 0
                fi
            fi
            # 如果PID文件不存在或进程已死，检查是否有其他进程占用端口
            if lsof -i:3000 >/dev/null 2>&1; then
                return 0
            fi
            ;;
        "admin-frontend")
            # 检查PID文件和进程状态
            if [ -f "logs/admin-frontend.pid" ] && kill -0 $(cat logs/admin-frontend.pid) 2>/dev/null; then
                # 进一步检查端口是否被占用
                if lsof -i:3001 >/dev/null 2>&1; then
                    return 0
                fi
            fi
            # 如果PID文件不存在或进程已死，检查是否有其他进程占用端口
            if lsof -i:3001 >/dev/null 2>&1; then
                return 0
            fi
            ;;
        "backend")
            # 检查PID文件和进程状态
            if [ -f "logs/backend.pid" ] && kill -0 $(cat logs/backend.pid) 2>/dev/null; then
                # 进一步检查端口是否被占用
                if lsof -i:8080 >/dev/null 2>&1; then
                    return 0
                fi
            fi
            # 如果PID文件不存在或进程已死，检查是否有其他进程占用端口
            if lsof -i:8080 >/dev/null 2>&1; then
                return 0
            fi
            ;;
        "mysql")
            if mysql -h mysql -u quick_code_user -pquick_code_pass -e "SELECT 1" >/dev/null 2>&1; then
                return 0
            fi
            ;;
        "redis")
            if redis-cli -h redis -a redis_pass ping >/dev/null 2>&1; then
                return 0
            fi
            ;;
        "minio")
            if curl -f http://minio:9000/minio/health/live >/dev/null 2>&1; then
                return 0
            fi
            ;;
    esac
    return 1
}

# 显示帮助信息
show_help() {
    echo -e "${CYAN}速码网开发工具${NC}"
    echo ""
    echo "用法: $0 [命令] [服务名称]"
    echo ""
    echo "批量操作命令:"
    echo "  start                    启动所有开发服务"
    echo "  stop                     停止所有开发服务"
    echo "  restart                  重启所有开发服务"
    echo "  status                   查看所有服务状态"
    echo ""
    echo "单服务操作命令:"
    echo "  start <服务名>           启动指定服务"
    echo "  stop <服务名>            停止指定服务"
    echo "  restart <服务名>         重启指定服务"
    echo "  status <服务名>          查看指定服务状态"
    echo ""
    echo "其他命令:"
    echo "  list                     列出所有可用服务"
    echo "  logs [服务名]            查看服务日志"
    echo "  clean                    清理缓存和临时文件"
    echo "  test                     运行测试"
    echo "  build                    构建项目"
    echo "  db-reset                 重置数据库"
    echo "  db-backup                备份数据库"
    echo "  install                  安装/更新依赖"
    echo "  lint                     代码检查"
    echo "  format                   代码格式化"
    echo "  help, -h, --help         显示此帮助信息"
    echo ""
    list_services
    echo ""
    echo "示例:"
    echo "  $0 start mysql           # 启动MySQL服务"
    echo "  $0 stop backend          # 停止后端服务"
    echo "  $0 restart user-frontend # 重启用户端前端"
    echo "  $0 status redis          # 查看Redis状态"
    echo ""
}

# 启动单个服务
start_single_service() {
    local service="$1"

    # 检查服务是否已运行
    if check_service_status "$service"; then
        log_warning "${SERVICES[$service]} 已在运行中"
        return 0
    fi

    case "$service" in
        "user-frontend")
            if [ -f "user-frontend/package.json" ]; then
                log_info "启动${SERVICES[$service]} (端口: ${SERVICE_PORTS[$service]})..."
                cd user-frontend
                # 确保使用正确的端口启动
                VITE_DEV_PORT=3000 npm run dev > ../logs/user-frontend.log 2>&1 &
                echo $! > ../logs/user-frontend.pid
                cd ..
                sleep 5
                if check_service_status "$service"; then
                    log_success "${SERVICES[$service]} 启动成功"
                    log_info "访问地址: http://localhost:3000"
                else
                    log_error "${SERVICES[$service]} 启动失败"
                    log_info "查看日志: ./scripts/dev-tools.sh logs user-frontend"
                    return 1
                fi
            else
                log_error "用户端前端项目不存在"
                return 1
            fi
            ;;
        "admin-frontend")
            if [ -f "admin-frontend/package.json" ]; then
                log_info "启动${SERVICES[$service]} (端口: ${SERVICE_PORTS[$service]})..."
                cd admin-frontend
                npm run dev -- --port 3001 > ../logs/admin-frontend.log 2>&1 &
                echo $! > ../logs/admin-frontend.pid
                cd ..
                sleep 3
                if check_service_status "$service"; then
                    log_success "${SERVICES[$service]} 启动成功"
                else
                    log_error "${SERVICES[$service]} 启动失败"
                    return 1
                fi
            else
                log_error "管理后台前端项目不存在"
                return 1
            fi
            ;;
        "backend")
            if [ -f "backend/pom.xml" ]; then
                log_info "启动${SERVICES[$service]} (端口: ${SERVICE_PORTS[$service]})..."
                cd backend
                # 确保JAVA_HOME环境变量正确设置并传递给子进程
                JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64 PATH=$JAVA_HOME/bin:$PATH mvn spring-boot:run > ../logs/backend.log 2>&1 &
                echo $! > ../logs/backend.pid
                cd ..
                sleep 5
                if check_service_status "$service"; then
                    log_success "${SERVICES[$service]} 启动成功"
                else
                    log_error "${SERVICES[$service]} 启动失败"
                    return 1
                fi
            else
                log_error "后端项目不存在"
                return 1
            fi
            ;;
        "mysql"|"redis"|"minio")
            log_info "检查${SERVICES[$service]}连接状态..."
            if check_service_status "$service"; then
                log_success "${SERVICES[$service]} 连接正常"
            else
                log_error "${SERVICES[$service]} 连接失败，请检查容器是否正常运行"
                return 1
            fi
            ;;
        *)
            log_error "不支持启动服务: $service"
            return 1
            ;;
    esac
}

# 启动开发环境（批量）
start_dev() {
    local service="$1"

    if [[ -n "$service" ]]; then
        # 启动单个服务
        if ! validate_service "$service"; then
            return 1
        fi
        log_header "启动服务: ${SERVICES[$service]}"
        start_single_service "$service"
    else
        # 启动所有服务
        log_header "启动开发环境"

        # 检查是否在容器内
        if [ ! -f /.dockerenv ]; then
            log_error "请在devcontainer内运行此脚本"
            exit 1
        fi

        # 启动所有应用服务
        for service in "user-frontend" "admin-frontend" "backend"; do
            start_single_service "$service"
        done

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
    fi
}

# 强制清理服务进程
force_kill_service_processes() {
    local service="$1"

    case "$service" in
        "user-frontend")
            # 清理所有user-frontend相关进程
            pkill -f "user-frontend.*vite" 2>/dev/null || true
            pkill -f "user-frontend.*npm" 2>/dev/null || true
            # 清理可能运行在3000或5174端口的vite进程
            local pids=$(lsof -ti:3000,5174 2>/dev/null || true)
            if [[ -n "$pids" ]]; then
                echo "$pids" | xargs kill -9 2>/dev/null || true
            fi
            ;;
        "admin-frontend")
            # 清理所有admin-frontend相关进程
            pkill -f "admin-frontend.*vite" 2>/dev/null || true
            pkill -f "admin-frontend.*npm" 2>/dev/null || true
            # 清理可能运行在3001端口的vite进程
            local pids=$(lsof -ti:3001 2>/dev/null || true)
            if [[ -n "$pids" ]]; then
                echo "$pids" | xargs kill -9 2>/dev/null || true
            fi
            ;;
        "backend")
            # 清理所有backend相关进程
            pkill -f "spring-boot:run" 2>/dev/null || true
            pkill -f "backend.*mvn" 2>/dev/null || true
            # 清理可能运行在8080端口的进程
            local pids=$(lsof -ti:8080 2>/dev/null || true)
            if [[ -n "$pids" ]]; then
                echo "$pids" | xargs kill -9 2>/dev/null || true
            fi
            ;;
    esac
}

# 停止单个服务
stop_single_service() {
    local service="$1"

    log_info "停止${SERVICES[$service]}..."

    case "$service" in
        "user-frontend"|"admin-frontend"|"backend")
            # 首先尝试优雅停止
            if [ -f "logs/${service}.pid" ]; then
                local pid=$(cat "logs/${service}.pid")
                if kill -0 "$pid" 2>/dev/null; then
                    log_info "优雅停止进程 $pid..."
                    kill "$pid" 2>/dev/null || true
                    sleep 2
                fi
                rm -f "logs/${service}.pid"
            fi

            # 强制清理所有相关进程
            log_info "清理所有相关进程..."
            force_kill_service_processes "$service"

            # 等待进程完全停止
            sleep 2

            # 验证停止结果
            if ! check_service_status "$service"; then
                log_success "${SERVICES[$service]} 已完全停止"
            else
                log_warning "${SERVICES[$service]} 可能仍有残留进程，正在强制清理..."
                force_kill_service_processes "$service"
                sleep 1
                if ! check_service_status "$service"; then
                    log_success "${SERVICES[$service]} 已强制停止"
                else
                    log_error "${SERVICES[$service]} 停止失败，请手动检查进程"
                    return 1
                fi
            fi
            ;;
        "mysql"|"redis"|"minio")
            log_warning "${SERVICES[$service]} 是容器服务，无法通过此脚本停止"
            log_info "请使用 docker-compose 或容器管理工具停止"
            ;;
        *)
            log_error "不支持停止服务: $service"
            return 1
            ;;
    esac
}

# 停止开发环境（批量）
stop_dev() {
    local service="$1"

    if [[ -n "$service" ]]; then
        # 停止单个服务
        if ! validate_service "$service"; then
            return 1
        fi
        log_header "停止服务: ${SERVICES[$service]}"
        stop_single_service "$service"
    else
        # 停止所有服务
        log_header "停止开发环境"

        # 创建logs目录
        mkdir -p logs

        # 停止所有应用服务
        for service in "backend" "admin-frontend" "user-frontend"; do
            stop_single_service "$service"
        done

        log_success "开发环境已停止"
    fi
}

# 重启开发环境
restart_dev() {
    local service="$1"

    if [[ -n "$service" ]]; then
        # 重启单个服务
        if ! validate_service "$service"; then
            return 1
        fi
        log_header "重启服务: ${SERVICES[$service]}"
        stop_single_service "$service"
        sleep 2
        start_single_service "$service"
    else
        # 重启所有服务
        log_header "重启开发环境"
        stop_dev
        sleep 2
        start_dev
    fi
}

# 显示单个服务状态
show_single_status() {
    local service="$1"

    echo -e "${CYAN}服务状态: ${SERVICES[$service]}${NC}"
    echo "端口: ${SERVICE_PORTS[$service]}"

    if check_service_status "$service"; then
        echo -e "状态: ${GREEN}运行中${NC}"

        # 显示额外信息
        case "$service" in
            "user-frontend"|"admin-frontend"|"backend")
                if [ -f "logs/${service}.pid" ]; then
                    local pid=$(cat "logs/${service}.pid")
                    echo "进程ID: $pid"
                    if [ -f "logs/${service}.log" ]; then
                        echo "日志文件: logs/${service}.log"
                    fi
                fi
                ;;
        esac

        # 显示访问地址
        case "$service" in
            "user-frontend")
                echo "访问地址: http://localhost:3000"
                ;;
            "admin-frontend")
                echo "访问地址: http://localhost:3001"
                ;;
            "backend")
                echo "访问地址: http://localhost:8080"
                ;;
            "mysql")
                echo "连接地址: localhost:3306"
                ;;
            "redis")
                echo "连接地址: localhost:6379"
                ;;
            "minio")
                echo "访问地址: http://localhost:9000"
                echo "控制台: http://localhost:9001"
                ;;
        esac
    else
        echo -e "状态: ${RED}已停止${NC}"
    fi
}

# 查看服务状态
show_status() {
    local service="$1"

    if [[ -n "$service" ]]; then
        # 显示单个服务状态
        if ! validate_service "$service"; then
            return 1
        fi
        show_single_status "$service"
    else
        # 显示所有服务状态
        log_header "服务状态"

        for service in "${!SERVICES[@]}"; do
            if check_service_status "$service"; then
                echo -e "${SERVICES[$service]}: ${GREEN}运行中${NC}"
            else
                echo -e "${SERVICES[$service]}: ${RED}已停止${NC}"
            fi
        done

        echo ""
        echo "详细状态请使用: $0 status <服务名>"
    fi
}

# 查看日志
show_logs() {
    local service="$1"

    if [[ -n "$service" ]]; then
        # 显示指定服务日志
        if ! validate_service "$service"; then
            return 1
        fi

        case "$service" in
            "user-frontend"|"admin-frontend"|"backend")
                local log_file="logs/${service}.log"
                if [ -f "$log_file" ]; then
                    log_header "${SERVICES[$service]} 日志"
                    echo "日志文件: $log_file"
                    echo "按 Ctrl+C 退出日志查看"
                    echo ""
                    tail -f "$log_file"
                else
                    log_warning "${SERVICES[$service]} 日志文件不存在: $log_file"
                fi
                ;;
            "mysql"|"redis"|"minio")
                log_warning "${SERVICES[$service]} 是容器服务，请使用 docker logs 查看日志"
                echo "示例: docker logs mysql-container"
                ;;
            *)
                log_error "服务 $service 不支持日志查看"
                ;;
        esac
    else
        # 交互式选择日志
        log_header "服务日志"

        echo "选择要查看的日志:"
        echo "1) 用户端前端"
        echo "2) 管理后台前端"
        echo "3) 后端服务"
        echo "4) 全部日志"
        read -p "请选择 (1-4): " choice

        case $choice in
            1)
                show_logs "user-frontend"
                ;;
            2)
                show_logs "admin-frontend"
                ;;
            3)
                show_logs "backend"
                ;;
            4)
                if [ -d "logs" ] && ls logs/*.log >/dev/null 2>&1; then
                    log_header "全部服务日志"
                    echo "按 Ctrl+C 退出日志查看"
                    echo ""
                    tail -f logs/*.log
                else
                    log_warning "没有找到日志文件"
                fi
                ;;
            *)
                log_error "无效选择"
                ;;
        esac
    fi
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

    local command="${1:-help}"
    local service="$2"

    case "$command" in
        start)
            start_dev "$service"
            ;;
        stop)
            stop_dev "$service"
            ;;
        restart)
            restart_dev "$service"
            ;;
        status)
            show_status "$service"
            ;;
        logs)
            show_logs "$service"
            ;;
        list)
            list_services
            ;;
        clean)
            clean_cache
            ;;
        help|-h|--help|*)
            show_help
            ;;
    esac
}

# 执行主函数
main "$@"
