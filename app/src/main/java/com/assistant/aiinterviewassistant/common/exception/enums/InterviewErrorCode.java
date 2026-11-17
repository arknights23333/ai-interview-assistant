package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 文字面试会话和本场题目的业务错误。 */
@Getter
@RequiredArgsConstructor
public enum InterviewErrorCode implements BusinessErrorCode {

    INVALID_INTERVIEW_ID(40021, HttpStatus.BAD_REQUEST, "面试会话或题目 ID 必须为正整数"),
    INVALID_QUESTION_BATCH(40022, HttpStatus.BAD_REQUEST, "题目列表为空或题目内容、顺序不合法"),

    /** 会话不存在、已删除或不属于当前用户时统一返回。 */
    INTERVIEW_SESSION_NOT_FOUND(40406, HttpStatus.NOT_FOUND, "面试会话不存在,或不属于当前用户"),
    INTERVIEW_QUESTION_NOT_FOUND(40407, HttpStatus.NOT_FOUND, "面试题目不存在，或不属于当前用户"),

    /** 已交卷或当前题变化等导致操作不再适用。 */
    INTERVIEW_STATE_CONFLICT(40907, HttpStatus.CONFLICT, "面试状态已变化，请刷新后重试"),

    /** 保存主问题、追加追问或写入答案失败。 */
    INTERVIEW_QUESTION_SAVE_FAILED(50028, HttpStatus.INTERNAL_SERVER_ERROR, "面试题目或作答保存失败，请稍后重试");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
