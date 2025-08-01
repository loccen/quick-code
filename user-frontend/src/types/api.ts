/**
 * API相关类型定义
 */

/**
 * 统一API响应格式
 */
export interface ApiResponse<T = any> {
  /** 状态码 */
  code: number
  /** 响应消息 */
  message: string
  /** 响应数据 */
  data: T
  /** 时间戳 */
  timestamp: number
  /** 请求ID */
  requestId?: string
}

/**
 * 分页响应数据
 */
export interface PageResponse<T = any> {
  /** 数据列表 */
  content: T[]
  /** 当前页码 */
  page: number
  /** 每页大小 */
  size: number
  /** 总页数 */
  totalPages: number
  /** 总记录数 */
  total: number
  /** 是否为第一页 */
  first: boolean
  /** 是否为最后一页 */
  last: boolean
  /** 是否有下一页 */
  hasNext: boolean
  /** 是否有上一页 */
  hasPrevious: boolean
  /** 是否为空页 */
  empty: boolean
  /** 当前页记录数 */
  currentSize: number
}

/**
 * 分页请求参数
 */
export interface PageRequest {
  /** 页码，从0开始 */
  page?: number
  /** 每页大小 */
  size?: number
  /** 排序字段 */
  sort?: string
  /** 排序方向 */
  direction?: 'ASC' | 'DESC'
}

/**
 * HTTP请求方法
 */
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'

/**
 * 请求配置
 */
export interface RequestConfig {
  /** 请求URL */
  url: string
  /** 请求方法 */
  method?: HttpMethod
  /** 请求参数 */
  params?: Record<string, any>
  /** 请求体数据 */
  data?: any
  /** 请求头 */
  headers?: Record<string, string>
  /** 超时时间 */
  timeout?: number
  /** 是否需要认证 */
  auth?: boolean
  /** 是否显示loading */
  loading?: boolean
  /** 是否显示错误提示 */
  showError?: boolean
}

/**
 * API错误信息
 */
export interface ApiError {
  /** 错误码 */
  code: number
  /** 错误消息 */
  message: string
  /** 错误详情 */
  details?: any
  /** 时间戳 */
  timestamp: number
  /** 请求路径 */
  path?: string
}

/**
 * 项目相关类型
 */
export interface Project {
  id: number
  title: string
  description: string
  category: string
  price: number
  thumbnail?: string
  author: string
  authorId?: number
  rating?: number
  downloads?: number
  tags?: string[]
  status?: 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED'
  featured?: boolean
  createdAt: string
  updatedAt?: string
  // 详细信息
  features?: string[]
  techStack?: string[]
  sourceSize?: string
  license?: string
  demoUrl?: string
  sourceUrl?: string
}

/**
 * 项目分类类型
 */
export interface ProjectCategory {
  id: number
  name: string
  code: string
  description?: string
  count: number
  parentId?: number
  children?: ProjectCategory[]
}
