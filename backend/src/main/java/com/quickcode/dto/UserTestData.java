package com.quickcode.dto;

import lombok.Data;

/**
 * 用户测试数据DTO
 * 用于从JSON文件加载测试数据
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
public class UserTestData {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 密码（明文，将在代码中加密）
     */
    private String password;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 状态（1-正常，0-禁用）
     */
    private Integer status;
    
    /**
     * 邮箱是否已验证
     */
    private Boolean emailVerified;
    
    /**
     * 是否为管理员
     */
    private Boolean isAdmin;
}
