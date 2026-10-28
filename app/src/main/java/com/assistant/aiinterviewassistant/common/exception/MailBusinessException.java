package com.assistant.aiinterviewassistant.common.exception;

/**
 * 邮件相关业务异常。
 */
public class MailBusinessException extends RuntimeException {

    public MailBusinessException(String message) {
        super(message);
    }
}
