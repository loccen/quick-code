/**
 * Playwright全局设置
 */
import { chromium, FullConfig } from '@playwright/test'

async function globalSetup(config: FullConfig) {
  console.log('🚀 开始Playwright全局设置...')

  // 启动浏览器进行预热
  const browser = await chromium.launch()
  const context = await browser.newContext()
  const page = await context.newPage()

  try {
    // 访问应用首页进行预热
    const baseURL = config.projects[0].use.baseURL || 'http://localhost:3000'
    console.log(`📡 预热应用: ${baseURL}`)

    await page.goto(baseURL, { waitUntil: 'networkidle' })

    // 等待应用完全加载
    await page.waitForSelector('body', { timeout: 30000 })

    console.log('✅ 应用预热完成')
  } catch (error) {
    console.error('❌ 应用预热失败:', error)
    throw error
  } finally {
    await context.close()
    await browser.close()
  }

  console.log('✅ Playwright全局设置完成')
}

export default globalSetup
