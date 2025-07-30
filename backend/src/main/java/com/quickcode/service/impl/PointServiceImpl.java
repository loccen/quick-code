package com.quickcode.service.impl;

import com.quickcode.entity.PointAccount;
import com.quickcode.entity.PointTransaction;
import com.quickcode.entity.User;
import com.quickcode.repository.PointAccountRepository;
import com.quickcode.repository.PointTransactionRepository;
import com.quickcode.repository.UserRepository;
import com.quickcode.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 积分管理服务实现
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PointServiceImpl implements PointService {

    private final PointAccountRepository pointAccountRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final UserRepository userRepository;

    // ==================== 积分账户管理 ====================

    @Override
    @Transactional(readOnly = true)
    public Optional<PointAccount> getPointAccount(Long userId) {
        log.debug("获取用户积分账户: userId={}", userId);
        return pointAccountRepository.findByUserId(userId);
    }

    @Override
    public PointAccount createPointAccount(Long userId) {
        log.info("创建积分账户: userId={}", userId);

        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + userId));

        // 检查是否已有积分账户
        if (pointAccountRepository.existsByUserId(userId)) {
            throw new RuntimeException("用户已有积分账户: " + userId);
        }

        PointAccount pointAccount = PointAccount.builder()
                .userId(userId)
                .totalPoints(BigDecimal.ZERO)
                .availablePoints(BigDecimal.ZERO)
                .frozenPoints(BigDecimal.ZERO)
                .totalEarned(BigDecimal.ZERO)
                .totalSpent(BigDecimal.ZERO)
                .build();

        return pointAccountRepository.save(pointAccount);
    }

    @Override
    public PointAccount getOrCreatePointAccount(Long userId) {
        return getPointAccount(userId)
                .orElseGet(() -> createPointAccount(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkBalance(Long userId, BigDecimal amount) {
        log.debug("检查积分余额: userId={}, amount={}", userId, amount);
        
        Optional<PointAccount> accountOpt = getPointAccount(userId);
        if (accountOpt.isEmpty()) {
            return false;
        }
        
        return accountOpt.get().hasEnoughPoints(amount);
    }

    // ==================== 积分交易 ====================

    @Override
    public PointTransaction rechargePoints(Long userId, BigDecimal amount, String description) {
        log.info("充值积分: userId={}, amount={}", userId, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }

        PointAccount account = getOrCreatePointAccount(userId);
        BigDecimal balanceBefore = account.getAvailablePoints();

        // 增加积分
        account.addPoints(amount);
        pointAccountRepository.save(account);

        // 创建交易记录
        PointTransaction transaction = PointTransaction.builder()
                .userId(userId)
                .type(PointTransaction.Type.RECHARGE.getCode())
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(account.getAvailablePoints())
                .description(description != null ? description : "积分充值")
                .status(PointTransaction.Status.SUCCESS.getCode())
                .build();

        return pointTransactionRepository.save(transaction);
    }

    @Override
    public PointTransaction consumePoints(Long userId, BigDecimal amount, String description, String relatedOrderNo) {
        log.info("消费积分: userId={}, amount={}, orderNo={}", userId, amount, relatedOrderNo);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("消费金额必须大于0");
        }

        PointAccount account = getPointAccount(userId)
                .orElseThrow(() -> new RuntimeException("用户积分账户不存在"));

        if (!account.hasEnoughPoints(amount)) {
            throw new RuntimeException("积分余额不足，当前可用积分: " + account.getAvailablePoints());
        }

        BigDecimal balanceBefore = account.getAvailablePoints();

        // 扣减积分
        account.deductPoints(amount);
        pointAccountRepository.save(account);

        // 创建交易记录
        String finalDescription = description != null ? description : "积分消费";
        if (relatedOrderNo != null) {
            finalDescription += " (订单号: " + relatedOrderNo + ")";
        }

        PointTransaction transaction = PointTransaction.builder()
                .userId(userId)
                .type(PointTransaction.Type.CONSUME.getCode())
                .amount(amount.negate()) // 消费记录为负数
                .balanceBefore(balanceBefore)
                .balanceAfter(account.getAvailablePoints())
                .description(finalDescription)
                .referenceType("ORDER")
                .referenceId(null) // 暂时设为null，避免Long解析错误
                .status(PointTransaction.Status.SUCCESS.getCode())
                .build();

        return pointTransactionRepository.save(transaction);
    }

    @Override
    public PointTransaction transferPoints(Long fromUserId, Long toUserId, BigDecimal amount, String description) {
        log.info("转账积分: fromUserId={}, toUserId={}, amount={}", fromUserId, toUserId, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("转账金额必须大于0");
        }

        if (fromUserId.equals(toUserId)) {
            throw new RuntimeException("不能向自己转账");
        }

        // 扣减发送方积分
        consumePoints(fromUserId, amount, "转出积分: " + description, null);

        // 增加接收方积分
        return rechargePoints(toUserId, amount, "转入积分: " + description);
    }

    @Override
    public boolean freezePoints(Long userId, BigDecimal amount, String reason) {
        log.info("冻结积分: userId={}, amount={}, reason={}", userId, amount, reason);

        PointAccount account = getPointAccount(userId)
                .orElseThrow(() -> new RuntimeException("用户积分账户不存在"));

        if (!account.hasEnoughPoints(amount)) {
            return false;
        }

        account.freezePoints(amount);
        pointAccountRepository.save(account);

        return true;
    }

    @Override
    public boolean unfreezePoints(Long userId, BigDecimal amount, String reason) {
        log.info("解冻积分: userId={}, amount={}, reason={}", userId, amount, reason);

        PointAccount account = getPointAccount(userId)
                .orElseThrow(() -> new RuntimeException("用户积分账户不存在"));

        if (account.getFrozenPoints().compareTo(amount) < 0) {
            return false;
        }

        account.unfreezePoints(amount);
        pointAccountRepository.save(account);

        return true;
    }

    @Override
    public PointTransaction refundPoints(Long userId, BigDecimal amount, String description, String relatedOrderNo) {
        log.info("退还积分: userId={}, amount={}, orderNo={}", userId, amount, relatedOrderNo);

        return rechargePoints(userId, amount, description != null ? description : "积分退款");
    }

    // ==================== 交易记录查询 ====================

    @Override
    @Transactional(readOnly = true)
    public Page<PointTransaction> getUserTransactions(Long userId, Pageable pageable) {
        log.debug("获取用户积分交易记录: userId={}", userId);
        return pointTransactionRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointTransaction> getUserTransactionsByType(Long userId, String type, Pageable pageable) {
        log.debug("根据类型获取用户积分交易记录: userId={}, type={}", userId, type);

        try {
            // 支持多个类型，用逗号分隔
            String[] typeArray = type.split(",");
            List<Integer> typeCodes = new ArrayList<>();

            for (String typeStr : typeArray) {
                try {
                    PointTransaction.Type transactionType = PointTransaction.Type.valueOf(typeStr.trim().toUpperCase());
                    typeCodes.add(transactionType.getCode());
                } catch (IllegalArgumentException e) {
                    log.warn("无效的交易类型: {}", typeStr);
                    // 忽略无效的类型，继续处理其他类型
                }
            }

            if (typeCodes.isEmpty()) {
                // 如果没有有效的类型，返回所有交易记录
                return pointTransactionRepository.findByUserId(userId, pageable);
            } else {
                // 根据类型代码查询
                return pointTransactionRepository.findByUserIdAndTypeIn(userId, typeCodes, pageable);
            }
        } catch (Exception e) {
            log.error("根据类型获取用户积分交易记录失败", e);
            // 出错时返回所有交易记录
            return pointTransactionRepository.findByUserId(userId, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointTransaction> getRecentTransactions(Long userId, int limit) {
        log.debug("获取用户最近交易记录: userId={}, limit={}", userId, limit);
        Pageable pageable = Pageable.ofSize(limit);
        return pointTransactionRepository.findLatestByUserId(userId, pageable);
    }

    // ==================== 统计信息 ====================

    @Override
    @Transactional(readOnly = true)
    public Object getUserPointStatistics(Long userId) {
        log.debug("获取用户积分统计: userId={}", userId);

        PointAccount account = getPointAccount(userId).orElse(null);
        if (account == null) {
            return Map.of(
                    "totalPoints", BigDecimal.ZERO,
                    "availablePoints", BigDecimal.ZERO,
                    "frozenPoints", BigDecimal.ZERO,
                    "totalEarned", BigDecimal.ZERO,
                    "totalSpent", BigDecimal.ZERO,
                    "transactionCount", 0
            );
        }

        long transactionCount = pointTransactionRepository.countByUserId(userId);
        List<PointTransaction> recentTransactions = getRecentTransactions(userId, 5);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalPoints", account.getTotalPoints());
        statistics.put("availablePoints", account.getAvailablePoints());
        statistics.put("frozenPoints", account.getFrozenPoints());
        statistics.put("totalEarned", account.getTotalEarned());
        statistics.put("totalSpent", account.getTotalSpent());
        statistics.put("transactionCount", transactionCount);
        statistics.put("recentTransactions", recentTransactions);

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getSystemPointStatistics() {
        log.debug("获取系统积分统计");

        long totalUsers = pointAccountRepository.count();
        BigDecimal totalBalance = pointAccountRepository.sumTotalPoints();
        BigDecimal totalEarned = pointAccountRepository.sumTotalEarned();
        BigDecimal totalSpent = pointAccountRepository.sumTotalSpent();
        long transactionCount = pointTransactionRepository.count();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", totalUsers);
        statistics.put("totalBalance", totalBalance != null ? totalBalance : BigDecimal.ZERO);
        statistics.put("totalEarned", totalEarned != null ? totalEarned : BigDecimal.ZERO);
        statistics.put("totalSpent", totalSpent != null ? totalSpent : BigDecimal.ZERO);
        statistics.put("averageBalance", totalUsers > 0 ? (totalBalance != null ? totalBalance.divide(BigDecimal.valueOf(totalUsers), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO) : BigDecimal.ZERO);
        statistics.put("transactionCount", transactionCount);

        return statistics;
    }

    // ==================== 管理员功能 ====================

    @Override
    public PointTransaction adjustUserPoints(Long userId, BigDecimal amount, String reason, Long adminUserId) {
        log.info("管理员调整用户积分: userId={}, amount={}, reason={}, adminUserId={}", userId, amount, reason, adminUserId);

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("调整金额不能为0");
        }

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return rechargePoints(userId, amount, "管理员调整: " + reason);
        } else {
            return consumePoints(userId, amount.abs(), "管理员调整: " + reason, null);
        }
    }

    @Override
    public List<PointTransaction> batchRewardPoints(List<Long> userIds, BigDecimal amount, String reason, Long adminUserId) {
        log.info("批量发放积分奖励: userIds={}, amount={}, reason={}, adminUserId={}", userIds.size(), amount, reason, adminUserId);

        return userIds.stream()
                .map(userId -> rechargePoints(userId, amount, "批量奖励: " + reason))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointAccount> getAllPointAccounts(Pageable pageable) {
        log.debug("获取所有用户积分账户");
        return pointAccountRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointAccount> findInconsistentAccounts() {
        log.debug("查找积分不一致的账户");
        return pointAccountRepository.findInconsistentAccounts();
    }
}
