package com.assistant.aiinterviewassistant.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 登录时异常码
 */
@Getter
@RequiredArgsConstructor
public enum LoginErrorCode {

    BAD_CREDENTIAL(
            40101,
            HttpStatus.UNAUTHORIZED,
            "账号或密码错误"
            ),
    USER_DISABLED(
            40102,
            HttpStatus.UNAUTHORIZED,
            "账号已禁用"
            ),
    LOGIN_FAILED(
            50003,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "登录失败，请稍后重试"
    );
    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
