<template>
  <div class="my-projects-view">
    <!-- 页面容器 -->
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">我的项目</h1>
        <p class="page-subtitle">管理您上传、购买和收藏的项目</p>
      </div>

      <!-- 项目管理 -->
      <ModernCard title="项目管理" class="projects-card">
        <el-tabs v-model="activeProjectTab" class="project-tabs">
          <el-tab-pane label="上传的项目" name="uploaded">
            <div class="project-list">
              <div v-if="uploadedProjects.length === 0" class="empty-state">
                <el-empty description="暂无上传的项目">
                  <ModernButton type="primary" @click="$router.push('/upload')">
                    上传项目
                  </ModernButton>
                </el-empty>
              </div>
              <div v-else class="project-grid">
                <div v-for="project in uploadedProjects" :key="project.id" class="project-card">
                  <div class="project-image">
                    <img :src="project.thumbnail" :alt="project.name" />
                    <div class="project-status" :class="project.status">
                      {{ getStatusText(project.status) }}
                    </div>
                  </div>
                  <div class="project-info">
                    <h4 class="project-name">{{ project.name }}</h4>
                    <p class="project-desc">{{ project.description }}</p>
                    <div class="project-meta">
                      <span class="project-price">{{ project.price }} 积分</span>
                      <span class="project-sales">销量: {{ project.sales }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="购买的项目" name="purchased">
            <div class="project-list">
              <div v-if="purchasedProjects.length === 0" class="empty-state">
                <el-empty description="暂无购买的项目">
                  <ModernButton type="primary" @click="$router.push('/market')">
                    浏览项目市场
                  </ModernButton>
                </el-empty>
              </div>
              <div v-else class="project-grid">
                <div v-for="project in purchasedProjects" :key="project.id" class="project-card">
                  <div class="project-image">
                    <img :src="project.thumbnail" :alt="project.name" />
                  </div>
                  <div class="project-info">
                    <h4 class="project-name">{{ project.name }}</h4>
                    <p class="project-desc">{{ project.description }}</p>
                    <div class="project-actions">
                      <ModernButton size="small" type="primary">下载</ModernButton>
                      <ModernButton size="small">演示</ModernButton>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="收藏的项目" name="favorites">
            <div class="project-list">
              <div v-if="favoriteProjects.length === 0" class="empty-state">
                <el-empty description="暂无收藏的项目">
                  <ModernButton type="primary" @click="$router.push('/market')">
                    浏览项目市场
                  </ModernButton>
                </el-empty>
              </div>
              <div v-else class="project-grid">
                <div v-for="project in favoriteProjects" :key="project.id" class="project-card">
                  <div class="project-image">
                    <img :src="project.thumbnail" :alt="project.name" />
                  </div>
                  <div class="project-info">
                    <h4 class="project-name">{{ project.name }}</h4>
                    <p class="project-desc">{{ project.description }}</p>
                    <div class="project-actions">
                      <ModernButton size="small" type="primary">购买</ModernButton>
                      <ModernButton size="small">演示</ModernButton>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </ModernCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useAppStore } from '@/stores/app'
import ModernCard from '@/components/ui/ModernCard.vue'
import ModernButton from '@/components/ui/ModernButton.vue'

// Store
const appStore = useAppStore()

// 状态
const activeProjectTab = ref('uploaded')

// 项目数据
const uploadedProjects = ref([
  {
    id: 1,
    name: 'Vue3 管理后台',
    description: '基于Vue3+TypeScript的现代化管理后台',
    thumbnail: '/api/placeholder/300/200',
    price: 299,
    sales: 45,
    status: 'published'
  }
])

const purchasedProjects = ref([
  {
    id: 2,
    name: 'React 电商系统',
    description: '完整的电商解决方案',
    thumbnail: '/api/placeholder/300/200'
  }
])

const favoriteProjects = ref([
  {
    id: 3,
    name: 'Spring Boot 微服务',
    description: '企业级微服务架构',
    thumbnail: '/api/placeholder/300/200'
  }
])

// 获取项目状态文本
const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    draft: '草稿',
    pending: '审核中',
    published: '已发布',
    rejected: '已驳回'
  }
  return statusMap[status] || '未知'
}

// 页面初始化
onMounted(() => {
  appStore.setPageTitle('我的项目')
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.my-projects-view {
  min-height: calc(100vh - 200px);
  padding: $spacing-xl 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $spacing-lg;

    @include respond-below('lg') {
      padding: 0 $spacing-md;
    }

    @include respond-below('md') {
      padding: 0 $spacing-sm;
    }
  }

  .page-header {
    margin-bottom: $spacing-xl;

    .page-title {
      font-size: $font-size-3xl;
      font-weight: $font-weight-bold;
      color: var(--text-primary);
      margin: 0 0 $spacing-xs 0;
      @include gradient-text();
    }

    .page-subtitle {
      color: var(--text-secondary);
      font-size: $font-size-lg;
      margin: 0;
    }
  }

  .projects-card {
    background: var(--gradient-glass);
    backdrop-filter: var(--glass-blur-sm);
    border: 1px solid var(--glass-border);
    box-shadow: var(--shadow-layered-md);

    .project-tabs {
      :deep(.el-tabs__header) {
        margin-bottom: $spacing-lg;
        
        .el-tabs__nav-wrap {
          &::after {
            background: var(--border-light);
          }
        }

        .el-tabs__item {
          font-weight: 500;
          transition: all 0.3s ease;

          &.is-active {
            color: var(--primary-color);
          }

          &:hover {
            color: var(--primary-hover);
          }
        }

        .el-tabs__active-bar {
          background: var(--gradient-primary);
          height: 3px;
          border-radius: 2px;
        }
      }

      .project-list {
        .empty-state {
          text-align: center;
          padding: $spacing-3xl 0;
        }

        .project-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
          gap: $spacing-lg;

          .project-card {
            background: var(--gradient-card);
            border-radius: $radius-xl;
            overflow: hidden;
            box-shadow: var(--shadow-layered-sm);
            transition: all 0.3s ease;
            border: 1px solid var(--glass-border);

            &:hover {
              transform: translateY(-4px);
              box-shadow: var(--shadow-layered-lg);
            }

            .project-image {
              position: relative;
              height: 200px;
              overflow: hidden;

              img {
                width: 100%;
                height: 100%;
                object-fit: cover;
                transition: transform 0.3s ease;
              }

              .project-status {
                position: absolute;
                top: $spacing-sm;
                right: $spacing-sm;
                padding: $spacing-xs $spacing-sm;
                border-radius: $radius-md;
                font-size: $font-size-xs;
                font-weight: 500;
                backdrop-filter: blur(10px);

                &.draft {
                  background: rgba(140, 140, 140, 0.9);
                  color: white;
                }

                &.pending {
                  background: rgba(250, 173, 20, 0.9);
                  color: white;
                }

                &.published {
                  background: rgba(82, 196, 26, 0.9);
                  color: white;
                }

                &.rejected {
                  background: rgba(255, 77, 79, 0.9);
                  color: white;
                }
              }

              &:hover img {
                transform: scale(1.05);
              }
            }

            .project-info {
              padding: $spacing-lg;

              .project-name {
                font-size: $font-size-lg;
                font-weight: 600;
                color: var(--text-primary);
                margin: 0 0 $spacing-xs 0;
                line-height: $line-height-tight;
              }

              .project-desc {
                color: var(--text-secondary);
                font-size: $font-size-sm;
                margin: 0 0 $spacing-md 0;
                line-height: $line-height-base;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
              }

              .project-meta {
                @include flex-between();
                font-size: $font-size-sm;

                .project-price {
                  color: var(--primary-color);
                  font-weight: 600;
                }

                .project-sales {
                  color: var(--text-tertiary);
                }
              }

              .project-actions {
                display: flex;
                gap: $spacing-sm;
                margin-top: $spacing-md;
              }
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@include respond-below('lg') {
  .my-projects-view {
    .projects-card {
      .project-tabs {
        .project-list {
          .project-grid {
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
          }
        }
      }
    }
  }
}

@include respond-below('md') {
  .my-projects-view {
    .projects-card {
      .project-tabs {
        .project-list {
          .project-grid {
            grid-template-columns: 1fr;
            gap: $spacing-md;

            .project-card {
              .project-image {
                height: 160px;
              }

              .project-info {
                padding: $spacing-md;

                .project-name {
                  font-size: $font-size-base;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
