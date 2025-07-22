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
    return request.post('/api/projects', data)
  },

  /**
   * 更新项目
   */
  updateProject(id: number, data: Partial<ProjectUploadRequest>): Promise<ApiResponse<ProjectManagement>> {
    return request.put(`/api/projects/${id}`, data)
  },

  /**
   * 获取项目详情
   */
  getProject(id: number): Promise<ApiResponse<ProjectManagement>> {
    return request.get(`/api/projects/${id}`)
  },

  /**
   * 删除项目
   */
  deleteProject(id: number): Promise<ApiResponse<void>> {
    return request.delete(`/api/projects/${id}`)
  },

  /**
   * 获取我的项目列表
   */
  getMyProjects(params: ProjectQueryParams = {}): Promise<ApiResponse<PageResponse<ProjectManagement>>> {
    return request.get('/api/projects/my', { params })
  },

  /**
   * 发布项目
   */
  publishProject(id: number): Promise<ApiResponse<void>> {
    return request.post(`/api/projects/${id}/publish`)
  },

  /**
   * 下架项目
   */
  unpublishProject(id: number): Promise<ApiResponse<void>> {
    return request.post(`/api/projects/${id}/unpublish`)
  },

  /**
   * 获取项目统计信息
   */
  getProjectStats(id: number): Promise<ApiResponse<any>> {
    return request.get(`/api/projects/${id}/stats`)
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
    options: ProjectFileUploadRequest
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

    return request.post(`/api/upload/project/${projectId}/enhanced`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
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

    return request.post(`/api/upload/project/${projectId}/batch`, formData, {
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
    return request.get(`/api/projects/${projectId}/files`, { params })
  },

  /**
   * 删除项目文件
   */
  deleteFile(fileId: number): Promise<ApiResponse<void>> {
    return request.delete(`/api/projects/files/${fileId}`)
  },

  /**
   * 设置主文件
   */
  setPrimaryFile(fileId: number): Promise<ApiResponse<void>> {
    return request.post(`/api/projects/files/${fileId}/primary`)
  },

  /**
   * 替换项目文件
   */
  replaceFile(fileId: number, file: File): Promise<ApiResponse<ProjectFileUploadResponse>> {
    const formData = new FormData()
    formData.append('file', file)

    return request.post(`/api/upload/project/files/${fileId}/replace`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
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
    return request.post(`/api/download/token/${projectId}`, null, {
      params: { expirationMinutes }
    })
  },

  /**
   * 下载项目文件
   */
  downloadProject(projectId: number, source: string = 'WEB'): Promise<Blob> {
    return request.get(`/api/download/project/${projectId}`, {
      params: { source },
      responseType: 'blob'
    }).then(response => response.data)
  },

  /**
   * 下载指定文件
   */
  downloadFile(projectId: number, fileId: number, source: string = 'WEB'): Promise<Blob> {
    return request.get(`/api/download/project/${projectId}/file/${fileId}`, {
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
    return request.get('/api/download/history', { params })
  },

  /**
   * 检查下载权限
   */
  checkDownloadPermission(projectId: number): Promise<ApiResponse<boolean>> {
    return request.get(`/api/download/permission/${projectId}`)
  },

  /**
   * 获取项目下载统计
   */
  getDownloadStats(projectId: number, days: number = 30): Promise<ApiResponse<any>> {
    return request.get(`/api/download/stats/${projectId}`, {
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

    return request.post('/api/upload/avatar', formData, {
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

    return request.post('/api/upload/project', formData, {
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
