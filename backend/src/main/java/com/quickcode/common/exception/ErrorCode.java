package com.quickcode.common.exception;

/**
 * 错误码常量定义
 * 用于标准化业务异常的错误码
 * 
 * 错误码分类：
 * - 1xxx：认证相关错误
 * - 2xxx：授权相关错误
 * - 3xxx：验证相关错误
 * - 4xxx：资源相关错误
 * - 5xxx：状态相关错误
 * - 6xxx：业务逻辑错误
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public final class ErrorCode {

    private ErrorCode() {
        // 工具类，禁止实例化
    }

    // ==================== 认证错误 (1xxx) ====================
    
    /** 用户名或密码错误 */
    public static final int INVALID_CREDENTIALS = 1001;
    
    /** 密码和确认密码不一致 */
    public static final int PASSWORD_MISMATCH = 1002;
    
    /** 邮箱验证码不正确 */
    public static final int EMAIL_CODE_INVALID = 1003;
    
    /** 必须同意用户协议和隐私政策 */
    public static final int TERMS_NOT_AGREED = 1004;

    // ==================== 授权错误 (2xxx) ====================
    
    /** 权限不足 */
    public static final int INSUFFICIENT_PERMISSION = 2001;
    
    /** 访问被拒绝 */
    public static final int ACCESS_DENIED = 2002;

    // ==================== 验证错误 (3xxx) ====================
    
    /** 用户名格式不正确 */
    public static final int INVALID_USERNAME_FORMAT = 3001;
    
    /** 邮箱格式不正确 */
    public static final int INVALID_EMAIL_FORMAT = 3002;
    
    /** 密码强度不足 */
    public static final int WEAK_PASSWORD = 3003;
    
    /** 分页参数无效 */
    public static final int INVALID_PAGE_PARAMS = 3004;

    // ==================== 资源错误 (4xxx) ====================
    
    /** 用户不存在 */
    public static final int USER_NOT_FOUND = 4001;
    
    /** 用户名已存在 */
    public static final int USERNAME_EXISTS = 4002;
    
    /** 邮箱已存在 */
    public static final int EMAIL_EXISTS = 4003;
    
    /** 角色不存在 */
    public static final int ROLE_NOT_FOUND = 4004;
    
    /** 角色代码已存在 */
    public static final int ROLE_CODE_EXISTS = 4005;
    
    /** 权限不存在 */
    public static final int PERMISSION_NOT_FOUND = 4006;
    
    /** 权限代码已存在 */
    public static final int PERMISSION_CODE_EXISTS = 4007;
    
    /** 积分账户不存在 */
    public static final int POINT_ACCOUNT_NOT_FOUND = 4008;

    // ==================== 状态错误 (5xxx) ====================
    
    /** 用户已被禁用 */
    public static final int USER_DISABLED = 5001;
    
    /** 用户已被锁定 */
    public static final int USER_LOCKED = 5002;
    
    /** 邮箱已验证，无需重复验证 */
    public static final int EMAIL_ALREADY_VERIFIED = 5003;
    
    /** 验证令牌已过期 */
    public static final int VERIFICATION_TOKEN_EXPIRED = 5004;

    // ==================== 业务逻辑错误 (6xxx) ====================
    
    /** 积分不足 */
    public static final int INSUFFICIENT_POINTS = 6001;
    
    /** 冻结积分不足 */
    public static final int INSUFFICIENT_FROZEN_POINTS = 6002;
    
    /** 积分金额必须大于0 */
    public static final int INVALID_POINT_AMOUNT = 6003;

    // ==================== 错误消息映射 ====================
    
    /**
     * 根据错误码获取默认错误消息
     */
    public static String getDefaultMessage(int code) {
        return switch (code) {
            case INVALID_CREDENTIALS -> "用户名或密码错误";
            case PASSWORD_MISMATCH -> "密码和确认密码不一致";
            case EMAIL_CODE_INVALID -> "邮箱验证码不正确";
            case TERMS_NOT_AGREED -> "必须同意用户协议和隐私政策";
            
            case INSUFFICIENT_PERMISSION -> "权限不足";
            case ACCESS_DENIED -> "访问被拒绝";
            
            case INVALID_USERNAME_FORMAT -> "用户名格式不正确";
            case INVALID_EMAIL_FORMAT -> "邮箱格式不正确";
            case WEAK_PASSWORD -> "密码强度不足";
            case INVALID_PAGE_PARAMS -> "分页参数无效";
            
            case USER_NOT_FOUND -> "用户不存在";
            case USERNAME_EXISTS -> "用户名已存在";
            case EMAIL_EXISTS -> "邮箱已存在";
            case ROLE_NOT_FOUND -> "角色不存在";
            case ROLE_CODE_EXISTS -> "角色代码已存在";
            case PERMISSION_NOT_FOUND -> "权限不存在";
            case PERMISSION_CODE_EXISTS -> "权限代码已存在";
            case POINT_ACCOUNT_NOT_FOUND -> "积分账户不存在";
            
            case USER_DISABLED -> "用户已被禁用";
            case USER_LOCKED -> "用户已被锁定";
            case EMAIL_ALREADY_VERIFIED -> "邮箱已验证，无需重复验证";
            case VERIFICATION_TOKEN_EXPIRED -> "验证令牌已过期";
            
            case INSUFFICIENT_POINTS -> "积分不足";
            case INSUFFICIENT_FROZEN_POINTS -> "冻结积分不足";
            case INVALID_POINT_AMOUNT -> "积分金额必须大于0";
            
            default -> "未知错误";
        };
    }
    
    /**
     * 检查是否为认证错误
     */
    public static boolean isAuthenticationError(int code) {
        return code >= 1000 && code < 2000;
    }
    
    /**
     * 检查是否为授权错误
     */
    public static boolean isAuthorizationError(int code) {
        return code >= 2000 && code < 3000;
    }
    
    /**
     * 检查是否为验证错误
     */
    public static boolean isValidationError(int code) {
        return code >= 3000 && code < 4000;
    }
    
    /**
     * 检查是否为资源错误
     */
    public static boolean isResourceError(int code) {
        return code >= 4000 && code < 5000;
    }
    
    /**
     * 检查是否为状态错误
     */
    public static boolean isStateError(int code) {
        return code >= 5000 && code < 6000;
    }
    
    /**
     * 检查是否为业务逻辑错误
     */
    public static boolean isBusinessLogicError(int code) {
        return code >= 6000 && code < 7000;
    }
}
