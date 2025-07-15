package com.quickcode.common.exception;

/**
 * 参数无效异常
 * 当方法参数不符合要求时抛出
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public class InvalidParameterException extends BusinessException {

    public InvalidParameterException(String message) {
        super(400, message);
    }

    public InvalidParameterException(Integer code, String message) {
        super(code, message);
    }

    public InvalidParameterException(Integer code, String message, Object details) {
        super(code, message, details);
    }

    /**
     * 常用参数异常静态方法
     */
    public static InvalidParameterException invalidPageParams() {
        return new InvalidParameterException(ErrorCode.INVALID_PAGE_PARAMS, 
            ErrorCode.getDefaultMessage(ErrorCode.INVALID_PAGE_PARAMS));
    }

    public static InvalidParameterException invalidPageParams(String message) {
        return new InvalidParameterException(ErrorCode.INVALID_PAGE_PARAMS, message);
    }

    public static InvalidParameterException invalidPointAmount() {
        return new InvalidParameterException(ErrorCode.INVALID_POINT_AMOUNT, 
            ErrorCode.getDefaultMessage(ErrorCode.INVALID_POINT_AMOUNT));
    }

    public static InvalidParameterException invalidPointAmount(String message) {
        return new InvalidParameterException(ErrorCode.INVALID_POINT_AMOUNT, message);
    }

    public static InvalidParameterException invalidUsernameFormat() {
        return new InvalidParameterException(ErrorCode.INVALID_USERNAME_FORMAT, 
            ErrorCode.getDefaultMessage(ErrorCode.INVALID_USERNAME_FORMAT));
    }

    public static InvalidParameterException invalidEmailFormat() {
        return new InvalidParameterException(ErrorCode.INVALID_EMAIL_FORMAT, 
            ErrorCode.getDefaultMessage(ErrorCode.INVALID_EMAIL_FORMAT));
    }

    public static InvalidParameterException weakPassword() {
        return new InvalidParameterException(ErrorCode.WEAK_PASSWORD, 
            ErrorCode.getDefaultMessage(ErrorCode.WEAK_PASSWORD));
    }

    public static InvalidParameterException parameter(String paramName, Object value, String reason) {
        return new InvalidParameterException(400, 
            String.format("参数 %s 的值 %s 无效: %s", paramName, value, reason));
    }

    public static InvalidParameterException withCode(Integer code, String message) {
        return new InvalidParameterException(code, message);
    }
}
