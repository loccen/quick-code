import request from '@/utils/request'

/**
 * 积分管理API
 */
export const pointApi = {
  /**
   * 获取用户积分账户信息
   * @returns {Promise} API响应
   */
  getPointAccount() {
    return request({
      url: '/api/points/account',
      method: 'get'
    })
  },

  /**
   * 获取积分交易记录
   * @param {Object} params 查询参数
   * @param {number} params.page 页码
   * @param {number} params.size 每页大小
   * @param {string} params.type 交易类型
   * @param {string} params.startDate 开始日期
   * @param {string} params.endDate 结束日期
   * @returns {Promise} API响应
   */
  getPointTransactions(params = {}) {
    return request({
      url: '/api/points/transactions',
      method: 'get',
      params: {
        page: 0,
        size: 10,
        ...params
      }
    })
  },

  /**
   * 积分充值
   * @param {Object} data 充值数据
   * @param {number} data.amount 充值金额
   * @param {string} data.paymentMethod 支付方式
   * @returns {Promise} API响应
   */
  rechargePoints(data) {
    return request({
      url: '/api/points/recharge',
      method: 'post',
      data
    })
  },

  /**
   * 积分转账
   * @param {Object} data 转账数据
   * @param {number} data.toUserId 接收用户ID
   * @param {number} data.amount 转账金额
   * @param {string} data.remark 转账备注
   * @returns {Promise} API响应
   */
  transferPoints(data) {
    return request({
      url: '/api/points/transfer',
      method: 'post',
      data
    })
  },

  /**
   * 获取积分统计信息
   * @returns {Promise} API响应
   */
  getPointStatistics() {
    return request({
      url: '/api/points/statistics',
      method: 'get'
    })
  },

  /**
   * 获取积分规则配置
   * @returns {Promise} API响应
   */
  getPointRules() {
    return request({
      url: '/api/points/rules',
      method: 'get'
    })
  },

  /**
   * 签到获取积分
   * @returns {Promise} API响应
   */
  dailyCheckIn() {
    return request({
      url: '/api/points/checkin',
      method: 'post'
    })
  },

  /**
   * 获取签到状态
   * @returns {Promise} API响应
   */
  getCheckInStatus() {
    return request({
      url: '/api/points/checkin/status',
      method: 'get'
    })
  }
}

export default pointApi
