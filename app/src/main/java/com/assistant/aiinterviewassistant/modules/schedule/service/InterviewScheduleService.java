package com.assistant.aiinterviewassistant.modules.schedule.service;

import com.assistant.aiinterviewassistant.modules.schedule.domain.dto.ScheduleCreateDto;
import com.baomidou.mybatisplus.spring.service.IService;
import com.assistant.aiinterviewassistant.modules.schedule.domain.po.InterviewSchedulePo;


public interface InterviewScheduleService extends IService<InterviewSchedulePo> {
    /**
     * 创建面试日程
     * @param userId
     * @return 创建的面试日程ID
     */
    String createSchedule(Long userId, ScheduleCreateDto schedule);
}
