/**
 * 速码网用户端前端应用入口文件
 */
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/styles/main.scss'

console.log('🚀 启动速码网用户端前端应用...')

try {
  // 创建Vue应用实例
  const app = createApp(App)

  // 创建Pinia状态管理
  const pinia = createPinia()

  // 注册插件
  app.use(pinia)
  app.use(router)
  app.use(ElementPlus)

  // 全局错误处理
  app.config.errorHandler = (err, vm, info) => {
    console.error('❌ Vue应用错误:', err)
    console.error('📍 错误信息:', info)
  }

  // 挂载应用
  app.mount('#app')
  console.log('✅ 速码网用户端前端应用启动成功！')

} catch (error) {
  console.error('❌ 应用启动失败:', error)

  // 显示错误页面
  const appElement = document.getElementById('app')
  if (appElement) {
    appElement.innerHTML = `
      <div style="padding: 2rem; text-align: center; color: #ff4757; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;">
        <h1>应用启动失败</h1>
        <p>错误: ${error instanceof Error ? error.message : String(error)}</p>
        <button onclick="window.location.reload()" style="margin-top: 1rem; padding: 8px 16px; background: #1890ff; color: white; border: none; border-radius: 4px; cursor: pointer;">
          刷新页面
        </button>
      </div>
    `
  }
}
