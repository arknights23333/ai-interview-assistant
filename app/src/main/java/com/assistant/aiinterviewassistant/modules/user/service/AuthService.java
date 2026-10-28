package com.assistant.aiinterviewassistant.modules.user.service;

import com.assistant.aiinterviewassistant.modules.user.domain.dto.LoginDto;
import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.assistant.aiinterviewassistant.modules.user.domain.vo.LoginVo;
import com.baomidou.mybatisplus.spring.service.IService;

public interface AuthService extends IService<UserPo> {
    /**
     *登录接口
     * @param dto 登录参数
     * @return 登录结果，包含用户信息和令牌
     */
    public LoginVo login(LoginDto dto);

    /**
     *
     */
    void logout(Long userId,String sessionId);
}
