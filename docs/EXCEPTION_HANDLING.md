# 业务异常与错误码规范

更新日期：2026-11-18。

## 统一结构

可预期的业务拒绝共用 common/exception/exception/BusinessException，原因由实现 common/exception/BusinessErrorCode 的模块枚举表达。契约提供 getCode()、getHttpStatus()、getMessage()。

已有用户、登录、注册、密码、邮箱、文件、简历、日程、知识库、文档和面试错误码分组。新场景复用对应模块的常见类别，避免每个方法新增一种异常类或一个专用错误码。

```java
throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_SAVE_FAILED, cause);
```

业务码表达原因，HTTP 状态表达响应类别。现有协议以枚举源码为准，例如禁用账号是 HTTP 401 / 40102，登录凭据错误是 HTTP 401 / 40101。

## 新增与捕获原则

1. 在对应模块枚举增加确有用途的类别，新增专用数字前查重。40000 是现有通用参数错误码，专用错误避免无意复用。
2. 非空、长度等参数约束优先在 DTO 使用校验注解，并由 @Valid 或显式 Validator 触发；内部对象不会因有注解就自动受检。
3. 业务判断处抛 BusinessException；包装原始异常时保留 cause，响应文本不暴露数据库细节或密钥。
4. 批量逐项失败、上传补偿和外部服务错误可在局部捕获。数据库写入的转换可限定捕获 DataAccessException。
5. 不用 catch (Exception) 把所有错误混成一种业务失败。空查询是否异常按调用契约决定。
6. 要求原子性的整批写入在事务内处理，失败让异常传出。普通 return false 本身不会触发事务回滚。
7. PostgreSQL 事务里写入已经失败时，不能简单 catch 后继续查询；重试或复用处理需考虑事务回滚边界。

## HTTP 与日志

| 场景 | 响应方式 |
| --- | --- |
| BusinessException | GlobalExceotionHandler 按异常的 HTTP 状态返回 R.fail(code, message) |
| DTO 校验、请求体解析和参数类型错误 | HTTP 400 / 40000，返回校验或参数提示 |
| 未匹配接口或静态资源抛 NoResourceFoundException | HTTP 404 / 40400，提示请求的接口或资源不存在 |
| 未预期异常 | HTTP 500 / 500，提示服务器内部错误，请稍后重试 |
| Security 认证失败 | RestAuthenticationEntryPoint 直接写 HTTP 401 / 401 和统一 JSON |

业务 5xx 记录异常及 cause，可预期 4xx 按业务拒绝处理。Security EntryPoint 位于过滤链，不经过 MVC 的 @RestControllerAdvice。

部分批量接口整体返回 HTTP 200，单项失败通过 data 表达；例如上传逐项带状态和业务错误码，删除返回数据库未删掉的 ID。不能仅靠 HTTP 200 判断每一项成功。

## 验证代码与授权

- BusinessExceptionContractTests 固定部分历史错误的状态、业务码和提示，并覆盖 cause、参数及兜底响应；其用例数量不是当前所有错误码的数量。
- AuthServiceExceptionTests 通过 Mockito 覆盖部分账号、验证码、密码和异常链。
- SecurityErrorResponseTests 覆盖无 Token、无 Bearer、无效 Token 和认证后的异常响应。
- 应用、Redis 和对象存储集成测试需要对应的本机依赖；测试文件存在不代表本次执行通过。

仅在用户明确要求编译或测试后，才使用下面的本机命令：

```powershell
.\gradlew.bat :app:test --tests '*BusinessExceptionContractTests' --tests '*AuthServiceExceptionTests' --tests '*SecurityErrorResponseTests' --offline --no-daemon
```

当前各模块实现与验证状态见 [TECH_STACK](TECH_STACK.md)。
