/**
 * API服务统一导出
 */

// 导出基础服务
export { BaseApiService } from './base'

// 导出认证服务
export { default as authApi } from './modules/auth'

// 导出用户服务
export { default as userApi } from './user'

// 导出项目服务
export { default as projectApi } from './modules/project'

// 导出收藏服务
export { default as favoriteApi } from './modules/favorite'

// 导出用户统计服务
export { default as userStatsApi } from './modules/userStats'

// 导出下载服务
export { default as downloadApi } from './modules/download'

// 导出公开服务
export { publicProjectApi, publicContentApi } from './modules/public'

// 导出HTTP客户端
export { default as http } from '@/utils/http'

// 导出类型定义
export type * from '@/types/api'
export type * from '@/types/user'

// 统一API实例
import authApi from './modules/auth'
import userApi from './user'
import projectApi from './modules/project'
import { publicProjectApi, publicContentApi } from './modules/public'

export const api = {
  auth: authApi,
  user: userApi,
  project: projectApi,
  public: {
    project: publicProjectApi,
    content: publicContentApi
  }
}

export default api
