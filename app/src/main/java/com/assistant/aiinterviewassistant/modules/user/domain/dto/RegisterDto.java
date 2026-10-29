package com.assistant.aiinterviewassistant.modules.user.domain.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.ToString;

/**
 * 用户注册dto
 */
@Data
public class RegisterDto {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @ToString.Exclude
    @NotBlank(message = "密码不能为空")
    private String password;
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱地址不能为空")
    private String email;
    @Pattern(
            regexp = "^[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{6}$",
            message = "验证码格式不正确"
    )
    @NotBlank(message = "验证码不能为空")
    private String code;
    private String nickname;
    private String avatar;
}
