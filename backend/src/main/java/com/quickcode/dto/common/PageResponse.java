package com.quickcode.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * 数据列表
     */
    private List<T> content;

    /**
     * 当前页码（从0开始）
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 总记录数
     */
    private Long totalElements;

    /**
     * 是否为第一页
     */
    private Boolean first;

    /**
     * 是否为最后一页
     */
    private Boolean last;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 当前页记录数
     */
    private Integer numberOfElements;

    /**
     * 是否为空页
     */
    private Boolean empty;

    /**
     * 排序信息
     */
    private SortInfo sort;

    /**
     * 从Spring Data的Page对象创建PageResponse
     */
    public static <T> PageResponse<T> fromPage(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .first(page.isFirst())
                .last(page.isLast())
                .hasPrevious(page.hasPrevious())
                .hasNext(page.hasNext())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .sort(SortInfo.fromSort(page.getSort()))
                .build();
    }

    /**
     * 创建空的分页响应
     */
    public static <T> PageResponse<T> empty() {
        return PageResponse.<T>builder()
                .content(List.of())
                .page(0)
                .size(0)
                .totalPages(0)
                .totalElements(0L)
                .first(true)
                .last(true)
                .hasPrevious(false)
                .hasNext(false)
                .numberOfElements(0)
                .empty(true)
                .build();
    }

    /**
     * 检查是否有数据
     */
    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    /**
     * 获取下一页页码
     */
    public Integer getNextPage() {
        return hasNext ? page + 1 : null;
    }

    /**
     * 获取上一页页码
     */
    public Integer getPreviousPage() {
        return hasPrevious ? page - 1 : null;
    }

    /**
     * 获取页码范围（用于分页导航）
     */
    public List<Integer> getPageRange(int maxPages) {
        List<Integer> pages = new java.util.ArrayList<>();
        
        int start = Math.max(0, page - maxPages / 2);
        int end = Math.min(totalPages - 1, start + maxPages - 1);
        
        // 调整起始页，确保显示足够的页码
        if (end - start + 1 < maxPages && start > 0) {
            start = Math.max(0, end - maxPages + 1);
        }
        
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        
        return pages;
    }

    /**
     * 获取页码范围（默认显示10页）
     */
    public List<Integer> getPageRange() {
        return getPageRange(10);
    }

    /**
     * 获取分页信息文本
     */
    public String getPageInfo() {
        if (empty) {
            return "暂无数据";
        }
        
        long start = (long) page * size + 1;
        long end = Math.min(start + numberOfElements - 1, totalElements);
        
        return String.format("第 %d-%d 条，共 %d 条", start, end, totalElements);
    }

    /**
     * 获取页码信息文本
     */
    public String getPageNumberInfo() {
        if (totalPages == 0) {
            return "第 0 页，共 0 页";
        }
        return String.format("第 %d 页，共 %d 页", page + 1, totalPages);
    }

    /**
     * 排序信息DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortInfo {
        
        /**
         * 是否已排序
         */
        private Boolean sorted;
        
        /**
         * 是否未排序
         */
        private Boolean unsorted;
        
        /**
         * 是否为空排序
         */
        private Boolean empty;
        
        /**
         * 排序字段列表
         */
        private List<SortOrder> orders;
        
        /**
         * 从Spring Data的Sort对象创建SortInfo
         */
        public static SortInfo fromSort(org.springframework.data.domain.Sort sort) {
            List<SortOrder> orders = sort.stream()
                    .map(order -> SortOrder.builder()
                            .property(order.getProperty())
                            .direction(order.getDirection().name())
                            .ascending(order.isAscending())
                            .descending(order.isDescending())
                            .build())
                    .toList();
            
            return SortInfo.builder()
                    .sorted(sort.isSorted())
                    .unsorted(sort.isUnsorted())
                    .empty(sort.isEmpty())
                    .orders(orders)
                    .build();
        }
    }
    
    /**
     * 排序字段DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortOrder {
        
        /**
         * 排序字段
         */
        private String property;
        
        /**
         * 排序方向
         */
        private String direction;
        
        /**
         * 是否升序
         */
        private Boolean ascending;
        
        /**
         * 是否降序
         */
        private Boolean descending;
    }
}
