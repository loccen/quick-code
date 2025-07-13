/**
 * 仪表盘页面对象模型
 */
import { Page, Locator } from '@playwright/test'
import { BasePage } from './BasePage'

export class DashboardPage extends BasePage {
  // 页面元素选择器
  private readonly pageTitle: Locator
  private readonly userAvatar: Locator
  private readonly userMenu: Locator
  private readonly logoutButton: Locator
  private readonly sidebarToggle: Locator
  private readonly sidebar: Locator
  private readonly mainContent: Locator
  private readonly breadcrumb: Locator
  private readonly statsCards: Locator

  constructor(page: Page) {
    super(page)
    
    // 初始化页面元素
    this.pageTitle = page.locator('[data-testid="page-title"]')
    this.userAvatar = page.locator('[data-testid="user-avatar"]')
    this.userMenu = page.locator('[data-testid="user-menu"]')
    this.logoutButton = page.locator('[data-testid="logout-button"]')
    this.sidebarToggle = page.locator('[data-testid="sidebar-toggle"]')
    this.sidebar = page.locator('[data-testid="sidebar"]')
    this.mainContent = page.locator('[data-testid="main-content"]')
    this.breadcrumb = page.locator('[data-testid="breadcrumb"]')
    this.statsCards = page.locator('[data-testid="stats-card"]')
  }

  /**
   * 导航到仪表盘页面
   */
  async navigate() {
    await this.goto('/dashboard')
    await this.waitForLoad()
  }

  /**
   * 等待仪表盘加载完成
   */
  async waitForDashboardLoad() {
    await this.waitForVisible('[data-testid="page-title"]')
    await this.waitForVisible('[data-testid="main-content"]')
    await this.waitForNetworkIdle()
  }

  /**
   * 点击用户头像
   */
  async clickUserAvatar() {
    await this.click(this.userAvatar)
  }

  /**
   * 点击登出按钮
   */
  async clickLogout() {
    await this.clickUserAvatar()
    await this.waitForVisible('[data-testid="user-menu"]')
    await this.click(this.logoutButton)
  }

  /**
   * 切换侧边栏
   */
  async toggleSidebar() {
    await this.click(this.sidebarToggle)
  }

  /**
   * 导航到指定菜单项
   */
  async navigateToMenu(menuItem: string) {
    const menuSelector = `[data-testid="menu-${menuItem}"]`
    await this.waitAndClick(menuSelector)
  }

  /**
   * 获取页面标题
   */
  async getPageTitle(): Promise<string> {
    return await this.getText(this.pageTitle)
  }

  /**
   * 获取面包屑文本
   */
  async getBreadcrumbText(): Promise<string> {
    return await this.getText(this.breadcrumb)
  }

  /**
   * 获取统计卡片数量
   */
  async getStatsCardsCount(): Promise<number> {
    return await this.statsCards.count()
  }

  /**
   * 获取指定统计卡片的值
   */
  async getStatsCardValue(index: number): Promise<string> {
    const card = this.statsCards.nth(index)
    const valueElement = card.locator('[data-testid="stats-value"]')
    return await this.getText(valueElement)
  }

  /**
   * 检查侧边栏是否展开
   */
  async isSidebarExpanded(): Promise<boolean> {
    const sidebarClass = await this.getAttribute(this.sidebar, 'class')
    return !sidebarClass?.includes('collapsed')
  }

  /**
   * 检查用户菜单是否显示
   */
  async isUserMenuVisible(): Promise<boolean> {
    return await this.isVisible(this.userMenu)
  }

  /**
   * 验证仪表盘页面元素
   */
  async validateDashboardElements() {
    await this.waitForVisible('[data-testid="page-title"]')
    await this.waitForVisible('[data-testid="user-avatar"]')
    await this.waitForVisible('[data-testid="sidebar"]')
    await this.waitForVisible('[data-testid="main-content"]')
    await this.waitForVisible('[data-testid="breadcrumb"]')
  }

  /**
   * 等待统计数据加载
   */
  async waitForStatsLoad() {
    await this.waitForVisible('[data-testid="stats-card"]')
    // 等待所有统计卡片加载完成
    await this.page.waitForFunction(() => {
      const cards = document.querySelectorAll('[data-testid="stats-card"]')
      return Array.from(cards).every(card => {
        const value = card.querySelector('[data-testid="stats-value"]')
        return value && value.textContent !== '...'
      })
    }, { timeout: 10000 })
  }
}
