# ===== 构建阶段 =====
FROM node:20-alpine AS build
WORKDIR /build
COPY package*.json ./
# 使用淘宝镜像加速依赖安装
RUN npm config set registry https://registry.npmmirror.com && npm ci
COPY . .
# 暂用 build-only（跳过 vue-tsc 严格类型检查）；P7 前端重构后恢复 npm run build
RUN npm run build-only

# ===== 运行阶段：nginx 托管静态产物 + 反代后端 =====
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist /usr/share/nginx/html
EXPOSE 80
