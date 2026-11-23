package com.assistant.aiinterviewassistant.modules.interview.domain.vo;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/** 提交答案后的会话状态与当前题目。没有当前题时，所有题目字段均为空。 */
@Getter
@Setter
public class SubmitInterviewAnswerVo {

    /** 提交后的会话状态，IN_PROGRESS 或 COMPLETED。 */
    private String status;

    /** 下一道需要回答的题目 ID，提交该题答案时作为 questionId。 */
    @Nullable
    private Long nextQuestionId;

    /** 当前题目所属的主问题轮次，从 1 开始。 */
    @Nullable
    private Integer roundNo;

    /** 当前题目的轮内顺序，0 表示主问题，1 表示首版唯一的追问。 */
    @Nullable
    private Integer followUpNo;

    /** 当前题目的题干。 */
    @Nullable
    private String question;
}
