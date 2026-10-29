package com.assistant.aiinterviewassistant.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 邮件相关业务异常，携带具体业务码和对应的 HTTP 状态。
 */
@Getter
public class MailBusinessException extends RuntimeException {

    private final int code;
    private final HttpStatus httpStatus;

    public MailBusinessException(MailErrorCode errorCode) {
        this(errorCode, null);
    }

    public MailBusinessException(MailErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
}
