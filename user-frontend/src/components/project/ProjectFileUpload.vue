<template>
  <div class="project-file-upload">
    <!-- 文件类型选择 -->
    <div class="file-type-selector mb-4">
      <el-radio-group v-model="fileType" class="file-type-group">
        <el-radio-button label="SOURCE">源码文件</el-radio-button>
        <el-radio-button label="COVER">封面图片</el-radio-button>
        <el-radio-button label="DEMO">演示文件</el-radio-button>
        <el-radio-button label="DOCUMENT">文档文件</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 拖拽上传区域 -->
    <div 
      class="upload-area"
      :class="{ 
        'is-dragover': isDragOver,
        'is-uploading': isUploading,
        'has-files': selectedFiles.length > 0
      }"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @dragenter="handleDragEnter"
      @dragleave="handleDragLeave"
      @click="triggerFileSelect"
    >
      <input
        ref="fileInput"
        type="file"
        :multiple="allowMultiple"
        :accept="acceptedTypes"
        @change="handleFileSelect"
        style="display: none"
      />

      <div v-if="selectedFiles.length === 0" class="upload-placeholder">
        <el-icon class="upload-icon" :size="48">
          <UploadFilled />
        </el-icon>
        <div class="upload-text">
          <p class="primary-text">点击或拖拽文件到此区域上传</p>
          <p class="secondary-text">
            支持 {{ acceptedTypesText }}，单个文件不超过 {{ maxSizeText }}
          </p>
        </div>
      </div>

      <!-- 文件列表 -->
      <div v-else class="file-list">
        <div 
          v-for="(file, index) in selectedFiles" 
          :key="index"
          class="file-item"
        >
          <div class="file-info">
            <el-icon class="file-icon">
              <Document />
            </el-icon>
            <div class="file-details">
              <div class="file-name">{{ file.name }}</div>
              <div class="file-size">{{ formatFileSize(file.size) }}</div>
            </div>
          </div>
          
          <div class="file-actions">
            <el-button 
              type="danger" 
              size="small" 
              :icon="Delete"
              @click.stop="removeFile(index)"
              circle
            />
          </div>
        </div>
      </div>

      <!-- 上传进度 -->
      <div v-if="isUploading" class="upload-progress">
        <el-progress
          :percentage="uploadProgress"
          :status="uploadStatus"
          :stroke-width="6"
        />
        <p class="progress-text">{{ progressText }}</p>
        <div class="upload-details">
          <span class="current-file">{{ currentUploadingFile }}</span>
          <span class="upload-speed">{{ uploadSpeed }}</span>
        </div>
      </div>

      <!-- 错误信息显示 -->
      <div v-if="uploadErrors.length > 0" class="upload-errors">
        <el-alert
          title="上传过程中遇到以下问题："
          type="warning"
          :closable="false"
          class="error-alert"
        >
          <ul class="error-list">
            <li v-for="(error, index) in uploadErrors" :key="index">
              {{ error }}
            </li>
          </ul>
        </el-alert>
      </div>
    </div>

    <!-- 文件描述 -->
    <div class="file-description mt-4">
      <el-input
        v-model="description"
        type="textarea"
        :rows="3"
        placeholder="请输入文件描述（可选）"
        maxlength="500"
        show-word-limit
      />
    </div>

    <!-- 选项设置 -->
    <div class="upload-options mt-4">
      <el-checkbox v-model="isPrimary" :disabled="fileType !== 'SOURCE'">
        设为主文件
      </el-checkbox>
    </div>

    <!-- 操作按钮 -->
    <div class="upload-actions mt-6">
      <el-button 
        type="primary" 
        :loading="isUploading"
        :disabled="selectedFiles.length === 0"
        @click="handleUpload"
      >
        <el-icon><Upload /></el-icon>
        {{ isUploading ? '上传中...' : '开始上传' }}
      </el-button>
      
      <el-button @click="clearFiles">
        清空文件
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  UploadFilled, 
  Document, 
  Delete, 
  Upload 
} from '@element-plus/icons-vue'
import { projectFileApi } from '@/api/modules/project'
import type { ProjectFileUploadRequest } from '@/types/project'

// Props
interface Props {
  projectId: number
  allowMultiple?: boolean
  maxSize?: number // MB
  acceptedTypes?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  allowMultiple: false,
  maxSize: 100,
  acceptedTypes: () => ['.zip', '.rar', '.tar.gz', '.7z', '.jpg', '.jpeg', '.png', '.gif', '.pdf', '.doc', '.docx']
})

// Emits
const emit = defineEmits<{
  success: [files: any[]]
  error: [error: string]
  progress: [progress: number]
}>()

// 响应式数据
const fileInput = ref<HTMLInputElement>()
const selectedFiles = ref<File[]>([])
const isDragOver = ref(false)
const isUploading = ref(false)
const uploadProgress = ref(0)
const uploadStatus = ref<'success' | 'exception' | undefined>()
const fileType = ref<'SOURCE' | 'COVER' | 'DEMO' | 'DOCUMENT'>('SOURCE')
const description = ref('')
const isPrimary = ref(false)
const uploadErrors = ref<string[]>([])
const currentUploadingFile = ref('')
const uploadSpeed = ref('')
const uploadStartTime = ref(0)
const uploadedBytes = ref(0)

// 计算属性
const acceptedTypesText = computed(() => {
  return props.acceptedTypes.join(', ')
})

const maxSizeText = computed(() => {
  return `${props.maxSize}MB`
})

const progressText = computed(() => {
  if (uploadProgress.value === 100) {
    return '上传完成'
  }
  if (uploadProgress.value === 0) {
    return '准备上传...'
  }
  return `上传进度: ${uploadProgress.value}%`
})

const canUpload = computed(() => {
  return selectedFiles.value.length > 0 && !isUploading.value
})

const acceptedTypes = computed(() => {
  return props.acceptedTypes.join(',')
})

// 监听文件类型变化
watch(fileType, (newType) => {
  if (newType !== 'SOURCE') {
    isPrimary.value = false
  }
})

// 文件大小格式化
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 验证文件
const validateFile = (file: File): { isValid: boolean; error?: string } => {
  // 检查文件大小
  const maxSizeBytes = props.maxSize * 1024 * 1024
  if (file.size > maxSizeBytes) {
    const error = `文件 ${file.name} 大小 ${formatFileSize(file.size)} 超过最大限制 ${props.maxSize}MB`
    return { isValid: false, error }
  }

  // 检查文件类型
  const fileName = file.name.toLowerCase()
  const isValidType = props.acceptedTypes.some(type =>
    fileName.endsWith(type.toLowerCase())
  )

  if (!isValidType) {
    const error = `文件 ${file.name} 类型不支持，支持的类型：${props.acceptedTypes.join(', ')}`
    return { isValid: false, error }
  }

  // 检查文件名长度
  if (file.name.length > 255) {
    const error = `文件 ${file.name} 名称过长，请重命名后再上传`
    return { isValid: false, error }
  }

  // 检查是否为空文件
  if (file.size === 0) {
    const error = `文件 ${file.name} 为空文件，无法上传`
    return { isValid: false, error }
  }

  return { isValid: true }
}

// 触发文件选择
const triggerFileSelect = () => {
  if (!isUploading.value) {
    fileInput.value?.click()
  }
}

// 处理文件选择
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = Array.from(target.files || [])
  addFiles(files)
}

// 拖拽事件处理
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = true
}

const handleDragEnter = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = true
}

const handleDragLeave = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = false
}

const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = false
  
  if (isUploading.value) return
  
  const files = Array.from(event.dataTransfer?.files || [])
  addFiles(files)
}

// 添加文件
const addFiles = (files: File[]) => {
  const validFiles: File[] = []
  const errors: string[] = []

  files.forEach(file => {
    const validation = validateFile(file)
    if (validation.isValid) {
      validFiles.push(file)
    } else if (validation.error) {
      errors.push(validation.error)
    }
  })

  // 显示验证错误
  if (errors.length > 0) {
    uploadErrors.value = errors
    errors.forEach(error => ElMessage.error(error))
  } else {
    uploadErrors.value = []
  }

  // 添加有效文件
  if (validFiles.length > 0) {
    if (!props.allowMultiple) {
      selectedFiles.value = validFiles.slice(0, 1)
    } else {
      selectedFiles.value.push(...validFiles)
    }

    if (validFiles.length !== files.length) {
      ElMessage.warning(`已添加 ${validFiles.length} 个有效文件，${files.length - validFiles.length} 个文件被跳过`)
    }
  }
}

// 移除文件
const removeFile = (index: number) => {
  selectedFiles.value.splice(index, 1)
}

// 清空文件
const clearFiles = () => {
  selectedFiles.value = []
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 上传文件
const handleUpload = async () => {
  if (selectedFiles.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }

  isUploading.value = true
  uploadProgress.value = 0
  uploadStatus.value = undefined
  uploadErrors.value = []
  uploadStartTime.value = Date.now()
  uploadedBytes.value = 0

  try {
    const totalFiles = selectedFiles.value.length
    let completedFiles = 0
    const results: any[] = []
    const errors: string[] = []

    for (let index = 0; index < selectedFiles.value.length; index++) {
      const file = selectedFiles.value[index]
      currentUploadingFile.value = file.name

      try {
        const options: ProjectFileUploadRequest = {
          projectId: props.projectId,
          fileType: fileType.value,
          description: description.value || undefined,
          isPrimary: isPrimary.value && index === 0 // 只有第一个文件可以设为主文件
        }

        const response = await projectFileApi.uploadFile(props.projectId, file, options)
        results.push(response.data)
        completedFiles++

        // 更新总体进度
        uploadProgress.value = Math.round((completedFiles / totalFiles) * 100)
        emit('progress', uploadProgress.value)

        // 更新上传速度
        updateUploadSpeed(file.size)

      } catch (error: unknown) {
        const errorMessage = getErrorMessage(error, file.name)
        errors.push(errorMessage)
        console.error(`文件 ${file.name} 上传失败:`, error)
      }
    }

    // 处理上传结果
    if (results.length > 0) {
      uploadStatus.value = 'success'
      ElMessage.success(`成功上传 ${results.length} 个文件`)
      emit('success', results)
    }

    if (errors.length > 0) {
      uploadErrors.value = errors
      if (results.length === 0) {
        uploadStatus.value = 'exception'
        ElMessage.error('所有文件上传失败')
      } else {
        ElMessage.warning(`${errors.length} 个文件上传失败`)
      }
    }

    // 清空表单（仅在全部成功时）
    if (errors.length === 0) {
      clearFiles()
      description.value = ''
      isPrimary.value = false
    }

  } catch (error: unknown) {
    uploadStatus.value = 'exception'
    const errorMessage = getErrorMessage(error)
    ElMessage.error(errorMessage)
    emit('error', errorMessage)
  } finally {
    isUploading.value = false
    currentUploadingFile.value = ''
    uploadSpeed.value = ''
  }
}

// 更新上传速度
const updateUploadSpeed = (fileSize: number) => {
  uploadedBytes.value += fileSize
  const elapsedTime = (Date.now() - uploadStartTime.value) / 1000 // 秒

  if (elapsedTime > 0) {
    const speed = uploadedBytes.value / elapsedTime // 字节/秒
    const speedMB = speed / (1024 * 1024) // MB/秒

    if (speedMB >= 1) {
      uploadSpeed.value = `${speedMB.toFixed(1)} MB/s`
    } else {
      const speedKB = speed / 1024 // KB/秒
      uploadSpeed.value = `${speedKB.toFixed(1)} KB/s`
    }
  }
}

// 获取错误信息
const getErrorMessage = (error: unknown, fileName?: string): string => {
  const prefix = fileName ? `文件 ${fileName} ` : ''

  if (error && typeof error === 'object' && 'response' in error) {
    const response = (error as any).response
    if (response?.data?.message) {
      return prefix + response.data.message
    }
  }

  if (error instanceof Error) {
    return prefix + error.message
  }

  return prefix + '上传失败，请重试'
}


</script>

<style scoped>
.project-file-upload {
  width: 100%;
}

.file-type-group {
  width: 100%;
}

.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.upload-area:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.upload-area.is-dragover {
  border-color: #409eff;
  background: #e6f7ff;
  transform: scale(1.02);
}

.upload-area.is-uploading {
  cursor: not-allowed;
  opacity: 0.8;
}

.upload-area.has-files {
  padding: 20px;
  text-align: left;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.upload-icon {
  color: #409eff;
}

.upload-text .primary-text {
  font-size: 16px;
  color: #303133;
  margin: 0 0 8px 0;
}

.upload-text .secondary-text {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.file-list {
  width: 100%;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 8px;
  background: white;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.file-icon {
  color: #409eff;
  font-size: 24px;
}

.file-details {
  flex: 1;
}

.file-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.upload-progress {
  width: 100%;
  margin-top: 20px;
}

.progress-text {
  text-align: center;
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.upload-actions {
  display: flex;
  gap: 12px;
}
</style>
