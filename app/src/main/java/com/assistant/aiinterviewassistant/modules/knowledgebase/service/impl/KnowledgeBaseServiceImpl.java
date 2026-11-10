package com.assistant.aiinterviewassistant.modules.knowledgebase.service.impl;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.exception.enums.KnowledgeBaseErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseNameDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseQueryDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.KnowledgeBasePo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.KnowledgeBaseVo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.mapper.KnowledgeBaseMapper;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.KnowledgeBaseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;


@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBasePo> implements KnowledgeBaseService {

    @Override
    public Long createKnowledgeBase(Long userId, KnowledgeBaseNameDto dto) {
        KnowledgeBasePo po = new KnowledgeBasePo();
        po.setUserId(userId);
        po.setName(dto.getName());
        if (!save(po)) {
            throw new BusinessException(KnowledgeBaseErrorCode.KNOWLEDGE_BASE_CREATE_FAILED);
        }
        return po.getId();
    }

    @Override
    public void updateKnowledgeBaseName(Long userId, Long id, KnowledgeBaseNameDto dto) {
        boolean updated = update(new LambdaUpdateWrapper<KnowledgeBasePo>()
                .eq(KnowledgeBasePo::getId, id)
                .eq(KnowledgeBasePo::getUserId, userId)
                .set(KnowledgeBasePo::getName, dto.getName())
                .set(KnowledgeBasePo::getUpdateAt, OffsetDateTime.now()));
        if (!updated) {
            throw new BusinessException(KnowledgeBaseErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
    }

    @Override
    public Page<KnowledgeBaseVo> listKnowledgeBases(Long userId, KnowledgeBaseQueryDto dto, PageQuery pageQuery) {
        Page<KnowledgeBasePo> page = new Page<>(pageQuery.getPageNum(),pageQuery.getPageSize());
        String name = dto.getName();
        LambdaQueryWrapper<KnowledgeBasePo> wrapper = new LambdaQueryWrapper<KnowledgeBasePo>()
                .eq(KnowledgeBasePo::getUserId,userId)
                .like(name != null && !name.isBlank(), KnowledgeBasePo::getName, name)
                .orderByAsc(KnowledgeBasePo::getId);

        Page<KnowledgeBasePo> pos = page(page,wrapper);
        List<KnowledgeBaseVo> list = pos.getRecords().stream().map(
                po -> {
                    KnowledgeBaseVo vo = new KnowledgeBaseVo();
                    BeanUtils.copyProperties(po, vo);
                    return vo;
                }
        ).toList();
        Page<KnowledgeBaseVo> result = new Page<>(pos.getCurrent(), pos.getSize(), pos.getTotal());
        result.setRecords(list);
        return result ;
    }
}
