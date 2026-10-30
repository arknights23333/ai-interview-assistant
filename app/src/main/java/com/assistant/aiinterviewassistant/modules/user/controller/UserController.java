package com.assistant.aiinterviewassistant.modules.user.controller;

import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.UpdateNicknameDto;
import com.assistant.aiinterviewassistant.modules.user.domain.vo.UserVo;
import com.assistant.aiinterviewassistant.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前用户资料接口。
 *
 * @folder 用户资料
 */
@RestController
@RequestMapping("/api/user/me")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /** 获取当前登录用户的资料。 */
    @GetMapping
    public R<UserVo> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return R.ok(userService.getCurrentUser(Long.valueOf(jwt.getSubject())));
    }

    /** 修改当前登录用户的昵称。 */
    @PatchMapping("/nickname")
    public R<Void> updateNickname(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateNicknameDto dto) {
        userService.updateNickname(Long.valueOf(jwt.getSubject()), dto.getNickname());
        return R.ok(200, "昵称修改成功");
    }
}
