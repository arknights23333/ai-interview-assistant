package com.assistant.aiinterviewassistant.modules.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 修改当前用户昵称的请求。 */
@Data
public class UpdateNicknameDto {
    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称不能超过64个字符")
    private String nickname;
}
