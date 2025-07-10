package com.quickcode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮箱验证配置属性
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.business.email")
public class EmailProperties {

    /**
     * 邮箱验证码（开发和测试环境使用固定验证码）
     */
    private String verificationCode = "123456";

    /**
     * 邮箱验证码过期时间（分钟）
     */
    private Integer verificationExpireMinutes = 30;

    /**
     * 密码重置验证码（开发和测试环境使用固定验证码）
     */
    private String passwordResetCode = "888888";

    /**
     * 密码重置验证码过期时间（分钟）
     */
    private Integer passwordResetExpireMinutes = 15;
}
