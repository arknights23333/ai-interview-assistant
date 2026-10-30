package com.assistant.aiinterviewassistant.modules.user.domain.dto;

import com.assistant.aiinterviewassistant.modules.user.domain.enums.MailCodeScene;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    /** 验证码用途：REGISTER、RESET_PASSWORD 或 CHANGE_PASSWORD。 */
    @NotNull(message = "请选择验证码用途")
    private MailCodeScene scene;
}
