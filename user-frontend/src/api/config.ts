// API配置文件
export const API_CONFIG = {
  // 基础URL
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',

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

// API端点
export const API_ENDPOINTS = {
  // 用户相关
  USER: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    PROFILE: '/user/profile',
    UPDATE_PROFILE: '/user/profile',
    CHANGE_PASSWORD: '/user/change-password'
  },

  // 项目相关
  PROJECT: {
    LIST: '/projects',
    DETAIL: '/projects/:id',
    CREATE: '/projects',
    UPDATE: '/projects/:id',
    DELETE: '/projects/:id',
    SEARCH: '/projects/search',
    CATEGORIES: '/projects/categories',
    UPLOAD: '/projects/upload'
  },

  // 订单相关
  ORDER: {
    LIST: '/orders',
    CREATE: '/orders',
    DETAIL: '/orders/:id',
    PAY: '/orders/:id/pay',
    CANCEL: '/orders/:id/cancel'
  },

  // 积分相关
  POINT: {
    BALANCE: '/points/balance',
    TRANSACTIONS: '/points/transactions',
    RECHARGE: '/points/recharge',
    WITHDRAW: '/points/withdraw'
  },

  // 部署相关
  DEPLOY: {
    CREATE: '/deploy',
    STATUS: '/deploy/:id/status',
    LOGS: '/deploy/:id/logs',
    STOP: '/deploy/:id/stop'
  }
}
