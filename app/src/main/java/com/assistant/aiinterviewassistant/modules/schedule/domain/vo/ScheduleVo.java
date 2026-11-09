package com.assistant.aiinterviewassistant.modules.schedule.domain.vo;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/** 面试日程的展示信息。 */
@Getter
@Setter
public class ScheduleVo {

    /** 面试日程 ID。 */
    private Long id;

    /** 面试公司名称。 */
    private String companyName;

    /** 应聘岗位名称。 */
    private String positionName;

    /** 面试开始时间，包含时区偏移。 */
    private OffsetDateTime startAt;

    /** 预计结束时间，未填写时为空。 */
    @Nullable
    private OffsetDateTime endAt;

    /** 面试方式，例如线上、线下或电话。 */
    private String interviewType;

    /** 线上会议链接，未填写时为空。 */
    @Nullable
    private String meetingUrl;

    /** 线下面试地点，未填写时为空。 */
    @Nullable
    private String location;

    /** 面试轮次，从 1 开始，未填写时为空。 */
    @Nullable
    private Integer roundNumber;

    /** 日程状态，PENDING 待进行、COMPLETED 已完成、CANCELLED 已取消。 */
    private String status;

    /** 日程备注，未填写时为空。 */
    @Nullable
    private String notes;

    /** 日程创建时间。 */
    private OffsetDateTime createAt;

    /** 日程最近一次修改时间。 */
    private OffsetDateTime updateAt;
}
