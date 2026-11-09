package com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/** 知识库列表的筛选条件。 */
@Data
public class KnowledgeBaseQueryDto {

    /** 按知识库名称模糊搜索。 */
    @Nullable
    @Size(max = 255, message = "知识库名称关键词不能超过255个字符")
    private String name;
}
