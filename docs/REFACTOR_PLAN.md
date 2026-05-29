# 白帽工坊 · 全面重构计划（REFACTOR_PLAN）

> 状态：已与负责人对齐，待 P1 启动。本文件为后续多阶段工作的权威依据。

## 0. 背景与约束

- **目标**：工程最佳实践、可直接投入使用。
- **需求基准**：现有功能**意图不变**，实现可改。
- **自由度**：技术栈、语言、架构、目录均可改。非生产环境、有远程 git 备份，故不惧重构期间中断。
- **决策依据**：与负责人直接讨论确认（2026-05-29）。

## 1. 目标架构

### 1.1 部署拓扑
```
Nginx (反向代理 / TLS / 静态资源)
   ├── /api/**     → 主应用 (Spring Boot 模块化单体)
   ├── /agent/**   → AI Agent 服务 (FastAPI + LangGraph)
   ├── /sandbox/** → 沙箱编排服务 (Spring Boot, 独立)
   └── /           → 前端静态产物 (Vue 构建)

数据层: PostgreSQL (主数据 + pgvector RAG) · Redis (缓存/会话/限流) · 卷/对象存储 (文件/视频)
```

### 1.2 架构风格
**模块化单体 + 两个独立服务**（明确否决完整微服务，理由 YAGNI/KISS）：
- 主应用 = 模块化单体，按域分包，模块间只走接口（保留将来按需拆服务的可能，但不预付微服务成本）。
- 沙箱编排服务独立：直连 Docker daemon、拉起不可信靶场容器，强安全边界。
- AI Agent 服务独立：不同运行时/语言、依赖外部 LLM、独立限流扩缩容。

### 1.3 主应用模块划分
| 模块 | 涵盖现有内容 |
|------|------|
| `auth` | 登录/注册/JWT/鉴权（含 admin 登录） |
| `user` | User · UserProfile · HonoraryTitle · VIP · InviteCode · Reward |
| `challenge` | Challenge · Category · Question · LabFlag · Hole · 答题进度 |
| `community` | Post · Comment · Feedback |
| `chat` | ChatRoom · ChatMessage · WebSocket |
| `competition` | Room/竞赛 · 排名 |
| `content` | News · Announcement · Changelog · Video |
| `analytics` | Stats · Rank · UserAccuracy · 学习行为 · 推荐 |
| `admin` | 后台管理聚合 |

### 1.4 AI Agent 服务
- **能力**：①渐进式提示/答疑（不直接给 flag）②分析提交与学习行为给建议 ③操作靶场沙箱 ④平台知识检索 RAG。
- **框架**：LangGraph / LangChain 编排；FastAPI 承载；LLM 用 DeepSeek。
- **向量库**：PostgreSQL + pgvector（复用主库，不引入独立向量库）。
- **工具与接口**：需与主应用约定数据接口（题目/答题/进度）、与沙箱服务约定动作接口（查看/启动容器）。共享主应用鉴权。

### 1.5 技术选型
- 后端：Spring Boot 3.4 LTS · Java 21 · Spring Data JPA + Flyway · Spring Security + JWT · Validation · springdoc-openapi。
- 前端：Vue 3 + Vite + TS · Element Plus（单库）· Pinia · 单入口 · ESLint + Prettier。
- Agent：Python · FastAPI · LangGraph · pgvector。
- 部署：每服务 Dockerfile + 统一 docker-compose · GitHub Actions CI · Actuator 健康检查 · 结构化日志。
- 配置：全部外部化，零硬编码密钥。

### 1.6 目标目录结构（已确认全面重组）
```
/apps
  /api        Spring Boot 主应用（模块化单体）
  /sandbox    沙箱编排服务（Spring Boot）
  /agent      AI Agent 服务（FastAPI + LangGraph）
  /web        Vue3 前端
  /android    安卓 App
/deploy       docker-compose · nginx · CI 配置
/docs         本计划等文档
```

## 2. 分阶段计划（每阶段产出可运行、可回归）

| 阶段 | 内容 | 依赖 |
|------|------|------|
| **P1 安全加固** | 凭据外部化 + 作废重置泄露密钥 + `.gitignore`/`.env.example` + 鉴权漏洞修补 | 无，立即可做 |
| **P2 骨架与可部署性** | 目标目录重组 + Spring Boot 升级 + Flyway + Docker 化 + compose + CI + Actuator | P1 |
| **P3+P4 数据层迁移与逐域重构（已合并为纵向切片）** | 见下方说明 | P2 |
| **P5 沙箱服务抽取** | Docker 编排 + 安全工具独立成服务，定义与主应用的接口契约 | P4 |
| **P6 AI Agent 服务** | FastAPI + LangGraph，落地 4 能力与工具集，接 pgvector RAG，对接主应用/沙箱契约 | P3, P5 |
| **P7 前端重构** | UI 收敛 Element Plus + Pinia + 单入口，按新接口对接 | P4 |
| **P8 测试与验收** | 单元/集成/E2E 测试体系 + 回归 + 部署演练 | 全部 |

### 2.1 P3+P4 纵向切片说明（已与负责人对齐）

数据层与业务逻辑改动不可分割，故 **P3、P4 合并为按模块纵向切片**执行。关键决策：
- **全新建库，无 ETL**：MongoDB 无需保留数据，不写数据迁移脚本，只设计 schema + JPA 实体。
- **混合映射**：清晰关联（用户-角色、外键）正规化为表；纯值映射/数组（如 `vulnScores`）用 **JSONB** 列兜底。
- **切片节奏**：每模块一次做完 `Flyway schema → JPA 实体 → 仓库 → 适配 service/controller → 验证`，再下一个。顺序 auth/user → challenge → community → chat → competition → content → analytics → admin。
- **共存**：迁移期 MongoDB（未迁模块）与 PostgreSQL/JPA（已迁模块）并存；JPA 仓库与 Mongo 仓库按包分离。
- **MySQL 真相**：`spring.datasource` 原 MySQL 配置为死配置（无业务使用），已改指 PostgreSQL 供 JPA。`MySQLService`（靶场动态建库，硬编码凭据）属沙箱，留待 P5。
- **pgvector**：RAG 专用，推迟到 P6。

**P3.0 地基（已完成）**：pom 加 `spring-boot-starter-data-jpa`/`postgresql`/`flyway-core`/`flyway-database-postgresql`；`spring.datasource` 改 PostgreSQL；JPA `ddl-auto=validate` + Flyway `classpath:db/migration`；编译通过。

## 3. 紧急安全债（P1 必须处理）
已提交进 git 的明文凭据（须外部化 **且作废重置**）：
- `application.properties` MySQL 密码、QQ 邮箱 SMTP 授权码、管理员 bcrypt hash
- `application.yml` 注释残留的 MongoDB 生产密码

## 4. 工作纪律
- 小步推进，一次一个动作；每步动手前先列出涉及文件并确认。
- 不删除未提及的代码；危险操作先确认。
- 中文注释，与现有代码库一致。
- 未经要求不做 git 提交/分支操作。
