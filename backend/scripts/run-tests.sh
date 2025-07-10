#!/bin/bash

# 速码网后端测试运行脚本
# 提供各种测试运行选项和覆盖率报告生成

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 打印带颜色的消息
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# 打印帮助信息
print_help() {
    echo "速码网后端测试运行脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  unit              运行单元测试"
    echo "  integration       运行集成测试"
    echo "  all               运行所有测试"
    echo "  coverage          运行测试并生成覆盖率报告"
    echo "  coverage-check    运行测试并检查覆盖率阈值"
    echo "  clean             清理测试相关文件"
    echo "  help              显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 unit           # 只运行单元测试"
    echo "  $0 integration    # 只运行集成测试"
    echo "  $0 coverage       # 运行所有测试并生成覆盖率报告"
}

# 清理测试相关文件
clean_test_files() {
    print_message $YELLOW "清理测试相关文件..."
    
    # 清理Maven目标目录
    if [ -d "target" ]; then
        rm -rf target/test-classes
        rm -rf target/surefire-reports
        rm -rf target/failsafe-reports
        rm -rf target/site/jacoco
        rm -rf target/test-logs
        print_message $GREEN "已清理Maven测试文件"
    fi
    
    # 清理日志文件
    if [ -d "logs" ]; then
        rm -f logs/test*.log
        print_message $GREEN "已清理测试日志文件"
    fi
    
    print_message $GREEN "清理完成"
}

# 运行单元测试
run_unit_tests() {
    print_message $BLUE "运行单元测试..."
    
    mvn clean test \
        -Dtest="**/*Test" \
        -DfailIfNoTests=false \
        -Dmaven.test.failure.ignore=false
    
    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        print_message $GREEN "单元测试通过"
    else
        print_message $RED "单元测试失败"
        exit $exit_code
    fi
}

# 运行集成测试
run_integration_tests() {
    print_message $BLUE "运行集成测试..."
    
    mvn clean verify \
        -Dtest="**/*IntegrationTest,**/*IT" \
        -DfailIfNoTests=false \
        -Dmaven.test.failure.ignore=false
    
    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        print_message $GREEN "集成测试通过"
    else
        print_message $RED "集成测试失败"
        exit $exit_code
    fi
}

# 运行所有测试
run_all_tests() {
    print_message $BLUE "运行所有测试..."
    
    mvn clean verify \
        -DfailIfNoTests=false \
        -Dmaven.test.failure.ignore=false
    
    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        print_message $GREEN "所有测试通过"
    else
        print_message $RED "测试失败"
        exit $exit_code
    fi
}

# 运行测试并生成覆盖率报告
run_tests_with_coverage() {
    print_message $BLUE "运行测试并生成覆盖率报告..."
    
    mvn clean verify jacoco:report \
        -DfailIfNoTests=false \
        -Dmaven.test.failure.ignore=false
    
    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        print_message $GREEN "测试完成，覆盖率报告已生成"
        print_message $YELLOW "覆盖率报告位置: target/site/jacoco/index.html"
        
        # 如果是在本地环境，尝试打开报告
        if command -v open >/dev/null 2>&1; then
            open target/site/jacoco/index.html
        elif command -v xdg-open >/dev/null 2>&1; then
            xdg-open target/site/jacoco/index.html
        fi
    else
        print_message $RED "测试失败"
        exit $exit_code
    fi
}

# 运行测试并检查覆盖率阈值
run_tests_with_coverage_check() {
    print_message $BLUE "运行测试并检查覆盖率阈值..."
    
    mvn clean verify jacoco:report jacoco:check \
        -DfailIfNoTests=false \
        -Dmaven.test.failure.ignore=false
    
    local exit_code=$?
    if [ $exit_code -eq 0 ]; then
        print_message $GREEN "测试通过，覆盖率达到要求"
    else
        print_message $RED "测试失败或覆盖率不达标"
        exit $exit_code
    fi
}

# 显示测试结果摘要
show_test_summary() {
    print_message $BLUE "测试结果摘要:"
    
    # 单元测试结果
    if [ -f "target/surefire-reports/TEST-*.xml" ]; then
        local unit_tests=$(find target/surefire-reports -name "TEST-*.xml" -exec grep -l "testcase" {} \; | wc -l)
        print_message $GREEN "单元测试文件数: $unit_tests"
    fi
    
    # 集成测试结果
    if [ -f "target/failsafe-reports/TEST-*.xml" ]; then
        local integration_tests=$(find target/failsafe-reports -name "TEST-*.xml" -exec grep -l "testcase" {} \; | wc -l)
        print_message $GREEN "集成测试文件数: $integration_tests"
    fi
    
    # 覆盖率信息
    if [ -f "target/site/jacoco/index.html" ]; then
        print_message $GREEN "覆盖率报告: target/site/jacoco/index.html"
    fi
}

# 主函数
main() {
    case "${1:-help}" in
        "unit")
            run_unit_tests
            show_test_summary
            ;;
        "integration")
            run_integration_tests
            show_test_summary
            ;;
        "all")
            run_all_tests
            show_test_summary
            ;;
        "coverage")
            run_tests_with_coverage
            show_test_summary
            ;;
        "coverage-check")
            run_tests_with_coverage_check
            show_test_summary
            ;;
        "clean")
            clean_test_files
            ;;
        "help"|*)
            print_help
            ;;
    esac
}

# 检查是否在正确的目录
if [ ! -f "pom.xml" ]; then
    print_message $RED "错误: 请在项目根目录运行此脚本"
    exit 1
fi

# 运行主函数
main "$@"
