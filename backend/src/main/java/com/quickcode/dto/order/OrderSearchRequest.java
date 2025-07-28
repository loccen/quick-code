package com.quickcode.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单搜索请求
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchRequest {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 买家用户ID
     */
    private Long buyerId;

    /**
     * 卖家用户ID
     */
    private Long sellerId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 最小金额
     */
    private BigDecimal minAmount;

    /**
     * 最大金额
     */
    private BigDecimal maxAmount;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 页码（从0开始）
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 10;

    /**
     * 排序字段
     */
    @Builder.Default
    private String sortBy = "createdTime";

    /**
     * 排序方向
     */
    @Builder.Default
    private String sortDirection = "DESC";

    /**
     * 设置默认值
     */
    public void setDefaults() {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0 || size > 100) {
            size = 10;
        }
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "createdTime";
        }
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "DESC";
        }
    }

    /**
     * 标准化请求数据
     */
    public void normalize() {
        if (orderNo != null) {
            orderNo = orderNo.trim();
            if (orderNo.isEmpty()) {
                orderNo = null;
            }
        }

        if (paymentMethod != null) {
            paymentMethod = paymentMethod.trim().toUpperCase();
            if (paymentMethod.isEmpty()) {
                paymentMethod = null;
            }
        }

        if (sortBy != null) {
            sortBy = sortBy.trim();
        }

        if (sortDirection != null) {
            sortDirection = sortDirection.trim().toUpperCase();
        }
    }

    /**
     * 验证请求数据
     */
    public void validate() {
        if (buyerId != null && buyerId <= 0) {
            throw new IllegalArgumentException("买家用户ID必须为正数");
        }

        if (sellerId != null && sellerId <= 0) {
            throw new IllegalArgumentException("卖家用户ID必须为正数");
        }

        if (projectId != null && projectId <= 0) {
            throw new IllegalArgumentException("项目ID必须为正数");
        }

        if (status != null && (status < 0 || status > 4)) {
            throw new IllegalArgumentException("订单状态值无效");
        }

        if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("最小金额不能为负数");
        }

        if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("最大金额不能为负数");
        }

        if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("最小金额不能大于最大金额");
        }

        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        // 验证排序字段
        String[] allowedSortFields = {"id", "orderNo", "amount", "status", "createdTime", "updatedTime", "paymentTime"};
        boolean validSortField = false;
        for (String field : allowedSortFields) {
            if (field.equals(sortBy)) {
                validSortField = true;
                break;
            }
        }
        if (!validSortField) {
            throw new IllegalArgumentException("不支持的排序字段: " + sortBy);
        }

        // 验证排序方向
        if (!"ASC".equals(sortDirection) && !"DESC".equals(sortDirection)) {
            throw new IllegalArgumentException("排序方向只能是ASC或DESC");
        }
    }

    /**
     * 转换为Pageable对象
     */
    public Pageable toPageable() {
        Sort.Direction direction = "ASC".equals(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }

    /**
     * 检查是否有搜索条件
     */
    public boolean hasSearchCriteria() {
        return orderNo != null || buyerId != null || sellerId != null || projectId != null ||
               status != null || paymentMethod != null || minAmount != null || maxAmount != null ||
               startTime != null || endTime != null;
    }
}
