package com.assistant.aiinterviewassistant.modules.interview.domain.vo;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/** 面试会话摘要，用于列表和详情。 */
@Getter
@Setter
public class InterviewSessionSummaryVo {

    /** 面试会话 ID。 */
    private Long id;

    /** 题目来源：NORMAL 普通面试，KNOWLEDGE_BASE 知识库专项。 */
    private String sourceType;

    /** 岗位方向或专项题库方向。 */
    private String jobDirection;

    /** 面试难度。 */
    private String difficulty;

    /** 计划主问题数，不包含追问。 */
    private Integer mainQuestionCount;

    /** 会话状态：IN_PROGRESS 进行中，COMPLETED 已结束。 */
    private String status;

    /** 会话创建时间。 */
    private OffsetDateTime createdAt;

    /** 面试结束时间，进行中为空。 */
    @Nullable
    private OffsetDateTime completedAt;
}
