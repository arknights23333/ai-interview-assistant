# 前端工程

本目录使用 Vue 3、TypeScript、Vite、Vue Router 和 Pinia，为前端初始工程。跨模块业务范围和接入状态见 [项目入口](../README.md) 与 [模块概况](../docs/TECH_STACK.md)。

Node.js 要求为 ^22.18.0 或 >=24.12.0，依赖范围以 package.json 为准，锁定版本以 package-lock.json 为准。

## 授权后的本机操作参考

在 front 目录执行：

```powershell
npm.cmd ci
npm.cmd run dev
```

编译或检查需要用户明确授权；以下为可用命令说明。

| 命令 | 作用 |
| --- | --- |
| npm.cmd run build | 类型检查并构建 |
| npm.cmd run type-check | TypeScript 检查 |
| npm.cmd run build-only | Vite 构建 |
| npm.cmd run preview | 预览已有构建产物 |
| npm.cmd run lint | oxlint 与 ESLint，包含自动修复，会修改文件 |
| npm.cmd run format | Prettier 格式化 src，会修改文件 |

后端统一响应为 code、msg、data，认证头为 Authorization: Bearer 加 Token。业务页面按模块接入时核对 Controller、DTO/VO 和 Apifox 文档。
