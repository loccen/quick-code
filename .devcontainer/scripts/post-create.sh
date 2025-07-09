#!/bin/bash

# 速码网开发环境 - 容器创建后执行脚本
# 此脚本在devcontainer创建后执行一次，用于初始化开发环境

set -e

echo "🚀 开始初始化速码网开发环境..."

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

# 检查项目结构
check_project_structure() {
    log_info "检查项目结构..."
    
    # 检查三个核心项目是否存在
    local projects_exist=true
    
    if [ ! -f "user-frontend/package.json" ]; then
        log_warning "用户端前端项目不存在"
        projects_exist=false
    else
        log_success "用户端前端项目已存在"
    fi
    
    if [ ! -f "admin-frontend/package.json" ]; then
        log_warning "管理后台前端项目不存在"
        projects_exist=false
    else
        log_success "管理后台前端项目已存在"
    fi
    
    if [ ! -f "backend/pom.xml" ]; then
        log_warning "后端项目不存在"
        projects_exist=false
    else
        log_success "后端项目已存在"
    fi
    
    if [ "$projects_exist" = false ]; then
        log_error "部分项目不存在，请先创建完整的项目结构"
        return 1
    fi
}

# 安装前端依赖
install_frontend_dependencies() {
    log_info "安装前端项目依赖..."
    
    # 安装用户端前端依赖
    if [ -f "user-frontend/package.json" ]; then
        if [ ! -d "user-frontend/node_modules" ] || [ ! -f "user-frontend/node_modules/.package-lock.json" ]; then
            log_info "安装用户端前端依赖..."
            cd user-frontend
            npm install --silent
            cd ..
            log_success "用户端前端依赖安装完成"
        else
            log_info "用户端前端依赖已存在"
        fi
    fi
    
    # 安装管理后台前端依赖
    if [ -f "admin-frontend/package.json" ]; then
        if [ ! -d "admin-frontend/node_modules" ] || [ ! -f "admin-frontend/node_modules/.package-lock.json" ]; then
            log_info "安装管理后台前端依赖..."
            cd admin-frontend
            npm install --silent
            cd ..
            log_success "管理后台前端依赖安装完成"
        else
            log_info "管理后台前端依赖已存在"
        fi
    fi
}

# 编译后端项目
compile_backend_project() {
    log_info "编译后端项目..."
    
    if [ -f "backend/pom.xml" ]; then
        cd backend
        
        # 检查是否需要编译
        if [ ! -d "target/classes" ]; then
            log_info "编译后端项目..."
            mvn clean compile -q
            if [ $? -eq 0 ]; then
                log_success "后端项目编译成功"
            else
                log_warning "后端项目编译失败，请检查依赖配置"
            fi
        else
            log_info "后端项目已编译"
        fi
        
        cd ..
    fi
}

# 配置Git
configure_git() {
    log_info "配置Git..."
    
    # 设置Git配置（如果还没有配置）
    if [ -z "$(git config --global user.name)" ]; then
        git config --global user.name "loccen"
        git config --global user.email "loccen@foxmail.com"
        log_info "已设置默认Git用户信息，请根据需要修改"
    fi
    
    # 设置Git别名
    git config --global alias.st status
    git config --global alias.co checkout
    git config --global alias.br branch
    git config --global alias.ci commit
    git config --global alias.unstage 'reset HEAD --'
    git config --global alias.last 'log -1 HEAD'
    
    log_success "Git配置完成"
}

# 创建开发脚本
create_dev_scripts() {
    log_info "检查开发脚本..."
    
    if [ ! -f "scripts/dev-tools.sh" ]; then
        log_warning "开发脚本不存在，请检查scripts目录"
    else
        log_success "开发脚本已存在"
        chmod +x scripts/dev-tools.sh
    fi
}

# 验证环境
verify_environment() {
    log_info "验证开发环境..."
    
    # 检查Node.js
    if command -v node >/dev/null 2>&1; then
        log_success "Node.js 版本: $(node --version)"
    else
        log_error "Node.js 未安装"
    fi
    
    # 检查npm
    if command -v npm >/dev/null 2>&1; then
        log_success "npm 版本: $(npm --version)"
    else
        log_error "npm 未安装"
    fi
    
    # 检查Java
    if command -v java >/dev/null 2>&1; then
        log_success "Java 版本: $(java --version | head -n 1)"
    else
        log_error "Java 未安装"
    fi
    
    # 检查Maven
    if command -v mvn >/dev/null 2>&1; then
        log_success "Maven 版本: $(mvn --version | head -n 1)"
    else
        log_error "Maven 未安装"
    fi
}

# 显示启动信息
show_startup_info() {
    log_success "🎉 速码网开发环境初始化完成！"
    echo ""
    echo "📋 项目信息:"
    echo "  用户端前端:     http://localhost:3000"
    echo "  管理后台前端:   http://localhost:3001"
    echo "  后端API:       http://localhost:8080"
    echo "  MySQL数据库:   localhost:3306"
    echo "  Redis缓存:     localhost:6379"
    echo "  MinIO存储:     http://localhost:9000"
    echo "  MinIO控制台:   http://localhost:9001"
    echo ""
    echo "🛠️  常用命令:"
    echo "  启动开发环境:   ./scripts/dev-tools.sh start"
    echo "  查看服务状态:   ./scripts/dev-tools.sh status"
    echo "  查看服务日志:   ./scripts/dev-tools.sh logs"
    echo "  停止开发环境:   ./scripts/dev-tools.sh stop"
    echo ""
    echo "📚 更多信息请查看 README.md 和 docs/ 目录"
}

# 主执行流程
main() {
    log_info "开始执行容器创建后初始化..."
    
    check_project_structure
    install_frontend_dependencies
    compile_backend_project
    configure_git
    create_dev_scripts
    verify_environment
    show_startup_info
}

# 执行主函数
main "$@"
