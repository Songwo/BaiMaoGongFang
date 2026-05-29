<template>
  <div class="verify-email-container">
    <div class="background-animation"></div>
    <div class="content-wrapper">
      <div class="logo-section">
        <img src="@/assets/logo/logo/信息.png" alt="Logo" class="logo" />
        <h1 class="site-name">白帽工坊-网络攻防安全学习平台</h1>
      </div>
      
      <el-card class="verify-card glass-effect">
        <template #header>
          <div class="card-header">
            <el-icon :size="24"><Message /></el-icon>
            <span>邮箱验证</span>
          </div>
        </template>

        <div class="verify-content">
          <div v-if="verifying" class="verifying">
            <el-icon class="loading-icon" :size="32"><Loading /></el-icon>
            <p class="status-text">正在验证邮箱，请稍候...</p>
          </div>

          <div v-else-if="verificationSuccess" class="success">
            <el-icon class="success-icon" :size="32"><CircleCheck /></el-icon>
            <h2 class="status-title">验证成功！</h2>
            <p class="status-text">您的邮箱已验证成功</p>
            <el-button type="primary" @click="goToProfile" class="action-button">返回个人中心</el-button>
          </div>

          <div v-else-if="verificationError" class="error">
            <el-icon class="error-icon" :size="32"><CircleClose /></el-icon>
            <h2 class="status-title">验证失败</h2>
            <p class="status-text error-message">{{ errorMessage }}</p>
            <el-button type="primary" @click="goToProfile" class="action-button">返回个人中心</el-button>
          </div>
        </div>
      </el-card>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message, Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import ToUrl from '@/api/api'
import store from '@/store'

const route = useRoute()
const router = useRouter()

const verifying = ref(true)
const verificationSuccess = ref(false)
const verificationError = ref(false)
const errorMessage = ref('')

const verifyEmail = async () => {
  const token = route.query.token
  if (!token) {
    verificationError.value = true
    errorMessage.value = '无效的验证链接'
    verifying.value = false
    return
  }

  try {
    await axios.post(ToUrl.url + '/user/verify-email', {
      token: token
    }, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    
    verificationSuccess.value = true
    ElMessage.success('邮箱验证成功')
  } catch (error) {
    verificationError.value = true
    errorMessage.value = error.response?.data?.message || '验证失败，请重试'
    ElMessage.error(errorMessage.value)
  } finally {
    verifying.value = false
  }
}

const goToProfile = () => {
  router.push('/bmgf/profile')
}

onMounted(() => {
  verifyEmail()
})
</script>

<style scoped>
.verify-email-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.background-animation {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  animation: gradientBG 15s ease infinite;
  z-index: 0;
}

@keyframes gradientBG {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.content-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 800px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
}

.logo-section {
  text-align: center;
  margin-bottom: 20px;
}

.logo {
  width: 120px;
  height: auto;
  margin-bottom: 10px;
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.3));
}

.site-name {
  color: #ffffff;
  font-size: 32px;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  margin: 0;
}

.verify-card {
  width: 100%;
  max-width: 500px;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
  padding: 15px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.verify-content {
  padding: 30px;
  text-align: center;
}

.verifying,
.success,
.error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.loading-icon {
  animation: rotate 1s linear infinite;
  color: #ffffff;
}

.success-icon {
  color: #67C23A;
}

.error-icon {
  color: #F56C6C;
}

.status-title {
  margin: 0;
  color: #ffffff;
  font-size: 24px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.status-text {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  line-height: 1.5;
}

.error-message {
  color: #F56C6C;
  font-weight: 500;
}

.action-button {
  margin-top: 20px;
  min-width: 120px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #ffffff;
  transition: all 0.3s ease;
}

.action-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

:deep(.el-button) {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #ffffff;
}

:deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
  color: #ffffff;
}

:deep(.el-card) {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-card__header) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.app-footer {
  text-align: center;
  padding: 20px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin-top: 30px;
  width: 100%;
  max-width: 800px;
}

.app-footer p {
  margin: 8px 0;
  line-height: 1.5;
}

.app-footer a {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  transition: color 0.3s ease;
}

.app-footer a:hover {
  color: #ffffff;
  text-decoration: underline;
}

.app-footer .disclaimer {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  margin-top: 15px;
}
</style>