<template>
  <div class="content-container">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
// 无需props，纯容器组件
</script>

<style scoped>
.content-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}
</style>
