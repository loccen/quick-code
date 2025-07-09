#!/bin/bash

# 速码网开发环境配置验证脚本
# 验证devcontainer配置是否正确

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

# 验证项目结构
validate_project_structure() {
    log_header "验证项目结构"
    
    local errors=0
    
    # 检查核心项目目录
    if [ ! -d "user-frontend" ]; then
        log_error "用户端前端目录不存在: user-frontend/"
        errors=$((errors + 1))
    else
        log_success "用户端前端目录存在"
    fi
    
    if [ ! -d "admin-frontend" ]; then
        log_error "管理后台前端目录不存在: admin-frontend/"
        errors=$((errors + 1))
    else
        log_success "管理后台前端目录存在"
    fi
    
    if [ ! -d "backend" ]; then
        log_error "后端目录不存在: backend/"
        errors=$((errors + 1))
    else
        log_success "后端目录存在"
    fi
    
    # 检查关键文件
    if [ ! -f "user-frontend/package.json" ]; then
        log_error "用户端前端package.json不存在"
        errors=$((errors + 1))
    else
        log_success "用户端前端package.json存在"
    fi
    
    if [ ! -f "admin-frontend/package.json" ]; then
        log_error "管理后台前端package.json不存在"
        errors=$((errors + 1))
    else
        log_success "管理后台前端package.json存在"
    fi
    
    if [ ! -f "backend/pom.xml" ]; then
        log_error "后端pom.xml不存在"
        errors=$((errors + 1))
    else
        log_success "后端pom.xml存在"
    fi
    
    return $errors
}

# 验证devcontainer配置
validate_devcontainer_config() {
    log_header "验证DevContainer配置"
    
    local errors=0
    
    # 检查devcontainer文件
    if [ ! -f ".devcontainer/devcontainer.json" ]; then
        log_error "devcontainer.json不存在"
        errors=$((errors + 1))
    else
        log_success "devcontainer.json存在"
    fi
    
    if [ ! -f ".devcontainer/docker-compose.yml" ]; then
        log_error "docker-compose.yml不存在"
        errors=$((errors + 1))
    else
        log_success "docker-compose.yml存在"
    fi
    
    if [ ! -f ".devcontainer/Dockerfile" ]; then
        log_error "Dockerfile不存在"
        errors=$((errors + 1))
    else
        log_success "Dockerfile存在"
    fi
    
    # 检查初始化脚本
    if [ ! -f ".devcontainer/scripts/post-create.sh" ]; then
        log_error "post-create.sh不存在"
        errors=$((errors + 1))
    else
        log_success "post-create.sh存在"
        if [ ! -x ".devcontainer/scripts/post-create.sh" ]; then
            log_warning "post-create.sh不可执行"
        fi
    fi
    
    if [ ! -f ".devcontainer/scripts/post-start.sh" ]; then
        log_error "post-start.sh不存在"
        errors=$((errors + 1))
    else
        log_success "post-start.sh存在"
        if [ ! -x ".devcontainer/scripts/post-start.sh" ]; then
            log_warning "post-start.sh不可执行"
        fi
    fi
    
    return $errors
}

# 验证环境配置
validate_environment_config() {
    log_header "验证环境配置"
    
    local errors=0
    
    # 检查环境文件
    if [ ! -f ".env" ]; then
        log_error "根目录.env文件不存在"
        errors=$((errors + 1))
    else
        log_success "根目录.env文件存在"
    fi
    
    if [ ! -f ".env.example" ]; then
        log_warning ".env.example文件不存在"
    else
        log_success ".env.example文件存在"
    fi
    
    # 检查前端环境文件
    if [ ! -f "user-frontend/.env" ]; then
        log_warning "用户端前端.env文件不存在"
    else
        log_success "用户端前端.env文件存在"
    fi
    
    if [ ! -f "admin-frontend/.env" ]; then
        log_warning "管理后台前端.env文件不存在"
    else
        log_success "管理后台前端.env文件存在"
    fi
    
    # 检查后端配置文件
    if [ ! -f "backend/src/main/resources/application.yml" ]; then
        log_error "后端application.yml不存在"
        errors=$((errors + 1))
    else
        log_success "后端application.yml存在"
    fi
    
    if [ ! -f "backend/src/main/resources/application-dev.yml" ]; then
        log_warning "后端application-dev.yml不存在"
    else
        log_success "后端application-dev.yml存在"
    fi
    
    return $errors
}

# 验证端口配置
validate_port_config() {
    log_header "验证端口配置"
    
    local errors=0
    
    # 检查前端Vite配置
    if [ -f "user-frontend/vite.config.ts" ]; then
        if grep -q "port: 3000" "user-frontend/vite.config.ts"; then
            log_success "用户端前端端口配置正确 (3000)"
        else
            log_warning "用户端前端端口配置可能不正确"
        fi
    fi
    
    if [ -f "admin-frontend/vite.config.ts" ]; then
        if grep -q "port: 3001" "admin-frontend/vite.config.ts"; then
            log_success "管理后台前端端口配置正确 (3001)"
        else
            log_warning "管理后台前端端口配置可能不正确"
        fi
    fi
    
    # 检查后端端口配置
    if [ -f "backend/src/main/resources/application.yml" ]; then
        if grep -q "port: 8080" "backend/src/main/resources/application.yml"; then
            log_success "后端端口配置正确 (8080)"
        else
            log_warning "后端端口配置可能不正确"
        fi
    fi
    
    return $errors
}

# 验证开发脚本
validate_dev_scripts() {
    log_header "验证开发脚本"
    
    local errors=0
    
    if [ ! -f "scripts/dev-tools.sh" ]; then
        log_error "开发工具脚本不存在"
        errors=$((errors + 1))
    else
        log_success "开发工具脚本存在"
        if [ ! -x "scripts/dev-tools.sh" ]; then
            log_warning "开发工具脚本不可执行"
        fi
    fi
    
    return $errors
}

# 主验证函数
main() {
    echo "🔍 开始验证速码网开发环境配置..."
    echo ""
    
    local total_errors=0
    
    validate_project_structure
    total_errors=$((total_errors + $?))
    echo ""
    
    validate_devcontainer_config
    total_errors=$((total_errors + $?))
    echo ""
    
    validate_environment_config
    total_errors=$((total_errors + $?))
    echo ""
    
    validate_port_config
    total_errors=$((total_errors + $?))
    echo ""
    
    validate_dev_scripts
    total_errors=$((total_errors + $?))
    echo ""
    
    # 总结
    log_header "验证结果"
    if [ $total_errors -eq 0 ]; then
        log_success "✅ 所有配置验证通过！开发环境配置正确。"
        echo ""
        echo "🚀 您现在可以："
        echo "  1. 在VSCode中打开项目"
        echo "  2. 按Ctrl+Shift+P，选择'Dev Containers: Reopen in Container'"
        echo "  3. 等待容器构建完成"
        echo "  4. 使用 ./scripts/dev-tools.sh start 启动开发环境"
    else
        log_error "❌ 发现 $total_errors 个配置问题，请修复后重试。"
        exit 1
    fi
}

# 执行主函数
main "$@"
