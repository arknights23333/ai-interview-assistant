package com.assistant.aiinterviewassistant.modules.user.domain.dto;

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
    @NotBlank(message = "邮箱地址不能为空")
    private String email;
}
