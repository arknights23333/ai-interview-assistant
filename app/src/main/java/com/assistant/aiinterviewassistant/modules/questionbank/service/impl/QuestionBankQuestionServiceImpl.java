package com.assistant.aiinterviewassistant.modules.questionbank.service.impl;

import com.assistant.aiinterviewassistant.modules.questionbank.domain.po.QuestionBankQuestionPo;
import com.assistant.aiinterviewassistant.modules.questionbank.mapper.QuestionBankQuestionMapper;
import com.assistant.aiinterviewassistant.modules.questionbank.service.QuestionBankQuestionService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/** 知识库题库题目服务实现。 */
@Service
public class QuestionBankQuestionServiceImpl
        extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestionPo>
        implements QuestionBankQuestionService {
}
