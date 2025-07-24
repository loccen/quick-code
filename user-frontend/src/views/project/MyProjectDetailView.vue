<template>
  <div class="my-project-detail-view">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 项目详情 -->
    <div v-else-if="project" class="project-content">
      <!-- 项目头部信息 -->
      <div class="project-header">
        <div class="project-info">
          <h1 class="project-title">{{ project.title }}</h1>
          <div class="project-meta">
            <el-tag :type="getStatusType(project.status)">{{ project.statusDesc }}</el-tag>
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ project.username }}
            </span>
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ formatDate(project.createdTime) }}
            </span>
            <span class="meta-item">
              <el-icon><View /></el-icon>
              {{ project.viewCount }} 浏览
            </span>
          </div>
        </div>
        <div class="project-actions">
          <el-button type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon>
            编辑项目
          </el-button>
          <el-button @click="handleDelete" type="danger">
            <el-icon><Delete /></el-icon>
            删除项目
          </el-button>
        </div>
      </div>

      <!-- 项目描述 -->
      <ModernCard title="项目描述" class="description-card">
        <p class="project-description">{{ project.description }}</p>
      </ModernCard>

      <!-- 项目信息 -->
      <div class="project-details">
        <ModernCard title="基本信息" class="info-card">
          <div class="info-grid">
            <div class="info-item">
              <label>项目分类</label>
              <span>{{ project.categoryName }}</span>
            </div>
            <div class="info-item">
              <label>项目价格</label>
              <span>{{ project.isFree ? '免费' : `${project.price} 积分` }}</span>
            </div>
            <div class="info-item">
              <label>下载次数</label>
              <span>{{ project.downloadCount }}</span>
            </div>
            <div class="info-item">
              <label>点赞次数</label>
              <span>{{ project.likeCount }}</span>
            </div>
          </div>
        </ModernCard>

        <!-- 技术栈 -->
        <ModernCard title="技术栈" class="tech-card">
          <div class="tech-tags">
            <el-tag
              v-for="tech in project.techStack"
              :key="tech"
              type="info"
              class="tech-tag"
            >
              {{ tech }}
            </el-tag>
          </div>
        </ModernCard>

        <!-- 项目标签 -->
        <ModernCard title="项目标签" class="tags-card">
          <div class="project-tags">
            <el-tag
              v-for="tag in project.tags"
              :key="tag"
              class="project-tag"
            >
              {{ tag }}
            </el-tag>
          </div>
        </ModernCard>
      </div>

      <!-- 项目文件 -->
      <ModernCard title="项目文件" class="files-card">
        <div v-if="filesLoading" class="files-loading">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="projectFiles.length > 0" class="files-list">
          <div
            v-for="file in projectFiles"
            :key="file.id"
            class="file-item"
          >
            <div class="file-info">
              <el-icon class="file-icon"><Document /></el-icon>
              <div class="file-details">
                <div class="file-name">{{ file.originalName }}</div>
                <div class="file-meta">
                  <span>{{ file.fileTypeDesc }}</span>
                  <span>{{ file.readableFileSize }}</span>
                  <span>{{ formatDate(file.uploadTime) }}</span>
                </div>
              </div>
            </div>
            <div class="file-actions">
              <el-button size="small" @click="downloadFile(file)">
                <el-icon><Download /></el-icon>
                下载
              </el-button>
            </div>
          </div>
        </div>
        <div v-else class="no-files">
          <el-empty description="暂无文件" />
        </div>
      </ModernCard>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <el-result
        icon="error"
        title="项目不存在"
        sub-title="请检查项目ID是否正确"
      >
        <template #extra>
          <el-button type="primary" @click="router.push('/user/my-projects')">
            返回我的项目
          </el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  Edit,
  Delete,
  Download,
  User,
  Calendar,
  View
} from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import ModernCard from '@/components/ui/ModernCard.vue'
import { projectApi, projectFileApi } from '@/api/modules/project'
import type { ProjectManagement, ProjectFile } from '@/types/project'

// 路由
const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

// 响应式数据
const loading = ref(true)
const filesLoading = ref(false)
const project = ref<ProjectManagement>()
const projectFiles = ref<ProjectFile[]>([])

// 获取项目ID
const projectId = computed(() => {
  return Number(route.params.id)
})

// 加载项目数据
const loadProject = async () => {
  loading.value = true
  try {
    const response = await projectApi.getProject(projectId.value)
    if (response && response.code === 200 && response.data) {
      // 转换后端数据格式为前端格式
      const backendData = response.data
      project.value = {
        id: backendData.id,
        title: backendData.title,
        description: backendData.description,
        categoryId: backendData.categoryId,
        categoryName: backendData.categoryName || `分类-${backendData.categoryId}`,
        userId: backendData.userId,
        username: backendData.username || `用户-${backendData.userId}`,
        coverImage: backendData.coverImage,
        price: backendData.price ? Number(backendData.price) : 0,
        isFree: !backendData.price || Number(backendData.price) === 0,
        status: backendData.status,
        statusDesc: backendData.statusText || getStatusDescription(backendData.status),
        isFeatured: backendData.isFeatured || false,
        viewCount: backendData.viewCount || 0,
        downloadCount: backendData.downloadCount || 0,
        likeCount: backendData.likeCount || 0,
        rating: backendData.rating ? Number(backendData.rating) : undefined,
        ratingCount: backendData.ratingCount || 0,
        publishedTime: backendData.publishedTime,
        createdTime: backendData.createdTime,
        updatedTime: backendData.updatedTime,
        tags: backendData.tags || [],
        techStack: backendData.techStack || []
      }

      appStore.setPageTitle(`项目详情 - ${project.value.title}`)

      // 加载项目文件
      loadProjectFiles()
    } else {
      throw new Error(response?.message || '获取项目详情失败')
    }
  } catch (error: unknown) {
    console.error('加载项目失败:', error)
    const errorMessage = error instanceof Error ? error.message : '加载项目失败'
    ElMessage.error(errorMessage)
    project.value = undefined
  } finally {
    loading.value = false
  }
}

// 获取状态描述
const getStatusDescription = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待审核',
    1: '已发布',
    2: '已下架',
    3: '审核拒绝'
  }
  return statusMap[status] || '未知状态'
}

// 加载项目文件
const loadProjectFiles = async () => {
  if (!project.value) return

  filesLoading.value = true
  try {
    const response = await projectFileApi.getProjectFiles(project.value.id)
    if (response && response.code === 200 && response.data) {
      projectFiles.value = response.data.content || []
    }
  } catch (error: unknown) {
    console.error('加载项目文件失败:', error)
    projectFiles.value = []
  } finally {
    filesLoading.value = false
  }
}

// 获取状态类型
const getStatusType = (status: number) => {
  const statusMap: Record<number, string> = {
    0: 'info',     // 待审核
    1: 'success',  // 已发布
    2: 'warning',  // 已下架
    3: 'danger'    // 审核拒绝
  }
  return statusMap[status] || 'info'
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 编辑项目
const handleEdit = () => {
  router.push(`/user/project/edit/${projectId.value}`)
}

// 删除项目
const handleDelete = async () => {
  if (!project.value) return

  try {
    await ElMessageBox.confirm(
      `确定要删除项目"${project.value.title}"吗？此操作不可恢复。`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )

    const response = await projectApi.deleteProject(project.value.id)
    if (response && response.code === 200) {
      ElMessage.success('项目删除成功')
      router.push('/user/my-projects')
    } else {
      throw new Error(response?.message || '删除项目失败')
    }
  } catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除项目失败:', error)
      const errorMessage = error instanceof Error ? error.message : '删除项目失败'
      ElMessage.error(errorMessage)
    }
  }
}

// 下载文件
const downloadFile = async (file: ProjectFile) => {
  try {
    // TODO: 实现文件下载
    console.log('下载文件:', file.originalName)
    ElMessage.info('文件下载功能开发中...')
  } catch (error: unknown) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败')
  }
}

// 组件挂载时加载数据
onMounted(() => {
  if (projectId.value) {
    loadProject()
  } else {
    ElMessage.error('项目ID无效')
    router.push('/user/my-projects')
  }
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.my-project-detail-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: $spacing-lg;

  .loading-container {
    padding: $spacing-xl;
    background: white;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-md;
  }

  .project-content {
    .project-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: $spacing-xl;
      padding: $spacing-xl;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: $border-radius-lg;
      color: white;

      .project-info {
        flex: 1;

        .project-title {
          font-size: $font-size-3xl;
          font-weight: $font-weight-bold;
          margin: 0 0 $spacing-md 0;
        }

        .project-meta {
          display: flex;
          align-items: center;
          gap: $spacing-lg;
          flex-wrap: wrap;

          .meta-item {
            display: flex;
            align-items: center;
            gap: $spacing-xs;
            font-size: $font-size-sm;
            opacity: 0.9;
          }
        }
      }

      .project-actions {
        display: flex;
        gap: $spacing-md;
      }
    }

    .description-card {
      margin-bottom: $spacing-xl;

      .project-description {
        font-size: $font-size-base;
        line-height: 1.6;
        color: var(--text-primary);
        margin: 0;
        white-space: pre-wrap;
      }
    }

    .project-details {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: $spacing-lg;
      margin-bottom: $spacing-xl;

      .info-card {
        .info-grid {
          display: grid;
          gap: $spacing-md;

          .info-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: $spacing-sm 0;
            border-bottom: 1px solid var(--border-color);

            &:last-child {
              border-bottom: none;
            }

            label {
              font-weight: $font-weight-medium;
              color: var(--text-secondary);
            }

            span {
              color: var(--text-primary);
              font-weight: $font-weight-medium;
            }
          }
        }
      }

      .tech-card, .tags-card {
        .tech-tags, .project-tags {
          display: flex;
          flex-wrap: wrap;
          gap: $spacing-sm;

          .tech-tag, .project-tag {
            margin: 0;
          }
        }
      }
    }

    .files-card {
      .files-loading {
        padding: $spacing-lg;
      }

      .files-list {
        .file-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: $spacing-md;
          border: 1px solid var(--border-color);
          border-radius: $border-radius-md;
          margin-bottom: $spacing-sm;
          transition: all 0.3s ease;

          &:hover {
            border-color: var(--color-primary);
            box-shadow: $shadow-sm;
          }

          &:last-child {
            margin-bottom: 0;
          }

          .file-info {
            display: flex;
            align-items: center;
            gap: $spacing-md;

            .file-icon {
              font-size: $font-size-xl;
              color: var(--color-primary);
            }

            .file-details {
              .file-name {
                font-weight: $font-weight-medium;
                color: var(--text-primary);
                margin-bottom: $spacing-xs;
              }

              .file-meta {
                display: flex;
                gap: $spacing-md;
                font-size: $font-size-sm;
                color: var(--text-secondary);

                span {
                  &:not(:last-child)::after {
                    content: '•';
                    margin-left: $spacing-md;
                  }
                }
              }
            }
          }

          .file-actions {
            display: flex;
            gap: $spacing-sm;
          }
        }
      }

      .no-files {
        padding: $spacing-xl;
        text-align: center;
      }
    }
  }

  .error-state {
    background: white;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-md;
    padding: $spacing-xl;
  }
}

@media (max-width: 768px) {
  .my-project-detail-view {
    padding: $spacing-md;

    .project-content {
      .project-header {
        flex-direction: column;
        gap: $spacing-lg;

        .project-actions {
          width: 100%;
          justify-content: center;
        }
      }

      .project-details {
        grid-template-columns: 1fr;
      }
    }
  }
}
</style>