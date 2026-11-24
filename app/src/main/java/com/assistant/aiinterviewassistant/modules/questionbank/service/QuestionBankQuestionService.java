package com.assistant.aiinterviewassistant.modules.questionbank.service;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.dto.CreateQuestionDto;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.dto.QuestionBankQueryDto;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.po.QuestionBankQuestionPo;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.vo.QuestionBankQuestionDetailVo;
import com.assistant.aiinterviewassistant.modules.questionbank.domain.vo.QuestionBankQuestionListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /** 分页查询本人指定知识库下的题目摘要。 */
    Page<QuestionBankQuestionListVo> listQuestions(Long userId, QuestionBankQueryDto dto, PageQuery pageQuery);

    /** 获取本人题目或修改草稿的完整内容。 */
    QuestionBankQuestionDetailVo getQuestionDetail(Long userId, Long questionId);
}
