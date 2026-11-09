package com.assistant.aiinterviewassistant.modules.schedule.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** 修改面试日程状态的请求参数。 */
@Getter
@Setter
public class UpdateScheduleStatusDto {

    /** 目标状态，PENDING 待进行、COMPLETED 已完成、CANCELLED 已取消。 */
    @NotBlank(message = "面试日程状态不能为空")
    private String status;
}
