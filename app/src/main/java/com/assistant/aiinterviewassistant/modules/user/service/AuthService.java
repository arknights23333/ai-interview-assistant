package com.assistant.aiinterviewassistant.modules.user.service;

import com.assistant.aiinterviewassistant.modules.user.domain.dto.*;
import com.assistant.aiinterviewassistant.modules.user.domain.po.UserPo;
import com.assistant.aiinterviewassistant.modules.user.domain.vo.LoginVo;
import com.baomidou.mybatisplus.spring.service.IService;
import jakarta.validation.Valid;

public interface AuthService extends IService<UserPo> {
    /**
     *登录接口
     * @param dto 登录参数
     * @return 登录结果，包含用户信息和令牌
     */
    public LoginVo login(LoginDto dto);

    /**
     *推出登录
     * @param userId 用户Id（数据库唯一主键）
     * @param sessionId 唯一会话ID
     * returen 滚木
     */
    void logout(Long userId,String sessionId);

    /**
     * 按指定用途发送邮箱验证码，同一邮箱共享发送间隔。
     * @param dto 发送邮件dto
     */
    void sendVerifyMail(@Valid MailSendDto dto);

    /**
     * 原子地校验并消费指定用途的邮箱验证码。
     * @param keyPrefix 验证码用途前缀，由服务端业务固定指定，不接受前端传入
     * @param email 邮箱
     * @param code 验证码
     */
    void verifyAndConsumeMailCode(String keyPrefix, String email, String code);

    /**
     * 用户注册
     * @param dto
     */
    void register(@Valid RegisterDto dto);

    /**
     * 已登录用户通过旧密码或当前绑定邮箱的验证码修改密码。
     * @param userId
     * @param dto
     */
    void changePassword(Long userId, @Valid ChangePasswordDto dto);

    /**
     * 未登录用户通过邮箱重置密码
     * @param dto
     */
    void resetPassword(@Valid ResetPasswordDto dto);

    /**
     * 已登录用户修改邮箱
     * @param userId 用户Id
      * @param dto 修改邮箱参数
     */
    void changeEmail(Long UserID, ChangeEmailDto dto);
}
