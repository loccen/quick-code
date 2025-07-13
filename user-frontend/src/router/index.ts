/**
 * Vue Router配置
 */
import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuards } from './guards'
import { routes } from './routes'

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 如果有保存的位置，恢复到该位置
    if (savedPosition) {
      return savedPosition
    }
    // 如果有锚点，滚动到锚点
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth'
      }
    }
    // 否则滚动到顶部
    return { top: 0 }
  }
})

// 设置路由守卫
setupRouterGuards(router)

export default router

// 导出路由相关工具
export { createAuthGuard, createPermissionGuard, createRoleGuard, setupRouterGuards } from './guards'
export { basicRoutes, dynamicRoutes, mainRoutes, publicRoutes, routes } from './routes'

