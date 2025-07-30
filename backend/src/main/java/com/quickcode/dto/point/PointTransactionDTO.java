package com.quickcode.dto.point;

import com.quickcode.entity.PointTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分交易记录DTO
 * 用于API响应，避免实体序列化问题
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointTransactionDTO {

    /**
     * 交易记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 交易类型代码
     */
    private Integer type;

    /**
     * 交易类型名称
     */
    private String typeName;

    /**
     * 交易类型描述
     */
    private String typeDescription;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 交易描述
     */
    private String description;

    /**
     * 关联ID（订单ID、项目ID等）
     */
    private Long referenceId;

    /**
     * 关联类型
     */
    private String referenceType;

    /**
     * 交易状态代码
     */
    private Integer status;

    /**
     * 交易状态描述
     */
    private String statusDescription;

    /**
     * 是否为收入交易
     */
    private Boolean isIncome;

    /**
     * 是否为支出交易
     */
    private Boolean isExpense;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 从PointTransaction实体转换为DTO
     */
    public static PointTransactionDTO fromPointTransaction(PointTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        // 获取交易类型信息
        PointTransaction.Type transactionType = null;
        String typeName = "UNKNOWN";
        String typeDescription = "未知类型";
        
        try {
            transactionType = PointTransaction.Type.fromCode(transaction.getType());
            typeName = transactionType.name();
            typeDescription = transactionType.getDescription();
        } catch (IllegalArgumentException e) {
            // 处理未知类型
        }

        // 获取交易状态信息
        PointTransaction.Status transactionStatus = null;
        String statusDescription = "未知状态";
        
        try {
            transactionStatus = PointTransaction.Status.fromCode(transaction.getStatus());
            statusDescription = transactionStatus.getDescription();
        } catch (IllegalArgumentException e) {
            // 处理未知状态
        }

        return PointTransactionDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .type(transaction.getType())
                .typeName(typeName)
                .typeDescription(typeDescription)
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .referenceType(transaction.getReferenceType())
                .status(transaction.getStatus())
                .statusDescription(statusDescription)
                .isIncome(transaction.isIncome())
                .isExpense(transaction.isExpense())
                .createdTime(transaction.getCreatedTime())
                .updatedTime(transaction.getUpdatedTime())
                .build();
    }
}
