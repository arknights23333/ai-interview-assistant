package com.assistant.aiinterviewassistant.modules.interview.service.impl;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.exception.enums.InterviewErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.modules.interview.constant.InterviewConstant;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.FollowUpQuestionDraft;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.InterviewSessionQueryDto;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.SessionCreateDto;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.SubmitInterviewAnswerDto;
import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionPo;
import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionQuestionPo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewQuestionVo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewSessionDetailVo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewSessionSummaryVo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.SubmitInterviewAnswerVo;
import com.assistant.aiinterviewassistant.modules.interview.mapper.InterviewSessionMapper;
import com.assistant.aiinterviewassistant.modules.interview.service.InterviewSessionQuestionService;
import com.assistant.aiinterviewassistant.modules.interview.service.InterviewSessionService;
import com.assistant.aiinterviewassistant.modules.knowledgebase.service.KnowledgeBaseService;
import com.assistant.aiinterviewassistant.modules.resume.service.ResumeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSessionServiceImpl extends ServiceImpl<InterviewSessionMapper, InterviewSessionPo>
        implements InterviewSessionService {

    private final InterviewSessionQuestionService questionService;
    private final ResumeService resumeService;
    private final KnowledgeBaseService knowledgeBaseService;

    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;
    @Override
    public Page<InterviewSessionSummaryVo> listInterviewSessions(
            Long userId, InterviewSessionQueryDto dto, PageQuery pageQuery) {
        Page<InterviewSessionPo> page = new Page<InterviewSessionPo>(pageQuery.getPageNum(),pageQuery.getPageSize());
        LambdaQueryWrapper<InterviewSessionPo> wrapper = new LambdaQueryWrapper<InterviewSessionPo> ();
        wrapper.eq(InterviewSessionPo::getUserId, userId);
        wrapper.eq(dto.getSourceType()!=null,InterviewSessionPo::getSourceType,dto.getSourceType());
        String jobDirection = dto.getJobDirection();
        wrapper.like(
                jobDirection != null && !jobDirection.isBlank(),
                InterviewSessionPo::getJobDirection,
                jobDirection
        );
        wrapper.eq(dto.getStatus()!=null,InterviewSessionPo::getStatus,dto.getStatus());
        wrapper.ge(dto.getCreatedFrom()!=null,InterviewSessionPo::getCreatedAt,dto.getCreatedFrom());
        wrapper.lt(dto.getCreatedTo()!=null,InterviewSessionPo::getCreatedAt,dto.getCreatedTo());
        wrapper.orderByDesc(InterviewSessionPo::getCreatedAt, InterviewSessionPo::getId);

        Page<InterviewSessionPo> pagePo = page(page,wrapper);

        List<InterviewSessionSummaryVo> resultList = pagePo.getRecords().stream().map(
                po -> {
                    InterviewSessionSummaryVo vo = new InterviewSessionSummaryVo();
                    BeanUtils.copyProperties(po, vo);
                    return vo;
                }
        ).toList();
        Page<InterviewSessionSummaryVo> result = new Page<InterviewSessionSummaryVo>( pagePo.getCurrent(),
                pagePo.getSize(),
                pagePo.getTotal());
        result.setRecords(resultList);
       return result;
    }

    @Override
    public InterviewSessionDetailVo getInterviewSessionDetail(Long sessionId, Long userId) {
        if(sessionId==null||sessionId<=0){
            throw new BusinessException(InterviewErrorCode.INVALID_INTERVIEW_ID);
        }
        InterviewSessionPo po = getOne(new LambdaQueryWrapper<InterviewSessionPo>()
                .eq(InterviewSessionPo::getId,sessionId)
                .eq(InterviewSessionPo::getUserId,userId));
        if(po==null){
            throw new BusinessException(InterviewErrorCode.INTERVIEW_SESSION_NOT_FOUND);
        }
        InterviewSessionSummaryVo summary = new InterviewSessionSummaryVo();
        summary.setSourceType(po.getSourceType());
        summary.setJobDirection(po.getJobDirection());
        summary.setDifficulty(po.getDifficulty());
        summary.setMainQuestionCount(po.getMainQuestionCount());
        summary.setStatus(po.getStatus());
        summary.setCreatedAt(po.getCreatedAt());
        summary.setCompletedAt(po.getCompletedAt());
        summary.setId(po.getId());

        InterviewSessionDetailVo result = new InterviewSessionDetailVo();
        result.setSession(summary);
        result.setResumeId(po.getResumeId());
        result.setJobDescription(po.getJobDescription());
        result.setKnowledgeBaseId(po.getKnowledgeBaseId());



        //设置会话下全部题目
        List<InterviewSessionQuestionPo> questionsPo = questionService.selectQuestions(sessionId, null);
        List<InterviewQuestionVo> questionVos = questionsPo.stream()
                .map(po2 -> {
                    InterviewQuestionVo vo = new InterviewQuestionVo();
                    BeanUtils.copyProperties(po2, vo);

                    String keyPointsJson = po2.getKeyPointsJson();
                    if (keyPointsJson != null) {
                        vo.setKeyPoints(objectMapper.readValue(
                                keyPointsJson, new TypeReference<List<String>>() {}));
                    }

                    return vo;
                })
                .toList();
        result.setQuestions(questionVos);
        //设置当前题目
        Long currentQuestionId = po.getCurrentQuestionId();
        if (currentQuestionId != null) {
            result.setCurrentQuestion(questionVos.stream()
                    .filter(question -> currentQuestionId.equals(question.getId()))
                    .findFirst()
                    .orElse(null));
        }


        return result;
    }

    @Override
    @Transactional
    public Long createSession(Long userId, SessionCreateDto dto) {
        if (InterviewConstant.SourceType.NORMAL.equals(dto.getSourceType())
                && dto.getKnowledgeBaseId() != null) {
            throw new BusinessException(InterviewErrorCode.INVALID_INTERVIEW_SOURCE);
        }

        if (InterviewConstant.SourceType.KNOWLEDGE_BASE.equals(dto.getSourceType())
                && dto.getKnowledgeBaseId() == null) {
            throw new BusinessException(InterviewErrorCode.INVALID_INTERVIEW_SOURCE);
        }
        Long resumeId = dto.getResumeId();
        if (resumeId != null) {
            resumeService.getAvailableResumeOrThrow(resumeId, userId);
        }
        Long knowledgeBaseId = dto.getKnowledgeBaseId();
        if (knowledgeBaseId != null) {
            knowledgeBaseService.requireOwnedKnowledgeBase(knowledgeBaseId, userId);
        }

        InterviewSessionPo po = new InterviewSessionPo();
        po.setSourceType(dto.getSourceType());
        po.setJobDirection(dto.getJobDirection());
        po.setDifficulty(dto.getDifficulty());
        po.setResumeId(resumeId);
        po.setKnowledgeBaseId(knowledgeBaseId);
        po.setJobDescription(dto.getJobDescription());
        po.setUserId(userId);
        po.setMainQuestionCount(dto.getQuestions().size());

        if (!save(po)) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_SESSION_SAVE_FAILED);
        }

        Long sessionId = po.getId();
        Long firstQuestionId = questionService.saveMainQuestions(sessionId, dto.getQuestions());

        // 先保存题目，再设置当前题，满足当前题必须属于本会话的外键约束。
        boolean updated = update(new LambdaUpdateWrapper<InterviewSessionPo>()
                .eq(InterviewSessionPo::getId, sessionId)
                .set(InterviewSessionPo::getCurrentQuestionId, firstQuestionId));
        if (!updated) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_SESSION_SAVE_FAILED);
        }
        return sessionId;
    }

    @Override
    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new BusinessException(InterviewErrorCode.INVALID_INTERVIEW_ID);
        }

        InterviewSessionPo sessionPo = getOne(new LambdaQueryWrapper<InterviewSessionPo>()
                .eq(InterviewSessionPo::getId, sessionId)
                .eq(InterviewSessionPo::getUserId, userId));
        if (sessionPo == null) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_SESSION_NOT_FOUND);
        }

        if (!removeById(sessionId)) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_SESSION_NOT_FOUND);
        }
    }

    @Override
    public SubmitInterviewAnswerVo submitAnswer(SubmitInterviewAnswerDto dto, Long userId) {
        Long sessionId = dto.getSessionId();
        Long questionId = dto.getQuestionId();
        // 先按用户和会话 ID 查本人面试。
        InterviewSessionPo sessionPo = getOne(new LambdaQueryWrapper<InterviewSessionPo>()
                .eq(InterviewSessionPo::getId, sessionId)
                .eq(InterviewSessionPo::getUserId, userId));
        if (sessionPo == null) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_SESSION_NOT_FOUND);
        }
        // 只接受进行中会话的当前题，防止旧页面提交上一题。
        if (!InterviewConstant.Status.IN_PROGRESS.equals(sessionPo.getStatus())
                || !questionId.equals(sessionPo.getCurrentQuestionId())) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_STATE_CONFLICT);
        }

        // 在本场会话内读取题目，用轮次和追问序号决定答完后走哪个分支。
        InterviewSessionQuestionPo questionPo = questionService.getOne(
                new LambdaQueryWrapper<InterviewSessionQuestionPo>()
                        .eq(InterviewSessionQuestionPo::getId, questionId)
                        .eq(InterviewSessionQuestionPo::getSessionId, sessionId));
        if (questionPo == null) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
        }

        // 白卷照常记录为已答。仅主问题的非空白回答需要一次 AI 决策，追问答完直接推进。
        String answer = dto.getAnswer();
        if (answer == null || answer.isBlank()) {
            answer = "";
        }
        Optional<FollowUpQuestionDraft> followUp = Optional.empty();
        if (questionPo.getFollowUpNo() == 0 && !answer.isEmpty()) {
            followUp = generateFollowUpQuestionDraft(questionPo, answer);
        }

        return persistAnswerAndAdvance(sessionPo, questionPo, answer, followUp);
    }



    /**
     * 在同一事务中保存本题答案并推进会话。需要追问时进入本轮追问，否则进入下一轮主问题或结束面试。
     *
     * @param followUp 本次生成的追问草稿，{@link Optional#empty()} 表示跳过追问
     * @return 提交后的会话状态；进行中时包含下一题，结束时下一题字段为空
     */
    private SubmitInterviewAnswerVo persistAnswerAndAdvance(
            InterviewSessionPo session, InterviewSessionQuestionPo question,
            String answer, Optional<FollowUpQuestionDraft> followUp) {
        // AI 决策已完成。以下写入同一事务，推进失败时一起回滚，可重新提交。
        return transactionTemplate.execute(transaction -> {
            if (!questionService.saveAnswer(session.getId(), question.getId(), answer)) {
                throw new BusinessException(InterviewErrorCode.INTERVIEW_STATE_CONFLICT);
            }

            InterviewSessionQuestionPo next = null;
            if (followUp.isPresent()) {
                // 最后一轮的主问题也可能需要追问，先处理追问，再判断是否结束。
                next = questionService.appendFollowUpQuestion(session.getId(), question.getId(), followUp.get());
            } else if (question.getRoundNo() < session.getMainQuestionCount()) {
                next = questionService.getOne(new LambdaQueryWrapper<InterviewSessionQuestionPo>()
                        .eq(InterviewSessionQuestionPo::getSessionId, session.getId())
                        .eq(InterviewSessionQuestionPo::getRoundNo, question.getRoundNo() + 1)
                        .eq(InterviewSessionQuestionPo::getFollowUpNo, 0));
                if (next == null) {
                    throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
                }
            }

            // 只有会话仍停在本题时才推进，避免覆盖另一请求刚刚写入的进度。
            LambdaUpdateWrapper<InterviewSessionPo> progress = new LambdaUpdateWrapper<InterviewSessionPo>()
                    .eq(InterviewSessionPo::getId, session.getId())
                    .eq(InterviewSessionPo::getUserId, session.getUserId())
                    .eq(InterviewSessionPo::getStatus, InterviewConstant.Status.IN_PROGRESS)
                    .eq(InterviewSessionPo::getCurrentQuestionId, question.getId());
            SubmitInterviewAnswerVo result = new SubmitInterviewAnswerVo();
            if (next == null) {
                progress.set(InterviewSessionPo::getCurrentQuestionId, null)
                        .set(InterviewSessionPo::getStatus, InterviewConstant.Status.COMPLETED)
                        .set(InterviewSessionPo::getCompletedAt, OffsetDateTime.now());
                result.setStatus(InterviewConstant.Status.COMPLETED);
            } else {
                progress.set(InterviewSessionPo::getCurrentQuestionId, next.getId());
                result.setStatus(InterviewConstant.Status.IN_PROGRESS);
                result.setNextQuestionId(next.getId());
                result.setRoundNo(next.getRoundNo());
                result.setFollowUpNo(next.getFollowUpNo());
                result.setQuestion(next.getQuestion());
            }
            if (!update(progress)) {
                throw new BusinessException(InterviewErrorCode.INTERVIEW_STATE_CONFLICT);
            }
            return result;
        });
    }

    /**
     * 根据本场题干、评分要点和本次回答，一次决定是否追问。
     * Optional.empty() 表示跳过；接入模型前固定返回占位草稿。
     */
    private Optional<FollowUpQuestionDraft> generateFollowUpQuestionDraft(
            InterviewSessionQuestionPo mainQuestion, String answer) {
        return Optional.of(new FollowUpQuestionDraft(
                "滚木题目：Q7#_∴罒%2⊡m9丷^¥z0宁静Ξ|◈亍6≋x凵@F4⟂乂+◇n3彳⊙U8§丿≠k÷艮5⊗~乙R1▒（调用大模型生成追问占位题）",
                "滚木答案：□9@Z≠凵*4¶s⊥艮7∷k祥◇卩1≋M#乚0⊕x和丿△5^Ω§彡u8⊘冫=H2◈旡~",
                "[\"6◆J|卩∞8_乂%≔亍令人N0丷⊕u向7◐\", \"亠+R0§往凵⟂4m*⊞乚◇w9冫¤2\"]",
                "滚木评分：∷V!8丿~弋孙⊡m4亠≔W笑0凹|◈己z6⊕川_χ◒艮2×N#!丶7≋"
        ));
    }

    @Override
    public void complete(Long userId, Long sessionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new BusinessException(InterviewErrorCode.INVALID_INTERVIEW_ID);
        }

        // 只有首次交卷会命中更新，同时记录结束时间并清空当前题。
        boolean completed = update(new LambdaUpdateWrapper<InterviewSessionPo>()
                .eq(InterviewSessionPo::getId, sessionId)
                .eq(InterviewSessionPo::getUserId, userId)
                .eq(InterviewSessionPo::getStatus, InterviewConstant.Status.IN_PROGRESS)
                .set(InterviewSessionPo::getStatus, InterviewConstant.Status.COMPLETED)
                .set(InterviewSessionPo::getCompletedAt, OffsetDateTime.now())
                .set(InterviewSessionPo::getCurrentQuestionId, null));
        if (completed) {
            return;
        }

        // 未命中时确认会话是否已经结束，避免把不存在或无权访问的会话当成重复交卷。
        InterviewSessionPo session = getOne(new LambdaQueryWrapper<InterviewSessionPo>()
                .select(InterviewSessionPo::getStatus)
                .eq(InterviewSessionPo::getId, sessionId)
                .eq(InterviewSessionPo::getUserId, userId));
        if (session == null) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_SESSION_NOT_FOUND);
        }
        if (!InterviewConstant.Status.COMPLETED.equals(session.getStatus())) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_STATE_CONFLICT);
        }
    }
}
