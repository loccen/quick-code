<template>
  <div class="login-view">
    <!-- 背景装饰 -->
    <div class="login-background">
      <div class="bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>

    <!-- 登录表单容器 -->
    <div class="login-container">
      <ModernCard class="login-card" :glass="true" :hoverable="false">
        <!-- 头部 -->
        <div class="login-header">
          <div class="logo">
            <img src="/favicon.ico" alt="Logo" class="logo-image">
            <h1 class="logo-text">速码网</h1>
          </div>
          <p class="login-subtitle">欢迎回来，请登录您的账户</p>
        </div>

        <!-- 登录表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              data-testid="username-input"
              placeholder="请输入邮箱或用户名"
              size="large"
              clearable
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              data-testid="password-input"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <div class="form-options">
              <el-checkbox
                v-model="loginForm.rememberMe"
                data-testid="remember-me-checkbox"
              >
                记住我
              </el-checkbox>
              <router-link
                to="/forgot-password"
                class="forgot-link"
                data-testid="forgot-password-link"
              >
                忘记密码？
              </router-link>
            </div>
          </el-form-item>

          <el-form-item>
            <ModernButton
              type="primary"
              size="large"
              block
              :loading="loading"
              data-testid="login-button"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </ModernButton>
          </el-form-item>
        </el-form>

        <!-- 底部链接 -->
        <div class="login-footer">
          <span>还没有账户？</span>
          <router-link
            to="/register"
            class="register-link"
            data-testid="register-link"
          >
            立即注册
          </router-link>
        </div>

        <!-- 错误提示 -->
        <div
          v-if="errorMessage"
          class="error-message"
          data-testid="error-message"
        >
          <el-icon><WarningFilled /></el-icon>
          {{ errorMessage }}
        </div>

        <!-- 加载状态 -->
        <div
          v-if="loading"
          class="loading-overlay"
          data-testid="loading-spinner"
        >
          <el-icon class="loading-icon"><Loading /></el-icon>
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
import type { LoginRequest } from '@/types/user'
import { Loading, Lock, User, WarningFilled } from '@element-plus/icons-vue'
import { ElForm, ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

// 表单引用
const loginFormRef = ref<InstanceType<typeof ElForm>>()

// 表单数据
const loginForm = reactive<LoginRequest>({
  username: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入邮箱或用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ]
}

// 状态
const loading = ref(false)
const errorMessage = ref('')

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 验证表单
    await loginFormRef.value.validate()

    loading.value = true
    errorMessage.value = ''

    // 执行登录
    const success = await userStore.login(loginForm)

    if (success) {
      ElMessage.success('登录成功')

      // 获取重定向路径
      const redirect = route.query.redirect as string || '/dashboard'
      router.push(redirect)
    } else {
      errorMessage.value = '用户名或密码错误'
    }
  } catch (error: any) {
    console.error('登录失败:', error)
    errorMessage.value = error.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}

// 页面初始化
onMounted(() => {
  // 设置页面标题
  appStore.setPageTitle('用户登录')

  // 如果已登录，重定向到仪表盘
  if (userStore.isAuthenticated) {
    router.push('/dashboard')
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.login-view {
  min-height: 100vh;
  @include flex-center();
  position: relative;
  overflow: hidden;
  background: var(--gradient-hero);
}

.login-background {
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
        width: 300px;
        height: 300px;
        top: 10%;
        left: 10%;
        animation-delay: 0s;
      }

      &.shape-2 {
        width: 200px;
        height: 200px;
        top: 60%;
        right: 15%;
        animation-delay: 2s;
      }

      &.shape-3 {
        width: 150px;
        height: 150px;
        bottom: 20%;
        left: 20%;
        animation-delay: 4s;
      }
    }
  }
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: $spacing-lg;

  .login-card {
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

.login-header {
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

  .login-subtitle {
    color: rgba(255, 255, 255, 0.9);
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    margin: 0;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }
}

.login-form {
  .form-options {
    @include flex-between();
    width: 100%;

    .forgot-link {
      color: rgba(255, 255, 255, 0.9);
      font-size: $font-size-sm;
      text-decoration: none;
      font-weight: $font-weight-medium;
      transition: color var(--transition-fast);

      &:hover {
        color: #ffffff;
        text-shadow: 0 0 8px rgba(255, 255, 255, 0.5);
      }
    }
  }

  :deep(.el-form-item) {
    margin-bottom: $spacing-lg;

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
  }
}

.login-footer {
  text-align: center;
  margin-top: $spacing-lg;
  color: rgba(255, 255, 255, 0.8);
  font-size: $font-size-sm;

  .register-link {
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

.error-message {
  @include flex-center();
  gap: $spacing-xs;
  margin-top: $spacing-md;
  padding: $spacing-sm $spacing-md;
  background: rgba(255, 77, 79, 0.15);
  border: 1px solid rgba(255, 77, 79, 0.4);
  border-radius: $radius-lg;
  color: #ffffff;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  @include animate-shake();
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  @include flex-center();
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  border-radius: inherit;
  z-index: 10;

  .loading-icon {
    font-size: 24px;
    color: var(--primary-color);
    animation: spin 1s linear infinite;
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
  .login-container {
    padding: $spacing-md;

    .login-card {
      padding: $spacing-lg $spacing-xl;
    }
  }

  .login-header {
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

  .login-form {
    :deep(.el-form-item) {
      margin-bottom: $spacing-md;
    }
  }

  .login-footer {
    margin-top: $spacing-md;
  }
}
</style>
