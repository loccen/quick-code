package com.quickcode.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付请求
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    /**
     * 支付方式
     */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /**
     * 使用积分金额
     */
    @DecimalMin(value = "0.00", message = "使用积分金额不能为负数")
    private BigDecimal pointsAmount;

    /**
     * 使用余额金额
     */
    @DecimalMin(value = "0.00", message = "使用余额金额不能为负数")
    private BigDecimal balanceAmount;

    /**
     * 支付密码（如果需要）
     */
    private String paymentPassword;

    /**
     * 验证请求数据
     */
    public void validate() {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("支付方式不能为空");
        }

        // 验证支付方式
        try {
            com.quickcode.entity.Order.PaymentMethod.fromCode(paymentMethod);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("不支持的支付方式: " + paymentMethod);
        }

        // 验证支付金额
        if ("POINTS".equals(paymentMethod)) {
            if (pointsAmount == null || pointsAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("积分支付金额必须大于0");
            }
        } else if ("BALANCE".equals(paymentMethod)) {
            if (balanceAmount == null || balanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("余额支付金额必须大于0");
            }
        } else if ("MIXED".equals(paymentMethod)) {
            if ((pointsAmount == null || pointsAmount.compareTo(BigDecimal.ZERO) <= 0) &&
                (balanceAmount == null || balanceAmount.compareTo(BigDecimal.ZERO) <= 0)) {
                throw new IllegalArgumentException("混合支付至少需要指定一种支付金额");
            }
        }
    }

    /**
     * 标准化请求数据
     */
    public void normalize() {
        if (paymentMethod != null) {
            paymentMethod = paymentMethod.trim().toUpperCase();
        }

        if (pointsAmount == null) {
            pointsAmount = BigDecimal.ZERO;
        }

        if (balanceAmount == null) {
            balanceAmount = BigDecimal.ZERO;
        }

        if (paymentPassword != null) {
            paymentPassword = paymentPassword.trim();
            if (paymentPassword.isEmpty()) {
                paymentPassword = null;
            }
        }
    }

    /**
     * 获取总支付金额
     */
    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        if (pointsAmount != null) {
            total = total.add(pointsAmount);
        }
        if (balanceAmount != null) {
            total = total.add(balanceAmount);
        }
        return total;
    }

    /**
     * 检查是否使用积分支付
     */
    public boolean usesPoints() {
        return pointsAmount != null && pointsAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 检查是否使用余额支付
     */
    public boolean usesBalance() {
        return balanceAmount != null && balanceAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 检查是否为混合支付
     */
    public boolean isMixedPayment() {
        return usesPoints() && usesBalance();
    }
}
