<template>
  <div class="project-edit-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Edit /></el-icon>
          编辑项目
        </h1>
        <p class="page-description">
          修改项目信息和文件
        </p>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 编辑表单 -->
    <div v-else-if="projectData" class="edit-content">
      <ProjectUploadIntegrated
        :is-edit-mode="true"
        :project-id="projectId"
        :initial-data="projectData"
        @submit-success="handleSubmitSuccess"
        @cancel="handleCancel"
      />
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <el-result
        icon="error"
        title="项目不存在"
        sub-title="请检查项目ID是否正确"
      >
        <template #extra>
          <el-button type="primary" @click="router.push('/user/my-projects')">
            返回我的项目
          </el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import ProjectUploadIntegrated from '@/components/project/ProjectUploadIntegrated.vue'
import { projectApi } from '@/api/modules/project'
import type { ProjectUploadRequest } from '@/types/project'

// 路由
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const projectData = ref<ProjectUploadRequest>()

// 获取项目ID
const projectId = computed(() => {
  return Number(route.params.id)
})

// 加载项目数据
const loadProject = async () => {
  loading.value = true
  try {
    const response = await projectApi.getProject(projectId.value)
    if (response && response.code === 200 && response.data) {
      // 转换后端数据格式为前端表单格式
      projectData.value = {
        title: response.data.title,
        description: response.data.description,
        categoryId: response.data.categoryId,
        tags: response.data.tags || [],
        price: response.data.price ? Number(response.data.price) : 0,
        demoUrl: response.data.demoUrl || '',
        techStack: response.data.techStack || [],
        coverImage: response.data.coverImage || ''
      }
    } else {
      throw new Error(response?.message || '获取项目详情失败')
    }
  } catch (error: unknown) {
    console.error('加载项目失败:', error)
    const errorMessage = error instanceof Error ? error.message : '加载项目失败'
    ElMessage.error(errorMessage)
    projectData.value = undefined
  } finally {
    loading.value = false
  }
}

// 处理提交成功
const handleSubmitSuccess = (updatedProjectId: number) => {
  console.log('项目更新成功:', updatedProjectId)
  router.push('/user/my-projects')
}

// 取消编辑
const handleCancel = () => {
  router.back()
}

// 组件挂载时加载数据
onMounted(() => {
  if (projectId.value) {
    loadProject()
  } else {
    ElMessage.error('项目ID无效')
    router.push('/user/my-projects')
  }
})
</script>

<style scoped>
.project-edit-view {
  max-width: 1000px;
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
  font-size: 28px;
  margin: 0 0 8px 0;
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

.loading-container {
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.edit-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.error-state {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 40px;
}
</style>
