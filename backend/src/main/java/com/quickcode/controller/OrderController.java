package com.quickcode.controller;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.common.response.PageResponse;
import com.quickcode.dto.order.OrderCreateRequest;
import com.quickcode.dto.order.OrderDTO;
import com.quickcode.dto.order.OrderSearchRequest;
import com.quickcode.dto.order.PaymentRequest;
import com.quickcode.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单管理控制器
 * 提供订单管理功能的API端点
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class OrderController extends BaseController {

    private final OrderService orderService;

    // ==================== 订单创建和管理 ====================

    /**
     * 创建订单
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<OrderDTO> createOrder(@Valid @RequestBody OrderCreateRequest request,
                                           Authentication authentication) {
        log.info("创建订单: projectId={}, user={}", request.getProjectId(), authentication.getName());

        try {
            Long userId = getCurrentUserId();
            OrderDTO order = orderService.createOrder(request, userId);
            return success(order);
        } catch (Exception e) {
            log.error("创建订单失败: projectId={}", request.getProjectId(), e);
            return error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/{orderNo}/pay")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<OrderService.PaymentResult> payOrder(@PathVariable String orderNo,
                                                           @Valid @RequestBody PaymentRequest request,
                                                           Authentication authentication) {
        log.info("支付订单: orderNo={}, user={}", orderNo, authentication.getName());

        try {
            Long userId = getCurrentUserId();
            OrderService.PaymentResult result = orderService.payOrder(orderNo, request, userId);
            
            if (result.isSuccess()) {
                return success(result);
            } else {
                return error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("支付订单失败: orderNo={}", orderNo, e);
            return error("支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderNo}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> cancelOrder(@PathVariable String orderNo,
                                       @RequestParam(required = false) String reason,
                                       Authentication authentication) {
        log.info("取消订单: orderNo={}, user={}", orderNo, authentication.getName());

        try {
            Long userId = getCurrentUserId();
            boolean success = orderService.cancelOrder(orderNo, userId, reason);
            
            if (success) {
                return success();
            } else {
                return error("取消订单失败");
            }
        } catch (Exception e) {
            log.error("取消订单失败: orderNo={}", orderNo, e);
            return error("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 完成订单
     */
    @PostMapping("/{orderNo}/complete")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> completeOrder(@PathVariable String orderNo,
                                         Authentication authentication) {
        log.info("完成订单: orderNo={}, user={}", orderNo, authentication.getName());

        try {
            Long userId = getCurrentUserId();
            boolean success = orderService.completeOrder(orderNo, userId);
            
            if (success) {
                return success();
            } else {
                return error("完成订单失败");
            }
        } catch (Exception e) {
            log.error("完成订单失败: orderNo={}", orderNo, e);
            return error("完成订单失败: " + e.getMessage());
        }
    }

    /**
     * 申请退款
     */
    @PostMapping("/{orderNo}/refund")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> requestRefund(@PathVariable String orderNo,
                                         @RequestParam(required = false) String reason,
                                         Authentication authentication) {
        log.info("申请退款: orderNo={}, user={}", orderNo, authentication.getName());

        try {
            Long userId = getCurrentUserId();
            boolean success = orderService.requestRefund(orderNo, userId, reason);
            
            if (success) {
                return success();
            } else {
                return error("申请退款失败");
            }
        } catch (Exception e) {
            log.error("申请退款失败: orderNo={}", orderNo, e);
            return error("申请退款失败: " + e.getMessage());
        }
    }

    // ==================== 订单查询 ====================

    /**
     * 根据订单号获取订单详情
     */
    @GetMapping("/{orderNo}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<OrderDTO> getOrderByOrderNo(@PathVariable String orderNo,
                                                  Authentication authentication) {
        log.info("获取订单详情: orderNo={}, user={}", orderNo, authentication.getName());

        try {
            Long userId = getCurrentUserId();

            // 验证用户权限
            if (!orderService.canUserAccessOrder(orderNo, userId)) {
                return error("无权限访问此订单");
            }
            
            OrderDTO order = orderService.getOrderByOrderNo(orderNo);
            return success(order);
        } catch (Exception e) {
            log.error("获取订单详情失败: orderNo={}", orderNo, e);
            return error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户购买订单列表
     */
    @GetMapping("/purchases")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResponse<OrderDTO>> getUserPurchaseOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            Authentication authentication) {
        
        log.info("获取用户购买订单列表: user={}, page={}, size={}", authentication.getName(), page, size);

        try {
            Long userId = getCurrentUserId();

            Sort.Direction direction = "ASC".equals(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            PageResponse<OrderDTO> orders = orderService.getUserPurchaseOrders(userId, pageable);
            return success(orders);
        } catch (Exception e) {
            log.error("获取用户购买订单列表失败: user={}", authentication.getName(), e);
            return error("获取购买订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户销售订单列表
     */
    @GetMapping("/sales")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResponse<OrderDTO>> getUserSalesOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            Authentication authentication) {
        
        log.info("获取用户销售订单列表: user={}, page={}, size={}", authentication.getName(), page, size);

        try {
            Long userId = getCurrentUserId();

            Sort.Direction direction = "ASC".equals(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            PageResponse<OrderDTO> orders = orderService.getUserSalesOrders(userId, pageable);
            return success(orders);
        } catch (Exception e) {
            log.error("获取用户销售订单列表失败: user={}", authentication.getName(), e);
            return error("获取销售订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户最近订单
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<OrderDTO>> getUserRecentOrders(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        
        log.info("获取用户最近订单: user={}, limit={}", authentication.getName(), limit);

        try {
            Long userId = getCurrentUserId();
            List<OrderDTO> orders = orderService.getUserRecentOrders(userId, limit);
            return success(orders);
        } catch (Exception e) {
            log.error("获取用户最近订单失败: user={}", authentication.getName(), e);
            return error("获取最近订单失败: " + e.getMessage());
        }
    }

    /**
     * 搜索订单
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PageResponse<OrderDTO>> searchOrders(@Valid @RequestBody OrderSearchRequest request,
                                                          Authentication authentication) {
        log.info("搜索订单: user={}, request={}", authentication.getName(), request);

        try {
            Long userId = getCurrentUserId();

            // 限制用户只能搜索自己的订单
            if (request.getBuyerId() == null && request.getSellerId() == null) {
                request.setBuyerId(userId);
            } else if (request.getBuyerId() != null && !request.getBuyerId().equals(userId) &&
                      request.getSellerId() != null && !request.getSellerId().equals(userId)) {
                return error("只能搜索自己的订单");
            }
            
            PageResponse<OrderDTO> orders = orderService.searchOrders(request);
            return success(orders);
        } catch (Exception e) {
            log.error("搜索订单失败: user={}", authentication.getName(), e);
            return error("搜索订单失败: " + e.getMessage());
        }
    }

    // ==================== 权限验证 ====================

    /**
     * 检查用户是否可以购买项目
     */
    @GetMapping("/check-purchase/{projectId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Boolean> canUserPurchaseProject(@PathVariable Long projectId,
                                                     Authentication authentication) {
        log.info("检查购买权限: projectId={}, user={}", projectId, authentication.getName());

        try {
            Long userId = getCurrentUserId();
            boolean canPurchase = orderService.canUserPurchaseProject(projectId, userId);
            return success(canPurchase);
        } catch (Exception e) {
            log.error("检查购买权限失败: projectId={}", projectId, e);
            return error("检查购买权限失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否已购买项目
     */
    @GetMapping("/check-purchased/{projectId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Boolean> hasUserPurchasedProject(@PathVariable Long projectId,
                                                      Authentication authentication) {
        log.info("检查购买状态: projectId={}, user={}", projectId, authentication.getName());

        try {
            Long userId = getCurrentUserId();
            boolean hasPurchased = orderService.hasUserPurchasedProject(projectId, userId);
            return success(hasPurchased);
        } catch (Exception e) {
            log.error("检查购买状态失败: projectId={}", projectId, e);
            return error("检查购买状态失败: " + e.getMessage());
        }
    }

    // ==================== 统计分析 ====================

    /**
     * 获取用户购买统计
     */
    @GetMapping("/statistics/purchases")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<PurchaseStatistics> getUserPurchaseStatistics(Authentication authentication) {
        log.info("获取用户购买统计: user={}", authentication.getName());

        try {
            Long userId = getCurrentUserId();

            Long purchaseCount = orderService.countUserPurchases(userId);
            java.math.BigDecimal purchaseAmount = orderService.sumUserPurchaseAmount(userId);

            PurchaseStatistics statistics = new PurchaseStatistics(purchaseCount, purchaseAmount);
            return success(statistics);
        } catch (Exception e) {
            log.error("获取用户购买统计失败: user={}", authentication.getName(), e);
            return error("获取购买统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户销售统计
     */
    @GetMapping("/statistics/sales")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<SalesStatistics> getUserSalesStatistics(Authentication authentication) {
        log.info("获取用户销售统计: user={}", authentication.getName());

        try {
            Long userId = getCurrentUserId();

            Long salesCount = orderService.countUserSales(userId);
            java.math.BigDecimal salesAmount = orderService.sumUserSalesAmount(userId);

            SalesStatistics statistics = new SalesStatistics(salesCount, salesAmount);
            return success(statistics);
        } catch (Exception e) {
            log.error("获取用户销售统计失败: user={}", authentication.getName(), e);
            return error("获取销售统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目销售统计
     */
    @GetMapping("/statistics/project/{projectId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<ProjectSalesStatistics> getProjectSalesStatistics(@PathVariable Long projectId,
                                                                        Authentication authentication) {
        log.info("获取项目销售统计: projectId={}, user={}", projectId, authentication.getName());

        try {
            // TODO: 验证用户是否为项目所有者

            Long salesCount = orderService.countProjectSales(projectId);
            java.math.BigDecimal salesAmount = orderService.sumProjectSalesAmount(projectId);

            ProjectSalesStatistics statistics = new ProjectSalesStatistics(projectId, salesCount, salesAmount);
            return success(statistics);
        } catch (Exception e) {
            log.error("获取项目销售统计失败: projectId={}", projectId, e);
            return error("获取项目销售统计失败: " + e.getMessage());
        }
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员搜索所有订单
     */
    @PostMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<OrderDTO>> adminSearchOrders(@Valid @RequestBody OrderSearchRequest request) {
        log.info("管理员搜索订单: request={}", request);

        try {
            PageResponse<OrderDTO> orders = orderService.searchOrders(request);
            return success(orders);
        } catch (Exception e) {
            log.error("管理员搜索订单失败", e);
            return error("搜索订单失败: " + e.getMessage());
        }
    }

    /**
     * 管理员处理超时订单
     */
    @PostMapping("/admin/handle-timeout")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Integer> handleTimeoutOrders(@RequestParam(defaultValue = "30") int timeoutMinutes) {
        log.info("管理员处理超时订单: timeoutMinutes={}", timeoutMinutes);

        try {
            int handledCount = orderService.handleTimeoutOrders(timeoutMinutes);
            return success(handledCount);
        } catch (Exception e) {
            log.error("处理超时订单失败", e);
            return error("处理超时订单失败: " + e.getMessage());
        }
    }

    /**
     * 管理员自动完成订单
     */
    @PostMapping("/admin/auto-complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Integer> autoCompleteOrders(@RequestParam(defaultValue = "7") int autoCompleteDays) {
        log.info("管理员自动完成订单: autoCompleteDays={}", autoCompleteDays);

        try {
            int completedCount = orderService.autoCompleteOrders(autoCompleteDays);
            return success(completedCount);
        } catch (Exception e) {
            log.error("自动完成订单失败", e);
            return error("自动完成订单失败: " + e.getMessage());
        }
    }

    // ==================== 内部类 ====================

    /**
     * 购买统计
     */
    public static class PurchaseStatistics {
        private final Long purchaseCount;
        private final java.math.BigDecimal purchaseAmount;

        public PurchaseStatistics(Long purchaseCount, java.math.BigDecimal purchaseAmount) {
            this.purchaseCount = purchaseCount;
            this.purchaseAmount = purchaseAmount;
        }

        public Long getPurchaseCount() { return purchaseCount; }
        public java.math.BigDecimal getPurchaseAmount() { return purchaseAmount; }
    }

    /**
     * 销售统计
     */
    public static class SalesStatistics {
        private final Long salesCount;
        private final java.math.BigDecimal salesAmount;

        public SalesStatistics(Long salesCount, java.math.BigDecimal salesAmount) {
            this.salesCount = salesCount;
            this.salesAmount = salesAmount;
        }

        public Long getSalesCount() { return salesCount; }
        public java.math.BigDecimal getSalesAmount() { return salesAmount; }
    }

    /**
     * 项目销售统计
     */
    public static class ProjectSalesStatistics {
        private final Long projectId;
        private final Long salesCount;
        private final java.math.BigDecimal salesAmount;

        public ProjectSalesStatistics(Long projectId, Long salesCount, java.math.BigDecimal salesAmount) {
            this.projectId = projectId;
            this.salesCount = salesCount;
            this.salesAmount = salesAmount;
        }

        public Long getProjectId() { return projectId; }
        public Long getSalesCount() { return salesCount; }
        public java.math.BigDecimal getSalesAmount() { return salesAmount; }
    }
}
