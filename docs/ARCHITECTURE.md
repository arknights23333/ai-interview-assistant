# 后端结构说明

## 工程边界

根工程只负责插件声明、版本目录和包含 `app`。`app` 是唯一的后端应用子项目，统一构建和部署。`front`、`docs`、`gradle` 与 `app` 位于同一层，Docker Compose 文件保留在根目录。

Java 根包为 `com.assistant.aiinterviewassistant`。业务模块采用包隔离，不拆分为多个 Gradle 子项目或独立服务。

## 包结构

```text
com.assistant.aiinterviewassistant
├── AiInterviewAssistantApplication
├── common
│   ├── response
│   ├── exception
│   └── config
├── infrastructure
│   ├── security
│   ├── storage
│   ├── async
│   └── export
└── modules
    ├── user
    ├── resume
    ├── interview
    │   ├── evaluation
    │   └── voice
    ├── knowledgebase
    ├── questionbank
    ├── schedule
    └── model
```

每个业务模块使用 `controller`、`service`、`mapper` 和 `domain`。持久化对象统一放在 `domain/po`，请求及数据传输对象放在 `domain/dto`；展示响应对象按需放在 `domain/vo`，用户模块已采用 `UserVo`。类型名使用 `UserPo`、`UserVo`、`UserMapper` 等驼峰命名。实现类位于 `service/impl`。

`mapper` 当前用于 MyBatis-Plus 数据库 Mapper，不再兼作 MapStruct 对象转换包；如后续引入 MapStruct，单独设置转换职责。原骨架中的 `repository` 目录可保留为待用占位，不要求为一次数据库查询再加一层空转 Repository；当前持久层不使用 JPA。部分历史 `package-info.java` 的 Mapper 描述仍待后续清理，以上述职责为准。

## 业务职责

| 包 | 职责与边界 |
| --- | --- |
| user | 注册、登录、退出、用户信息和角色；通用令牌验证机制位于 infrastructure.security |
| resume | 简历文件的业务归属、解析、分析任务、分析报告与删除 |
| interview | 统一面试会话、题目、回答和进度；evaluation 负责统一评价，voice 负责语音连接与交互 |
| knowledgebase | 私有资料集合、文档处理、分块与向量化、检索、RAG 会话与引用 |
| questionbank | 从授权资料生成和维护题目、参考答案、评分要点；向面试模块提供可用题目 |
| schedule | 日程 CRUD、邀请解析、日历查询、提醒与状态 |
| model | 可用模型、默认配置、LLM 路由、固定 Embedding 配置及 ASR/TTS 厂商适配 |

## 分层和调用规则

1. 请求由 Controller 接入，Service 编排业务，MyBatis-Plus Mapper 访问本模块数据；通用 CRUD 可使用 IService / ServiceImpl。正式接口中的业务判断和 PO 到 Vo 转换放在 Service 或专门转换组件，Controller 保持委托。
2. 资源归属检查由对应业务 Service 执行，身份来自经过验证的当前用户。
3. 跨模块调用通过对方提供的 Service 方法；不直接读写其他模块的 Mapper 或持久化数据。
4. 面试可调用简历、题库和模型服务；题库可调用知识库和模型服务；模型能力不反向依赖面试等业务流程。
5. 文件存储、异步执行和 PDF 生成放在 infrastructure；具体上传权限、任务状态和报告内容仍属于业务模块。
6. 模型连接与厂商协议由 model 负责；简历分析内容、面试追问与评分规则由对应业务模块负责。
7. 外部 AI、文件存储和 HTTP 调用不放在长数据库事务中。

文字、语音是面试交互方式，LLM 生成与题库是题目来源。这些场景共用面试与评价核心数据，避免重复建立独立的问答和评分体系。

管理员是角色，管理接口放在对应业务模块。面试历史和报告归 interview，简历分析历史归 resume；页面入口不直接决定后端模块边界。

## 认证、响应与数据库约定

- 认证选型为 Spring Security + JWT + Redis + BCrypt。账号查询及登录业务属于 `modules/user`；通用安全配置、可信当前用户和令牌验证属于 `infrastructure/security`。
- 当前仅完成依赖接入和默认 Basic Auth 下的查询联调。数据库用户认证、JWT、Redis 会话、正式登录和退出均待实现；按 ID 查询的开发接口不能替代正式本人资料接口或证明数据隔离已完成。
- 统一 JSON 响应使用 `common/response/R<T>`，字段为 `code`、`msg`、`data`；PO 不直接返回前端。全局异常与安全过滤链错误的统一适配待实现。
- 数据库结构由 Flyway 版本 SQL 管理，迁移位于 `app/src/main/resources/db/migration`。已执行并共享使用的迁移不直接修改，结构变化新增版本脚本。
- 用户主键在数据库为 BIGINT，在 Java 为 Long；时间列 TIMESTAMPTZ 对应 OffsetDateTime。更新时由应用维护 `updated_at`，数据库默认值只负责初始写入。
- Apifox Helper 公共生成规则位于根目录 `.apifox-helper.properties`，令牌留在本机；接口及字段说明使用 Javadoc，并主动同步。按 ID 查询的现有方法注释还是普通块注释，完整文档化待补。

## 开发与验证入口

在根目录使用 `gradlew.bat :app:bootRun` 启动后端，使用 `gradlew.bat :app:test --no-daemon` 运行现有测试。前端命令在 `front` 中执行，Docker Compose 命令在根目录执行。

需求来源及优先级见 [软件需求规格说明书](requirements/SRS.md)。模型策略见 [模型选型](MODEL_SELECTION.md)，技术与状态见 [技术栈](TECH_STACK.md)。本文定义包结构，不改变需求优先级或声明业务已完成。
