<template>
  <div class="page-layout">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
// 无需props，纯容器组件
</script>

<style scoped>
.page-layout {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
