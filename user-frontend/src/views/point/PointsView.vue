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
                  <el-option label="消费" value="CONSUME" />
                  <el-option label="退款" value="REFUND" />
                  <el-option label="奖励" value="REWARD" />
                  <el-option label="提现" value="WITHDRAW" />
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
                <span class="transaction-date">{{ transaction.createdTime }}</span>
              </div>
              <div class="transaction-amount" :class="getAmountClass(transaction.isIncome)">
                <span class="amount">{{ getAmountText(transaction) }}</span>
                <span class="unit">积分</span>
              </div>
            </div>

            <div class="transaction-content">
              <div class="transaction-details">
                <div class="transaction-type">
                  <el-tag :type="getTypeColor(transaction.typeName)">
                    {{ getTypeText(transaction) }}
                  </el-tag>
                </div>
                <div class="transaction-description">
                  <h4 class="description-title">{{ transaction.description }}</h4>
                  <p v-if="transaction.remark" class="description-remark">{{ transaction.remark }}</p>
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
import { pointApi, PointTransactionType, POINT_TRANSACTION_TYPE_CONFIG } from '@/api/modules/point'

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
const getTypeColor = (typeName: string) => {
  const config = getTransactionTypeConfig(typeName)
  return config.color
}

/**
 * 获取交易类型文本
 */
const getTypeText = (transaction: any) => {
  // 优先使用后端返回的typeDescription，如果没有则使用配置
  return transaction.typeDescription || getTransactionTypeConfig(transaction.typeName).label
}

/**
 * 获取金额样式类
 */
const getAmountClass = (isIncome: boolean) => {
  return isIncome ? 'income' : 'expense'
}

/**
 * 获取金额文本
 */
const getAmountText = (transaction: any) => {
  return formatTransactionAmount(transaction.amount, transaction.isIncome)
}



/**
 * 充值积分
 */
const handleRecharge = async () => {
  try {
    const { value: amount } = await ElMessageBox.prompt(
      '请输入充值金额（元）',
      '积分充值',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^\d+(\.\d{1,2})?$/,
        inputErrorMessage: '请输入有效的金额'
      }
    )

    if (amount) {
      // 使用URLSearchParams发送表单数据
      const params = new URLSearchParams()
      params.append('amount', amount)
      params.append('description', '积分充值')

      const response = await pointApi.rechargePoints(params)

      if (response && response.code === 200) {
        ElMessage.success('充值成功')
        loadPointAccount() // 重新加载账户信息
        loadTransactions() // 重新加载交易记录
      } else {
        throw new Error(response?.message || '充值失败')
      }
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('充值失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '充值失败')
    }
  }
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
const handleRequestRefund = async (transaction: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要申请退款 ${transaction.amount} 积分吗？`,
      '申请退款',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 这里需要根据实际业务逻辑调用相应的退款API
    // 如果是订单退款，应该调用订单退款API
    // 如果是积分转账退款，应该调用积分退款API
    ElMessage.info('退款功能需要根据具体业务场景实现')

    // 示例：如果有通用的退款API
    // const response = await pointApi.requestRefund(transaction.id, '用户申请退款')
    // if (response && response.code === 200) {
    //   ElMessage.success('退款申请已提交')
    //   await loadTransactions()
    // }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('申请退款失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '申请退款失败')
    }
  }
}

/**
 * 处理标签页变化
 */
const handleTabChange = () => {
  currentPage.value = 1

  // 根据tab页自动设置交易类型筛选
  switch (activeTab.value) {
    case 'income':
      // 收入类型：充值、奖励、退款
      transactionType.value = 'RECHARGE,REWARD,REFUND'
      break
    case 'expense':
      // 支出类型：消费、提现
      transactionType.value = 'CONSUME,WITHDRAW'
      break
    default:
      // 全部记录
      transactionType.value = ''
      break
  }

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
 * 加载积分账户信息
 */
const loadPointAccount = async () => {
  try {
    const response = await pointApi.getPointAccount()
    if (response && response.code === 200 && response.data) {
      stats.value = {
        currentBalance: response.data.availablePoints || 0,
        totalEarned: response.data.totalEarned || 0,
        totalSpent: response.data.totalSpent || 0,
        recentTransactions: 0 // 需要单独计算本月交易
      }
    }
  } catch (error: any) {
    console.error('加载积分账户信息失败:', error)
    // 账户信息加载失败不显示错误消息，保持默认值
  }
}

/**
 * 加载交易记录数据
 */
const loadTransactions = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value - 1, // 后端页码从0开始
      size: pageSize.value,
      sortBy: 'createdTime',
      sortDirection: 'DESC'
    }

    // 添加交易类型筛选
    if (transactionType.value) {
      params.type = transactionType.value
    }

    // 添加日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString().split('T')[0]
      params.endDate = dateRange.value[1].toISOString().split('T')[0]
    }

    // 根据标签页筛选
    if (activeTab.value === 'income') {
      // 收入记录：充值、退款、奖励
      params.type = 'RECHARGE,REFUND,REWARD'
    } else if (activeTab.value === 'expense') {
      // 支出记录：消费
      params.type = 'CONSUME,WITHDRAW'
    }

    const response = await pointApi.getPointTransactions(params)
    if (response && response.code === 200 && response.data) {
      transactions.value = response.data.content || []
      totalElements.value = response.data.total || 0
    } else {
      throw new Error(response?.message || '获取交易记录失败')
    }
  } catch (error: any) {
    console.error('加载交易记录失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '加载交易记录失败')
    transactions.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 获取交易类型显示配置
 */
const getTransactionTypeConfig = (typeName: string) => {
  return POINT_TRANSACTION_TYPE_CONFIG[typeName as PointTransactionType] || {
    label: typeName,
    color: 'info',
    isIncome: false
  }
}

/**
 * 格式化交易金额显示
 */
const formatTransactionAmount = (amount: number, isIncome: boolean) => {
  const prefix = isIncome ? '+' : '-'
  return `${prefix}${Math.abs(amount)}`
}

onMounted(() => {
  loadPointAccount()
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
