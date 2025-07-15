<template>
  <div class="settings-view">
    <div class="page-header">
      <h1 class="page-title">系统设置</h1>
      <p class="page-subtitle">管理您的系统偏好设置</p>
    </div>

    <ModernCard title="系统设置" class="settings-card">
      <div class="coming-soon">
        <el-icon :size="64"><Setting /></el-icon>
        <h3>功能开发中</h3>
        <p>系统设置功能正在开发中，敬请期待...</p>
      </div>
    </ModernCard>
  </div>
</template>

<script setup lang="ts">
import { Setting } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import ModernCard from '@/components/ui/ModernCard.vue'

const appStore = useAppStore()
appStore.setPageTitle('系统设置')
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.settings-view {
  .page-header {
    margin-bottom: $spacing-xl;

    .page-title {
      font-size: $font-size-3xl;
      font-weight: $font-weight-bold;
      color: var(--text-primary);
      margin: 0 0 $spacing-xs 0;
      @include gradient-text();
    }

    .page-subtitle {
      color: var(--text-secondary);
      font-size: $font-size-lg;
      margin: 0;
    }
  }

  .settings-card {
    .coming-soon {
      @include flex-center();
      flex-direction: column;
      gap: $spacing-md;
      padding: $spacing-3xl;
      color: var(--text-secondary);
      text-align: center;

      h3 {
        margin: 0;
        color: var(--text-primary);
      }

      p {
        margin: 0;
      }
    }
  }
}
</style>
