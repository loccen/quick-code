<template>
  <div class="existing-files-list">
    <!-- 文件列表头部 -->
    <div class="files-header">
      <h4 class="files-title">
        <el-icon><FolderOpened /></el-icon>
        已上传的文件
      </h4>
      <el-button 
        size="small" 
        type="primary" 
        @click="refreshFiles"
        :loading="loading"
        :icon="Refresh"
      >
        刷新
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 文件列表 -->
    <div v-else-if="files.length > 0" class="files-content">
      <div class="file-type-sections">
        <!-- 源码文件 -->
        <div v-if="sourceFiles.length > 0" class="file-type-section">
          <h5 class="file-type-title">
            <el-icon><Document /></el-icon>
            源码文件 ({{ sourceFiles.length }})
          </h5>
          <div class="file-items">
            <div
              v-for="file in sourceFiles"
              :key="file.id"
              class="file-item"
              :class="{ 'is-primary': file.isPrimary }"
            >
              <div class="file-info">
                <span class="file-name">{{ file.originalName }}</span>
                <el-tag v-if="file.isPrimary" type="primary" size="small">主文件</el-tag>
              </div>
              <div class="file-meta">
                <span class="file-size">{{ file.readableFileSize }}</span>
                <span class="file-time">{{ formatTime(file.uploadTime) }}</span>
              </div>
              <div class="file-actions">
                <el-button size="small" type="danger" @click="handleDeleteFile(file)">
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 封面图片 -->
        <div v-if="coverFiles.length > 0" class="file-type-section">
          <h5 class="file-type-title">
            <el-icon><Picture /></el-icon>
            封面图片 ({{ coverFiles.length }})
          </h5>
          <div class="file-items">
            <div
              v-for="file in coverFiles"
              :key="file.id"
              class="file-item"
              :class="{ 'is-primary': file.isPrimary }"
            >
              <div class="file-info">
                <span class="file-name">{{ file.originalName }}</span>
                <el-tag v-if="file.isPrimary" type="primary" size="small">主文件</el-tag>
              </div>
              <div class="file-meta">
                <span class="file-size">{{ file.readableFileSize }}</span>
                <span class="file-time">{{ formatTime(file.uploadTime) }}</span>
              </div>
              <div class="file-actions">
                <el-button size="small" type="danger" @click="handleDeleteFile(file)">
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 演示文件 -->
        <div v-if="demoFiles.length > 0" class="file-type-section">
          <h5 class="file-type-title">
            <el-icon><VideoPlay /></el-icon>
            演示文件 ({{ demoFiles.length }})
          </h5>
          <div class="file-items">
            <div
              v-for="file in demoFiles"
              :key="file.id"
              class="file-item"
            >
              <div class="file-info">
                <span class="file-name">{{ file.originalName }}</span>
              </div>
              <div class="file-meta">
                <span class="file-size">{{ file.readableFileSize }}</span>
                <span class="file-time">{{ formatTime(file.uploadTime) }}</span>
              </div>
              <div class="file-actions">
                <el-button size="small" type="danger" @click="handleDeleteFile(file)">
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 文档文件 -->
        <div v-if="documentFiles.length > 0" class="file-type-section">
          <h5 class="file-type-title">
            <el-icon><Reading /></el-icon>
            文档文件 ({{ documentFiles.length }})
          </h5>
          <div class="file-items">
            <div
              v-for="file in documentFiles"
              :key="file.id"
              class="file-item"
            >
              <div class="file-info">
                <span class="file-name">{{ file.originalName }}</span>
              </div>
              <div class="file-meta">
                <span class="file-size">{{ file.readableFileSize }}</span>
                <span class="file-time">{{ formatTime(file.uploadTime) }}</span>
              </div>
              <div class="file-actions">
                <el-button size="small" type="danger" @click="handleDeleteFile(file)">
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-icon class="empty-icon"><DocumentRemove /></el-icon>
      <p class="empty-text">暂无已上传的文件</p>
      <p class="empty-hint">使用下方的文件选择器添加新文件</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  FolderOpened,
  Document,
  Picture,
  VideoPlay,
  Reading,
  Refresh,
  DocumentRemove
} from '@element-plus/icons-vue'
import { projectFileApi } from '@/api/modules/project'
import type { ProjectFile } from '@/types/project'

// Props
interface Props {
  projectId: number
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'files-updated': []
}>()

// 响应式数据
const loading = ref(false)
const files = ref<ProjectFile[]>([])

// 计算属性 - 按类型分组的文件
const sourceFiles = computed(() => 
  files.value.filter(file => file.fileType === 'SOURCE')
)

const coverFiles = computed(() => 
  files.value.filter(file => file.fileType === 'COVER')
)

const demoFiles = computed(() => 
  files.value.filter(file => file.fileType === 'DEMO')
)

const documentFiles = computed(() => 
  files.value.filter(file => file.fileType === 'DOCUMENT')
)

// 加载项目文件列表
const loadProjectFiles = async () => {
  loading.value = true
  try {
    const response = await projectFileApi.getProjectFiles(props.projectId, {
      page: 1,
      size: 100 // 获取所有文件
    })
    
    if (response && response.code === 200 && response.data) {
      files.value = response.data.content || []
    } else {
      throw new Error(response?.message || '获取文件列表失败')
    }
  } catch (error: unknown) {
    console.error('加载文件列表失败:', error)
    const errorMessage = error instanceof Error ? error.message : '加载文件列表失败'
    ElMessage.error(errorMessage)
    files.value = []
  } finally {
    loading.value = false
  }
}

// 刷新文件列表
const refreshFiles = () => {
  loadProjectFiles()
}

// 处理文件删除
const handleDeleteFile = async (file: ProjectFile) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${file.originalName}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await projectFileApi.deleteFile(props.projectId, file.id)
    if (response && response.code === 200) {
      ElMessage.success('文件删除成功')
      loadProjectFiles()
      emit('files-updated')
    } else {
      throw new Error(response?.message || '删除文件失败')
    }
  } catch (error: unknown) {
    if (error !== 'cancel') {
      console.error('删除文件失败:', error)
      const errorMessage = error instanceof Error ? error.message : '删除文件失败'
      ElMessage.error(errorMessage)
    }
  }
}

// 时间格式化
const formatTime = (timeStr: string) => {
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 组件挂载时加载数据
onMounted(() => {
  loadProjectFiles()
})

// 暴露刷新方法
defineExpose({
  refresh: loadProjectFiles
})
</script>

<style scoped>
.existing-files-list {
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.files-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.files-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #374151;
}

.loading-container {
  padding: 20px 0;
}

.file-type-section {
  margin-bottom: 20px;
}

.file-type-section:last-child {
  margin-bottom: 0;
}

.file-type-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #4b5563;
}

.file-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.file-item:hover {
  border-color: #d1d5db;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.file-item.is-primary {
  border-color: #3b82f6;
  background: #eff6ff;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: #1f2937;
  word-break: break-all;
}

.file-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #6b7280;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #9ca3af;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #d1d5db;
}

.empty-text {
  font-size: 16px;
  margin: 0 0 8px 0;
  color: #6b7280;
}

.empty-hint {
  font-size: 14px;
  margin: 0;
  color: #9ca3af;
}

@media (max-width: 768px) {
  .file-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .file-meta {
    width: 100%;
    justify-content: space-between;
  }
  
  .file-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
