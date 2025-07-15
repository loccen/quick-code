/**
 * 登录页面对象模型
 */
import { Locator, Page } from '@playwright/test'
import { BasePage } from './BasePage'

export class LoginPage extends BasePage {
  // 页面元素选择器
  private readonly usernameInput: Locator
  private readonly passwordInput: Locator
  private readonly loginButton: Locator
  private readonly registerLink: Locator
  private readonly forgotPasswordLink: Locator
  private readonly rememberMeCheckbox: Locator
  private readonly errorMessage: Locator
  private readonly loadingSpinner: Locator

  constructor(page: Page) {
    super(page)

    // 初始化页面元素
    this.usernameInput = page.locator('[data-testid="username-input"]')
    this.passwordInput = page.locator('[data-testid="password-input"]')
    this.loginButton = page.locator('[data-testid="login-button"]')
    this.registerLink = page.locator('[data-testid="register-link"]')
    this.forgotPasswordLink = page.locator('[data-testid="forgot-password-link"]')
    this.rememberMeCheckbox = page.locator('[data-testid="remember-me-checkbox"]')
    this.errorMessage = page.locator('[data-testid="error-message"]')
    this.loadingSpinner = page.locator('[data-testid="loading-spinner"]')
  }

  /**
   * 导航到登录页面
   */
  async navigate() {
    await this.goto('/login')
    await this.waitForLoad()
  }

  /**
   * 填写用户名
   */
  async fillUsername(username: string) {
    await this.fill(this.usernameInput, username)
  }

  /**
   * 填写密码
   */
  async fillPassword(password: string) {
    await this.fill(this.passwordInput, password)
  }

  /**
   * 点击登录按钮
   */
  async clickLogin() {
    await this.click(this.loginButton)
  }

  /**
   * 点击注册链接
   */
  async clickRegister() {
    await this.click(this.registerLink)
  }

  /**
   * 点击忘记密码链接
   */
  async clickForgotPassword() {
    await this.click(this.forgotPasswordLink)
  }

  /**
   * 切换记住我选项
   */
  async toggleRememberMe() {
    await this.click(this.rememberMeCheckbox)
  }

  /**
   * 执行登录操作
   */
  async login(username: string, password: string, rememberMe = false) {
    await this.fillUsername(username)
    await this.fillPassword(password)

    if (rememberMe) {
      await this.toggleRememberMe()
    }

    await this.clickLogin()
  }

  /**
   * 快速登录（使用默认测试账号）
   */
  async quickLogin() {
    await this.login('testuser@example.com', 'password123')
  }

  /**
   * 等待登录完成
   */
  async waitForLoginComplete() {
    // 等待加载动画消失
    await this.waitForHidden('[data-testid="loading-spinner"]')

    // 等待跳转到仪表盘
    await this.page.waitForURL('/user/dashboard', { timeout: 10000 })
  }

  /**
   * 获取错误消息
   */
  async getErrorMessage(): Promise<string> {
    await this.waitForVisible('[data-testid="error-message"]')
    return await this.getText(this.errorMessage)
  }

  /**
   * 检查是否显示错误消息
   */
  async hasErrorMessage(): Promise<boolean> {
    return await this.isVisible(this.errorMessage)
  }

  /**
   * 检查登录按钮是否可用
   */
  async isLoginButtonEnabled(): Promise<boolean> {
    return await this.isEnabled(this.loginButton)
  }

  /**
   * 检查是否正在加载
   */
  async isLoading(): Promise<boolean> {
    return await this.isVisible(this.loadingSpinner)
  }

  /**
   * 检查记住我是否选中
   */
  async isRememberMeChecked(): Promise<boolean> {
    return await this.isChecked(this.rememberMeCheckbox)
  }

  /**
   * 清空表单
   */
  async clearForm() {
    await this.clearAndFill(this.usernameInput, '')
    await this.clearAndFill(this.passwordInput, '')
  }

  /**
   * 验证页面元素是否存在
   */
  async validatePageElements() {
    await this.waitForVisible('[data-testid="username-input"]')
    await this.waitForVisible('[data-testid="password-input"]')
    await this.waitForVisible('[data-testid="login-button"]')
    await this.waitForVisible('[data-testid="register-link"]')
    await this.waitForVisible('[data-testid="forgot-password-link"]')
  }
}
