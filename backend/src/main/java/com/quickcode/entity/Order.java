package com.quickcode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 对应数据库表：orders
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_buyer_id", columnList = "buyer_id"),
    @Index(name = "idx_seller_id", columnList = "seller_id"),
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_time", columnList = "created_time"),
    @Index(name = "idx_order_no", columnList = "order_no", unique = true)
})
public class Order extends BaseEntity {

    /**
     * 订单号（唯一标识）
     */
    @NotNull(message = "订单号不能为空")
    @Size(max = 32, message = "订单号长度不能超过32个字符")
    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    /**
     * 买家用户ID
     */
    @NotNull(message = "买家用户ID不能为空")
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    /**
     * 卖家用户ID
     */
    @NotNull(message = "卖家用户ID不能为空")
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 订单金额（积分）
     */
    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.00", message = "订单金额不能为负数")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * 订单状态
     * 0: 待支付
     * 1: 已支付
     * 2: 已完成
     * 3: 已取消
     * 4: 已退款
     */
    @Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 支付方式
     */
    @Size(max = 50, message = "支付方式长度不能超过50个字符")
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /**
     * 支付时间
     */
    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    /**
     * 完成时间
     */
    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    /**
     * 取消时间
     */
    @Column(name = "cancellation_time")
    private LocalDateTime cancellationTime;

    /**
     * 退款时间
     */
    @Column(name = "refund_time")
    private LocalDateTime refundTime;

    /**
     * 退款金额
     */
    @DecimalMin(value = "0.00", message = "退款金额不能为负数")
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    /**
     * 订单备注
     */
    @Size(max = 500, message = "订单备注长度不能超过500个字符")
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 关联的买家用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", insertable = false, updatable = false)
    private User buyer;

    /**
     * 关联的卖家用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User seller;

    /**
     * 关联的项目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        PENDING_PAYMENT(0, "待支付"),
        PAID(1, "已支付"),
        COMPLETED(2, "已完成"),
        CANCELLED(3, "已取消"),
        REFUNDED(4, "已退款");

        private final Integer code;
        private final String description;

        OrderStatus(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static OrderStatus fromCode(Integer code) {
            for (OrderStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("未知的订单状态代码: " + code);
        }
    }

    /**
     * 支付方式枚举
     */
    public enum PaymentMethod {
        POINTS("POINTS", "积分支付"),
        BALANCE("BALANCE", "余额支付"),
        MIXED("MIXED", "混合支付");

        private final String code;
        private final String description;

        PaymentMethod(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static PaymentMethod fromCode(String code) {
            for (PaymentMethod method : values()) {
                if (method.code.equals(code)) {
                    return method;
                }
            }
            throw new IllegalArgumentException("未知的支付方式代码: " + code);
        }
    }

    // ==================== 业务方法 ====================

    /**
     * 检查订单是否可以支付
     */
    public boolean canPay() {
        return OrderStatus.PENDING_PAYMENT.getCode().equals(this.status);
    }

    /**
     * 检查订单是否可以取消
     */
    public boolean canCancel() {
        return OrderStatus.PENDING_PAYMENT.getCode().equals(this.status) ||
               OrderStatus.PAID.getCode().equals(this.status);
    }

    /**
     * 检查订单是否可以退款
     */
    public boolean canRefund() {
        return OrderStatus.PAID.getCode().equals(this.status) ||
               OrderStatus.COMPLETED.getCode().equals(this.status);
    }

    /**
     * 标记订单为已支付
     */
    public void markAsPaid(String paymentMethod) {
        this.status = OrderStatus.PAID.getCode();
        this.paymentMethod = paymentMethod;
        this.paymentTime = LocalDateTime.now();
    }

    /**
     * 标记订单为已完成
     */
    public void markAsCompleted() {
        this.status = OrderStatus.COMPLETED.getCode();
        this.completionTime = LocalDateTime.now();
    }

    /**
     * 标记订单为已取消
     */
    public void markAsCancelled(String reason) {
        this.status = OrderStatus.CANCELLED.getCode();
        this.cancellationTime = LocalDateTime.now();
        this.remark = reason;
    }

    /**
     * 标记订单为已退款
     */
    public void markAsRefunded(BigDecimal refundAmount) {
        this.status = OrderStatus.REFUNDED.getCode();
        this.refundTime = LocalDateTime.now();
        this.refundAmount = refundAmount;
    }

    /**
     * 获取订单状态描述
     */
    public String getStatusDescription() {
        return OrderStatus.fromCode(this.status).getDescription();
    }

    /**
     * 检查订单是否已完成
     */
    public boolean isCompleted() {
        return OrderStatus.COMPLETED.getCode().equals(this.status);
    }

    /**
     * 检查订单是否已支付
     */
    public boolean isPaid() {
        return OrderStatus.PAID.getCode().equals(this.status) ||
               OrderStatus.COMPLETED.getCode().equals(this.status);
    }

    /**
     * 检查订单是否已取消
     */
    public boolean isCancelled() {
        return OrderStatus.CANCELLED.getCode().equals(this.status);
    }

    /**
     * 检查订单是否已退款
     */
    public boolean isRefunded() {
        return OrderStatus.REFUNDED.getCode().equals(this.status);
    }
}
