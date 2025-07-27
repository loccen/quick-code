/**
 * 项目管理相关类型定义
 */

/**
 * 项目文件类型
 */
export interface ProjectFile {
  /** 文件ID */
  id: number
  /** 项目ID */
  projectId: number
  /** 文件名 */
  fileName: string
  /** 原始文件名 */
  originalName: string
  /** 文件路径 */
  filePath: string
  /** 文件URL */
  fileUrl: string
  /** 文件类型 */
  fileType: 'SOURCE' | 'COVER' | 'DEMO' | 'DOCUMENT'
  /** 文件类型描述 */
  fileTypeDesc: string
  /** MIME类型 */
  mimeType: string
  /** 文件大小 */
  fileSize: number
  /** 文件大小（可读格式） */
  readableFileSize: string
  /** 文件哈希值 */
  fileHash: string
  /** 文件状态 */
  fileStatus: number
  /** 文件状态描述 */
  fileStatusDesc: string
  /** 上传时间 */
  uploadTime: string
  /** 处理时间 */
  processTime?: string
  /** 是否为主文件 */
  isPrimary: boolean
  /** 文件描述 */
  description?: string
  /** 创建时间 */
  createdTime: string
  /** 更新时间 */
  updatedTime: string
}

/**
 * 项目下载记录
 */
export interface ProjectDownload {
  /** 下载记录ID */
  id: number
  /** 项目ID */
  projectId: number
  /** 项目标题 */
  projectTitle: string
  /** 用户ID */
  userId: number
  /** 用户名 */
  username: string
  /** 文件ID */
  fileId?: number
  /** 文件名 */
  fileName?: string
  /** 下载时间 */
  downloadTime: string
  /** 下载状态 */
  downloadStatus: number
  /** 下载状态描述 */
  downloadStatusDesc: string
  /** 下载IP地址 */
  downloadIp: string
  /** 用户代理 */
  userAgent: string
  /** 下载来源 */
  downloadSource: string
  /** 下载来源描述 */
  downloadSourceDesc: string
  /** 文件大小 */
  fileSize?: number
  /** 文件大小（可读格式） */
  readableFileSize?: string
  /** 下载耗时（毫秒） */
  downloadDuration?: number
  /** 下载耗时（可读格式） */
  readableDuration?: string
  /** 是否为重复下载 */
  isRepeat: boolean
  /** 下载备注 */
  remark?: string
  /** 创建时间 */
  createdTime: string
  /** 更新时间 */
  updatedTime: string
}

/**
 * 项目管理信息
 */
export interface ProjectManagement {
  /** 项目ID */
  id: number
  /** 项目标题 */
  title: string
  /** 项目描述 */
  description: string
  /** 分类ID */
  categoryId: number
  /** 分类名称 */
  categoryName: string
  /** 用户ID */
  userId: number
  /** 用户名 */
  username: string
  /** 封面图片URL */
  coverImage?: string
  /** 项目价格 */
  price: number
  /** 是否免费 */
  isFree: boolean
  /** 项目状态 */
  status: number
  /** 项目状态描述 */
  statusDesc: string
  /** 是否精选 */
  isFeatured: boolean
  /** 浏览次数 */
  viewCount: number
  /** 下载次数 */
  downloadCount: number
  /** 点赞次数 */
  likeCount: number
  /** 评分 */
  rating?: number
  /** 评分人数 */
  ratingCount: number
  /** 发布时间 */
  publishedTime?: string
  /** 创建时间 */
  createdTime: string
  /** 更新时间 */
  updatedTime: string
  /** 项目标签 */
  tags: string[]
  /** 技术栈 */
  techStack: string[]
  /** 文件统计 */
  fileStatistics?: FileStatistics
  /** 下载统计 */
  downloadStatistics?: DownloadStatistics
}

/**
 * 文件统计信息
 */
export interface FileStatistics {
  /** 总文件数 */
  totalFiles: number
  /** 总大小 */
  totalSize: number
  /** 总大小（可读格式） */
  readableTotalSize: string
  /** 源码文件数 */
  sourceFiles: number
  /** 封面图片数 */
  coverImages: number
  /** 演示文件数 */
  demoFiles: number
  /** 文档文件数 */
  documentFiles: number
}

/**
 * 下载统计信息
 */
export interface DownloadStatistics {
  /** 总下载次数 */
  totalDownloads: number
  /** 独立下载用户数 */
  uniqueDownloaders: number
  /** 今日下载次数 */
  todayDownloads: number
  /** 本周下载次数 */
  weekDownloads: number
  /** 本月下载次数 */
  monthDownloads: number
}

/**
 * 项目文件上传请求
 */
export interface ProjectFileUploadRequest {
  /** 项目ID */
  projectId: number
  /** 文件类型 */
  fileType: 'SOURCE' | 'COVER' | 'DEMO' | 'DOCUMENT'
  /** 文件描述 */
  description?: string
  /** 是否设为主文件 */
  isPrimary?: boolean
  /** 自定义文件路径 */
  customPath?: string
}

/**
 * 项目文件上传响应
 */
export interface ProjectFileUploadResponse {
  /** 文件ID */
  fileId: number
  /** 项目ID */
  projectId: number
  /** 文件名 */
  fileName: string
  /** 原始文件名 */
  originalName: string
  /** 文件URL */
  fileUrl: string
  /** 文件类型 */
  fileType: string
  /** 文件大小 */
  fileSize: number
  /** 文件状态 */
  fileStatus: number
  /** 文件状态描述 */
  fileStatusDesc: string
  /** 是否为主文件 */
  isPrimary: boolean
  /** 上传时间 */
  uploadTime: string
  /** 文件描述 */
  description?: string
}

/**
 * 批量上传响应
 */
export interface ProjectFileBatchUploadResponse {
  /** 总文件数 */
  totalFiles: number
  /** 成功上传数 */
  successCount: number
  /** 失败上传数 */
  failureCount: number
  /** 成功上传的文件列表 */
  successFiles: ProjectFileUploadResponse[]
  /** 失败上传的文件列表 */
  failedFiles: FailedFileInfo[]
}

/**
 * 失败文件信息
 */
export interface FailedFileInfo {
  /** 原始文件名 */
  originalName: string
  /** 失败原因 */
  reason: string
  /** 错误代码 */
  errorCode: string
}

/**
 * 项目上传请求（简化版本）
 */
export interface ProjectUploadRequest {
  /** 项目标题 */
  title: string
  /** 项目描述 */
  description: string
  /** 项目分类ID */
  categoryId: number
  /** 项目标签 */
  tags: string[]
  /** 项目价格（积分） */
  price: number
  /** 项目演示URL */
  demoUrl?: string
  /** 技术栈 */
  techStack: string[]
  /** 封面图片URL */
  coverImage?: string
}

/**
 * 下载令牌响应
 */
export interface DownloadTokenResponse {
  /** 下载令牌 */
  token: string
  /** 项目ID */
  projectId: number
  /** 过期时间（分钟） */
  expirationMinutes: number
  /** 过期时间 */
  expirationTime: string
}

/**
 * 下载历史响应
 */
export interface ProjectDownloadHistoryResponse {
  /** 下载记录ID */
  downloadId: number
  /** 项目ID */
  projectId: number
  /** 项目标题 */
  projectTitle: string
  /** 文件ID */
  fileId?: number
  /** 文件名 */
  fileName?: string
  /** 下载时间 */
  downloadTime: string
  /** 下载状态 */
  downloadStatus: number
  /** 下载状态描述 */
  downloadStatusDesc: string
  /** 下载来源 */
  downloadSource: string
  /** 文件大小 */
  fileSize?: number
  /** 文件大小（可读格式） */
  readableFileSize?: string
  /** 下载耗时（毫秒） */
  downloadDuration?: number
  /** 下载耗时（可读格式） */
  readableDuration?: string
  /** 是否为重复下载 */
  isRepeat: boolean
}

/**
 * 下载统计响应
 */
export interface ProjectDownloadStatisticsResponse {
  /** 总下载次数 */
  totalDownloads: number
  /** 独立下载用户数 */
  uniqueDownloaders: number
  /** 总下载大小（字节） */
  totalSize: number
  /** 总下载大小（可读格式） */
  readableTotalSize: string
  /** 平均下载耗时（毫秒） */
  averageDuration: number
  /** 平均下载耗时（可读格式） */
  readableAverageDuration: string
  /** 按来源统计下载次数 */
  downloadsBySource: Record<string, number>
  /** 按日期统计下载次数 */
  downloadsByDate: Record<string, number>
}
