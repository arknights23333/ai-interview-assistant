package com.assistant.aiinterviewassistant.modules.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * 账号密码登录请求。
 */
@Data
public class LoginDto {

    /** 登录账号。 */
    @Size(max=64,message="账号长度不能超过64个字符")
    @NotBlank(message = "账号不能为空")
    private String username;

    /** 登录密码，仅用于本次身份认证。 */
    @NotBlank(message = "密码不能为空")
    @ToString.Exclude
    private String password;
}
