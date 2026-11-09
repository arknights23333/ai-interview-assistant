package com.assistant.aiinterviewassistant.modules.schedule.service.impl;

import com.assistant.aiinterviewassistant.common.exception.enums.ScheduleErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.schedule.domain.dto.ScheduleCreateDto;
import com.assistant.aiinterviewassistant.modules.schedule.domain.po.InterviewSchedulePo;

import com.assistant.aiinterviewassistant.modules.schedule.service.InterviewScheduleService;
import com.assistant.aiinterviewassistant.modules.schedule.mapper.InterviewScheduleMapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


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

    @Override
    public R<List<Long>> deleteSchedule(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException(ScheduleErrorCode.INVALID_IDS);
        }
        List<Long> deleteIds = getBaseMapper().deleteSchedule(userId, ids);
        List<Long> faildIds = ids.stream().filter(id->!deleteIds.contains(id)).toList();
        if (faildIds.size() == 0) {
            return R.ok(faildIds,"全部删除成功");
        }
        if(faildIds.size() == ids.size()){
            return R.ok(faildIds, "未删除任何日程");
        }
        return R.ok(faildIds,"部分删除成功，失败的ID：" + faildIds);

    }
}
