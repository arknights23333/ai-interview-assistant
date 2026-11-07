package com.assistant.aiinterviewassistant.modules.schedule.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/** 创建面试日程的请求参数。 */
@Getter
@Setter
public class ScheduleCreateDto {
    @NotBlank(message = "公司名称不能为空")
    @Size(max = 255, message = "公司名称不能超过255个字符")
    private String companyName;

    @NotBlank(message = "应聘岗位不能为空")
    @Size(max = 255, message = "应聘岗位不能超过255个字符")
    private String positionName;

    @NotNull(message = "面试开始时间不能为空")
    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    @NotBlank(message = "面试方式不能为空")
    @Size(max = 16, message = "面试方式不能超过16个字符")
    private String interviewType;

    private String meetingUrl;

    @Size(max = 255, message = "面试地点不能超过255个字符")
    private String location;

    @Positive(message = "面试轮次必须大于0")
    private Integer roundNumber;

    private String notes;
}
