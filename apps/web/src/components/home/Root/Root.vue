<template>
  <div class="container">
    <!-- Header -->
    <el-header class="header" :class="{ 'header-hidden': !isHeaderVisible }">
      <div class="logo">
        <img src="@/assets/logo/logo/信息.png" alt="白帽工坊logo" class="logo-img">
        白帽工坊
      </div>
      <!-- 移动端菜单按钮 -->
      <div class="mobile-menu-btn" @click="toggleMobileMenu">
        <el-icon>
          <Menu />
        </el-icon>
      </div>
      <!-- 导航菜单 -->
      <el-menu class="menu" mode="horizontal" :default-active="activeMenu" :class="{ 'mobile-menu': isMobileMenuOpen }">
        <!-- 主要导航项 -->
        <el-menu-item v-for="(item, key) in mainMenuItems" :key="key" :index="key">
          <RouterLink :to="`/bmgf/${key}`" class="router-link">{{ item.label }}</RouterLink>
        </el-menu-item>

        
        <!-- AI智能解答 -->
        <el-menu-item index="chat" v-if="isLoggedIn">
          <RouterLink to="/bmgf/chat" class="router-link">AI智能解答</RouterLink>
        </el-menu-item>

        <!-- 聊天室 -->
        <el-menu-item index="discussion">
          <RouterLink to="/bmgf/discussion" class="router-link">实时聊天室</RouterLink>
        </el-menu-item>
        
        <!-- 学习资源下拉菜单 -->
        <el-menu-item class="more">
          <el-dropdown trigger="click">
            <span>学习资源</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToAttack">视频课程</el-dropdown-item>
                <el-dropdown-item @click="goToBugHole">漏洞库</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-menu-item>

        <!-- 个人中心下拉菜单 -->
        <el-menu-item class="more" v-if="isLoggedIn">
          <el-dropdown trigger="click">
            <span>个人中心</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToMySorts">个人排名</el-dropdown-item>
                <el-dropdown-item @click="goToRewards">奖励页面</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-menu-item>


        <!-- 更多菜单项下的下拉菜单 -->
        <el-menu-item class="more">
          <el-dropdown trigger="click">
            <span>更多</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="downloadApp">App端下载</el-dropdown-item>
                <el-dropdown-item @click="goToAbout">关于我们</el-dropdown-item>
                <el-dropdown-item @click="goToHelp">帮助中心</el-dropdown-item>
                <el-dropdown-item @click="goToFeedback">意见反馈</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-menu-item>
      </el-menu>

      <!-- User Info -->
      <template v-if="isLoggedIn">
        <el-dropdown class="user-dropdown" trigger="click">
          <span class="user-info">
            <el-avatar :src="at.avatar" :size="32" class="user-avatar" />
            <template v-if="isFirstLogin">
              你好，{{ at.user }}
            </template>
            <template v-else>
              <span class="welcome-text">欢迎回来，{{ at.user }}</span>
              <el-tag 
                v-if="currentTitle.name !== '未设置'" 
                :type="currentTitle.type" 
                effect="dark" 
                size="small" 
                class="user-title"
              >
                {{ currentTitle.name }}
              </el-tag>
            </template>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="Mine">个人信息</el-dropdown-item>
              <el-dropdown-item @click="laout">退出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template v-else>
        <div class="login-buttons">
          <el-button 
            type="primary" 
            @click="goToLogin"
            class="login-button"
            :loading="isLoading"
          >
            <span class="button-content">
              <span class="button-text">登录</span>
              <el-icon class="button-icon"><ArrowRight /></el-icon>
            </span>
          </el-button>
        </div>
      </template>
    </el-header>

    <!-- Main Content -->
    <div class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <!-- 页脚 -->
    <footer class="app-footer">
      <p>
        <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer">
          ICP备2025024694号
        </a>
      </p>
      <p>
        联系我们: <a href="mailto:zhaoqsnyah@gmail.com">zhaoqsnyah@gmail.com</a>
      </p>
      <p class="disclaimer">
        警告：任何未经授权访问、攻击或窃取本网站数据的行为均属违法，必将追究法律责任。
      </p>
    </footer>
  </div>
</template>

<script setup name="Root">
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'
import { computed, ref, onMounted, watch, onUnmounted } from 'vue'
import ToUrl from '@/api/api'
import { Menu, ArrowRight } from '@element-plus/icons-vue'
import gsap from 'gsap'

// 移动端菜单状态
const isMobileMenuOpen = ref(false)

// 切换移动端菜单
const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
  if (isMobileMenuOpen.value) {
    gsap.fromTo('.mobile-menu .el-menu-item', 
      { 
        opacity: 0,
        y: -20
      },
      { 
        opacity: 1,
        y: 0,
        duration: 0.5,
        stagger: 0.1,
        ease: 'power2.out'
      }
    )
  }
}

// 使用 route 路径匹配标签
const route = useRoute()
const router = useRouter()
const store = useStore()

// 动态加载用户名
const at = computed(() => ({
  token: store.state.token,
  user: store.state.user,
  avatar: `${ToUrl.url}/${store.state.avatar}`
}))

// 判断是否登录
const isLoggedIn = computed(() => {
  return store.state.token && store.state.token !== 'null' && store.state.token !== '';
});

// 主要导航菜单项
const mainMenuItems = computed(() => {
  const items = {
    home: { label: '首页', requireAuth: false },
    private: { label: '实战工具', requireAuth: true },
    community: { label: '社区论坛', requireAuth: false },
    questions: { label: '答题测试', requireAuth: true },
    game: { label: '靶场实战', requireAuth: true }
  };

  // 根据登录状态过滤菜单项
  const filtered = {};
  Object.entries(items).forEach(([key, item]) => {
    if (!item.requireAuth || isLoggedIn.value) {
      filtered[key] = item;
    }
  });
  return filtered;
});

// 修改 activeMenu 计算属性
const activeMenu = computed(() => {
  const path = route.path
  // 主要导航项
  if (path.includes('/bmgf/home')) return 'home'
  if (path.includes('/bmgf/private')) return 'private'
  if (path.includes('/bmgf/community')) return 'community'
  if (path.includes('/bmgf/questions')) return 'questions'
  if (path.includes('/bmgf/game')) return 'game'
  
  // 下拉菜单项
  if (path.includes('/bmgf/attack')) return 'attack'
  if (path.includes('/bmgf/bug-hole')) return 'bug-hole'
  if (path.includes('/bmgf/my-sorts')) return 'my-sorts'
  if (path.includes('/bmgf/rewards')) return 'rewards'
  if (path.includes('/bmgf/chat')) return 'chat'
  if (path.includes('/bmgf/discussion')) return 'discussion'
  
  // 更多菜单项
  if (path.includes('/bmgf/about')) return 'about'
  if (path.includes('/bmgf/help')) return 'help'
  if (path.includes('/bmgf/feedback')) return 'feedback'
  
  return 'home' //不存在返回home
})

// 修改监听activeMenu的变化
watch(activeMenu, (newValue, oldValue) => {
  if (oldValue) {
    const oldMenuItem = document.querySelector(`.menu .el-menu-item[index="${oldValue}"]`)
    if (oldMenuItem) {
      gsap.to(oldMenuItem, {
        backgroundColor: 'transparent',
        borderBottom: 'none',
        fontWeight: 'normal',
        color: '#ffffff',
        duration: 0.2
      })
    }
  }
  
  if (newValue) {
    const activeMenuItem = document.querySelector(`.menu .el-menu-item[index="${newValue}"]`)
    if (activeMenuItem) {
      gsap.timeline()
        .to(activeMenuItem, {
          backgroundColor: 'rgba(255, 255, 255, 0.15)',
          color: '#ffffff',
          fontWeight: 'bold',
          duration: 0.2,
          ease: 'power1.out'
        })
        .to(activeMenuItem, {
          borderBottom: '3px solid #4CAF50',
          duration: 0.2,
          ease: 'power1.out'
        }, '-=0.1')
    }
  }
})

// 退出登录
const laout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    store.dispatch('logout')
    router.push('/')
    ElMessage.success('退出成功')
  }).catch(() => { })
}

// 前往个人信息页
const Mine = () => {
  router.push('/bmgf/mine-mes')
}

// App端下载
const downloadApp = () => {
  // ElMessage.success('跳转到App端下载页面')
  window.open('/bmgf/app', 'App_down')
}

// 添加加载状态
const isLoading = ref(false)

// 优化登录跳转
const goToLogin = async () => {
  isLoading.value = true
  try {
    // 使用 GSAP 创建过渡动画
    await gsap.to('.main-content', {
      opacity: 0,
      y: -20,
      duration: 0.2,
      ease: 'power2.in'
    })
    
    // 跳转到登录页
    await router.push('/bmgf/login')
  } catch (error) {
    console.error('Navigation error:', error)
    ElMessage.error('页面跳转失败')
  } finally {
    isLoading.value = false
  }
}

// 添加下拉菜单激活状态处理
const handleDropdownClick = (path) => {
  router.push(path);
  // 手动更新激活状态
  const menuItem = document.querySelector(`.el-menu-item[index="${path.split('/').pop()}"]`);
  if (menuItem) {
    menuItem.classList.add('is-active');
  }
};

// 修改导航方法
const goToAttack = () => {
  handleDropdownClick('/bmgf/attack');
};

const goToBugHole = () => {
  handleDropdownClick('/bmgf/bug-hole');
};

const goToMySorts = () => {
  handleDropdownClick('/bmgf/my-sorts');
};

const goToRewards = () => {
  handleDropdownClick('/bmgf/rewards');
};

const goToAbout = () => {
  handleDropdownClick('/bmgf/about');
};

const goToHelp = () => {
  handleDropdownClick('/bmgf/help');
};

const goToFeedback = () => {
  handleDropdownClick('/bmgf/feedback');
};

// 判断是否为首次登录
const isFirstLogin = computed(() => {
  return !localStorage.getItem('hasLoggedIn');
});

// 获取用户当前称号
const currentTitle = computed(() => {
  // 从 store 中获取称号数据
  const titleData = store.state.honoraryTitle || {};
  // 查找值为1的称号（当前选中的称号）
  const selectedTitle = Object.entries(titleData).find(([_, value]) => value === 1);
  
  if (selectedTitle) {
    // 称号类型映射表，用于设置不同称号的标签颜色
    const typeMap = {
      '安全新秀': 'success',
      '漏洞侦探': 'warning',
      '攻防入门者': 'danger',
      '中级分析师': 'info',
      '加密解码手': 'success',
      '脚本小能手': 'warning',
      '红队先锋': 'danger',
      '蓝队守护者': 'info',
      '逆向专家': 'success',
      '网络安全大师': 'danger'
    };
    return {
      name: selectedTitle[0],
      type: typeMap[selectedTitle[0]] || 'info'
    };
  }
  
  // 如果没有称号，返回默认值
  return {
    name: '未设置',
    type: 'info'
  };
});

// 添加滚动相关状态
const lastScrollTop = ref(0)
const isHeaderVisible = ref(true)

// 处理滚动事件
const handleScroll = () => {
  const currentScrollTop = window.pageYOffset || document.documentElement.scrollTop
  
  // 判断滚动方向
  if (currentScrollTop > lastScrollTop.value && currentScrollTop > 60) {
    // 向下滚动且超过60px时隐藏header
    isHeaderVisible.value = false
  } else {
    // 向上滚动时显示header
    isHeaderVisible.value = true
  }
  
  lastScrollTop.value = currentScrollTop
}

// 在组件挂载时添加滚动监听
onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  
  // 为每个菜单项添加悬停动画
  const menuItems = document.querySelectorAll('.menu .el-menu-item')
  menuItems.forEach(item => {
    item.addEventListener('mouseenter', () => {
      gsap.to(item, {
        scale: 1.02,
        duration: 0.2,
        ease: 'power1.out'
      })
    })
    
    item.addEventListener('mouseleave', () => {
      gsap.to(item, {
        scale: 1,
        duration: 0.2,
        ease: 'power1.out'
      })
    })
  })

  // 为logo添加初始动画
  gsap.from('.logo', {
    opacity: 0,
    x: -20,
    duration: 0.5,
    ease: 'power1.out'
  })

  // 为用户信息添加初始动画
  gsap.from('.user-dropdown, .login-buttons', {
    opacity: 0,
    x: 20,
    duration: 0.5,
    ease: 'power1.out'
  })

  // 如果用户已登录，设置登录标记
  if (isLoggedIn.value) {
    localStorage.setItem('hasLoggedIn', 'true');
  }
})

// 在组件卸载时移除滚动监听
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  position: relative;
  overflow: hidden;
}

.container::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 60%);
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.header {
  background: rgba(102, 126, 234, 0.7);
  backdrop-filter: blur(10px);
  position: fixed;
  width: 100%;
  z-index: 1000;
  top: 0;
  left: 0;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease-in-out;
}

.header-hidden {
  transform: translateY(-100%);
}

.main-content {
  padding-top: 60px;
  min-height: calc(100vh - 60px);
  position: relative;
  z-index: 1;
}

.logo {
  color: #ffffff;
  font-size: 24px;
  font-weight: bold;
  margin-right: 40px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-img {
  height: 40px;
  width: auto;
}

.menu {
  background: transparent;
  border: none;
  flex-grow: 1;
  flex-shrink: 1;
  overflow: hidden;
}

.menu .el-menu-item {
  color: #ffffff;
  font-size: clamp(12px, 1.2vw, 16px);
  transition: all 0.2s ease;
  flex-shrink: 0;
  padding: 0 15px;
  position: relative;
  will-change: transform, background-color;
}

.menu .el-menu-item.is-active {
  position: relative;
  background: rgba(255, 255, 255, 0.15);
  color: #ffffff;
  font-weight: bold;
  border-bottom: 3px solid #4CAF50;
}

.menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  transform: translateY(-1px);
}

.router-link {
  color: inherit;
  text-decoration: none;
}

.user-dropdown {
  margin-left: auto;
  padding-right: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #fff;
  padding: 5px 10px;
  border-radius: 5px;
  background-color: rgba(255, 255, 255, 0.2);
  transition: all 0.2s ease;
  will-change: transform, background-color;
  gap: 8px;
}

.welcome-text {
  font-size: 14px;
  font-weight: 500;
}

.user-title {
  margin-left: 4px;
  font-size: 12px;
  padding: 0 6px;
  height: 20px;
  line-height: 20px;
  border-radius: 10px;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.user-avatar {
  margin-right: 8px;
  border: 2px solid rgba(255, 255, 255, 0.8);
}

/* 修改下拉菜单样式 */
:deep(.el-dropdown-menu) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(102, 126, 234, 0.2);
  min-width: 160px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 8px;
  margin-top: 8px;
}

:deep(.el-dropdown-menu__item) {
  color: #2d3748;
  font-size: 14px;
  padding: 10px 16px;
  border-radius: 8px;
  margin: 2px 0;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  transform: translateX(4px);
}

:deep(.el-dropdown-menu__item:active) {
  background: rgba(102, 126, 234, 0.2);
}

/* 添加下拉菜单动画 */
:deep(.el-dropdown-menu) {
  animation: dropdownFadeIn 0.2s ease-out;
}

@keyframes dropdownFadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 美化下拉菜单触发按钮 */
.more .el-dropdown {
  color: #fff;
  font-size: clamp(12px, 1.2vw, 16px);
  cursor: pointer;
  padding: 0 15px;
  height: 60px;
  line-height: 60px;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
  position: relative;
}

.more .el-dropdown:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

.more .el-dropdown::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: #4CAF50;
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.more .el-dropdown:hover::after {
  width: 80%;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .header {
    padding: 0 10px;
  }

  .logo {
    font-size: 18px;
    margin-right: 10px;
  }

  .logo-img {
    height: 30px;
  }

  .mobile-menu-btn {
    display: block;
  }

  .menu {
    position: absolute;
    top: 60px;
    left: 0;
    width: 100%;
    background: rgba(102, 126, 234, 0.9);
    backdrop-filter: blur(10px);
    display: none;
    flex-direction: column;

  }

  .menu.mobile-menu {
    display: flex;
  }

  .menu .el-menu-item {
    width: 100%;
    text-align: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  .user-dropdown {
    margin-left: 0;
    padding-right: 10px;
  }

  .user-info {
    padding: 3px 8px;
    font-size: 14px;
  }

  .user-avatar {
    margin-right: 5px;
  }

  :deep(.el-dropdown-menu) {
    min-width: 140px;
    padding: 6px;
  }

  :deep(.el-dropdown-menu__item) {
    padding: 8px 12px;
    font-size: 13px;
  }
}
@media (min-width: 769px) and (max-width: 1024px) {
  .menu .el-menu-item {
    padding: 0 10px;
  }

  .more .el-dropdown {
    padding: 0 10px;
  }

  .logo {
    font-size: 20px;
    margin-right: 20px;
  }

  .user-info {
    font-size: clamp(12px, 1.1vw, 14px);
  }
}
/* 页脚样式 */
.app-footer {
  text-align: center;
  padding: 25px 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  line-height: 1.8;
  background-color: transparent;
  border-top: none;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.app-footer p {
  margin: 4px 0;
}

.app-footer a {
  color: inherit;
  text-decoration: none;
  transition: color 0.3s ease;
}

.app-footer a:hover {
  color: #ffffff;
  text-decoration: underline;
}

.disclaimer {
  color: rgba(255, 255, 255, 0.6);
  font-size: 11px;
}

.login-buttons {
  display: flex;
  gap: 10px;
  margin-left: auto;
  padding-right: 20px;
}

.login-buttons .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  transition: all 0.2s ease;
  will-change: transform, background-color;
}

.login-buttons .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 优化动画性能 */
.fade-enter-active,
.fade-leave-active {
  will-change: transform, opacity;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .fade-enter-from,
  .fade-leave-to {
    transform: translateY(5px);
  }
}

.login-button {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 0 24px;
  height: 40px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  will-change: transform, background-color;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.button-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.button-icon {
  transition: transform 0.3s ease;
}

.login-button:hover .button-icon {
  transform: translateX(4px);
}

/* 添加按钮加载动画 */
.login-button.is-loading {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.8;
}

.login-button.is-loading .button-content {
  opacity: 0;
}

/* 优化移动端适配 */
@media (max-width: 768px) {
  .login-button {
    height: 36px;
    padding: 0 20px;
  }
  
  .button-content {
    font-size: 14px;
  }
}
</style>