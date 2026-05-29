# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 交流约定

- 无论用什么语言提问，都用**中文**回复。
- 代码注释语言与现有代码库保持一致（中文）。

## 项目概述

**白帽工坊**是一个网络安全 CTF 靶场 / 学习社区平台。仓库为多组件 monorepo，包含四个独立可部署的工程：

| 目录 | 技术栈 | 角色 |
|------|--------|------|
| `apps/api/` | Spring Boot 3 / Java 17 / Maven | 主后端 API（用户、靶场、社区、竞赛、聊天、文件） |
| `apps/agent/` | Flask 3 / Python | AI 聊天微服务（对接 DeepSeek，独立进程） |
| `apps/web/` | Vue 3 + Vite 6 + TypeScript | Web 前端 |
| `apps/android/` | Android（Gradle Kotlin DSL） | 配套安卓视频 App |

> 注意：仓库根目录还存有大量文档/演示文件（`*.pdf`、`*.docx`、`*.apk`、`视频1.mp4`、`White Hat Workshop/` 等），非代码资产，通常无需改动。

## 常用命令

### 主后端（`apps/api/`）

```bash
cd apps/api
./mvnw spring-boot:run          # 启动后端，监听 http://localhost:8080
./mvnw clean package            # 编译打包
./mvnw test                     # 运行测试（仅有占位测试 CsStudentApplicationTests）
./mvnw test -Dtest=类名#方法名   # 运行单个测试
```

### AI 聊天微服务（`apps/agent/`）

```bash
cd apps/agent
pip install -r requirements.txt
python app.py                   # 端口由 .env 的 FLASK_PORT 决定（默认 5000）
```

配置全部来自环境变量 / 根目录 `.env`（见 `config.py`）：`DEEPSEEK_API_KEY`、`DEEPSEEK_API_URL`、`MONGODB_URI`、`REDIS_HOST/PORT`、`JWT_SECRET`（须与 Spring Boot 的 JWT 密钥一致以共享登录态）、`ALLOWED_ORIGINS`。

### 前端（`apps/web/`）

```bash
cd apps/web
npm install
npm run dev          # 开发服务器（Vite 默认端口 5173）
npm run build        # 先 vue-tsc 类型检查再 vite build
npm run type-check   # 仅类型检查（vue-tsc --build）
npm run preview      # 预览构建产物
```

`vite.config.ts` **没有配置 `/api` 代理**，前端通过 `apps/web/src/axios` 中配置的 baseURL 直连后端，调试跨域请确认后端 CORS / 该 baseURL。

### 安卓 App（`apps/android/`）

Gradle Kotlin DSL 工程，`./gradlew assembleDebug` 构建。使用 Retrofit + Gson 调接口、ExoPlayer 播放视频。

### 依赖服务

- **MongoDB**（主数据存储，库名 `CsNet`，默认 `127.0.0.1:27017`）—— Spring Boot 与 Flask 共用。
- **MySQL**（次要存储，仅通过原生 JDBC 使用）—— 见 `application.properties` 的 `spring.datasource.*`。
- **Redis**（Flask 聊天/限流使用）。
- **Docker daemon**（靶场容器，地址见 `application.yml` 的 `docker.host`，支持 TLS）。

后端配置分散在两处：`application.yml`（MongoDB、Docker、MVC、日志）+ `application.properties`（MySQL、邮件、文件存储路径、管理员账号 `admin.username`）。两文件含硬编码的远程地址/凭据，本地运行需按环境覆盖。

## 架构要点

### 主后端分层（`com.bmgf`）

Controller → Service（`service/` 接口 + `service/impl/`）→ 数据访问。数据访问有两条路径：

- **MongoDB（主）**：`dao/` 下是 Spring Data MongoDB 的 `*Repository` 接口；实体（POJO）在 `po/`，直接以文档形式持久化（无 ORM 映射层、无 MyBatis）。
- **MySQL（次）**：通过 `service/MySQLService` 用原生 `JdbcTemplate`/JDBC 访问，仅用于少量场景。

其余关键模块：

- **统一响应**：接口返回 `po/Result`，前端据其 `code` 判断成败。业务异常由 `Config/GlobalExceptionHandler`（`@RestControllerAdvice`）统一捕获。
- **认证授权**：JWT 无状态认证。`util/JwtUtil` 签发/校验，`util/JwtAuthenticationFilter` 解析 token，`util/CustomUserDetails` 为 Spring Security 用户主体。放行规则见 `Config/SecurityConfig`——登录/注册（`/user/login`、`/user/register`、`/admin/login`）、静态资源（`/avatar/**`、`/images/**`、`/video/**`、`/upload/**`）、部分社区 GET（`/post/findAll`、`/post/findById`、`/comments/find/**` 等）、`/api/news/**`、`/api/public/**`、`/ws/**` 公开；`/admin/**` 需 `ROLE_ADMIN`，其余需认证。
- **Docker 靶场**：`Config/DockerConfig` 配置 docker-java 客户端；靶场实体含 `po/ContainerInstance`、`po/ComposeEnvironment`、`po/ImageConfig`，相关接口在 `LabController` / `VipController`（`/lab/create-compose`、`vip/create-compose`）。
- **WebSocket**：`handler/ChatWebSocketHandler`（路径 `/ws/chat`）与 `handler/ChatOverviewWebSocketHandler`（`/ws/overview`），注册见 `Config/WebSocketConfig`；聊天室状态由 `Manger/ChatRoomManager` 管理（注意目录名拼写为 `Manger`）。
- **异步与定时**：`Config/AsyncConfig`、`Config/SchedulerConfig`；文件存储路径策略在 `Config/StorageConfig`（视频/头像/帖子/反馈分目录，落盘到 `uploads/`）。
- **DTO**：`DTO/` 存放请求/响应传输对象；登录注册请求体 `util/LoginRequest`、`util/RegisterRequest`。

### 业务域（按 controller / po 划分）

用户与资料（`User`/`UserProfile`/`HonoraryTitle`）、靶场与题目（`Challenge`/`Category`/`Question`/`LabFlag`/`Hole`/漏洞）、答题与进度（`AnswerRecord`/`ExamRecord`/`UserChallengeProgress`/`CompletedChallenge`）、社区（`Post`/`Comment`）、聊天（`ChatRoom`/`ChatMessage`）、运营（`Announcement`/`News`/`Changelog`/`Feedback`/`InviteCode`/`Reward`/`Vip`）、统计与推荐（`Stats`/`RankVO`/`UserAccuracy`/`UserLearningBehavior` + `RecommendationController`）、视频（`VideoItem`/`Video_Category`）。

### 前端结构（`apps/web/src`）

- **构建特性**：TypeScript + `unplugin-auto-import` + `unplugin-vue-components` 自动导入（生成 `auto-imports.d.ts`、`components.d.ts`，勿手改）。同时存在 `main.js` 与 `main.ts`，以 `index.html` 实际引用的入口为准。
- **UI/库**：Vuetify 3 + Element Plus 双 UI 库；ECharts 图表、md-editor-v3/marked + highlight.js 渲染、Phaser（小游戏）、three.js、GSAP 动画。
- **状态管理**：**Vuex**（`store/`）+ `vuex-persistedstate` 持久化（**非 Pinia**）。
- **请求层**：`axios/` 为 axios 实例（注入 token、统一处理响应），`api/` 封装各业务接口。
- **路由/视图**：`router/` 路由，`views/` 按业务域分目录。
- **路径别名**：`@` → `apps/web/src`。

## 约定

- 主后端无 MyBatis-Plus、无逻辑删除字段约定；持久化以 MongoDB 文档为主，改数据访问优先在 `dao/*Repository` 上扩展查询方法。
- 改后端配置时注意 `application.yml` 与 `application.properties` 双文件并存，且含写死的远程地址/密钥，提交前勿泄露或覆盖他人环境值。
- 前端改动避免手工编辑自动生成的 `*.d.ts`（`auto-imports.d.ts`、`components.d.ts`）。
