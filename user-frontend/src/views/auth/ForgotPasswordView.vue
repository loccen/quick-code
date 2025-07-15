<template>
  <div class="forgot-password-view">
    <!-- 背景装饰 -->
    <div class="forgot-password-background">
      <div class="bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>

    <!-- 密码重置表单容器 -->
    <div class="forgot-password-container">
      <ModernCard class="forgot-password-card" :glass="true" :hoverable="false">
        <!-- 头部 -->
        <div class="forgot-password-header">
          <div class="logo">
            <img src="/favicon.ico" alt="Logo" class="logo-image">
            <h1 class="logo-text">速码网</h1>
          </div>
          <h2 class="forgot-password-title">重置密码</h2>
          <p class="forgot-password-subtitle">请输入您的邮箱地址，我们将发送重置密码的验证码</p>
        </div>

        <el-form
          ref="forgotPasswordFormRef"
          :model="forgotPasswordForm"
          :rules="forgotPasswordRules"
          class="forgot-password-form"
        >
          <el-form-item prop="email">
            <el-input
              v-model="forgotPasswordForm.email"
              placeholder="请输入邮箱"
              size="large"
              clearable
              :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item prop="emailCode">
            <div class="code-input-group">
              <el-input
                v-model="forgotPasswordForm.emailCode"
                placeholder="请输入邮箱验证码"
                size="large"
                clearable
                :prefix-icon="Key"
              />
              <ModernButton
                :disabled="codeCountdown > 0"
                @click="sendEmailCode"
              >
                {{ codeCountdown > 0 ? `${codeCountdown}s` : '发送验证码' }}
              </ModernButton>
            </div>
          </el-form-item>

          <el-form-item prop="newPassword">
            <el-input
              v-model="forgotPasswordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              size="large"
              show-password
              clearable
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="forgotPasswordForm.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              size="large"
              show-password
              clearable
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item>
            <ModernButton
              type="primary"
              size="large"
              block
              :loading="loading"
              @click="handleResetPassword"
            >
              {{ loading ? '重置中...' : '重置密码' }}
            </ModernButton>
          </el-form-item>
        </el-form>

        <div class="forgot-password-footer">
          <router-link to="/login" class="back-link">
            <el-icon><ArrowLeft /></el-icon>
            返回登录
          </router-link>
        </div>
      </ModernCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import ModernButton from '@/components/ui/ModernButton.vue'
import ModernCard from '@/components/ui/ModernCard.vue'
import { useAppStore } from '@/stores/app'
import type { ResetPasswordRequest } from '@/types/user'
import { ArrowLeft, Key, Lock, Message } from '@element-plus/icons-vue'
import { ElForm, ElMessage, type FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const appStore = useAppStore()

const forgotPasswordFormRef = ref<InstanceType<typeof ElForm>>()

const forgotPasswordForm = reactive<ResetPasswordRequest>({
  email: '',
  newPassword: '',
  confirmPassword: '',
  emailCode: ''
})

const forgotPasswordRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email' as const, message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== forgotPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loading = ref(false)
const codeCountdown = ref(0)

const sendEmailCode = async () => {
  if (!forgotPasswordForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  try {
    // 这里应该调用发送验证码的API
    ElMessage.success('验证码已发送')

    // 开始倒计时
    codeCountdown.value = 60
    const timer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

const handleResetPassword = async () => {
  if (!forgotPasswordFormRef.value) return

  try {
    await forgotPasswordFormRef.value.validate()
    loading.value = true

    // 这里应该调用重置密码的API
    // const success = await userApi.resetPassword(forgotPasswordForm)

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('密码重置成功，请使用新密码登录')
    router.push('/login')
  } catch (error) {
    console.error('重置密码失败:', error)
  } finally {
    loading.value = false
  }
}

// 设置页面标题
appStore.setPageTitle('重置密码')
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.forgot-password-view {
  min-height: 100vh;
  @include flex-center();
  position: relative;
  overflow: hidden;
  background: var(--gradient-hero);
}

.forgot-password-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;

  .bg-shapes {
    position: relative;
    width: 100%;
    height: 100%;

    .shape {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(20px);
      animation: float 6s ease-in-out infinite;

      &.shape-1 {
        width: 240px;
        height: 240px;
        top: 10%;
        right: 15%;
        animation-delay: 0s;
      }

      &.shape-2 {
        width: 160px;
        height: 160px;
        top: 60%;
        left: 10%;
        animation-delay: 2s;
      }

      &.shape-3 {
        width: 200px;
        height: 200px;
        bottom: 20%;
        right: 25%;
        animation-delay: 4s;
      }
    }
  }
}

.forgot-password-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
  padding: $spacing-lg;

  .forgot-password-card {
    padding: $spacing-xl $spacing-2xl;
    @include shadow-layered-lg();
    border: 1px solid rgba(255, 255, 255, 0.2);
    position: relative;
    overflow: visible;
    backdrop-filter: blur(25px);

    &::before {
      content: '';
      position: absolute;
      top: -1px;
      left: -1px;
      right: -1px;
      bottom: -1px;
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0.1));
      border-radius: inherit;
      z-index: -1;
    }

    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
      z-index: 1;
    }
  }
}

.forgot-password-header {
  text-align: center;
  margin-bottom: $spacing-xl;

  .logo {
    @include flex-center();
    flex-direction: column;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;

    .logo-image {
      width: 56px;
      height: 56px;
      border-radius: $radius-xl;
      @include shadow-layered-md();
    }

    .logo-text {
      background: linear-gradient(135deg, #ffffff 0%, #f0f9ff 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      font-size: $font-size-2xl;
      font-weight: $font-weight-bold;
      margin: 0;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }

  .forgot-password-title {
    color: rgba(255, 255, 255, 0.95);
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    margin: 0 0 $spacing-sm 0;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }

  .forgot-password-subtitle {
    color: rgba(255, 255, 255, 0.85);
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    margin: 0;
    line-height: var(--line-height-relaxed);
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }
}

.forgot-password-form {
  .code-input-group {
    display: flex;
    gap: $spacing-sm;

    .el-input {
      flex: 1;
    }
  }

  :deep(.el-form-item) {
    margin-bottom: $spacing-md;

    .el-input {
      .el-input__wrapper {
        @include glass-effect();
        border: 1px solid rgba(255, 255, 255, 0.3);
        border-radius: $radius-lg;
        transition: all var(--transition-base);
        padding: $spacing-sm $spacing-md;
        min-height: 48px;

        &:hover {
          border-color: rgba(255, 255, 255, 0.5);
          transform: translateY(-1px);
          @include shadow-colored(rgba(255, 255, 255, 0.2), 0.1);
        }

        &.is-focus {
          border-color: rgba(255, 255, 255, 0.7);
          @include shadow-colored(rgba(255, 255, 255, 0.3), 0.25);
          transform: translateY(-2px);
        }

        .el-input__inner {
          color: #ffffff;
          font-weight: $font-weight-medium;

          &::placeholder {
            color: rgba(255, 255, 255, 0.6);
            font-weight: $font-weight-normal;
          }
        }
      }
    }

    // 表单验证错误提示样式优化
    .el-form-item__error {
      position: absolute !important;
      top: 50% !important;
      left: calc(100% + #{$spacing-sm}) !important;
      transform: translateY(-50%) !important;
      color: #ffffff !important;
      background: rgba(255, 77, 79, 0.95) !important;
      padding: $spacing-xs $spacing-sm !important;
      border-radius: $radius-md !important;
      font-size: $font-size-xs !important;
      font-weight: $font-weight-semibold !important;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3) !important;
      border: 1px solid rgba(255, 77, 79, 1) !important;
      margin: 0 !important;
      box-shadow: 0 2px 8px rgba(255, 77, 79, 0.4) !important;
      white-space: nowrap !important;
      z-index: 10 !important;
      max-width: 180px !important;
      overflow: hidden !important;
      text-overflow: ellipsis !important;

      // 添加小箭头指向输入框
      &::before {
        content: '' !important;
        position: absolute !important;
        left: -6px !important;
        top: 50% !important;
        transform: translateY(-50%) !important;
        width: 0 !important;
        height: 0 !important;
        border-top: 6px solid transparent !important;
        border-bottom: 6px solid transparent !important;
        border-right: 6px solid rgba(255, 77, 79, 1) !important;
      }
    }

    // 确保表单项有相对定位，为绝对定位的错误提示提供定位上下文
    &.is-error {
      position: relative !important;
    }

    // 移动端适配：错误提示显示在下方，但增加足够间距
    @media (max-width: 768px) {
      .el-form-item__error {
        position: static !important;
        transform: none !important;
        left: auto !important;
        top: auto !important;
        margin-top: $spacing-sm !important;
        margin-bottom: $spacing-md !important;
        max-width: 100% !important;
        white-space: normal !important;

        &::before {
          display: none !important;
        }
      }
    }
  }
}

.forgot-password-footer {
  text-align: center;
  margin-top: $spacing-lg;

  .back-link {
    @include flex-center();
    gap: $spacing-xs;
    color: rgba(255, 255, 255, 0.9);
    text-decoration: none;
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    transition: all var(--transition-fast);

    &:hover {
      color: #ffffff;
      text-shadow: 0 0 8px rgba(255, 255, 255, 0.5);
    }
  }
}

// 浮动动画
@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

// 响应式设计
@include respond-below('md') {
  .forgot-password-container {
    padding: $spacing-md;

    .forgot-password-card {
      padding: $spacing-lg $spacing-xl;
    }
  }

  .forgot-password-header {
    margin-bottom: $spacing-lg;

    .logo {
      .logo-image {
        width: 48px;
        height: 48px;
      }

      .logo-text {
        font-size: $font-size-xl;
      }
    }

    .forgot-password-title {
      font-size: $font-size-lg;
    }
  }

  .forgot-password-form {
    .code-input-group {
      flex-direction: column;
      gap: $spacing-xs;
    }

    :deep(.el-form-item) {
      margin-bottom: $spacing-sm;
    }
  }

  .forgot-password-footer {
    margin-top: $spacing-md;
  }
}
</style>
