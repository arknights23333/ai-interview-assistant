package com.assistant.aiinterviewassistant.modules.schedule.service.impl;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.exception.enums.ScheduleErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.schedule.domain.dto.ScheduleCreateDto;
import com.assistant.aiinterviewassistant.modules.schedule.domain.dto.ScheduleQueryDto;
import com.assistant.aiinterviewassistant.modules.schedule.domain.dto.ScheduleTimeRangeDto;
import com.assistant.aiinterviewassistant.modules.schedule.domain.po.InterviewSchedulePo;

import com.assistant.aiinterviewassistant.modules.schedule.domain.vo.ScheduleVo;
import com.assistant.aiinterviewassistant.modules.schedule.service.InterviewScheduleService;
import com.assistant.aiinterviewassistant.modules.schedule.mapper.InterviewScheduleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewScheduleServiceImpl extends ServiceImpl<InterviewScheduleMapper, InterviewSchedulePo> implements InterviewScheduleService {

    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "COMPLETED", "CANCELLED");

    @Override
    public String createSchedule(Long userId, ScheduleCreateDto dto) {
        if (dto.getEndAt() != null && !dto.getEndAt().isAfter(dto.getStartAt())) {
            throw new BusinessException(ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        InterviewSchedulePo po = new InterviewSchedulePo();
        po.setUserId(userId);
        BeanUtils.copyProperties(dto, po);
        if (!save(po)) {
            log.error("保存面试日程失败，userId={}", userId);
            throw new IllegalStateException("保存面试日程失败，userId=" + userId);
        }
        return String.valueOf(po.getId());
    }
    @Override
    public void updateSchedule(Long userId, ScheduleCreateDto dto, Long id) {
        if (dto.getEndAt() != null && !dto.getEndAt().isAfter(dto.getStartAt())) {
            throw new BusinessException(ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        boolean result = update(new LambdaUpdateWrapper<InterviewSchedulePo>().
                eq(InterviewSchedulePo::getId, id).
                eq(InterviewSchedulePo::getUserId, userId).
                set(InterviewSchedulePo::getStartAt, dto.getStartAt()).
                set(InterviewSchedulePo::getEndAt, dto.getEndAt()).
                set(InterviewSchedulePo::getCompanyName, dto.getCompanyName()).
                set(InterviewSchedulePo::getPositionName, dto.getPositionName()).
                set(InterviewSchedulePo::getInterviewType, dto.getInterviewType()).
                set(InterviewSchedulePo::getMeetingUrl, dto.getMeetingUrl()).
                set(InterviewSchedulePo::getLocation, dto.getLocation()).
                set(InterviewSchedulePo::getRoundNumber, dto.getRoundNumber()).
                set(InterviewSchedulePo::getNotes, dto.getNotes()).
                set(InterviewSchedulePo::getUpdateAt, OffsetDateTime.now()));
        if(!result){
            log.error("更新面试日程失败，userId={}, id={}", userId, id);
            throw new BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
        }

    }

    @Override
    public void updateScheduleStatus(Long userId, Long id, String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new BusinessException(ScheduleErrorCode.INVALID_STATUS);
        }

        boolean updated = update(new LambdaUpdateWrapper<InterviewSchedulePo>()
                .eq(InterviewSchedulePo::getId, id)
                .eq(InterviewSchedulePo::getUserId, userId)
                .set(InterviewSchedulePo::getStatus, status)
                .set(InterviewSchedulePo::getUpdateAt, OffsetDateTime.now()));
        if (!updated) {
            throw new BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
        }
    }

    @Override
    public R<Page<ScheduleVo>> getScheduleList(Long userId, ScheduleQueryDto dto, @Valid PageQuery pageQuery) {


        String status = dto.getStatus();
        if(status!=null&&!VALID_STATUSES.contains(status)){
            throw new BusinessException(ScheduleErrorCode.INVALID_STATUS);
        }

        LambdaQueryWrapper<InterviewSchedulePo> wrapper = new LambdaQueryWrapper<InterviewSchedulePo>()
                .eq(InterviewSchedulePo::getUserId, userId)
                .eq(dto.getStatus()!=null, InterviewSchedulePo::getStatus, dto.getStatus())
                .like(dto.getCompanyName()!=null, InterviewSchedulePo::getCompanyName, dto.getCompanyName())
                .orderByAsc(InterviewSchedulePo::getStartAt)
                .orderByAsc(InterviewSchedulePo::getId);

        ScheduleTimeRangeDto timeRange = dto.getTimeRange();
        if(timeRange!=null){
            if(!timeRange.getTo().isAfter(timeRange.getFrom())){
                throw new BusinessException(ScheduleErrorCode.INVALID_QUERY_TIME_RANGE);
            }
            wrapper.ge(InterviewSchedulePo::getStartAt, timeRange.getFrom())
                    .lt(InterviewSchedulePo::getStartAt, timeRange.getTo());
        }

        Page<InterviewSchedulePo> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<InterviewSchedulePo> result = page(page,wrapper);

        List<ScheduleVo> list = result.getRecords().stream().
                map(
                        po ->{
                            ScheduleVo vo = new ScheduleVo();
                            BeanUtils.copyProperties(po,vo);
                            return vo;
                        }
                ).toList();
        Page<ScheduleVo> resultVo = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultVo.setRecords(list);
        return R.ok(resultVo,"获取面试日程列表成功");
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
