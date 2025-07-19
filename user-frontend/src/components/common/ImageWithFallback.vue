<template>
  <div class="image-with-fallback" :class="containerClass">
    <!-- 加载状态 -->
    <div v-if="loading && showLoading" class="image-loading">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span v-if="loadingText" class="loading-text">{{ loadingText }}</span>
    </div>

    <!-- 图片 -->
    <img 
      v-if="!loadFailed && !loading"
      :src="currentSrc" 
      :alt="alt"
      :class="imageClass"
      @error="handleError"
      @load="handleLoad"
      @loadstart="handleLoadStart"
      v-bind="$attrs"
    />

    <!-- 占位符 -->
    <div v-if="loadFailed && !loading" class="image-placeholder" :class="placeholderClass">
      <slot name="placeholder" :failed="loadFailed" :src="originalSrc">
        <el-icon class="placeholder-icon"><Picture /></el-icon>
        <span v-if="placeholderText" class="placeholder-text">{{ placeholderText }}</span>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import { Loading, Picture } from '@element-plus/icons-vue'

/**
 * 图片加载组件属性
 */
interface Props {
  /** 图片源地址 */
  src?: string
  /** 备用图片地址 */
  fallbackSrc?: string
  /** 图片alt属性 */
  alt?: string
  /** 占位符文本 */
  placeholderText?: string
  /** 加载文本 */
  loadingText?: string
  /** 是否显示加载状态 */
  showLoading?: boolean
  /** 容器CSS类 */
  containerClass?: string
  /** 图片CSS类 */
  imageClass?: string
  /** 占位符CSS类 */
  placeholderClass?: string
  /** 是否启用懒加载 */
  lazy?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  fallbackSrc: '/images/default-project.jpg',
  placeholderText: '暂无图片',
  loadingText: '',
  showLoading: true,
  containerClass: '',
  imageClass: '',
  placeholderClass: '',
  lazy: false
})

/**
 * 组件事件
 */
interface Emits {
  (e: 'load', event: Event): void
  (e: 'error', event: Event): void
  (e: 'loadstart', event: Event): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const loadFailed = ref(false)
const loading = ref(false)
const hasTriedFallback = ref(false)
const originalSrc = ref(props.src)

// 计算当前图片源
const currentSrc = computed(() => {
  if (!originalSrc.value && !props.fallbackSrc) {
    return ''
  }
  return originalSrc.value || props.fallbackSrc
})

/**
 * 处理图片加载开始
 */
const handleLoadStart = (event: Event) => {
  loading.value = true
  emit('loadstart', event)
}

/**
 * 处理图片加载成功
 */
const handleLoad = (event: Event) => {
  loading.value = false
  loadFailed.value = false
  emit('load', event)
}

/**
 * 处理图片加载错误
 */
const handleError = (event: Event) => {
  loading.value = false
  const img = event.target as HTMLImageElement
  
  // 如果还没有尝试过备用图片，且当前不是备用图片，则尝试备用图片
  if (!hasTriedFallback.value && props.fallbackSrc && img.src !== props.fallbackSrc) {
    hasTriedFallback.value = true
    originalSrc.value = props.fallbackSrc
    return
  }
  
  // 所有图片都失败了，显示占位符
  loadFailed.value = true
  emit('error', event)
}

/**
 * 重置加载状态
 */
const resetLoadState = () => {
  loadFailed.value = false
  loading.value = false
  hasTriedFallback.value = false
}

/**
 * 监听src变化
 */
watch(() => props.src, (newSrc) => {
  resetLoadState()
  originalSrc.value = newSrc
}, { immediate: true })

/**
 * 暴露方法给父组件
 */
defineExpose({
  resetLoadState,
  retry: () => {
    resetLoadState()
    originalSrc.value = props.src
  }
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.image-with-fallback {
  position: relative;
  display: inline-block;
  
  .image-loading {
    @include flex-center();
    flex-direction: column;
    gap: $spacing-sm;
    padding: $spacing-lg;
    color: var(--text-secondary);
    
    .loading-icon {
      font-size: 1.5rem;
      animation: rotate 1s linear infinite;
    }
    
    .loading-text {
      font-size: $font-size-sm;
    }
  }
  
  .image-placeholder {
    @include flex-center();
    flex-direction: column;
    gap: $spacing-sm;
    padding: $spacing-lg;
    background: var(--bg-secondary);
    border: 2px dashed var(--border-color);
    border-radius: $border-radius-md;
    color: var(--text-secondary);
    min-height: 100px;
    
    .placeholder-icon {
      font-size: 2rem;
      opacity: 0.6;
    }
    
    .placeholder-text {
      font-size: $font-size-sm;
      opacity: 0.8;
      text-align: center;
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
