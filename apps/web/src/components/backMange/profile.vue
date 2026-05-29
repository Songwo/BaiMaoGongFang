<template>
  <el-container class="profile-container">
    <!-- 加载进度条 -->
    <div class="loading-progress" v-if="isLoading">
      <el-progress 
        :percentage="progress" 
        :show-text="false" 
        :stroke-width="2" 
        :color="'#409EFF'"
      />
    </div>

    <el-main class="profile-main">
      <el-row :gutter="20">
        <!-- 左侧个人信息卡片 -->
        <el-col :xs="24" :sm="8">
          <el-card class="profile-card" shadow="hover">
            <div class="avatar-container">
              <el-avatar 
                :size="120" 
                :src="computedAvatarUrl" 
                @click="handleAvatarClick"
                class="avatar-hover"
              />
              <div class="avatar-hint">点击更换头像</div>
              <input
                type="file"
                ref="avatarInput"
                hidden
                accept="image/*"
                @change="uploadAvatar"
              />
            </div>
            <div class="user-info">
              <h2>{{ userInfo.username }}</h2>
              <p class="email">{{ userInfo.email }}</p>
              <el-tag v-if="userInfo.emailVerified" type="success" effect="dark">邮箱已验证</el-tag>
              <el-tag v-else type="warning" effect="dark">邮箱未验证</el-tag>
            </div>
            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-value">{{ userInfo.totalScore }}</span>
                <span class="stat-label">总积分</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ userInfo.correctCount }}</span>
                <span class="stat-label">正确答题</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ userInfo.totalAttempts }}</span>
                <span class="stat-label">总尝试</span>
              </div>
            </div>
          </el-card>

          <!-- 安全设置卡片 -->
          <el-card class="security-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Lock /></el-icon>
                <span>安全设置</span>
              </div>
            </template>
            <div class="security-items">
              <div class="security-item" @click="handleChangePassword">
                <el-icon><Key /></el-icon>
                <span>修改密码</span>
                <el-icon><ArrowRight /></el-icon>
              </div>
              <div class="security-item" @click="handleVerifyEmail">
                <el-icon><Message /></el-icon>
                <span>邮箱验证</span>
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧信息编辑卡片 -->
        <el-col :xs="24" :sm="16">
          <el-card class="edit-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Edit /></el-icon>
                <span>编辑资料</span>
              </div>
            </template>
            <el-form 
              :model="editForm" 
              :rules="rules" 
              ref="formRef" 
              label-width="100px"
              class="edit-form"
            >
              <el-form-item label="用户名" prop="username">
                <el-input v-model="editForm.username" placeholder="请输入用户名">
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="电子邮箱" prop="email">
                <el-input v-model="editForm.email" placeholder="请输入电子邮箱">
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveChanges" :loading="saving">
                  保存修改
                </el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 最近活动卡片 -->
          <el-card class="activity-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Timer /></el-icon>
                <span>最近活动</span>
              </div>
            </template>
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in activities"
                :key="index"
                :timestamp="activity.time"
                :type="activity.type"
              >
                {{ activity.content }}
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>
      </el-row>
    </el-main>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form
        :model="passwordForm"
        :rules="passwordRules"
        ref="passwordFormRef"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="passwordForm.currentPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePasswordChanges" :loading="savingPassword">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Message,
  Lock,
  Edit,
  Timer,
  Key,
  ArrowRight
} from '@element-plus/icons-vue'
import axios from 'axios'
import ToUrl from '@/api/api'

const store = useStore()

// 加载状态
const isLoading = ref(false)
const progress = ref(0)
let progressTimer = null

// 用户信息
const userInfo = ref({
  id: '',
  username: '',
  email: '',
  avatar: '',
  totalScore: 0,
  emailVerified: false,
  createTime: '',
  roles: [],
  lastLoginTime: '',
  correctCount: 0,
  totalAttempts: 0,
  completedQuestions: [],
  vulnScores: {},
  activeLabs: [],
  completedLabs: [],
  currentLabToken: null
})

// 编辑表单
const editForm = ref({
  username: '',
  email: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 密码表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 对话框状态
const passwordDialogVisible = ref(false)
const saving = ref(false)
const savingPassword = ref(false)

// 最近活动数据
const activities = ref([
  {
    content: '修改了个人资料',
    time: '2024-03-20 14:30',
    type: 'primary'
  },
  {
    content: '完成了安全认证',
    time: '2024-03-19 10:15',
    type: 'success'
  },
  {
    content: '登录了系统',
    time: '2024-03-18 09:00',
    type: 'info'
  }
])

// 计算头像URL
const computedAvatarUrl = computed(() => {
  const avatar = store.state.avatar
  return avatar ? `${ToUrl.url}/${avatar}` : 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

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

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const response = await axios.get(`${ToUrl.url}/user/mes/${store.state.user}`, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    const data = response.data.data
    userInfo.value = {
      ...data,
      email: data.email || '',
      createTime: data.createTime || '',
      lastLoginTime: data.lastLoginTime || ''
    }
    editForm.value = {
      username: data.username,
      email: data.email || ''
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 点击头像
const handleAvatarClick = () => {
  const input = document.querySelector('input[type="file"]')
  input.click()
}

// 上传头像
const uploadAvatar = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('avatar', file)

  try {
    const response = await axios.post(`${ToUrl.url}/user/avatar`, formData, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`,
        'Content-Type': 'multipart/form-data'
      }
    })
    store.commit('setAvatar', response.data.avatar)
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error('头像上传失败')
  }
}

// 修改密码
const handleChangePassword = () => {
  passwordDialogVisible.value = true
}

// 保存密码修改
const savePasswordChanges = async () => {
  try {
    savingPassword.value = true
    await axios.put(`${ToUrl.url}/user/changepwd`, {
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    }, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    savingPassword.value = false
  }
}

// 验证邮箱
const handleVerifyEmail = async () => {
  try {
    await axios.post(`${ToUrl.url}/user/verify-email`, {}, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    ElMessage.success('验证邮件已发送，请查收')
  } catch (error) {
    ElMessage.error('发送验证邮件失败')
  }
}

// 保存修改
const saveChanges = async () => {
  try {
    saving.value = true
    await axios.put(`${ToUrl.url}/user/update`, editForm.value, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    store.commit('setUser', editForm.value.username)
    ElMessage.success('信息保存成功')
    fetchUserInfo()
  } catch (error) {
    ElMessage.error('信息保存失败')
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  editForm.value = {
    username: userInfo.value.username,
    email: userInfo.value.email
  }
}

// 组件挂载时
onMounted(() => {
  startLoading()
  fetchUserInfo()
  setTimeout(finishLoading, 1000)
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.profile-main {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  margin-bottom: 20px;
  text-align: center;
}

.avatar-container {
  position: relative;
  margin-bottom: 20px;
}

.avatar-hover {
  cursor: pointer;
  transition: transform 0.3s;
}

.avatar-hover:hover {
  transform: scale(1.05);
}

.avatar-hint {
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #909399;
}

.user-info {
  margin: 20px 0;
}

.user-info h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.user-info .email {
  margin: 10px 0;
  color: #909399;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.security-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.security-item:hover {
  background-color: #f5f7fa;
}

.edit-card {
  margin-bottom: 20px;
}

.edit-form {
  max-width: 500px;
}

.activity-card {
  margin-top: 20px;
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
</style> 