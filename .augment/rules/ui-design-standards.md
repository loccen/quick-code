---
type: "agent_requested"
description: "## 适用场景 本规范适用于速码网项目的所有UI设计和前端开发工作，包括用户端前端应用的界面设计和开发、管理后台前端应用的界面设计和开发、组件库的设计和实现、响应式布局的实现、用户体验优化、无障碍访问功能实现。AI在进行任何UI相关的设计、开发或优化时，必须严格遵循本规范。"
---
# 速码网UI设计规范指南



## 1. 设计系统概述

### 1.1 双前端应用设计原则
- **用户端应用**：简洁直观、一致性、响应式、高效性、可访问性
- **管理后台应用**：功能性优先、信息密度合理、数据可视化、操作便捷、权限清晰

### 1.2 设计系统变量
```scss
// 主色调
$primary-color: #1890ff;
$success-color: #52c41a;
$warning-color: #faad14;
$error-color: #ff4d4f;

// 中性色
$text-primary: #262626;
$text-secondary: #595959;
$border-color: #d9d9d9;
$background-color: #f5f5f5;

// 字体系统
$font-family-base: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
$font-size-base: 16px;
$font-size-sm: 14px;
$font-size-lg: 18px;
$line-height-base: 1.5;

// 间距系统
$spacing-unit: 8px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;
```

## 2. 组件库设计规范

### 2.1 Element Plus定制化
```scss
:root {
  --el-color-primary: #{$primary-color};
  --el-color-success: #{$success-color};
  --el-color-warning: #{$warning-color};
  --el-color-danger: #{$error-color};
  --el-font-size-base: #{$font-size-base};
  --el-border-radius-base: 6px;
}
```

### 2.2 通用组件设计

#### 按钮组件规范
```vue
<template>
  <el-button type="primary">主要按钮</el-button>
  <el-button type="default">次要按钮</el-button>
  <el-button type="danger">删除</el-button>
</template>

<style scoped lang="scss">
.el-button {
  margin-right: $spacing-sm;
  border-radius: 6px;
  height: 40px;
}
</style>
```

#### 表单组件规范
```vue
<template>
  <el-form :model="form" label-width="120px">
    <el-form-item label="用户名" prop="username">
      <el-input v-model="form.username" placeholder="请输入用户名" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="handleSubmit">提交</el-button>
    </el-form-item>
  </el-form>
</template>

<style scoped lang="scss">
.form-container {
  max-width: 600px;
  .el-form-item { margin-bottom: $spacing-lg; }
}
</style>
```

#### 卡片组件规范
```vue
<template>
  <el-card class="project-card" shadow="hover">
    <template #header>
      <h3 class="project-title">{{ project.title }}</h3>
      <el-tag>{{ project.status }}</el-tag>
    </template>
    <div class="card-content">
      <p class="project-description">{{ project.description }}</p>
      <div class="card-footer">
        <span class="price">¥{{ project.price }}</span>
        <el-button type="primary" size="small">购买</el-button>
      </div>
    </div>
  </el-card>
</template>

<style scoped lang="scss">
.project-card {
  height: 100%;
  &:hover { transform: translateY(-2px); }
  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
```

## 3. 响应式设计标准

### 3.1 断点系统
```scss
$breakpoints: (
  xs: 0, sm: 576px, md: 768px, lg: 992px, xl: 1200px
);

@mixin respond-to($breakpoint) {
  @media (min-width: map-get($breakpoints, $breakpoint)) {
    @content;
  }
}
```

### 3.2 栅格系统
```vue
<template>
  <el-row :gutter="24">
    <el-col :xs="24" :sm="12" :md="8" :lg="6">
      <ProjectCard :project="project" />
    </el-col>
  </el-row>
</template>
```

### 3.3 移动端适配
```scss
.mobile-optimized {
  .el-button { min-height: 44px; }
  .el-input { font-size: 16px; } // 防止iOS缩放
}
```

## 4. 用户体验设计原则

### 4.1 交互反馈
```vue
<template>
  <el-button type="primary" :loading="isSubmitting">
    {{ isSubmitting ? '提交中...' : '提交' }}
  </el-button>
  <el-skeleton v-if="isLoading" :rows="5" animated />
  <el-empty v-else-if="!projects.length" description="暂无项目" />
</template>
```

### 4.2 错误处理
```vue
<template>
  <el-form :model="form" :rules="rules">
    <el-form-item label="邮箱" prop="email" :error="emailError">
      <el-input v-model="form.email" />
    </el-form-item>
  </el-form>
  <el-alert v-if="globalError" :title="globalError" type="error" />
</template>

<script setup lang="ts">
const showError = (message: string) => ElMessage.error(message)
</script>
```

### 4.3 操作确认
```vue
<template>
  <el-popconfirm title="确定要删除吗？" @confirm="handleDelete">
    <template #reference>
      <el-button type="danger">删除</el-button>
    </template>
  </el-popconfirm>
</template>
```

## 5. 无障碍访问要求

### 5.1 语义化HTML
```vue
<template>
  <main role="main">
    <header>
      <h1>项目市场</h1>
      <nav aria-label="面包屑导航">
        <el-breadcrumb>
          <el-breadcrumb-item to="/">首页</el-breadcrumb-item>
        </el-breadcrumb>
      </nav>
    </header>
    <section aria-label="搜索区域">
      <el-input v-model="searchQuery" aria-label="搜索项目" />
    </section>
  </main>
</template>
```

### 5.2 键盘导航
```vue
<script setup lang="ts">
const handleKeydown = (event: KeyboardEvent) => {
  switch (event.key) {
    case 'Enter': handleActivate(); break
    case 'Escape': handleClose(); break
  }
}
</script>
```

### 5.3 颜色对比度
```scss
.high-contrast {
  color: $text-primary; // 确保4.5:1对比度
  a { color: $primary-color; }
}
```

## 6. 性能优化

### 6.1 图片优化
```vue
<template>
  <img :src="project.imageUrl" :alt="project.title" loading="lazy" />
</template>
```

### 6.2 懒加载
```vue
<template>
  <el-virtual-list :data="projects" :height="600" :item-size="120">
    <template #default="{ item }">
      <ProjectCard :project="item" />
    </template>
  </el-virtual-list>
</template>
```
