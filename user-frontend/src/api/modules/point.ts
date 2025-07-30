/**
 * 积分管理API模块
 * 提供积分账户、交易记录、充值等功能
 */
import type { ApiResponse, PageResponse } from '@/types/api'
import { request } from '../request'

/**
 * 积分交易类型枚举（与后端PointTransaction.Type保持一致）
 */
export enum PointTransactionType {
  RECHARGE = 'RECHARGE',           // 充值
  CONSUME = 'CONSUME',             // 消费
  REWARD = 'REWARD',               // 奖励
  REFUND = 'REFUND',              // 退款
  WITHDRAW = 'WITHDRAW'            // 提现
}

/**
 * 积分交易类型显示配置
 */
export const POINT_TRANSACTION_TYPE_CONFIG = {
  [PointTransactionType.RECHARGE]: {
    label: '充值',
    color: 'success',
    isIncome: true
  },
  [PointTransactionType.CONSUME]: {
    label: '消费',
    color: 'danger',
    isIncome: false
  },
  [PointTransactionType.REWARD]: {
    label: '奖励',
    color: 'warning',
    isIncome: true
  },
  [PointTransactionType.REFUND]: {
    label: '退款',
    color: 'info',
    isIncome: true
  },
  [PointTransactionType.WITHDRAW]: {
    label: '提现',
    color: 'danger',
    isIncome: false
  }
}

/**
 * 积分账户信息
 */
export interface PointAccount {
  id: number
  userId: number
  totalPoints: number
  availablePoints: number
  frozenPoints: number
  totalEarned: number
  totalSpent: number
  createdTime: string
  updatedTime: string
}

/**
 * 积分交易记录
 */
export interface PointTransaction {
  id: number
  userId: number
  username: string
  type: PointTransactionType
  amount: number
  balanceBefore: number
  balanceAfter: number
  description: string
  relatedOrderNo?: string
  relatedProjectId?: number
  createdTime: string
}

/**
 * 积分统计信息
 */
export interface PointStatistics {
  totalUsers: number
  totalBalance: number
  totalRecharge: number
  totalConsumption: number
  averageBalance: number
  transactionCount: number
  recentTransactions: PointTransaction[]
}

/**
 * 积分查询参数
 */
export interface PointQueryParams {
  page?: number
  size?: number
  type?: PointTransactionType | string
  startDate?: string
  endDate?: string
  minAmount?: number
  maxAmount?: number
  sortBy?: string
  sortDirection?: 'ASC' | 'DESC'
}

/**
 * 充值请求参数
 */
export interface RechargeRequest {
  amount: number
  paymentMethod: string
  description?: string
}

/**
 * 转账请求参数
 */
export interface TransferRequest {
  toUserId: number
  amount: number
  description?: string
}

/**
 * 积分API
 */
export const pointApi = {
  /**
   * 获取积分账户信息
   */
  getPointAccount(): Promise<ApiResponse<PointAccount>> {
    return request.get('/points/account')
  },

  /**
   * 获取积分交易记录
   */
  getPointTransactions(params: PointQueryParams = {}): Promise<ApiResponse<PageResponse<PointTransaction>>> {
    return request.get('/points/transactions', {
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
   * 积分充值
   */
  rechargePoints(data: URLSearchParams): Promise<ApiResponse<PointTransaction>> {
    return request.post('/points/recharge', data, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
  },

  /**
   * 积分转账
   */
  transferPoints(data: TransferRequest): Promise<ApiResponse<PointTransaction>> {
    return request.post('/points/transfer', data)
  },

  /**
   * 获取积分统计
   */
  getPointStatistics(): Promise<ApiResponse<PointStatistics>> {
    return request.get('/points/statistics')
  },

  /**
   * 检查积分余额是否足够
   */
  checkBalance(amount: number): Promise<ApiResponse<boolean>> {
    return request.get('/points/check-balance', { params: { amount } })
  },

  /**
   * 冻结积分
   */
  freezePoints(amount: number, reason: string): Promise<ApiResponse<boolean>> {
    return request.post('/points/freeze', { amount, reason })
  },

  /**
   * 解冻积分
   */
  unfreezePoints(amount: number, reason: string): Promise<ApiResponse<boolean>> {
    return request.post('/points/unfreeze', { amount, reason })
  },

  /**
   * 获取积分使用规则
   */
  getPointRules(): Promise<ApiResponse<any>> {
    return request.get('/points/rules')
  },

  /**
   * 获取积分兑换比例
   */
  getExchangeRates(): Promise<ApiResponse<any>> {
    return request.get('/points/exchange-rates')
  },

  /**
   * 计算购买所需积分
   */
  calculatePurchasePoints(projectId: number): Promise<ApiResponse<{ requiredPoints: number, projectPrice: number }>> {
    return request.get(`/points/calculate-purchase/${projectId}`)
  }
}

/**
 * 管理员积分API
 */
export const adminPointApi = {
  /**
   * 获取所有用户积分账户（管理员）
   */
  getAllPointAccounts(params: any = {}): Promise<ApiResponse<PageResponse<PointAccount>>> {
    return request.get('/points/admin/accounts', { params })
  },

  /**
   * 获取系统积分统计（管理员）
   */
  getSystemPointStatistics(): Promise<ApiResponse<PointStatistics>> {
    return request.get('/points/admin/statistics')
  },

  /**
   * 手动调整用户积分（管理员）
   */
  adjustUserPoints(userId: number, amount: number, reason: string): Promise<ApiResponse<PointTransaction>> {
    return request.post('/points/admin/adjust', { userId, amount, reason })
  },

  /**
   * 批量发放积分奖励（管理员）
   */
  batchRewardPoints(userIds: number[], amount: number, reason: string): Promise<ApiResponse<PointTransaction[]>> {
    return request.post('/points/admin/batch-reward', { userIds, amount, reason })
  },

  /**
   * 设置积分规则（管理员）
   */
  setPointRules(rules: any): Promise<ApiResponse<boolean>> {
    return request.post('/points/admin/rules', rules)
  },

  /**
   * 设置兑换比例（管理员）
   */
  setExchangeRates(rates: any): Promise<ApiResponse<boolean>> {
    return request.post('/points/admin/exchange-rates', rates)
  }
}
