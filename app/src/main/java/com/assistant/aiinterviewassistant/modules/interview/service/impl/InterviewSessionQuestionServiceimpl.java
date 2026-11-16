package com.assistant.aiinterviewassistant.modules.interview.service.impl;

import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionQuestionPo;
import com.assistant.aiinterviewassistant.modules.interview.mapper.InterviewSessionQuestionMapper;
import com.assistant.aiinterviewassistant.modules.interview.service.InterviewSessionQuestionService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewSessionQuestionServiceimpl extends ServiceImpl<InterviewSessionQuestionMapper, InterviewSessionQuestionPo> implements InterviewSessionQuestionService {
    @Override
    public boolean saveMainQuestions(Long sessionId, List<InterviewSessionQuestionPo> questions) {
        return false;
    }
}
