package com.quickcode.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 创建评价请求DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 评分（1-5分）
     */
    @NotNull(message = "评分不能为空")
    @DecimalMin(value = "1.0", message = "评分不能低于1分")
    @DecimalMax(value = "5.0", message = "评分不能超过5分")
    private BigDecimal rating;

    /**
     * 评价内容
     */
    @Size(max = 1000, message = "评价内容长度不能超过1000个字符")
    private String content;

    /**
     * 是否匿名评价
     */
    private Boolean isAnonymous;

    /**
     * 验证请求数据
     */
    public void validate() {
        // 验证评分范围
        if (rating != null) {
            if (rating.compareTo(new BigDecimal("1.0")) < 0) {
                throw new IllegalArgumentException("评分不能低于1分");
            }
            if (rating.compareTo(new BigDecimal("5.0")) > 0) {
                throw new IllegalArgumentException("评分不能超过5分");
            }
            // 验证评分精度（只允许0.5的倍数）
            BigDecimal remainder = rating.remainder(new BigDecimal("0.5"));
            if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("评分只能是0.5的倍数（如1.0, 1.5, 2.0等）");
            }
        }

        // 验证评价内容
        if (content != null && content.trim().length() > 1000) {
            throw new IllegalArgumentException("评价内容长度不能超过1000个字符");
        }

        // 验证评价内容不能包含敏感词
        if (content != null && containsSensitiveWords(content)) {
            throw new IllegalArgumentException("评价内容包含敏感词汇，请修改后重试");
        }
    }

    /**
     * 检查是否包含敏感词
     */
    private boolean containsSensitiveWords(String text) {
        // 简单的敏感词检测，实际项目中应该使用专门的敏感词过滤库
        String[] sensitiveWords = {
            "垃圾", "骗子", "诈骗", "病毒", "木马", "黑客",
            "政治", "反动", "暴力", "色情", "赌博", "毒品"
        };
        
        String lowerText = text.toLowerCase();
        for (String word : sensitiveWords) {
            if (lowerText.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清理和标准化数据
     */
    public void normalize() {
        // 清理评价内容
        if (content != null) {
            content = content.trim();
            if (content.isEmpty()) {
                content = null;
            }
        }

        // 设置默认匿名状态
        if (isAnonymous == null) {
            isAnonymous = false;
        }

        // 标准化评分（保留一位小数）
        if (rating != null) {
            rating = rating.setScale(1, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * 检查是否为匿名评价
     */
    public boolean isAnonymousReview() {
        return Boolean.TRUE.equals(isAnonymous);
    }

    /**
     * 检查是否有评价内容
     */
    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }

    /**
     * 获取评分星级（整数）
     */
    public int getStarRating() {
        return rating != null ? rating.intValue() : 0;
    }

    /**
     * 检查评分是否有效
     */
    public boolean isValidRating() {
        return rating != null 
            && rating.compareTo(new BigDecimal("1.0")) >= 0 
            && rating.compareTo(new BigDecimal("5.0")) <= 0;
    }

    /**
     * 获取评价质量等级
     */
    public String getQualityLevel() {
        if (!hasContent()) {
            return "简单评价";
        }
        
        int contentLength = content.length();
        if (contentLength < 20) {
            return "简单评价";
        } else if (contentLength < 100) {
            return "详细评价";
        } else {
            return "深度评价";
        }
    }

    /**
     * 获取评分文本
     */
    public String getRatingText() {
        if (rating == null) {
            return "未评分";
        }
        return rating.stripTrailingZeros().toPlainString() + "分";
    }

    /**
     * 获取评分描述
     */
    public String getRatingDescription() {
        if (rating == null) {
            return "未评分";
        }
        
        BigDecimal score = rating;
        if (score.compareTo(new BigDecimal("1.0")) >= 0 && score.compareTo(new BigDecimal("1.5")) < 0) {
            return "非常不满意";
        } else if (score.compareTo(new BigDecimal("1.5")) >= 0 && score.compareTo(new BigDecimal("2.5")) < 0) {
            return "不满意";
        } else if (score.compareTo(new BigDecimal("2.5")) >= 0 && score.compareTo(new BigDecimal("3.5")) < 0) {
            return "一般";
        } else if (score.compareTo(new BigDecimal("3.5")) >= 0 && score.compareTo(new BigDecimal("4.5")) < 0) {
            return "满意";
        } else {
            return "非常满意";
        }
    }

    /**
     * 设置为匿名评价
     */
    public void setAsAnonymous() {
        this.isAnonymous = true;
    }

    /**
     * 取消匿名
     */
    public void unsetAnonymous() {
        this.isAnonymous = false;
    }

    /**
     * 获取评价内容字数
     */
    public int getContentLength() {
        return hasContent() ? content.length() : 0;
    }

    /**
     * 检查评价内容是否充实
     */
    public boolean isContentRich() {
        return hasContent() && content.length() >= 50;
    }

    /**
     * 获取评价完整度百分比
     */
    public int getCompletenessPercentage() {
        int score = 0;
        
        // 评分占50%
        if (isValidRating()) {
            score += 50;
        }
        
        // 评价内容占50%
        if (hasContent()) {
            if (content.length() >= 20) {
                score += 25;
            }
            if (content.length() >= 100) {
                score += 25;
            }
        }
        
        return score;
    }
}
