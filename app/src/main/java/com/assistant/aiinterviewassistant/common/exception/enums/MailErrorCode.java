package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 邮件及邮箱验证码的业务错误定义，分别维护业务码和 HTTP 状态。
 */
@Getter
@RequiredArgsConstructor
public enum MailErrorCode implements BusinessErrorCode {

    INVALID_EMAIL(40001, HttpStatus.BAD_REQUEST, "收件邮箱格式错误"),
    CODE_MISMATCH(40002, HttpStatus.BAD_REQUEST, "验证码不正确"),
    CODE_EXPIRED(40003, HttpStatus.BAD_REQUEST, "验证码已过期，请重新获取"),
    SEND_TOO_FREQUENT(42901, HttpStatus.TOO_MANY_REQUESTS, "验证码发送过于频繁，请稍后重试"),
    SEND_FAILED(50001, HttpStatus.INTERNAL_SERVER_ERROR, "邮件发送失败，请稍后重试"),
    INVALID_GENERATED_CODE(50002, HttpStatus.INTERNAL_SERVER_ERROR, "验证码生成异常，请稍后重试"),
    EMAIL_ALREADY_BOUND(40902, HttpStatus.CONFLICT, "该邮箱已绑定账号"),
    NEW_EMAIL_SAME_AS_OLD(40008, HttpStatus.BAD_REQUEST, "新邮箱与旧邮箱相同"),
    EMAIL_CHANGE_VERIFICATION_FAILED(40009, HttpStatus.BAD_REQUEST, "新邮箱验证码错误或已过期"),
    EMAIL_CHANGE_FAILED(50006, HttpStatus.INTERNAL_SERVER_ERROR, "邮箱改绑失败，请稍后重试");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
