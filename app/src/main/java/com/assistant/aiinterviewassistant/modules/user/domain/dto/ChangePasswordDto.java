package com.assistant.aiinterviewassistant.modules.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.ToString;

public class ChangePasswordDto {
    private String oldPassword;
    private String code;
    @ToString.Exclude
    @NotBlank(message = "新密码不能为空")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*[0-9])[\\x21-\\x7E]{8,64}$",
            message = "密码须为8～64位，至少包含字母和数字，且不能包含空格或中文"
    )
    private String newPassword;
}
