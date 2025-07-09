package com.quickcode.repository;

import com.quickcode.entity.PointAccount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 积分账户Repository接口
 * 提供积分账户相关的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface PointAccountRepository extends BaseRepository<PointAccount, Long> {

    /**
     * 根据用户ID查找积分账户
     */
    Optional<PointAccount> findByUserId(Long userId);

    /**
     * 检查用户是否已有积分账户
     */
    boolean existsByUserId(Long userId);

    /**
     * 查找总积分大于指定值的账户
     */
    List<PointAccount> findByTotalPointsGreaterThan(BigDecimal totalPoints);

    /**
     * 查找可用积分大于指定值的账户
     */
    List<PointAccount> findByAvailablePointsGreaterThan(BigDecimal availablePoints);

    /**
     * 查找冻结积分大于指定值的账户
     */
    List<PointAccount> findByFrozenPointsGreaterThan(BigDecimal frozenPoints);

    /**
     * 查找总积分在指定范围内的账户
     */
    @Query("SELECT pa FROM PointAccount pa WHERE pa.totalPoints BETWEEN :minPoints AND :maxPoints")
    List<PointAccount> findByTotalPointsBetween(@Param("minPoints") BigDecimal minPoints, 
                                               @Param("maxPoints") BigDecimal maxPoints);

    /**
     * 查找可用积分在指定范围内的账户
     */
    @Query("SELECT pa FROM PointAccount pa WHERE pa.availablePoints BETWEEN :minPoints AND :maxPoints")
    List<PointAccount> findByAvailablePointsBetween(@Param("minPoints") BigDecimal minPoints, 
                                                   @Param("maxPoints") BigDecimal maxPoints);

    /**
     * 查找积分账户余额排行榜（前N名）
     */
    @Query("SELECT pa FROM PointAccount pa ORDER BY pa.totalPoints DESC")
    List<PointAccount> findTopByTotalPointsDesc();

    /**
     * 查找积分消费排行榜（前N名）
     */
    @Query("SELECT pa FROM PointAccount pa ORDER BY pa.totalSpent DESC")
    List<PointAccount> findTopByTotalSpentDesc();

    /**
     * 查找积分获得排行榜（前N名）
     */
    @Query("SELECT pa FROM PointAccount pa ORDER BY pa.totalEarned DESC")
    List<PointAccount> findTopByTotalEarnedDesc();

    /**
     * 查找有冻结积分的账户
     */
    @Query("SELECT pa FROM PointAccount pa WHERE pa.frozenPoints > 0")
    List<PointAccount> findAccountsWithFrozenPoints();

    /**
     * 查找积分不一致的账户（用于数据校验）
     */
    @Query("SELECT pa FROM PointAccount pa WHERE pa.totalPoints != (pa.availablePoints + pa.frozenPoints)")
    List<PointAccount> findInconsistentAccounts();

    /**
     * 统计积分账户总数
     */
    @Query("SELECT COUNT(pa) FROM PointAccount pa")
    Long countAllAccounts();

    /**
     * 统计总积分总和
     */
    @Query("SELECT SUM(pa.totalPoints) FROM PointAccount pa")
    BigDecimal sumTotalPoints();

    /**
     * 统计可用积分总和
     */
    @Query("SELECT SUM(pa.availablePoints) FROM PointAccount pa")
    BigDecimal sumAvailablePoints();

    /**
     * 统计冻结积分总和
     */
    @Query("SELECT SUM(pa.frozenPoints) FROM PointAccount pa")
    BigDecimal sumFrozenPoints();

    /**
     * 统计累计获得积分总和
     */
    @Query("SELECT SUM(pa.totalEarned) FROM PointAccount pa")
    BigDecimal sumTotalEarned();

    /**
     * 统计累计消费积分总和
     */
    @Query("SELECT SUM(pa.totalSpent) FROM PointAccount pa")
    BigDecimal sumTotalSpent();

    /**
     * 计算平均积分余额
     */
    @Query("SELECT AVG(pa.totalPoints) FROM PointAccount pa")
    BigDecimal avgTotalPoints();

    /**
     * 统计有积分的账户数量
     */
    @Query("SELECT COUNT(pa) FROM PointAccount pa WHERE pa.totalPoints > 0")
    Long countAccountsWithPoints();

    /**
     * 统计零余额账户数量
     */
    @Query("SELECT COUNT(pa) FROM PointAccount pa WHERE pa.totalPoints = 0")
    Long countZeroBalanceAccounts();
}
