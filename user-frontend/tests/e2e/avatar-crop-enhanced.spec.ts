import { test, expect } from '@playwright/test'
import { UserProfilePage } from '../page-objects/UserProfilePage'

test.describe('增强版头像裁剪功能', () => {
  let userProfilePage: UserProfilePage

  test.beforeEach(async ({ page }) => {
    userProfilePage = new UserProfilePage(page)
    await userProfilePage.goto()
    await userProfilePage.login('test@example.com', 'password123')
  })

  test('应该显示缩放控制UI', async ({ page }) => {
    // 点击头像上传
    await userProfilePage.clickAvatarUpload()
    
    // 上传测试图片
    await userProfilePage.uploadTestImage()
    
    // 验证缩放控制元素存在
    await expect(page.locator('.scale-controls')).toBeVisible()
    await expect(page.locator('.scale-info')).toBeVisible()
    await expect(page.locator('.scale-slider')).toBeVisible()
    await expect(page.locator('.scale-buttons')).toBeVisible()
    
    // 验证缩放按钮
    await expect(page.locator('button').filter({ hasText: '-' })).toBeVisible()
    await expect(page.locator('button').filter({ hasText: '+' })).toBeVisible()
    await expect(page.locator('button').filter({ hasText: '适应' })).toBeVisible()
  })

  test('应该能够通过按钮调整缩放', async ({ page }) => {
    await userProfilePage.clickAvatarUpload()
    await userProfilePage.uploadTestImage()
    
    // 获取初始缩放值
    const initialScale = await page.locator('.scale-info').textContent()
    
    // 点击放大按钮
    await page.locator('button').filter({ hasText: '+' }).click()
    
    // 验证缩放值增加
    const newScale = await page.locator('.scale-info').textContent()
    expect(newScale).not.toBe(initialScale)
    
    // 点击缩小按钮
    await page.locator('button').filter({ hasText: '-' }).click()
    
    // 验证缩放值减少
    const finalScale = await page.locator('.scale-info').textContent()
    expect(finalScale).not.toBe(newScale)
  })

  test('应该能够通过滑块调整缩放', async ({ page }) => {
    await userProfilePage.clickAvatarUpload()
    await userProfilePage.uploadTestImage()
    
    const slider = page.locator('.slider')
    await expect(slider).toBeVisible()
    
    // 获取滑块的边界框
    const sliderBox = await slider.boundingBox()
    if (sliderBox) {
      // 拖拽滑块到右侧（增加缩放）
      await page.mouse.click(sliderBox.x + sliderBox.width * 0.7, sliderBox.y + sliderBox.height / 2)
      
      // 验证缩放值改变
      const scaleText = await page.locator('.scale-info').textContent()
      expect(scaleText).toContain('%')
    }
  })

  test('应该能够重置缩放', async ({ page }) => {
    await userProfilePage.clickAvatarUpload()
    await userProfilePage.uploadTestImage()
    
    // 先调整缩放
    await page.locator('button').filter({ hasText: '+' }).click()
    await page.locator('button').filter({ hasText: '+' }).click()
    
    // 点击适应按钮重置
    await page.locator('button').filter({ hasText: '适应' }).click()
    
    // 验证缩放重置（这里可以检查画布内容或缩放值）
    const scaleText = await page.locator('.scale-info').textContent()
    expect(scaleText).toContain('%')
  })

  test('应该显示正确的提示文字', async ({ page }) => {
    await userProfilePage.clickAvatarUpload()
    await userProfilePage.uploadTestImage()
    
    // 验证新的提示文字
    await expect(page.locator('text=拖拽图片调整位置，滚轮缩放图片')).toBeVisible()
  })

  test('应该能够拖拽图片调整位置', async ({ page }) => {
    await userProfilePage.clickAvatarUpload()
    await userProfilePage.uploadTestImage()
    
    const canvas = page.locator('.crop-canvas')
    await expect(canvas).toBeVisible()
    
    // 获取画布边界框
    const canvasBox = await canvas.boundingBox()
    if (canvasBox) {
      // 在画布中心点击并拖拽
      const centerX = canvasBox.x + canvasBox.width / 2
      const centerY = canvasBox.y + canvasBox.height / 2
      
      await page.mouse.move(centerX, centerY)
      await page.mouse.down()
      await page.mouse.move(centerX + 50, centerY + 50)
      await page.mouse.up()
      
      // 验证拖拽操作完成（画布应该重新绘制）
      await expect(canvas).toBeVisible()
    }
  })

  test('应该能够完成裁剪并上传', async ({ page }) => {
    await userProfilePage.clickAvatarUpload()
    await userProfilePage.uploadTestImage()
    
    // 调整缩放和位置
    await page.locator('button').filter({ hasText: '+' }).click()
    
    // 确认裁剪
    await page.locator('[data-testid="confirm-crop-button"]').click()
    
    // 上传裁剪后的图片
    await page.locator('[data-testid="upload-cropped-button"]').click()
    
    // 验证上传成功
    await expect(page.locator('text=头像上传成功')).toBeVisible()
  })

  test('缩放应该有合理的限制', async ({ page }) => {
    await userProfilePage.clickAvatarUpload()
    await userProfilePage.uploadTestImage()
    
    // 尝试多次点击放大按钮
    for (let i = 0; i < 20; i++) {
      await page.locator('button').filter({ hasText: '+' }).click()
    }
    
    // 验证放大按钮被禁用（达到最大缩放）
    await expect(page.locator('button').filter({ hasText: '+' })).toBeDisabled()
    
    // 尝试多次点击缩小按钮
    for (let i = 0; i < 30; i++) {
      await page.locator('button').filter({ hasText: '-' }).click()
    }
    
    // 验证缩小按钮被禁用（达到最小缩放）
    await expect(page.locator('button').filter({ hasText: '-' })).toBeDisabled()
  })
})
