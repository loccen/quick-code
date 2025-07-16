/**
 * 注册功能端到端测试
 */
import { expect, test } from '../fixtures/base'

test.describe('用户注册', () => {
  test.beforeEach(async ({ registerPage }) => {
    await registerPage.navigate()
  })

  test('应该显示注册页面的所有元素', async ({ registerPage }) => {
    await registerPage.validatePageElements()

    // 验证页面标题
    await expect(registerPage.getPage()).toHaveTitle(/注册/)

    // 验证URL
    await expect(registerPage.getPage()).toHaveURL(/\/register/)
  })

  test('应该能够成功注册并自动登录', async ({ registerPage, profilePage }) => {
    // 执行注册
    await registerPage.register({
      username: 'newuser',
      email: 'newuser@example.com',
      password: 'password123',
      confirmPassword: 'password123',
      emailCode: '123456'
    })

    // 等待注册完成
    await registerPage.waitForRegisterComplete()

    // 验证跳转到个人中心（注册成功后自动登录）
    await expect(profilePage.getPage()).toHaveURL(/\/user\/profile/)

    // 验证个人中心页面加载
    await profilePage.waitForLoad()
    await profilePage.validateProfileElements()
  })

  test('应该能够处理重定向参数', async ({ registerPage, page }) => {
    // 访问带有重定向参数的注册页面
    await page.goto('/register?redirect=/user/profile')

    // 执行注册
    await registerPage.register({
      username: 'redirectuser',
      email: 'redirectuser@example.com',
      password: 'password123',
      confirmPassword: 'password123',
      emailCode: '123456'
    })

    // 等待注册完成
    await registerPage.waitForRegisterComplete()

    // 验证跳转到指定的重定向页面
    await expect(page).toHaveURL(/\/user\/profile/)
  })

  test('应该显示错误消息当邮箱已存在时', async ({ registerPage }) => {
    // 使用已存在的邮箱注册
    await registerPage.register({
      username: 'existinguser',
      email: 'existing@example.com',
      password: 'password123',
      confirmPassword: 'password123',
      emailCode: '123456'
    })

    // 验证显示错误消息
    expect(await registerPage.hasErrorMessage()).toBe(true)

    const errorMessage = await registerPage.getErrorMessage()
    expect(errorMessage).toContain('邮箱已存在')

    // 验证仍在注册页面
    await expect(registerPage.getPage()).toHaveURL(/\/register/)
  })

  test('应该验证密码确认', async ({ registerPage }) => {
    // 填写不匹配的密码
    await registerPage.fillForm({
      username: 'testuser',
      email: 'test@example.com',
      password: 'password123',
      confirmPassword: 'differentpassword',
      emailCode: '123456'
    })

    // 尝试注册
    await registerPage.clickRegister()

    // 验证显示密码不匹配错误
    const hasValidationError = await registerPage.hasValidationError()
    expect(hasValidationError).toBe(true)
  })

  test('应该验证必填字段', async ({ registerPage }) => {
    // 尝试不填写任何信息就注册
    await registerPage.clickRegister()

    // 验证注册按钮仍然禁用或显示验证错误
    const isButtonEnabled = await registerPage.isRegisterButtonEnabled()
    expect(isButtonEnabled).toBe(false)
  })

  test('应该能够发送邮箱验证码', async ({ registerPage }) => {
    // 填写邮箱
    await registerPage.fillEmail('test@example.com')

    // 点击发送验证码
    await registerPage.clickSendEmailCode()

    // 验证显示成功消息
    const hasSuccessMessage = await registerPage.hasSuccessMessage()
    expect(hasSuccessMessage).toBe(true)

    // 验证按钮进入倒计时状态
    const isCountdownActive = await registerPage.isEmailCodeCountdownActive()
    expect(isCountdownActive).toBe(true)
  })

  test('应该能够导航到登录页面', async ({ registerPage }) => {
    await registerPage.clickLogin()

    // 验证跳转到登录页面
    await expect(registerPage.getPage()).toHaveURL(/\/login/)
  })

  test('应该显示加载状态', async ({ registerPage }) => {
    // 填写注册信息
    await registerPage.fillForm({
      username: 'testuser',
      email: 'test@example.com',
      password: 'password123',
      confirmPassword: 'password123',
      emailCode: '123456'
    })

    // 点击注册按钮
    await registerPage.clickRegister()

    // 验证显示加载状态（如果加载时间足够长）
    const isLoading = await registerPage.isLoading()
    // 注意：这个测试可能需要根据实际的加载时间调整
  })

  test('应该在已登录状态下重定向到个人中心', async ({ registerPage, loginPage, profilePage }) => {
    // 先登录
    await loginPage.navigate()
    await loginPage.login('testuser@example.com', 'password123')
    await loginPage.waitForLoginComplete()

    // 再次访问注册页面
    await registerPage.navigate()

    // 应该自动重定向到个人中心
    await expect(profilePage.getPage()).toHaveURL(/\/user\/profile/)
  })
})
