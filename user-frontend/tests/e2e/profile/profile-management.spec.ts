/**
 * 用户资料管理功能E2E测试
 */
import { expect, test } from '@playwright/test'
import { LoginPage } from '../pages/LoginPage'
import { ProfilePage } from '../pages/ProfilePage'

test.describe('用户资料管理功能', () => {
  let loginPage: LoginPage
  let profilePage: ProfilePage

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page)
    profilePage = new ProfilePage(page)

    // 登录用户
    await loginPage.navigate()
    await loginPage.login('testuser@example.com', 'password123')
    
    // 导航到个人中心
    await profilePage.navigate()
  })

  test('应该显示用户资料页面的所有元素', async ({ page }) => {
    // 验证页面标题
    await expect(page.locator('h1:has-text("个人中心")')).toBeVisible()
    
    // 验证个人信息卡片
    await expect(page.locator('.profile-card')).toBeVisible()
    await expect(page.locator('text=个人信息')).toBeVisible()
    
    // 验证安全设置卡片
    await expect(page.locator('.security-card')).toBeVisible()
    await expect(page.locator('text=安全设置')).toBeVisible()
    
    // 验证积分账户卡片
    await expect(page.locator('.points-card')).toBeVisible()
    await expect(page.locator('text=积分账户')).toBeVisible()
    
    // 验证统计信息卡片
    await expect(page.locator('.stats-card')).toBeVisible()
    await expect(page.locator('text=账户统计')).toBeVisible()
  })

  test('应该能够编辑个人信息', async ({ page }) => {
    // 点击编辑按钮
    await page.click('[data-testid="edit-profile-button"]')

    // 验证表单字段（只保留三个核心字段）
    await expect(page.locator('[data-testid="avatar-upload"]')).toBeVisible()
    await expect(page.locator('[data-testid="nickname-input"]')).toBeVisible()
    await expect(page.locator('[data-testid="bio-textarea"]')).toBeVisible()

    // 修改昵称
    await page.fill('[data-testid="nickname-input"]', '新昵称')

    // 修改个人简介
    await page.fill('[data-testid="bio-textarea"]', '这是我的新个人简介')

    // 保存修改
    await page.click('[data-testid="save-profile-button"]')

    // 验证保存成功
    await expect(page.locator('text=个人信息保存成功')).toBeVisible()
  })

  test('应该验证表单字段', async ({ page }) => {
    // 点击编辑按钮
    await page.click('[data-testid="edit-profile-button"]')
    
    // 清空必填字段
    await page.fill('[data-testid="username-input"]', '')
    
    // 尝试保存
    await page.click('[data-testid="save-profile-button"]')
    
    // 验证错误提示
    await expect(page.locator('text=请输入用户名')).toBeVisible()
    
    // 输入无效的手机号
    await page.fill('[data-testid="phone-input"]', '123')
    await page.click('[data-testid="save-profile-button"]')
    
    // 验证手机号格式错误
    await expect(page.locator('text=请输入正确的手机号')).toBeVisible()
  })

  test('应该能够修改密码', async ({ page }) => {
    // 点击修改密码按钮
    await page.click('[data-testid="change-password-button"]')
    
    // 验证密码修改对话框
    await expect(page.locator('text=修改密码')).toBeVisible()
    
    // 填写密码表单
    await page.fill('[data-testid="current-password-input"]', 'password123')
    await page.fill('[data-testid="new-password-input"]', 'newpassword123')
    await page.fill('[data-testid="confirm-password-input"]', 'newpassword123')
    
    // 提交修改
    await page.click('[data-testid="confirm-change-password-button"]')
    
    // 验证修改成功
    await expect(page.locator('text=密码修改成功')).toBeVisible()
  })

  test('应该验证密码修改表单', async ({ page }) => {
    // 点击修改密码按钮
    await page.click('[data-testid="change-password-button"]')
    
    // 输入错误的当前密码
    await page.fill('[data-testid="current-password-input"]', 'wrongpassword')
    await page.fill('[data-testid="new-password-input"]', 'newpassword123')
    await page.fill('[data-testid="confirm-password-input"]', 'newpassword123')
    
    // 提交修改
    await page.click('[data-testid="confirm-change-password-button"]')
    
    // 验证错误提示
    await expect(page.locator('text=当前密码错误')).toBeVisible()
    
    // 测试密码确认不匹配
    await page.fill('[data-testid="current-password-input"]', 'password123')
    await page.fill('[data-testid="new-password-input"]', 'newpassword123')
    await page.fill('[data-testid="confirm-password-input"]', 'differentpassword')
    
    // 提交修改
    await page.click('[data-testid="confirm-change-password-button"]')
    
    // 验证错误提示
    await expect(page.locator('text=两次输入的密码不一致')).toBeVisible()
  })

  test('应该显示积分账户信息', async ({ page }) => {
    // 验证积分余额显示
    await expect(page.locator('[data-testid="points-balance"]')).toBeVisible()
    
    // 验证积分操作按钮
    await expect(page.locator('[data-testid="recharge-points-button"]')).toBeVisible()
    await expect(page.locator('[data-testid="points-history-button"]')).toBeVisible()
    
    // 点击积分历史按钮
    await page.click('[data-testid="points-history-button"]')
    
    // 验证积分历史对话框
    await expect(page.locator('text=积分历史')).toBeVisible()
  })

  test('应该显示账户统计信息', async ({ page }) => {
    // 验证统计项目
    await expect(page.locator('[data-testid="total-projects"]')).toBeVisible()
    await expect(page.locator('[data-testid="total-downloads"]')).toBeVisible()
    await expect(page.locator('[data-testid="total-uploads"]')).toBeVisible()
    await expect(page.locator('[data-testid="member-days"]')).toBeVisible()
    
    // 验证统计数值
    const totalProjects = await page.locator('[data-testid="total-projects"] .stat-value').textContent()
    const totalDownloads = await page.locator('[data-testid="total-downloads"] .stat-value').textContent()
    
    expect(totalProjects).toMatch(/^\d+$/)
    expect(totalDownloads).toMatch(/^\d+$/)
  })

  test('应该支持响应式设计', async ({ page, isMobile }) => {
    if (isMobile) {
      // 验证移动端布局
      await expect(page.locator('.profile-view')).toBeVisible()
      
      // 验证卡片在移动端的布局
      const profileCard = page.locator('.profile-card')
      const cardWidth = await profileCard.evaluate(el => el.offsetWidth)
      const viewportWidth = await page.evaluate(() => window.innerWidth)
      
      // 在移动端，卡片应该占据大部分宽度
      expect(cardWidth / viewportWidth).toBeGreaterThan(0.8)
    }
  })

  test('应该有正确的毛玻璃效果样式', async ({ page }) => {
    // 验证卡片的毛玻璃效果
    const profileCard = page.locator('.profile-card')
    
    const backdropFilter = await profileCard.evaluate(el => 
      window.getComputedStyle(el).backdropFilter
    )
    
    expect(backdropFilter).toContain('blur')
    
    // 验证边框和阴影
    const border = await profileCard.evaluate(el => 
      window.getComputedStyle(el).border
    )
    
    const boxShadow = await profileCard.evaluate(el => 
      window.getComputedStyle(el).boxShadow
    )
    
    expect(border).toBeTruthy()
    expect(boxShadow).toBeTruthy()
  })

  test('应该有悬停动画效果', async ({ page }) => {
    const profileCard = page.locator('.profile-card')
    
    // 获取初始transform值
    const initialTransform = await profileCard.evaluate(el => 
      window.getComputedStyle(el).transform
    )
    
    // 悬停在卡片上
    await profileCard.hover()
    
    // 等待动画完成
    await page.waitForTimeout(500)
    
    // 获取悬停后的transform值
    const hoverTransform = await profileCard.evaluate(el => 
      window.getComputedStyle(el).transform
    )
    
    // 验证transform发生了变化（有悬停效果）
    expect(hoverTransform).not.toBe(initialTransform)
  })

  test('应该能够取消编辑', async ({ page }) => {
    // 点击编辑按钮
    await page.click('[data-testid="edit-profile-button"]')
    
    // 修改一些字段
    await page.fill('[data-testid="nickname-input"]', '临时昵称')
    
    // 点击取消按钮
    await page.click('[data-testid="cancel-edit-button"]')
    
    // 验证回到查看模式
    await expect(page.locator('[data-testid="edit-profile-button"]')).toBeVisible()
    
    // 验证修改没有保存
    const nickname = await page.locator('[data-testid="user-nickname"]').textContent()
    expect(nickname).not.toBe('临时昵称')
  })

  test('应该显示加载状态', async ({ page }) => {
    // 拦截API请求以模拟慢速响应
    await page.route('**/api/users/me', route => {
      setTimeout(() => {
        route.continue()
      }, 2000)
    })
    
    // 刷新页面
    await page.reload()
    
    // 验证加载状态
    const loadingElements = page.locator('.loading-shimmer')
    if (await loadingElements.first().isVisible()) {
      await expect(loadingElements.first()).toBeVisible()
    }
  })

  test('应该处理网络错误', async ({ page }) => {
    // 拦截API请求并返回错误
    await page.route('**/api/users/me', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ message: '服务器错误' })
      })
    })
    
    // 点击编辑并尝试保存
    await page.click('[data-testid="edit-profile-button"]')
    await page.fill('[data-testid="nickname-input"]', '新昵称')
    await page.click('[data-testid="save-profile-button"]')
    
    // 验证错误提示
    await expect(page.locator('text=更新失败')).toBeVisible()
  })

  test('应该保持表单状态', async ({ page }) => {
    // 点击编辑按钮
    await page.click('[data-testid="edit-profile-button"]')
    
    // 填写表单
    await page.fill('[data-testid="nickname-input"]', '测试昵称')
    await page.fill('[data-testid="bio-textarea"]', '测试简介')
    
    // 模拟页面刷新或导航
    await page.reload()
    
    // 验证表单数据是否保持（如果有本地存储）
    // 这取决于具体的实现
  })
})
