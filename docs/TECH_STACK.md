# 技术栈、接入状态与模块概况

更新日期：2026-11-24。适用项目：`D:\ai-interview-assistant`。

本页汇总跨模块接手所需的实际状态。版本依据为构建与配置文件，实现状态通过源码和本地工作区核对。用户手动联调和自动测试结果按实际记录。接手时核对最新提交与本地改动，并在本机根据 Flyway 记录确认迁移执行情况。

## 当前技术栈

| 范围 | 配置依据 | 接入状态 |
| --- | --- | --- |
| Java | app/build.gradle：Java 25 | 已配置工具链 |
| 框架 | gradle/libs.versions.toml：Spring Boot 4.1.1 | Web MVC、Validation、Security、Mail 等已接入 |
| 构建 | Gradle Wrapper 9.8.2；dependency-management 1.1.7 | app 为唯一后端子项目 |
| 持久层 | MyBatis-Plus 3.5.17，Boot 4 Starter | 已用于各模块，含分页插件与 Mapper 扫描；当前不使用 JPA |
| 数据库 | Compose：pgvector/pgvector:pg16 | 已使用 PostgreSQL；尚未建立业务向量检索链路 |
| 迁移 | Spring Boot Flyway Starter、PostgreSQL 支持 | 当前工作区包含 V1～V11 脚本，执行历史按本机 Flyway 记录核实 |
| 缓存 | Compose Redis 7、Spring Data Redis | 已用于登录会话、验证码和发送间隔控制，支持 Lua 原子操作 |
| 认证 | Spring Security、BCrypt、Resource Server JWT | 已实现账号认证、JWT 签发与校验、Redis 会话验证、退出和统一认证响应 |
| 邮件 | Spring Mail / SMTP | 已实现发送验证码，按用途隔离和消费 |
| 存储 | RustFS、AWS SDK S3 2.25.27 | 已实现上传、服务端下载、预签名下载、单个及批量删除 |
| 文档 | Apache Tika 3.3.3 | 已接入类型检测、解析、文本清洗及哈希服务，配置专用解析线程池 |
| JSON | Spring Boot 管理的 Jackson | 项目使用 Jackson 3 的 tools.jackson 包；JSONB 字符串有专用 MyBatis TypeHandler |
| 前端 | Vue ^3.5.42、TypeScript ~6.0.0、Vite ^8.2.2、Vue Router ^5.3.1、Pinia ^4.0.3 | front/ 为初始工程；完整业务页面尚待建设 |
| Node.js | front/package.json：^22.18.0 或 >=24.12.0 | 记录项目要求，实际安装版本另核实 |
| 联调 | Apifox、IDEA Apifox Helper | 根目录配置分组、身份参数忽略、分页 Query 导出与可空字段 |

## 业务模块概况

| 模块 | 已有实现 | 后续范围 | 验证依据 |
| --- | --- | --- | --- |
| user | 注册、登录、退出、验证码、修改和重置密码、改绑邮箱、本人资料及昵称修改 | 持续核对需求中的频控、权限与资料扩展 | 用户已反馈主要认证、邮箱和密码流程手测通过；已有部分自动测试代码 |
| resume | 单个/批量上传、去重与回收站恢复、异步正文解析、详情、分页、预签名下载、回收站操作和批删 | AI 简历分析、报告展示与导出 | 用户已反馈基础接口和多格式正文解析测试通过 |
| schedule | 新增、修改、状态变更、分页筛选、批量删除 | 邀请解析、提醒及前端日历 | 用户已反馈基本 CRUD 和状态接口手测通过 |
| knowledgebase | 知识库创建、改名、分页和整库删除；文档批量上传、分页、单个下载和批删 | 文档处理链路、切片、向量化、RAG 会话与引用 | 用户已反馈这 8 个基础接口的成功、失败及归属用例通过 |
| interview | 会话及本场题目表。已实现会话创建、分页列表、详情与进度恢复、删除、单题作答及推进、主动交卷。当前追问使用固定占位题 | 接入真实 AI 出题、追问与评价 | 当前进度以源码静态核对为依据，接口联调结果待确认 |
| questionbank | V9 建题库表，V10 删除候选追问列。手动创建入口校验完整题目、知识库归属和来源文档，保存为已启用题 | 查询、修改草稿、归档、删除，以及后续的 AI 补题与专项抽题 | 手动创建流程已静态核对，联调结果待确认 |
| model | 包结构预留 | 聊天模型路由与配置、固定 Embedding、语音供应商适配 | 业务尚未实现 |
| interview/evaluation、voice | 包结构预留 | 评价任务、报告、语音面试 | 业务尚未实现 |

主流程仍先完成建表和基础 CRUD。Spring AI 尚未加入依赖，Redis Stream 消费基础设施、AI 评价、业务向量表、RAG 与 PDF 导出留在后续迭代。未来能力的模块职责见 [架构](ARCHITECTURE.md)，验收范围见 [SRS](requirements/SRS.md)。

## 数据迁移入口

| 版本 | 内容 |
| --- | --- |
| V1 | app_user 用户表 |
| V2 | 邮箱仅域名转小写的唯一性规则 |
| V3 | resume、resume_analysis |
| V4 | 用户和简历哈希全记录唯一，包含回收站记录 |
| V5 | interview_schedule |
| V6 | knowledge_base、document |
| V7 | interview_session、interview_session_question |
| V8 | 允许提交空答案，仍要求答案与提交时间成对保存 |
| V9 | question_bank_question，含同库来源文档、修改草稿关联与启用条件 |
| V10 | 删除题库题目的候选追问列及其约束 |
| V11 | 同步题库列注释，说明新题直接启用和修改草稿的用途 |

执行过的迁移通过新增版本修改。数据库最终结构以完整迁移链及本机 Flyway 历史为准。

## 跨模块接手要点

- 统一响应 `R<T>` 包含 code、msg、data，HTTP 状态按异常类型返回。本人身份来自认证后的 JWT。
- 用户邮箱唯一性保留本地部分大小写，仅域名不区分大小写。
- 简历按用户、文件名与内容共同计算的哈希去重；知识库文档按知识库与内容哈希去重，复用时返回原记录文件名。
- 简历是回收站加物理删除；日程物理删除；文档和整库删除先完成数据库删除，再清理对象存储，清理失败记日志供补删。当前没有自动补删任务。
- 知识库文档每批最多 5 份，每份最多 50 MiB；上传失败逐项返回。基础存储能力不承担资源归属判断。
- 预签名下载当前有效期 1 分钟。签名前检查对象存在，浏览器随后直接访问 RustFS。
- 前端分页走 URL Query，筛选对象按 Controller 的请求体声明传。避免同名分页参数同时出现在 Query 和表单。
- `GET /api/user/auth/{userID}` 旧开发查询入口仍在代码中；普通用户本人资料使用 `GET /api/user/me`，旧入口权限收口需单独审查。
- 参考项目是 D:\interview-guide，其 JPA、React 或完成情况不代表本仓库采用相同技术或已实现同样功能。
