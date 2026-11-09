package com.assistant.aiinterviewassistant.modules.knowledgebase.service.impl;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseNameDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseQueryDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.KnowledgeBaseVo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.KnowledgeBaseService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    @Override
    public Long createKnowledgeBase(Long userId, KnowledgeBaseNameDto dto) {
        return null;
    }

    @Override
    public void updateKnowledgeBaseName(Long userId, Long id, KnowledgeBaseNameDto dto) {
    }

    @Override
    public Page<KnowledgeBaseVo> listKnowledgeBases(Long userId, KnowledgeBaseQueryDto dto, PageQuery pageQuery) {
        return null;
    }
}
