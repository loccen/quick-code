<template>
  <div class="role-management-view">
    <div class="page-header">
      <h1 class="page-title">角色管理</h1>
      <p class="page-subtitle">管理系统角色</p>
    </div>

    <ModernCard title="角色管理" class="management-card">
      <div class="coming-soon">
        <el-icon :size="64"><UserFilled /></el-icon>
        <h3>功能开发中</h3>
        <p>角色管理功能正在开发中，敬请期待...</p>
      </div>
    </ModernCard>
  </div>
</template>

<script setup lang="ts">
import { UserFilled } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import ModernCard from '@/components/ui/ModernCard.vue'

const appStore = useAppStore()
appStore.setPageTitle('角色管理')
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.role-management-view {
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

  .management-card {
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
