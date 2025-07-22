<template>
  <div class="project-list">
    <!-- 筛选和搜索栏 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索项目..."
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
          class="search-input"
        />
        
        <el-select
          v-model="selectedCategory"
          placeholder="选择分类"
          clearable
          @change="handleCategoryChange"
          class="category-select"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>

        <el-select
          v-model="selectedStatus"
          placeholder="项目状态"
          clearable
          @change="handleStatusChange"
          class="status-select"
        >
          <el-option label="全部" :value="undefined" />
          <el-option label="草稿" :value="0" />
          <el-option label="待审核" :value="1" />
          <el-option label="已发布" :value="2" />
          <el-option label="已拒绝" :value="3" />
          <el-option label="已下架" :value="4" />
        </el-select>
      </div>

      <div class="filter-right">
        <el-select
          v-model="sortBy"
          @change="handleSortChange"
          class="sort-select"
        >
          <el-option label="最新创建" value="createdTime" />
          <el-option label="最近更新" value="updatedTime" />
          <el-option label="下载量" value="downloadCount" />
          <el-option label="评分" value="rating" />
          <el-option label="价格" value="price" />
        </el-select>

        <el-button-group class="view-mode">
          <el-button 
            :type="viewMode === 'grid' ? 'primary' : 'default'"
            :icon="Grid"
            @click="viewMode = 'grid'"
          />
          <el-button 
            :type="viewMode === 'list' ? 'primary' : 'default'"
            :icon="List"
            @click="viewMode = 'list'"
          />
        </el-button-group>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="projects.length === 0" class="empty-state">
      <el-empty description="暂无项目数据">
        <el-button type="primary" @click="$emit('refresh')">
          刷新数据
        </el-button>
      </el-empty>
    </div>

    <!-- 项目列表 -->
    <div v-else class="projects-container">
      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="projects-grid">
        <div 
          v-for="project in projects" 
          :key="project.id"
          class="project-grid-item"
        >
          <ProjectCard
            :project="project"
            @click="handleProjectClick(project)"
            @edit="handleProjectEdit(project)"
            @delete="handleProjectDelete(project)"
            @publish="handleProjectPublish(project)"
            @unpublish="handleProjectUnpublish(project)"
          />
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="projects-list">
        <div 
          v-for="project in projects" 
          :key="project.id"
          class="project-list-item"
          @click="handleProjectClick(project)"
        >
          <div class="project-info">
            <div class="project-thumbnail">
              <img 
                :src="project.coverImage || '/images/default-project.png'" 
                :alt="project.title"
                @error="handleImageError"
              />
            </div>
            
            <div class="project-details">
              <h3 class="project-title">{{ project.title }}</h3>
              <p class="project-description">{{ project.description }}</p>
              
              <div class="project-meta">
                <el-tag size="small" type="primary">{{ getCategoryName(project.categoryId) }}</el-tag>
                <el-tag 
                  size="small" 
                  :type="getStatusType(project.status)"
                >
                  {{ getStatusText(project.status) }}
                </el-tag>
                <span class="project-price">
                  {{ project.isFree ? '免费' : `${project.price} 积分` }}
                </span>
              </div>

              <div class="project-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ project.viewCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><Download /></el-icon>
                  {{ project.downloadCount || 0 }}
                </span>
                <span class="stat-item">
                  <el-icon><Star /></el-icon>
                  {{ project.rating || 0 }}
                </span>
              </div>
            </div>
          </div>

          <div class="project-actions">
            <el-button 
              size="small" 
              @click.stop="handleProjectEdit(project)"
            >
              编辑
            </el-button>
            
            <el-button 
              v-if="project.status === 0 || project.status === 3"
              size="small" 
              type="primary"
              @click.stop="handleProjectPublish(project)"
            >
              发布
            </el-button>
            
            <el-button 
              v-if="project.status === 2"
              size="small" 
              type="warning"
              @click.stop="handleProjectUnpublish(project)"
            >
              下架
            </el-button>

            <el-dropdown @command="handleDropdownCommand" trigger="click">
              <el-button size="small" :icon="MoreFilled" circle />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="`view-${project.id}`">查看详情</el-dropdown-item>
                  <el-dropdown-item :command="`stats-${project.id}`">查看统计</el-dropdown-item>
                  <el-dropdown-item :command="`files-${project.id}`">管理文件</el-dropdown-item>
                  <el-dropdown-item 
                    :command="`delete-${project.id}`"
                    divided
                    class="danger-item"
                  >
                    删除项目
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Grid, 
  List, 
  View, 
  Download, 
  Star, 
  MoreFilled 
} from '@element-plus/icons-vue'
import ProjectCard from '@/components/common/ProjectCard.vue'
import { publicProjectApi } from '@/api/modules/public'
import type { ProjectManagement } from '@/types/project'
import type { ProjectCategory } from '@/api/modules/public'

// Props
interface Props {
  projects?: ProjectManagement[]
  loading?: boolean
  total?: number
  showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  projects: () => [],
  loading: false,
  total: 0,
  showActions: true
})

// Emits
const emit = defineEmits<{
  refresh: []
  search: [keyword: string]
  filter: [filters: any]
  sort: [sortBy: string, sortDir: string]
  pageChange: [page: number, size: number]
  projectClick: [project: ProjectManagement]
  projectEdit: [project: ProjectManagement]
  projectDelete: [project: ProjectManagement]
  projectPublish: [project: ProjectManagement]
  projectUnpublish: [project: ProjectManagement]
}>()

// 路由
const router = useRouter()

// 响应式数据
const searchKeyword = ref('')
const selectedCategory = ref<number>()
const selectedStatus = ref<number>()
const sortBy = ref('createdTime')
const sortDir = ref('DESC')
const viewMode = ref<'grid' | 'list'>('grid')
const currentPage = ref(1)
const pageSize = ref(20)
const categories = ref<ProjectCategory[]>([])

// 计算属性
const projects = computed(() => props.projects)
const total = computed(() => props.total)
const loading = computed(() => props.loading)

// 状态映射
const statusMap = {
  0: { text: '草稿', type: 'info' },
  1: { text: '待审核', type: 'warning' },
  2: { text: '已发布', type: 'success' },
  3: { text: '已拒绝', type: 'danger' },
  4: { text: '已下架', type: 'info' }
}

// 获取分类名称
const getCategoryName = (categoryId?: number) => {
  if (!categoryId) return '未分类'
  const category = categories.value.find(c => c.id === categoryId)
  return category?.name || '未知分类'
}

// 获取状态文本
const getStatusText = (status?: number) => {
  return statusMap[status as keyof typeof statusMap]?.text || '未知'
}

// 获取状态类型
const getStatusType = (status?: number) => {
  return statusMap[status as keyof typeof statusMap]?.type || 'info'
}

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-project.png'
}

// 搜索处理
const handleSearch = () => {
  emit('search', searchKeyword.value)
}

// 分类变化处理
const handleCategoryChange = () => {
  emitFilter()
}

// 状态变化处理
const handleStatusChange = () => {
  emitFilter()
}

// 排序变化处理
const handleSortChange = () => {
  emit('sort', sortBy.value, sortDir.value)
}

// 发送筛选事件
const emitFilter = () => {
  emit('filter', {
    category: selectedCategory.value,
    status: selectedStatus.value
  })
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  emit('pageChange', currentPage.value, size)
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  emit('pageChange', page, pageSize.value)
}

// 项目操作处理
const handleProjectClick = (project: ProjectManagement) => {
  emit('projectClick', project)
}

const handleProjectEdit = (project: ProjectManagement) => {
  emit('projectEdit', project)
}

const handleProjectDelete = (project: ProjectManagement) => {
  emit('projectDelete', project)
}

const handleProjectPublish = (project: ProjectManagement) => {
  emit('projectPublish', project)
}

const handleProjectUnpublish = (project: ProjectManagement) => {
  emit('projectUnpublish', project)
}

// 下拉菜单命令处理
const handleDropdownCommand = (command: string) => {
  const [action, projectId] = command.split('-')
  const project = projects.value.find(p => p.id === Number(projectId))
  
  if (!project) return

  switch (action) {
    case 'view':
      router.push(`/user/project/${project.id}`)
      break
    case 'stats':
      // TODO: 打开统计页面
      ElMessage.info('统计功能开发中')
      break
    case 'files':
      // TODO: 打开文件管理页面
      ElMessage.info('文件管理功能开发中')
      break
    case 'delete':
      handleProjectDelete(project)
      break
  }
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
.project-list {
  width: 100%;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.category-select,
.status-select {
  width: 150px;
}

.filter-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.sort-select {
  width: 120px;
}

.loading-container {
  padding: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.projects-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.project-list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.project-list-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.project-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.project-thumbnail {
  width: 80px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.project-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.project-details {
  flex: 1;
}

.project-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.project-description {
  font-size: 14px;
  color: #606266;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.project-price {
  font-size: 14px;
  font-weight: 600;
  color: #e6a23c;
}

.project-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.project-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

:deep(.danger-item) {
  color: #f56c6c;
}

:deep(.danger-item:hover) {
  background-color: #fef0f0;
}
</style>
