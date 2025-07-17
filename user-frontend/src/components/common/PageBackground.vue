<template>
  <div v-if="shouldShowBackground" class="page-background">
    <!-- 主背景层 -->
    <div class="bg-layer bg-main"></div>

    <!-- 装饰性渐变层 -->
    <div class="bg-layer bg-decoration-1"></div>
    <div class="bg-layer bg-decoration-2"></div>
    <div class="bg-layer bg-decoration-3"></div>

    <!-- 几何图案层 -->
    <div class="bg-layer bg-pattern"></div>

    <!-- 微妙的网格层 -->
    <div class="bg-layer bg-grid"></div>

    <!-- 顶部高光效果 -->
    <div class="bg-layer bg-highlight"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 不显示背景的页面路径
const excludedPaths = ['/login', '/register', '/forgot-password', '/reset-password']

// 判断是否应该显示背景
const shouldShowBackground = computed(() => {
  return !excludedPaths.includes(route.path)
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.page-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -1;
  overflow: hidden;
}

.bg-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

// 主背景层 - 微妙的渐变
.bg-main {
  background: $gradient-page-bg;
}

// 装饰性渐变层
.bg-decoration-1 {
  background: $gradient-decoration-1;
  animation: float-slow 20s ease-in-out infinite;
}

.bg-decoration-2 {
  background: $gradient-decoration-2;
  animation: float-slow 25s ease-in-out infinite reverse;
}

.bg-decoration-3 {
  background: $gradient-decoration-3;
  animation: float-slow 30s ease-in-out infinite;
}

// 几何图案层 - 微妙的点状图案
.bg-pattern {
  background: $pattern-dots;
  background-size: 45px 45px;
  opacity: 0.5;
  animation: pattern-shift 70s linear infinite;
}

// 网格层 - 微妙的网格线
.bg-grid {
  background: $pattern-grid;
  background-size: 70px 70px;
  opacity: 0.25;
}

// 顶部高光效果
.bg-highlight {
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.7) 0%,
    rgba(255, 255, 255, 0.4) 10%,
    rgba(255, 255, 255, 0.2) 18%,
    transparent 30%
  );
  height: 250px;
  top: 0;
  bottom: auto;
}

// 动画定义
@keyframes float-slow {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(-10px, -10px) scale(1.02);
  }
  50% {
    transform: translate(10px, -5px) scale(0.98);
  }
  75% {
    transform: translate(-5px, 10px) scale(1.01);
  }
}

@keyframes pattern-shift {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(40px, 40px);
  }
}

// 响应式调整
@media (max-width: 768px) {
  .bg-pattern {
    background-size: 30px 30px;
    opacity: 0.2;
  }
  
  .bg-grid {
    background-size: 40px 40px;
    opacity: 0.1;
  }
  
  .bg-decoration-1,
  .bg-decoration-2,
  .bg-decoration-3 {
    opacity: 0.8;
  }
}

// 深色模式适配（如果需要）
@media (prefers-color-scheme: dark) {
  .bg-main {
    background: linear-gradient(135deg, #0f1419 0%, #1a1a1a 25%, #0f1419 50%, #1a1a1a 75%, #0f1419 100%);
  }
  
  .bg-highlight {
    background: linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.05) 0%,
      rgba(255, 255, 255, 0.02) 10%,
      transparent 20%
    );
  }
}
</style>
