package com.assistant.aiinterviewassistant.modules.schedule.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@TableName("interview_schedule")
public class InterviewSchedulePo {
    /**数据库自增主键*/
    @TableId(type = IdType.AUTO)
    private Long id;

    /**用户id,关联用户表*/
    private Long userId;

    /**创建时间*/
    private OffsetDateTime createAt;

    /**更新时间*/
    private OffsetDateTime updateAt;

    /**公司名称*/
    private String companyName;

    /**职位名称*/
    private String positionName;

    /**面试开始时间*/
    private OffsetDateTime startAt;

    /**面试结束时间*/
    private OffsetDateTime endAt;

    /**面试类型*/
    private String interviewType;

    /**面试会议链接*/
    private String meetingUrl;

    /**面试地点*/
    private String location;

    /**面试轮次*/
    private Integer roundNumber;

    /** 日程状态，新建时由数据库默认填入 PENDING。 */
    private String status;

    /**面试备注*/
    private String notes;
}
