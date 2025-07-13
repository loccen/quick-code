<template>
  <div class="public-layout">
    <!-- 公开页面头部 -->
    <header class="public-header">
      <div class="header-container">
        <!-- Logo和网站名称 -->
        <div class="logo-section">
          <router-link to="/" class="logo-link">
            <img src="/src/assets/logo.svg" alt="速码网" class="logo-image" />
            <span class="site-name">速码网</span>
          </router-link>
        </div>

        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/market" class="nav-item">项目市场</router-link>
          <router-link to="/about" class="nav-item">关于我们</router-link>
        </nav>

        <!-- 用户操作区域 -->
        <div class="user-actions">
          <template v-if="!userStore.isAuthenticated">
            <router-link to="/login" class="btn btn-outline">登录</router-link>
            <router-link to="/register" class="btn btn-primary">注册</router-link>
          </template>
          <template v-else>
            <el-dropdown @command="handleUserCommand">
              <div class="user-info">
                <el-avatar :src="userStore.user?.avatar" :size="32">
                  {{ userStore.user?.username?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ userStore.user?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="dashboard">
                    <el-icon><Dashboard /></el-icon>
                    仪表盘
                  </el-dropdown-item>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="my-projects">
                    <el-icon><FolderOpened /></el-icon>
                    我的项目
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="public-main">
      <router-view />
    </main>

    <!-- 公开页面底部 -->
    <footer class="public-footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>速码网</h4>
            <p>专业的源码交易平台</p>
            <p>为开发者提供优质的源码资源</p>
          </div>
          <div class="footer-section">
            <h4>快速链接</h4>
            <ul>
              <li><router-link to="/">首页</router-link></li>
              <li><router-link to="/market">项目市场</router-link></li>
              <li><router-link to="/about">关于我们</router-link></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>帮助支持</h4>
            <ul>
              <li><a href="#">使用帮助</a></li>
              <li><a href="#">常见问题</a></li>
              <li><a href="#">联系我们</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>关注我们</h4>
            <div class="social-links">
              <a href="#" class="social-link">微信</a>
              <a href="#" class="social-link">微博</a>
              <a href="#" class="social-link">QQ群</a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2024 速码网. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import {
    ArrowDown,
    Dashboard,
    FolderOpened,
    SwitchButton,
    User
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()

/**
 * 处理用户下拉菜单命令
 */
const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'dashboard':
      router.push('/user/dashboard')
      break
    case 'profile':
      router.push('/user/profile')
      break
    case 'my-projects':
      router.push('/user/my-projects')
      break
    case 'logout':
      try {
        await userStore.logout()
        ElMessage.success('退出登录成功')
        router.push('/')
      } catch (error) {
        ElMessage.error('退出登录失败')
      }
      break
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.public-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

.public-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .header-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $spacing-lg;
    height: 64px;
    @include flex-between();
  }

  .logo-section {
    .logo-link {
      @include flex-center();
      gap: $spacing-sm;
      text-decoration: none;
      color: var(--text-primary);

      .logo-image {
        width: 32px;
        height: 32px;
      }

      .site-name {
        font-size: $font-size-xl;
        font-weight: $font-weight-bold;
        @include gradient-text();
      }
    }
  }

  .nav-menu {
    @include flex-center();
    gap: $spacing-xl;

    .nav-item {
      padding: $spacing-sm $spacing-md;
      text-decoration: none;
      color: var(--text-secondary);
      font-weight: $font-weight-medium;
      border-radius: $border-radius-md;
      transition: all 0.3s ease;

      &:hover,
      &.router-link-active {
        color: var(--primary-color);
        background: rgba(var(--primary-color-rgb), 0.1);
      }
    }
  }

  .user-actions {
    @include flex-center();
    gap: $spacing-md;

    .btn {
      padding: $spacing-sm $spacing-lg;
      border-radius: $border-radius-md;
      text-decoration: none;
      font-weight: $font-weight-medium;
      transition: all 0.3s ease;

      &.btn-outline {
        color: var(--primary-color);
        border: 1px solid var(--primary-color);
        background: transparent;

        &:hover {
          background: var(--primary-color);
          color: white;
        }
      }

      &.btn-primary {
        background: var(--primary-color);
        color: white;
        border: 1px solid var(--primary-color);

        &:hover {
          background: var(--primary-color-hover);
        }
      }
    }

    .user-info {
      @include flex-center();
      gap: $spacing-sm;
      padding: $spacing-sm;
      cursor: pointer;
      border-radius: $border-radius-md;
      transition: background-color 0.3s ease;

      &:hover {
        background: var(--bg-secondary);
      }

      .username {
        font-weight: $font-weight-medium;
        color: var(--text-primary);
      }
    }
  }
}

.public-main {
  flex: 1;
  min-height: calc(100vh - 64px - 200px);
}

.public-footer {
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
  margin-top: auto;

  .footer-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: $spacing-3xl $spacing-lg $spacing-lg;
  }

  .footer-content {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: $spacing-xl;
    margin-bottom: $spacing-xl;

    .footer-section {
      h4 {
        color: var(--text-primary);
        font-weight: $font-weight-bold;
        margin-bottom: $spacing-md;
      }

      p {
        color: var(--text-secondary);
        margin-bottom: $spacing-sm;
      }

      ul {
        list-style: none;
        padding: 0;

        li {
          margin-bottom: $spacing-sm;

          a {
            color: var(--text-secondary);
            text-decoration: none;
            transition: color 0.3s ease;

            &:hover {
              color: var(--primary-color);
            }
          }
        }
      }

      .social-links {
        @include flex-start();
        gap: $spacing-md;

        .social-link {
          color: var(--text-secondary);
          text-decoration: none;
          transition: color 0.3s ease;

          &:hover {
            color: var(--primary-color);
          }
        }
      }
    }
  }

  .footer-bottom {
    padding-top: $spacing-lg;
    border-top: 1px solid var(--border-color);
    text-align: center;

    p {
      color: var(--text-secondary);
      margin: 0;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .public-header {
    .header-container {
      padding: 0 $spacing-md;
    }

    .nav-menu {
      display: none;
    }

    .user-actions {
      gap: $spacing-sm;

      .btn {
        padding: $spacing-xs $spacing-md;
        font-size: $font-size-sm;
      }
    }
  }

  .public-footer {
    .footer-content {
      grid-template-columns: 1fr;
      gap: $spacing-lg;
    }
  }
}
</style>
