package com.assistant.aiinterviewassistant.infrastructure.mail.service.impl;

import com.assistant.aiinterviewassistant.infrastructure.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final MailSender mailSender;

    private final MailProperties mailProperties;


    @Override
    public void sendVerificationCodeByMail(String to, String code) {


        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getUsername());
        message.setTo(to);
        message.setSubject("情趣科比，在线等你！❤❤❤");
        message.setText("您的验证码是：" + code + "，请勿泄露给他人。");
        mailSender.send(message);
    }
}
