package com.quickcode.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 双因素认证设置请求DTO
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorSetupRequest {

    /**
     * 是否启用2FA
     */
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    /**
     * TOTP验证码（启用时必填）
     */
    @Size(min = 6, max = 6, message = "验证码必须是6位数字")
    @Pattern(regexp = "^\\d{6}$", message = "验证码只能包含数字")
    private String totpCode;
}
