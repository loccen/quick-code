package com.quickcode.service.impl;

import com.quickcode.common.response.PageResponse;
import com.quickcode.dto.order.OrderCreateRequest;
import com.quickcode.dto.order.OrderDTO;
import com.quickcode.dto.order.OrderSearchRequest;
import com.quickcode.dto.order.PaymentRequest;
import com.quickcode.dto.order.UserOrderStats;
import com.quickcode.entity.Order;
import com.quickcode.entity.PointAccount;
import com.quickcode.entity.Project;
import com.quickcode.entity.User;
import com.quickcode.repository.OrderRepository;
import com.quickcode.repository.PointAccountRepository;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.repository.UserRepository;
import com.quickcode.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PointAccountRepository pointAccountRepository;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderCreateRequest request, Long buyerId) {
        log.info("创建订单: projectId={}, buyerId={}", request.getProjectId(), buyerId);

        // 验证和标准化请求数据
        request.validate();
        request.normalize();

        // 验证买家用户
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("买家用户不存在: " + buyerId));

        // 验证项目
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("项目不存在: " + request.getProjectId()));

        // 检查项目是否已发布
        if (!project.isPublished()) {
            throw new RuntimeException("项目尚未发布，无法购买");
        }

        // 检查是否可以购买
        if (!canUserPurchaseProject(request.getProjectId(), buyerId)) {
            throw new RuntimeException("无法购买此项目");
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建订单
        Order order = Order.builder()
                .orderNo(orderNo)
                .buyerId(buyerId)
                .sellerId(project.getUserId())
                .projectId(request.getProjectId())
                .amount(project.getPrice())
                .status(Order.OrderStatus.PENDING_PAYMENT.getCode())
                .remark(request.getRemark())
                .build();

        // 保存订单
        order = orderRepository.save(order);

        log.info("订单创建成功: orderNo={}, amount={}", orderNo, project.getPrice());

        return OrderDTO.fromOrderSimple(order);
    }

    @Override
    @Transactional
    public PaymentResult payOrder(String orderNo, PaymentRequest request, Long userId) {
        log.info("支付订单: orderNo={}, userId={}, paymentMethod={}", orderNo, userId, request.getPaymentMethod());

        // 验证和标准化请求数据
        request.validate();
        request.normalize();

        // 查找订单
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderNo));

        // 验证用户权限
        if (!canUserAccessOrder(orderNo, userId)) {
            throw new RuntimeException("无权限访问此订单");
        }

        // 检查订单状态
        if (!order.canPay()) {
            throw new RuntimeException("订单状态不允许支付: " + order.getStatusDescription());
        }

        // 验证支付金额
        BigDecimal totalPaymentAmount = request.getTotalAmount();
        if (totalPaymentAmount.compareTo(order.getAmount()) != 0) {
            throw new RuntimeException("支付金额与订单金额不匹配");
        }

        try {
            // 执行支付
            executePayment(order, request, userId);

            // 更新订单状态
            order.markAsPaid(request.getPaymentMethod());
            orderRepository.save(order);

            log.info("订单支付成功: orderNo={}, amount={}", orderNo, order.getAmount());

            return new PaymentResult(true, "支付成功", orderNo, order.getAmount());

        } catch (Exception e) {
            log.error("订单支付失败: orderNo={}", orderNo, e);
            return new PaymentResult(false, "支付失败: " + e.getMessage(), orderNo, BigDecimal.ZERO);
        }
    }

    @Override
    @Transactional
    public boolean cancelOrder(String orderNo, Long userId, String reason) {
        log.info("取消订单: orderNo={}, userId={}, reason={}", orderNo, userId, reason);

        try {
            // 查找订单
            Order order = orderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new RuntimeException("订单不存在: " + orderNo));

            // 验证用户权限
            if (!canUserAccessOrder(orderNo, userId)) {
                throw new RuntimeException("无权限访问此订单");
            }

            // 检查订单状态
            if (!order.canCancel()) {
                throw new RuntimeException("订单状态不允许取消: " + order.getStatusDescription());
            }

            // 如果订单已支付，需要退款
            if (order.isPaid()) {
                refundOrder(order, reason);
            }

            // 更新订单状态
            order.markAsCancelled(reason);
            orderRepository.save(order);

            log.info("订单取消成功: orderNo={}", orderNo);
            return true;

        } catch (Exception e) {
            log.error("订单取消失败: orderNo={}", orderNo, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean completeOrder(String orderNo, Long userId) {
        log.info("完成订单: orderNo={}, userId={}", orderNo, userId);

        try {
            // 查找订单
            Order order = orderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new RuntimeException("订单不存在: " + orderNo));

            // 验证用户权限（买家或卖家都可以完成订单）
            if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
                throw new RuntimeException("无权限操作此订单");
            }

            // 检查订单状态
            if (!Order.OrderStatus.PAID.getCode().equals(order.getStatus())) {
                throw new RuntimeException("只有已支付的订单才能完成");
            }

            // 更新订单状态
            order.markAsCompleted();
            orderRepository.save(order);

            log.info("订单完成成功: orderNo={}", orderNo);
            return true;

        } catch (Exception e) {
            log.error("订单完成失败: orderNo={}", orderNo, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean requestRefund(String orderNo, Long userId, String reason) {
        log.info("申请退款: orderNo={}, userId={}, reason={}", orderNo, userId, reason);

        try {
            // 查找订单
            Order order = orderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new RuntimeException("订单不存在: " + orderNo));

            // 验证用户权限（只有买家可以申请退款）
            if (!order.getBuyerId().equals(userId)) {
                throw new RuntimeException("只有买家可以申请退款");
            }

            // 检查订单状态
            if (!order.canRefund()) {
                throw new RuntimeException("订单状态不允许退款: " + order.getStatusDescription());
            }

            // 执行退款
            refundOrder(order, reason);

            // 更新订单状态
            order.markAsRefunded(order.getAmount());
            orderRepository.save(order);

            log.info("退款申请成功: orderNo={}", orderNo);
            return true;

        } catch (Exception e) {
            log.error("退款申请失败: orderNo={}", orderNo, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderByOrderNo(String orderNo) {
        log.debug("根据订单号获取订单: orderNo={}", orderNo);

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderNo));

        return OrderDTO.fromOrder(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDTO> getUserPurchaseOrders(Long buyerId, Pageable pageable) {
        log.debug("获取用户购买订单列表: buyerId={}", buyerId);

        Page<Order> orderPage = orderRepository.findByBuyerId(buyerId, pageable);
        return PageResponse.fromPage(orderPage.map(OrderDTO::fromOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDTO> getUserSalesOrders(Long sellerId, Pageable pageable) {
        log.debug("获取用户销售订单列表: sellerId={}", sellerId);

        Page<Order> orderPage = orderRepository.findBySellerId(sellerId, pageable);
        return PageResponse.fromPage(orderPage.map(OrderDTO::fromOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDTO> getProjectOrders(Long projectId, Pageable pageable) {
        log.debug("获取项目订单列表: projectId={}", projectId);

        Page<Order> orderPage = orderRepository.findByProjectId(projectId, pageable);
        return PageResponse.fromPage(orderPage.map(OrderDTO::fromOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getUserRecentOrders(Long userId, int limit) {
        log.debug("获取用户最近订单: userId={}, limit={}", userId, limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Order> orders = orderRepository.findUserRecentOrders(userId, pageable);
        
        return orders.stream()
                .map(OrderDTO::fromOrder)
                .collect(Collectors.toList());
    }

    // ==================== 权限验证方法 ====================

    @Override
    @Transactional(readOnly = true)
    public boolean canUserPurchaseProject(Long projectId, Long userId) {
        log.debug("检查用户是否可以购买项目: projectId={}, userId={}", projectId, userId);

        // 检查项目是否存在
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return false;
        }

        // 项目必须已发布
        if (!project.isPublished()) {
            return false;
        }

        // 项目作者不能购买自己的项目
        if (project.getUserId().equals(userId)) {
            return false;
        }

        // 检查用户是否已经购买过
        if (hasUserPurchasedProject(projectId, userId)) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserPurchasedProject(Long projectId, Long userId) {
        return orderRepository.hasUserPurchasedProject(userId, projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserAccessOrder(String orderNo, Long userId) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return false;
        }

        // 买家或卖家都可以访问订单
        return order.getBuyerId().equals(userId) || order.getSellerId().equals(userId);
    }

    // ==================== 统计分析方法 ====================

    @Override
    @Transactional(readOnly = true)
    public Long countUserPurchases(Long userId) {
        return orderRepository.countUserPurchases(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUserSales(Long userId) {
        return orderRepository.countUserSales(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumUserPurchaseAmount(Long userId) {
        return orderRepository.sumUserPurchaseAmount(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumUserSalesAmount(Long userId) {
        return orderRepository.sumUserSalesAmount(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countProjectSales(Long projectId) {
        return orderRepository.countProjectSales(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumProjectSalesAmount(Long projectId) {
        return orderRepository.sumProjectSalesAmount(projectId);
    }

    // ==================== 系统管理方法 ====================

    @Override
    @Transactional
    public int handleTimeoutOrders(int timeoutMinutes) {
        log.info("处理超时订单: timeoutMinutes={}", timeoutMinutes);

        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        LocalDateTime cancellationTime = LocalDateTime.now();

        int cancelledCount = orderRepository.cancelTimeoutOrders(timeoutTime, cancellationTime);

        log.info("处理超时订单完成: 取消了{}个订单", cancelledCount);
        return cancelledCount;
    }

    @Override
    @Transactional
    public int autoCompleteOrders(int autoCompleteDays) {
        log.info("自动完成订单: autoCompleteDays={}", autoCompleteDays);

        LocalDateTime autoCompleteTime = LocalDateTime.now().minusDays(autoCompleteDays);
        List<Order> ordersToComplete = orderRepository.findOrdersForAutoCompletion(autoCompleteTime);

        int completedCount = 0;
        for (Order order : ordersToComplete) {
            try {
                order.markAsCompleted();
                orderRepository.save(order);
                completedCount++;
            } catch (Exception e) {
                log.warn("自动完成订单失败: orderNo={}", order.getOrderNo(), e);
            }
        }

        log.info("自动完成订单完成: 完成了{}个订单", completedCount);
        return completedCount;
    }

    @Override
    public String generateOrderNo() {
        // 生成格式：yyyyMMddHHmmss + 6位随机数
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = ThreadLocalRandom.current().nextInt(100000, 999999);
        String orderNo = timestamp + randomNum;

        // 确保订单号唯一
        while (orderRepository.existsByOrderNo(orderNo)) {
            randomNum = ThreadLocalRandom.current().nextInt(100000, 999999);
            orderNo = timestamp + randomNum;
        }

        return orderNo;
    }

    // ==================== 搜索方法 ====================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDTO> searchOrders(OrderSearchRequest request) {
        log.debug("搜索订单: {}", request);

        // 验证和标准化请求数据
        request.setDefaults();
        request.normalize();
        request.validate();

        // 构建分页参数
        Pageable pageable = request.toPageable();

        // 执行搜索（这里简化实现，实际项目中应该使用Specification或自定义查询）
        Page<Order> orderPage;

        if (request.getOrderNo() != null) {
            // 按订单号精确查找
            Optional<Order> order = orderRepository.findByOrderNo(request.getOrderNo());
            if (order.isPresent()) {
                orderPage = new org.springframework.data.domain.PageImpl<>(List.of(order.get()), pageable, 1);
            } else {
                orderPage = Page.empty(pageable);
            }
        } else if (request.getBuyerId() != null && request.getStatus() != null) {
            // 按买家ID和状态查找
            orderPage = orderRepository.findByBuyerIdAndStatus(request.getBuyerId(), request.getStatus(), pageable);
        } else if (request.getBuyerId() != null) {
            // 按买家ID查找
            orderPage = orderRepository.findByBuyerId(request.getBuyerId(), pageable);
        } else if (request.getSellerId() != null && request.getStatus() != null) {
            // 按卖家ID和状态查找
            orderPage = orderRepository.findBySellerIdAndStatus(request.getSellerId(), request.getStatus(), pageable);
        } else if (request.getSellerId() != null) {
            // 按卖家ID查找
            orderPage = orderRepository.findBySellerId(request.getSellerId(), pageable);
        } else if (request.getProjectId() != null) {
            // 按项目ID查找
            orderPage = orderRepository.findByProjectId(request.getProjectId(), pageable);
        } else if (request.getStatus() != null) {
            // 按状态查找
            orderPage = orderRepository.findByStatus(request.getStatus(), pageable);
        } else if (request.getStartTime() != null && request.getEndTime() != null) {
            // 按时间范围查找
            orderPage = orderRepository.findByCreatedTimeBetween(request.getStartTime(), request.getEndTime(), pageable);
        } else if (request.getMinAmount() != null && request.getMaxAmount() != null) {
            // 按金额范围查找
            orderPage = orderRepository.findByAmountBetween(request.getMinAmount(), request.getMaxAmount(), pageable);
        } else {
            // 查找所有订单
            orderPage = orderRepository.findAll(pageable);
        }

        return PageResponse.fromPage(orderPage.map(OrderDTO::fromOrder));
    }

    // ==================== BaseService接口实现 ====================

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public List<Order> saveAll(List<Order> entities) {
        return orderRepository.saveAll(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(Order entity) {
        orderRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteAll(List<Order> entities) {
        orderRepository.deleteAll(entities);
    }

    @Override
    @Transactional
    public void deleteAll() {
        orderRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return orderRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return orderRepository.count();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 执行支付
     */
    private void executePayment(Order order, PaymentRequest request, Long userId) {
        String paymentMethod = request.getPaymentMethod();

        switch (paymentMethod) {
            case "POINTS":
                executePointsPayment(order, request.getPointsAmount(), userId);
                break;
            case "BALANCE":
                executeBalancePayment(order, request.getBalanceAmount(), userId);
                break;
            case "MIXED":
                if (request.usesPoints()) {
                    executePointsPayment(order, request.getPointsAmount(), userId);
                }
                if (request.usesBalance()) {
                    executeBalancePayment(order, request.getBalanceAmount(), userId);
                }
                break;
            default:
                throw new RuntimeException("不支持的支付方式: " + paymentMethod);
        }
    }

    /**
     * 执行积分支付
     */
    private void executePointsPayment(Order order, BigDecimal amount, Long userId) {
        log.debug("执行积分支付: orderNo={}, amount={}, userId={}", order.getOrderNo(), amount, userId);

        // 获取用户积分账户
        PointAccount pointAccount = pointAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户积分账户不存在"));

        // 检查积分余额
        if (!pointAccount.hasEnoughPoints(amount)) {
            throw new RuntimeException("积分余额不足，当前可用积分: " + pointAccount.getAvailablePoints());
        }

        // 扣减积分
        pointAccount.deductPoints(amount);
        pointAccountRepository.save(pointAccount);

        // 给卖家增加积分（可选，根据业务需求）
        addPointsToSeller(order.getSellerId(), amount);

        log.info("积分支付成功: orderNo={}, amount={}", order.getOrderNo(), amount);
    }

    /**
     * 执行余额支付（暂时简化实现）
     */
    private void executeBalancePayment(Order order, BigDecimal amount, Long userId) {
        log.debug("执行余额支付: orderNo={}, amount={}, userId={}", order.getOrderNo(), amount, userId);

        // TODO: 实现余额支付逻辑
        // 这里需要实现用户余额账户的扣减逻辑

        log.info("余额支付成功: orderNo={}, amount={}", order.getOrderNo(), amount);
    }

    /**
     * 给卖家增加积分
     */
    private void addPointsToSeller(Long sellerId, BigDecimal amount) {
        try {
            PointAccount sellerAccount = pointAccountRepository.findByUserId(sellerId)
                    .orElseGet(() -> createPointAccount(sellerId));

            sellerAccount.addPoints(amount);
            pointAccountRepository.save(sellerAccount);

            log.debug("给卖家增加积分成功: sellerId={}, amount={}", sellerId, amount);
        } catch (Exception e) {
            log.warn("给卖家增加积分失败: sellerId={}, amount={}", sellerId, amount, e);
        }
    }

    /**
     * 创建积分账户
     */
    private PointAccount createPointAccount(Long userId) {
        PointAccount account = PointAccount.builder()
                .userId(userId)
                .build();
        return pointAccountRepository.save(account);
    }

    /**
     * 退款处理
     */
    private void refundOrder(Order order, String reason) {
        log.info("处理订单退款: orderNo={}, reason={}", order.getOrderNo(), reason);

        if (order.getPaymentMethod() == null) {
            return; // 未支付的订单无需退款
        }

        try {
            String paymentMethod = order.getPaymentMethod();
            BigDecimal refundAmount = order.getAmount();

            switch (paymentMethod) {
                case "POINTS":
                    refundPoints(order.getBuyerId(), refundAmount);
                    deductPointsFromSeller(order.getSellerId(), refundAmount);
                    break;
                case "BALANCE":
                    refundBalance(order.getBuyerId(), refundAmount);
                    break;
                case "MIXED":
                    // 混合支付的退款需要按比例退还
                    refundMixedPayment(order, refundAmount);
                    break;
                default:
                    log.warn("未知的支付方式，无法退款: {}", paymentMethod);
            }

            log.info("订单退款成功: orderNo={}, amount={}", order.getOrderNo(), refundAmount);

        } catch (Exception e) {
            log.error("订单退款失败: orderNo={}", order.getOrderNo(), e);
            throw new RuntimeException("退款处理失败: " + e.getMessage());
        }
    }

    /**
     * 退还积分
     */
    private void refundPoints(Long userId, BigDecimal amount) {
        PointAccount pointAccount = pointAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户积分账户不存在"));

        pointAccount.addPoints(amount);
        pointAccountRepository.save(pointAccount);
    }

    /**
     * 从卖家扣减积分
     */
    private void deductPointsFromSeller(Long sellerId, BigDecimal amount) {
        PointAccount sellerAccount = pointAccountRepository.findByUserId(sellerId)
                .orElse(null);

        if (sellerAccount != null && sellerAccount.hasEnoughPoints(amount)) {
            sellerAccount.deductPoints(amount);
            pointAccountRepository.save(sellerAccount);
        }
    }

    /**
     * 退还余额
     */
    private void refundBalance(Long userId, BigDecimal amount) {
        // TODO: 实现余额退还逻辑
        log.info("退还余额: userId={}, amount={}", userId, amount);
    }

    /**
     * 混合支付退款
     */
    private void refundMixedPayment(Order order, BigDecimal totalAmount) {
        // TODO: 实现混合支付的退款逻辑
        // 需要记录原始支付的积分和余额比例
        log.info("混合支付退款: orderNo={}, amount={}", order.getOrderNo(), totalAmount);
    }

    @Override
    public UserOrderStats getUserOrderStatistics(Long userId) {
        log.debug("获取用户订单统计信息: userId={}", userId);

        try {
            // 统计订单总数
            long totalOrders = orderRepository.countByBuyerId(userId);

            // 统计订单总金额
            BigDecimal totalAmount = orderRepository.sumAmountByBuyerId(userId);
            if (totalAmount == null) {
                totalAmount = BigDecimal.ZERO;
            }

            // 统计各状态订单数量
            long pendingOrders = orderRepository.countByBuyerIdAndStatus(userId, "PENDING");
            long completedOrders = orderRepository.countByBuyerIdAndStatus(userId, "PAID");
            long cancelledOrders = orderRepository.countByBuyerIdAndStatus(userId, "CANCELLED");
            long refundedOrders = orderRepository.countByBuyerIdAndStatus(userId, "REFUNDED");

            // 统计本月订单数据
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);

            long monthlyOrders = orderRepository.countByBuyerIdAndCreatedTimeBetween(userId, monthStart, monthEnd);
            BigDecimal monthlyAmount = orderRepository.sumAmountByBuyerIdAndCreatedTimeBetween(userId, monthStart, monthEnd);
            if (monthlyAmount == null) {
                monthlyAmount = BigDecimal.ZERO;
            }

            // 获取最近订单时间
            LocalDateTime lastOrderTime = orderRepository.findTopByBuyerIdOrderByCreatedTimeDesc(userId)
                    .map(Order::getCreatedTime)
                    .orElse(null);

            // 获取最大和最小订单金额
            BigDecimal maxOrderAmount = orderRepository.findMaxAmountByBuyerId(userId);
            BigDecimal minOrderAmount = orderRepository.findMinAmountByBuyerId(userId);
            if (maxOrderAmount == null) maxOrderAmount = BigDecimal.ZERO;
            if (minOrderAmount == null) minOrderAmount = BigDecimal.ZERO;

            // 构建统计数据
            UserOrderStats stats = UserOrderStats.builder()
                    .totalOrders(totalOrders)
                    .totalAmount(totalAmount)
                    .pendingOrders(pendingOrders)
                    .completedOrders(completedOrders)
                    .cancelledOrders(cancelledOrders)
                    .refundedOrders(refundedOrders)
                    .monthlyOrders(monthlyOrders)
                    .monthlyAmount(monthlyAmount)
                    .lastOrderTime(lastOrderTime)
                    .maxOrderAmount(maxOrderAmount)
                    .minOrderAmount(minOrderAmount)
                    .build();

            // 计算平均订单金额
            stats.calculateAverageOrderAmount();

            log.info("获取用户订单统计成功: userId={}, stats={}", userId, stats);
            return stats;

        } catch (Exception e) {
            log.error("获取用户订单统计失败: userId={}", userId, e);
            return UserOrderStats.empty();
        }
    }
}
