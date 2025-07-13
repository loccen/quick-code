<template>
  <div class="my-orders-view">
    <div class="page-header">
      <h1 class="page-title">我的订单</h1>
      <p class="page-subtitle">查看您的购买记录和订单状态</p>
    </div>

    <!-- 订单筛选 -->
    <div class="filter-section">
      <el-tabs v-model="activeStatus" @tab-change="handleStatusChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待支付" name="pending" />
        <el-tab-pane label="已支付" name="paid" />
        <el-tab-pane label="已取消" name="cancelled" />
        <el-tab-pane label="已退款" name="refunded" />
      </el-tabs>

      <div class="filter-actions">
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
      </div>
    </div>

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
              <span class="amount">¥{{ order.amount }}</span>
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
              size="small"
              type="primary"
              @click="handlePayOrder(order)"
            >
              立即支付
            </el-button>

            <el-button
              v-if="order.status === 'PENDING'"
              size="small"
              type="danger"
              @click="handleCancelOrder(order)"
            >
              取消订单
            </el-button>

            <el-button
              v-if="order.status === 'PAID'"
              size="small"
              type="success"
              @click="handleDownloadProject(order.project)"
            >
              下载源码
            </el-button>

            <el-button
              v-if="order.status === 'PAID'"
              size="small"
              @click="handleRequestRefund(order)"
            >
              申请退款
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty description="暂无订单记录" />
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
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

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

const totalPages = computed(() => Math.ceil(totalElements.value / pageSize.value))

/**
 * 获取订单状态类型
 */
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': 'warning',
    'PAID': 'success',
    'CANCELLED': 'info',
    'REFUNDED': 'danger'
  }
  return statusMap[status] || 'info'
}

/**
 * 获取订单状态文本
 */
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款'
  }
  return statusMap[status] || '未知'
}

/**
 * 处理状态切换
 */
const handleStatusChange = (status: string) => {
  activeStatus.value = status
  currentPage.value = 1
  fetchOrders()
}

/**
 * 处理日期范围变化
 */
const handleDateChange = () => {
  currentPage.value = 1
  fetchOrders()
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

/**
 * 处理页码变化
 */
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchOrders()
}

/**
 * 获取订单列表
 */
const fetchOrders = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 模拟数据
    const mockOrders = Array.from({ length: 5 }, (_, index) => ({
      id: index + 1,
      orderNo: `ORD${Date.now()}${index}`,
      amount: 299 + index * 100,
      status: ['PENDING', 'PAID', 'CANCELLED', 'REFUNDED'][index % 4],
      createdAt: `2024-01-${String(index + 1).padStart(2, '0')} 10:30:00`,
      paymentTime: index % 2 === 0 ? `2024-01-${String(index + 1).padStart(2, '0')} 10:35:00` : null,
      project: {
        id: index + 1,
        title: `项目 ${index + 1}`,
        description: `这是第 ${index + 1} 个项目的描述`,
        thumbnail: `/images/project-${index + 1}.jpg`,
        author: `开发者${index + 1}`,
        category: 'Web应用'
      }
    }))

    orders.value = mockOrders
    totalElements.value = mockOrders.length
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
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
const handlePayOrder = (order: any) => {
  ElMessage.info('支付功能开发中...')
}

/**
 * 取消订单
 */
const handleCancelOrder = async (order: any) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这个订单吗？',
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    ElMessage.success('订单已取消')
    fetchOrders()
  } catch {
    // 用户取消操作
  }
}

/**
 * 下载项目
 */
const handleDownloadProject = (project: any) => {
  ElMessage.info('下载功能开发中...')
}

/**
 * 申请退款
 */
const handleRequestRefund = async (order: any) => {
  try {
    await ElMessageBox.confirm(
      '确定要申请退款吗？退款将在3-5个工作日内处理。',
      '申请退款',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    ElMessage.success('退款申请已提交')
    fetchOrders()
  } catch {
    // 用户取消操作
  }
}

// 生命周期
onMounted(() => {
  fetchOrders()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.my-orders-view {
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

  .filter-section {
    margin-bottom: $spacing-xl;

    .filter-actions {
      @include flex-end();
      gap: $spacing-md;
      margin-top: $spacing-md;

      .el-input {
        width: 300px;
      }
    }
  }

  .loading-container {
    padding: $spacing-xl;
  }

  .orders-list {
    .order-item {
      background: var(--bg-primary);
      border: 1px solid var(--border-color);
      border-radius: $border-radius-lg;
      margin-bottom: $spacing-lg;
      overflow: hidden;

      .order-header {
        @include flex-between();
        padding: $spacing-md $spacing-lg;
        background: var(--bg-secondary);
        border-bottom: 1px solid var(--border-color);

        .order-info {
          @include flex-start();
          gap: $spacing-lg;

          .order-no {
            font-weight: $font-weight-medium;
            color: var(--text-primary);
          }

          .order-date {
            color: var(--text-secondary);
            font-size: $font-size-sm;
          }
        }
      }

      .order-content {
        padding: $spacing-lg;
        @include flex-between();
        gap: $spacing-lg;

        .project-info {
          @include flex-start();
          gap: $spacing-md;
          flex: 1;

          .project-thumbnail {
            width: 80px;
            height: 60px;
            border-radius: $border-radius-md;
            overflow: hidden;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }
          }

          .project-details {
            flex: 1;

            .project-title {
              font-size: $font-size-lg;
              font-weight: $font-weight-medium;
              color: var(--text-primary);
              margin: 0 0 $spacing-xs 0;
            }

            .project-description {
              color: var(--text-secondary);
              font-size: $font-size-sm;
              margin: 0 0 $spacing-sm 0;
              @include text-ellipsis;
            }

            .project-meta {
              @include flex-start();
              gap: $spacing-md;

              span {
                color: var(--text-secondary);
                font-size: $font-size-xs;
              }
            }
          }
        }

        .order-amount {
          text-align: center;

          .amount-info {
            .amount {
              font-size: $font-size-xl;
              font-weight: $font-weight-bold;
              color: var(--primary-color);
            }

            .unit {
              color: var(--text-secondary);
              font-size: $font-size-sm;
              margin-left: $spacing-xs;
            }
          }
        }

        .order-actions {
          @include flex-center();
          flex-direction: column;
          gap: $spacing-sm;
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

// 响应式设计
@media (max-width: 768px) {
  .my-orders-view {
    .filter-section {
      .filter-actions {
        flex-direction: column;
        align-items: stretch;

        .el-input {
          width: 100%;
        }
      }
    }

    .orders-list {
      .order-item {
        .order-content {
          flex-direction: column;
          gap: $spacing-md;

          .order-actions {
            flex-direction: row;
            flex-wrap: wrap;
          }
        }
      }
    }
  }
}
</style>
