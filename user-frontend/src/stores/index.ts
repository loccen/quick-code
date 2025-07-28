/**
 * 状态管理统一导出
 */
import { createPinia } from 'pinia'

// 创建pinia实例
export const pinia = createPinia()

// 导出所有store
export { useUserStore } from './user'
export { useAppStore } from './app'
export { useUploadStore } from './upload'

// 导出类型
export type { ThemeMode, Language, LayoutMode } from './app'
