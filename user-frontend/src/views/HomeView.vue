<template>
  <div class="home-view">
    <!-- Hero区域 -->
    <section class="hero">
      <div class="container">
        <div class="hero-content">
          <div class="hero-text">
            <h1 class="hero-title">
              专业的<span class="highlight">源码交易</span>平台
            </h1>
            <p class="hero-subtitle">
              Docker化标准 · 一键部署演示 · 积分经济体系<br>
              为开发者提供高质量的源码交易和部署服务
            </p>
            <div class="hero-actions">
              <button class="btn btn-primary btn-large" @click="handleBrowseProjects">
                <i class="fas fa-rocket"></i>
                浏览项目
              </button>
              <button class="btn btn-outline btn-large" @click="handleUploadProject">
                <i class="fas fa-upload"></i>
                上传项目
              </button>
            </div>
            <div class="hero-features">
              <div class="feature-item">
                <i class="fab fa-docker"></i>
                <span>Docker化标准</span>
              </div>
              <div class="feature-item">
                <i class="fas fa-play-circle"></i>
                <span>一键部署</span>
              </div>
              <div class="feature-item">
                <i class="fas fa-coins"></i>
                <span>积分奖励</span>
              </div>
            </div>
          </div>
          <div class="hero-visual">
            <div class="code-window">
              <div class="window-header">
                <div class="window-controls">
                  <span class="control close"></span>
                  <span class="control minimize"></span>
                  <span class="control maximize"></span>
                </div>
                <div class="window-title">docker-compose.yml</div>
              </div>
              <div class="window-content">
                <pre class="code-content">
<span class="code-keyword">version:</span> <span class="code-string">'3.8'</span>
<span class="code-keyword">services:</span>
  <span class="code-property">web:</span>
    <span class="code-keyword">build:</span> <span class="code-string">.</span>
    <span class="code-keyword">ports:</span>
      - <span class="code-string">"3000:3000"</span>
    <span class="code-keyword">environment:</span>
      - <span class="code-property">NODE_ENV=production</span>
  <span class="code-property">db:</span>
    <span class="code-keyword">image:</span> <span class="code-string">mysql:8.0</span>
    <span class="code-keyword">environment:</span>
      - <span class="code-property">MYSQL_ROOT_PASSWORD=secret</span>
                </pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 特色功能区 -->
    <section class="features">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">为什么选择速码网？</h2>
          <p class="section-subtitle">专业的技术标准，完善的服务体系</p>
        </div>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fab fa-docker"></i>
            </div>
            <h3 class="feature-title">Docker化标准</h3>
            <p class="feature-description">
              所有项目采用Docker容器化标准，确保一致的运行环境，降低部署复杂度
            </p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fas fa-rocket"></i>
            </div>
            <h3 class="feature-title">一键部署演示</h3>
            <p class="feature-description">
              提供免费演示环境，购买前即可体验项目效果，降低购买风险
            </p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fas fa-shield-alt"></i>
            </div>
            <h3 class="feature-title">质量保证</h3>
            <p class="feature-description">
              人工审核机制，确保代码质量和安全性，为用户提供可靠的源码资源
            </p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fas fa-coins"></i>
            </div>
            <h3 class="feature-title">积分经济</h3>
            <p class="feature-description">
              完善的积分体系，上传优质项目获得奖励，Docker化项目享受双倍积分
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门项目展示 -->
    <section class="projects">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">热门项目</h2>
          <p class="section-subtitle">精选优质源码项目</p>
          <a href="#" class="view-all-link" @click.prevent="handleViewAllProjects">
            查看全部 <i class="fas fa-arrow-right"></i>
          </a>
        </div>
        <div class="projects-grid">
          <ProjectCard
            v-for="project in featuredProjects"
            :key="project.id"
            :project="project"
            @demo="handleDemo"
            @purchase="handlePurchase"
          />
        </div>
      </div>
    </section>

    <!-- 平台统计 -->
    <section class="stats">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-number" :data-target="1234">{{ animatedStats.projects }}</div>
            <div class="stat-label">优质项目</div>
          </div>
          <div class="stat-item">
            <div class="stat-number" :data-target="5678">{{ animatedStats.users }}</div>
            <div class="stat-label">注册用户</div>
          </div>
          <div class="stat-item">
            <div class="stat-number" :data-target="9876">{{ animatedStats.transactions }}</div>
            <div class="stat-label">成功交易</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ animatedStats.successRate }}%</div>
            <div class="stat-label">部署成功率</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import ProjectCard from '@/components/common/ProjectCard.vue'
import { useUserStore } from '@/stores/user'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { publicContentApi } from '@/api/modules/public'

// 定义项目类型
interface Project {
  id: number
  title: string
  description: string
  icon?: string
  tags?: string[]
  price: number
  isHot?: boolean
  hasDocker?: boolean
  // 后端实际字段名
  username?: string // 作者用户名
  rating?: number
  downloadCount?: number // 下载次数
  createdTime?: string // 创建时间
  category?: string
  // 兼容旧字段名
  author?: string
  downloads?: number
  createdAt?: string
}

// 定义统计数据类型
interface AnimatedStats {
  projects: number
  users: number
  transactions: number
  successRate: number
}

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const featuredProjects = ref<Project[]>([
  {
    id: 1,
    title: 'Vue3 管理后台模板',
    description: '基于Vue3 + TypeScript + Element Plus的现代化管理后台模板',
    icon: 'fab fa-vue',
    tags: ['Vue3', 'TypeScript', 'Element Plus'],
    price: 299,
    isHot: false,
    hasDocker: true,
    username: 'vue_master',
    rating: 4.8,
    downloadCount: 1250,
    createdTime: '2024-01-15T10:30:00'
  },
  {
    id: 2,
    title: 'React 电商系统',
    description: '完整的电商解决方案，包含用户端、管理后台和API服务',
    icon: 'fab fa-react',
    tags: ['React', 'Node.js', 'MongoDB'],
    price: 599,
    isHot: true,
    hasDocker: true,
    username: 'react_dev',
    rating: 4.9,
    downloadCount: 2100,
    createdTime: '2024-01-10T14:20:00'
  },
  {
    id: 3,
    title: 'Spring Boot 微服务',
    description: '企业级微服务架构，包含网关、认证、配置中心等组件',
    icon: 'fab fa-java',
    tags: ['Spring Boot', '微服务', 'Redis'],
    price: 899,
    isHot: false,
    hasDocker: true,
    username: 'java_expert',
    rating: 4.7,
    downloadCount: 890,
    createdTime: '2024-01-05T09:15:00'
  }
])

// 动画统计数据
const animatedStats = ref<AnimatedStats>({
  projects: 0,
  users: 0,
  transactions: 0,
  successRate: 0
})

/**
 * 浏览项目
 */
const handleBrowseProjects = () => {
  router.push('/market')
}

/**
 * 上传项目
 */
const handleUploadProject = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再上传项目')
    router.push('/login')
    return
  }
  router.push('/user/project/upload')
}

/**
 * 查看全部项目
 */
const handleViewAllProjects = () => {
  router.push('/market')
}

/**
 * 项目演示
 */
const handleDemo = (project: Project) => {
  ElMessage.info(`正在启动 ${project.title} 的演示环境...`)
  // 这里可以添加演示逻辑
}

/**
 * 获取热门项目数据
 */
const loadFeaturedProjects = async () => {
  try {
    const response = await publicContentApi.getFeaturedProjects(3)
    if (response.code === 200 && response.data) {
      featuredProjects.value = response.data
    }
  } catch (error) {
    console.warn('获取热门项目失败，使用模拟数据:', error)
    // 保持使用模拟数据作为fallback
  }
}

/**
 * 处理项目购买
 */
const handlePurchase = (project: Project) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后再购买项目')
    router.push('/login')
    return
  }

  // 跳转到购买页面
  router.push(`/user/project/purchase/${project.id}`)
}

/**
 * 动画计数器
 */
const animateCounter = (target: number, key: keyof AnimatedStats, duration = 2000) => {
  const start = 0
  const increment = target / (duration / 16)
  let current = start

  const timer = setInterval(() => {
    current += increment
    if (current >= target) {
      current = target
      clearInterval(timer)
    }
    animatedStats.value[key] = Math.floor(current) as never
  }, 16)
}

// 视差效果
const initParallaxEffects = () => {
  const codeWindow = document.querySelector('.code-window') as HTMLElement
  if (!codeWindow) return

  const handleScroll = () => {
    const scrolled = window.pageYOffset
    const rate = scrolled * -0.5
    codeWindow.style.transform = `translateY(${rate}px)`
  }

  // 节流函数
  let ticking = false
  const throttledScroll = () => {
    if (!ticking) {
      requestAnimationFrame(() => {
        handleScroll()
        ticking = false
      })
      ticking = true
    }
  }

  window.addEventListener('scroll', throttledScroll, { passive: true })
}

// 3D卡片倾斜效果
const addCardTiltEffect = () => {
  const cards = document.querySelectorAll('.project-card, .feature-card') as NodeListOf<HTMLElement>

  cards.forEach(card => {
    card.addEventListener('mousemove', (e: MouseEvent) => {
      const rect = card.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top

      const centerX = rect.width / 2
      const centerY = rect.height / 2

      const rotateX = (y - centerY) / 10
      const rotateY = (centerX - x) / 10

      card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateZ(10px)`
    })

    card.addEventListener('mouseleave', () => {
      card.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) translateZ(0)'
    })
  })
}

// 生命周期
onMounted(() => {
  // 加载热门项目数据
  loadFeaturedProjects()

  // 初始化视差效果
  initParallaxEffects()

  // 初始化3D卡片倾斜效果
  addCardTiltEffect()

  // 启动统计数字动画
  setTimeout(() => {
    animateCounter(1234, 'projects')
    animateCounter(5678, 'users')
    animateCounter(9876, 'transactions')
    animateCounter(98, 'successRate')
  }, 500)
})
</script>



<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.home-view {
  min-height: 100vh;
  // 移除背景色，让页面背景系统生效

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $spacing-lg;
  }

  // Hero区域
  .hero {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: $spacing-3xl 0;
    position: relative;
    overflow: hidden;

    .container {
      position: relative;
      z-index: 2;
    }

    .hero-content {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: $spacing-4xl;
      align-items: center;
      padding: $spacing-2xl 0;
    }

    .hero-text {
      .hero-title {
        font-size: 3.5rem;
        font-weight: 700;
        line-height: 1.2;
        margin-bottom: $spacing-lg;
        color: white;

        .highlight {
          background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }
      }

      .hero-subtitle {
        font-size: $font-size-lg;
        line-height: 1.6;
        margin-bottom: $spacing-3xl;
        color: rgba(255, 255, 255, 0.9);
      }

      .hero-actions {
        display: flex;
        gap: $spacing-lg;
        margin-bottom: $spacing-3xl;

        .btn {
          display: inline-flex;
          align-items: center;
          gap: $spacing-sm;
          padding: $spacing-md $spacing-xl;
          border-radius: $radius-lg;
          font-weight: 600;
          font-size: $font-size-base;
          text-decoration: none;
          border: 2px solid transparent;
          transition: all 0.3s ease;
          cursor: pointer;

          &.btn-large {
            padding: $spacing-lg $spacing-2xl;
            font-size: $font-size-lg;
          }

          &.btn-primary {
            background: white;
            color: $primary-color;
            box-shadow: $shadow-primary;

            &:hover {
              background: $primary-color;
              color: white;
              transform: translateY(-2px);
              box-shadow: $shadow-primary-hover;
            }
          }

          &.btn-outline {
            background: transparent;
            color: white;
            border: 2px solid rgba(255, 255, 255, 0.5);

            &:hover {
              background: rgba(255, 255, 255, 0.1);
              border-color: white;
              transform: translateY(-2px);
            }
          }
        }
      }

      .hero-features {
        display: flex;
        gap: $spacing-lg;
        flex-wrap: nowrap; // 确保不换行

        .feature-item {
          display: flex;
          align-items: center;
          gap: $spacing-sm;
          color: rgba(255, 255, 255, 0.8);
          font-size: $font-size-sm;
          flex: 0 0 auto; // 不伸缩，保持内容宽度
          white-space: nowrap; // 文字不换行

          i {
            color: $primary-color;
            font-size: $font-size-lg;
            flex-shrink: 0; // 图标不缩小
          }
        }
      }
    }

    .hero-visual {
      display: flex;
      justify-content: center;
      align-items: center;

      .code-window {
        background: linear-gradient(145deg, #1e1e1e 0%, #2d2d2d 100%);
        backdrop-filter: $glass-blur;
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: $radius-xl;
        overflow: hidden;
        box-shadow:
          $shadow-layered-xl,
          inset 0 1px 0 rgba(255, 255, 255, 0.1);
        transform: perspective(1000px) rotateY(-5deg) rotateX(5deg);
        transition: all $transition-base;
        max-width: 500px;
        width: 100%;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: $gradient-glass;
          pointer-events: none;
          border-radius: $radius-xl;
        }

        &:hover {
          transform: perspective(1000px) rotateY(0deg) rotateX(0deg) scale(1.05);
          box-shadow:
            $shadow-layered-xl,
            0 20px 40px rgba(24, 144, 255, 0.1),
            inset 0 1px 0 rgba(255, 255, 255, 0.2);
        }

        .window-header {
          background: linear-gradient(145deg, #2d2d2d 0%, #3a3a3a 100%);
          padding: $spacing-sm $spacing-md;
          display: flex;
          align-items: center;
          justify-content: space-between;
          border-bottom: 1px solid rgba(255, 255, 255, 0.1);

          .window-controls {
            display: flex;
            gap: $spacing-xs;

            .control {
              width: 12px;
              height: 12px;
              border-radius: 50%;

              &.close { background: #ff5f56; }
              &.minimize { background: #ffbd2e; }
              &.maximize { background: #27ca3f; }
            }
          }

          .window-title {
            color: rgba(255, 255, 255, 0.9);
            font-size: $font-size-sm;
            font-weight: 500;
          }
        }

        .window-content {
          padding: $spacing-lg;

          .code-content {
            font-family: $font-family-mono;
            font-size: $font-size-sm;
            line-height: 1.6;
            color: rgba(255, 255, 255, 0.9);
            margin: 0;

            .code-keyword { color: #ff79c6; }
            .code-string { color: #f1fa8c; }
            .code-property { color: #8be9fd; }
          }
        }
      }
    }
  }

  // 特色功能区
  .features {
    padding: $spacing-4xl 0;
    // 使用半透明背景，与页面背景系统协调
    background: rgba(248, 250, 252, 0.6);
    backdrop-filter: blur(10px);

    .section-header {
      text-align: center;
      margin-bottom: $spacing-4xl;

      .section-title {
        font-size: 2.5rem;
        font-weight: 700;
        color: $text-primary;
        margin-bottom: $spacing-md;
      }

      .section-subtitle {
        font-size: $font-size-lg;
        color: $text-secondary;
        margin: 0;
      }
    }

    .features-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: $spacing-xl;

      .feature-card {
        background: $gradient-card;
        border-radius: $radius-xl;
        padding: $spacing-xl;
        text-align: center;
        border: 1px solid rgba(255, 255, 255, 0.2);
        backdrop-filter: $glass-blur-sm;
        box-shadow: $shadow-layered-sm;
        transition: all $transition-base;
        position: relative;
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 1px;
          background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
        }

        &:hover {
          border-color: rgba(24, 144, 255, 0.3);
          box-shadow: $shadow-layered-lg;
          transform: translateY(-8px) scale(1.02);

          .feature-icon {
            transform: scale(1.1) rotate(5deg);
            box-shadow: $shadow-primary;
          }
        }

        .feature-icon {
          width: 80px;
          height: 80px;
          margin: 0 auto $spacing-lg;
          background: $gradient-primary;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: $shadow-primary;

          i {
            font-size: 2rem;
            color: white;
          }
        }

        .feature-title {
          font-size: $font-size-xl;
          font-weight: 600;
          color: $text-primary;
          margin-bottom: $spacing-md;
        }

        .feature-description {
          color: $text-secondary;
          line-height: 1.6;
          margin: 0;
        }
      }
    }
  }

  // 热门项目展示
  .projects {
    padding: $spacing-4xl 0;

    .section-header {
      text-align: center;
      margin-bottom: $spacing-4xl;
      position: relative;

      .section-title {
        font-size: 2.5rem;
        font-weight: 700;
        color: $text-primary;
        margin-bottom: $spacing-md;
      }

      .section-subtitle {
        font-size: $font-size-lg;
        color: $text-secondary;
        margin-bottom: $spacing-lg;
      }

      .view-all-link {
        color: $primary-color;
        text-decoration: none;
        font-weight: 600;
        display: inline-flex;
        align-items: center;
        gap: $spacing-xs;
        transition: all 0.3s ease;

        &:hover {
          color: $primary-hover;
          transform: translateX(4px);
        }

        i {
          transition: transform 0.3s ease;
        }

        &:hover i {
          transform: translateX(4px);
        }
      }
    }

    .projects-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
      gap: $spacing-xl;

      // 项目卡片样式现在由 ProjectCard 组件提供

    }
  }

  // 平台统计
  .stats {
    padding: $spacing-4xl 0;
    background: $gradient-primary;
    color: white;

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: $spacing-xl;

      .stat-item {
        text-align: center;

        .stat-number {
          font-size: 3rem;
          font-weight: 700;
          margin-bottom: $spacing-sm;
          color: white;
        }

        .stat-label {
          font-size: $font-size-lg;
          color: rgba(255, 255, 255, 0.9);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1199px) {
  .home-view {
    .features {
      .features-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }

    .projects {
      .projects-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }
  }
}

@media (max-width: 991px) {
  .home-view {
    .hero {
      .hero-content {
        grid-template-columns: 1fr;
        gap: $spacing-xl;
        text-align: center;
      }

      .hero-text {
        .hero-title {
          font-size: 2.25rem;
        }

        .hero-features {
          justify-content: center;
        }
      }
    }

    .projects {
      .projects-grid {
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      }
    }
  }
}

@media (max-width: 767px) {
  .home-view {
    .container {
      padding: 0 $spacing-md;
    }

    .hero {
      padding: $spacing-2xl 0;

      .hero-text {
        .hero-title {
          font-size: 2rem;
        }

        .hero-actions {
          flex-direction: column;
          align-items: center;

          .btn {
            width: 100%;
            max-width: 280px;
          }
        }

        .hero-features {
          flex-direction: column;
          gap: $spacing-sm;
        }
      }

      .hero-visual {
        .code-window {
          max-width: 100%;
        }
      }
    }

    .features {
      .features-grid {
        grid-template-columns: 1fr;
        gap: $spacing-lg;
      }
    }

    .projects {
      .projects-grid {
        grid-template-columns: 1fr;
      }
    }

    .stats {
      .stats-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: $spacing-lg;

        .stat-item {
          .stat-number {
            font-size: 2rem;
          }
        }
      }
    }
  }
}
</style>
