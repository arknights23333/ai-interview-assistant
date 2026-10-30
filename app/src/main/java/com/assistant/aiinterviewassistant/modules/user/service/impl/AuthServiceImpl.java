package com.assistant.aiinterviewassistant.modules.user.service.impl;

import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;

import com.assistant.aiinterviewassistant.common.exception.enums.LoginErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.MailErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.PasswordErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.RegisterErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.UserErrorCode;
import com.assistant.aiinterviewassistant.infrastructure.mail.service.MailService;
import com.assistant.aiinterviewassistant.infrastructure.redis.constant.AuthRedisKeyConstant;
import com.assistant.aiinterviewassistant.infrastructure.redis.service.RedisService;
import com.assistant.aiinterviewassistant.infrastructure.security.config.JwtProperties;
import com.assistant.aiinterviewassistant.infrastructure.security.service.JwtService;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.*;
import com.assistant.aiinterviewassistant.modules.user.domain.enums.MailCodeScene;
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
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
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
            throw new BusinessException(UserErrorCode.USER_DISABLED, e);
        } catch(BadCredentialsException e){
            throw new BusinessException(LoginErrorCode.BAD_CREDENTIAL,e);
        }
        long validitySeconds = jwtProperties.getValiditySeconds();

        UserPo user = userService.getUserByUsername(dto.getUsername());
        if (user == null) {
            // 认证后再次查询时用户也可能已被删除，仍统一提示账号或密码错误。
            throw new BusinessException(LoginErrorCode.BAD_CREDENTIAL, null);
        }
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
        String email = normalizeMailAddress(dto.getEmail());
        String codeKey = resolveMailCodeKeyPrefix(dto.getScene()) + email;
        String limitKey = AuthRedisKeyConstant.AUTH_MAIL_LIMIT_KEY + email;
        String code=generateVerificationCode();
        if (code == null || !code.matches(CODE_REGEX)) {
            // 验证码格式错误
            throw new BusinessException(MailErrorCode.INVALID_GENERATED_CODE);
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
                                   List.of(codeKey, limitKey),
                                   code,
                                   120L,
                                   60L);

        if(result == null || result == 0)
        {
            throw new BusinessException(MailErrorCode.SEND_TOO_FREQUENT);
        }
        try{
                mailService.sendVerificationCodeByMail(dto.getEmail(), code);
        } catch (MailException e){
                try{
                    redisService.deleteIfValueMatches(codeKey, code);
                }catch (RuntimeException cleanupException){
                    // 清理失败也保留排查信息，不覆盖原始发送异常。
                    e.addSuppressed(cleanupException);
                }
           throw new BusinessException(MailErrorCode.SEND_FAILED, e);
        }

    }

    @Override
    public void verifyAndConsumeMailCode(String keyPrefix, String email, String code) {
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
                List.of(keyPrefix + normalizeMailAddress(email)),
                code
        );
        if (result == null) {
            throw new IllegalStateException("验证码校验脚本未返回执行结果");
        }
        if (result == 0L) {
            throw new BusinessException(MailErrorCode.CODE_EXPIRED);
        }
        if (result == -1L) {
            throw new BusinessException(MailErrorCode.CODE_MISMATCH);
        }
    }

    @Override
    public void register(RegisterDto dto) {
        String email = normalizeMailAddress(dto.getEmail());
        if(userService.getOne(new LambdaQueryWrapper<UserPo>().eq(UserPo::getUsername, dto.getUsername())) != null){
            throw new BusinessException(RegisterErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if(userService.getOne(new LambdaQueryWrapper<UserPo>().eq(UserPo::getEmail, email)) != null){
            throw new BusinessException(MailErrorCode.EMAIL_ALREADY_BOUND);
        }
        verifyAndConsumeMailCode(
                AuthRedisKeyConstant.REGISTER_MAIL_CODE_KEY,
                email,
                dto.getCode()
        );

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        UserPo po =new UserPo();
        po.setUsername(dto.getUsername());
        po.setPasswordHash(encodedPassword);
        po.setEmail(email);
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
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        // 优先验证旧密码；未提供旧密码时，验证当前绑定邮箱的验证码。
        if (StringUtils.hasText(dto.getOldPassword())) {
            if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
                throw new BusinessException(PasswordErrorCode.OLD_PASSWORD_MIS_MATCH);
            }
        } else if (StringUtils.hasText(dto.getCode())) {
            verifyAndConsumeMailCode(
                    AuthRedisKeyConstant.CHANGE_PASSWORD_MAIL_CODE_KEY,
                    user.getEmail(),
                    dto.getCode()
            );
        } else {
            throw new BusinessException(PasswordErrorCode.VERIFICATION_REQUIRED);
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(PasswordErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        if (!userService.updateById(user)) {
            throw new BusinessException(PasswordErrorCode.PASSWORD_RESET_FAILED);
        }

        redisService.deleteObject(AuthRedisKeyConstant.AUTH_LOGIN_KEY + userId);

    }

    @Override
    public void resetPassword(ResetPasswordDto dto) {
        UserPo user = userService.getOne(new LambdaQueryWrapper<UserPo>().eq(UserPo::getUsername, dto.getUsername()));
        if (user == null || !normalizeMailAddress(user.getEmail()).equals(normalizeMailAddress(dto.getEmail()))) {
            throw new BusinessException(PasswordErrorCode.PASSWORD_RESET_VERIFICATION_FAILD);
        }

        try {
            verifyAndConsumeMailCode(AuthRedisKeyConstant.RESET_PASSWORD_MAIL_CODE_KEY, user.getEmail(), dto.getCode());
        } catch (BusinessException e) {
            // 仅统一验证码错误和过期的对外提示，其他业务错误与系统故障继续抛出。
            if (e.getCode() == MailErrorCode.CODE_MISMATCH.getCode()
                    || e.getCode() == MailErrorCode.CODE_EXPIRED.getCode()) {
                throw new BusinessException(PasswordErrorCode.PASSWORD_RESET_VERIFICATION_FAILD, e);
            }
            throw e;
        }

        if(passwordEncoder.matches(dto.getNewPassword(),user.getPasswordHash())){
            throw new BusinessException(PasswordErrorCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));

        if (!userService.updateById(user)) {
            throw new BusinessException(PasswordErrorCode.PASSWORD_RESET_FAILED);
        }

        redisService.deleteObject(
                AuthRedisKeyConstant.AUTH_LOGIN_KEY + user.getId()
        );

    }

    /** 保留邮箱本地部分的大小写，仅将最后一个 @ 后的域名转为小写。 */
    private String normalizeMailAddress(String email) {
        int separator = email.lastIndexOf('@');
        if (separator <= 0 || separator == email.length() - 1) {
            throw new IllegalArgumentException("邮箱地址缺少有效的本地部分或域名");
        }
        return email.substring(0, separator + 1)
                + email.substring(separator + 1).toLowerCase(Locale.ROOT);
    }

    /** 将业务用途映射为服务端固定的 Redis 前缀。 */
    private String resolveMailCodeKeyPrefix(MailCodeScene scene) {
        return switch (scene) {
            case REGISTER -> AuthRedisKeyConstant.REGISTER_MAIL_CODE_KEY;
            case RESET_PASSWORD -> AuthRedisKeyConstant.RESET_PASSWORD_MAIL_CODE_KEY;
            case CHANGE_PASSWORD -> AuthRedisKeyConstant.CHANGE_PASSWORD_MAIL_CODE_KEY;
        };
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
