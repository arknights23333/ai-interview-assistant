# 业务异常与错误码规范

## 统一结构

所有可预期的业务拒绝共用 `common/exception/exception/BusinessException`，错误原因由实现 `common/exception/BusinessErrorCode` 的枚举定义。

现有 LoginErrorCode、RegisterErrorCode、MailErrorCode、PasswordErrorCode、UserErrorCode 保持各自分组。它们都提供 getCode()、getHttpStatus()、getMessage()，不再各自配一个异常类和全局处理方法。

```java
throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
throw new BusinessException(MailErrorCode.SEND_FAILED, cause);
```

业务码表达具体原因，HTTP 状态表达响应类别，两者分别维护。迁移不修改现有枚举名称、数字、HTTP 状态或提示，包括禁用账号仍返回 HTTP 401 / 业务码 40102。

## 增加业务错误

1. 在对应业务的错误码枚举中增加常量，检查业务码全局唯一。
2. 在业务判断处抛出 BusinessException，确有原始异常时传入 cause，保留排查信息。
3. 新模块可增加实现 BusinessErrorCode 的枚举，无需修改 BusinessException 构造器或新增全局处理方法。
4. 对外 message 使用可展示给用户的提示，内部错误细节放在原始异常中，不写入响应消息。
5. 参数约束继续使用 DTO 校验注解，不为每条校验规则新建业务异常。

不要通过 catch (Exception) 把所有错误都包装成一种业务失败。注册查重时查不到用户是正常结果；需要操作既有用户时查不到才拒绝操作。未知程序错误交给兜底处理，避免误报为验证码过期等业务原因。

## 响应与日志

GlobalExceotionHandler.handleBusinessException 统一按异常中的 HTTP 状态返回 R.fail(code, message)。业务 5xx 记录完整异常及 cause；可预期的 4xx 不按系统故障记录堆栈。参数校验、请求体解析和未知异常继续使用各自入口。

未知异常保持 HTTP 500 / 业务码 500，只返回“服务器内部错误，请稍后重试”。

SC 过滤层认证失败继续由 RestAuthenticationEntryPoint 返回 HTTP 401 / 业务码 401；它不经过 MVC 的 @RestControllerAdvice。登录接口自身的账号密码错误则属于业务异常，仍返回业务码 40101。不要为了合并业务异常而移除 EntryPoint。

## 回归测试

本次回归用例不启动数据库、Redis 或 SMTP，也不读取真实登录会话：

- BusinessExceptionContractTests：固定现有 17 个业务错误的 HTTP 状态、业务码和提示，验证统一响应、cause 保留、参数校验和安全兜底。
- AuthServiceExceptionTests：使用 Mockito 验证登录失败、用户不存在、验证码失败、重复注册、密码更新失败与成功顺序、邮件失败及清理失败的异常链。
- SecurityErrorResponseTests：加载真实 SecurityConfig 和 EntryPoint，替换外部令牌解码依赖，验证无 Token、无 Bearer 前缀、无效 Token、认证后业务失败及公开登录入口。

```powershell
.\gradlew.bat :app:compileJava :app:test --tests '*BusinessExceptionContractTests' --tests '*AuthServiceExceptionTests' --tests '*SecurityErrorResponseTests' --offline --no-daemon
```

现有 ApplicationTests、RedisConnectionTests 需要本地基础服务和邮件配置，不属于上述隔离回归。编译和运行测试仍应遵循当前会话的用户授权。
