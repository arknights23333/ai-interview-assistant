package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 用户通用业务错误码，用于用户不存在、用户被禁用等场景。
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BusinessErrorCode {

    /** 要操作的用户不存在。 */
    USER_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "用户不存在"),

    /** 用户被禁用，沿用原登录流程的业务码和 HTTP 状态。 */
    USER_DISABLED(40102, HttpStatus.UNAUTHORIZED, "账号已禁用");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
