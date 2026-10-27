package com.assistant.aiinterviewassistant.common.exception.handler;


import com.assistant.aiinterviewassistant.common.exception.LoginException;
import com.assistant.aiinterviewassistant.common.response.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceotionHandler {

    @ExceptionHandler(LoginException.class)
    public R handleLoginException(Exception e)
    {
        return R.fail(401,e.getMessage());
    }
}
