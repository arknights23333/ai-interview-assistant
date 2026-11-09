package com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建或重命名知识库的请求参数。 */
@Data
public class KnowledgeBaseNameDto {

    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 255, message = "知识库名称不能超过255个字符")
    private String name;
}
