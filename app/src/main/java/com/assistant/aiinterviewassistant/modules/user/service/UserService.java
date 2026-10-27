package com.assistant.aiinterviewassistant.modules.user.service;

import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.baomidou.mybatisplus.spring.service.IService;
import org.apache.ibatis.annotations.Param;

/**
 * 用户业务服务。
 */

public interface UserService extends IService<UserPo> {
    /**
     * 根据用户账号查询用于身份认证的用户信息。
     *
     * <p>返回信息包含密码摘要，仅供服务端内部使用，不应直接返回给客户端。</p>
     *
     * @param username 用户账号
     * @return 包含密码摘要的用户信息；账号不存在时返回 {@code null}
     */
    UserPo getUserByUsername(String username);
}
