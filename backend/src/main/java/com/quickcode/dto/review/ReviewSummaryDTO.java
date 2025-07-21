package com.quickcode.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 评价摘要DTO
 * 用于项目评价统计信息展示
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDTO {

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 总评价数
     */
    private Long totalReviews;

    /**
     * 平均评分
     */
    private BigDecimal averageRating;

    /**
     * 评分分布（1-5分的数量）
     */
    private Map<Integer, Long> ratingDistribution;

    /**
     * 5星评价数
     */
    private Long fiveStarCount;

    /**
     * 4星评价数
     */
    private Long fourStarCount;

    /**
     * 3星评价数
     */
    private Long threeStarCount;

    /**
     * 2星评价数
     */
    private Long twoStarCount;

    /**
     * 1星评价数
     */
    private Long oneStarCount;

    /**
     * 有内容的评价数
     */
    private Long reviewsWithContentCount;

    /**
     * 匿名评价数
     */
    private Long anonymousReviewsCount;

    /**
     * 最新评价数（最近30天）
     */
    private Long recentReviewsCount;

    /**
     * 好评率（4星及以上）
     */
    private BigDecimal positiveRate;

    /**
     * 检查是否有评价
     */
    public boolean hasReviews() {
        return totalReviews != null && totalReviews > 0;
    }

    /**
     * 获取平均评分文本
     */
    public String getAverageRatingText() {
        if (averageRating == null || !hasReviews()) {
            return "暂无评分";
        }
        return String.format("%.1f分", averageRating.doubleValue());
    }

    /**
     * 获取评价总数文本
     */
    public String getTotalReviewsText() {
        if (!hasReviews()) {
            return "暂无评价";
        }
        if (totalReviews >= 1000) {
            return String.format("%.1fk条评价", totalReviews / 1000.0);
        }
        return totalReviews + "条评价";
    }

    /**
     * 获取好评率文本
     */
    public String getPositiveRateText() {
        if (positiveRate == null || !hasReviews()) {
            return "暂无数据";
        }
        return String.format("%.1f%%", positiveRate.doubleValue());
    }

    /**
     * 获取评分星级（整数）
     */
    public int getStarRating() {
        return averageRating != null ? averageRating.intValue() : 0;
    }

    /**
     * 获取评分星级数组（用于前端显示星星）
     */
    public boolean[] getStarArray() {
        boolean[] stars = new boolean[5];
        if (averageRating != null) {
            int fullStars = averageRating.intValue();
            for (int i = 0; i < 5; i++) {
                stars[i] = i < fullStars;
            }
        }
        return stars;
    }

    /**
     * 获取半星显示（是否显示半星）
     */
    public boolean hasHalfStar() {
        if (averageRating == null) {
            return false;
        }
        BigDecimal remainder = averageRating.remainder(BigDecimal.ONE);
        return remainder.compareTo(new BigDecimal("0.3")) >= 0 && 
               remainder.compareTo(new BigDecimal("0.8")) < 0;
    }

    /**
     * 获取某个星级的评价数量
     */
    public Long getRatingCount(int star) {
        if (ratingDistribution == null) {
            return 0L;
        }
        return ratingDistribution.getOrDefault(star, 0L);
    }

    /**
     * 获取某个星级的评价百分比
     */
    public BigDecimal getRatingPercentage(int star) {
        if (!hasReviews()) {
            return BigDecimal.ZERO;
        }
        Long count = getRatingCount(star);
        return new BigDecimal(count)
                .divide(new BigDecimal(totalReviews), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 获取某个星级的评价百分比文本
     */
    public String getRatingPercentageText(int star) {
        BigDecimal percentage = getRatingPercentage(star);
        return String.format("%.1f%%", percentage.doubleValue());
    }

    /**
     * 获取评价质量描述
     */
    public String getQualityDescription() {
        if (!hasReviews()) {
            return "暂无评价";
        }
        
        if (averageRating == null) {
            return "评分异常";
        }
        
        BigDecimal rating = averageRating;
        if (rating.compareTo(new BigDecimal("4.5")) >= 0) {
            return "优秀";
        } else if (rating.compareTo(new BigDecimal("4.0")) >= 0) {
            return "良好";
        } else if (rating.compareTo(new BigDecimal("3.0")) >= 0) {
            return "一般";
        } else if (rating.compareTo(new BigDecimal("2.0")) >= 0) {
            return "较差";
        } else {
            return "很差";
        }
    }

    /**
     * 获取内容评价比例
     */
    public BigDecimal getContentReviewRate() {
        if (!hasReviews() || reviewsWithContentCount == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(reviewsWithContentCount)
                .divide(new BigDecimal(totalReviews), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 获取内容评价比例文本
     */
    public String getContentReviewRateText() {
        BigDecimal rate = getContentReviewRate();
        return String.format("%.1f%%", rate.doubleValue());
    }

    /**
     * 获取匿名评价比例
     */
    public BigDecimal getAnonymousReviewRate() {
        if (!hasReviews() || anonymousReviewsCount == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(anonymousReviewsCount)
                .divide(new BigDecimal(totalReviews), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 获取匿名评价比例文本
     */
    public String getAnonymousReviewRateText() {
        BigDecimal rate = getAnonymousReviewRate();
        return String.format("%.1f%%", rate.doubleValue());
    }

    /**
     * 获取最近评价比例
     */
    public BigDecimal getRecentReviewRate() {
        if (!hasReviews() || recentReviewsCount == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(recentReviewsCount)
                .divide(new BigDecimal(totalReviews), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 获取最近评价比例文本
     */
    public String getRecentReviewRateText() {
        BigDecimal rate = getRecentReviewRate();
        return String.format("%.1f%%", rate.doubleValue());
    }

    /**
     * 检查评价是否活跃（最近有评价）
     */
    public boolean isActiveReviews() {
        return recentReviewsCount != null && recentReviewsCount > 0;
    }

    /**
     * 检查是否有详细评价
     */
    public boolean hasDetailedReviews() {
        return reviewsWithContentCount != null && reviewsWithContentCount > 0;
    }

    /**
     * 获取评价活跃度描述
     */
    public String getActivityDescription() {
        if (!hasReviews()) {
            return "无评价";
        }
        
        BigDecimal recentRate = getRecentReviewRate();
        if (recentRate.compareTo(new BigDecimal("50")) >= 0) {
            return "评价活跃";
        } else if (recentRate.compareTo(new BigDecimal("20")) >= 0) {
            return "评价一般";
        } else {
            return "评价较少";
        }
    }

    /**
     * 获取推荐指数（基于评分和评价数量）
     */
    public int getRecommendationIndex() {
        if (!hasReviews() || averageRating == null) {
            return 0;
        }
        
        // 基础分数（评分 * 20）
        int baseScore = averageRating.multiply(new BigDecimal("20")).intValue();
        
        // 评价数量加成
        int reviewBonus = 0;
        if (totalReviews >= 100) {
            reviewBonus = 20;
        } else if (totalReviews >= 50) {
            reviewBonus = 15;
        } else if (totalReviews >= 20) {
            reviewBonus = 10;
        } else if (totalReviews >= 10) {
            reviewBonus = 5;
        }
        
        return Math.min(100, baseScore + reviewBonus);
    }

    /**
     * 获取推荐指数文本
     */
    public String getRecommendationIndexText() {
        int index = getRecommendationIndex();
        return index + "%";
    }
}
