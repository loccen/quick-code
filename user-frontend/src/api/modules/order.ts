/**
 * 订单管理API模块
 * 提供订单创建、支付、查询等功能
 */
import type { ApiResponse, PageResponse } from '@/types/api'
import { request } from '../request'

/**
 * 订单状态枚举
 */
export enum OrderStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED',
  REFUNDED = 'REFUNDED'
}

/**
 * 订单查询参数
 */
export interface OrderQueryParams {
  page?: number
  size?: number
  status?: OrderStatus | string
  startDate?: string
  endDate?: string
  sortBy?: string
  sortDirection?: 'ASC' | 'DESC'
}

/**
 * 创建订单请求参数
 */
export interface CreateOrderRequest {
  projectId: number
  remark?: string
}

/**
 * 支付订单请求参数
 */
export interface PayOrderRequest {
  paymentMethod?: string
  [key: string]: any
}

/**
 * 订单信息
 */
export interface Order {
  id: number
  orderNo: string
  buyerId: number
  buyerUsername: string
  buyerNickname?: string
  sellerId: number
  sellerUsername: string
  sellerNickname?: string
  projectId: number
  projectName: string
  projectDescription: string
  projectPrice: number
  projectCoverImage?: string
  amount: number
  status: number
  statusDescription: string
  paymentMethod?: string
  paymentMethodDescription?: string
  paymentTime?: string
  completionTime?: string
  cancellationTime?: string
  refundTime?: string
  refundAmount?: number
  remark?: string
  createdTime: string
  updatedTime: string
  canPay: boolean
  canCancel: boolean
  canRefund: boolean
  cancelled: boolean
  completed: boolean
  pendingPayment: boolean
  paid: boolean
  refunded: boolean
}

/**
 * 订单统计信息
 */
export interface OrderStatistics {
  totalOrders: number
  totalAmount: number
  pendingOrders: number
  paidOrders: number
  completedOrders: number
  cancelledOrders: number
  refundedOrders: number
}

/**
 * 订单API
 */
export const orderApi = {
  /**
   * 创建订单
   */
  createOrder(data: CreateOrderRequest): Promise<ApiResponse<Order>> {
    return request.post('/orders', data)
  },

  /**
   * 支付订单
   */
  payOrder(orderNo: string, data: PayOrderRequest): Promise<ApiResponse<Order>> {
    return request.post(`/orders/${orderNo}/pay`, data)
  },

  /**
   * 取消订单
   */
  cancelOrder(orderNo: string, reason?: string): Promise<ApiResponse<Order>> {
    return request.post(`/orders/${orderNo}/cancel`, { reason })
  },

  /**
   * 完成订单
   */
  completeOrder(orderNo: string): Promise<ApiResponse<Order>> {
    return request.post(`/orders/${orderNo}/complete`)
  },

  /**
   * 申请退款
   */
  requestRefund(orderNo: string, reason?: string): Promise<ApiResponse<Order>> {
    return request.post(`/orders/${orderNo}/refund`, { reason })
  },

  /**
   * 根据订单号获取订单详情
   */
  getOrderByOrderNo(orderNo: string): Promise<ApiResponse<Order>> {
    return request.get(`/orders/${orderNo}`)
  },

  /**
   * 获取用户购买订单列表
   */
  getUserPurchaseOrders(params: OrderQueryParams = {}): Promise<ApiResponse<PageResponse<Order>>> {
    return request.get('/orders/purchases', {
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
   */
  getUserSalesOrders(params: OrderQueryParams = {}): Promise<ApiResponse<PageResponse<Order>>> {
    return request.get('/orders/sales', {
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
   */
  getUserRecentOrders(limit: number = 5): Promise<ApiResponse<Order[]>> {
    return request.get('/orders/recent', { params: { limit } })
  },

  /**
   * 检查用户是否可以购买项目
   */
  canUserPurchaseProject(projectId: number): Promise<ApiResponse<boolean>> {
    return request.get(`/orders/check-purchase/${projectId}`)
  },

  /**
   * 检查用户是否已购买项目
   */
  hasUserPurchasedProject(projectId: number): Promise<ApiResponse<boolean>> {
    return request.get(`/orders/check-purchased/${projectId}`)
  },

  /**
   * 获取用户购买统计
   */
  getUserPurchaseStatistics(): Promise<ApiResponse<OrderStatistics>> {
    return request.get('/orders/statistics/purchases')
  },

  /**
   * 获取用户销售统计
   */
  getUserSalesStatistics(): Promise<ApiResponse<OrderStatistics>> {
    return request.get('/orders/statistics/sales')
  },

  /**
   * 获取项目销售统计
   */
  getProjectSalesStatistics(projectId: number): Promise<ApiResponse<OrderStatistics>> {
    return request.get(`/orders/statistics/project/${projectId}`)
  },

  /**
   * 获取用户订单统计信息
   */
  getUserOrderStatistics(): Promise<ApiResponse<{
    totalOrders: number
    totalAmount: number
    pendingOrders: number
    completedOrders: number
  }>> {
    return request.get('/orders/statistics/user')
  },

}

/**
 * 管理员订单API
 */
export const adminOrderApi = {
  /**
   * 搜索所有订单（管理员）
   */
  searchAllOrders(searchData: any): Promise<ApiResponse<PageResponse<Order>>> {
    return request.post('/orders/admin/search', searchData)
  },

  /**
   * 处理超时订单（管理员）
   */
  handleTimeoutOrders(timeoutMinutes: number = 30): Promise<ApiResponse<any>> {
    return request.post('/orders/admin/handle-timeout', { timeoutMinutes })
  },

  /**
   * 自动完成订单（管理员）
   */
  autoCompleteOrders(autoCompleteDays: number = 7): Promise<ApiResponse<any>> {
    return request.post('/orders/admin/auto-complete', { autoCompleteDays })
  }
}
