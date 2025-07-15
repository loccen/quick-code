<template>
  <div class="exception-test">
    <div class="test-header">
      <h1>异常处理机制测试</h1>
      <p>测试后端新的异常处理机制，验证前端能够正确接收和处理不同类型的错误</p>
    </div>

    <div class="test-grid">
      <div class="test-category">
        <h2>认证相关错误 (1xxx)</h2>
        <div class="test-buttons">
          <button @click="testException('password-mismatch')" class="test-btn auth">
            密码不一致 (1002)
          </button>
          <button @click="testException('invalid-email-code')" class="test-btn auth">
            邮箱验证码错误 (1003)
          </button>
        </div>
      </div>

      <div class="test-category">
        <h2>资源相关错误 (4xxx)</h2>
        <div class="test-buttons">
          <button @click="testException('user-not-found')" class="test-btn resource">
            用户不存在 (4001)
          </button>
          <button @click="testException('username-exists')" class="test-btn resource">
            用户名已存在 (4002)
          </button>
          <button @click="testException('email-exists')" class="test-btn resource">
            邮箱已存在 (4003)
          </button>
        </div>
      </div>

      <div class="test-category">
        <h2>状态相关错误 (5xxx)</h2>
        <div class="test-buttons">
          <button @click="testException('user-disabled')" class="test-btn state">
            用户已被禁用 (5001)
          </button>
          <button @click="testException('user-locked')" class="test-btn state">
            用户已被锁定 (5002)
          </button>
        </div>
      </div>

      <div class="test-category">
        <h2>业务逻辑错误 (6xxx)</h2>
        <div class="test-buttons">
          <button @click="testException('insufficient-points')" class="test-btn business">
            积分不足 (6001)
          </button>
        </div>
      </div>
    </div>

    <div class="test-results">
      <h2>测试结果</h2>
      <div class="result-item" v-for="result in testResults" :key="result.id">
        <div class="result-header">
          <span class="result-endpoint">{{ result.endpoint }}</span>
          <span class="result-time">{{ result.time }}</span>
        </div>
        <div class="result-content">
          <div class="result-status" :class="result.success ? 'success' : 'error'">
            {{ result.success ? '✓ 成功' : '✗ 失败' }}
          </div>
          <div class="result-details">
            <p><strong>错误码:</strong> {{ result.code }}</p>
            <p><strong>错误消息:</strong> {{ result.message }}</p>
            <p><strong>HTTP状态:</strong> {{ result.httpStatus }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '@/utils/http'

interface TestResult {
  id: string
  endpoint: string
  time: string
  success: boolean
  code: number
  message: string
  httpStatus: number
}

const testResults = ref<TestResult[]>([])

const testException = async (type: string) => {
  const endpoint = `/api/test/exception/${type}`
  const startTime = new Date()
  
  try {
    // 这个请求预期会失败，我们要捕获错误信息
    await http.get(endpoint, { showError: false })
  } catch (error: any) {
    const result: TestResult = {
      id: `${type}-${Date.now()}`,
      endpoint,
      time: startTime.toLocaleTimeString(),
      success: true, // 能够正确捕获和解析错误就算成功
      code: error.code || 0,
      message: error.message || '未知错误',
      httpStatus: error.status || 0
    }
    
    testResults.value.unshift(result)
    
    // 显示测试结果
    ElMessage.success(`测试成功: ${type} - 错误码: ${error.code}`)
    
    console.log('异常测试结果:', {
      endpoint,
      error: {
        code: error.code,
        message: error.message,
        status: error.status,
        data: error.data
      }
    })
  }
}

// 清空测试结果
const clearResults = () => {
  testResults.value = []
}
</script>

<style scoped>
.exception-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-header {
  text-align: center;
  margin-bottom: 40px;
}

.test-header h1 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.test-header p {
  color: #7f8c8d;
  font-size: 16px;
}

.test-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.test-category {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.test-category h2 {
  margin-bottom: 15px;
  color: #2c3e50;
  font-size: 18px;
}

.test-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.test-btn {
  padding: 12px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.test-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.test-btn.auth {
  background: #e74c3c;
  color: white;
}

.test-btn.auth:hover {
  background: #c0392b;
}

.test-btn.resource {
  background: #f39c12;
  color: white;
}

.test-btn.resource:hover {
  background: #e67e22;
}

.test-btn.state {
  background: #9b59b6;
  color: white;
}

.test-btn.state:hover {
  background: #8e44ad;
}

.test-btn.business {
  background: #3498db;
  color: white;
}

.test-btn.business:hover {
  background: #2980b9;
}

.test-results {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.test-results h2 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.result-item {
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  margin-bottom: 15px;
  overflow: hidden;
}

.result-header {
  background: #f8f9fa;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-endpoint {
  font-family: monospace;
  font-weight: 500;
  color: #2c3e50;
}

.result-time {
  color: #7f8c8d;
  font-size: 12px;
}

.result-content {
  padding: 15px;
}

.result-status {
  margin-bottom: 10px;
  font-weight: 500;
}

.result-status.success {
  color: #27ae60;
}

.result-status.error {
  color: #e74c3c;
}

.result-details p {
  margin: 5px 0;
  font-size: 14px;
}

.result-details strong {
  color: #2c3e50;
}
</style>
