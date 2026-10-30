package com.assistant.aiinterviewassistant.modules.user.domain.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.ToString;

@Data
public class ChangeEmailDto {

    /**
     * 新邮箱
     */
    @NotBlank(message = "新邮箱不能为空")
    @Email(message = "新邮箱格式不正确")
    private String newEmail;
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
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @ToString.Exclude
    private String password;
}
