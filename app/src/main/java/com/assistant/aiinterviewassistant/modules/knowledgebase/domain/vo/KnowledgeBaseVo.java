package com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo;

import lombok.Data;

import java.time.OffsetDateTime;

/** 知识库列表中的展示信息。 */
@Data
public class KnowledgeBaseVo {

    private Long id;
    private String name;
    private OffsetDateTime createAt;
    private OffsetDateTime updateAt;
}
