#!/bin/bash

# 速码网开发环境 - npm权限快速修复脚本
# 解决容器中npm安装权限问题

set -e

echo "🔧 修复npm权限问题..."

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

# 配置npm
configure_npm() {
    log_info "配置npm设置..."
    
    # 设置npm全局配置
    npm config set unsafe-perm true
    npm config set user 0
    npm config set cache ~/.npm --global
    
    log_success "npm配置完成"
}

# 清理并重新安装用户端前端依赖
fix_user_frontend() {
    if [ -f "user-frontend/package.json" ]; then
        log_info "修复用户端前端依赖..."
        
        cd user-frontend
        
        # 清理现有依赖
        rm -rf node_modules package-lock.json 2>/dev/null || true
        
        # 重新安装
        npm install --unsafe-perm=true --allow-root
        
        cd ..
        log_success "用户端前端依赖修复完成"
    else
        log_warning "用户端前端项目不存在"
    fi
}

# 清理并重新安装管理后台前端依赖
fix_admin_frontend() {
    if [ -f "admin-frontend/package.json" ]; then
        log_info "修复管理后台前端依赖..."
        
        cd admin-frontend
        
        # 清理现有依赖
        rm -rf node_modules package-lock.json 2>/dev/null || true
        
        # 重新安装
        npm install --unsafe-perm=true --allow-root
        
        cd ..
        log_success "管理后台前端依赖修复完成"
    else
        log_warning "管理后台前端项目不存在"
    fi
}

# 主执行流程
main() {
    log_info "开始npm权限修复..."
    
    configure_npm
    
    # 根据参数决定修复哪个项目
    case "$1" in
        "user")
            fix_user_frontend
            ;;
        "admin")
            fix_admin_frontend
            ;;
        "all"|"")
            fix_user_frontend
            fix_admin_frontend
            ;;
        *)
            echo "用法: $0 [user|admin|all]"
            echo "  user  - 只修复用户端前端"
            echo "  admin - 只修复管理后台前端"
            echo "  all   - 修复所有前端项目（默认）"
            exit 1
            ;;
    esac
    
    log_success "🎉 npm权限修复完成！"
    echo ""
    echo "现在可以尝试启动开发服务器："
    echo "  用户端前端: cd user-frontend && npm run dev"
    echo "  管理后台前端: cd admin-frontend && npm run dev"
}

# 执行主函数
main "$@"
