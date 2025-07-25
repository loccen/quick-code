# 速码网M3模块第一阶段手动测试指南

## 文档信息
- **版本**: 1.0.0
- **创建日期**: 2024-07-25
- **更新日期**: 2024-07-25
- **测试范围**: FR-020 项目上传功能
- **测试类型**: 手动功能测试

## 目录
1. [测试环境要求](#1-测试环境要求)
2. [测试文件准备](#2-测试文件准备)
3. [测试用例清单](#3-测试用例清单)
4. [预期结果说明](#4-预期结果说明)
5. [测试执行流程](#5-测试执行流程)
6. [问题记录模板](#6-问题记录模板)

## 1. 测试环境要求

### 1.1 服务启动确认

#### 后端服务启动
```bash
# 进入后端目录
cd backend

# 启动Spring Boot服务
mvn spring-boot:run

# 验证服务状态
curl http://localhost:8080/actuator/health
# 预期返回: {"status":"UP"}
```

#### 前端服务启动
```bash
# 进入前端目录
cd user-frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev

# 验证服务状态
# 浏览器访问: http://localhost:3000
```

#### 数据库服务确认
```bash
# 检查MySQL服务状态
mysql -u root -p -e "SELECT 1"

# 检查Redis服务状态
redis-cli ping
# 预期返回: PONG
```

### 1.2 数据库初始化

#### 项目分类数据准备
```sql
-- 连接到quickcode数据库
USE quickcode;

-- 检查项目分类数据
SELECT id, name, description FROM project_categories WHERE is_active = 1;

-- 如果没有数据，执行以下插入语句
INSERT INTO project_categories (name, description, sort_order, is_active, created_at, updated_at) VALUES
('Web开发', 'Web应用开发项目，包括前端、后端、全栈项目', 1, 1, NOW(), NOW()),
('移动开发', '移动应用开发项目，包括iOS、Android、跨平台应用', 2, 1, NOW(), NOW()),
('桌面应用', '桌面应用开发项目，包括Windows、macOS、Linux应用', 3, 1, NOW(), NOW()),
('游戏开发', '游戏开发项目，包括2D、3D、手游、网游', 4, 1, NOW(), NOW()),
('工具软件', '实用工具软件，包括开发工具、系统工具、办公工具', 5, 1, NOW(), NOW()),
('数据分析', '数据分析和机器学习项目', 6, 1, NOW(), NOW()),
('区块链', '区块链和加密货币相关项目', 7, 1, NOW(), NOW()),
('物联网', '物联网和嵌入式系统项目', 8, 1, NOW(), NOW());
```

#### 测试用户准备
```sql
-- 检查测试用户（如果没有请先注册）
SELECT id, username, email, status FROM users WHERE username = 'testuser';

-- 确认用户状态为激活状态（status = 1）
```

### 1.3 用户登录状态
- 测试前需要登录测试用户账号
- 确认用户具有项目上传权限
- 验证用户积分余额（用于付费项目测试）

## 2. 测试文件准备

### 2.1 测试文件生成脚本

创建测试文件生成脚本 `scripts/generate-test-files.sh`：

```bash
#!/bin/bash

echo "=== 速码网M3模块测试文件生成 ==="

# 创建测试文件目录结构
mkdir -p test-files/{normal,boundary,security,invalid,images}

echo "1. 生成正常测试文件..."

# 创建Vue3项目示例
mkdir -p test-files/normal/vue3-project/{src/components,public,docs}

# package.json
cat > test-files/normal/vue3-project/package.json << 'EOF'
{
  "name": "vue3-demo-project",
  "version": "1.0.0",
  "description": "Vue3演示项目 - 现代化前端开发框架",
  "main": "src/main.js",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "test": "vitest",
    "lint": "eslint src --ext .vue,.js,.ts"
  },
  "dependencies": {
    "vue": "^3.3.4",
    "vue-router": "^4.2.4",
    "pinia": "^2.1.6",
    "element-plus": "^2.3.8",
    "axios": "^1.4.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.2.3",
    "vite": "^4.4.5",
    "vitest": "^0.34.1",
    "eslint": "^8.45.0",
    "@typescript-eslint/parser": "^6.2.1",
    "typescript": "^5.1.6"
  },
  "keywords": ["vue3", "vite", "typescript", "element-plus", "前端框架"],
  "author": "速码网测试用户",
  "license": "MIT"
}
EOF

# main.js
cat > test-files/normal/vue3-project/src/main.js << 'EOF'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
EOF

# App.vue
cat > test-files/normal/vue3-project/src/App.vue << 'EOF'
<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>Vue3 演示项目</h1>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
// Vue3 Composition API 示例
</script>

<style scoped>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
</style>
EOF

# README.md
cat > test-files/normal/vue3-project/README.md << 'EOF'
# Vue3 演示项目

这是一个基于Vue3的现代化前端项目，展示了最新的前端开发技术和最佳实践。

## 🚀 技术栈

- **框架**: Vue 3.3+ (Composition API)
- **构建工具**: Vite 4.4+
- **路由**: Vue Router 4.2+
- **状态管理**: Pinia 2.1+
- **UI组件库**: Element Plus 2.3+
- **HTTP客户端**: Axios 1.4+
- **开发语言**: TypeScript 5.1+
- **代码规范**: ESLint + Prettier
- **测试框架**: Vitest

## 📦 安装和运行

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖
```bash
npm install
```

### 开发环境运行
```bash
npm run dev
```

### 生产环境构建
```bash
npm run build
```

### 运行测试
```bash
npm run test
```

## 🌟 功能特性

- ✅ 响应式设计，支持多端适配
- ✅ 组件化开发，代码复用性高
- ✅ TypeScript支持，类型安全
- ✅ 路由懒加载，性能优化
- ✅ 状态管理，数据流清晰
- ✅ 国际化支持
- ✅ 主题定制
- ✅ PWA支持

## 📁 项目结构

```
src/
├── components/          # 公共组件
├── views/              # 页面组件
├── router/             # 路由配置
├── stores/             # Pinia状态管理
├── utils/              # 工具函数
├── api/                # API接口
├── assets/             # 静态资源
└── styles/             # 样式文件
```

## 🔧 开发指南

### 代码规范
项目使用ESLint和Prettier进行代码规范检查，请在提交代码前运行：
```bash
npm run lint
```

### 组件开发
推荐使用Vue3的Composition API进行组件开发：
```vue
<script setup>
import { ref, computed, onMounted } from 'vue'

const count = ref(0)
const doubleCount = computed(() => count.value * 2)

onMounted(() => {
  console.log('组件已挂载')
})
</script>
```

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request 来改进这个项目。

## 📞 联系方式

- 作者：速码网开发团队
- 邮箱：dev@quickcode.com
- 官网：https://quickcode.com
EOF

# LICENSE文件
cat > test-files/normal/vue3-project/LICENSE << 'EOF'
MIT License

Copyright (c) 2024 速码网 (QuickCode)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
EOF

# Dockerfile
cat > test-files/normal/vue3-project/Dockerfile << 'EOF'
# 构建阶段
FROM node:18-alpine as build-stage

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

# 生产阶段
FROM nginx:stable-alpine as production-stage

COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
EOF

# 打包Vue3项目
cd test-files/normal
zip -r vue3-project.zip vue3-project/
cd ../..

echo "2. 生成Spring Boot项目示例..."

# 创建Spring Boot项目
mkdir -p test-files/normal/springboot-api/{src/main/java/com/quickcode/demo,src/main/resources,src/test/java}

# pom.xml
cat > test-files/normal/springboot-api/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.quickcode</groupId>
    <artifactId>springboot-demo-api</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>Spring Boot Demo API</name>
    <description>Spring Boot RESTful API 演示项目</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.2</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Spring Boot Web Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Spring Boot Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MySQL Connector -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Spring Boot Test -->
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
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
EOF

# 打包Spring Boot项目
cd test-files/normal
zip -r springboot-api.zip springboot-api/
cd ../..

echo "3. 生成边界条件测试文件..."

# 生成不同大小的文件
dd if=/dev/zero of=test-files/boundary/file-50mb.zip bs=1M count=50 2>/dev/null
dd if=/dev/zero of=test-files/boundary/file-99mb.zip bs=1M count=99 2>/dev/null
dd if=/dev/zero of=test-files/boundary/file-101mb.zip bs=1M count=101 2>/dev/null
dd if=/dev/zero of=test-files/boundary/file-500mb.zip bs=1M count=500 2>/dev/null

# 生成空文件
touch test-files/boundary/empty-file.zip

# 生成长文件名
touch "test-files/boundary/$(printf 'a%.0s' {1..200}).zip"

echo "4. 生成安全测试文件..."

# 创建包含可执行文件的项目
mkdir -p test-files/security/malicious-project/{scripts,config}

# 创建可执行脚本
cat > test-files/security/malicious-project/scripts/install.sh << 'EOF'
#!/bin/bash
echo "这是一个测试脚本"
echo "正在安装依赖..."
npm install
EOF
chmod +x test-files/security/malicious-project/scripts/install.sh

# 创建Windows批处理文件
cat > test-files/security/malicious-project/scripts/setup.bat << 'EOF'
@echo off
echo 这是一个Windows批处理文件
echo 正在设置环境...
pause
EOF

# 创建包含敏感信息的配置文件
cat > test-files/security/malicious-project/config/.env << 'EOF'
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=secret123456

# API密钥
API_KEY=sk-1234567890abcdefghijklmnopqrstuvwxyz
SECRET_KEY=your-secret-key-here

# JWT配置
JWT_SECRET=jwt-secret-token-12345
JWT_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VJTUt9Us8cKB
UmNvWwdGMkFEMFvKjn6w4flA1VdHBhBHMpVr+pAqfBOgFqq+ooLw7w6OiC5InP5u
-----END PRIVATE KEY-----

# 第三方服务
STRIPE_SECRET_KEY=sk_test_1234567890abcdef
ALIPAY_PRIVATE_KEY=MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VJTUt9Us8cKB
WECHAT_APP_SECRET=1234567890abcdef1234567890abcdef

# 邮件配置
SMTP_PASSWORD=email_password_123
SMTP_TOKEN=smtp_token_456789
EOF

# 打包恶意内容
cd test-files/security
zip -r malicious-project.zip malicious-project/
cd ../..

echo "5. 生成无效文件类型..."

# 创建伪装的压缩文件
echo "这不是一个真正的ZIP文件" > test-files/invalid/fake.zip
echo "这不是一个真正的RAR文件" > test-files/invalid/fake.rar

# 创建不支持的文件类型
echo "普通文本文件内容" > test-files/invalid/document.txt
echo "Word文档内容" > test-files/invalid/document.doc
echo "PDF文档内容" > test-files/invalid/document.pdf

# 创建可执行文件
echo "#!/bin/bash\necho 'test'" > test-files/invalid/script.sh
chmod +x test-files/invalid/script.sh

echo "6. 生成图片测试文件..."

# 创建SVG封面图片
cat > test-files/images/project-cover.svg << 'EOF'
<svg width="800" height="600" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" style="stop-color:#667eea;stop-opacity:1" />
      <stop offset="100%" style="stop-color:#764ba2;stop-opacity:1" />
    </linearGradient>
  </defs>
  <rect width="100%" height="100%" fill="url(#bg)"/>
  <text x="400" y="250" font-family="Arial, sans-serif" font-size="48" 
        text-anchor="middle" fill="white" font-weight="bold">
    速码网项目
  </text>
  <text x="400" y="320" font-family="Arial, sans-serif" font-size="24" 
        text-anchor="middle" fill="white" opacity="0.9">
    Vue3 + Spring Boot 全栈项目
  </text>
  <text x="400" y="380" font-family="Arial, sans-serif" font-size="18" 
        text-anchor="middle" fill="white" opacity="0.7">
    800 × 600 像素 | 现代化UI设计
  </text>
</svg>
EOF

# 创建大尺寸图片文件（模拟）
dd if=/dev/zero of=test-files/images/large-cover-5mb.jpg bs=1M count=5 2>/dev/null
dd if=/dev/zero of=test-files/images/oversized-cover-15mb.png bs=1M count=15 2>/dev/null

echo "=== 测试文件生成完成 ==="
echo ""
echo "📁 文件目录结构："
echo "test-files/"
echo "├── normal/           # 正常测试文件"
echo "│   ├── vue3-project.zip"
echo "│   └── springboot-api.zip"
echo "├── boundary/         # 边界条件测试文件"
echo "│   ├── file-50mb.zip"
echo "│   ├── file-99mb.zip"
echo "│   ├── file-101mb.zip"
echo "│   └── empty-file.zip"
echo "├── security/         # 安全测试文件"
echo "│   └── malicious-project.zip"
echo "├── invalid/          # 无效文件类型"
echo "│   ├── fake.zip"
echo "│   ├── document.txt"
echo "│   └── script.sh"
echo "└── images/           # 图片测试文件"
echo "    ├── project-cover.svg"
echo "    ├── large-cover-5mb.jpg"
echo "    └── oversized-cover-15mb.png"
echo ""
echo "📊 文件统计："
find test-files -type f -exec ls -lh {} \; | awk '{print $5 "\t" $9}' | sort -k2
echo ""
echo "✅ 所有测试文件已生成完成，可以开始测试！"
```

### 2.2 执行文件生成

```bash
# 创建脚本目录
mkdir -p scripts

# 复制上述脚本内容到文件
# 给脚本执行权限
chmod +x scripts/generate-test-files.sh

# 执行脚本生成测试文件
./scripts/generate-test-files.sh
```

### 2.3 测试文件说明

| 文件类型 | 文件名 | 大小 | 用途 |
|---------|--------|------|------|
| 正常项目 | vue3-project.zip | ~50KB | 测试Vue3项目上传和结构分析 |
| 正常项目 | springboot-api.zip | ~20KB | 测试Spring Boot项目上传 |
| 边界测试 | file-99mb.zip | 99MB | 测试接近大小限制的文件 |
| 边界测试 | file-101mb.zip | 101MB | 测试超过大小限制的文件 |
| 安全测试 | malicious-project.zip | ~5KB | 测试包含可执行文件和敏感信息的项目 |
| 无效类型 | fake.zip | ~30B | 测试伪装的压缩文件 |
| 图片文件 | project-cover.svg | ~2KB | 测试项目封面图片上传 |
| 图片文件 | large-cover-5mb.jpg | 5MB | 测试大尺寸图片上传 |

## 3. 测试用例清单

### 3.1 文件上传安全检查测试

#### 3.1.1 文件类型验证测试

**测试目标**: 验证系统能正确识别和验证文件类型

| 用例ID | 测试描述 | 测试文件 | 操作步骤 | 预期结果 | 优先级 |
|--------|----------|----------|----------|----------|--------|
| TC-001 | 上传有效的ZIP文件 | vue3-project.zip | 1. 进入上传页面<br>2. 选择文件<br>3. 点击上传 | ✅ 上传成功，显示"文件安全检查通过" | P0 |
| TC-002 | 上传有效的Spring Boot项目 | springboot-api.zip | 同上 | ✅ 上传成功，显示项目结构分析结果 | P0 |
| TC-003 | 上传伪装的ZIP文件 | fake.zip | 同上 | ❌ 显示"文件类型验证失败" | P0 |
| TC-004 | 上传文本文件 | document.txt | 同上 | ❌ 显示"不支持的文件类型：txt" | P0 |
| TC-005 | 上传可执行文件 | script.sh | 同上 | ❌ 显示"危险的文件扩展名：sh" | P0 |

#### 3.1.2 文件大小限制测试

**测试目标**: 验证系统能正确处理不同大小的文件

| 用例ID | 测试描述 | 测试文件 | 操作步骤 | 预期结果 | 优先级 |
|--------|----------|----------|----------|----------|--------|
| TC-006 | 上传正常大小文件 | file-50mb.zip | 选择并上传文件 | ✅ 上传成功 | P1 |
| TC-007 | 上传接近限制大小文件 | file-99mb.zip | 选择并上传文件 | ✅ 上传成功，可能显示大文件警告 | P1 |
| TC-008 | 上传超过限制大小文件 | file-101mb.zip | 选择并上传文件 | ❌ 显示"文件大小 101MB 超过最大限制 100MB" | P0 |
| TC-009 | 上传空文件 | empty-file.zip | 选择并上传文件 | ❌ 显示"文件为空文件，无法上传" | P1 |
| TC-010 | 上传超大文件 | file-500mb.zip | 选择并上传文件 | ❌ 显示文件大小超限错误 | P1 |

#### 3.1.3 安全内容检查测试

**测试目标**: 验证系统能检测文件中的安全风险

| 用例ID | 测试描述 | 测试文件 | 操作步骤 | 预期结果 | 优先级 |
|--------|----------|----------|----------|----------|--------|
| TC-011 | 检测可执行文件内容 | malicious-project.zip | 上传包含.sh和.bat文件的压缩包 | ⚠️ 显示"检测到可执行文件内容"警告 | P0 |
| TC-012 | 检测敏感信息 | malicious-project.zip | 上传包含密码等敏感信息的文件 | ⚠️ 显示"检测到敏感信息"警告 | P0 |
| TC-013 | 风险等级评估 | malicious-project.zip | 查看安全检查结果 | 显示"高风险"或"中风险"等级 | P1 |

### 3.2 项目信息表单测试

#### 3.2.1 基本信息填写测试

**测试目标**: 验证项目基本信息表单的功能和验证

| 用例ID | 测试描述 | 输入数据 | 操作步骤 | 预期结果 | 优先级 |
|--------|----------|----------|----------|----------|--------|
| TC-014 | 填写有效项目标题 | "Vue3现代化前端项目" | 在标题字段输入文本 | ✅ 正常输入，显示字符计数 | P0 |
| TC-015 | 输入超长标题 | 101个字符的标题 | 输入超长文本 | ❌ 显示"标题长度不能超过100字符" | P1 |
| TC-016 | 填写项目描述 | 详细的项目描述文本 | 在描述字段输入多行文本 | ✅ 支持换行，显示字符计数 | P0 |
| TC-017 | 选择项目分类 | "Web开发" | 点击分类下拉框选择 | ✅ 显示所有可用分类，选择成功 | P0 |
| TC-018 | 输入演示地址 | "https://demo.example.com" | 输入有效URL | ✅ URL格式验证通过 | P1 |
| TC-019 | 输入无效URL | "invalid-url" | 输入无效URL格式 | ❌ 显示"请输入有效的URL地址" | P1 |

#### 3.2.2 技术栈选择器测试

**测试目标**: 验证技术栈选择器的交互和功能

| 用例ID | 测试描述 | 操作步骤 | 预期结果 | 优先级 |
|--------|----------|----------|----------|--------|
| TC-020 | 切换技术栈分类 | 1. 点击"前端技术"标签<br>2. 点击"后端技术"标签 | ✅ 显示对应分类的技术选项 | P0 |
| TC-021 | 选择技术栈标签 | 1. 点击"Vue.js"标签<br>2. 点击"Spring Boot"标签 | ✅ 标签变为选中状态，添加到已选列表 | P0 |
| TC-022 | 自定义技术栈输入 | 1. 在输入框输入"Nuxt.js"<br>2. 按回车键 | ✅ 自定义技术栈添加到已选列表 | P1 |
| TC-023 | 应用推荐技术栈组合 | 点击"Vue3 全栈"组合标签 | ✅ 一键添加Vue.js、TypeScript、Element Plus等 | P1 |
| TC-024 | 超过技术栈数量限制 | 选择超过20个技术栈 | ❌ 显示"最多只能选择20个技术栈" | P1 |
| TC-025 | 删除已选技术栈 | 点击已选技术栈的关闭按钮 | ✅ 技术栈从已选列表中移除 | P1 |
| TC-026 | 清空所有技术栈 | 点击"清空全部"按钮 | ✅ 所有已选技术栈被清空 | P2 |

#### 3.2.3 价格设置测试

**测试目标**: 验证价格设置组件的功能和计算

| 用例ID | 测试描述 | 操作步骤 | 预期结果 | 优先级 |
|--------|----------|----------|----------|--------|
| TC-027 | 选择免费项目 | 点击"免费项目"单选按钮 | ✅ 显示免费项目说明和优势列表 | P0 |
| TC-028 | 选择付费项目 | 点击"付费项目"单选按钮 | ✅ 显示价格输入框和相关设置 | P0 |
| TC-029 | 输入有效价格 | 在价格输入框输入"50" | ✅ 显示价格预览和收益计算 | P0 |
| TC-030 | 点击推荐价格 | 点击"进阶 (50积分)"标签 | ✅ 自动填入50积分价格 | P1 |
| TC-031 | 输入负数价格 | 输入"-10" | ❌ 显示"价格不能为负数" | P1 |
| TC-032 | 输入超大价格 | 输入"1000000" | ❌ 显示"价格超过最大限制" | P1 |
| TC-033 | 价格预览计算 | 输入价格"100" | ✅ 显示平台服务费10积分，收益90积分 | P1 |
| TC-034 | 人民币换算显示 | 输入价格"100" | ✅ 显示"约等于 ¥10.0" | P2 |

### 3.3 文件上传组件测试

#### 3.3.1 拖拽上传测试

**测试目标**: 验证拖拽上传功能的交互体验

| 用例ID | 测试描述 | 操作步骤 | 预期结果 | 优先级 |
|--------|----------|----------|----------|--------|
| TC-035 | 拖拽文件到上传区域 | 1. 选择vue3-project.zip文件<br>2. 拖拽到上传区域 | ✅ 区域高亮显示，显示"释放文件开始上传" | P1 |
| TC-036 | 拖拽多个文件 | 同时拖拽多个文件到上传区域 | ✅ 显示文件数量，支持批量上传 | P1 |
| TC-037 | 拖拽无效文件 | 拖拽.txt文件到上传区域 | ❌ 显示文件类型错误提示 | P1 |
| TC-038 | 拖拽离开上传区域 | 拖拽文件进入后移出上传区域 | ✅ 高亮状态取消，恢复正常样式 | P2 |

#### 3.3.2 上传进度测试

**测试目标**: 验证上传进度显示的准确性和用户体验

| 用例ID | 测试描述 | 操作步骤 | 预期结果 | 优先级 |
|--------|----------|----------|----------|--------|
| TC-039 | 单文件上传进度 | 上传vue3-project.zip文件 | ✅ 显示上传进度条和百分比 | P0 |
| TC-040 | 多文件上传进度 | 同时上传多个文件 | ✅ 显示当前文件名和整体进度 | P1 |
| TC-041 | 大文件上传进度 | 上传file-50mb.zip文件 | ✅ 显示上传速度和预计剩余时间 | P1 |
| TC-042 | 上传速度显示 | 观察上传过程中的速度显示 | ✅ 显示实时上传速度（MB/s或KB/s） | P2 |
| TC-043 | 上传完成状态 | 等待文件上传完成 | ✅ 进度条变为绿色，显示"上传完成" | P1 |

#### 3.3.3 文件管理测试

**测试目标**: 验证已上传文件的管理功能

| 用例ID | 测试描述 | 操作步骤 | 预期结果 | 优先级 |
|--------|----------|----------|----------|--------|
| TC-044 | 查看已上传文件列表 | 上传文件后查看文件列表 | ✅ 显示文件名、类型、大小、状态 | P0 |
| TC-045 | 删除已上传文件 | 点击文件的删除按钮 | ✅ 弹出确认对话框，确认后删除文件 | P0 |
| TC-046 | 取消删除操作 | 点击删除按钮后点击取消 | ✅ 取消删除，文件保留在列表中 | P1 |
| TC-047 | 文件状态显示 | 观察文件上传后的状态变化 | ✅ 显示"上传中"→"已上传"→"处理中"→"已处理" | P1 |

### 3.4 完整上传流程测试

#### 3.4.1 正常流程测试

**测试目标**: 验证完整的项目上传流程

| 用例ID | 测试描述 | 操作步骤 | 预期结果 | 优先级 |
|--------|----------|----------|----------|--------|
| TC-048 | 完整上传流程 | 1. 填写项目信息<br>2. 上传源码文件<br>3. 预览确认<br>4. 保存项目 | ✅ 流程顺畅，最终跳转到项目列表 | P0 |
| TC-049 | 步骤间数据保持 | 在各步骤间切换 | ✅ 已填写的信息在步骤间保持不丢失 | P0 |
| TC-050 | 项目创建成功 | 完成整个上传流程 | ✅ 显示"项目创建成功"，生成项目ID | P0 |

#### 3.4.2 步骤验证测试

**测试目标**: 验证步骤间的验证逻辑

| 用例ID | 测试描述 | 操作步骤 | 预期结果 | 优先级 |
|--------|----------|----------|----------|--------|
| TC-051 | 信息不完整时进入下一步 | 项目标题为空时点击"下一步" | ❌ 显示"请完成当前步骤的必填项" | P0 |
| TC-052 | 未上传文件时进入预览 | 未上传源码文件时点击"下一步" | ❌ 显示"请至少上传一个源码文件" | P0 |
| TC-053 | 返回上一步 | 在任意步骤点击"上一步" | ✅ 返回上一步，保留已填写信息 | P0 |
| TC-054 | 步骤指示器状态 | 观察页面顶部的步骤指示器 | ✅ 正确显示当前步骤和完成状态 | P1 |

## 4. 预期结果说明

### 4.1 正常流程预期行为

#### 4.1.1 项目信息填写阶段
- **表单验证**: 实时验证，错误提示清晰明确
- **技术栈选择**: 分类清晰，选择直观，支持自定义
- **价格设置**: 免费/付费切换流畅，价格预览准确
- **用户体验**: 界面响应迅速，操作流畅自然

#### 4.1.2 文件上传阶段
- **上传反馈**: 进度条平滑更新，速度显示准确
- **安全检查**: 检查结果及时反馈，风险提示明确
- **文件管理**: 已上传文件清晰展示，支持删除操作
- **错误处理**: 上传失败时提供明确的错误信息和解决建议

#### 4.1.3 预览确认阶段
- **信息展示**: 项目信息完整展示，格式美观
- **文件统计**: 文件类型和数量统计准确
- **操作反馈**: 保存按钮状态正确，加载状态明确
- **成功跳转**: 保存成功后跳转到正确页面

### 4.2 错误情况预期处理

#### 4.2.1 文件上传错误处理

```
错误类型: 文件类型不支持
错误信息: "文件 document.txt 类型不支持，支持的类型：zip, rar, 7z, tar.gz, tar"
处理方式: 阻止上传，显示错误提示，不影响其他文件上传
用户指导: 提示用户将文件打包为支持的压缩格式

错误类型: 文件大小超限
错误信息: "文件 large-file.zip 大小 101MB 超过最大限制 100MB"
处理方式: 阻止上传，显示错误提示，建议压缩或分割文件
用户指导: 提供文件压缩建议和分割上传方案

错误类型: 安全检查失败
错误信息: "文件安全检查失败: 检测到可执行文件内容, 检测到敏感信息"
处理方式: 显示警告信息，允许用户选择是否继续
用户指导: 说明安全风险，建议清理敏感信息后重新上传
```

#### 4.2.2 表单验证错误处理

```
错误类型: 必填字段为空
错误信息: "项目标题不能为空"
处理方式: 字段边框变红，显示错误提示，阻止提交
视觉反馈: 错误字段高亮显示，错误信息显示在字段下方

错误类型: 字符长度超限
错误信息: "标题长度不能超过100字符，当前105字符"
处理方式: 实时字符计数，超限时显示警告
视觉反馈: 字符计数器变红，显示超出字符数

错误类型: 格式验证失败
错误信息: "请输入有效的URL地址，格式如：https://example.com"
处理方式: 字段验证失败，显示格式要求
视觉反馈: 输入框边框变红，显示格式示例
```

### 4.3 安全检查预期结果

#### 4.3.1 通过条件
- ✅ 文件类型在白名单内（zip, rar, 7z, tar.gz, tar）
- ✅ 文件大小在限制内（≤500MB）
- ✅ 文件扩展名安全（不包含exe, bat, cmd等危险扩展名）
- ✅ MIME类型安全（不包含可执行文件MIME类型）
- ✅ 文件内容检查通过（无恶意文件特征）
- ✅ 敏感信息检查通过（无明显敏感信息泄露）

#### 4.3.2 拦截条件
- ❌ **危险文件扩展名**: exe, bat, cmd, scr, vbs, js, jar, com, pif, msi, dll
- ❌ **危险MIME类型**: application/x-executable, application/x-msdownload等
- ❌ **恶意文件特征**: PE可执行文件头(4D5A), ELF可执行文件头(7F454C46)
- ❌ **敏感信息关键词**: password, secret, private_key, api_key, token

#### 4.3.3 风险等级评估
- **低风险**: 无安全问题，正常项目文件
- **中风险**: 包含少量敏感信息或可疑内容，建议审查
- **高风险**: 包含可执行文件或大量敏感信息，强烈建议不上传
- **严重风险**: 检测到明确的恶意文件特征，禁止上传

## 5. 测试执行流程

### 5.1 测试前准备

#### 5.1.1 环境检查清单
- [ ] 后端服务正常启动（端口8080）
- [ ] 前端服务正常启动（端口3000）
- [ ] MySQL数据库连接正常
- [ ] Redis服务连接正常
- [ ] 测试文件已生成完成
- [ ] 测试用户已登录系统

#### 5.1.2 数据准备清单
- [ ] 项目分类数据已初始化
- [ ] 测试用户账号状态正常
- [ ] 用户积分余额充足（用于付费项目测试）
- [ ] 清理历史测试数据

### 5.2 测试执行顺序

#### 阶段一：基础功能测试（P0优先级）
1. **文件类型验证测试** (TC-001 ~ TC-005)
   - 测试时间：15分钟
   - 重点验证：文件类型识别和拦截机制

2. **文件大小限制测试** (TC-008)
   - 测试时间：10分钟
   - 重点验证：大小限制和错误提示

3. **安全内容检查测试** (TC-011 ~ TC-012)
   - 测试时间：15分钟
   - 重点验证：安全检查机制

4. **基本信息表单测试** (TC-014, TC-017)
   - 测试时间：10分钟
   - 重点验证：必填字段验证

5. **技术栈选择器测试** (TC-020 ~ TC-021)
   - 测试时间：10分钟
   - 重点验证：选择器基本功能

6. **价格设置测试** (TC-027 ~ TC-029)
   - 测试时间：10分钟
   - 重点验证：价格设置基本功能

7. **完整流程测试** (TC-048 ~ TC-050)
   - 测试时间：20分钟
   - 重点验证：端到端流程

8. **步骤验证测试** (TC-051 ~ TC-053)
   - 测试时间：15分钟
   - 重点验证：流程控制逻辑

**阶段一总计时间：105分钟**

#### 阶段二：增强功能测试（P1优先级）
1. **边界条件测试** (TC-006 ~ TC-010)
2. **表单验证测试** (TC-015 ~ TC-019)
3. **技术栈高级功能** (TC-022 ~ TC-026)
4. **价格设置高级功能** (TC-030 ~ TC-034)
5. **文件上传组件测试** (TC-035 ~ TC-047)

**阶段二总计时间：90分钟**

#### 阶段三：用户体验测试（P2优先级）
1. **拖拽交互测试**
2. **界面响应性测试**
3. **错误恢复测试**
4. **性能观察测试**

**阶段三总计时间：60分钟**

### 5.3 测试记录要求

#### 5.3.1 测试执行记录
每个测试用例需要记录：
- 执行时间
- 执行结果（通过/失败/阻塞）
- 实际结果描述
- 问题截图（如有）
- 备注说明

#### 5.3.2 问题记录要求
发现问题时需要记录：
- 问题标题和描述
- 重现步骤
- 预期结果 vs 实际结果
- 问题截图或录屏
- 浏览器和版本信息
- 严重程度评级

### 5.4 测试完成标准

#### 5.4.1 通过标准
- P0级别测试用例通过率 ≥ 95%
- P1级别测试用例通过率 ≥ 90%
- 无严重级别（Blocker/Critical）问题
- 核心流程功能完整可用

#### 5.4.2 验收标准
- 文件上传安全检查机制有效
- 项目信息表单验证完整
- 完整上传流程顺畅无阻
- 用户体验良好，错误提示清晰

## 6. 问题记录模板

### 6.1 问题报告模板

```markdown
## 问题报告 #[问题编号]

### 基本信息
- **发现时间**: 2024-07-25 14:30:00
- **测试用例**: TC-XXX
- **测试人员**: [测试人员姓名]
- **严重程度**: [Blocker/Critical/Major/Minor]
- **优先级**: [High/Medium/Low]

### 环境信息
- **浏览器**: Chrome 115.0.5790.110
- **操作系统**: macOS 13.4.1
- **前端版本**: user-frontend v1.0.0
- **后端版本**: backend v1.0.0

### 问题描述
[详细描述问题现象]

### 重现步骤
1. 步骤一
2. 步骤二
3. 步骤三

### 预期结果
[描述预期应该发生的情况]

### 实际结果
[描述实际发生的情况]

### 附件
- 截图: [附加截图文件]
- 录屏: [附加录屏文件]
- 日志: [相关日志信息]

### 临时解决方案
[如果有临时解决方案，请描述]

### 备注
[其他相关信息]
```

### 6.2 测试执行记录模板

```markdown
## 测试执行记录

### 测试会话信息
- **测试日期**: 2024-07-25
- **测试人员**: [测试人员姓名]
- **测试环境**: [环境描述]
- **测试版本**: [软件版本]

### 测试结果汇总
| 优先级 | 总数 | 通过 | 失败 | 阻塞 | 通过率 |
|--------|------|------|------|------|--------|
| P0     | 20   | 19   | 1    | 0    | 95%    |
| P1     | 25   | 22   | 2    | 1    | 88%    |
| P2     | 15   | 13   | 2    | 0    | 87%    |
| **总计** | **60** | **54** | **5** | **1** | **90%** |

### 详细测试结果
| 用例ID | 测试描述 | 结果 | 执行时间 | 备注 |
|--------|----------|------|----------|------|
| TC-001 | 上传有效ZIP文件 | ✅ 通过 | 14:30 | 正常 |
| TC-002 | 上传Spring Boot项目 | ✅ 通过 | 14:32 | 正常 |
| TC-003 | 上传伪装ZIP文件 | ❌ 失败 | 14:35 | 见问题#001 |

### 发现问题列表
1. **问题#001**: 伪装ZIP文件检测失效 [Critical]
2. **问题#002**: 大文件上传进度显示异常 [Major]
3. **问题#003**: 技术栈选择器样式问题 [Minor]

### 测试总结
- **整体评估**: [整体测试情况评估]
- **主要问题**: [主要问题总结]
- **建议**: [改进建议]
- **下一步**: [后续测试计划]
```

---

## 附录

### A. 测试环境配置参考

#### A.1 浏览器配置
- **推荐浏览器**: Chrome 115+, Firefox 115+, Safari 16+
- **必要插件**: Vue.js devtools（用于调试）
- **开发者工具**: 保持Network和Console面板开启

#### A.2 网络环境
- **带宽要求**: 上行带宽 ≥ 10Mbps（用于大文件上传测试）
- **延迟要求**: 延迟 ≤ 100ms
- **稳定性**: 测试期间保持网络稳定

#### A.3 系统资源
- **内存**: 可用内存 ≥ 4GB
- **磁盘**: 可用磁盘空间 ≥ 2GB（用于测试文件）
- **CPU**: 避免高CPU占用的其他程序

### B. 常见问题排查

#### B.1 服务启动问题
```bash
# 检查端口占用
lsof -i :8080  # 后端端口
lsof -i :3000  # 前端端口

# 检查服务状态
curl http://localhost:8080/actuator/health
curl http://localhost:3000
```

#### B.2 数据库连接问题
```bash
# 检查MySQL连接
mysql -u root -p -e "SELECT 1"

# 检查Redis连接
redis-cli ping
```

#### B.3 文件上传问题
- 检查文件权限
- 检查磁盘空间
- 检查网络连接
- 查看浏览器控制台错误
- 查看后端日志

### C. 测试数据清理

#### C.1 清理测试项目
```sql
-- 清理测试项目数据
DELETE FROM project_files WHERE project_id IN (
    SELECT id FROM projects WHERE title LIKE '%测试%' OR title LIKE '%test%'
);

DELETE FROM projects WHERE title LIKE '%测试%' OR title LIKE '%test%';
```

#### C.2 清理测试文件
```bash
# 清理上传的测试文件
rm -rf uploads/test-*
rm -rf uploads/extracted/test-*

# 清理生成的测试文件
rm -rf test-files/
```

---

**文档版本**: 1.0.0
**最后更新**: 2024-07-25
**维护人员**: 速码网开发团队
