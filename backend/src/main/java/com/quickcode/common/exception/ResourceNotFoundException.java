package com.quickcode.common.exception;

/**
 * 资源不存在异常
 * 当请求的资源不存在时抛出
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    public ResourceNotFoundException(Integer code, String message) {
        super(code, message);
    }

    public ResourceNotFoundException(String resourceType, Object id) {
        super(404, String.format("%s (ID: %s) 不存在", resourceType, id));
    }

    public ResourceNotFoundException(String resourceType, String field, Object value) {
        super(404, String.format("%s (%s: %s) 不存在", resourceType, field, value));
    }

    /**
     * 常用资源不存在异常
     */
    public static ResourceNotFoundException user(Long userId) {
        return new ResourceNotFoundException("用户", userId);
    }

    public static ResourceNotFoundException userByUsername(String username) {
        return new ResourceNotFoundException("用户", "用户名", username);
    }

    public static ResourceNotFoundException userByEmail(String email) {
        return new ResourceNotFoundException("用户", "邮箱", email);
    }

    public static ResourceNotFoundException role(Long roleId) {
        return new ResourceNotFoundException("角色", roleId);
    }

    public static ResourceNotFoundException roleByCode(String roleCode) {
        return new ResourceNotFoundException("角色", "代码", roleCode);
    }

    public static ResourceNotFoundException permission(Long permissionId) {
        return new ResourceNotFoundException("权限", permissionId);
    }

    public static ResourceNotFoundException permissionByCode(String permissionCode) {
        return new ResourceNotFoundException("权限", "代码", permissionCode);
    }

    public static ResourceNotFoundException pointAccount(Long userId) {
        return new ResourceNotFoundException("积分账户", "用户ID", userId);
    }
}
