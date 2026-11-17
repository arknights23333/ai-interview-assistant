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

    /**
     * 按照面试会话id和题目id查询题目
     * @param sessionId
     * @param questionId
     * @return
     */
    List<InterviewSessionQuestionPo> selectOneQeustion(Long sessionId,List<Long> questionId);

    /**
     * 按照面试会话id和题目id保存答案
     * @param sessionId
     * @param questionId
     * @param answer
     * @return
     */
    boolean saveAnswer(Long sessionId , Long questionId,String answer);
}
