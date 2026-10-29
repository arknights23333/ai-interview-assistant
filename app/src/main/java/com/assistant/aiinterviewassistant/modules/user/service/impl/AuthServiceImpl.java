package com.assistant.aiinterviewassistant.modules.user.service.impl;

import com.assistant.aiinterviewassistant.common.exception.enums.LoginErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.MailErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.RegisterErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.LoginException;
import com.assistant.aiinterviewassistant.common.exception.exception.MailBusinessException;
import com.assistant.aiinterviewassistant.common.exception.exception.RegisterException;
import com.assistant.aiinterviewassistant.infrastructure.mail.service.MailService;
import com.assistant.aiinterviewassistant.infrastructure.redis.constant.AuthRedisKeyConstant;
import com.assistant.aiinterviewassistant.infrastructure.redis.service.RedisService;
import com.assistant.aiinterviewassistant.infrastructure.security.config.JwtProperties;
import com.assistant.aiinterviewassistant.infrastructure.security.service.JwtService;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.ChangePasswordDto;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.LoginDto;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.MailSendDto;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.RegisterDto;
import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.assistant.aiinterviewassistant.modules.user.domain.vo.LoginVo;
import com.assistant.aiinterviewassistant.modules.user.mapper.UserMapper;
import com.assistant.aiinterviewassistant.modules.user.service.AuthService;
import com.assistant.aiinterviewassistant.modules.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
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

    private final MailService mailService;

    private final PasswordEncoder passwordEncoder;

    /** 验证码字符集：去掉易混淆的 0/O、1/I/L，共 31 个字符。 */
    private static final String CODE_ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";

    /** SecureRandom 线程安全，可全局复用。 */
    private static final SecureRandom secureRandom = new SecureRandom();
    //验证码正则
    private static final String CODE_REGEX =
            "^[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{6}$";

    @Override
    public LoginVo login(LoginDto dto) {
        //验证账号密码
        Authentication authRequest=UsernamePasswordAuthenticationToken.unauthenticated(dto.getUsername(), dto.getPassword());
        try {
            Authentication authResponse=authenticationManager.authenticate(authRequest);
        } catch (DisabledException e) {
            throw new LoginException(LoginErrorCode.USER_DISABLED,e);
        } catch(BadCredentialsException e){
            throw new LoginException(LoginErrorCode.BAD_CREDENTIAL,e);
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

    @Override
    public void sendVerifyMail(MailSendDto dto) {

        String code=generateVerificationCode();
        if (code == null || !code.matches(CODE_REGEX)) {
            // 验证码格式错误
            throw new MailBusinessException(MailErrorCode.INVALID_GENERATED_CODE);
        }

        String script= """
                if redis.call('exists', KEYS[2])==1 then return 0
                end
                redis.call('set',KEYS[1],ARGV[1],'EX',ARGV[2])
                redis.call('set',KEYS[2],'科比','EX',ARGV[3])
                return 1 
                """;
        Long result=redisService.executeScript(script,
                                   Long.class,
                                   List.of(AuthRedisKeyConstant.AUTH_MAIL_CODE_KEY+dto.getEmail(),AuthRedisKeyConstant.AUTH_MAIL_LIMIT_KEY+dto.getEmail()),
                                   code,
                                   120L,
                                   60L);

        if(result == null || result == 0)
        {
            throw new MailBusinessException(MailErrorCode.SEND_TOO_FREQUENT);
        }
        try{
                mailService.sendVerificationCodeByMail(dto.getEmail(), code);
        } catch (MailException e){
                try{
                    redisService.deleteIfValueMatches(AuthRedisKeyConstant.AUTH_MAIL_CODE_KEY+dto.getEmail(), code);
                }catch (RuntimeException cleanupException){
                    // 清理失败也保留排查信息，不覆盖原始发送异常。
                    e.addSuppressed(cleanupException);
                }
           throw new MailBusinessException(MailErrorCode.SEND_FAILED, e);
        }

    }

    @Override
    public void verifyAndConsumeMailCode(String email, String code) {
        String script= """
                local cacheCode = redis.call('get',KEYS[1])
                
                if not cacheCode then
                return 0
                end
                
                if cacheCode ~= ARGV[1] then
                return -1
                end
                redis.call('del',KEYS[1])
                return 1
                """;
        Long result=redisService.executeScript(
                script,
                Long.class,
                List.of(AuthRedisKeyConstant.AUTH_MAIL_CODE_KEY+email),
                code
        );
        if (result == null) {
            throw new IllegalStateException("验证码校验脚本未返回执行结果");
        }
        if (result == 0L) {
            throw new MailBusinessException(MailErrorCode.CODE_EXPIRED);
        }
        if (result == -1L) {
            throw new MailBusinessException(MailErrorCode.CODE_MISMATCH);
        }
    }

    @Override
    public void register(RegisterDto dto) {
        if(userService.getOne(new LambdaQueryWrapper<UserPo>().eq(UserPo::getUsername, dto.getUsername())) != null){
            throw new RegisterException(RegisterErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if(userService.getOne(new LambdaQueryWrapper<UserPo>().eq(UserPo::getEmail, dto.getEmail())) != null){
            throw new RegisterException(RegisterErrorCode.EMAIL_ALREADY_BOUND);
        }
        verifyAndConsumeMailCode(dto.getEmail(), dto.getCode());

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        UserPo po =new UserPo();
        po.setUsername(dto.getUsername());
        po.setPasswordHash(encodedPassword);
        po.setEmail(dto.getEmail());
        if(dto.getNickname() != null && !dto.getNickname().isEmpty()){
            po.setNickname(dto.getNickname());
        }
        if(dto.getAvatar() != null && !dto.getAvatar().isEmpty())
        po.setAvatar(dto.getAvatar());

        userService.save(po);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDto dto) {
        UserPo user = userService.getById(userId);

    }

    /**
     * 生成 6 位字母数字混合验证码。
     *
     * @return 形如 "K7M2QX" 的验证码
     */
    private String generateVerificationCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }


}
