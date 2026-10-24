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

每个业务模块建立 `controller`、`service`、`repository`、`entity`、`dto`、`mapper` 包。`package-info.java` 说明包职责并使目录随 Git 保留；目前没有对应的业务实现类。

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

1. 请求由 Controller 接入，Service 编排业务，Repository 访问本模块数据。
2. 资源归属检查由对应业务 Service 执行，身份来自经过验证的当前用户。
3. 跨模块调用通过对方提供的 Service 方法；不直接读写其他模块的 Repository。
4. 面试可调用简历、题库和模型服务；题库可调用知识库和模型服务；模型能力不反向依赖面试等业务流程。
5. 文件存储、异步执行和 PDF 生成放在 infrastructure；具体上传权限、任务状态和报告内容仍属于业务模块。
6. 模型连接与厂商协议由 model 负责；简历分析内容、面试追问与评分规则由对应业务模块负责。
7. 外部 AI、文件存储和 HTTP 调用不放在长数据库事务中。

文字、语音是面试交互方式，LLM 生成与题库是题目来源。这些场景共用面试与评价核心数据，避免重复建立独立的问答和评分体系。

管理员是角色，管理接口放在对应业务模块。面试历史和报告归 interview，简历分析历史归 resume；页面入口不直接决定后端模块边界。

## 开发与验证入口

在根目录使用 `gradlew.bat :app:bootRun` 启动后端，使用 `gradlew.bat :app:test --no-daemon` 运行现有测试。前端命令在 `front` 中执行，Docker Compose 命令在根目录执行。

需求来源及优先级见 [软件需求规格说明书](requirements/SRS.md)。模型策略见 [模型选型](MODEL_SELECTION.md)。本文定义包结构，不改变需求优先级或声明业务已完成。
