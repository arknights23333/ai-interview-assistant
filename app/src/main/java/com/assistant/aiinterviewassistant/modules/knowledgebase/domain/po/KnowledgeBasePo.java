package com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@TableName("knowledge_base")
public class KnowledgeBasePo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private OffsetDateTime createAt;
    private OffsetDateTime updateAt;
}
