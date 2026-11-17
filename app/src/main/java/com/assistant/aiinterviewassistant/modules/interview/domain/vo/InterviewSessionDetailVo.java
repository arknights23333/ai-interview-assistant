package com.assistant.aiinterviewassistant.modules.interview.domain.vo;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** 用于恢复面试或查看历史的会话详情。 */
@Getter
@Setter
public class InterviewSessionDetailVo {

    /** 与列表相同的会话摘要。 */
    private InterviewSessionSummaryVo session;

    /** 关联简历 ID，未关联或已删除时为空。 */
    @Nullable
    private Long resumeId;

    /** 来源知识库 ID，未关联或已删除时为空。 */
    @Nullable
    private Long knowledgeBaseId;

    /** 用户提供的岗位描述，未填写时为空。 */
    @Nullable
    private String jobDescription;

    /** 当前题目。会话没有当前题时为空。 */
    @Nullable
    private InterviewQuestionVo currentQuestion;

    /** 按轮次及轮内顺序展示的本场题目和作答。 */
    private List<InterviewQuestionVo> questions;
}
