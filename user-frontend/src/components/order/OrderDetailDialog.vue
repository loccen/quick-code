<template>
  <el-dialog
    v-model="visible"
    title="订单详情"
    width="600px"
    :before-close="handleClose"
    class="order-detail-dialog"
  >
    <div v-if="order" class="order-detail-content">
      <!-- 订单基本信息 -->
      <div class="detail-section">
        <h3 class="section-title">订单信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">订单号</span>
            <span class="info-value">{{ order.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">订单状态</span>
            <el-tag :type="getStatusType(order.status)">
              {{ order.statusDescription }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ formatDateTime(order.createdTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">订单金额</span>
            <span class="info-value amount">{{ order.amount }} 积分</span>
          </div>
        </div>
      </div>

      <!-- 项目信息 -->
      <div class="detail-section">
        <h3 class="section-title">项目信息</h3>
        <div class="project-info">
          <img 
            :src="order.projectCoverImage || '/default-project.png'" 
            :alt="order.projectName"
            class="project-cover"
          />
          <div class="project-details">
            <h4 class="project-title">{{ order.projectName }}</h4>
            <p class="project-description">{{ order.projectDescription }}</p>
            <div class="project-meta">
              <span class="meta-item">作者：{{ order.sellerNickname || order.sellerUsername }}</span>
              <span class="meta-item">价格：{{ order.projectPrice }} 积分</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 支付信息 -->
      <div v-if="order.paymentTime" class="detail-section">
        <h3 class="section-title">支付信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">支付方式</span>
            <span class="info-value">{{ order.paymentMethodDescription || '积分支付' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">支付时间</span>
            <span class="info-value">{{ formatDateTime(order.paymentTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">支付金额</span>
            <span class="info-value amount">{{ order.amount }} 积分</span>
          </div>
        </div>
      </div>

      <!-- 完成信息 -->
      <div v-if="order.completionTime" class="detail-section">
        <h3 class="section-title">完成信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">完成时间</span>
            <span class="info-value">{{ formatDateTime(order.completionTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 取消信息 -->
      <div v-if="order.cancellationTime" class="detail-section">
        <h3 class="section-title">取消信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">取消时间</span>
            <span class="info-value">{{ formatDateTime(order.cancellationTime) }}</span>
          </div>
          <div v-if="order.remark" class="info-item full-width">
            <span class="info-label">取消原因</span>
            <span class="info-value">{{ order.remark }}</span>
          </div>
        </div>
      </div>

      <!-- 退款信息 -->
      <div v-if="order.refundTime" class="detail-section">
        <h3 class="section-title">退款信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">退款时间</span>
            <span class="info-value">{{ formatDateTime(order.refundTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">退款金额</span>
            <span class="info-value amount">{{ order.refundAmount || order.amount }} 积分</span>
          </div>
        </div>
      </div>

      <!-- 订单备注 -->
      <div v-if="order.remark && !order.cancellationTime" class="detail-section">
        <h3 class="section-title">订单备注</h3>
        <div class="remark-content">
          {{ order.remark }}
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        
        <el-button 
          v-if="order?.canCancel" 
          type="warning"
          @click="handleCancel"
        >
          取消订单
        </el-button>
        
        <el-button 
          v-if="order?.canRefund" 
          type="danger"
          @click="handleRefund"
        >
          申请退款
        </el-button>
        
        <el-button 
          v-if="order?.isPaid && !order?.isCompleted" 
          type="success"
          @click="handleDownload"
        >
          下载项目
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/modules/order'

const router = useRouter()

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  order: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'refresh'])

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 方法
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

const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const handleClose = () => {
  visible.value = false
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
      `确认取消订单"${props.order.projectName}"？`,
      '取消订单',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await orderApi.cancelOrder(props.order.orderNo, '用户主动取消')
    
    if (response.success) {
      ElMessage.success('订单取消成功')
      emit('refresh')
      handleClose()
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

const handleRefund = async () => {
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

    const response = await orderApi.requestRefund(props.order.orderNo, reason)
    
    if (response.success) {
      ElMessage.success('退款申请提交成功')
      emit('refresh')
      handleClose()
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

const handleDownload = () => {
  // 跳转到项目详情页进行下载
  router.push(`/projects/${props.order.projectId}`)
  handleClose()
}
</script>

<style scoped>
.order-detail-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.order-detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.section-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border-radius: 2px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 0.875rem;
  color: #666;
  font-weight: 500;
}

.info-value {
  font-size: 1rem;
  color: #333;
  font-weight: 500;
}

.info-value.amount {
  color: #f56c6c;
  font-weight: 600;
}

.project-info {
  display: flex;
  gap: 1rem;
}

.project-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #eee;
}

.project-details {
  flex: 1;
}

.project-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.project-description {
  color: #666;
  line-height: 1.5;
  margin-bottom: 0.5rem;
}

.project-meta {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.meta-item {
  font-size: 0.875rem;
  color: #888;
}

.remark-content {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  color: #666;
  line-height: 1.5;
  border: 1px solid #eee;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .project-info {
    flex-direction: column;
    text-align: center;
  }
  
  .project-cover {
    align-self: center;
  }
  
  .dialog-footer {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
