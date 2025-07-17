/**
 * 用户状态管理
 */
import { authApi } from '@/api/modules/auth'
import { userApi } from '@/api/user'
import { envConfig } from '@/config/env'
import type {
  LoginRequest,
  RegisterRequest,
  TwoFactorLoginRequest,
  TwoFactorRequiredResponse,
  UpdateUserRequest,
  User
} from '@/types/user'
import { ElMessage } from 'element-plus'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

/**
 * 用户状态store
 */
export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref<User | null>(null)
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const isLoggedIn = ref<boolean>(false)
  const permissions = ref<string[]>([])

  // 计算属性
  const userInfo = computed(() => user.value)
  const isAuthenticated = computed(() => isLoggedIn.value && !!token.value)
  const userRoles = computed(() => user.value?.roles || [])
  const userPermissions = computed(() => permissions.value)

  /**
   * 初始化用户状态
   */
  const initUserState = () => {
    const savedToken = localStorage.getItem(envConfig.TOKEN_KEY)
    const savedRefreshToken = localStorage.getItem(envConfig.REFRESH_TOKEN_KEY)
    const savedUser = localStorage.getItem('user_info')

    if (savedToken && savedUser) {
      token.value = savedToken
      refreshToken.value = savedRefreshToken || ''
      user.value = JSON.parse(savedUser)
      isLoggedIn.value = true
      permissions.value = user.value?.permissions || []
    }
  }

  /**
   * 设置用户信息
   */
  const setUser = (userInfo: User) => {
    user.value = userInfo
    permissions.value = userInfo.permissions || []
    localStorage.setItem('user_info', JSON.stringify(userInfo))
  }

  /**
   * 设置令牌
   */
  const setTokens = (accessToken: string, refreshTokenValue: string) => {
    token.value = accessToken
    refreshToken.value = refreshTokenValue
    isLoggedIn.value = true

    localStorage.setItem(envConfig.TOKEN_KEY, accessToken)
    localStorage.setItem(envConfig.REFRESH_TOKEN_KEY, refreshTokenValue)
  }

  /**
   * 清除用户状态
   */
  const clearUserState = () => {
    user.value = null
    token.value = ''
    refreshToken.value = ''
    isLoggedIn.value = false
    permissions.value = []

    localStorage.removeItem(envConfig.TOKEN_KEY)
    localStorage.removeItem(envConfig.REFRESH_TOKEN_KEY)
    localStorage.removeItem('user_info')
  }

  /**
   * 用户登录（第一步：用户名密码验证）
   * 返回值：
   * - true: 登录成功（无需2FA）
   * - false: 登录失败
   * - TwoFactorRequiredResponse: 需要2FA验证
   */
  const login = async (loginData: LoginRequest): Promise<boolean | TwoFactorRequiredResponse> => {
    try {
      const response = await authApi.login(loginData)

      // 检查响应中的业务错误码
      if (response.code !== 200) {
        return false
      }

      const loginResponse = response.data

      // 检查是否需要2FA验证
      if (loginResponse.requiresTwoFactor && loginResponse.twoFactorResponse) {
        return loginResponse.twoFactorResponse
      }

      // 直接登录成功（无需2FA）
      if (loginResponse.jwtResponse) {
        const { accessToken, refreshToken: refreshTokenValue, user: userInfo } = loginResponse.jwtResponse
        setTokens(accessToken, refreshTokenValue)
        setUser(userInfo)
        ElMessage.success('登录成功')
        return true
      }

      ElMessage.error('登录响应格式错误')
      return false
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('网络错误，请稍后重试')
      return false
    }
  }

  /**
   * 双因素认证登录（第二步：TOTP验证码验证）
   */
  const loginWithTwoFactor = async (twoFactorData: TwoFactorLoginRequest): Promise<boolean> => {
    try {
      const response = await authApi.loginWithTwoFactor(twoFactorData)

      // 检查响应中的业务错误码
      if (response.code !== 200) {
        ElMessage.error(response.message || '验证码错误')
        return false
      }

      const { accessToken, refreshToken: refreshTokenValue, user: userInfo } = response.data
      setTokens(accessToken, refreshTokenValue)
      setUser(userInfo)

      ElMessage.success('登录成功')
      return true
    } catch (error) {
      console.error('2FA登录失败:', error)
      ElMessage.error('验证码错误，请重试')
      return false
    }
  }

  /**
   * 用户注册
   */
  const register = async (registerData: RegisterRequest): Promise<boolean> => {
    try {
      const response = await authApi.register(registerData)
      console.log(response)
      // 检查响应中的业务错误码
      if (response.code !== 200) {
        ElMessage.error(response.message || '注册失败')
        return false
      }

      // 注册成功后，后端返回完整的认证信息，直接设置登录状态
      const { accessToken, refreshToken: refreshTokenValue, user: userInfo } = response.data

      setTokens(accessToken, refreshTokenValue)
      setUser(userInfo)

      ElMessage.success('注册成功，欢迎加入速码网！')
      return true
    } catch (error) {
      console.error('注册失败:', error)
      ElMessage.error('网络错误，请稍后重试')
      return false
    }
  }

  /**
   * 用户登出
   */
  const logout = async (): Promise<void> => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      clearUserState()
      ElMessage.success('已退出登录')

      // 跳转到首页
      window.location.href = '/'
    }
  }

  /**
   * 刷新令牌
   */
  const refreshAccessToken = async (): Promise<boolean> => {
    try {
      if (!refreshToken.value) {
        throw new Error('没有刷新令牌')
      }

      const response = await authApi.refreshToken(refreshToken.value)
      const { accessToken, refreshToken: newRefreshToken } = response.data

      setTokens(accessToken, newRefreshToken)
      return true
    } catch (error) {
      console.error('刷新令牌失败:', error)
      logout()
      return false
    }
  }

  /**
   * 获取当前用户信息
   */
  const fetchCurrentUser = async (): Promise<boolean> => {
    try {
      const response = await userApi.getCurrentUser()
      setUser(response.data)
      return true
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return false
    }
  }

  /**
   * 更新用户信息
   */
  const updateUser = async (userData: UpdateUserRequest): Promise<boolean> => {
    try {
      const response = await userApi.updateCurrentUser(userData)
      setUser(response.data)
      return true
    } catch (error) {
      console.error('更新用户信息失败:', error)
      return false
    }
  }

  /**
   * 上传头像
   */
  const uploadAvatar = async (file: File): Promise<boolean> => {
    try {
      const response = await userApi.uploadAvatar(file)
      if (user.value) {
        user.value.avatarUrl = response.data.url
        localStorage.setItem('user_info', JSON.stringify(user.value))
      }
      // 不在这里显示成功提示，由调用组件负责显示
      return true
    } catch (error) {
      console.error('头像上传失败:', error)
      return false
    }
  }

  /**
   * 检查权限
   */
  const hasPermission = (permission: string): boolean => {
    return permissions.value.includes(permission)
  }

  /**
   * 检查角色
   */
  const hasRole = (roleCode: string): boolean => {
    return userRoles.value.some(role => role === roleCode)
  }

  /**
   * 检查多个权限（AND关系）
   */
  const hasAllPermissions = (permissionList: string[]): boolean => {
    return permissionList.every(permission => hasPermission(permission))
  }

  /**
   * 检查多个权限（OR关系）
   */
  const hasAnyPermission = (permissionList: string[]): boolean => {
    return permissionList.some(permission => hasPermission(permission))
  }

  // 初始化状态
  initUserState()

  return {
    // 状态
    user: userInfo,
    token,
    refreshToken,
    isLoggedIn,
    permissions,

    // 计算属性
    isAuthenticated,
    userRoles,
    userPermissions,

    // 方法
    login,
    loginWithTwoFactor,
    register,
    logout,
    refreshAccessToken,
    fetchCurrentUser,
    updateUser,
    uploadAvatar,
    hasPermission,
    hasRole,
    hasAllPermissions,
    hasAnyPermission,
    setUser,
    setTokens,
    clearUserState
  }
})
