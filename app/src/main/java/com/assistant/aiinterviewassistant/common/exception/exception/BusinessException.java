package com.assistant.aiinterviewassistant.common.exception.exception;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * 通用业务异常，接受任意实现统一契约的错误码，保留业务码、HTTP 状态及原始异常。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final HttpStatus httpStatus;

    public BusinessException(BusinessErrorCode errorCode) {
        this(errorCode, null);
    }

    public BusinessException(BusinessErrorCode errorCode, Throwable cause) {
        super(Objects.requireNonNull(errorCode, "业务错误码不能为空").getMessage(), cause);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
}
