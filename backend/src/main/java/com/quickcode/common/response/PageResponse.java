package com.quickcode.common.response;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页响应格式
 *
 * @param <T> 数据类型
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
   * 当前页码（从1开始）
   */
  private Integer page;

  /**
   * 每页大小
   */
  private Integer size;

  /**
   * 总记录数
   */
  private Long total;

  /**
   * 总页数
   */
  private Integer totalPages;

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
  public Boolean getHasPrevious() {
    return page != null && page > 1;
  }

  /**
   * 是否有下一页
   */
  public Boolean getHasNext() {
    return !last;
  }

  /**
   * 获取当前页的记录数
   */
  public Integer getCurrentSize() {
    return content != null ? content.size() : 0;
  }

  /**
   * 检查是否为空页
   */
  public Boolean isEmpty() {
    return content == null || content.isEmpty();
  }

  /**
   * 从Spring Data的Page对象创建PageResponse
   *
   * @param page Spring Data的Page对象
   * @param mapper 数据转换函数
   * @param <S> 源数据类型
   * @param <T> 目标数据类型
   * @return PageResponse对象
   */
  public static <S, T> PageResponse<T> fromPage(Page<S> page, Function<S, T> mapper) {
    List<T> content = page.getContent().stream().map(mapper).toList();

    return PageResponse.<T>builder().content(content).page(page.getNumber() + 1) // 前端页码从1开始
        .size(page.getSize()).total(page.getTotalElements()).totalPages(page.getTotalPages())
        .first(page.isFirst()).last(page.isLast()).build();
  }

  /**
   * 从Spring Data的Page对象创建PageResponse（不转换数据类型）
   *
   * @param page Spring Data的Page对象
   * @param <T> 数据类型
   * @return PageResponse对象
   */
  public static <T> PageResponse<T> fromPage(Page<T> page) {
    return fromPage(page, Function.identity());
  }
}
