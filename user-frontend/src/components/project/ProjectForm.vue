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
            maxlength="1000"
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
        
        <el-form-item label="定价模式" prop="isFree">
          <el-radio-group v-model="formData.isFree">
            <el-radio :label="true">免费项目</el-radio>
            <el-radio :label="false">付费项目</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item 
          v-if="!formData.isFree" 
          label="项目价格" 
          prop="price"
        >
          <el-input-number
            v-model="formData.price"
            :min="1"
            :max="99999"
            :step="1"
            placeholder="请输入价格（积分）"
            style="width: 200px"
          />
          <span class="price-unit">积分</span>
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

        <el-form-item label="项目版本" prop="version">
          <el-input
            v-model="formData.version"
            placeholder="如：1.0.0"
            style="width: 200px"
          />
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

        <el-form-item label="文档地址" prop="documentUrl">
          <el-input
            v-model="formData.documentUrl"
            placeholder="请输入项目文档地址（可选）"
          />
        </el-form-item>

        <el-form-item label="仓库地址" prop="repositoryUrl">
          <el-input
            v-model="formData.repositoryUrl"
            placeholder="请输入源码仓库地址（可选）"
          />
        </el-form-item>
      </div>

      <!-- 项目特性 -->
      <div class="form-section">
        <h3 class="section-title">项目特性</h3>
        
        <el-form-item label="主要特性" prop="features">
          <el-select
            v-model="formData.features"
            multiple
            filterable
            allow-create
            placeholder="请输入项目的主要特性"
            style="width: 100%"
          >
            <el-option
              v-for="feature in commonFeatures"
              :key="feature"
              :label="feature"
              :value="feature"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="许可证类型" prop="licenseType">
          <el-select
            v-model="formData.licenseType"
            placeholder="请选择许可证类型"
            style="width: 200px"
          >
            <el-option label="MIT" value="MIT" />
            <el-option label="Apache 2.0" value="Apache-2.0" />
            <el-option label="GPL v3" value="GPL-3.0" />
            <el-option label="BSD 3-Clause" value="BSD-3-Clause" />
            <el-option label="商业许可" value="Commercial" />
            <el-option label="其他" value="Other" />
          </el-select>
        </el-form-item>
      </div>

      <!-- 使用说明 -->
      <div class="form-section">
        <h3 class="section-title">使用说明</h3>
        
        <el-form-item label="安装说明" prop="installInstructions">
          <el-input
            v-model="formData.installInstructions"
            type="textarea"
            :rows="3"
            placeholder="请输入项目安装说明"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="使用说明" prop="usageInstructions">
          <el-input
            v-model="formData.usageInstructions"
            type="textarea"
            :rows="3"
            placeholder="请输入项目使用说明"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="系统要求" prop="systemRequirements">
          <el-input
            v-model="formData.systemRequirements"
            type="textarea"
            :rows="2"
            placeholder="请输入最低系统要求"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </div>

      <!-- 其他设置 -->
      <div class="form-section">
        <h3 class="section-title">其他设置</h3>
        
        <el-form-item label="项目属性">
          <el-checkbox-group v-model="projectAttributes">
            <el-checkbox label="isOpenSource">开源项目</el-checkbox>
            <el-checkbox label="isCommercialUse">支持商业使用</el-checkbox>
            <el-checkbox label="publishImmediately">立即发布</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="联系方式" prop="contactInfo">
          <el-input
            v-model="formData.contactInfo"
            placeholder="请输入联系方式（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="备注信息" prop="remarks">
          <el-input
            v-model="formData.remarks"
            type="textarea"
            :rows="3"
            placeholder="其他需要说明的信息"
            maxlength="1000"
            show-word-limit
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
  price: undefined,
  isFree: true,
  demoUrl: '',
  documentUrl: '',
  repositoryUrl: '',
  techStack: [],
  features: [],
  installInstructions: '',
  usageInstructions: '',
  systemRequirements: '',
  version: '',
  isOpenSource: false,
  isCommercialUse: false,
  licenseType: '',
  contactInfo: '',
  publishImmediately: false,
  remarks: ''
})

// 项目属性（用于复选框组）
const projectAttributes = ref<string[]>([])

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

const commonFeatures = [
  '响应式设计', '用户认证', '权限管理', '数据可视化',
  '实时通信', '文件上传', '支付集成', '多语言支持',
  'SEO优化', '性能优化', '安全防护', 'API接口'
]

// 表单验证规则
const formRules: FormRules = {
  title: [
    { required: true, message: '请输入项目标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入项目描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '描述长度在 10 到 1000 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择项目分类', trigger: 'change' }
  ],
  price: [
    {
      validator: (rule, value, callback) => {
        if (!formData.isFree && (!value || value <= 0)) {
          callback(new Error('付费项目请设置正确的价格'))
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

// 监听项目属性变化
watch(projectAttributes, (newAttrs) => {
  formData.isOpenSource = newAttrs.includes('isOpenSource')
  formData.isCommercialUse = newAttrs.includes('isCommercialUse')
  formData.publishImmediately = newAttrs.includes('publishImmediately')
}, { deep: true })

// 监听表单数据变化
watch(formData, (newData) => {
  emit('update:modelValue', newData)
}, { deep: true })

// 监听外部数据变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    Object.assign(formData, newValue)
    updateProjectAttributes()
  }
}, { immediate: true, deep: true })

// 更新项目属性复选框
const updateProjectAttributes = () => {
  const attrs: string[] = []
  if (formData.isOpenSource) attrs.push('isOpenSource')
  if (formData.isCommercialUse) attrs.push('isCommercialUse')
  if (formData.publishImmediately) attrs.push('publishImmediately')
  projectAttributes.value = attrs
}

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
  projectAttributes.value = []
}

// 取消操作
const handleCancel = () => {
  emit('cancel')
}

// 组件挂载时加载数据
onMounted(() => {
  loadCategories()
  updateProjectAttributes()
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
