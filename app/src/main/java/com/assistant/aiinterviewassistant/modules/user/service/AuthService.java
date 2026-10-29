package com.assistant.aiinterviewassistant.modules.user.service;

import com.assistant.aiinterviewassistant.modules.user.domain.dto.ChangePasswordDto;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.LoginDto;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.MailSendDto;
import com.assistant.aiinterviewassistant.modules.user.domain.dto.RegisterDto;
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
     * 发送登录验证码邮件
     * @param dto 发送邮件dto
     */
    void sendVerifyMail(@Valid MailSendDto dto);

    /**
     * 验证并消费邮箱验证码
     * @param email 邮箱
     * @param code 验证码
     */
    void verifyAndConsumeMailCode(@Valid String email, String code);

    /**
     * 用户注册
     * @param dto
     */
    void register(@Valid RegisterDto dto);

    /**
     * 在记得旧密码且已登录的情况下修改密码
     * @param userId
     * @param dto
     */
    void changePassword(Long userId, @Valid ChangePasswordDto dto);
}
