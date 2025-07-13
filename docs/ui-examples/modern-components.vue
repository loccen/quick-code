<!--
  速码网现代化组件示例
  包含按钮、卡片、表单、导航等核心组件的实现示例
-->

<template>
  <div class="modern-components-showcase">
    <!-- 现代化按钮组件示例 -->
    <section class="component-section">
      <h3>现代化按钮组件</h3>

      <!-- 主要按钮 - 使用渐变和彩色阴影 -->
      <el-button type="primary" class="btn-modern btn-primary" @click="handleRipple">
        <i class="fas fa-rocket"></i>
        主要操作
      </el-button>

      <!-- 次要按钮 - 使用毛玻璃效果 -->
      <el-button class="btn-modern btn-glass">
        次要操作
      </el-button>

      <!-- 危险按钮 - 使用错误色阴影 -->
      <el-button type="danger" class="btn-modern btn-danger">
        删除操作
      </el-button>
    </section>

    <!-- 现代化表单组件示例 -->
    <section class="component-section">
      <h3>现代化表单组件</h3>

      <el-form :model="form" label-width="120px" class="modern-form">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            class="glass-input"
          />
        </el-form-item>

        <!-- 浮动标签输入框 -->
        <div class="floating-label">
          <el-input
            v-model="form.email"
            placeholder=" "
            class="floating-input"
          />
          <label class="floating-label-text">邮箱地址</label>
        </div>

        <el-form-item>
          <el-button type="primary" class="btn-modern btn-primary" @click="handleSubmit">
            提交
          </el-button>
        </el-form-item>
      </el-form>
    </section>

    <!-- 现代化卡片组件示例 -->
    <section class="component-section">
      <h3>现代化卡片组件</h3>

      <div class="project-card modern-card" @mousemove="handleMouseMove" @mouseleave="handleMouseLeave">
        <div class="card-image">
          <div class="project-placeholder">
            <i class="fab fa-vue"></i>
          </div>
          <div class="card-badges">
            <span class="badge badge-docker">
              <i class="fab fa-docker"></i>
              Docker
            </span>
            <span class="badge badge-hot">热门</span>
          </div>
        </div>

        <div class="card-content">
          <h3 class="card-title">Vue3 管理后台模板</h3>
          <p class="card-description">基于Vue3 + TypeScript + Element Plus的现代化管理后台模板</p>

          <div class="card-tags">
            <span class="tag">Vue3</span>
            <span class="tag">TypeScript</span>
            <span class="tag">Element Plus</span>
          </div>

          <div class="card-footer">
            <div class="card-price">
              <i class="fas fa-coins"></i>
              <span>299 积分</span>
            </div>
            <div class="card-actions">
              <el-button size="small" class="btn-modern btn-glass">
                <i class="fas fa-play"></i>
                演示
              </el-button>
              <el-button type="primary" size="small" class="btn-modern btn-primary">
                购买
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 现代化导航组件示例 -->
    <section class="component-section">
      <h3>现代化导航组件</h3>

      <header class="modern-header" :class="{ 'scrolled': isScrolled, 'hidden': isHidden }">
        <nav class="navbar">
          <div class="container">
            <div class="nav-brand">
              <div class="logo">
                <i class="fas fa-code"></i>
                <span class="brand-name">速码网</span>
              </div>
            </div>

            <div class="nav-menu">
              <ul class="nav-links">
                <li><a href="#" class="nav-link active">首页</a></li>
                <li><a href="#" class="nav-link">项目市场</a></li>
                <li><a href="#" class="nav-link">上传项目</a></li>
                <li><a href="#" class="nav-link">我的项目</a></li>
              </ul>
            </div>

            <div class="nav-search">
              <div class="search-box">
                <el-input
                  v-model="searchQuery"
                  placeholder="搜索项目、技术栈..."
                  class="glass-search"
                >
                  <template #suffix>
                    <i class="fas fa-search"></i>
                  </template>
                </el-input>
              </div>
            </div>

            <div class="nav-user">
              <div class="user-actions">
                <el-button class="btn-modern btn-glass">登录</el-button>
                <el-button type="primary" class="btn-modern btn-primary">注册</el-button>
              </div>
            </div>
          </div>
        </nav>
      </header>
    </section>

    <!-- 现代化反馈组件示例 -->
    <section class="component-section">
      <h3>现代化反馈组件</h3>

      <!-- 加载状态 -->
      <el-button
        type="primary"
        :loading="isSubmitting"
        class="btn-modern btn-primary"
        @click="handleRipple"
      >
        <template v-if="!isSubmitting">
          <i class="fas fa-rocket"></i>
          提交
        </template>
        <template v-else>
          提交中...
        </template>
      </el-button>

      <!-- 骨架屏 -->
      <div v-if="isLoading" class="skeleton-container">
        <el-skeleton :rows="5" animated class="modern-skeleton" />
      </div>

      <!-- 空状态 -->
      <el-empty
        v-else-if="!projects.length"
        description="暂无项目"
        class="modern-empty"
      >
        <template #image>
          <div class="empty-icon">
            <i class="fas fa-code"></i>
          </div>
        </template>
        <template #description>
          <p class="empty-text">还没有项目，快去上传第一个项目吧！</p>
        </template>
        <el-button type="primary" class="btn-modern btn-primary">
          上传项目
        </el-button>
      </el-empty>
    </section>

    <!-- 现代化确认对话框示例 -->
    <section class="component-section">
      <h3>现代化确认对话框</h3>

      <el-dialog
        v-model="showConfirm"
        title="确认删除"
        width="400px"
        class="modern-dialog"
        :show-close="false"
      >
        <div class="confirm-content">
          <div class="confirm-icon">
            <i class="fas fa-exclamation-triangle"></i>
          </div>
          <h3 class="confirm-title">确定要删除这个项目吗？</h3>
          <p class="confirm-description">
            删除后将无法恢复，请谨慎操作。
          </p>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button class="btn-modern btn-glass" @click="showConfirm = false">
              取消
            </el-button>
            <el-button
              type="danger"
              class="btn-modern btn-danger"
              @click="handleConfirm"
              :loading="isDeleting"
            >
              确认删除
            </el-button>
          </div>
        </template>
      </el-dialog>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useCardTilt, useRipple, useSmartNavigation } from '@/composables/ui';
import { ref } from 'vue';

// 组合式函数
const { handleRipple } = useRipple();
const { handleMouseMove, handleMouseLeave } = useCardTilt();
const { isScrolled, isHidden } = useSmartNavigation();

// 响应式数据
const form = ref({
  username: '',
  email: ''
});

const searchQuery = ref('');
const isSubmitting = ref(false);
const isLoading = ref(false);
const isDeleting = ref(false);
const showConfirm = ref(false);
const projects = ref([]);

// 方法
const handleSubmit = () => {
  console.log('提交表单', form.value);
};

const handleConfirm = () => {
  console.log('确认删除');
  showConfirm.value = false;
};
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.modern-components-showcase {
  padding: $spacing-xl;
  max-width: 1200px;
  margin: 0 auto;
}

.component-section {
  margin-bottom: $spacing-3xl;

  h3 {
    font-size: $font-size-xl;
    font-weight: 600;
    margin-bottom: $spacing-lg;
    color: $text-primary;
  }
}

// 引用其他样式文件中的具体实现
// 详细样式实现请参考：
// - docs/ui-examples/design-tokens.scss (设计Token和混入)
// - docs/ui-examples/responsive-patterns.scss (响应式和无障碍样式)
// - docs/ui-examples/interaction-functions.ts (交互动画函数)
</style>
