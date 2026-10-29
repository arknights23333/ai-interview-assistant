package com.assistant.aiinterviewassistant.infrastructure.mail.service;

public interface MailService {
    /**
     * 发送邮件验证码
     * @param to 收件人邮箱
     * @param code  邮箱验证码
     */
    void sendVerificationCodeByMail(String to, String code);
}
