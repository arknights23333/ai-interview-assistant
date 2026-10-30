package com.assistant.aiinterviewassistant.infrastructure.redis.constant;

public class AuthRedisKeyConstant {
    /**
     * 用于在 Redis 维护登录会话。
     * <p>格式：auth:login:userId</p>
     */
    public static final String AUTH_LOGIN_KEY = "auth:login:";

    /** 注册邮箱验证码，格式：auth:mail:code:register:email。 */
    public static final String REGISTER_MAIL_CODE_KEY = "auth:mail:code:register:";

    /** 重置密码邮箱验证码，格式：auth:mail:code:reset-password:email。 */
    public static final String RESET_PASSWORD_MAIL_CODE_KEY = "auth:mail:code:reset-password:";

    /** 修改密码邮箱验证码，格式：auth:mail:code:change-password:email。 */
    public static final String CHANGE_PASSWORD_MAIL_CODE_KEY = "auth:mail:code:change-password:";

    /**
     * 同一邮箱的所有用途共享发送间隔。
     * <p>格式：auth:mail:limitemail；保留现有键格式。</p>
     */
    public static final String AUTH_MAIL_LIMIT_KEY = "auth:mail:limit";

    private AuthRedisKeyConstant() {
    }
}
