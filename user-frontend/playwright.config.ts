/**
 * Playwright测试配置
 */
import { defineConfig, devices } from '@playwright/test'

/**
 * 从环境变量读取配置
 */
const baseURL = process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:3000'
const headless = process.env.PLAYWRIGHT_HEADLESS !== 'false'
const workers = process.env.CI ? 1 : undefined

export default defineConfig({
  // 测试目录
  testDir: './tests/e2e',

  // 全局设置
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers,

  // 报告配置
  reporter: [
    ['html', { outputFolder: 'test-results/html-report' }],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/junit.xml' }],
    process.env.CI ? ['github'] : ['list']
  ],

  // 全局测试配置
  use: {
    baseURL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 10000,
    navigationTimeout: 30000
  },

  // 项目配置
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        // 设置视口大小
        viewport: { width: 1280, height: 720 },
        // 设置用户代理
        userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
      }
    }
  ],

  // Web服务器配置
  webServer: {
    command: 'npm run dev',
    url: baseURL,
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
    stdout: 'ignore',
    stderr: 'pipe'
  },

  // 输出目录
  outputDir: 'test-results/artifacts',

  // 全局设置和拆卸
  globalSetup: './tests/e2e/global-setup.ts',
  globalTeardown: './tests/e2e/global-teardown.ts',

  // 测试超时
  timeout: 30 * 1000,
  expect: {
    timeout: 5 * 1000
  }
})
