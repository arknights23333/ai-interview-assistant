package com.assistant.aiinterviewassistant.modules.knowledgebase.service;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseNameDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseQueryDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.KnowledgeBaseVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface KnowledgeBaseService {
    /**
     * 新增知识库
     * @param userId
     * @param dto
     * @return
     */
    Long createKnowledgeBase(Long userId, KnowledgeBaseNameDto dto);

    /**
     * 修改知识库名字
     * @param userId
     * @param id
     * @param dto
     */
    void updateKnowledgeBaseName(Long userId, Long id, KnowledgeBaseNameDto dto);

    /**
     * 查询知识库列表
     * @param userId
     * @param dto
     * @param pageQuery
     * @return
     */
    Page<KnowledgeBaseVo> listKnowledgeBases(Long userId, KnowledgeBaseQueryDto dto, PageQuery pageQuery);
}
