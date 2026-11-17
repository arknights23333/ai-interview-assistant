package com.assistant.aiinterviewassistant.modules.interview.service;

import com.assistant.aiinterviewassistant.modules.interview.domain.dto.FollowUpQuestionDraft;
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
     *
     * @param sessionId
     * @param questionId
     * @param answer
     * @return true 成功，false表示已答过，其余情况抛异常
     */
    boolean saveAnswer(Long sessionId , Long questionId,String answer);

    /**
     * 为本场面试追加一道追问。
     *
     * @param sessionId 所属面试会话 ID
     * @param parentQuestionId 父题 ID
     * @param draft 追问题干与评分材料
     * @return 新建或复用的本场追问
     */
    InterviewSessionQuestionPo appendFollowUpQuestion(
            Long sessionId, Long parentQuestionId, FollowUpQuestionDraft draft);
}
