<template>
  <div class="url-encode-container">
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
    <h1 class="title">URL编码工具</h1>

    <!-- 功能介绍 -->
    <el-card class="intro-card">
      <template #header>
        <div class="card-header">
          <span>功能介绍</span>
        </div>
      </template>
      <p class="intro-text">
        URL编码工具可以帮助您对URL进行编码和解码操作，支持UTF-8编码，并提供了格式化功能。
        编码后的URL可以安全地在网络上传输，避免特殊字符导致的错误。
      </p>
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
        placeholder="请输入需要编码或解码的文本..."
        resize="none"
      />
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button-group>
        <el-button type="primary" @click="encodeUrl">URL编码</el-button>
        <el-button type="success" @click="decodeUrl">URL解码</el-button>
        <el-button type="warning" @click="formatUrl">格式化URL</el-button>
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
        placeholder="编码/解码结果将显示在这里..."
        resize="none"
        readonly
      />
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
        <el-table-column prop="type" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === '编码' ? 'primary' : 'success'">
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

// 返回主页
const goBack = () => {
  router.push('/bmgf/private')
}

// 清空输入
const clearInput = () => {
  inputText.value = ''
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

// URL编码
const encodeUrl = () => {
  if (!inputText.value) {
    ElMessage.warning('请输入要编码的内容')
    return
  }
  try {
    outputText.value = encodeURIComponent(inputText.value)
    addToHistory('编码')
    ElMessage.success('编码成功')
  } catch (error) {
    ElMessage.error('编码失败：' + error.message)
  }
}

// URL解码
const decodeUrl = () => {
  if (!inputText.value) {
    ElMessage.warning('请输入要解码的内容')
    return
  }
  try {
    outputText.value = decodeURIComponent(inputText.value)
    addToHistory('解码')
    ElMessage.success('解码成功')
  } catch (error) {
    ElMessage.error('解码失败：' + error.message)
  }
}

// 格式化URL
const formatUrl = () => {
  if (!inputText.value) {
    ElMessage.warning('请输入要格式化的URL')
    return
  }
  try {
    const url = new URL(inputText.value)
    outputText.value = JSON.stringify({
      protocol: url.protocol,
      hostname: url.hostname,
      port: url.port,
      pathname: url.pathname,
      search: url.search,
      hash: url.hash
    }, null, 2)
    addToHistory('格式化')
    ElMessage.success('格式化成功')
  } catch (error) {
    ElMessage.error('URL格式化失败：' + error.message)
  }
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
  link.download = 'url_encode_result.txt'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

// 添加历史记录
const addToHistory = (type) => {
  history.value.unshift({
    input: inputText.value,
    output: outputText.value,
    type: type,
    time: new Date().toLocaleString()
  })
  // 限制历史记录数量
  if (history.value.length > 10) {
    history.value.pop()
  }
  // 保存到本地存储
  localStorage.setItem('urlEncodeHistory', JSON.stringify(history.value))
}

// 清空历史记录
const clearHistory = () => {
  history.value = []
  localStorage.removeItem('urlEncodeHistory')
  ElMessage.success('历史记录已清空')
}

// 使用历史记录项
const useHistoryItem = (item) => {
  inputText.value = item.input
  outputText.value = item.output
}

// 删除历史记录项
const deleteHistoryItem = (item) => {
  const index = history.value.indexOf(item)
  if (index > -1) {
    history.value.splice(index, 1)
    localStorage.setItem('urlEncodeHistory', JSON.stringify(history.value))
    ElMessage.success('已删除历史记录')
  }
}

// 加载历史记录
onMounted(() => {
  const savedHistory = localStorage.getItem('urlEncodeHistory')
  if (savedHistory) {
    history.value = JSON.parse(savedHistory)
  }
})
</script>

<style scoped>
.url-encode-container {
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

.intro-card {
  margin-bottom: 30px;
}

.intro-text {
  color: #606266;
  line-height: 1.6;
}

.input-card,
.output-card {
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
</style> 