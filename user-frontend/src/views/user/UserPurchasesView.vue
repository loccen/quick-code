<template>
  <PageLayout>
    <!-- 页面头部 -->
    <PageHeader
      title="购买记录"
      description="查看您购买的所有项目和下载记录"
      :icon="ShoppingBag"
    >
      <template #actions>
        <el-button @click="$router.push('/market')">
          <el-icon><ShoppingBag /></el-icon>
          浏览项目市场
        </el-button>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <StatsGrid>
      <StatCard
        :icon="ShoppingBag"
        icon-class="purchased"
        :value="stats.totalPurchases"
        label="购买项目数"
      />
      <StatCard
        :icon="CreditCard"
        icon-class="earnings"
        :value="stats.totalSpent"
        label="总消费（积分）"
      />
      <StatCard
        :icon="Download"
        icon-class="downloads"
        :value="stats.totalDownloads"
        label="下载次数"
      />
      <StatCard
        :icon="Star"
        icon-class="favorites"
        :value="stats.favoriteProjects"
        label="收藏项目"
      />
    </StatsGrid>

    <!-- 购买记录管理 -->
    <ContentContainer>
      <div class="purchases-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部购买" name="all">
            <TabHeader title="全部购买记录">
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
                  placeholder="搜索项目名称或作者..."
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
          
          <el-tab-pane label="最近购买" name="recent">
            <TabHeader title="最近购买">
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
          
          <el-tab-pane label="收藏项目" name="favorites">
            <TabHeader title="收藏的购买项目">
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

        <!-- 购买记录列表 -->
        <div v-else-if="purchases.length > 0" class="purchases-list">
          <div
            v-for="purchase in purchases"
            :key="purchase.id"
            class="purchase-item"
          >
            <div class="purchase-header">
              <div class="purchase-info">
                <span class="purchase-date">购买时间：{{ purchase.purchaseDate }}</span>
                <span class="order-no">订单号：{{ purchase.orderNo }}</span>
              </div>
              <div class="purchase-amount">
                <span class="amount">{{ purchase.amount }}</span>
                <span class="unit">积分</span>
              </div>
            </div>

            <div class="purchase-content">
              <div class="project-info">
                <div class="project-thumbnail">
                  <img
                    :src="purchase.project?.thumbnail || purchase.project?.coverImage || '/images/default-project.jpg'"
                    :alt="purchase.project?.title || '项目'"
                  />
                </div>
                <div class="project-details">
                  <h3 class="project-title">{{ purchase.project?.title || '未知项目' }}</h3>
                  <p class="project-description">{{ purchase.project?.description || '暂无描述' }}</p>
                  <div class="project-meta">
                    <span class="project-author">作者：{{ purchase.project?.author || purchase.project?.username || '未知' }}</span>
                    <span class="project-category">分类：{{ purchase.project?.category || purchase.project?.categoryName || '未分类' }}</span>
                    <span class="download-count">下载：{{ purchase.downloadCount || 0 }}次</span>
                  </div>
                </div>
              </div>

              <div class="purchase-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleDownload(purchase.project)"
                >
                  <el-icon><Download /></el-icon>
                  下载项目
                </el-button>
                
                <el-button
                  size="small"
                  @click="handleViewProject(purchase.project)"
                >
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
                
                <el-button
                  :type="purchase.isFavorite ? 'warning' : 'default'"
                  size="small"
                  @click="handleToggleFavorite(purchase)"
                >
                  <el-icon><Star /></el-icon>
                  {{ purchase.isFavorite ? '取消收藏' : '收藏' }}
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="purchases.length > 0" class="pagination-container">
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
          <el-empty description="暂无购买记录" />
        </div>
      </div>
    </ContentContainer>
  </PageLayout>
</template>

<script setup lang="ts">
import { Search, ShoppingBag, CreditCard, Download, Star, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageLayout from '@/components/common/PageLayout.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatsGrid from '@/components/common/StatsGrid.vue'
import StatCard from '@/components/common/StatCard.vue'
import ContentContainer from '@/components/common/ContentContainer.vue'
import TabHeader from '@/components/common/TabHeader.vue'
import { orderApi } from '@/api/modules/order'
import { projectApi, projectDownloadApi } from '@/api/modules/project'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const activeTab = ref('all')
const dateRange = ref<[Date, Date] | null>(null)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)

const purchases = ref<any[]>([])

// 统计数据
const stats = ref({
  totalPurchases: 0,
  totalSpent: 0,
  totalDownloads: 0,
  favoriteProjects: 0
})

/**
 * 查看项目详情
 */
const handleViewProject = (project: any) => {
  router.push(`/market/project/${project.id}`)
}

/**
 * 下载项目
 */
const handleDownload = async (project: any) => {
  try {
    ElMessage.info('正在准备下载...')

    // 生成下载令牌
    const tokenResponse = await projectDownloadApi.generateDownloadToken(project.id)
    if (tokenResponse && tokenResponse.code === 200) {
      // 直接下载项目文件
      const blob = await projectDownloadApi.downloadProject(project.id)

      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${project.title}.zip`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      ElMessage.success('下载成功')

      // 重新加载购买记录以更新下载次数
      loadPurchases()
    } else {
      throw new Error(tokenResponse?.message || '生成下载令牌失败')
    }
  } catch (error: any) {
    console.error('下载项目失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '下载失败')
  }
}

/**
 * 切换收藏状态
 */
const handleToggleFavorite = async (purchase: any) => {
  try {
    const project = purchase.project
    if (purchase.isFavorite) {
      // 取消收藏
      const response = await projectApi.unfavoriteProject(project.id)
      if (response && response.code === 200) {
        purchase.isFavorite = false
        ElMessage.success('已取消收藏')
      } else {
        throw new Error(response?.message || '取消收藏失败')
      }
    } else {
      // 添加收藏
      const response = await projectApi.favoriteProject(project.id)
      if (response && response.code === 200) {
        purchase.isFavorite = true
        ElMessage.success('已添加到收藏')
      } else {
        throw new Error(response?.message || '添加收藏失败')
      }
    }

    // 重新加载统计数据
    loadPurchaseStatistics()
  } catch (error: any) {
    console.error('切换收藏状态失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '操作失败')
  }
}

/**
 * 处理标签页变化
 */
const handleTabChange = () => {
  currentPage.value = 1
  loadPurchases()
}

/**
 * 处理日期变化
 */
const handleDateChange = () => {
  currentPage.value = 1
  loadPurchases()
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  loadPurchases()
}

/**
 * 处理分页变化
 */
const handlePageChange = () => {
  loadPurchases()
}

/**
 * 加载购买统计数据
 */
const loadPurchaseStatistics = async () => {
  try {
    const response = await orderApi.getUserPurchaseStatistics()
    if (response && response.code === 200 && response.data) {
      // 使用OrderStatistics类型的字段
      stats.value = {
        totalPurchases: response.data.totalOrders || 0,
        totalSpent: response.data.totalAmount || 0,
        totalDownloads: 0, // 这个字段需要从其他API获取
        favoriteProjects: 0 // 这个字段需要从其他API获取
      }
    }
  } catch (error: any) {
    console.error('加载购买统计数据失败:', error)
    // 统计数据加载失败不显示错误消息，保持默认值0
  }
}

/**
 * 加载购买记录数据
 */
const loadPurchases = async () => {
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
      // 最近购买：只获取最近30天的记录
      const thirtyDaysAgo = new Date()
      thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
      params.startDate = thirtyDaysAgo.toISOString().split('T')[0]
    } else if (activeTab.value === 'favorites') {
      // 收藏项目：通过收藏API获取
      const response = await projectApi.getFavoriteProjects(params)
      if (response && response.code === 200 && response.data) {
        // 转换收藏项目为购买记录格式
        purchases.value = response.data.content?.map((project: any) => ({
          id: `fav-${project.id}`,
          project: project,
          purchaseDate: project.createdTime,
          orderNo: `FAV-${project.id}`,
          amount: project.price || 0,
          downloadCount: 0,
          isFavorite: true
        })) || []
        totalElements.value = response.data.total || 0
      }
      return
    }

    const response = await orderApi.getUserPurchaseOrders(params)
    if (response && response.code === 200 && response.data) {
      purchases.value = response.data.content || []
      totalElements.value = response.data.total || 0
    } else {
      throw new Error(response?.message || '获取购买记录失败')
    }
  } catch (error: any) {
    console.error('加载购买记录失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '加载购买记录失败')
    purchases.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPurchaseStatistics()
  loadPurchases()
})
</script>

<style scoped>
.purchases-tabs {
  padding: 0 24px;
}

.loading-container {
  padding: 24px;
}

.purchases-list {
  padding: 0 24px 24px;
}

.purchase-item {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.purchase-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.purchase-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.purchase-info {
  display: flex;
  gap: 24px;
}

.purchase-date {
  font-weight: 600;
  color: #303133;
}

.order-no {
  color: #909399;
  font-size: 14px;
}

.purchase-amount {
  text-align: right;
}

.amount {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}

.unit {
  color: #909399;
  font-size: 14px;
  margin-left: 4px;
}

.purchase-content {
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

.purchase-actions {
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