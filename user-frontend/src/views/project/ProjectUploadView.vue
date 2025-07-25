<template>
  <div class="project-upload-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Upload /></el-icon>
          上传项目
        </h1>
        <p class="page-description">
          分享您的优秀项目，让更多开发者受益
        </p>
      </div>
    </div>

    <!-- 上传步骤指示器 -->
    <div class="upload-steps">
      <el-steps :active="currentStep" align-center>
        <el-step title="项目信息" description="填写项目基本信息" />
        <el-step title="文件上传" description="上传项目文件" />
        <el-step title="预览确认" description="预览并确认发布" />
      </el-steps>
    </div>

    <!-- 主要内容区域 -->
    <div class="upload-content">
      <!-- 步骤1：项目信息 -->
      <div v-show="currentStep === 0" class="step-content">
        <div class="step-header">
          <h2>项目信息</h2>
          <p>请详细填写项目信息，这将帮助用户更好地了解您的项目</p>
        </div>
        
        <ProjectForm
          v-model="projectData"
          @submit="handleProjectSubmit"
          @cancel="handleCancel"
        />
        
        <div class="step-actions">
          <el-button type="primary" @click="nextStep" :disabled="!isProjectDataValid">
            下一步：上传文件
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 步骤2：文件上传 -->
      <div v-show="currentStep === 1" class="step-content">
        <div class="step-header">
          <h2>文件上传</h2>
          <p>请上传项目相关文件，包括源码、封面图片、演示文件等</p>
        </div>

        <!-- 文件上传提示 -->
        <el-alert
          title="上传提示"
          type="info"
          :closable="false"
          class="upload-tips"
        >
          <ul>
            <li>源码文件：支持 .zip、.rar、.tar.gz、.7z 格式，建议包含完整的项目结构</li>
            <li>封面图片：支持 .jpg、.png、.gif 格式，建议尺寸 800x600 像素</li>
            <li>演示文件：可上传项目截图、演示视频等</li>
            <li>文档文件：可上传项目说明文档、API文档等</li>
          </ul>
        </el-alert>

        <!-- 文件上传组件 -->
        <div class="upload-section">
          <ProjectFileUpload
            v-if="createdProjectId"
            :project-id="createdProjectId"
            :allow-multiple="true"
            :max-size="100"
            @success="handleFileUploadSuccess"
            @error="handleFileUploadError"
            @progress="handleFileUploadProgress"
          />
        </div>

        <!-- 已上传文件列表 -->
        <div v-if="uploadedFiles.length > 0" class="uploaded-files">
          <h3>已上传文件</h3>
          <div class="file-list">
            <div 
              v-for="file in uploadedFiles" 
              :key="file.id"
              class="file-item"
            >
              <div class="file-info">
                <el-icon class="file-icon">
                  <Document />
                </el-icon>
                <div class="file-details">
                  <div class="file-name">{{ file.originalName }}</div>
                  <div class="file-meta">
                    <span class="file-type">{{ file.fileTypeDesc }}</span>
                    <span class="file-size">{{ file.readableFileSize }}</span>
                    <span class="file-status" :class="getStatusClass(file.fileStatus)">
                      {{ file.fileStatusDesc }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="file-actions">
                <el-button
                  type="danger"
                  size="small"
                  :icon="Delete"
                  @click="handleDeleteFile(file.id)"
                  circle
                />
              </div>
            </div>
          </div>
        </div>

        <div class="step-actions">
          <el-button @click="handlePrevStep">
            <el-icon><ArrowLeft /></el-icon>
            上一步
          </el-button>
          <el-button
            type="primary"
            @click="handleNextStep"
            :disabled="!hasRequiredFiles"
          >
            下一步：预览确认
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 步骤3：预览确认 -->
      <div v-show="currentStep === 2" class="step-content">
        <div class="step-header">
          <h2>预览确认</h2>
          <p>请确认项目信息和文件无误后发布</p>
        </div>

        <!-- 项目预览 -->
        <div class="project-preview">
          <div class="preview-card">
            <div class="preview-header">
              <h3>{{ projectData.title }}</h3>
              <div class="preview-meta">
                <el-tag type="primary">{{ getCategoryName(projectData.categoryId) }}</el-tag>
                <el-tag v-if="!projectData.price || projectData.price === 0" type="success">免费</el-tag>
                <el-tag v-else type="warning">{{ projectData.price }} 积分</el-tag>
              </div>
            </div>
            
            <div class="preview-content">
              <p class="preview-description">{{ projectData.description }}</p>
              
              <div v-if="projectData.techStack?.length" class="preview-tech">
                <strong>技术栈：</strong>
                <el-tag 
                  v-for="tech in projectData.techStack" 
                  :key="tech"
                  size="small"
                  class="tech-tag"
                >
                  {{ tech }}
                </el-tag>
              </div>
              
              <!-- 移除特性显示，因为已简化 -->
            </div>
          </div>

          <!-- 文件预览 -->
          <div class="files-preview">
            <h4>项目文件 ({{ uploadedFiles.length }})</h4>
            <div class="files-summary">
              <div class="file-type-count">
                <span>源码文件: {{ getFileCountByType('SOURCE') }}</span>
                <span>封面图片: {{ getFileCountByType('COVER') }}</span>
                <span>演示文件: {{ getFileCountByType('DEMO') }}</span>
                <span>文档文件: {{ getFileCountByType('DOCUMENT') }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="step-actions">
          <el-button @click="handlePrevStep">
            <el-icon><ArrowLeft /></el-icon>
            上一步
          </el-button>
          <el-button
            type="primary"
            @click="handlePublish"
            :loading="isPublishing"
          >
            <el-icon><Check /></el-icon>
            保存项目
          </el-button>
        </div>
      </div>
    </div>

    <!-- 上传进度对话框 -->
    <el-dialog
      v-model="showProgressDialog"
      title="上传进度"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      width="400px"
    >
      <div class="progress-content">
        <el-progress
          :percentage="uploadProgress.overall"
          :status="uploadState.isUploadingFiles ? undefined : 'success'"
          :stroke-width="8"
        />
        <p class="progress-text">{{ progressText }}</p>
        <p v-if="uploadProgress.currentFile" class="current-file">
          正在上传: {{ uploadProgress.currentFile }}
        </p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Upload,
  ArrowRight,
  ArrowLeft,
  Document,
  Delete,
  Check
} from '@element-plus/icons-vue'
import ProjectForm from '@/components/project/ProjectForm.vue'
import ProjectFileUpload from '@/components/project/ProjectFileUpload.vue'
import { useProjectUpload } from '@/composables/useProjectUpload'
import { publicProjectApi } from '@/api/modules/public'
import type { ProjectUploadRequest, ProjectFile } from '@/types/project'
import type { ProjectCategory } from '@/api/modules/public'

// 路由
const router = useRouter()

// 使用项目上传组合式函数
const {
  uploadState,
  projectData,
  uploadResult,
  uploadProgress,
  isProjectDataValid,
  hasRequiredFiles,
  canProceedToNextStep,
  uploadSummary,
  nextStep,
  prevStep,
  createProject,
  uploadFiles,
  deleteFile,
  publishProject,
  resetUpload,
  getUploadStatus
} = useProjectUpload()

// 其他响应式数据
const categories = ref<ProjectCategory[]>([])
const showProgressDialog = ref(false)

// 计算属性
const currentStep = computed(() => uploadState.currentStep)
const createdProjectId = computed(() => uploadResult.projectId)
const uploadedFiles = computed(() => uploadResult.uploadedFiles)
const isPublishing = computed(() => uploadState.isPublishing)

const progressText = computed(() => {
  if (uploadProgress.overall === 100) {
    return '上传完成'
  }
  return `上传进度: ${uploadProgress.overall}%`
})

// 获取分类名称
const getCategoryName = (categoryId: number) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category?.name || '未知分类'
}

// 获取文件状态样式
const getStatusClass = (status: number) => {
  switch (status) {
    case 0: return 'status-uploading'
    case 1: return 'status-uploaded'
    case 2: return 'status-processing'
    case 3: return 'status-processed'
    case 4: return 'status-failed'
    case 5: return 'status-deleted'
    default: return ''
  }
}

// 获取指定类型文件数量
const getFileCountByType = (fileType: string) => {
  return uploadedFiles.value.filter(file => file.fileType === fileType).length
}

// 步骤控制方法（使用组合式函数中的方法）
const handleNextStep = async () => {
  const success = await nextStep()
  if (!success) {
    console.log('进入下一步失败')
  }
}

const handlePrevStep = () => {
  prevStep()
}

// 处理项目表单提交
const handleProjectSubmit = (data: ProjectUploadRequest) => {
  console.log('处理项目表单提交:', data)
  // 更新项目数据
  Object.assign(projectData.value, data)
  // 进入下一步
  handleNextStep()
}

// 处理文件上传成功
const handleFileUploadSuccess = (files: any[]) => {
  // 文件上传成功的处理已经在组合式函数中完成
  console.log('文件上传成功:', files)
}

// 处理文件上传错误
const handleFileUploadError = (error: string) => {
  console.error('文件上传错误:', error)
}

// 处理文件上传进度
const handleFileUploadProgress = (progress: number) => {
  console.log('文件上传进度:', progress)
}

// 删除文件（使用组合式函数中的方法）
const handleDeleteFile = async (fileId: number) => {
  try {
    await deleteFile(fileId)
  } catch (error) {
    console.error('删除文件失败:', error)
  }
}

// 发布项目（使用组合式函数中的方法）
const handlePublish = async () => {
  try {
    await publishProject()

    // 跳转到我的项目页面
    router.push('/user/my-projects')
  } catch (error: any) {
    console.error('发布项目失败:', error)
  }
}

// 取消操作
const handleCancel = () => {
  router.back()
}

// 加载分类数据
const loadCategories = async () => {
  try {
    const response = await publicProjectApi.getCategories()
    categories.value = response.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.project-upload-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.page-title {
  font-size: 32px;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.page-description {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

.upload-steps {
  margin-bottom: 40px;
}

.upload-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.step-content {
  padding: 40px;
}

.step-header {
  text-align: center;
  margin-bottom: 32px;
}

.step-header h2 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
}

.step-header p {
  color: #606266;
  margin: 0;
}

.upload-tips {
  margin-bottom: 24px;
}

.upload-tips ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.upload-tips li {
  margin-bottom: 4px;
  color: #606266;
}

.upload-section {
  margin-bottom: 32px;
}

.uploaded-files {
  margin-bottom: 32px;
}

.uploaded-files h3 {
  margin: 0 0 16px 0;
  color: #303133;
}

.file-list {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f5f7fa;
  background: white;
}

.file-item:last-child {
  border-bottom: none;
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

.file-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
}

.file-type {
  color: #409eff;
}

.file-size {
  color: #909399;
}

.file-status {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.status-uploaded {
  background: #f0f9ff;
  color: #409eff;
}

.status-processing {
  background: #fff7e6;
  color: #e6a23c;
}

.status-processed {
  background: #f0f9ff;
  color: #67c23a;
}

.status-failed {
  background: #fef0f0;
  color: #f56c6c;
}

.project-preview {
  margin-bottom: 32px;
}

.preview-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.preview-header h3 {
  margin: 0;
  color: #303133;
}

.preview-meta {
  display: flex;
  gap: 8px;
}

.preview-description {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
}

.preview-tech {
  margin-bottom: 16px;
}

.tech-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.preview-features ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.preview-features li {
  margin-bottom: 4px;
  color: #606266;
}

.files-preview h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.file-type-count {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #606266;
}

.step-actions {
  text-align: center;
  padding-top: 32px;
  border-top: 1px solid #e4e7ed;
}

.step-actions .el-button {
  margin: 0 8px;
  min-width: 120px;
}

.progress-content {
  text-align: center;
}

.progress-text {
  margin-top: 16px;
  color: #606266;
}
</style>
