package com.assistant.aiinterviewassistant.modules.knowledgebase.service;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseNameDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseQueryDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.KnowledgeBaseVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface KnowledgeBaseService {

    Long createKnowledgeBase(Long userId, KnowledgeBaseNameDto dto);

    void updateKnowledgeBaseName(Long userId, Long id, KnowledgeBaseNameDto dto);

    Page<KnowledgeBaseVo> listKnowledgeBases(Long userId, KnowledgeBaseQueryDto dto, PageQuery pageQuery);
}
