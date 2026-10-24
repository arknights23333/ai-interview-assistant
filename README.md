# 智能面试辅助系统

基于 Java、Spring Boot 和 Vue 的个人毕业设计项目，当前处于基础工程与认证模块开发阶段。需求范围见 [软件需求规格说明书](docs/requirements/SRS.md)，模型计划见 [模型选型](docs/MODEL_SELECTION.md)，版本依据与接入状态见 [技术栈](docs/TECH_STACK.md)。

## 工程结构

```text
ai-interview-assistant/
├── app/                      # 唯一的后端应用子项目
│   ├── build.gradle
│   └── src/
├── front/                    # Vue 3 + TypeScript + Vite 前端
├── docs/                     # 需求、模型选型与设计文档
├── gradle/                   # Gradle Wrapper 与版本目录
├── docker-compose.yml        # PostgreSQL、Redis 开发环境
├── build.gradle              # 根工程插件声明
├── settings.gradle           # 仅包含 app 后端子项目
├── gradlew
└── gradlew.bat
```

业务包与协作边界见 [后端结构说明](docs/ARCHITECTURE.md)。

## 技术栈与当前进度

- 后端：Java 25、Spring Boot 4.1.1、Gradle 9.8.2、MyBatis-Plus 3.5.17；已从初始 JPA 骨架切换至 Mapper 持久层。
- 数据：PostgreSQL 16（开发镜像包含 pgvector）、Redis 7、Flyway；首份用户表迁移已建立。
- 安全：选定 Spring Security + JWT + Redis + BCrypt；当前只验证了默认 Basic Auth 下的用户查询，正式账号认证和 Token 会话尚未完成。
- 前端：Vue 3、TypeScript、Vite、Vue Router、Pinia，代码在 `front/`；精确版本范围见其 package.json。
- 联调：Apifox Helper 从代码与 Javadoc 同步接口，根目录保存公共规则，个人令牌不提交。
- 后续：Spring AI、RAG/pgvector 业务检索、Redis Stream、S3 兼容存储和邮箱发送尚待接入。

用户模块已有 `UserPo`、`UserVo`、`UserMapper`、`UserService` / `UserServiceImpl`、统一响应 `R<T>`；开发查询接口 `GET /api/user/auth/{userID}` 已由项目负责人确认联调成功。此接口当前按传入 ID 查询，不是正式登录接口，也不代表已实现本人数据隔离。

## 本地开发

后端使用 JDK 25，通过根目录 Gradle Wrapper 构建。前端 Node.js 要求以 [package.json](front/package.json) 为准。下面命令在 Windows PowerShell 中执行。

在项目根目录启动开发依赖：

```powershell
docker compose up -d
```

启动后端：

```powershell
.\gradlew.bat :app:bootRun
```

前端首次安装依赖并启动：

```powershell
cd front
npm.cmd ci
npm.cmd run dev
```

本地后端连接配置位于 [application.yaml](app/src/main/resources/application.yaml)。现有后端测试依赖该配置中的 PostgreSQL 和 Redis 可访问。

## 验证

以下命令均在项目根目录执行：

```powershell
.\gradlew.bat :app:test --no-daemon
.\gradlew.bat :app:bootJar --no-daemon
npm.cmd --prefix front run build
docker compose config --quiet
```

后端构建产物位于 `app/build/`，前端构建产物位于 `front/dist/`。本地学习计划和含凭证的环境文件由 Git 忽略，不随项目提交。
