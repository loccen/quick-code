/**
 * 认证相关API模块
 * 提供用户认证功能的接口
 */
import type { ApiResponse } from '@/types/api'
import type {
    JwtResponse,
    LoginRequest,
    LoginResponse,
    RegisterRequest,
    TwoFactorLoginRequest,
    TwoFactorVerifyRequest
} from '@/types/user'
import { request } from '../request'

/**
 * 认证API服务
 */
export const authApi = {
  /**
   * 用户登录
   */
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    // 转换字段名以匹配后端
    const loginData = {
      usernameOrEmail: data.username,
      password: data.password,
      rememberMe: data.rememberMe
    }
    return request.post('/auth/login', loginData)
  },

  /**
   * 用户注册
   */
  register(data: RegisterRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/auth/register', data)
  },

  /**
   * 用户登出
   */
  logout(): Promise<ApiResponse<void>> {
    return request.post('/auth/logout')
  },

  /**
   * 双因素认证登录（第二步：TOTP验证码验证）
   */
  loginWithTwoFactor(data: TwoFactorLoginRequest): Promise<ApiResponse<JwtResponse>> {
    return request.post('/auth/login/2fa', data)
  },

  /**
   * 刷新令牌
   */
  refreshToken(refreshToken: string): Promise<ApiResponse<LoginResponse>> {
    return request.post('/auth/refresh', null, {
      params: { refreshToken }
    })
  },

  /**
   * 发送邮箱验证码
   */
  sendEmailVerification(email: string): Promise<ApiResponse<void>> {
    return request.post('/auth/send-email-verification', null, {
      params: { email }
    })
  },

  /**
   * 验证邮箱
   */
  verifyEmail(token: string): Promise<ApiResponse<void>> {
    return request.post('/auth/verify-email', null, {
      params: { token }
    })
  },

  /**
   * 发送密码重置邮件
   */
  forgotPassword(email: string): Promise<ApiResponse<void>> {
    return request.post('/auth/forgot-password', null, {
      params: { email }
    })
  },

  /**
   * 重置密码
   */
  resetPassword(token: string, newPassword: string): Promise<ApiResponse<void>> {
    return request.post('/auth/reset-password', null, {
      params: { token, newPassword }
    })
  },

  /**
   * 检查用户名是否可用
   */
  checkUsername(username: string): Promise<ApiResponse<boolean>> {
    return request.get('/auth/check-username', {
      params: { username }
    })
  },

  /**
   * 检查邮箱是否可用
   */
  checkEmail(email: string): Promise<ApiResponse<boolean>> {
    return request.get('/auth/check-email', {
      params: { email }
    })
  },

  /**
   * 验证2FA代码（登录时使用）
   */
  verifyTwoFactorLogin(data: TwoFactorVerifyRequest): Promise<ApiResponse<LoginResponse>> {
    return request.post('/auth/verify-2fa', data)
  }
}

export default authApi
