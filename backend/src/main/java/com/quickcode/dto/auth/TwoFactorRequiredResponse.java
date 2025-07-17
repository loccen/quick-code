package com.quickcode.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 需要双因素认证响应DTO
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorRequiredResponse {

    /**
     * 用户ID（用于第二步验证）
     */
    private Long userId;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 是否需要2FA验证
     */
    private boolean requiresTwoFactor;
}
