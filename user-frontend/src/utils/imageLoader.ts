/**
 * 图片加载工具函数
 * 提供统一的图片加载错误处理和缓存机制
 */

/**
 * 图片加载选项
 */
interface ImageLoadOptions {
  /** 备用图片URL */
  fallbackSrc?: string
  /** 超时时间(毫秒) */
  timeout?: number
  /** 是否启用缓存 */
  cache?: boolean
  /** 重试次数 */
  retryCount?: number
  /** 重试延迟(毫秒) */
  retryDelay?: number
}

/**
 * 图片加载结果
 */
interface ImageLoadResult {
  /** 是否成功 */
  success: boolean
  /** 最终使用的图片URL */
  src: string
  /** 错误信息 */
  error?: string
  /** 是否使用了备用图片 */
  usedFallback: boolean
}

/**
 * 图片缓存
 */
const imageCache = new Map<string, Promise<ImageLoadResult>>()

/**
 * 默认配置
 */
const DEFAULT_OPTIONS: Required<ImageLoadOptions> = {
  fallbackSrc: '/images/default-project.jpg',
  timeout: 10000,
  cache: true,
  retryCount: 1,
  retryDelay: 1000
}

/**
 * 加载单个图片
 */
function loadSingleImage(src: string, timeout: number): Promise<boolean> {
  return new Promise((resolve) => {
    const img = new Image()
    let timeoutId: number | null = null
    
    const cleanup = () => {
      if (timeoutId) {
        clearTimeout(timeoutId)
        timeoutId = null
      }
      img.onload = null
      img.onerror = null
    }
    
    img.onload = () => {
      cleanup()
      resolve(true)
    }
    
    img.onerror = () => {
      cleanup()
      resolve(false)
    }
    
    // 设置超时
    timeoutId = window.setTimeout(() => {
      cleanup()
      resolve(false)
    }, timeout)
    
    img.src = src
  })
}

/**
 * 带重试的图片加载
 */
async function loadImageWithRetry(
  src: string, 
  options: Required<ImageLoadOptions>
): Promise<ImageLoadResult> {
  let lastError = ''
  
  // 尝试加载主图片
  for (let i = 0; i <= options.retryCount; i++) {
    try {
      const success = await loadSingleImage(src, options.timeout)
      if (success) {
        return {
          success: true,
          src,
          usedFallback: false
        }
      }
      lastError = `图片加载失败: ${src}`
    } catch (error) {
      lastError = error instanceof Error ? error.message : '未知错误'
    }
    
    // 如果不是最后一次尝试，等待后重试
    if (i < options.retryCount) {
      await new Promise(resolve => setTimeout(resolve, options.retryDelay))
    }
  }
  
  // 主图片失败，尝试备用图片
  if (options.fallbackSrc && options.fallbackSrc !== src) {
    try {
      const success = await loadSingleImage(options.fallbackSrc, options.timeout)
      if (success) {
        return {
          success: true,
          src: options.fallbackSrc,
          usedFallback: true
        }
      }
    } catch (error) {
      // 备用图片也失败了
    }
  }
  
  // 所有图片都失败
  return {
    success: false,
    src: src,
    error: lastError,
    usedFallback: false
  }
}

/**
 * 加载图片
 * @param src 图片URL
 * @param options 加载选项
 * @returns Promise<ImageLoadResult>
 */
export async function loadImage(
  src: string, 
  options: ImageLoadOptions = {}
): Promise<ImageLoadResult> {
  if (!src) {
    return {
      success: false,
      src: '',
      error: '图片URL为空',
      usedFallback: false
    }
  }
  
  const finalOptions = { ...DEFAULT_OPTIONS, ...options }
  const cacheKey = `${src}_${JSON.stringify(finalOptions)}`
  
  // 检查缓存
  if (finalOptions.cache && imageCache.has(cacheKey)) {
    return imageCache.get(cacheKey)!
  }
  
  // 创建加载Promise
  const loadPromise = loadImageWithRetry(src, finalOptions)
  
  // 缓存Promise
  if (finalOptions.cache) {
    imageCache.set(cacheKey, loadPromise)
  }
  
  return loadPromise
}

/**
 * 预加载图片列表
 * @param urls 图片URL列表
 * @param options 加载选项
 * @returns Promise<ImageLoadResult[]>
 */
export async function preloadImages(
  urls: string[], 
  options: ImageLoadOptions = {}
): Promise<ImageLoadResult[]> {
  const promises = urls.map(url => loadImage(url, options))
  return Promise.all(promises)
}

/**
 * 清除图片缓存
 * @param url 可选，指定要清除的图片URL，不传则清除所有
 */
export function clearImageCache(url?: string): void {
  if (url) {
    // 清除特定URL的缓存
    for (const key of imageCache.keys()) {
      if (key.startsWith(url)) {
        imageCache.delete(key)
      }
    }
  } else {
    // 清除所有缓存
    imageCache.clear()
  }
}

/**
 * 获取缓存统计信息
 */
export function getCacheStats(): { size: number; keys: string[] } {
  return {
    size: imageCache.size,
    keys: Array.from(imageCache.keys())
  }
}

/**
 * 检查图片是否可用
 * @param src 图片URL
 * @param timeout 超时时间
 * @returns Promise<boolean>
 */
export async function checkImageAvailable(
  src: string, 
  timeout: number = 5000
): Promise<boolean> {
  if (!src) return false
  return loadSingleImage(src, timeout)
}

/**
 * 创建响应式图片URL
 * @param baseSrc 基础图片URL
 * @param width 目标宽度
 * @param height 目标高度
 * @param quality 图片质量 (1-100)
 * @returns 响应式图片URL
 */
export function createResponsiveImageUrl(
  baseSrc: string,
  width?: number,
  height?: number,
  quality: number = 80
): string {
  if (!baseSrc) return ''
  
  const url = new URL(baseSrc, window.location.origin)
  const params = new URLSearchParams()
  
  if (width) params.set('w', width.toString())
  if (height) params.set('h', height.toString())
  if (quality !== 80) params.set('q', quality.toString())
  
  if (params.toString()) {
    url.search = params.toString()
  }
  
  return url.toString()
}
