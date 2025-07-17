<template>
  <div 
    class="project-card" 
    @click="handleCardClick"
    :style="{ cursor: 'pointer' }"
  >
    <!-- 项目图片区域 -->
    <div class="project-image">
      <div class="project-placeholder">
        <i :class="project.icon || 'fas fa-code'"></i>
      </div>
      <div class="project-badges">
        <span class="badge badge-docker" v-if="project.hasDocker">
          <i class="fab fa-docker"></i>
          Docker
        </span>
        <span v-if="project.isHot" class="badge badge-hot">热门</span>
      </div>
    </div>

    <!-- 项目内容区域 -->
    <div class="project-content">
      <h3 class="project-title">{{ project.title }}</h3>
      <p class="project-description">{{ project.description }}</p>
      
      <!-- 项目标签 -->
      <div class="project-tags">
        <span v-for="tag in project.tags?.slice(0, 3)" :key="tag" class="tag">
          {{ tag }}
        </span>
      </div>

      <!-- 项目底部信息 -->
      <div class="project-footer">
        <div class="project-price">
          <i class="fas fa-coins"></i>
          <span>{{ project.price }} 积分</span>
        </div>
        <div class="project-actions">
          <button 
            class="btn btn-sm btn-outline" 
            @click.stop="handleDemo"
          >
            <i class="fas fa-play"></i>
            演示
          </button>
          <button 
            class="btn btn-sm btn-primary" 
            @click.stop="handlePurchase"
          >
            购买
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

interface Project {
  id: number
  title: string
  description: string
  icon?: string
  price: number
  tags?: string[]
  isHot?: boolean
  hasDocker?: boolean
  author?: string
  rating?: number
  downloads?: number
  category?: string
}

interface Props {
  project: Project
}

interface Emits {
  (e: 'demo', project: Project): void
  (e: 'purchase', project: Project): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const router = useRouter()

/**
 * 处理卡片点击 - 跳转到项目详情页
 */
const handleCardClick = () => {
  router.push(`/market/project/${props.project.id}`)
}

/**
 * 处理演示按钮点击
 */
const handleDemo = () => {
  emit('demo', props.project)
}

/**
 * 处理购买按钮点击
 */
const handlePurchase = () => {
  emit('purchase', props.project)
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.project-card {
  background: $gradient-card;
  border-radius: $radius-xl;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: $glass-blur-sm;
  box-shadow: $shadow-layered-sm;
  transition: all $transition-base;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
    z-index: 1;
  }

  &:hover {
    box-shadow: $shadow-layered-lg;
    transform: translateY(-8px) scale(1.02);
    border-color: rgba(24, 144, 255, 0.3);
  }

  .project-image {
    position: relative;
    height: 200px;
    background: $gradient-card;
    display: flex;
    align-items: center;
    justify-content: center;

    .project-placeholder {
      width: 80px;
      height: 80px;
      background: $gradient-primary;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: $shadow-primary;

      i {
        font-size: 2rem;
        color: white;
      }
    }

    .project-badges {
      position: absolute;
      top: $spacing-md;
      right: $spacing-md;
      display: flex;
      flex-direction: column;
      gap: $spacing-xs;

      .badge {
        padding: $spacing-xs $spacing-sm;
        border-radius: $radius-md;
        font-size: $font-size-xs;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: $spacing-xs;
        backdrop-filter: $glass-blur-sm;
        border: 1px solid rgba(255, 255, 255, 0.2);

        &.badge-docker {
          background: rgba(0, 123, 255, 0.9);
          color: white;
        }

        &.badge-hot {
          background: rgba(255, 77, 79, 0.9);
          color: white;
        }
      }
    }
  }

  .project-content {
    padding: $spacing-lg;

    .project-title {
      font-size: $font-size-lg;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: $spacing-sm;
      line-height: 1.3;
    }

    .project-description {
      color: $text-secondary;
      line-height: 1.6;
      margin-bottom: $spacing-md;
      font-size: $font-size-sm;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .project-tags {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-xs;
      margin-bottom: $spacing-lg;

      .tag {
        padding: $spacing-xs $spacing-sm;
        background: $primary-light;
        color: $primary-color;
        border-radius: $radius-sm;
        font-size: $font-size-xs;
        font-weight: 500;
      }
    }

    .project-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .project-price {
        display: flex;
        align-items: center;
        gap: $spacing-xs;
        color: $primary-color;
        font-weight: 600;

        i {
          color: $warning-color;
        }
      }

      .project-actions {
        display: flex;
        gap: $spacing-sm;

        .btn {
          padding: $spacing-xs $spacing-md;
          border-radius: $radius-md;
          font-size: $font-size-sm;
          font-weight: 500;
          border: none;
          cursor: pointer;
          transition: all 0.3s ease;
          display: flex;
          align-items: center;
          gap: $spacing-xs;

          &.btn-sm {
            padding: $spacing-xs $spacing-sm;
          }

          &.btn-outline {
            background: transparent;
            color: $primary-color;
            border: 1px solid $primary-color;

            &:hover {
              background: $primary-color;
              color: white;
              transform: translateY(-1px);
            }
          }

          &.btn-primary {
            background: $primary-color;
            color: white;
            box-shadow: $shadow-primary;

            &:hover {
              background: $primary-hover;
              transform: translateY(-1px);
              box-shadow: $shadow-primary-hover;
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .project-card {
    .project-image {
      height: 160px;

      .project-placeholder {
        width: 60px;
        height: 60px;

        i {
          font-size: 1.5rem;
        }
      }
    }

    .project-content {
      padding: $spacing-md;

      .project-footer {
        flex-direction: column;
        gap: $spacing-sm;
        align-items: flex-start;

        .project-actions {
          width: 100%;
          justify-content: space-between;

          .btn {
            flex: 1;
            justify-content: center;
          }
        }
      }
    }
  }
}
</style>
