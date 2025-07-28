import request from '@/utils/request'

/**
 * 订单管理API
 */
export const orderApi = {
  /**
   * 创建订单
   * @param {Object} data 订单创建数据
   * @param {number} data.projectId 项目ID
   * @param {string} data.remark 订单备注
   * @returns {Promise} API响应
   */
  createOrder(data) {
    return request({
      url: '/api/orders',
      method: 'post',
      data
    })
  },

  /**
   * 支付订单
   * @param {string} orderNo 订单号
   * @param {Object} data 支付数据
   * @param {string} data.paymentMethod 支付方式
   * @param {number} data.pointsAmount 积分金额
   * @param {number} data.balanceAmount 余额金额
   * @returns {Promise} API响应
   */
  payOrder(orderNo, data) {
    return request({
      url: `/api/orders/${orderNo}/pay`,
      method: 'post',
      data
    })
  },

  /**
   * 取消订单
   * @param {string} orderNo 订单号
   * @param {string} reason 取消原因
   * @returns {Promise} API响应
   */
  cancelOrder(orderNo, reason) {
    return request({
      url: `/api/orders/${orderNo}/cancel`,
      method: 'post',
      params: { reason }
    })
  },

  /**
   * 完成订单
   * @param {string} orderNo 订单号
   * @returns {Promise} API响应
   */
  completeOrder(orderNo) {
    return request({
      url: `/api/orders/${orderNo}/complete`,
      method: 'post'
    })
  },

  /**
   * 申请退款
   * @param {string} orderNo 订单号
   * @param {string} reason 退款原因
   * @returns {Promise} API响应
   */
  requestRefund(orderNo, reason) {
    return request({
      url: `/api/orders/${orderNo}/refund`,
      method: 'post',
      params: { reason }
    })
  },

  /**
   * 根据订单号获取订单详情
   * @param {string} orderNo 订单号
   * @returns {Promise} API响应
   */
  getOrderByOrderNo(orderNo) {
    return request({
      url: `/api/orders/${orderNo}`,
      method: 'get'
    })
  },

  /**
   * 获取用户购买订单列表
   * @param {Object} params 查询参数
   * @param {number} params.page 页码
   * @param {number} params.size 每页大小
   * @param {string} params.sortBy 排序字段
   * @param {string} params.sortDirection 排序方向
   * @returns {Promise} API响应
   */
  getUserPurchaseOrders(params = {}) {
    return request({
      url: '/api/orders/purchases',
      method: 'get',
      params: {
        page: 0,
        size: 10,
        sortBy: 'createdTime',
        sortDirection: 'DESC',
        ...params
      }
    })
  },

  /**
   * 获取用户销售订单列表
   * @param {Object} params 查询参数
   * @returns {Promise} API响应
   */
  getUserSalesOrders(params = {}) {
    return request({
      url: '/api/orders/sales',
      method: 'get',
      params: {
        page: 0,
        size: 10,
        sortBy: 'createdTime',
        sortDirection: 'DESC',
        ...params
      }
    })
  },

  /**
   * 获取用户最近订单
   * @param {number} limit 限制数量
   * @returns {Promise} API响应
   */
  getUserRecentOrders(limit = 5) {
    return request({
      url: '/api/orders/recent',
      method: 'get',
      params: { limit }
    })
  },

  /**
   * 搜索订单
   * @param {Object} searchData 搜索条件
   * @returns {Promise} API响应
   */
  searchOrders(searchData) {
    return request({
      url: '/api/orders/search',
      method: 'post',
      data: searchData
    })
  },

  /**
   * 检查用户是否可以购买项目
   * @param {number} projectId 项目ID
   * @returns {Promise} API响应
   */
  canUserPurchaseProject(projectId) {
    return request({
      url: `/api/orders/check-purchase/${projectId}`,
      method: 'get'
    })
  },

  /**
   * 检查用户是否已购买项目
   * @param {number} projectId 项目ID
   * @returns {Promise} API响应
   */
  hasUserPurchasedProject(projectId) {
    return request({
      url: `/api/orders/check-purchased/${projectId}`,
      method: 'get'
    })
  },

  /**
   * 获取用户购买统计
   * @returns {Promise} API响应
   */
  getUserPurchaseStatistics() {
    return request({
      url: '/api/orders/statistics/purchases',
      method: 'get'
    })
  },

  /**
   * 获取用户销售统计
   * @returns {Promise} API响应
   */
  getUserSalesStatistics() {
    return request({
      url: '/api/orders/statistics/sales',
      method: 'get'
    })
  },

  /**
   * 获取项目销售统计
   * @param {number} projectId 项目ID
   * @returns {Promise} API响应
   */
  getProjectSalesStatistics(projectId) {
    return request({
      url: `/api/orders/statistics/project/${projectId}`,
      method: 'get'
    })
  }
}

/**
 * 管理员订单API
 */
export const adminOrderApi = {
  /**
   * 管理员搜索所有订单
   * @param {Object} searchData 搜索条件
   * @returns {Promise} API响应
   */
  searchAllOrders(searchData) {
    return request({
      url: '/api/orders/admin/search',
      method: 'post',
      data: searchData
    })
  },

  /**
   * 处理超时订单
   * @param {number} timeoutMinutes 超时分钟数
   * @returns {Promise} API响应
   */
  handleTimeoutOrders(timeoutMinutes = 30) {
    return request({
      url: '/api/orders/admin/handle-timeout',
      method: 'post',
      params: { timeoutMinutes }
    })
  },

  /**
   * 自动完成订单
   * @param {number} autoCompleteDays 自动完成天数
   * @returns {Promise} API响应
   */
  autoCompleteOrders(autoCompleteDays = 7) {
    return request({
      url: '/api/orders/admin/auto-complete',
      method: 'post',
      params: { autoCompleteDays }
    })
  }
}

export default orderApi
