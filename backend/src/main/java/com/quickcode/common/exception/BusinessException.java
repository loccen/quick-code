package com.quickcode.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于处理业务逻辑相关的异常
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 错误详情
     */
    private final Object details;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
        this.details = null;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.details = null;
    }

    public BusinessException(Integer code, String message, Object details) {
        super(message);
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
        this.details = null;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.details = null;
    }

    /**
     * 常用业务异常静态方法
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(409, message);
    }

    public static BusinessException internalError(String message) {
        return new BusinessException(500, message);
    }
}
