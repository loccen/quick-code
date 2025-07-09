package com.quickcode.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 积分交易记录实体类
 * 对应数据库表：point_transactions
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
@Table(name = "point_transactions", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_time", columnList = "created_time"),
    @Index(name = "idx_reference", columnList = "reference_type, reference_id")
})
public class PointTransaction extends BaseEntity {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 交易类型
     * 1: 充值
     * 2: 消费
     * 3: 奖励
     * 4: 退款
     * 5: 提现
     */
    @NotNull(message = "交易类型不能为空")
    @Column(name = "type", nullable = false)
    private Integer type;

    /**
     * 交易金额
     */
    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "交易金额必须大于0")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * 交易前余额
     */
    @NotNull(message = "交易前余额不能为空")
    @DecimalMin(value = "0.00", message = "交易前余额不能为负数")
    @Column(name = "balance_before", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    @NotNull(message = "交易后余额不能为空")
    @DecimalMin(value = "0.00", message = "交易后余额不能为负数")
    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    /**
     * 交易描述
     */
    @Size(max = 255, message = "交易描述长度不能超过255个字符")
    @Column(name = "description")
    private String description;

    /**
     * 关联ID（订单ID、项目ID等）
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * 关联类型
     */
    @Size(max = 50, message = "关联类型长度不能超过50个字符")
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * 交易状态
     * 0: 失败
     * 1: 成功
     * 2: 处理中
     */
    @Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 关联的用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 交易类型枚举
     */
    public enum Type {
        RECHARGE(1, "充值"),
        CONSUME(2, "消费"),
        REWARD(3, "奖励"),
        REFUND(4, "退款"),
        WITHDRAW(5, "提现");

        private final Integer code;
        private final String description;

        Type(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static Type fromCode(Integer code) {
            for (Type type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("未知的交易类型代码: " + code);
        }
    }

    /**
     * 交易状态枚举
     */
    public enum Status {
        FAILED(0, "失败"),
        SUCCESS(1, "成功"),
        PROCESSING(2, "处理中");

        private final Integer code;
        private final String description;

        Status(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static Status fromCode(Integer code) {
            for (Status status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("未知的交易状态代码: " + code);
        }
    }

    /**
     * 关联类型枚举
     */
    public static class ReferenceType {
        public static final String ORDER = "ORDER";
        public static final String PROJECT = "PROJECT";
        public static final String REGISTER = "REGISTER";
        public static final String UPLOAD = "UPLOAD";
        public static final String REVIEW = "REVIEW";
        public static final String MANUAL = "MANUAL";
    }

    /**
     * 检查交易是否成功
     */
    public boolean isSuccess() {
        return Status.SUCCESS.getCode().equals(this.status);
    }

    /**
     * 检查交易是否失败
     */
    public boolean isFailed() {
        return Status.FAILED.getCode().equals(this.status);
    }

    /**
     * 检查交易是否处理中
     */
    public boolean isProcessing() {
        return Status.PROCESSING.getCode().equals(this.status);
    }

    /**
     * 检查是否为收入交易
     */
    public boolean isIncome() {
        return Type.RECHARGE.getCode().equals(this.type) || 
               Type.REWARD.getCode().equals(this.type) || 
               Type.REFUND.getCode().equals(this.type);
    }

    /**
     * 检查是否为支出交易
     */
    public boolean isExpense() {
        return Type.CONSUME.getCode().equals(this.type) || 
               Type.WITHDRAW.getCode().equals(this.type);
    }

    /**
     * 验证交易数据一致性
     */
    public boolean isConsistent() {
        if (isIncome()) {
            return balanceAfter.subtract(balanceBefore).compareTo(amount) == 0;
        } else if (isExpense()) {
            return balanceBefore.subtract(balanceAfter).compareTo(amount) == 0;
        }
        return false;
    }
}
