package com.quickcode.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 积分账户实体类 对应数据库表：point_accounts
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
@Table(name = "point_accounts",
    indexes = {@Index(name = "idx_user_id", columnList = "user_id", unique = true)})
public class PointAccount extends BaseEntity {

  /**
   * 用户ID
   */
  @NotNull(message = "用户ID不能为空")
  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  /**
   * 总积分
   */
  @Builder.Default
  @DecimalMin(value = "0.00", message = "总积分不能为负数")
  @Column(name = "total_points", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalPoints = BigDecimal.ZERO;

  /**
   * 可用积分
   */
  @Builder.Default
  @DecimalMin(value = "0.00", message = "可用积分不能为负数")
  @Column(name = "available_points", nullable = false, precision = 15, scale = 2)
  private BigDecimal availablePoints = BigDecimal.ZERO;

  /**
   * 冻结积分
   */
  @Builder.Default
  @DecimalMin(value = "0.00", message = "冻结积分不能为负数")
  @Column(name = "frozen_points", nullable = false, precision = 15, scale = 2)
  private BigDecimal frozenPoints = BigDecimal.ZERO;

  /**
   * 累计获得积分
   */
  @Builder.Default
  @DecimalMin(value = "0.00", message = "累计获得积分不能为负数")
  @Column(name = "total_earned", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalEarned = BigDecimal.ZERO;

  /**
   * 累计消费积分
   */
  @Builder.Default
  @DecimalMin(value = "0.00", message = "累计消费积分不能为负数")
  @Column(name = "total_spent", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalSpent = BigDecimal.ZERO;

  /**
   * 关联的用户
   */
  @JsonIgnore
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  /**
   * 检查可用积分是否足够
   */
  public boolean hasEnoughPoints(BigDecimal amount) {
    return availablePoints.compareTo(amount) >= 0;
  }

  /**
   * 增加积分
   */
  public void addPoints(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw com.quickcode.common.exception.InvalidParameterException.invalidPointAmount();
    }

    this.totalPoints = this.totalPoints.add(amount);
    this.availablePoints = this.availablePoints.add(amount);
    this.totalEarned = this.totalEarned.add(amount);
  }

  /**
   * 扣减积分
   */
  public void deductPoints(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw com.quickcode.common.exception.InvalidParameterException.invalidPointAmount();
    }

    if (!hasEnoughPoints(amount)) {
      throw com.quickcode.common.exception.InsufficientResourceException.insufficientPoints();
    }

    this.availablePoints = this.availablePoints.subtract(amount);
    this.totalSpent = this.totalSpent.add(amount);
  }

  /**
   * 冻结积分
   */
  public void freezePoints(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw com.quickcode.common.exception.InvalidParameterException.invalidPointAmount();
    }

    if (!hasEnoughPoints(amount)) {
      throw com.quickcode.common.exception.InsufficientResourceException.insufficientPoints();
    }

    this.availablePoints = this.availablePoints.subtract(amount);
    this.frozenPoints = this.frozenPoints.add(amount);
  }

  /**
   * 解冻积分
   */
  public void unfreezePoints(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw com.quickcode.common.exception.InvalidParameterException.invalidPointAmount();
    }

    if (frozenPoints.compareTo(amount) < 0) {
      throw com.quickcode.common.exception.InsufficientResourceException.insufficientFrozenPoints();
    }

    this.frozenPoints = this.frozenPoints.subtract(amount);
    this.availablePoints = this.availablePoints.add(amount);
  }

  /**
   * 扣减冻结积分（用于确认消费）
   */
  public void deductFrozenPoints(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw com.quickcode.common.exception.InvalidParameterException.invalidPointAmount();
    }

    if (frozenPoints.compareTo(amount) < 0) {
      throw com.quickcode.common.exception.InsufficientResourceException.insufficientFrozenPoints();
    }

    this.frozenPoints = this.frozenPoints.subtract(amount);
    this.totalSpent = this.totalSpent.add(amount);
  }

  /**
   * 获取积分使用率
   */
  public BigDecimal getUsageRate() {
    if (totalEarned.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return totalSpent.divide(totalEarned, 4, RoundingMode.HALF_UP);
  }

  /**
   * 验证积分账户数据一致性
   */
  public boolean isConsistent() {
    BigDecimal calculatedTotal = availablePoints.add(frozenPoints);
    return totalPoints.compareTo(calculatedTotal) == 0;
  }
}
