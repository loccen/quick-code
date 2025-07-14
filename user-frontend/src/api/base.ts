/**
 * API服务基类
 */
import type { ApiResponse, PageRequest, PageResponse } from '@/types/api'
import http from '@/utils/http'

/**
 * API服务基类
 */
export abstract class BaseApiService {
  protected baseUrl: string

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl
  }

  /**
   * 构建完整URL
   */
  protected buildUrl(path: string): string {
    return `${this.baseUrl}${path}`
  }

  /**
   * GET请求
   */
  protected get<T = any>(path: string, params?: any): Promise<ApiResponse<T>> {
    return http.get(this.buildUrl(path), params)
  }

  /**
   * POST请求
   */
  protected post<T = any>(path: string, data?: any, config?: any): Promise<ApiResponse<T>> {
    return http.post(this.buildUrl(path), data, config)
  }

  /**
   * PUT请求
   */
  protected put<T = any>(path: string, data?: any): Promise<ApiResponse<T>> {
    return http.put(this.buildUrl(path), data)
  }

  /**
   * DELETE请求
   */
  protected delete<T = any>(path: string): Promise<ApiResponse<T>> {
    return http.delete(this.buildUrl(path))
  }

  /**
   * PATCH请求
   */
  protected patch<T = any>(path: string, data?: any): Promise<ApiResponse<T>> {
    return http.patch(this.buildUrl(path), data)
  }

  /**
   * 分页查询
   */
  protected getPage<T = any>(path: string, params?: PageRequest): Promise<ApiResponse<PageResponse<T>>> {
    return this.get(path, params)
  }

  /**
   * 根据ID查询
   */
  protected getById<T = any>(id: string | number): Promise<ApiResponse<T>> {
    return this.get(`/${id}`)
  }

  /**
   * 创建资源
   */
  protected create<T = any>(data: any): Promise<ApiResponse<T>> {
    return this.post('', data)
  }

  /**
   * 更新资源
   */
  protected update<T = any>(id: string | number, data: any): Promise<ApiResponse<T>> {
    return this.put(`/${id}`, data)
  }

  /**
   * 删除资源
   */
  protected remove<T = any>(id: string | number): Promise<ApiResponse<T>> {
    return this.delete(`/${id}`)
  }

  /**
   * 批量删除
   */
  protected batchRemove<T = any>(ids: (string | number)[]): Promise<ApiResponse<T>> {
    return this.delete(`/batch?ids=${ids.join(',')}`)
  }

  /**
   * 上传文件
   */
  protected upload<T = any>(path: string, file: File, onProgress?: (progress: number) => void): Promise<ApiResponse<T>> {
    return http.upload(this.buildUrl(path), file, onProgress)
  }

  /**
   * 下载文件
   */
  protected download(path: string, filename?: string): Promise<void> {
    return http.download(this.buildUrl(path), filename)
  }
}
