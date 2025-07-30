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
          <span>{{ project.price }}</span>
        </div>
        <div class="project-actions">
          <!-- 演示按钮 - 所有模式都显示 -->
          <button
            class="btn btn-sm btn-outline"
            @click.stop="handleDemo"
          >
            <i class="fas fa-play"></i>
            演示
          </button>

          <!-- 我上传的项目模式 -->
          <template v-if="mode === 'uploaded'">
            <button
              class="btn btn-sm btn-primary"
              @click.stop="handleEdit"
            >
              <i class="fas fa-edit"></i>
              编辑
            </button>
            <button
              class="btn btn-sm btn-danger"
              @click.stop="handleDelete"
            >
              <i class="fas fa-trash"></i>
              删除
            </button>
          </template>

          <!-- 我购买的项目模式 -->
          <template v-else-if="mode === 'purchased'">
            <button
              class="btn btn-sm btn-primary"
              @click.stop="handleDownload"
            >
              <i class="fas fa-download"></i>
              下载
            </button>
          </template>

          <!-- 我收藏的项目模式 -->
          <template v-else-if="mode === 'favorites'">
            <button
              class="btn btn-sm btn-primary"
              @click.stop="handlePurchase"
            >
              <i class="fas fa-shopping-cart"></i>
              购买
            </button>
            <button
              class="btn btn-sm btn-outline"
              @click.stop="handleUnfavorite"
            >
              <i class="fas fa-heart-broken"></i>
              取消收藏
            </button>
          </template>

          <!-- 市场模式（默认） -->
          <template v-else>
            <button
              class="btn btn-sm btn-primary"
              @click.stop="handlePurchase"
            >
              <i class="fas fa-shopping-cart"></i>
              购买
            </button>
            <button
              class="btn btn-sm btn-outline"
              @click.stop="handleFavorite"
              :class="{ 'btn-danger': project.isFavorite }"
            >
              <i :class="project.isFavorite ? 'fas fa-heart' : 'far fa-heart'"></i>
              {{ project.isFavorite ? '已收藏' : '收藏' }}
            </button>
          </template>
        </div>
      </div>

      <!-- 项目统计信息 -->
      <div class="project-stats">
        <!-- 评分 -->
        <div class="stat-item" v-if="project.rating && project.rating > 0">
          <i class="fas fa-star"></i>
          <span>{{ formatRating(project.rating) }}</span>
        </div>

        <!-- 浏览量 -->
        <div class="stat-item" v-if="project.viewCount && project.viewCount > 0">
          <i class="fas fa-eye"></i>
          <span>{{ formatViews(project.viewCount) }}</span>
        </div>

        <!-- 下载量 -->
        <div class="stat-item" v-if="project.downloadCount && project.downloadCount > 0">
          <i class="fas fa-download"></i>
          <span>{{ formatDownloads(project.downloadCount) }}</span>
        </div>

        <!-- 发布时间 -->
        <div class="stat-item" v-if="project.createdTime">
          <i class="fas fa-clock"></i>
          <span>{{ formatDate(project.createdTime) }}</span>
        </div>

        <!-- 作者 -->
        <div class="stat-item" v-if="project.username">
          <i class="fas fa-user"></i>
          <span>{{ project.username }}</span>
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
  username?: string // 作者用户名
  rating?: number
  downloadCount?: number // 下载次数
  viewCount?: number // 浏览次数
  category?: string
  createdTime?: string // 创建时间
  updatedTime?: string
  // 兼容旧字段名
  author?: string
  downloads?: number
  createdAt?: string
}

// 项目卡片显示模式
type ProjectCardMode = 'market' | 'uploaded' | 'purchased' | 'favorites'

interface Props {
  project: Project
  mode?: ProjectCardMode // 显示模式，默认为 'market'
}

interface Emits {
  (e: 'demo', project: Project): void
  (e: 'purchase', project: Project): void
  (e: 'edit', project: Project): void
  (e: 'delete', project: Project): void
  (e: 'download', project: Project): void
  (e: 'favorite', project: Project): void
  (e: 'unfavorite', project: Project): void
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'market'
})
const emit = defineEmits<Emits>()
const router = useRouter()

// 开发环境调试信息
if (import.meta.env.DEV) {
  console.log('ProjectCard props.project:', props.project)
  console.log('ProjectCard mode:', props.mode)
}

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

/**
 * 处理编辑按钮点击
 */
const handleEdit = () => {
  emit('edit', props.project)
}

/**
 * 处理删除按钮点击
 */
const handleDelete = () => {
  emit('delete', props.project)
}

/**
 * 处理下载按钮点击
 */
const handleDownload = () => {
  emit('download', props.project)
}

/**
 * 处理收藏按钮点击
 */
const handleFavorite = () => {
  emit('favorite', props.project)
}

/**
 * 处理取消收藏按钮点击
 */
const handleUnfavorite = () => {
  emit('unfavorite', props.project)
}

/**
 * 格式化评分显示
 */
const formatRating = (rating: number | string): string => {
  const numRating = typeof rating === 'string' ? parseFloat(rating) : rating
  return numRating.toFixed(1)
}

/**
 * 格式化下载量显示
 */
const formatDownloads = (downloads: number): string => {
  if (downloads >= 10000) {
    return `${(downloads / 10000).toFixed(1)}万`
  } else if (downloads >= 1000) {
    return `${(downloads / 1000).toFixed(1)}k`
  }
  return downloads.toString()
}

/**
 * 格式化浏览量显示
 */
const formatViews = (views: number): string => {
  if (views >= 10000) {
    return `${(views / 10000).toFixed(1)}万`
  } else if (views >= 1000) {
    return `${(views / 1000).toFixed(1)}k`
  }
  return views.toString()
}

/**
 * 格式化日期显示
 */
const formatDate = (dateString: string): string => {
  try {
    // 处理后端返回的LocalDateTime格式，如 "2024-01-15T10:30:00"
    const date = new Date(dateString)
    if (isNaN(date.getTime())) {
      return '未知'
    }

    const now = new Date()
    const diffTime = Math.abs(now.getTime() - date.getTime())
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

    if (diffDays === 0) {
      return '今天'
    } else if (diffDays === 1) {
      return '昨天'
    } else if (diffDays <= 7) {
      return `${diffDays}天前`
    } else if (diffDays <= 30) {
      return `${Math.ceil(diffDays / 7)}周前`
    } else if (diffDays <= 365) {
      return `${Math.ceil(diffDays / 30)}月前`
    } else {
      return `${Math.ceil(diffDays / 365)}年前`
    }
  } catch (error) {
    console.warn('日期格式化失败:', dateString, error)
    return '未知'
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.project-card {
  background: $gradient-card;
  border-radius: $radius-xl;
  overflow: hidden; // 恢复hidden，但调整内部布局
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
    padding: $spacing-lg $spacing-lg $spacing-md $spacing-lg; // 保持适当的底部padding

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
      margin-bottom: $spacing-md;

      .tag {
        padding: $spacing-xs $spacing-sm;
        background: $primary-light;
        color: $primary-color;
        border-radius: $radius-sm;
        font-size: $font-size-xs;
        font-weight: 500;
      }
    }

    .project-stats {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: $spacing-xs;
      margin: $spacing-sm 0 0 0; // 只有顶部margin
      padding: $spacing-sm $spacing-md $spacing-md $spacing-md; // 增加底部padding确保内容完全显示
      background: rgba(255, 255, 255, 0.03);
      border-radius: 0; // 移除圆角，让它与卡片边界对齐
      border-top: 1px solid rgba(255, 255, 255, 0.1);

      .stat-item {
        display: flex;
        align-items: center;
        gap: $spacing-xs;
        font-size: $font-size-xs;
        color: $text-secondary;
        flex: 1;
        justify-content: center;
        padding: $spacing-xs;
        border-radius: $radius-sm;
        transition: all 0.2s ease;

        &:hover {
          background: rgba(255, 255, 255, 0.05);
          color: $text-primary;
        }

        i {
          width: 10px;
          text-align: center;
          opacity: 0.8;
          font-size: 10px;

          &.fa-star {
            color: $warning-color;
          }
          &.fa-download {
            color: $success-color;
          }
          &.fa-clock {
            color: $info-color;
          }
          &.fa-user {
            color: $primary-color;
          }
        }

        span {
          font-weight: 500;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          font-size: $font-size-xs;
        }
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

          &.btn-danger {
            background: #f56565;
            color: white;
            box-shadow: 0 2px 8px rgba(245, 101, 101, 0.3);

            &:hover {
              background: #e53e3e;
              transform: translateY(-1px);
              box-shadow: 0 4px 12px rgba(245, 101, 101, 0.4);
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
      padding: $spacing-md $spacing-md $spacing-sm $spacing-md; // 移动端保持适当的底部padding

      .project-stats {
        flex-wrap: wrap;
        gap: $spacing-xs;
        margin: $spacing-xs 0 0 0; // 只有顶部margin
        padding: $spacing-xs $spacing-sm $spacing-sm $spacing-sm; // 增加底部padding

        .stat-item {
          font-size: $font-size-xs;
          padding: $spacing-xs;
          flex: 1;
          min-width: 0; // 允许flex项目收缩

          span {
            font-size: 10px;
          }

          i {
            font-size: 8px;
          }
        }
      }

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
