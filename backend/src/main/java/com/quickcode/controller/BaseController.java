package com.quickcode.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import com.quickcode.common.response.ApiResponse;
import com.quickcode.common.response.PageResponse;
import com.quickcode.security.jwt.UserPrincipal;

/**
 * 基础Controller类 提供通用的响应处理和分页处理方法
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
public abstract class BaseController {

  /**
   * 默认页大小
   */
  protected static final int DEFAULT_PAGE_SIZE = 20;

  /**
   * 最大页大小
   */
  protected static final int MAX_PAGE_SIZE = 100;

  /**
   * 成功响应
   */
  protected <T> ApiResponse<T> success(T data) {
    return ApiResponse.success(data);
  }

  /**
   * 成功响应（无数据）
   */
  protected ApiResponse<Void> success() {
    return ApiResponse.success();
  }

  /**
   * 成功响应（带消息）
   */
  protected <T> ApiResponse<T> success(T data, String message) {
    return ApiResponse.success(data, message);
  }

  /**
   * 失败响应
   */
  protected <T> ApiResponse<T> error(String message) {
    return ApiResponse.error(message);
  }

  /**
   * 失败响应（带错误码）
   */
  protected <T> ApiResponse<T> error(int code, String message) {
    return ApiResponse.error(code, message);
  }

  /**
   * 分页响应
   */
  protected <T> ApiResponse<PageResponse<T>> pageSuccess(Page<T> page) {
    PageResponse<T> pageResponse =
        PageResponse.<T>builder().content(page.getContent()).page(page.getNumber() + 1) // 前端页码从1开始
            .size(page.getSize()).total(page.getTotalElements()).totalPages(page.getTotalPages())
            .first(page.isFirst()).last(page.isLast()).build();
    return ApiResponse.success(pageResponse);
  }

  /**
   * 创建分页对象
   */
  protected Pageable createPageable(Integer page, Integer size, String sort, String direction) {
    // 处理页码（前端从1开始，后端从0开始）
    int pageNumber = (page != null && page > 0) ? page - 1 : 0;

    // 处理页大小
    int pageSize = (size != null && size > 0) ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

    // 处理排序
    if (sort != null && !sort.trim().isEmpty()) {
      Sort.Direction sortDirection =
          "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
      return PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sort));
    }

    return PageRequest.of(pageNumber, pageSize);
  }

  /**
   * 创建默认分页对象（按创建时间倒序）
   */
  protected Pageable createDefaultPageable(Integer page, Integer size) {
    return createPageable(page, size, "createdTime", "desc");
  }

  /**
   * 验证分页参数
   */
  protected void validatePageParams(Integer page, Integer size) {
    if (page != null && page < 1) {
      throw new IllegalArgumentException("页码必须大于0");
    }
    if (size != null && (size < 1 || size > MAX_PAGE_SIZE)) {
      throw new IllegalArgumentException("页大小必须在1-" + MAX_PAGE_SIZE + "之间");
    }
  }

  /**
   * 获取当前用户ID（从安全上下文中获取）
   */
  protected Long getCurrentUserId() {
    SecurityContext context = SecurityContextHolder.getContext();
    Authentication authentication = context.getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      return userPrincipal.getId();
    }
    return null;
  }

  /**
   * 检查是否为当前用户
   */
  protected boolean isCurrentUser(Long userId) {
    Long currentUserId = getCurrentUserId();
    return currentUserId != null && currentUserId.equals(userId);
  }

  /**
   * 检查当前用户是否有权限访问指定用户的数据
   */
  protected void checkUserAccess(Long userId) {
    if (!isCurrentUser(userId) && !hasAdminRole()) {
      throw new SecurityException("无权访问该用户数据");
    }
  }

  /**
   * 检查当前用户是否有管理员权限
   */
  protected boolean hasAdminRole() {
    SecurityContext context = SecurityContextHolder.getContext();
    Authentication authentication = context.getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      return userPrincipal.hasRole("ADMIN");
    }
    return false;
  }
}
