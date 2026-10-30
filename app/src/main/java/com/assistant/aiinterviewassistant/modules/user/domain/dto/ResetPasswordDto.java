package com.assistant.aiinterviewassistant.modules.user.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * 不记得旧密码且处于未登录状态下，通过邮箱验证来重置密码
 */
@Data
public class ResetPasswordDto {
    /**
     * 用户账号
     */
    @Size(max=64,message="账号长度不能超过64个字符")
    @NotBlank(message = "账号不能为空")
    private String username;
    /**
     * 邮箱地址
     */
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱地址不能为空")
    private String email;
    /**
     * 邮箱验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(
            regexp = "^[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{6}$",
            message = "验证码格式不正确"
    )
    private String code;
    /**
     * 新密码
     */
    @ToString.Exclude
    @NotBlank(message = "新密码不能为空")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*[0-9])[\\x21-\\x7E]{8,64}$",
            message = "密码须为8～64位，至少包含字母和数字，且不能包含空格或中文"
    )
    private String newPassword;
}
