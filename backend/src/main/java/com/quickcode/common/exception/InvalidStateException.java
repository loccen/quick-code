package com.quickcode.common.exception;

/**
 * 状态无效异常
 * 当资源处于不正确的状态时抛出
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public class InvalidStateException extends BusinessException {

    public InvalidStateException(String message) {
        super(400, message);
    }

    public InvalidStateException(Integer code, String message) {
        super(code, message);
    }

    public InvalidStateException(Integer code, String message, Object details) {
        super(code, message, details);
    }

    /**
     * 常用状态异常静态方法
     */
    public static InvalidStateException userDisabled() {
        return new InvalidStateException(ErrorCode.USER_DISABLED, 
            ErrorCode.getDefaultMessage(ErrorCode.USER_DISABLED));
    }

    public static InvalidStateException userDisabled(String message) {
        return new InvalidStateException(ErrorCode.USER_DISABLED, message);
    }

    public static InvalidStateException userLocked() {
        return new InvalidStateException(ErrorCode.USER_LOCKED, 
            ErrorCode.getDefaultMessage(ErrorCode.USER_LOCKED));
    }

    public static InvalidStateException userLocked(String message) {
        return new InvalidStateException(ErrorCode.USER_LOCKED, message);
    }

    public static InvalidStateException emailAlreadyVerified() {
        return new InvalidStateException(ErrorCode.EMAIL_ALREADY_VERIFIED, 
            ErrorCode.getDefaultMessage(ErrorCode.EMAIL_ALREADY_VERIFIED));
    }

    public static InvalidStateException emailAlreadyVerified(String message) {
        return new InvalidStateException(ErrorCode.EMAIL_ALREADY_VERIFIED, message);
    }

    public static InvalidStateException verificationTokenExpired() {
        return new InvalidStateException(ErrorCode.VERIFICATION_TOKEN_EXPIRED, 
            ErrorCode.getDefaultMessage(ErrorCode.VERIFICATION_TOKEN_EXPIRED));
    }

    public static InvalidStateException verificationTokenExpired(String message) {
        return new InvalidStateException(ErrorCode.VERIFICATION_TOKEN_EXPIRED, message);
    }

    public static InvalidStateException withCode(Integer code, String message) {
        return new InvalidStateException(code, message);
    }
}
