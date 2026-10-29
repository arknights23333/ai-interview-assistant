package com.assistant.aiinterviewassistant.modules.user.controller;


import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.LoginDto;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.MailSendDto;
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
    @PostMapping("/sendMail")
    public R<Void> sendMail(@Valid @RequestParam MailSendDto dto){
        authService.sendMail(dto);
        return R.ok(200,"邮件发送成功");
    }
}
