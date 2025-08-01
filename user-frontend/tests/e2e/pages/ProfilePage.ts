/**
 * 个人中心页面对象模型
 */
import { Locator, Page } from '@playwright/test'
import { BasePage } from './BasePage'

export class ProfilePage extends BasePage {
  // 页面元素选择器
  private readonly avatarUpload: Locator
  private readonly nicknameInput: Locator
  private readonly emailInput: Locator
  private readonly phoneInput: Locator
  private readonly bioTextarea: Locator
  private readonly saveButton: Locator
  private readonly cancelButton: Locator
  private readonly changePasswordButton: Locator
  private readonly currentPasswordInput: Locator
  private readonly newPasswordInput: Locator
  private readonly confirmPasswordInput: Locator
  private readonly submitPasswordButton: Locator
  private readonly editProfileButton: Locator
  private readonly usernameInput: Locator
  private readonly birthdayInput: Locator
  private readonly saveProfileButton: Locator
  private readonly cancelEditButton: Locator
  private readonly confirmChangePasswordButton: Locator
  private readonly setup2FAButton: Locator
  private readonly pointsBalance: Locator
  private readonly rechargePointsButton: Locator
  private readonly pointsHistoryButton: Locator
  private readonly totalProjects: Locator
  private readonly totalDownloads: Locator
  private readonly totalUploads: Locator
  private readonly memberDays: Locator
  private readonly userNickname: Locator

  constructor(page: Page) {
    super(page)

    // 初始化页面元素
    this.avatarUpload = page.locator('[data-testid="avatar-upload"]')
    this.nicknameInput = page.locator('[data-testid="nickname-input"]')
    this.emailInput = page.locator('[data-testid="email-input"]')
    this.phoneInput = page.locator('[data-testid="phone-input"]')
    this.bioTextarea = page.locator('[data-testid="bio-textarea"]')
    this.saveButton = page.locator('[data-testid="save-button"]')
    this.cancelButton = page.locator('[data-testid="cancel-button"]')
    this.changePasswordButton = page.locator('[data-testid="change-password-button"]')
    this.currentPasswordInput = page.locator('[data-testid="current-password-input"]')
    this.newPasswordInput = page.locator('[data-testid="new-password-input"]')
    this.confirmPasswordInput = page.locator('[data-testid="confirm-password-input"]')
    this.submitPasswordButton = page.locator('[data-testid="submit-password-button"]')
    this.editProfileButton = page.locator('[data-testid="edit-profile-button"]')
    this.usernameInput = page.locator('[data-testid="username-input"]')
    this.birthdayInput = page.locator('[data-testid="birthday-input"]')
    this.saveProfileButton = page.locator('[data-testid="save-profile-button"]')
    this.cancelEditButton = page.locator('[data-testid="cancel-edit-button"]')
    this.confirmChangePasswordButton = page.locator('[data-testid="confirm-change-password-button"]')
    this.setup2FAButton = page.locator('[data-testid="setup-2fa-button"]')
    this.pointsBalance = page.locator('[data-testid="points-balance"]')
    this.rechargePointsButton = page.locator('[data-testid="recharge-points-button"]')
    this.pointsHistoryButton = page.locator('[data-testid="points-history-button"]')
    this.totalProjects = page.locator('[data-testid="total-projects"]')
    this.totalDownloads = page.locator('[data-testid="total-downloads"]')
    this.totalUploads = page.locator('[data-testid="total-uploads"]')
    this.memberDays = page.locator('[data-testid="member-days"]')
    this.userNickname = page.locator('[data-testid="user-nickname"]')
  }

  /**
   * 导航到个人中心页面
   */
  async navigate() {
    await this.goto('/user/profile')
    await this.waitForLoad()
  }

  /**
   * 更新昵称
   */
  async updateNickname(nickname: string) {
    await this.clearAndFill(this.nicknameInput, nickname)
  }

  /**
   * 更新手机号
   */
  async updatePhone(phone: string) {
    await this.clearAndFill(this.phoneInput, phone)
  }

  /**
   * 更新个人简介
   */
  async updateBio(bio: string) {
    await this.clearAndFill(this.bioTextarea, bio)
  }

  /**
   * 保存个人信息
   */
  async saveProfile() {
    await this.click(this.saveButton)
    await this.waitForNetworkIdle()
  }

  /**
   * 取消编辑
   */
  async cancelEdit() {
    await this.click(this.cancelButton)
  }

  /**
   * 点击修改密码按钮
   */
  async clickChangePassword() {
    await this.click(this.changePasswordButton)
  }

  /**
   * 修改密码
   */
  async changePassword(currentPassword: string, newPassword: string, confirmPassword: string) {
    await this.fill(this.currentPasswordInput, currentPassword)
    await this.fill(this.newPasswordInput, newPassword)
    await this.fill(this.confirmPasswordInput, confirmPassword)
    await this.click(this.submitPasswordButton)
    await this.waitForNetworkIdle()
  }

  /**
   * 上传头像
   */
  async uploadAvatar(filePath: string) {
    await this.page.setInputFiles('[data-testid="avatar-upload"] input[type="file"]', filePath)
    await this.waitForNetworkIdle()
  }

  /**
   * 获取当前昵称
   */
  async getNickname(): Promise<string> {
    return await this.getAttribute(this.nicknameInput, 'value') || ''
  }

  /**
   * 获取当前邮箱
   */
  async getEmail(): Promise<string> {
    return await this.getAttribute(this.emailInput, 'value') || ''
  }

  /**
   * 获取当前手机号
   */
  async getPhone(): Promise<string> {
    return await this.getAttribute(this.phoneInput, 'value') || ''
  }

  /**
   * 获取当前个人简介
   */
  async getBio(): Promise<string> {
    return await this.getAttribute(this.bioTextarea, 'value') || ''
  }

  /**
   * 检查保存按钮是否可用
   */
  async isSaveButtonEnabled(): Promise<boolean> {
    return await this.isEnabled(this.saveButton)
  }

  /**
   * 验证个人中心页面元素
   */
  async validateProfileElements() {
    await this.waitForVisible('[data-testid="avatar-upload"]')
    await this.waitForVisible('[data-testid="nickname-input"]')
    await this.waitForVisible('[data-testid="email-input"]')
    await this.waitForVisible('[data-testid="phone-input"]')
    await this.waitForVisible('[data-testid="bio-textarea"]')
    await this.waitForVisible('[data-testid="save-button"]')
    await this.waitForVisible('[data-testid="change-password-button"]')
  }
}
