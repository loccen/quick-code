package com.quickcode.repository;

import com.quickcode.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订单Repository接口
 * 提供订单相关的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface OrderRepository extends BaseRepository<Order, Long> {

    /**
     * 根据订单号查找订单
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 检查订单号是否存在
     */
    boolean existsByOrderNo(String orderNo);

    /**
     * 根据买家ID查找订单
     */
    List<Order> findByBuyerId(Long buyerId);

    /**
     * 根据买家ID分页查找订单
     */
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);

    /**
     * 根据卖家ID查找订单
     */
    List<Order> findBySellerId(Long sellerId);

    /**
     * 根据卖家ID分页查找订单
     */
    Page<Order> findBySellerId(Long sellerId, Pageable pageable);

    /**
     * 根据项目ID查找订单
     */
    List<Order> findByProjectId(Long projectId);

    /**
     * 根据项目ID分页查找订单
     */
    Page<Order> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 根据状态查找订单
     */
    List<Order> findByStatus(Integer status);

    /**
     * 根据状态分页查找订单
     */
    Page<Order> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据买家ID和项目ID查找订单
     */
    List<Order> findByBuyerIdAndProjectId(Long buyerId, Long projectId);

    /**
     * 检查用户是否已购买项目
     */
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.buyerId = :buyerId AND o.projectId = :projectId AND o.status IN (1, 2)")
    boolean hasUserPurchasedProject(@Param("buyerId") Long buyerId, @Param("projectId") Long projectId);

    /**
     * 根据买家ID和状态查找订单
     */
    Page<Order> findByBuyerIdAndStatus(Long buyerId, Integer status, Pageable pageable);

    /**
     * 根据卖家ID和状态查找订单
     */
    Page<Order> findBySellerIdAndStatus(Long sellerId, Integer status, Pageable pageable);

    /**
     * 查找指定时间范围内的订单
     */
    @Query("SELECT o FROM Order o WHERE o.createdTime BETWEEN :startTime AND :endTime")
    List<Order> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定时间范围内的订单（分页）
     */
    @Query("SELECT o FROM Order o WHERE o.createdTime BETWEEN :startTime AND :endTime")
    Page<Order> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime, Pageable pageable);

    /**
     * 查找超时未支付的订单
     */
    @Query("SELECT o FROM Order o WHERE o.status = 0 AND o.createdTime < :timeoutTime")
    List<Order> findTimeoutOrders(@Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 统计用户购买次数
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyerId = :buyerId AND o.status IN (1, 2)")
    Long countUserPurchases(@Param("buyerId") Long buyerId);

    /**
     * 统计用户销售次数
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.sellerId = :sellerId AND o.status IN (1, 2)")
    Long countUserSales(@Param("sellerId") Long sellerId);

    /**
     * 统计用户购买总金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.buyerId = :buyerId AND o.status IN (1, 2)")
    BigDecimal sumUserPurchaseAmount(@Param("buyerId") Long buyerId);

    /**
     * 统计用户销售总金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.sellerId = :sellerId AND o.status IN (1, 2)")
    BigDecimal sumUserSalesAmount(@Param("sellerId") Long sellerId);

    /**
     * 统计项目销售次数
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.projectId = :projectId AND o.status IN (1, 2)")
    Long countProjectSales(@Param("projectId") Long projectId);

    /**
     * 统计项目销售总金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.projectId = :projectId AND o.status IN (1, 2)")
    BigDecimal sumProjectSalesAmount(@Param("projectId") Long projectId);

    /**
     * 根据状态统计订单数量
     */
    Long countByStatus(Integer status);

    /**
     * 统计指定时间范围内的订单数量
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdTime BETWEEN :startTime AND :endTime")
    Long countByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的订单总金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.createdTime BETWEEN :startTime AND :endTime AND o.status IN (1, 2)")
    BigDecimal sumAmountByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 查找热门项目（按销售量排序）
     */
    @Query("SELECT o.projectId, COUNT(o) as salesCount FROM Order o WHERE o.status IN (1, 2) GROUP BY o.projectId ORDER BY salesCount DESC")
    List<Object[]> findPopularProjectsBySales();

    /**
     * 查找热门项目（按销售额排序）
     */
    @Query("SELECT o.projectId, SUM(o.amount) as salesAmount FROM Order o WHERE o.status IN (1, 2) GROUP BY o.projectId ORDER BY salesAmount DESC")
    List<Object[]> findPopularProjectsByAmount();

    /**
     * 查找活跃买家（按购买次数排序）
     */
    @Query("SELECT o.buyerId, COUNT(o) as purchaseCount FROM Order o WHERE o.status IN (1, 2) GROUP BY o.buyerId ORDER BY purchaseCount DESC")
    List<Object[]> findActiveBuyersByCount();

    /**
     * 查找活跃卖家（按销售次数排序）
     */
    @Query("SELECT o.sellerId, COUNT(o) as salesCount FROM Order o WHERE o.status IN (1, 2) GROUP BY o.sellerId ORDER BY salesCount DESC")
    List<Object[]> findActiveSellersByCount();

    /**
     * 批量更新超时订单状态为已取消
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = 3, o.cancellationTime = :cancellationTime, o.remark = '订单超时自动取消' WHERE o.status = 0 AND o.createdTime < :timeoutTime")
    int cancelTimeoutOrders(@Param("timeoutTime") LocalDateTime timeoutTime, 
                           @Param("cancellationTime") LocalDateTime cancellationTime);

    /**
     * 查找用户最近的订单
     */
    @Query("SELECT o FROM Order o WHERE o.buyerId = :buyerId ORDER BY o.createdTime DESC")
    List<Order> findUserRecentOrders(@Param("buyerId") Long buyerId, Pageable pageable);

    /**
     * 查找卖家最近的订单
     */
    @Query("SELECT o FROM Order o WHERE o.sellerId = :sellerId ORDER BY o.createdTime DESC")
    List<Order> findSellerRecentOrders(@Param("sellerId") Long sellerId, Pageable pageable);

    /**
     * 根据金额范围查找订单
     */
    @Query("SELECT o FROM Order o WHERE o.amount BETWEEN :minAmount AND :maxAmount")
    Page<Order> findByAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                   @Param("maxAmount") BigDecimal maxAmount, Pageable pageable);

    /**
     * 查找需要自动完成的订单（已支付超过指定天数）
     */
    @Query("SELECT o FROM Order o WHERE o.status = 1 AND o.paymentTime < :autoCompleteTime")
    List<Order> findOrdersForAutoCompletion(@Param("autoCompleteTime") LocalDateTime autoCompleteTime);

    /**
     * 统计每日订单数量
     */
    @Query("SELECT DATE(o.createdTime) as orderDate, COUNT(o) as orderCount FROM Order o WHERE o.createdTime BETWEEN :startTime AND :endTime GROUP BY DATE(o.createdTime) ORDER BY orderDate")
    List<Object[]> countDailyOrders(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计每日订单金额
     */
    @Query("SELECT DATE(o.createdTime) as orderDate, COALESCE(SUM(o.amount), 0) as totalAmount FROM Order o WHERE o.createdTime BETWEEN :startTime AND :endTime AND o.status IN (1, 2) GROUP BY DATE(o.createdTime) ORDER BY orderDate")
    List<Object[]> sumDailyOrderAmount(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户订单总数
     */
    long countByBuyerId(Long buyerId);

    /**
     * 统计用户订单总金额
     */
    @Query("SELECT SUM(o.amount) FROM Order o WHERE o.buyerId = :buyerId")
    BigDecimal sumAmountByBuyerId(@Param("buyerId") Long buyerId);

    /**
     * 统计用户指定状态的订单数量
     */
    long countByBuyerIdAndStatus(Long buyerId, Integer status);

    /**
     * 统计用户指定时间范围内的订单数量
     */
    long countByBuyerIdAndCreatedTimeBetween(Long buyerId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计用户指定时间范围内的订单金额
     */
    @Query("SELECT SUM(o.amount) FROM Order o WHERE o.buyerId = :buyerId AND o.createdTime BETWEEN :startTime AND :endTime")
    BigDecimal sumAmountByBuyerIdAndCreatedTimeBetween(@Param("buyerId") Long buyerId,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 获取用户最近的订单
     */
    Optional<Order> findTopByBuyerIdOrderByCreatedTimeDesc(Long buyerId);

    /**
     * 获取用户最大订单金额
     */
    @Query("SELECT MAX(o.amount) FROM Order o WHERE o.buyerId = :buyerId")
    BigDecimal findMaxAmountByBuyerId(@Param("buyerId") Long buyerId);

    /**
     * 获取用户最小订单金额
     */
    @Query("SELECT MIN(o.amount) FROM Order o WHERE o.buyerId = :buyerId")
    BigDecimal findMinAmountByBuyerId(@Param("buyerId") Long buyerId);
}
