<template>
  <div class="project-purchase-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/projects' }">项目市场</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: `/projects/${projectId}` }">项目详情</el-breadcrumb-item>
          <el-breadcrumb-item>购买确认</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <div class="container">
        <div class="purchase-container">
          <!-- 左侧：项目信息 -->
          <div class="project-info-section">
            <div class="glass-card">
              <h2 class="section-title">
                <el-icon><ShoppingCart /></el-icon>
                购买项目
              </h2>
              
              <div v-if="loading" class="loading-state">
                <el-skeleton :rows="5" animated />
              </div>
              
              <div v-else-if="project" class="project-details">
                <div class="project-header">
                  <img 
                    :src="project.coverImage || '/default-project.png'" 
                    :alt="project.title"
                    class="project-cover"
                  />
                  <div class="project-meta">
                    <h3 class="project-title">{{ project.title }}</h3>
                    <p class="project-description">{{ project.description }}</p>
                    <div class="project-tags">
                      <el-tag 
                        v-for="tag in project.tags" 
                        :key="tag" 
                        size="small"
                        class="tag-item"
                      >
                        {{ tag }}
                      </el-tag>
                    </div>
                  </div>
                </div>
                
                <div class="project-stats">
                  <div class="stat-item">
                    <span class="stat-label">作者</span>
                    <span class="stat-value">{{ project.authorName }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">下载次数</span>
                    <span class="stat-value">{{ project.downloadCount || 0 }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">评分</span>
                    <span class="stat-value">
                      <el-rate 
                        v-model="project.rating" 
                        disabled 
                        show-score 
                        text-color="#ff9900"
                        score-template="{value}"
                      />
                    </span>
                  </div>
                </div>
                
                <div class="price-section">
                  <div class="price-display">
                    <span class="price-label">项目价格</span>
                    <span class="price-value">{{ project.price }} 积分</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：支付信息 -->
          <div class="payment-section">
            <div class="glass-card">
              <h2 class="section-title">
                <el-icon><CreditCard /></el-icon>
                支付信息
              </h2>
              
              <!-- 账户余额 -->
              <div class="balance-info">
                <div class="balance-item">
                  <span class="balance-label">可用积分</span>
                  <span class="balance-value">{{ userBalance.points || 0 }} 积分</span>
                </div>
                <div class="balance-item">
                  <span class="balance-label">账户余额</span>
                  <span class="balance-value">¥{{ userBalance.balance || 0 }}</span>
                </div>
              </div>
              
              <!-- 支付方式选择 -->
              <div class="payment-method">
                <h3 class="method-title">选择支付方式</h3>
                <el-radio-group v-model="paymentForm.paymentMethod" class="method-options">
                  <el-radio label="POINTS" class="method-option">
                    <div class="method-content">
                      <el-icon><Star /></el-icon>
                      <span>积分支付</span>
                      <span class="method-desc">使用积分直接购买</span>
                    </div>
                  </el-radio>
                  <el-radio label="BALANCE" class="method-option" disabled>
                    <div class="method-content">
                      <el-icon><Wallet /></el-icon>
                      <span>余额支付</span>
                      <span class="method-desc">暂不支持</span>
                    </div>
                  </el-radio>
                  <el-radio label="MIXED" class="method-option" disabled>
                    <div class="method-content">
                      <el-icon><Money /></el-icon>
                      <span>混合支付</span>
                      <span class="method-desc">积分+余额</span>
                    </div>
                  </el-radio>
                </el-radio-group>
              </div>
              
              <!-- 支付金额详情 -->
              <div class="payment-details">
                <div class="detail-item">
                  <span class="detail-label">项目价格</span>
                  <span class="detail-value">{{ project?.price || 0 }} 积分</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">优惠折扣</span>
                  <span class="detail-value">-0 积分</span>
                </div>
                <div class="detail-item total">
                  <span class="detail-label">应付总额</span>
                  <span class="detail-value">{{ project?.price || 0 }} 积分</span>
                </div>
              </div>
              
              <!-- 余额不足提示 -->
              <div v-if="!hasEnoughBalance" class="insufficient-balance">
                <el-alert
                  title="积分余额不足"
                  type="warning"
                  :description="`您的积分余额为 ${userBalance.points} 积分，需要 ${project?.price || 0} 积分`"
                  show-icon
                  :closable="false"
                />
                <div class="recharge-hint">
                  <el-button type="primary" size="small" @click="goToRecharge">
                    <el-icon><Plus /></el-icon>
                    充值积分
                  </el-button>
                </div>
              </div>
              
              <!-- 购买按钮 -->
              <div class="purchase-actions">
                <el-button 
                  size="large" 
                  @click="goBack"
                  class="cancel-btn"
                >
                  取消
                </el-button>
                <el-button 
                  type="primary" 
                  size="large" 
                  :loading="purchasing"
                  :disabled="!canPurchase"
                  @click="confirmPurchase"
                  class="purchase-btn"
                >
                  <el-icon v-if="!purchasing"><Check /></el-icon>
                  {{ purchasing ? '处理中...' : '确认购买' }}
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ShoppingCart, 
  CreditCard, 
  Star, 
  Wallet, 
  Money, 
  Plus, 
  Check 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { projectApi } from '@/api/project'
import { orderApi } from '@/api/order'
import { pointApi } from '@/api/point'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const projectId = ref(route.params.id)
const loading = ref(true)
const purchasing = ref(false)
const project = ref(null)
const userBalance = ref({
  points: 0,
  balance: 0
})

// 支付表单
const paymentForm = ref({
  paymentMethod: 'POINTS',
  pointsAmount: 0,
  balanceAmount: 0
})

// 计算属性
const hasEnoughBalance = computed(() => {
  if (!project.value || !userBalance.value) return false
  return userBalance.value.points >= project.value.price
})

const canPurchase = computed(() => {
  return hasEnoughBalance.value && !purchasing.value && project.value
})

// 方法
const loadProjectInfo = async () => {
  try {
    const response = await projectApi.getProjectById(projectId.value)
    if (response.success) {
      project.value = response.data
      paymentForm.value.pointsAmount = response.data.price
    } else {
      ElMessage.error('加载项目信息失败')
      goBack()
    }
  } catch (error) {
    console.error('加载项目信息失败:', error)
    ElMessage.error('加载项目信息失败')
    goBack()
  }
}

const loadUserBalance = async () => {
  try {
    const response = await pointApi.getPointAccount()
    if (response.success) {
      userBalance.value.points = response.data.availablePoints
    }
    // TODO: 加载用户余额
  } catch (error) {
    console.error('加载用户余额失败:', error)
  }
}

const confirmPurchase = async () => {
  try {
    // 二次确认
    await ElMessageBox.confirm(
      `确认购买项目"${project.value.title}"？将消耗 ${project.value.price} 积分。`,
      '购买确认',
      {
        confirmButtonText: '确认购买',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    purchasing.value = true

    // 创建订单
    const createOrderResponse = await orderApi.createOrder({
      projectId: projectId.value,
      remark: '项目购买'
    })

    if (!createOrderResponse.success) {
      throw new Error(createOrderResponse.message || '创建订单失败')
    }

    const orderNo = createOrderResponse.data.orderNo

    // 支付订单
    const paymentResponse = await orderApi.payOrder(orderNo, {
      paymentMethod: paymentForm.value.paymentMethod,
      pointsAmount: paymentForm.value.pointsAmount,
      balanceAmount: paymentForm.value.balanceAmount
    })

    if (paymentResponse.success) {
      ElMessage.success('购买成功！')
      // 跳转到项目详情页或我的购买页面
      router.push(`/projects/${projectId.value}`)
    } else {
      throw new Error(paymentResponse.message || '支付失败')
    }

  } catch (error) {
    if (error !== 'cancel') {
      console.error('购买失败:', error)
      ElMessage.error(error.message || '购买失败，请重试')
    }
  } finally {
    purchasing.value = false
  }
}

const goBack = () => {
  router.back()
}

const goToRecharge = () => {
  router.push('/user/points')
}

// 生命周期
onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      loadProjectInfo(),
      loadUserBalance()
    ])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.project-purchase-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.page-header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 1rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.main-content {
  padding: 2rem 0;
}

.purchase-container {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 2rem;
  align-items: start;
}

.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.5rem;
  font-weight: 600;
  color: white;
  margin-bottom: 1.5rem;
}

.project-header {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.project-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.project-meta {
  flex: 1;
}

.project-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: white;
  margin-bottom: 0.5rem;
}

.project-description {
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-item {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.project-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-label {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
}

.stat-value {
  font-weight: 600;
  color: white;
}

.price-section {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 1.5rem;
}

.price-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-label {
  font-size: 1.125rem;
  color: rgba(255, 255, 255, 0.8);
}

.price-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #ffd700;
}

.balance-info {
  margin-bottom: 1.5rem;
}

.balance-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.balance-label {
  color: rgba(255, 255, 255, 0.8);
}

.balance-value {
  font-weight: 600;
  color: white;
}

.payment-method {
  margin-bottom: 1.5rem;
}

.method-title {
  font-size: 1.125rem;
  color: white;
  margin-bottom: 1rem;
}

.method-options {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.method-option {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.method-option:hover {
  background: rgba(255, 255, 255, 0.1);
}

.method-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: white;
}

.method-desc {
  margin-left: auto;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
}

.payment-details {
  margin-bottom: 1.5rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
}

.detail-item.total {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 1rem;
  margin-top: 0.5rem;
  font-weight: 600;
  font-size: 1.125rem;
}

.detail-label {
  color: rgba(255, 255, 255, 0.8);
}

.detail-value {
  color: white;
  font-weight: 500;
}

.insufficient-balance {
  margin-bottom: 1.5rem;
}

.recharge-hint {
  margin-top: 0.75rem;
  text-align: center;
}

.purchase-actions {
  display: flex;
  gap: 1rem;
}

.cancel-btn {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
}

.purchase-btn {
  flex: 2;
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
}

@media (max-width: 768px) {
  .purchase-container {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .project-header {
    flex-direction: column;
    text-align: center;
  }
  
  .project-stats {
    grid-template-columns: 1fr;
  }
}
</style>
