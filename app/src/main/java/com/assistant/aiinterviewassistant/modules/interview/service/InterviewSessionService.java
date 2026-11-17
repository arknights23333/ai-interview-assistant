package com.assistant.aiinterviewassistant.modules.interview.service;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.InterviewSessionQueryDto;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewSessionDetailVo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewSessionSummaryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/** 文字面试会话的业务入口。 */
public interface InterviewSessionService {

    /** 分页查询当前用户的面试记录。 */
    Page<InterviewSessionSummaryVo> listInterviewSessions(
            Long userId, InterviewSessionQueryDto dto, PageQuery pageQuery);

    /** 查询当前用户有权访问的单场面试详情。 */
    InterviewSessionDetailVo getInterviewSessionDetail(Long sessionId, Long userId);
}
