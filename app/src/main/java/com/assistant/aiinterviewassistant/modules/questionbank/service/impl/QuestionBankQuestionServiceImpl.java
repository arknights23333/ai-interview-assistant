package com.assistant.aiinterviewassistant.modules.questionbank.service.impl;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.exception.enums.DocumentErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.QuestionBankErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.DocumentPo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.DocumentService;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.KnowledgeBaseService;
import com.assistant.aiinterviewassistant.modules.questionbank.constant.QuestionBankConstant;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

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
}
