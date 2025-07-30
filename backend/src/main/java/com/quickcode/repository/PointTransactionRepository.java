package com.quickcode.repository;

import com.quickcode.entity.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分交易记录Repository接口
 * 提供积分交易记录相关的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface PointTransactionRepository extends BaseRepository<PointTransaction, Long> {

    /**
     * 根据用户ID查找交易记录
     */
    List<PointTransaction> findByUserId(Long userId);

    /**
     * 根据用户ID分页查找交易记录
     */
    Page<PointTransaction> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据交易类型查找交易记录
     */
    List<PointTransaction> findByType(Integer type);

    /**
     * 根据交易状态查找交易记录
     */
    List<PointTransaction> findByStatus(Integer status);

    /**
     * 根据用户ID和交易类型查找交易记录
     */
    List<PointTransaction> findByUserIdAndType(Long userId, Integer type);

    /**
     * 根据用户ID和多个交易类型分页查找交易记录
     */
    Page<PointTransaction> findByUserIdAndTypeIn(Long userId, List<Integer> types, Pageable pageable);

    /**
     * 根据用户ID和交易状态查找交易记录
     */
    List<PointTransaction> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 根据关联类型和关联ID查找交易记录
     */
    List<PointTransaction> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    /**
     * 根据创建时间范围查找交易记录
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.createdTime BETWEEN :startTime AND :endTime")
    List<PointTransaction> findByCreatedTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 根据用户ID和时间范围查找交易记录
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.userId = :userId AND pt.createdTime BETWEEN :startTime AND :endTime")
    List<PointTransaction> findByUserIdAndCreatedTimeBetween(@Param("userId") Long userId,
                                                           @Param("startTime") LocalDateTime startTime, 
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 根据交易金额范围查找交易记录
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.amount BETWEEN :minAmount AND :maxAmount")
    List<PointTransaction> findByAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                              @Param("maxAmount") BigDecimal maxAmount);

    /**
     * 查找用户的最新交易记录
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.userId = :userId ORDER BY pt.createdTime DESC")
    List<PointTransaction> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查找用户的收入交易记录
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.userId = :userId AND pt.type IN (1, 3, 4)")
    List<PointTransaction> findIncomeTransactionsByUserId(@Param("userId") Long userId);

    /**
     * 查找用户的支出交易记录
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.userId = :userId AND pt.type IN (2, 5)")
    List<PointTransaction> findExpenseTransactionsByUserId(@Param("userId") Long userId);

    /**
     * 统计用户交易总数
     */
    @Query("SELECT COUNT(pt) FROM PointTransaction pt WHERE pt.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 根据交易类型统计交易数量
     */
    @Query("SELECT COUNT(pt) FROM PointTransaction pt WHERE pt.type = :type")
    Long countByType(@Param("type") Integer type);

    /**
     * 根据交易状态统计交易数量
     */
    @Query("SELECT COUNT(pt) FROM PointTransaction pt WHERE pt.status = :status")
    Long countByStatus(@Param("status") Integer status);

    /**
     * 统计用户收入总额
     */
    @Query("SELECT SUM(pt.amount) FROM PointTransaction pt WHERE pt.userId = :userId AND pt.type IN (1, 3, 4) AND pt.status = 1")
    BigDecimal sumIncomeByUserId(@Param("userId") Long userId);

    /**
     * 统计用户支出总额
     */
    @Query("SELECT SUM(pt.amount) FROM PointTransaction pt WHERE pt.userId = :userId AND pt.type IN (2, 5) AND pt.status = 1")
    BigDecimal sumExpenseByUserId(@Param("userId") Long userId);

    /**
     * 统计指定时间范围内的交易总额
     */
    @Query("SELECT SUM(pt.amount) FROM PointTransaction pt WHERE pt.createdTime BETWEEN :startTime AND :endTime AND pt.status = 1")
    BigDecimal sumAmountByTimePeriod(@Param("startTime") LocalDateTime startTime, 
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 根据交易类型统计交易总额
     */
    @Query("SELECT SUM(pt.amount) FROM PointTransaction pt WHERE pt.type = :type AND pt.status = 1")
    BigDecimal sumAmountByType(@Param("type") Integer type);

    /**
     * 统计每日交易数量
     */
    @Query("SELECT DATE(pt.createdTime), COUNT(pt) FROM PointTransaction pt WHERE pt.createdTime BETWEEN :startTime AND :endTime GROUP BY DATE(pt.createdTime)")
    List<Object[]> countDailyTransactions(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计每日交易金额
     */
    @Query("SELECT DATE(pt.createdTime), SUM(pt.amount) FROM PointTransaction pt WHERE pt.createdTime BETWEEN :startTime AND :endTime AND pt.status = 1 GROUP BY DATE(pt.createdTime)")
    List<Object[]> sumDailyTransactionAmount(@Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 查找大额交易记录
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.amount >= :threshold ORDER BY pt.amount DESC")
    List<PointTransaction> findLargeTransactions(@Param("threshold") BigDecimal threshold);

    /**
     * 查找失败的交易记录
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.status = 0 ORDER BY pt.createdTime DESC")
    List<PointTransaction> findFailedTransactions();

    /**
     * 查找处理中的交易记录
     */
    @Query("SELECT pt FROM PointTransaction pt WHERE pt.status = 2 ORDER BY pt.createdTime ASC")
    List<PointTransaction> findProcessingTransactions();
}
