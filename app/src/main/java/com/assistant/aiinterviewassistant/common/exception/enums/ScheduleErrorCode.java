package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 面试日程业务错误。 */
@Getter
@RequiredArgsConstructor
public enum ScheduleErrorCode implements BusinessErrorCode {

    INVALID_TIME_RANGE(40000, HttpStatus.BAD_REQUEST, "面试结束时间必须晚于开始时间");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
