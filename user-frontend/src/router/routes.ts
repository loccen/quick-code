/**
 * 路由配置
 */
import type { RouteRecordRaw } from 'vue-router'

/**
 * 基础路由（不需要认证）
 */
export const basicRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: {
      title: '用户登录',
      hidden: true
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: {
      title: '用户注册',
      hidden: true
    }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPasswordView.vue'),
    meta: {
      title: '忘记密码',
      hidden: true
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404View.vue'),
    meta: {
      title: '页面不存在',
      hidden: true
    }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403View.vue'),
    meta: {
      title: '权限不足',
      hidden: true
    }
  },
  {
    path: '/500',
    name: 'ServerError',
    component: () => import('@/views/error/500View.vue'),
    meta: {
      title: '服务器错误',
      hidden: true
    }
  }
]

/**
 * 公开路由（匿名可访问）
 */
export const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'PublicLayout',
    component: () => import('@/layouts/PublicLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: {
          title: '速码网 - 专业的源码交易平台',
          keepAlive: true
        }
      },
      {
        path: 'market',
        name: 'Market',
        component: () => import('@/views/market/MarketView.vue'),
        meta: {
          title: '项目市场',
          keepAlive: true
        }
      },
      {
        path: 'market/project/:id',
        name: 'PublicProjectDetail',
        component: () => import('@/views/market/ProjectDetailView.vue'),
        meta: {
          title: '项目详情',
          hidden: true
        }
      },

    ]
  }
]

/**
 * 用户相关路由（使用公共布局）
 */
export const userRoutes: RouteRecordRaw[] = [
  {
    path: '/user',
    name: 'UserLayout',
    component: () => import('@/layouts/PublicLayout.vue'),
    redirect: '/user/profile',
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: {
          title: '个人中心',
          icon: 'User',
          requiresAuth: true
        }
      },
      {
        path: 'my-projects',
        name: 'MyProjects',
        component: () => import('@/views/user/MyProjectsView.vue'),
        meta: {
          title: '我的项目',
          icon: 'FolderOpened',
          requiresAuth: true
        }
      },
      {
        path: 'project/upload',
        name: 'ProjectUpload',
        component: () => import('@/views/project/ProjectUploadIntegratedView.vue'),
        meta: {
          title: '上传项目',
          icon: 'Upload',
          requiresAuth: true
        }
      },
      {
        path: 'project/edit/:id',
        name: 'ProjectEdit',
        component: () => import('@/views/project/ProjectEditView.vue'),
        meta: {
          title: '编辑项目',
          hidden: true,
          requiresAuth: true
        }
      },
      {
        path: 'project/:id',
        name: 'MyProjectDetail',
        component: () => import('@/views/project/MyProjectDetailView.vue'),
        meta: {
          title: '我的项目详情',
          hidden: true,
          requiresAuth: true
        }
      },
      {
        path: 'my-orders',
        name: 'MyOrders',
        component: () => import('@/views/order/MyOrdersView.vue'),
        meta: {
          title: '我的订单',
          icon: 'ShoppingCart',
          requiresAuth: true
        }
      },
      {
        path: 'points',
        name: 'Points',
        component: () => import('@/views/point/PointsView.vue'),
        meta: {
          title: '积分管理',
          icon: 'Coin',
          requiresAuth: true
        }
      },
      {
        path: 'project/purchase/:id',
        name: 'ProjectPurchase',
        component: () => import('@/views/project/ProjectPurchaseView.vue'),
        meta: {
          title: '购买项目',
          hidden: true,
          requiresAuth: true
        }
      },
      {
        path: 'project/download/:id',
        name: 'ProjectDownload',
        component: () => import('@/views/project/ProjectDownloadView.vue'),
        meta: {
          title: '下载项目',
          hidden: true,
          requiresAuth: true
        }
      },
      {
        path: 'downloads',
        name: 'UserDownloads',
        component: () => import('@/views/user/UserDownloadsView.vue'),
        meta: {
          title: '下载记录',
          icon: 'Download',
          requiresAuth: true
        }
      },

      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/user/SettingsView.vue'),
        meta: {
          title: '系统设置',
          icon: 'Setting',
          requiresAuth: true
        }
      }
    ]
  }
]

/**
 * 动态路由（根据权限加载）
 * 注意：管理员功能由独立的 admin-frontend 项目提供
 */
export const dynamicRoutes: RouteRecordRaw[] = [
  // 当前用户端不包含管理员路由
  // 所有管理功能都在 admin-frontend 项目中实现
]

/**
 * 所有路由
 */
export const routes: RouteRecordRaw[] = [
  ...basicRoutes,
  ...publicRoutes,
  ...userRoutes,
  ...dynamicRoutes,
  // 404路由必须放在最后
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]
