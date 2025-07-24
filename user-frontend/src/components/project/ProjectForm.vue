<template>
  <div class="project-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      label-position="top"
      @submit.prevent="handleSubmit"
    >
      <!-- 基本信息 -->
      <div class="form-section">
        <h3 class="section-title">基本信息</h3>
        
        <el-form-item label="项目标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="请输入项目标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您的项目功能、特点等"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="项目分类" prop="categoryId">
          <el-select
            v-model="formData.categoryId"
            placeholder="请选择项目分类"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
      </div>

      <!-- 价格设置 -->
      <div class="form-section">
        <h3 class="section-title">价格设置</h3>

        <el-form-item label="项目价格" prop="price">
          <el-input-number
            v-model="formData.price"
            :min="0"
            :max="999999"
            :step="1"
            :precision="2"
            placeholder="请输入价格（积分，0表示免费）"
            style="width: 250px"
          />
          <span class="price-unit">积分（0 = 免费项目）</span>
        </el-form-item>
      </div>

      <!-- 技术信息 -->
      <div class="form-section">
        <h3 class="section-title">技术信息</h3>
        
        <el-form-item label="技术栈" prop="techStack">
          <el-select
            v-model="formData.techStack"
            multiple
            filterable
            allow-create
            placeholder="请选择或输入技术栈"
            style="width: 100%"
          >
            <el-option
              v-for="tech in commonTechStack"
              :key="tech"
              :label="tech"
              :value="tech"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="项目标签" prop="tags">
          <el-select
            v-model="formData.tags"
            multiple
            filterable
            allow-create
            placeholder="请选择或输入项目标签"
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
      </div>

      <!-- 链接信息 -->
      <div class="form-section">
        <h3 class="section-title">链接信息</h3>

        <el-form-item label="演示地址" prop="demoUrl">
          <el-input
            v-model="formData.demoUrl"
            placeholder="请输入在线演示地址（可选）"
          />
        </el-form-item>

        <el-form-item label="封面图片" prop="coverImage">
          <el-input
            v-model="formData.coverImage"
            placeholder="请输入封面图片URL（可选）"
          />
        </el-form-item>
      </div>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button type="primary" @click="handleSubmit" :loading="isSubmitting">
          {{ isEdit ? '更新项目' : '创建项目' }}
        </el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { publicProjectApi } from '@/api/modules/public'
import type { ProjectUploadRequest } from '@/types/project'
import type { ProjectCategory } from '@/api/modules/public'

// Props
interface Props {
  modelValue?: Partial<ProjectUploadRequest>
  isEdit?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isEdit: false
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: ProjectUploadRequest]
  submit: [data: ProjectUploadRequest]
  cancel: []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const isSubmitting = ref(false)
const categories = ref<ProjectCategory[]>([])

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

// 常用选项
const commonTechStack = [
  'Vue.js', 'React', 'Angular', 'Node.js', 'Express', 'Koa',
  'Spring Boot', 'Django', 'Flask', 'Laravel', 'PHP',
  'MySQL', 'PostgreSQL', 'MongoDB', 'Redis',
  'Docker', 'Kubernetes', 'AWS', 'Azure'
]

const commonTags = [
  '前端', '后端', '全栈', '移动端', '桌面应用',
  '管理系统', '电商', '博客', '论坛', '工具',
  '游戏', '教育', '金融', '医疗', '物联网'
]

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
  ],
  coverImage: [
    {
      validator: (_rule, value, callback) => {
        if (value && value.trim() !== '') {
          const urlPattern = /^https?:\/\/.+/
          if (!urlPattern.test(value)) {
            callback(new Error('封面图片URL格式不正确，必须以http://或https://开头'))
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

// 计算属性
const isEdit = computed(() => props.isEdit)

// 移除项目属性监听，因为已简化字段

// 监听表单数据变化
watch(formData, (newData) => {
  emit('update:modelValue', newData)
}, { deep: true })

// 监听外部数据变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    Object.assign(formData, newValue)
  }
}, { immediate: true, deep: true })

// 移除项目属性更新函数，因为已简化字段

// 加载分类数据
const loadCategories = async () => {
  try {
    const response = await publicProjectApi.getCategories()
    categories.value = response.data
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    isSubmitting.value = true
    emit('submit', formData)
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    isSubmitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
}

// 取消操作
const handleCancel = () => {
  emit('cancel')
}

// 组件挂载时加载数据
onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.project-form {
  max-width: 800px;
  margin: 0 auto;
}

.form-section {
  margin-bottom: 32px;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
}

.price-unit {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.form-actions {
  text-align: center;
  padding: 24px 0;
  border-top: 1px solid #e4e7ed;
  margin-top: 32px;
}

.form-actions .el-button {
  margin: 0 8px;
  min-width: 100px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-textarea__inner) {
  resize: vertical;
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
