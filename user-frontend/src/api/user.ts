/**
 * 用户相关API服务
 */
import type { ApiResponse, PageRequest, PageResponse } from '@/types/api'
import type {
  ChangePasswordRequest,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  UpdateUserRequest,
  User,
  UserQueryParams
} from '@/types/user'
import { BaseApiService } from './base'

/**
 * 用户API服务类
 */
class UserApiService extends BaseApiService {
  constructor() {
    super('/users')
  }

  /**
   * 用户登录
   */
  login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return this.request.post('/auth/login', data)
  }

  /**
   * 用户注册
   */
  register(data: RegisterRequest): Promise<ApiResponse<LoginResponse>> {
    return this.request.post('/auth/register', data)
  }

  /**
   * 用户登出
   */
  logout(): Promise<ApiResponse<void>> {
    return this.request.post('/auth/logout')
  }

  /**
   * 刷新令牌
   */
  refreshToken(refreshToken: string): Promise<ApiResponse<LoginResponse>> {
    return this.request.post('/auth/refresh', null, {
      params: { refreshToken }
    })
  }

  /**
   * 发送邮箱验证码
   */
  sendEmailVerification(email: string): Promise<ApiResponse<void>> {
    return this.request.post('/auth/send-email-verification', null, {
      params: { email }
    })
  }

  /**
   * 验证邮箱
   */
  verifyEmail(token: string): Promise<ApiResponse<void>> {
    return this.request.post('/auth/verify-email', null, {
      params: { token }
    })
  }

  /**
   * 发送密码重置邮件
   */
  forgotPassword(email: string): Promise<ApiResponse<void>> {
    return this.request.post('/auth/forgot-password', null, {
      params: { email }
    })
  }

  /**
   * 重置密码
   */
  resetPassword(token: string, newPassword: string): Promise<ApiResponse<void>> {
    return this.request.post('/auth/reset-password', null, {
      params: { token, newPassword }
    })
  }

  /**
   * 检查用户名是否可用
   */
  checkUsername(username: string): Promise<ApiResponse<boolean>> {
    return this.request.get('/auth/check-username', {
      params: { username }
    })
  }

  /**
   * 检查邮箱是否可用
   */
  checkEmail(email: string): Promise<ApiResponse<boolean>> {
    return this.request.get('/auth/check-email', {
      params: { email }
    })
  }

  /**
   * 获取当前用户信息
   */
  getCurrentUser(): Promise<ApiResponse<User>> {
    return this.get('/me')
  }

  /**
   * 更新当前用户信息
   */
  updateCurrentUser(data: UpdateUserRequest): Promise<ApiResponse<User>> {
    return this.put('/me', data)
  }

  /**
   * 修改密码
   */
  changePassword(data: ChangePasswordRequest): Promise<ApiResponse<void>> {
    return this.post('/me/change-password', data)
  }

  /**
   * 上传头像
   */
  uploadAvatar(file: File, onProgress?: (progress: number) => void): Promise<ApiResponse<{ url: string }>> {
    return this.upload('/me/avatar', file, onProgress)
  }

  /**
   * 分页查询用户列表
   */
  getUserList(params: UserQueryParams & PageRequest): Promise<ApiResponse<PageResponse<User>>> {
    return this.getPage('', params)
  }

  /**
   * 根据ID获取用户信息
   */
  getUserById(id: string): Promise<ApiResponse<User>> {
    return this.getById(id)
  }

  /**
   * 创建用户
   */
  createUser(data: Partial<User>): Promise<ApiResponse<User>> {
    return this.create(data)
  }

  /**
   * 更新用户信息
   */
  updateUser(id: string, data: Partial<User>): Promise<ApiResponse<User>> {
    return this.update(id, data)
  }

  /**
   * 删除用户
   */
  deleteUser(id: string): Promise<ApiResponse<void>> {
    return this.remove(id)
  }

  /**
   * 批量删除用户
   */
  batchDeleteUsers(ids: string[]): Promise<ApiResponse<void>> {
    return this.batchRemove(ids)
  }

  /**
   * 启用用户
   */
  enableUser(id: string): Promise<ApiResponse<void>> {
    return this.post(`/${id}/enable`)
  }

  /**
   * 禁用用户
   */
  disableUser(id: string): Promise<ApiResponse<void>> {
    return this.post(`/${id}/disable`)
  }

  /**
   * 重置用户密码
   */
  resetUserPassword(id: string, newPassword: string): Promise<ApiResponse<void>> {
    return this.post(`/${id}/reset-password`, { newPassword })
  }

  /**
   * 分配角色
   */
  assignRoles(id: string, roleIds: string[]): Promise<ApiResponse<void>> {
    return this.post(`/${id}/roles`, { roleIds })
  }

  /**
   * 移除角色
   */
  removeRoles(id: string, roleIds: string[]): Promise<ApiResponse<void>> {
    return this.delete(`/${id}/roles?roleIds=${roleIds.join(',')}`)
  }

  /**
   * 检查用户名是否可用
   */
  checkUsername(username: string): Promise<ApiResponse<{ available: boolean }>> {
    return this.get('/check-username', { username })
  }

  /**
   * 检查邮箱是否可用
   */
  checkEmail(email: string): Promise<ApiResponse<{ available: boolean }>> {
    return this.get('/check-email', { email })
  }
}

/**
 * 用户API服务实例
 */
export const userApi = new UserApiService()

/**
 * 导出默认实例
 */
export default userApi
