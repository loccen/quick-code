<template>
  <PageLayout>
    <!-- 页面头部 -->
    <PageHeader
      title="下载记录"
      description="查看您的项目下载历史和管理下载文件"
      :icon="Download"
    >
      <template #actions>
        <el-button @click="$router.push('/user/purchases')">
          <el-icon><ShoppingBag /></el-icon>
          查看购买记录
        </el-button>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <StatsGrid>
      <StatCard
        :icon="Download"
        icon-class="downloads"
        :value="stats.totalDownloads"
        label="总下载次数"
      />
      <StatCard
        :icon="Folder"
        icon-class="purchased"
        :value="stats.downloadedProjects"
        label="已下载项目"
      />
      <StatCard
        :icon="Clock"
        icon-class="transactions"
        :value="stats.recentDownloads"
        label="本月下载"
      />
      <StatCard
        :icon="Folder"
        icon-class="points"
        :value="stats.totalSize"
        label="下载大小"
      />
    </StatsGrid>

    <!-- 下载记录管理 -->
    <ContentContainer>
      <div class="downloads-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部下载" name="all">
            <TabHeader title="全部下载记录">
              <template #actions>
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  @change="handleDateChange"
                />
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索项目名称..."
                  clearable
                  @input="handleSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </template>
            </TabHeader>
          </el-tab-pane>
          
          <el-tab-pane label="最近下载" name="recent">
            <TabHeader title="最近下载">
              <template #actions>
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索项目名称..."
                  clearable
                  @input="handleSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </template>
            </TabHeader>
          </el-tab-pane>
          
          <el-tab-pane label="常用项目" name="frequent">
            <TabHeader title="常用项目">
              <template #actions>
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索项目名称..."
                  clearable
                  @input="handleSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </template>
            </TabHeader>
          </el-tab-pane>
        </el-tabs>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="4" animated />
        </div>

        <!-- 下载记录列表 -->
        <div v-else-if="downloads.length > 0" class="downloads-list">
          <div
            v-for="download in downloads"
            :key="download.downloadId"
            class="download-item"
          >
            <div class="download-header">
              <div class="download-info">
                <span class="download-date">下载时间：{{ formatDate(download.downloadTime) }}</span>
                <span class="download-file" v-if="download.fileName">文件：{{ download.fileName }}</span>
              </div>
              <div class="download-status">
                <el-tag :type="getStatusType(download.downloadStatus)">
                  {{ download.downloadStatusDesc }}
                </el-tag>
              </div>
            </div>

            <div class="download-content">
              <div class="project-info">
                <div class="project-thumbnail">
                  <img
                    :src="'/images/default-project.svg'"
                    :alt="download.projectTitle"
                  />
                </div>
                <div class="project-details">
                  <h3 class="project-title">{{ download.projectTitle }}</h3>
                  <p class="project-description">项目ID：{{ download.projectId }}</p>
                  <div class="project-meta">
                    <span class="project-source">来源：{{ download.downloadSource }}</span>
                    <span class="project-size" v-if="download.readableFileSize">大小：{{ download.readableFileSize }}</span>
                    <span class="download-duration" v-if="download.readableDuration">耗时：{{ download.readableDuration }}</span>
                    <span class="repeat-flag" v-if="download.isRepeat">
                      <el-tag type="warning" size="small">重复下载</el-tag>
                    </span>
                  </div>
                </div>
              </div>

              <div class="download-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleRedownload(download)"
                >
                  <el-icon><Download /></el-icon>
                  重新下载
                </el-button>
                
                <el-button
                  size="small"
                  @click="handleViewProject(download.project)"
                >
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
                
                <el-button
                  size="small"
                  @click="handleViewHistory(download)"
                >
                  <el-icon><Clock /></el-icon>
                  下载历史
                </el-button>
                
                <el-button
                  v-if="download.canDelete"
                  type="danger"
                  size="small"
                  @click="handleDeleteRecord(download)"
                >
                  <el-icon><Delete /></el-icon>
                  删除记录
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="downloads.length > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="totalElements"
            layout="total, prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading" class="empty-state">
          <el-empty description="暂无下载记录" />
        </div>
      </div>
    </ContentContainer>
  </PageLayout>
</template>

<script setup lang="ts">
import { Search, Download, ShoppingBag, Folder, Clock, View, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageLayout from '@/components/common/PageLayout.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatsGrid from '@/components/common/StatsGrid.vue'
import StatCard from '@/components/common/StatCard.vue'
import ContentContainer from '@/components/common/ContentContainer.vue'
import TabHeader from '@/components/common/TabHeader.vue'
import { downloadApi } from '@/api/modules/download'
import { projectDownloadApi } from '@/api/modules/project'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const activeTab = ref('all')
const dateRange = ref<[Date, Date] | null>(null)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)

const downloads = ref<any[]>([])

// 统计数据
const stats = ref({
  totalDownloads: 0,
  downloadedProjects: 0,
  recentDownloads: 0,
  totalSize: '0 MB'
})

/**
 * 格式化日期
 */
const formatDate = (dateString: string) => {
  if (!dateString) return '未知时间'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return dateString
  }
}

/**
 * 获取下载状态类型
 */
const getStatusType = (status: number | string) => {
  // 支持数字状态码和字符串状态
  const statusMap: Record<string | number, string> = {
    // 数字状态码（根据后端ProjectDownload.DownloadStatus枚举）
    1: 'success',    // 下载成功
    2: 'danger',     // 下载失败
    3: 'warning',    // 下载中
    4: 'info',       // 已取消
    // 字符串状态（向后兼容）
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'DOWNLOADING': 'warning',
    'CANCELLED': 'info',
    'EXPIRED': 'info'
  }
  return statusMap[status] || 'info'
}

/**
 * 获取下载状态文本
 */
const getStatusText = (status: number | string) => {
  // 支持数字状态码和字符串状态
  const statusMap: Record<string | number, string> = {
    // 数字状态码
    1: '下载成功',
    2: '下载失败',
    3: '下载中',
    4: '已取消',
    // 字符串状态（向后兼容）
    'SUCCESS': '下载成功',
    'FAILED': '下载失败',
    'DOWNLOADING': '下载中',
    'CANCELLED': '已取消',
    'EXPIRED': '链接过期'
  }
  return statusMap[status] || '未知'
}

/**
 * 查看项目详情
 */
const handleViewProject = (project: any) => {
  router.push(`/market/project/${project.id}`)
}

/**
 * 重新下载
 */
const handleRedownload = async (download: any) => {
  try {
    ElMessage.info('正在准备重新下载...')

    // 生成下载令牌
    const tokenResponse = await projectDownloadApi.generateDownloadToken(download.projectId)
    if (tokenResponse && tokenResponse.code === 200) {
      // 直接下载项目文件
      const blob = await projectDownloadApi.downloadProject(download.projectId)

      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${download.projectTitle}.zip`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      ElMessage.success('重新下载成功')

      // 重新加载下载记录
      loadDownloads()
    } else {
      throw new Error(tokenResponse?.message || '生成下载令牌失败')
    }
  } catch (error: any) {
    console.error('重新下载失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '重新下载失败')
  }
}

/**
 * 查看下载历史
 */
const handleViewHistory = (_download: any) => {
  ElMessage.info('查看下载历史功能开发中...')
}

/**
 * 删除下载记录
 */
const handleDeleteRecord = async (download: any) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条下载记录吗？',
      '删除记录',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await downloadApi.deleteDownloadRecord(download.downloadId)
    if (response && response.code === 200) {
      ElMessage.success('下载记录已删除')
      await loadDownloads()
      await loadDownloadStatistics() // 重新加载统计数据
    } else {
      throw new Error(response?.message || '删除下载记录失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除下载记录失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '删除下载记录失败')
    }
  }
}

/**
 * 处理标签页变化
 */
const handleTabChange = () => {
  currentPage.value = 1
  loadDownloads()
}

/**
 * 处理日期变化
 */
const handleDateChange = () => {
  currentPage.value = 1
  loadDownloads()
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  loadDownloads()
}

/**
 * 处理分页变化
 */
const handlePageChange = () => {
  loadDownloads()
}

/**
 * 加载下载统计数据
 */
const loadDownloadStatistics = async () => {
  try {
    const response = await downloadApi.getUserDownloadStatistics()
    if (response && response.code === 200 && response.data) {
      stats.value = {
        totalDownloads: response.data.totalDownloads || 0,
        downloadedProjects: response.data.uniqueDownloaders || 0,
        recentDownloads: 0, // 需要单独计算本月下载
        totalSize: response.data.readableTotalSize || formatFileSize(response.data.totalSize || 0)
      }
    }
  } catch (error: any) {
    console.error('加载下载统计数据失败:', error)
    // 统计数据加载失败不显示错误消息，保持默认值
  }
}

/**
 * 格式化文件大小
 */
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 加载下载记录数据
 */
const loadDownloads = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value - 1, // 后端页码从0开始
      size: pageSize.value,
      sortBy: 'createdTime',
      sortDirection: 'DESC'
    }

    // 添加搜索关键词
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }

    // 添加日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString().split('T')[0]
      params.endDate = dateRange.value[1].toISOString().split('T')[0]
    }

    // 根据标签页筛选
    if (activeTab.value === 'recent') {
      // 最近下载：只获取最近7天的记录
      const sevenDaysAgo = new Date()
      sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7)
      params.startDate = sevenDaysAgo.toISOString().split('T')[0]
    } else if (activeTab.value === 'frequent') {
      // 常用项目：按下载次数排序
      params.sortBy = 'downloadCount'
      params.sortDirection = 'DESC'
    }

    const response = await downloadApi.getUserDownloadRecords(params)
    if (response && response.code === 200 && response.data) {
      downloads.value = response.data.content || []
      totalElements.value = response.data.total || 0
    } else {
      throw new Error(response?.message || '获取下载记录失败')
    }
  } catch (error: any) {
    console.error('加载下载记录失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '加载下载记录失败')
    downloads.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDownloadStatistics()
  loadDownloads()
})
</script>

<style scoped>
.downloads-tabs {
  padding: 0 24px;
}

.loading-container {
  padding: 24px;
}

.downloads-list {
  padding: 0 24px 24px;
}

.download-item {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.download-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.download-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.download-info {
  display: flex;
  gap: 24px;
}

.download-date {
  font-weight: 600;
  color: #303133;
}

.download-version {
  color: #909399;
  font-size: 14px;
}

.download-content {
  padding: 24px;
  display: flex;
  justify-content: space-between;
  gap: 24px;
}

.project-info {
  display: flex;
  gap: 16px;
  flex: 1;
}

.project-thumbnail {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
}

.project-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.project-details {
  flex: 1;
}

.project-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.project-description {
  color: #606266;
  font-size: 14px;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-meta {
  display: flex;
  gap: 16px;
}

.project-meta span {
  color: #909399;
  font-size: 12px;
}

.download-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 24px;
}

.empty-state {
  padding: 48px 24px;
}
</style>
