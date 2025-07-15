package com.quickcode.common.exception;

/**
 * 资源重复异常
 * 当尝试创建已存在的资源时抛出
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String message) {
        super(409, message);
    }

    public DuplicateResourceException(Integer code, String message) {
        super(code, message);
    }

    public DuplicateResourceException(Integer code, String message, Object details) {
        super(code, message, details);
    }

    /**
     * 常用资源重复异常静态方法
     */
    public static DuplicateResourceException usernameExists(String username) {
        return new DuplicateResourceException(ErrorCode.USERNAME_EXISTS, 
            "用户名已存在: " + username);
    }

    public static DuplicateResourceException emailExists(String email) {
        return new DuplicateResourceException(ErrorCode.EMAIL_EXISTS, 
            "邮箱已存在: " + email);
    }

    public static DuplicateResourceException roleCodeExists(String roleCode) {
        return new DuplicateResourceException(ErrorCode.ROLE_CODE_EXISTS, 
            "角色代码已存在: " + roleCode);
    }

    public static DuplicateResourceException permissionCodeExists(String permissionCode) {
        return new DuplicateResourceException(ErrorCode.PERMISSION_CODE_EXISTS, 
            "权限代码已存在: " + permissionCode);
    }

    public static DuplicateResourceException resource(String resourceType, Object value) {
        return new DuplicateResourceException(409, 
            String.format("%s '%s' 已存在", resourceType, value));
    }

    public static DuplicateResourceException withCode(Integer code, String message) {
        return new DuplicateResourceException(code, message);
    }
}
