#!/bin/bash

# 速码网开发环境 - 权限修复脚本
# 解决Docker容器中的文件权限问题

set -e

echo "🔧 开始修复权限问题..."

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

# 获取当前用户信息
CURRENT_USER=$(whoami)
CURRENT_UID=$(id -u)
CURRENT_GID=$(id -g)

log_info "当前用户: $CURRENT_USER (UID: $CURRENT_UID, GID: $CURRENT_GID)"

# 修复工作目录权限
fix_workspace_permissions() {
    log_info "修复工作目录权限..."
    
    # 修复整个workspace目录的权限
    if [ -w "/workspace" ]; then
        sudo chown -R $CURRENT_UID:$CURRENT_GID /workspace 2>/dev/null || {
            log_warning "无法使用sudo修复权限，尝试其他方法..."
            chown -R $CURRENT_UID:$CURRENT_GID /workspace 2>/dev/null || {
                log_warning "权限修复失败，但可能不影响正常使用"
            }
        }
        log_success "工作目录权限修复完成"
    else
        log_warning "无法写入工作目录，权限可能有问题"
    fi
}

# 修复node_modules权限
fix_node_modules_permissions() {
    log_info "修复node_modules权限..."
    
    # 修复用户端前端node_modules
    if [ -d "/workspace/user-frontend/node_modules" ]; then
        log_info "修复用户端前端node_modules权限..."
        sudo chown -R $CURRENT_UID:$CURRENT_GID /workspace/user-frontend/node_modules 2>/dev/null || {
            chown -R $CURRENT_UID:$CURRENT_GID /workspace/user-frontend/node_modules 2>/dev/null || {
                log_warning "无法修复用户端前端node_modules权限"
            }
        }
    fi
    
    # 修复管理后台前端node_modules
    if [ -d "/workspace/admin-frontend/node_modules" ]; then
        log_info "修复管理后台前端node_modules权限..."
        sudo chown -R $CURRENT_UID:$CURRENT_GID /workspace/admin-frontend/node_modules 2>/dev/null || {
            chown -R $CURRENT_UID:$CURRENT_GID /workspace/admin-frontend/node_modules 2>/dev/null || {
                log_warning "无法修复管理后台前端node_modules权限"
            }
        }
    fi
    
    log_success "node_modules权限修复完成"
}

# 修复Maven缓存权限
fix_maven_permissions() {
    log_info "修复Maven缓存权限..."
    
    if [ -d "/home/$CURRENT_USER/.m2" ]; then
        sudo chown -R $CURRENT_UID:$CURRENT_GID /home/$CURRENT_USER/.m2 2>/dev/null || {
            chown -R $CURRENT_UID:$CURRENT_GID /home/$CURRENT_USER/.m2 2>/dev/null || {
                log_warning "无法修复Maven缓存权限"
            }
        }
        log_success "Maven缓存权限修复完成"
    fi
}

# 清理并重新安装依赖
reinstall_dependencies() {
    log_info "清理并重新安装前端依赖..."
    
    # 清理用户端前端依赖
    if [ -f "/workspace/user-frontend/package.json" ]; then
        log_info "重新安装用户端前端依赖..."
        cd /workspace/user-frontend
        
        # 清理现有依赖
        rm -rf node_modules package-lock.json 2>/dev/null || true
        
        # 重新安装
        npm install --no-optional --prefer-offline --unsafe-perm=true || {
            log_warning "用户端前端依赖安装失败"
        }
        
        cd /workspace
    fi
    
    # 清理管理后台前端依赖
    if [ -f "/workspace/admin-frontend/package.json" ]; then
        log_info "重新安装管理后台前端依赖..."
        cd /workspace/admin-frontend
        
        # 清理现有依赖
        rm -rf node_modules package-lock.json 2>/dev/null || true
        
        # 重新安装
        npm install --no-optional --prefer-offline --unsafe-perm=true || {
            log_warning "管理后台前端依赖安装失败"
        }
        
        cd /workspace
    fi
    
    log_success "依赖重新安装完成"
}

# 设置npm配置
configure_npm() {
    log_info "配置npm设置..."
    
    # 设置npm缓存目录权限
    npm config set cache /home/$CURRENT_USER/.npm
    npm config set unsafe-perm true
    
    # 创建npm缓存目录
    mkdir -p /home/$CURRENT_USER/.npm
    sudo chown -R $CURRENT_UID:$CURRENT_GID /home/$CURRENT_USER/.npm 2>/dev/null || true
    
    log_success "npm配置完成"
}

# 主执行流程
main() {
    log_info "开始权限修复流程..."
    
    configure_npm
    fix_workspace_permissions
    fix_node_modules_permissions
    fix_maven_permissions
    
    # 如果指定了重新安装参数
    if [ "$1" = "--reinstall" ]; then
        reinstall_dependencies
    fi
    
    log_success "🎉 权限修复完成！"
    echo ""
    echo "如果仍然遇到权限问题，请尝试："
    echo "  1. 重新构建容器: docker-compose down && docker-compose up --build"
    echo "  2. 使用重新安装参数: ./fix-permissions.sh --reinstall"
    echo "  3. 手动删除node_modules后重新安装依赖"
}

# 执行主函数
main "$@"
