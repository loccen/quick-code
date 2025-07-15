<template>
  <div class="points-view">
    <div class="page-header">
      <h1 class="page-title">积分管理</h1>
      <p class="page-subtitle">管理您的积分余额和交易记录</p>
    </div>

    <!-- 积分概览 -->
    <div class="points-overview">
      <div class="balance-card">
        <div class="balance-info">
          <div class="balance-amount">
            <span class="amount">{{ pointAccount.balance }}</span>
            <span class="unit">积分</span>
          </div>
          <div class="balance-label">当前余额</div>
        </div>
        <div class="balance-actions">
          <el-button type="primary" @click="handleRecharge">
            <el-icon><Plus /></el-icon>
            充值积分
          </el-button>
          <el-button @click="handleWithdraw">
            <el-icon><Minus /></el-icon>
            提现
          </el-button>
        </div>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ pointAccount.totalEarned }}</div>
            <div class="stat-label">累计收入</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ pointAccount.totalSpent }}</div>
            <div class="stat-label">累计支出</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <el-icon><Lock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ pointAccount.frozenAmount }}</div>
            <div class="stat-label">冻结金额</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 交易记录 -->
    <div class="transactions-section">
      <div class="section-header">
        <h2 class="section-title">交易记录</h2>
        
        <div class="section-filters">
          <el-select v-model="filterType" placeholder="交易类型" @change="handleFilterChange">
            <el-option label="全部" value="" />
            <el-option label="收入" value="EARN" />
            <el-option label="支出" value="SPEND" />
            <el-option label="退款" value="REFUND" />
            <el-option label="冻结" value="FREEZE" />
            <el-option label="解冻" value="UNFREEZE" />
          </el-select>
          
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleDateChange"
          />
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 交易列表 -->
      <div v-else-if="transactions.length > 0" class="transactions-list">
        <div
          v-for="transaction in transactions"
          :key="transaction.id"
          class="transaction-item"
        >
          <div class="transaction-icon">
            <el-icon :class="getTransactionIconClass(transaction.type)">
              <component :is="getTransactionIcon(transaction.type)" />
            </el-icon>
          </div>
          
          <div class="transaction-info">
            <div class="transaction-description">{{ transaction.description }}</div>
            <div class="transaction-time">{{ transaction.createdAt }}</div>
          </div>
          
          <div class="transaction-amount">
            <span :class="getAmountClass(transaction.type)">
              {{ getAmountPrefix(transaction.type) }}{{ transaction.amount }}
            </span>
            <div class="transaction-balance">余额：{{ transaction.balance }}</div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="暂无交易记录" />
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalElements"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Plus, 
  Minus, 
  TrendCharts, 
  ShoppingCart, 
  Lock,
  ArrowUp,
  ArrowDown,
  RefreshRight,
  Lock as LockIcon,
  Unlock
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const filterType = ref('')
const dateRange = ref<[Date, Date] | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)

const pointAccount = ref({
  balance: 8500,
  frozenAmount: 200,
  totalEarned: 15000,
  totalSpent: 6700
})

const transactions = ref<any[]>([])

const totalPages = computed(() => Math.ceil(totalElements.value / pageSize.value))

/**
 * 获取交易类型图标
 */
const getTransactionIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    'EARN': ArrowUp,
    'SPEND': ArrowDown,
    'REFUND': RefreshRight,
    'FREEZE': LockIcon,
    'UNFREEZE': Unlock
  }
  return iconMap[type] || ArrowUp
}

/**
 * 获取交易图标样式类
 */
const getTransactionIconClass = (type: string) => {
  const classMap: Record<string, string> = {
    'EARN': 'icon-earn',
    'SPEND': 'icon-spend',
    'REFUND': 'icon-refund',
    'FREEZE': 'icon-freeze',
    'UNFREEZE': 'icon-unfreeze'
  }
  return classMap[type] || 'icon-earn'
}

/**
 * 获取金额样式类
 */
const getAmountClass = (type: string) => {
  return type === 'EARN' || type === 'REFUND' || type === 'UNFREEZE' 
    ? 'amount-positive' 
    : 'amount-negative'
}

/**
 * 获取金额前缀
 */
const getAmountPrefix = (type: string) => {
  return type === 'EARN' || type === 'REFUND' || type === 'UNFREEZE' ? '+' : '-'
}

/**
 * 处理筛选变化
 */
const handleFilterChange = () => {
  currentPage.value = 1
  fetchTransactions()
}

/**
 * 处理日期范围变化
 */
const handleDateChange = () => {
  currentPage.value = 1
  fetchTransactions()
}

/**
 * 处理页码变化
 */
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchTransactions()
}

/**
 * 获取交易记录
 */
const fetchTransactions = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    const mockTransactions = [
      {
        id: 1,
        type: 'EARN',
        amount: 299,
        balance: 8500,
        description: '项目销售收入 - Vue3管理后台模板',
        createdAt: '2024-01-15 14:30:00'
      },
      {
        id: 2,
        type: 'SPEND',
        amount: 199,
        balance: 8201,
        description: '购买项目 - React Native电商App',
        createdAt: '2024-01-14 10:20:00'
      },
      {
        id: 3,
        type: 'EARN',
        amount: 599,
        balance: 8400,
        description: '项目销售收入 - Spring Boot微服务架构',
        createdAt: '2024-01-13 16:45:00'
      },
      {
        id: 4,
        type: 'REFUND',
        amount: 299,
        balance: 7801,
        description: '订单退款 - 项目质量问题',
        createdAt: '2024-01-12 09:15:00'
      },
      {
        id: 5,
        type: 'FREEZE',
        amount: 200,
        balance: 7502,
        description: '资金冻结 - 争议处理中',
        createdAt: '2024-01-11 11:30:00'
      }
    ]
    
    transactions.value = mockTransactions
    totalElements.value = mockTransactions.length
  } catch (error) {
    console.error('获取交易记录失败:', error)
    ElMessage.error('获取交易记录失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理充值
 */
const handleRecharge = () => {
  ElMessage.info('充值功能开发中...')
}

/**
 * 处理提现
 */
const handleWithdraw = () => {
  ElMessage.info('提现功能开发中...')
}

// 生命周期
onMounted(() => {
  fetchTransactions()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.points-view {
  .page-header {
    margin-bottom: $spacing-xl;

    .page-title {
      font-size: $font-size-3xl;
      font-weight: $font-weight-bold;
      color: var(--text-primary);
      margin: 0 0 $spacing-xs 0;
    }

    .page-subtitle {
      color: var(--text-secondary);
      margin: 0;
    }
  }

  .points-overview {
    margin-bottom: $spacing-3xl;

    .balance-card {
      background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-hover) 100%);
      color: white;
      border-radius: $border-radius-lg;
      padding: $spacing-xl;
      margin-bottom: $spacing-lg;
      @include flex-between();

      .balance-info {
        .balance-amount {
          .amount {
            font-size: $font-size-5xl;
            font-weight: $font-weight-bold;
          }

          .unit {
            font-size: $font-size-lg;
            margin-left: $spacing-sm;
            opacity: 0.9;
          }
        }

        .balance-label {
          font-size: $font-size-lg;
          margin-top: $spacing-sm;
          opacity: 0.8;
        }
      }

      .balance-actions {
        @include flex-center();
        gap: $spacing-md;

        .el-button {
          background: rgba(255, 255, 255, 0.2);
          border-color: rgba(255, 255, 255, 0.3);
          color: white;

          &:hover {
            background: rgba(255, 255, 255, 0.3);
          }
        }
      }
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: $spacing-lg;

      .stat-card {
        background: var(--bg-primary);
        border: 1px solid var(--border-color);
        border-radius: $border-radius-lg;
        padding: $spacing-lg;
        @include flex-start();
        gap: $spacing-md;

        .stat-icon {
          width: 48px;
          height: 48px;
          background: var(--bg-secondary);
          border-radius: 50%;
          @include flex-center();
          color: var(--primary-color);

          .el-icon {
            font-size: 24px;
          }
        }

        .stat-info {
          .stat-number {
            font-size: $font-size-2xl;
            font-weight: $font-weight-bold;
            color: var(--text-primary);
          }

          .stat-label {
            color: var(--text-secondary);
            font-size: $font-size-sm;
          }
        }
      }
    }
  }

  .transactions-section {
    .section-header {
      @include flex-between();
      margin-bottom: $spacing-lg;

      .section-title {
        font-size: $font-size-2xl;
        font-weight: $font-weight-bold;
        color: var(--text-primary);
        margin: 0;
      }

      .section-filters {
        @include flex-center();
        gap: $spacing-md;
      }
    }

    .loading-container {
      padding: $spacing-xl;
    }

    .transactions-list {
      background: var(--bg-primary);
      border: 1px solid var(--border-color);
      border-radius: $border-radius-lg;
      overflow: hidden;

      .transaction-item {
        @include flex-start();
        gap: $spacing-md;
        padding: $spacing-lg;
        border-bottom: 1px solid var(--border-color);

        &:last-child {
          border-bottom: none;
        }

        .transaction-icon {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          @include flex-center();

          &.icon-earn {
            background: rgba(var(--success-color-rgb), 0.1);
            color: var(--success-color);
          }

          &.icon-spend {
            background: rgba(var(--danger-color-rgb), 0.1);
            color: var(--danger-color);
          }

          &.icon-refund {
            background: rgba(var(--warning-color-rgb), 0.1);
            color: var(--warning-color);
          }

          &.icon-freeze,
          &.icon-unfreeze {
            background: rgba(var(--info-color-rgb), 0.1);
            color: var(--info-color);
          }
        }

        .transaction-info {
          flex: 1;

          .transaction-description {
            font-weight: $font-weight-medium;
            color: var(--text-primary);
            margin-bottom: $spacing-xs;
          }

          .transaction-time {
            color: var(--text-secondary);
            font-size: $font-size-sm;
          }
        }

        .transaction-amount {
          text-align: right;

          .amount-positive {
            color: var(--success-color);
            font-weight: $font-weight-bold;
            font-size: $font-size-lg;
          }

          .amount-negative {
            color: var(--danger-color);
            font-weight: $font-weight-bold;
            font-size: $font-size-lg;
          }

          .transaction-balance {
            color: var(--text-secondary);
            font-size: $font-size-sm;
            margin-top: $spacing-xs;
          }
        }
      }
    }

    .empty-state {
      padding: $spacing-3xl;
      text-align: center;
    }

    .pagination-container {
      @include flex-center();
      justify-content: center;
      padding-top: $spacing-xl;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .points-view {
    .points-overview {
      .balance-card {
        flex-direction: column;
        gap: $spacing-lg;
        text-align: center;
      }

      .stats-grid {
        grid-template-columns: 1fr;
      }
    }

    .transactions-section {
      .section-header {
        flex-direction: column;
        gap: $spacing-md;
        align-items: stretch;

        .section-filters {
          justify-content: stretch;
          flex-direction: column;
        }
      }
    }
  }
}
</style>
