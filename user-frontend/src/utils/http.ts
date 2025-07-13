/**
 * HTTP客户端工具类
 */
import { envConfig } from '@/config/env'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import type { ApiError, ApiResponse, RequestConfig } from '@/types/api'
import type { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import axios from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import type { LoadingInstance } from 'element-plus/es/components/loading/src/loading'

/**
 * HTTP客户端类
 */
class HttpClient {
  private instance: AxiosInstance
  private loadingInstance: LoadingInstance | null = null
  private requestCount = 0

  constructor() {
    this.instance = axios.create({
      baseURL: envConfig.API_BASE_URL,
      timeout: envConfig.API_TIMEOUT,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    this.setupInterceptors()
  }

  /**
   * 设置拦截器
   */
  private setupInterceptors() {
    // 请求拦截器
    this.instance.interceptors.request.use(
      (config) => {
        this.handleRequestStart(config)
        return config
      },
      (error) => {
        this.handleRequestEnd()
        return Promise.reject(error)
      }
    )

    // 响应拦截器
    this.instance.interceptors.response.use(
      (response) => {
        this.handleRequestEnd()
        return this.handleResponse(response)
      },
      (error) => {
        this.handleRequestEnd()
        return this.handleError(error)
      }
    )
  }

  /**
   * 处理请求开始
   */
  private handleRequestStart(config: AxiosRequestConfig) {
    const userStore = useUserStore()
    const appStore = useAppStore()

    // 添加认证token
    if (userStore.token && config.headers) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }

    // 添加请求ID
    if (config.headers) {
      config.headers['X-Request-ID'] = this.generateRequestId()
    }

    // 显示loading
    const showLoading = (config as any).showLoading !== false
    if (showLoading) {
      this.showLoading()
    }

    // 设置loading状态
    appStore.setLoading(true)

    if (envConfig.DEBUG) {
      console.log('🚀 Request:', config)
    }
  }

  /**
   * 处理请求结束
   */
  private handleRequestEnd() {
    const appStore = useAppStore()

    this.hideLoading()
    appStore.setLoading(false)
  }

  /**
   * 处理响应成功
   */
  private handleResponse(response: AxiosResponse): any {
    if (envConfig.DEBUG) {
      console.log('✅ Response:', response)
    }

    const { data } = response

    // 检查业务状态码
    if (data.code !== undefined && data.code !== 200) {
      const error: ApiError = {
        code: data.code,
        message: data.message || '请求失败',
        timestamp: Date.now()
      }
      return Promise.reject(error)
    }

    return data
  }

  /**
   * 处理请求错误
   */
  private handleError(error: AxiosError): Promise<never> {
    if (envConfig.DEBUG) {
      console.error('❌ Error:', error)
    }

    const appStore = useAppStore()
    let apiError: ApiError

    if (error.response) {
      // 服务器响应错误
      const { status, data } = error.response
      apiError = {
        code: status,
        message: (data as any)?.message || this.getErrorMessage(status),
        details: data,
        timestamp: Date.now()
      }
    } else if (error.request) {
      // 网络错误
      apiError = {
        code: 0,
        message: '网络连接失败，请检查网络设置',
        timestamp: Date.now()
      }
    } else {
      // 其他错误
      apiError = {
        code: -1,
        message: error.message || '请求失败',
        timestamp: Date.now()
      }
    }

    // 处理特殊错误码
    this.handleSpecialErrors(apiError)

    // 显示错误提示
    const showError = (error.config as any)?.showError !== false
    if (showError) {
      ElMessage.error(apiError.message)
    }

    // 设置全局错误状态
    appStore.setError(apiError.message)

    return Promise.reject(apiError)
  }

  /**
   * 处理特殊错误码
   */
  private handleSpecialErrors(error: ApiError) {
    const userStore = useUserStore()

    switch (error.code) {
      case 401:
        // 未授权，清除token并跳转登录
        userStore.logout()
        break
      case 403:
        // 权限不足
        ElMessage.error('权限不足，请联系管理员')
        break
      case 429:
        // 请求过于频繁
        ElMessage.error('请求过于频繁，请稍后再试')
        break
    }
  }

  /**
   * 获取错误消息
   */
  private getErrorMessage(status: number): string {
    const messages: Record<number, string> = {
      400: '请求参数错误',
      401: '未授权，请重新登录',
      403: '权限不足',
      404: '请求的资源不存在',
      405: '请求方法不允许',
      408: '请求超时',
      409: '资源冲突',
      422: '请求参数验证失败',
      429: '请求过于频繁',
      500: '服务器内部错误',
      502: '网关错误',
      503: '服务不可用',
      504: '网关超时'
    }
    return messages[status] || `请求失败 (${status})`
  }

  /**
   * 显示loading
   */
  private showLoading() {
    this.requestCount++
    if (this.requestCount === 1 && !this.loadingInstance) {
      this.loadingInstance = ElLoading.service({
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })
    }
  }

  /**
   * 隐藏loading
   */
  private hideLoading() {
    this.requestCount = Math.max(0, this.requestCount - 1)
    if (this.requestCount === 0 && this.loadingInstance) {
      this.loadingInstance.close()
      this.loadingInstance = null
    }
  }

  /**
   * 生成请求ID
   */
  private generateRequestId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * GET请求
   */
  get<T = any>(url: string, params?: any, config?: Partial<RequestConfig>): Promise<ApiResponse<T>> {
    return this.instance.get(url, { params, ...config })
  }

  /**
   * POST请求
   */
  post<T = any>(url: string, data?: any, config?: Partial<RequestConfig>): Promise<ApiResponse<T>> {
    return this.instance.post(url, data, config)
  }

  /**
   * PUT请求
   */
  put<T = any>(url: string, data?: any, config?: Partial<RequestConfig>): Promise<ApiResponse<T>> {
    return this.instance.put(url, data, config)
  }

  /**
   * DELETE请求
   */
  delete<T = any>(url: string, config?: Partial<RequestConfig>): Promise<ApiResponse<T>> {
    return this.instance.delete(url, config)
  }

  /**
   * PATCH请求
   */
  patch<T = any>(url: string, data?: any, config?: Partial<RequestConfig>): Promise<ApiResponse<T>> {
    return this.instance.patch(url, data, config)
  }

  /**
   * 上传文件
   */
  upload<T = any>(url: string, file: File, onProgress?: (progress: number) => void): Promise<ApiResponse<T>> {
    const formData = new FormData()
    formData.append('file', file)

    return this.instance.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      }
    })
  }

  /**
   * 下载文件
   */
  download(url: string, filename?: string): Promise<void> {
    return this.instance.get(url, {
      responseType: 'blob'
    }).then((response) => {
      const blob = new Blob([response.data])
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = filename || 'download'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    })
  }
}

/**
 * HTTP客户端实例
 */
export const http = new HttpClient()

/**
 * 导出默认实例
 */
export default http
