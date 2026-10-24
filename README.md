# 智能面试辅助系统

基于 Java、Spring Boot 和 Vue 的个人毕业设计项目，当前处于工程骨架搭建阶段。需求范围见 [软件需求规格说明书](docs/requirements/SRS.md)，模型计划见 [模型选型](docs/MODEL_SELECTION.md)。

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
