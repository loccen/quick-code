import { ref, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi, projectFileApi } from '@/api/modules/project'
import type { ProjectUploadRequest, ProjectFile, ProjectFileUploadRequest } from '@/types/project'

/**
 * 项目上传流程管理组合式函数
 * 提供完整的项目上传流程管理功能
 */
export function useProjectUpload() {
  // 上传状态
  const uploadState = reactive({
    currentStep: 0,
    isCreatingProject: false,
    isUploadingFiles: false,
    isPublishing: false,
    projectCreated: false,
    filesUploaded: false,
    allCompleted: false
  })

  // 项目数据
  const projectData = ref<ProjectUploadRequest>({
    title: '',
    description: '',
    categoryId: 0,
    tags: [],
    price: 0,
    demoUrl: '',
    techStack: [],
    coverImage: ''
  })

  // 上传结果
  const uploadResult = reactive({
    projectId: null as number | null,
    uploadedFiles: [] as ProjectFile[],
    errors: [] as string[],
    warnings: [] as string[]
  })

  // 上传进度
  const uploadProgress = reactive({
    overall: 0,
    currentFile: '',
    filesCompleted: 0,
    totalFiles: 0,
    speed: '',
    estimatedTime: ''
  })

  // 计算属性
  const isProjectDataValid = computed(() => {
    return projectData.value.title.trim() !== '' &&
           projectData.value.description.trim() !== '' &&
           projectData.value.categoryId > 0 &&
           projectData.value.techStack.length > 0
  })

  const hasRequiredFiles = computed(() => {
    return uploadResult.uploadedFiles.some(file => file.fileType === 'SOURCE')
  })

  const canProceedToNextStep = computed(() => {
    switch (uploadState.currentStep) {
      case 0: // 项目信息步骤
        return isProjectDataValid.value
      case 1: // 文件上传步骤
        return hasRequiredFiles.value
      default:
        return true
    }
  })

  const uploadSummary = computed(() => {
    const fileTypes = {
      SOURCE: uploadResult.uploadedFiles.filter(f => f.fileType === 'SOURCE').length,
      COVER: uploadResult.uploadedFiles.filter(f => f.fileType === 'COVER').length,
      DEMO: uploadResult.uploadedFiles.filter(f => f.fileType === 'DEMO').length,
      DOCUMENT: uploadResult.uploadedFiles.filter(f => f.fileType === 'DOCUMENT').length
    }

    return {
      totalFiles: uploadResult.uploadedFiles.length,
      fileTypes,
      totalSize: uploadResult.uploadedFiles.reduce((sum, file) => sum + file.fileSize, 0),
      hasErrors: uploadResult.errors.length > 0,
      hasWarnings: uploadResult.warnings.length > 0
    }
  })

  // 步骤管理
  const nextStep = async () => {
    if (!canProceedToNextStep.value) {
      ElMessage.warning('请完成当前步骤的必填项')
      return false
    }

    try {
      if (uploadState.currentStep === 0) {
        // 创建项目
        await createProject()
      }

      uploadState.currentStep++
      return true
    } catch (error) {
      console.error('进入下一步失败:', error)
      return false
    }
  }

  const prevStep = () => {
    if (uploadState.currentStep > 0) {
      uploadState.currentStep--
    }
  }

  const goToStep = (step: number) => {
    if (step >= 0 && step <= 2) {
      uploadState.currentStep = step
    }
  }

  // 项目创建
  const createProject = async () => {
    if (uploadState.projectCreated) {
      return uploadResult.projectId
    }

    uploadState.isCreatingProject = true
    
    try {
      // 验证项目数据
      const validationResult = validateProjectData()
      if (!validationResult.isValid) {
        throw new Error(validationResult.message)
      }

      const response = await projectApi.createProject(projectData.value)
      
      if (response && response.code === 200 && response.data) {
        uploadResult.projectId = response.data.id
        uploadState.projectCreated = true
        
        ElMessage.success('项目创建成功')
        return response.data.id
      } else {
        throw new Error(response?.message || '项目创建失败')
      }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || error.message || '项目创建失败'
      ElMessage.error(errorMessage)
      uploadResult.errors.push(errorMessage)
      throw error
    } finally {
      uploadState.isCreatingProject = false
    }
  }

  // 文件上传
  const uploadFiles = async (files: File[], fileType: string = 'SOURCE') => {
    if (!uploadResult.projectId) {
      throw new Error('请先创建项目')
    }

    uploadState.isUploadingFiles = true
    uploadProgress.totalFiles = files.length
    uploadProgress.filesCompleted = 0

    const results: ProjectFile[] = []
    const errors: string[] = []

    try {
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        uploadProgress.currentFile = file.name

        try {
          const options: ProjectFileUploadRequest = {
            projectId: uploadResult.projectId,
            fileType: fileType as any,
            isPrimary: fileType === 'SOURCE' && i === 0 // 第一个源码文件设为主文件
          }

          const response = await projectFileApi.uploadFile(uploadResult.projectId, file, options)
          
          if (response && response.code === 200 && response.data) {
            results.push(response.data as any)
            uploadProgress.filesCompleted++
            uploadProgress.overall = Math.round((uploadProgress.filesCompleted / uploadProgress.totalFiles) * 100)
          } else {
            throw new Error(response?.message || '文件上传失败')
          }
        } catch (error: any) {
          const errorMessage = `文件 ${file.name} 上传失败: ${error.response?.data?.message || error.message}`
          errors.push(errorMessage)
          console.error(errorMessage, error)
        }
      }

      // 更新上传结果
      uploadResult.uploadedFiles.push(...results)
      uploadResult.errors.push(...errors)

      if (results.length > 0) {
        ElMessage.success(`成功上传 ${results.length} 个文件`)
        uploadState.filesUploaded = true
      }

      if (errors.length > 0) {
        if (results.length === 0) {
          ElMessage.error('所有文件上传失败')
        } else {
          ElMessage.warning(`${errors.length} 个文件上传失败`)
        }
      }

      return { success: results, errors }
    } finally {
      uploadState.isUploadingFiles = false
      uploadProgress.currentFile = ''
    }
  }

  // 删除文件
  const deleteFile = async (fileId: number) => {
    if (!uploadResult.projectId) {
      throw new Error('项目ID不存在')
    }

    try {
      await ElMessageBox.confirm('确定要删除这个文件吗？', '确认删除', {
        type: 'warning'
      })

      await projectFileApi.deleteFile(uploadResult.projectId, fileId)
      
      uploadResult.uploadedFiles = uploadResult.uploadedFiles.filter(file => file.id !== fileId)
      ElMessage.success('文件删除成功')
      
      return true
    } catch (error: any) {
      if (error !== 'cancel') {
        const errorMessage = error.response?.data?.message || '删除文件失败'
        ElMessage.error(errorMessage)
        throw error
      }
      return false
    }
  }

  // 发布项目
  const publishProject = async () => {
    if (!uploadResult.projectId) {
      throw new Error('项目ID不存在')
    }

    if (!hasRequiredFiles.value) {
      throw new Error('请至少上传一个源码文件')
    }

    uploadState.isPublishing = true

    try {
      // 这里可以添加项目发布的API调用
      // 目前项目创建后就是待审核状态，所以不需要额外的发布操作
      
      uploadState.allCompleted = true
      ElMessage.success('项目提交成功，等待管理员审核')
      
      return true
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || '项目发布失败'
      ElMessage.error(errorMessage)
      uploadResult.errors.push(errorMessage)
      throw error
    } finally {
      uploadState.isPublishing = false
    }
  }

  // 验证项目数据
  const validateProjectData = () => {
    const errors: string[] = []

    if (!projectData.value.title.trim()) {
      errors.push('项目标题不能为空')
    }

    if (!projectData.value.description.trim()) {
      errors.push('项目描述不能为空')
    }

    if (projectData.value.categoryId <= 0) {
      errors.push('请选择项目分类')
    }

    if (projectData.value.techStack.length === 0) {
      errors.push('请至少选择一个技术栈')
    }

    if (projectData.value.price < 0) {
      errors.push('项目价格不能为负数')
    }

    return {
      isValid: errors.length === 0,
      message: errors.join('; ')
    }
  }

  // 重置上传状态
  const resetUpload = () => {
    uploadState.currentStep = 0
    uploadState.isCreatingProject = false
    uploadState.isUploadingFiles = false
    uploadState.isPublishing = false
    uploadState.projectCreated = false
    uploadState.filesUploaded = false
    uploadState.allCompleted = false

    uploadResult.projectId = null
    uploadResult.uploadedFiles = []
    uploadResult.errors = []
    uploadResult.warnings = []

    uploadProgress.overall = 0
    uploadProgress.currentFile = ''
    uploadProgress.filesCompleted = 0
    uploadProgress.totalFiles = 0
    uploadProgress.speed = ''
    uploadProgress.estimatedTime = ''
  }

  // 获取上传状态摘要
  const getUploadStatus = () => {
    return {
      step: uploadState.currentStep,
      isLoading: uploadState.isCreatingProject || uploadState.isUploadingFiles || uploadState.isPublishing,
      canProceed: canProceedToNextStep.value,
      projectCreated: uploadState.projectCreated,
      filesUploaded: uploadState.filesUploaded,
      allCompleted: uploadState.allCompleted,
      summary: uploadSummary.value
    }
  }

  return {
    // 状态
    uploadState,
    projectData,
    uploadResult,
    uploadProgress,

    // 计算属性
    isProjectDataValid,
    hasRequiredFiles,
    canProceedToNextStep,
    uploadSummary,

    // 方法
    nextStep,
    prevStep,
    goToStep,
    createProject,
    uploadFiles,
    deleteFile,
    publishProject,
    validateProjectData,
    resetUpload,
    getUploadStatus
  }
}
