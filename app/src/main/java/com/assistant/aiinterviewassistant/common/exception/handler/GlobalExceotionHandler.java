package com.assistant.aiinterviewassistant.common.exception.handler;


import com.assistant.aiinterviewassistant.common.exception.exception.LoginException;
import com.assistant.aiinterviewassistant.common.exception.exception.MailBusinessException;
import com.assistant.aiinterviewassistant.common.response.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceotionHandler {

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<R<Void>> handleLoginException(LoginException e) {
        if (e.getHttpStatus().is5xxServerError()) {
            log.error("登录业务处理失败，业务码：{}", e.getCode(), e);
        }
        return ResponseEntity.status(e.getHttpStatus())
                .body(R.<Void>fail(e.getCode(), e.getMessage()));
    }
    @ExceptionHandler(MailBusinessException.class)
    public ResponseEntity<R<Void>> handleMailBusinessException(MailBusinessException e) {
        if (e.getHttpStatus().is5xxServerError()) {
            log.error("邮件业务处理失败，业务码：{}", e.getCode(), e);
        }
        // ResponseEntity 设置 HTTP 状态，R.code 表达具体业务原因。
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
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception e) {
        log.error("请求处理发生未预期异常", e);
        return ResponseEntity.internalServerError()
                .body(R.<Void>fail(500, "服务器内部错误，请稍后重试"));
    }
}
