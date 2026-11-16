package com.assistant.aiinterviewassistant.modules.interview.service;

import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionQuestionPo;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

public interface InterviewSessionQuestionService extends IService<InterviewSessionQuestionPo> {
    /**
     * 批量保存面试会话题目
     * @param sessionId 会话id
     * @param questions 问题
     * @return
     */
    boolean saveMainQuestions(Long sessionId, List<InterviewSessionQuestionPo> questions);
}
