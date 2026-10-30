package com.assistant.aiinterviewassistant.common.exception.handler;


import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceotionHandler {

    /** 所有业务错误共用此入口，具体状态和提示由错误码定义。 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusinessException(BusinessException e) {
        if (e.getHttpStatus().is5xxServerError()) {
            log.error("业务处理失败，业务码：{}", e.getCode(), e);
        }
        return ResponseEntity.status(e.getHttpStatus())
                .body(R.<Void>fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("参数不合法");
        return ResponseEntity.badRequest()
                .body(R.<Void>fail(40000, message));
    }
    /** 包括验证码用途不在枚举范围内等请求体解析失败。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<R<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(R.<Void>fail(40000, "请求体格式或字段值不正确"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception e) {
        log.error("请求处理发生未预期异常", e);
        return ResponseEntity.internalServerError()
                .body(R.<Void>fail(500, "服务器内部错误，请稍后重试"));
    }
}
