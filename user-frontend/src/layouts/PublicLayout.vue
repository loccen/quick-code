<template>
  <div class="public-layout">
    <!-- 页面背景 -->
    <PageBackground />

    <!-- 顶部导航栏 -->
    <header class="header">
      <nav class="navbar">
        <div class="container">
          <div class="nav-brand">
            <div class="logo">
              <i class="fas fa-code"></i>
              <span class="brand-name">速码网</span>
            </div>
          </div>

          <div class="nav-menu">
            <ul class="nav-links">
              <li><router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">首页</router-link></li>
              <li><router-link to="/market" class="nav-link" :class="{ active: $route.path === '/market' }">项目市场</router-link></li>
            </ul>
          </div>

          <div class="nav-search">
            <div class="search-box">
              <input type="text" placeholder="搜索项目、技术栈..." class="search-input" v-model="searchQuery">
              <button class="search-btn" @click="handleSearch">
                <i class="fas fa-search"></i>
              </button>
            </div>
          </div>

          <div class="nav-user">
            <div class="user-actions" v-if="!userStore.isAuthenticated">
              <router-link :to="generateLoginUrlWithRedirect()" class="btn btn-outline">登录</router-link>
              <router-link :to="generateRegisterUrlWithRedirect()" class="btn btn-primary">注册</router-link>
            </div>

            <div class="user-actions" v-else>
              <el-dropdown class="user-dropdown" @command="handleUserCommand">
                <div class="user-info">
                  <el-avatar
                    :src="userStore.user?.avatarUrl"
                    :size="32"
                    class="user-avatar"
                  >
                    <el-icon><User /></el-icon>
                  </el-avatar>
                  <span class="user-name">
                    {{ userStore.user?.nickname || userStore.user?.username || '用户' }}
                  </span>
                  <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">
                      <el-icon><User /></el-icon>
                      个人中心
                    </el-dropdown-item>
                    <el-dropdown-item command="my-projects">
                      <el-icon><FolderOpened /></el-icon>
                      我的项目
                    </el-dropdown-item>
                    <el-dropdown-item command="upload-project">
                      <el-icon><Upload /></el-icon>
                      上传项目
                    </el-dropdown-item>
                    <el-dropdown-item command="my-orders">
                      <el-icon><ShoppingCart /></el-icon>
                      我的订单
                    </el-dropdown-item>
                    <el-dropdown-item command="purchases">
                      <el-icon><ShoppingBag /></el-icon>
                      购买记录
                    </el-dropdown-item>
                    <el-dropdown-item command="downloads">
                      <el-icon><Download /></el-icon>
                      下载记录
                    </el-dropdown-item>
                    <el-dropdown-item command="points">
                      <el-icon><Coin /></el-icon>
                      积分管理
                    </el-dropdown-item>
                    <el-dropdown-item command="settings">
                      <el-icon><Setting /></el-icon>
                      系统设置
                    </el-dropdown-item>
                    <el-dropdown-item divided command="logout">
                      <el-icon><SwitchButton /></el-icon>
                      退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="mobile-menu-toggle" @click="toggleMobileMenu">
            <i class="fas fa-bars"></i>
          </div>
        </div>
      </nav>
    </header>

    <!-- 主要内容区域 -->
    <main class="public-content">
      <router-view />
    </main>

    <!-- 底部 -->
    <Footer />

    <!-- 全局上传进度悬浮组件 -->
    <UploadProgressFloat />
  </div>
</template>

<script setup lang="ts">
import Footer from '@/components/Footer.vue'
import PageBackground from '@/components/common/PageBackground.vue'
import UploadProgressFloat from '@/components/upload/UploadProgressFloat.vue'
import { useUserStore } from '@/stores/user'
import { generateLoginUrl, generateRegisterUrl } from '@/utils/redirect'
import { ArrowDown, Coin, Download, FolderOpened, Setting, ShoppingBag, ShoppingCart, SwitchButton, Upload, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const searchQuery = ref('')
const mobileMenuOpen = ref(false)

/**
 * 处理搜索
 */
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      path: '/market',
      query: { search: searchQuery.value.trim() }
    })
  }
}

/**
 * 切换移动端菜单
 */
const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

/**
 * 处理用户菜单命令
 */
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'my-projects':
      router.push('/user/my-projects')
      break
    case 'upload-project':
      router.push('/user/project/upload')
      break
    case 'my-orders':
      router.push('/user/my-orders')
      break
    case 'purchases':
      router.push('/user/purchases')
      break
    case 'downloads':
      router.push('/user/downloads')
      break
    case 'points':
      router.push('/user/points')
      break
    case 'settings':
      router.push('/user/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

/**
 * 生成带redirect参数的登录URL
 */
const generateLoginUrlWithRedirect = () => {
  return generateLoginUrl(route.fullPath)
}

/**
 * 生成带redirect参数的注册URL
 */
const generateRegisterUrlWithRedirect = () => {
  return generateRegisterUrl(route.fullPath)
}

/**
 * 处理用户退出登录
 */
const handleLogout = async () => {
  try {
    await userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/')
  } catch (error) {
    console.error('退出登录失败:', error)
    ElMessage.error('退出登录失败')
  }
}
</script>



<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.public-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  // 背景现在由 PageBackground 组件提供
}

// 顶部导航栏
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: $glass-bg;
  backdrop-filter: $glass-blur;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: $shadow-layered-sm;
  transition: all 0.3s ease;

  .navbar {
    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 $spacing-lg;
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 70px;
    }

    .nav-brand {
      .logo {
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        text-decoration: none;
        color: $text-primary;

        i {
          font-size: $font-size-xl;
          color: $primary-color;
        }

        .brand-name {
          font-size: $font-size-xl;
          font-weight: 700;
          color: $text-primary;
        }
      }
    }

    .nav-menu {
      .nav-links {
        display: flex;
        list-style: none;
        gap: $spacing-xl;
        margin: 0;
        padding: 0;

        .nav-link {
          color: $text-secondary;
          text-decoration: none;
          font-weight: 500;
          padding: $spacing-sm $spacing-md;
          border-radius: $radius-md;
          transition: all 0.3s ease;

          &:hover,
          &.active {
            color: $primary-color;
            background: rgba(24, 144, 255, 0.1);
          }
        }
      }
    }

    .nav-search {
      .search-box {
        position: relative;
        display: flex;
        align-items: center;

        .search-input {
          width: 300px;
          padding: $spacing-sm $spacing-md;
          padding-right: 40px;
          border: 1px solid rgba(255, 255, 255, 0.3);
          border-radius: $radius-lg;
          background: rgba(255, 255, 255, 0.1);
          backdrop-filter: $glass-blur-sm;
          color: $text-primary;
          font-size: $font-size-sm;
          transition: all 0.3s ease;

          &::placeholder {
            color: rgba(0, 0, 0, 0.5);
          }

          &:focus {
            outline: none;
            border-color: $primary-color;
            background: rgba(255, 255, 255, 0.2);
            box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
          }
        }

        .search-btn {
          position: absolute;
          right: $spacing-sm;
          background: none;
          border: none;
          color: $text-secondary;
          cursor: pointer;
          padding: $spacing-xs;
          border-radius: $radius-sm;
          transition: color 0.3s ease;

          &:hover {
            color: $primary-color;
          }
        }
      }
    }

    .nav-user {
      .user-actions {
        display: flex;
        align-items: center;
        gap: $spacing-md;

        .btn {
          padding: $spacing-sm $spacing-lg;
          border-radius: $radius-lg;
          text-decoration: none;
          font-weight: 600;
          font-size: $font-size-sm;
          transition: all 0.3s ease;
          border: 2px solid transparent;
          cursor: pointer;

          &.btn-outline {
            background: transparent;
            color: $primary-color;
            border: 2px solid $primary-color;

            &:hover {
              background: $primary-color;
              color: white;
              transform: translateY(-1px);
            }
          }

          &.btn-primary {
            background: $primary-color;
            color: white;
            box-shadow: $shadow-primary;

            &:hover {
              background: $primary-hover;
              transform: translateY(-1px);
              box-shadow: $shadow-primary-hover;
            }
          }
        }

        .user-dropdown {
          .user-info {
            display: flex;
            align-items: center;
            gap: $spacing-sm;
            padding: $spacing-sm $spacing-md;
            border-radius: $radius-lg;
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: $glass-blur-sm;
            cursor: pointer;
            transition: all 0.3s ease;

            &:hover {
              background: rgba(255, 255, 255, 0.2);
            }

            .user-avatar {
              border: 2px solid rgba(255, 255, 255, 0.3);
            }

            .user-name {
              font-weight: 500;
              color: $text-primary;
              font-size: $font-size-sm;
            }

            .dropdown-icon {
              color: $text-secondary;
              font-size: $font-size-xs;
              transition: transform 0.3s ease;
            }

            &:hover .dropdown-icon {
              transform: rotate(180deg);
            }
          }
        }
      }
    }

    .mobile-menu-toggle {
      display: none;
      background: none;
      border: none;
      color: $text-primary;
      font-size: $font-size-lg;
      cursor: pointer;
      padding: $spacing-sm;
      border-radius: $radius-md;
      transition: all 0.3s ease;

      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: $primary-color;
      }
    }
  }
}

// 主要内容区域
.public-content {
  flex: 1;
  padding-top: 70px; // 为固定导航栏留出空间
}

// 响应式设计
@media (max-width: 1199px) {
  .header {
    .navbar {
      .nav-search {
        .search-box {
          .search-input {
            width: 250px;
          }
        }
      }
    }
  }
}

@media (max-width: 991px) {
  .header {
    .navbar {
      .nav-menu {
        display: none;
      }

      .nav-search {
        display: none;
      }

      .mobile-menu-toggle {
        display: block;
      }
    }
  }
}

@media (max-width: 767px) {
  .header {
    .navbar {
      .container {
        padding: 0 $spacing-md;
      }

      .nav-user {
        .user-actions {
          gap: $spacing-sm;

          .btn {
            padding: $spacing-xs $spacing-md;
            font-size: $font-size-xs;
          }
        }
      }
    }
  }
}
</style>
