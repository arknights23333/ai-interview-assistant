package com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@TableName("document")
public class DocumentPo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long knowledgeBaseId;
    private String originalFilename;
    private String fileHash;
    private String storageKey;
    private Long fileSize;
    private String contentType;
    private String processStatus;
    /** 文档处理失败原因，可为空。 */
    private String processError;
    private OffsetDateTime createAt;
    private OffsetDateTime updateAt;
}
