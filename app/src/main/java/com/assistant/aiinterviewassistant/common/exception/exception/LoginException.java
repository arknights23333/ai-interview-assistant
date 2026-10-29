package com.assistant.aiinterviewassistant.common.exception.exception;

import com.assistant.aiinterviewassistant.common.exception.enums.LoginErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 登录时异常，携带具体业务码和对应的 HTTP 状态。
 */
@Getter
public class LoginException extends RuntimeException {
    private final int code;
    private final HttpStatus httpStatus;

    public  LoginException(LoginErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
}
