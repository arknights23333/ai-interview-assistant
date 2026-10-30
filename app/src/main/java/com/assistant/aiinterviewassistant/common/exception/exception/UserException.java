package com.assistant.aiinterviewassistant.common.exception.exception;

import com.assistant.aiinterviewassistant.common.exception.enums.UserErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 用户通用业务异常，携带业务错误码和对应的 HTTP 状态。
 */
@Getter
public class UserException extends RuntimeException {

    private final int code;
    private final HttpStatus httpStatus;

    public UserException(UserErrorCode errorCode) {
        this(errorCode, null);
    }

    public UserException(UserErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
}
