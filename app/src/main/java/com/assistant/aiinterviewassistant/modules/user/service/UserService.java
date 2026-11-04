package com.assistant.aiinterviewassistant.modules.user.service;

import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.assistant.aiinterviewassistant.modules.user.domain.vo.UserVo;
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

    /**
     * 根据用户 ID 查询用户，不存在时抛出业务异常。
     *
     * @param userId 用户 ID
     * @return 用户持久化对象，包含密码摘要，仅供服务端内部使用
     * @throws com.assistant.aiinterviewassistant.common.exception.exception.BusinessException 用户不存在
     */
    UserPo getUserByIdOrThrow(Long userId);

    /**
     * 根据用户 ID 查询有效用户，不存在或状态非 ACTIVE 时抛出业务异常。
     *
     * @param userId 用户 ID
     * @return 状态有效的用户持久化对象，包含密码摘要，仅供服务端内部使用
     */
    UserPo getActiveUserByIdOrThrow(Long userId);
    /** 根据当前用户 ID 获取个人信息。 */
    UserVo getCurrentUser(Long userId);

    /** 更新当前用户的昵称。 */
    void updateNickname(Long userId, String nickname);
}
