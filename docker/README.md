# 白帽工坊 · Docker 一键部署

针对 [Issue #2](https://github.com/Songwo/BaiMaoGongFang/issues/2)「搭建难度太大」做的容器化方案：
一条命令拉起 **MongoDB + Redis + SpringBoot 主后端 + Flask AI 后端 + Vue3 前端**。

## 前置条件

- 安装 [Docker](https://docs.docker.com/get-docker/)（含 Docker Compose 插件，Docker Desktop 自带）
- 2 核 4G 以上内存（SpringBoot 构建较吃资源）

## 一键启动

```bash
# 1. 克隆仓库
git clone https://github.com/Songwo/BaiMaoGongFang.git
cd BaiMaoGongFang

# 2. 准备环境变量（可选：AI 问答需要填 DeepSeek Key）
cp .env.example .env
# 编辑 .env，填入 DEEPSEEK_API_KEY（不填也能跑，只是 AI 问答不可用）

# 3. 一键启动（首次会构建镜像，约 5~15 分钟）
docker compose up -d --build

# 4. 浏览器打开
# http://localhost
```

## 服务一览

| 服务     | 说明                  | 端口              |
| -------- | --------------------- | ----------------- |
| frontend | Vue3 前端（nginx）    | 80 → http://localhost |
| backend  | SpringBoot 主后端     | 8080              |
| flask    | Flask AI 问答后端     | 5000              |
| mongo    | MongoDB 7             | 仅内网            |
| redis    | Redis 7               | 仅内网            |

## 常用命令

```bash
docker compose ps              # 查看服务状态
docker compose logs -f backend # 看某个服务的日志
docker compose down            # 停止并删除容器（数据卷保留）
docker compose down -v          # 停止并删除所有数据（慎用）
```

## 部署到远程服务器

前端在构建时把后端地址写死进去了，部署到服务器时需要重建 frontend：

```bash
# 在 .env 里取消注释并改成服务器地址，例如：
# VITE_API_URL=http://1.2.3.4:8080
# VITE_AI_URL=http://1.2.3.4:5000/api/chat
docker compose up -d --build frontend
```

然后在服务器安全组放行 80 / 8080 / 5000 端口。

## 说明

- MongoDB / Redis 默认无密码，仅监听 compose 内网，不对外暴露，本地/自用场景够用；
  如需公网暴露请自行加密码。
- CTF 动态靶场需要访问宿主机 Docker，已挂载 `/var/run/docker.sock`；
  不用 CTF 功能可在 `docker-compose.yml` 里删掉相关两行。
- `BackendFlask/.env` 不要提交真实密钥到仓库（详见主 README 安全提示）。
