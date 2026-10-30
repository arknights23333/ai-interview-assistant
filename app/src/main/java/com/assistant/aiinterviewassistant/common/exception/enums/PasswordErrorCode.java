package com.assistant.aiinterviewassistant.common.exception.enums;

import com.assistant.aiinterviewassistant.common.exception.BusinessErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 密码管理业务错误码，用于修改密码和重置密码场景。
 */
@Getter
@RequiredArgsConstructor
public enum PasswordErrorCode implements BusinessErrorCode {
    //提交的旧密码与当前不匹配
    OLD_PASSWORD_MIS_MATCH(
            40004,
            HttpStatus.BAD_REQUEST,
            "旧密码不匹配"
    ),
    //新密码与旧密码相同
    NEW_PASSWORD_SAME_AS_OLD(
            40005,
            HttpStatus.BAD_REQUEST,
            "新密码与旧密码相同"
    ),
    //未提供旧密码或邮箱验证码，无法完成身份验证
    VERIFICATION_REQUIRED(
            40006,
            HttpStatus.BAD_REQUEST,
            "请提供旧密码或邮箱验证码"
    ),
    PASSWORD_RESET_VERIFICATION_FAILD(
            40007,
            HttpStatus.BAD_REQUEST,
            "账号、邮箱或验证码不正确"
    ),
    //密码重置过程中发生内部错误
    PASSWORD_RESET_FAILED(
            50005,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "密码重置失败，请稍后重试"
    );

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
