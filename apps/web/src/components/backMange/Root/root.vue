<template>
  <el-container class="admin-container">
    <!-- 加载进度条 -->
    <div class="loading-progress" v-if="isLoading">
      <el-progress 
        :percentage="progress" 
        :show-text="false" 
        :stroke-width="2" 
        :color="'#409EFF'"
      />
    </div>

    <!-- 侧边栏 -->
    <el-aside width="200px" class="admin-aside">
      <!-- 渐变标题 -->
      <div class="admin-title">
        <img src="@/assets/logo/logo/信息.png" alt="Logo" class="admin-logo" />
        <span>白帽工坊管理平台</span>
      </div>

      <el-menu 
        :default-active="activeMenu" 
        active-text-color="#409EFF" 
        background-color="#f0f8ff" 
        class="admin-menu"
      >
        <!-- 首页 -->
        <RouterLink :to="homePath" class="router-link">
          <el-menu-item index="0">
            <el-icon>
              <HomeFilled />
            </el-icon>
            <span>首页</span>
          </el-menu-item>
        </RouterLink>

        <!-- 内容管理 -->
        <el-sub-menu index="1">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </template>
          <RouterLink :to="commentPath" class="router-link">
            <el-menu-item index="1-1">
              <el-icon><ChatDotRound /></el-icon>
              <span>评论管理</span>
            </el-menu-item>
          </RouterLink>
          <RouterLink :to="videoPath" class="router-link">
            <el-menu-item index="1-2">
              <el-icon><VideoCamera /></el-icon>
              <span>视频管理</span>
            </el-menu-item>
          </RouterLink>
          <RouterLink :to="announcementPath" class="router-link">
            <el-menu-item index="1-3">
              <el-icon><Bell /></el-icon>
              <span>公告管理</span>
            </el-menu-item>
          </RouterLink>
          <RouterLink :to="newsPath" class="router-link">
            <el-menu-item index="1-4">
              <el-icon><Document /></el-icon>
              <span>新闻管理</span>
            </el-menu-item>
          </RouterLink>
        </el-sub-menu>

        <!-- 用户管理 -->
        <el-sub-menu index="2">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <RouterLink :to="userPath" class="router-link">
            <el-menu-item index="2-1">
              <el-icon><User /></el-icon>
              <span>用户列表</span>
            </el-menu-item>
          </RouterLink>
          <RouterLink :to="rewardPath" class="router-link">
            <el-menu-item index="2-2">
              <el-icon><Trophy /></el-icon>
              <span>奖励管理</span>
            </el-menu-item>
          </RouterLink>
        </el-sub-menu>

        <!-- 系统管理 -->
        <el-sub-menu index="3">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <RouterLink :to="categoryPath" class="router-link">
            <el-menu-item index="3-1">
              <el-icon><Folder /></el-icon>
              <span>分类管理</span>
            </el-menu-item>
          </RouterLink>
          <RouterLink :to="questionPath" class="router-link">
            <el-menu-item index="3-2">
              <el-icon><EditPen /></el-icon>
              <span>题目管理</span>
            </el-menu-item>
          </RouterLink>
          <RouterLink :to="bugPath" class="router-link">
            <el-menu-item index="3-3">
              <el-icon><Warning /></el-icon>
              <span>漏洞管理</span>
            </el-menu-item>
          </RouterLink>
          <RouterLink :to="feedbackPath" class="router-link">
            <el-menu-item index="3-4">
              <el-icon><ChatDotRound /></el-icon>
              <span>反馈管理</span>
            </el-menu-item>
          </RouterLink>
        </el-sub-menu>

        <!-- 数据分析 -->
        <el-sub-menu index="4">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>数据分析</span>
          </template>
          <RouterLink :to="dataPath" class="router-link">
            <el-menu-item index="4-1">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </el-menu-item>
          </RouterLink>
          <RouterLink :to="trendPath" class="router-link">
            <el-menu-item index="4-2">
              <el-icon><TrendCharts /></el-icon>
              <span>趋势分析</span>
            </el-menu-item>
          </RouterLink>
        </el-sub-menu>

        <!-- 帮助中心 -->
        <RouterLink :to="helpPath" class="router-link">
          <el-menu-item index="5">
            <el-icon><QuestionFilled /></el-icon>
            <span>帮助中心</span>
          </el-menu-item>
        </RouterLink>

        <!-- 文档中心 -->
        <RouterLink :to="documentPath" class="router-link">
          <el-menu-item index="6">
            <el-icon><Document /></el-icon>
            <span>文档中心</span>
          </el-menu-item>
        </RouterLink>
      </el-menu>
    </el-aside>

    <el-main class="admin-main">
      <!-- 面包屑导航 -->
      <div class="breadcrumb-container">
        <el-breadcrumb separator=">" class="breadcrumb">
          <el-breadcrumb-item v-for="(item, index) in breadcrumbList" :key="index">
            <span 
              class="breadcrumb-link" 
              @click="handleBreadcrumbClick(index)"
              :class="{ 'last-item': index === breadcrumbList.length - 1 }"
            >
              {{ item }}
            </span>
          </el-breadcrumb-item>
        </el-breadcrumb>
        <div class="admin-info">
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ currentDate }}</span>
          </div>
          <div class="info-item">
            <el-icon><Clock /></el-icon>
            <span>{{ currentTime }}</span>
          </div>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="avatar-dropdown">
              <el-avatar :size="32" :src="computedAvatarUrl" />
              <span class="admin-name">{{ store.state.username || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="text" @click="toggleFullScreen">
            <el-icon><FullScreen /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 路由视图，添加过渡动画 -->
      <transition name="fade-slide" mode="out-in">
        <RouterView v-slot="{ Component }">
          <component :is="Component" />
        </RouterView>
      </transition>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ToUrl from '@/api/api'
import store from '@/store'
import {
  HomeFilled,
  ChatDotRound,
  User,
  DataAnalysis,
  VideoCamera,
  Warning,
  Folder,
  EditPen,
  Trophy,
  ArrowDown,
  FullScreen,
  Setting,
  SwitchButton,
  Document,
  Bell,
  Calendar,
  Clock,
  TrendCharts,
  QuestionFilled
} from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 动态路径
const homePath = ref('/bmgf/admin/home')
const commentPath = ref('/bmgf/admin/comment')
const userPath = ref('/bmgf/admin/user')
const dataPath = ref('/bmgf/admin/data')
const videoPath = ref('/bmgf/admin/video')
const bugPath = ref('/bmgf/admin/bug')
const categoryPath = ref('/bmgf/admin/category')
const questionPath = ref('/bmgf/admin/ques')
const rewardPath = ref('/bmgf/admin/reword')
const announcementPath = ref('/bmgf/admin/announcement')
const newsPath = ref('/bmgf/admin/news')
const trendPath = ref('/bmgf/admin/trend')
const helpPath = ref('/bmgf/admin/help')
const documentPath = ref('/bmgf/admin/document')
const feedbackPath = ref('/bmgf/admin/feedback')

// 加载进度相关
const isLoading = ref(false)
const progress = ref(0)
let progressTimer = null

// 当前日期和时间
const currentDate = ref('')
const currentTime = ref('')

// 开始加载进度
const startLoading = () => {
  isLoading.value = true
  progress.value = 0
  clearInterval(progressTimer)
  progressTimer = setInterval(() => {
    if (progress.value < 90) {
      progress.value += Math.random() * 10
    }
  }, 200)
}

// 完成加载进度
const finishLoading = () => {
  progress.value = 100
  clearInterval(progressTimer)
  setTimeout(() => {
    isLoading.value = false
  }, 300)
}

// 路由更新时触发加载进度
router.beforeEach((to, from, next) => {
  startLoading()
  next()
})

router.afterEach(() => {
  setTimeout(finishLoading, 500)
})

// 组件挂载时触发加载进度
onMounted(() => {
  startLoading()
  setTimeout(finishLoading, 1000)
  updateDateTime()
  // 每秒更新一次时间
  setInterval(updateDateTime, 1000)
})

// 头像动态绑定
const computedAvatarUrl = computed(() => {
  const avatar = store.state.avatar
  return avatar ? `${ToUrl.url}/${avatar}` : 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      // 跳转到个人信息页面
      router.push('/bmgf/admin/profile')
      break
    case 'settings':
      // 跳转到系统设置页面
      router.push('/bmgf/admin/settings')
      break
    case 'logout':
      // 退出登录
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        store.dispatch('logout')
        router.push('/bmgf/login')
        ElMessage.success('退出成功')
      }).catch(() => {})
      break
  }
}

// 面包屑导航
const breadcrumbList = computed(() => {
  const pathMap = {
    'home': '首页',
    'comment': '评论管理',
    'user': '用户管理',
    'data': '数据统计',
    'video': '视频管理',
    'bug': '漏洞管理',
    'category': '分类管理',
    'ques': '题目管理',
    'reword': '奖励管理',
    'announcement': '公告管理',
    'news': '新闻管理',
    'trend': '趋势分析',
    'help': '帮助中心',
    'document': '文档中心',
    'feedback': '反馈管理'
  }

  const path = route.path
  const segments = path.split('/').filter(Boolean)
  const breadcrumbs = ['管理平台']
  
  if (segments.length >= 3) {
    const pageName = segments[segments.length - 1]
    breadcrumbs.push(pathMap[pageName] || pageName)
  }

  return breadcrumbs
})

// 处理面包屑点击
const handleBreadcrumbClick = (index) => {
  if (index === 0) {
    router.push('/bmgf/admin/home')
  }
}

// 动态设置 default-active
const activeMenu = computed(() => {
  const path = route.path;
  switch (path) {
    case '/backMange/home':
      return '0';
    case '/backMange/comment':
      return '1-1';
    case '/backMange/video':
      return '1-2';
    case '/backMange/announcement':
      return '1-3';
    case '/backMange/user':
      return '2-1';
    case '/backMange/reword':
      return '2-2';
    case '/backMange/category':
      return '3-1';
    case '/backMange/ques':
      return '3-2';
    case '/backMange/bug':
      return '3-3';
    case '/backMange/data':
      return '4-1';
    case '/backMange/trend':
      return '4-2';
    case '/backMange/help':
      return '5';
    case '/backMange/document':
      return '6';
    case '/backMange/news':
      return '1-4';
    case '/backMange/feedback':
      return '3-4';
    default:
      return '0';
  }
});

// 全屏切换
const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    }
  }
}

// 更新当前日期和时间
const updateDateTime = () => {
  const now = new Date()
  currentDate.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    weekday: 'long'
  })
  currentTime.value = now.toLocaleString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}
</script>

<style scoped>
.admin-container {
  height: 100vh;
  background: #f5f9ff;
}

.admin-aside {
  background-color: #f0f8ff;
  border-right: 1px solid #e6f0ff;
  display: flex;
  flex-direction: column;
  width: 220px !important;
}

.admin-title {
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(90deg, #6028e1, #a8a9f6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-align: center;
  margin: 30px 0 20px 0;
  letter-spacing: 2px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.admin-logo {
  width: 64px;
  height: 64px;
  object-fit: contain;
}

.admin-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

:deep(.el-menu-item) {
  height: 45px;
  line-height: 45px;
  margin: 2px 0;
  border-radius: 4px;
  transition: all 0.3s;
  padding: 0 20px !important;
}

/* 添加子菜单样式 */
:deep(.el-menu--inline) {
  padding-left: 20px;
}

:deep(.el-menu--inline .el-menu-item) {
  padding-left: 40px !important;
  height: 40px;
  line-height: 40px;
  font-size: 13px;
  color: #606266;
}

:deep(.el-menu--inline .el-menu-item.is-active) {
  background-color: #e6f0ff !important;
  color: #409EFF !important;
  font-weight: 500;
}

:deep(.el-menu--inline .el-menu-item:hover) {
  background-color: #f5f7fa;
}

:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  margin: 4px 0;
  border-radius: 4px;
  transition: all 0.3s;
  padding: 0 20px !important;
  font-weight: 500;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #f5f7fa;
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  margin-right: 12px;
  font-size: 18px;
}

:deep(.el-menu-item span),
:deep(.el-sub-menu__title span) {
  font-size: 14px;
}

.router-link {
  text-decoration: none;
  color: inherit;
}

.admin-main {
  padding: 20px;
  background: #ffffff;
  border-radius: 8px;
  margin: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.breadcrumb-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  background: #f5f7fa;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.breadcrumb {
  display: flex;
  align-items: center;
  margin: 0;
  padding: 0;
}

:deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

:deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  color: #606266;
  transition: all 0.3s;
  padding: 4px 8px;
  border-radius: 4px;
}

:deep(.el-breadcrumb__separator) {
  margin: 0 8px;
  color: #909399;
  font-weight: bold;
}

.breadcrumb-link {
  cursor: pointer;
  color: #409EFF;
  transition: all 0.3s;
  font-size: 15px;
  font-weight: 500;
  text-decoration: none;
  padding: 4px 8px;
  border-radius: 4px;
  background: rgba(64, 158, 255, 0.1);
}

.breadcrumb-link:hover {
  color: #66b1ff;
  background: rgba(64, 158, 255, 0.2);
  transform: translateY(-1px);
}

.breadcrumb-link.last-item {
  color: #606266;
  cursor: default;
  background: none;
  font-weight: 600;
}

.breadcrumb-link.last-item:hover {
  color: #606266;
  background: none;
  transform: none;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.info-item .el-icon {
  font-size: 16px;
  color: #909399;
}

.avatar-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
  color: #303133;
}

.avatar-dropdown:hover {
  background-color: #e6f0ff;
}

.avatar-dropdown .el-icon {
  font-size: 12px;
  color: #909399;
}

.admin-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

:deep(.el-button--text) {
  color: #303133;
}

:deep(.el-button--text:hover) {
  color: #409EFF;
}

:deep(.el-dropdown-menu__item) {
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: #e6f0ff;
  color: #409EFF;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
  margin-right: 4px;
}

.loading-progress {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
}

:deep(.el-progress-bar__outer) {
  background-color: transparent;
}

:deep(.el-progress-bar__inner) {
  transition: width 0.3s ease;
}

/* 页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
  transform: translateX(0);
}
</style>