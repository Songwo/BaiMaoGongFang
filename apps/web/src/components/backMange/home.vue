<template>
  <div class="home-container">
    <!-- 工作台 -->
    <div class="workbench">
      <div class="welcome-card">
        <el-avatar :size="64" :src="computedAvatarUrl" />
        <div class="welcome-text">
          <h3>{{ welcomeMessage }}</h3>
          <p>今天是 {{ currentDate }}</p>
        </div>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 左侧区域 -->
      <div class="left-section">
        <!-- 项目卡片区 -->
        <div class="projects-section">
          <h2 class="section-title">开源资源</h2>
          <div class="project-cards">
            <el-card v-for="resource in openSourceResources" :key="resource.id" class="project-card" shadow="hover">
              <template #header>
                <div class="project-header">
                  <el-icon :size="24" :color="resource.color">
                    <component :is="resource.icon" />
                  </el-icon>
                  <span class="project-name">{{ resource.name }}</span>
                </div>
              </template>
              <div class="project-content">
                <p class="project-desc">{{ resource.description }}</p>
                <div class="project-tags">
                  <el-tag v-for="tag in resource.tags" :key="tag" size="small" class="tag-item">
                    {{ tag }}
                  </el-tag>
                </div>
                <div class="project-stats">
                  <span class="stat-item">
                    <el-icon><Star /></el-icon>
                    {{ resource.stars }}
                  </span>
                  <span class="stat-item">
                    <el-icon><View /></el-icon>
                    {{ resource.views }}
                  </span>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 消息卡片 -->
        <el-card class="messages-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>最新消息</h3>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in activities"
              :key="index"
              :timestamp="activity.timestamp"
              :type="activity.type"
              :color="activity.color"
              :size="activity.size"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>

      <!-- 右侧快速操作区 -->
      <div class="quick-actions-section">
        <el-card class="quick-actions-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>快捷操作</h3>
            </div>
          </template>
          <div class="quick-actions">
            <el-card v-for="(action, index) in quickActions" :key="index" class="action-card" shadow="hover" @click="handleAction(action)">
              <div class="action-content">
                <el-icon :size="32" class="action-icon"><component :is="action.icon" /></el-icon>
                <span class="action-label">{{ action.label }}</span>
              </div>
            </el-card>
          </div>
        </el-card>

        <!-- 广告位 -->
        <div class="ad-space">
          <el-card shadow="hover" class="ad-card">
            <template #header>
              <div class="ad-title">推广</div>
            </template>
            <div class="ad-content">
              <img src="https://via.placeholder.com/300x200" alt="广告" class="ad-image" />
              <p class="ad-text">这里是广告内容描述</p>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Star,
  View,
} from '@element-plus/icons-vue'
import store from '@/store'
import ToUrl from '@/api/api.ts'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

//添加计算属性动态加载头像
const computedAvatarUrl = computed(() => {
  const avatar = store.state.avatar;
  //图片未找到的话加载默认头像
  return avatar ? `${ToUrl.url}/${avatar}` : 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png';
})

// 欢迎语列表
const welcomeMessages = [
  '欢迎回来，今天也要加油哦！',
  '新的一天，新的开始！',
  '工作愉快，效率满满！',
  '保持专注，继续前进！',
  '今天也要元气满满！'
]

// 随机欢迎语
const welcomeMessage = computed(() => {
  return welcomeMessages[Math.floor(Math.random() * welcomeMessages.length)]
})

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 快捷操作列表
const quickActions = [
  { 
    label: '新建项目', 
    icon: 'Plus', 
    type: 'primary',
    path: null,
    message: '该功能暂未开发'
  },
  { 
    label: '上传文件', 
    icon: 'Upload', 
    type: 'success',
    path: null,
    message: '该功能暂未开发'
  },
  { 
    label: '数据分析', 
    icon: 'DataLine', 
    type: 'warning',
    path: '/bmgf/admin/trend'
  },
  { 
    label: '用户管理', 
    icon: 'User', 
    type: 'info',
    path: '/bmgf/admin/user'
  },
  { 
    label: '系统设置', 
    icon: 'Setting', 
    type: 'primary',
    path: '/bmgf/admin/settings'
  },
  { 
    label: '帮助中心', 
    icon: 'Help', 
    type: 'success',
    path: '/bmgf/admin/help'
  },
  { 
    label: '文档中心', 
    icon: 'Document', 
    type: 'warning',
    path: '/bmgf/admin/document'
  },
  { 
    label: '工具中心', 
    icon: 'Tools', 
    type: 'info',
    path: null,
    message: '该功能暂未开发'
  }
]

// 处理快捷操作点击
const handleAction = (action) => {
  if (action.path) {
    router.push(action.path)
  } else if (action.message) {
    ElMessage.info(action.message)
  }
}

const activities = ref([
  {
    content: `${store.state.user} 登录了系统`,
    timestamp: new Date().toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }),
    type: 'success',
    color: '#0bbd87',
    size: 'normal'
  },
  {
    content: '系统更新完成',
    timestamp: '2024-03-20 10:00',
    type: 'primary',
    color: '#0bbd87',
    size: 'normal'
  },
  {
    content: '新用户注册',
    timestamp: '2024-03-20 09:30',
    type: 'success',
    color: '#0bbd87',
    size: 'normal'
  },
  {
    content: '系统维护通知',
    timestamp: '2024-03-20 09:00',
    type: 'warning',
    color: '#e6a23c',
    size: 'normal'
  }
])

// 开源资源数据
const openSourceResources = ref([
  {
    id: 1,
    name: 'Vue.js',
    description: '渐进式 JavaScript 框架，用于构建用户界面',
    icon: 'Connection',
    color: '#42b883',
    stars: '200k',
    views: '1.2M',
    tags: ['前端', '框架', 'JavaScript']
  },
  {
    id: 2,
    name: 'React',
    description: '用于构建用户界面的 JavaScript 库',
    icon: 'Monitor',
    color: '#61dafb',
    stars: '180k',
    views: '1M',
    tags: ['前端', '库', 'JavaScript']
  },
  {
    id: 3,
    name: 'GitHub',
    description: '全球最大的代码托管平台',
    icon: 'Platform',
    color: '#24292e',
    stars: '150k',
    views: '2M',
    tags: ['平台', '代码托管', '协作']
  },
  {
    id: 4,
    name: 'HTML5',
    description: '最新的 HTML 标准，提供更丰富的网页功能',
    icon: 'DataBoard',
    color: '#e34f26',
    stars: '100k',
    views: '800k',
    tags: ['前端', '标准', 'Web']
  },
  {
    id: 5,
    name: 'JavaScript',
    description: '轻量级的解释型编程语言',
    icon: 'Operation',
    color: '#f7df1e',
    stars: '120k',
    views: '900k',
    tags: ['语言', '前端', '后端']
  },
  {
    id: 6,
    name: 'Java',
    description: '面向对象的编程语言，跨平台应用开发',
    icon: 'Coffee',
    color: '#007396',
    stars: '160k',
    views: '1.1M',
    tags: ['后端', '语言', '企业级']
  }
])
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 15px;
  gap: 15px;
}

/* 工作台样式 */
.workbench {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.welcome-card {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome-text h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.welcome-text p {
  margin: 3px 0 0;
  color: #909399;
  font-size: 13px;
}

/* 主要内容区 */
.main-content {
  display: grid;
  grid-template-columns: 6fr 4fr;
  gap: 15px;
  flex: 1;
}

/* 左侧区域样式 */
.left-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

/* 项目卡片样式 */
.projects-section {
  flex: 1;
  background: #fff;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.project-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.project-card {
  height: 180px;
  display: flex;
  flex-direction: column;
}

.project-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #ebeef5;
}

.project-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.project-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 6px 0;
  overflow: hidden;
}

.project-desc {
  margin: 2px 0;
  color: #606266;
  font-size: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin: 2px 0;
  min-height: 30px;
}

.tag-item {
  font-size: 11px;
  padding: 0 4px;
  height: 22px;
  line-height: 22px;
}

.project-stats {
  display: flex;
  gap: 12px;
  margin-top: auto;
  font-size: 11px;
  padding-top: 4px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
}

/* 消息卡片样式 */
.messages-card {
  margin-top: 15px;
}

.messages-card .card-header {
  padding: 8px 15px;
}

.messages-card .card-header h3 {
  font-size: 15px;
}

/* 快捷操作卡片样式 */
.quick-actions-card {
  margin-bottom: 15px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 8px;
}

.action-card {
  height: 90px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fff;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.action-icon {
  font-size: 28px;
  color: #409EFF;
}

.action-label {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

:deep(.el-card__header) {
  padding: 10px 15px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 10px;
}

/* 广告位样式 */
.ad-space {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.ad-card {
  width: 100%;
  height: 180px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.ad-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.ad-title {
  padding: 8px 15px;
  font-size: 15px;
}

.ad-content {
  padding: 15px;
}

.ad-image {
  height: 90px;
  width: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.ad-text {
  margin-top: 8px;
  font-size: 13px;
}
</style> 