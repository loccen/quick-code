/**
 * 环境变量配置
 */

export interface EnvConfig {
  /** 应用标题 */
  APP_TITLE: string
  /** 环境类型 */
  APP_ENV: string
  /** API基础URL */
  API_BASE_URL: string
  /** API超时时间 */
  API_TIMEOUT: number
  /** Token存储键名 */
  TOKEN_KEY: string
  /** 刷新Token存储键名 */
  REFRESH_TOKEN_KEY: string
  /** 是否开启调试 */
  DEBUG: boolean
  /** 是否使用Mock API */
  MOCK_API: boolean
}

/**
 * 获取环境变量配置
 */
export const getEnvConfig = (): EnvConfig => {
  return {
    APP_TITLE: import.meta.env.VITE_APP_TITLE || '速码网',
    APP_ENV: import.meta.env.VITE_APP_ENV || 'development',
    API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    API_TIMEOUT: Number(import.meta.env.VITE_API_TIMEOUT) || 10000,
    TOKEN_KEY: import.meta.env.VITE_TOKEN_KEY || 'access_token',
    REFRESH_TOKEN_KEY: import.meta.env.VITE_REFRESH_TOKEN_KEY || 'refresh_token',
    DEBUG: import.meta.env.VITE_DEBUG === 'true',
    MOCK_API: import.meta.env.VITE_MOCK_API === 'true'
  }
}

/**
 * 环境变量配置实例
 */
export const envConfig = getEnvConfig()

/**
 * 是否为开发环境
 */
export const isDev = envConfig.APP_ENV === 'development'

/**
 * 是否为生产环境
 */
export const isProd = envConfig.APP_ENV === 'production'
