<template>
  <PageLayout>
    <!-- 页面头部 -->
    <PageHeader
      title="积分管理"
      description="管理您的积分余额和查看交易记录"
      :icon="CreditCard"
    >
      <template #actions>
        <el-button type="primary" @click="handleRecharge">
          <el-icon><Plus /></el-icon>
          充值积分
        </el-button>
      </template>
    </PageHeader>

    <!-- 统计卡片 -->
    <StatsGrid>
      <StatCard
        :icon="CreditCard"
        icon-class="points"
        :value="stats.currentBalance"
        label="当前余额"
      />
      <StatCard
        :icon="TrendCharts"
        icon-class="earnings"
        :value="stats.totalEarned"
        label="累计获得"
      />
      <StatCard
        :icon="ShoppingCart"
        icon-class="purchased"
        :value="stats.totalSpent"
        label="累计消费"
      />
      <StatCard
        :icon="Clock"
        icon-class="transactions"
        :value="stats.recentTransactions"
        label="本月交易"
      />
    </StatsGrid>

    <!-- 积分管理 -->
    <ContentContainer>
      <div class="points-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部记录" name="all">
            <TabHeader title="全部交易记录">
              <template #actions>
                <el-select
                  v-model="transactionType"
                  placeholder="交易类型"
                  clearable
                  @change="handleFilterChange"
                >
                  <el-option label="全部类型" value="" />
                  <el-option label="充值" value="RECHARGE" />
                  <el-option label="消费" value="PURCHASE" />
                  <el-option label="退款" value="REFUND" />
                  <el-option label="奖励" value="REWARD" />
                </el-select>
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  @change="handleDateChange"
                />
              </template>
            </TabHeader>
          </el-tab-pane>
          
          <el-tab-pane label="收入记录" name="income">
            <TabHeader title="收入记录">
              <template #actions>
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  @change="handleDateChange"
                />
              </template>
            </TabHeader>
          </el-tab-pane>
          
          <el-tab-pane label="支出记录" name="expense">
            <TabHeader title="支出记录">
              <template #actions>
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  @change="handleDateChange"
                />
              </template>
            </TabHeader>
          </el-tab-pane>
        </el-tabs>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="4" animated />
        </div>

        <!-- 交易记录列表 -->
        <div v-else-if="transactions.length > 0" class="transactions-list">
          <div
            v-for="transaction in transactions"
            :key="transaction.id"
            class="transaction-item"
          >
            <div class="transaction-header">
              <div class="transaction-info">
                <span class="transaction-date">{{ transaction.createdAt }}</span>
                <span class="transaction-id">交易号：{{ transaction.transactionId }}</span>
              </div>
              <div class="transaction-amount" :class="getAmountClass(transaction.type)">
                <span class="amount">{{ getAmountText(transaction) }}</span>
                <span class="unit">积分</span>
              </div>
            </div>

            <div class="transaction-content">
              <div class="transaction-details">
                <div class="transaction-type">
                  <el-tag :type="getTypeColor(transaction.type)">
                    {{ getTypeText(transaction.type) }}
                  </el-tag>
                </div>
                <div class="transaction-description">
                  <h4 class="description-title">{{ transaction.description }}</h4>
                  <p v-if="transaction.remark" class="description-remark">{{ transaction.remark }}</p>
                  <div class="transaction-meta">
                    <span v-if="transaction.relatedProject" class="related-project">
                      相关项目：{{ transaction.relatedProject }}
                    </span>
                    <span class="transaction-status">
                      状态：{{ getStatusText(transaction.status) }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="transaction-actions">
                <el-button
                  size="small"
                  @click="handleViewDetail(transaction)"
                >
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
                
                <el-button
                  v-if="transaction.canRefund"
                  type="warning"
                  size="small"
                  @click="handleRequestRefund(transaction)"
                >
                  <el-icon><RefreshLeft /></el-icon>
                  申请退款
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="transactions.length > 0" class="pagination-container">
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
          <el-empty description="暂无交易记录" />
        </div>
      </div>
    </ContentContainer>
  </PageLayout>
</template>

<script setup lang="ts">
import { CreditCard, TrendCharts, ShoppingCart, Clock, Plus, View, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
const transactionType = ref('')
const dateRange = ref<[Date, Date] | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)

const transactions = ref<any[]>([])

// 统计数据
const stats = ref({
  currentBalance: 0,
  totalEarned: 0,
  totalSpent: 0,
  recentTransactions: 0
})

/**
 * 获取交易类型颜色
 */
const getTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    RECHARGE: 'success',
    PURCHASE: 'warning',
    REFUND: 'info',
    REWARD: 'primary'
  }
  return colorMap[type] || 'default'
}

/**
 * 获取交易类型文本
 */
const getTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    RECHARGE: '充值',
    PURCHASE: '消费',
    REFUND: '退款',
    REWARD: '奖励'
  }
  return textMap[type] || '未知'
}

/**
 * 获取金额样式类
 */
const getAmountClass = (type: string) => {
  return type === 'RECHARGE' || type === 'REFUND' || type === 'REWARD' ? 'income' : 'expense'
}

/**
 * 获取金额文本
 */
const getAmountText = (transaction: any) => {
  const prefix = getAmountClass(transaction.type) === 'income' ? '+' : '-'
  return `${prefix}${transaction.amount}`
}

/**
 * 获取状态文本
 */
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    SUCCESS: '成功',
    PENDING: '处理中',
    FAILED: '失败',
    CANCELLED: '已取消'
  }
  return statusMap[status] || '未知'
}

/**
 * 充值积分
 */
const handleRecharge = () => {
  ElMessage.info('充值功能开发中...')
}

/**
 * 查看交易详情
 */
const handleViewDetail = (_transaction: any) => {
  ElMessage.info('查看详情功能开发中...')
}

/**
 * 申请退款
 */
const handleRequestRefund = async (_transaction: any) => {
  try {
    await ElMessageBox.confirm(
      '确定要申请退款吗？',
      '申请退款',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('退款申请已提交')
    await loadTransactions()
  } catch {
    // 用户取消操作
  }
}

/**
 * 处理标签页变化
 */
const handleTabChange = () => {
  currentPage.value = 1
  loadTransactions()
}

/**
 * 处理筛选变化
 */
const handleFilterChange = () => {
  currentPage.value = 1
  loadTransactions()
}

/**
 * 处理日期变化
 */
const handleDateChange = () => {
  currentPage.value = 1
  loadTransactions()
}

/**
 * 处理分页变化
 */
const handlePageChange = () => {
  loadTransactions()
}

/**
 * 加载交易记录数据
 */
const loadTransactions = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    transactions.value = []
    
    totalElements.value = transactions.value.length
    
    // 更新统计数据
    stats.value = {
      currentBalance: 1580,
      totalEarned: 5000,
      totalSpent: 3420,
      recentTransactions: 8
    }
  } catch (error) {
    ElMessage.error('加载交易记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTransactions()
})
</script>

<style scoped>
.points-tabs {
  padding: 0 24px;
}

.loading-container {
  padding: 24px;
}

.transactions-list {
  padding: 0 24px 24px;
}

.transaction-item {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.transaction-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.transaction-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.transaction-info {
  display: flex;
  gap: 24px;
}

.transaction-date {
  font-weight: 600;
  color: #303133;
}

.transaction-id {
  color: #909399;
  font-size: 14px;
}

.transaction-amount {
  text-align: right;
}

.transaction-amount.income .amount {
  color: #67c23a;
}

.transaction-amount.expense .amount {
  color: #f56c6c;
}

.amount {
  font-size: 18px;
  font-weight: 700;
}

.unit {
  color: #909399;
  font-size: 14px;
  margin-left: 4px;
}

.transaction-content {
  padding: 24px;
  display: flex;
  justify-content: space-between;
  gap: 24px;
}

.transaction-details {
  display: flex;
  gap: 16px;
  flex: 1;
}

.transaction-type {
  flex-shrink: 0;
}

.transaction-description {
  flex: 1;
}

.description-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.description-remark {
  color: #606266;
  font-size: 14px;
  margin: 0 0 12px 0;
}

.transaction-meta {
  display: flex;
  gap: 16px;
}

.transaction-meta span {
  color: #909399;
  font-size: 12px;
}

.transaction-actions {
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
