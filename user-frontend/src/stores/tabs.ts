/**
 * 标签页状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { RouteLocationNormalized } from 'vue-router'
import type { TabItem } from '@/types/router'

/**
 * 标签页状态store
 */
export const useTabsStore = defineStore('tabs', () => {
  // 状态
  const tabs = ref<TabItem[]>([])
  const activeTab = ref<string>('')

  // 计算属性
  const tabList = computed(() => tabs.value)
  const currentTab = computed(() => tabs.value.find(tab => tab.name === activeTab.value))

  /**
   * 初始化标签页
   */
  const initTabs = () => {
    // 添加首页标签（固定标签）
    const homeTab: TabItem = {
      name: 'Dashboard',
      title: '仪表盘',
      path: '/dashboard',
      icon: 'Dashboard',
      affix: true,
      closable: false
    }
    
    if (!tabs.value.find(tab => tab.name === homeTab.name)) {
      tabs.value.push(homeTab)
    }
  }

  /**
   * 添加标签页
   */
  const addTab = (route: RouteLocationNormalized) => {
    // 检查是否已存在
    const existingTab = tabs.value.find(tab => tab.name === route.name as string)
    if (existingTab) {
      // 更新现有标签页
      existingTab.title = route.meta?.title || existingTab.title
      existingTab.path = route.fullPath
      setActiveTab(route.name as string)
      return
    }

    // 创建新标签页
    const newTab: TabItem = {
      name: route.name as string,
      title: route.meta?.title || '未命名页面',
      path: route.fullPath,
      icon: route.meta?.icon,
      affix: route.meta?.affix || false,
      closable: !route.meta?.affix
    }

    tabs.value.push(newTab)
    setActiveTab(route.name as string)
  }

  /**
   * 移除标签页
   */
  const removeTab = (tabName: string) => {
    const index = tabs.value.findIndex(tab => tab.name === tabName)
    if (index === -1) return

    const tab = tabs.value[index]
    
    // 固定标签页不能关闭
    if (tab.affix) return

    // 如果关闭的是当前激活标签页，需要切换到其他标签页
    if (activeTab.value === tabName) {
      // 优先切换到右边的标签页，如果没有则切换到左边的
      const nextTab = tabs.value[index + 1] || tabs.value[index - 1]
      if (nextTab) {
        setActiveTab(nextTab.name)
      }
    }

    tabs.value.splice(index, 1)
    return tab
  }

  /**
   * 移除其他标签页
   */
  const removeOtherTabs = (tabName: string) => {
    const currentTab = tabs.value.find(tab => tab.name === tabName)
    if (!currentTab) return

    // 保留固定标签页和当前标签页
    tabs.value = tabs.value.filter(tab => tab.affix || tab.name === tabName)
    setActiveTab(tabName)
  }

  /**
   * 移除左侧标签页
   */
  const removeLeftTabs = (tabName: string) => {
    const index = tabs.value.findIndex(tab => tab.name === tabName)
    if (index === -1) return

    // 移除左侧非固定标签页
    for (let i = index - 1; i >= 0; i--) {
      const tab = tabs.value[i]
      if (!tab.affix) {
        tabs.value.splice(i, 1)
      }
    }
  }

  /**
   * 移除右侧标签页
   */
  const removeRightTabs = (tabName: string) => {
    const index = tabs.value.findIndex(tab => tab.name === tabName)
    if (index === -1) return

    // 移除右侧非固定标签页
    for (let i = tabs.value.length - 1; i > index; i--) {
      const tab = tabs.value[i]
      if (!tab.affix) {
        tabs.value.splice(i, 1)
      }
    }
  }

  /**
   * 移除所有标签页
   */
  const removeAllTabs = () => {
    // 只保留固定标签页
    tabs.value = tabs.value.filter(tab => tab.affix)
    
    // 如果有固定标签页，激活第一个
    if (tabs.value.length > 0) {
      setActiveTab(tabs.value[0].name)
    }
  }

  /**
   * 设置激活标签页
   */
  const setActiveTab = (tabName: string) => {
    activeTab.value = tabName
  }

  /**
   * 更新标签页标题
   */
  const updateTabTitle = (tabName: string, title: string) => {
    const tab = tabs.value.find(tab => tab.name === tabName)
    if (tab) {
      tab.title = title
    }
  }

  /**
   * 获取标签页索引
   */
  const getTabIndex = (tabName: string): number => {
    return tabs.value.findIndex(tab => tab.name === tabName)
  }

  /**
   * 获取下一个标签页
   */
  const getNextTab = (tabName: string): TabItem | null => {
    const index = getTabIndex(tabName)
    if (index === -1 || index === tabs.value.length - 1) return null
    return tabs.value[index + 1]
  }

  /**
   * 获取上一个标签页
   */
  const getPrevTab = (tabName: string): TabItem | null => {
    const index = getTabIndex(tabName)
    if (index <= 0) return null
    return tabs.value[index - 1]
  }

  /**
   * 检查标签页是否存在
   */
  const hasTab = (tabName: string): boolean => {
    return tabs.value.some(tab => tab.name === tabName)
  }

  /**
   * 检查标签页是否可关闭
   */
  const isTabClosable = (tabName: string): boolean => {
    const tab = tabs.value.find(tab => tab.name === tabName)
    return tab ? tab.closable !== false : true
  }

  /**
   * 重置标签页状态
   */
  const resetTabs = () => {
    tabs.value = []
    activeTab.value = ''
    initTabs()
  }

  // 初始化
  initTabs()

  return {
    // 状态
    tabs: tabList,
    activeTab,
    currentTab,

    // 方法
    addTab,
    removeTab,
    removeOtherTabs,
    removeLeftTabs,
    removeRightTabs,
    removeAllTabs,
    setActiveTab,
    updateTabTitle,
    getTabIndex,
    getNextTab,
    getPrevTab,
    hasTab,
    isTabClosable,
    resetTabs
  }
})
