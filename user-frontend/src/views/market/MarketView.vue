<template>
  <div class="market-view">
    <!-- 市场头部 -->
    <div class="market-header">
      <div class="container">
        <h1 class="market-title">项目市场</h1>
        <p class="market-subtitle">发现优质的源码项目，加速您的开发进程</p>
        
        <!-- 搜索和筛选 -->
        <div class="search-section">
          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索项目名称、描述、标签..."
              size="large"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </div>
          
          <div class="filter-bar">
            <el-select
              v-model="selectedCategory"
              placeholder="选择分类"
              clearable
              @change="handleCategoryChange"
            >
              <el-option
                v-for="category in categories"
                :key="category.code"
                :label="category.name"
                :value="category.code"
              />
            </el-select>
            
            <el-select
              v-model="sortBy"
              placeholder="排序方式"
              @change="handleSortChange"
            >
              <el-option label="最新发布" value="createdAt" />
              <el-option label="下载量" value="downloads" />
              <el-option label="评分" value="rating" />
              <el-option label="价格" value="price" />
            </el-select>
          </div>
        </div>
      </div>
    </div>

    <!-- 项目列表 -->
    <div class="market-content">
      <div class="container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="6" animated />
        </div>

        <!-- 项目网格 -->
        <div v-else-if="projects.length > 0" class="projects-grid">
          <ProjectCard
            v-for="project in projects"
            :key="project.id"
            :project="project"
            @view-detail="handleViewDetail"
            @purchase="handlePurchase"
            @demo="handleDemo"
          />
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <el-empty description="暂无项目数据" />
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="totalElements"
            :page-sizes="[12, 24, 48]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import ProjectCard from '@/components/market/ProjectCard.vue'
import { useUserStore } from '@/stores/user'
import { publicProjectApi } from '@/api/modules/public'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const selectedCategory = ref('')
const sortBy = ref('createdAt')
const currentPage = ref(1)
const pageSize = ref(12)

const projects = ref<any[]>([])
const categories = ref<any[]>([])
const totalElements = ref(0)
const totalPages = computed(() => Math.ceil(totalElements.value / pageSize.value))

/**
 * 获取项目分类
 */
const fetchCategories = async () => {
  try {
    const response = await publicProjectApi.getCategories()
    categories.value = response.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

/**
 * 获取项目列表
 */
const fetchProjects = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      category: selectedCategory.value,
      keyword: searchKeyword.value,
      sortBy: sortBy.value,
      sortDir: 'desc'
    }
    
    const response = await publicProjectApi.getProjects(params)
    projects.value = response.data.content
    totalElements.value = response.data.totalElements
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error('获取项目列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  fetchProjects()
}

/**
 * 处理分类变化
 */
const handleCategoryChange = () => {
  currentPage.value = 1
  fetchProjects()
}

/**
 * 处理排序变化
 */
const handleSortChange = () => {
  currentPage.value = 1
  fetchProjects()
}

/**
 * 处理页码变化
 */
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchProjects()
}

/**
 * 处理页面大小变化
 */
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchProjects()
}

/**
 * 查看项目详情
 */
const handleViewDetail = (project: any) => {
  router.push(`/market/project/${project.id}`)
}

/**
 * 处理项目购买
 */
const handlePurchase = (project: any) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再购买项目')
    router.push({
      path: '/login',
      query: { redirect: `/market/project/${project.id}` }
    })
    return
  }
  
  // 跳转到购买页面或显示购买弹窗
  ElMessage.info('购买功能开发中...')
}

/**
 * 处理项目演示
 */
const handleDemo = (project: any) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再体验项目演示')
    router.push({
      path: '/login',
      query: { redirect: `/market/project/${project.id}` }
    })
    return
  }
  
  // 打开演示页面
  ElMessage.info('演示功能开发中...')
}

// 生命周期
onMounted(() => {
  fetchCategories()
  fetchProjects()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.market-view {
  min-height: 100vh;
}

.market-header {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-hover) 100%);
  color: white;
  padding: $spacing-3xl 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $spacing-lg;
  }

  .market-title {
    font-size: $font-size-4xl;
    font-weight: $font-weight-bold;
    margin: 0 0 $spacing-md 0;
    text-align: center;
  }

  .market-subtitle {
    font-size: $font-size-lg;
    text-align: center;
    margin: 0 0 $spacing-3xl 0;
    opacity: 0.9;
  }

  .search-section {
    max-width: 800px;
    margin: 0 auto;

    .search-bar {
      margin-bottom: $spacing-lg;

      :deep(.el-input) {
        .el-input__wrapper {
          border-radius: $border-radius-lg;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
      }
    }

    .filter-bar {
      @include flex-center();
      gap: $spacing-md;
      justify-content: center;

      .el-select {
        width: 150px;
      }
    }
  }
}

.market-content {
  padding: $spacing-3xl 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $spacing-lg;
  }

  .loading-container {
    padding: $spacing-xl;
  }

  .projects-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: $spacing-xl;
    margin-bottom: $spacing-3xl;
  }

  .empty-state {
    padding: $spacing-3xl;
    text-align: center;
  }

  .pagination-container {
    @include flex-center();
    justify-content: center;
    padding-top: $spacing-xl;
    border-top: 1px solid var(--border-color);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .market-header {
    padding: $spacing-xl 0;

    .market-title {
      font-size: $font-size-2xl;
    }

    .search-section {
      .filter-bar {
        flex-direction: column;
        gap: $spacing-sm;

        .el-select {
          width: 100%;
        }
      }
    }
  }

  .market-content {
    .projects-grid {
      grid-template-columns: 1fr;
      gap: $spacing-lg;
    }
  }
}
</style>
