package com.assistant.aiinterviewassistant.modules.schedule.service.impl;

import com.assistant.aiinterviewassistant.modules.schedule.domain.po.InterviewSchedulePo;

import com.assistant.aiinterviewassistant.modules.schedule.service.InterviewScheduleService;
import com.assistant.aiinterviewassistant.modules.schedule.mapper.InterviewScheduleMapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewScheduleServiceImpl extends ServiceImpl<InterviewScheduleMapper, InterviewSchedulePo> implements InterviewScheduleService {
}
