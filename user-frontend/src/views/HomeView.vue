<template>
  <div class="home-view">
    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <h1 class="hero-title">速码网</h1>
          <p class="hero-subtitle">专业的源码交易平台</p>
          <p class="hero-description">
            为开发者提供优质的源码资源，加速您的开发进程
          </p>
          <div class="hero-actions">
            <router-link to="/market" class="btn btn-primary btn-large">
              浏览项目市场
            </router-link>
            <router-link
              v-if="!userStore.isAuthenticated"
              to="/register"
              class="btn btn-outline btn-large"
            >
              免费注册
            </router-link>
            <router-link
              v-else
              to="/user/dashboard"
              class="btn btn-outline btn-large"
            >
              进入控制台
            </router-link>
          </div>
        </div>
        <div class="hero-image">
          <img src="/images/hero-illustration.svg" alt="速码网" />
        </div>
      </div>
    </section>

    <!-- 平台统计 -->
    <section class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-number">1000+</div>
            <div class="stat-label">优质项目</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">5000+</div>
            <div class="stat-label">注册用户</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">10000+</div>
            <div class="stat-label">下载次数</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">99%</div>
            <div class="stat-label">用户满意度</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门项目 -->
    <section class="featured-projects">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">热门项目</h2>
          <p class="section-subtitle">精选优质源码项目</p>
          <router-link to="/market" class="view-more">查看更多 →</router-link>
        </div>

        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>

        <div v-else class="projects-grid">
          <ProjectCard
            v-for="project in featuredProjects"
            :key="project.id"
            :project="project"
            @view-detail="handleViewDetail"
            @purchase="handlePurchase"
            @demo="handleDemo"
          />
        </div>
      </div>
    </section>

    <!-- 功能特色 -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">为什么选择速码网</h2>
          <p class="section-subtitle">专业、安全、高效的源码交易平台</p>
        </div>

        <div class="features-grid">
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Shield /></el-icon>
            </div>
            <h3 class="feature-title">安全可靠</h3>
            <p class="feature-description">
              严格的代码审核机制，确保每个项目的质量和安全性
            </p>
          </div>

          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Lightning /></el-icon>
            </div>
            <h3 class="feature-title">快速交付</h3>
            <p class="feature-description">
              即买即用，快速下载，让您的项目开发事半功倍
            </p>
          </div>

          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Star /></el-icon>
            </div>
            <h3 class="feature-title">精品项目</h3>
            <p class="feature-description">
              精选优质源码项目，涵盖各种技术栈和应用场景
            </p>
          </div>

          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Service /></el-icon>
            </div>
            <h3 class="feature-title">专业服务</h3>
            <p class="feature-description">
              提供专业的技术支持和售后服务，解决您的后顾之忧
            </p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { publicContentApi } from '@/api/modules/public'
import ProjectCard from '@/components/market/ProjectCard.vue'
import { useUserStore } from '@/stores/user'
import { Lightning, Service, Shield, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const featuredProjects = ref<any[]>([])

/**
 * 获取推荐项目
 */
const fetchFeaturedProjects = async () => {
  loading.value = true
  try {
    const response = await publicContentApi.getFeaturedProjects(6)
    featuredProjects.value = response.data
  } catch (error) {
    console.error('获取推荐项目失败:', error)
  } finally {
    loading.value = false
  }
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

  ElMessage.info('演示功能开发中...')
}

// 生命周期
onMounted(() => {
  fetchFeaturedProjects()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.home-view {
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $spacing-lg;
  }

  // 英雄区域
  .hero-section {
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-hover) 100%);
    color: white;
    padding: $spacing-4xl 0;
    min-height: 600px;
    @include flex-center();

    .container {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: $spacing-3xl;
      align-items: center;
    }

    .hero-content {
      .hero-title {
        font-size: $font-size-5xl;
        font-weight: $font-weight-bold;
        margin: 0 0 $spacing-lg 0;
        line-height: 1.2;
      }

      .hero-subtitle {
        font-size: $font-size-2xl;
        margin: 0 0 $spacing-md 0;
        opacity: 0.9;
      }

      .hero-description {
        font-size: $font-size-lg;
        margin: 0 0 $spacing-3xl 0;
        opacity: 0.8;
        line-height: 1.6;
      }

      .hero-actions {
        @include flex-start();
        gap: $spacing-lg;

        .btn {
          padding: $spacing-md $spacing-xl;
          border-radius: $border-radius-lg;
          text-decoration: none;
          font-weight: $font-weight-medium;
          font-size: $font-size-lg;
          transition: all 0.3s ease;

          &.btn-large {
            padding: $spacing-lg $spacing-2xl;
          }

          &.btn-primary {
            background: white;
            color: var(--primary-color);
            border: 2px solid white;

            &:hover {
              background: transparent;
              color: white;
            }
          }

          &.btn-outline {
            background: transparent;
            color: white;
            border: 2px solid white;

            &:hover {
              background: white;
              color: var(--primary-color);
            }
          }
        }
      }
    }

    .hero-image {
      text-align: center;

      img {
        max-width: 100%;
        height: auto;
      }
    }
  }

  // 统计区域
  .stats-section {
    padding: $spacing-3xl 0;
    background: var(--bg-secondary);

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: $spacing-xl;

      .stat-item {
        text-align: center;

        .stat-number {
          font-size: $font-size-4xl;
          font-weight: $font-weight-bold;
          color: var(--primary-color);
          margin-bottom: $spacing-sm;
        }

        .stat-label {
          color: var(--text-secondary);
          font-size: $font-size-lg;
        }
      }
    }
  }

  // 推荐项目区域
  .featured-projects {
    padding: $spacing-4xl 0;

    .section-header {
      text-align: center;
      margin-bottom: $spacing-3xl;

      .section-title {
        font-size: $font-size-3xl;
        font-weight: $font-weight-bold;
        color: var(--text-primary);
        margin: 0 0 $spacing-md 0;
      }

      .section-subtitle {
        color: var(--text-secondary);
        font-size: $font-size-lg;
        margin: 0 0 $spacing-lg 0;
      }

      .view-more {
        color: var(--primary-color);
        text-decoration: none;
        font-weight: $font-weight-medium;
        transition: color 0.3s ease;

        &:hover {
          color: var(--primary-color-hover);
        }
      }
    }

    .loading-container {
      padding: $spacing-xl;
    }

    .projects-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: $spacing-xl;
    }
  }

  // 功能特色区域
  .features-section {
    padding: $spacing-4xl 0;
    background: var(--bg-secondary);

    .section-header {
      text-align: center;
      margin-bottom: $spacing-3xl;

      .section-title {
        font-size: $font-size-3xl;
        font-weight: $font-weight-bold;
        color: var(--text-primary);
        margin: 0 0 $spacing-md 0;
      }

      .section-subtitle {
        color: var(--text-secondary);
        font-size: $font-size-lg;
        margin: 0;
      }
    }

    .features-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: $spacing-xl;

      .feature-item {
        text-align: center;
        padding: $spacing-xl;
        background: var(--bg-primary);
        border-radius: $border-radius-lg;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        transition: transform 0.3s ease;

        &:hover {
          transform: translateY(-4px);
        }

        .feature-icon {
          width: 64px;
          height: 64px;
          margin: 0 auto $spacing-lg;
          background: var(--primary-color);
          border-radius: 50%;
          @include flex-center();
          color: white;

          .el-icon {
            font-size: 32px;
          }
        }

        .feature-title {
          font-size: $font-size-xl;
          font-weight: $font-weight-bold;
          color: var(--text-primary);
          margin: 0 0 $spacing-md 0;
        }

        .feature-description {
          color: var(--text-secondary);
          line-height: 1.6;
          margin: 0;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .home-view {
    .hero-section {
      padding: $spacing-2xl 0;

      .container {
        grid-template-columns: 1fr;
        gap: $spacing-xl;
        text-align: center;
      }

      .hero-content {
        .hero-title {
          font-size: $font-size-3xl;
        }

        .hero-actions {
          justify-content: center;
          flex-direction: column;

          .btn {
            width: 100%;
            max-width: 300px;
          }
        }
      }
    }

    .stats-section {
      .stats-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: $spacing-lg;
      }
    }

    .featured-projects {
      .projects-grid {
        grid-template-columns: 1fr;
      }
    }

    .features-section {
      .features-grid {
        grid-template-columns: 1fr;
        gap: $spacing-lg;
      }
    }
  }
}
</style>
