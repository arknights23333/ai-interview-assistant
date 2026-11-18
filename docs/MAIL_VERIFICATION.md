# 邮箱验证码用途与账号流程

更新日期：2026-11-18。约定依据为本项目 AuthController、AuthServiceImpl、MailCodeScene、Redis 键常量和邮箱迁移。

## 发送请求

`POST /api/user/auth/sendMail` 允许未登录访问，示例请求：

```json
{
  "email": "user@example.com",
  "scene": "REGISTER"
}
```

| scene | 业务用途 | 验证码键前缀 |
| --- | --- | --- |
| REGISTER | 注册 | auth:mail:code:register: |
| RESET_PASSWORD | 忘记密码后重置密码 | auth:mail:code:reset-password: |
| CHANGE_PASSWORD | 已登录后通过邮箱验证码改密码 | auth:mail:code:change-password: |
| CHANGE_EMAIL | 已登录后改绑新邮箱 | auth:mail:code:change-email: |

`scene` 为大写枚举。漏传或传 null 返回 HTTP 400 / 40000；非法枚举也按请求体错误处理。验证码不返回给前端。

邮箱规范化只把最后一个 @ 后的域名转为小写，本地部分保留大小写。发送、消费、注册存储和绑定比较沿用同一规则；数据库唯一性由 V2 维护。具体 DTO 字段以当前源码为准。

## Redis 与 SMTP

- 验证码由 SecureRandom 从约定的字母数字字符集中生成，长度为六位，有效期 120 秒。
- 同一规范化邮箱的所有用途共享 60 秒发送间隔，限流键为 auth:mail:limit 加邮箱。
- Lua 原子检查间隔键，再写入用途验证码键和间隔键。
- SMTP 发送失败时，仅比较并删除本次写入的验证码，保留间隔键。
- 消费 Lua 在键不存在时返回 0；不匹配返回 -1 且保留验证码；相等则删除并返回 1。
- AuthService 的 verifyAndConsumeMailCode(keyPrefix, email, code) 由业务调用者固定传前缀，前端不能指定消费哪个 Redis 键。
- 不同用途互不覆盖、不能跨用途消费。

## 已实现的消费流程

| 接口 | 身份与验证 | 成功后的动作 |
| --- | --- | --- |
| POST /api/user/auth/register | 账号、邮箱及 REGISTER 验证码 | 保存 BCrypt 摘要并创建普通用户，用户随后手动登录 |
| POST /api/user/auth/changePassword | 已认证用户，校验旧密码或 CHANGE_PASSWORD 邮箱验证码 | 更新密码并删除原登录会话 |
| POST /api/user/auth/resetPassword | 账号、绑定邮箱及 RESET_PASSWORD 验证码 | 更新密码并删除原登录会话，错误与过期验证码统一为重置验证失败 |
| POST /api/user/auth/changeEmail | 已认证用户，校验密码及新邮箱 CHANGE_EMAIL 验证码 | 校验新旧邮箱和绑定冲突，更新绑定邮箱 |

找回密码不能只用邮箱定位账号，当前 ResetPasswordDto 同时接收账号。已登录操作的 userId 从 JWT 取得。

## 手动验证参考

发送间隔、错误验证码、过期、重复消费和跨用途消费分别验证。注册后验证手动登录；修改或重置密码后验证旧密码失效和原 Token 无法继续使用；改绑邮箱后核对本人资料和数据库。

本轮只更新说明。用户此前已反馈相关流程手动验证通过，本次未重新执行。自动测试现状见 [异常规范](EXCEPTION_HANDLING.md)，跨模块状态见 [技术栈与模块概况](TECH_STACK.md)。
