/**
 * 全局上传队列管理store
 */
import { defineStore } from 'pinia'
import { ref, computed, reactive } from 'vue'
import type {
  UploadTask,
  UploadQueueStats,
  UploadConfig,
  UploadEvent,
  UploadEventType,
  UploadEventCallback,
  BatchUploadOptions,
  FileType
} from '@/types/upload'
import {
  UploadTaskStatus as TaskStatus,
  FileType as FType,
  UploadEventType as EventType
} from '@/types/upload'
import { projectFileApi } from '@/api/modules/project'

/**
 * 生成唯一ID
 */
const generateId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substring(2)
}

/**
 * 上传队列管理store
 */
export const useUploadStore = defineStore('upload', () => {
  // 上传任务队列
  const tasks = ref<UploadTask[]>([])
  
  // 上传配置
  const config = reactive<UploadConfig>({
    concurrency: 3, // 并发上传数量
    maxRetries: 3, // 最大重试次数
    retryDelay: 2000, // 重试延迟 2秒
    autoStart: true, // 自动开始上传
    autoRemoveCompleted: false, // 不自动移除已完成任务
    autoRemoveDelay: 5000 // 自动移除延迟 5秒
  })

  // 队列状态
  const isRunning = ref(false)
  const isPaused = ref(false)

  // 事件监听器
  const eventListeners = ref<Map<UploadEventType, UploadEventCallback[]>>(new Map())

  // 计算属性
  const stats = computed<UploadQueueStats>(() => {
    const total = tasks.value.length
    const pending = tasks.value.filter(t => t.status === TaskStatus.PENDING).length
    const uploading = tasks.value.filter(t => t.status === TaskStatus.UPLOADING).length
    const completed = tasks.value.filter(t => t.status === TaskStatus.COMPLETED).length
    const failed = tasks.value.filter(t => t.status === TaskStatus.FAILED).length
    const paused = tasks.value.filter(t => t.status === TaskStatus.PAUSED).length
    const cancelled = tasks.value.filter(t => t.status === TaskStatus.CANCELLED).length

    // 计算总进度
    const totalProgress = total > 0 
      ? tasks.value.reduce((sum, task) => sum + task.progress, 0) / total 
      : 0

    // 计算总速度
    const totalSpeed = tasks.value
      .filter(t => t.status === TaskStatus.UPLOADING)
      .reduce((sum, task) => sum + task.speed, 0)

    return {
      total,
      pending,
      uploading,
      completed,
      failed,
      paused,
      cancelled,
      overallProgress: Math.round(totalProgress),
      totalSpeed
    }
  })

  const activeTasks = computed(() => 
    tasks.value.filter(t => 
      t.status === TaskStatus.PENDING || 
      t.status === TaskStatus.UPLOADING ||
      t.status === TaskStatus.PAUSED
    )
  )

  const completedTasks = computed(() => 
    tasks.value.filter(t => t.status === TaskStatus.COMPLETED)
  )

  const failedTasks = computed(() => 
    tasks.value.filter(t => t.status === TaskStatus.FAILED)
  )

  // 事件发射器
  const emit = (type: UploadEventType, taskId?: string, task?: UploadTask, data?: unknown) => {
    const event: UploadEvent = {
      type,
      taskId,
      task,
      data,
      timestamp: new Date()
    }

    const listeners = eventListeners.value.get(type) || []
    listeners.forEach(callback => {
      try {
        callback(event)
      } catch (error) {
        console.error('Upload event callback error:', error)
      }
    })
  }

  // 添加事件监听器
  const on = (type: UploadEventType, callback: UploadEventCallback) => {
    if (!eventListeners.value.has(type)) {
      eventListeners.value.set(type, [])
    }
    eventListeners.value.get(type)!.push(callback)
  }

  // 移除事件监听器
  const off = (type: UploadEventType, callback: UploadEventCallback) => {
    const listeners = eventListeners.value.get(type)
    if (listeners) {
      const index = listeners.indexOf(callback)
      if (index > -1) {
        listeners.splice(index, 1)
      }
    }
  }

  // 添加上传任务
  const addTask = (
    projectId: number,
    file: File,
    fileType: FileType,
    options: {
      isPrimary?: boolean
      description?: string
      autoStart?: boolean
    } = {}
  ): string => {
    const task: UploadTask = {
      id: generateId(),
      projectId,
      file,
      fileType,
      status: TaskStatus.PENDING,
      progress: 0,
      speed: 0,
      uploadedBytes: 0,
      retryCount: 0,
      maxRetries: config.maxRetries,
      createdAt: new Date(),
      isPrimary: options.isPrimary,
      description: options.description
    }

    tasks.value.push(task)
    emit(EventType.TASK_ADDED, task.id, task)

    // 自动开始上传
    if (options.autoStart !== false && config.autoStart) {
      startQueue()
    }

    return task.id
  }

  // 批量添加任务
  const addBatchTasks = (options: BatchUploadOptions): string[] => {
    const taskIds: string[] = []

    options.files.forEach((file, index) => {
      const taskId = addTask(
        options.projectId,
        file,
        options.fileType,
        {
          isPrimary: index === 0 && options.fileType === FType.SOURCE,
          description: options.description,
          autoStart: false // 批量添加时不自动开始
        }
      )
      taskIds.push(taskId)
    })

    // 批量添加完成后再开始队列
    if (options.autoStart !== false && config.autoStart) {
      startQueue()
    }

    return taskIds
  }

  // 开始队列
  const startQueue = async () => {
    if (isRunning.value) return

    isRunning.value = true
    isPaused.value = false
    emit(EventType.QUEUE_STARTED)

    await processQueue()
  }

  // 暂停队列
  const pauseQueue = () => {
    isPaused.value = true
    emit(EventType.QUEUE_PAUSED)
  }

  // 恢复队列
  const resumeQueue = async () => {
    if (!isRunning.value) {
      await startQueue()
    } else {
      isPaused.value = false
      await processQueue()
    }
  }

  // 停止队列
  const stopQueue = () => {
    isRunning.value = false
    isPaused.value = false
    
    // 暂停所有上传中的任务
    tasks.value
      .filter(t => t.status === TaskStatus.UPLOADING)
      .forEach(task => {
        task.status = TaskStatus.PAUSED
        emit(EventType.TASK_PAUSED, task.id, task)
      })
  }

  // 处理队列
  const processQueue = async () => {
    while (isRunning.value && !isPaused.value) {
      const pendingTasks = tasks.value.filter(t => t.status === TaskStatus.PENDING)
      const uploadingTasks = tasks.value.filter(t => t.status === TaskStatus.UPLOADING)

      // 检查是否有待处理任务
      if (pendingTasks.length === 0) {
        // 等待所有上传中的任务完成
        if (uploadingTasks.length === 0) {
          isRunning.value = false
          emit(EventType.QUEUE_COMPLETED)
          break
        }
        // 等待一段时间后再检查
        await new Promise(resolve => setTimeout(resolve, 1000))
        continue
      }

      // 检查并发限制
      if (uploadingTasks.length >= config.concurrency) {
        await new Promise(resolve => setTimeout(resolve, 1000))
        continue
      }

      // 开始下一个任务
      const task = pendingTasks[0]
      await uploadTask(task)
    }
  }

  // 上传单个任务
  const uploadTask = async (task: UploadTask) => {
    if (task.status !== TaskStatus.PENDING) return

    task.status = TaskStatus.UPLOADING
    task.startedAt = new Date()
    emit(EventType.TASK_STARTED, task.id, task)

    try {
      // 使用已封装的上传接口，带进度回调
      const response = await projectFileApi.uploadFile(
        task.projectId,
        task.file,
        {
          projectId: task.projectId,
          fileType: task.fileType,
          description: task.description,
          isPrimary: task.isPrimary
        },
        (progress: number) => {
          // 更新任务进度
          task.progress = progress

          // 计算上传速度
          const now = Date.now()
          const elapsed = (now - (task.startedAt?.getTime() || now)) / 1000
          if (elapsed > 0) {
            task.uploadedBytes = (task.file.size * progress) / 100
            task.speed = task.uploadedBytes / elapsed
          }

          // 发射进度事件
          emit(EventType.TASK_PROGRESS, task.id, task, { progress, speed: task.speed })
        }
      )

      if (response && response.code === 200 && response.data) {
        task.status = TaskStatus.COMPLETED
        task.progress = 100
        task.completedAt = new Date()
        task.result = response.data

        emit(EventType.TASK_COMPLETED, task.id, task)

        // 自动移除已完成任务
        if (config.autoRemoveCompleted) {
          setTimeout(() => {
            removeTask(task.id)
          }, config.autoRemoveDelay)
        }
      } else {
        throw new Error(response?.message || '上传失败')
      }
    } catch (error: unknown) {
      task.status = TaskStatus.FAILED

      // 处理错误信息
      if (error instanceof Error) {
        task.error = error.message
      } else if (typeof error === 'object' && error !== null && 'response' in error) {
        const axiosError = error as { response?: { data?: { message?: string } } }
        task.error = axiosError.response?.data?.message || '上传失败'
      } else {
        task.error = '上传失败'
      }

      emit(EventType.TASK_FAILED, task.id, task)

      // 重试逻辑
      if (task.retryCount < task.maxRetries) {
        setTimeout(() => {
          retryTask(task.id)
        }, config.retryDelay)
      }
    }
  }

  // 重试任务
  const retryTask = async (taskId: string) => {
    const task = tasks.value.find(t => t.id === taskId)
    if (!task || task.status !== TaskStatus.FAILED) return

    task.retryCount++
    task.status = TaskStatus.PENDING
    task.progress = 0
    task.speed = 0
    task.uploadedBytes = 0
    task.error = undefined

    emit(EventType.TASK_RETRIED, task.id, task)

    // 如果队列正在运行，任务会自动被处理
    if (!isRunning.value) {
      await startQueue()
    }
  }

  // 取消任务
  const cancelTask = (taskId: string) => {
    const task = tasks.value.find(t => t.id === taskId)
    if (!task) return

    if (task.status === TaskStatus.UPLOADING) {
      // TODO: 实现取消正在上传的任务
    }

    task.status = TaskStatus.CANCELLED
    emit(EventType.TASK_CANCELLED, task.id, task)
  }

  // 移除任务
  const removeTask = (taskId: string) => {
    const index = tasks.value.findIndex(t => t.id === taskId)
    if (index > -1) {
      const task = tasks.value[index]

      // 如果任务正在上传，先取消
      if (task.status === TaskStatus.UPLOADING) {
        cancelTask(taskId)
      }

      tasks.value.splice(index, 1)
    }
  }

  // 清空已完成任务
  const clearCompleted = () => {
    tasks.value = tasks.value.filter(t => t.status !== TaskStatus.COMPLETED)
  }

  // 清空失败任务
  const clearFailed = () => {
    tasks.value = tasks.value.filter(t => t.status !== TaskStatus.FAILED)
  }

  // 清空所有任务
  const clearAll = () => {
    stopQueue()
    tasks.value = []
  }

  // 获取任务
  const getTask = (taskId: string) => {
    return tasks.value.find(t => t.id === taskId)
  }

  // 获取项目的所有任务
  const getProjectTasks = (projectId: number) => {
    return tasks.value.filter(t => t.projectId === projectId)
  }

  return {
    // 状态
    tasks,
    config,
    isRunning,
    isPaused,

    // 计算属性
    stats,
    activeTasks,
    completedTasks,
    failedTasks,

    // 方法
    addTask,
    addBatchTasks,
    startQueue,
    pauseQueue,
    resumeQueue,
    stopQueue,
    uploadTask,
    retryTask,
    cancelTask,
    removeTask,
    clearCompleted,
    clearFailed,
    clearAll,
    getTask,
    getProjectTasks,
    on,
    off
  }
})
