<template>
  <div class="project-upload-integrated" :class="{ 'edit-mode': isEditMode }">
    <!-- 页面头部 - 仅在非编辑模式下显示 -->
    <div v-if="!isEditMode" class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Upload /></el-icon>
          上传项目
        </h1>
        <p class="page-description">
          一站式项目上传，填写信息并选择文件后即可完成上传
        </p>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="upload-content">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        label-position="top"
        @submit.prevent="handleSubmit"
      >
        <!-- 项目信息部分 -->
        <div class="form-section">
          <div class="section-header">
            <h2 class="section-title">
              <el-icon><Document /></el-icon>
              项目信息
            </h2>
            <p class="section-description">请详细填写项目信息，这将帮助用户更好地了解您的项目</p>
          </div>

          <div class="form-grid">
            <el-form-item label="项目标题" prop="title" class="form-item-full">
              <el-input
                v-model="formData.title"
                placeholder="请输入项目标题"
                maxlength="100"
                show-word-limit
                size="large"
              />
            </el-form-item>

            <el-form-item label="项目描述" prop="description" class="form-item-full">
              <el-input
                v-model="formData.description"
                type="textarea"
                :rows="4"
                placeholder="请详细描述您的项目功能、特点等"
                maxlength="2000"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="项目分类" prop="categoryId" class="form-item-half">
              <CategorySelect
                v-model="formData.categoryId"
                placeholder="请选择项目分类"
                size="large"
                value-field="id"
                @change="handleCategoryChange"
              />
            </el-form-item>

            <el-form-item label="项目价格" prop="price" class="form-item-half">
              <PriceSelector
                v-model="formData.price"
                @change="handlePriceChange"
              />
            </el-form-item>

            <el-form-item label="技术栈" prop="techStack" class="form-item-full">
              <TechStackSelector
                v-model="formData.techStack"
                @change="handleTechStackChange"
              />
            </el-form-item>

            <el-form-item label="项目标签" prop="tags" class="form-item-full">
              <el-select
                v-model="formData.tags"
                multiple
                filterable
                allow-create
                placeholder="请选择或输入项目标签"
                size="large"
                style="width: 100%"
              >
                <el-option
                  v-for="tag in commonTags"
                  :key="tag"
                  :label="tag"
                  :value="tag"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="演示地址" prop="demoUrl" class="form-item-full">
              <el-input
                v-model="formData.demoUrl"
                placeholder="请输入项目演示地址（可选）"
                size="large"
              />
            </el-form-item>
          </div>
        </div>

        <!-- 文件上传部分 -->
        <div class="form-section">
          <div class="section-header">
            <h2 class="section-title">
              <el-icon><FolderOpened /></el-icon>
              项目文件
            </h2>
            <p class="section-description">请上传项目相关文件，至少需要上传一个源码文件</p>
          </div>

          <!-- 文件上传提示 -->
          <el-alert
            title="上传说明"
            type="info"
            :closable="false"
            class="upload-tips"
          >
            <ul>
              <li><strong>源码文件（必需）：</strong>支持 .zip、.rar、.tar.gz、.7z 格式，建议包含完整的项目结构</li>
              <li><strong>封面图片（推荐）：</strong>支持 .jpg、.png、.gif、.svg 格式，建议尺寸 800x600 像素</li>
              <li><strong>演示文件（可选）：</strong>可上传项目截图、演示视频等</li>
              <li><strong>文档说明（可选）：</strong>可上传项目说明文档、API文档等</li>
            </ul>
          </el-alert>

          <!-- 编辑模式下显示已有文件 -->
          <ExistingFilesList
            v-if="isEditMode && projectId"
            :project-id="projectId"
            @files-updated="handleFilesUpdated"
          />

          <FileSelector
            v-model="selectedFiles"
          />
        </div>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <el-button @click="handleCancel" size="large">
            取消
          </el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="isSubmitting"
            :disabled="!canSubmit"
            size="large"
          >
            <el-icon v-if="isEditMode"><Edit /></el-icon>
            <el-icon v-else><Upload /></el-icon>
            {{ isEditMode ? '保存修改' : '上传项目' }}
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Upload,
  Document,
  FolderOpened,
  Edit
} from '@element-plus/icons-vue'
import CategorySelect from './CategorySelect.vue'
import TechStackSelector from './TechStackSelector.vue'
import PriceSelector from './PriceSelector.vue'
import FileSelector from './FileSelector.vue'
import ExistingFilesList from './ExistingFilesList.vue'
import { useUploadStore } from '@/stores/upload'
import { projectApi } from '@/api/modules/project'
import type { ProjectUploadRequest } from '@/types/project'
import { FileType } from '@/types/upload'

// Props
interface Props {
  /** 是否为编辑模式 */
  isEditMode?: boolean
  /** 编辑时的项目ID */
  projectId?: number
  /** 编辑时的初始数据 */
  initialData?: Partial<ProjectUploadRequest>
}

const props = withDefaults(defineProps<Props>(), {
  isEditMode: false,
  projectId: undefined,
  initialData: undefined
})

// Emits
const emit = defineEmits<{
  'submit-success': [projectId: number]
  'cancel': []
}>()

// 路由
const router = useRouter()

// Store
const uploadStore = useUploadStore()

// 响应式数据
const formRef = ref<FormInstance>()
const isSubmitting = ref(false)

// 计算属性
const isEditMode = computed(() => props.isEditMode)
const projectId = computed(() => props.projectId)

// 表单数据
const formData = reactive<ProjectUploadRequest>({
  title: '',
  description: '',
  categoryId: 0,
  tags: [],
  price: 0,
  demoUrl: '',
  techStack: [],
  coverImage: ''
})

// 选择的文件
const selectedFiles = ref<{
  [FileType.SOURCE]: File[]
  [FileType.COVER]: File[]
  [FileType.DEMO]: File[]
  [FileType.DOCUMENT]: File[]
}>({
  [FileType.SOURCE]: [],
  [FileType.COVER]: [],
  [FileType.DEMO]: [],
  [FileType.DOCUMENT]: []
})

// 常用标签选项
const commonTags = [
  '前端', '后端', '全栈', '移动端', '桌面应用',
  '管理系统', '电商', '博客', '论坛', '工具',
  '游戏', '教育', '金融', '医疗', '物联网'
]

// 初始化表单数据
const initializeFormData = () => {
  if (props.initialData) {
    Object.assign(formData, {
      title: props.initialData.title || '',
      description: props.initialData.description || '',
      categoryId: props.initialData.categoryId || 0,
      tags: props.initialData.tags || [],
      price: props.initialData.price || 0,
      demoUrl: props.initialData.demoUrl || '',
      techStack: props.initialData.techStack || [],
      coverImage: props.initialData.coverImage || ''
    })
  }
}

// 监听初始数据变化
watch(() => props.initialData, () => {
  initializeFormData()
}, { immediate: true, deep: true })

// 组件挂载时初始化
onMounted(() => {
  initializeFormData()
})

// 表单验证规则
const formRules: FormRules = {
  title: [
    { required: true, message: '请输入项目标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入项目描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述长度在 10 到 2000 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择项目分类', trigger: 'change' }
  ],
  techStack: [
    { required: true, message: '请至少选择一个技术栈', trigger: 'change' },
    {
      validator: (_rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少选择一个技术栈'))
        } else if (value.length > 20) {
          callback(new Error('技术栈数量不能超过20个'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  tags: [
    {
      validator: (_rule, value, callback) => {
        if (value && value.length > 10) {
          callback(new Error('标签数量不能超过10个'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  price: [
    {
      validator: (_rule, value, callback) => {
        if (value !== undefined && value !== null) {
          if (value < 0) {
            callback(new Error('项目价格不能为负数'))
          } else if (value > 999999.99) {
            callback(new Error('项目价格不能超过999999.99'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  demoUrl: [
    {
      validator: (_rule, value, callback) => {
        if (value && value.trim() !== '') {
          const urlPattern = /^https?:\/\/.+/
          if (!urlPattern.test(value)) {
            callback(new Error('演示URL格式不正确，必须以http://或https://开头'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 工具函数
const getErrorMessage = (error: unknown): string => {
  if (error instanceof Error) {
    return error.message
  }
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    return response?.data?.message || '操作失败'
  }
  return '未知错误'
}

// 计算属性
const canSubmit = computed(() => {
  const hasTitle = formData.title.trim() !== ''
  const hasDescription = formData.description.trim() !== ''
  // 修复：categoryId现在是数字类型
  const hasCategory = formData.categoryId > 0
  const hasTechStack = formData.techStack.length > 0
  const hasSourceFiles = selectedFiles.value[FileType.SOURCE].length > 0

  console.log('canSubmit conditions:', {
    hasTitle,
    hasDescription,
    hasCategory,
    hasTechStack,
    hasSourceFiles,
    isEditMode: isEditMode.value,
    categoryId: formData.categoryId,
    sourceFilesCount: selectedFiles.value[FileType.SOURCE].length
  })

  // 编辑模式下不强制要求有源码文件（可能已经存在）
  if (isEditMode.value) {
    return hasTitle && hasDescription && hasCategory && hasTechStack
  }

  // 创建模式下需要所有必填项包括源码文件
  return hasTitle && hasDescription && hasCategory && hasTechStack && hasSourceFiles
})

// 事件处理
const handleCategoryChange = (value: string | number | null) => {
  if (typeof value === 'number') {
    formData.categoryId = value
  }
}

const handleTechStackChange = (techStack: string[]) => {
  formData.techStack = techStack
}

const handlePriceChange = (price: number) => {
  formData.price = price
}

const handleFilesUpdated = () => {
  // 文件更新后的处理逻辑，可以在这里刷新相关状态
  console.log('项目文件已更新')
}



// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    // 编辑模式下，如果没有新文件且没有现有文件，则不需要验证文件
    if (!isEditMode.value && selectedFiles.value[FileType.SOURCE].length === 0) {
      ElMessage.warning('请至少上传一个源码文件')
      return
    }

    isSubmitting.value = true

    let projectId: number

    if (isEditMode.value) {
      // 编辑模式：更新项目信息
      if (!props.projectId) {
        throw new Error('编辑模式下项目ID不能为空')
      }

      const projectResponse = await projectApi.updateProject(props.projectId, formData)

      if (!projectResponse || projectResponse.code !== 200) {
        throw new Error(projectResponse?.message || '项目更新失败')
      }

      projectId = props.projectId
      ElMessage.success('项目信息更新成功')
    } else {
      // 创建模式：创建新项目
      const projectResponse = await projectApi.createProject(formData)

      if (!projectResponse || projectResponse.code !== 200 || !projectResponse.data) {
        throw new Error(projectResponse?.message || '项目创建失败')
      }

      projectId = projectResponse.data.id
      ElMessage.success('项目创建成功，开始上传文件...')
    }

    // 2. 处理文件上传（如果有新文件）
    const hasNewFiles = Object.values(selectedFiles.value).some(files => files.length > 0)

    if (hasNewFiles) {
      const allTaskIds: string[] = []

      // 上传源码文件
      if (selectedFiles.value[FileType.SOURCE].length > 0) {
        const taskIds = uploadStore.addBatchTasks({
          projectId,
          files: selectedFiles.value[FileType.SOURCE],
          fileType: FileType.SOURCE,
          description: '项目源码',
          autoStart: false
        })
        allTaskIds.push(...taskIds)
      }

      // 上传封面图片
      if (selectedFiles.value[FileType.COVER].length > 0) {
        const taskIds = uploadStore.addBatchTasks({
          projectId,
          files: selectedFiles.value[FileType.COVER],
          fileType: FileType.COVER,
          description: '项目封面',
          autoStart: false
        })
        allTaskIds.push(...taskIds)
      }

      // 上传演示文件
      if (selectedFiles.value[FileType.DEMO].length > 0) {
        const taskIds = uploadStore.addBatchTasks({
          projectId,
          files: selectedFiles.value[FileType.DEMO],
          fileType: FileType.DEMO,
          description: '演示文件',
          autoStart: false
        })
        allTaskIds.push(...taskIds)
      }

      // 上传文档文件
      if (selectedFiles.value[FileType.DOCUMENT].length > 0) {
        const taskIds = uploadStore.addBatchTasks({
          projectId,
          files: selectedFiles.value[FileType.DOCUMENT],
          fileType: FileType.DOCUMENT,
          description: '文档说明',
          autoStart: false
        })
        allTaskIds.push(...taskIds)
      }

      // 3. 开始上传队列
      await uploadStore.startQueue()

      if (isEditMode.value) {
        ElMessage.success('项目更新成功，新文件上传已开始')
      } else {
        ElMessage.success('项目上传已开始，您可以在右下角查看上传进度')
      }
    } else if (isEditMode.value) {
      ElMessage.success('项目信息更新成功')
    }

    // 发射成功事件或跳转页面
    if (isEditMode.value) {
      emit('submit-success', projectId)
    } else {
      router.push('/user/my-projects')
    }

  } catch (error: unknown) {
    console.error('项目上传失败:', error)
    ElMessage.error(getErrorMessage(error))
  } finally {
    isSubmitting.value = false
  }
}

// 取消操作
const handleCancel = async () => {
  const hasChanges = formData.title || formData.description ||
                    Object.values(selectedFiles.value).some(files => files.length > 0)

  if (hasChanges) {
    try {
      const message = isEditMode.value
        ? '确定要取消编辑吗？未保存的修改将会丢失。'
        : '确定要取消上传吗？已填写的信息将会丢失。'

      await ElMessageBox.confirm(message, '确认取消', {
        type: 'warning'
      })

      if (isEditMode.value) {
        emit('cancel')
      } else {
        router.back()
      }
    } catch {
      // 用户取消确认
    }
  } else {
    if (isEditMode.value) {
      emit('cancel')
    } else {
      router.back()
    }
  }
}
</script>

<style scoped>
.project-upload-integrated {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.project-upload-integrated.edit-mode {
  padding: 0;
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

.upload-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.upload-content.edit-mode {
  /* 编辑模式下融入父容器，去掉重复的装饰 */
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  padding: 40px;
  margin-top: 0;
}

.form-section {
  margin-bottom: 48px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-header {
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f2f5;
}

.section-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-description {
  color: #606266;
  margin: 0;
  font-size: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.form-item-full {
  grid-column: 1 / -1;
}

.form-item-half {
  grid-column: span 1;
}

.upload-tips {
  margin-bottom: 24px;
}

.upload-tips ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.upload-tips li {
  margin-bottom: 8px;
  color: #606266;
  line-height: 1.5;
}

.form-actions {
  text-align: center;
  padding-top: 32px;
  border-top: 1px solid #e4e7ed;
  margin-top: 32px;
}

.form-actions .el-button {
  margin: 0 8px;
  min-width: 120px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .project-upload-integrated {
    padding: 16px;
  }

  .page-header {
    padding: 24px 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .upload-content {
    padding: 24px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-item-half {
    grid-column: span 1;
  }
}
</style>
