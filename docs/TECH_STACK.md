# 技术栈与接入状态

更新日期：2026-10-24。适用项目：`D:\ai-interview-assistant`。

版本以本项目构建文件为准；“依赖已加入”“工具已联调”和“业务已完成”分别记录。参考项目 `D:\interview-guide` 只用于学习，不是本项目交付目录。

## 当前技术栈

| 范围 | 技术 / 版本依据 | 当前状态 |
| --- | --- | --- |
| Java | Java 25，app/build.gradle toolchain | 已配置；命令行默认 JDK 与 Gradle 工具链可能不同 |
| 应用框架 | Spring Boot 4.1.1，gradle/libs.versions.toml | 已接入 Web MVC、Validation 等基础能力 |
| 构建 | Gradle 9.8.2 Wrapper；dependency-management 插件 1.1.7 | 根项目组织构建，app 为唯一后端子项目 |
| 持久层 | MyBatis-Plus 3.5.17，mybatis-plus-spring-boot4-starter | 已加入依赖，按 ID 查询已联调；JPA 已移除 |
| 数据库 | PostgreSQL 16，Compose 镜像 pgvector/pgvector:pg16 | 本地数据库已使用；镜像包含 pgvector 不等于已实现向量检索 |
| 迁移 | Spring Boot Flyway Starter + flyway-database-postgresql，版本随 Boot 管理 | V1__create_user_table.sql 定义 app_user；历史表已查看，查询成功由用户确认 |
| 缓存 | Redis 7 + Spring Data Redis | 本地依赖和连接测试已有；登录会话、验证码和业务限流待实现 |
| 认证 | Spring Security，版本由 Boot 管理 | 仅默认 Basic Auth 查询联调成功，数据库认证及自定义安全规则待接入 |
| 密码 | Spring Security BCryptPasswordEncoder | 本地已用库生成并验证测试摘要；应用内 PasswordEncoder Bean 尚未配置 |
| Token | JWT + Redis 登录会话 | 方案已确定；JWT 库、签名配置、会话键和有效期尚未落实，不宣称已集成 |
| 前端 | Vue ^3.5.42、TypeScript ~6.0.0、Vite ^8.2.2、Vue Router ^5.3.1、Pinia ^4.0.3 | package.json 声明范围，实际安装精确版本以 package-lock.json 为准；当前为骨架 |
| Node.js | package.json engines：^22.18.0 或 >=24.12.0 | 项目环境要求，不把范围写成已安装版本 |
| 开发辅助 | Lombok、DevTools | 已接入 |
| 接口工具 | Apifox + IDEA Apifox Helper | 接口已同步并测试；根目录 .apifox-helper.properties 支持 @folder / @ignore |

## 已确定、尚待实现的能力

- 数据库账号认证 → 登录签发 JWT 并建立 Redis 会话 → 请求校验与当前用户 → 退出使会话失效。
- 邮箱注册验证与找回密码：SMTP 发件 + Redis 验证码；发件服务商与凭证未配置。正式注册验证成功后再建用户，普通登录仍为账号密码；首版不做短信验证。
- Spring AI：尚未加入依赖。LLM 候选、固定 Embedding、百炼/腾讯 ASR/TTS 按 [模型选型](MODEL_SELECTION.md) 执行，接入时核实实际权限与版本。
- pgvector 业务索引、文档处理、Redis Stream、对象存储、文件解析与报告导出尚未实现；不从参考工程直接推断已接入的库。

## 结构与接口约定

- 业务包采用 controller / service / mapper / domain；实现类位于 service/impl。
- domain/po 存放持久化对象，domain/dto 存放请求和传输对象，domain/vo 按需存放展示响应。类型名采用 UserPo、UserVo 等驼峰形式。
- Mapper 用于数据库访问；对象转换另设职责。当前 user 使用 IService / ServiceImpl，Service 查询方法是 getById，Mapper 方法是 selectById。
- R<T> 响应字段为 code、msg、data，成功业务码 200、通用失败业务码 500。业务码不等同于 HTTP 状态码，Security 失败及全局异常统一 JSON 仍待实现。
- app_user 有 id、username、email、password_hash、nickname、avatar、role、status、created_at、updated_at 十个字段；账号区分大小写唯一，邮箱不区分大小写唯一，角色 USER/ADMIN，状态 ACTIVE/DISABLED。
- 主键使用 Long；时间使用 OffsetDateTime。PO 含密码摘要，不能直接作为接口响应。
- 当前 GET /api/user/auth/{userID} 为开发联调入口；按 ID 查他人资料的行为不能沿用为普通用户正式访问规则。开发接口需在正式账号权限接入时收口。
- Flyway 创建和升级表结构，JPA 的 ddl-auto 已移除。开发测试账号不作为生产默认账号。
- API Key、个人访问令牌和真实登录凭证不写入文档或 Git；尚未完成的配置整改不能被描述为已完成。

## 本次状态依据与验证

版本来自 app/build.gradle、gradle/libs.versions.toml、Gradle Wrapper、docker-compose.yml、front/package.json；接入状态来自当前源码和本次聊天中的实际联调结果。最近的后端测试曾通过，但不能替代每次后续修改的验证；本次只更新文档，不新增功能完成声明。

接口与需求见 [SRS](requirements/SRS.md)，包边界见 [架构说明](ARCHITECTURE.md)。
