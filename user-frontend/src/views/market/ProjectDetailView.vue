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
              <!-- 已购买：显示下载按钮 -->
              <el-button
                v-if="userStore.isAuthenticated && hasPurchased"
                type="success"
                size="large"
                @click="handleDownload"
                :loading="downloading"
              >
                <el-icon><Download /></el-icon>
                {{ downloading ? '下载中...' : '下载项目' }}
              </el-button>

              <!-- 未购买：显示购买按钮 -->
              <el-button
                v-else
                type="primary"
                size="large"
                @click="handlePurchase"
                :disabled="!userStore.isAuthenticated || checkingPurchase"
                :loading="checkingPurchase"
              >
                <el-icon><ShoppingCart /></el-icon>
                {{
                  checkingPurchase ? '检查中...' :
                  userStore.isAuthenticated ? '立即购买' : '登录后购买'
                }}
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
          <!-- 内容导航标签页 -->
          <div class="content-tabs">
            <el-tabs v-model="activeTab" type="card" class="modern-tabs">
              <el-tab-pane label="项目介绍" name="overview">
                <div class="tab-content">
                  <!-- 功能特性 -->
                  <div class="content-section glass-card">
                    <h2>
                      <el-icon><Star /></el-icon>
                      功能特性
                    </h2>
                    <ul class="feature-list">
                      <li v-for="feature in project.features" :key="feature">
                        <el-icon><Check /></el-icon>
                        {{ feature }}
                      </li>
                    </ul>
                  </div>

                  <!-- 技术栈 -->
                  <div class="content-section glass-card">
                    <h2>
                      <el-icon><Tools /></el-icon>
                      技术栈
                    </h2>
                    <div class="tech-stack">
                      <el-tag
                        v-for="tech in project.techStack"
                        :key="tech"
                        type="success"
                        size="large"
                        class="tech-tag"
                      >
                        {{ tech }}
                      </el-tag>
                    </div>
                  </div>

                  <!-- 项目信息 -->
                  <div class="content-section glass-card">
                    <h2>
                      <el-icon><InfoFilled /></el-icon>
                      项目信息
                    </h2>
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
              </el-tab-pane>

              <el-tab-pane label="项目文档" name="documentation">
                <div class="tab-content">
                  <div class="content-section glass-card">
                    <MarkdownRenderer
                      :content="project.documentContent || project.readmeContent || '暂无项目文档'"
                      :glass="false"
                    />
                  </div>
                </div>
              </el-tab-pane>

              <el-tab-pane label="演示预览" name="preview">
                <div class="tab-content">
                  <div class="content-section glass-card">
                    <h2>
                      <el-icon><Picture /></el-icon>
                      项目演示
                    </h2>
                    <div v-if="project.mediaFiles && project.mediaFiles.length > 0" class="media-gallery">
                      <div
                        v-for="media in project.mediaFiles"
                        :key="media.id"
                        class="media-item"
                      >
                        <img
                          v-if="media.fileType === 'IMAGE'"
                          :src="media.fileUrl"
                          :alt="media.description || media.fileName"
                          class="media-image"
                          @click="previewImage(media.fileUrl)"
                        />
                        <video
                          v-else-if="media.fileType === 'VIDEO'"
                          :src="media.fileUrl"
                          controls
                          class="media-video"
                        />
                        <div class="media-info">
                          <span class="media-name">{{ media.fileName }}</span>
                          <span class="media-desc">{{ media.description }}</span>
                        </div>
                      </div>
                    </div>
                    <div v-else class="empty-media">
                      <el-empty description="暂无演示文件" />
                    </div>
                  </div>
                </div>
              </el-tab-pane>

              <el-tab-pane label="更新日志" name="changelog">
                <div class="tab-content">
                  <div class="content-section glass-card">
                    <MarkdownRenderer
                      :content="project.changelogContent || '暂无更新日志'"
                      :glass="false"
                    />
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
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
import { ref, onMounted, watch } from 'vue'
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
  View,
  Tools,
  InfoFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { publicProjectApi } from '@/api/modules/public'
import { orderApi } from '@/api/modules/order'
import { downloadApi } from '@/api/modules/download'
import MarkdownRenderer from '@/components/ui/MarkdownRenderer.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 项目媒体文件类型
interface ProjectMediaFile {
  id: number
  fileName: string
  fileUrl: string
  fileType: 'IMAGE' | 'VIDEO' | 'DEMO'
  mimeType: string
  fileSize: number
  description?: string
}

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
  // 新增字段
  documentContent?: string  // Markdown格式的项目文档
  mediaFiles?: ProjectMediaFile[]  // 项目媒体文件列表
  readmeContent?: string  // README文档内容
  changelogContent?: string  // 更新日志内容
}

// 响应式数据
const loading = ref(false)
const project = ref<ProjectDetail | null>(null)
const imageLoadFailed = ref(false)
const hasTriedFallback = ref(false)
const hasPurchased = ref(false)
const checkingPurchase = ref(false)
const downloading = ref(false)
const activeTab = ref('overview')

/**
 * 检查用户是否已购买项目
 */
const checkPurchaseStatus = async (projectId: number) => {
  if (!userStore.isAuthenticated) {
    hasPurchased.value = false
    return
  }

  checkingPurchase.value = true
  try {
    const response = await orderApi.hasUserPurchasedProject(projectId)
    hasPurchased.value = response.data
  } catch (error) {
    console.error('检查购买状态失败:', error)
    hasPurchased.value = false
  } finally {
    checkingPurchase.value = false
  }
}

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

    // 临时添加示例数据用于测试新功能
    if (project.value) {
      project.value.documentContent = project.value.documentContent || `
# 项目介绍

这是一个基于Vue3的现代化前端项目，展示了最新的前端开发技术和最佳实践。

## 🚀 技术栈

- **框架**: Vue 3.3+ (Composition API)
- **构建工具**: Vite 4.4+
- **路由**: Vue Router 4.2+
- **状态管理**: Pinia 2.1+
- **UI组件库**: Element Plus 2.3+

## 📦 安装和运行

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖
\`\`\`bash
npm install
\`\`\`

### 开发环境运行
\`\`\`bash
npm run dev
\`\`\`

## 🌟 功能特性

- ✅ 响应式设计，支持多端适配
- ✅ 组件化开发，代码复用性高
- ✅ TypeScript支持，类型安全
- ✅ 路由懒加载，性能优化
`

      project.value.changelogContent = project.value.changelogContent || `
# 更新日志

## v2.1.0 (2024-01-15)

### 新增功能
- 🎉 新增项目详情页Markdown文档展示
- 🎉 新增媒体文件预览功能
- 🎉 新增购买状态检查和下载功能

### 优化改进
- 🔧 优化UI设计，采用毛玻璃效果
- 🔧 改进响应式布局
- 🔧 提升用户体验

### 问题修复
- 🐛 修复图片加载失败的问题
- 🐛 修复移动端显示异常

## v2.0.0 (2023-12-01)

### 重大更新
- 🎉 全新的UI设计
- 🎉 重构项目架构
`

      // 添加示例媒体文件
      project.value.mediaFiles = project.value.mediaFiles || [
        {
          id: 1,
          fileName: 'screenshot1.png',
          fileUrl: '/images/default-project.jpg',
          fileType: 'IMAGE' as const,
          mimeType: 'image/png',
          fileSize: 1024000,
          description: '项目主界面截图'
        },
        {
          id: 2,
          fileName: 'demo.mp4',
          fileUrl: 'https://www.w3schools.com/html/mov_bbb.mp4',
          fileType: 'VIDEO' as const,
          mimeType: 'video/mp4',
          fileSize: 5120000,
          description: '项目演示视频'
        }
      ]
    }

    // 重置图片加载状态
    imageLoadFailed.value = false
    hasTriedFallback.value = false

    // 检查购买状态
    await checkPurchaseStatus(Number(projectId))
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
 * 处理项目下载
 */
const handleDownload = async () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再下载项目')
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  if (!project.value) {
    ElMessage.error('项目信息加载失败')
    return
  }

  downloading.value = true
  try {
    // 检查下载权限
    const permissionResponse = await downloadApi.checkDownloadPermission(project.value.id)
    if (!permissionResponse.data.hasPermission) {
      ElMessage.error(permissionResponse.data.reason || '您没有下载此项目的权限')
      return
    }

    // 下载项目文件
    const blob = await downloadApi.downloadProject(project.value.id)

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${project.value.title}.zip`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('项目下载成功')
  } catch (error) {
    console.error('下载项目失败:', error)
    let errorMessage = '下载项目失败，请稍后重试'
    if (error && typeof error === 'object' && 'response' in error) {
      const response = (error as { response?: { data?: { message?: string } } }).response
      errorMessage = response?.data?.message || errorMessage
    }
    ElMessage.error(errorMessage)
  } finally {
    downloading.value = false
  }
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

  if (!project.value) {
    ElMessage.error('项目信息加载失败')
    return
  }

  // 跳转到购买页面
  router.push(`/user/project/purchase/${project.value.id}`)
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
 * 预览图片
 */
const previewImage = (imageUrl: string) => {
  // 创建图片预览弹窗
  const overlay = document.createElement('div')
  overlay.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.8);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
    cursor: pointer;
  `

  const img = document.createElement('img')
  img.src = imageUrl
  img.style.cssText = `
    max-width: 90%;
    max-height: 90%;
    object-fit: contain;
    border-radius: 8px;
  `

  overlay.appendChild(img)
  document.body.appendChild(overlay)

  // 点击关闭预览
  overlay.addEventListener('click', () => {
    document.body.removeChild(overlay)
  })

  // ESC键关闭预览
  const handleKeydown = (e: KeyboardEvent) => {
    if (e.key === 'Escape') {
      document.body.removeChild(overlay)
      document.removeEventListener('keydown', handleKeydown)
    }
  }
  document.addEventListener('keydown', handleKeydown)
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

// 监听用户登录状态变化，重新检查购买状态
watch(() => userStore.isAuthenticated, (newValue) => {
  if (newValue && project.value) {
    checkPurchaseStatus(project.value.id)
  } else {
    hasPurchased.value = false
  }
})

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
  .content-tabs {
    margin-top: $spacing-xl;

    .modern-tabs {
      :deep(.el-tabs__header) {
        margin: 0 0 $spacing-xl 0;

        .el-tabs__nav-wrap {
          &::after {
            display: none;
          }
        }

        .el-tabs__item {
          background: rgba(255, 255, 255, 0.1);
          backdrop-filter: blur(10px);
          border: 1px solid rgba(255, 255, 255, 0.2);
          border-radius: $border-radius-lg $border-radius-lg 0 0;
          margin-right: $spacing-sm;
          padding: $spacing-md $spacing-xl;
          font-weight: $font-weight-medium;
          transition: all 0.3s ease;

          &:hover {
            background: rgba(255, 255, 255, 0.15);
            transform: translateY(-2px);
          }

          &.is-active {
            background: rgba(255, 255, 255, 0.2);
            color: var(--primary-color);
            border-bottom-color: transparent;
          }
        }
      }

      :deep(.el-tabs__content) {
        padding: 0;
      }
    }
  }

  .tab-content {
    min-height: 400px;
  }

  .glass-card {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: $border-radius-lg;
    padding: $spacing-xl;
    margin-bottom: $spacing-xl;
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.15);
      transform: translateY(-2px);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    }
  }

  .content-section {
    h2 {
      @include flex-start();
      gap: $spacing-sm;
      font-size: $font-size-2xl;
      font-weight: $font-weight-bold;
      color: var(--text-primary);
      margin: 0 0 $spacing-lg 0;
      padding-bottom: $spacing-md;
      border-bottom: 2px solid var(--primary-color);

      .el-icon {
        color: var(--primary-color);
      }
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

      .tech-tag {
        background: linear-gradient(135deg, $success-color, $success-hover);
        border: none;
        color: white;
        font-weight: $font-weight-medium;
        padding: $spacing-sm $spacing-md;
        border-radius: $border-radius-lg;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
      }
    }

    .media-gallery {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: $spacing-lg;

      .media-item {
        background: rgba(255, 255, 255, 0.05);
        border-radius: $border-radius-lg;
        overflow: hidden;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
        }

        .media-image, .media-video {
          width: 100%;
          height: 200px;
          object-fit: cover;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            transform: scale(1.05);
          }
        }

        .media-video {
          cursor: default;

          &:hover {
            transform: none;
          }
        }

        .media-info {
          padding: $spacing-md;

          .media-name {
            display: block;
            font-weight: $font-weight-medium;
            color: var(--text-primary);
            margin-bottom: $spacing-xs;
          }

          .media-desc {
            display: block;
            font-size: $font-size-sm;
            color: var(--text-secondary);
            line-height: 1.4;
          }
        }
      }
    }

    .empty-media {
      text-align: center;
      padding: $spacing-3xl;
      color: var(--text-secondary);
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
