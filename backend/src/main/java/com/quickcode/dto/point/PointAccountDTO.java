package com.quickcode.dto.point;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分账户DTO
 * 用于API响应，避免实体序列化问题
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointAccountDTO {

    /**
     * 账户ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 总积分
     */
    private BigDecimal totalPoints;

    /**
     * 可用积分
     */
    private BigDecimal availablePoints;

    /**
     * 冻结积分
     */
    private BigDecimal frozenPoints;

    /**
     * 累计获得积分
     */
    private BigDecimal totalEarned;

    /**
     * 累计消费积分
     */
    private BigDecimal totalSpent;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
