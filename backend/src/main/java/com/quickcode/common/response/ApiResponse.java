package com.quickcode.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一API响应格式
 * 
 * @param <T> 响应数据类型
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 请求追踪ID
     */
    private String traceId;

    /**
     * 响应码常量
     */
    public static class Code {
        public static final int SUCCESS = 200;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int INTERNAL_ERROR = 500;
        public static final int SERVICE_UNAVAILABLE = 503;
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(Code.SUCCESS)
                .message("操作成功")
                .data(data)
                .build();
    }

    /**
     * 成功响应（无数据）
     */
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .code(Code.SUCCESS)
                .message("操作成功")
                .build();
    }

    /**
     * 成功响应（带自定义消息）
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(Code.SUCCESS)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .code(Code.INTERNAL_ERROR)
                .message(message)
                .build();
    }

    /**
     * 失败响应（带错误码）
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 参数错误响应
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
                .code(Code.BAD_REQUEST)
                .message(message)
                .build();
    }

    /**
     * 未授权响应
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .code(Code.UNAUTHORIZED)
                .message(message != null ? message : "未授权访问")
                .build();
    }

    /**
     * 禁止访问响应
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
                .code(Code.FORBIDDEN)
                .message(message != null ? message : "禁止访问")
                .build();
    }

    /**
     * 资源不存在响应
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .code(Code.NOT_FOUND)
                .message(message != null ? message : "资源不存在")
                .build();
    }

    /**
     * 服务不可用响应
     */
    public static <T> ApiResponse<T> serviceUnavailable(String message) {
        return ApiResponse.<T>builder()
                .code(Code.SERVICE_UNAVAILABLE)
                .message(message != null ? message : "服务暂时不可用")
                .build();
    }

    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return Code.SUCCESS == this.code;
    }

    /**
     * 设置追踪ID
     */
    public ApiResponse<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
}
