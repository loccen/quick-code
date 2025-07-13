/**
 * Playwright测试基础fixtures
 */
import { test as base, expect } from '@playwright/test'
import { LoginPage } from '../pages/LoginPage'
import { DashboardPage } from '../pages/DashboardPage'
import { ProfilePage } from '../pages/ProfilePage'

// 扩展测试上下文
type TestFixtures = {
  loginPage: LoginPage
  dashboardPage: DashboardPage
  profilePage: ProfilePage
}

type WorkerFixtures = {
  // 可以添加worker级别的fixtures
}

/**
 * 扩展的测试对象
 */
export const test = base.extend<TestFixtures, WorkerFixtures>({
  // 登录页面对象
  loginPage: async ({ page }, use) => {
    const loginPage = new LoginPage(page)
    await use(loginPage)
  },

  // 仪表盘页面对象
  dashboardPage: async ({ page }, use) => {
    const dashboardPage = new DashboardPage(page)
    await use(dashboardPage)
  },

  // 个人中心页面对象
  profilePage: async ({ page }, use) => {
    const profilePage = new ProfilePage(page)
    await use(profilePage)
  }
})

/**
 * 导出expect
 */
export { expect } from '@playwright/test'

/**
 * 自定义断言
 */
export const customExpect = {
  /**
   * 检查元素是否可见且可点击
   */
  async toBeClickable(locator: any) {
    await expect(locator).toBeVisible()
    await expect(locator).toBeEnabled()
  },

  /**
   * 检查页面是否加载完成
   */
  async toBeLoaded(page: any) {
    await expect(page).toHaveLoadState('networkidle')
  },

  /**
   * 检查URL是否匹配
   */
  async toHaveURL(page: any, url: string | RegExp) {
    await expect(page).toHaveURL(url)
  },

  /**
   * 检查页面标题
   */
  async toHaveTitle(page: any, title: string | RegExp) {
    await expect(page).toHaveTitle(title)
  }
}
