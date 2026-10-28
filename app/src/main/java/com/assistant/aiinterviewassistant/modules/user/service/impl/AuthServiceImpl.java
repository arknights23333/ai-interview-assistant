package com.assistant.aiinterviewassistant.modules.user.service.impl;

import com.assistant.aiinterviewassistant.infrastructure.security.config.JwtProperties;
import com.assistant.aiinterviewassistant.common.exception.LoginException;
import com.assistant.aiinterviewassistant.infrastructure.redis.service.RedisService;
import com.assistant.aiinterviewassistant.infrastructure.redis.constant.AuthRedisKeyConstant;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.LoginDto;
import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.assistant.aiinterviewassistant.modules.user.domain.vo.LoginVo;
import com.assistant.aiinterviewassistant.modules.user.mapper.UserMapper;
import com.assistant.aiinterviewassistant.modules.user.service.AuthService;
import com.assistant.aiinterviewassistant.infrastructure.security.service.JwtService;
import com.assistant.aiinterviewassistant.modules.user.service.UserService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<UserMapper, UserPo> implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final UserService userService;

    private final RedisService redisService;

    private final JwtProperties jwtProperties;
    @Override
    public LoginVo login(LoginDto dto) {
        //验证账号密码
        Authentication authRequest=UsernamePasswordAuthenticationToken.unauthenticated(dto.getUsername(), dto.getPassword());
        try {
            Authentication authResponse=authenticationManager.authenticate(authRequest);
        } catch (DisabledException e) {
            throw new LoginException("用户已禁用");
        } catch(BadCredentialsException e){
            throw new LoginException("账号密码错误");
        }
        long validitySeconds = jwtProperties.getValiditySeconds();

        UserPo user = userService.getUserByUsername(dto.getUsername());
        String sessionId = UUID.randomUUID().toString();
        String token = jwtService.generateToken(user.getId(), sessionId, validitySeconds);
        redisService.setCacheObject(AuthRedisKeyConstant.AUTH_LOGIN_KEY + user.getId(), sessionId, validitySeconds, TimeUnit.SECONDS);

        return new LoginVo(token, validitySeconds, user.getUsername(), user.getNickname(), user.getEmail(), user.getAvatar(), user.getRole());

    }

    @Override
    public void logout(Long userId, String sessionId) {

        redisService.deleteIfValueMatches(AuthRedisKeyConstant.AUTH_LOGIN_KEY + userId, sessionId);

    }

}
