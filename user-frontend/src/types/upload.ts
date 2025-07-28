/**
 * 上传相关类型定义
 */

/**
 * 上传任务状态
 */
export enum UploadTaskStatus {
  /** 等待中 */
  PENDING = 'pending',
  /** 上传中 */
  UPLOADING = 'uploading',
  /** 已完成 */
  COMPLETED = 'completed',
  /** 已失败 */
  FAILED = 'failed',
  /** 已暂停 */
  PAUSED = 'paused',
  /** 已取消 */
  CANCELLED = 'cancelled'
}

/**
 * 文件类型
 */
export enum FileType {
  /** 源码文件 */
  SOURCE = 'SOURCE',
  /** 封面图片 */
  COVER = 'COVER',
  /** 演示文件 */
  DEMO = 'DEMO',
  /** 文档文件 */
  DOCUMENT = 'DOCUMENT'
}

/**
 * 上传任务
 */
export interface UploadTask {
  /** 任务ID */
  id: string
  /** 项目ID */
  projectId: number
  /** 文件对象 */
  file: File
  /** 文件类型 */
  fileType: FileType
  /** 任务状态 */
  status: UploadTaskStatus
  /** 上传进度 (0-100) */
  progress: number
  /** 上传速度 (字节/秒) */
  speed: number
  /** 已上传字节数 */
  uploadedBytes: number
  /** 错误信息 */
  error?: string
  /** 重试次数 */
  retryCount: number
  /** 最大重试次数 */
  maxRetries: number
  /** 创建时间 */
  createdAt: Date
  /** 开始时间 */
  startedAt?: Date
  /** 完成时间 */
  completedAt?: Date
  /** 是否为主文件 */
  isPrimary?: boolean
  /** 文件描述 */
  description?: string
  /** 上传结果 */
  result?: any
}

/**
 * 上传队列统计
 */
export interface UploadQueueStats {
  /** 总任务数 */
  total: number
  /** 等待中任务数 */
  pending: number
  /** 上传中任务数 */
  uploading: number
  /** 已完成任务数 */
  completed: number
  /** 已失败任务数 */
  failed: number
  /** 已暂停任务数 */
  paused: number
  /** 已取消任务数 */
  cancelled: number
  /** 总上传进度 */
  overallProgress: number
  /** 总上传速度 */
  totalSpeed: number
}

/**
 * 上传配置
 */
export interface UploadConfig {
  /** 并发上传数量 */
  concurrency: number
  /** 最大重试次数 */
  maxRetries: number
  /** 重试延迟 (毫秒) */
  retryDelay: number
  /** 自动开始上传 */
  autoStart: boolean
  /** 上传完成后自动移除任务 */
  autoRemoveCompleted: boolean
  /** 自动移除延迟 (毫秒) */
  autoRemoveDelay: number
}

/**
 * 上传事件类型
 */
export enum UploadEventType {
  /** 任务添加 */
  TASK_ADDED = 'task_added',
  /** 任务开始 */
  TASK_STARTED = 'task_started',
  /** 任务进度更新 */
  TASK_PROGRESS = 'task_progress',
  /** 任务完成 */
  TASK_COMPLETED = 'task_completed',
  /** 任务失败 */
  TASK_FAILED = 'task_failed',
  /** 任务暂停 */
  TASK_PAUSED = 'task_paused',
  /** 任务取消 */
  TASK_CANCELLED = 'task_cancelled',
  /** 任务重试 */
  TASK_RETRIED = 'task_retried',
  /** 队列开始 */
  QUEUE_STARTED = 'queue_started',
  /** 队列暂停 */
  QUEUE_PAUSED = 'queue_paused',
  /** 队列完成 */
  QUEUE_COMPLETED = 'queue_completed'
}

/**
 * 上传事件
 */
export interface UploadEvent {
  /** 事件类型 */
  type: UploadEventType
  /** 任务ID */
  taskId?: string
  /** 任务数据 */
  task?: UploadTask
  /** 事件数据 */
  data?: any
  /** 时间戳 */
  timestamp: Date
}

/**
 * 上传回调函数类型
 */
export type UploadEventCallback = (event: UploadEvent) => void

/**
 * 上传进度回调函数类型
 */
export type UploadProgressCallback = (taskId: string, progress: number, speed: number) => void

/**
 * 批量上传选项
 */
export interface BatchUploadOptions {
  /** 项目ID */
  projectId: number
  /** 文件列表 */
  files: File[]
  /** 文件类型 */
  fileType: FileType
  /** 文件描述 */
  description?: string
  /** 是否自动开始 */
  autoStart?: boolean
  /** 进度回调 */
  onProgress?: UploadProgressCallback
  /** 完成回调 */
  onComplete?: (results: UploadTask[]) => void
  /** 错误回调 */
  onError?: (error: string, task: UploadTask) => void
}
