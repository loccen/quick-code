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
            <div class="search-input-wrapper">
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
              </el-input>
              <el-button
                class="search-button"
                type="primary"
                @click="handleSearch"
              >
                搜索
              </el-button>
            </div>
          </div>

          <div class="filter-bar">
            <CategorySelect
              v-model="selectedCategory"
              placeholder="选择分类"
              :show-all-levels="false"
              value-field="code"
              @change="handleCategoryChange"
              class="category-filter"
            />

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

            <el-button
              class="sort-direction-btn"
              :class="{ 'asc': sortDirection === 'asc' }"
              @click="toggleSortDirection"
              :title="sortDirection === 'desc' ? '点击切换为升序' : '点击切换为降序'"
            >
              <el-icon>
                <ArrowDown v-if="sortDirection === 'desc'" />
                <ArrowUp v-else />
              </el-icon>
              {{ sortDirection === 'desc' ? '降序' : '升序' }}
            </el-button>
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
            :total="total"
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
import { publicProjectApi } from '@/api/modules/public'
import ProjectCard from '@/components/common/ProjectCard.vue'
import CategorySelect from '@/components/project/CategorySelect.vue'
import { useUserStore } from '@/stores/user'
import { Search, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()

// 项目类型定义
interface Project {
  id: number
  title: string
  description: string
  thumbnail?: string
  coverImage?: string
  price: number
  rating?: number
  viewCount?: number
  downloadCount?: number
  username?: string
  author?: string
  createdTime?: string
  createdAt?: string
  tags?: string[]
  category?: string
}

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const selectedCategory = ref<string | null>(null)
const sortBy = ref('createdAt')
const sortDirection = ref('desc')
const currentPage = ref(1)
const pageSize = ref(12)

const projects = ref<Project[]>([])
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 分类数据现在由CategorySelect组件管理

/**
 * 获取项目列表
 */
const fetchProjects = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      category: selectedCategory.value || undefined,
      keyword: searchKeyword.value,
      sortBy: sortBy.value,
      sortDir: sortDirection.value
    }

    const response = await publicProjectApi.getProjects(params)
    projects.value = response.data.content
    total.value = response.data.total
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
const handleCategoryChange = (value: string | number | null, categoryData?: unknown) => {
  selectedCategory.value = typeof value === 'string' ? value : null
  currentPage.value = 1
  fetchProjects()
  console.log('选择的分类:', categoryData)
}

/**
 * 处理排序变化
 */
const handleSortChange = () => {
  currentPage.value = 1
  fetchProjects()
}

/**
 * 切换排序方向
 */
const toggleSortDirection = () => {
  sortDirection.value = sortDirection.value === 'desc' ? 'asc' : 'desc'
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
 * 处理项目购买
 */
const handlePurchase = (project: Project) => {
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
const handleDemo = (project: Project) => {
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
  fetchProjects()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.market-view {
  min-height: 100vh;
}

.market-header {
  color: $text-primary;
  padding: $spacing-md 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $spacing-lg;
  }

  .market-title {
    font-size: $font-size-4xl;
    font-weight: $font-weight-bold;
    color: var(--text-primary);
    margin: 0 0 $spacing-md 0;
    text-align: center;
    @include gradient-text();
  }

  .market-subtitle {
    font-size: $font-size-lg;
    text-align: center;
    margin: 0 0 $spacing-3xl 0;
    color: $text-secondary;
    opacity: 1; // 移除透明度，使用语义化颜色
  }

  .search-section {
    max-width: 800px;
    margin: 0 auto;

    .search-bar {
      margin-bottom: $spacing-lg;

      .search-input-wrapper {
        position: relative;
        display: flex;
        align-items: center;

        .search-button {
          position: absolute;
          right: 8px;
          top: 50%;
          transform: translateY(-50%);
          z-index: 10;
          height: 32px;
          padding: 0 $spacing-md;
          font-size: $font-size-sm;
          border-radius: $radius-md;
          background: $gradient-primary;
          border: none;
          color: white;
          cursor: pointer;
          transition: all $transition-base;
          box-shadow: $shadow-sm;

          &:hover {
            background: $gradient-primary;
            filter: brightness(1.1);
            box-shadow: $shadow-md;
            transform: translateY(-50%) scale(1.02);
          }

          &:active {
            transform: translateY(-50%) scale(0.98);
          }
        }

        // 调整输入框样式以为按钮留出空间
        :deep(.el-input__wrapper) {
          padding-right: 80px; // 为搜索按钮留出空间
        }
      }

      :deep(.el-input) {
        .el-input__wrapper {
          background: rgba(255, 255, 255, 0.1);
          backdrop-filter: blur(10px);
          border: 1px solid rgba(255, 255, 255, 0.3);
          border-radius: $radius-lg;
          box-shadow:
            0 8px 32px rgba(31, 38, 135, 0.15),
            0 4px 16px rgba(31, 38, 135, 0.1),
            inset 0 1px 0 rgba(255, 255, 255, 0.2);
          transition: all 0.3s ease;

          &:hover {
            background: rgba(255, 255, 255, 0.15);
            border-color: rgba(255, 255, 255, 0.4);
            box-shadow:
              0 12px 40px rgba(31, 38, 135, 0.2),
              0 6px 20px rgba(31, 38, 135, 0.15),
              inset 0 1px 0 rgba(255, 255, 255, 0.3);
          }

          &.is-focus {
            background: rgba(255, 255, 255, 0.2);
            border-color: $primary-color;
            box-shadow:
              0 0 0 2px rgba(24, 144, 255, 0.2),
              0 12px 40px rgba(31, 38, 135, 0.2),
              inset 0 1px 0 rgba(255, 255, 255, 0.3);
          }
        }

        .el-input__inner {
          background: transparent;
          border: none;
          color: $text-primary;

          &::placeholder {
            color: rgba(107, 114, 126, 0.8);
          }
        }

        // 搜索按钮样式
        .el-input-group__append {
          background: transparent;
          border: none;
          padding: 0;

          .el-button {
            background: linear-gradient(135deg, $primary-color 0%, $primary-hover 100%);
            border: none;
            color: white;
            padding: 12px 24px;
            border-radius: 0 $radius-lg 0 0;
            font-weight: $font-weight-medium;
            transition: all 0.3s ease;
            box-shadow:
              0 4px 16px rgba(24, 144, 255, 0.3),
              inset 0 1px 0 rgba(255, 255, 255, 0.2);

            &:hover {
              background: linear-gradient(135deg, $primary-hover 0%, darken($primary-hover, 10%) 100%);
              transform: translateY(-1px);
              box-shadow:
                0 6px 20px rgba(24, 144, 255, 0.4),
                inset 0 1px 0 rgba(255, 255, 255, 0.3);
            }

            &:active {
              transform: translateY(0);
              box-shadow:
                0 2px 8px rgba(24, 144, 255, 0.3),
                inset 0 1px 0 rgba(255, 255, 255, 0.2);
            }
          }
        }
      }
    }

    .filter-bar {
      @include flex-center();
      gap: $spacing-md;
      justify-content: center;

      .el-select {
        width: 150px;

        :deep(.el-input) {
          height: 32px; // 明确设置高度

          .el-input__wrapper {
            height: 32px; // 确保wrapper高度一致
            min-height: 32px;
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.3);
            border-radius: $radius-lg;
            box-shadow:
              0 4px 16px rgba(31, 38, 135, 0.1),
              inset 0 1px 0 rgba(255, 255, 255, 0.2);
            transition: all 0.3s ease;

            &:hover {
              background: rgba(255, 255, 255, 0.15);
              border-color: rgba(255, 255, 255, 0.4);
              box-shadow:
                0 6px 20px rgba(31, 38, 135, 0.15),
                inset 0 1px 0 rgba(255, 255, 255, 0.3);
            }

            &.is-focus {
              background: rgba(255, 255, 255, 0.2);
              border-color: $primary-color;
              box-shadow:
                0 0 0 2px rgba(24, 144, 255, 0.2),
                0 6px 20px rgba(31, 38, 135, 0.15),
                inset 0 1px 0 rgba(255, 255, 255, 0.3);
            }
          }

          .el-input__inner {
            height: 30px; // 内部输入框高度，留出边框空间
            line-height: 30px;
            background: transparent;
            border: none;
            color: $text-primary;

            &::placeholder {
              color: rgba(107, 114, 126, 0.8);
            }
          }

          .el-input__suffix {
            .el-input__suffix-inner {
              .el-select__caret {
                color: $text-secondary;
              }
            }
          }
        }
      }

      .sort-direction-btn {
        height: 32px; // 与 el-select 默认高度保持一致
        min-height: 32px;
        padding: 0 $spacing-md;
        background: rgba(255, 255, 255, 0.1);
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.3);
        border-radius: $radius-lg;
        color: $text-primary;
        font-size: $font-size-sm;
        font-weight: $font-weight-medium;
        line-height: 1;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow:
          0 4px 16px rgba(31, 38, 135, 0.1),
          inset 0 1px 0 rgba(255, 255, 255, 0.2);

        @include flex-center();
        gap: $spacing-xs;

        &:hover {
          background: rgba(255, 255, 255, 0.15);
          border-color: rgba(255, 255, 255, 0.4);
          box-shadow:
            0 6px 20px rgba(31, 38, 135, 0.15),
            inset 0 1px 0 rgba(255, 255, 255, 0.3);
          transform: translateY(-1px);
        }

        &:active {
          transform: translateY(0);
          box-shadow:
            0 2px 8px rgba(31, 38, 135, 0.1),
            inset 0 1px 0 rgba(255, 255, 255, 0.2);
        }

        &.asc {
          background: rgba(24, 144, 255, 0.15);
          border-color: rgba(24, 144, 255, 0.3);
          color: $primary-color;

          &:hover {
            background: rgba(24, 144, 255, 0.2);
            border-color: rgba(24, 144, 255, 0.4);
          }
        }

        .el-icon {
          font-size: $font-size-base;
          transition: transform 0.3s ease;
        }
      }
    }
  }
}

// 下拉框选项样式（全局）
:deep(.el-select-dropdown) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: $radius-lg;
  box-shadow:
    0 20px 40px rgba(31, 38, 135, 0.15),
    0 8px 24px rgba(31, 38, 135, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);

  .el-select-dropdown__item {
    color: $text-primary;
    transition: all 0.2s ease;

    &:hover {
      background: rgba(24, 144, 255, 0.1);
      color: $primary-color;
    }

    &.selected {
      background: rgba(24, 144, 255, 0.15);
      color: $primary-color;
      font-weight: $font-weight-medium;
    }
  }
}

.market-content {
  padding: $spacing-xs 0;

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

        .sort-direction-btn {
          width: 100%;
          height: 32px; // 确保移动端也保持一致的高度
          min-height: 32px;
          justify-content: center;
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
