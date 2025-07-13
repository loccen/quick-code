<template>
  <div class="project-card" @click="handleCardClick">
    <!-- 项目缩略图 -->
    <div class="project-thumbnail">
      <img
        :src="project.thumbnail || '/images/default-project.jpg'"
        :alt="project.title"
        @error="handleImageError"
      />
      <div class="overlay">
        <div class="overlay-actions">
          <el-button
            type="primary"
            size="small"
            @click.stop="$emit('demo', project)"
          >
            <el-icon><VideoPlay /></el-icon>
            演示
          </el-button>
          <el-button
            type="success"
            size="small"
            @click.stop="$emit('purchase', project)"
          >
            <el-icon><ShoppingCart /></el-icon>
            购买
          </el-button>
        </div>
      </div>
    </div>

    <!-- 项目信息 -->
    <div class="project-info">
      <!-- 项目标题 -->
      <h3 class="project-title">{{ project.title }}</h3>

      <!-- 项目描述 -->
      <p class="project-description">{{ project.description }}</p>

      <!-- 项目标签 -->
      <div class="project-tags">
        <el-tag
          v-for="tag in project.tags?.slice(0, 3)"
          :key="tag"
          size="small"
          type="info"
        >
          {{ tag }}
        </el-tag>
      </div>

      <!-- 项目统计 -->
      <div class="project-stats">
        <div class="stat-item">
          <el-icon><Star /></el-icon>
          <span>{{ project.rating || 0 }}</span>
        </div>
        <div class="stat-item">
          <el-icon><Download /></el-icon>
          <span>{{ formatNumber(project.downloads || 0) }}</span>
        </div>
        <div class="stat-item">
          <el-icon><User /></el-icon>
          <span>{{ project.author }}</span>
        </div>
      </div>

      <!-- 价格和操作 -->
      <div class="project-footer">
        <div class="price-section">
          <span class="price">¥{{ project.price }}</span>
          <span class="price-unit">积分</span>
        </div>
        <div class="action-buttons">
          <el-button
            size="small"
            @click.stop="$emit('viewDetail', project)"
          >
            查看详情
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
    Download,
    ShoppingCart,
    Star,
    User,
    VideoPlay
} from '@element-plus/icons-vue'

interface Project {
  id: number
  title: string
  description: string
  thumbnail?: string
  price: number
  rating?: number
  downloads?: number
  author: string
  tags?: string[]
  category: string
}

interface Props {
  project: Project
}

interface Emits {
  (e: 'viewDetail', project: Project): void
  (e: 'purchase', project: Project): void
  (e: 'demo', project: Project): void
}

defineProps<Props>()
defineEmits<Emits>()

/**
 * 处理卡片点击
 */
const handleCardClick = () => {
  // 点击卡片查看详情
  // 这里可以添加点击卡片的逻辑
}

/**
 * 处理图片加载错误
 */
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-project.jpg'
}

/**
 * 格式化数字
 */
const formatNumber = (num: number): string => {
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.project-card {
  background: var(--bg-primary);
  border-radius: $border-radius-lg;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid var(--border-color);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);

    .project-thumbnail .overlay {
      opacity: 1;
    }
  }

  .project-thumbnail {
    position: relative;
    width: 100%;
    height: 200px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.7);
      opacity: 0;
      transition: opacity 0.3s ease;
      @include flex-center();

      .overlay-actions {
        @include flex-center();
        gap: $spacing-md;
      }
    }

    &:hover img {
      transform: scale(1.05);
    }
  }

  .project-info {
    padding: $spacing-lg;

    .project-title {
      font-size: $font-size-lg;
      font-weight: $font-weight-bold;
      color: var(--text-primary);
      margin: 0 0 $spacing-sm 0;
      line-height: 1.4;
      @include text-ellipsis-multiline(2);
    }

    .project-description {
      color: var(--text-secondary);
      font-size: $font-size-sm;
      line-height: 1.5;
      margin: 0 0 $spacing-md 0;
      @include text-ellipsis-multiline(2);
    }

    .project-tags {
      @include flex-start();
      gap: $spacing-xs;
      margin-bottom: $spacing-md;
      flex-wrap: wrap;
    }

    .project-stats {
      @include flex-between();
      margin-bottom: $spacing-lg;
      padding: $spacing-sm 0;
      border-top: 1px solid var(--border-color);
      border-bottom: 1px solid var(--border-color);

      .stat-item {
        @include flex-center();
        gap: $spacing-xs;
        color: var(--text-secondary);
        font-size: $font-size-sm;

        .el-icon {
          font-size: 14px;
        }
      }
    }

    .project-footer {
      @include flex-between();
      align-items: center;

      .price-section {
        @include flex-center();
        gap: $spacing-xs;

        .price {
          font-size: $font-size-xl;
          font-weight: $font-weight-bold;
          color: var(--primary-color);
        }

        .price-unit {
          font-size: $font-size-sm;
          color: var(--text-secondary);
        }
      }

      .action-buttons {
        @include flex-center();
        gap: $spacing-sm;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .project-card {
    .project-thumbnail {
      height: 160px;

      .overlay-actions {
        flex-direction: column;
        gap: $spacing-sm;

        .el-button {
          width: 80px;
        }
      }
    }

    .project-info {
      padding: $spacing-md;

      .project-stats {
        flex-direction: column;
        gap: $spacing-xs;
        align-items: flex-start;

        .stat-item {
          justify-content: flex-start;
        }
      }

      .project-footer {
        flex-direction: column;
        gap: $spacing-sm;
        align-items: flex-start;
      }
    }
  }
}
</style>
