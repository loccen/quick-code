package com.quickcode.controller;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.common.response.PageResponse;
import com.quickcode.entity.PointAccount;
import com.quickcode.entity.PointTransaction;
import com.quickcode.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 积分管理控制器
 * 提供积分账户管理、交易记录、充值转账等功能的API端点
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PointController extends BaseController {

    private final PointService pointService;

    // ==================== 积分账户管理 ====================

    /**
     * 获取当前用户积分账户信息
     */
    @GetMapping("/account")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PointAccount> getPointAccount() {
        log.info("获取用户积分账户信息");

        try {
            Long userId = getCurrentUserId();
            PointAccount account = pointService.getOrCreatePointAccount(userId);
            return success(account);
        } catch (Exception e) {
            log.error("获取积分账户信息失败", e);
            return error("获取积分账户信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查积分余额是否足够
     */
    @GetMapping("/check-balance")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Boolean> checkBalance(@RequestParam BigDecimal amount) {
        log.info("检查积分余额: amount={}", amount);

        try {
            Long userId = getCurrentUserId();
            boolean sufficient = pointService.checkBalance(userId, amount);
            return success(sufficient);
        } catch (Exception e) {
            log.error("检查积分余额失败", e);
            return error("检查积分余额失败: " + e.getMessage());
        }
    }

    // ==================== 积分交易 ====================

    /**
     * 积分充值
     */
    @PostMapping("/recharge")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PointTransaction> rechargePoints(@RequestParam BigDecimal amount,
                                                       @RequestParam(required = false) String description) {
        log.info("积分充值: amount={}", amount);

        try {
            Long userId = getCurrentUserId();
            PointTransaction transaction = pointService.rechargePoints(userId, amount, description);
            return success(transaction, "积分充值成功");
        } catch (RuntimeException e) {
            log.warn("积分充值失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("积分充值失败", e);
            return error("积分充值失败: " + e.getMessage());
        }
    }

    /**
     * 积分转账
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PointTransaction> transferPoints(@RequestParam Long toUserId,
                                                       @RequestParam BigDecimal amount,
                                                       @RequestParam(required = false) String description) {
        log.info("积分转账: toUserId={}, amount={}", toUserId, amount);

        try {
            Long fromUserId = getCurrentUserId();
            PointTransaction transaction = pointService.transferPoints(fromUserId, toUserId, amount, description);
            return success(transaction, "积分转账成功");
        } catch (RuntimeException e) {
            log.warn("积分转账失败: {}", e.getMessage());
            return error(e.getMessage());
        } catch (Exception e) {
            log.error("积分转账失败", e);
            return error("积分转账失败: " + e.getMessage());
        }
    }

    /**
     * 冻结积分
     */
    @PostMapping("/freeze")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Boolean> freezePoints(@RequestParam BigDecimal amount,
                                           @RequestParam String reason) {
        log.info("冻结积分: amount={}, reason={}", amount, reason);

        try {
            Long userId = getCurrentUserId();
            boolean success = pointService.freezePoints(userId, amount, reason);
            return success(success, success ? "积分冻结成功" : "积分冻结失败");
        } catch (Exception e) {
            log.error("冻结积分失败", e);
            return error("冻结积分失败: " + e.getMessage());
        }
    }

    /**
     * 解冻积分
     */
    @PostMapping("/unfreeze")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Boolean> unfreezePoints(@RequestParam BigDecimal amount,
                                             @RequestParam String reason) {
        log.info("解冻积分: amount={}, reason={}", amount, reason);

        try {
            Long userId = getCurrentUserId();
            boolean success = pointService.unfreezePoints(userId, amount, reason);
            return success(success, success ? "积分解冻成功" : "积分解冻失败");
        } catch (Exception e) {
            log.error("解冻积分失败", e);
            return error("解冻积分失败: " + e.getMessage());
        }
    }

    // ==================== 交易记录查询 ====================

    /**
     * 获取用户积分交易记录
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResponse<PointTransaction>> getUserTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("获取用户积分交易记录: page={}, size={}, type={}", page, size, type);

        try {
            Long userId = getCurrentUserId();
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<PointTransaction> transactionPage;
            if (type != null && !type.isEmpty()) {
                transactionPage = pointService.getUserTransactionsByType(userId, type, pageable);
            } else {
                transactionPage = pointService.getUserTransactions(userId, pageable);
            }

            PageResponse<PointTransaction> pageResponse = PageResponse.<PointTransaction>builder()
                    .content(transactionPage.getContent())
                    .page(transactionPage.getNumber() + 1) // PageResponse页码从1开始
                    .size(transactionPage.getSize())
                    .total(transactionPage.getTotalElements())
                    .totalPages(transactionPage.getTotalPages())
                    .first(transactionPage.isFirst())
                    .last(transactionPage.isLast())
                    .build();

            return success(pageResponse);
        } catch (Exception e) {
            log.error("获取积分交易记录失败", e);
            return error("获取积分交易记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户最近交易记录
     */
    @GetMapping("/transactions/recent")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<PointTransaction>> getRecentTransactions(
            @RequestParam(defaultValue = "5") int limit) {
        
        log.info("获取用户最近交易记录: limit={}", limit);

        try {
            Long userId = getCurrentUserId();
            List<PointTransaction> transactions = pointService.getRecentTransactions(userId, limit);
            return success(transactions);
        } catch (Exception e) {
            log.error("获取最近交易记录失败", e);
            return error("获取最近交易记录失败: " + e.getMessage());
        }
    }

    // ==================== 统计信息 ====================

    /**
     * 获取用户积分统计
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Object> getUserPointStatistics() {
        log.info("获取用户积分统计");

        try {
            Long userId = getCurrentUserId();
            Object statistics = pointService.getUserPointStatistics(userId);
            return success(statistics);
        } catch (Exception e) {
            log.error("获取用户积分统计失败", e);
            return error("获取用户积分统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取积分使用规则
     */
    @GetMapping("/rules")
    public ApiResponse<Object> getPointRules() {
        log.info("获取积分使用规则");

        try {
            // 返回积分使用规则
            Object rules = java.util.Map.of(
                    "minRecharge", 1,
                    "maxRecharge", 10000,
                    "minTransfer", 1,
                    "maxTransfer", 1000,
                    "transferFeeRate", 0.01,
                    "exchangeRate", 1.0
            );
            return success(rules);
        } catch (Exception e) {
            log.error("获取积分使用规则失败", e);
            return error("获取积分使用规则失败: " + e.getMessage());
        }
    }

    /**
     * 获取积分兑换比例
     */
    @GetMapping("/exchange-rates")
    public ApiResponse<Object> getExchangeRates() {
        log.info("获取积分兑换比例");

        try {
            // 返回积分兑换比例
            Object rates = java.util.Map.of(
                    "pointToMoney", 0.01,  // 1积分 = 0.01元
                    "moneyToPoint", 100,   // 1元 = 100积分
                    "minExchange", 100,    // 最小兑换100积分
                    "maxExchange", 100000  // 最大兑换100000积分
            );
            return success(rates);
        } catch (Exception e) {
            log.error("获取积分兑换比例失败", e);
            return error("获取积分兑换比例失败: " + e.getMessage());
        }
    }

    /**
     * 计算购买项目所需积分
     */
    @GetMapping("/calculate-purchase/{projectId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Object> calculatePurchasePoints(@PathVariable Long projectId) {
        log.info("计算购买项目所需积分: projectId={}", projectId);

        try {
            // TODO: 实现根据项目ID计算所需积分的逻辑
            // 这里暂时返回模拟数据
            Object result = java.util.Map.of(
                    "requiredPoints", 1000,
                    "projectPrice", 10.0,
                    "exchangeRate", 100
            );
            return success(result);
        } catch (Exception e) {
            log.error("计算购买积分失败", e);
            return error("计算购买积分失败: " + e.getMessage());
        }
    }
}
