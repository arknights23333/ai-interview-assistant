package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 用户注册业务错误，包含业务码、HTTP 状态和对外提示。
 */
@Getter
@RequiredArgsConstructor
public enum RegisterErrorCode implements BusinessErrorCode {

    USERNAME_ALREADY_EXISTS(40901, HttpStatus.CONFLICT, "用户名已存在"),
    EMAIL_ALREADY_BOUND(40902, HttpStatus.CONFLICT, "该邮箱已绑定账号"),
    REGISTER_FAILED(50004, HttpStatus.INTERNAL_SERVER_ERROR, "注册失败，请稍后重试");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
