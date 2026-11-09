package com.assistant.aiinterviewassistant.modules.schedule.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

/** 面试日程列表和日历查询共用的时间范围。 */
@Getter
@Setter
public class ScheduleTimeRangeDto {

    /** 查询起点，包含该时刻。 */
    @NotNull(message = "查询开始时间不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime from;

    /** 查询终点，不包含该时刻，须晚于起点。 */
    @NotNull(message = "查询结束时间不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime to;
}
