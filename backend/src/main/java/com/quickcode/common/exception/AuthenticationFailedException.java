package com.quickcode.common.exception;

/**
 * 认证失败异常
 * 用于处理用户认证相关的业务异常
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public class AuthenticationFailedException extends BusinessException {

    public AuthenticationFailedException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, message);
    }

    public AuthenticationFailedException(Integer code, String message) {
        super(code, message);
    }

    public AuthenticationFailedException(Integer code, String message, Object details) {
        super(code, message, details);
    }

    /**
     * 常用认证异常静态方法
     */
    public static AuthenticationFailedException invalidCredentials() {
        return new AuthenticationFailedException(ErrorCode.INVALID_CREDENTIALS, 
            ErrorCode.getDefaultMessage(ErrorCode.INVALID_CREDENTIALS));
    }

    public static AuthenticationFailedException invalidCredentials(String message) {
        return new AuthenticationFailedException(ErrorCode.INVALID_CREDENTIALS, message);
    }

    public static AuthenticationFailedException passwordMismatch() {
        return new AuthenticationFailedException(ErrorCode.PASSWORD_MISMATCH, 
            ErrorCode.getDefaultMessage(ErrorCode.PASSWORD_MISMATCH));
    }

    public static AuthenticationFailedException passwordMismatch(String message) {
        return new AuthenticationFailedException(ErrorCode.PASSWORD_MISMATCH, message);
    }

    public static AuthenticationFailedException invalidEmailCode() {
        return new AuthenticationFailedException(ErrorCode.EMAIL_CODE_INVALID, 
            ErrorCode.getDefaultMessage(ErrorCode.EMAIL_CODE_INVALID));
    }

    public static AuthenticationFailedException invalidEmailCode(String message) {
        return new AuthenticationFailedException(ErrorCode.EMAIL_CODE_INVALID, message);
    }

    public static AuthenticationFailedException termsNotAgreed() {
        return new AuthenticationFailedException(ErrorCode.TERMS_NOT_AGREED, 
            ErrorCode.getDefaultMessage(ErrorCode.TERMS_NOT_AGREED));
    }

    public static AuthenticationFailedException termsNotAgreed(String message) {
        return new AuthenticationFailedException(ErrorCode.TERMS_NOT_AGREED, message);
    }
}
