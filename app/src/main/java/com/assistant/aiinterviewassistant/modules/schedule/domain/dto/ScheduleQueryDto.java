package com.assistant.aiinterviewassistant.modules.schedule.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/** 面试日程查询的筛选条件。 */
@Getter
@Setter
public class ScheduleQueryDto {

    /** 公司名称关键词，按包含关系匹配。 */
    @Nullable
    @Size(max = 255, message = "公司名称关键词不能超过255个字符")
    private String companyName;

    /** 日程状态，可选值为 PENDING、COMPLETED、CANCELLED。 */
    @Nullable
    private String status;

    /** 按面试开始时间筛选，包含起点，不包含终点。 */
    @Nullable
    @Valid
    private ScheduleTimeRangeDto timeRange;
}
