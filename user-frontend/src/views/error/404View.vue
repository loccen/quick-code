<template>
  <div class="error-view">
    <div class="error-container">
      <ModernCard class="error-card" :glass="true" :hoverable="false">
        <div class="error-content">
          <!-- 错误图标 -->
          <div class="error-icon">
            <el-icon :size="120">
              <QuestionFilled />
            </el-icon>
          </div>

          <!-- 错误信息 -->
          <div class="error-info">
            <h1 class="error-code">404</h1>
            <h2 class="error-title">页面不存在</h2>
            <p class="error-description">
              抱歉，您访问的页面不存在或已被移除。
              <br>
              请检查URL是否正确，或返回首页继续浏览。
            </p>
          </div>

          <!-- 操作按钮 -->
          <div class="error-actions">
            <ModernButton type="primary" @click="goHome">
              <el-icon><HomeFilled /></el-icon>
              返回首页
            </ModernButton>
            <ModernButton @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回上页
            </ModernButton>
          </div>
        </div>
      </ModernCard>
    </div>

    <!-- 背景装饰 -->
    <div class="error-background">
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { QuestionFilled, HomeFilled, ArrowLeft } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import ModernCard from '@/components/ui/ModernCard.vue'
import ModernButton from '@/components/ui/ModernButton.vue'

const router = useRouter()
const appStore = useAppStore()

// 返回首页
const goHome = () => {
  router.push('/')
}

// 返回上一页
const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}

// 设置页面标题
appStore.setPageTitle('页面不存在')
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.error-view {
  min-height: 100vh;
  @include flex-center();
  position: relative;
  background: var(--gradient-hero);
  overflow: hidden;
}

.error-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 500px;
  padding: $spacing-lg;

  .error-card {
    padding: $spacing-3xl;
    text-align: center;
    @include shadow-layered-xl();
  }
}

.error-content {
  .error-icon {
    margin-bottom: $spacing-xl;
    color: var(--warning-color);
    @include animate-bounce-in();
  }

  .error-info {
    margin-bottom: $spacing-2xl;

    .error-code {
      font-size: 6rem;
      font-weight: $font-weight-bold;
      @include gradient-text();
      margin: 0 0 $spacing-md 0;
      line-height: 1;
    }

    .error-title {
      font-size: $font-size-2xl;
      font-weight: $font-weight-semibold;
      color: var(--text-primary);
      margin: 0 0 $spacing-lg 0;
    }

    .error-description {
      color: var(--text-secondary);
      font-size: $font-size-base;
      line-height: var(--line-height-relaxed);
      margin: 0;
    }
  }

  .error-actions {
    @include flex-center();
    gap: $spacing-md;
    flex-wrap: wrap;
  }
}

.error-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;

  .floating-shapes {
    position: relative;
    width: 100%;
    height: 100%;

    .shape {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(20px);
      animation: float 8s ease-in-out infinite;

      &.shape-1 {
        width: 200px;
        height: 200px;
        top: 20%;
        left: 10%;
        animation-delay: 0s;
      }

      &.shape-2 {
        width: 150px;
        height: 150px;
        top: 70%;
        right: 20%;
        animation-delay: 3s;
      }

      &.shape-3 {
        width: 100px;
        height: 100px;
        bottom: 30%;
        left: 30%;
        animation-delay: 6s;
      }
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  33% {
    transform: translateY(-20px) rotate(120deg);
  }
  66% {
    transform: translateY(20px) rotate(240deg);
  }
}

// 响应式设计
@include respond-below('md') {
  .error-container {
    padding: $spacing-md;

    .error-card {
      padding: $spacing-2xl;
    }
  }

  .error-content {
    .error-info {
      .error-code {
        font-size: 4rem;
      }

      .error-title {
        font-size: $font-size-xl;
      }
    }

    .error-actions {
      flex-direction: column;
      width: 100%;

      .modern-button {
        width: 100%;
      }
    }
  }
}
</style>
