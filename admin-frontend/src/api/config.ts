// 管理后台API配置文件
export const API_CONFIG = {
  // 基础URL
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',

  // WebSocket URL
  WS_URL: import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws',

  // 上传URL
  UPLOAD_URL: import.meta.env.VITE_UPLOAD_URL || 'http://localhost:8080/api/upload',

  // MinIO URL
  MINIO_URL: import.meta.env.VITE_MINIO_URL || 'http://localhost:9000',

  // 请求超时时间
  TIMEOUT: 10000,

  // 请求重试次数
  RETRY_COUNT: 3
}

// 管理后台API端点
export const API_ENDPOINTS = {
  // 管理员认证
  AUTH: {
    LOGIN: '/admin/auth/login',
    LOGOUT: '/admin/auth/logout',
    REFRESH: '/admin/auth/refresh',
    PROFILE: '/admin/auth/profile'
  },

  // 用户管理
  USER: {
    LIST: '/admin/users',
    DETAIL: '/admin/users/:id',
    CREATE: '/admin/users',
    UPDATE: '/admin/users/:id',
    DELETE: '/admin/users/:id',
    DISABLE: '/admin/users/:id/disable',
    ENABLE: '/admin/users/:id/enable',
    RESET_PASSWORD: '/admin/users/:id/reset-password'
  },

  // 项目管理
  PROJECT: {
    LIST: '/admin/projects',
    DETAIL: '/admin/projects/:id',
    APPROVE: '/admin/projects/:id/approve',
    REJECT: '/admin/projects/:id/reject',
    DELETE: '/admin/projects/:id',
    FEATURED: '/admin/projects/:id/featured',
    CATEGORIES: '/admin/projects/categories',
    STATS: '/admin/projects/stats'
  },

  // 订单管理
  ORDER: {
    LIST: '/admin/orders',
    DETAIL: '/admin/orders/:id',
    REFUND: '/admin/orders/:id/refund',
    STATS: '/admin/orders/stats'
  },

  // 财务管理
  FINANCE: {
    OVERVIEW: '/admin/finance/overview',
    TRANSACTIONS: '/admin/finance/transactions',
    WITHDRAWALS: '/admin/finance/withdrawals',
    APPROVE_WITHDRAWAL: '/admin/finance/withdrawals/:id/approve',
    REJECT_WITHDRAWAL: '/admin/finance/withdrawals/:id/reject'
  },

  // 系统管理
  SYSTEM: {
    CONFIGS: '/admin/system/configs',
    UPDATE_CONFIG: '/admin/system/configs/:key',
    LOGS: '/admin/system/logs',
    STATS: '/admin/system/stats',
    HEALTH: '/admin/system/health'
  },

  // 内容审核
  REVIEW: {
    PENDING: '/admin/review/pending',
    HISTORY: '/admin/review/history',
    APPROVE: '/admin/review/:id/approve',
    REJECT: '/admin/review/:id/reject'
  }
}
