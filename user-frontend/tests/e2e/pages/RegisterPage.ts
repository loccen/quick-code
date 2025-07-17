/**
 * 注册页面对象模型
 */
import { Locator, Page } from '@playwright/test'
import { BasePage } from './BasePage'

export interface RegisterFormData {
  username: string
  email: string
  password: string
  confirmPassword: string
  emailCode: string
}

export class RegisterPage extends BasePage {
  // 页面元素选择器
  private readonly usernameInput: Locator
  private readonly emailInput: Locator
  private readonly passwordInput: Locator
  private readonly confirmPasswordInput: Locator
  private readonly emailCodeInput: Locator
  private readonly sendEmailCodeButton: Locator
  private readonly agreeTermsCheckbox: Locator
  private readonly registerButton: Locator
  private readonly loginLink: Locator
  private readonly errorMessage: Locator
  private readonly successMessage: Locator
  private readonly loadingSpinner: Locator

  constructor(page: Page) {
    super(page)

    // 初始化页面元素
    this.usernameInput = page.locator('[data-testid="username-input"]')
    this.emailInput = page.locator('[data-testid="email-input"]')
    this.passwordInput = page.locator('[data-testid="password-input"]')
    this.confirmPasswordInput = page.locator('[data-testid="confirm-password-input"]')
    this.emailCodeInput = page.locator('[data-testid="email-code-input"]')
    this.sendEmailCodeButton = page.locator('[data-testid="send-code-button"]')
    this.agreeTermsCheckbox = page.locator('[data-testid="agree-terms-checkbox"]')
    this.registerButton = page.locator('[data-testid="register-button"]')
    this.loginLink = page.locator('[data-testid="login-link"]')
    this.errorMessage = page.locator('[data-testid="error-message"]')
    this.successMessage = page.locator('.el-message--success')
    this.loadingSpinner = page.locator('[data-testid="loading-spinner"]')
  }

  /**
   * 导航到注册页面
   */
  async navigate() {
    await this.goto('/register')
    await this.waitForLoad()
  }

  /**
   * 填写用户名
   */
  async fillUsername(username: string) {
    await this.fill(this.usernameInput, username)
  }

  /**
   * 填写邮箱
   */
  async fillEmail(email: string) {
    await this.fill(this.emailInput, email)
  }

  /**
   * 填写密码
   */
  async fillPassword(password: string) {
    await this.fill(this.passwordInput, password)
  }

  /**
   * 填写确认密码
   */
  async fillConfirmPassword(confirmPassword: string) {
    await this.fill(this.confirmPasswordInput, confirmPassword)
  }

  /**
   * 填写邮箱验证码
   */
  async fillEmailCode(emailCode: string) {
    await this.fill(this.emailCodeInput, emailCode)
  }

  /**
   * 点击发送邮箱验证码按钮
   */
  async clickSendEmailCode() {
    await this.click(this.sendEmailCodeButton)
  }

  /**
   * 切换同意条款选项
   */
  async toggleAgreeTerms() {
    await this.click(this.agreeTermsCheckbox)
  }

  /**
   * 点击注册按钮
   */
  async clickRegister() {
    await this.click(this.registerButton)
  }

  /**
   * 点击登录链接
   */
  async clickLogin() {
    await this.click(this.loginLink)
  }

  /**
   * 填写表单
   */
  async fillForm(data: RegisterFormData) {
    await this.fillUsername(data.username)
    await this.fillEmail(data.email)
    await this.fillPassword(data.password)
    await this.fillConfirmPassword(data.confirmPassword)
    await this.fillEmailCode(data.emailCode)
    await this.toggleAgreeTerms()
  }

  /**
   * 执行注册操作
   */
  async register(data: RegisterFormData) {
    await this.fillForm(data)
    await this.clickRegister()
  }

  /**
   * 等待注册完成
   */
  async waitForRegisterComplete() {
    // 等待加载动画消失
    await this.waitForHidden('[data-testid="loading-spinner"]')

    // 等待跳转到个人中心（注册成功后自动登录）
    await this.page.waitForURL('/user/profile', { timeout: 10000 })
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
   * 检查是否显示成功消息
   */
  async hasSuccessMessage(): Promise<boolean> {
    return await this.isVisible(this.successMessage)
  }

  /**
   * 检查是否有验证错误
   */
  async hasValidationError(): Promise<boolean> {
    const validationErrors = this.page.locator('.el-form-item__error')
    return await validationErrors.count() > 0
  }

  /**
   * 检查注册按钮是否可用
   */
  async isRegisterButtonEnabled(): Promise<boolean> {
    return await this.isEnabled(this.registerButton)
  }

  /**
   * 检查是否正在加载
   */
  async isLoading(): Promise<boolean> {
    return await this.isVisible(this.loadingSpinner)
  }

  /**
   * 检查邮箱验证码按钮是否在倒计时状态
   */
  async isEmailCodeCountdownActive(): Promise<boolean> {
    const buttonText = await this.getText(this.sendEmailCodeButton)
    return buttonText.includes('秒')
  }

  /**
   * 验证页面元素是否存在
   */
  async validatePageElements() {
    await this.waitForVisible('[data-testid="username-input"]')
    await this.waitForVisible('[data-testid="email-input"]')
    await this.waitForVisible('[data-testid="password-input"]')
    await this.waitForVisible('[data-testid="confirm-password-input"]')
    await this.waitForVisible('[data-testid="email-code-input"]')
    await this.waitForVisible('[data-testid="send-code-button"]')
    await this.waitForVisible('[data-testid="agree-terms-checkbox"]')
    await this.waitForVisible('[data-testid="register-button"]')
    await this.waitForVisible('[data-testid="login-link"]')
  }
}
