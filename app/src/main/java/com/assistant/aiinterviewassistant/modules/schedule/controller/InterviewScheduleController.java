package com.assistant.aiinterviewassistant.modules.schedule.controller;

import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.schedule.domain.dto.ScheduleCreateDto;
import com.assistant.aiinterviewassistant.modules.schedule.service.InterviewScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * 面试日程管理模块
 * @folder 面试日程管理模块
 */
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class InterviewScheduleController {

    private final InterviewScheduleService interviewScheduleService;
    @PostMapping("/create")
    public R<String> createSchedule(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ScheduleCreateDto dto){
        String id = interviewScheduleService.createSchedule(Long.valueOf(jwt.getSubject()),dto);

        return R.ok(id,"创建成功");
    }

}
