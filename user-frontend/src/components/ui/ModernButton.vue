<template>
  <component
    :is="tag"
    :class="[
      'modern-button',
      `modern-button--${type}`,
      `modern-button--${size}`,
      {
        'modern-button--loading': loading,
        'modern-button--disabled': disabled,
        'modern-button--block': block,
        'modern-button--round': round,
        'modern-button--circle': circle,
        'modern-button--glass': glass
      }
    ]"
    :disabled="disabled || loading"
    :to="to"
    :type="buttonType"
    @click="handleClick"
  >
    <!-- 加载图标 -->
    <el-icon v-if="loading" class="modern-button__loading">
      <Loading />
    </el-icon>

    <!-- 前置图标 -->
    <el-icon v-else-if="icon && !iconRight" class="modern-button__icon">
      <component :is="icon" />
    </el-icon>

    <!-- 按钮文本 -->
    <span v-if="$slots.default || text" class="modern-button__text">
      <slot>{{ text }}</slot>
    </span>

    <!-- 后置图标 -->
    <el-icon v-if="icon && iconRight && !loading" class="modern-button__icon modern-button__icon--right">
      <component :is="icon" />
    </el-icon>

    <!-- 光效动画 -->
    <span class="modern-button__shine" />
  </component>
</template>

<script setup lang="ts">
import { Loading } from '@element-plus/icons-vue'
import { computed } from 'vue'
import type { RouteLocationRaw } from 'vue-router'

interface Props {
  /** 按钮类型 */
  type?: 'primary' | 'success' | 'warning' | 'error' | 'info' | 'default' | 'text'
  /** 按钮尺寸 */
  size?: 'small' | 'medium' | 'large'
  /** 按钮文本 */
  text?: string
  /** 按钮图标 */
  icon?: string
  /** 图标是否在右侧 */
  iconRight?: boolean
  /** 是否加载中 */
  loading?: boolean
  /** 是否禁用 */
  disabled?: boolean
  /** 是否块级按钮 */
  block?: boolean
  /** 是否圆角按钮 */
  round?: boolean
  /** 是否圆形按钮 */
  circle?: boolean
  /** 是否使用毛玻璃效果 */
  glass?: boolean
  /** 路由跳转 */
  to?: RouteLocationRaw
  /** HTML 按钮类型 */
  buttonType?: 'button' | 'submit' | 'reset'
}

interface Emits {
  (e: 'click', event: MouseEvent): void
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  size: 'medium',
  iconRight: false,
  loading: false,
  disabled: false,
  block: false,
  round: false,
  circle: false,
  glass: false,
  buttonType: 'button'
})

const emit = defineEmits<Emits>()

const tag = computed(() => {
  return props.to ? 'router-link' : 'button'
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.modern-button {
  @include modern-button();
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  font-weight: $font-weight-medium;
  text-decoration: none;
  user-select: none;
  vertical-align: middle;

  // 尺寸变体
  &--small {
    padding: $spacing-xs $spacing-sm;
    font-size: $font-size-xs;
    border-radius: $radius-md;
    min-height: 28px;
  }

  &--medium {
    padding: $spacing-sm $spacing-md;
    font-size: $font-size-sm;
    border-radius: $radius-lg;
    min-height: 36px;
  }

  &--large {
    padding: $spacing-md $spacing-xl;
    font-size: $font-size-lg;
    border-radius: $radius-xl;
    min-height: 44px;
  }

  // 类型变体
  &--default {
    background: $bg-primary;
    color: $text-primary;
    border: 1px solid $border-color;
    box-shadow: $shadow-sm;

    &:hover {
      background: $bg-secondary;
      border-color: $primary-color;
      box-shadow: $shadow-md;
    }
  }

  &--primary {
    background: $gradient-primary;
    color: white;
    border: 1px solid $primary-color;
    box-shadow: $shadow-primary;

    &:hover {
      box-shadow: $shadow-primary-hover;
    }
  }

  &--success {
    background: linear-gradient(135deg, $success-color 0%, lighten($success-color, 10%) 100%);
    color: white;
    border: 1px solid $success-color;
    box-shadow: $shadow-success;

    &:hover {
      box-shadow: 0 12px 35px rgba($success-color, 0.25);
    }
  }

  &--warning {
    background: linear-gradient(135deg, $warning-color 0%, lighten($warning-color, 10%) 100%);
    color: white;
    border: 1px solid $warning-color;
    box-shadow: $shadow-warning;

    &:hover {
      box-shadow: 0 12px 35px rgba($warning-color, 0.25);
    }
  }

  &--error {
    background: linear-gradient(135deg, $error-color 0%, lighten($error-color, 10%) 100%);
    color: white;
    border: 1px solid $error-color;
    box-shadow: $shadow-error;

    &:hover {
      box-shadow: 0 12px 35px rgba($error-color, 0.25);
    }
  }

  &--info {
    background: linear-gradient(135deg, $info-color 0%, lighten($info-color, 10%) 100%);
    color: white;
    border: 1px solid $info-color;
    box-shadow: 0 8px 25px rgba($info-color, 0.15);

    &:hover {
      box-shadow: 0 12px 35px rgba($info-color, 0.25);
    }
  }

  &--text {
    background: transparent;
    color: $primary-color;
    border: none;
    box-shadow: none;
    padding: $spacing-xs $spacing-sm;

    &:hover {
      background: rgba($primary-color, 0.1);
      transform: none;
      box-shadow: none;
    }
  }

  // 状态变体
  &--loading {
    pointer-events: none;
    opacity: 0.8;
  }

  &--disabled {
    opacity: 0.6;
    cursor: not-allowed;
    pointer-events: none;

    &:hover {
      transform: none;
    }
  }

  &--block {
    width: 100%;
  }

  &--round {
    border-radius: $radius-full;
  }

  &--circle {
    border-radius: 50%;
    width: 40px;
    height: 40px;
    padding: 0;

    &.modern-button--small {
      width: 28px;
      height: 28px;
    }

    &.modern-button--large {
      width: 48px;
      height: 48px;
    }
  }

  &--glass {
    @include glass-effect();
    backdrop-filter: $glass-blur-sm;
  }

  // 子元素
  &__loading {
    animation: spin 1s linear infinite;
  }

  &__icon {
    font-size: 1.2em;

    &--right {
      order: 1;
    }
  }

  &__text {
    flex: 1;
    text-align: center;
  }

  &__shine {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s;
    pointer-events: none;
  }

  &:hover &__shine {
    left: 100%;
  }
}

// 暗色主题适配
.dark .modern-button {
  &--default {
    background: #1f1f1f;
    color: rgba(255, 255, 255, 0.85);
    border-color: #434343;

    &:hover {
      background: #2a2a2a;
      border-color: $primary-color;
    }
  }

  &--text {
    &:hover {
      background: rgba($primary-color, 0.2);
    }
  }
}
</style>
