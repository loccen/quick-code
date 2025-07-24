<template>
  <div class="my-projects-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><FolderOpened /></el-icon>
          我的项目
        </h1>
        <p class="page-description">管理您上传、购买和收藏的项目</p>
      </div>

      <div class="header-actions">
        <el-button type="primary" @click="$router.push('/user/project/upload')">
          <el-icon><Plus /></el-icon>
          上传项目
        </el-button>
      </div>
    </div>

    <!-- 项目统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon uploaded">
          <el-icon><Upload /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ uploadedCount }}</div>
          <div class="stat-label">上传的项目</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon purchased">
          <el-icon><ShoppingCart /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ purchasedCount }}</div>
          <div class="stat-label">购买的项目</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon favorites">
          <el-icon><Star /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ favoritesCount }}</div>
          <div class="stat-label">收藏的项目</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon earnings">
          <el-icon><Coin /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ totalEarnings }}</div>
          <div class="stat-label">总收益（积分）</div>
        </div>
      </div>
    </div>

    <!-- 项目管理标签页 -->
    <div class="projects-container">
      <el-tabs v-model="activeTab" class="project-tabs">
        <el-tab-pane label="我上传的项目" name="uploaded">
          <div class="tab-header">
            <h3>我上传的项目</h3>
            <el-button type="primary" @click="$router.push('/user/project/upload')">
              <el-icon><Plus /></el-icon>
              上传新项目
            </el-button>
          </div>

          <ProjectList
            :projects="uploadedProjects"
            :loading="uploadedLoading"
            :total="uploadedTotal"
            :show-status-filter="true"
            mode="uploaded"
            @refresh="loadUploadedProjects"
            @search="handleUploadedSearch"
            @filter="handleUploadedFilter"
            @sort="handleUploadedSort"
            @page-change="handleUploadedPageChange"
            @project-click="handleProjectClick"
            @project-demo="handleProjectDemo"
            @project-edit="handleProjectEdit"
            @project-delete="handleProjectDelete"
            @project-publish="handleProjectPublish"
            @project-unpublish="handleProjectUnpublish"
          />
        </el-tab-pane>

        <el-tab-pane label="购买的项目" name="purchased">
          <div class="tab-header">
            <h3>购买的项目</h3>
            <el-button @click="$router.push('/market')">
              <el-icon><ShoppingCart /></el-icon>
              浏览项目市场
            </el-button>
          </div>

          <ProjectList
            :projects="purchasedProjects"
            :loading="purchasedLoading"
            :total="purchasedTotal"
            :show-actions="false"
            :show-status-filter="false"
            mode="purchased"
            @refresh="loadPurchasedProjects"
            @search="handlePurchasedSearch"
            @filter="handlePurchasedFilter"
            @sort="handlePurchasedSort"
            @page-change="handlePurchasedPageChange"
            @project-click="handleProjectClick"
            @project-demo="handleProjectDemo"
            @project-download="handleProjectDownload"
          />
        </el-tab-pane>

        <el-tab-pane label="收藏的项目" name="favorites">
          <div class="tab-header">
            <h3>收藏的项目</h3>
            <el-button @click="$router.push('/market')">
              <el-icon><Star /></el-icon>
              发现更多项目
            </el-button>
          </div>

          <ProjectList
            :projects="favoriteProjects"
            :loading="favoritesLoading"
            :total="favoritesTotal"
            :show-actions="false"
            :show-status-filter="false"
            mode="favorites"
            @refresh="loadFavoriteProjects"
            @search="handleFavoritesSearch"
            @filter="handleFavoritesFilter"
            @sort="handleFavoritesSort"
            @page-change="handleFavoritesPageChange"
            @project-click="handleProjectClick"
            @project-demo="handleProjectDemo"
            @project-purchase="handleProjectPurchase"
            @project-unfavorite="handleProjectUnfavorite"
          />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  FolderOpened,
  Plus,
  Upload,
  ShoppingCart,
  Star,
  Coin
} from '@element-plus/icons-vue'
import ProjectList from '@/components/project/ProjectList.vue'
import { projectApi } from '@/api/modules/project'
import type { ProjectManagement } from '@/types/project'

// 路由
const router = useRouter()

// 响应式数据
const activeTab = ref('uploaded')

// 统计数据
const uploadedCount = ref(0)
const purchasedCount = ref(0)
const favoritesCount = ref(0)
const totalEarnings = ref(0)

// 上传的项目
const uploadedProjects = ref<ProjectManagement[]>([])
const uploadedLoading = ref(false)
const uploadedTotal = ref(0)
const uploadedParams = ref({
  page: 1,
  size: 20,
  keyword: '',
  status: undefined as number | undefined,
  // categoryId: undefined as number | null | undefined, // 后端API不支持分类筛选，暂时移除
  sortBy: 'createdTime',
  sortDir: 'DESC'
})

// 购买的项目
const purchasedProjects = ref<ProjectManagement[]>([])
const purchasedLoading = ref(false)
const purchasedTotal = ref(0)
const purchasedParams = ref({
  page: 1,
  size: 20,
  keyword: '',
  sortBy: 'createdTime',
  sortDir: 'DESC'
})

// 收藏的项目
const favoriteProjects = ref<ProjectManagement[]>([])
const favoritesLoading = ref(false)
const favoritesTotal = ref(0)
const favoritesParams = ref({
  page: 1,
  size: 20,
  keyword: '',
  sortBy: 'createdTime',
  sortDir: 'DESC'
})

// 加载上传的项目
const loadUploadedProjects = async () => {
  uploadedLoading.value = true
  try {
    const params = {
      page: uploadedParams.value.page - 1, // 后端页码从0开始
      size: uploadedParams.value.size,
      keyword: uploadedParams.value.keyword,
      status: uploadedParams.value.status,
      // categoryId: uploadedParams.value.categoryId, // 后端API不支持分类筛选
      sortBy: uploadedParams.value.sortBy,
      sortDir: uploadedParams.value.sortDir
    }

    const response = await projectApi.getMyProjects(params)
    if (response && response.code === 200 && response.data) {
      uploadedProjects.value = response.data.content || []
      uploadedTotal.value = response.data.total || 0
      uploadedCount.value = response.data.total || 0
    } else {
      throw new Error(response?.message || '获取项目列表失败')
    }
  } catch (error: any) {
    console.error('加载上传项目失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '加载项目失败')
    uploadedProjects.value = []
    uploadedTotal.value = 0
    uploadedCount.value = 0
  } finally {
    uploadedLoading.value = false
  }
}

// 加载购买的项目
const loadPurchasedProjects = async () => {
  purchasedLoading.value = true
  try {
    // TODO: 实现购买项目API调用
    // 暂时使用空数据
    purchasedProjects.value = []
    purchasedTotal.value = 0
    purchasedCount.value = 0
  } catch (error: any) {
    console.error('加载购买项目失败:', error)
    ElMessage.error('加载购买项目失败')
    purchasedProjects.value = []
    purchasedTotal.value = 0
    purchasedCount.value = 0
  } finally {
    purchasedLoading.value = false
  }
}

// 加载收藏的项目
const loadFavoriteProjects = async () => {
  favoritesLoading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 模拟数据
    const mockData: ProjectManagement[] = []
    favoriteProjects.value = mockData
    favoritesTotal.value = mockData.length
    favoritesCount.value = mockData.length
  } catch (error) {
    console.error('加载收藏项目失败:', error)
  } finally {
    favoritesLoading.value = false
  }
}

// 上传项目事件处理
const handleUploadedSearch = (keyword: string) => {
  uploadedParams.value.keyword = keyword
  uploadedParams.value.page = 1
  loadUploadedProjects()
}

interface FilterParams {
  category?: number | null
  status?: number
}

const handleUploadedFilter = (filters: FilterParams) => {
  uploadedParams.value.status = filters.status
  // uploadedParams.value.categoryId = filters.category // 后端API不支持分类筛选
  uploadedParams.value.page = 1
  console.log('上传项目筛选参数:', filters)
  loadUploadedProjects()
}

const handleUploadedSort = (sortBy: string, sortDir: string) => {
  uploadedParams.value.sortBy = sortBy
  uploadedParams.value.sortDir = sortDir
  loadUploadedProjects()
}

const handleUploadedPageChange = (page: number, size: number) => {
  uploadedParams.value.page = page
  uploadedParams.value.size = size
  loadUploadedProjects()
}

// 购买项目事件处理
const handlePurchasedSearch = (keyword: string) => {
  purchasedParams.value.keyword = keyword
  purchasedParams.value.page = 1
  loadPurchasedProjects()
}

const handlePurchasedFilter = (filters: any) => {
  purchasedParams.value.page = 1
  loadPurchasedProjects()
}

const handlePurchasedSort = (sortBy: string, sortDir: string) => {
  purchasedParams.value.sortBy = sortBy
  purchasedParams.value.sortDir = sortDir
  loadPurchasedProjects()
}

const handlePurchasedPageChange = (page: number, size: number) => {
  purchasedParams.value.page = page
  purchasedParams.value.size = size
  loadPurchasedProjects()
}

// 收藏项目事件处理
const handleFavoritesSearch = (keyword: string) => {
  favoritesParams.value.keyword = keyword
  favoritesParams.value.page = 1
  loadFavoriteProjects()
}

const handleFavoritesFilter = (filters: any) => {
  favoritesParams.value.page = 1
  loadFavoriteProjects()
}

const handleFavoritesSort = (sortBy: string, sortDir: string) => {
  favoritesParams.value.sortBy = sortBy
  favoritesParams.value.sortDir = sortDir
  loadFavoriteProjects()
}

const handleFavoritesPageChange = (page: number, size: number) => {
  favoritesParams.value.page = page
  favoritesParams.value.size = size
  loadFavoriteProjects()
}

// 项目操作事件处理
const handleProjectClick = (project: ProjectManagement) => {
  router.push(`/user/project/${project.id}`)
}

const handleProjectEdit = (project: ProjectManagement) => {
  router.push(`/user/project/edit/${project.id}`)
}

const handleProjectDelete = async (project: ProjectManagement) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目"${project.title}"吗？此操作不可恢复。`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )

    const response = await projectApi.deleteProject(project.id)
    if (response && response.code === 200) {
      ElMessage.success('项目删除成功')
      loadUploadedProjects()
    } else {
      throw new Error(response?.message || '删除项目失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除项目失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '删除项目失败')
    }
  }
}

const handleProjectDemo = (project: ProjectManagement) => {
  // 打开项目演示
  // TODO: 后续需要在ProjectManagement类型中添加demoUrl字段
  const demoUrl = (project as any).demoUrl
  if (demoUrl) {
    window.open(demoUrl, '_blank')
  } else {
    ElMessage.info('该项目暂无演示链接')
  }
}

const handleProjectPurchase = (project: ProjectManagement) => {
  // 跳转到项目详情页进行购买
  router.push(`/market/project/${project.id}`)
}

const handleProjectDownload = (project: ProjectManagement) => {
  // 下载已购买的项目
  console.log('下载项目:', project.title)
  ElMessage.info('下载功能开发中')
  // TODO: 实现项目下载功能
}

const handleProjectUnfavorite = async (project: ProjectManagement) => {
  try {
    // TODO: 调用取消收藏API
    // await projectApi.unfavoriteProject(project.id)

    console.log('取消收藏项目:', project.title)
    ElMessage.success('已取消收藏')
    loadFavoriteProjects()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '取消收藏失败')
  }
}

const handleProjectPublish = async (project: ProjectManagement) => {
  try {
    // TODO: 调用发布API
    // await projectApi.publishProject(project.id)

    console.log('发布项目:', project.title)
    ElMessage.success('项目发布成功')
    loadUploadedProjects()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '发布失败')
  }
}

const handleProjectUnpublish = async (project: ProjectManagement) => {
  try {
    await ElMessageBox.confirm(
      `确定要下架项目"${project.title}"吗？`,
      '确认下架',
      {
        type: 'warning',
        confirmButtonText: '确定下架',
        cancelButtonText: '取消'
      }
    )

    // TODO: 调用下架API
    // await projectApi.unpublishProject(project.id)

    ElMessage.success('项目下架成功')
    loadUploadedProjects()
  } catch (error) {
    // 用户取消下架
    console.log('下架操作取消:', error)
  }
}

// 页面初始化
onMounted(() => {
  loadUploadedProjects()
  loadPurchasedProjects()
  loadFavoriteProjects()
})
</script>

<style scoped>
.my-projects-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 28px;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-description {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

.header-actions {
  flex-shrink: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.uploaded {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.purchased {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.favorites {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.earnings {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.projects-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.project-tabs {
  padding: 0 24px;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px 24px 0 24px;
}

.tab-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
</style>
