package com.quickcode.common.exception;

import java.math.BigDecimal;

/**
 * 资源不足异常
 * 当资源数量不足以完成操作时抛出
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public class InsufficientResourceException extends BusinessException {

    public InsufficientResourceException(String message) {
        super(400, message);
    }

    public InsufficientResourceException(Integer code, String message) {
        super(code, message);
    }

    public InsufficientResourceException(Integer code, String message, Object details) {
        super(code, message, details);
    }

    /**
     * 常用资源不足异常静态方法
     */
    public static InsufficientResourceException insufficientPoints() {
        return new InsufficientResourceException(ErrorCode.INSUFFICIENT_POINTS, 
            ErrorCode.getDefaultMessage(ErrorCode.INSUFFICIENT_POINTS));
    }

    public static InsufficientResourceException insufficientPoints(String message) {
        return new InsufficientResourceException(ErrorCode.INSUFFICIENT_POINTS, message);
    }

    public static InsufficientResourceException insufficientPoints(BigDecimal required, BigDecimal available) {
        return new InsufficientResourceException(ErrorCode.INSUFFICIENT_POINTS, 
            String.format("积分不足，需要 %s，可用 %s", required, available));
    }

    public static InsufficientResourceException insufficientFrozenPoints() {
        return new InsufficientResourceException(ErrorCode.INSUFFICIENT_FROZEN_POINTS, 
            ErrorCode.getDefaultMessage(ErrorCode.INSUFFICIENT_FROZEN_POINTS));
    }

    public static InsufficientResourceException insufficientFrozenPoints(String message) {
        return new InsufficientResourceException(ErrorCode.INSUFFICIENT_FROZEN_POINTS, message);
    }

    public static InsufficientResourceException insufficientFrozenPoints(BigDecimal required, BigDecimal available) {
        return new InsufficientResourceException(ErrorCode.INSUFFICIENT_FROZEN_POINTS, 
            String.format("冻结积分不足，需要 %s，可用 %s", required, available));
    }

    public static InsufficientResourceException resource(String resourceType, Object required, Object available) {
        return new InsufficientResourceException(400, 
            String.format("%s不足，需要 %s，可用 %s", resourceType, required, available));
    }

    public static InsufficientResourceException withCode(Integer code, String message) {
        return new InsufficientResourceException(code, message);
    }
}
