<template>
  <div class="error-view">
    <div class="error-container">
      <ModernCard class="error-card" :glass="true" :hoverable="false">
        <div class="error-content">
          <div class="error-icon">
            <el-icon :size="120"><Warning /></el-icon>
          </div>
          <div class="error-info">
            <h1 class="error-code">500</h1>
            <h2 class="error-title">服务器错误</h2>
            <p class="error-description">
              抱歉，服务器遇到了一些问题。
              <br>
              我们正在努力修复，请稍后再试。
            </p>
          </div>
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
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Warning, HomeFilled, ArrowLeft } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import ModernCard from '@/components/ui/ModernCard.vue'
import ModernButton from '@/components/ui/ModernButton.vue'

const router = useRouter()
const appStore = useAppStore()

const goHome = () => router.push('/')
const goBack = () => window.history.length > 1 ? router.go(-1) : router.push('/')

appStore.setPageTitle('服务器错误')
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.error-view {
  min-height: 100vh;
  @include flex-center();
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);

  .error-container {
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
      color: var(--error-color);
    }

    .error-info {
      margin-bottom: $spacing-2xl;

      .error-code {
        font-size: 6rem;
        font-weight: $font-weight-bold;
        background: linear-gradient(135deg, var(--error-color) 0%, #ff6b6b 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
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
}

@include respond-below('md') {
  .error-view {
    .error-container {
      padding: $spacing-md;
      .error-card { padding: $spacing-2xl; }
    }
    .error-content {
      .error-info .error-code { font-size: 4rem; }
      .error-actions {
        flex-direction: column;
        width: 100%;
        .modern-button { width: 100%; }
      }
    }
  }
}
</style>
