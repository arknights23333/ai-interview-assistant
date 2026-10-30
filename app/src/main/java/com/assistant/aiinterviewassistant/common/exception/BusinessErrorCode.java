package com.assistant.aiinterviewassistant.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 业务错误码的统一契约，各业务模块的错误码枚举实现此接口。
 */
public interface BusinessErrorCode {

    /** 对外业务错误码，与 HTTP 状态分别表达错误原因和响应类别。 */
    int getCode();

    /** 对应的 HTTP 响应状态。 */
    HttpStatus getHttpStatus();

    /** 可以返回给客户端的业务提示，不应包含内部敏感信息。 */
    String getMessage();
}
