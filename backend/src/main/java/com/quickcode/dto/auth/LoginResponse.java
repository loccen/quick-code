package com.quickcode.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO（联合类型）
 * 可能是JWT响应或需要2FA验证的响应
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * 是否需要2FA验证
     */
    private boolean requiresTwoFactor;

    /**
     * JWT响应（当不需要2FA时）
     */
    private JwtResponse jwtResponse;

    /**
     * 2FA验证响应（当需要2FA时）
     */
    private TwoFactorRequiredResponse twoFactorResponse;

    /**
     * 创建JWT响应
     */
    public static LoginResponse jwt(JwtResponse jwtResponse) {
        return LoginResponse.builder()
                .requiresTwoFactor(false)
                .jwtResponse(jwtResponse)
                .build();
    }

    /**
     * 创建2FA验证响应
     */
    public static LoginResponse twoFactor(TwoFactorRequiredResponse twoFactorResponse) {
        return LoginResponse.builder()
                .requiresTwoFactor(true)
                .twoFactorResponse(twoFactorResponse)
                .build();
    }
}
