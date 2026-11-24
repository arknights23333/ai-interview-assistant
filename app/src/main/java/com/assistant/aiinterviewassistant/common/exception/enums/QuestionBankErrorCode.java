package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 题库原题与草稿维护的业务错误。 */
@Getter
@RequiredArgsConstructor
public enum QuestionBankErrorCode implements BusinessErrorCode {

    /** 查询、编辑或删除题目时使用了无效 ID。 */
    INVALID_QUESTION_ID(40024, HttpStatus.BAD_REQUEST, "题目 ID 必须为正整数"),

    /** 启用新题或采纳修改草稿时缺少必填内容。 */
    QUESTION_NOT_READY(40026, HttpStatus.BAD_REQUEST, "请补齐题干、岗位方向、难度、参考答案和至少一个评分要点"),

    /** 指定知识库中找不到该题目或草稿。 */
    QUESTION_NOT_FOUND(40408, HttpStatus.NOT_FOUND, "题目不存在或不属于当前知识库"),

    /** 当前状态不支持编辑、启用、采纳或归档操作，或状态已被其他请求更新。 */
    QUESTION_STATE_CONFLICT(40908, HttpStatus.CONFLICT, "当前题目状态不支持此操作，请刷新后重试"),

    /** 新建或保存草稿，或更新原题、状态时写入失败。 */
    QUESTION_SAVE_FAILED(50031, HttpStatus.INTERNAL_SERVER_ERROR, "题目保存失败，请稍后重试"),

    /** 删除原题或放弃草稿时删除失败。 */
    QUESTION_DELETE_FAILED(50032, HttpStatus.INTERNAL_SERVER_ERROR, "题目删除失败，请稍后重试");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
