<template>
  <div class="user-purchases-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <h1 class="page-title">
          <el-icon><ShoppingBag /></el-icon>
          我的购买
        </h1>
        <p class="page-description">管理您购买的所有项目</p>
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
                <el-icon><ShoppingCart /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.purchaseCount || 0 }}</div>
                <div class="stat-label">购买次数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ statistics.purchaseAmount || 0 }}</div>
                <div class="stat-label">消费积分</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Download /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ completedOrders }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ pendingOrders }}</div>
                <div class="stat-label">处理中</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 订单列表 -->
        <div class="orders-section">
          <div class="glass-card">
            <!-- 筛选器 -->
            <div class="filters-bar">
              <div class="filter-group">
                <el-select 
                  v-model="filters.status" 
                  placeholder="订单状态" 
                  clearable
                  @change="handleFilterChange"
                  class="filter-select"
                >
                  <el-option label="全部状态" value="" />
                  <el-option label="待支付" :value="0" />
                  <el-option label="已支付" :value="1" />
                  <el-option label="已完成" :value="2" />
                  <el-option label="已取消" :value="3" />
                  <el-option label="已退款" :value="4" />
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
                  @click="loadOrders"
                  :loading="loading"
                >
                  刷新
                </el-button>
              </div>
            </div>

            <!-- 订单列表 -->
            <div v-if="loading" class="loading-state">
              <el-skeleton :rows="5" animated />
            </div>
            
            <div v-else-if="orders.length === 0" class="empty-state">
              <el-empty description="暂无购买记录">
                <el-button type="primary" @click="$router.push('/projects')">
                  去逛逛项目市场
                </el-button>
              </el-empty>
            </div>
            
            <div v-else class="orders-list">
              <div 
                v-for="order in orders" 
                :key="order.id" 
                class="order-item"
              >
                <div class="order-header">
                  <div class="order-info">
                    <span class="order-no">订单号：{{ order.orderNo }}</span>
                    <span class="order-time">{{ formatDate(order.createdTime) }}</span>
                  </div>
                  <div class="order-status">
                    <el-tag 
                      :type="getStatusType(order.status)" 
                      size="large"
                    >
                      {{ order.statusDescription }}
                    </el-tag>
                  </div>
                </div>
                
                <div class="order-content">
                  <div class="project-info">
                    <img 
                      :src="order.projectCoverImage || '/default-project.png'" 
                      :alt="order.projectName"
                      class="project-cover"
                    />
                    <div class="project-details">
                      <h3 class="project-title">{{ order.projectName }}</h3>
                      <p class="project-description">{{ order.projectDescription }}</p>
                      <div class="project-meta">
                        <span class="seller-name">作者：{{ order.sellerNickname || order.sellerUsername }}</span>
                      </div>
                    </div>
                  </div>
                  
                  <div class="order-amount">
                    <div class="amount-value">{{ order.amount }} 积分</div>
                    <div class="payment-method">{{ order.paymentMethodDescription }}</div>
                  </div>
                </div>
                
                <div class="order-actions">
                  <el-button 
                    size="small" 
                    @click="viewOrderDetail(order)"
                  >
                    查看详情
                  </el-button>
                  
                  <el-button 
                    v-if="order.canCancel" 
                    size="small" 
                    type="warning"
                    @click="cancelOrder(order)"
                  >
                    取消订单
                  </el-button>
                  
                  <el-button 
                    v-if="order.canRefund" 
                    size="small" 
                    type="danger"
                    @click="requestRefund(order)"
                  >
                    申请退款
                  </el-button>
                  
                  <el-button 
                    v-if="order.isPaid && !order.isCompleted" 
                    size="small" 
                    type="success"
                    @click="downloadProject(order)"
                  >
                    下载项目
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
      </div>
    </div>

    <!-- 订单详情对话框 -->
    <OrderDetailDialog 
      v-model="orderDetailVisible"
      :order="selectedOrder"
      @refresh="loadOrders"
    />
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ShoppingBag, 
  ShoppingCart, 
  Star, 
  Download, 
  Clock, 
  Refresh 
} from '@element-plus/icons-vue'
import { orderApi } from '@/api/modules/order'
import OrderDetailDialog from '@/components/order/OrderDetailDialog.vue'

const router = useRouter()

// 响应式数据
const loading = ref(true)
const orders = ref([])
const statistics = ref({})
const selectedOrder = ref(null)
const orderDetailVisible = ref(false)

// 筛选器
const filters = ref({
  status: '',
  dateRange: null
})

// 分页
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 计算属性
const completedOrders = computed(() => {
  return orders.value.filter(order => order.isCompleted).length
})

const pendingOrders = computed(() => {
  return orders.value.filter(order => order.isPaid && !order.isCompleted).length
})

// 方法
const loadOrders = async () => {
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
      // @ts-ignore
      params.startDate = filters.value.dateRange[0].toISOString()
      // @ts-ignore
      params.endDate = filters.value.dateRange[1].toISOString()
    }
    
    const response = await orderApi.getUserPurchaseOrders(params)
    
    if (response.success) {
      orders.value = response.data.content
      pagination.value.total = response.data.totalElements
    } else {
      ElMessage.error('加载订单列表失败')
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await orderApi.getUserPurchaseStatistics()
    if (response.success) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const handleFilterChange = () => {
  pagination.value.page = 1
  loadOrders()
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadOrders()
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadOrders()
}

const getStatusType = (status) => {
  const statusMap = {
    0: 'warning',  // 待支付
    1: 'primary',  // 已支付
    2: 'success',  // 已完成
    3: 'info',     // 已取消
    4: 'danger'    // 已退款
  }
  return statusMap[status] || 'info'
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const viewOrderDetail = (order) => {
  selectedOrder.value = order
  orderDetailVisible.value = true
}

const cancelOrder = async (order: any) => {
  try {
    await ElMessageBox.confirm(
      `确认取消订单"${order.projectName}"？`,
      '取消订单',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await orderApi.cancelOrder(order.orderNo, '用户主动取消')
    
    if (response.success) {
      ElMessage.success('订单取消成功')
      loadOrders()
    } else {
      ElMessage.error(response.message || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

const requestRefund = async (order: any) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入退款原因',
      '申请退款',
      {
        confirmButtonText: '提交申请',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '请输入退款原因'
      }
    )

    const response = await orderApi.requestRefund(order.orderNo, reason)
    
    if (response.success) {
      ElMessage.success('退款申请提交成功')
      loadOrders()
    } else {
      ElMessage.error(response.message || '退款申请失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('申请退款失败:', error)
      ElMessage.error('申请退款失败')
    }
  }
}

const downloadProject = async (order: any) => {
  try {
    // 跳转到项目详情页进行下载
    router.push(`/projects/${order.projectId}`)
  } catch (error) {
    console.error('下载项目失败:', error)
    ElMessage.error('下载项目失败')
  }
}

// 生命周期
onMounted(async () => {
  await Promise.all([
    loadOrders(),
    loadStatistics()
  ])
})
</script>

<style scoped>
.user-purchases-view {
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

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.order-item {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.order-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.order-no {
  font-weight: 600;
  color: white;
}

.order-time {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.project-info {
  display: flex;
  gap: 1rem;
  flex: 1;
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
  margin-bottom: 0.25rem;
  line-height: 1.4;
}

.project-meta {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
}

.order-amount {
  text-align: right;
}

.amount-value {
  font-size: 1.25rem;
  font-weight: 600;
  color: #ffd700;
  margin-bottom: 0.25rem;
}

.payment-method {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
}

.order-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
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
  
  .order-content {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .order-amount {
    text-align: left;
  }
  
  .order-actions {
    justify-content: center;
  }
}
</style>
