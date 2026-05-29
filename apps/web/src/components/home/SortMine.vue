<template>
  <div class="main-container">
    <!-- 左侧排名 -->
    <div class="left-ranking">
      <!-- 原有排名内容保持不动 -->
      <div class="ranking-container">
      <!-- 标题区 -->
      <div class="header">
        <h2>
          <span class="title-icon">🏆</span>
          实时排行榜
          <span class="title-badge" v-if="isNewUpdate">新</span>
        </h2>
        <div class="header-actions">
          <el-tooltip content="查看我的排名" placement="top">
            <el-button 
              type="primary" 
              :icon="User" 
              circle 
              @click="scrollToMyRank"
              :disabled="!myRank"
            />
          </el-tooltip>
          <el-button 
            type="info" 
            :icon="Refresh" 
            circle 
            :loading="loading"
            @click="refreshRankings"
          />
        </div>
      </div>
  
      <!-- 加载状态 -->
      <el-skeleton :rows="5" animated v-if="loading" />
  
      <!-- 排名列表 -->
      <el-scrollbar v-else height="600px" ref="scrollbarRef">
        <transition-group name="list" tag="div">
          <div 
            v-for="(item, index) in rankings" 
            :key="item.id"
            class="rank-item"
            :class="{
              'top-1': index === 0,
              'top-2': index === 1,
              'top-3': index === 2,
              'hover-effect': index > 2,
              'my-rank': item.id === store.state.id
            }"
            @click="showRankDetails(item)"
          >
            <!-- 奖牌图标 -->
            <div class="medal">
              <el-icon v-if="index === 0" color="#ffd700"><Medal /></el-icon>
              <el-icon v-if="index === 1" color="#c0c0c0"><Medal /></el-icon>
              <el-icon v-if="index === 2" color="#cd7f32"><Medal /></el-icon>
              <span v-if="index > 2">{{ index + 1 }}</span>
            </div>
  
            <!-- 用户信息 -->
            <div class="user-info">
              <el-avatar 
                :src="getAvatarUrl(item.avatar)" 
                loading="lazy"
                @error="handleAvatarError"
                :class="{ 'avatar-glow': index < 3 }"
              />
              <div class="detail">
                <div class="name-row">
                  <span class="name">{{ item.username }}</span>
                  <el-tag 
                    v-if="item.title && item.title !== '未拥有称号'"
                    size="small" 
                    :type="item.title ? 'success' : 'info'" 
                    class="user-title"
                  >
                    {{ item.title }}
                  </el-tag>
                </div>
                <span class="department">{{ item.department }}</span>
              </div>
            </div>
  
            <!-- 分数 -->
            <div class="score">
              <el-tag :type="getScoreType(item.score)" class="score-tag">
                {{ item.score }} 分
              </el-tag>
              <div class="score-trend" v-if="item.trend">
                <el-icon :class="item.trend === 'up' ? 'trend-up' : 'trend-down'">
                  <component :is="item.trend === 'up' ? 'ArrowUp' : 'ArrowDown'" />
                </el-icon>
              </div>
            </div>
          </div>
        </transition-group>
      </el-scrollbar>
  
      <!-- 最后更新时间 -->
      <div class="update-time">
        <el-icon><Timer /></el-icon>
        最后更新：{{ lastUpdate }}
      </div>
    </div>

    <!-- 学习趋势图表 -->
    <div class="trend-container">
      <div class="trend-header">
        <h3>
          <span class="trend-icon">📈</span>
          学习趋势
          <el-tag size="small" type="info" effect="plain" class="trend-tag">近7天</el-tag>
        </h3>
        <div class="trend-actions">
          <el-radio-group v-model="trendTimeRange" size="small">
            <el-radio-button :value="'week'">周</el-radio-button>
            <el-radio-button :value="'month'">月</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      <div class="trend-chart" ref="trendChartRef"></div>
    </div>
  </div>

    <!-- 右侧信息栏 -->
    <div class="right-info">
      <!-- 最新公告 -->
      <div class="info-section announcement">
        <div class="info-header">
          <h3>
            <span class="section-icon">📢</span>
            最新公告
            <el-tag type="danger" effect="dark" size="small">Hot</el-tag>
          </h3>
        </div>
        <el-scrollbar height="240px">
          <div class="announcement-list">
            <div 
              v-for="item in announcements" 
              :key="item.id" 
              class="announcement-item"
              @click="viewAnnouncement(item.id)"
            >
              <div class="announcement-time">{{ item.time }}</div>
              <div class="announcement-title">{{ item.title }}</div>
              <el-tag 
                :type="item.type" 
                size="small" 
                effect="plain"
                class="announcement-tag"
              >{{ item.tag }}</el-tag>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- 公告详情对话框 -->
      <el-dialog
        v-model="dialogVisible"
        title="公告详情"
        width="50%"
      >
        <div v-if="currentAnnouncement">
          <h3>{{ currentAnnouncement.title }}</h3>
          <div class="announcement-meta">
            <el-tag :type="currentAnnouncement.type" size="small">{{ currentAnnouncement.tag }}</el-tag>
            <span class="time">{{ new Date(currentAnnouncement.createTime).toLocaleString() }}</span>
          </div>
          <div class="announcement-content">
            {{ currentAnnouncement.content }}
          </div>
        </div>
      </el-dialog>

      <!-- 更新内容 -->
      <div class="info-section changelog">
        <div class="info-header">
          <h3>
            <span class="section-icon">🛠</span>
            版本更新
            <el-tag type="info" effect="dark" size="small">点击查看</el-tag>
          </h3>
        </div>
        <el-scrollbar height="320px">
          <div class="changelog-list">
            <div 
              v-for="(log, index) in changelogs" 
              :key="index" 
              class="changelog-item"
              @click="viewChangelog(log)"
            >
              <el-tag :type="getChangelogType(log.type)" size="small">
                {{ log.type === 'feature' ? '新功能' : log.type === 'fix' ? '修复' : '改进' }}
              </el-tag>
              <span class="changelog-time">{{ new Date(log.createTime).toLocaleDateString() }}</span>
            </div>
          </div>
        </el-scrollbar>
      </div>

      <!-- 更新日志详情对话框 -->
      <el-dialog
        v-model="changelogDialogVisible"
        title="版本更新详情"
        width="50%"
      >
        <div v-if="currentChangelog">
          <div class="changelog-meta">
            <el-tag :type="getChangelogType(currentChangelog.type)" size="small">
              {{ currentChangelog.type === 'feature' ? '新功能' : currentChangelog.type === 'fix' ? '修复' : '改进' }}
            </el-tag>
            <span class="time">{{ new Date(currentChangelog.createTime).toLocaleString() }}</span>
          </div>
          <div class="changelog-content">
            {{ currentChangelog.content }}
          </div>
        </div>
      </el-dialog>
    </div>

    <!-- 用户详情弹窗 -->
    <el-dialog
      v-model="userDetailVisible"
      title="用户详情"
      width="400px"
      class="user-detail-dialog"
      :close-on-click-modal="true"
      @close="handleDialogClose"
    >
      <div v-if="selectedUser" class="user-detail-content">
        <div class="user-detail-header">
          <el-avatar :size="80" :src="selectedUser.avatar" />
          <h3>{{ selectedUser.username }}</h3>
          <el-tag 
            v-if="selectedUser.title && selectedUser.title !== '未拥有称号'"
            :type="selectedUser.title ? 'success' : 'info'"
          >
            {{ selectedUser.title }}
          </el-tag>
        </div>
        <div class="user-detail-stats">
          <div class="stat-item">
            <span class="stat-label">当前排名</span>
            <span class="stat-value">{{ selectedUser.rank }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总积分</span>
            <span class="stat-value">{{ selectedUser.score }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
  
<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { Medal, Refresh, User, Timer, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import store from '@/store'
import ToUrl from '@/api/api'
import { getAnnouncements, getAnnouncementDetail } from '@/api/announcement'
import { getChangelogs } from '@/api/changelog'
import { debounce } from 'lodash-es'
import * as echarts from 'echarts'
import defaultAvatar from '@/assets/ab.jpg'  // 导入默认头像

// 响应式数据
const rankings = ref([])
const loading = ref(true)
const lastUpdate = ref('')
const announcements = ref([])
const changelogs = ref([])
const dialogVisible = ref(false)
const changelogDialogVisible = ref(false)
const currentAnnouncement = ref(null)
const currentChangelog = ref(null)

// 新增的响应式数据
const isNewUpdate = ref(false)
const userDetailVisible = ref(false)
const selectedUser = ref(null)
const scrollbarRef = ref(null)
const myRank = ref(null)

// 趋势图相关
const trendChartRef = ref(null)
const trendTimeRange = ref('week')
let trendChart = null

// 缓存相关
const rankingsCache = {
  data: null,
  timestamp: null,
  maxAge: 5 * 60 * 1000 // 5分钟缓存
}

// 定时器引用
let refreshTimer = null

// 创建axios实例
const api = axios.create({
  baseURL: ToUrl.url,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  }
})

// 添加请求拦截器
api.interceptors.request.use(
  config => {
    const token = store.state.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 初始化加载
onMounted(() => {
  loadRankings()
  loadAnnouncements()
  loadChangelogs()
  initTrendChart()
  window.addEventListener('resize', handleResize)
  
  // 使用防抖的刷新函数
  const debouncedRefresh = debounce(() => {
    loadRankings()
    loadAnnouncements()
    loadChangelogs()
  }, 300000) // 5分钟

  // 设置定时刷新
  refreshTimer = setInterval(debouncedRefresh, 300000)
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
})

// 加载排名数据
const loadRankings = async () => {
  try {
    // 检查缓存是否有效
    const now = Date.now()
    if (rankingsCache.data && rankingsCache.timestamp && 
        (now - rankingsCache.timestamp < rankingsCache.maxAge)) {
      rankings.value = rankingsCache.data
      lastUpdate.value = new Date().toLocaleTimeString()
      return
    }

    loading.value = true
    const res = await api.get('/user/rank')

    // 数据转换处理
    const processedData = res.data.data
      .map(item => {
        const honoraryTitle = item.honoraryTitle || {};
        const selectedTitle = Object.entries(honoraryTitle).find(([_, value]) => value === 1);
        const title = selectedTitle ? selectedTitle[0] : '未拥有称号';
        
        // 如果是当前用户，更新 store 中的称号
        if (item.id === store.state.id) {
          store.commit('setHonoraryTitle', honoraryTitle)
        }

        return {
          id: item.id,        
          username: item.username, 
          score: item.totalScore,  
          department: item.id, 
          title: title,
          avatar: item.avatar || 'avatar/0736dfa5-a96f-45a7-8208-2e8e3b72161e_ab.jpg',
          trend: Math.random() > 0.5 ? 'up' : 'down'
        };
      })
      .sort((a, b) => b.score - a.score)

    // 更新缓存
    rankingsCache.data = processedData
    rankingsCache.timestamp = now
    
    rankings.value = processedData
    lastUpdate.value = new Date().toLocaleTimeString()

    // 更新我的排名
    myRank.value = processedData.findIndex(item => item.id === store.state.id) + 1

    // 设置新更新标记
    isNewUpdate.value = true
    setTimeout(() => {
      isNewUpdate.value = false
    }, 3000)
  } catch (error) {
    console.error('加载排名失败:', error)
    ElMessage.error(`排名加载失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// 手动刷新排名
const refreshRankings = debounce(async () => {
  // 清除缓存
  rankingsCache.data = null
  rankingsCache.timestamp = null
  await loadRankings()
}, 300)

// 加载公告
const loadAnnouncements = async () => {
  try {
    const res = await getAnnouncements()
    announcements.value = res.data.map(item => ({
      id: item.id,
      time: new Date(item.createTime).toLocaleDateString(),
      title: item.title,
      type: item.type,
      tag: item.tag
    }))
  } catch (error) {
    ElMessage.error('公告加载失败')
  }
}

// 加载更新日志
const loadChangelogs = async () => {
  try {
    const res = await getChangelogs()
    changelogs.value = res.data
  } catch (error) {
    ElMessage.error('更新日志加载失败')
  }
}

// 查看公告详情
const viewAnnouncement = async (id) => {
  try {
    const res = await getAnnouncementDetail(id)
    currentAnnouncement.value = res.data
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取公告详情失败')
  }
}

// 查看更新日志详情
const viewChangelog = (log) => {
  currentChangelog.value = log
  changelogDialogVisible.value = true
}

// 获取更新日志类型对应的标签类型
const getChangelogType = (type) => {
  switch (type) {
    case 'feature':
      return 'success'
    case 'fix':
      return 'warning'
    case 'improvement':
      return 'info'
    default:
      return 'info'
  }
}

// 分数标签类型
const getScoreType = (score) => {
  if (score >= 95) return 'success'
  if (score >= 90) return 'warning'
  return 'danger'
}

// 修改头像URL处理函数
const getAvatarUrl = (avatar) => {
  console.log('Processing avatar URL:', avatar);
  
  if (!avatar) {
    console.log('No avatar provided, using default');
    return defaultAvatar;
  }
  
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http')) {
    console.log('Full URL detected:', avatar);
    return avatar;
  }
  
  // 添加后端基础URL
  const processedUrl = `${ToUrl.url}/${avatar}`;
  console.log('Processed URL with backend:', processedUrl);
  return processedUrl;
}

// 修改头像错误处理函数
const handleAvatarError = (e) => {
  console.log('Avatar load error:', e.target.src);
  if (e.target.src !== defaultAvatar) {
    console.log('Falling back to default avatar');
    e.target.src = defaultAvatar;
    e.target.onerror = null; // 防止循环调用
  }
}

// 添加获取当前用户称号的计算属性
const getCurrentUserTitle = computed(() => {
  const honoraryTitle = store.state.honoraryTitle
  const selectedTitle = Object.entries(honoraryTitle).find(([_, value]) => value === 1)
  return selectedTitle ? selectedTitle[0] : '未拥有称号'
})

// 修改显示用户详情的函数
const showRankDetails = (user) => {
  selectedUser.value = {
    ...user,
    rank: rankings.value.findIndex(item => item.id === user.id) + 1,
    title: user.id === store.state.id ? getCurrentUserTitle.value : user.title,
    avatar: getAvatarUrl(user.avatar)
  }
  userDetailVisible.value = true
}

// 隐藏用户详情
const hideRankDetails = () => {
  // 移除自动关闭
  // userDetailVisible.value = false
}

// 添加点击空白处关闭弹窗的功能
const handleDialogClose = () => {
  userDetailVisible.value = false
  selectedUser.value = null
}

// 滚动到我的排名
const scrollToMyRank = () => {
  if (!myRank.value) return
  
  const index = rankings.value.findIndex(item => item.id === store.state.id)
  if (index !== -1) {
    const itemHeight = 80 // 每个排名项的高度
    const scrollTop = index * itemHeight
    scrollbarRef.value?.setScrollTop(scrollTop)
    
    // 添加高亮动画
    const element = document.querySelector('.my-rank')
    if (element) {
      element.classList.add('highlight')
      setTimeout(() => {
        element.classList.remove('highlight')
      }, 2000)
    }
  }
}

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  updateTrendChart()
}

// 更新趋势图数据
const updateTrendChart = () => {
  if (!trendChart) return

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '学习时长(小时)',
        nameTextStyle: {
          color: '#666'
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: '#999'
          }
        },
        axisLabel: {
          color: '#666'
        },
        splitLine: {
          lineStyle: {
            color: '#eee'
          }
        }
      },
      {
        type: 'value',
        name: '排名',
        nameTextStyle: {
          color: '#666'
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: '#999'
          }
        },
        axisLabel: {
          color: '#666'
        },
        splitLine: {
          show: false
        },
        inverse: true
      }
    ],
    series: [
      {
        name: '学习时长',
        type: 'bar',
        data: [2.5, 3.2, 4.1, 3.8, 4.5, 5.2, 4.8],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      },
      {
        name: '排名变化',
        type: 'line',
        yAxisIndex: 1,
        data: [45, 42, 38, 35, 32, 30, 28],
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#91cc75'
        },
        itemStyle: {
          color: '#91cc75'
        }
      }
    ]
  }

  trendChart.setOption(option)
}

// 监听时间范围变化
watch(trendTimeRange, () => {
  updateTrendChart()
})

// 监听窗口大小变化
const handleResize = () => {
  trendChart?.resize()
}
</script>
  
<style scoped>
.main-container {
  display: flex;
  gap: 20px;
  padding: 20px;
  min-height: 100vh;
  color: black;

}

.left-ranking {
  flex: 1;
  min-width: 600px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ranking-container {
  padding: 20px;
  color: black;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  background: rgba(143, 129, 224, 0.703);
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 10px;
  color: #f9f3f3;
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 15px;
  margin: 8px 0;
  background: #f8f9fa;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.medal {
  width: 40px;
  text-align: center;
  font-weight: bold;
  font-size: 18px;
}

.user-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
}

.detail {
  display: flex;
  flex-direction: column;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.name {
  font-weight: 600;
}

.user-title {
  font-size: 0.8em;
  background-color: rgba(76, 175, 80, 0.1);
  border-color: #4caf50;
  color: #4caf50;
}

.user-title.el-tag--info {
  background-color: rgba(144, 147, 153, 0.1);
  border-color: #909399;
  color: #909399;
}

.department {
  font-size: 0.8em;
  color: #666;
}

.score {
  margin-left: auto;
}

/* 前三名特殊样式 */
.top-1 { background: linear-gradient(90deg, #fff3e0 0%, #ffe0b2 100%); }
.top-2 { background: linear-gradient(90deg, #f5f5f5 0%, #eeeeee 100%); }
.top-3 { background: linear-gradient(90deg, #fff8e1 0%, #ffecb3 100%); }

/* 悬停效果 */
.hover-effect:hover {
  transform: translateX(10px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* 过渡动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.update-time {
  margin-top: 15px;
  text-align: right;
  font-size: 1.2em;
  color: #ffffff;
}

.el-scrollbar {
  flex: 1;
  min-height: 400px;
}

.trend-container {
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.703);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  padding: 20px;
  min-height: 400px;
}

.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.trend-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #333;
  font-size: 18px;
}

.trend-icon {
  font-size: 1.2em;
}

.trend-tag {
  margin-left: 8px;
}

.trend-actions {
  display: flex;
  gap: 10px;
}

.trend-chart {
  height: 350px;
  width: 100%;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .main-container {
    flex-direction: column;
  }
  
  .left-ranking,
  .right-info {
    width: 100%;
  }
  
  .right-info {
    margin-top: 20px;
  }

  .trend-container {
    margin-top: 15px;
  }
  
  .trend-chart {
    height: 300px;
  }
}

.right-info {
  width: 480px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 321px;
}

.info-section {
  background: rgba(143, 129, 224, 0.703);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  padding: 15px;
  height: 320px;
}

.info-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 4px;
}

.announcement-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  transition: all 0.3s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.announcement-item:hover {
  transform: translateX(5px);
  background: #f1f3f5;
}

.announcement-time {
  font-size: 0.8em;
  color: #666;
}

.announcement-title {
  font-weight: 500;
  word-break: break-word;
  line-height: 1.4;
  margin-right: 8px;
}

.announcement-tag {
  align-self: flex-start;
  margin-top: 4px;
}

.changelog-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 10px 0;
}

.changelog-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.changelog-item:hover {
  transform: translateX(5px);
  background: #f1f3f5;
}

.changelog-time {
  font-size: 0.8em;
  color: #666;
}

.title-icon {
  margin-right: 8px;
  animation: bounce 2s infinite;
}

.title-badge {
  background: #ff4d4f;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  margin-left: 8px;
  animation: pulse 1s infinite;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.avatar-glow {
  animation: glow 2s infinite;
}

.score-trend {
  margin-left: 8px;
}

.trend-up {
  color: #52c41a;
  animation: slideUp 0.3s ease-out;
}

.trend-down {
  color: #ff4d4f;
  animation: slideDown 0.3s ease-out;
}

.my-rank {
  border: 2px solid #1890ff;
  background: rgba(24, 144, 255, 0.1);
}

.my-rank.highlight {
  animation: highlight 2s ease-out;
}

.user-detail-dialog {
  border-radius: 12px;
}

.user-detail-content {
  text-align: center;
}

.user-detail-header {
  margin-bottom: 24px;
}

.user-detail-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 24px;
}

.stat-item {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 8px;
  text-align: center;
}

.stat-label {
  display: block;
  color: #666;
  font-size: 14px;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

@keyframes glow {
  0%, 100% { box-shadow: 0 0 5px rgba(255, 215, 0, 0.5); }
  50% { box-shadow: 0 0 20px rgba(255, 215, 0, 0.8); }
}

@keyframes slideUp {
  from { transform: translateY(10px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

@keyframes slideDown {
  from { transform: translateY(-10px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

@keyframes highlight {
  0% { background: rgba(24, 144, 255, 0.3); }
  100% { background: rgba(24, 144, 255, 0.1); }
}

.section-icon {
  margin-right: 8px;
  font-size: 1.2em;
}

.el-avatar {
  object-fit: cover;
  background-color: #f0f0f0;
  border: 1px solid #eee;
}

.el-avatar img {
  object-fit: cover;
  width: 100%;
  height: 100%;
}

/* 添加图片加载过渡效果 */
.el-avatar img {
  transition: opacity 0.3s ease;
}

.el-avatar img[src=""] {
  opacity: 0;
}

.el-avatar img:not([src=""]) {
  opacity: 1;
}
</style>