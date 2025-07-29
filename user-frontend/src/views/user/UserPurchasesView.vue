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
                    :src="purchase.project.thumbnail || '/images/default-project.jpg'"
                    :alt="purchase.project.title"
                  />
                </div>
                <div class="project-details">
                  <h3 class="project-title">{{ purchase.project.title }}</h3>
                  <p class="project-description">{{ purchase.project.description }}</p>
                  <div class="project-meta">
                    <span class="project-author">作者：{{ purchase.project.author }}</span>
                    <span class="project-category">分类：{{ purchase.project.category }}</span>
                    <span class="download-count">下载：{{ purchase.downloadCount }}次</span>
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
const handleDownload = (_project: any) => {
  ElMessage.success('开始下载项目...')
}

/**
 * 切换收藏状态
 */
const handleToggleFavorite = (purchase: any) => {
  purchase.isFavorite = !purchase.isFavorite
  ElMessage.success(purchase.isFavorite ? '已添加到收藏' : '已取消收藏')
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
 * 加载购买记录数据
 */
const loadPurchases = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    purchases.value = []
    
    totalElements.value = purchases.value.length
    
    // 更新统计数据
    stats.value = {
      totalPurchases: 15,
      totalSpent: 4580,
      totalDownloads: 42,
      favoriteProjects: 8
    }
  } catch (error) {
    ElMessage.error('加载购买记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
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