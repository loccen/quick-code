import request from '@/utils/request'

/**
 * 下载管理API
 */
export const downloadApi = {
  /**
   * 检查下载权限
   * @param {number} projectId 项目ID
   * @returns {Promise} API响应
   */
  checkDownloadPermission(projectId) {
    return request({
      url: `/api/downloads/project/${projectId}/permission`,
      method: 'get'
    })
  },

  /**
   * 生成下载令牌
   * @param {number} projectId 项目ID
   * @param {number} expirationMinutes 过期时间（分钟）
   * @returns {Promise} API响应
   */
  generateDownloadToken(projectId, expirationMinutes = 60) {
    return request({
      url: `/api/downloads/project/${projectId}/token`,
      method: 'post',
      params: { expirationMinutes }
    })
  },

  /**
   * 验证下载令牌
   * @param {string} token 下载令牌
   * @param {number} projectId 项目ID
   * @returns {Promise} API响应
   */
  validateDownloadToken(token, projectId) {
    return request({
      url: `/api/downloads/token/${token}/validate`,
      method: 'post',
      data: { projectId }
    })
  },

  /**
   * 获取下载URL
   * @param {number} projectId 项目ID
   * @param {string} token 下载令牌
   * @returns {Promise} 下载URL
   */
  async getDownloadUrl(projectId, token) {
    const response = await request({
      url: `/api/downloads/project/${projectId}/download`,
      method: 'get',
      params: { token },
      responseType: 'blob'
    })
    
    // 创建下载URL
    const blob = new Blob([response.data])
    return URL.createObjectURL(blob)
  },

  /**
   * 下载项目文件
   * @param {number} projectId 项目ID
   * @param {string} token 下载令牌
   * @param {Function} onProgress 进度回调
   * @returns {Promise} API响应
   */
  downloadProject(projectId, token, onProgress) {
    return request({
      url: `/api/downloads/project/${projectId}/download`,
      method: 'get',
      params: { token },
      responseType: 'blob',
      onDownloadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percentage = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress({
            percentage,
            loaded: progressEvent.loaded,
            total: progressEvent.total
          })
        }
      }
    })
  },

  /**
   * 获取下载历史
   * @param {Object} params 查询参数
   * @param {number} params.page 页码
   * @param {number} params.size 每页大小
   * @param {number} params.projectId 项目ID（可选）
   * @returns {Promise} API响应
   */
  getDownloadHistory(params = {}) {
    return request({
      url: '/api/downloads/history',
      method: 'get',
      params: {
        page: 0,
        size: 20,
        ...params
      }
    })
  },

  /**
   * 获取下载统计
   * @returns {Promise} API响应
   */
  getDownloadStatistics() {
    return request({
      url: '/api/downloads/statistics/user',
      method: 'get'
    })
  },

  /**
   * 获取项目下载统计
   * @param {number} projectId 项目ID
   * @returns {Promise} API响应
   */
  getProjectDownloadStatistics(projectId) {
    return request({
      url: `/api/downloads/statistics/project/${projectId}`,
      method: 'get'
    })
  },

  /**
   * 刷新下载令牌
   * @param {string} token 原令牌
   * @param {number} expirationMinutes 新的过期时间（分钟）
   * @returns {Promise} API响应
   */
  refreshDownloadToken(token, expirationMinutes = 60) {
    return request({
      url: `/api/downloads/token/${token}/refresh`,
      method: 'post',
      params: { expirationMinutes }
    })
  },

  /**
   * 撤销下载令牌
   * @param {string} token 下载令牌
   * @returns {Promise} API响应
   */
  revokeDownloadToken(token) {
    return request({
      url: `/api/downloads/token/${token}`,
      method: 'delete'
    })
  },

  /**
   * 获取用户活跃下载令牌
   * @returns {Promise} API响应
   */
  getUserActiveTokens() {
    return request({
      url: '/api/downloads/tokens/active',
      method: 'get'
    })
  },

  /**
   * 获取热门下载项目
   * @param {number} limit 限制数量
   * @param {number} days 天数
   * @returns {Promise} API响应
   */
  getPopularDownloads(limit = 10, days = 7) {
    return request({
      url: '/api/downloads/statistics/popular',
      method: 'get',
      params: { limit, days }
    })
  },

  /**
   * 获取下载趋势
   * @param {number} projectId 项目ID（可选）
   * @param {number} days 天数
   * @returns {Promise} API响应
   */
  getDownloadTrends(projectId, days = 30) {
    return request({
      url: '/api/downloads/statistics/trends',
      method: 'get',
      params: { projectId, days }
    })
  }
}

/**
 * 管理员下载API
 */
export const adminDownloadApi = {
  /**
   * 获取下载来源统计
   * @param {number} days 天数
   * @returns {Promise} API响应
   */
  getDownloadSourceStatistics(days = 30) {
    return request({
      url: '/api/downloads/statistics/sources',
      method: 'get',
      params: { days }
    })
  },

  /**
   * 获取下载排行用户
   * @param {number} limit 限制数量
   * @param {number} days 天数
   * @returns {Promise} API响应
   */
  getTopDownloaders(limit = 10, days = 7) {
    return request({
      url: '/api/downloads/statistics/top-downloaders',
      method: 'get',
      params: { limit, days }
    })
  },

  /**
   * 检测异常下载行为
   * @param {number} userId 用户ID（可选）
   * @param {string} clientIp 客户端IP（可选）
   * @param {number} timeWindowMinutes 时间窗口（分钟）
   * @returns {Promise} API响应
   */
  detectAbnormalDownloadBehavior(userId, clientIp, timeWindowMinutes = 60) {
    return request({
      url: '/api/downloads/security/detect-abnormal',
      method: 'post',
      params: { userId, clientIp, timeWindowMinutes }
    })
  },

  /**
   * 获取令牌统计信息
   * @returns {Promise} API响应
   */
  getTokenStatistics() {
    return request({
      url: '/api/downloads/tokens/statistics',
      method: 'get'
    })
  },

  /**
   * 清理过期令牌
   * @returns {Promise} API响应
   */
  cleanupExpiredTokens() {
    return request({
      url: '/api/downloads/tokens/cleanup',
      method: 'post'
    })
  }
}

export default downloadApi
