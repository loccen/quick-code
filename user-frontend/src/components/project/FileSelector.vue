<template>
  <div class="file-selector">
    <div class="file-type-tabs">
      <el-tabs v-model="activeFileType" @tab-change="handleTabChange">
        <el-tab-pane label="源码文件" name="SOURCE">
          <div class="file-upload-area">
            <el-upload
              ref="sourceUploadRef"
              :file-list="sourceFiles"
              :auto-upload="false"
              :multiple="false"
              :accept="sourceAcceptTypes"
              :before-upload="beforeSourceUpload"
              @change="handleSourceFileChange"
              @remove="handleSourceFileRemove"
              drag
              class="upload-dragger"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将源码文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 .zip、.rar、.tar.gz、.7z 格式，文件大小不超过 100MB
                </div>
              </template>
            </el-upload>
          </div>
        </el-tab-pane>

        <el-tab-pane label="封面图片" name="COVER">
          <div class="file-upload-area">
            <el-upload
              ref="coverUploadRef"
              :file-list="coverFiles"
              :auto-upload="false"
              :multiple="false"
              :accept="imageAcceptTypes"
              :before-upload="beforeImageUpload"
              @change="handleCoverFileChange"
              @remove="handleCoverFileRemove"
              drag
              class="upload-dragger"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将封面图片拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 .jpg、.png、.gif、.svg 格式，建议尺寸 800x600 像素，文件大小不超过 10MB
                </div>
              </template>
            </el-upload>
          </div>
        </el-tab-pane>

        <el-tab-pane label="演示文件" name="DEMO">
          <div class="file-upload-area">
            <el-upload
              ref="demoUploadRef"
              :file-list="demoFiles"
              :auto-upload="false"
              :multiple="true"
              :accept="demoAcceptTypes"
              :before-upload="beforeDemoUpload"
              @change="handleDemoFileChange"
              @remove="handleDemoFileRemove"
              drag
              class="upload-dragger"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将演示文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持图片、视频等格式，可上传多个文件，单个文件不超过 50MB
                </div>
              </template>
            </el-upload>
          </div>
        </el-tab-pane>

        <el-tab-pane label="文档说明" name="DOCUMENT">
          <div class="file-upload-area">
            <el-upload
              ref="documentUploadRef"
              :file-list="documentFiles"
              :auto-upload="false"
              :multiple="true"
              :accept="documentAcceptTypes"
              :before-upload="beforeDocumentUpload"
              @change="handleDocumentFileChange"
              @remove="handleDocumentFileRemove"
              drag
              class="upload-dragger"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将文档文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 .pdf、.doc、.docx、.md、.txt 格式，可上传多个文件，单个文件不超过 20MB
                </div>
              </template>
            </el-upload>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 文件列表摘要 -->
    <div v-if="hasFiles" class="files-summary">
      <h4>已选择文件</h4>
      <div class="summary-stats">
        <el-tag v-if="sourceFiles.length" type="primary">
          源码文件: {{ sourceFiles.length }}
        </el-tag>
        <el-tag v-if="coverFiles.length" type="success">
          封面图片: {{ coverFiles.length }}
        </el-tag>
        <el-tag v-if="demoFiles.length" type="warning">
          演示文件: {{ demoFiles.length }}
        </el-tag>
        <el-tag v-if="documentFiles.length" type="info">
          文档文件: {{ documentFiles.length }}
        </el-tag>
      </div>
      <div class="total-size">
        总大小: {{ formatFileSize(totalSize) }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, type UploadFile, type UploadFiles } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { FileType } from '@/types/upload'

// Props
interface Props {
  modelValue?: {
    [FileType.SOURCE]: File[]
    [FileType.COVER]: File[]
    [FileType.DEMO]: File[]
    [FileType.DOCUMENT]: File[]
  }
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({
    [FileType.SOURCE]: [],
    [FileType.COVER]: [],
    [FileType.DEMO]: [],
    [FileType.DOCUMENT]: []
  })
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: {
    [FileType.SOURCE]: File[]
    [FileType.COVER]: File[]
    [FileType.DEMO]: File[]
    [FileType.DOCUMENT]: File[]
  }]
  change: [files: {
    [FileType.SOURCE]: File[]
    [FileType.COVER]: File[]
    [FileType.DEMO]: File[]
    [FileType.DOCUMENT]: File[]
  }]
}>()

// 响应式数据
const activeFileType = ref<FileType>(FileType.SOURCE)
const sourceFiles = ref<UploadFile[]>([])
const coverFiles = ref<UploadFile[]>([])
const demoFiles = ref<UploadFile[]>([])
const documentFiles = ref<UploadFile[]>([])

// 文件类型限制
const sourceAcceptTypes = '.zip,.rar,.tar.gz,.7z'
const imageAcceptTypes = '.jpg,.jpeg,.png,.gif,.svg'
const demoAcceptTypes = '.jpg,.jpeg,.png,.gif,.mp4,.avi,.mov,.wmv'
const documentAcceptTypes = '.pdf,.doc,.docx,.md,.txt'

// 计算属性
const hasFiles = computed(() => {
  return sourceFiles.value.length > 0 ||
         coverFiles.value.length > 0 ||
         demoFiles.value.length > 0 ||
         documentFiles.value.length > 0
})

const totalSize = computed(() => {
  const allFiles = [
    ...sourceFiles.value,
    ...coverFiles.value,
    ...demoFiles.value,
    ...documentFiles.value
  ]
  return allFiles.reduce((total, file) => total + (file.size || 0), 0)
})

// 工具函数
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const convertUploadFilesToFiles = (uploadFiles: UploadFile[]): File[] => {
  const files: File[] = []
  uploadFiles.forEach(uploadFile => {
    if (uploadFile.raw && uploadFile.raw instanceof File) {
      files.push(uploadFile.raw)
    }
  })
  return files
}

// 发射更新事件
const emitUpdate = () => {
  const files = {
    [FileType.SOURCE]: convertUploadFilesToFiles(sourceFiles.value),
    [FileType.COVER]: convertUploadFilesToFiles(coverFiles.value),
    [FileType.DEMO]: convertUploadFilesToFiles(demoFiles.value),
    [FileType.DOCUMENT]: convertUploadFilesToFiles(documentFiles.value)
  }
  emit('update:modelValue', files)
  emit('change', files)
}

// 文件上传前验证
const beforeSourceUpload = (file: File) => {
  const isValidType = /\.(zip|rar|tar\.gz|7z)$/i.test(file.name)
  const isValidSize = file.size <= 100 * 1024 * 1024 // 100MB

  if (!isValidType) {
    ElMessage.error('源码文件只支持 .zip、.rar、.tar.gz、.7z 格式')
    return false
  }
  if (!isValidSize) {
    ElMessage.error('源码文件大小不能超过 100MB')
    return false
  }
  // 验证通过，返回 false 阻止自动上传（因为我们设置了 auto-upload="false"）
  return false
}

const beforeImageUpload = (file: File) => {
  const isValidType = /\.(jpg|jpeg|png|gif|svg)$/i.test(file.name)
  const isValidSize = file.size <= 10 * 1024 * 1024 // 10MB

  if (!isValidType) {
    ElMessage.error('封面图片只支持 .jpg、.png、.gif、.svg 格式')
    return false
  }
  if (!isValidSize) {
    ElMessage.error('封面图片大小不能超过 10MB')
    return false
  }
  return false // 阻止自动上传
}

const beforeDemoUpload = (file: File) => {
  const isValidSize = file.size <= 50 * 1024 * 1024 // 50MB

  if (!isValidSize) {
    ElMessage.error('演示文件大小不能超过 50MB')
    return false
  }
  return false // 阻止自动上传
}

const beforeDocumentUpload = (file: File) => {
  const isValidType = /\.(pdf|doc|docx|md|txt)$/i.test(file.name)
  const isValidSize = file.size <= 20 * 1024 * 1024 // 20MB

  if (!isValidType) {
    ElMessage.error('文档文件只支持 .pdf、.doc、.docx、.md、.txt 格式')
    return false
  }
  if (!isValidSize) {
    ElMessage.error('文档文件大小不能超过 20MB')
    return false
  }
  return false // 阻止自动上传
}

// 文件变化处理
const handleSourceFileChange = (_file: UploadFile, files: UploadFiles) => {
  sourceFiles.value = files
  emitUpdate()
}

const handleCoverFileChange = (_file: UploadFile, files: UploadFiles) => {
  coverFiles.value = files
  emitUpdate()
}

const handleDemoFileChange = (_file: UploadFile, files: UploadFiles) => {
  demoFiles.value = files
  emitUpdate()
}

const handleDocumentFileChange = (_file: UploadFile, files: UploadFiles) => {
  documentFiles.value = files
  emitUpdate()
}

// 文件移除处理
const handleSourceFileRemove = (_file: UploadFile, files: UploadFiles) => {
  sourceFiles.value = files
  emitUpdate()
}

const handleCoverFileRemove = (_file: UploadFile, files: UploadFiles) => {
  coverFiles.value = files
  emitUpdate()
}

const handleDemoFileRemove = (_file: UploadFile, files: UploadFiles) => {
  demoFiles.value = files
  emitUpdate()
}

const handleDocumentFileRemove = (_file: UploadFile, files: UploadFiles) => {
  documentFiles.value = files
  emitUpdate()
}

// 标签页切换
const handleTabChange = (name: string | number) => {
  activeFileType.value = name as FileType
}

// 监听外部数据变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    // 这里可以根据需要同步外部文件到内部状态
    // 但通常文件选择器是单向的，不需要从外部同步文件
  }
}, { immediate: true, deep: true })
</script>

<style scoped>
.file-selector {
  width: 100%;
}

.file-type-tabs {
  margin-bottom: 24px;
}

.file-upload-area {
  padding: 20px 0;
}

.upload-dragger {
  width: 100%;
}

.upload-dragger :deep(.el-upload-dragger) {
  width: 100%;
  height: 180px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.upload-dragger :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
  background: #f0f9ff;
}

.upload-dragger :deep(.el-icon--upload) {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.upload-dragger :deep(.el-upload__text) {
  color: #606266;
  font-size: 14px;
}

.upload-dragger :deep(.el-upload__text em) {
  color: #409eff;
  font-style: normal;
}

.upload-dragger :deep(.el-upload__tip) {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.files-summary {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  margin-top: 24px;
}

.files-summary h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.summary-stats {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.total-size {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}
</style>
