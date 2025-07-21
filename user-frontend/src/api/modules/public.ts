/**
 * 公开API模块
 * 提供无需认证的公开接口
 */
import type { ApiResponse, PageResponse } from '@/types/api'
import { request } from '../request'

/**
 * 项目查询参数
 */
export interface ProjectQueryParams {
  page?: number
  size?: number
  category?: string
  keyword?: string
  sortBy?: string
  sortDir?: string
}

/**
 * 项目数据类型（匹配后端ProjectDTO）
 */
export interface Project {
  id: number
  title: string
  description: string
  categoryId?: number
  categoryName?: string
  userId?: number
  username?: string // 作者用户名
  userNickname?: string
  userAvatar?: string
  price: number
  coverImage?: string
  demoUrl?: string
  techStack?: string[]
  tags?: string[]
  downloadCount?: number // 下载次数
  viewCount?: number
  likeCount?: number
  rating?: number
  ratingCount?: number
  status?: number
  statusText?: string
  isFeatured?: boolean
  publishedTime?: string
  createdTime?: string // 创建时间
  updatedTime?: string
  // 兼容旧字段名
  category?: string
  thumbnail?: string
  author?: string
  downloads?: number
  createdAt?: string
  updatedAt?: string
  features?: string[]
  sourceSize?: string
  license?: string
}

/**
 * 项目分类数据类型
 */
export interface ProjectCategory {
  id: number
  name: string
  code: string
  count: number
}

/**
 * 公开项目API
 */
export const publicProjectApi = {
  /**
   * 获取项目列表
   */
  getProjects(params: ProjectQueryParams = {}): Promise<ApiResponse<PageResponse<Project>>> {
    return request.get('/public/projects', { params })
  },

  /**
   * 获取项目详情
   */
  getProjectDetail(id: number): Promise<ApiResponse<Project>> {
    return request.get(`/public/projects/${id}`)
  },

  /**
   * 获取项目分类
   */
  getCategories(): Promise<ApiResponse<ProjectCategory[]>> {
    return request.get('/public/projects/categories')
  },

  /**
   * 搜索项目
   */
  searchProjects(keyword: string, limit = 10): Promise<ApiResponse<Project[]>> {
    return request.get('/public/projects/search', {
      params: { keyword, limit }
    })
  }
}

/**
 * 公开内容API
 */
export const publicContentApi = {
  /**
   * 获取平台统计信息
   */
  getPlatformStats(): Promise<ApiResponse<any>> {
    return request.get('/public/stats')
  },

  /**
   * 获取热门项目
   */
  getHotProjects(limit = 6): Promise<ApiResponse<Project[]>> {
    return request.get('/public/projects/popular', {
      params: { limit }
    })
  },

  /**
   * 获取最新项目
   */
  getLatestProjects(limit = 6): Promise<ApiResponse<Project[]>> {
    return request.get('/public/projects/latest', {
      params: { limit }
    })
  },

  /**
   * 获取精选项目
   */
  getFeaturedProjects(limit = 6): Promise<ApiResponse<Project[]>> {
    return request.get('/public/projects/featured', {
      params: { limit }
    })
  }
}

/**
 * 公开用户API
 */
export const publicUserApi = {
  /**
   * 检查用户名是否可用
   */
  checkUsername(username: string): Promise<ApiResponse<{ available: boolean }>> {
    return request.get('/public/users/check-username', {
      params: { username }
    })
  },

  /**
   * 检查邮箱是否可用
   */
  checkEmail(email: string): Promise<ApiResponse<{ available: boolean }>> {
    return request.get('/public/users/check-email', {
      params: { email }
    })
  }
}

// 导出所有公开API
export default {
  project: publicProjectApi,
  content: publicContentApi,
  user: publicUserApi
}
