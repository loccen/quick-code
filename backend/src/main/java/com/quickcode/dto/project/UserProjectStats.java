package com.quickcode.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户项目统计数据DTO
 * 包含用户相关的项目统计信息
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProjectStats {

    /**
     * 上传的项目数量
     */
    private Long uploadedCount;

    /**
     * 购买的项目数量
     */
    private Long purchasedCount;

    /**
     * 收藏的项目数量
     */
    private Long favoritesCount;

    /**
     * 总收益（积分）
     */
    private BigDecimal totalEarnings;

    /**
     * 已发布的项目数量
     */
    private Long publishedCount;

    /**
     * 待审核的项目数量
     */
    private Long pendingCount;

    /**
     * 项目总下载次数
     */
    private Long totalDownloads;

    /**
     * 项目总浏览次数
     */
    private Long totalViews;

    /**
     * 项目总点赞次数
     */
    private Long totalLikes;

    /**
     * 平均项目评分
     */
    private BigDecimal averageRating;

    /**
     * 创建空的统计数据
     * 
     * @return 空的统计数据
     */
    public static UserProjectStats empty() {
        return UserProjectStats.builder()
                .uploadedCount(0L)
                .purchasedCount(0L)
                .favoritesCount(0L)
                .totalEarnings(BigDecimal.ZERO)
                .publishedCount(0L)
                .pendingCount(0L)
                .totalDownloads(0L)
                .totalViews(0L)
                .totalLikes(0L)
                .averageRating(BigDecimal.ZERO)
                .build();
    }

    /**
     * 检查是否有数据
     * 
     * @return 是否有数据
     */
    public boolean hasData() {
        return uploadedCount > 0 || purchasedCount > 0 || favoritesCount > 0 || 
               totalEarnings.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 获取项目活跃度评分（0-100）
     * 基于上传数量、下载次数、点赞数等计算
     * 
     * @return 活跃度评分
     */
    public int getActivityScore() {
        if (!hasData()) {
            return 0;
        }

        // 简单的活跃度计算公式
        long baseScore = uploadedCount * 10 + publishedCount * 15;
        long interactionScore = totalDownloads + totalViews + totalLikes * 2;
        long totalScore = baseScore + interactionScore / 10;

        return Math.min(100, (int) totalScore);
    }

    /**
     * 获取收益率（每个项目的平均收益）
     * 
     * @return 收益率
     */
    public BigDecimal getEarningsRate() {
        if (publishedCount == 0) {
            return BigDecimal.ZERO;
        }
        return totalEarnings.divide(BigDecimal.valueOf(publishedCount), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取项目成功率（已发布项目占上传项目的比例）
     * 
     * @return 成功率（0-1）
     */
    public BigDecimal getSuccessRate() {
        if (uploadedCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(publishedCount)
                .divide(BigDecimal.valueOf(uploadedCount), 4, BigDecimal.ROUND_HALF_UP);
    }
}
