<template>
  <div class="register-view">
    <div class="register-container">
      <ModernCard class="register-card" :glass="true" :hoverable="false">
        <div class="register-header">
          <h1 class="register-title">创建账户</h1>
          <p class="register-subtitle">加入速码网，开始您的编程之旅</p>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              size="large"
              clearable
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              size="large"
              clearable
              :prefix-icon="Message"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              size="large"
              show-password
              clearable
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item prop="emailCode">
            <div class="code-input-group">
              <el-input
                v-model="registerForm.emailCode"
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

          <el-form-item prop="agreeTerms">
            <el-checkbox v-model="registerForm.agreeTerms">
              我已阅读并同意
              <a href="#" class="terms-link">《用户协议》</a>
              和
              <a href="#" class="terms-link">《隐私政策》</a>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <ModernButton
              type="primary"
              size="large"
              block
              :loading="loading"
              @click="handleRegister"
            >
              {{ loading ? '注册中...' : '注册' }}
            </ModernButton>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <span>已有账户？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
        </div>
      </ModernCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElForm, ElMessage } from 'element-plus'
import { User, Message, Lock, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import ModernCard from '@/components/ui/ModernCard.vue'
import ModernButton from '@/components/ui/ModernButton.vue'
import type { RegisterRequest } from '@/types/user'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const registerFormRef = ref<InstanceType<typeof ElForm>>()

const registerForm = reactive<RegisterRequest>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  emailCode: '',
  agreeTerms: false
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' }
  ],
  agreeTerms: [
    {
      validator: (rule: any, value: boolean, callback: Function) => {
        if (!value) {
          callback(new Error('请同意用户协议和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const loading = ref(false)
const codeCountdown = ref(0)

const sendEmailCode = async () => {
  if (!registerForm.email) {
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

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    const success = await userStore.register(registerForm)
    
    if (success) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    }
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

// 设置页面标题
appStore.setPageTitle('用户注册')
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.register-view {
  min-height: 100vh;
  @include flex-center();
  background: var(--gradient-hero);

  .register-container {
    width: 100%;
    max-width: 450px;
    padding: $spacing-lg;

    .register-card {
      padding: $spacing-2xl;
      @include shadow-layered-lg();
    }
  }

  .register-header {
    text-align: center;
    margin-bottom: $spacing-2xl;

    .register-title {
      @include gradient-text();
      font-size: $font-size-3xl;
      font-weight: $font-weight-bold;
      margin: 0 0 $spacing-sm 0;
    }

    .register-subtitle {
      color: var(--text-secondary);
      font-size: $font-size-sm;
      margin: 0;
    }
  }

  .register-form {
    .code-input-group {
      display: flex;
      gap: $spacing-sm;

      .el-input {
        flex: 1;
      }
    }

    .terms-link {
      color: var(--primary-color);
      text-decoration: none;

      &:hover {
        color: var(--primary-hover);
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

  .register-footer {
    text-align: center;
    margin-top: $spacing-xl;
    color: var(--text-secondary);
    font-size: $font-size-sm;

    .login-link {
      color: var(--primary-color);
      text-decoration: none;
      font-weight: $font-weight-medium;
      margin-left: $spacing-xs;

      &:hover {
        color: var(--primary-hover);
      }
    }
  }
}

@include respond-below('md') {
  .register-view {
    .register-container {
      padding: $spacing-md;

      .register-card {
        padding: $spacing-xl;
      }
    }

    .register-form {
      .code-input-group {
        flex-direction: column;
      }
    }
  }
}
</style>
