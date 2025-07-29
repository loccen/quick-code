<template>
  <div class="stats-cards">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
// 无需props，纯容器组件
</script>

<style scoped>
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}
</style>
