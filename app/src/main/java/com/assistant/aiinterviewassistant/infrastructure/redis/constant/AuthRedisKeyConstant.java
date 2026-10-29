package com.assistant.aiinterviewassistant.infrastructure.redis.constant;

public class AuthRedisKeyConstant {
    /**
     * 用于在redis维护登录会话的键
     * <p>格式：auth:login:userId</p>
     */
    public final static String AUTH_LOGIN_KEY="auth:login:";
    /**
     * 用于在redis维护邮箱验证码的键
     * <p>格式：auth:mail:code:email</p>
     */
    public static final String AUTH_MAIL_CODE_KEY = "auth:mail:code:";
    /**
     * 用与在redis维护邮箱发送频率限制的键
     * <P>格式 auth:login:limit:email</P>
     */
    public static final String AUTH_MAIL_LIMIT_KEY= "auth:mail:limit";
    private AuthRedisKeyConstant() {
    }
}
