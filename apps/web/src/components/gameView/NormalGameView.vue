<template>
  <div class="normal-game-container">
    <!-- 标题栏 -->
    <div class="header">
      <div class="title">网络安全靶场</div>
      <div class="header-buttons">
        <el-button type="text" class="switch-btn" @click="handleSwitchView">
          切换到VIP版
        </el-button>
        <el-button class="tutorial-button" @click="showTutorial">
          <el-icon><Guide /></el-icon>
          查看教程
        </el-button>
      </div>
    </div>

    <!-- 顶部数据统计 -->
    <el-row :gutter="16" class="dashboard-stats">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" v-for="(stat, index) in dashboardStats" :key="index">
        <div class="stat-card">
          <div class="stat-icon"><el-icon><component :is="stat.icon" /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 挑战列表 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card v-for="(challenge, index) in challenges" :key="challenge.id" class="challenge-card">
          <template #header>
            <div class="card-header">
              <div class="title-section">
                <h2>{{ challenge.title }}</h2>
                <el-tag :type="getDifficultyType(challenge.difficulty)" class="difficulty-tag">
                  ★{{ challenge.difficulty }}
                </el-tag>
              </div>
            </div>
          </template>

          <div class="card-content">
            <el-row :gutter="20">
              <el-col :span="16">
                <div class="left-content">
                  <div class="description-section">
                    <h3>
                      <el-icon><Document /></el-icon>
                      挑战描述
                    </h3>
                    <p>{{ challenge.description }}</p>
                  </div>
                  <div class="task-section">
                    <h3>
                      <el-icon><Flag /></el-icon>
                      任务目标
                    </h3>
                    <p>{{ challenge.task }}</p>
                  </div>
                </div>
              </el-col>

              <el-col :span="8">
                <div class="right-content">
                  <div class="action-section">
                    <div class="action-buttons">
                      <el-button type="primary" size="large" @click="startLab(challenge)" 
                        :loading="challenge.loading"
                        :disabled="challenge.disabled || !challenge.unlocked || challenge.completed || isAnyLabRunning() && !challenge.labUrl">
                        启动靶场
                      </el-button>
                      <el-button type="success" size="large" @click="verifyFlag(challenge)">
                        验证FLAG
                      </el-button>
                    </div>

                    <!-- 添加进度条显示 -->
                    <div v-if="challenge.loading" class="startup-progress">
                      <el-progress 
                        :percentage="challenge.startProgress"
                        :status="challenge.startProgress === 100 ? 'success' : ''"
                        :stroke-width="20"
                        :show-text="true">
                        <template #default="{ percentage }">
                          <span class="progress-text">
                            {{ percentage < 100 ? '启动中...' : '启动完成' }}
                            {{ percentage }}%
                          </span>
                        </template>
                      </el-progress>
                    </div>

                    <div v-if="challenge.labUrl" class="lab-info">
                      <el-link :href="challenge.labUrl" target="_blank" type="primary" class="lab-link">
                        前往靶场
                      </el-link>
                    </div>

                    <div v-if="challenge.completed" class="completion-section">
                      <el-alert type="success" description="挑战通关！" show-icon />
                      <div class="completion-details">
                        <p>完成时间：{{ formatTime(challenge.completionTime) }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 开发中提示 -->
    <el-card class="coming-soon-notice">
      <div class="coming-soon-content">
        <el-icon class="coming-soon-icon"><Tools /></el-icon>
        <div class="coming-soon-text">
          <h3>更多靶场正在开发中</h3>
          <p>我们正在努力开发更多有趣的靶场挑战，敬请期待！</p>
        </div>
      </div>
    </el-card>

    <!-- VIP提示 -->
    <el-card class="vip-notice">
      <div class="vip-notice-content">
        <div class="vip-features">
          <div class="vip-feature-item">
            <el-icon class="feature-icon"><Monitor /></el-icon>
            <div class="feature-info">
              <h4>独立靶场环境</h4>
              <p>独享专属靶场，避免多人同时使用导致的干扰</p>
            </div>
          </div>
          <div class="vip-feature-item">
            <el-icon class="feature-icon"><Timer /></el-icon>
            <div class="feature-info">
              <h4>无限练习时长</h4>
              <p>突破普通用户的时间限制，尽情练习</p>
            </div>
          </div>
          <div class="vip-feature-item">
            <el-icon class="feature-icon"><Trophy /></el-icon>
            <div class="feature-info">
              <h4>高级挑战</h4>
              <p>解锁更多高级漏洞挑战，提升技能水平</p>
            </div>
          </div>
          <div class="vip-feature-item">
            <el-icon class="feature-icon"><Connection /></el-icon>
            <div class="feature-info">
              <h4>实时技术支持</h4>
              <p>遇到问题随时获得专业指导</p>
            </div>
          </div>
        </div>
        <div class="vip-action">
          <div class="vip-text">
            <h3>升级VIP，开启专业学习之旅</h3>
            <p>立即升级VIP，享受更多特权，加速你的安全技能提升！</p>
          </div>
          <el-button type="warning" size="large" @click="handleSwitchView">
            <el-icon><Star /></el-icon>
            立即升级
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 教程对话框 -->
    <el-dialog
      v-model="showTutorialDialog"
      title="靶场教程"
      width="60%"
      class="tutorial-dialog"
    >
      <div class="tutorial-content">
        <el-carousel
          ref="tutorialCarousel"
          :interval="0"
          :autoplay="false"
          indicator-position="none"
          height="400px"
          class="tutorial-carousel"
        >
          <el-carousel-item v-for="(step, index) in tutorialSteps" :key="index">
            <div class="tutorial-step">
              <div class="step-number">{{ index + 1 }}</div>
              <div class="step-info">
                <h3>{{ step.title }}</h3>
                <p>{{ step.description }}</p>
                <div class="step-image" @click="handleImageClick(step.image)">
                  <el-image 
                    :src="step.image" 
                    fit="cover" 
                    style="width: 100%; max-width: 400px; border-radius: 8px; margin-top: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.3); cursor: pointer;"
                  />
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
        
        <div class="tutorial-navigation">
          <el-button 
            :disabled="currentStep === 0"
            @click="prevStep"
            class="nav-button"
          >
            <el-icon><ArrowLeft /></el-icon>
            上一步
          </el-button>
          
          <div class="step-indicators">
            <div 
              v-for="(step, index) in tutorialSteps" 
              :key="index"
              :class="['step-dot', { active: currentStep === index }]"
              @click="goToStep(index)"
            ></div>
          </div>
          
          <el-button 
            :disabled="currentStep === tutorialSteps.length - 1"
            @click="nextStep"
            class="nav-button"
          >
            下一步
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- FLAG验证对话框 -->
    <el-dialog v-model="showFlagDialog" title="验证FLAG" width="30%" class="flag-dialog">
      <el-input v-model="inputFlag" placeholder="请输入FLAG" />
      <template #footer>
        <el-button @click="showFlagDialog = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="confirmVerify">确认</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="showImagePreview"
      width="80%"
      class="image-preview-dialog"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="preview-container">
        <el-image 
          :src="currentPreviewImage" 
          fit="contain"
          style="width: 100%; height: 80vh;"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import ToUrl from '@/api/api'
import { Lock, Unlock, Check, Loading, Document, Flag, Guide, ArrowLeft, ArrowRight, 
  VideoPlay, Trophy, Connection, TrendCharts, Tools, Reading, Monitor, 
  User, Clock, Medal, Timer, Rank, Aim, QuestionFilled, Star } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import gsap from 'gsap'
import { useRouter } from 'vue-router'

const store = useStore()
const router = useRouter()

// 静态靶场数据
const challenges = ref([
  {
    id: "oregret/sql-shared-lab:latest",
    title: 'SQL注入基础',
    description: '学习基本的SQL注入技术，通过构造特殊的SQL语句来获取数据库中的敏感信息。',
    difficulty: '简单',
    task: '通过SQL注入获取管理员密码',
    flag: 'FLAG{SQL_INJECTION_BASIC_2024}',
    score: 100,
    type: 'sql',
    unlocked: true,
    completed: false,
    labConfig: {
      images: {
        frontend: { image: 'sql-injection-frontend:latest', port: 80 },
        backend: { image: 'sql-injection-backend:latest', port: 3000 }
      },
      duration: 30
    }
  },
  {
    id: 'xss-basic',
    title: 'XSS跨站脚本',
    description: '学习XSS攻击的基本原理，通过注入恶意脚本来获取用户信息。',
    difficulty: '简单',
    task: '通过XSS攻击获取用户Cookie',
    flag: 'FLAG{XSS_BASIC_2024}',
    score: 100,
    type: 'xss',
    unlocked: true,
    completed: false,
    labConfig: {
      images: {
        frontend: { image: 'xss-frontend:latest', port: 80 },
        backend: { image: 'xss-backend:latest', port: 3000 }
      },
      duration: 30
    }
  }
])

// 响应式变量
const showTutorialDialog = ref(false)
const tutorialCarousel = ref(null)
const currentStep = ref(0)
const showFlagDialog = ref(false)
const inputFlag = ref('')
const currentVerifyChallenge = ref(null)
const runningLabInfo = ref(null)
const showImagePreview = ref(false)
const currentPreviewImage = ref('')
const countdownTimer = ref(null)

// 仪表盘数据
const dashboardStats = [
  {
    icon: 'Trophy',
    value: computed(() => getCompletedChallengesCount()),
    label: '已完成挑战'
  },
  {
    icon: 'Timer',
    value: computed(() => getTotalPracticeTime()),
    label: '练习总时长'
  },
  {
    icon: 'Rank',
    value: computed(() => getSkillLevel()),
    label: '安全技能等级'
  },
  {
    icon: 'Medal',
    value: computed(() => getBadgesCount()),
    label: '技能徽章'
  }
]

// 教程步骤
const tutorialSteps = [
  {
    title: '启动靶场',
    description: '点击"启动靶场"按钮，系统会自动为你创建一个包含漏洞的靶场环境。启动过程可能需要一些时间，请耐心等待。',
    image: '/src/assets/Jiaoc/start.png'
  },
  {
    title: '访问靶场',
    description: '靶场启动成功后，点击"前往靶场"链接，系统会在新标签页中打开靶场环境。',
    image: '/src/assets/Jiaoc/前往靶场.png'
  },
  {
    title: '分析目标',
    description: '仔细阅读挑战描述和任务目标，分析目标系统中可能存在的漏洞。',
    image: '/src/assets/Jiaoc/分析.png'
  },
  {
    title: '获取FLAG',
    description: '利用发现的漏洞，尝试获取系统中的FLAG。FLAG通常是一串特定的字符串，可能隐藏在系统的某个位置。',
    image: '/src/assets/Jiaoc/通关.png'
  },
  {
    title: '提交验证',
    description: '获取到FLAG后，点击"验证FLAG"按钮，将FLAG提交给系统进行验证。验证成功后即可完成挑战。',
    image: '/src/assets/Jiaoc/验证.png'
  }
]

// 组件卸载时清理定时器
onUnmounted(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
})

// 启动靶场
const startLab = async (challenge) => {
  challenge.loading = true
  challenge.startProgress = 0
  
  const progressAnimation = gsap.to(challenge, {
    duration: 30,
    startProgress: 100,
    ease: "none",
    onUpdate: () => {
      if (challenge.startProgress >= 100) {
        progressAnimation.kill()
      }
    }
  })

  try {
    const response = await axios.post(ToUrl.url+'/lab/shared', {
      userId: store.state.id,
      vulnType: challenge.id,
      durationMinutes: 30
    }, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })

    if (response.data && response.data.accessUrl) {
      store.commit('setBackendUrl', response.data.accessUrl)
      router.push('/bmgf/game/sql')
    } else {
      ElMessage.error('启动靶场失败：未获取到后端URL')
    }
  } catch (error) {
    console.error('启动靶场失败:', error)
    ElMessage.error('启动靶场失败，请稍后重试')
  } finally {
    progressAnimation.kill()
    challenge.loading = false
  }
}

// 验证FLAG
const verifyFlag = (challenge) => {
  currentVerifyChallenge.value = challenge
  showFlagDialog.value = true
}

// 确认验证
const confirmVerify = async () => {
  if (!inputFlag.value || !currentVerifyChallenge.value) return
  
  const challenge = currentVerifyChallenge.value
  challenge.loading = true

  try {
    const response = await axios.post(
      ToUrl.url + '/lab/flag',
      {
        userId: store.state.id,
        imageName: challenge.labConfig.images.frontend.image,
        flag: inputFlag.value
      },
      { headers: { 'Authorization': `Bearer ${store.state.token}` } }
    )

    if (response.data.code == 200) {
      challenge.completed = true
      challenge.completionTime = new Date()
      ElMessage.success('验证成功！挑战通关')
    } else {
      ElMessage.error('FLAG验证失败，请重试')
    }
  } catch (error) {
    console.error('验证失败:', error)
    ElMessage.error('验证请求失败：' + error.message)
  } finally {
    challenge.loading = false
    showFlagDialog.value = false
    inputFlag.value = ''
  }
}

// 切换视图
const handleSwitchView = () => {
  if (store.state.roles.includes('ROLE_VIP')) {
    router.push('/bmgf/game/vip')
  } else {
    ElMessage.warning('请先升级为VIP用户')
  }
}

// 显示教程
const showTutorial = () => {
  showTutorialDialog.value = true
  currentStep.value = 0
  if (tutorialCarousel.value) {
    tutorialCarousel.value.setActiveItem(0)
  }
}

// 教程导航
const nextStep = () => {
  if (currentStep.value < tutorialSteps.length - 1) {
    currentStep.value++
    tutorialCarousel.value?.setActiveItem(currentStep.value)
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
    tutorialCarousel.value?.setActiveItem(currentStep.value)
  }
}

const goToStep = (index) => {
  currentStep.value = index
  tutorialCarousel.value?.setActiveItem(index)
}

// 图片预览
const handleImageClick = (imageUrl) => {
  currentPreviewImage.value = imageUrl
  showImagePreview.value = true
}

// 工具函数
const getDifficultyType = (difficulty) => {
  if (difficulty === '简单') return 'success'
  if (difficulty === '中等') return 'warning'
  if (difficulty === '困难') return 'danger'
  return 'info'
}

const formatTime = (time) => {
  return time ? new Date(time).toLocaleString() : 'N/A'
}

const isAnyLabRunning = () => {
  return challenges.value.some(ch => ch.labUrl && !ch.completed)
}

const getCompletedChallengesCount = () => {
  return challenges.value.filter(ch => ch.completed).length
}

const getTotalPracticeTime = () => {
  return `${Math.floor(Math.random() * 20) + 5}小时`
}

const getSkillLevel = () => {
  const completedCount = getCompletedChallengesCount()
  if (completedCount >= 8) return '高级'
  if (completedCount >= 4) return '中级'
  return '初级'
}

const getBadgesCount = () => {
  return Math.min(getCompletedChallengesCount(), 10) + '项'
}

// 生命周期钩子
onMounted(async () => {
  animateStatCards()
})

// 动画效果
const animateStatCards = () => {
  const cards = document.querySelectorAll('.stat-card')
  cards.forEach((card, index) => {
    gsap.from(card, {
      opacity: 0,
      y: 20,
      delay: index * 0.1,
      duration: 0.8,
      ease: 'back.out(1.7)'
    })
  })
}
</script>

<style scoped>
.normal-game-container {
  padding: 20px;
  min-height: 100vh;
  background: transparent;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #7c3aed, #b5a0f4);
  color: #fff;
  border-radius: 10px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.header-buttons {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 24px;
  font-weight: bold;
}

.tutorial-button {
  background: #fff;
  color: #6e45e2;
  border: none;
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(110, 69, 226, 0.3);
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
}

.tutorial-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(110, 69, 226, 0.4);
  background: #f0f0f0;
}

.switch-btn {
  color: white;
  font-weight: 500;
  background-color: rgba(255, 255, 255, 0.1);
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.switch-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.dashboard-stats {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  padding: 16px;
  color: white;
  display: flex;
  align-items: center;
  height: 100px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background-size: 200% 200%;
  animation: gradientBackground 5s ease infinite;
  margin-bottom: 16px;
}

.stat-card:nth-child(1) {
  background: linear-gradient(135deg, #673ab7 0%, #9c27b0 100%);
}

.stat-card:nth-child(2) {
  background: linear-gradient(135deg, #3f51b5 0%, #2196f3 100%);
}

.stat-card:nth-child(3) {
  background: linear-gradient(135deg, #009688 0%, #4caf50 100%);
}

.stat-card:nth-child(4) {
  background: linear-gradient(135deg, #ff9800 0%, #f44336 100%);
}

@keyframes gradientBackground {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.stat-icon {
  font-size: 40px;
  margin-right: 20px;
  opacity: 0.8;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.challenge-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.challenge-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-section h2 {
  margin: 0;
  font-size: 18px;
  color: #ffffff;
}

.difficulty-tag {
  font-size: 14px;
  color: #ffffff !important;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.difficulty-tag.el-tag--success {
  border-color: var(--el-color-success);
  color: #ffffff !important;
}

.difficulty-tag.el-tag--warning {
  border-color: var(--el-color-warning);
  color: #ffffff !important;
}

.difficulty-tag.el-tag--danger {
  border-color: var(--el-color-danger);
  color: #ffffff !important;
}

.card-content {
  padding: 20px;
}

.left-content {
  padding-right: 20px;
}

.description-section,
.task-section {
  margin-bottom: 20px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.description-section h3,
.task-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 10px 0;
  color: #ffffff;
  font-size: 16px;
}

.description-section p,
.task-section p {
  margin: 0;
  color: #ffffff;
  line-height: 1.6;
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 100%;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.action-buttons .el-button {
  width: 100%;
  height: 40px;
  margin: 0;
  color: #333333 !important;
}

.action-buttons .el-button--primary {
  background-color: #409eff;
  border-color: #409eff;
  color: #333333 !important;
}

.action-buttons .el-button--success {
  background-color: #67c23a;
  border-color: #67c23a;
  color: #333333 !important;
}

.startup-progress {
  margin-top: 10px;
}

.lab-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  margin-top: 10px;
}

.lab-link {
  font-size: 16px;
  font-weight: bold;
  padding: 8px 16px;
  border-radius: 6px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.lab-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.timer {
  color: #ffffff;
  font-size: 14px;
}

.completion-section {
  margin-top: 10px;
}

.completion-details {
  margin-top: 8px;
  text-align: center;
}

.completion-details p {
  color: #ffffff;
  margin: 5px 0;
}

.vip-notice {
  margin-top: 30px;
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.1), rgba(255, 193, 7, 0.05));
  border: 1px solid rgba(255, 193, 7, 0.2);
  border-radius: 12px;
  overflow: hidden;
}

.vip-notice-content {
  padding: 30px;
}

.vip-features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.vip-feature-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.vip-feature-item:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-2px);
}

.feature-icon {
  font-size: 24px;
  color: #ffc107;
}

.feature-info h4 {
  color: #ffc107;
  margin: 0 0 5px 0;
  font-size: 16px;
}

.feature-info p {
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-size: 14px;
}

.vip-action {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 20px;
  background: rgba(255, 193, 7, 0.1);
  border-radius: 8px;
}

.vip-text {
  flex: 1;
}

.vip-text h3 {
  color: #ffc107;
  margin: 0 0 10px 0;
  font-size: 20px;
}

.vip-text p {
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-size: 14px;
}

.vip-action .el-button {
  padding: 12px 24px;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 教程对话框样式 */
.tutorial-dialog {
  :deep(.el-dialog) {
    background: rgba(30, 30, 30, 0.95);
    border-radius: 12px;
    overflow: hidden;
  }

  :deep(.el-dialog__title) {
    color: #ffffff;
    font-weight: bold;
  }

  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: #ffffff;
  }

  :deep(.el-button) {
    color: #333333 !important;
  }

  :deep(.el-button--primary) {
    background-color: #409eff;
    border-color: #409eff;
    color: #333333 !important;
  }
}

.tutorial-content {
  padding: 10px;
}

.tutorial-step {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
}

.step-number {
  width: 40px;
  height: 40px;
  background: #6e45e2;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: bold;
}

.step-info {
  flex: 1;
}

.step-info h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 18px;
}

.step-info p {
  margin: 0 0 20px 0;
  color: #666;
  line-height: 1.6;
}

.tutorial-navigation {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-top: 1px solid #ebeef5;
}

.nav-button {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-indicators {
  display: flex;
  gap: 8px;
}

.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dcdfe6;
  cursor: pointer;
  transition: all 0.3s ease;
}

.step-dot.active {
  background: #6e45e2;
  transform: scale(1.2);
}

/* FLAG验证对话框样式 */
.flag-dialog {
  :deep(.el-dialog) {
    background: rgba(30, 30, 30, 0.95);
    border-radius: 12px;
  }

  :deep(.el-dialog__title) {
    color: #ffffff;
    font-weight: bold;
  }

  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: #ffffff;
  }

  :deep(.el-button) {
    color: #333333 !important;
  }

  :deep(.el-button--default) {
    background-color: #ffffff;
    border-color: #dcdfe6;
    color: #333333 !important;
  }

  :deep(.el-button--primary) {
    background-color: #409eff;
    border-color: #409eff;
    color: #333333 !important;
  }

  :deep(.el-input__inner) {
    background-color: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.2);
    color: #333333 !important;
  }

  :deep(.el-input__inner::placeholder) {
    color: #666666 !important;
  }
}

/* 图片预览对话框样式 */
.image-preview-dialog {
  :deep(.el-dialog) {
    background: rgba(30, 30, 30, 0.95);
    border-radius: 12px;
    overflow: hidden;
  }

  :deep(.el-dialog__headerbtn .el-dialog__close) {
    color: #ffffff;
  }

  :deep(.el-button) {
    color: #333333 !important;
  }
}

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  background: #000;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .header-buttons {
    width: 100%;
    justify-content: center;
  }

  .left-content {
    padding-right: 0;
    margin-bottom: 20px;
  }

  .vip-features {
    grid-template-columns: 1fr;
  }
  
  .vip-action {
    flex-direction: column;
    text-align: center;
  }
  
  .vip-action .el-button {
    width: 100%;
  }

  .tutorial-step {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}

:deep(.el-progress-bar__inner) {
  background-color: var(--el-color-primary);
}

:deep(.el-progress__text) {
  color: #ffffff;
}

:deep(.el-alert__title) {
  color: #ffffff;
}

:deep(.el-alert__description) {
  color: #ffffff;
}

.cancel-btn {
  color: #333333 !important;
}

:deep(.cancel-btn) {
  color: #333333 !important;
}

:deep(.cancel-btn:hover) {
  color: #333333 !important;
}

:deep(.cancel-btn span) {
  color: #333333 !important;
}

.coming-soon-notice {
  margin: 30px 0;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.05));
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
}

.coming-soon-content {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.coming-soon-icon {
  font-size: 32px;
  color: #409eff;
}

.coming-soon-text h3 {
  color: #ffffff;
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: bold;
}

.coming-soon-text p {
  color: #ffffff;
  margin: 0;
  font-size: 14px;
}
</style> 