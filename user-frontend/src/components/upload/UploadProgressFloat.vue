<template>
  <Teleport to="body">
    <Transition name="upload-float">
      <div
        v-if="shouldShow"
        class="upload-progress-float"
        :class="{ 'minimized': isMinimized }"
      >
        <!-- 头部 -->
        <div class="float-header" @click="toggleMinimized">
          <div class="header-left">
            <el-icon class="upload-icon">
              <Upload />
            </el-icon>
            <div class="header-info">
              <div class="header-title">
                {{ isMinimized ? '上传中' : '文件上传' }}
                <span class="task-count">({{ stats.uploading + stats.pending }})</span>
              </div>
              <div v-if="!isMinimized" class="header-subtitle">
                {{ getStatusText() }}
              </div>
            </div>
          </div>
          <div class="header-actions">
            <el-button
              v-if="!isMinimized"
              :icon="uploadStore.isPaused ? VideoPlay : VideoPause"
              @click.stop="togglePause"
              size="small"
              circle
              :disabled="stats.uploading === 0 && stats.pending === 0"
            />
            <el-button
              :icon="isMinimized ? ArrowUp : ArrowDown"
              @click.stop="toggleMinimized"
              size="small"
              circle
            />
            <el-button
              :icon="Close"
              @click.stop="handleClose"
              size="small"
              circle
            />
          </div>
        </div>

        <!-- 内容区域 -->
        <div v-if="!isMinimized" class="float-content">
          <!-- 总体进度 -->
          <div v-if="activeTasks.length > 0" class="overall-progress">
            <el-progress
              :percentage="stats.overallProgress"
              :status="getProgressStatus()"
              :stroke-width="6"
              :show-text="false"
            />
            <div class="progress-info">
              <span class="progress-text">{{ stats.overallProgress }}%</span>
              <span class="speed-text">{{ formatSpeed(stats.totalSpeed) }}</span>
            </div>
          </div>

          <!-- 任务列表 -->
          <div class="task-list">
            <div
              v-for="task in displayTasks"
              :key="task.id"
              class="task-item"
              :class="getTaskStatusClass(task.status)"
            >
              <div class="task-info">
                <div class="task-name">{{ task.file.name }}</div>
                <div class="task-meta">
                  <span class="file-type">{{ getFileTypeText(task.fileType) }}</span>
                  <span class="file-size">{{ formatFileSize(task.file.size) }}</span>
                  <span class="task-status">{{ getTaskStatusText(task.status) }}</span>
                </div>
              </div>
              
              <div class="task-progress">
                <el-progress
                  v-if="task.status === 'uploading'"
                  :percentage="task.progress"
                  :stroke-width="4"
                  :show-text="false"
                />
                <div v-else class="task-status-icon">
                  <el-icon v-if="task.status === 'completed'" class="success-icon">
                    <Check />
                  </el-icon>
                  <el-icon v-else-if="task.status === 'failed'" class="error-icon">
                    <Close />
                  </el-icon>
                  <el-icon v-else-if="task.status === 'paused'" class="pause-icon">
                    <VideoPause />
                  </el-icon>
                  <el-icon v-else class="pending-icon">
                    <Clock />
                  </el-icon>
                </div>
              </div>

              <div class="task-actions">
                <el-button
                  v-if="task.status === 'failed'"
                  :icon="Refresh"
                  @click="retryTask(task.id)"
                  size="small"
                  circle
                  type="primary"
                />
                <el-button
                  v-if="task.status === 'uploading' || task.status === 'pending'"
                  :icon="Close"
                  @click="cancelTask(task.id)"
                  size="small"
                  circle
                  type="danger"
                />
                <el-button
                  v-if="task.status === 'completed' || task.status === 'failed' || task.status === 'cancelled'"
                  :icon="Delete"
                  @click="removeTask(task.id)"
                  size="small"
                  circle
                />
              </div>
            </div>
          </div>

          <!-- 底部操作 -->
          <div v-if="activeTasks.length > 0" class="float-footer">
            <el-button
              v-if="stats.completed > 0"
              @click="clearCompleted"
              size="small"
              type="info"
            >
              清除已完成 ({{ stats.completed }})
            </el-button>
            <el-button
              v-if="stats.failed > 0"
              @click="clearFailed"
              size="small"
              type="warning"
            >
              清除失败 ({{ stats.failed }})
            </el-button>
            <el-button
              @click="clearAll"
              size="small"
              type="danger"
            >
              清除全部
            </el-button>
          </div>
        </div>

        <!-- 最小化状态的进度条 -->
        <div v-if="isMinimized && activeTasks.length > 0" class="minimized-progress">
          <el-progress
            :percentage="stats.overallProgress"
            :status="getProgressStatus()"
            :stroke-width="3"
            :show-text="false"
          />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Upload,
  ArrowUp,
  ArrowDown,
  Close,
  VideoPlay,
  VideoPause,
  Check,
  Clock,
  Refresh,
  Delete
} from '@element-plus/icons-vue'
import { useUploadStore } from '@/stores/upload'
import { UploadTaskStatus, FileType, UploadEventType } from '@/types/upload'

// Store
const uploadStore = useUploadStore()

// 响应式数据
const isMinimized = ref(false)
const maxDisplayTasks = 5

// 计算属性
const stats = computed(() => uploadStore.stats)
const activeTasks = computed(() => uploadStore.activeTasks)
const completedTasks = computed(() => uploadStore.completedTasks)
const failedTasks = computed(() => uploadStore.failedTasks)

const shouldShow = computed(() => {
  return activeTasks.value.length > 0 || 
         completedTasks.value.length > 0 || 
         failedTasks.value.length > 0
})

const displayTasks = computed(() => {
  const tasks = [...activeTasks.value, ...completedTasks.value, ...failedTasks.value]
  return tasks.slice(0, maxDisplayTasks)
})

// 工具函数
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatSpeed = (bytesPerSecond: number): string => {
  if (bytesPerSecond === 0) return ''
  const k = 1024
  const sizes = ['B/s', 'KB/s', 'MB/s', 'GB/s']
  const i = Math.floor(Math.log(bytesPerSecond) / Math.log(k))
  return parseFloat((bytesPerSecond / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const getFileTypeText = (fileType: FileType): string => {
  const typeMap = {
    [FileType.SOURCE]: '源码',
    [FileType.COVER]: '封面',
    [FileType.DEMO]: '演示',
    [FileType.DOCUMENT]: '文档'
  }
  return typeMap[fileType] || '未知'
}

const getTaskStatusText = (status: UploadTaskStatus): string => {
  const statusMap = {
    [UploadTaskStatus.PENDING]: '等待中',
    [UploadTaskStatus.UPLOADING]: '上传中',
    [UploadTaskStatus.COMPLETED]: '已完成',
    [UploadTaskStatus.FAILED]: '失败',
    [UploadTaskStatus.PAUSED]: '已暂停',
    [UploadTaskStatus.CANCELLED]: '已取消'
  }
  return statusMap[status] || '未知'
}

const getTaskStatusClass = (status: UploadTaskStatus): string => {
  return `task-${status.toLowerCase()}`
}

const getProgressStatus = () => {
  if (stats.value.failed > 0) return 'exception'
  if (stats.value.uploading > 0) return undefined
  if (stats.value.completed > 0) return 'success'
  return undefined
}

const getStatusText = (): string => {
  if (uploadStore.isPaused) {
    return '已暂停'
  }
  if (stats.value.uploading > 0) {
    return `正在上传 ${stats.value.uploading} 个文件`
  }
  if (stats.value.pending > 0) {
    return `等待上传 ${stats.value.pending} 个文件`
  }
  if (stats.value.completed > 0) {
    return `已完成 ${stats.value.completed} 个文件`
  }
  return '无任务'
}

// 事件处理
const toggleMinimized = () => {
  isMinimized.value = !isMinimized.value
}

const togglePause = () => {
  if (uploadStore.isPaused) {
    uploadStore.resumeQueue()
  } else {
    uploadStore.pauseQueue()
  }
}

const handleClose = async () => {
  if (stats.value.uploading > 0) {
    try {
      await ElMessageBox.confirm(
        '还有文件正在上传中，确定要关闭上传面板吗？',
        '确认关闭',
        { type: 'warning' }
      )
      uploadStore.stopQueue()
    } catch {
      return
    }
  }
  
  // 清除所有已完成和失败的任务
  uploadStore.clearCompleted()
  uploadStore.clearFailed()
}

const retryTask = (taskId: string) => {
  uploadStore.retryTask(taskId)
}

const cancelTask = (taskId: string) => {
  uploadStore.cancelTask(taskId)
}

const removeTask = (taskId: string) => {
  uploadStore.removeTask(taskId)
}

const clearCompleted = () => {
  uploadStore.clearCompleted()
}

const clearFailed = () => {
  uploadStore.clearFailed()
}

const clearAll = async () => {
  if (stats.value.uploading > 0) {
    try {
      await ElMessageBox.confirm(
        '还有文件正在上传中，确定要清除所有任务吗？',
        '确认清除',
        { type: 'warning' }
      )
    } catch {
      return
    }
  }
  
  uploadStore.clearAll()
}

// 监听上传完成事件
onMounted(() => {
  uploadStore.on(UploadEventType.QUEUE_COMPLETED, () => {
    ElMessage.success('所有文件上传完成')
  })
})
</script>

<style scoped>
.upload-progress-float {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 400px;
  max-height: 600px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border: 1px solid #e4e7ed;
  z-index: 2000;
  overflow: hidden;
  transition: all 0.3s ease;
}

.upload-progress-float.minimized {
  width: 300px;
  max-height: 80px;
}

.float-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  cursor: pointer;
  user-select: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.upload-icon {
  color: #409eff;
  font-size: 20px;
}

.header-info {
  flex: 1;
}

.header-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.task-count {
  color: #909399;
  font-weight: normal;
}

.header-subtitle {
  font-size: 12px;
  color: #606266;
}

.header-actions {
  display: flex;
  gap: 4px;
}

.float-content {
  max-height: 500px;
  overflow-y: auto;
}

.overall-progress {
  padding: 16px;
  border-bottom: 1px solid #f0f2f5;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;
}

.progress-text {
  color: #303133;
  font-weight: 600;
}

.speed-text {
  color: #909399;
}

.task-list {
  max-height: 300px;
  overflow-y: auto;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f5f7fa;
  transition: background-color 0.2s ease;
}

.task-item:hover {
  background: #f8f9fa;
}

.task-item:last-child {
  border-bottom: none;
}

.task-info {
  flex: 1;
  min-width: 0;
}

.task-name {
  font-size: 13px;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-meta {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: #909399;
}

.task-progress {
  width: 60px;
  margin: 0 12px;
}

.task-status-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
}

.success-icon {
  color: #67c23a;
}

.error-icon {
  color: #f56c6c;
}

.pause-icon {
  color: #e6a23c;
}

.pending-icon {
  color: #909399;
}

.task-actions {
  display: flex;
  gap: 4px;
}

.task-uploading {
  background: #f0f9ff;
}

.task-completed {
  background: #f0f9ff;
}

.task-failed {
  background: #fef0f0;
}

.task-paused {
  background: #fdf6ec;
}

.task-cancelled {
  background: #f5f7fa;
  opacity: 0.7;
}

.float-footer {
  padding: 12px 16px;
  border-top: 1px solid #f0f2f5;
  background: #fafafa;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.minimized-progress {
  padding: 8px 16px;
}

/* 动画效果 */
.upload-float-enter-active,
.upload-float-leave-active {
  transition: all 0.3s ease;
}

.upload-float-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.upload-float-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* 滚动条样式 */
.float-content::-webkit-scrollbar,
.task-list::-webkit-scrollbar {
  width: 6px;
}

.float-content::-webkit-scrollbar-track,
.task-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.float-content::-webkit-scrollbar-thumb,
.task-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.float-content::-webkit-scrollbar-thumb:hover,
.task-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .upload-progress-float {
    width: calc(100vw - 40px);
    right: 20px;
    left: 20px;
  }

  .upload-progress-float.minimized {
    width: calc(100vw - 40px);
  }
}
</style>
