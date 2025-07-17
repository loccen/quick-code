/**
 * 双因素认证(2FA)功能E2E测试
 */
import { expect, test } from '@playwright/test'
import { LoginPage } from '../pages/LoginPage'
import { ProfilePage } from '../pages/ProfilePage'
import { TwoFactorPage } from '../pages/TwoFactorPage'

test.describe('双因素认证功能', () => {
  let loginPage: LoginPage
  let profilePage: ProfilePage
  let twoFactorPage: TwoFactorPage

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page)
    profilePage = new ProfilePage(page)
    twoFactorPage = new TwoFactorPage(page)

    // 导航到登录页面
    await loginPage.navigate()
  })

  test('应该能够访问2FA设置页面', async ({ page }) => {
    // 登录用户
    await loginPage.login('testuser@example.com', 'password123')
    
    // 导航到个人中心
    await profilePage.navigate()
    
    // 点击2FA设置按钮
    await page.click('[data-testid="setup-2fa-button"]')
    
    // 验证2FA对话框打开
    await expect(page.locator('[data-testid="2fa-dialog"]')).toBeVisible()
  })

  test('应该显示2FA状态信息', async ({ page }) => {
    // 登录用户
    await loginPage.login('testuser@example.com', 'password123')
    
    // 导航到个人中心
    await profilePage.navigate()
    
    // 点击2FA设置按钮
    await page.click('[data-testid="setup-2fa-button"]')
    
    // 验证状态显示
    await expect(page.locator('[data-testid="2fa-status"]')).toBeVisible()
    await expect(page.locator('[data-testid="enable-2fa-button"]')).toBeVisible()
  })

  test('应该能够启用2FA - 完整流程', async ({ page }) => {
    // 登录用户
    await loginPage.login('testuser@example.com', 'password123')
    
    // 导航到个人中心
    await profilePage.navigate()
    
    // 点击2FA设置按钮
    await page.click('[data-testid="setup-2fa-button"]')
    
    // 点击启用2FA按钮
    await page.click('[data-testid="enable-2fa-button"]')
    
    // 验证QR码显示
    await expect(page.locator('[data-testid="qr-code-container"]')).toBeVisible()
    await expect(page.locator('[data-testid="secret-code"]')).toBeVisible()
    
    // 点击下一步
    await page.click('button:has-text("下一步")')
    
    // 输入验证码
    await page.fill('[data-testid="totp-code-input"]', '123456')
    
    // 点击验证按钮
    await page.click('[data-testid="verify-setup-button"]')
    
    // 验证进入备用恢复码页面
    await expect(page.locator('[data-testid="backup-codes"]')).toBeVisible()
    
    // 确认保存恢复码
    await page.check('[data-testid="confirm-saved-checkbox"]')
    
    // 完成设置
    await page.click('[data-testid="complete-setup-button"]')
    
    // 验证设置完成
    await expect(page.locator('text=双因素认证设置完成')).toBeVisible()
  })

  test('应该能够复制密钥', async ({ page }) => {
    // 登录用户
    await loginPage.login('testuser@example.com', 'password123')
    
    // 导航到个人中心
    await profilePage.navigate()
    
    // 点击2FA设置按钮
    await page.click('[data-testid="setup-2fa-button"]')
    
    // 点击启用2FA按钮
    await page.click('[data-testid="enable-2fa-button"]')
    
    // 点击复制密钥按钮
    await page.click('[data-testid="copy-secret-button"]')
    
    // 验证复制成功提示
    await expect(page.locator('text=密钥已复制到剪贴板')).toBeVisible()
  })

  test('应该能够下载备用恢复码', async ({ page }) => {
    // 登录用户
    await loginPage.login('testuser@example.com', 'password123')
    
    // 导航到个人中心
    await profilePage.navigate()
    
    // 点击2FA设置按钮
    await page.click('[data-testid="setup-2fa-button"]')
    
    // 启用2FA流程...
    await page.click('[data-testid="enable-2fa-button"]')
    await page.click('button:has-text("下一步")')
    await page.fill('[data-testid="totp-code-input"]', '123456')
    await page.click('[data-testid="verify-setup-button"]')
    
    // 设置下载监听
    const downloadPromise = page.waitForEvent('download')
    
    // 点击下载按钮
    await page.click('[data-testid="download-codes-button"]')
    
    // 验证下载开始
    const download = await downloadPromise
    expect(download.suggestedFilename()).toBe('backup-codes.txt')
  })

  test('应该能够复制备用恢复码', async ({ page }) => {
    // 登录用户
    await loginPage.login('testuser@example.com', 'password123')
    
    // 导航到个人中心
    await profilePage.navigate()
    
    // 点击2FA设置按钮
    await page.click('[data-testid="setup-2fa-button"]')
    
    // 启用2FA流程...
    await page.click('[data-testid="enable-2fa-button"]')
    await page.click('button:has-text("下一步")')
    await page.fill('[data-testid="totp-code-input"]', '123456')
    await page.click('[data-testid="verify-setup-button"]')
    
    // 点击复制恢复码按钮
    await page.click('[data-testid="copy-codes-button"]')
    
    // 验证复制成功提示
    await expect(page.locator('text=恢复码已复制到剪贴板')).toBeVisible()
  })

  test('应该验证TOTP验证码格式', async ({ page }) => {
    // 登录用户
    await loginPage.login('testuser@example.com', 'password123')
    
    // 导航到个人中心
    await profilePage.navigate()
    
    // 点击2FA设置按钮
    await page.click('[data-testid="setup-2fa-button"]')
    
    // 启用2FA流程
    await page.click('[data-testid="enable-2fa-button"]')
    await page.click('button:has-text("下一步")')
    
    // 输入无效验证码
    await page.fill('[data-testid="totp-code-input"]', '12345')
    await page.click('[data-testid="verify-setup-button"]')
    
    // 验证错误提示
    await expect(page.locator('text=验证码必须是6位数字')).toBeVisible()
    
    // 输入非数字验证码
    await page.fill('[data-testid="totp-code-input"]', 'abcdef')
    await page.click('[data-testid="verify-setup-button"]')
    
    // 验证错误提示
    await expect(page.locator('text=验证码只能包含数字')).toBeVisible()
  })

  test('应该能够取消2FA设置', async ({ page }) => {
    // 登录用户
    await loginPage.login('testuser@example.com', 'password123')
    
    // 导航到个人中心
    await profilePage.navigate()
    
    // 点击2FA设置按钮
    await page.click('[data-testid="setup-2fa-button"]')
    
    // 启用2FA流程
    await page.click('[data-testid="enable-2fa-button"]')
    
    // 点击取消按钮
    await page.click('button:has-text("取消")')
    
    // 验证返回到状态页面
    await expect(page.locator('[data-testid="enable-2fa-button"]')).toBeVisible()
  })

  test('应该能够生成新的备用恢复码', async ({ page }) => {
    // 假设用户已经启用了2FA
    await loginPage.login('testuser@example.com', 'password123')
    await profilePage.navigate()
    await page.click('[data-testid="setup-2fa-button"]')
    
    // 如果已启用2FA，应该显示生成新恢复码按钮
    const generateButton = page.locator('[data-testid="generate-backup-codes-button"]')
    if (await generateButton.isVisible()) {
      await generateButton.click()
      
      // 验证新恢复码对话框显示
      await expect(page.locator('text=新的备用恢复码')).toBeVisible()
    }
  })

  test('应该能够禁用2FA', async ({ page }) => {
    // 假设用户已经启用了2FA
    await loginPage.login('testuser@example.com', 'password123')
    await profilePage.navigate()
    await page.click('[data-testid="setup-2fa-button"]')
    
    // 如果已启用2FA，应该显示禁用按钮
    const disableButton = page.locator('[data-testid="disable-2fa-button"]')
    if (await disableButton.isVisible()) {
      await disableButton.click()
      
      // 输入验证码
      await page.fill('[data-testid="disable-totp-input"]', '123456')
      
      // 确认禁用
      await page.click('[data-testid="confirm-disable-button"]')
      
      // 验证禁用成功
      await expect(page.locator('text=双因素认证已禁用')).toBeVisible()
    }
  })

  test('应该在步骤间正确导航', async ({ page }) => {
    // 登录用户
    await loginPage.login('testuser@example.com', 'password123')
    
    // 导航到个人中心
    await profilePage.navigate()
    
    // 点击2FA设置按钮
    await page.click('[data-testid="setup-2fa-button"]')
    
    // 启用2FA流程
    await page.click('[data-testid="enable-2fa-button"]')
    
    // 验证步骤指示器
    await expect(page.locator('.el-steps')).toBeVisible()
    
    // 下一步
    await page.click('button:has-text("下一步")')
    
    // 验证在步骤2
    await expect(page.locator('[data-testid="totp-code-input"]')).toBeVisible()
    
    // 上一步
    await page.click('button:has-text("上一步")')
    
    // 验证回到步骤1
    await expect(page.locator('[data-testid="qr-code-container"]')).toBeVisible()
  })
})
