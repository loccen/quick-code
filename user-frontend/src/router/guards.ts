/**
 * 路由守卫
 */
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { NavigationGuardNext, RouteLocationNormalized, Router } from 'vue-router'

/**
 * 设置路由守卫
 */
export function setupRouterGuards(router: Router) {
  // 全局前置守卫
  router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()
    const appStore = useAppStore()

    // 设置页面加载状态
    appStore.setPageLoading(true)

    // 设置页面标题
    if (to.meta?.title) {
      appStore.setPageTitle(to.meta.title)
    }

    // 如果已登录且访问登录页，重定向到用户个人中心
    if (to.path === '/login' && userStore.isAuthenticated) {
      next('/user/profile')
      return
    }

    // 认证检查 - 只对明确需要认证的路由进行检查
    if (to.meta?.requiresAuth && !userStore.isAuthenticated) {
      ElMessage.warning('请先登录后再访问该功能')
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 权限检查 - 只在已登录且需要特定权限时检查
    if (userStore.isAuthenticated && to.meta?.permissions && to.meta.permissions.length > 0) {
      const hasPermission = userStore.hasAnyPermission(to.meta.permissions)
      if (!hasPermission) {
        ElMessage.error('权限不足')
        next('/403')
        return
      }
    }

    // 角色检查 - 只在已登录且需要特定角色时检查
    if (userStore.isAuthenticated && to.meta?.roles && to.meta.roles.length > 0) {
      const hasRole = to.meta.roles.some(role => userStore.hasRole(role))
      if (!hasRole) {
        ElMessage.error('角色权限不足')
        next('/403')
        return
      }
    }

    next()
  })

  // 全局后置守卫
  router.afterEach((to, from) => {
    const appStore = useAppStore()

    // 关闭页面加载状态
    appStore.setPageLoading(false)

    // 清除错误状态
    appStore.setError('')
  })

  // 全局解析守卫
  router.beforeResolve(async (to, from, next) => {
    // 在这里可以进行一些异步操作，比如获取用户信息
    const userStore = useUserStore()

    // 如果用户已登录但没有用户信息，尝试获取
    if (userStore.isAuthenticated && !userStore.user) {
      try {
        await userStore.fetchCurrentUser()
      } catch (error) {
        console.error('获取用户信息失败:', error)
        // 如果获取用户信息失败，可能token已过期，尝试刷新
        const refreshSuccess = await userStore.refreshAccessToken()
        if (!refreshSuccess) {
          // 刷新失败，跳转到登录页
          next('/login')
          return
        }
      }
    }

    next()
  })
}

/**
 * 权限检查守卫
 */
export function createPermissionGuard(permissions: string[]) {
  return (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    const userStore = useUserStore()

    if (!userStore.hasAnyPermission(permissions)) {
      ElMessage.error('权限不足')
      next('/403')
      return
    }

    next()
  }
}

/**
 * 角色检查守卫
 */
export function createRoleGuard(roles: string[]) {
  return (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    const userStore = useUserStore()

    const hasRole = roles.some(role => userStore.hasRole(role))
    if (!hasRole) {
      ElMessage.error('角色权限不足')
      next('/403')
      return
    }

    next()
  }
}

/**
 * 认证检查守卫
 */
export function createAuthGuard() {
  return (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    const userStore = useUserStore()

    if (!userStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    next()
  }
}
