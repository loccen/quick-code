/**
 * 页面对象模型基类
 */
import { Locator, Page } from '@playwright/test'

export abstract class BasePage {
  protected page: Page

  constructor(page: Page) {
    this.page = page
  }

  /**
   * 获取页面对象
   */
  getPage(): Page {
    return this.page
  }

  /**
   * 导航到指定URL
   */
  async goto(url: string, options?: { waitUntil?: 'load' | 'domcontentloaded' | 'networkidle' }) {
    await this.page.goto(url, { waitUntil: 'networkidle', ...options })
  }

  /**
   * 等待页面加载完成
   */
  async waitForLoad() {
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * 等待元素可见
   */
  async waitForVisible(selector: string, timeout = 10000) {
    await this.page.waitForSelector(selector, { state: 'visible', timeout })
  }

  /**
   * 等待元素隐藏
   */
  async waitForHidden(selector: string, timeout = 10000) {
    await this.page.waitForSelector(selector, { state: 'hidden', timeout })
  }

  /**
   * 点击元素
   */
  async click(selector: string | Locator, options?: { timeout?: number; force?: boolean }) {
    const locator = typeof selector === 'string' ? this.page.locator(selector) : selector
    await locator.click(options)
  }

  /**
   * 填写输入框
   */
  async fill(selector: string | Locator, value: string, options?: { timeout?: number }) {
    const locator = typeof selector === 'string' ? this.page.locator(selector) : selector
    await locator.fill(value, options)
  }

  /**
   * 清空并填写输入框
   */
  async clearAndFill(selector: string | Locator, value: string) {
    const locator = typeof selector === 'string' ? this.page.locator(selector) : selector
    await locator.clear()
    await locator.fill(value)
  }

  /**
   * 选择下拉框选项
   */
  async selectOption(selector: string | Locator, value: string | string[]) {
    const locator = typeof selector === 'string' ? this.page.locator(selector) : selector
    await locator.selectOption(value)
  }

  /**
   * 获取元素文本
   */
  async getText(selector: string | Locator): Promise<string> {
    const locator = typeof selector === 'string' ? this.page.locator(selector) : selector
    return await locator.textContent() || ''
  }

  /**
   * 获取元素属性
   */
  async getAttribute(selector: string | Locator, name: string): Promise<string | null> {
    const locator = typeof selector === 'string' ? this.page.locator(selector) : selector
    return await locator.getAttribute(name)
  }

  /**
   * 检查元素是否可见
   */
  async isVisible(selector: string | Locator): Promise<boolean> {
    const locator = typeof selector === 'string' ? this.page.locator(selector) : selector
    return await locator.isVisible()
  }

  /**
   * 检查元素是否启用
   */
  async isEnabled(selector: string | Locator): Promise<boolean> {
    const locator = typeof selector === 'string' ? this.page.locator(selector) : selector
    return await locator.isEnabled()
  }

  /**
   * 检查复选框是否选中
   */
  async isChecked(selector: string | Locator): Promise<boolean> {
    const locator = typeof selector === 'string' ? this.page.locator(selector) : selector
    return await locator.isChecked()
  }

  /**
   * 悬停在元素上
   */
  async hover(selector: string | Locator) {
    const locator = typeof selector === 'string' ? this.page.locator(selector) : selector
    await locator.hover()
  }

  /**
   * 滚动到元素
   */
  async scrollTo(selector: string | Locator) {
    const locator = typeof selector === 'string' ? this.page.locator(selector) : selector
    await locator.scrollIntoViewIfNeeded()
  }

  /**
   * 等待并点击
   */
  async waitAndClick(selector: string, timeout = 10000) {
    await this.waitForVisible(selector, timeout)
    await this.click(selector)
  }

  /**
   * 等待并填写
   */
  async waitAndFill(selector: string, value: string, timeout = 10000) {
    await this.waitForVisible(selector, timeout)
    await this.fill(selector, value)
  }

  /**
   * 截图
   */
  async screenshot(name: string, options?: { fullPage?: boolean }) {
    await this.page.screenshot({
      path: `test-results/screenshots/${name}.png`,
      fullPage: options?.fullPage || false
    })
  }

  /**
   * 等待网络空闲
   */
  async waitForNetworkIdle(timeout = 30000) {
    await this.page.waitForLoadState('networkidle', { timeout })
  }

  /**
   * 等待特定的网络请求
   */
  async waitForRequest(urlPattern: string | RegExp, timeout = 30000) {
    return await this.page.waitForRequest(urlPattern, { timeout })
  }

  /**
   * 等待特定的网络响应
   */
  async waitForResponse(urlPattern: string | RegExp, timeout = 30000) {
    return await this.page.waitForResponse(urlPattern, { timeout })
  }

  /**
   * 执行JavaScript代码
   */
  async evaluate<T>(fn: () => T): Promise<T> {
    return await this.page.evaluate(fn)
  }

  /**
   * 获取页面标题
   */
  async getTitle(): Promise<string> {
    return await this.page.title()
  }

  /**
   * 获取当前URL
   */
  async getURL(): Promise<string> {
    return this.page.url()
  }

  /**
   * 刷新页面
   */
  async reload() {
    await this.page.reload({ waitUntil: 'networkidle' })
  }

  /**
   * 返回上一页
   */
  async goBack() {
    await this.page.goBack({ waitUntil: 'networkidle' })
  }

  /**
   * 前进到下一页
   */
  async goForward() {
    await this.page.goForward({ waitUntil: 'networkidle' })
  }
}
