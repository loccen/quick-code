<template>
  <div class="project-purchase-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="container">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/market' }">项目市场</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: `/market/project/${projectId}` }">项目详情</el-breadcrumb-item>
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
                  <div class="project-image-container">
                    <img
                      v-if="project.coverImage || project.thumbnail"
                      :src="project.coverImage || project.thumbnail || '/images/default-project.jpg'"
                      :alt="project.title"
                      class="project-cover"
                    />
                    <div v-else class="image-placeholder">
                      <el-icon class="placeholder-icon"><Picture /></el-icon>
                      <span class="placeholder-text">暂无图片</span>
                    </div>
                  </div>
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
                
                <!-- 项目作者信息 -->
                <div class="author-info">
                  <div class="author-avatar">
                    <img
                      :src="project.userAvatar || '/images/default-avatar.svg'"
                      :alt="project.authorName || project.username || project.author"
                      class="avatar-image"
                    />
                  </div>
                  <div class="author-details">
                    <div class="author-name">{{ project.authorName || project.username || project.author || '未知作者' }}</div>
                    <div class="author-title">项目作者</div>
                  </div>
                </div>

                <div class="project-stats">
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
                    <span class="price-value">
                      <i class="fas fa-coins"></i>
                      {{ project.price }}
                    </span>
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
                  <span class="balance-value">
                    <i class="fas fa-coins"></i>
                    {{ userBalance.points || 0 }}
                  </span>
                </div>
              </div>

              <!-- 支付方式选择 -->
              <div class="payment-method">
                <h3 class="method-title">选择支付方式</h3>
                <el-radio-group v-model="paymentForm.paymentMethod" class="method-options">
                  <el-radio label="POINTS" class="method-option">
                    <div class="method-content">
                      <i class="fas fa-coins"></i>
                      <span>积分支付</span>
                      <span class="method-desc">使用积分直接购买</span>
                    </div>
                  </el-radio>
                </el-radio-group>
              </div>
              
              <!-- 支付金额详情 -->
              <div class="payment-details">
                <div class="detail-item">
                  <span class="detail-label">项目价格</span>
                  <span class="detail-value">
                    <i class="fas fa-coins"></i>
                    {{ project?.price || 0 }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">优惠折扣</span>
                  <span class="detail-value">
                    <i class="fas fa-coins"></i>
                    -0
                  </span>
                </div>
                <div class="detail-item total">
                  <span class="detail-label">应付总额</span>
                  <span class="detail-value">
                    <i class="fas fa-coins"></i>
                    {{ project?.price || 0 }}
                  </span>
                </div>
              </div>
              
              <!-- 自己项目提示 -->
              <div v-if="isOwnProject" class="own-project-notice">
                <el-alert
                  title="无法购买自己的项目"
                  type="info"
                  description="这是您上传的项目，无需购买即可使用"
                  show-icon
                  :closable="false"
                />
              </div>

              <!-- 已购买提示 -->
              <div v-else-if="hasPurchased" class="already-purchased-notice">
                <el-alert
                  title="您已购买此项目"
                  type="success"
                  description="您已成功购买此项目，可以直接下载使用"
                  show-icon
                  :closable="false"
                />
                <div class="download-hint">
                  <el-button type="success" size="small" @click="goToDownloads">
                    <el-icon><Download /></el-icon>
                    查看下载记录
                  </el-button>
                </div>
              </div>

              <!-- 余额不足提示 -->
              <div v-else-if="!hasEnoughBalance" class="insufficient-balance">
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
                  {{
                    purchasing ? '处理中...' :
                    isOwnProject ? '这是您的项目' :
                    hasPurchased ? '已购买' :
                    '确认购买'
                  }}
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ShoppingCart,
  CreditCard,
  Picture,
  Plus,
  Check,
  Download
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { projectApi } from '@/api/modules/project'
import { orderApi } from '@/api/modules/order'
import { pointApi } from '@/api/modules/point'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const projectId = ref(route.params.id)
const loading = ref(true)
const purchasing = ref(false)
const project = ref<any>(null)
const userBalance = ref({
  points: 0,
  balance: 0
})
const hasPurchased = ref(false)
const checkingPurchaseStatus = ref(false)

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

const isOwnProject = computed(() => {
  if (!project.value || !userStore.user) return false
  return project.value.userId === userStore.user.id
})

const canPurchase = computed(() => {
  return hasEnoughBalance.value && !purchasing.value && project.value && !isOwnProject.value && !hasPurchased.value
})

// 方法
const loadProjectInfo = async () => {
  try {
    const response = await projectApi.getProject(projectId.value)
    if (response.success) {
      project.value = response.data
      paymentForm.value.pointsAmount = response.data.price
      // 检查购买状态
      await checkPurchaseStatus()
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

const checkPurchaseStatus = async () => {
  if (!userStore.isLoggedIn || !projectId.value) return

  try {
    checkingPurchaseStatus.value = true
    const response = await orderApi.hasUserPurchasedProject(projectId.value)
    if (response.success) {
      hasPurchased.value = response.data
    }
  } catch (error) {
    console.error('检查购买状态失败:', error)
  } finally {
    checkingPurchaseStatus.value = false
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
    // 检查是否是自己的项目
    if (isOwnProject.value) {
      ElMessage.warning('无法购买自己的项目')
      return
    }

    // 检查余额是否充足
    if (!hasEnoughBalance.value) {
      ElMessage.warning('积分余额不足')
      return
    }

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
      router.push(`/user/purchases`)
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

const goToDownloads = () => {
  router.push('/user/downloads')
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

/* 面包屑导航样式 */
:deep(.el-breadcrumb) {
  .el-breadcrumb__item {
    .el-breadcrumb__inner {
      color: rgba(255, 255, 255, 0.9) !important;
      font-weight: 500;

      &:hover {
        color: white !important;
      }
    }

    &:last-child .el-breadcrumb__inner {
      color: white !important;
      font-weight: 600;
    }
  }

  .el-breadcrumb__separator {
    color: rgba(255, 255, 255, 0.7) !important;
  }
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

.project-image-container {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}

.project-cover {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 2px dashed rgba(255, 255, 255, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  color: rgba(255, 255, 255, 0.6);

  .placeholder-icon {
    font-size: 2rem;
  }

  .placeholder-text {
    font-size: 0.875rem;
  }
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

.author-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 1.5rem;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-details {
  flex: 1;
}

.author-name {
  font-size: 1rem;
  font-weight: 600;
  color: white;
  margin-bottom: 0.25rem;
}

.author-title {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
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
  display: flex;
  align-items: center;
  gap: 0.5rem;

  i {
    color: #ffd700;
    font-size: 1.25rem;
  }
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
  display: flex;
  align-items: center;
  gap: 0.5rem;

  i {
    color: #ffd700;
    font-size: 1rem;
  }
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

  i {
    color: #ffd700;
    font-size: 1.125rem;
  }
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
  display: flex;
  align-items: center;
  gap: 0.5rem;

  i {
    color: #ffd700;
    font-size: 1rem;
  }
}

.own-project-notice {
  margin-bottom: 1.5rem;
}

.already-purchased-notice {
  margin-bottom: 1.5rem;
}

.download-hint {
  margin-top: 0.75rem;
  text-align: center;
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
