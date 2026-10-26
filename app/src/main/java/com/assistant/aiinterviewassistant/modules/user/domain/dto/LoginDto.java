package com.assistant.aiinterviewassistant.modules.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * 账号密码登录请求。
 */
@Data
public class LoginDto {

    /** 登录账号。 */
    @NotBlank(message = "账号不能为空")
    private String username;

    /** 登录密码，仅用于本次身份认证。 */
    @NotBlank(message = "密码不能为空")
    @ToString.Exclude
    private String password;
}
