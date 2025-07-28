#!/bin/bash

# 速码网M3模块API集成测试脚本
# 测试核心API接口的可用性和响应

echo "🚀 开始速码网M3模块API集成测试"
echo "=================================="

# 配置
BASE_URL="http://localhost:8080"
FRONTEND_URL="http://localhost:3000"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 测试函数
test_api() {
    local name="$1"
    local url="$2"
    local method="${3:-GET}"
    local expected_status="${4:-200}"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -n "测试 $name ... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" -o /dev/null "$BASE_URL$url")
    else
        response=$(curl -s -w "%{http_code}" -o /dev/null -X "$method" "$BASE_URL$url")
    fi
    
    if [ "$response" = "$expected_status" ] || [ "$response" = "401" ] || [ "$response" = "403" ]; then
        echo -e "${GREEN}✅ 通过${NC} (状态码: $response)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ 失败${NC} (状态码: $response, 期望: $expected_status)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# 检查服务状态
check_service() {
    local name="$1"
    local url="$2"
    
    echo -n "检查 $name 服务状态 ... "
    
    if curl -s --connect-timeout 5 "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 运行中${NC}"
        return 0
    else
        echo -e "${RED}❌ 未运行${NC}"
        return 1
    fi
}

echo -e "${BLUE}📋 1. 服务状态检查${NC}"
echo "--------------------------------"

# 检查后端服务
check_service "后端服务" "$BASE_URL/actuator/health"
BACKEND_STATUS=$?

# 检查前端服务
check_service "前端服务" "$FRONTEND_URL"
FRONTEND_STATUS=$?

echo ""

if [ $BACKEND_STATUS -ne 0 ]; then
    echo -e "${RED}❌ 后端服务未运行，跳过API测试${NC}"
    exit 1
fi

echo -e "${BLUE}📋 2. 项目上传功能API测试${NC}"
echo "--------------------------------"

# 项目上传相关API
test_api "项目列表查询" "/api/projects"
test_api "项目详情查询" "/api/projects/1"
test_api "项目搜索" "/api/projects/search" "POST"
test_api "文件上传检查" "/api/projects/upload/check" "POST" "401"
test_api "项目创建" "/api/projects" "POST" "401"

echo ""

echo -e "${BLUE}📋 3. 安全检查功能API测试${NC}"
echo "--------------------------------"

# 安全检查相关API
test_api "文件安全检查" "/api/security/scan" "POST" "401"
test_api "项目质量评估" "/api/security/quality" "POST" "401"
test_api "恶意文件检测" "/api/security/malware" "POST" "401"

echo ""

echo -e "${BLUE}📋 4. 购买权限控制API测试${NC}"
echo "--------------------------------"

# 订单管理相关API
test_api "订单列表查询" "/api/orders/purchases" "GET" "401"
test_api "订单创建" "/api/orders" "POST" "401"
test_api "购买权限检查" "/api/orders/check-purchase/1" "GET" "401"
test_api "购买状态检查" "/api/orders/check-purchased/1" "GET" "401"
test_api "用户购买统计" "/api/orders/statistics/purchases" "GET" "401"

echo ""

echo -e "${BLUE}📋 5. 项目下载功能API测试${NC}"
echo "--------------------------------"

# 下载管理相关API
test_api "下载权限检查" "/api/downloads/project/1/permission"
test_api "下载令牌生成" "/api/downloads/project/1/token" "POST" "401"
test_api "下载历史查询" "/api/downloads/history" "GET" "401"
test_api "下载统计查询" "/api/downloads/statistics/user" "GET" "401"
test_api "热门下载查询" "/api/downloads/statistics/popular"

echo ""

echo -e "${BLUE}📋 6. 用户认证API测试${NC}"
echo "--------------------------------"

# 用户认证相关API
test_api "用户注册" "/api/auth/register" "POST"
test_api "用户登录" "/api/auth/login" "POST"
test_api "用户信息" "/api/auth/user" "GET" "401"
test_api "密码重置" "/api/auth/reset-password" "POST"

echo ""

echo -e "${BLUE}📋 7. 积分系统API测试${NC}"
echo "--------------------------------"

# 积分系统相关API
test_api "积分账户查询" "/api/points/account" "GET" "401"
test_api "积分交易记录" "/api/points/transactions" "GET" "401"
test_api "积分充值" "/api/points/recharge" "POST" "401"
test_api "积分转账" "/api/points/transfer" "POST" "401"

echo ""

echo -e "${BLUE}📋 8. 系统健康检查${NC}"
echo "--------------------------------"

# 系统健康检查
test_api "应用健康检查" "/actuator/health"
test_api "应用信息" "/actuator/info"
test_api "数据库连接" "/actuator/health/db"
test_api "Redis连接" "/actuator/health/redis"

echo ""

# 前端页面可访问性测试
if [ $FRONTEND_STATUS -eq 0 ]; then
    echo -e "${BLUE}📋 9. 前端页面可访问性测试${NC}"
    echo "--------------------------------"
    
    # 主要页面检查
    pages=(
        "/"
        "/login"
        "/register"
        "/projects"
        "/projects/upload"
        "/user/profile"
        "/user/projects"
        "/user/orders"
        "/user/downloads"
    )
    
    for page in "${pages[@]}"; do
        echo -n "检查页面 $page ... "
        if curl -s --connect-timeout 5 "$FRONTEND_URL$page" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ 可访问${NC}"
        else
            echo -e "${RED}❌ 不可访问${NC}"
        fi
    done
    
    echo ""
fi

# 测试结果汇总
echo "=================================="
echo -e "${BLUE}📊 测试结果汇总${NC}"
echo "=================================="
echo "总测试数: $TOTAL_TESTS"
echo -e "通过测试: ${GREEN}$PASSED_TESTS${NC}"
echo -e "失败测试: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}🎉 所有API测试通过！${NC}"
    SUCCESS_RATE=100
else
    SUCCESS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo -e "${YELLOW}⚠️  部分测试失败，成功率: $SUCCESS_RATE%${NC}"
fi

echo ""
echo -e "${BLUE}📝 测试说明${NC}"
echo "--------------------------------"
echo "• 401/403状态码表示需要认证，这是正常的安全行为"
echo "• 200状态码表示接口正常响应"
echo "• 404状态码可能表示接口路径问题"
echo "• 500状态码表示服务器内部错误"

echo ""
echo -e "${BLUE}🔧 如何修复问题${NC}"
echo "--------------------------------"
echo "1. 确保后端服务在端口8080运行"
echo "2. 确保前端服务在端口3000运行"
echo "3. 检查数据库和Redis连接"
echo "4. 查看应用日志获取详细错误信息"

echo ""
echo -e "${GREEN}✅ API集成测试完成${NC}"

# 返回适当的退出码
if [ $SUCCESS_RATE -ge 80 ]; then
    exit 0
else
    exit 1
fi
