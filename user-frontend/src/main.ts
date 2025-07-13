/**
 * Vue应用程序入口文件
 */
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import ElementPlus from 'element-plus'
import { createApp } from 'vue'

// 样式导入（按正确顺序）
import '@/styles/main.scss'
import 'element-plus/dist/index.css'

// 应用组件和配置
import { envConfig, isDev } from '@/config/env'
import { pinia } from '@/stores'
import App from './App.vue'
import router from './router'

/**
 * 创建Vue应用实例
 */
async function createVueApp() {
  try {
    const app = createApp(App)

    // 注册Element Plus
    app.use(ElementPlus, {
      // Element Plus配置
      size: 'default',
      zIndex: 3000
    })

    // 注册Element Plus图标
    for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
      app.component(key, component)
    }

    // 注册状态管理
    app.use(pinia)

    // 注册路由
    app.use(router)

    // 全局错误处理
    app.config.errorHandler = (err, vm, info) => {
      console.error('Vue应用错误:', err)
      console.error('错误信息:', info)

      if (isDev) {
        console.error('组件实例:', vm)
      }
    }

    // 全局警告处理
    app.config.warnHandler = (msg, vm, trace) => {
      if (isDev) {
        console.warn('Vue警告:', msg)
        console.warn('组件追踪:', trace)
      }
    }

    // 设置全局属性
    app.config.globalProperties.$env = envConfig

    // 挂载应用
    app.mount('#app')

    if (isDev) {
      console.log('🚀 Vue应用启动成功')
      console.log('📦 环境配置:', envConfig)
    }

    return app
  } catch (error) {
    console.error('❌ Vue应用启动失败:', error)

    // 显示错误页面
    document.body.innerHTML = `
      <div style="
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100vh;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
        color: #333;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      ">
        <div style="
          background: rgba(255, 255, 255, 0.9);
          padding: 2rem;
          border-radius: 12px;
          box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
          backdrop-filter: blur(10px);
          text-align: center;
          max-width: 400px;
        ">
          <h1 style="margin: 0 0 1rem 0; color: #e74c3c;">应用启动失败</h1>
          <p style="margin: 0 0 1rem 0; color: #666;">
            抱歉，应用无法正常启动。请刷新页面重试。
          </p>
          <button
            onclick="window.location.reload()"
            style="
              background: #3498db;
              color: white;
              border: none;
              padding: 0.5rem 1rem;
              border-radius: 6px;
              cursor: pointer;
              font-size: 14px;
            "
          >
            刷新页面
          </button>
        </div>
      </div>
    `

    throw error
  }
}

// 启动应用
createVueApp()
