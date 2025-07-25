#!/bin/bash

# 速码网M3模块测试文件生成脚本
# 用于生成项目上传功能的各种测试文件
# 版本: 1.0.0
# 创建日期: 2024-07-25

set -e  # 遇到错误立即退出

echo "=== 速码网M3模块测试文件生成器 ==="
echo "版本: 1.0.0"
echo "用途: 为项目上传功能生成各种测试文件"
echo ""

# 检查依赖命令
check_dependencies() {
    echo "🔍 检查依赖命令..."
    
    local missing_deps=()
    
    if ! command -v zip &> /dev/null; then
        missing_deps+=("zip")
    fi
    
    if ! command -v dd &> /dev/null; then
        missing_deps+=("dd")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        echo "❌ 缺少必要的命令: ${missing_deps[*]}"
        echo "请安装缺少的命令后重试"
        exit 1
    fi
    
    echo "✅ 依赖检查通过"
}

# 创建目录结构
create_directories() {
    echo ""
    echo "📁 创建测试文件目录结构..."
    
    mkdir -p test-files/{normal,boundary,security,invalid,images}
    mkdir -p test-files/normal/{vue3-project,springboot-api,react-app}
    mkdir -p test-files/security/{malicious-project,sensitive-info}
    
    echo "✅ 目录结构创建完成"
}

# 生成Vue3项目示例
generate_vue3_project() {
    echo ""
    echo "🚀 生成Vue3项目示例..."
    
    local project_dir="test-files/normal/vue3-project"
    
    # 创建项目结构
    mkdir -p "$project_dir"/{src/{components,views,stores,utils,api},public,docs,tests}
    
    # package.json
    cat > "$project_dir/package.json" << 'EOF'
{
  "name": "vue3-modern-project",
  "version": "1.0.0",
  "description": "Vue3现代化前端项目 - 展示最新前端开发技术栈",
  "main": "src/main.js",
  "type": "module",
  "scripts": {
    "dev": "vite --host",
    "build": "vite build",
    "preview": "vite preview",
    "test:unit": "vitest",
    "test:e2e": "playwright test",
    "lint": "eslint src --ext .vue,.js,.ts --fix",
    "format": "prettier --write src/"
  },
  "dependencies": {
    "vue": "^3.3.4",
    "vue-router": "^4.2.4",
    "pinia": "^2.1.6",
    "element-plus": "^2.3.8",
    "axios": "^1.4.0",
    "@element-plus/icons-vue": "^2.1.0",
    "vue-i18n": "^9.2.2"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.2.3",
    "vite": "^4.4.5",
    "vitest": "^0.34.1",
    "playwright": "^1.36.2",
    "eslint": "^8.45.0",
    "eslint-plugin-vue": "^9.15.1",
    "prettier": "^3.0.0",
    "@typescript-eslint/parser": "^6.2.1",
    "typescript": "^5.1.6",
    "sass": "^1.64.1"
  },
  "keywords": [
    "vue3",
    "vite",
    "typescript",
    "element-plus",
    "pinia",
    "前端框架",
    "现代化开发"
  ],
  "author": "速码网开发团队",
  "license": "MIT",
  "repository": {
    "type": "git",
    "url": "https://github.com/quickcode/vue3-modern-project.git"
  },
  "bugs": {
    "url": "https://github.com/quickcode/vue3-modern-project/issues"
  },
  "homepage": "https://vue3-demo.quickcode.com"
}
EOF

    # main.js
    cat > "$project_dir/src/main.js" << 'EOF'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createI18n } from 'vue-i18n'

import App from './App.vue'
import router from './router'
import './styles/main.scss'

// 创建应用实例
const app = createApp(App)

// 状态管理
const pinia = createPinia()
app.use(pinia)

// 路由
app.use(router)

// UI组件库
app.use(ElementPlus)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 国际化
const i18n = createI18n({
  locale: 'zh-CN',
  fallbackLocale: 'en',
  messages: {
    'zh-CN': {
      hello: '你好，世界！'
    },
    'en': {
      hello: 'Hello, World!'
    }
  }
})
app.use(i18n)

// 挂载应用
app.mount('#app')
EOF

    # App.vue
    cat > "$project_dir/src/App.vue" << 'EOF'
<template>
  <div id="app">
    <el-config-provider :locale="locale">
      <router-view />
    </el-config-provider>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const locale = ref(zhCn)
</script>

<style lang="scss">
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  min-height: 100vh;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
EOF

    # README.md
    cat > "$project_dir/README.md" << 'EOF'
# Vue3 现代化前端项目

这是一个基于Vue3的现代化前端项目，集成了最新的前端开发技术栈和最佳实践。

## 🌟 项目特色

- ⚡️ **极速开发**: 基于Vite构建，HMR热更新
- 🎯 **TypeScript**: 完整的类型支持，开发更安全
- 🎨 **现代UI**: Element Plus组件库，美观易用
- 📱 **响应式**: 完美适配桌面端和移动端
- 🌍 **国际化**: 内置i18n支持，多语言切换
- 🔧 **工程化**: ESLint + Prettier代码规范
- 🧪 **测试**: Vitest单元测试 + Playwright E2E测试

## 🚀 技术栈

### 核心框架
- **Vue 3.3+** - 渐进式JavaScript框架
- **Vite 4.4+** - 下一代前端构建工具
- **TypeScript 5.1+** - JavaScript的超集

### 状态管理与路由
- **Vue Router 4.2+** - 官方路由管理器
- **Pinia 2.1+** - 新一代状态管理库

### UI与样式
- **Element Plus 2.3+** - 基于Vue3的组件库
- **Sass** - CSS预处理器
- **响应式设计** - 移动端适配

### 开发工具
- **ESLint** - 代码质量检查
- **Prettier** - 代码格式化
- **Vitest** - 单元测试框架
- **Playwright** - E2E测试框架

## 📦 快速开始

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0 或 yarn >= 1.22.0

### 安装依赖
```bash
# 使用npm
npm install

# 或使用yarn
yarn install
```

### 开发环境
```bash
# 启动开发服务器
npm run dev

# 访问 http://localhost:5173
```

### 生产构建
```bash
# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

### 代码质量
```bash
# 代码检查
npm run lint

# 代码格式化
npm run format

# 运行测试
npm run test:unit
npm run test:e2e
```

## 📁 项目结构

```
src/
├── api/                 # API接口定义
├── assets/              # 静态资源
├── components/          # 公共组件
│   ├── common/          # 通用组件
│   └── business/        # 业务组件
├── composables/         # 组合式函数
├── router/              # 路由配置
├── stores/              # Pinia状态管理
├── styles/              # 全局样式
├── utils/               # 工具函数
├── views/               # 页面组件
├── App.vue              # 根组件
└── main.js              # 入口文件
```

## 🔧 开发指南

### 组件开发
推荐使用Vue3的Composition API：

```vue
<template>
  <div class="my-component">
    <h1>{{ title }}</h1>
    <p>计数: {{ count }}</p>
    <el-button @click="increment">增加</el-button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 响应式数据
const count = ref(0)
const title = ref('我的组件')

// 计算属性
const doubleCount = computed(() => count.value * 2)

// 方法
const increment = () => {
  count.value++
}
</script>

<style lang="scss" scoped>
.my-component {
  padding: 20px;
  
  h1 {
    color: #409eff;
    margin-bottom: 16px;
  }
}
</style>
```

### 状态管理
使用Pinia进行状态管理：

```javascript
// stores/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    name: '',
    email: '',
    isLoggedIn: false
  }),
  
  getters: {
    displayName: (state) => state.name || '游客'
  },
  
  actions: {
    login(userData) {
      this.name = userData.name
      this.email = userData.email
      this.isLoggedIn = true
    },
    
    logout() {
      this.name = ''
      this.email = ''
      this.isLoggedIn = false
    }
  }
})
```

### API调用
统一的API调用方式：

```javascript
// api/user.js
import request from '@/utils/request'

export const userApi = {
  // 获取用户信息
  getUserInfo() {
    return request.get('/user/info')
  },
  
  // 更新用户信息
  updateUserInfo(data) {
    return request.put('/user/info', data)
  }
}
```

## 🌍 部署指南

### Docker部署
```dockerfile
# Dockerfile
FROM node:18-alpine as build-stage

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 环境变量配置
```bash
# .env.production
VITE_API_BASE_URL=https://api.example.com
VITE_APP_TITLE=Vue3现代化项目
```

## 📄 许可证

本项目采用 [MIT](LICENSE) 许可证。

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 📞 联系我们

- 官网: https://quickcode.com
- 邮箱: dev@quickcode.com
- QQ群: 123456789

---

**Made with ❤️ by 速码网开发团队**
EOF

    # vite.config.js
    cat > "$project_dir/vite.config.js" << 'EOF'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    open: true,
    cors: true
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]'
      }
    }
  }
})
EOF

    # Dockerfile
    cat > "$project_dir/Dockerfile" << 'EOF'
# 多阶段构建
FROM node:18-alpine as build-stage

# 设置工作目录
WORKDIR /app

# 复制package文件
COPY package*.json ./

# 安装依赖
RUN npm ci --only=production

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 生产阶段
FROM nginx:stable-alpine as production-stage

# 复制构建结果
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
EOF

    # LICENSE
    cat > "$project_dir/LICENSE" << 'EOF'
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

    # 打包项目
    cd test-files/normal
    zip -r vue3-project.zip vue3-project/ > /dev/null 2>&1
    cd ../..
    
    echo "✅ Vue3项目示例生成完成"
}

# 生成Spring Boot项目示例
generate_springboot_project() {
    echo ""
    echo "☕ 生成Spring Boot项目示例..."
    
    local project_dir="test-files/normal/springboot-api"
    
    # 创建项目结构
    mkdir -p "$project_dir"/src/{main/{java/com/quickcode/demo/{controller,service,repository,entity,dto,config,common},resources},test/java}
    
    # pom.xml
    cat > "$project_dir/pom.xml" << 'EOF'
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
    <description>Spring Boot RESTful API 演示项目 - 现代化后端开发框架</description>

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
        <mybatis-plus.version>3.5.3.1</mybatis-plus.version>
        <knife4j.version>4.1.0</knife4j.version>
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

        <!-- Spring Boot Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL Connector -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Knife4j API文档 -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
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

        <!-- Spring Security Test -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
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

    # README.md
    cat > "$project_dir/README.md" << 'EOF'
# Spring Boot Demo API

这是一个基于Spring Boot 3的现代化RESTful API项目，展示了企业级后端开发的最佳实践。

## 🚀 技术栈

### 核心框架
- **Spring Boot 3.1.2** - 企业级应用开发框架
- **Spring Security 6** - 安全认证和授权
- **Spring Data JPA** - 数据访问层
- **MyBatis Plus 3.5.3** - 增强的MyBatis框架

### 数据库
- **MySQL 8.0** - 关系型数据库
- **Redis 7.0** - 缓存和会话存储

### 开发工具
- **Java 17** - LTS版本Java
- **Maven 3.8+** - 项目构建工具
- **Knife4j 4.1.0** - API文档生成
- **Lombok** - 简化Java代码

## 📦 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+

### 数据库配置
```sql
-- 创建数据库
CREATE DATABASE quickcode_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'demo_user'@'localhost' IDENTIFIED BY 'demo_password';
GRANT ALL PRIVILEGES ON quickcode_demo.* TO 'demo_user'@'localhost';
FLUSH PRIVILEGES;
```

### 应用配置
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/quickcode_demo
    username: demo_user
    password: demo_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  redis:
    host: localhost
    port: 6379
    database: 0
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### 运行应用
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run

# 访问API文档
# http://localhost:8080/doc.html
```

## 📁 项目结构

```
src/main/java/com/quickcode/demo/
├── controller/          # 控制器层
├── service/            # 业务逻辑层
├── repository/         # 数据访问层
├── entity/             # 实体类
├── dto/                # 数据传输对象
├── config/             # 配置类
├── common/             # 公共组件
└── DemoApplication.java # 启动类
```

## 🔧 开发指南

### RESTful API设计
```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关API")
public class UserController {
    
    @GetMapping
    @Operation(summary = "获取用户列表")
    public Result<PageResponse<User>> getUsers(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        // 实现逻辑
    }
    
    @PostMapping
    @Operation(summary = "创建用户")
    public Result<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        // 实现逻辑
    }
}
```

### 数据访问层
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.status = ?1")
    Page<User> findByStatus(UserStatus status, Pageable pageable);
}
```

### 业务逻辑层
```java
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User createUser(CreateUserRequest request) {
        // 业务逻辑实现
    }
}
```

## 🔒 安全配置

### JWT认证
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

## 📊 监控和日志

### 应用监控
- Spring Boot Actuator
- Micrometer指标收集
- 健康检查端点

### 日志配置
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

## 🚀 部署指南

### Docker部署
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 环境变量
```bash
# 生产环境配置
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://prod-db:3306/quickcode
SPRING_REDIS_HOST=prod-redis
```

## 📄 许可证

本项目采用 [MIT](LICENSE) 许可证。

---

**Made with ☕ by 速码网开发团队**
EOF

    # 打包项目
    cd test-files/normal
    zip -r springboot-api.zip springboot-api/ > /dev/null 2>&1
    cd ../..
    
    echo "✅ Spring Boot项目示例生成完成"
}

# 生成边界条件测试文件
generate_boundary_files() {
    echo ""
    echo "⚖️ 生成边界条件测试文件..."
    
    # 生成不同大小的文件
    echo "  📏 生成大小测试文件..."
    dd if=/dev/zero of=test-files/boundary/file-1mb.zip bs=1M count=1 2>/dev/null
    dd if=/dev/zero of=test-files/boundary/file-10mb.zip bs=1M count=10 2>/dev/null
    dd if=/dev/zero of=test-files/boundary/file-50mb.zip bs=1M count=50 2>/dev/null
    dd if=/dev/zero of=test-files/boundary/file-99mb.zip bs=1M count=99 2>/dev/null
    dd if=/dev/zero of=test-files/boundary/file-101mb.zip bs=1M count=101 2>/dev/null
    dd if=/dev/zero of=test-files/boundary/file-500mb.zip bs=1M count=500 2>/dev/null
    
    # 生成空文件
    touch test-files/boundary/empty-file.zip
    
    # 生成长文件名
    local long_name=$(printf 'a%.0s' {1..200})
    touch "test-files/boundary/${long_name}.zip"
    
    echo "✅ 边界条件测试文件生成完成"
}

# 生成安全测试文件
generate_security_files() {
    echo ""
    echo "🔒 生成安全测试文件..."
    
    local malicious_dir="test-files/security/malicious-project"
    mkdir -p "$malicious_dir"/{scripts,config,bin}
    
    # 创建可执行脚本文件
    cat > "$malicious_dir/scripts/install.sh" << 'EOF'
#!/bin/bash
echo "这是一个测试安装脚本"
echo "正在安装依赖包..."
npm install
echo "安装完成"
EOF
    chmod +x "$malicious_dir/scripts/install.sh"
    
    # 创建Windows批处理文件
    cat > "$malicious_dir/scripts/setup.bat" << 'EOF'
@echo off
echo 这是一个Windows安装脚本
echo 正在配置环境...
npm install
pause
EOF
    
    # 创建包含敏感信息的配置文件
    cat > "$malicious_dir/config/.env" << 'EOF'
# 数据库配置
DATABASE_HOST=localhost
DATABASE_PORT=3306
DATABASE_NAME=production_db
DATABASE_USERNAME=admin
DATABASE_PASSWORD=super_secret_password_123

# API密钥配置
API_KEY=sk-1234567890abcdefghijklmnopqrstuvwxyz
SECRET_KEY=your-top-secret-key-here
ENCRYPTION_KEY=aes256-encryption-key-32-chars

# JWT配置
JWT_SECRET=jwt-super-secret-signing-key
JWT_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VJTUt9Us8cKB
UmNvWwdGMkFEMFvKjn6w4flA1VdHBhBHMpVr+pAqfBOgFqq+ooLw7w6OiC5InP5u
-----END PRIVATE KEY-----

# 第三方服务密钥
STRIPE_SECRET_KEY=sk_test_51234567890abcdefghijklmnop
STRIPE_PUBLISHABLE_KEY=pk_test_51234567890abcdefghijklmnop
ALIPAY_APP_ID=2021001234567890
ALIPAY_PRIVATE_KEY=MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VJTUt9Us8cKB
WECHAT_APP_ID=wx1234567890abcdef
WECHAT_APP_SECRET=1234567890abcdef1234567890abcdef

# 邮件服务配置
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=admin@company.com
SMTP_PASSWORD=email_password_very_secret
SENDGRID_API_KEY=SG.1234567890abcdefghijklmnopqrstuvwxyz

# 云服务配置
AWS_ACCESS_KEY_ID=AKIA1234567890ABCDEF
AWS_SECRET_ACCESS_KEY=1234567890abcdefghijklmnopqrstuvwxyz123456
AZURE_CLIENT_ID=12345678-1234-1234-1234-123456789012
AZURE_CLIENT_SECRET=client-secret-value-here
GOOGLE_CLOUD_PROJECT_ID=my-project-123456
GOOGLE_CLOUD_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VJTUt9Us8cKB\n-----END PRIVATE KEY-----

# 其他敏感信息
ADMIN_TOKEN=admin-super-secret-token-123456
MASTER_PASSWORD=master-password-do-not-share
ENCRYPTION_SALT=random-salt-for-encryption-12345
SESSION_SECRET=session-secret-key-for-cookies
EOF
    
    # 创建可疑的二进制文件
    echo -e "\x4d\x5a\x90\x00" > "$malicious_dir/bin/suspicious.exe"  # PE文件头
    echo -e "\x7f\x45\x4c\x46" > "$malicious_dir/bin/suspicious"      # ELF文件头
    
    # 打包恶意内容
    cd test-files/security
    zip -r malicious-project.zip malicious-project/ > /dev/null 2>&1
    cd ../..
    
    echo "✅ 安全测试文件生成完成"
}

# 生成无效文件类型
generate_invalid_files() {
    echo ""
    echo "❌ 生成无效文件类型..."
    
    # 创建伪装的压缩文件
    echo "这不是一个真正的ZIP文件，只是伪装的" > test-files/invalid/fake.zip
    echo "这不是一个真正的RAR文件，只是伪装的" > test-files/invalid/fake.rar
    echo "这不是一个真正的7Z文件，只是伪装的" > test-files/invalid/fake.7z
    
    # 创建不支持的文件类型
    echo "这是一个普通的文本文件内容" > test-files/invalid/document.txt
    echo "这是一个Word文档的模拟内容" > test-files/invalid/document.doc
    echo "这是一个PDF文档的模拟内容" > test-files/invalid/document.pdf
    echo "这是一个Excel文档的模拟内容" > test-files/invalid/spreadsheet.xlsx
    
    # 创建可执行文件
    echo "#!/bin/bash" > test-files/invalid/script.sh
    echo "echo 'This is a shell script'" >> test-files/invalid/script.sh
    chmod +x test-files/invalid/script.sh
    
    echo "@echo off" > test-files/invalid/script.bat
    echo "echo This is a batch file" >> test-files/invalid/script.bat
    
    # 创建其他危险文件类型
    echo "console.log('This is JavaScript');" > test-files/invalid/script.js
    echo "print('This is Python')" > test-files/invalid/script.py
    echo "<?php echo 'This is PHP'; ?>" > test-files/invalid/script.php
    
    echo "✅ 无效文件类型生成完成"
}

# 生成图片测试文件
generate_image_files() {
    echo ""
    echo "🖼️ 生成图片测试文件..."
    
    # 创建SVG封面图片
    cat > test-files/images/project-cover.svg << 'EOF'
<svg width="800" height="600" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="bgGradient" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" style="stop-color:#667eea;stop-opacity:1" />
      <stop offset="100%" style="stop-color:#764ba2;stop-opacity:1" />
    </linearGradient>
    <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="#000" flood-opacity="0.3"/>
    </filter>
  </defs>
  
  <!-- 背景 -->
  <rect width="100%" height="100%" fill="url(#bgGradient)"/>
  
  <!-- 装饰圆圈 -->
  <circle cx="150" cy="150" r="80" fill="rgba(255,255,255,0.1)"/>
  <circle cx="650" cy="450" r="60" fill="rgba(255,255,255,0.1)"/>
  
  <!-- 主标题 -->
  <text x="400" y="220" font-family="Arial, sans-serif" font-size="48" 
        text-anchor="middle" fill="white" font-weight="bold" filter="url(#shadow)">
    速码网项目
  </text>
  
  <!-- 副标题 -->
  <text x="400" y="280" font-family="Arial, sans-serif" font-size="24" 
        text-anchor="middle" fill="white" opacity="0.9">
    Vue3 + Spring Boot 全栈开发
  </text>
  
  <!-- 描述文字 -->
  <text x="400" y="340" font-family="Arial, sans-serif" font-size="18" 
        text-anchor="middle" fill="white" opacity="0.8">
    现代化技术栈 | 企业级解决方案
  </text>
  
  <!-- 尺寸信息 -->
  <text x="400" y="420" font-family="Arial, sans-serif" font-size="16" 
        text-anchor="middle" fill="white" opacity="0.7">
    推荐尺寸: 800 × 600 像素
  </text>
  
  <!-- 底部装饰 -->
  <rect x="300" y="480" width="200" height="4" fill="rgba(255,255,255,0.6)" rx="2"/>
  
  <!-- 版权信息 -->
  <text x="400" y="550" font-family="Arial, sans-serif" font-size="12" 
        text-anchor="middle" fill="white" opacity="0.6">
    © 2024 速码网 QuickCode.com
  </text>
</svg>
EOF
    
    # 创建不同大小的模拟图片文件
    dd if=/dev/zero of=test-files/images/small-cover-100kb.jpg bs=1K count=100 2>/dev/null
    dd if=/dev/zero of=test-files/images/normal-cover-1mb.png bs=1M count=1 2>/dev/null
    dd if=/dev/zero of=test-files/images/large-cover-5mb.jpg bs=1M count=5 2>/dev/null
    dd if=/dev/zero of=test-files/images/oversized-cover-15mb.png bs=1M count=15 2>/dev/null
    
    echo "✅ 图片测试文件生成完成"
}

# 显示文件统计
show_file_statistics() {
    echo ""
    echo "📊 测试文件统计信息:"
    echo "===================="
    
    echo ""
    echo "📁 目录结构:"
    tree test-files/ 2>/dev/null || find test-files -type d | sed 's|[^/]*/|  |g'
    
    echo ""
    echo "📋 文件列表:"
    printf "%-12s %-8s %-40s\n" "类型" "大小" "文件名"
    echo "--------------------------------------------------------"
    
    find test-files -type f -exec ls -lh {} \; | while read -r line; do
        size=$(echo "$line" | awk '{print $5}')
        file=$(echo "$line" | awk '{print $9}')
        filename=$(basename "$file")
        
        if [[ "$file" == *"/normal/"* ]]; then
            type="正常文件"
        elif [[ "$file" == *"/boundary/"* ]]; then
            type="边界测试"
        elif [[ "$file" == *"/security/"* ]]; then
            type="安全测试"
        elif [[ "$file" == *"/invalid/"* ]]; then
            type="无效类型"
        elif [[ "$file" == *"/images/"* ]]; then
            type="图片文件"
        else
            type="其他"
        fi
        
        printf "%-12s %-8s %-40s\n" "$type" "$size" "$filename"
    done
    
    echo ""
    echo "📈 统计汇总:"
    echo "- 正常测试文件: $(find test-files/normal -type f | wc -l) 个"
    echo "- 边界测试文件: $(find test-files/boundary -type f | wc -l) 个"
    echo "- 安全测试文件: $(find test-files/security -type f | wc -l) 个"
    echo "- 无效类型文件: $(find test-files/invalid -type f | wc -l) 个"
    echo "- 图片测试文件: $(find test-files/images -type f | wc -l) 个"
    echo "- 总计文件数量: $(find test-files -type f | wc -l) 个"
    
    local total_size=$(du -sh test-files/ | cut -f1)
    echo "- 总计文件大小: $total_size"
}

# 主函数
main() {
    echo "开始生成测试文件..."
    
    # 检查依赖
    check_dependencies
    
    # 创建目录
    create_directories
    
    # 生成各类测试文件
    generate_vue3_project
    generate_springboot_project
    generate_boundary_files
    generate_security_files
    generate_invalid_files
    generate_image_files
    
    # 显示统计信息
    show_file_statistics
    
    echo ""
    echo "🎉 所有测试文件生成完成！"
    echo ""
    echo "📖 使用说明:"
    echo "1. 测试文件已保存在 test-files/ 目录中"
    echo "2. 请参考 docs/testing/M3-Phase1-Manual-Testing-Guide.md 进行测试"
    echo "3. 测试完成后可以运行以下命令清理文件:"
    echo "   rm -rf test-files/"
    echo ""
    echo "✅ 准备就绪，可以开始测试了！"
}

# 执行主函数
main "$@"
