<template>
  <div class="my-projects-view">
    <div class="page-header">
      <h1 class="page-title">我的项目</h1>
      <p class="page-subtitle">管理您上传和购买的项目</p>
      <div class="header-actions">
        <el-button type="primary" @click="handleUploadProject">
          <el-icon><Plus /></el-icon>
          上传项目
        </el-button>
      </div>
    </div>

    <!-- 项目统计 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><FolderOpened /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.uploadedCount }}</div>
            <div class="stat-label">已上传</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.purchasedCount }}</div>
            <div class="stat-label">已购买</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Download /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalDownloads }}</div>
            <div class="stat-label">总下载量</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Coin /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.totalEarnings }}</div>
            <div class="stat-label">总收益</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 项目列表 -->
    <div class="projects-section">
      <div class="section-header">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="我上传的" name="uploaded" />
          <el-tab-pane label="我购买的" name="purchased" />
        </el-tabs>

        <div class="section-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索项目..."
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="4" animated />
      </div>

      <!-- 项目列表 -->
      <div v-else-if="projects.length > 0" class="projects-list">
        <div
          v-for="project in projects"
          :key="project.id"
          class="project-item"
        >
          <div class="project-thumbnail">
            <img
              :src="project.thumbnail || '/images/default-project.jpg'"
              :alt="project.title"
            />
          </div>

          <div class="project-info">
            <h3 class="project-title">{{ project.title }}</h3>
            <p class="project-description">{{ project.description }}</p>

            <div class="project-meta">
              <el-tag :type="getStatusType(project.status)">
                {{ getStatusText(project.status) }}
              </el-tag>
              <span class="project-date">{{ project.createdAt }}</span>
              <span class="project-price">¥{{ project.price }}</span>
            </div>
          </div>

          <div class="project-actions">
            <el-button size="small" @click="handleViewProject(project)">
              查看详情
            </el-button>
            <el-button
              v-if="activeTab === 'uploaded'"
              size="small"
              type="primary"
              @click="handleEditProject(project)"
            >
              编辑
            </el-button>
            <el-button
              v-if="activeTab === 'purchased'"
              size="small"
              type="success"
              @click="handleDownloadProject(project)"
            >
              下载
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty :description="getEmptyDescription()" />
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalElements"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
    Coin,
    Download,
    FolderOpened,
    Plus,
    Search,
    ShoppingCart
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const activeTab = ref('uploaded')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)

const projects = ref<any[]>([])
const stats = ref({
  uploadedCount: 5,
  purchasedCount: 12,
  totalDownloads: 1250,
  totalEarnings: 8900
})

const totalPages = computed(() => Math.ceil(totalElements.value / pageSize.value))

/**
 * 获取项目状态类型
 */
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return statusMap[status] || 'info'
}

/**
 * 获取项目状态文本
 */
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'DRAFT': '草稿',
    'PENDING': '审核中',
    'APPROVED': '已发布',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取空状态描述
 */
const getEmptyDescription = () => {
  return activeTab.value === 'uploaded'
    ? '您还没有上传任何项目'
    : '您还没有购买任何项目'
}

/**
 * 处理标签页切换
 */
const handleTabChange = (tabName: string) => {
  activeTab.value = tabName
  currentPage.value = 1
  fetchProjects()
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  fetchProjects()
}

/**
 * 处理页码变化
 */
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchProjects()
}

/**
 * 获取项目列表
 */
const fetchProjects = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 模拟数据
    const mockProjects = Array.from({ length: 5 }, (_, index) => ({
      id: index + 1,
      title: `项目 ${index + 1}`,
      description: `这是第 ${index + 1} 个项目的描述`,
      thumbnail: `/images/project-${index + 1}.jpg`,
      price: 299 + index * 100,
      status: ['DRAFT', 'PENDING', 'APPROVED', 'REJECTED'][index % 4],
      createdAt: `2024-01-${String(index + 1).padStart(2, '0')}`
    }))

    projects.value = mockProjects
    totalElements.value = mockProjects.length
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error('获取项目列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理上传项目
 */
const handleUploadProject = () => {
  ElMessage.info('上传项目功能开发中...')
}

/**
 * 处理查看项目
 */
const handleViewProject = (project: any) => {
  router.push(`/market/project/${project.id}`)
}

/**
 * 处理编辑项目
 */
const handleEditProject = (project: any) => {
  ElMessage.info('编辑项目功能开发中...')
}

/**
 * 处理下载项目
 */
const handleDownloadProject = (project: any) => {
  ElMessage.info('下载项目功能开发中...')
}

// 生命周期
onMounted(() => {
  fetchProjects()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.my-projects-view {
  .page-header {
    @include flex-between();
    align-items: flex-start;
    margin-bottom: $spacing-xl;

    .page-title {
      font-size: $font-size-3xl;
      font-weight: $font-weight-bold;
      color: var(--text-primary);
      margin: 0 0 $spacing-xs 0;
    }

    .page-subtitle {
      color: var(--text-secondary);
      margin: 0;
    }
  }

  .stats-section {
    margin-bottom: $spacing-xl;

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: $spacing-lg;

      .stat-card {
        background: var(--bg-primary);
        border: 1px solid var(--border-color);
        border-radius: $border-radius-lg;
        padding: $spacing-lg;
        @include flex-start();
        gap: $spacing-md;

        .stat-icon {
          width: 48px;
          height: 48px;
          background: var(--primary-color);
          border-radius: 50%;
          @include flex-center();
          color: white;

          .el-icon {
            font-size: 24px;
          }
        }

        .stat-info {
          .stat-number {
            font-size: $font-size-2xl;
            font-weight: $font-weight-bold;
            color: var(--text-primary);
          }

          .stat-label {
            color: var(--text-secondary);
            font-size: $font-size-sm;
          }
        }
      }
    }
  }

  .projects-section {
    .section-header {
      @include flex-between();
      margin-bottom: $spacing-lg;

      .section-actions {
        .el-input {
          width: 300px;
        }
      }
    }

    .loading-container {
      padding: $spacing-xl;
    }

    .projects-list {
      .project-item {
        background: var(--bg-primary);
        border: 1px solid var(--border-color);
        border-radius: $border-radius-lg;
        padding: $spacing-lg;
        margin-bottom: $spacing-md;
        @include flex-start();
        gap: $spacing-lg;

        .project-thumbnail {
          width: 120px;
          height: 80px;
          border-radius: $border-radius-md;
          overflow: hidden;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .project-info {
          flex: 1;

          .project-title {
            font-size: $font-size-lg;
            font-weight: $font-weight-bold;
            color: var(--text-primary);
            margin: 0 0 $spacing-sm 0;
          }

          .project-description {
            color: var(--text-secondary);
            margin: 0 0 $spacing-md 0;
            @include text-ellipsis-multiline(2);
          }

          .project-meta {
            @include flex-start();
            gap: $spacing-md;
            align-items: center;

            .project-date,
            .project-price {
              color: var(--text-secondary);
              font-size: $font-size-sm;
            }

            .project-price {
              color: var(--primary-color);
              font-weight: $font-weight-medium;
            }
          }
        }

        .project-actions {
          @include flex-center();
          gap: $spacing-sm;
        }
      }
    }

    .empty-state {
      padding: $spacing-3xl;
      text-align: center;
    }

    .pagination-container {
      @include flex-center();
      justify-content: center;
      padding-top: $spacing-xl;
    }
  }
}
</style>
