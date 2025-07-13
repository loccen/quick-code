<template>
  <div class="forgot-password-view">
    <div class="forgot-password-container">
      <ModernCard class="forgot-password-card" :glass="true" :hoverable="false">
        <div class="forgot-password-header">
          <h1 class="forgot-password-title">重置密码</h1>
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElForm, ElMessage } from 'element-plus'
import { Message, Lock, Key, ArrowLeft } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import ModernCard from '@/components/ui/ModernCard.vue'
import ModernButton from '@/components/ui/ModernButton.vue'
import type { ResetPasswordRequest } from '@/types/user'

const router = useRouter()
const appStore = useAppStore()

const forgotPasswordFormRef = ref<InstanceType<typeof ElForm>>()

const forgotPasswordForm = reactive<ResetPasswordRequest>({
  email: '',
  newPassword: '',
  confirmPassword: '',
  emailCode: ''
})

const forgotPasswordRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
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
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.forgot-password-view {
  min-height: 100vh;
  @include flex-center();
  background: var(--gradient-hero);

  .forgot-password-container {
    width: 100%;
    max-width: 450px;
    padding: $spacing-lg;

    .forgot-password-card {
      padding: $spacing-2xl;
      @include shadow-layered-lg();
    }
  }

  .forgot-password-header {
    text-align: center;
    margin-bottom: $spacing-2xl;

    .forgot-password-title {
      @include gradient-text();
      font-size: $font-size-3xl;
      font-weight: $font-weight-bold;
      margin: 0 0 $spacing-sm 0;
    }

    .forgot-password-subtitle {
      color: var(--text-secondary);
      font-size: $font-size-sm;
      margin: 0;
      line-height: var(--line-height-relaxed);
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
      margin-bottom: $spacing-lg;

      .el-input .el-input__wrapper {
        border-radius: $radius-lg;
        transition: all var(--transition-base);

        &:hover {
          border-color: rgba(var(--primary-color), 0.5);
        }

        &.is-focus {
          border-color: var(--primary-color);
          @include shadow-colored(var(--primary-color), 0.1);
        }
      }
    }
  }

  .forgot-password-footer {
    text-align: center;
    margin-top: $spacing-xl;

    .back-link {
      @include flex-center();
      gap: $spacing-xs;
      color: var(--text-secondary);
      text-decoration: none;
      font-size: $font-size-sm;
      transition: color var(--transition-fast);

      &:hover {
        color: var(--primary-color);
      }
    }
  }
}

@include respond-below('md') {
  .forgot-password-view {
    .forgot-password-container {
      padding: $spacing-md;

      .forgot-password-card {
        padding: $spacing-xl;
      }
    }

    .forgot-password-form {
      .code-input-group {
        flex-direction: column;
      }
    }
  }
}
</style>
