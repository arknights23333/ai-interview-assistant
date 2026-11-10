package com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo;

import lombok.Data;

import java.time.OffsetDateTime;

/** 知识库文档列表中的展示信息。 */
@Data
public class DocumentVo {

    private Long id;
    private String originalFilename;

    /** 原始文件大小，单位为字节。 */
    private Long fileSize;

    /** 文档记录创建时间。 */
    private OffsetDateTime createAt;

    private String processStatus;
}
