package com.assistant.aiinterviewassistant.modules.interview.service.impl;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.exception.enums.InterviewErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.InterviewSessionQueryDto;
import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionPo;
import com.assistant.aiinterviewassistant.modules.interview.domain.po.InterviewSessionQuestionPo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewQuestionVo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewSessionDetailVo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewSessionSummaryVo;
import com.assistant.aiinterviewassistant.modules.interview.mapper.InterviewSessionMapper;
import com.assistant.aiinterviewassistant.modules.interview.service.InterviewSessionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewSessionServiceImpl extends ServiceImpl<InterviewSessionMapper, InterviewSessionPo>
        implements InterviewSessionService {

    private final InterviewSessionQuestionServiceimpl questionService;

    private final ObjectMapper objectMapper;
    @Override
    public Page<InterviewSessionSummaryVo> listInterviewSessions(
            Long userId, InterviewSessionQueryDto dto, PageQuery pageQuery) {
        throw new UnsupportedOperationException("面试会话分页查询待实现");
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
}
