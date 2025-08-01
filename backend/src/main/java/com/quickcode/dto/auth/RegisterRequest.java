package com.quickcode.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求DTO
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须在8-100个字符之间")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$",
             message = "密码必须包含至少一个字母和一个数字")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 邮箱验证码
     */
    @NotBlank(message = "邮箱验证码不能为空")
    @Size(min = 6, max = 6, message = "邮箱验证码必须为6位数字")
    @Pattern(regexp = "^\\d{6}$", message = "邮箱验证码必须为6位数字")
    private String emailCode;

    /**
     * 是否同意用户协议和隐私政策
     */
    @NotNull(message = "必须同意用户协议和隐私政策")
    private Boolean agreeTerms;

    /**
     * 验证密码是否一致
     */
    public boolean isPasswordMatched() {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * 验证是否同意条款
     */
    public boolean isAgreeTerms() {
        return agreeTerms != null && agreeTerms;
    }
}
