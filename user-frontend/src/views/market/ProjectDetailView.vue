<template>
  <div class="project-detail-view">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 项目详情内容 -->
    <div v-else-if="project" class="project-detail">
      <div class="container">
        <!-- 项目头部信息 -->
        <div class="project-header">
          <div class="project-main-info">
            <div class="project-thumbnail">
              <img
                v-if="!imageLoadFailed"
                :src="project.thumbnail || '/images/default-project.jpg'"
                :alt="project.title"
                @error="handleImageError"
                @load="handleImageLoad"
              />
              <div v-else class="image-placeholder">
                <el-icon class="placeholder-icon"><Picture /></el-icon>
                <span class="placeholder-text">暂无图片</span>
              </div>
            </div>
            <div class="project-info">
              <h1 class="project-title">{{ project.title }}</h1>
              <p class="project-description">{{ project.description }}</p>

              <!-- 项目标签 -->
              <div class="project-tags">
                <el-tag
                  v-for="tag in project.tags"
                  :key="tag"
                  type="info"
                >
                  {{ tag }}
                </el-tag>
              </div>

              <!-- 项目统计 -->
              <div class="project-stats">
                <div class="stat-item">
                  <el-icon><Star /></el-icon>
                  <span>{{ formatRating(project.rating || 0) }}</span>
                </div>
                <div class="stat-item">
                  <el-icon><View /></el-icon>
                  <span>{{ formatNumber(project.viewCount || project.views || 0) }}</span>
                </div>
                <div class="stat-item">
                  <el-icon><Download /></el-icon>
                  <span>{{ formatNumber(project.downloadCount || project.downloads || 0) }}</span>
                </div>
                <div class="stat-item">
                  <el-icon><User /></el-icon>
                  <span>{{ project.username || project.author || '未知作者' }}</span>
                </div>
                <div class="stat-item">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(project.createdTime || project.createdAt) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 购买区域 -->
          <div class="purchase-section">
            <div class="price-info">
              <div class="current-price">
                <span class="price">
                  <i class="fas fa-coins"></i>
                  {{ project.price }}</span>
              </div>
            </div>

            <div class="action-buttons">
              <el-button
                type="primary"
                size="large"
                @click="handlePurchase"
                :disabled="!userStore.isAuthenticated"
              >
                <el-icon><ShoppingCart /></el-icon>
                {{ userStore.isAuthenticated ? '立即购买' : '登录后购买' }}
              </el-button>

              <el-button
                size="large"
                @click="handleDemo"
                :disabled="!userStore.isAuthenticated"
              >
                <el-icon><VideoPlay /></el-icon>
                {{ userStore.isAuthenticated ? '在线演示' : '登录后演示' }}
              </el-button>
            </div>

            <div v-if="!userStore.isAuthenticated" class="login-tip">
              <p>
                <router-link :to="{ path: '/login', query: { redirect: route.fullPath } }" class="login-link">登录</router-link>
                后即可购买和体验项目
              </p>
            </div>
          </div>
        </div>

        <!-- 项目详细信息 -->
        <div class="project-content">
          <!-- 功能特性 -->
          <div class="content-section">
            <h2>功能特性</h2>
            <ul class="feature-list">
              <li v-for="feature in project.features" :key="feature">
                <el-icon><Check /></el-icon>
                {{ feature }}
              </li>
            </ul>
          </div>

          <!-- 技术栈 -->
          <div class="content-section">
            <h2>技术栈</h2>
            <div class="tech-stack">
              <el-tag
                v-for="tech in project.techStack"
                :key="tech"
                type="success"
                size="large"
              >
                {{ tech }}
              </el-tag>
            </div>
          </div>

          <!-- 项目信息 -->
          <div class="content-section">
            <h2>项目信息</h2>
            <div class="project-meta">
              <div class="meta-item">
                <span class="label">源码大小：</span>
                <span class="value">{{ project.sourceSize }}</span>
              </div>
              <div class="meta-item">
                <span class="label">开源协议：</span>
                <span class="value">{{ project.license }}</span>
              </div>
              <div class="meta-item">
                <span class="label">更新时间：</span>
                <span class="value">{{ formatDate(project.updatedTime || project.updatedAt) }}</span>
              </div>
              <div v-if="project.demoUrl" class="meta-item">
                <span class="label">演示地址：</span>
                <a :href="project.demoUrl" target="_blank" class="demo-link">
                  {{ project.demoUrl }}
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <el-result
        icon="error"
        title="项目不存在"
        sub-title="抱歉，您访问的项目不存在或已被删除"
      >
        <template #extra>
          <el-button type="primary" @click="router.push('/market')">
            返回项目市场
          </el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Star,
  Download,
  User,
  Calendar,
  ShoppingCart,
  VideoPlay,
  Check,
  Picture,
  View
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { publicProjectApi } from '@/api/modules/public'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 项目详情类型定义
interface ProjectDetail {
  id: number
  title: string
  description: string
  thumbnail?: string
  coverImage?: string
  price: number
  rating?: number
  viewCount?: number
  views?: number
  downloadCount?: number
  downloads?: number
  username?: string
  author?: string
  createdTime?: string
  createdAt?: string
  updatedTime?: string
  updatedAt?: string
  tags?: string[]
  techStack?: string[]
  features?: string[]
  sourceSize?: string
  license?: string
  demoUrl?: string
}

// 响应式数据
const loading = ref(false)
const project = ref<ProjectDetail | null>(null)
const imageLoadFailed = ref(false)
const hasTriedFallback = ref(false)

/**
 * 获取项目详情
 */
const fetchProjectDetail = async () => {
  const projectId = route.params.id as string
  if (!projectId) {
    return
  }

  loading.value = true
  try {
    const response = await publicProjectApi.getProjectDetail(Number(projectId))
    project.value = response.data
    // 重置图片加载状态
    imageLoadFailed.value = false
    hasTriedFallback.value = false
  } catch (error) {
    console.error('获取项目详情失败:', error)
    ElMessage.error('获取项目详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理图片加载错误
 */
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement

  // 如果已经尝试过默认图片或者当前就是默认图片，则显示占位符
  if (hasTriedFallback.value || img.src.includes('/images/default-project.jpg')) {
    imageLoadFailed.value = true
    return
  }

  // 第一次失败，尝试加载默认图片
  hasTriedFallback.value = true
  img.src = '/images/default-project.jpg'
}

/**
 * 处理图片加载成功
 */
const handleImageLoad = () => {
  imageLoadFailed.value = false
}

/**
 * 处理购买
 */
const handlePurchase = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再购买项目')
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  ElMessage.info('购买功能开发中...')
}

/**
 * 处理演示
 */
const handleDemo = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再体验项目演示')
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  ElMessage.info('演示功能开发中...')
}

/**
 * 格式化评分显示
 */
const formatRating = (rating: number): string => {
  return rating.toFixed(1)
}

/**
 * 格式化数字显示（浏览量、下载量等）
 */
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}万`
  } else if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}k`
  }
  return num.toString()
}

/**
 * 格式化日期显示
 */
const formatDate = (dateStr?: string): string => {
  if (!dateStr) return '未知时间'

  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  } catch {
    return '未知时间'
  }
}

// 生命周期
onMounted(() => {
  fetchProjectDetail()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.project-detail-view {
  min-height: 100vh;
  padding: $spacing-xl 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 $spacing-lg;
}

.loading-container {
  padding: $spacing-3xl;
}

.project-header {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: $spacing-3xl;
  margin-bottom: $spacing-3xl;
  padding-bottom: $spacing-3xl;
  border-bottom: 1px solid var(--border-color);

  .project-main-info {
    display: grid;
    grid-template-columns: 300px 1fr;
    gap: $spacing-xl;

    .project-thumbnail {
      img {
        width: 100%;
        height: 200px;
        object-fit: cover;
        border-radius: $border-radius-lg;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .image-placeholder {
        width: 100%;
        height: 200px;
        border-radius: $border-radius-lg;
        background: var(--bg-secondary);
        border: 2px dashed var(--border-color);
        @include flex-center();
        flex-direction: column;
        gap: $spacing-sm;
        color: var(--text-secondary);

        .placeholder-icon {
          font-size: 2rem;
          opacity: 0.6;
        }

        .placeholder-text {
          font-size: $font-size-sm;
          opacity: 0.8;
        }
      }
    }

    .project-info {
      .project-title {
        font-size: $font-size-3xl;
        font-weight: $font-weight-bold;
        color: var(--text-primary);
        margin: 0 0 $spacing-md 0;
        line-height: 1.3;
      }

      .project-description {
        color: var(--text-secondary);
        font-size: $font-size-lg;
        line-height: 1.6;
        margin: 0 0 $spacing-lg 0;
      }

      .project-tags {
        @include flex-start();
        gap: $spacing-sm;
        margin-bottom: $spacing-lg;
        flex-wrap: wrap;
      }

      .project-stats {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: $spacing-md;

        .stat-item {
          @include flex-start();
          gap: $spacing-sm;
          color: var(--text-secondary);
          font-size: $font-size-sm;

          .el-icon {
            color: var(--primary-color);
          }
        }
      }
    }
  }

  .purchase-section {
    background: var(--bg-secondary);
    padding: $spacing-xl;
    border-radius: $border-radius-lg;
    border: 1px solid var(--border-color);
    height: fit-content;

    .price-info {
      text-align: center;
      margin-bottom: $spacing-xl;

      .current-price {
        .price {
          font-size: $font-size-4xl;
          font-weight: $font-weight-bold;
          color: var(--primary-color);

          i {
          color: $warning-color;
          }
        }

        .unit {
          font-size: $font-size-lg;
          color: var(--text-secondary);
          margin-left: $spacing-sm;
        }
      }
    }

    .action-buttons {
      @include flex-center();
      flex-direction: column;
      gap: $spacing-md;
      margin-bottom: $spacing-lg;

      .el-button {
        width: 100%;
      }
    }

    .login-tip {
      text-align: center;
      color: var(--text-secondary);
      font-size: $font-size-sm;

      .login-link {
        color: var(--primary-color);
        text-decoration: none;

        &:hover {
          text-decoration: underline;
        }
      }
    }
  }
}

.project-content {
  .content-section {
    margin-bottom: $spacing-3xl;

    h2 {
      font-size: $font-size-2xl;
      font-weight: $font-weight-bold;
      color: var(--text-primary);
      margin: 0 0 $spacing-lg 0;
      padding-bottom: $spacing-md;
      border-bottom: 2px solid var(--primary-color);
    }

    .feature-list {
      list-style: none;
      padding: 0;

      li {
        @include flex-start();
        gap: $spacing-sm;
        margin-bottom: $spacing-md;
        color: var(--text-primary);

        .el-icon {
          color: var(--success-color);
          margin-top: 2px;
        }
      }
    }

    .tech-stack {
      @include flex-start();
      gap: $spacing-md;
      flex-wrap: wrap;
    }

    .project-meta {
      .meta-item {
        @include flex-start();
        gap: $spacing-sm;
        margin-bottom: $spacing-md;
        padding: $spacing-md;
        background: var(--bg-secondary);
        border-radius: $border-radius-md;

        .label {
          font-weight: $font-weight-medium;
          color: var(--text-secondary);
          min-width: 100px;
        }

        .value {
          color: var(--text-primary);
        }

        .demo-link {
          color: var(--primary-color);
          text-decoration: none;

          &:hover {
            text-decoration: underline;
          }
        }
      }
    }
  }
}

.error-state {
  padding: $spacing-3xl;
}

// 响应式设计
@media (max-width: 768px) {
  .project-header {
    grid-template-columns: 1fr;
    gap: $spacing-xl;

    .project-main-info {
      grid-template-columns: 1fr;
      gap: $spacing-lg;

      .project-thumbnail {
        order: -1;
      }
    }

    .purchase-section {
      order: -1;
    }
  }
}
</style>
