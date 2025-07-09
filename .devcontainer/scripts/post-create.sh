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

# 检查并创建项目目录结构
create_project_structure() {
    log_info "创建项目目录结构..."
    
    # 创建主要目录
    mkdir -p user-frontend
    mkdir -p admin-frontend
    mkdir -p backend
    mkdir -p shared
    mkdir -p scripts
    mkdir -p docker
    mkdir -p docs/api
    mkdir -p docs/deployment
    
    log_success "项目目录结构创建完成"
}

# 初始化前端项目
init_frontend_projects() {
    log_info "检查前端项目..."
    
    # 检查用户端前端
    if [ ! -f "user-frontend/package.json" ]; then
        log_info "初始化用户端前端项目..."
        cd user-frontend
        npm create vue@latest . -- --typescript --router --pinia --eslint --prettier
        cd ..
        log_success "用户端前端项目初始化完成"
    else
        log_info "用户端前端项目已存在，跳过初始化"
    fi
    
    # 检查管理后台前端
    if [ ! -f "admin-frontend/package.json" ]; then
        log_info "初始化管理后台前端项目..."
        cd admin-frontend
        npm create vue@latest . -- --typescript --router --pinia --eslint --prettier
        cd ..
        log_success "管理后台前端项目初始化完成"
    else
        log_info "管理后台前端项目已存在，跳过初始化"
    fi
}

# 初始化后端项目
init_backend_project() {
    log_info "检查后端项目..."
    
    if [ ! -f "backend/pom.xml" ]; then
        log_info "创建Spring Boot后端项目结构..."
        cd backend
        
        # 创建Maven项目结构
        mkdir -p src/main/java/com/quickcode
        mkdir -p src/main/resources
        mkdir -p src/test/java/com/quickcode
        
        # 创建基础的pom.xml
        cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.quickcode</groupId>
    <artifactId>quick-code-backend</artifactId>
    <version>1.0.0</version>
    <name>quick-code-backend</name>
    <description>速码网后端服务</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOF
        
        cd ..
        log_success "后端项目结构创建完成"
    else
        log_info "后端项目已存在，跳过初始化"
    fi
}

# 安装依赖
install_dependencies() {
    log_info "安装项目依赖..."
    
    # 安装用户端前端依赖
    if [ -f "user-frontend/package.json" ]; then
        log_info "安装用户端前端依赖..."
        cd user-frontend
        npm install
        cd ..
        log_success "用户端前端依赖安装完成"
    fi
    
    # 安装管理后台前端依赖
    if [ -f "admin-frontend/package.json" ]; then
        log_info "安装管理后台前端依赖..."
        cd admin-frontend
        npm install
        cd ..
        log_success "管理后台前端依赖安装完成"
    fi
    
    # 下载Maven依赖
    if [ -f "backend/pom.xml" ]; then
        log_info "下载后端Maven依赖..."
        cd backend
        mvn dependency:resolve
        cd ..
        log_success "后端Maven依赖下载完成"
    fi
}

# 配置Git
configure_git() {
    log_info "配置Git..."
    
    # 设置Git配置（如果还没有配置）
    if [ -z "$(git config --global user.name)" ]; then
        git config --global user.name "Developer"
        git config --global user.email "developer@quickcode.com"
        log_info "已设置默认Git用户信息，请根据需要修改"
    fi
    
    # 设置Git别名
    git config --global alias.st status
    git config --global alias.co checkout
    git config --global alias.br branch
    git config --global alias.ci commit
    git config --global alias.unstage 'reset HEAD --'
    git config --global alias.last 'log -1 HEAD'
    git config --global alias.visual '!gitk'
    
    log_success "Git配置完成"
}

# 创建开发脚本
create_dev_scripts() {
    log_info "创建开发脚本..."
    
    # 创建scripts目录
    mkdir -p scripts
    
    # 创建启动脚本
    cat > scripts/start-dev.sh << 'EOF'
#!/bin/bash
# 启动开发环境

echo "🚀 启动速码网开发环境..."

# 启动用户端前端
if [ -f "user-frontend/package.json" ]; then
    echo "启动用户端前端..."
    cd user-frontend
    npm run dev &
    cd ..
fi

# 启动管理后台前端
if [ -f "admin-frontend/package.json" ]; then
    echo "启动管理后台前端..."
    cd admin-frontend
    npm run dev -- --port 3001 &
    cd ..
fi

# 启动后端服务
if [ -f "backend/pom.xml" ]; then
    echo "启动后端服务..."
    cd backend
    mvn spring-boot:run &
    cd ..
fi

echo "✅ 开发环境启动完成！"
echo "用户端前端: http://localhost:3000"
echo "管理后台前端: http://localhost:3001"
echo "后端API: http://localhost:8080"
EOF
    
    chmod +x scripts/start-dev.sh
    
    log_success "开发脚本创建完成"
}

# 主执行流程
main() {
    log_info "开始执行容器创建后初始化..."
    
    create_project_structure
    init_frontend_projects
    init_backend_project
    install_dependencies
    configure_git
    create_dev_scripts
    
    log_success "🎉 速码网开发环境初始化完成！"
    log_info "使用 './scripts/start-dev.sh' 启动开发环境"
    log_info "或者查看 README.md 了解更多使用方法"
}

# 执行主函数
main "$@"
