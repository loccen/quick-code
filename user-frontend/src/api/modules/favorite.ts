/**
 * 收藏功能API模块
 * 提供项目收藏相关的接口
 */
import type { ApiResponse, PageResponse, Project } from '@/types/api'
import { request } from '../request'

/**
 * 收藏统计信息
 */
export interface FavoriteStats {
  /** 总收藏数 */
  totalFavorites: number
  /** 用户ID */
  userId: number
}

/**
 * 收藏API服务
 */
export const favoriteApi = {
  /**
   * 收藏项目
   */
  favoriteProject(projectId: number): Promise<ApiResponse<void>> {
    return request.post(`/projects/${projectId}/favorite`)
  },

  /**
   * 取消收藏项目
   */
  unfavoriteProject(projectId: number): Promise<ApiResponse<void>> {
    return request.delete(`/projects/${projectId}/favorite`)
  },

  /**
   * 检查项目收藏状态
   */
  checkFavoriteStatus(projectId: number): Promise<ApiResponse<boolean>> {
    return request.get(`/projects/${projectId}/favorite/status`)
  },

  /**
   * 获取用户收藏的项目列表
   */
  getUserFavoriteProjects(params: {
    page?: number
    size?: number
    keyword?: string
    sortBy?: string
    sortDir?: 'ASC' | 'DESC'
  } = {}): Promise<ApiResponse<PageResponse<Project>>> {
    const {
      page = 0,
      size = 12,
      keyword,
      sortBy = 'createdTime',
      sortDir = 'DESC'
    } = params

    return request.get('/projects/favorites', {
      params: {
        page,
        size,
        keyword,
        sortBy,
        sortDir
      }
    })
  },

  /**
   * 获取用户最近收藏的项目
   */
  getUserRecentFavorites(limit = 5): Promise<ApiResponse<Project[]>> {
    return request.get('/projects/favorites/recent', {
      params: { limit }
    })
  },

  /**
   * 获取热门收藏项目（公开接口）
   */
  getPopularFavoriteProjects(limit = 10): Promise<ApiResponse<Project[]>> {
    return request.get('/projects/favorites/popular', {
      params: { limit }
    })
  },

  /**
   * 批量检查项目收藏状态
   */
  batchCheckFavoriteStatus(projectIds: number[]): Promise<ApiResponse<Record<number, boolean>>> {
    return request.post('/projects/favorites/batch-check', projectIds)
  },

  /**
   * 获取用户收藏统计
   */
  getUserFavoriteStats(): Promise<ApiResponse<FavoriteStats>> {
    return request.get('/projects/favorites/stats')
  }
}

export default favoriteApi
