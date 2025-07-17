package com.quickcode.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 双因素认证设置响应DTO
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorSetupResponse {

    /**
     * 密钥（用于生成QR码）
     */
    private String secret;

    /**
     * QR码数据URL
     */
    private String qrCodeUrl;
}
