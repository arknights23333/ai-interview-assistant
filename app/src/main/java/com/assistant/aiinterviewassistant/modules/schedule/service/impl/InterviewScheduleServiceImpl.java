package com.assistant.aiinterviewassistant.modules.schedule.service.impl;

import com.assistant.aiinterviewassistant.common.exception.enums.ScheduleErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.modules.schedule.domain.dto.ScheduleCreateDto;
import com.assistant.aiinterviewassistant.modules.schedule.domain.po.InterviewSchedulePo;

import com.assistant.aiinterviewassistant.modules.schedule.service.InterviewScheduleService;
import com.assistant.aiinterviewassistant.modules.schedule.mapper.InterviewScheduleMapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewScheduleServiceImpl extends ServiceImpl<InterviewScheduleMapper, InterviewSchedulePo> implements InterviewScheduleService {
    @Override
    public String createSchedule(Long userId, ScheduleCreateDto dto) {
        if (dto.getEndAt() != null && !dto.getEndAt().isAfter(dto.getStartAt())) {
            throw new BusinessException(ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        InterviewSchedulePo po = new InterviewSchedulePo();
        po.setUserId(userId);
        BeanUtils.copyProperties(dto, po);
        if (!save(po)) {
            throw new IllegalStateException("保存面试日程失败，userId=" + userId);
        }
        return String.valueOf(po.getId());
    }
}
