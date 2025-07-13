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
 * 主要路由（需要认证）
 */
export const mainRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: {
          title: '仪表盘',
          icon: 'Dashboard',
          requiresAuth: true,
          affix: true,
          keepAlive: true
        }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: {
          title: '个人中心',
          icon: 'User',
          requiresAuth: true,
          hidden: true
        }
      },
      {
        path: '/settings',
        name: 'Settings',
        component: () => import('@/views/user/SettingsView.vue'),
        meta: {
          title: '系统设置',
          icon: 'Setting',
          requiresAuth: true,
          hidden: true
        }
      }
    ]
  }
]

/**
 * 动态路由（根据权限加载）
 */
export const dynamicRoutes: RouteRecordRaw[] = [
  {
    path: '/projects',
    name: 'Projects',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: {
      title: '项目管理',
      icon: 'FolderOpened',
      requiresAuth: true,
      permissions: ['project:view']
    },
    children: [
      {
        path: '',
        name: 'ProjectList',
        component: () => import('@/views/project/ProjectListView.vue'),
        meta: {
          title: '项目列表',
          requiresAuth: true,
          permissions: ['project:view']
        }
      },
      {
        path: 'create',
        name: 'ProjectCreate',
        component: () => import('@/views/project/ProjectCreateView.vue'),
        meta: {
          title: '创建项目',
          requiresAuth: true,
          permissions: ['project:create'],
          hidden: true
        }
      },
      {
        path: ':id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/ProjectDetailView.vue'),
        meta: {
          title: '项目详情',
          requiresAuth: true,
          permissions: ['project:view'],
          hidden: true
        }
      }
    ]
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: {
      title: '系统管理',
      icon: 'Setting',
      requiresAuth: true,
      roles: ['admin']
    },
    children: [
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagementView.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          permissions: ['user:manage']
        }
      },
      {
        path: 'roles',
        name: 'RoleManagement',
        component: () => import('@/views/admin/RoleManagementView.vue'),
        meta: {
          title: '角色管理',
          requiresAuth: true,
          permissions: ['role:manage']
        }
      },
      {
        path: 'permissions',
        name: 'PermissionManagement',
        component: () => import('@/views/admin/PermissionManagementView.vue'),
        meta: {
          title: '权限管理',
          requiresAuth: true,
          permissions: ['permission:manage']
        }
      }
    ]
  }
]

/**
 * 所有路由
 */
export const routes: RouteRecordRaw[] = [
  ...basicRoutes,
  ...mainRoutes,
  ...dynamicRoutes,
  // 404路由必须放在最后
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]
