package com.assistant.aiinterviewassistant.modules.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录成功后返回的登录凭证及用户信息。
 */
@Data
@AllArgsConstructor
public class LoginVo {

    /** 访问受保护接口时携带的登录凭证。 */
    private String token;

    /** Token 剩余有效时间，单位为秒。 */
    private Long expiresIn;

    /** 登录账号。 */
    private String username;

    /** 用户昵称。 */
    private String nickname;

    /** 已验证邮箱。 */
    private String email;

    /** 头像的稳定文件标识或地址，未设置时为空。 */
    private String avatar;

    /** 用户角色：USER 普通用户，ADMIN 管理员。 */
    private String role;
}
