package com.quickcode.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;

/**
 * 项目搜索请求DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSearchRequest {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类代码列表
     */
    private List<String> categoryCodes;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 最低评分
     */
    private BigDecimal minRating;

    /**
     * 最高评分
     */
    private BigDecimal maxRating;

    /**
     * 技术栈列表
     */
    private List<String> techStack;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 项目状态
     */
    private Integer status;

    /**
     * 是否只显示精选项目
     */
    private Boolean featuredOnly;

    /**
     * 是否只显示免费项目
     */
    private Boolean freeOnly;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 排序字段
     * 可选值：created_time, published_time, download_count, view_count, like_count, rating, price
     */
    private String sortBy;

    /**
     * 排序方向
     * 可选值：asc, desc
     */
    private String sortDirection;

    /**
     * 页码（从0开始）
     */
    @Min(value = 0, message = "页码不能小于0")
    private Integer page;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size;

    /**
     * 设置默认值
     */
    public void setDefaults() {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size < 1) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "createdTime";
        }
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "desc";
        }
        if (status == null) {
            status = 1; // 默认只查询已发布的项目
        }
    }

    /**
     * 验证搜索参数
     */
    public void validate() {
        // 验证价格范围
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("最低价格不能为负数");
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("最高价格不能为负数");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("最低价格不能大于最高价格");
        }

        // 验证评分范围
        if (minRating != null && (minRating.compareTo(BigDecimal.ZERO) < 0 || minRating.compareTo(new BigDecimal("5")) > 0)) {
            throw new IllegalArgumentException("最低评分必须在0-5之间");
        }
        if (maxRating != null && (maxRating.compareTo(BigDecimal.ZERO) < 0 || maxRating.compareTo(new BigDecimal("5")) > 0)) {
            throw new IllegalArgumentException("最高评分必须在0-5之间");
        }
        if (minRating != null && maxRating != null && minRating.compareTo(maxRating) > 0) {
            throw new IllegalArgumentException("最低评分不能大于最高评分");
        }

        // 验证排序字段
        if (sortBy != null && !isValidSortField(sortBy)) {
            throw new IllegalArgumentException("无效的排序字段: " + sortBy);
        }

        // 验证排序方向
        if (sortDirection != null && !sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("排序方向只能是asc或desc");
        }

        // 验证技术栈数量
        if (techStack != null && techStack.size() > 10) {
            throw new IllegalArgumentException("技术栈筛选条件不能超过10个");
        }

        // 验证标签数量
        if (tags != null && tags.size() > 10) {
            throw new IllegalArgumentException("标签筛选条件不能超过10个");
        }
    }

    /**
     * 检查是否为有效的排序字段
     */
    private boolean isValidSortField(String field) {
        return switch (field) {
            case "createdTime", "publishedTime", "downloadCount", "viewCount",
                 "likeCount", "rating", "price", "updatedTime" -> true;
            default -> false;
        };
    }

    /**
     * 清理和标准化数据
     */
    public void normalize() {
        // 清理关键词
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }

        // 清理排序字段 - 映射前端字段名到Java实体属性名
        if (sortBy != null) {
            sortBy = mapSortFieldToEntityProperty(sortBy.trim());
        }

        // 清理排序方向
        if (sortDirection != null) {
            sortDirection = sortDirection.trim().toLowerCase();
        }

        // 清理技术栈
        if (techStack != null) {
            techStack = techStack.stream()
                    .filter(tech -> tech != null && !tech.trim().isEmpty())
                    .map(String::trim)
                    .distinct()
                    .toList();
            if (techStack.isEmpty()) {
                techStack = null;
            }
        }

        // 清理标签
        if (tags != null) {
            tags = tags.stream()
                    .filter(tag -> tag != null && !tag.trim().isEmpty())
                    .map(String::trim)
                    .distinct()
                    .toList();
            if (tags.isEmpty()) {
                tags = null;
            }
        }

        // 清理分类代码
        if (categoryCodes != null) {
            categoryCodes = categoryCodes.stream()
                    .filter(code -> code != null && !code.trim().isEmpty())
                    .map(String::trim)
                    .distinct()
                    .toList();
            if (categoryCodes.isEmpty()) {
                categoryCodes = null;
            }
        }
    }

    /**
     * 检查是否有搜索条件
     */
    public boolean hasSearchCriteria() {
        return keyword != null || categoryId != null || categoryCodes != null ||
               minPrice != null || maxPrice != null || minRating != null || maxRating != null ||
               techStack != null || tags != null || userId != null ||
               (featuredOnly != null && featuredOnly) || (freeOnly != null && freeOnly);
    }

    /**
     * 检查是否为免费项目筛选
     */
    public boolean isFreeOnlyFilter() {
        return Boolean.TRUE.equals(freeOnly);
    }

    /**
     * 检查是否为精选项目筛选
     */
    public boolean isFeaturedOnlyFilter() {
        return Boolean.TRUE.equals(featuredOnly);
    }

    /**
     * 获取排序方向（升序或降序）
     */
    public boolean isAscending() {
        return "asc".equalsIgnoreCase(sortDirection);
    }

    /**
     * 转换为Spring Data的Pageable
     */
    public org.springframework.data.domain.Pageable toPageable() {
        org.springframework.data.domain.Sort.Direction direction =
            isAscending() ? org.springframework.data.domain.Sort.Direction.ASC
                         : org.springframework.data.domain.Sort.Direction.DESC;

        org.springframework.data.domain.Sort sort =
            org.springframework.data.domain.Sort.by(direction, sortBy);

        return org.springframework.data.domain.PageRequest.of(page, size, sort);
    }

    /**
     * 映射前端排序字段名到Java实体属性名
     */
    private String mapSortFieldToEntityProperty(String frontendField) {
        if (frontendField == null || frontendField.isEmpty()) {
            return "createdTime"; // 默认排序字段
        }

        // 前端字段名 -> Java实体属性名的映射
        switch (frontendField.toLowerCase()) {
            case "createdat":
            case "created_at":
            case "createdtime":
                return "createdTime";
            case "downloads":
            case "download_count":
            case "downloadcount":
                return "downloadCount";
            case "rating":
                return "rating";
            case "price":
                return "price";
            case "updatedat":
            case "updated_at":
            case "updatedtime":
                return "updatedTime";
            case "viewcount":
            case "view_count":
                return "viewCount";
            case "likecount":
            case "like_count":
                return "likeCount";
            case "ratingcount":
            case "rating_count":
                return "ratingCount";
            case "title":
                return "title";
            case "status":
                return "status";
            default:
                // 如果没有匹配的映射，返回默认排序字段
                return "createdTime";
        }
    }
}
