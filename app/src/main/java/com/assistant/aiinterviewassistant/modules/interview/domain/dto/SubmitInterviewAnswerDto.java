package com.assistant.aiinterviewassistant.modules.interview.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/** 提交一道面试题答案的请求参数。 */
@Data
public class SubmitInterviewAnswerDto {

    /** 本次作答所属的面试会话 ID。 */
    @NotNull(message = "面试会话 ID 不能为空")
    @Positive(message = "面试会话 ID 必须为正整数")
    private Long sessionId;

    /** 本次回答的题目 ID，可以是主问题或追问。 */
    @NotNull(message = "面试题目 ID 不能为空")
    @Positive(message = "面试题目 ID 必须为正整数")
    private Long questionId;

    /** 本次提交的答案，null 或空字符串表示交白卷。 */
    @Nullable
    private String answer;
}
