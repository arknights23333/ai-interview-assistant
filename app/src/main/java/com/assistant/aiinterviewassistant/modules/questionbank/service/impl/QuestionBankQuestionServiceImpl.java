package com.assistant.aiinterviewassistant.modules.questionbank.service.impl;

import com.assistant.aiinterviewassistant.common.exception.enums.DocumentErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.QuestionBankErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.modules.knowledgebase.domain.po.DocumentPo;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.DocumentService;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.KnowledgeBaseService;
import com.assistant.aiinterviewassistant.modules.questionbank.constant.QuestionBankConstant;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.dto.CreateQuestionDto;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.po.QuestionBankQuestionPo;
import com.assistant.aiinterviewassistant.modules.questionbank.mapper.QuestionBankQuestionMapper;
import com.assistant.aiinterviewassistant.modules.questionbank.service.QuestionBankQuestionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

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
