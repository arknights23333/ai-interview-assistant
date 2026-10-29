package com.assistant.aiinterviewassistant.modules.user.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
/**
 * 发送邮箱验证码DTO
 */
@Data
public class MailSendDto {
    /**
     * 邮箱地址
     */
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱地址不能为空")
    private String email;
}
