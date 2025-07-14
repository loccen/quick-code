/**
 * 认证相关API服务
 */
import type { ApiResponse } from '@/types/api'
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest
} from '@/types/user'
import { BaseApiService } from './base'

/**
 * 认证API服务类
 */
class AuthApiService extends BaseApiService {
  constructor() {
    super('/auth')
  }

  /**
   * 用户登录
   */
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return this.post('/login', data)
  }

  /**
   * 用户注册
   */
  register(data: RegisterRequest): Promise<ApiResponse<LoginResponse>> {
    return this.post('/register', data)
  }

  /**
   * 用户登出
   */
  logout(): Promise<ApiResponse<void>> {
    return this.post('/logout')
  }

  /**
   * 刷新令牌
   */
  refreshToken(refreshToken: string): Promise<ApiResponse<LoginResponse>> {
    return this.post('/refresh', null, {
      params: { refreshToken }
    })
  }

  /**
   * 发送邮箱验证码
   */
  sendEmailVerification(email: string): Promise<ApiResponse<void>> {
    return this.post('/send-email-verification', null, {
      params: { email }
    })
  }

  /**
   * 验证邮箱
   */
  verifyEmail(token: string): Promise<ApiResponse<void>> {
    return this.post('/verify-email', null, {
      params: { token }
    })
  }

  /**
   * 发送密码重置邮件
   */
  forgotPassword(email: string): Promise<ApiResponse<void>> {
    return this.post('/forgot-password', null, {
      params: { email }
    })
  }

  /**
   * 重置密码
   */
  resetPassword(token: string, newPassword: string): Promise<ApiResponse<void>> {
    return this.post('/reset-password', null, {
      params: { token, newPassword }
    })
  }

  /**
   * 检查用户名是否可用
   */
  checkUsername(username: string): Promise<ApiResponse<boolean>> {
    return this.get('/check-username', {
      params: { username }
    })
  }

  /**
   * 检查邮箱是否可用
   */
  checkEmail(email: string): Promise<ApiResponse<boolean>> {
    return this.get('/check-email', {
      params: { email }
    })
  }
}

/**
 * 认证API服务实例
 */
export const authApi = new AuthApiService()

/**
 * 导出默认实例
 */
export default authApi
