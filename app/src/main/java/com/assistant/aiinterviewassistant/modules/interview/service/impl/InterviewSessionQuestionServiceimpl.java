package com.assistant.aiinterviewassistant.modules.interview.service.impl;

import com.assistant.aiinterviewassistant.common.exception.enums.InterviewErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.FollowUpQuestionDraft;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.MainQuestionDraft;
import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionQuestionPo;
import com.assistant.aiinterviewassistant.modules.interview.mapper.InterviewSessionQuestionMapper;
import com.assistant.aiinterviewassistant.modules.interview.service.InterviewSessionQuestionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSessionQuestionServiceimpl extends ServiceImpl<InterviewSessionQuestionMapper, InterviewSessionQuestionPo> implements InterviewSessionQuestionService {
    private final Validator validator;

    @Override
    @Transactional
    public Long saveMainQuestions(Long sessionId, List<MainQuestionDraft> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new BusinessException(InterviewErrorCode.INVALID_QUESTION);
        }

        List<InterviewSessionQuestionPo> records = new ArrayList<>(questions.size());
        int roundNo = 1;
        for (MainQuestionDraft draft : questions) {
            if (draft == null || !validator.validate(draft).isEmpty()) {
                throw new BusinessException(InterviewErrorCode.INVALID_QUESTION);
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

        Long firstQuestionId = records.getFirst().getId();
        if (!saved || firstQuestionId == null) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_SAVE_FAILED);
        }
        return firstQuestionId;
    }

    @Override
    public List<InterviewSessionQuestionPo> selectQuestions( Long sessionId, List<Long> questionId) {
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

    @Override
    public boolean saveAnswer(Long sessionId, Long questionId, String answer) {
        if(sessionId==null||questionId==null||sessionId<=0||questionId<=0){
            throw new BusinessException(InterviewErrorCode.INVALID_INTERVIEW_ID);
        }
        //答案不进行空值校验，提交空
        LambdaUpdateWrapper<InterviewSessionQuestionPo> wrapper = new LambdaUpdateWrapper<InterviewSessionQuestionPo>()
                .eq(InterviewSessionQuestionPo::getSessionId,sessionId)
                .eq(InterviewSessionQuestionPo::getId,questionId)
                .isNull(InterviewSessionQuestionPo::getAnsweredAt)
                .set(InterviewSessionQuestionPo::getAnsweredAt, OffsetDateTime.now());
        wrapper.set(InterviewSessionQuestionPo::getUserAnswer, Objects.requireNonNullElse(answer, ""));
        boolean result = update(wrapper);
        if(!result){
            InterviewSessionQuestionPo po = getOne(new LambdaQueryWrapper<InterviewSessionQuestionPo>()
                    .eq(InterviewSessionQuestionPo::getSessionId,sessionId)
                    .eq(InterviewSessionQuestionPo::getId,questionId));
            if(po==null){
                throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
            }
        }
        return result;
    }

    @Override
    public InterviewSessionQuestionPo appendFollowUpQuestion(
            Long sessionId, Long parentQuestionId, FollowUpQuestionDraft draft) {
        if (sessionId == null || parentQuestionId == null || sessionId <= 0 || parentQuestionId <= 0) {
            throw new BusinessException(InterviewErrorCode.INVALID_INTERVIEW_ID);
        }
        if (draft == null || !validator.validate(draft).isEmpty()) {
            throw new BusinessException(InterviewErrorCode.INVALID_QUESTION);
        }
        InterviewSessionQuestionPo parent = getOne(new LambdaQueryWrapper<InterviewSessionQuestionPo>()
                .eq(InterviewSessionQuestionPo::getSessionId,sessionId)
                .eq(InterviewSessionQuestionPo::getId,parentQuestionId));
        if(parent==null){
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
        }
        InterviewSessionQuestionPo po = getOne(new LambdaQueryWrapper<InterviewSessionQuestionPo>()
                .eq(InterviewSessionQuestionPo::getSessionId,sessionId)
                .eq(InterviewSessionQuestionPo::getRoundNo,parent.getRoundNo())
                .eq(InterviewSessionQuestionPo::getFollowUpNo,1));
        if(po!=null){
            log.info("已复用,追问题目id为：{}",po.getId());
            return po;
        }
        InterviewSessionQuestionPo save = new InterviewSessionQuestionPo();
        save.setSessionId(sessionId);
        save.setRoundNo(parent.getRoundNo());
        save.setFollowUpNo(1);
        save.setParentQuestionId(parent.getId());
        save.setCategory(parent.getCategory());
        save.setQuestion(draft.question());
        save.setReferenceAnswer(draft.referenceAnswer());
        save.setKeyPointsJson(draft.keyPointsJson());
        save.setScoringRubric(draft.scoringRubric());

        boolean saved;
        try {
            saved = save(save);
        } catch (DataAccessException e) {
            throw new BusinessException(
                    InterviewErrorCode.INTERVIEW_QUESTION_SAVE_FAILED, e);
        }

        if (!saved) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_SAVE_FAILED);
        }

        return save;

    }
}
