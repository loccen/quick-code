<template>
  <div class="project-download-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/projects' }">项目市场</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: `/projects/${projectId}` }">项目详情</el-breadcrumb-item>
          <el-breadcrumb-item>下载项目</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <div class="container">
        <div class="download-container">
          <!-- 左侧：项目信息 -->
          <div class="project-info-section">
            <div class="glass-card">
              <h2 class="section-title">
                <el-icon><Download /></el-icon>
                下载项目
              </h2>
              
              <div v-if="loading" class="loading-state">
                <el-skeleton :rows="5" animated />
              </div>
              
              <div v-else-if="project" class="project-details">
                <div class="project-header">
                  <img 
                    :src="project.coverImage || '/default-project.png'" 
                    :alt="project.title"
                    class="project-cover"
                  />
                  <div class="project-meta">
                    <h3 class="project-title">{{ project.title }}</h3>
                    <p class="project-description">{{ project.description }}</p>
                    <div class="project-tags">
                      <el-tag 
                        v-for="tag in project.tags" 
                        :key="tag" 
                        size="small"
                        class="tag-item"
                      >
                        {{ tag }}
                      </el-tag>
                    </div>
                  </div>
                </div>
                
                <div class="project-stats">
                  <div class="stat-item">
                    <span class="stat-label">作者</span>
                    <span class="stat-value">{{ project.authorName }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">文件大小</span>
                    <span class="stat-value">{{ formatFileSize(project.fileSize) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">下载次数</span>
                    <span class="stat-value">{{ project.downloadCount || 0 }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">更新时间</span>
                    <span class="stat-value">{{ formatDate(project.updatedTime) }}</span>
                  </div>
                </div>
                
                <div class="download-info">
                  <div class="info-item">
                    <el-icon><InfoFilled /></el-icon>
                    <span>下载将包含完整的项目源代码和相关文档</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Lock /></el-icon>
                    <span>下载链接将在1小时后过期，请及时下载</span>
                  </div>
                  <div class="info-item">
                    <el-icon><CircleCheck /></el-icon>
                    <span>项目已通过安全检查，可放心使用</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：下载操作 -->
          <div class="download-section">
            <div class="glass-card">
              <h2 class="section-title">
                <el-icon><Download /></el-icon>
                下载操作
              </h2>
              
              <!-- 权限检查结果 -->
              <div v-if="permissionInfo" class="permission-status">
                <div v-if="permissionInfo.hasPermission" class="permission-granted">
                  <el-icon><CircleCheck /></el-icon>
                  <span>您有权限下载此项目</span>
                </div>
                <div v-else class="permission-denied">
                  <el-icon><CircleClose /></el-icon>
                  <span>{{ permissionInfo.reason }}</span>
                </div>
              </div>
              
              <!-- 下载步骤 -->
              <div class="download-steps">
                <el-steps :active="currentStep" direction="vertical" finish-status="success">
                  <el-step title="权限验证" :description="stepDescriptions.permission" />
                  <el-step title="生成下载令牌" :description="stepDescriptions.token" />
                  <el-step title="开始下载" :description="stepDescriptions.download" />
                </el-steps>
              </div>
              
              <!-- 下载进度 -->
              <div v-if="downloadProgress.show" class="download-progress">
                <div class="progress-header">
                  <span class="progress-title">下载进度</span>
                  <span class="progress-percentage">{{ downloadProgress.percentage }}%</span>
                </div>
                <el-progress 
                  :percentage="downloadProgress.percentage" 
                  :status="downloadProgress.status"
                  :stroke-width="8"
                />
                <div class="progress-details">
                  <span>已下载: {{ formatFileSize(downloadProgress.downloaded) }}</span>
                  <span>总大小: {{ formatFileSize(downloadProgress.total) }}</span>
                  <span>速度: {{ downloadProgress.speed }}</span>
                </div>
              </div>
              
              <!-- 下载按钮 -->
              <div class="download-actions">
                <el-button 
                  size="large" 
                  @click="goBack"
                  class="cancel-btn"
                >
                  返回
                </el-button>
                <el-button 
                  type="primary" 
                  size="large" 
                  :loading="downloading"
                  :disabled="!canDownload"
                  @click="startDownload"
                  class="download-btn"
                >
                  <el-icon v-if="!downloading"><Download /></el-icon>
                  {{ downloading ? '准备下载...' : '开始下载' }}
                </el-button>
              </div>
              
              <!-- 下载历史 -->
              <div v-if="downloadHistory.length > 0" class="download-history">
                <h3 class="history-title">最近下载</h3>
                <div class="history-list">
                  <div 
                    v-for="record in downloadHistory" 
                    :key="record.id"
                    class="history-item"
                  >
                    <div class="history-info">
                      <span class="history-time">{{ formatDateTime(record.downloadTime) }}</span>
                      <span class="history-size">{{ formatFileSize(record.fileSize) }}</span>
                    </div>
                    <div class="history-status">
                      <el-tag 
                        :type="getDownloadStatusType(record.status)"
                        size="small"
                      >
                        {{ getDownloadStatusText(record.status) }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Download,
  InfoFilled,
  Lock,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { projectApi } from '@/api/modules/project'
import { downloadApi } from '@/api/modules/download'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const projectId = ref(route.params.id)
const loading = ref(true)
const downloading = ref(false)
const project = ref(null)
const permissionInfo = ref(null)
const downloadHistory = ref([])
const currentStep = ref(0)

// 下载进度
const downloadProgress = ref({
  show: false,
  percentage: 0,
  downloaded: 0,
  total: 0,
  speed: '0 KB/s',
  status: 'normal'
})

// 步骤描述
const stepDescriptions = ref({
  permission: '检查下载权限...',
  token: '等待生成下载令牌',
  download: '等待开始下载'
})

// 计算属性
const canDownload = computed(() => {
  return permissionInfo.value?.hasPermission && !downloading.value
})

// 方法
const loadProjectInfo = async () => {
  try {
    const response = await projectApi.getProject(projectId.value)
    if (response.success) {
      project.value = response.data
    } else {
      ElMessage.error('加载项目信息失败')
      goBack()
    }
  } catch (error) {
    console.error('加载项目信息失败:', error)
    ElMessage.error('加载项目信息失败')
    goBack()
  }
}

const checkDownloadPermission = async () => {
  try {
    const response = await downloadApi.checkDownloadPermission(projectId.value)
    if (response.success) {
      permissionInfo.value = response.data
      if (permissionInfo.value.hasPermission) {
        currentStep.value = 1
        stepDescriptions.value.permission = '权限验证通过'
        stepDescriptions.value.token = '准备生成下载令牌'
      } else {
        stepDescriptions.value.permission = permissionInfo.value.reason
      }
    }
  } catch (error) {
    console.error('检查下载权限失败:', error)
    permissionInfo.value = {
      hasPermission: false,
      reason: '权限检查失败，请稍后重试'
    }
  }
}

const loadDownloadHistory = async () => {
  try {
    const response = await downloadApi.getDownloadHistory({
      projectId: projectId.value,
      size: 5
    })
    if (response.success) {
      downloadHistory.value = response.data.content || []
    }
  } catch (error) {
    console.error('加载下载历史失败:', error)
  }
}

const startDownload = async () => {
  try {
    downloading.value = true
    currentStep.value = 2
    stepDescriptions.value.token = '正在生成下载令牌...'

    // 生成下载令牌
    const tokenResponse = await downloadApi.generateDownloadToken(projectId.value)
    if (!tokenResponse.success) {
      throw new Error(tokenResponse.message || '生成下载令牌失败')
    }

    stepDescriptions.value.token = '下载令牌生成成功'
    stepDescriptions.value.download = '正在准备下载...'
    currentStep.value = 3

    // 开始下载
    const downloadUrl = await downloadApi.getDownloadUrl(projectId.value, tokenResponse.data.token)
    
    // 模拟下载进度
    downloadProgress.value.show = true
    downloadProgress.value.total = project.value.fileSize || 1024 * 1024
    
    await simulateDownload(downloadUrl)
    
    ElMessage.success('下载完成！')
    
    // 刷新下载历史
    await loadDownloadHistory()

  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error(error.message || '下载失败，请重试')
    downloadProgress.value.status = 'exception'
  } finally {
    downloading.value = false
  }
}

const simulateDownload = async (downloadUrl) => {
  return new Promise((resolve) => {
    let progress = 0
    const interval = setInterval(() => {
      progress += Math.random() * 10
      if (progress >= 100) {
        progress = 100
        clearInterval(interval)
        downloadProgress.value.percentage = 100
        downloadProgress.value.status = 'success'
        stepDescriptions.value.download = '下载完成'
        
        // 触发实际下载
        const link = document.createElement('a')
        link.href = downloadUrl
        link.download = `${project.value.title}.zip`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        
        resolve()
      } else {
        downloadProgress.value.percentage = Math.floor(progress)
        downloadProgress.value.downloaded = Math.floor(downloadProgress.value.total * progress / 100)
        downloadProgress.value.speed = `${Math.floor(Math.random() * 500 + 100)} KB/s`
      }
    }, 200)
  })
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const getDownloadStatusType = (status) => {
  const statusMap = {
    1: 'success',  // 成功
    0: 'warning',  // 进行中
    '-1': 'danger'   // 失败
  }
  return statusMap[status] || 'info'
}

const getDownloadStatusText = (status) => {
  const statusMap = {
    1: '成功',
    0: '进行中',
    '-1': '失败'
  }
  return statusMap[status] || '未知'
}

const goBack = () => {
  router.back()
}

// 生命周期
onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      loadProjectInfo(),
      checkDownloadPermission(),
      loadDownloadHistory()
    ])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.project-download-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.page-header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 1rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.main-content {
  padding: 2rem 0;
}

.download-container {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 2rem;
  align-items: start;
}

.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.5rem;
  font-weight: 600;
  color: white;
  margin-bottom: 1.5rem;
}

.project-header {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.project-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.project-meta {
  flex: 1;
}

.project-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: white;
  margin-bottom: 0.5rem;
}

.project-description {
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-item {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.project-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-label {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
}

.stat-value {
  font-weight: 600;
  color: white;
}

.download-info {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 1.5rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.875rem;
}

.permission-status {
  margin-bottom: 1.5rem;
}

.permission-granted {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #67c23a;
  font-weight: 500;
}

.permission-denied {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #f56c6c;
  font-weight: 500;
}

.download-steps {
  margin-bottom: 1.5rem;
}

.download-progress {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.progress-title {
  font-weight: 600;
  color: white;
}

.progress-percentage {
  font-weight: 600;
  color: #409eff;
}

.progress-details {
  display: flex;
  justify-content: space-between;
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
}

.download-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.cancel-btn {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
}

.download-btn {
  flex: 2;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
}

.download-history {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 1.5rem;
}

.history-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: white;
  margin-bottom: 1rem;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.history-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.history-time {
  font-size: 0.875rem;
  color: white;
}

.history-size {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.7);
}

@media (max-width: 768px) {
  .download-container {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .project-header {
    flex-direction: column;
    text-align: center;
  }
  
  .project-stats {
    grid-template-columns: 1fr;
  }
  
  .progress-details {
    flex-direction: column;
    gap: 0.25rem;
  }
}
</style>
