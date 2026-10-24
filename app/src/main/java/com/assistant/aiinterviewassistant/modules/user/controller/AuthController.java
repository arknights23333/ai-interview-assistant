package com.assistant.aiinterviewassistant.modules.user.controller;


import com.assistant.aiinterviewassistant.common.response.R;
import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.assistant.aiinterviewassistant.modules.user.domain.vo.UserVo;
import com.assistant.aiinterviewassistant.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    /*
     * 根据用户ID获取用户信息,不包含密码
     */
    @GetMapping("/{userID}")
    public R<UserVo> getuser(@PathVariable Long userID){
        UserPo po =userService.getById(userID);
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(po,vo);
        return R.ok(vo);
    }
}
