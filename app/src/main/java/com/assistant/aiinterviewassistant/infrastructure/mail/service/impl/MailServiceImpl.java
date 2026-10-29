package com.assistant.aiinterviewassistant.infrastructure.mail.service.impl;

import com.assistant.aiinterviewassistant.common.exception.MailBusinessException;
import com.assistant.aiinterviewassistant.common.exception.MailErrorCode;
import com.assistant.aiinterviewassistant.infrastructure.mail.service.MailService;
import com.assistant.aiinterviewassistant.infrastructure.redis.constant.AuthRedisKeyConstant;
import com.assistant.aiinterviewassistant.infrastructure.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final MailSender mailSender;
    private final RedisService redisService;
    private final MailProperties mailProperties;
    //验证码正则
    private static final String CODE_REGEX = "^[0-9]{6}$";
    //邮箱正则
    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9_%+-]+(?:\\.[A-Za-z0-9_%+-]+)*@"
                    + "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"
                    + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$";
    @Override
    public void sendVerificationCodeByMail(String to, String code) {
        if (code == null || !code.matches(CODE_REGEX)) {
            // 验证码格式错误
            throw new MailBusinessException(MailErrorCode.INVALID_GENERATED_CODE);
        }

        if (to == null || !to.matches(EMAIL_REGEX)) {
            // 收件邮箱格式错误
            throw new MailBusinessException(MailErrorCode.INVALID_EMAIL);
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getUsername());
        message.setTo(to);
        message.setSubject("情趣科比，在线等你！❤❤❤");
        message.setText("您的验证码是：" + code + "，请勿泄露给他人。");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            // 保留 SMTP 原始异常供排查，对外使用统一的业务码和提示。
            throw new MailBusinessException(MailErrorCode.SEND_FAILED, e);
        }

    }
}
