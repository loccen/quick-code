package com.quickcode.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 双因素认证登录请求DTO
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorLoginRequest {

    /**
     * 用户ID（第一步验证后获得）
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * TOTP验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须是6位数字")
    @Pattern(regexp = "^\\d{6}$", message = "验证码只能包含数字")
    private String totpCode;
}
