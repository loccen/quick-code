<template>
  <PageLayout>
    <!-- 页面头部 -->
    <PageHeader
      title="我的订单"
      description="查看您的购买记录和订单状态"
      :icon="ShoppingCart"
    >
      <template #actions>
        <el-button @click="$router.push('/market')">
          <el-icon><ShoppingCart /></el-icon>
          浏览项目市场
        </el-button>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <StatsGrid>
      <StatCard
        :icon="ShoppingCart"
        icon-class="orders"
        :value="stats.totalOrders"
        label="总订单数"
      />
      <StatCard
        :icon="CreditCard"
        icon-class="earnings"
        :value="stats.totalAmount"
        label="总消费（积分）"
      />
      <StatCard
        :icon="Clock"
        icon-class="purchased"
        :value="stats.pendingOrders"
        label="待支付订单"
      />
      <StatCard
        :icon="Check"
        icon-class="favorites"
        :value="stats.completedOrders"
        label="已完成订单"
      />
    </StatsGrid>

    <!-- 订单管理 -->
    <ContentContainer>
      <div class="orders-tabs">
        <el-tabs v-model="activeStatus" @tab-change="handleStatusChange">
          <el-tab-pane label="全部" name="all">
            <TabHeader title="全部订单">
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
                  placeholder="搜索订单号或项目名称..."
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

          <el-tab-pane label="待支付" name="pending">
            <TabHeader title="待支付订单">
              <template #actions>
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索订单号或项目名称..."
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

          <el-tab-pane label="已支付" name="paid">
            <TabHeader title="已支付订单">
              <template #actions>
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索订单号或项目名称..."
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

          <el-tab-pane label="已取消" name="cancelled">
            <TabHeader title="已取消订单" />
          </el-tab-pane>

          <el-tab-pane label="已退款" name="refunded">
            <TabHeader title="已退款订单" />
          </el-tab-pane>
        </el-tabs>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="4" animated />
        </div>

        <!-- 订单列表 -->
        <div v-else-if="orders.length > 0" class="orders-list">
          <div
            v-for="order in orders"
            :key="order.id"
            class="order-item"
          >
            <div class="order-header">
              <div class="order-info">
                <span class="order-no">订单号：{{ order.orderNo }}</span>
                <span class="order-date">{{ order.createdAt }}</span>
              </div>
              <div class="order-status">
                <el-tag :type="getStatusType(order.status)">
                  {{ getStatusText(order.status) }}
                </el-tag>
              </div>
            </div>

            <div class="order-content">
              <div class="project-info">
                <div class="project-thumbnail">
                  <img
                    :src="order.project.thumbnail || '/images/default-project.jpg'"
                    :alt="order.project.title"
                  />
                </div>
                <div class="project-details">
                  <h3 class="project-title">{{ order.project.title }}</h3>
                  <p class="project-description">{{ order.project.description }}</p>
                  <div class="project-meta">
                    <span class="project-author">作者：{{ order.project.author }}</span>
                    <span class="project-category">分类：{{ order.project.category }}</span>
                  </div>
                </div>
              </div>

              <div class="order-amount">
                <div class="amount-info">
                  <span class="amount">{{ order.amount }}</span>
                  <span class="unit">积分</span>
                </div>
              </div>

              <div class="order-actions">
                <el-button
                  size="small"
                  @click="handleViewProject(order.project)"
                >
                  查看项目
                </el-button>

                <el-button
                  v-if="order.status === 'PENDING'"
                  type="primary"
                  size="small"
                  @click="handlePayOrder(order)"
                >
                  立即支付
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="orders.length > 0" class="pagination-container">
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
          <el-empty description="暂无订单数据" />
        </div>
      </div>
    </ContentContainer>
  </PageLayout>
</template>

<script setup lang="ts">
import { Search, ShoppingCart, CreditCard, Clock, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
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
const activeStatus = ref('all')
const dateRange = ref<[Date, Date] | null>(null)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)

const orders = ref<any[]>([])

// 统计数据
const stats = ref({
  totalOrders: 0,
  totalAmount: 0,
  pendingOrders: 0,
  completedOrders: 0
})

const totalPages = computed(() => Math.ceil(totalElements.value / pageSize.value))

/**
 * 获取订单状态类型
 */
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    PENDING: 'warning',
    PAID: 'success',
    CANCELLED: 'info',
    REFUNDED: 'danger'
  }
  return statusMap[status] || 'info'
}

/**
 * 获取订单状态文本
 */
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    PENDING: '待支付',
    PAID: '已支付',
    CANCELLED: '已取消',
    REFUNDED: '已退款'
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
 * 支付订单
 */
const handlePayOrder = (_order: any) => {
  ElMessage.info('支付功能开发中...')
}

/**
 * 处理状态变化
 */
const handleStatusChange = () => {
  currentPage.value = 1
  loadOrders()
}

/**
 * 处理日期变化
 */
const handleDateChange = () => {
  currentPage.value = 1
  loadOrders()
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  loadOrders()
}

/**
 * 处理分页变化
 */
const handlePageChange = () => {
  loadOrders()
}

/**
 * 加载订单数据
 */
const loadOrders = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 模拟数据
    orders.value = []

    totalElements.value = orders.value.length

    // 更新统计数据
    stats.value = {
      totalOrders: 12,
      totalAmount: 2580,
      pendingOrders: 2,
      completedOrders: 8
    }
  } catch (error) {
    ElMessage.error('加载订单数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-tabs {
  padding: 0 24px;
}

.loading-container {
  padding: 24px;
}

.orders-list {
  padding: 0 24px 24px;
}

.order-item {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.order-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.order-info {
  display: flex;
  gap: 24px;
}

.order-no {
  font-weight: 600;
  color: #303133;
}

.order-date {
  color: #909399;
  font-size: 14px;
}

.order-content {
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

.order-amount {
  text-align: center;
}

.amount {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.unit {
  color: #909399;
  font-size: 14px;
  margin-left: 4px;
}

.order-actions {
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