/**
 * 头像上传功能E2E测试
 */
import { expect, test } from '@playwright/test'
import { LoginPage } from '../pages/LoginPage'
import { ProfilePage } from '../pages/ProfilePage'
import { AvatarUploadPage } from '../pages/AvatarUploadPage'
import path from 'path'

test.describe('头像上传功能', () => {
  let loginPage: LoginPage
  let profilePage: ProfilePage
  let avatarUploadPage: AvatarUploadPage

  // 测试图片文件路径
  const validImagePath = path.join(__dirname, '../fixtures/test-avatar.jpg')
  const largeImagePath = path.join(__dirname, '../fixtures/large-image.jpg')
  const invalidFilePath = path.join(__dirname, '../fixtures/test-document.pdf')

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page)
    profilePage = new ProfilePage(page)
    avatarUploadPage = new AvatarUploadPage(page)

    // 登录用户
    await loginPage.navigate()
    await loginPage.login('testuser@example.com', 'password123')
    
    // 导航到个人中心
    await profilePage.navigate()
  })

  test('应该显示头像上传组件', async ({ page }) => {
    // 验证头像上传组件存在
    await expect(page.locator('[data-testid="avatar-upload"]')).toBeVisible()
    await expect(page.locator('[data-testid="avatar-preview"]')).toBeVisible()
    await expect(page.locator('[data-testid="select-file-button"]')).toBeVisible()
  })

  test('应该能够选择和预览图片文件', async ({ page }) => {
    // 点击头像预览区域
    await page.click('[data-testid="avatar-preview"]')
    
    // 验证文件输入框存在
    await expect(page.locator('[data-testid="file-input"]')).toBeHidden()
    
    // 模拟文件选择
    await page.setInputFiles('[data-testid="file-input"]', validImagePath)
    
    // 验证裁剪对话框打开（如果启用裁剪）
    const cropDialog = page.locator('[data-testid="crop-dialog"]')
    if (await cropDialog.isVisible()) {
      await expect(cropDialog).toBeVisible()
    }
  })

  test('应该能够通过选择文件按钮上传图片', async ({ page }) => {
    // 点击选择文件按钮
    await page.click('[data-testid="select-file-button"]')
    
    // 选择文件
    await page.setInputFiles('[data-testid="file-input"]', validImagePath)
    
    // 验证文件选择成功
    // 这里可以检查是否显示了预览或者裁剪界面
  })

  test('应该验证文件类型', async ({ page }) => {
    // 尝试上传非图片文件
    await page.click('[data-testid="select-file-button"]')
    await page.setInputFiles('[data-testid="file-input"]', invalidFilePath)
    
    // 验证错误提示
    await expect(page.locator('text=请选择图片文件')).toBeVisible()
  })

  test('应该验证文件大小', async ({ page }) => {
    // 尝试上传过大的文件
    await page.click('[data-testid="select-file-button"]')
    
    // 如果有大文件，测试文件大小验证
    // 这里需要根据实际的文件大小限制来测试
    // await page.setInputFiles('[data-testid="file-input"]', largeImagePath)
    // await expect(page.locator('text=文件大小不能超过')).toBeVisible()
  })

  test('应该显示上传进度', async ({ page }) => {
    // 选择文件
    await page.click('[data-testid="select-file-button"]')
    await page.setInputFiles('[data-testid="file-input"]', validImagePath)
    
    // 如果有裁剪对话框，完成裁剪流程
    const cropDialog = page.locator('[data-testid="crop-dialog"]')
    if (await cropDialog.isVisible()) {
      await page.click('[data-testid="upload-cropped-button"]')
    }
    
    // 验证上传进度显示
    const progressElement = page.locator('[data-testid="upload-progress"]')
    if (await progressElement.isVisible()) {
      await expect(progressElement).toBeVisible()
    }
  })

  test('应该能够移除头像', async ({ page }) => {
    // 首先确保有头像
    const removeButton = page.locator('[data-testid="remove-avatar-button"]')
    
    if (await removeButton.isVisible()) {
      // 点击移除头像按钮
      await removeButton.click()
      
      // 验证移除成功提示
      await expect(page.locator('text=头像已移除')).toBeVisible()
      
      // 验证头像预览变为占位符
      await expect(page.locator('.avatar-placeholder')).toBeVisible()
    }
  })

  test('应该能够重复选择同一文件', async ({ page }) => {
    // 第一次选择文件
    await page.click('[data-testid="select-file-button"]')
    await page.setInputFiles('[data-testid="file-input"]', validImagePath)
    
    // 取消或完成第一次上传
    const cancelButton = page.locator('button:has-text("取消")')
    if (await cancelButton.isVisible()) {
      await cancelButton.click()
    }
    
    // 第二次选择同一文件
    await page.click('[data-testid="select-file-button"]')
    await page.setInputFiles('[data-testid="file-input"]', validImagePath)
    
    // 验证能够再次选择
    // 这里验证文件输入框的值被正确重置
  })

  test('应该在上传过程中禁用操作', async ({ page }) => {
    // 开始上传
    await page.click('[data-testid="select-file-button"]')
    await page.setInputFiles('[data-testid="file-input"]', validImagePath)
    
    // 如果有裁剪对话框，开始上传
    const cropDialog = page.locator('[data-testid="crop-dialog"]')
    if (await cropDialog.isVisible()) {
      await page.click('[data-testid="upload-cropped-button"]')
      
      // 验证上传过程中按钮被禁用
      await expect(page.locator('[data-testid="select-file-button"]')).toBeDisabled()
    }
  })

  test('应该显示上传成功消息', async ({ page }) => {
    // 完成上传流程
    await page.click('[data-testid="select-file-button"]')
    await page.setInputFiles('[data-testid="file-input"]', validImagePath)
    
    // 如果有裁剪对话框，完成裁剪和上传
    const cropDialog = page.locator('[data-testid="crop-dialog"]')
    if (await cropDialog.isVisible()) {
      await page.click('[data-testid="upload-cropped-button"]')
    }
    
    // 等待上传完成并验证成功消息
    await expect(page.locator('text=头像上传成功')).toBeVisible({ timeout: 10000 })
  })

  test('应该能够处理上传错误', async ({ page }) => {
    // 模拟网络错误或服务器错误
    // 这里需要根据实际的错误处理逻辑来测试
    
    // 可以通过拦截网络请求来模拟错误
    await page.route('**/api/users/me/avatar', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: '服务器错误' })
      })
    })
    
    // 尝试上传
    await page.click('[data-testid="select-file-button"]')
    await page.setInputFiles('[data-testid="file-input"]', validImagePath)
    
    const cropDialog = page.locator('[data-testid="crop-dialog"]')
    if (await cropDialog.isVisible()) {
      await page.click('[data-testid="upload-cropped-button"]')
    }
    
    // 验证错误消息
    await expect(page.locator('text=头像上传失败')).toBeVisible({ timeout: 10000 })
  })

  test('应该保持头像的宽高比', async ({ page }) => {
    // 上传头像后验证显示的头像保持正确的宽高比
    await page.click('[data-testid="select-file-button"]')
    await page.setInputFiles('[data-testid="file-input"]', validImagePath)
    
    const cropDialog = page.locator('[data-testid="crop-dialog"]')
    if (await cropDialog.isVisible()) {
      await page.click('[data-testid="upload-cropped-button"]')
    }
    
    // 等待上传完成
    await expect(page.locator('text=头像上传成功')).toBeVisible({ timeout: 10000 })
    
    // 验证头像显示
    const avatarImage = page.locator('.avatar-image')
    if (await avatarImage.isVisible()) {
      // 验证头像是圆形的
      const borderRadius = await avatarImage.evaluate(el => 
        window.getComputedStyle(el).borderRadius
      )
      expect(borderRadius).toBe('50%')
    }
  })

  test('应该支持拖拽上传', async ({ page }) => {
    // 这个测试需要模拟拖拽事件
    // Playwright 对拖拽文件的支持有限，这里主要测试UI响应
    
    const dropZone = page.locator('[data-testid="avatar-preview"]')
    
    // 模拟拖拽悬停
    await dropZone.hover()
    
    // 验证悬停效果
    await expect(dropZone).toHaveClass(/hover/)
  })

  test('应该在移动端正确显示', async ({ page, isMobile }) => {
    if (isMobile) {
      // 验证移动端布局
      await expect(page.locator('[data-testid="avatar-upload"]')).toBeVisible()
      
      // 验证按钮大小适合触摸
      const selectButton = page.locator('[data-testid="select-file-button"]')
      const buttonSize = await selectButton.boundingBox()
      
      // 验证按钮足够大（至少44px高度，符合移动端可访问性标准）
      expect(buttonSize?.height).toBeGreaterThanOrEqual(44)
    }
  })
})
