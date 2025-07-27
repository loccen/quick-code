/**
 * 图片性能监控工具
 * 用于检测和报告图片加载性能问题，包括重复请求
 */

interface ImageRequest {
  url: string
  timestamp: number
  status: 'loading' | 'success' | 'error'
  loadTime?: number
  size?: number
}

interface ImageStats {
  url: string
  requestCount: number
  successCount: number
  errorCount: number
  totalLoadTime: number
  averageLoadTime: number
  lastRequestTime: number
  isRepeatedFailure: boolean
}

class ImagePerformanceMonitor {
  private requests: Map<string, ImageRequest[]> = new Map()
  private observers: PerformanceObserver[] = []
  private isMonitoring = false

  /**
   * 开始监控
   */
  start(): void {
    if (this.isMonitoring) return

    this.isMonitoring = true
    this.setupPerformanceObserver()
    this.setupNetworkMonitoring()
    
    console.log('🔍 图片性能监控已启动')
  }

  /**
   * 停止监控
   */
  stop(): void {
    if (!this.isMonitoring) return

    this.isMonitoring = false
    this.observers.forEach(observer => observer.disconnect())
    this.observers = []
    
    console.log('⏹️ 图片性能监控已停止')
  }

  /**
   * 设置性能观察器
   */
  private setupPerformanceObserver(): void {
    if (!('PerformanceObserver' in window)) return

    const observer = new PerformanceObserver((list) => {
      const entries = list.getEntries()
      entries.forEach((entry) => {
        const resourceEntry = entry as PerformanceResourceTiming
        if (resourceEntry.initiatorType === 'img' || entry.name.match(/\.(jpg|jpeg|png|gif|webp|svg)$/i)) {
          this.recordImageRequest(entry.name, {
            timestamp: entry.startTime,
            status: resourceEntry.responseEnd > 0 ? 'success' : 'error',
            loadTime: resourceEntry.responseEnd - entry.startTime,
            size: resourceEntry.transferSize || 0
          })
        }
      })
    })

    observer.observe({ entryTypes: ['resource'] })
    this.observers.push(observer)
  }

  /**
   * 设置网络监控
   */
  private setupNetworkMonitoring(): void {
    // 监控 fetch 请求
    const originalFetch = window.fetch
    window.fetch = async (...args) => {
      const url = args[0] as string
      if (this.isImageUrl(url)) {
        this.recordImageRequest(url, {
          timestamp: Date.now(),
          status: 'loading'
        })
      }
      
      try {
        const response = await originalFetch(...args)
        if (this.isImageUrl(url)) {
          this.updateImageRequest(url, {
            status: response.ok ? 'success' : 'error',
            loadTime: Date.now() - this.getLastRequestTime(url)
          })
        }
        return response
      } catch (error) {
        if (this.isImageUrl(url)) {
          this.updateImageRequest(url, {
            status: 'error',
            loadTime: Date.now() - this.getLastRequestTime(url)
          })
        }
        throw error
      }
    }

    // 监控 Image 对象
    const OriginalImage = window.Image
    window.Image = class extends OriginalImage {
      constructor() {
        super()
        
        const originalSrcSetter = Object.getOwnPropertyDescriptor(OriginalImage.prototype, 'src')?.set
        if (originalSrcSetter) {
          Object.defineProperty(this, 'src', {
            set: function(value: string) {
              if (value && monitor.isImageUrl(value)) {
                monitor.recordImageRequest(value, {
                  timestamp: Date.now(),
                  status: 'loading'
                })
              }
              originalSrcSetter.call(this, value)
            },
            get: function() {
              return this.getAttribute('src') || ''
            }
          })
        }

        this.addEventListener('load', () => {
          if (this.src && monitor.isImageUrl(this.src)) {
            monitor.updateImageRequest(this.src, {
              status: 'success',
              loadTime: Date.now() - monitor.getLastRequestTime(this.src)
            })
          }
        })

        this.addEventListener('error', () => {
          if (this.src && monitor.isImageUrl(this.src)) {
            monitor.updateImageRequest(this.src, {
              status: 'error',
              loadTime: Date.now() - monitor.getLastRequestTime(this.src)
            })
          }
        })
      }
    } as any

    const monitor = this
  }

  /**
   * 记录图片请求
   */
  private recordImageRequest(url: string, request: Partial<ImageRequest>): void {
    if (!this.requests.has(url)) {
      this.requests.set(url, [])
    }

    const requests = this.requests.get(url)!
    const fullRequest: ImageRequest = {
      url,
      timestamp: Date.now(),
      status: 'loading',
      ...request
    }

    requests.push(fullRequest)

    // 检查是否有重复请求
    this.checkForRepeatedRequests(url)
  }

  /**
   * 更新图片请求状态
   */
  private updateImageRequest(url: string, update: Partial<ImageRequest>): void {
    const requests = this.requests.get(url)
    if (!requests || requests.length === 0) return

    const lastRequest = requests[requests.length - 1]
    Object.assign(lastRequest, update)
  }

  /**
   * 获取最后一次请求时间
   */
  private getLastRequestTime(url: string): number {
    const requests = this.requests.get(url)
    if (!requests || requests.length === 0) return Date.now()
    return requests[requests.length - 1].timestamp
  }

  /**
   * 检查重复请求
   */
  private checkForRepeatedRequests(url: string): void {
    const requests = this.requests.get(url)
    if (!requests || requests.length < 3) return

    const recentRequests = requests.slice(-3)
    const timeSpan = recentRequests[2].timestamp - recentRequests[0].timestamp

    // 如果在5秒内有3次或以上请求，认为是重复请求
    if (timeSpan < 5000) {
      console.warn('🚨 检测到图片重复请求:', {
        url,
        requestCount: requests.length,
        recentRequests: recentRequests.length,
        timeSpan: `${timeSpan}ms`
      })

      // 触发警告事件
      this.emitWarning('repeated-requests', {
        url,
        requestCount: requests.length,
        timeSpan
      })
    }
  }

  /**
   * 判断是否为图片URL
   */
  private isImageUrl(url: string): boolean {
    if (!url) return false
    return /\.(jpg|jpeg|png|gif|webp|svg)(\?.*)?$/i.test(url) || 
           url.includes('image') || 
           url.includes('avatar') ||
           url.includes('thumbnail')
  }

  /**
   * 触发警告事件
   */
  private emitWarning(type: string, data: any): void {
    const event = new CustomEvent('image-performance-warning', {
      detail: { type, data, timestamp: Date.now() }
    })
    window.dispatchEvent(event)
  }

  /**
   * 获取统计信息
   */
  getStats(): ImageStats[] {
    const stats: ImageStats[] = []

    this.requests.forEach((requests, url) => {
      const successCount = requests.filter(r => r.status === 'success').length
      const errorCount = requests.filter(r => r.status === 'error').length
      const loadTimes = requests.filter(r => r.loadTime).map(r => r.loadTime!)
      const totalLoadTime = loadTimes.reduce((sum, time) => sum + time, 0)

      stats.push({
        url,
        requestCount: requests.length,
        successCount,
        errorCount,
        totalLoadTime,
        averageLoadTime: loadTimes.length > 0 ? totalLoadTime / loadTimes.length : 0,
        lastRequestTime: requests[requests.length - 1].timestamp,
        isRepeatedFailure: errorCount > 2 && successCount === 0
      })
    })

    return stats.sort((a, b) => b.requestCount - a.requestCount)
  }

  /**
   * 获取问题报告
   */
  getIssueReport(): {
    repeatedRequests: ImageStats[]
    failedImages: ImageStats[]
    slowImages: ImageStats[]
    summary: {
      totalRequests: number
      uniqueImages: number
      problemImages: number
    }
  } {
    const stats = this.getStats()
    
    const repeatedRequests = stats.filter(s => s.requestCount > 3)
    const failedImages = stats.filter(s => s.isRepeatedFailure)
    const slowImages = stats.filter(s => s.averageLoadTime > 3000)

    return {
      repeatedRequests,
      failedImages,
      slowImages,
      summary: {
        totalRequests: stats.reduce((sum, s) => sum + s.requestCount, 0),
        uniqueImages: stats.length,
        problemImages: new Set([
          ...repeatedRequests.map(s => s.url),
          ...failedImages.map(s => s.url),
          ...slowImages.map(s => s.url)
        ]).size
      }
    }
  }

  /**
   * 清除统计数据
   */
  clearStats(): void {
    this.requests.clear()
    console.log('📊 图片性能统计数据已清除')
  }

  /**
   * 导出统计数据
   */
  exportStats(): string {
    const report = this.getIssueReport()
    return JSON.stringify(report, null, 2)
  }
}

// 创建全局实例
const imagePerformanceMonitor = new ImagePerformanceMonitor()

// 开发环境自动启动监控
if (import.meta.env.DEV) {
  imagePerformanceMonitor.start()
  
  // 添加全局方法方便调试
  ;(window as any).imageMonitor = {
    getStats: () => imagePerformanceMonitor.getStats(),
    getReport: () => imagePerformanceMonitor.getIssueReport(),
    export: () => imagePerformanceMonitor.exportStats(),
    clear: () => imagePerformanceMonitor.clearStats()
  }
  
  console.log('🔧 开发模式：图片性能监控已启动，使用 window.imageMonitor 查看统计信息')
}

export default imagePerformanceMonitor
