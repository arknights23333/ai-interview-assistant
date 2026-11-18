package com.assistant.aiinterviewassistant.modules.interview.controller;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.InterviewSessionQueryDto;
import com.assistant.aiinterviewassistant.modules.interview.domain.dto.SessionCreateDto;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewSessionDetailVo;
import com.assistant.aiinterviewassistant.modules.interview.domain.vo.InterviewSessionSummaryVo;
import com.assistant.aiinterviewassistant.modules.interview.service.InterviewSessionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文字面试会话接口。
 *
 * @folder 模拟面试
 */
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewSessionController {

    private final InterviewSessionService interviewSessionService;

    /** 分页查询本人面试记录。 */
    @PostMapping("/list")
    public R<Page<InterviewSessionSummaryVo>> listInterviewSessions(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InterviewSessionQueryDto dto,
            @Valid @ModelAttribute PageQuery pageQuery) {
        return R.ok(interviewSessionService.listInterviewSessions(Long.valueOf(jwt.getSubject()), dto, pageQuery));
    }

    /** 查询面试详情，用于恢复进度或查看历史。 */
    @GetMapping("/detail/{sessionId}")
    public R<InterviewSessionDetailVo> getInterviewSessionDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("sessionId") Long sessionId) {
        return R.ok(interviewSessionService.getInterviewSessionDetail(sessionId, Long.valueOf(jwt.getSubject())));
    }
    @PostMapping("/create")
    public R<Long> createSession(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SessionCreateDto dto
    ){
        return R.ok(interviewSessionService.createSession(Long.valueOf(jwt.getSubject()),dto),"面试训练创建成功");
    }
}
