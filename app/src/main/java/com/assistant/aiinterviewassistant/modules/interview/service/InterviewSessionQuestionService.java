package com.assistant.aiinterviewassistant.modules.interview.service;

import com.assistant.aiinterviewassistant.modules.interview.domain.dto.MainQuestionDraft;
import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionQuestionPo;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

public interface InterviewSessionQuestionService extends IService<InterviewSessionQuestionPo> {
    /**
     * 按调用方给出的顺序批量保存本场主问题。
     *
     * @param sessionId 所属面试会话 ID
     * @param questions 已排序的主问题草稿
     */
    void saveMainQuestions(Long sessionId, List<MainQuestionDraft> questions);
}
