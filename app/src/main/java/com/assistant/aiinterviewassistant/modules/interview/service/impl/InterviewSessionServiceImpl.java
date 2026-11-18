package com.assistant.aiinterviewassistant.modules.interview.service.impl;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.exception.enums.InterviewErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.modules.interview.constant.InterviewSourceTypeConstant;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.InterviewSessionQueryDto;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.SessionCreateDto;
import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionPo;
import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionQuestionPo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewQuestionVo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewSessionDetailVo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewSessionSummaryVo;
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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewSessionServiceImpl extends ServiceImpl<InterviewSessionMapper, InterviewSessionPo>
        implements InterviewSessionService {

    private final InterviewSessionQuestionService questionService;
    private final ResumeService resumeService;
    private final KnowledgeBaseService knowledgeBaseService;

    private final ObjectMapper objectMapper;
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
        if (InterviewSourceTypeConstant.NORMAL.equals(dto.getSourceType())
                && dto.getKnowledgeBaseId() != null) {
            throw new BusinessException(InterviewErrorCode.INVALID_INTERVIEW_SOURCE);
        }

        if (InterviewSourceTypeConstant.KNOWLEDGE_BASE.equals(dto.getSourceType())
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
}
