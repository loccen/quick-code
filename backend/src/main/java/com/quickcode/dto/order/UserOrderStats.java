package com.quickcode.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户订单统计数据DTO
 * 包含用户相关的订单统计信息
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderStats {

    /**
     * 订单总数
     */
    private Long totalOrders;

    /**
     * 订单总金额（积分）
     */
    private BigDecimal totalAmount;

    /**
     * 待支付订单数
     */
    private Long pendingOrders;

    /**
     * 已完成订单数
     */
    private Long completedOrders;

    /**
     * 已取消订单数
     */
    private Long cancelledOrders;

    /**
     * 已退款订单数
     */
    private Long refundedOrders;

    /**
     * 本月订单数
     */
    private Long monthlyOrders;

    /**
     * 本月消费金额
     */
    private BigDecimal monthlyAmount;

    /**
     * 平均订单金额
     */
    private BigDecimal averageOrderAmount;

    /**
     * 最近订单时间
     */
    private LocalDateTime lastOrderTime;

    /**
     * 最大单笔订单金额
     */
    private BigDecimal maxOrderAmount;

    /**
     * 最小单笔订单金额
     */
    private BigDecimal minOrderAmount;

    /**
     * 创建空的统计数据
     * 
     * @return 空的统计数据
     */
    public static UserOrderStats empty() {
        return UserOrderStats.builder()
                .totalOrders(0L)
                .totalAmount(BigDecimal.ZERO)
                .pendingOrders(0L)
                .completedOrders(0L)
                .cancelledOrders(0L)
                .refundedOrders(0L)
                .monthlyOrders(0L)
                .monthlyAmount(BigDecimal.ZERO)
                .averageOrderAmount(BigDecimal.ZERO)
                .maxOrderAmount(BigDecimal.ZERO)
                .minOrderAmount(BigDecimal.ZERO)
                .build();
    }

    /**
     * 检查是否有数据
     * 
     * @return 是否有数据
     */
    public boolean hasData() {
        return totalOrders > 0 || totalAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 获取订单完成率（已完成订单占总订单的比例）
     * 
     * @return 完成率（0-1）
     */
    public BigDecimal getCompletionRate() {
        if (totalOrders == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(completedOrders)
                .divide(BigDecimal.valueOf(totalOrders), 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取订单取消率（已取消订单占总订单的比例）
     * 
     * @return 取消率（0-1）
     */
    public BigDecimal getCancellationRate() {
        if (totalOrders == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(cancelledOrders)
                .divide(BigDecimal.valueOf(totalOrders), 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取用户活跃度评分（0-100）
     * 基于订单数量、金额、频率等计算
     * 
     * @return 活跃度评分
     */
    public int getActivityScore() {
        if (!hasData()) {
            return 0;
        }

        // 简单的活跃度计算公式
        long orderScore = totalOrders * 5;
        long amountScore = totalAmount.divide(BigDecimal.valueOf(100), 0, BigDecimal.ROUND_DOWN).longValue();
        long monthlyScore = monthlyOrders * 10;
        
        long totalScore = orderScore + amountScore + monthlyScore;
        return Math.min(100, (int) totalScore);
    }

    /**
     * 获取消费等级描述
     * 
     * @return 消费等级
     */
    public String getConsumerLevel() {
        if (totalAmount.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            return "钻石用户";
        } else if (totalAmount.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            return "黄金用户";
        } else if (totalAmount.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return "白银用户";
        } else if (totalAmount.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return "青铜用户";
        } else {
            return "新手用户";
        }
    }

    /**
     * 计算平均订单金额
     */
    public void calculateAverageOrderAmount() {
        if (totalOrders > 0) {
            this.averageOrderAmount = totalAmount.divide(
                    BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.averageOrderAmount = BigDecimal.ZERO;
        }
    }
}
