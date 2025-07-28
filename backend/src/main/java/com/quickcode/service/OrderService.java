package com.quickcode.service;

import com.quickcode.common.response.PageResponse;
import com.quickcode.dto.order.OrderCreateRequest;
import com.quickcode.dto.order.OrderDTO;
import com.quickcode.dto.order.OrderSearchRequest;
import com.quickcode.dto.order.PaymentRequest;
import com.quickcode.entity.Order;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务接口
 * 提供订单管理相关的业务逻辑
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface OrderService extends BaseService<Order, Long> {

    // ==================== 订单创建和管理 ====================

    /**
     * 创建订单
     * 
     * @param request 订单创建请求
     * @param buyerId 买家用户ID
     * @return 订单DTO
     */
    OrderDTO createOrder(OrderCreateRequest request, Long buyerId);

    /**
     * 支付订单
     * 
     * @param orderNo 订单号
     * @param request 支付请求
     * @param userId 用户ID
     * @return 支付结果
     */
    PaymentResult payOrder(String orderNo, PaymentRequest request, Long userId);

    /**
     * 取消订单
     * 
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 是否成功
     */
    boolean cancelOrder(String orderNo, Long userId, String reason);

    /**
     * 完成订单
     * 
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean completeOrder(String orderNo, Long userId);

    /**
     * 申请退款
     * 
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param reason 退款原因
     * @return 是否成功
     */
    boolean requestRefund(String orderNo, Long userId, String reason);

    // ==================== 订单查询 ====================

    /**
     * 根据订单号获取订单
     * 
     * @param orderNo 订单号
     * @return 订单DTO
     */
    OrderDTO getOrderByOrderNo(String orderNo);

    /**
     * 获取用户购买订单列表
     * 
     * @param buyerId 买家用户ID
     * @param pageable 分页参数
     * @return 订单列表
     */
    PageResponse<OrderDTO> getUserPurchaseOrders(Long buyerId, Pageable pageable);

    /**
     * 获取用户销售订单列表
     * 
     * @param sellerId 卖家用户ID
     * @param pageable 分页参数
     * @return 订单列表
     */
    PageResponse<OrderDTO> getUserSalesOrders(Long sellerId, Pageable pageable);

    /**
     * 获取项目订单列表
     * 
     * @param projectId 项目ID
     * @param pageable 分页参数
     * @return 订单列表
     */
    PageResponse<OrderDTO> getProjectOrders(Long projectId, Pageable pageable);

    /**
     * 搜索订单
     * 
     * @param request 搜索请求
     * @return 订单列表
     */
    PageResponse<OrderDTO> searchOrders(OrderSearchRequest request);

    /**
     * 获取用户最近订单
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 订单列表
     */
    List<OrderDTO> getUserRecentOrders(Long userId, int limit);

    // ==================== 权限验证 ====================

    /**
     * 检查用户是否可以购买项目
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否可以购买
     */
    boolean canUserPurchaseProject(Long projectId, Long userId);

    /**
     * 检查用户是否已购买项目
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否已购买
     */
    boolean hasUserPurchasedProject(Long projectId, Long userId);

    /**
     * 检查用户是否可以访问订单
     * 
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 是否可以访问
     */
    boolean canUserAccessOrder(String orderNo, Long userId);

    // ==================== 统计分析 ====================

    /**
     * 统计用户购买次数
     * 
     * @param userId 用户ID
     * @return 购买次数
     */
    Long countUserPurchases(Long userId);

    /**
     * 统计用户销售次数
     * 
     * @param userId 用户ID
     * @return 销售次数
     */
    Long countUserSales(Long userId);

    /**
     * 统计用户购买总金额
     * 
     * @param userId 用户ID
     * @return 购买总金额
     */
    BigDecimal sumUserPurchaseAmount(Long userId);

    /**
     * 统计用户销售总金额
     * 
     * @param userId 用户ID
     * @return 销售总金额
     */
    BigDecimal sumUserSalesAmount(Long userId);

    /**
     * 统计项目销售次数
     * 
     * @param projectId 项目ID
     * @return 销售次数
     */
    Long countProjectSales(Long projectId);

    /**
     * 统计项目销售总金额
     * 
     * @param projectId 项目ID
     * @return 销售总金额
     */
    BigDecimal sumProjectSalesAmount(Long projectId);

    // ==================== 系统管理 ====================

    /**
     * 处理超时订单
     * 
     * @param timeoutMinutes 超时分钟数
     * @return 处理的订单数量
     */
    int handleTimeoutOrders(int timeoutMinutes);

    /**
     * 自动完成订单
     * 
     * @param autoCompleteDays 自动完成天数
     * @return 处理的订单数量
     */
    int autoCompleteOrders(int autoCompleteDays);

    /**
     * 生成订单号
     * 
     * @return 订单号
     */
    String generateOrderNo();

    // ==================== 内部类 ====================

    /**
     * 支付结果
     */
    class PaymentResult {
        private final boolean success;
        private final String message;
        private final String orderNo;
        private final BigDecimal paidAmount;

        public PaymentResult(boolean success, String message, String orderNo, BigDecimal paidAmount) {
            this.success = success;
            this.message = message;
            this.orderNo = orderNo;
            this.paidAmount = paidAmount;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getOrderNo() { return orderNo; }
        public BigDecimal getPaidAmount() { return paidAmount; }
    }
}
