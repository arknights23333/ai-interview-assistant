package com.assistant.aiinterviewassistant.modules.user.controller;

import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;


import com.assistant.aiinterviewassistant.common.exception.enums.UserErrorCode;
import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.*;
import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.assistant.aiinterviewassistant.modules.user.domain.vo.LoginVo;
import com.assistant.aiinterviewassistant.modules.user.domain.vo.UserVo;
import com.assistant.aiinterviewassistant.modules.user.service.AuthService;
import com.assistant.aiinterviewassistant.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证接口。
 *
 * @folder 用户认证
 */
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    /**
     * 根据用户ID获取用户信息,不包含密码
     */
    @GetMapping("/{userID}")
    public R<UserVo> getuser(@PathVariable Long userID){
        UserPo po =userService.getById(userID);
        if (po == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(po,vo);
        return R.ok(vo);
    }

    /**
     * 账号密码登录
     * @param dto 账号密码
     * @return 登录结果 ，含token
     */
    @PostMapping("/login")
    public R<LoginVo> login(@Valid @RequestBody LoginDto dto){
        LoginVo vo = authService.login(dto);
        return R.ok(vo);
    }

    /**
     * 推出登录接口
     * @param jwt
     * @return 滚木
     */
    @PostMapping("/logout")
    public R<Void> logout(@AuthenticationPrincipal Jwt jwt){
        //这个接口只有登录状态下才能进入，不需要判断sessionId或用户Id为空
        authService.logout(Long.valueOf(jwt.getSubject()), jwt.getId());
        return R.ok(200,"退出登录成功");
    }

    /**
     * 发送邮箱验证码接口
     * @param dto
     * @return滚木
     */
    @PostMapping("/sendMail")
    public R<Void> sendMail(@Valid @RequestBody MailSendDto dto){
        authService.sendVerifyMail(dto);
        return R.ok(200,"邮件发送成功");
    }

    /**
     * 用户注册接口
     * @param dto
     * @return滚木
     */
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterDto dto){
        authService.register(dto);
        return R.ok(200,"注册成功");
    }

    /**
     * 记得旧密码且已登录的情况下修改密码接口
     * @param dto
     * @return滚木
     */
    @PostMapping("/changePassword")
    public R<Void> changePassword(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody ChangePasswordDto dto){
        authService.changePassword(Long.valueOf(jwt.getSubject()),dto);
        return R.ok(200,"修改成功");
    }
    /**
     * 不记得旧密码且未登录的情况下修改密码接口
     * @param dto
     * @return滚木
     */
    @PostMapping("resetPassword")
    public R<Void> resetPassword(@Valid @RequestBody ResetPasswordDto dto){
        authService.resetPassword(dto);
        return R.ok(200,"重置成功");
    }
}
