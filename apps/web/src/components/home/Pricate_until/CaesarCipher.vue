<template>
  <div class="caesar-container">
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
    <h1 class="title">凯撒密码加解密工具</h1>

    <!-- 功能介绍 -->
    <el-card class="intro-card">
      <template #header>
        <div class="card-header">
          <span>功能介绍</span>
        </div>
      </template>
      <p class="intro-text">
        凯撒密码是一种替换加密的技术，通过将字母表中的每个字母移动固定的位数来实现加密。
        本工具支持自定义位移量，并提供暴力破解功能，帮助您快速解密未知位移量的密文。
      </p>
    </el-card>

    <!-- 配置区域 -->
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>加密配置</span>
        </div>
      </template>
      <div class="config-content">
        <el-form :model="config" label-width="100px">
          <el-form-item label="位移量">
            <el-slider
              v-model="config.shift"
              :min="-25"
              :max="25"
              :step="1"
              show-stops
              show-input
            />
          </el-form-item>
          <el-form-item label="字母表">
            <el-radio-group v-model="config.alphabet">
              <el-radio label="lower">小写字母 (a-z)</el-radio>
              <el-radio label="upper">大写字母 (A-Z)</el-radio>
              <el-radio label="both">全部字母 (a-zA-Z)</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="保留空格">
            <el-switch v-model="config.preserveSpaces" />
          </el-form-item>
          <el-form-item label="保留标点">
            <el-switch v-model="config.preservePunctuation" />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 输入区域 -->
    <el-card class="input-card">
      <template #header>
        <div class="card-header">
          <span>输入区域</span>
          <div class="header-actions">
            <el-button type="primary" @click="clearInput">清空</el-button>
            <el-button type="success" @click="copyInput">复制</el-button>
          </div>
        </div>
      </template>
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="6"
        placeholder="请输入需要加密或解密的文本..."
        resize="none"
      />
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button-group>
        <el-button type="primary" @click="encrypt">加密</el-button>
        <el-button type="success" @click="decrypt">解密</el-button>
        <el-button type="warning" @click="bruteForce">暴力破解</el-button>
      </el-button-group>
    </div>

    <!-- 输出区域 -->
    <el-card class="output-card">
      <template #header>
        <div class="card-header">
          <span>输出结果</span>
          <div class="header-actions">
            <el-button type="primary" @click="copyOutput">复制结果</el-button>
            <el-button type="success" @click="downloadResult">下载结果</el-button>
          </div>
        </div>
      </template>
      <el-input
        v-model="outputText"
        type="textarea"
        :rows="6"
        placeholder="加密/解密结果将显示在这里..."
        resize="none"
        readonly
      />
    </el-card>

    <!-- 暴力破解结果 -->
    <el-card v-if="bruteForceResults.length > 0" class="brute-force-card">
      <template #header>
        <div class="card-header">
          <span>暴力破解结果</span>
        </div>
      </template>
      <el-table :data="bruteForceResults" style="width: 100%">
        <el-table-column prop="shift" label="位移量" width="100" />
        <el-table-column prop="result" label="解密结果" show-overflow-tooltip />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="text" @click="useBruteForceResult(row)">使用</el-button>
          </template>
        </el-table-column>
      </el-table>
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
        <el-table-column prop="input" label="输入内容" show-overflow-tooltip />
        <el-table-column prop="output" label="输出结果" show-overflow-tooltip />
        <el-table-column prop="shift" label="位移量" width="100" />
        <el-table-column prop="type" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === '加密' ? 'primary' : 'success'">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="操作时间" width="180" />
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const inputText = ref('')
const outputText = ref('')
const history = ref([])
const bruteForceResults = ref([])

const config = ref({
  shift: 3,
  alphabet: 'both',
  preserveSpaces: true,
  preservePunctuation: true
})

// 返回主页
const goBack = () => {
  router.push('/bmgf/private')
}

// 清空输入
const clearInput = () => {
  inputText.value = ''
  outputText.value = ''
  bruteForceResults.value = []
}

// 复制输入
const copyInput = () => {
  if (!inputText.value) {
    ElMessage.warning('请输入要复制的内容')
    return
  }
  navigator.clipboard.writeText(inputText.value)
  ElMessage.success('已复制到剪贴板')
}

// 凯撒密码加密
const encrypt = () => {
  if (!inputText.value) {
    ElMessage.warning('请输入要加密的内容')
    return
  }
  try {
    outputText.value = caesarCipher(inputText.value, config.value.shift, true)
    addToHistory('加密')
    ElMessage.success('加密成功')
  } catch (error) {
    ElMessage.error('加密失败：' + error.message)
  }
}

// 凯撒密码解密
const decrypt = () => {
  if (!inputText.value) {
    ElMessage.warning('请输入要解密的内容')
    return
  }
  try {
    outputText.value = caesarCipher(inputText.value, config.value.shift, false)
    addToHistory('解密')
    ElMessage.success('解密成功')
  } catch (error) {
    ElMessage.error('解密失败：' + error.message)
  }
}

// 暴力破解
const bruteForce = () => {
  if (!inputText.value) {
    ElMessage.warning('请输入要破解的内容')
    return
  }
  bruteForceResults.value = []
  for (let shift = 1; shift <= 25; shift++) {
    const result = caesarCipher(inputText.value, shift, false)
    bruteForceResults.value.push({
      shift: shift,
      result: result
    })
  }
  ElMessage.success('暴力破解完成')
}

// 凯撒密码核心算法
const caesarCipher = (text, shift, encrypt) => {
  const alphabet = getAlphabet()
  const result = []
  
  for (let i = 0; i < text.length; i++) {
    const char = text[i]
    
    // 处理空格
    if (char === ' ' && config.value.preserveSpaces) {
      result.push(char)
      continue
    }
    
    // 处理标点符号
    if (isPunctuation(char) && config.value.preservePunctuation) {
      result.push(char)
      continue
    }
    
    // 处理字母
    const index = alphabet.indexOf(char)
    if (index !== -1) {
      const newIndex = encrypt
        ? (index + shift + alphabet.length) % alphabet.length
        : (index - shift + alphabet.length) % alphabet.length
      result.push(alphabet[newIndex])
    } else {
      result.push(char)
    }
  }
  
  return result.join('')
}

// 获取字母表
const getAlphabet = () => {
  switch (config.value.alphabet) {
    case 'lower':
      return 'abcdefghijklmnopqrstuvwxyz'
    case 'upper':
      return 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
    case 'both':
      return 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'
    default:
      return 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'
  }
}

// 判断是否为标点符号
const isPunctuation = (char) => {
  return /[.,\/#!$%\^&\*;:{}=\-_`~()]/.test(char)
}

// 复制输出
const copyOutput = () => {
  if (!outputText.value) {
    ElMessage.warning('没有可复制的内容')
    return
  }
  navigator.clipboard.writeText(outputText.value)
  ElMessage.success('已复制到剪贴板')
}

// 下载结果
const downloadResult = () => {
  if (!outputText.value) {
    ElMessage.warning('没有可下载的内容')
    return
  }
  const blob = new Blob([outputText.value], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'caesar_cipher_result.txt'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

// 使用暴力破解结果
const useBruteForceResult = (row) => {
  inputText.value = row.result
  config.value.shift = row.shift
}

// 添加历史记录
const addToHistory = (type) => {
  history.value.unshift({
    input: inputText.value,
    output: outputText.value,
    shift: config.value.shift,
    type: type,
    time: new Date().toLocaleString()
  })
  // 限制历史记录数量
  if (history.value.length > 10) {
    history.value.pop()
  }
  // 保存到本地存储
  localStorage.setItem('caesarCipherHistory', JSON.stringify(history.value))
}

// 清空历史记录
const clearHistory = () => {
  history.value = []
  localStorage.removeItem('caesarCipherHistory')
  ElMessage.success('历史记录已清空')
}

// 使用历史记录项
const useHistoryItem = (item) => {
  inputText.value = item.input
  outputText.value = item.output
  config.value.shift = item.shift
}

// 删除历史记录项
const deleteHistoryItem = (item) => {
  const index = history.value.indexOf(item)
  if (index > -1) {
    history.value.splice(index, 1)
    localStorage.setItem('caesarCipherHistory', JSON.stringify(history.value))
    ElMessage.success('已删除历史记录')
  }
}

// 加载历史记录
onMounted(() => {
  const savedHistory = localStorage.getItem('caesarCipherHistory')
  if (savedHistory) {
    history.value = JSON.parse(savedHistory)
  }
})
</script>

<style scoped>
.caesar-container {
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
.output-card,
.brute-force-card {
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

.action-buttons {
  margin: 20px 0;
  display: flex;
  justify-content: center;
}

.history-card {
  margin-top: 30px;
}

:deep(.el-card__header) {
  background-color: #f5f7fa;
  padding: 15px 20px;
}

:deep(.el-textarea__inner) {
  font-family: monospace;
}

:deep(.el-slider) {
  width: 100%;
}
</style> 