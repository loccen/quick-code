<template>
  <div 
    :class="[
      'modern-card',
      `modern-card--${variant}`,
      `modern-card--${size}`,
      {
        'modern-card--hoverable': hoverable,
        'modern-card--loading': loading,
        'modern-card--glass': glass
      }
    ]"
    @click="handleClick"
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="modern-card__loading">
      <el-icon class="is-loading">
        <Loading />
      </el-icon>
      <span>{{ loadingText }}</span>
    </div>

    <!-- 卡片内容 -->
    <template v-else>
      <!-- 头部 -->
      <div v-if="$slots.header || title" class="modern-card__header">
        <slot name="header">
          <div class="modern-card__title">
            <el-icon v-if="icon" class="modern-card__icon">
              <component :is="icon" />
            </el-icon>
            <span>{{ title }}</span>
          </div>
          <div v-if="$slots.extra" class="modern-card__extra">
            <slot name="extra" />
          </div>
        </slot>
      </div>

      <!-- 主体内容 -->
      <div class="modern-card__body">
        <slot />
      </div>

      <!-- 底部 -->
      <div v-if="$slots.footer" class="modern-card__footer">
        <slot name="footer" />
      </div>
    </template>

    <!-- 装饰性元素 -->
    <div v-if="!loading" class="modern-card__decoration" />
  </div>
</template>

<script setup lang="ts">
import { Loading } from '@element-plus/icons-vue'

interface Props {
  /** 卡片标题 */
  title?: string
  /** 卡片图标 */
  icon?: string
  /** 卡片变体 */
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'error'
  /** 卡片尺寸 */
  size?: 'small' | 'medium' | 'large'
  /** 是否可悬停 */
  hoverable?: boolean
  /** 是否加载中 */
  loading?: boolean
  /** 加载文本 */
  loadingText?: string
  /** 是否使用毛玻璃效果 */
  glass?: boolean
}

interface Emits {
  (e: 'click', event: MouseEvent): void
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
  size: 'medium',
  hoverable: true,
  loading: false,
  loadingText: '加载中...',
  glass: false
})

const emit = defineEmits<Emits>()

const handleClick = (event: MouseEvent) => {
  if (!props.loading) {
    emit('click', event)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.modern-card {
  @include enhanced-card();
  cursor: pointer;
  user-select: none;

  &--small {
    padding: $spacing-sm;
    border-radius: $radius-md;
  }

  &--medium {
    padding: $spacing-md;
    border-radius: $radius-lg;
  }

  &--large {
    padding: $spacing-lg;
    border-radius: $radius-xl;
  }

  &--primary {
    border-color: rgba($primary-color, 0.3);
    
    &:hover {
      border-color: rgba($primary-color, 0.5);
      box-shadow: $shadow-primary-hover;
    }
  }

  &--success {
    border-color: rgba($success-color, 0.3);
    
    &:hover {
      border-color: rgba($success-color, 0.5);
      box-shadow: $shadow-success;
    }
  }

  &--warning {
    border-color: rgba($warning-color, 0.3);
    
    &:hover {
      border-color: rgba($warning-color, 0.5);
      box-shadow: $shadow-warning;
    }
  }

  &--error {
    border-color: rgba($error-color, 0.3);
    
    &:hover {
      border-color: rgba($error-color, 0.5);
      box-shadow: $shadow-error;
    }
  }

  &--glass {
    @include glass-effect();
  }

  &--loading {
    pointer-events: none;
    opacity: 0.8;
  }

  &:not(&--hoverable) {
    cursor: default;
    
    &:hover {
      transform: none;
      box-shadow: $shadow-layered-sm;
    }
  }

  &__loading {
    @include flex-center();
    flex-direction: column;
    gap: $spacing-sm;
    padding: $spacing-xl;
    color: $text-secondary;

    .el-icon {
      font-size: 24px;
    }
  }

  &__header {
    @include flex-between();
    margin-bottom: $spacing-md;
    padding-bottom: $spacing-sm;
    border-bottom: 1px solid $border-light;
  }

  &__title {
    @include flex-center();
    gap: $spacing-xs;
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $text-primary;
  }

  &__icon {
    font-size: 20px;
    color: $primary-color;
  }

  &__extra {
    color: $text-secondary;
  }

  &__body {
    flex: 1;
  }

  &__footer {
    margin-top: $spacing-md;
    padding-top: $spacing-sm;
    border-top: 1px solid $border-light;
  }

  &__decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
    pointer-events: none;
  }
}

// 暗色主题适配
.dark .modern-card {
  &__header {
    border-bottom-color: #434343;
  }

  &__footer {
    border-top-color: #434343;
  }

  &__decoration {
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  }
}
</style>
