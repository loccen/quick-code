<template>
  <div class="main-layout" :class="{ 'main-layout--mobile': appStore.isMobile }">
    <!-- 侧边栏 -->
    <aside
      class="main-layout__sidebar"
      :class="{ 'main-layout__sidebar--collapsed': appStore.sidebarCollapsed }"
    >
      <div class="sidebar-content">
        <!-- Logo区域 -->
        <div class="sidebar-header">
          <router-link to="/" class="logo">
            <img src="/favicon.ico" alt="Logo" class="logo__image">
            <span v-show="!appStore.sidebarCollapsed" class="logo__text">速码网</span>
          </router-link>
        </div>

        <!-- 导航菜单 -->
        <nav class="sidebar-nav">
          <el-menu
            :default-active="$route.path"
            :collapse="appStore.sidebarCollapsed"
            :unique-opened="true"
            router
            class="sidebar-menu"
          >
            <el-menu-item index="/dashboard">
              <el-icon><Dashboard /></el-icon>
              <template #title>仪表盘</template>
            </el-menu-item>

            <el-sub-menu index="/projects">
              <template #title>
                <el-icon><FolderOpened /></el-icon>
                <span>项目管理</span>
              </template>
              <el-menu-item index="/projects">项目列表</el-menu-item>
              <el-menu-item index="/projects/create">创建项目</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="/admin" v-if="userStore.hasRole('admin')">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统管理</span>
              </template>
              <el-menu-item index="/admin/users">用户管理</el-menu-item>
              <el-menu-item index="/admin/roles">角色管理</el-menu-item>
              <el-menu-item index="/admin/permissions">权限管理</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </nav>
      </div>
    </aside>

    <!-- 主内容区域 -->
    <div class="main-layout__content">
      <!-- 顶部头部 -->
      <header class="main-layout__header">
        <div class="header-left">
          <!-- 侧边栏切换按钮 -->
          <el-button
            type="text"
            class="sidebar-toggle"
            @click="appStore.toggleSidebar"
          >
            <el-icon><Menu /></el-icon>
          </el-button>

          <!-- 面包屑导航 -->
          <el-breadcrumb class="breadcrumb" separator="/">
            <el-breadcrumb-item
              v-for="item in breadcrumbItems"
              :key="item.path"
              :to="item.path"
            >
              <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 主题切换 -->
          <el-button
            type="text"
            class="theme-toggle"
            @click="toggleTheme"
          >
            <el-icon>
              <Sunny v-if="appStore.isDarkMode" />
              <Moon v-else />
            </el-icon>
          </el-button>

          <!-- 用户菜单 -->
          <el-dropdown class="user-dropdown" @command="handleUserCommand">
            <div class="user-info">
              <el-avatar
                :src="userStore.user?.avatar"
                :size="32"
                class="user-avatar"
              >
                <el-icon><User /></el-icon>
              </el-avatar>
              <span v-if="!appStore.isMobile" class="user-name">
                {{ userStore.user?.nickname || userStore.user?.username }}
              </span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
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
      </header>

      <!-- 标签页 -->
      <div v-if="appStore.showTabs" class="main-layout__tabs">
        <el-tabs
          v-model="tabsStore.activeTab"
          type="card"
          class="layout-tabs"
          @tab-remove="tabsStore.removeTab"
          @tab-click="handleTabClick"
        >
          <el-tab-pane
            v-for="tab in tabsStore.tabs"
            :key="tab.name"
            :label="tab.title"
            :name="tab.name"
            :closable="tab.closable"
          >
            <template #label>
              <span class="tab-label">
                <el-icon v-if="tab.icon"><component :is="tab.icon" /></el-icon>
                {{ tab.title }}
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>

        <!-- 标签页操作菜单 -->
        <el-dropdown class="tabs-actions" @command="handleTabsCommand">
          <el-button type="text" size="small">
            <el-icon><MoreFilled /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="closeOthers">关闭其他</el-dropdown-item>
              <el-dropdown-item command="closeLeft">关闭左侧</el-dropdown-item>
              <el-dropdown-item command="closeRight">关闭右侧</el-dropdown-item>
              <el-dropdown-item command="closeAll">关闭全部</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 页面内容 -->
      <main class="main-layout__main">
        <div class="page-container">
          <router-view v-slot="{ Component, route }">
            <transition name="page-fade" mode="out-in">
              <keep-alive :include="keepAliveComponents">
                <component :is="Component" :key="route.fullPath" />
              </keep-alive>
            </transition>
          </router-view>
        </div>
      </main>

      <!-- 底部 -->
      <footer v-if="appStore.showFooter" class="main-layout__footer">
        <div class="footer-content">
          <span>&copy; 2024 速码网. All rights reserved.</span>
          <div class="footer-links">
            <a href="#" target="_blank">帮助中心</a>
            <a href="#" target="_blank">隐私政策</a>
            <a href="#" target="_blank">服务条款</a>
          </div>
        </div>
      </footer>
    </div>

    <!-- 移动端遮罩 -->
    <div
      v-if="appStore.isMobile && !appStore.sidebarCollapsed"
      class="mobile-overlay"
      @click="appStore.setSidebarCollapsed(true)"
    />
  </div>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import { useTabsStore } from '@/stores/tabs'
import { useUserStore } from '@/stores/user'
import { getRouteBreadcrumb } from '@/utils/breadcrumb'
import {
    ArrowDown,
    Dashboard, FolderOpened,
    Menu,
    Moon, MoreFilled,
    Setting,
    Sunny,
    SwitchButton,
    User
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
const tabsStore = useTabsStore()

// 面包屑导航
const breadcrumbItems = computed(() => {
  return getRouteBreadcrumb(route)
})

// 需要缓存的组件
const keepAliveComponents = computed(() => {
  return tabsStore.tabs
    .filter(tab => route.meta?.keepAlive)
    .map(tab => tab.name)
})

// 监听路由变化，添加标签页
watch(route, (newRoute) => {
  if (newRoute.meta?.title && newRoute.name) {
    tabsStore.addTab(newRoute)
  }
}, { immediate: true })

// 切换主题
const toggleTheme = () => {
  const newTheme = appStore.isDarkMode ? 'light' : 'dark'
  appStore.setTheme(newTheme)
}

// 处理用户菜单命令
const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await userStore.logout()
      } catch {
        // 用户取消
      }
      break
  }
}

// 处理标签页点击
const handleTabClick = (tab: any) => {
  const targetTab = tabsStore.tabs.find(t => t.name === tab.paneName)
  if (targetTab && targetTab.path !== route.fullPath) {
    router.push(targetTab.path)
  }
}

// 处理标签页操作命令
const handleTabsCommand = (command: string) => {
  const currentTab = tabsStore.activeTab

  switch (command) {
    case 'closeOthers':
      tabsStore.removeOtherTabs(currentTab)
      break
    case 'closeLeft':
      tabsStore.removeLeftTabs(currentTab)
      break
    case 'closeRight':
      tabsStore.removeRightTabs(currentTab)
      break
    case 'closeAll':
      tabsStore.removeAllTabs()
      router.push('/dashboard')
      break
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.main-layout {
  display: flex;
  height: 100vh;
  background: var(--bg-primary);
  transition: all var(--transition-base);

  &--mobile {
    .main-layout__sidebar {
      position: fixed;
      z-index: $z-fixed;
      transform: translateX(-100%);

      &:not(.main-layout__sidebar--collapsed) {
        transform: translateX(0);
      }
    }

    .main-layout__content {
      margin-left: 0;
    }
  }

  // 侧边栏
  &__sidebar {
    width: $sidebar-width;
    background: var(--glass-bg);
    @include glass-effect();
    border-right: 1px solid var(--glass-border);
    transition: all var(--transition-base);
    z-index: $z-sticky;

    &--collapsed {
      width: $sidebar-width-collapsed;
    }

    .sidebar-content {
      height: 100%;
      display: flex;
      flex-direction: column;
    }

    .sidebar-header {
      height: $header-height;
      @include flex-center();
      padding: 0 $spacing-md;
      border-bottom: 1px solid var(--glass-border);

      .logo {
        @include flex-center();
        gap: $spacing-sm;
        text-decoration: none;
        color: var(--text-primary);
        font-weight: $font-weight-bold;
        font-size: $font-size-lg;
        transition: all var(--transition-fast);

        &:hover {
          color: var(--primary-color);
        }

        &__image {
          width: 32px;
          height: 32px;
          border-radius: $radius-sm;
        }

        &__text {
          @include gradient-text();
        }
      }
    }

    .sidebar-nav {
      flex: 1;
      overflow-y: auto;
      padding: $spacing-sm 0;

      .sidebar-menu {
        border: none;
        background: transparent;

        :deep(.el-menu-item),
        :deep(.el-sub-menu__title) {
          height: 48px;
          line-height: 48px;
          margin: 0 $spacing-sm;
          border-radius: $radius-lg;
          transition: all var(--transition-fast);

          &:hover {
            background: rgba(var(--primary-color), 0.1);
            color: var(--primary-color);
          }

          &.is-active {
            background: var(--gradient-primary);
            color: white;
            @include shadow-colored(var(--primary-color), 0.2);
          }
        }

        :deep(.el-sub-menu .el-menu-item) {
          margin: 0 $spacing-md;
          padding-left: 48px !important;
        }
      }
    }
  }

  // 主内容区域
  &__content {
    flex: 1;
    display: flex;
    flex-direction: column;
    margin-left: $sidebar-width;
    transition: margin-left var(--transition-base);

    .main-layout__sidebar--collapsed + & {
      margin-left: $sidebar-width-collapsed;
    }
  }

  // 头部
  &__header {
    height: $header-height;
    @include flex-between();
    padding: 0 $spacing-lg;
    background: var(--glass-bg);
    @include glass-effect();
    border-bottom: 1px solid var(--glass-border);
    @include shadow-sm();

    .header-left {
      @include flex-center();
      gap: $spacing-lg;

      .sidebar-toggle {
        padding: $spacing-sm;
        border-radius: $radius-md;
        transition: all var(--transition-fast);

        &:hover {
          background: rgba(var(--primary-color), 0.1);
          color: var(--primary-color);
        }
      }

      .breadcrumb {
        :deep(.el-breadcrumb__item) {
          .el-breadcrumb__inner {
            @include flex-center();
            gap: $spacing-xs;
            color: var(--text-secondary);
            transition: color var(--transition-fast);

            &:hover {
              color: var(--primary-color);
            }
          }

          &:last-child .el-breadcrumb__inner {
            color: var(--text-primary);
            font-weight: $font-weight-medium;
          }
        }
      }
    }

    .header-right {
      @include flex-center();
      gap: $spacing-md;

      .theme-toggle {
        padding: $spacing-sm;
        border-radius: $radius-md;
        transition: all var(--transition-fast);

        &:hover {
          background: rgba(var(--primary-color), 0.1);
          color: var(--primary-color);
        }
      }

      .user-dropdown {
        .user-info {
          @include flex-center();
          gap: $spacing-sm;
          padding: $spacing-xs $spacing-sm;
          border-radius: $radius-lg;
          cursor: pointer;
          transition: all var(--transition-fast);

          &:hover {
            background: rgba(var(--primary-color), 0.1);
          }

          .user-avatar {
            @include shadow-sm();
          }

          .user-name {
            font-weight: $font-weight-medium;
            color: var(--text-primary);
          }

          .dropdown-icon {
            font-size: 12px;
            color: var(--text-tertiary);
            transition: transform var(--transition-fast);
          }

          &:hover .dropdown-icon {
            transform: rotate(180deg);
          }
        }
      }
    }
  }

  // 标签页
  &__tabs {
    position: relative;
    background: var(--bg-secondary);
    border-bottom: 1px solid var(--border-light);
    padding: 0 $spacing-lg;

    .layout-tabs {
      :deep(.el-tabs__header) {
        margin: 0;
        border: none;

        .el-tabs__nav-wrap {
          &::after {
            display: none;
          }
        }

        .el-tabs__item {
          height: $tabs-height;
          line-height: $tabs-height;
          border: none;
          border-radius: $radius-md $radius-md 0 0;
          margin-right: $spacing-xs;
          padding: 0 $spacing-md;
          background: transparent;
          transition: all var(--transition-fast);

          &:hover {
            background: var(--bg-primary);
            color: var(--primary-color);
          }

          &.is-active {
            background: var(--bg-primary);
            color: var(--primary-color);
            @include shadow-sm();
          }

          .tab-label {
            @include flex-center();
            gap: $spacing-xs;
          }
        }
      }
    }

    .tabs-actions {
      position: absolute;
      right: $spacing-lg;
      top: 50%;
      transform: translateY(-50%);
    }
  }

  // 主要内容
  &__main {
    flex: 1;
    overflow: hidden;
    background: var(--bg-secondary);

    .page-container {
      height: 100%;
      padding: $spacing-lg;
      overflow-y: auto;
    }
  }

  // 底部
  &__footer {
    background: var(--bg-primary);
    border-top: 1px solid var(--border-light);
    padding: $spacing-md $spacing-lg;

    .footer-content {
      @include flex-between();
      color: var(--text-secondary);
      font-size: $font-size-sm;

      .footer-links {
        @include flex-center();
        gap: $spacing-lg;

        a {
          color: var(--text-tertiary);
          text-decoration: none;
          transition: color var(--transition-fast);

          &:hover {
            color: var(--primary-color);
          }
        }
      }
    }
  }
}

// 移动端遮罩
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: $z-modal-backdrop;
  backdrop-filter: blur(4px);
}

// 页面切换动画
.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.3s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

// 响应式设计
@include respond-below('md') {
  .main-layout {
    &__header {
      padding: 0 $spacing-md;

      .header-left {
        gap: $spacing-md;

        .breadcrumb {
          display: none;
        }
      }

      .header-right {
        gap: $spacing-sm;

        .user-name {
          display: none;
        }
      }
    }

    &__tabs {
      padding: 0 $spacing-md;
    }

    &__main .page-container {
      padding: $spacing-md;
    }

    &__footer {
      padding: $spacing-sm $spacing-md;

      .footer-content {
        flex-direction: column;
        gap: $spacing-sm;
        text-align: center;

        .footer-links {
          gap: $spacing-md;
        }
      }
    }
  }
}

// 暗色主题适配
.dark .main-layout {
  &__sidebar {
    .sidebar-header {
      border-bottom-color: rgba(255, 255, 255, 0.1);
    }
  }

  &__header {
    border-bottom-color: rgba(255, 255, 255, 0.1);
  }

  &__tabs {
    background: #1a1a1a;
    border-bottom-color: #434343;
  }

  &__footer {
    border-top-color: #434343;
  }
}
</style>
