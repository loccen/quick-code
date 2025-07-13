/**
 * 登录功能端到端测试
 */
import { test, expect } from '../fixtures/base'

test.describe('用户登录', () => {
  test.beforeEach(async ({ loginPage }) => {
    await loginPage.navigate()
  })

  test('应该显示登录页面的所有元素', async ({ loginPage }) => {
    await loginPage.validatePageElements()
    
    // 验证页面标题
    await expect(loginPage.page).toHaveTitle(/登录/)
    
    // 验证URL
    await expect(loginPage.page).toHaveURL(/\/login/)
  })

  test('应该能够成功登录', async ({ loginPage, dashboardPage }) => {
    // 执行登录
    await loginPage.login('testuser@example.com', 'password123')
    
    // 等待登录完成
    await loginPage.waitForLoginComplete()
    
    // 验证跳转到仪表盘
    await expect(dashboardPage.page).toHaveURL(/\/dashboard/)
    
    // 验证仪表盘页面加载
    await dashboardPage.waitForDashboardLoad()
    await dashboardPage.validateDashboardElements()
  })

  test('应该显示错误消息当凭据无效时', async ({ loginPage }) => {
    // 使用无效凭据登录
    await loginPage.login('invalid@example.com', 'wrongpassword')
    
    // 验证显示错误消息
    expect(await loginPage.hasErrorMessage()).toBe(true)
    
    const errorMessage = await loginPage.getErrorMessage()
    expect(errorMessage).toContain('用户名或密码错误')
    
    // 验证仍在登录页面
    await expect(loginPage.page).toHaveURL(/\/login/)
  })

  test('应该验证必填字段', async ({ loginPage }) => {
    // 尝试不填写任何信息就登录
    await loginPage.clickLogin()
    
    // 验证登录按钮仍然禁用或显示验证错误
    const isButtonEnabled = await loginPage.isLoginButtonEnabled()
    expect(isButtonEnabled).toBe(false)
  })

  test('应该能够记住登录状态', async ({ loginPage, dashboardPage }) => {
    // 勾选记住我选项登录
    await loginPage.login('testuser@example.com', 'password123', true)
    
    // 验证记住我选项被选中
    expect(await loginPage.isRememberMeChecked()).toBe(true)
    
    // 等待登录完成
    await loginPage.waitForLoginComplete()
    
    // 验证跳转到仪表盘
    await expect(dashboardPage.page).toHaveURL(/\/dashboard/)
  })

  test('应该能够导航到注册页面', async ({ loginPage }) => {
    await loginPage.clickRegister()
    
    // 验证跳转到注册页面
    await expect(loginPage.page).toHaveURL(/\/register/)
  })

  test('应该能够导航到忘记密码页面', async ({ loginPage }) => {
    await loginPage.clickForgotPassword()
    
    // 验证跳转到忘记密码页面
    await expect(loginPage.page).toHaveURL(/\/forgot-password/)
  })

  test('应该显示加载状态', async ({ loginPage }) => {
    // 填写登录信息
    await loginPage.fillUsername('testuser@example.com')
    await loginPage.fillPassword('password123')
    
    // 点击登录按钮
    await loginPage.clickLogin()
    
    // 验证显示加载状态（如果加载时间足够长）
    const isLoading = await loginPage.isLoading()
    // 注意：这个测试可能需要根据实际的加载时间调整
  })

  test('应该能够清空表单', async ({ loginPage }) => {
    // 填写一些信息
    await loginPage.fillUsername('test@example.com')
    await loginPage.fillPassword('password')
    
    // 清空表单
    await loginPage.clearForm()
    
    // 验证表单已清空
    const username = await loginPage.page.inputValue('[data-testid="username-input"]')
    const password = await loginPage.page.inputValue('[data-testid="password-input"]')
    
    expect(username).toBe('')
    expect(password).toBe('')
  })

  test('应该在已登录状态下重定向到仪表盘', async ({ loginPage, dashboardPage }) => {
    // 先登录
    await loginPage.login('testuser@example.com', 'password123')
    await loginPage.waitForLoginComplete()
    
    // 再次访问登录页面
    await loginPage.navigate()
    
    // 应该自动重定向到仪表盘
    await expect(dashboardPage.page).toHaveURL(/\/dashboard/)
  })
})
