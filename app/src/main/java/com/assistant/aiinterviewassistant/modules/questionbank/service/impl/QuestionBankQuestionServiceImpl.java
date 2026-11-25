package com.assistant.aiinterviewassistant.modules.questionbank.service.impl;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.exception.enums.DocumentErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.QuestionBankErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.DocumentPo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.DocumentService;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.KnowledgeBaseService;
import com.assistant.aiinterviewassistant.modules.questionbank.constant.QuestionBankConstant;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.dto.CreateDraftDto;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.dto.CreateQuestionDto;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.dto.QuestionBankQueryDto;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.po.QuestionBankQuestionPo;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.vo.QuestionBankQuestionDetailVo;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.vo.QuestionBankQuestionListVo;
import com.assistant.aiinterviewassistant.modules.questionbank.mapper.QuestionBankQuestionMapper;
import com.assistant.aiinterviewassistant.modules.questionbank.service.QuestionBankQuestionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** 知识库题库题目服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionBankQuestionServiceImpl
        extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestionPo>
        implements QuestionBankQuestionService {

    private final KnowledgeBaseService knowledgeBaseService;
    private final DocumentService documentService;

    private final ObjectMapper objectMapper;

    @Override
    public Long create(CreateQuestionDto dto, Long userId) {
        QuestionBankQuestionPo po = saveQuestion(dto, QuestionBankConstant.CreationType.MANUAL,userId);
        return po.getId();
    }

    @Override
    public Page<QuestionBankQuestionListVo> listQuestions(Long userId, QuestionBankQueryDto dto, PageQuery pageQuery) {
        Long knowledgeBaseId = dto.getKnowledgeBaseId();
        knowledgeBaseService.requireOwnedKnowledgeBase(knowledgeBaseId, userId);

        String jobDirection = dto.getJobDirection();
        String difficulty = dto.getDifficulty();
        String status = dto.getStatus();
        LambdaQueryWrapper<QuestionBankQuestionPo> wrapper = new LambdaQueryWrapper<QuestionBankQuestionPo>()
                .select(QuestionBankQuestionPo::getId,
                        QuestionBankQuestionPo::getKnowledgeBaseId,
                        QuestionBankQuestionPo::getSourceDocumentId,
                        QuestionBankQuestionPo::getOriginalQuestionId,
                        QuestionBankQuestionPo::getCreationType,
                        QuestionBankQuestionPo::getStatus,
                        QuestionBankQuestionPo::getQuestion,
                        QuestionBankQuestionPo::getJobDirection,
                        QuestionBankQuestionPo::getDifficulty,
                        QuestionBankQuestionPo::getCategory,
                        QuestionBankQuestionPo::getCreatedAt,
                        QuestionBankQuestionPo::getUpdatedAt)
                .eq(QuestionBankQuestionPo::getKnowledgeBaseId, knowledgeBaseId)
                .like(StringUtils.hasText(jobDirection), QuestionBankQuestionPo::getJobDirection, jobDirection)
                .eq(difficulty != null, QuestionBankQuestionPo::getDifficulty, difficulty)
                .eq(status != null, QuestionBankQuestionPo::getStatus, status)
                .orderByDesc(QuestionBankQuestionPo::getUpdatedAt, QuestionBankQuestionPo::getId);
        Page<QuestionBankQuestionPo> questions = page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()), wrapper);
        Page<QuestionBankQuestionListVo> result = new Page<>(questions.getCurrent(), questions.getSize(), questions.getTotal());
        result.setRecords(questions.getRecords().stream().map(question -> {
            QuestionBankQuestionListVo vo = new QuestionBankQuestionListVo();
            BeanUtils.copyProperties(question, vo);
            return vo;
        }).toList());
        return result;
    }

    @Override
    public QuestionBankQuestionDetailVo getQuestionDetail(Long userId, Long questionId) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(QuestionBankErrorCode.INVALID_QUESTION_ID);
        }

        QuestionBankQuestionPo question = getOne(new LambdaQueryWrapper<QuestionBankQuestionPo>()
                .eq(QuestionBankQuestionPo::getId, questionId)
                .apply("""
                        EXISTS (
                            SELECT 1 FROM knowledge_base kb
                            WHERE kb.id = question_bank_question.knowledge_base_id
                              AND kb.user_id = {0}
                        )
                        """, userId));
        if (question == null) {
            throw new BusinessException(QuestionBankErrorCode.QUESTION_NOT_FOUND);
        }

        QuestionBankQuestionDetailVo detail = new QuestionBankQuestionDetailVo();
        BeanUtils.copyProperties(question, detail);
        detail.setKeyPoints(objectMapper.readValue(question.getKeyPointsJson(), new TypeReference<List<String>>() {}));
        return detail;
    }

    @Override
    public List<Long> deleteBatch(Long userId, Long knowledgeBaseId, List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException(QuestionBankErrorCode.INVALID_QUESTION_ID);
        }
        knowledgeBaseService.requireOwnedKnowledgeBase(knowledgeBaseId, userId);

        List<Long> targetIds = ids.stream().distinct().toList();
        List<Long> removedIds;
        try {
            removedIds = getBaseMapper().deleteBatchAndReturnIds(userId, knowledgeBaseId, targetIds);
        } catch (DataAccessException e) {
            throw new BusinessException(QuestionBankErrorCode.QUESTION_DELETE_FAILED, e);
        }

        Set<Long> removedIdSet = new HashSet<>(removedIds);
        List<Long> undeletedIds = targetIds.stream().filter(id -> !removedIdSet.contains(id)).toList();
        log.info("题库批量删除完成，knowledgeBaseId={} userId={} requested={} deleted={} undeleted={}",
                knowledgeBaseId, userId, targetIds.size(), removedIdSet.size(), undeletedIds.size());
        return undeletedIds;
    }



    /**
     * 保存一条完整的新题。提供来源文档时，文档必须属于题目所在的知识库。
     *
     * @param dto 新题内容及所属知识库
     * @param createType 调用方指定的创建方式
     * @param userId 发起创建的用户 ID
     * @return 保存后的题目记录
     */
    public QuestionBankQuestionPo saveQuestion(CreateQuestionDto dto, String createType, Long userId) {
        Long knowledgeBaseId = dto.getKnowledgeBaseId();
        knowledgeBaseService.requireOwnedKnowledgeBase(knowledgeBaseId, userId);

        Long sourceDocumentId = dto.getSourceDocumentId();
        if (sourceDocumentId != null && documentService.count(new LambdaQueryWrapper<DocumentPo>()
                .eq(DocumentPo::getId, sourceDocumentId)
                .eq(DocumentPo::getKnowledgeBaseId, knowledgeBaseId)) == 0) {
            throw new BusinessException(DocumentErrorCode.DOCUMENT_NOT_FOUND);
        }

        QuestionBankQuestionPo po = new QuestionBankQuestionPo();
        po.setKnowledgeBaseId(knowledgeBaseId);
        po.setSourceDocumentId(sourceDocumentId);
        po.setCreationType(createType);
        po.setStatus(QuestionBankConstant.Status.ACTIVE);
        po.setQuestion(dto.getQuestion());
        if(dto.getJobDirection()!=null){
            po.setJobDirection(dto.getJobDirection());
        }
        if(dto.getDifficulty()!=null){
            po.setDifficulty(dto.getDifficulty());
        }
        if(dto.getCategory()!=null){
            po.setCategory(dto.getCategory());
        }
        if(dto.getReferenceAnswer()!=null){
            po.setReferenceAnswer(dto.getReferenceAnswer());
        }
        po.setKeyPointsJson(objectMapper.writeValueAsString(dto.getKeyPoints()));

        if(dto.getScoringRubric()!=null){
            po.setScoringRubric(dto.getScoringRubric());
        }
        if(!save(po)){
            throw new BusinessException(QuestionBankErrorCode.QUESTION_SAVE_FAILED);
        }
        return po;
    }
    @Override
    public QuestionBankQuestionDetailVo createDraft(CreateDraftDto dto, Long userId) {
        Long questionId = dto.getOriginalQuestionId();
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(QuestionBankErrorCode.INVALID_QUESTION_ID);
        }

        QuestionBankQuestionPo original = getOne(new LambdaQueryWrapper<QuestionBankQuestionPo>()
                .eq(QuestionBankQuestionPo::getId, questionId)
                .apply("""
                        EXISTS (
                            SELECT 1 FROM knowledge_base kb
                            WHERE kb.id = question_bank_question.knowledge_base_id
                              AND kb.user_id = {0}
                        )
                        """, userId));
        if (original == null) {
            throw new BusinessException(QuestionBankErrorCode.QUESTION_NOT_FOUND);
        }
        if (!QuestionBankConstant.Status.ACTIVE.equals(original.getStatus())) {
            throw new BusinessException(QuestionBankErrorCode.QUESTION_STATE_CONFLICT);
        }
        if (count(new LambdaQueryWrapper<QuestionBankQuestionPo>()
                .eq(QuestionBankQuestionPo::getOriginalQuestionId, questionId)) > 0) {
            throw new BusinessException(QuestionBankErrorCode.QUESTION_DRAFT_EXISTS);
        }

        QuestionBankQuestionPo draft = new QuestionBankQuestionPo();
        draft.setKnowledgeBaseId(original.getKnowledgeBaseId());
        draft.setOriginalQuestionId(original.getId());
        draft.setCreationType(original.getCreationType());
        draft.setStatus(QuestionBankConstant.Status.DRAFT);

        setDraftQuestion(draft, dto.getQuestion(), original.getQuestion());
        setDraftJobDirection(draft, dto.getJobDirection(), original.getJobDirection());
        setDraftDifficulty(draft, dto.getDifficulty(), original.getDifficulty());
        setDraftCategory(draft, dto.getCategory(), original.getCategory());
        setDraftReferenceAnswer(draft, dto.getReferenceAnswer(), original.getReferenceAnswer());
        setDraftKeyPoints(draft, dto.getKeyPointsJson(), original.getKeyPointsJson());
        setDraftScoringRubric(draft, dto.getScoringRubric(), original.getScoringRubric());
        setDraftSourceDocument(draft, dto.getSourceDocumentId(), original);

        boolean saved;
        try {
            saved = save(draft);
        } catch (DataAccessException e) {
            throw new BusinessException(QuestionBankErrorCode.QUESTION_SAVE_FAILED, e);
        }
        if (!saved) {
            throw new BusinessException(QuestionBankErrorCode.QUESTION_SAVE_FAILED);
        }
        return getQuestionDetail(userId, draft.getId());
    }

    private void setDraftQuestion(QuestionBankQuestionPo draft, String value, String fallback) {
        if (value == null) {
            draft.setQuestion(fallback);
        } else if (!StringUtils.hasText(value)) {
            draft.setQuestion(QuestionBankConstant.DraftPlaceholder.QUESTION);
        } else {
            draft.setQuestion(value);
        }
    }

    private void setDraftJobDirection(QuestionBankQuestionPo draft, String value, String fallback) {
        if (value == null) {
            draft.setJobDirection(fallback);
            return;
        }
        if (!StringUtils.hasText(value)) {
            draft.setJobDirection(null);
            return;
        }
        validateShortTextField(value);
        draft.setJobDirection(value);
    }

    private void setDraftDifficulty(QuestionBankQuestionPo draft, String value, String fallback) {
        if (value == null) {
            draft.setDifficulty(fallback);
            return;
        }
        if (!StringUtils.hasText(value)) {
            draft.setDifficulty(null);
            return;
        }
        if (!Pattern.matches(QuestionBankConstant.Difficulty.VALID_PATTERN, value)) {
            throw new BusinessException(QuestionBankErrorCode.INVALID_DIFFICULTY);
        }
        draft.setDifficulty(value);
    }

    private void setDraftCategory(QuestionBankQuestionPo draft, String value, String fallback) {
        if (value == null) {
            draft.setCategory(fallback);
            return;
        }
        if (!StringUtils.hasText(value)) {
            draft.setCategory(null);
            return;
        }
        validateShortTextField(value);
        draft.setCategory(value);
    }

    private void setDraftReferenceAnswer(QuestionBankQuestionPo draft, String value, String fallback) {
        if (value == null) {
            draft.setReferenceAnswer(fallback);
        } else if (!StringUtils.hasText(value)) {
            draft.setReferenceAnswer(null);
        } else {
            draft.setReferenceAnswer(value);
        }
    }

    private void setDraftScoringRubric(QuestionBankQuestionPo draft, String value, String fallback) {
        if (value == null) {
            draft.setScoringRubric(fallback);
        } else if (!StringUtils.hasText(value)) {
            draft.setScoringRubric(null);
        } else {
            draft.setScoringRubric(value);
        }
    }

    private void setDraftKeyPoints(QuestionBankQuestionPo draft, String value, String fallback) {
        if (value == null) {
            draft.setKeyPointsJson(fallback);
            return;
        }
        if (!StringUtils.hasText(value)) {
            draft.setKeyPointsJson(QuestionBankConstant.DraftPlaceholder.EMPTY_KEY_POINTS_JSON);
            return;
        }

        List<String> keyPoints;
        try {
            keyPoints = objectMapper.readValue(value, new TypeReference<List<String>>() {});
        } catch (RuntimeException e) {
            throw new BusinessException(QuestionBankErrorCode.INVALID_KEY_POINTS, e);
        }
        if (keyPoints == null || keyPoints.stream().anyMatch(point -> !StringUtils.hasText(point))) {
            throw new BusinessException(QuestionBankErrorCode.INVALID_KEY_POINTS);
        }
        try {
            draft.setKeyPointsJson(objectMapper.writeValueAsString(keyPoints));
        } catch (RuntimeException e) {
            throw new BusinessException(QuestionBankErrorCode.QUESTION_SAVE_FAILED, e);
        }
    }

    private void setDraftSourceDocument(QuestionBankQuestionPo draft, Long sourceDocumentId,
                                         QuestionBankQuestionPo original) {
        if (sourceDocumentId == null) {
            draft.setSourceDocumentId(original.getSourceDocumentId());
            return;
        }
        if (sourceDocumentId <= 0 || documentService.count(new LambdaQueryWrapper<DocumentPo>()
                .eq(DocumentPo::getId, sourceDocumentId)
                .eq(DocumentPo::getKnowledgeBaseId, original.getKnowledgeBaseId())) == 0) {
            throw new BusinessException(DocumentErrorCode.DOCUMENT_NOT_FOUND);
        }
        draft.setSourceDocumentId(sourceDocumentId);
    }

    private void validateShortTextField(String value) {
        if (value.length() > 64) {
            throw new BusinessException(QuestionBankErrorCode.QUESTION_FIELD_TOO_LONG);
        }
    }
}
