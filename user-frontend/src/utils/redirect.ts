/**
 * 智能页面跳转工具
 * 处理用户登录/注册后的页面重定向逻辑
 */

/**
 * 安全的重定向路径列表
 * 只允许跳转到这些路径，防止开放重定向漏洞
 */
const SAFE_REDIRECT_PATHS = [
  // 用户相关页面
  '/user/profile',
  '/user/my-projects',
  '/user/my-orders',

  // 公共页面
  '/',
  '/market',
  '/upload',

  // 项目详情页面（动态路径）
  '/project/',
  '/market/project/',

  // 项目市场相关页面
  '/market/category/',
  '/market/search'
]

/**
 * 检查路径是否为安全的重定向路径
 * @param path 要检查的路径
 * @returns 是否为安全路径
 */
function isSafeRedirectPath(path: string): boolean {
  // 检查是否为空或无效路径
  if (!path || typeof path !== 'string') {
    return false
  }

  // 移除查询参数和hash
  const cleanPath = path.split('?')[0].split('#')[0]

  // 检查是否为外部链接
  if (cleanPath.startsWith('http://') || cleanPath.startsWith('https://') || cleanPath.startsWith('//')) {
    return false
  }

  // 检查是否在安全路径列表中
  for (const safePath of SAFE_REDIRECT_PATHS) {
    if (safePath.endsWith('/')) {
      // 动态路径匹配（如 /project/）
      if (cleanPath.startsWith(safePath)) {
        return true
      }
    } else {
      // 精确路径匹配
      if (cleanPath === safePath) {
        return true
      }
    }
  }

  return false
}

/**
 * 获取智能重定向路径
 * @param redirectParam 来自URL查询参数的redirect值
 * @param defaultPath 默认跳转路径
 * @returns 安全的重定向路径
 */
export function getSmartRedirectPath(
  redirectParam?: string | null,
  defaultPath: string = '/user/profile'
): string {
  // 如果有redirect参数且为安全路径，使用它
  if (redirectParam && isSafeRedirectPath(redirectParam)) {
    return redirectParam
  }

  // 如果redirect参数不安全或不存在，使用默认路径
  return defaultPath
}

/**
 * 检查路径是否为登录/注册相关页面
 * @param path 要检查的路径
 * @returns 是否为认证页面
 */
export function isAuthPage(path: string): boolean {
  const authPaths = ['/login', '/register', '/forgot-password', '/reset-password']
  return authPaths.includes(path)
}

/**
 * 执行智能重定向
 * @param router Vue Router实例
 * @param redirectParam 来自URL查询参数的redirect值
 * @param defaultPath 默认跳转路径
 */
export function performSmartRedirect(
  router: { push: (path: string) => void; currentRoute: { value: { path: string } } },
  redirectParam?: string | null,
  defaultPath: string = '/user/profile'
): void {
  const targetPath = getSmartRedirectPath(redirectParam, defaultPath)
  
  // 如果目标路径就是当前页面，不进行跳转
  if (router.currentRoute.value.path === targetPath) {
    return
  }

  // 执行跳转
  router.push(targetPath)
}

/**
 * 为需要登录的操作生成登录URL
 * @param currentPath 当前页面路径
 * @returns 带有redirect参数的登录URL
 */
export function generateLoginUrl(currentPath: string): string {
  // 如果当前就是认证页面，不添加redirect参数
  if (isAuthPage(currentPath)) {
    return '/login'
  }

  // 添加redirect参数
  return `/login?redirect=${encodeURIComponent(currentPath)}`
}

/**
 * 为需要登录的操作生成注册URL
 * @param currentPath 当前页面路径
 * @returns 带有redirect参数的注册URL
 */
export function generateRegisterUrl(currentPath: string): string {
  // 如果当前就是认证页面，不添加redirect参数
  if (isAuthPage(currentPath)) {
    return '/register'
  }

  // 添加redirect参数
  return `/register?redirect=${encodeURIComponent(currentPath)}`
}
