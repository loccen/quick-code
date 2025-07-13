/**
 * 应用状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 应用主题类型
 */
export type ThemeMode = 'light' | 'dark' | 'auto'

/**
 * 语言类型
 */
export type Language = 'zh-CN' | 'en-US'

/**
 * 布局模式
 */
export type LayoutMode = 'vertical' | 'horizontal' | 'mix'

/**
 * 应用状态store
 */
export const useAppStore = defineStore('app', () => {
  // 基础状态
  const loading = ref<boolean>(false)
  const error = ref<string>('')
  const success = ref<string>('')
  
  // 主题设置
  const theme = ref<ThemeMode>('light')
  const primaryColor = ref<string>('#1890ff')
  
  // 语言设置
  const language = ref<Language>('zh-CN')
  
  // 布局设置
  const layoutMode = ref<LayoutMode>('vertical')
  const sidebarCollapsed = ref<boolean>(false)
  const showBreadcrumb = ref<boolean>(true)
  const showTabs = ref<boolean>(true)
  const showFooter = ref<boolean>(true)
  
  // 页面设置
  const pageTitle = ref<string>('速码网')
  const pageLoading = ref<boolean>(false)
  
  // 设备信息
  const isMobile = ref<boolean>(false)
  const isTablet = ref<boolean>(false)
  const isDesktop = ref<boolean>(true)
  
  // 网络状态
  const isOnline = ref<boolean>(navigator.onLine)
  
  // 计算属性
  const isDarkMode = computed(() => {
    if (theme.value === 'auto') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return theme.value === 'dark'
  })
  
  const isLightMode = computed(() => !isDarkMode.value)
  
  const deviceType = computed(() => {
    if (isMobile.value) return 'mobile'
    if (isTablet.value) return 'tablet'
    return 'desktop'
  })

  /**
   * 初始化应用状态
   */
  const initAppState = () => {
    // 从localStorage恢复设置
    const savedTheme = localStorage.getItem('app_theme') as ThemeMode
    const savedLanguage = localStorage.getItem('app_language') as Language
    const savedLayoutMode = localStorage.getItem('app_layout_mode') as LayoutMode
    const savedPrimaryColor = localStorage.getItem('app_primary_color')
    const savedSidebarCollapsed = localStorage.getItem('app_sidebar_collapsed')
    
    if (savedTheme) theme.value = savedTheme
    if (savedLanguage) language.value = savedLanguage
    if (savedLayoutMode) layoutMode.value = savedLayoutMode
    if (savedPrimaryColor) primaryColor.value = savedPrimaryColor
    if (savedSidebarCollapsed) sidebarCollapsed.value = JSON.parse(savedSidebarCollapsed)
    
    // 检测设备类型
    detectDeviceType()
    
    // 监听网络状态
    window.addEventListener('online', () => setOnlineStatus(true))
    window.addEventListener('offline', () => setOnlineStatus(false))
    
    // 监听系统主题变化
    if (theme.value === 'auto') {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      mediaQuery.addEventListener('change', applyTheme)
    }
    
    // 应用主题
    applyTheme()
  }

  /**
   * 检测设备类型
   */
  const detectDeviceType = () => {
    const width = window.innerWidth
    isMobile.value = width < 768
    isTablet.value = width >= 768 && width < 1024
    isDesktop.value = width >= 1024
  }

  /**
   * 设置加载状态
   */
  const setLoading = (value: boolean) => {
    loading.value = value
  }

  /**
   * 设置页面加载状态
   */
  const setPageLoading = (value: boolean) => {
    pageLoading.value = value
  }

  /**
   * 设置错误信息
   */
  const setError = (message: string) => {
    error.value = message
    if (message) {
      setTimeout(() => {
        error.value = ''
      }, 5000)
    }
  }

  /**
   * 设置成功信息
   */
  const setSuccess = (message: string) => {
    success.value = message
    if (message) {
      setTimeout(() => {
        success.value = ''
      }, 3000)
    }
  }

  /**
   * 设置主题
   */
  const setTheme = (newTheme: ThemeMode) => {
    theme.value = newTheme
    localStorage.setItem('app_theme', newTheme)
    applyTheme()
  }

  /**
   * 应用主题
   */
  const applyTheme = () => {
    const root = document.documentElement
    if (isDarkMode.value) {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }
  }

  /**
   * 设置主色调
   */
  const setPrimaryColor = (color: string) => {
    primaryColor.value = color
    localStorage.setItem('app_primary_color', color)
    
    // 应用主色调到CSS变量
    const root = document.documentElement
    root.style.setProperty('--primary-color', color)
  }

  /**
   * 设置语言
   */
  const setLanguage = (lang: Language) => {
    language.value = lang
    localStorage.setItem('app_language', lang)
  }

  /**
   * 设置布局模式
   */
  const setLayoutMode = (mode: LayoutMode) => {
    layoutMode.value = mode
    localStorage.setItem('app_layout_mode', mode)
  }

  /**
   * 切换侧边栏折叠状态
   */
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
    localStorage.setItem('app_sidebar_collapsed', JSON.stringify(sidebarCollapsed.value))
  }

  /**
   * 设置侧边栏折叠状态
   */
  const setSidebarCollapsed = (collapsed: boolean) => {
    sidebarCollapsed.value = collapsed
    localStorage.setItem('app_sidebar_collapsed', JSON.stringify(collapsed))
  }

  /**
   * 设置页面标题
   */
  const setPageTitle = (title: string) => {
    pageTitle.value = title
    document.title = `${title} - 速码网`
  }

  /**
   * 设置网络状态
   */
  const setOnlineStatus = (status: boolean) => {
    isOnline.value = status
  }

  /**
   * 重置应用状态
   */
  const resetAppState = () => {
    loading.value = false
    error.value = ''
    success.value = ''
    pageLoading.value = false
  }

  /**
   * 清除所有设置
   */
  const clearAppSettings = () => {
    localStorage.removeItem('app_theme')
    localStorage.removeItem('app_language')
    localStorage.removeItem('app_layout_mode')
    localStorage.removeItem('app_primary_color')
    localStorage.removeItem('app_sidebar_collapsed')
    
    // 重置为默认值
    theme.value = 'light'
    language.value = 'zh-CN'
    layoutMode.value = 'vertical'
    primaryColor.value = '#1890ff'
    sidebarCollapsed.value = false
    
    applyTheme()
    setPrimaryColor(primaryColor.value)
  }

  // 初始化状态
  initAppState()

  // 监听窗口大小变化
  window.addEventListener('resize', detectDeviceType)

  return {
    // 基础状态
    loading,
    error,
    success,
    
    // 主题设置
    theme,
    primaryColor,
    isDarkMode,
    isLightMode,
    
    // 语言设置
    language,
    
    // 布局设置
    layoutMode,
    sidebarCollapsed,
    showBreadcrumb,
    showTabs,
    showFooter,
    
    // 页面设置
    pageTitle,
    pageLoading,
    
    // 设备信息
    isMobile,
    isTablet,
    isDesktop,
    deviceType,
    
    // 网络状态
    isOnline,
    
    // 方法
    setLoading,
    setPageLoading,
    setError,
    setSuccess,
    setTheme,
    setPrimaryColor,
    setLanguage,
    setLayoutMode,
    toggleSidebar,
    setSidebarCollapsed,
    setPageTitle,
    setOnlineStatus,
    resetAppState,
    clearAppSettings
  }
})
