package com.quickcode.service;

import com.quickcode.entity.PointAccount;
import com.quickcode.entity.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 积分管理服务接口
 * 提供积分账户管理、交易记录、充值转账等功能
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface PointService {

    // ==================== 积分账户管理 ====================

    /**
     * 获取用户积分账户
     */
    Optional<PointAccount> getPointAccount(Long userId);

    /**
     * 创建积分账户
     */
    PointAccount createPointAccount(Long userId);

    /**
     * 获取或创建积分账户
     */
    PointAccount getOrCreatePointAccount(Long userId);

    /**
     * 检查积分余额是否足够
     */
    boolean checkBalance(Long userId, BigDecimal amount);

    // ==================== 积分交易 ====================

    /**
     * 充值积分
     */
    PointTransaction rechargePoints(Long userId, BigDecimal amount, String description);

    /**
     * 消费积分
     */
    PointTransaction consumePoints(Long userId, BigDecimal amount, String description, String relatedOrderNo);

    /**
     * 转账积分
     */
    PointTransaction transferPoints(Long fromUserId, Long toUserId, BigDecimal amount, String description);

    /**
     * 冻结积分
     */
    boolean freezePoints(Long userId, BigDecimal amount, String reason);

    /**
     * 解冻积分
     */
    boolean unfreezePoints(Long userId, BigDecimal amount, String reason);

    /**
     * 退还积分
     */
    PointTransaction refundPoints(Long userId, BigDecimal amount, String description, String relatedOrderNo);

    // ==================== 交易记录查询 ====================

    /**
     * 获取用户积分交易记录
     */
    Page<PointTransaction> getUserTransactions(Long userId, Pageable pageable);

    /**
     * 根据类型获取用户积分交易记录
     */
    Page<PointTransaction> getUserTransactionsByType(Long userId, String type, Pageable pageable);

    /**
     * 获取用户最近交易记录
     */
    List<PointTransaction> getRecentTransactions(Long userId, int limit);

    // ==================== 统计信息 ====================

    /**
     * 获取用户积分统计
     */
    Object getUserPointStatistics(Long userId);

    /**
     * 获取系统积分统计（管理员）
     */
    Object getSystemPointStatistics();

    // ==================== 管理员功能 ====================

    /**
     * 手动调整用户积分（管理员）
     */
    PointTransaction adjustUserPoints(Long userId, BigDecimal amount, String reason, Long adminUserId);

    /**
     * 批量发放积分奖励（管理员）
     */
    List<PointTransaction> batchRewardPoints(List<Long> userIds, BigDecimal amount, String reason, Long adminUserId);

    /**
     * 获取所有用户积分账户（管理员）
     */
    Page<PointAccount> getAllPointAccounts(Pageable pageable);

    /**
     * 查找积分不一致的账户（管理员）
     */
    List<PointAccount> findInconsistentAccounts();
}
