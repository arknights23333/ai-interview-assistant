package com.assistant.aiinterviewassistant.modules.schedule.service;

import com.assistant.aiinterviewassistant.common.domain.dto.PageQuery;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.schedule.domain.dto.ScheduleCreateDto;
import com.assistant.aiinterviewassistant.modules.schedule.domain.dto.ScheduleQueryDto;
import com.assistant.aiinterviewassistant.modules.schedule.domain.vo.ScheduleVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.assistant.aiinterviewassistant.modules.schedule.domain.po.InterviewSchedulePo;
import jakarta.validation.Valid;

import java.util.List;


public interface InterviewScheduleService extends IService<InterviewSchedulePo> {
    /**
     * 创建面试日程
     * @param userId
     * @return 创建的面试日程ID
     */
    String createSchedule(Long userId, ScheduleCreateDto schedule);

    /**
     * 删除面试日程
     *
     * @param userId
     * @param ids
     * @return
     */
    R<List<Long>> deleteSchedule(Long userId, @Valid List<Long> ids);

    /**
     * 修改面试日程
     *
     * @param userId
     * @param dto
     * @param id
     */
    void updateSchedule(Long userId, @Valid ScheduleCreateDto dto, Long id);

    /** 修改指定用户的日程状态。 */
    void updateScheduleStatus(Long userId, Long id, String status);

    /**
     * 获取面试日程列表
     *
     * @param userId
     * @param dto
     * @param pageQuery
     * @return
     */
    R<Page<ScheduleVo>> getScheduleList(Long userId, @Valid ScheduleQueryDto dto, @Valid PageQuery pageQuery);
}
