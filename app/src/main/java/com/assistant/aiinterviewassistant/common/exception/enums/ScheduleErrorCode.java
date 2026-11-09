package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 面试日程业务错误。 */
@Getter
@RequiredArgsConstructor
public enum ScheduleErrorCode implements BusinessErrorCode {

    INVALID_TIME_RANGE(40000, HttpStatus.BAD_REQUEST, "面试结束时间必须晚于开始时间"),
    INVALID_IDS(40015, HttpStatus.BAD_REQUEST, "面试日程 ID 不能为空且必须为正整数"),
    INVALID_STATUS(40016, HttpStatus.BAD_REQUEST, "面试日程状态不合法"),
    INVALID_QUERY_TIME_RANGE(40017, HttpStatus.BAD_REQUEST, "查询结束时间必须晚于开始时间"),
    SCHEDULE_NOT_FOUND(40403, HttpStatus.NOT_FOUND, "面试日程不存在"),
    SCHEDULE_CREATE_FAILED(50019, HttpStatus.INTERNAL_SERVER_ERROR, "面试日程创建失败，请稍后重试"),
    SCHEDULE_UPDATE_FAILED(50020, HttpStatus.INTERNAL_SERVER_ERROR, "面试日程更新失败，请稍后重试"),
    SCHEDULE_DELETE_FAILED(50021, HttpStatus.INTERNAL_SERVER_ERROR, "面试日程删除失败，请稍后重试");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
