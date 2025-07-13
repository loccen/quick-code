<template>
  <div class="register-view">
    <!-- 背景装饰 -->
    <div class="register-background">
      <div class="bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
    </div>

    <!-- 注册表单容器 -->
    <div class="register-container">
      <ModernCard class="register-card" :glass="true" :hoverable="false">
        <!-- 头部 -->
        <div class="register-header">
          <div class="logo">
            <img src="/favicon.ico" alt="Logo" class="logo-image">
            <h1 class="logo-text">速码网</h1>
          </div>
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
import ModernButton from '@/components/ui/ModernButton.vue'
import ModernCard from '@/components/ui/ModernCard.vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import type { RegisterRequest } from '@/types/user'
import { Key, Lock, Message, User } from '@element-plus/icons-vue'
import { ElForm, ElMessage } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

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
  position: relative;
  overflow: hidden;
  background: var(--gradient-hero);
}

.register-background {
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
        width: 280px;
        height: 280px;
        top: 5%;
        right: 10%;
        animation-delay: 0s;
      }

      &.shape-2 {
        width: 180px;
        height: 180px;
        top: 50%;
        left: 5%;
        animation-delay: 1.5s;
      }

      &.shape-3 {
        width: 220px;
        height: 220px;
        bottom: 15%;
        right: 20%;
        animation-delay: 3s;
      }

      &.shape-4 {
        width: 120px;
        height: 120px;
        top: 25%;
        left: 25%;
        animation-delay: 4.5s;
      }
    }
  }
}

.register-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 480px;
  padding: $spacing-lg;

  .register-card {
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

.register-header {
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

  .register-subtitle {
    color: rgba(255, 255, 255, 0.9);
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    margin: 0;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
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
    color: rgba(255, 255, 255, 0.9);
    text-decoration: none;
    font-weight: $font-weight-medium;
    transition: color var(--transition-fast);

    &:hover {
      color: #ffffff;
      text-shadow: 0 0 8px rgba(255, 255, 255, 0.5);
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

    .el-checkbox {
      .el-checkbox__label {
        color: rgba(255, 255, 255, 0.9);
        font-size: $font-size-sm;
        font-weight: $font-weight-medium;
      }
    }

    // 表单验证错误提示样式优化
    .el-form-item__error {
      color: #ffffff !important;
      background: rgba(255, 77, 79, 0.9) !important;
      padding: $spacing-xs $spacing-sm !important;
      border-radius: $radius-md !important;
      font-size: $font-size-xs !important;
      font-weight: $font-weight-semibold !important;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3) !important;
      border: 1px solid rgba(255, 77, 79, 1) !important;
      margin-top: $spacing-xs !important;
      box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3) !important;
    }
  }
}

.register-footer {
  text-align: center;
  margin-top: $spacing-lg;
  color: rgba(255, 255, 255, 0.8);
  font-size: $font-size-sm;

  .login-link {
    color: rgba(255, 255, 255, 0.95);
    text-decoration: none;
    font-weight: $font-weight-semibold;
    margin-left: $spacing-xs;
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
  .register-container {
    padding: $spacing-md;

    .register-card {
      padding: $spacing-lg $spacing-xl;
    }
  }

  .register-header {
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
  }

  .register-form {
    .code-input-group {
      flex-direction: column;
      gap: $spacing-xs;
    }

    :deep(.el-form-item) {
      margin-bottom: $spacing-sm;
    }
  }

  .register-footer {
    margin-top: $spacing-md;
  }
}
</style>
