package com.quickcode.common.exception;

import java.util.Map;

/**
 * 验证异常
 * 当数据验证失败时抛出
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(400, message);
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(400, message, fieldErrors);
    }

    /**
     * 常用验证异常
     */
    public static ValidationException invalidParameter(String paramName, Object value) {
        return new ValidationException(String.format("参数 %s 的值 %s 无效", paramName, value));
    }

    public static ValidationException requiredParameter(String paramName) {
        return new ValidationException(String.format("参数 %s 不能为空", paramName));
    }

    public static ValidationException invalidFormat(String field, String expectedFormat) {
        return new ValidationException(String.format("%s 格式不正确，期望格式: %s", field, expectedFormat));
    }

    public static ValidationException outOfRange(String field, Object min, Object max) {
        return new ValidationException(String.format("%s 超出有效范围 [%s, %s]", field, min, max));
    }

    public static ValidationException duplicateValue(String field, Object value) {
        return new ValidationException(String.format("%s '%s' 已存在", field, value));
    }

    public static ValidationException passwordTooWeak() {
        return new ValidationException("密码强度不足，密码必须至少8位，包含字母、数字和特殊字符中的至少两种");
    }

    public static ValidationException emailFormat() {
        return new ValidationException("邮箱格式不正确");
    }

    public static ValidationException usernameFormat() {
        return new ValidationException("用户名格式不正确，只能包含字母、数字和下划线，长度3-50个字符");
    }
}
