# 速码网系统架构设计文档

## 1. 引言

### 1.1 目的
本文档描述了速码网系统的整体架构设计，包括系统架构、技术选型、模块设计、接口设计和部署方案，为开发团队提供技术实施指导。

### 1.2 范围
本文档涵盖速码网平台的完整技术架构，包括双前端应用、后端服务、数据库设计、部署架构等所有技术组件。

### 1.3 术语定义
- **双前端架构**：用户端前端应用和管理后台前端应用分离的架构模式
- **容器化**：使用Docker技术将应用程序及其依赖打包为容器
- **微服务**：将单一应用程序分解为多个小型、独立服务的架构模式

## 2. 系统概述

### 2.1 系统目标
- 构建高可用、高性能的源码交易平台
- 实现Docker化项目的标准化管理和部署
- 提供完整的积分经济体系和交易功能
- 支持大规模用户并发访问和项目管理

### 2.2 系统架构
速码网采用双前端+微服务后端的分布式架构，支持水平扩展和高可用部署。

### 2.3 技术栈

#### 前端技术栈
- **框架**：Vue 3 + Composition API
- **语言**：TypeScript
- **构建工具**：Vite
- **状态管理**：Pinia
- **路由管理**：Vue Router 4
- **UI组件库**：Element Plus
- **样式方案**：SCSS + CSS Modules

#### 后端技术栈
- **框架**：Spring Boot 3
- **语言**：Java 17
- **安全框架**：Spring Security 6
- **数据访问**：Spring Data JPA + MyBatis Plus
- **缓存**：Redis 7.0
- **消息队列**：RabbitMQ
- **API文档**：SpringDoc OpenAPI 3

#### 数据存储
- **关系数据库**：MySQL 8.0
- **缓存数据库**：Redis 7.0
- **对象存储**：MinIO
- **搜索引擎**：Elasticsearch 8.0（可选）

## 3. 系统设计

### 3.1 架构设计

#### 3.1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    用户端前端应用 (Vue3)                      │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │  项目市场   │ │  我的项目   │ │  积分管理   │ │ 消息中心 │ │
│  │  浏览搜索   │ │  上传管理   │ │  充值提现   │ │ 通知推送 │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ HTTPS/WebSocket
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      API网关 (Nginx)                        │
│              负载均衡 + SSL终止 + 限流控制                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Spring Boot 3 微服务集群                   │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │  用户服务   │ │  项目服务   │ │  交易服务   │ │ 部署服务 │ │
│  │ 认证授权    │ │ 上传审核    │ │ 支付分账    │ │ 容器管理 │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │  积分服务   │ │  消息服务   │ │  文件服务   │ │ 监控服务 │ │
│  │ 充值提现    │ │ 通知推送    │ │ 存储管理    │ │ 日志收集 │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据存储层                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │   MySQL     │ │    Redis    │ │   MinIO     │ │RabbitMQ │ │
│  │  主数据库   │ │   缓存层    │ │  对象存储   │ │ 消息队列 │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   管理后台前端应用 (Vue3)                    │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │  用户管理   │ │  内容审核   │ │  财务管理   │ │ 系统监控 │ │
│  │  权限控制   │ │  项目审核   │ │  分账统计   │ │ 性能分析 │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
└─────────────────────────────────────────────────────────────┘
```

#### 3.1.2 微服务架构设计

**服务划分原则：**
- 按业务领域划分服务边界
- 每个服务独立部署和扩展
- 服务间通过API进行通信
- 共享数据通过事件同步

**核心微服务：**

1. **用户服务 (User Service)**
   - 用户注册、登录、认证
   - 用户资料管理
   - 权限控制和授权

2. **项目服务 (Project Service)**
   - 项目上传和管理
   - 项目搜索和浏览
   - 项目分类和标签

3. **交易服务 (Trade Service)**
   - 项目购买和支付
   - 订单管理
   - 分账处理

4. **部署服务 (Deploy Service)**
   - Docker容器管理
   - 演示站部署
   - 一键部署功能

5. **积分服务 (Point Service)**
   - 积分充值和消费
   - 积分提现
   - 积分历史记录

### 3.2 模块设计

#### 3.2.1 用户端前端模块

**核心模块结构：**
```
src/
├── components/              # 组件库
│   ├── common/             # 通用组件
│   │   ├── Header.vue      # 页面头部
│   │   ├── Footer.vue      # 页面底部
│   │   ├── Loading.vue     # 加载组件
│   │   └── Modal.vue       # 模态框组件
│   └── business/           # 业务组件
│       ├── ProjectCard.vue # 项目卡片
│       ├── SearchBar.vue   # 搜索栏
│       └── PaymentForm.vue # 支付表单
├── views/                  # 页面组件
│   ├── market/             # 项目市场
│   │   ├── MarketHome.vue  # 市场首页
│   │   ├── ProjectList.vue # 项目列表
│   │   └── ProjectDetail.vue # 项目详情
│   ├── project/            # 项目管理
│   │   ├── MyProjects.vue  # 我的项目
│   │   ├── UploadProject.vue # 上传项目
│   │   └── ProjectEdit.vue # 编辑项目
│   ├── profile/            # 个人中心
│   │   ├── UserProfile.vue # 个人资料
│   │   ├── PointsManage.vue # 积分管理
│   │   └── OrderHistory.vue # 订单历史
│   └── deploy/             # 部署管理
│       ├── DemoSite.vue    # 演示站
│       └── DeployGuide.vue # 部署指南
├── stores/                 # 状态管理
│   ├── user.ts            # 用户状态
│   ├── project.ts         # 项目状态
│   ├── trade.ts           # 交易状态
│   └── deploy.ts          # 部署状态
├── services/              # API服务
│   ├── api.ts             # API基础配置
│   ├── userApi.ts         # 用户API
│   ├── projectApi.ts      # 项目API
│   ├── tradeApi.ts        # 交易API
│   └── deployApi.ts       # 部署API
└── utils/                 # 工具函数
    ├── request.ts         # HTTP请求封装
    ├── auth.ts            # 认证工具
    ├── format.ts          # 格式化工具
    └── validation.ts      # 验证工具
```

#### 3.2.2 管理后台前端模块

**核心模块结构：**
```
src/
├── components/              # 管理组件
│   ├── layout/             # 布局组件
│   │   ├── AdminLayout.vue # 管理布局
│   │   ├── Sidebar.vue     # 侧边栏
│   │   └── Breadcrumb.vue  # 面包屑
│   └── charts/             # 图表组件
│       ├── LineChart.vue   # 折线图
│       ├── BarChart.vue    # 柱状图
│       └── PieChart.vue    # 饼图
├── views/                  # 管理页面
│   ├── dashboard/          # 仪表板
│   │   └── Dashboard.vue   # 总览页面
│   ├── users/              # 用户管理
│   │   ├── UserList.vue    # 用户列表
│   │   └── UserDetail.vue  # 用户详情
│   ├── content/            # 内容管理
│   │   ├── ProjectReview.vue # 项目审核
│   │   └── ContentManage.vue # 内容管理
│   ├── finance/            # 财务管理
│   │   ├── TradeStats.vue  # 交易统计
│   │   └── PointsManage.vue # 积分管理
│   └── system/             # 系统管理
│       ├── SystemConfig.vue # 系统配置
│       └── LogMonitor.vue  # 日志监控
└── shared/                 # 共享资源
    ├── components/         # 共享组件
    ├── utils/              # 共享工具
    └── types/              # 共享类型
```

### 3.3 接口设计

#### 3.3.1 RESTful API设计规范

**API基础规范：**
- 使用HTTP动词表示操作：GET、POST、PUT、DELETE
- 使用名词表示资源：/users、/projects、/orders
- 使用HTTP状态码表示结果：200、201、400、401、404、500
- 统一的响应格式：`{code, message, data, timestamp}`

**核心API接口：**

```yaml
# 用户相关API
GET    /api/users/profile          # 获取用户资料
PUT    /api/users/profile          # 更新用户资料
POST   /api/auth/login             # 用户登录
POST   /api/auth/register          # 用户注册
POST   /api/auth/logout            # 用户登出

# 项目相关API
GET    /api/projects               # 获取项目列表
GET    /api/projects/{id}          # 获取项目详情
POST   /api/projects               # 上传项目
PUT    /api/projects/{id}          # 更新项目
DELETE /api/projects/{id}          # 删除项目
GET    /api/projects/search        # 搜索项目

# 交易相关API
POST   /api/orders                 # 创建订单
GET    /api/orders/{id}            # 获取订单详情
POST   /api/payments               # 处理支付
GET    /api/trades/history         # 交易历史

# 部署相关API
POST   /api/deploy/demo            # 创建演示站
GET    /api/deploy/status/{id}     # 获取部署状态
DELETE /api/deploy/demo/{id}       # 删除演示站
POST   /api/deploy/download        # 下载部署包

# 积分相关API
GET    /api/points/balance         # 获取积分余额
POST   /api/points/recharge        # 积分充值
POST   /api/points/withdraw        # 积分提现
GET    /api/points/history         # 积分历史
```

#### 3.3.2 WebSocket接口设计

**实时通信接口：**
```yaml
# 消息通知
/ws/notifications              # 实时消息推送
/ws/deploy/status             # 部署状态更新
/ws/system/alerts             # 系统告警通知
```

### 3.4 数据设计

#### 3.4.1 核心数据模型

**用户表 (users)**
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(255),
    points DECIMAL(10,2) DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**项目表 (projects)**
```sql
CREATE TABLE projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    tech_stack JSON,
    price DECIMAL(8,2) NOT NULL,
    docker_image VARCHAR(255),
    source_url VARCHAR(255),
    demo_url VARCHAR(255),
    status TINYINT DEFAULT 0,
    downloads INT DEFAULT 0,
    rating DECIMAL(3,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**订单表 (orders)**
```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    amount DECIMAL(8,2) NOT NULL,
    status TINYINT DEFAULT 0,
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (project_id) REFERENCES projects(id)
);
```

## 4. 详细设计

### 4.1 核心算法

#### 4.1.1 项目推荐算法
基于用户行为和项目特征的协同过滤推荐算法：
- 用户行为权重：浏览(1) < 收藏(3) < 购买(5)
- 项目特征匹配：技术栈相似度、价格区间、评分等级
- 实时更新推荐结果，提高推荐准确性

#### 4.1.2 积分计算算法
动态积分奖励机制：
- 基础上传奖励：100积分
- Docker化奖励：额外100积分（双倍）
- 销售分成：70%给卖家，30%给平台
- 质量奖励：根据评分和下载量动态调整

### 4.2 数据结构

#### 4.2.1 缓存数据结构
```redis
# 用户会话缓存
user:session:{token} -> {user_id, permissions, expire_time}

# 项目热度缓存
project:hot:list -> [project_id1, project_id2, ...]

# 搜索结果缓存
search:{keyword}:{filters} -> {results, total, expire_time}
```

### 4.3 错误处理

#### 4.3.1 统一错误码设计
```java
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),
    
    // 业务错误码
    USER_NOT_FOUND(1001, "用户不存在"),
    PROJECT_NOT_FOUND(1002, "项目不存在"),
    INSUFFICIENT_POINTS(1003, "积分不足"),
    DEPLOY_FAILED(1004, "部署失败");
}
```

### 4.4 安全设计

#### 4.4.1 认证授权机制
- **JWT Token认证**：无状态认证，支持分布式部署
- **RBAC权限控制**：基于角色的访问控制
- **API限流**：防止恶意请求和DDoS攻击
- **数据加密**：敏感数据AES-256加密存储

#### 4.4.2 容器安全
- **镜像安全扫描**：使用Trivy等工具扫描漏洞
- **运行时安全**：限制容器权限和资源访问
- **网络隔离**：使用Kubernetes网络策略
- **镜像签名**：确保镜像来源可信

## 5. 部署设计

### 5.1 部署架构

#### 5.1.1 容器化部署
```yaml
# docker-compose.yml
version: '3.8'
services:
  user-frontend:
    build: ./user-frontend
    ports:
      - "3000:80"
    
  admin-frontend:
    build: ./admin-frontend
    ports:
      - "3001:80"
    
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=password
    
  redis:
    image: redis:7.0
    
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
```

### 5.2 环境配置

#### 5.2.1 开发环境
- **容器化开发**：使用VSCode Dev Containers
- **热重载**：前端Vite热重载，后端Spring Boot DevTools
- **数据库**：本地MySQL容器
- **调试工具**：集成开发调试工具

#### 5.2.2 生产环境
- **Kubernetes集群**：支持自动扩缩容
- **负载均衡**：Nginx Ingress Controller
- **服务发现**：Kubernetes Service
- **配置管理**：ConfigMap和Secret

### 5.3 监控方案

#### 5.3.1 应用监控
- **Prometheus**：指标收集和存储
- **Grafana**：可视化监控面板
- **Jaeger**：分布式链路追踪
- **ELK Stack**：日志收集和分析

#### 5.3.2 关键指标
- **系统指标**：CPU、内存、磁盘、网络
- **应用指标**：响应时间、错误率、吞吐量
- **业务指标**：用户活跃度、交易量、转化率

---

*本架构设计文档将根据项目进展和技术演进持续更新和优化。*
