/**
 * 双因素认证页面对象模型
 */
import { Locator, Page } from '@playwright/test'
import { BasePage } from './BasePage'

export class TwoFactorPage extends BasePage {
  // 页面元素选择器
  private readonly dialog: Locator
  private readonly statusSection: Locator
  private readonly enableButton: Locator
  private readonly disableButton: Locator
  private readonly qrCodeContainer: Locator
  private readonly secretCode: Locator
  private readonly copySecretButton: Locator
  private readonly totpCodeInput: Locator
  private readonly verifySetupButton: Locator
  private readonly backupCodes: Locator
  private readonly downloadCodesButton: Locator
  private readonly copyCodesButton: Locator
  private readonly confirmSavedCheckbox: Locator
  private readonly completeSetupButton: Locator
  private readonly generateBackupCodesButton: Locator
  private readonly disableTotpInput: Locator
  private readonly confirmDisableButton: Locator
  private readonly nextButton: Locator
  private readonly prevButton: Locator
  private readonly cancelButton: Locator

  constructor(page: Page) {
    super(page)

    // 初始化页面元素
    this.dialog = page.locator('[data-testid="2fa-dialog"]')
    this.statusSection = page.locator('[data-testid="2fa-status"]')
    this.enableButton = page.locator('[data-testid="enable-2fa-button"]')
    this.disableButton = page.locator('[data-testid="disable-2fa-button"]')
    this.qrCodeContainer = page.locator('[data-testid="qr-code-container"]')
    this.secretCode = page.locator('[data-testid="secret-code"]')
    this.copySecretButton = page.locator('[data-testid="copy-secret-button"]')
    this.totpCodeInput = page.locator('[data-testid="totp-code-input"]')
    this.verifySetupButton = page.locator('[data-testid="verify-setup-button"]')
    this.backupCodes = page.locator('[data-testid="backup-codes"]')
    this.downloadCodesButton = page.locator('[data-testid="download-codes-button"]')
    this.copyCodesButton = page.locator('[data-testid="copy-codes-button"]')
    this.confirmSavedCheckbox = page.locator('[data-testid="confirm-saved-checkbox"]')
    this.completeSetupButton = page.locator('[data-testid="complete-setup-button"]')
    this.generateBackupCodesButton = page.locator('[data-testid="generate-backup-codes-button"]')
    this.disableTotpInput = page.locator('[data-testid="disable-totp-input"]')
    this.confirmDisableButton = page.locator('[data-testid="confirm-disable-button"]')
    this.nextButton = page.locator('button:has-text("下一步")')
    this.prevButton = page.locator('button:has-text("上一步")')
    this.cancelButton = page.locator('button:has-text("取消")')
  }

  /**
   * 打开2FA设置对话框
   */
  async openDialog() {
    await this.click(page.locator('[data-testid="setup-2fa-button"]'))
    await this.waitForVisible('[data-testid="2fa-dialog"]')
  }

  /**
   * 启用2FA
   */
  async enableTwoFactor() {
    await this.click(this.enableButton)
    await this.waitForVisible('[data-testid="qr-code-container"]')
  }

  /**
   * 进入下一步
   */
  async goToNextStep() {
    await this.click(this.nextButton)
  }

  /**
   * 返回上一步
   */
  async goToPreviousStep() {
    await this.click(this.prevButton)
  }

  /**
   * 输入TOTP验证码
   */
  async enterTotpCode(code: string) {
    await this.fill(this.totpCodeInput, code)
  }

  /**
   * 验证设置
   */
  async verifySetup() {
    await this.click(this.verifySetupButton)
  }

  /**
   * 复制密钥
   */
  async copySecret() {
    await this.click(this.copySecretButton)
  }

  /**
   * 下载备用恢复码
   */
  async downloadBackupCodes() {
    const downloadPromise = this.page.waitForEvent('download')
    await this.click(this.downloadCodesButton)
    return await downloadPromise
  }

  /**
   * 复制备用恢复码
   */
  async copyBackupCodes() {
    await this.click(this.copyCodesButton)
  }

  /**
   * 确认已保存恢复码
   */
  async confirmSaved() {
    await this.check(this.confirmSavedCheckbox)
  }

  /**
   * 完成设置
   */
  async completeSetup() {
    await this.click(this.completeSetupButton)
  }

  /**
   * 生成新的备用恢复码
   */
  async generateNewBackupCodes() {
    await this.click(this.generateBackupCodesButton)
  }

  /**
   * 禁用2FA
   */
  async disableTwoFactor(totpCode: string) {
    await this.click(this.disableButton)
    await this.fill(this.disableTotpInput, totpCode)
    await this.click(this.confirmDisableButton)
  }

  /**
   * 取消设置
   */
  async cancelSetup() {
    await this.click(this.cancelButton)
  }

  /**
   * 完整的2FA启用流程
   */
  async enableTwoFactorComplete(totpCode: string = '123456') {
    // 启用2FA
    await this.enableTwoFactor()
    
    // 进入验证步骤
    await this.goToNextStep()
    
    // 输入验证码
    await this.enterTotpCode(totpCode)
    
    // 验证设置
    await this.verifySetup()
    
    // 确认保存恢复码
    await this.confirmSaved()
    
    // 完成设置
    await this.completeSetup()
  }

  /**
   * 验证QR码是否显示
   */
  async isQrCodeVisible(): Promise<boolean> {
    return await this.isVisible(this.qrCodeContainer)
  }

  /**
   * 验证密钥是否显示
   */
  async isSecretCodeVisible(): Promise<boolean> {
    return await this.isVisible(this.secretCode)
  }

  /**
   * 验证备用恢复码是否显示
   */
  async areBackupCodesVisible(): Promise<boolean> {
    return await this.isVisible(this.backupCodes)
  }

  /**
   * 获取密钥文本
   */
  async getSecretCode(): Promise<string> {
    return await this.getText(this.secretCode)
  }

  /**
   * 验证2FA状态
   */
  async getTwoFactorStatus(): Promise<'enabled' | 'disabled'> {
    const enableButtonVisible = await this.isVisible(this.enableButton)
    const disableButtonVisible = await this.isVisible(this.disableButton)
    
    if (disableButtonVisible) {
      return 'enabled'
    } else if (enableButtonVisible) {
      return 'disabled'
    } else {
      throw new Error('无法确定2FA状态')
    }
  }

  /**
   * 验证步骤指示器
   */
  async getCurrentStep(): Promise<number> {
    const steps = this.page.locator('.el-steps .el-step')
    const activeStep = this.page.locator('.el-steps .el-step.is-process')
    
    const totalSteps = await steps.count()
    const activeIndex = await activeStep.first().getAttribute('data-step-index')
    
    return activeIndex ? parseInt(activeIndex) + 1 : 1
  }

  /**
   * 验证表单验证错误
   */
  async getValidationError(): Promise<string | null> {
    const errorElement = this.page.locator('.el-form-item__error')
    if (await errorElement.isVisible()) {
      return await errorElement.textContent()
    }
    return null
  }

  /**
   * 验证成功消息
   */
  async getSuccessMessage(): Promise<string | null> {
    const successElement = this.page.locator('.el-message--success')
    if (await successElement.isVisible()) {
      return await successElement.textContent()
    }
    return null
  }

  /**
   * 验证错误消息
   */
  async getErrorMessage(): Promise<string | null> {
    const errorElement = this.page.locator('.el-message--error')
    if (await errorElement.isVisible()) {
      return await errorElement.textContent()
    }
    return null
  }

  /**
   * 等待对话框关闭
   */
  async waitForDialogClose() {
    await this.waitForHidden('[data-testid="2fa-dialog"]')
  }

  /**
   * 验证页面元素
   */
  async validateTwoFactorElements() {
    await this.waitForVisible('[data-testid="2fa-dialog"]')
    await this.waitForVisible('[data-testid="2fa-status"]')
  }
}
