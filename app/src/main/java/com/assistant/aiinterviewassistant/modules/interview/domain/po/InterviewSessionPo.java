package com.assistant.aiinterviewassistant.modules.interview.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/** 一场文字面试的持久化对象，普通面试和知识库专项共用。 */
@Getter
@Setter
@TableName("interview_session")
public class InterviewSessionPo {

    /** 数据库自增主键，也是接口使用的会话 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 ID。 */
    private Long userId;

    /** 题目来源：NORMAL 普通面试，KNOWLEDGE_BASE 知识库专项。 */
    private String sourceType;

    /** 关联的简历 ID，删除简历后为空。 */
    private Long resumeId;

    /** 专项面试的知识库 ID，删除原库后为空。 */
    private Long knowledgeBaseId;

    /** 本场面试的岗位方向或专项题库方向。 */
    private String jobDirection;

    /** 用户提供的岗位描述，可为空。 */
    private String jobDescription;

    /** 本场面试的难度标识。 */
    private String difficulty;

    /** 计划主问题数，不含追问。 */
    private Integer mainQuestionCount;

    /** 本场当前题目 ID，尚未指定或面试结束后可为空。 */
    private Long currentQuestionId;

    /** 会话状态：IN_PROGRESS 进行中，COMPLETED 已结束。 */
    private String status;

    /** 会话创建时间。 */
    private OffsetDateTime createdAt;

    /** 面试结束时间，进行中为空。 */
    private OffsetDateTime completedAt;
}
