# 白帽工坊（BaiMaoGongFang）- 全栈网络安全学习平台

本项目是一个综合性的网络安全学习平台，包含 Web 前端（Vue3）、后端服务（SpringBoot & Python Flask）、微信小程序和安卓 App。采用 MongoDB 和 Redis 作为数据存储，已上线运行。

## 目录结构

```
/
├── Frontend/                # Vue3 前端源码
├── BackendSpringBoot/       # Java SpringBoot 后端（主API、CTF、用户等）
├── BackendFlask/            # Python Flask 后端（AI 问答、聊天等）
├── White Hat Workshop/      # 微信小程序源码
├── VideoApp/                # 安卓 App 源码
```

## 技术栈

- **前端：** Vue3、Vite、Element Plus、Vuetify、Vuex、Vue Router、Echarts、TypeScript
- **后端：** SpringBoot（Java 17）、Python Flask
- **小程序：** 微信小程序（WXML、WXSS、JS）
- **App：** Android（Kotlin/Java, ExoPlayer, Retrofit, Glide）
- **数据库：** MongoDB、Redis
- **其他：** Docker（CTF 靶场）、JWT、RESTful API

## 主要功能

- **实战 CTF 靶场：** 基于 Docker 动态创建攻防环境，支持 flag 验证与积分。
- **系统化课程：** 安全课程、视频学习与答疑。
- **安全工具：** 在线 CMS 指纹识别、Base64 编解码、IP 查询、哈希生成、密码强度检测等。
- **AI 问答：** 集成 AI 聊天机器人（Flask 后端）。
- **社区互动：** 论坛、发帖、评论、奖励、排行榜。
- **多端适配：** 支持 Web、小程序、安卓 App。
- **后台管理：** 用户/内容/视频/漏洞管理。

## Docker 一键部署（推荐）

```bash
git clone https://github.com/Songwo/BaiMaoGongFang.git
cd BaiMaoGongFang
cp .env.example .env   # 可选：填入 DEEPSEEK_API_KEY 以启用 AI 问答
docker compose up -d --build
```

浏览器打开 http://localhost 即可。详细说明见 [docker/README.md](docker/README.md)。

> ⚠️ 安全提示：`BackendFlask/.env` 曾包含真实密钥并已提交到仓库，请务必轮换
> DeepSeek API Key、JWT_SECRET 和 MongoDB 密码，不要再把真实密钥提交到公开仓库。

## 快速启动

### 1. 前端（Vue3）

```bash
cd Frontend
npm install
npm run dev         # 开发环境
npm run build       # 生产构建
npm run preview     # 预览生产包
```

### 2. 后端（SpringBoot）

```bash
cd BackendSpringBoot
# 编辑 src/main/resources/application.yml 配置数据库/Docker
mvn clean package
java -jar target/*.jar
```

### 3. 后端（Flask，AI 问答）

```bash
cd BackendFlask
python -m venv .venv
source .venv/bin/activate      # Windows 用 .venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env           # 编辑 .env 配置 MongoDB/Redis/OpenAI
python app.py
```

### 4. 微信小程序

- 用微信开发者工具导入 `White Hat Workshop` 目录即可。

### 5. 安卓 App

- 用 Android Studio 打开 `VideoApp`，同步 Gradle，编译运行。

### 6. 数据库

- 请确保 MongoDB 和 Redis 已启动并与后端配置一致。

---

## 部署脚本说明

### 前端

- `npm run build` 生成静态文件到 `Frontend/dist/`
- 将 `dist/` 部署到 Web 服务器（如 Nginx、Apache 等）

### SpringBoot 后端

- `mvn clean package` 构建
- `java -jar target/CsStudent-*.jar` 运行
- 数据库、Redis、Docker 配置见 `application.yml`

### Flask 后端

- `pip install -r requirements.txt` 安装依赖
- `.env` 配置环境变量（参考 .env.example）
- `python app.py` 启动服务

### 小程序

- 微信开发者工具导入并运行

### 安卓 App

- Android Studio 构建或 `./gradlew assembleDebug` 打包

---

## 简略接口文档

### SpringBoot 主要接口

- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录（返回 JWT）
- `GET /user/mes/{username}` - 获取用户信息
- `PUT /user/changepwd` - 修改密码
- `PUT /user/update` - 更新用户信息
- `GET /user/rank` - 获取排行榜
- `GET /user/videos` - 视频列表
- `POST /lab/create` - 创建 CTF 靶场（Docker）
- `POST /lab/flag` - 提交 flag 验证

### Flask AI 问答接口

- `POST /api/chat` - AI 问答（需 JWT）
  - 请求：`{ "message": "你的问题" }`
  - 响应：`{ "reply": "AI 答案" }`
- `GET /api/chat/history` - 获取聊天历史（需 JWT）

> **说明：** 需登录的接口需在 Header 中携带 `Authorization: Bearer <token>`

---

## 环境变量

- **SpringBoot：** 编辑 `src/main/resources/application.yml` 配置数据库、Redis、Docker 等
- **Flask：** 复制 `.env.example` 为 `.env` 并设置：
  - `MONGODB_URI`
  - `REDIS_HOST`, `REDIS_PORT`
  - `JWT_SECRET`
  - `DEEPSEEK_API_KEY`（AI 问答）

---

## 贡献

- 欢迎 issue、PR 和 star！
- 请遵循代码风格和提交规范。
- 重大更改请先提 issue 讨论。

## 联系方式

- 邮箱：zhaoqsnyah@gmail.com
- [在线演示](http://wacyg.fun)
- [App 下载](http://wacyg.fun/apk/白帽工坊.apk)

---

*警告：任何未经授权的访问、攻击或数据窃取行为均属违法，必将追究法律责任。*

---