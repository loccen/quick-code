package com.quickcode.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 项目实体类
 * 对应数据库表：projects
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_time", columnList = "created_time"),
    @Index(name = "idx_price", columnList = "price"),
    @Index(name = "idx_download_count", columnList = "download_count"),
    @Index(name = "idx_category_id", columnList = "category_id"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
public class Project extends BaseEntity {

    /**
     * 项目标题
     */
    @NotBlank(message = "项目标题不能为空")
    @Size(max = 100, message = "项目标题长度不能超过100个字符")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    /**
     * 项目描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 项目分类ID
     */
    @NotNull(message = "项目分类不能为空")
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * 上传用户ID
     */
    @NotNull(message = "上传用户不能为空")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 项目价格（积分）
     */
    @Builder.Default
    @DecimalMin(value = "0.00", message = "项目价格不能为负数")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * 封面图片URL
     */
    @Size(max = 255, message = "封面图片URL长度不能超过255个字符")
    @Column(name = "cover_image", length = 255)
    private String coverImage;

    /**
     * 演示地址
     */
    @Size(max = 255, message = "演示地址长度不能超过255个字符")
    @Column(name = "demo_url", length = 255)
    private String demoUrl;

    /**
     * 源码文件URL
     */
    @Size(max = 255, message = "源码文件URL长度不能超过255个字符")
    @Column(name = "source_file_url", length = 255)
    private String sourceFileUrl;

    /**
     * Docker镜像名称
     */
    @Size(max = 255, message = "Docker镜像名称长度不能超过255个字符")
    @Column(name = "docker_image", length = 255)
    private String dockerImage;

    /**
     * 技术栈（JSON格式）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tech_stack", columnDefinition = "JSON")
    private List<String> techStack;

    /**
     * 标签（JSON格式）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "JSON")
    private List<String> tags;

    /**
     * 下载次数
     */
    @Builder.Default
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;

    /**
     * 浏览次数
     */
    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    /**
     * 点赞次数
     */
    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    /**
     * 收藏次数
     */
    @Builder.Default
    @Column(name = "favorite_count", nullable = false)
    private Integer favoriteCount = 0;

    /**
     * 评分（0-5）
     */
    @Builder.Default
    @DecimalMin(value = "0.00", message = "评分不能为负数")
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    /**
     * 评分人数
     */
    @Builder.Default
    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0;

    /**
     * 项目状态
     * 0: 待审核
     * 1: 已发布
     * 2: 已下架
     * 3: 审核拒绝
     */
    @Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 是否精选
     * 0: 否
     * 1: 是
     */
    @Builder.Default
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    /**
     * 发布时间
     */
    @Column(name = "published_time")
    private LocalDateTime publishedTime;

    /**
     * 关联的用户实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 关联的分类实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    /**
     * 项目评价列表
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    /**
     * 项目状态枚举
     */
    public enum Status {
        PENDING(0, "待审核"),
        PUBLISHED(1, "已发布"),
        OFFLINE(2, "已下架"),
        REJECTED(3, "审核拒绝");

        private final Integer code;
        private final String description;

        Status(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static Status fromCode(Integer code) {
            for (Status status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("未知的项目状态代码: " + code);
        }
    }

    /**
     * 检查项目是否已发布
     */
    public boolean isPublished() {
        return Status.PUBLISHED.getCode().equals(this.status);
    }

    /**
     * 检查项目是否为精选项目
     */
    public boolean isFeaturedProject() {
        return Boolean.TRUE.equals(this.isFeatured);
    }

    /**
     * 增加下载次数
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    /**
     * 增加浏览次数
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 增加点赞次数
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 减少点赞次数
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 更新评分
     */
    public void updateRating(BigDecimal newRating) {
        if (newRating != null && newRating.compareTo(BigDecimal.ZERO) >= 0 
            && newRating.compareTo(new BigDecimal("5.00")) <= 0) {
            // 计算新的平均评分
            BigDecimal totalRating = this.rating.multiply(new BigDecimal(this.ratingCount));
            totalRating = totalRating.add(newRating);
            this.ratingCount++;
            this.rating = totalRating.divide(new BigDecimal(this.ratingCount), 2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 发布项目
     */
    public void publish() {
        this.status = Status.PUBLISHED.getCode();
        this.publishedTime = LocalDateTime.now();
    }

    /**
     * 下架项目
     */
    public void takeOffline() {
        this.status = Status.OFFLINE.getCode();
    }

    /**
     * 拒绝项目
     */
    public void reject() {
        this.status = Status.REJECTED.getCode();
    }

    /**
     * 设置为精选项目
     */
    public void setAsFeatured() {
        this.isFeatured = true;
    }

    /**
     * 取消精选
     */
    public void unsetFeatured() {
        this.isFeatured = false;
    }
}
