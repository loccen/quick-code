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
  return `上传进度: ${uploadProgress.value}%`
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
const validateFile = (file: File): boolean => {
  // 检查文件大小
  const maxSizeBytes = props.maxSize * 1024 * 1024
  if (file.size > maxSizeBytes) {
    ElMessage.error(`文件 ${file.name} 超过最大限制 ${props.maxSize}MB`)
    return false
  }

  // 检查文件类型
  const fileName = file.name.toLowerCase()
  const isValidType = props.acceptedTypes.some(type => 
    fileName.endsWith(type.toLowerCase())
  )
  
  if (!isValidType) {
    ElMessage.error(`文件 ${file.name} 类型不支持`)
    return false
  }

  return true
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
  const validFiles = files.filter(validateFile)
  
  if (!props.allowMultiple) {
    selectedFiles.value = validFiles.slice(0, 1)
  } else {
    selectedFiles.value.push(...validFiles)
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

  try {
    const uploadPromises = selectedFiles.value.map(async (file, index) => {
      const options: ProjectFileUploadRequest = {
        projectId: props.projectId,
        fileType: fileType.value,
        description: description.value || undefined,
        isPrimary: isPrimary.value && index === 0 // 只有第一个文件可以设为主文件
      }

      const response = await projectFileApi.uploadFile(props.projectId, file, options)
      
      // 更新进度
      uploadProgress.value = Math.round(((index + 1) / selectedFiles.value.length) * 100)
      emit('progress', uploadProgress.value)
      
      return response.data
    })

    const results = await Promise.all(uploadPromises)
    
    uploadStatus.value = 'success'
    ElMessage.success('文件上传成功')
    emit('success', results)
    
    // 清空表单
    clearFiles()
    description.value = ''
    isPrimary.value = false
    
  } catch (error: any) {
    uploadStatus.value = 'exception'
    const errorMessage = error.response?.data?.message || '文件上传失败'
    ElMessage.error(errorMessage)
    emit('error', errorMessage)
  } finally {
    isUploading.value = false
  }
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
