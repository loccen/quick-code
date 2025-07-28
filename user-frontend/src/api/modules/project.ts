/**
 * 项目管理API模块
 * 提供项目CRUD操作、文件上传下载等功能
 */
import type { ApiResponse, PageResponse } from '@/types/api'
import type { 
  ProjectUploadRequest, 
  ProjectFileUploadRequest,
  ProjectFileUploadResponse,
  ProjectFile,
  ProjectDownload,
  DownloadTokenResponse,
  ProjectManagement
} from '@/types/project'
import { request } from '../request'

/**
 * 项目查询参数
 */
export interface ProjectQueryParams {
  page?: number
  size?: number
  status?: number
  keyword?: string
  sortBy?: string
  sortDir?: string
}

/**
 * 项目文件查询参数
 */
export interface ProjectFileQueryParams {
  page?: number
  size?: number
  fileType?: string
  status?: number
}

/**
 * 项目管理API
 */
export const projectApi = {
  /**
   * 创建项目
   */
  createProject(data: ProjectUploadRequest): Promise<ApiResponse<ProjectManagement>> {
    return request.post('/projects', data)
  },

  /**
   * 更新项目
   */
  updateProject(id: number, data: Partial<ProjectUploadRequest>): Promise<ApiResponse<ProjectManagement>> {
    return request.put(`/projects/${id}`, data)
  },

  /**
   * 获取项目详情
   */
  getProject(id: number): Promise<ApiResponse<any>> {
    return request.get(`/projects/${id}`)
  },

  /**
   * 删除项目
   */
  deleteProject(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/projects/${id}`)
  },

  /**
   * 获取我的项目列表
   */
  getMyProjects(params: ProjectQueryParams = {}): Promise<ApiResponse<PageResponse<ProjectManagement>>> {
    return request.get('/projects/my', { params })
  },

  /**
   * 发布项目
   */
  publishProject(id: number): Promise<ApiResponse<void>> {
    return request.post(`/projects/${id}/publish`)
  },

  /**
   * 下架项目
   */
  unpublishProject(id: number): Promise<ApiResponse<void>> {
    return request.post(`/projects/${id}/unpublish`)
  },

  /**
   * 获取项目统计信息
   */
  getProjectStats(id: number): Promise<ApiResponse<any>> {
    return request.get(`/projects/${id}/stats`)
  }
}

/**
 * 项目文件管理API
 */
export const projectFileApi = {
  /**
   * 上传项目文件
   */
  uploadFile(
    projectId: number,
    file: File,
    options: ProjectFileUploadRequest,
    onProgress?: (progress: number) => void
  ): Promise<ApiResponse<ProjectFileUploadResponse>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('fileType', options.fileType)
    if (options.description) {
      formData.append('description', options.description)
    }
    if (options.isPrimary !== undefined) {
      formData.append('isPrimary', String(options.isPrimary))
    }

    return request.post(`/upload/project/${projectId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: onProgress ? (progressEvent) => {
        if (progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      } : undefined
    })
  },

  /**
   * 批量上传项目文件
   */
  uploadFiles(
    projectId: number, 
    files: File[], 
    fileType: string = 'SOURCE'
  ): Promise<ApiResponse<any>> {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    formData.append('fileType', fileType)

    return request.post(`/upload/project/${projectId}/batch`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 获取项目文件列表
   */
  getProjectFiles(
    projectId: number, 
    params: ProjectFileQueryParams = {}
  ): Promise<ApiResponse<PageResponse<ProjectFile>>> {
    return request.get(`/projects/${projectId}/files`, { params })
  },

  /**
   * 删除项目文件
   */
  deleteFile(projectId: number, fileId: number): Promise<ApiResponse<void>> {
    return request.delete(`/projects/${projectId}/files/${fileId}`)
  },

  /**
   * 批量删除项目文件
   */
  deleteFiles(projectId: number, fileIds: number[]): Promise<ApiResponse<any>> {
    return request.delete(`/projects/${projectId}/files/batch`, {
      data: fileIds
    })
  },

  /**
   * 替换项目文件
   */
  replaceFile(fileId: number, file: File): Promise<ApiResponse<ProjectFileUploadResponse>> {
    const formData = new FormData()
    formData.append('file', file)

    return request.put(`/upload/project/file/${fileId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 设置主文件
   */
  setPrimaryFile(projectId: number, fileId: number): Promise<ApiResponse<void>> {
    return request.post(`/projects/${projectId}/files/${fileId}/primary`)
  },

  /**
   * 获取项目文件统计
   */
  getFileStatistics(projectId: number): Promise<ApiResponse<any>> {
    return request.get(`/projects/${projectId}/files/statistics`)
  }
}

/**
 * 项目下载API
 */
export const projectDownloadApi = {
  /**
   * 生成下载令牌
   */
  generateDownloadToken(
    projectId: number, 
    expirationMinutes: number = 60
  ): Promise<ApiResponse<DownloadTokenResponse>> {
    return request.post(`/download/token/${projectId}`, null, {
      params: { expirationMinutes }
    })
  },

  /**
   * 下载项目文件
   */
  downloadProject(projectId: number, source: string = 'WEB'): Promise<Blob> {
    return request.get(`/download/project/${projectId}`, {
      params: { source },
      responseType: 'blob'
    }).then(response => response.data)
  },

  /**
   * 下载指定文件
   */
  downloadFile(projectId: number, fileId: number, source: string = 'WEB'): Promise<Blob> {
    return request.get(`/download/project/${projectId}/file/${fileId}`, {
      params: { source },
      responseType: 'blob'
    }).then(response => response.data)
  },

  /**
   * 获取下载历史
   */
  getDownloadHistory(params: {
    page?: number
    size?: number
    sort?: string
    direction?: string
  } = {}): Promise<ApiResponse<PageResponse<ProjectDownload>>> {
    return request.get('/download/history', { params })
  },

  /**
   * 检查下载权限
   */
  checkDownloadPermission(projectId: number): Promise<ApiResponse<boolean>> {
    return request.get(`/download/permission/${projectId}`)
  },

  /**
   * 获取项目下载统计
   */
  getDownloadStats(projectId: number, days: number = 30): Promise<ApiResponse<any>> {
    return request.get(`/download/stats/${projectId}`, {
      params: { days }
    })
  }
}

/**
 * 文件上传通用API
 */
export const fileUploadApi = {
  /**
   * 上传头像
   */
  uploadAvatar(file: File): Promise<ApiResponse<{ url: string; filename: string }>> {
    const formData = new FormData()
    formData.append('file', file)

    return request.post('/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 上传项目文件（基础版）
   */
  uploadProjectFile(file: File): Promise<ApiResponse<{ url: string; filename: string }>> {
    const formData = new FormData()
    formData.append('file', file)

    return request.post('/upload/project', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

/**
 * 统一导出
 */
export default {
  project: projectApi,
  file: projectFileApi,
  download: projectDownloadApi,
  upload: fileUploadApi
}
