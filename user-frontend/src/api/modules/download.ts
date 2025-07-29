/**
 * 下载管理API模块
 * 提供项目下载权限检查、令牌生成等功能
 */
import type { ApiResponse, PageResponse } from '@/types/api'
import { request } from '../request'

/**
 * 下载权限检查结果
 */
export interface DownloadPermission {
  canDownload: boolean
  reason?: string
  requiresPurchase?: boolean
  projectPrice?: number
}

/**
 * 下载令牌信息
 */
export interface DownloadToken {
  token: string
  projectId: number
  expiresAt: string
  createdAt: string
}

/**
 * 下载记录
 */
export interface DownloadRecord {
  id: number
  projectId: number
  projectTitle: string
  userId: number
  username: string
  downloadTime: string
  ipAddress: string
  userAgent: string
  fileSize: number
  downloadDuration: number
  status: 'SUCCESS' | 'FAILED' | 'CANCELLED'
}

/**
 * 下载统计信息
 */
export interface DownloadStatistics {
  totalDownloads: number
  successfulDownloads: number
  failedDownloads: number
  totalSize: number
  averageSize: number
  popularProjects: Array<{
    projectId: number
    projectTitle: string
    downloadCount: number
  }>
}

/**
 * 下载查询参数
 */
export interface DownloadQueryParams {
  page?: number
  size?: number
  status?: string
  startDate?: string
  endDate?: string
  projectId?: number
  sortBy?: string
  sortDirection?: 'ASC' | 'DESC'
}

/**
 * 活跃下载令牌
 */
export interface ActiveDownloadToken {
  token: string
  projectId: number
  projectTitle: string
  expiresAt: string
  remainingTime: number
}

/**
 * 下载API
 */
export const downloadApi = {
  /**
   * 检查下载权限
   */
  checkDownloadPermission(projectId: number): Promise<ApiResponse<DownloadPermission>> {
    return request.get(`/downloads/project/${projectId}/permission`)
  },

  /**
   * 生成下载令牌
   */
  generateDownloadToken(projectId: number, expirationMinutes: number = 60): Promise<ApiResponse<DownloadToken>> {
    return request.post(`/downloads/project/${projectId}/token`, { expirationMinutes })
  },

  /**
   * 验证下载令牌
   */
  validateDownloadToken(token: string, projectId: number): Promise<ApiResponse<boolean>> {
    return request.post(`/downloads/token/${token}/validate`, { projectId })
  },

  /**
   * 使用令牌下载项目
   */
  downloadProjectWithToken(token: string): Promise<Blob> {
    return request.get(`/downloads/token/${token}/download`, {
      responseType: 'blob'
    })
  },

  /**
   * 直接下载项目（需要权限）
   */
  downloadProject(projectId: number): Promise<Blob> {
    return request.get(`/downloads/project/${projectId}/download`, {
      responseType: 'blob'
    })
  },

  /**
   * 获取用户下载记录
   */
  getUserDownloadRecords(params: DownloadQueryParams = {}): Promise<ApiResponse<PageResponse<DownloadRecord>>> {
    return request.get('/downloads/records', {
      params: {
        page: 0,
        size: 10,
        sortBy: 'downloadTime',
        sortDirection: 'DESC',
        ...params
      }
    })
  },

  /**
   * 获取用户下载统计
   */
  getUserDownloadStatistics(): Promise<ApiResponse<DownloadStatistics>> {
    return request.get('/downloads/statistics')
  },

  /**
   * 获取活跃下载令牌
   */
  getActiveDownloadTokens(): Promise<ApiResponse<ActiveDownloadToken[]>> {
    return request.get('/downloads/tokens/active')
  },

  /**
   * 撤销下载令牌
   */
  revokeDownloadToken(token: string): Promise<ApiResponse<boolean>> {
    return request.delete(`/downloads/token/${token}`)
  },

  /**
   * 获取项目下载统计
   */
  getProjectDownloadStatistics(projectId: number): Promise<ApiResponse<DownloadStatistics>> {
    return request.get(`/downloads/project/${projectId}/statistics`)
  },

  /**
   * 记录下载开始
   */
  recordDownloadStart(projectId: number, token?: string): Promise<ApiResponse<{ recordId: number }>> {
    return request.post('/downloads/record/start', { projectId, token })
  },

  /**
   * 记录下载完成
   */
  recordDownloadComplete(recordId: number, fileSize: number, duration: number): Promise<ApiResponse<boolean>> {
    return request.post(`/downloads/record/${recordId}/complete`, { fileSize, duration })
  },

  /**
   * 记录下载失败
   */
  recordDownloadFailed(recordId: number, reason: string): Promise<ApiResponse<boolean>> {
    return request.post(`/downloads/record/${recordId}/failed`, { reason })
  }
}

/**
 * 管理员下载API
 */
export const adminDownloadApi = {
  /**
   * 获取所有下载记录（管理员）
   */
  getAllDownloadRecords(params: DownloadQueryParams = {}): Promise<ApiResponse<PageResponse<DownloadRecord>>> {
    return request.get('/downloads/admin/records', { params })
  },

  /**
   * 获取系统下载统计（管理员）
   */
  getSystemDownloadStatistics(): Promise<ApiResponse<DownloadStatistics>> {
    return request.get('/downloads/admin/statistics')
  },

  /**
   * 清理过期令牌（管理员）
   */
  cleanupExpiredTokens(): Promise<ApiResponse<{ cleanedCount: number }>> {
    return request.post('/downloads/admin/cleanup-tokens')
  }
}
