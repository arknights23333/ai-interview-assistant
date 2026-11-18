package com.assistant.aiinterviewassistant.modules.knowledgebase.service.impl;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.exception.enums.KnowledgeBaseErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.infrastructure.storage.domain.StorageDeleteModels;
import com.assistant.aiinterviewassistant.infrastructure.storage.service.FileStorageService;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseNameDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.dto.KnowledgeBaseQueryDto;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.DocumentPo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.KnowledgeBasePo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.vo.KnowledgeBaseVo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.mapper.DocumentMapper;
import com.assistant.aiinterviewassistant.modules.knowledgebase.mapper.KnowledgeBaseMapper;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.KnowledgeBaseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBasePo> implements KnowledgeBaseService {

    private final DocumentMapper documentMapper;
    private final FileStorageService fileStorageService;

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
    public void requireOwnedKnowledgeBase(Long knowledgeBaseId, Long userId) {
        boolean owned = count(new LambdaQueryWrapper<KnowledgeBasePo>()
                .eq(KnowledgeBasePo::getId, knowledgeBaseId)
                .eq(KnowledgeBasePo::getUserId, userId)) > 0;
        if (!owned) {
            throw new BusinessException(KnowledgeBaseErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeBase(Long userId, Long knowledgeBaseId) {
        requireOwnedKnowledgeBase(knowledgeBaseId, userId);

        // 外键限制要求先删除文档记录，再删除知识库。
        List<DocumentPo> documents = documentMapper.deleteDocumentByKnowledgeBaseAndReturn(userId, knowledgeBaseId);
        try {
            if (getBaseMapper().deleteById(knowledgeBaseId) == 0) {
                throw new BusinessException(KnowledgeBaseErrorCode.KNOWLEDGE_BASE_DELETE_FAILED);
            }
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(KnowledgeBaseErrorCode.KNOWLEDGE_BASE_DELETE_FAILED, e);
        }

        if (documents.isEmpty()) {
            return;
        }

        Map<String, Long> idByKey = documents.stream()
                .collect(Collectors.toMap(DocumentPo::getStorageKey, DocumentPo::getId));

        // 数据库回滚时保留原文件，只在事务提交后清理对象存储。
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cleanupDocumentFiles(knowledgeBaseId, userId, idByKey);
            }
        });
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

    private void cleanupDocumentFiles(Long knowledgeBaseId, Long userId, Map<String, Long> idByKey) {
        try {
            StorageDeleteModels.BatchDeleteResult<Long> result = fileStorageService.fileDeleteBatch(idByKey);
            log.info("整库删除后原文件清理完成，knowledgeBaseId={} userId={} deleted={} failed={}",
                    knowledgeBaseId, userId, result.successIds().size(), result.failures().size());
            for (var failure : result.failures()) {
                log.warn("文档原文件待补删 knowledgeBaseId={} documentId={} storageKey={} code={} s3Code={} reason={}",
                        knowledgeBaseId, failure.id(), failure.key(), failure.code(), failure.s3Code(),
                        failure.errorMessage());
            }
        } catch (RuntimeException e) {
            log.error("文档源文件清理异常，knowledgeBaseId={} 待补删对象及文档 ID={}",
                    knowledgeBaseId, idByKey, e);
        }
    }
}
