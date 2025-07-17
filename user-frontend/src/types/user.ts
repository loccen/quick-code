/**
 * 用户相关类型定义
 */

/**
 * 用户信息
 */
export interface User {
  /** 用户ID */
  id: number
  /** 用户名 */
  username: string
  /** 邮箱 */
  email: string
  /** 昵称 */
  nickname?: string
  /** 头像URL */
  avatarUrl?: string
  /** 手机号 */
  phone?: string
  /** 性别 */
  gender?: 'MALE' | 'FEMALE' | 'OTHER'
  /** 生日 */
  birthday?: string
  /** 个人简介 */
  bio?: string
  /** 用户状态 */
  status: number
  /** 邮箱是否已验证 */
  emailVerified?: boolean
  /** 是否启用双因素认证 */
  twoFactorEnabled?: boolean
  /** 角色列表 */
  roles?: string[]
  /** 权限列表 */
  permissions?: string[]
  /** 创建时间 */
  createdAt?: string
  /** 更新时间 */
  updatedAt?: string
  /** 最后登录时间 */
  lastLoginTime?: string
}

/**
 * 角色信息
 */
export interface Role {
  /** 角色ID */
  id: string
  /** 角色名称 */
  name: string
  /** 角色代码 */
  code: string
  /** 角色描述 */
  description?: string
  /** 权限列表 */
  permissions: Permission[]
}

/**
 * 权限信息
 */
export interface Permission {
  /** 权限ID */
  id: string
  /** 权限名称 */
  name: string
  /** 权限代码 */
  code: string
  /** 权限描述 */
  description?: string
  /** 权限类型 */
  type: 'MENU' | 'BUTTON' | 'API'
  /** 父权限ID */
  parentId?: string
}

/**
 * 登录请求
 */
export interface LoginRequest {
  /** 用户名或邮箱 */
  username: string
  /** 密码 */
  password: string
  /** 记住我 */
  rememberMe?: boolean
  /** 验证码 */
  captcha?: string
  /** 2FA验证码 */
  totpCode?: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  /** 访问令牌 */
  accessToken: string
  /** 刷新令牌 */
  refreshToken: string
  /** 令牌类型 */
  tokenType: string
  /** 过期时间（秒） */
  expiresIn: number
  /** 用户信息 */
  user: User
  /** 是否需要2FA验证 */
  requiresTwoFactor?: boolean
  /** 临时令牌（用于2FA验证） */
  tempToken?: string
}

/**
 * 注册请求
 */
export interface RegisterRequest {
  /** 用户名 */
  username: string
  /** 邮箱 */
  email: string
  /** 密码 */
  password: string
  /** 确认密码 */
  confirmPassword: string
  /** 邮箱验证码 */
  emailCode: string
  /** 是否同意用户协议和隐私政策 */
  agreeTerms: boolean
}

/**
 * 重置密码请求
 */
export interface ResetPasswordRequest {
  /** 邮箱 */
  email: string
  /** 新密码 */
  newPassword: string
  /** 确认密码 */
  confirmPassword: string
  /** 邮箱验证码 */
  emailCode: string
}

/**
 * 修改密码请求
 */
export interface ChangePasswordRequest {
  /** 当前密码 */
  currentPassword: string
  /** 新密码 */
  newPassword: string
  /** 确认密码 */
  confirmPassword: string
}

/**
 * 更新用户信息请求
 */
export interface UpdateUserRequest {
  /** 头像URL */
  avatar?: string
  /** 昵称 */
  nickname?: string
  /** 个人简介 */
  bio?: string
}

/**
 * 发送邮箱验证码请求
 */
export interface SendEmailCodeRequest {
  /** 邮箱 */
  email: string
  /** 验证码类型 */
  type: 'REGISTER' | 'RESET_PASSWORD' | 'CHANGE_EMAIL'
}

/**
 * 刷新令牌请求
 */
export interface RefreshTokenRequest {
  /** 刷新令牌 */
  refreshToken: string
}

/**
 * 用户查询参数
 */
export interface UserQueryParams {
  /** 关键词搜索 */
  keyword?: string
  /** 用户状态 */
  status?: 'ACTIVE' | 'INACTIVE' | 'BANNED'
  /** 角色ID */
  roleId?: string
  /** 创建时间开始 */
  createdAtStart?: string
  /** 创建时间结束 */
  createdAtEnd?: string
}

/**
 * 双因素认证设置请求
 */
export interface TwoFactorSetupRequest {
  /** 是否启用2FA */
  enabled: boolean
  /** TOTP验证码（启用时必填） */
  totpCode?: string
}

/**
 * 双因素认证设置响应
 */
export interface TwoFactorSetupResponse {
  /** 密钥（用于生成QR码） */
  secret: string
  /** QR码数据URL */
  qrCodeUrl: string
  /** 备用恢复码 */
  backupCodes: string[]
}

/**
 * 双因素认证验证请求
 */
export interface TwoFactorVerifyRequest {
  /** TOTP验证码 */
  totpCode: string
}

/**
 * 双因素认证状态响应
 */
export interface TwoFactorStatusResponse {
  /** 是否已启用2FA */
  enabled: boolean
  /** 备用恢复码数量 */
  backupCodesCount: number
}

/**
 * 生成备用恢复码响应
 */
export interface BackupCodesResponse {
  /** 新的备用恢复码 */
  backupCodes: string[]
}
