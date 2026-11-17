package com.assistant.aiinterviewassistant.modules.interview.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/** 本人面试历史的分页筛选条件，字段均可省略。 */
@Getter
@Setter
public class InterviewSessionQueryDto {

    /** 题目来源，可选 NORMAL 或 KNOWLEDGE_BASE。 */
    @Nullable
    private String sourceType;

    /** 岗位方向关键词。 */
    @Nullable
    @Size(max = 64, message = "岗位方向关键词不能超过64个字符")
    private String jobDirection;

    /** 会话状态，可选 IN_PROGRESS 或 COMPLETED。 */
    @Nullable
    private String status;

    /** 会话创建时间的起点，包含该时间。 */
    @Nullable
    private OffsetDateTime createdFrom;

    /** 会话创建时间的终点，不包含该时间。 */
    @Nullable
    private OffsetDateTime createdTo;
}
