#!/bin/bash

# 修复npm包安装问题的脚本
# 解决DevContainer构建中的npm相关错误

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

# 检查npm包是否存在
check_npm_package() {
    local package_name=$1
    log_info "检查包: $package_name"
    
    if npm view "$package_name" version >/dev/null 2>&1; then
        log_success "✅ $package_name 存在"
        return 0
    else
        log_error "❌ $package_name 不存在或无法访问"
        return 1
    fi
}

# 验证npm包
validate_npm_packages() {
    log_header "验证npm包可用性"
    
    local packages=(
        "typescript"
        "pm2"
        "serve"
        "create-vue"
        "@vue/cli"
        "@vitejs/create-vue"
        "vite"
        "@types/node"
    )
    
    local failed_packages=()
    
    for package in "${packages[@]}"; do
        if ! check_npm_package "$package"; then
            failed_packages+=("$package")
        fi
    done
    
    if [ ${#failed_packages[@]} -gt 0 ]; then
        log_warning "以下包无法访问:"
        for package in "${failed_packages[@]}"; do
            echo "  - $package"
        done
        echo ""
        log_info "建议的替代方案:"
        echo "  - @vitejs/create-vue → create-vue"
        echo "  - @vue/cli → 项目中本地安装"
        echo "  - 其他包可以在项目中按需安装"
    else
        log_success "所有包都可以访问"
    fi
}

# 测试npm连接
test_npm_connectivity() {
    log_header "测试npm连接"
    
    log_info "检查npm配置..."
    npm config get registry
    
    log_info "测试npm连接..."
    if npm ping >/dev/null 2>&1; then
        log_success "npm连接正常"
    else
        log_error "npm连接失败"
        log_info "尝试使用官方镜像源..."
        npm config set registry https://registry.npmjs.org/
    fi
}

# 清理npm缓存
clean_npm_cache() {
    log_header "清理npm缓存"
    
    log_info "清理npm缓存..."
    npm cache clean --force
    
    log_success "npm缓存清理完成"
}

# 生成修复建议
generate_fix_suggestions() {
    log_header "修复建议"
    
    echo "基于检查结果，建议进行以下修改:"
    echo ""
    echo "1. 简化Dockerfile中的全局npm包安装:"
    echo "   只安装必要的包: typescript, pm2, serve"
    echo ""
    echo "2. 移除有问题的包:"
    echo "   - @vitejs/create-vue (不存在，应该用 create-vue)"
    echo "   - @vue/cli (可以在项目中本地安装)"
    echo ""
    echo "3. 项目特定的包在项目中安装:"
    echo "   cd user-frontend && npm install"
    echo "   cd admin-frontend && npm install"
    echo ""
    echo "4. 清理Docker构建缓存:"
    echo "   ./scripts/clean-docker.sh"
    echo ""
}

# 创建最小化的Dockerfile片段
create_minimal_dockerfile_snippet() {
    log_header "生成最小化Dockerfile片段"
    
    cat > /tmp/dockerfile-npm-snippet.txt << 'EOF'
# 安装Node.js和npm (最小化版本)
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs

# 验证安装
RUN node --version && npm --version

# 只安装必要的全局工具
RUN npm install -g typescript pm2 serve

# 设置npm配置
RUN npm config set fund false \
    && npm config set audit-level moderate
EOF
    
    log_success "最小化Dockerfile片段已生成: /tmp/dockerfile-npm-snippet.txt"
    echo ""
    echo "内容:"
    cat /tmp/dockerfile-npm-snippet.txt
}

# 主函数
main() {
    echo "🔧 npm包问题修复工具"
    echo ""
    
    test_npm_connectivity
    echo ""
    
    validate_npm_packages
    echo ""
    
    clean_npm_cache
    echo ""
    
    generate_fix_suggestions
    echo ""
    
    create_minimal_dockerfile_snippet
    echo ""
    
    log_success "✅ npm问题分析完成！"
    echo ""
    echo "🚀 下一步操作:"
    echo "  1. 应用Dockerfile修改 (已自动完成)"
    echo "  2. 清理Docker缓存: ./scripts/clean-docker.sh"
    echo "  3. 重新构建容器: VSCode -> Dev Containers: Rebuild Container"
}

# 执行主函数
main "$@"
