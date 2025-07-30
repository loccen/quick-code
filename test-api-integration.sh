#!/bin/bash

# 速码网API集成测试脚本
# 测试主要功能的前后端集成

echo "🚀 开始速码网API集成测试..."

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
    local expected_status="$3"
    local method="${4:-GET}"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "${BLUE}测试: $name${NC}"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" -o /dev/null "$BASE_URL$url")
    else
        response=$(curl -s -w "%{http_code}" -o /dev/null -X "$method" "$BASE_URL$url")
    fi
    
    if [ "$response" = "$expected_status" ]; then
        echo -e "${GREEN}✓ 通过 (状态码: $response)${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ 失败 (期望: $expected_status, 实际: $response)${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    echo
}

# 测试前端页面可访问性
test_frontend() {
    local name="$1"
    local path="$2"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "${BLUE}测试前端页面: $name${NC}"
    
    response=$(curl -s -w "%{http_code}" -o /dev/null "$FRONTEND_URL$path")
    
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✓ 通过 (状态码: $response)${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ 失败 (状态码: $response)${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    echo
}

echo "=== 1. 后端服务健康检查 ==="
test_api "健康检查" "/actuator/health" "200"
test_api "API文档" "/swagger-ui.html" "302"  # Swagger UI重定向是正常的

echo "=== 2. 公开API测试 ==="
test_api "获取项目分类" "/api/public/projects/categories" "200"
test_api "获取热门项目" "/api/public/projects/popular?limit=10" "200"
test_api "获取最新项目" "/api/public/projects/latest?limit=10" "200"

echo "=== 3. 项目相关API测试 ==="
test_api "项目列表" "/api/projects?page=0&size=10" "401"  # 需要认证
test_api "公开项目搜索" "/api/public/projects/search?keyword=test&limit=10" "200"

echo "=== 4. 用户相关API测试 ==="
test_api "用户注册" "/api/auth/register" "405" "GET"  # GET方法不支持
test_api "用户登录" "/api/auth/login" "405" "GET"     # GET方法不支持

echo "=== 5. 前端页面可访问性测试 ==="
test_frontend "首页" "/"
test_frontend "项目市场" "/market"
test_frontend "登录页面" "/login"
test_frontend "注册页面" "/register"

echo "=== 6. 特定功能API测试 ==="
test_api "收藏功能API" "/api/projects/favorites" "401"  # 需要认证
test_api "下载记录API" "/api/downloads/user/records" "401"  # 需要认证
test_api "订单统计API" "/api/orders/statistics/user" "401"  # 需要认证
test_api "项目统计API" "/api/projects/user/stats" "401"  # 需要认证

echo "=== 测试结果汇总 ==="
echo -e "${BLUE}总测试数: $TOTAL_TESTS${NC}"
echo -e "${GREEN}通过: $PASSED_TESTS${NC}"
echo -e "${RED}失败: $FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}🎉 所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}❌ 有 $FAILED_TESTS 个测试失败${NC}"
    exit 1
fi
