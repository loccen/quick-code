/**
 * 用户统计API模块
 * 提供用户项目和订单统计相关的接口
 */
import type { ApiResponse } from '@/types/api'
import { request } from '../request'

/**
 * 用户项目统计信息
 */
export interface UserProjectStats {
  /** 上传的项目数量 */
  uploadedCount: number
  /** 购买的项目数量 */
  purchasedCount: number
  /** 收藏的项目数量 */
  favoritesCount: number
  /** 总收益（积分） */
  totalEarnings: number
  /** 已发布的项目数量 */
  publishedCount: number
  /** 待审核的项目数量 */
  pendingCount: number
  /** 项目总下载次数 */
  totalDownloads: number
  /** 项目总浏览次数 */
  totalViews: number
  /** 项目总点赞次数 */
  totalLikes: number
  /** 平均项目评分 */
  averageRating: number
}

/**
 * 用户订单统计信息
 */
export interface UserOrderStats {
  /** 订单总数 */
  totalOrders: number
  /** 订单总金额（积分） */
  totalAmount: number
  /** 待支付订单数 */
  pendingOrders: number
  /** 已完成订单数 */
  completedOrders: number
  /** 已取消订单数 */
  cancelledOrders: number
  /** 已退款订单数 */
  refundedOrders: number
  /** 本月订单数 */
  monthlyOrders: number
  /** 本月消费金额 */
  monthlyAmount: number
  /** 平均订单金额 */
  averageOrderAmount: number
  /** 最近订单时间 */
  lastOrderTime?: string
  /** 最大单笔订单金额 */
  maxOrderAmount: number
  /** 最小单笔订单金额 */
  minOrderAmount: number
}

/**
 * 用户统计API服务
 */
export const userStatsApi = {
  /**
   * 获取用户项目统计信息
   */
  getUserProjectStats(): Promise<ApiResponse<UserProjectStats>> {
    return request.get('/projects/user/stats')
  },

  /**
   * 获取用户订单统计信息
   */
  getUserOrderStats(): Promise<ApiResponse<UserOrderStats>> {
    return request.get('/orders/statistics/user')
  }
}

export default userStatsApi
