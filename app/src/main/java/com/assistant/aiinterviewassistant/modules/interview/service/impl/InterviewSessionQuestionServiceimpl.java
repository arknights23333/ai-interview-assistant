package com.assistant.aiinterviewassistant.modules.interview.service.impl;

import com.assistant.aiinterviewassistant.common.exception.enums.InterviewErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.MainQuestionDraft;
import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionQuestionPo;
import com.assistant.aiinterviewassistant.modules.interview.mapper.InterviewSessionQuestionMapper;
import com.assistant.aiinterviewassistant.modules.interview.service.InterviewSessionQuestionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewSessionQuestionServiceimpl extends ServiceImpl<InterviewSessionQuestionMapper, InterviewSessionQuestionPo> implements InterviewSessionQuestionService {
    private final Validator validator;

    @Override
    @Transactional
    public void saveMainQuestions(Long sessionId, List<MainQuestionDraft> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new BusinessException(InterviewErrorCode.INVALID_QUESTION_BATCH);
        }

        List<InterviewSessionQuestionPo> records = new ArrayList<>(questions.size());
        int roundNo = 1;
        for (MainQuestionDraft draft : questions) {
            if (draft == null || !validator.validate(draft).isEmpty()) {
                throw new BusinessException(InterviewErrorCode.INVALID_QUESTION_BATCH);
            }

            // 只复制题目材料，本场主问题从未作答状态开始。
            InterviewSessionQuestionPo question = new InterviewSessionQuestionPo();
            question.setSessionId(sessionId);
            question.setRoundNo(roundNo++);
            question.setFollowUpNo(0);
            question.setQuestion(draft.question());
            question.setCategory(draft.category());
            question.setReferenceAnswer(draft.referenceAnswer());
            question.setKeyPointsJson(draft.keyPointsJson());
            question.setScoringRubric(draft.scoringRubric());
            question.setFollowUpCandidatesJson(draft.followUpCandidatesJson());
            records.add(question);
        }

        boolean saved;
        try {
            saved = saveBatch(records);
        } catch (DataAccessException e) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_SAVE_FAILED, e);
        }

        if (!saved) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_SAVE_FAILED);
        }
    }

    @Override
    public List<InterviewSessionQuestionPo> selectOneQeustion( Long sessionId, List<Long> questionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new BusinessException(InterviewErrorCode.INVALID_INTERVIEW_ID);
        }
        if (questionId != null && questionId.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<InterviewSessionQuestionPo> wrapper = new LambdaQueryWrapper<InterviewSessionQuestionPo>()
                .eq(InterviewSessionQuestionPo::getSessionId,sessionId)
                .in(questionId!=null,InterviewSessionQuestionPo::getId,questionId)
                .orderByAsc(
                        InterviewSessionQuestionPo::getRoundNo,
                        InterviewSessionQuestionPo::getFollowUpNo
                );
        return list(wrapper);
    }
}
