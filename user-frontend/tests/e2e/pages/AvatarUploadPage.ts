/**
 * 头像上传页面对象模型
 */
import { Locator, Page } from '@playwright/test'
import { BasePage } from './BasePage'

export class AvatarUploadPage extends BasePage {
  // 页面元素选择器
  private readonly avatarUpload: Locator
  private readonly avatarPreview: Locator
  private readonly fileInput: Locator
  private readonly selectFileButton: Locator
  private readonly removeAvatarButton: Locator
  private readonly uploadProgress: Locator
  private readonly cropDialog: Locator
  private readonly cropCanvas: Locator
  private readonly confirmCropButton: Locator
  private readonly uploadCroppedButton: Locator
  private readonly resetCropButton: Locator
  private readonly avatarImage: Locator
  private readonly avatarPlaceholder: Locator
  private readonly uploadTips: Locator

  constructor(page: Page) {
    super(page)

    // 初始化页面元素
    this.avatarUpload = page.locator('[data-testid="avatar-upload"]')
    this.avatarPreview = page.locator('[data-testid="avatar-preview"]')
    this.fileInput = page.locator('[data-testid="file-input"]')
    this.selectFileButton = page.locator('[data-testid="select-file-button"]')
    this.removeAvatarButton = page.locator('[data-testid="remove-avatar-button"]')
    this.uploadProgress = page.locator('[data-testid="upload-progress"]')
    this.cropDialog = page.locator('[data-testid="crop-dialog"]')
    this.cropCanvas = page.locator('.crop-canvas')
    this.confirmCropButton = page.locator('[data-testid="confirm-crop-button"]')
    this.uploadCroppedButton = page.locator('[data-testid="upload-cropped-button"]')
    this.resetCropButton = page.locator('button:has-text("重置")')
    this.avatarImage = page.locator('.avatar-image')
    this.avatarPlaceholder = page.locator('.avatar-placeholder')
    this.uploadTips = page.locator('.upload-tips')
  }

  /**
   * 点击头像预览区域
   */
  async clickAvatarPreview() {
    await this.click(this.avatarPreview)
  }

  /**
   * 点击选择文件按钮
   */
  async clickSelectFile() {
    await this.click(this.selectFileButton)
  }

  /**
   * 选择文件
   */
  async selectFile(filePath: string) {
    await this.page.setInputFiles(this.fileInput, filePath)
  }

  /**
   * 通过预览区域选择文件
   */
  async selectFileByPreview(filePath: string) {
    await this.clickAvatarPreview()
    await this.selectFile(filePath)
  }

  /**
   * 通过按钮选择文件
   */
  async selectFileByButton(filePath: string) {
    await this.clickSelectFile()
    await this.selectFile(filePath)
  }

  /**
   * 移除头像
   */
  async removeAvatar() {
    await this.click(this.removeAvatarButton)
  }

  /**
   * 确认裁剪
   */
  async confirmCrop() {
    await this.click(this.confirmCropButton)
  }

  /**
   * 上传裁剪后的图片
   */
  async uploadCroppedImage() {
    await this.click(this.uploadCroppedButton)
  }

  /**
   * 重置裁剪
   */
  async resetCrop() {
    await this.click(this.resetCropButton)
  }

  /**
   * 取消裁剪对话框
   */
  async cancelCrop() {
    await this.click(page.locator('button:has-text("取消")'))
  }

  /**
   * 完整的头像上传流程
   */
  async uploadAvatar(filePath: string, useCrop: boolean = true) {
    // 选择文件
    await this.selectFileByButton(filePath)
    
    // 如果启用裁剪且对话框显示
    if (useCrop && await this.isCropDialogVisible()) {
      // 确认裁剪并上传
      await this.uploadCroppedImage()
    }
    
    // 等待上传完成
    await this.waitForUploadComplete()
  }

  /**
   * 等待上传完成
   */
  async waitForUploadComplete() {
    // 等待进度条消失或成功消息出现
    await Promise.race([
      this.waitForHidden('[data-testid="upload-progress"]'),
      this.waitForVisible('text=头像上传成功')
    ])
  }

  /**
   * 检查裁剪对话框是否可见
   */
  async isCropDialogVisible(): Promise<boolean> {
    return await this.isVisible(this.cropDialog)
  }

  /**
   * 检查头像是否已上传
   */
  async hasAvatar(): Promise<boolean> {
    return await this.isVisible(this.avatarImage)
  }

  /**
   * 检查是否显示头像占位符
   */
  async hasAvatarPlaceholder(): Promise<boolean> {
    return await this.isVisible(this.avatarPlaceholder)
  }

  /**
   * 检查移除按钮是否可见
   */
  async isRemoveButtonVisible(): Promise<boolean> {
    return await this.isVisible(this.removeAvatarButton)
  }

  /**
   * 检查上传进度是否显示
   */
  async isUploadProgressVisible(): Promise<boolean> {
    return await this.isVisible(this.uploadProgress)
  }

  /**
   * 获取上传进度百分比
   */
  async getUploadProgress(): Promise<number> {
    const progressText = await this.getText(page.locator('.progress-text'))
    const match = progressText.match(/(\d+)%/)
    return match ? parseInt(match[1]) : 0
  }

  /**
   * 检查选择文件按钮是否被禁用
   */
  async isSelectFileButtonDisabled(): Promise<boolean> {
    return await this.isDisabled(this.selectFileButton)
  }

  /**
   * 获取头像图片URL
   */
  async getAvatarUrl(): Promise<string | null> {
    if (await this.hasAvatar()) {
      return await this.getAttribute(this.avatarImage, 'src')
    }
    return null
  }

  /**
   * 验证文件类型错误消息
   */
  async getFileTypeError(): Promise<string | null> {
    const errorElement = this.page.locator('text=请选择图片文件')
    if (await errorElement.isVisible()) {
      return await errorElement.textContent()
    }
    return null
  }

  /**
   * 验证文件大小错误消息
   */
  async getFileSizeError(): Promise<string | null> {
    const errorElement = this.page.locator('text*=文件大小不能超过')
    if (await errorElement.isVisible()) {
      return await errorElement.textContent()
    }
    return null
  }

  /**
   * 验证上传成功消息
   */
  async getSuccessMessage(): Promise<string | null> {
    const successElement = this.page.locator('text=头像上传成功')
    if (await successElement.isVisible()) {
      return await successElement.textContent()
    }
    return null
  }

  /**
   * 验证上传失败消息
   */
  async getErrorMessage(): Promise<string | null> {
    const errorElement = this.page.locator('text=头像上传失败')
    if (await errorElement.isVisible()) {
      return await errorElement.textContent()
    }
    return null
  }

  /**
   * 验证移除成功消息
   */
  async getRemoveSuccessMessage(): Promise<string | null> {
    const successElement = this.page.locator('text=头像已移除')
    if (await successElement.isVisible()) {
      return await successElement.textContent()
    }
    return null
  }

  /**
   * 获取上传提示文本
   */
  async getUploadTips(): Promise<string[]> {
    const tips = await this.uploadTips.locator('.tip-text').allTextContents()
    return tips
  }

  /**
   * 验证头像尺寸
   */
  async getAvatarSize(): Promise<{ width: number; height: number } | null> {
    if (await this.hasAvatar()) {
      const boundingBox = await this.avatarImage.boundingBox()
      if (boundingBox) {
        return {
          width: boundingBox.width,
          height: boundingBox.height
        }
      }
    }
    return null
  }

  /**
   * 验证头像是否为圆形
   */
  async isAvatarCircular(): Promise<boolean> {
    if (await this.hasAvatar()) {
      const borderRadius = await this.avatarImage.evaluate(el => 
        window.getComputedStyle(el).borderRadius
      )
      return borderRadius === '50%'
    }
    return false
  }

  /**
   * 模拟拖拽文件到预览区域
   */
  async dragFileToPreview(filePath: string) {
    // Playwright 对拖拽文件的支持有限
    // 这里主要测试UI响应
    await this.avatarPreview.hover()
    
    // 可以添加更多拖拽相关的测试逻辑
  }

  /**
   * 验证悬停效果
   */
  async hasHoverEffect(): Promise<boolean> {
    await this.avatarPreview.hover()
    
    // 检查是否有悬停样式类
    const className = await this.avatarPreview.getAttribute('class')
    return className?.includes('hover') || false
  }

  /**
   * 验证头像上传组件的所有元素
   */
  async validateAvatarUploadElements() {
    await this.waitForVisible('[data-testid="avatar-upload"]')
    await this.waitForVisible('[data-testid="avatar-preview"]')
    await this.waitForVisible('[data-testid="select-file-button"]')
    
    // 验证提示文本
    await this.waitForVisible('.upload-tips')
  }

  /**
   * 等待裁剪对话框关闭
   */
  async waitForCropDialogClose() {
    await this.waitForHidden('[data-testid="crop-dialog"]')
  }
}
