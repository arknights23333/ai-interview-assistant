package com.assistant.aiinterviewassistant.common.exception.exception;

import com.assistant.aiinterviewassistant.common.exception.enums.PasswordErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 密码管理业务异常，携带业务错误码和对应的 HTTP 状态。
 */
@Getter
public class PasswordException extends RuntimeException {
    private final int code;
    private final HttpStatus httpStatus;

    public PasswordException(PasswordErrorCode errorCode) {
        this(errorCode, null);
    }

    public PasswordException(PasswordErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
}
