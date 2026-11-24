package com.assistant.aiinterviewassistant.modules.questionbank.service;

import com.assistant.aiinterviewassistant.modules.questionbank.domain.dto.CreateQuestionDto;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.po.QuestionBankQuestionPo;
import com.baomidou.mybatisplus.spring.service.IService;
import jakarta.validation.Valid;

/** 知识库题库题目服务。 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestionPo> {

    /**
     * 手动创建完整题目，以 {@code MANUAL} 记录来源，保存成功后直接启用。
     *
     * @param dto 新题内容
     * @param userId 当前用户 ID
     * @return 新题 ID
     */
    Long create(@Valid CreateQuestionDto dto,  Long userId);
}
