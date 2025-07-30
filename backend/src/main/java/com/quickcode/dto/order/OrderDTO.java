package com.quickcode.dto.order;

import com.quickcode.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单数据传输对象
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 买家用户ID
     */
    private Long buyerId;

    /**
     * 买家用户名
     */
    private String buyerUsername;

    /**
     * 买家昵称
     */
    private String buyerNickname;

    /**
     * 卖家用户ID
     */
    private Long sellerId;

    /**
     * 卖家用户名
     */
    private String sellerUsername;

    /**
     * 卖家昵称
     */
    private String sellerNickname;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目描述
     */
    private String projectDescription;

    /**
     * 项目价格
     */
    private BigDecimal projectPrice;

    /**
     * 项目封面图片URL
     */
    private String projectCoverImage;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 订单状态描述
     */
    private String statusDescription;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付方式描述
     */
    private String paymentMethodDescription;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 完成时间
     */
    private LocalDateTime completionTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancellationTime;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 是否可以支付
     */
    private Boolean canPay;

    /**
     * 是否可以取消
     */
    private Boolean canCancel;

    /**
     * 是否可以退款
     */
    private Boolean canRefund;

    /**
     * 从Order实体转换为OrderDTO
     * 
     * @param order 订单实体
     * @return 订单DTO
     */
    public static OrderDTO fromOrder(Order order) {
        if (order == null) {
            return null;
        }

        OrderDTOBuilder builder = OrderDTO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .projectId(order.getProjectId())
                .amount(order.getAmount())
                .status(order.getStatus())
                .statusDescription(order.getStatusDescription())
                .paymentMethod(order.getPaymentMethod())
                .paymentTime(order.getPaymentTime())
                .completionTime(order.getCompletionTime())
                .cancellationTime(order.getCancellationTime())
                .refundTime(order.getRefundTime())
                .refundAmount(order.getRefundAmount())
                .remark(order.getRemark())
                .createdTime(order.getCreatedTime())
                .updatedTime(order.getUpdatedTime())
                .canPay(order.canPay())
                .canCancel(order.canCancel())
                .canRefund(order.canRefund());

        // 设置支付方式描述
        if (order.getPaymentMethod() != null) {
            try {
                Order.PaymentMethod method = Order.PaymentMethod.fromCode(order.getPaymentMethod());
                builder.paymentMethodDescription(method.getDescription());
            } catch (IllegalArgumentException e) {
                builder.paymentMethodDescription(order.getPaymentMethod());
            }
        }

        // 设置买家信息
        if (order.getBuyer() != null) {
            builder.buyerUsername(order.getBuyer().getUsername())
                   .buyerNickname(order.getBuyer().getNickname());
        }

        // 设置卖家信息
        if (order.getSeller() != null) {
            builder.sellerUsername(order.getSeller().getUsername())
                   .sellerNickname(order.getSeller().getNickname());
        }

        // 设置项目信息
        if (order.getProject() != null) {
            builder.projectName(order.getProject().getTitle())
                   .projectDescription(order.getProject().getDescription())
                   .projectPrice(order.getProject().getPrice())
                   .projectCoverImage(order.getProject().getCoverImage());
        }

        return builder.build();
    }

    /**
     * 从Order实体转换为简化的OrderDTO（不包含关联信息）
     * 
     * @param order 订单实体
     * @return 简化的订单DTO
     */
    public static OrderDTO fromOrderSimple(Order order) {
        if (order == null) {
            return null;
        }

        OrderDTOBuilder builder = OrderDTO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .projectId(order.getProjectId())
                .amount(order.getAmount())
                .status(order.getStatus())
                .statusDescription(order.getStatusDescription())
                .paymentMethod(order.getPaymentMethod())
                .paymentTime(order.getPaymentTime())
                .completionTime(order.getCompletionTime())
                .cancellationTime(order.getCancellationTime())
                .refundTime(order.getRefundTime())
                .refundAmount(order.getRefundAmount())
                .remark(order.getRemark())
                .createdTime(order.getCreatedTime())
                .updatedTime(order.getUpdatedTime())
                .canPay(order.canPay())
                .canCancel(order.canCancel())
                .canRefund(order.canRefund());

        // 设置支付方式描述
        if (order.getPaymentMethod() != null) {
            try {
                Order.PaymentMethod method = Order.PaymentMethod.fromCode(order.getPaymentMethod());
                builder.paymentMethodDescription(method.getDescription());
            } catch (IllegalArgumentException e) {
                builder.paymentMethodDescription(order.getPaymentMethod());
            }
        }

        return builder.build();
    }

    /**
     * 检查订单是否已完成
     */
    public boolean isCompleted() {
        return Order.OrderStatus.COMPLETED.getCode().equals(this.status);
    }

    /**
     * 检查订单是否已支付
     */
    public boolean isPaid() {
        return Order.OrderStatus.PAID.getCode().equals(this.status) ||
               Order.OrderStatus.COMPLETED.getCode().equals(this.status);
    }

    /**
     * 检查订单是否已取消
     */
    public boolean isCancelled() {
        return Order.OrderStatus.CANCELLED.getCode().equals(this.status);
    }

    /**
     * 检查订单是否已退款
     */
    public boolean isRefunded() {
        return Order.OrderStatus.REFUNDED.getCode().equals(this.status);
    }

    /**
     * 检查订单是否待支付
     */
    public boolean isPendingPayment() {
        return Order.OrderStatus.PENDING_PAYMENT.getCode().equals(this.status);
    }
}
