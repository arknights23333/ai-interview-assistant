package com.assistant.aiinterviewassistant.infrastructure.security;

import com.assistant.aiinterviewassistant.infrastructure.redis.service.RedisService;
import com.assistant.aiinterviewassistant.infrastructure.redis.constant.AuthRedisKeyConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 登录会话校验器：比对 JWT 中的会话标识与当前缓存的登录会话。
 */
@Component
@RequiredArgsConstructor
public class LoginSessionValidator implements OAuth2TokenValidator<Jwt> {

    private final RedisService redisService;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String subject = token.getSubject();
        String id = token.getId();

        // 本项目要求 Token 同时包含用户 ID 和会话标识，缺失时不访问 Redis。
        if (!StringUtils.hasText(subject) || !StringUtils.hasText(id)) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN, "Token 缺少用户或会话信息", null));
        }

        String sessionId = redisService.getCacheObject(AuthRedisKeyConstant.AUTH_LOGIN_KEY + subject);
        // 会话过期、退出登录或被后一次登录覆盖时，旧 Token 均不能继续使用。
        if (sessionId == null || !sessionId.equals(id)) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN, "会话已失效，请重新登录", null));
        }
        return OAuth2TokenValidatorResult.success();
    }
}
