package com.assistant.aiinterviewassistant.modules.user.domain.enums;

/**
 * 邮箱验证码用途，发送时由前端选择，消费时由对应业务固定指定。
 */
public enum MailCodeScene {

    /** 用户注册。 */
    REGISTER,

    /** 忘记密码后重置密码。 */
    RESET_PASSWORD,

    /** 已登录用户通过邮箱验证码修改密码。 */
    CHANGE_PASSWORD,

    /** 已登录用户改绑邮箱。 */
    CHANGE_EMAIL
}
