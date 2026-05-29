<template>
  <div class="password-container">
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button class="back-btn" type="text" @click="goBack">
        <el-icon :size="24" class="icon">
          <ArrowLeft />
        </el-icon>
        返回
      </el-button>
    </div>

    <!-- 标题 -->
    <h1 class="title">密码强度检测工具</h1>

    <!-- 功能介绍 -->
    <el-card class="intro-card">
      <template #header>
        <div class="card-header">
          <span>功能介绍</span>
        </div>
      </template>
      <p class="intro-text">
        密码强度检测工具可以帮助您评估密码的安全性。
        通过分析密码的长度、复杂度、字符类型等因素，给出密码强度评分和建议。
      </p>
    </el-card>

    <!-- 配置区域 -->
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>检测配置</span>
        </div>
      </template>
      <div class="config-content">
        <el-form :model="config" label-width="100px">
          <el-form-item label="检测规则">
            <el-checkbox-group v-model="config.rules">
              <el-checkbox label="length">长度检查</el-checkbox>
              <el-checkbox label="complexity">复杂度检查</el-checkbox>
              <el-checkbox label="common">常见密码检查</el-checkbox>
              <el-checkbox label="pattern">模式检查</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="最小长度">
            <el-input-number v-model="config.minLength" :min="6" :max="32" />
          </el-form-item>
          <el-form-item label="字符类型">
            <el-checkbox-group v-model="config.charTypes">
              <el-checkbox label="lowercase">小写字母</el-checkbox>
              <el-checkbox label="uppercase">大写字母</el-checkbox>
              <el-checkbox label="numbers">数字</el-checkbox>
              <el-checkbox label="special">特殊字符</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 输入区域 -->
    <el-card class="input-card">
      <template #header>
        <div class="card-header">
          <span>密码输入</span>
          <div class="header-actions">
            <el-button type="primary" @click="clearInput">清空</el-button>
            <el-button type="success" @click="generatePassword">生成密码</el-button>
          </div>
        </div>
      </template>
      <el-input
        v-model="password"
        type="password"
        placeholder="请输入要检测的密码..."
        show-password
        class="password-input"
      />
      <div class="password-strength">
        <el-progress
          :percentage="strengthScore"
          :status="getStrengthStatus"
          :format="format"
        />
        <div class="strength-label">{{ strengthLabel }}</div>
      </div>
    </el-card>

    <!-- 检测结果 -->
    <el-card class="result-card">
      <template #header>
        <div class="card-header">
          <span>检测结果</span>
        </div>
      </template>
      <div class="result-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="密码长度">
            {{ password.length }} 个字符
          </el-descriptions-item>
          <el-descriptions-item label="字符类型">
            <el-tag v-for="type in usedCharTypes" :key="type" class="char-type-tag">
              {{ type }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="强度评分">
            <el-rate
              v-model="strengthScore"
              :max="5"
              disabled
              show-score
              text-color="#ff9900"
            />
          </el-descriptions-item>
          <el-descriptions-item label="建议">
            <ul class="suggestions">
              <li v-for="(suggestion, index) in suggestions" :key="index">
                {{ suggestion }}
              </li>
            </ul>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 历史记录 -->
    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span>历史记录</span>
          <el-button type="danger" @click="clearHistory">清空历史</el-button>
        </div>
      </template>
      <el-table :data="history" style="width: 100%">
        <el-table-column prop="password" label="密码" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="password-mask">{{ maskPassword(row.password) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="强度评分" width="120">
          <template #default="{ row }">
            <el-rate
              v-model="row.score"
              :max="5"
              disabled
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        <el-table-column prop="checkTime" label="检测时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="text" @click="useHistoryItem(row)">使用</el-button>
            <el-button type="text" @click="deleteHistoryItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const password = ref('')
const history = ref([])

const config = ref({
  rules: ['length', 'complexity', 'common', 'pattern'],
  minLength: 8,
  charTypes: ['lowercase', 'uppercase', 'numbers', 'special']
})

// 返回主页
const goBack = () => {
  router.push('/bmgf/private')
}

// 清空输入
const clearInput = () => {
  password.value = ''
}

// 生成密码
const generatePassword = () => {
  const chars = {
    lowercase: 'abcdefghijklmnopqrstuvwxyz',
    uppercase: 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
    numbers: '0123456789',
    special: '!@#$%^&*()_+-=[]{}|;:,.<>?'
  }

  let availableChars = ''
  config.value.charTypes.forEach(type => {
    availableChars += chars[type]
  })

  let generatedPassword = ''
  for (let i = 0; i < config.value.minLength; i++) {
    generatedPassword += availableChars.charAt(Math.floor(Math.random() * availableChars.length))
  }

  password.value = generatedPassword
  ElMessage.success('密码已生成')
}

// 计算密码强度
const strengthScore = computed(() => {
  if (!password.value) return 0

  let score = 0
  const length = password.value.length

  // 长度得分
  if (length >= 12) score += 2
  else if (length >= 8) score += 1

  // 字符类型得分
  const hasLowercase = /[a-z]/.test(password.value)
  const hasUppercase = /[A-Z]/.test(password.value)
  const hasNumbers = /[0-9]/.test(password.value)
  const hasSpecial = /[^a-zA-Z0-9]/.test(password.value)

  const typeCount = [hasLowercase, hasUppercase, hasNumbers, hasSpecial].filter(Boolean).length
  score += typeCount

  // 转换为5分制
  return Math.min(Math.floor((score / 6) * 5), 5)
})

// 获取强度状态
const getStrengthStatus = computed(() => {
  if (strengthScore.value >= 4) return 'success'
  if (strengthScore.value >= 2) return 'warning'
  return 'exception'
})

// 获取强度标签
const strengthLabel = computed(() => {
  if (strengthScore.value >= 4) return '非常强'
  if (strengthScore.value >= 3) return '强'
  if (strengthScore.value >= 2) return '中等'
  if (strengthScore.value >= 1) return '弱'
  return '非常弱'
})

// 获取使用的字符类型
const usedCharTypes = computed(() => {
  const types = []
  if (/[a-z]/.test(password.value)) types.push('小写字母')
  if (/[A-Z]/.test(password.value)) types.push('大写字母')
  if (/[0-9]/.test(password.value)) types.push('数字')
  if (/[^a-zA-Z0-9]/.test(password.value)) types.push('特殊字符')
  return types
})

// 获取建议
const suggestions = computed(() => {
  const suggestions = []
  if (password.value.length < config.value.minLength) {
    suggestions.push(`密码长度至少需要 ${config.value.minLength} 个字符`)
  }
  if (!/[a-z]/.test(password.value)) {
    suggestions.push('建议包含小写字母')
  }
  if (!/[A-Z]/.test(password.value)) {
    suggestions.push('建议包含大写字母')
  }
  if (!/[0-9]/.test(password.value)) {
    suggestions.push('建议包含数字')
  }
  if (!/[^a-zA-Z0-9]/.test(password.value)) {
    suggestions.push('建议包含特殊字符')
  }
  return suggestions
})

// 格式化进度条显示
const format = (percentage) => {
  return `${percentage * 20}%`
}

// 添加历史记录
const addToHistory = () => {
  if (!password.value) return

  history.value.unshift({
    password: password.value,
    score: strengthScore.value,
    checkTime: new Date().toLocaleString()
  })

  // 限制历史记录数量
  if (history.value.length > 10) {
    history.value.pop()
  }

  // 保存到本地存储
  localStorage.setItem('passwordHistory', JSON.stringify(history.value))
}

// 清空历史记录
const clearHistory = () => {
  history.value = []
  localStorage.removeItem('passwordHistory')
  ElMessage.success('历史记录已清空')
}

// 使用历史记录项
const useHistoryItem = (item) => {
  password.value = item.password
}

// 删除历史记录项
const deleteHistoryItem = (item) => {
  const index = history.value.indexOf(item)
  if (index > -1) {
    history.value.splice(index, 1)
    localStorage.setItem('passwordHistory', JSON.stringify(history.value))
    ElMessage.success('已删除历史记录')
  }
}

// 密码掩码显示
const maskPassword = (pwd) => {
  return '*'.repeat(pwd.length)
}

// 监听密码变化，自动添加到历史记录
watch(password, () => {
  if (password.value) {
    addToHistory()
  }
})

// 加载历史记录
onMounted(() => {
  const savedHistory = localStorage.getItem('passwordHistory')
  if (savedHistory) {
    history.value = JSON.parse(savedHistory)
  }
})
</script>

<style scoped>
.password-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.back-button {
  margin-bottom: 20px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #fff;
  transition: all 0.3s ease;
}

.back-btn:hover {
  transform: translateX(-5px);
  color: rgba(255, 255, 255, 0.8);
}

.title {
  text-align: center;
  font-size: 28px;
  color: #303133;
  margin-bottom: 30px;
}

.intro-card,
.config-card {
  margin-bottom: 30px;
}

.intro-text {
  color: #606266;
  line-height: 1.6;
}

.config-content {
  padding: 20px;
}

.input-card,
.result-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.password-input {
  margin-bottom: 20px;
}

.password-strength {
  margin-top: 10px;
}

.strength-label {
  text-align: center;
  margin-top: 5px;
  font-size: 14px;
  color: #606266;
}

.result-content {
  padding: 20px;
}

.char-type-tag {
  margin-right: 5px;
}

.suggestions {
  margin: 0;
  padding-left: 20px;
  color: #606266;
}

.suggestions li {
  margin-bottom: 5px;
}

.history-card {
  margin-top: 30px;
}

.password-mask {
  font-family: monospace;
  letter-spacing: 2px;
}

:deep(.el-card__header) {
  background-color: #f5f7fa;
  padding: 15px 20px;
}

:deep(.el-progress-bar__inner) {
  transition: width 0.3s ease;
}
</style>