/**
 * 用户相关API服务
 */
import type { ApiResponse, PageRequest, PageResponse } from '@/types/api'
import type {
    ChangePasswordRequest,
    TwoFactorSetupResponse,
    TwoFactorStatusResponse,
    TwoFactorVerifyRequest,
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
   * 获取当前用户信息
   */
  getCurrentUser(): Promise<ApiResponse<User>> {
    return this.get('/me')
  }

  /**
   * 更新当前用户信息
   */
  updateCurrentUser(data: UpdateUserRequest): Promise<ApiResponse<User>> {
    return this.put('/profile', data)
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

  // ==================== 双因素认证相关方法 ====================

  /**
   * 获取2FA状态
   */
  getTwoFactorStatus(): Promise<ApiResponse<TwoFactorStatusResponse>> {
    return this.get('/2fa/status')
  }

  /**
   * 获取2FA设置信息（生成密钥和QR码）
   */
  getTwoFactorSetup(): Promise<ApiResponse<TwoFactorSetupResponse>> {
    return this.get('/2fa/setup')
  }

  /**
   * 启用2FA（需要验证TOTP代码）
   */
  enableTwoFactor(data: TwoFactorVerifyRequest): Promise<ApiResponse<void>> {
    return this.put('/2fa/enable', data)
  }

  /**
   * 验证2FA代码
   */
  verifyTwoFactor(data: TwoFactorVerifyRequest): Promise<ApiResponse<void>> {
    return this.put('/2fa/verify', data)
  }

  /**
   * 禁用2FA（需要验证TOTP代码）
   */
  disableTwoFactor(data: TwoFactorVerifyRequest): Promise<ApiResponse<void>> {
    return this.put('/2fa/disable', data)
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
