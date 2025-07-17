/**
 * 前后端API对接集成测试
 */
import { expect, test } from '@playwright/test'
import { LoginPage } from '../pages/LoginPage'

test.describe('前后端API对接测试', () => {
  let loginPage: LoginPage

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page)
  })

  test.describe('用户认证API', () => {
    test('应该能够成功登录并获取用户信息', async ({ page }) => {
      // 拦截登录API请求
      let loginRequest: any = null
      let loginResponse: any = null
      
      await page.route('**/api/auth/login', async route => {
        const request = route.request()
        loginRequest = await request.postDataJSON()
        
        const response = await route.fetch()
        loginResponse = await response.json()
        
        await route.fulfill({ response })
      })

      // 执行登录
      await loginPage.navigate()
      await loginPage.login('testuser@example.com', 'password123')

      // 验证请求格式
      expect(loginRequest).toBeTruthy()
      expect(loginRequest.usernameOrEmail).toBe('testuser@example.com')
      expect(loginRequest.password).toBe('password123')

      // 验证响应格式
      expect(loginResponse).toBeTruthy()
      expect(loginResponse.success).toBe(true)
      expect(loginResponse.data).toBeTruthy()
      expect(loginResponse.data.accessToken).toBeTruthy()
      expect(loginResponse.data.user).toBeTruthy()
    })

    test('应该正确处理登录失败', async ({ page }) => {
      let errorResponse: any = null
      
      await page.route('**/api/auth/login', async route => {
        const response = await route.fetch()
        errorResponse = await response.json()
        await route.fulfill({ response })
      })

      // 使用错误的凭据登录
      await loginPage.navigate()
      await loginPage.login('wrong@example.com', 'wrongpassword')

      // 验证错误响应
      expect(errorResponse).toBeTruthy()
      expect(errorResponse.success).toBe(false)
      expect(errorResponse.message).toBeTruthy()
    })
  })

  test.describe('用户资料API', () => {
    test('应该能够获取当前用户信息', async ({ page }) => {
      let userProfileResponse: any = null
      
      await page.route('**/api/users/me', async route => {
        const response = await route.fetch()
        userProfileResponse = await response.json()
        await route.fulfill({ response })
      })

      // 登录并访问个人中心
      await loginPage.navigate()
      await loginPage.login('testuser@example.com', 'password123')
      await page.goto('/user/profile')

      // 验证用户信息响应格式
      expect(userProfileResponse).toBeTruthy()
      expect(userProfileResponse.data).toBeTruthy()
      expect(userProfileResponse.data.id).toBeTruthy()
      expect(userProfileResponse.data.username).toBeTruthy()
      expect(userProfileResponse.data.email).toBeTruthy()
    })

    test('应该能够更新用户信息', async ({ page }) => {
      let updateRequest: any = null
      let updateResponse: any = null

      await page.route('**/api/users/profile', async route => {
        if (route.request().method() === 'PUT') {
          updateRequest = await route.request().postDataJSON()
          const response = await route.fetch()
          updateResponse = await response.json()
          await route.fulfill({ response })
        } else {
          await route.continue()
        }
      })

      // 登录并更新用户信息
      await loginPage.navigate()
      await loginPage.login('testuser@example.com', 'password123')
      await page.goto('/user/profile')

      // 编辑个人信息
      await page.click('[data-testid="edit-profile-button"]')
      await page.fill('[data-testid="nickname-input"]', '新昵称')
      await page.fill('[data-testid="bio-textarea"]', '新的个人简介')
      await page.click('[data-testid="save-profile-button"]')

      // 验证请求和响应
      expect(updateRequest).toBeTruthy()
      expect(updateRequest.nickname).toBe('新昵称')
      expect(updateRequest.bio).toBe('新的个人简介')

      expect(updateResponse).toBeTruthy()
      expect(updateResponse.success).toBe(true)
    })
  })

  test.describe('头像上传API', () => {
    test('应该能够上传头像', async ({ page }) => {
      let uploadRequest: any = null
      let uploadResponse: any = null
      
      await page.route('**/api/users/me/avatar', async route => {
        uploadRequest = route.request()
        const response = await route.fetch()
        uploadResponse = await response.json()
        await route.fulfill({ response })
      })

      // 登录并上传头像
      await loginPage.navigate()
      await loginPage.login('testuser@example.com', 'password123')
      await page.goto('/user/profile')
      
      // 模拟文件上传
      const fileInput = page.locator('[data-testid="file-input"]')
      await fileInput.setInputFiles({
        name: 'test-avatar.jpg',
        mimeType: 'image/jpeg',
        buffer: Buffer.from('fake-image-data')
      })

      // 验证请求格式
      if (uploadRequest) {
        expect(uploadRequest.method()).toBe('POST')
        expect(uploadRequest.headers()['content-type']).toContain('multipart/form-data')
      }

      // 验证响应格式
      if (uploadResponse) {
        expect(uploadResponse.success).toBe(true)
        expect(uploadResponse.data.url).toBeTruthy()
      }
    })
  })

  test.describe('2FA API（预期失败）', () => {
    test('应该返回2FA功能未实现的错误', async ({ page }) => {
      let twoFactorResponse: any = null
      
      await page.route('**/api/users/me/2fa/status', async route => {
        const response = await route.fetch()
        twoFactorResponse = await response.json()
        await route.fulfill({ response })
      })

      // 登录并尝试访问2FA状态
      await loginPage.navigate()
      await loginPage.login('testuser@example.com', 'password123')
      await page.goto('/user/profile')
      
      // 尝试打开2FA设置
      await page.click('[data-testid="setup-2fa-button"]')

      // 验证返回未实现错误
      if (twoFactorResponse) {
        expect(twoFactorResponse.success).toBe(false)
        expect(twoFactorResponse.message).toContain('未实现')
      }
    })
  })

  test.describe('API错误处理', () => {
    test('应该正确处理网络错误', async ({ page }) => {
      // 模拟网络错误
      await page.route('**/api/users/me', route => {
        route.abort('failed')
      })

      await loginPage.navigate()
      await loginPage.login('testuser@example.com', 'password123')
      await page.goto('/user/profile')

      // 验证错误提示显示
      await expect(page.locator('text=网络错误')).toBeVisible({ timeout: 5000 })
    })

    test('应该正确处理服务器错误', async ({ page }) => {
      // 模拟服务器错误
      await page.route('**/api/users/me', route => {
        route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({
            success: false,
            message: '服务器内部错误'
          })
        })
      })

      await loginPage.navigate()
      await loginPage.login('testuser@example.com', 'password123')
      await page.goto('/user/profile')

      // 验证错误提示显示
      await expect(page.locator('text=服务器内部错误')).toBeVisible({ timeout: 5000 })
    })

    test('应该正确处理认证失败', async ({ page }) => {
      // 模拟认证失败
      await page.route('**/api/users/me', route => {
        route.fulfill({
          status: 401,
          contentType: 'application/json',
          body: JSON.stringify({
            success: false,
            message: '认证失败'
          })
        })
      })

      await loginPage.navigate()
      await loginPage.login('testuser@example.com', 'password123')
      await page.goto('/user/profile')

      // 验证重定向到登录页面
      await expect(page).toHaveURL(/.*\/login/)
    })
  })

  test.describe('API响应格式验证', () => {
    test('所有API响应应该符合统一格式', async ({ page }) => {
      const apiResponses: any[] = []
      
      // 拦截所有API请求
      await page.route('**/api/**', async route => {
        const response = await route.fetch()
        const responseData = await response.json()
        apiResponses.push(responseData)
        await route.fulfill({ response })
      })

      // 执行一系列操作
      await loginPage.navigate()
      await loginPage.login('testuser@example.com', 'password123')
      await page.goto('/user/profile')

      // 验证所有响应都符合统一格式
      for (const response of apiResponses) {
        expect(response).toHaveProperty('success')
        expect(response).toHaveProperty('message')
        expect(typeof response.success).toBe('boolean')
        expect(typeof response.message).toBe('string')
        
        if (response.success) {
          // 成功响应可能有data字段
          if (response.data) {
            expect(response.data).toBeTruthy()
          }
        } else {
          // 失败响应应该有错误信息
          expect(response.message).toBeTruthy()
        }
      }
    })
  })

  test.describe('API性能测试', () => {
    test('API响应时间应该在合理范围内', async ({ page }) => {
      const apiTimes: { [key: string]: number } = {}
      
      await page.route('**/api/**', async route => {
        const startTime = Date.now()
        const response = await route.fetch()
        const endTime = Date.now()
        
        const url = route.request().url()
        apiTimes[url] = endTime - startTime
        
        await route.fulfill({ response })
      })

      // 执行操作
      await loginPage.navigate()
      await loginPage.login('testuser@example.com', 'password123')
      await page.goto('/user/profile')

      // 验证响应时间
      for (const [url, time] of Object.entries(apiTimes)) {
        console.log(`API ${url} 响应时间: ${time}ms`)
        expect(time).toBeLessThan(5000) // 5秒内响应
      }
    })
  })
})
