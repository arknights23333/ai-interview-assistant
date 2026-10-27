package com.assistant.aiinterviewassistant.common.exception;

/**
 * 登录时异常
 */
public class LoginException extends RuntimeException {
    public LoginException(String message) {
        super(message);
    }
}
