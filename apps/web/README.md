# BaiMaoGongFang 前端项目

本项目基于 **Vue 3** + **Vite**，集成了 Element Plus、Vuetify、Vuex、Vue Router、Echarts 等现代前端技术栈，是一个功能丰富的网络安全学习与交流平台。

## 目录结构

```
Frontend/
├── public/                # 公共静态资源（图片、模型、视频等）
├── src/
│   ├── api/               # API 接口封装
│   ├── assets/            # 静态资源（图片、样式等）
│   ├── axios/             # axios 实例与拦截器
│   ├── components/        # 业务组件
│   │   ├── home/          # 首页及社区相关组件
│   │   ├── backMange/     # 后台管理相关组件
│   │   ├── gameView/      # 游戏相关组件
│   │   ├── app_download/  # APP 下载页组件
│   │   ├── ai_answer/     # AI 问答相关组件
│   │   ├── news/          # 新闻相关组件
│   │   ├── emailverify/   # 邮箱验证组件
│   │   └── LearningGuide/ # 学习指南组件
│   ├── plugins/           # 插件（如 Vuetify 配置）
│   ├── router/            # 路由配置
│   ├── store/             # Vuex 状态管理
│   ├── styles/            # 全局样式
│   ├── types/             # 类型定义
│   ├── utils/             # 工具函数
│   ├── App.vue            # 根组件
│   └── main.ts            # 入口文件
├── index.html             # 入口 HTML
├── package.json           # 项目依赖与脚本
├── tsconfig*.json         # TypeScript 配置
├── vite.config.ts         # Vite 配置
└── README.md              # 项目说明文档
```

## 主要依赖

- **Vue 3**：主流渐进式前端框架
- **Vite**：极速开发与构建工具
- **Element Plus**：UI 组件库
- **Vuetify**：Material Design 风格 UI 组件库
- **Vuex**：状态管理
- **Vue Router**：前端路由
- **Echarts**：数据可视化
- **axios**：HTTP 请求库
- **lodash-es**、**highlight.js**、**marked** 等工具库

## 快速开始

### 安装依赖

```bash
npm install
```

### 本地开发

```bash
npm run dev
```

### 生产环境构建

```bash
npm run build
```

### 预览生产环境

```bash
npm run preview
```

### 类型检查

```bash
npm run type-check
```

## 核心功能模块

### 1. 用户系统
- 登录/注册功能
- 邮箱验证
- 用户角色管理（普通用户、VIP用户、管理员）
- 个人中心

### 2. 社区功能
- 社区讨论区
- 公共讨论
- 问答系统
- 奖励机制
- 个人收藏

### 3. 实战工具集
- CMS 探测工具
- Base64 编解码
- IP 查询工具
- Hash 生成器
- 密码强度校验
- 子域名扫描
- 端口扫描
- URL 编码/解码
- 凯撒密码工具
- Unicode 转换

### 4. 游戏与学习
- 普通游戏模式
- VIP 专属游戏模式
- SQL 注入练习
- XSS 注入练习
- 学习指南
- 漏洞靶场

### 5. AI 问答系统
- 智能问答
- 技术咨询
- 学习辅导

### 6. 新闻系统
- 新闻列表
- 新闻详情
- 新闻管理

### 7. 后台管理系统
- 用户管理
- 评论管理
- 数据统计
- 视频管理
- BUG 管理
- 分类管理
- 问题管理
- 奖励管理
- 公告管理
- 新闻管理
- 趋势分析
- 系统设置
- 帮助文档
- 个人资料
- 反馈管理

### 8. 其他功能
- APP 下载
- 帮助中心
- 反馈系统
- 邮箱验证
- 消息中心

## 路由与页面

### 主要路由
- `/bmgf/login` - 登录/注册页
- `/bmgf/home` - 主页
- `/bmgf/community` - 社区
- `/bmgf/tools/*` - 实战工具集
- `/bmgf/game/*` - 游戏与练习
- `/bmgf/chat` - AI 问答
- `/bmgf/news/*` - 新闻系统
- `/bmgf/admin/*` - 后台管理
- `/bmgf/verify-email` - 邮箱验证

### 权限控制
- 普通用户权限
- VIP 用户权限
- 管理员权限
- 路由守卫保护

## 技术特点

- 支持 Element Plus 与 Vuetify 双 UI 框架
- 完整的 TypeScript 支持
- 模块化的组件设计
- 响应式布局，支持多端适配
- 完善的权限管理系统
- 丰富的安全工具集成
- 现代化的开发体验

## 开发建议

- 推荐使用 [VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) 进行开发
- 遵循 TypeScript 规范
- 组件开发按业务模块分类
- 工具函数统一管理
- 类型定义规范化

## 参考文档

- [Vue 3 官方文档](https://v3.vuejs.org/)
- [Vite 官方文档](https://vitejs.dev/)
- [Element Plus](https://element-plus.org/)
- [Vuetify](https://vuetifyjs.com/)

---