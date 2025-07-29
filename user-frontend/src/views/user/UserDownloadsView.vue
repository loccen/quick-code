<template>
  <div class="user-downloads-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">
          <el-icon><Download /></el-icon>
          我的下载
        </h1>
        <p class="page-description">管理您的下载历史和统计信息</p>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <div class="container">
        <!-- 统计卡片 -->
        <div class="stats-section">
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Download /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.totalDownloads || 0 }}</div>
                <div class="stat-label">总下载次数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Folder /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.uniqueProjects || 0 }}</div>
                <div class="stat-label">下载项目数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Files /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatFileSize(statistics.totalSize) }}</div>
                <div class="stat-label">总下载大小</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Key /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ activeTokens.length }}</div>
                <div class="stat-label">活跃令牌</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 下载历史 -->
        <div class="downloads-section">
          <div class="glass-card">
            <!-- 筛选器 -->
            <div class="filters-bar">
              <div class="filter-group">
                <el-select 
                  v-model="filters.status" 
                  placeholder="下载状态" 
                  clearable
                  @change="handleFilterChange"
                  class="filter-select"
                >
                  <el-option label="全部状态" value="" />
                  <el-option label="成功" :value="1" />
                  <el-option label="进行中" :value="0" />
                  <el-option label="失败" :value="-1" />
                </el-select>
                
                <el-date-picker
                  v-model="filters.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  @change="handleFilterChange"
                  class="filter-date"
                />
              </div>
              
              <div class="action-group">
                <el-button 
                  type="primary" 
                  :icon="Refresh" 
                  @click="loadDownloads"
                  :loading="loading"
                >
                  刷新
                </el-button>
              </div>
            </div>

            <!-- 下载列表 -->
            <div v-if="loading" class="loading-state">
              <el-skeleton :rows="5" animated />
            </div>
            
            <div v-else-if="downloads.length === 0" class="empty-state">
              <el-empty description="暂无下载记录">
                <el-button type="primary" @click="$router.push('/projects')">
                  去下载项目
                </el-button>
              </el-empty>
            </div>
            
            <div v-else class="downloads-list">
              <div 
                v-for="download in downloads" 
                :key="download.id" 
                class="download-item"
              >
                <div class="download-header">
                  <div class="download-info">
                    <span class="download-time">{{ formatDateTime(download.downloadTime) }}</span>
                    <span class="download-ip">IP: {{ download.downloadIp }}</span>
                  </div>
                  <div class="download-status">
                    <el-tag 
                      :type="getStatusType(download.downloadStatus)" 
                      size="large"
                    >
                      {{ getStatusText(download.downloadStatus) }}
                    </el-tag>
                  </div>
                </div>
                
                <div class="download-content">
                  <div class="project-info">
                    <img 
                      :src="download.projectCoverImage || '/default-project.png'" 
                      :alt="download.projectName"
                      class="project-cover"
                    />
                    <div class="project-details">
                      <h3 class="project-title">{{ download.projectName }}</h3>
                      <p class="project-description">{{ download.projectDescription }}</p>
                      <div class="project-meta">
                        <span class="file-size">大小：{{ formatFileSize(download.fileSize) }}</span>
                        <span v-if="download.downloadDuration" class="duration">
                          耗时：{{ formatDuration(download.downloadDuration) }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="download-actions">
                  <el-button 
                    size="small" 
                    @click="viewProject(download.projectId)"
                  >
                    查看项目
                  </el-button>
                  
                  <el-button 
                    v-if="download.downloadStatus === 1" 
                    size="small" 
                    type="primary"
                    @click="redownload(download.projectId)"
                  >
                    重新下载
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div v-if="pagination.total > 0" class="pagination-wrapper">
              <el-pagination
                v-model:current-page="pagination.page"
                v-model:page-size="pagination.size"
                :total="pagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
              />
            </div>
          </div>
        </div>

        <!-- 活跃令牌 -->
        <div v-if="activeTokens.length > 0" class="tokens-section">
          <div class="glass-card">
            <h2 class="section-title">
              <el-icon><Key /></el-icon>
              活跃下载令牌
            </h2>
            
            <div class="tokens-list">
              <div 
                v-for="token in activeTokens" 
                :key="token.projectId"
                class="token-item"
              >
                <div class="token-info">
                  <div class="token-project">项目ID: {{ token.projectId }}</div>
                  <div class="token-expiry">
                    过期时间: {{ formatDateTime(token.expirationTime) }}
                  </div>
                  <div class="token-remaining">
                    剩余时间: {{ formatRemainingTime(token.remainingTime) }}
                  </div>
                </div>
                <div class="token-actions">
                  <el-button 
                    size="small" 
                    type="warning"
                    @click="revokeToken(token)"
                  >
                    撤销令牌
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Download, 
  Folder, 
  Files, 
  Key, 
  Refresh 
} from '@element-plus/icons-vue'
import { downloadApi } from '@/api/modules/download'

const router = useRouter()

// 响应式数据
const loading = ref(true)
const downloads = ref<any[]>([])
const statistics = ref<any>({})
const activeTokens = ref<any[]>([])

// 筛选器
const filters = ref<any>({
  status: '',
  dateRange: null
})

// 分页
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 方法
const loadDownloads = async () => {
  try {
    loading.value = true
    
    const params = {
      page: pagination.value.page - 1,
      size: pagination.value.size
    }
    
    // 添加筛选条件
    if (filters.value.status !== '') {
      params.status = filters.value.status
    }
    
    if (filters.value.dateRange && filters.value.dateRange.length === 2) {
      params.startDate = filters.value.dateRange[0].toISOString()
      params.endDate = filters.value.dateRange[1].toISOString()
    }
    
    const response = await downloadApi.getDownloadHistory(params)
    
    if (response.success) {
      downloads.value = response.data.content
      pagination.value.total = response.data.totalElements
    } else {
      ElMessage.error('加载下载历史失败')
    }
  } catch (error) {
    console.error('加载下载历史失败:', error)
    ElMessage.error('加载下载历史失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await downloadApi.getDownloadStatistics()
    if (response.success) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const loadActiveTokens = async () => {
  try {
    const response = await downloadApi.getUserActiveTokens()
    if (response.success) {
      activeTokens.value = response.data
    }
  } catch (error) {
    console.error('加载活跃令牌失败:', error)
  }
}

const handleFilterChange = () => {
  pagination.value.page = 1
  loadDownloads()
}

const handlePageChange = (page: any) => {
  pagination.value.page = page
  loadDownloads()
}

const handleSizeChange = (size: any) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadDownloads()
}

const getStatusType = (status: any) => {
  const statusMap = {
    1: 'success',   // 成功
    0: 'warning',   // 进行中
    '-1': 'danger'  // 失败
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    1: '成功',
    0: '进行中',
    '-1': '失败'
  }
  return statusMap[status] || '未知'
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

const formatDuration = (seconds) => {
  if (!seconds) return '-'
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}分${remainingSeconds}秒`
}

const formatRemainingTime = (seconds) => {
  if (!seconds || seconds <= 0) return '已过期'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

const viewProject = (projectId) => {
  router.push(`/projects/${projectId}`)
}

const redownload = (projectId) => {
  router.push(`/projects/${projectId}/download`)
}

const revokeToken = async (token) => {
  try {
    await ElMessageBox.confirm(
      '确认撤销此下载令牌？撤销后将无法使用此令牌下载项目。',
      '撤销令牌',
      {
        confirmButtonText: '确认撤销',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await downloadApi.revokeDownloadToken(token.token)
    
    if (response.success) {
      ElMessage.success('令牌撤销成功')
      loadActiveTokens()
    } else {
      ElMessage.error(response.message || '撤销令牌失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤销令牌失败:', error)
      ElMessage.error('撤销令牌失败')
    }
  }
}

// 生命周期
onMounted(async () => {
  await Promise.all([
    loadDownloads(),
    loadStatistics(),
    loadActiveTokens()
  ])
})
</script>

<style scoped>
.user-downloads-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.page-header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 2rem;
  font-weight: 600;
  color: white;
  margin-bottom: 0.5rem;
}

.page-description {
  color: rgba(255, 255, 255, 0.8);
  font-size: 1.125rem;
}

.main-content {
  padding: 2rem 0;
}

.stats-section {
  margin-bottom: 2rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: white;
  line-height: 1;
}

.stat-label {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 0.25rem;
}

.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
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

.filters-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.filter-group {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.filter-select {
  min-width: 120px;
}

.filter-date {
  min-width: 240px;
}

.downloads-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.download-item {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.download-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.download-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.download-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.download-time {
  font-weight: 600;
  color: white;
}

.download-ip {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
}

.download-content {
  margin-bottom: 1rem;
}

.project-info {
  display: flex;
  gap: 1rem;
}

.project-cover {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.project-details {
  flex: 1;
}

.project-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: white;
  margin-bottom: 0.25rem;
}

.project-description {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.project-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
}

.download-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.tokens-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.token-item {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.token-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.token-project {
  font-weight: 600;
  color: white;
}

.token-expiry,
.token-remaining {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
}

.pagination-wrapper {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
}

.loading-state,
.empty-state {
  padding: 2rem;
  text-align: center;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .filters-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .download-content {
    flex-direction: column;
    gap: 1rem;
  }
  
  .project-meta {
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .token-item {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
}
</style>
