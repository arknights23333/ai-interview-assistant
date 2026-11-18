# 智能面试辅助系统

基于 Java、Spring Boot 和 Vue 的个人毕业设计项目。当前按“先完成各模块建表和基础 CRUD，再接入 AI”的顺序推进。用户认证、简历、日程及知识库文件管理已有基础实现，文字面试正在建设，AI 出题、评价与 RAG 留在后续阶段。

## 新聊天与模块接手入口

1. 阅读本文，确认正在操作本项目，了解工程结构和开发约定。
2. 阅读 [技术栈与模块状态](docs/TECH_STACK.md)，定位已有能力、验证范围和待实现模块。
3. 阅读 [架构说明](docs/ARCHITECTURE.md)，了解模块职责、调用边界和数据生命周期。
4. 在 [SRS](docs/requirements/SRS.md) 中只读取目标模块的需求、关联业务规则和验收用例。
5. 阅读目标模块的 Controller、Service、DTO/VO、PO 及迁移脚本，核对实际代码与工作区改动。
6. 按需要补读 [异常规范](docs/EXCEPTION_HANDLING.md)、[邮箱约定](docs/MAIL_VERIFICATION.md) 或 [模型计划](docs/MODEL_SELECTION.md)。

本文档适用于本仓库。开发目录为 `D:\ai-interview-assistant`；`D:\interview-guide` 是本地参考项目，两者的技术栈和实现不可混用。切换模块时继续维护上述文档，需求决策写入 SRS 或架构，模块完成情况写入 TECH_STACK。

## 协作约定

- 使用简体中文。讨论与静态检查阶段读取代码；用户明确要求修改时再写文件。
- 未经明确要求，只做静态检查，不执行编译、测试或构建。文档中的命令是供授权后使用的参考。
- Git 仅允许本地提交，禁止推送；提交须有用户授权。
- 只访问本机部署的服务，禁止连接公司内网或其他非本地服务。
- 先定需求与字段，再编排业务。按用户指定的范围实施，预留方法和 DTO 不代表功能已完成。
- 密钥、Token 和个人启动配置不进入文档或 Git。

## 工程结构

```text
ai-interview-assistant/
├── app/                      # 唯一后端应用子项目
│   ├── build.gradle
│   └── src/
│       ├── main/java/com/assistant/aiinterviewassistant/
│       ├── main/resources/db/migration/
│       └── test/
├── front/                    # Vue 3 + TypeScript + Vite
├── docs/                     # 需求、架构、接入状态与专题约定
├── gradle/                   # Wrapper 与版本目录
├── docker-compose.yml        # 本机 PostgreSQL、Redis、RustFS
├── build.gradle
├── settings.gradle
├── gradlew
└── gradlew.bat
```

后端使用 Java 25、Spring Boot 4.1.1、MyBatis-Plus 3.5.17、PostgreSQL、Redis、Flyway、RustFS/S3 与 Apache Tika。前端使用 Vue，详见 [前端说明](front/README.md)。实际版本以构建文件为准，接入状态集中维护在 TECH_STACK。

## 本机开发参考

先在 IDEA 启动配置中准备应用需要的环境变量，参照 `app/src/main/resources/application.yaml` 的占位符；邮件授权码和对象存储密钥使用本机配置。Compose 容器环境与 IDEA 应用环境各自配置。个人启动配置、环境文件和本地学习计划由忽略规则管理，不能假定克隆仓库即可获得它们。

以下命令仅在用户授权相应操作后执行。

```powershell
docker compose up -d
.\gradlew.bat :app:bootRun
```

前端命令见 [front/README.md](front/README.md)。编译或验证获授权后，可在根目录使用：

```powershell
.\gradlew.bat :app:compileJava
.\gradlew.bat :app:test --no-daemon
npm.cmd --prefix front run build
```

部分后端测试需要本机 PostgreSQL、Redis、RustFS 或邮件配置；存在测试文件不表示最近执行通过。验证记录区分静态检查、用户手动联调和自动测试结果。
