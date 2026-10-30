package com.assistant.aiinterviewassistant.modules.user.service;

import com.assistant.aiinterviewassistant.common.exception.enums.LoginErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.MailErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.PasswordErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.RegisterErrorCode;
import com.assistant.aiinterviewassistant.common.exception.enums.UserErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.infrastructure.mail.service.MailService;
import com.assistant.aiinterviewassistant.infrastructure.redis.constant.AuthRedisKeyConstant;
import com.assistant.aiinterviewassistant.infrastructure.redis.service.RedisService;
import com.assistant.aiinterviewassistant.infrastructure.security.config.JwtProperties;
import com.assistant.aiinterviewassistant.infrastructure.security.service.JwtService;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.ChangePasswordDto;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.LoginDto;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.MailSendDto;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.RegisterDto;
import com.assistant.aiinterviewassistant.modules.user.domain.enums.MailCodeScene;
import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.assistant.aiinterviewassistant.modules.user.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("认证业务迁移通用异常后的回归")
class AuthServiceExceptionTests {

    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserService userService;
    private RedisService redisService;
    private MailService mailService;
    private PasswordEncoder passwordEncoder;
    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        userService = mock(UserService.class);
        redisService = mock(RedisService.class);
        mailService = mock(MailService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new AuthServiceImpl(authenticationManager, jwtService, userService, redisService,
                new JwtProperties(), mailService, passwordEncoder);
    }

    @Test
    @DisplayName("禁用用户登录返回通用业务异常并保留 SC 原始异常")
    void disabledLogin() {
        DisabledException cause = new DisabledException("disabled");
        when(authenticationManager.authenticate(any())).thenThrow(cause);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.login(login()));
        assertEquals(UserErrorCode.USER_DISABLED.getCode(), exception.getCode());
        assertSame(cause, exception.getCause());
        verifyNoInteractions(jwtService, redisService);
    }

    @Test
    @DisplayName("错误凭证保留原业务码且不创建会话")
    void badCredentials() {
        BadCredentialsException cause = new BadCredentialsException("bad credentials");
        when(authenticationManager.authenticate(any())).thenThrow(cause);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.login(login()));
        assertEquals(LoginErrorCode.BAD_CREDENTIAL.getCode(), exception.getCode());
        assertSame(cause, exception.getCause());
        verifyNoInteractions(jwtService, redisService);
    }

    @Test
    @DisplayName("认证后二次查询用户已不存在时仍统一提示凭证错误")
    void userDisappearedAfterAuthentication() {
        when(authenticationManager.authenticate(any())).thenReturn(
                UsernamePasswordAuthenticationToken.authenticated("tester", null, List.of()));
        BusinessException exception = assertThrows(BusinessException.class, () -> service.login(login()));
        assertEquals(LoginErrorCode.BAD_CREDENTIAL.getCode(), exception.getCode());
        verifyNoInteractions(jwtService, redisService);
    }

    @Test
    @DisplayName("修改密码的用户不存在时停止后续操作")
    void missingUser() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.changePassword(1L, change("OldPass2026!", "NewPass2026!")));
        assertEquals(UserErrorCode.USER_NOT_FOUND.getCode(), exception.getCode());
        verifyNoInteractions(passwordEncoder, redisService);
    }

    @Test
    @DisplayName("旧密码错误时不更新密码也不撤销会话")
    void incorrectOldPassword() {
        UserPo user = user();
        when(userService.getById(1L)).thenReturn(user);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.changePassword(1L, change("wrong", "NewPass2026!")));
        assertEquals(PasswordErrorCode.OLD_PASSWORD_MIS_MATCH.getCode(), exception.getCode());
        verify(userService, never()).updateById(any(UserPo.class));
        verifyNoInteractions(redisService);
    }

    @Test
    @DisplayName("缺少两种验证信息时返回指定业务错误")
    void missingVerification() {
        when(userService.getById(1L)).thenReturn(user());
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.changePassword(1L, change(null, "NewPass2026!")));
        assertEquals(PasswordErrorCode.VERIFICATION_REQUIRED.getCode(), exception.getCode());
        verifyNoInteractions(passwordEncoder, redisService);
    }

    @Test
    @DisplayName("新旧密码一致时保留原业务错误")
    void samePassword() {
        when(userService.getById(1L)).thenReturn(user());
        when(passwordEncoder.matches("OldPass2026!", "stored-hash")).thenReturn(true);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.changePassword(1L, change("OldPass2026!", "OldPass2026!")));
        assertEquals(PasswordErrorCode.NEW_PASSWORD_SAME_AS_OLD.getCode(), exception.getCode());
        verifyNoInteractions(redisService);
    }

    @Test
    @DisplayName("密码更新未成功时不撤销登录会话")
    void updateFailed() {
        when(userService.getById(1L)).thenReturn(user());
        when(passwordEncoder.matches("OldPass2026!", "stored-hash")).thenReturn(true);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.changePassword(1L, change("OldPass2026!", "NewPass2026!")));
        assertEquals(PasswordErrorCode.PASSWORD_RESET_FAILED.getCode(), exception.getCode());
        verifyNoInteractions(redisService);
    }

    @Test
    @DisplayName("密码更新成功后才撤销会话")
    void successfulChangeKeepsUpdateOrder() {
        UserPo user = user();
        when(userService.getById(1L)).thenReturn(user);
        when(passwordEncoder.matches("OldPass2026!", "stored-hash")).thenReturn(true);
        when(passwordEncoder.encode("NewPass2026!")).thenReturn("new-hash");
        when(userService.updateById(user)).thenReturn(true);
        service.changePassword(1L, change("OldPass2026!", "NewPass2026!"));
        assertEquals("new-hash", user.getPasswordHash());
        InOrder order = inOrder(userService, redisService);
        order.verify(userService).updateById(user);
        order.verify(redisService).deleteObject(AuthRedisKeyConstant.AUTH_LOGIN_KEY + 1L);
    }

    @ParameterizedTest
    @CsvSource({"0,CODE_EXPIRED", "-1,CODE_MISMATCH"})
    @DisplayName("验证码失败仍保留过期与错误的区分")
    void verificationFailures(long result, MailErrorCode expected) {
        when(redisService.<Long>executeScript(anyString(), eq(Long.class), anyList(), eq("ABC234")))
                .thenReturn(result);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.verifyAndConsumeMailCode(AuthRedisKeyConstant.RESET_PASSWORD_MAIL_CODE_KEY,
                        "user@example.com", "ABC234"));
        assertEquals(expected.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("重复账号仍在消费验证码前被拒绝")
    void duplicateUsername() {
        when(userService.getOne(any())).thenReturn(user());
        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(new RegisterDto()));
        assertEquals(RegisterErrorCode.USERNAME_ALREADY_EXISTS.getCode(), exception.getCode());
        verifyNoInteractions(redisService, passwordEncoder);
    }

    @Test
    @DisplayName("重复邮箱仍在消费验证码前被拒绝")
    void duplicateEmail() {
        when(userService.getOne(any())).thenReturn(null, user());
        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(new RegisterDto()));
        assertEquals(RegisterErrorCode.EMAIL_ALREADY_BOUND.getCode(), exception.getCode());
        verifyNoInteractions(redisService, passwordEncoder);
    }

    @Test
    @DisplayName("邮件发送及清理均失败时保留原异常与补充异常")
    void mailFailureKeepsCauses() {
        MailSendDto dto = new MailSendDto();
        dto.setEmail("user@example.com");
        dto.setScene(MailCodeScene.REGISTER);
        when(redisService.<Long>executeScript(anyString(), eq(Long.class), anyList(), any(), any(), any()))
                .thenReturn(1L);
        MailSendException cause = new MailSendException("SMTP 内部错误");
        RuntimeException cleanup = new IllegalStateException("Redis 清理内部错误");
        doThrow(cause).when(mailService).sendVerificationCodeByMail(eq(dto.getEmail()), anyString());
        when(redisService.deleteIfValueMatches(anyString(), anyString())).thenThrow(cleanup);
        BusinessException exception = assertThrows(BusinessException.class, () -> service.sendVerifyMail(dto));
        assertEquals(MailErrorCode.SEND_FAILED.getCode(), exception.getCode());
        assertSame(cause, exception.getCause());
        assertSame(cleanup, exception.getCause().getSuppressed()[0]);
        verify(redisService).deleteIfValueMatches(eq(AuthRedisKeyConstant.REGISTER_MAIL_CODE_KEY + dto.getEmail()), anyString());
    }

    private LoginDto login() {
        LoginDto dto = new LoginDto();
        dto.setUsername("tester");
        dto.setPassword("OldPass2026!");
        return dto;
    }

    private ChangePasswordDto change(String oldPassword, String newPassword) {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        return dto;
    }

    private UserPo user() {
        UserPo user = new UserPo();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPasswordHash("stored-hash");
        return user;
    }
}
