package com.assistant.aiinterviewassistant.common.exception.exception;

import com.assistant.aiinterviewassistant.common.exception.enums.RegisterErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 用户注册业务异常，携带业务码和对应的 HTTP 状态。
 */
@Getter
public class RegisterException extends RuntimeException {

    private final int code;
    private final HttpStatus httpStatus;

    public RegisterException(RegisterErrorCode errorCode) {
        this(errorCode, null);
    }

    public RegisterException(RegisterErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
}
