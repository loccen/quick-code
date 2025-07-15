/**
 * API服务统一导出
 */

// 导出基础服务
export { BaseApiService } from './base'

// 导出认证服务
export { default as authApi } from './modules/auth'

// 导出用户服务
export { default as userApi } from './user'

// 导出HTTP客户端
export { default as http } from '@/utils/http'

// 导出类型定义
export type * from '@/types/api'
export type * from '@/types/user'

// 统一API实例
import authApi from './modules/auth'
import userApi from './user'

export const api = {
  auth: authApi,
  user: userApi
}

export default api
