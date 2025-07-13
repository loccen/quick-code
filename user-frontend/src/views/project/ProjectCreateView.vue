<template>
  <div class="project-create-view">
    <div class="page-header">
      <h1 class="page-title">创建项目</h1>
      <p class="page-subtitle">创建一个新的项目</p>
    </div>

    <ModernCard title="创建项目" class="project-card">
      <div class="coming-soon">
        <el-icon :size="64"><Plus /></el-icon>
        <h3>功能开发中</h3>
        <p>项目创建功能正在开发中，敬请期待...</p>
      </div>
    </ModernCard>
  </div>
</template>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import ModernCard from '@/components/ui/ModernCard.vue'

const appStore = useAppStore()
appStore.setPageTitle('创建项目')
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.project-create-view {
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

  .project-card {
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
