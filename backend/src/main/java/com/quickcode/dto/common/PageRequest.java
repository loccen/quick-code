package com.quickcode.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 分页请求DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

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
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向（asc/desc）
     */
    private String sortDirection;

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
            sortBy = "created_time";
        }
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "desc";
        }
    }

    /**
     * 验证分页参数
     */
    public void validate() {
        if (page != null && page < 0) {
            throw new IllegalArgumentException("页码不能小于0");
        }
        if (size != null && (size < 1 || size > 100)) {
            throw new IllegalArgumentException("每页大小必须在1-100之间");
        }
        if (sortDirection != null && !sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("排序方向只能是asc或desc");
        }
    }

    /**
     * 清理和标准化数据
     */
    public void normalize() {
        if (sortBy != null) {
            sortBy = sortBy.trim().toLowerCase();
        }
        if (sortDirection != null) {
            sortDirection = sortDirection.trim().toLowerCase();
        }
    }

    /**
     * 获取排序方向（升序或降序）
     */
    public boolean isAscending() {
        return "asc".equalsIgnoreCase(sortDirection);
    }

    /**
     * 获取偏移量
     */
    public int getOffset() {
        return page * size;
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
}
