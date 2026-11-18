# 后端结构与模块协作

更新日期：2026-11-18。本文保存长期设计与数据约定，模块实现和验证状态集中在 [TECH_STACK](TECH_STACK.md)，接手入口在 [README](../README.md)。

## 工程边界

本仓库是单体应用，app 是唯一后端 Gradle 子项目。Java 根包为 com.assistant.aiinterviewassistant，前端为 front/ 下的 Vue 工程。数据库访问使用 MyBatis-Plus，迁移使用 Flyway。D:\interview-guide 是独立参考项目，其 JPA、React 与本项目不同。

## 包结构

```text
com.assistant.aiinterviewassistant
├── common
│   ├── response
│   ├── exception
│   ├── domain/dto
│   └── config
├── infrastructure
│   ├── security
│   ├── redis
│   ├── storage
│   ├── document
│   ├── mail
│   ├── persistence/typehandler
│   ├── async
│   └── export
└── modules
    ├── user
    ├── resume
    ├── schedule
    ├── knowledgebase
    ├── interview
    │   ├── evaluation
    │   └── voice
    ├── questionbank
    └── model
```

async、export、evaluation、voice 等目录包含规划与占位，实际能力以 TECH_STACK 和源码为准。业务模块通常包含 controller、service/impl、mapper、domain/po、domain/dto 和 domain/vo。repository 中的历史占位不要求增加一层转发。Mapper 承担数据库访问，PO 到 VO 的转换目前常用 BeanUtils 和 Stream，转换复杂类型时显式处理。

## 模块边界

| 模块 | 职责与调用关系 |
| --- | --- |
| user | 账号认证、注册、密码与邮箱、本人资料。技术性令牌和会话校验由 infrastructure.security 提供 |
| resume | 简历归属、上传去重、回收站、正文解析和分析报告。调用存储与文档基础设施 |
| schedule | 面试日程 CRUD、状态；后续邀请解析和提醒 |
| knowledgebase | 知识库及文档管理；后续切片、向量化、RAG 会话和引用 |
| interview | 本场会话、题目、作答、进度、结束和历史。evaluation 规划为评价职责，voice 规划为语音交互职责 |
| questionbank | 题库原题、参考答案、评分要点、启用归档；向面试提供可用题目 |
| model | 后续模型配置、选择、调用路由、Embedding 和语音供应商适配 |

Controller 负责路由、入参校验和委托；Service 负责编排、权限和 VO 组装；Mapper 访问数据库。跨业务模块通过 Service 访问。基础设施提供技术能力，资源归属与业务删除策略由业务 Service 决定。管理员是角色，管理接口按对应业务模块组织。

## 认证与响应

- 请求身份来自 Spring Security 验证后的 JWT，Authorization 使用 Bearer 前缀。sub 存用户 ID，jti 存本次登录会话标识。
- Spring JWT Decoder 校验签名、有效期与 issuer，LoginSessionValidator 比对 Redis 中的会话标识。重复登录覆盖原会话，退出采用比较并删除；修改和重置密码清除原会话。
- 接口返回 R<T>，包含 code、msg、data。HTTP 状态按实际错误类别返回。错误原因枚举实现 BusinessErrorCode，业务失败使用统一 BusinessException。
- MVC 全局处理器与 Security EntryPoint 分别写出相同的响应结构，详见 [异常规范](EXCEPTION_HANDLING.md)。
- 会话归属在业务 Service 检查。内部 Service 可以返回 PO，由调用方组装对外 VO。
- 旧的 GET /api/user/auth/{userID} 开发查询仍存在，正式本人资料入口为 GET /api/user/me，旧入口需要独立做权限收口审查。

## 文字面试与题库

普通文字面试和知识库专项共用 interview_session、interview_session_question。会话有 user_id；每道主问题和实际提出的追问占题目表一行，保存参考答案、要点、scoring_rubric 等本场快照。会话不再保存 questions_json，也不增加 source_question_id。

题库原题由 questionbank 维护，本场快照归 interview。题库编辑或删除不能改写既有问答。知识库删除后解除会话的 knowledge_base_id，保留来源类型和本场快照，允许继续作答。后续出题和评分依赖本场材料，不回查已删除知识库。

- 会话 Service 检查用户归属、当前题、状态、追问策略并组装 VO。
- 题目 Service 负责本场题目保存、查询、首次答案写入及追加追问。查询始终限定 session_id。
- 主问题按传入列表顺序保存，round_no 从 1 开始，follow_up_no 为 0。
- 首版每轮最多追问一次，实际追问 follow_up_no 为 1；父题 ID 记录被追问的本场题目。候选追问随主问题保存，供现场生成时参考。
- 首次作答条件为 answered_at IS NULL；null 答案转空字符串，同时保存提交时间。白卷也是已提交，答案提交后不可覆盖。
- 文字会话状态为 IN_PROGRESS、COMPLETED；离开页面后仍为进行中。主动结束与最后一题完成都应走结束流程，语音暂停在后续另设计。
- 批量主问题保存失败抛异常，整批回滚。创建会话、保存主问题、设置当前题应在同一事务里完成，模型调用在事务外。

## 数据与资源生命周期

- 主键 BIGINT 映射 Long；TIMESTAMPTZ 映射 OffsetDateTime。时间字段沿用各表实际命名，更新默认值只负责插入，后续更新由应用维护。
- 迁移位于 app/src/main/resources/db/migration；已执行版本不改写，结构变化新增迁移。
- 简历按用户与文件名加内容的哈希去重，回收站也在唯一范围内；相同记录可复用或恢复。知识库文档按同库内容哈希去重，改名复用返回已有文件名。
- JSONB 字符串由 JsonbStringTypeHandler 映射；PO 与 VO 的字段名或类型不同，BeanUtils 不会自动解析。
- 删会话由外键级联删除本场题目；删简历或知识库用 SET NULL 解除会话关联。
- 文件与数据库不共享本地事务。上传落库失败会补偿删除本次对象，保留原异常与清理失败原因。
- 简历/文档批删先删库并取得 RETURNING 结果，再清理对象；整库删除把文档和知识库放入同一事务，对象清理通过 afterCommit 执行。对象清理失败记录对象键和业务 ID，当前由日志支持补删。
- 对外预签名下载与服务端字节下载是不同入口。已发出的预签名在有效期内可能仍可访问，签名前存在性检查不替代后续 RustFS 请求的实际结果。

## 公共配置与接口文档

MybatisPlusConfig 位于 common/config，包含 Mapper 扫描和分页插件。安全、Redis、存储和文档配置按能力放 infrastructure。PageQuery 通过 URL Query 接收页码和条数，筛选 DTO 是否用 JSON 以 Controller 为准。

Apifox 公共规则在 .apifox-helper.properties，读取 Javadoc 的 @folder、@ignore，忽略认证身份参数，导出可空字段，并把 PageQuery 转为 Query 参数。同步后注意旧路径记录可能仍需在 Apifox 清理。

具体模型与语音候选见 [模型计划](MODEL_SELECTION.md)。本机运行与授权要求见 [README](../README.md)。
