# 速码网后端测试Makefile
# 提供便捷的测试运行命令

.PHONY: help test test-unit test-integration test-all test-coverage test-coverage-check clean-test

# 默认目标
help:
	@echo "速码网后端测试命令"
	@echo ""
	@echo "可用命令:"
	@echo "  make test              - 运行所有测试"
	@echo "  make test-unit         - 运行单元测试"
	@echo "  make test-integration  - 运行集成测试"
	@echo "  make test-coverage     - 运行测试并生成覆盖率报告"
	@echo "  make test-coverage-check - 运行测试并检查覆盖率阈值"
	@echo "  make test-quick        - 运行快速测试（跳过慢速测试）"
	@echo "  make clean-test        - 清理测试相关文件"
	@echo "  make test-watch        - 监视文件变化并自动运行测试"
	@echo ""

# 运行所有测试
test:
	@echo "运行所有测试..."
	mvn clean verify

# 运行单元测试
test-unit:
	@echo "运行单元测试..."
	mvn clean test -Punit-tests

# 运行集成测试
test-integration:
	@echo "运行集成测试..."
	mvn clean verify -Pintegration-tests

# 运行所有测试（别名）
test-all: test

# 运行测试并生成覆盖率报告
test-coverage:
	@echo "运行测试并生成覆盖率报告..."
	mvn clean verify jacoco:report -Pcoverage
	@echo "覆盖率报告生成完成: target/site/jacoco/index.html"

# 运行测试并检查覆盖率阈值
test-coverage-check:
	@echo "运行测试并检查覆盖率阈值..."
	mvn clean verify jacoco:report jacoco:check -Pcoverage

# 运行快速测试
test-quick:
	@echo "运行快速测试..."
	mvn clean test -Pquick-tests

# 清理测试相关文件
clean-test:
	@echo "清理测试相关文件..."
	mvn clean
	rm -rf target/test-logs
	rm -f logs/test*.log

# 监视文件变化并自动运行测试（需要安装entr）
test-watch:
	@echo "监视文件变化并自动运行测试..."
	@if command -v entr >/dev/null 2>&1; then \
		find src -name "*.java" | entr -c make test-unit; \
	else \
		echo "错误: 需要安装 entr 工具"; \
		echo "Ubuntu/Debian: sudo apt-get install entr"; \
		echo "macOS: brew install entr"; \
	fi

# 运行特定测试类
test-class:
	@if [ -z "$(CLASS)" ]; then \
		echo "用法: make test-class CLASS=TestClassName"; \
		exit 1; \
	fi
	@echo "运行测试类: $(CLASS)"
	./mvnw test -Dtest=$(CLASS)

# 运行特定测试方法
test-method:
	@if [ -z "$(CLASS)" ] || [ -z "$(METHOD)" ]; then \
		echo "用法: make test-method CLASS=TestClassName METHOD=testMethodName"; \
		exit 1; \
	fi
	@echo "运行测试方法: $(CLASS)#$(METHOD)"
	./mvnw test -Dtest=$(CLASS)#$(METHOD)

# 生成测试报告
test-report:
	@echo "生成测试报告..."
	./mvnw surefire-report:report failsafe-report:report
	@echo "测试报告生成完成:"
	@echo "  单元测试报告: target/site/surefire-report.html"
	@echo "  集成测试报告: target/site/failsafe-report.html"

# 检查测试覆盖率
coverage-check:
	@echo "检查测试覆盖率..."
	./mvnw jacoco:check
	@echo "覆盖率检查完成"

# 打开覆盖率报告
coverage-open:
	@if [ -f "target/site/jacoco/index.html" ]; then \
		if command -v open >/dev/null 2>&1; then \
			open target/site/jacoco/index.html; \
		elif command -v xdg-open >/dev/null 2>&1; then \
			xdg-open target/site/jacoco/index.html; \
		else \
			echo "覆盖率报告位置: target/site/jacoco/index.html"; \
		fi; \
	else \
		echo "覆盖率报告不存在，请先运行 make test-coverage"; \
	fi

# 运行性能测试（如果有的话）
test-performance:
	@echo "运行性能测试..."
	./mvnw test -Dtest="**/*PerformanceTest"

# 运行安全测试（如果有的话）
test-security:
	@echo "运行安全测试..."
	./mvnw test -Dtest="**/*SecurityTest"

# 验证代码质量
verify:
	@echo "验证代码质量..."
	./mvnw clean verify

# 运行所有检查（测试 + 代码质量）
check-all: test-coverage-check verify
	@echo "所有检查完成"

# 持续集成测试
ci-test:
	@echo "运行CI测试..."
	./mvnw clean verify -Pcoverage -Dmaven.test.failure.ignore=false

# 显示测试统计信息
test-stats:
	@echo "测试统计信息:"
	@if [ -d "target/surefire-reports" ]; then \
		echo "单元测试文件数: $$(find target/surefire-reports -name "TEST-*.xml" | wc -l)"; \
	fi
	@if [ -d "target/failsafe-reports" ]; then \
		echo "集成测试文件数: $$(find target/failsafe-reports -name "TEST-*.xml" | wc -l)"; \
	fi
	@if [ -f "target/site/jacoco/index.html" ]; then \
		echo "覆盖率报告: 已生成"; \
	else \
		echo "覆盖率报告: 未生成"; \
	fi
