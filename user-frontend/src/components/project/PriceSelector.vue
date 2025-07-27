<template>
  <div class="price-selector">
    <!-- 价格类型选择 -->
    <div class="price-type-selector">
      <el-radio-group v-model="priceType" @change="handlePriceTypeChange">
        <el-radio-button label="free">
          <el-icon><Present /></el-icon>
          免费项目
        </el-radio-button>
        <el-radio-button label="paid">
          <el-icon><Money /></el-icon>
          付费项目
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 免费项目说明 -->
    <div v-if="priceType === 'free'" class="free-project-info">
      <el-alert
        title="免费项目"
        type="success"
        :closable="false"
        show-icon
      >
        <template #default>
          <p>免费项目将对所有用户开放下载，有助于提高项目曝光度和下载量。</p>
          <ul class="benefits-list">
            <li>✓ 更高的曝光度和搜索排名</li>
            <li>✓ 更多的用户下载和反馈</li>
            <li>✓ 有机会被推荐到首页</li>
            <li>✓ 建立个人品牌和影响力</li>
          </ul>
        </template>
      </el-alert>
    </div>

    <!-- 付费项目设置 -->
    <div v-else class="paid-project-settings">
      <div class="price-input-section">
        <el-form-item label="项目价格" required>
          <div class="price-input-wrapper">
            <el-input-number
              v-model="currentPrice"
              :min="minPrice"
              :max="maxPrice"
              :step="priceStep"
              :precision="2"
              placeholder="请输入价格"
              class="price-input"
              @change="handlePriceChange"
            />
            <span class="price-unit">积分</span>
          </div>
        </el-form-item>

        <!-- 价格建议 -->
        <div class="price-suggestions">
          <div class="suggestion-header">
            <span class="suggestion-title">推荐价格</span>
            <el-tooltip content="基于项目复杂度和市场行情的价格建议" placement="top">
              <el-icon class="suggestion-info"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          <div class="suggestion-tags">
            <el-tag
              v-for="suggestion in priceSuggestions"
              :key="suggestion.value"
              :type="currentPrice === suggestion.value ? 'primary' : 'info'"
              :effect="currentPrice === suggestion.value ? 'dark' : 'plain'"
              class="suggestion-tag"
              @click="selectSuggestion(suggestion.value)"
            >
              {{ suggestion.label }}
            </el-tag>
          </div>
        </div>

        <!-- 价格预览 -->
        <div class="price-preview">
          <div class="preview-card">
            <div class="preview-header">
              <span class="preview-title">价格预览</span>
            </div>
            <div class="preview-content">
              <div class="price-display">
                <span class="price-value">{{ formatPrice(currentPrice) }}</span>
                <span class="price-currency">积分</span>
              </div>
              <div class="price-equivalent">
                <span class="equivalent-text">约等于 ¥{{ calculateRMB(currentPrice) }}</span>
              </div>
              <div class="price-breakdown">
                <div class="breakdown-item">
                  <span class="breakdown-label">平台服务费 ({{ platformFeeRate }}%)</span>
                  <span class="breakdown-value">{{ formatPrice(platformFee) }} 积分</span>
                </div>
                <div class="breakdown-item">
                  <span class="breakdown-label">您的收益</span>
                  <span class="breakdown-value">{{ formatPrice(authorEarning) }} 积分</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 定价策略提示 -->
        <div class="pricing-tips">
          <el-alert
            title="定价建议"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <ul class="tips-list">
                <li>💡 新手项目建议定价 10-50 积分，有助于快速获得用户反馈</li>
                <li>🎯 成熟项目可根据功能复杂度定价 50-500 积分</li>
                <li>🏆 企业级项目可定价 500+ 积分</li>
                <li>📈 可根据用户反馈和下载量适时调整价格</li>
              </ul>
            </template>
          </el-alert>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Present, Money, QuestionFilled } from '@element-plus/icons-vue'

// Props
interface Props {
  modelValue: number
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: number]
  change: [value: number]
}>()

// 响应式数据
const currentPrice = ref(props.modelValue)
const priceType = ref(props.modelValue === 0 ? 'free' : 'paid')

// 价格配置
const minPrice = 1
const maxPrice = 999999
const priceStep = 1
const platformFeeRate = 10 // 平台服务费率 10%
const pointToRMBRate = 0.1 // 积分兑换人民币汇率 1积分=0.1元

// 价格建议
const priceSuggestions = [
  { label: '入门 (10积分)', value: 10 },
  { label: '进阶 (50积分)', value: 50 },
  { label: '专业 (100积分)', value: 100 },
  { label: '高级 (200积分)', value: 200 },
  { label: '企业 (500积分)', value: 500 }
]

// 计算属性
const platformFee = computed(() => {
  return Math.round(currentPrice.value * platformFeeRate / 100)
})

const authorEarning = computed(() => {
  return currentPrice.value - platformFee.value
})

// 方法
const handlePriceTypeChange = (type: string) => {
  if (type === 'free') {
    currentPrice.value = 0
    updateModelValue()
  } else {
    if (currentPrice.value === 0) {
      currentPrice.value = 10 // 默认付费价格
      updateModelValue()
    }
  }
}

const handlePriceChange = (value: number | null) => {
  if (value !== null) {
    currentPrice.value = value
    updateModelValue()
  }
}

const selectSuggestion = (price: number) => {
  if (props.disabled) return
  
  currentPrice.value = price
  priceType.value = 'paid'
  updateModelValue()
}

const formatPrice = (price: number): string => {
  return price.toLocaleString()
}

const calculateRMB = (points: number): string => {
  const rmb = points * pointToRMBRate
  return rmb.toFixed(1)
}

const updateModelValue = () => {
  emit('update:modelValue', currentPrice.value)
  emit('change', currentPrice.value)
}

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  currentPrice.value = newValue
  priceType.value = newValue === 0 ? 'free' : 'paid'
})
</script>

<style scoped>
.price-selector {
  width: 100%;
}

.price-type-selector {
  margin-bottom: 20px;
}

.price-type-selector .el-radio-button {
  margin-right: 12px;
}

.free-project-info {
  margin-bottom: 20px;
}

.benefits-list {
  margin: 12px 0 0 0;
  padding-left: 0;
  list-style: none;
}

.benefits-list li {
  margin: 8px 0;
  color: #67c23a;
  font-size: 14px;
}

.paid-project-settings {
  margin-top: 20px;
}

.price-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-input {
  width: 200px;
}

.price-unit {
  color: #606266;
  font-size: 14px;
}

.price-suggestions {
  margin: 20px 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 12px;
}

.suggestion-title {
  font-weight: 500;
  color: #303133;
}

.suggestion-info {
  font-size: 14px;
  color: #909399;
  cursor: help;
}

.suggestion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggestion-tag {
  cursor: pointer;
  transition: all 0.3s ease;
}

.suggestion-tag:hover {
  transform: translateY(-1px);
}

.price-preview {
  margin: 20px 0;
}

.preview-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.preview-header {
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.preview-title {
  font-weight: 500;
  color: #303133;
}

.preview-content {
  padding: 16px;
}

.price-display {
  text-align: center;
  margin-bottom: 16px;
}

.price-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.price-currency {
  font-size: 16px;
  color: #606266;
  margin-left: 8px;
}

.price-equivalent {
  text-align: center;
  margin-bottom: 16px;
}

.equivalent-text {
  color: #909399;
  font-size: 14px;
}

.price-breakdown {
  border-top: 1px solid #e4e7ed;
  padding-top: 16px;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.breakdown-label {
  color: #606266;
  font-size: 14px;
}

.breakdown-value {
  color: #303133;
  font-weight: 500;
}

.pricing-tips {
  margin-top: 20px;
}

.tips-list {
  margin: 12px 0 0 0;
  padding-left: 0;
  list-style: none;
}

.tips-list li {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}
</style>
