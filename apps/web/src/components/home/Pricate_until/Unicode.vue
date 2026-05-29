<template>
  <div class="unicode-container">
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
    <h1 class="title">Unicode编码工具</h1>

    <!-- 功能介绍 -->
    <el-card class="intro-card">
      <template #header>
        <div class="card-header">
          <span>功能介绍</span>
        </div>
      </template>
      <p class="intro-text">
        Unicode编码工具可以帮助您将文本转换为Unicode编码，或从Unicode编码还原为文本。
        支持UTF-8、UTF-16、UTF-32等多种编码格式，并提供格式化功能，方便查看和分析。
      </p>
    </el-card>

    <!-- 配置区域 -->
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>编码配置</span>
        </div>
      </template>
      <div class="config-content">
        <el-form :model="config" label-width="100px">
          <el-form-item label="编码格式">
            <el-radio-group v-model="config.format">
              <el-radio label="utf8">UTF-8</el-radio>
              <el-radio label="utf16">UTF-16</el-radio>
              <el-radio label="utf32">UTF-32</el-radio>
              <el-radio label="unicode">Unicode</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="输出格式">
            <el-radio-group v-model="config.outputFormat">
              <el-radio label="hex">十六进制</el-radio>
              <el-radio label="dec">十进制</el-radio>
              <el-radio label="bin">二进制</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="分隔符">
            <el-input v-model="config.separator" placeholder="输入分隔符，默认为空格" />
          </el-form-item>
          <el-form-item label="前缀">
            <el-input v-model="config.prefix" placeholder="输入前缀，如\u" />
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
        placeholder="请输入需要编码或解码的文本..."
        resize="none"
      />
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button-group>
        <el-button type="primary" @click="encode">编码</el-button>
        <el-button type="success" @click="decode">解码</el-button>
        <el-button type="warning" @click="format">格式化</el-button>
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

    <!-- 字符信息 -->
    <el-card class="char-info-card">
      <template #header>
        <div class="card-header">
          <span>字符信息</span>
        </div>
      </template>
      <el-table :data="charInfo" style="width: 100%">
        <el-table-column prop="char" label="字符" width="100" />
        <el-table-column prop="unicode" label="Unicode" width="120" />
        <el-table-column prop="utf8" label="UTF-8" show-overflow-tooltip />
        <el-table-column prop="utf16" label="UTF-16" show-overflow-tooltip />
        <el-table-column prop="utf32" label="UTF-32" show-overflow-tooltip />
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
        <el-table-column prop="format" label="编码格式" width="100" />
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
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const inputText = ref('')
const outputText = ref('')
const history = ref([])
const charInfo = ref([])

const config = ref({
  format: 'utf8',
  outputFormat: 'hex',
  separator: ' ',
  prefix: '\\u'
})

// 返回主页
const goBack = () => {
  router.push('/bmgf/private')
}

// 清空输入
const clearInput = () => {
  inputText.value = ''
  outputText.value = ''
  charInfo.value = []
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

// Unicode编码
const encode = () => {
  if (!inputText.value) {
    ElMessage.warning('请输入要编码的内容')
    return
  }
  try {
    const result = encodeUnicode(inputText.value)
    outputText.value = result
    updateCharInfo(inputText.value)
    addToHistory('编码')
    ElMessage.success('编码成功')
  } catch (error) {
    ElMessage.error('编码失败：' + error.message)
  }
}

// Unicode解码
const decode = () => {
  if (!inputText.value) {
    ElMessage.warning('请输入要解码的内容')
    return
  }
  try {
    const result = decodeUnicode(inputText.value)
    outputText.value = result
    updateCharInfo(result)
    addToHistory('解码')
    ElMessage.success('解码成功')
  } catch (error) {
    ElMessage.error('解码失败：' + error.message)
  }
}

// 格式化
const format = () => {
  if (!inputText.value) {
    ElMessage.warning('请输入要格式化的内容')
    return
  }
  try {
    const result = formatUnicode(inputText.value)
    outputText.value = result
    addToHistory('格式化')
    ElMessage.success('格式化成功')
  } catch (error) {
    ElMessage.error('格式化失败：' + error.message)
  }
}

// Unicode编码核心算法
const encodeUnicode = (text) => {
  const result = []
  for (let i = 0; i < text.length; i++) {
    const char = text[i]
    const codePoint = char.codePointAt(0)
    
    switch (config.value.format) {
      case 'utf8':
        result.push(encodeUTF8(codePoint))
        break
      case 'utf16':
        result.push(encodeUTF16(codePoint))
        break
      case 'utf32':
        result.push(encodeUTF32(codePoint))
        break
      case 'unicode':
        result.push(encodeUnicodePoint(codePoint))
        break
    }
  }
  return result.join(config.value.separator)
}

// UTF-8编码
const encodeUTF8 = (codePoint) => {
  if (codePoint <= 0x7F) {
    return formatNumber(codePoint, 2)
  } else if (codePoint <= 0x7FF) {
    const byte1 = 0xC0 | (codePoint >> 6)
    const byte2 = 0x80 | (codePoint & 0x3F)
    return formatNumber(byte1, 2) + config.value.separator + formatNumber(byte2, 2)
  } else if (codePoint <= 0xFFFF) {
    const byte1 = 0xE0 | (codePoint >> 12)
    const byte2 = 0x80 | ((codePoint >> 6) & 0x3F)
    const byte3 = 0x80 | (codePoint & 0x3F)
    return formatNumber(byte1, 2) + config.value.separator + 
           formatNumber(byte2, 2) + config.value.separator + 
           formatNumber(byte3, 2)
  } else {
    const byte1 = 0xF0 | (codePoint >> 18)
    const byte2 = 0x80 | ((codePoint >> 12) & 0x3F)
    const byte3 = 0x80 | ((codePoint >> 6) & 0x3F)
    const byte4 = 0x80 | (codePoint & 0x3F)
    return formatNumber(byte1, 2) + config.value.separator + 
           formatNumber(byte2, 2) + config.value.separator + 
           formatNumber(byte3, 2) + config.value.separator + 
           formatNumber(byte4, 2)
  }
}

// UTF-16编码
const encodeUTF16 = (codePoint) => {
  if (codePoint <= 0xFFFF) {
    return formatNumber(codePoint, 4)
  } else {
    const high = ((codePoint - 0x10000) >> 10) + 0xD800
    const low = (codePoint & 0x3FF) + 0xDC00
    return formatNumber(high, 4) + config.value.separator + formatNumber(low, 4)
  }
}

// UTF-32编码
const encodeUTF32 = (codePoint) => {
  return formatNumber(codePoint, 8)
}

// Unicode编码点
const encodeUnicodePoint = (codePoint) => {
  return config.value.prefix + formatNumber(codePoint, 4)
}

// 格式化数字
const formatNumber = (num, length) => {
  switch (config.value.outputFormat) {
    case 'hex':
      return num.toString(16).padStart(length, '0')
    case 'dec':
      return num.toString(10)
    case 'bin':
      return num.toString(2).padStart(length * 4, '0')
  }
}

// Unicode解码核心算法
const decodeUnicode = (text) => {
  const parts = text.split(config.value.separator)
  const result = []
  
  for (let i = 0; i < parts.length; i++) {
    const part = parts[i]
    let codePoint
    
    switch (config.value.format) {
      case 'utf8':
        codePoint = decodeUTF8(parts.slice(i))
        i += getUTF8Length(parseInt(parts[i], 16)) - 1
        break
      case 'utf16':
        if (part.length === 4) {
          codePoint = parseInt(part, 16)
        } else {
          const high = parseInt(parts[i], 16)
          const low = parseInt(parts[i + 1], 16)
          codePoint = ((high - 0xD800) << 10) + (low - 0xDC00) + 0x10000
          i++
        }
        break
      case 'utf32':
        codePoint = parseInt(part, 16)
        break
      case 'unicode':
        codePoint = parseInt(part.replace(config.value.prefix, ''), 16)
        break
    }
    
    result.push(String.fromCodePoint(codePoint))
  }
  
  return result.join('')
}

// UTF-8解码
const decodeUTF8 = (bytes) => {
  const first = parseInt(bytes[0], 16)
  if (first <= 0x7F) {
    return first
  } else if (first <= 0xDF) {
    return ((first & 0x1F) << 6) | (parseInt(bytes[1], 16) & 0x3F)
  } else if (first <= 0xEF) {
    return ((first & 0xF) << 12) | 
           ((parseInt(bytes[1], 16) & 0x3F) << 6) | 
           (parseInt(bytes[2], 16) & 0x3F)
  } else {
    return ((first & 0x7) << 18) | 
           ((parseInt(bytes[1], 16) & 0x3F) << 12) | 
           ((parseInt(bytes[2], 16) & 0x3F) << 6) | 
           (parseInt(bytes[3], 16) & 0x3F)
  }
}

// 获取UTF-8编码长度
const getUTF8Length = (firstByte) => {
  if (firstByte <= 0x7F) return 1
  if (firstByte <= 0xDF) return 2
  if (firstByte <= 0xEF) return 3
  return 4
}

// 格式化Unicode
const formatUnicode = (text) => {
  const result = []
  for (let i = 0; i < text.length; i++) {
    const char = text[i]
    const codePoint = char.codePointAt(0)
    result.push(`U+${codePoint.toString(16).padStart(4, '0')} ${char}`)
  }
  return result.join('\n')
}

// 更新字符信息
const updateCharInfo = (text) => {
  charInfo.value = []
  for (let i = 0; i < text.length; i++) {
    const char = text[i]
    const codePoint = char.codePointAt(0)
    charInfo.value.push({
      char: char,
      unicode: `U+${codePoint.toString(16).padStart(4, '0')}`,
      utf8: encodeUTF8(codePoint),
      utf16: encodeUTF16(codePoint),
      utf32: encodeUTF32(codePoint)
    })
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
  link.download = 'unicode_result.txt'
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
    format: config.value.format,
    type: type,
    time: new Date().toLocaleString()
  })
  // 限制历史记录数量
  if (history.value.length > 10) {
    history.value.pop()
  }
  // 保存到本地存储
  localStorage.setItem('unicodeHistory', JSON.stringify(history.value))
}

// 清空历史记录
const clearHistory = () => {
  history.value = []
  localStorage.removeItem('unicodeHistory')
  ElMessage.success('历史记录已清空')
}

// 使用历史记录项
const useHistoryItem = (item) => {
  inputText.value = item.input
  outputText.value = item.output
  config.value.format = item.format
}

// 删除历史记录项
const deleteHistoryItem = (item) => {
  const index = history.value.indexOf(item)
  if (index > -1) {
    history.value.splice(index, 1)
    localStorage.setItem('unicodeHistory', JSON.stringify(history.value))
    ElMessage.success('已删除历史记录')
  }
}

// 加载历史记录
onMounted(() => {
  const savedHistory = localStorage.getItem('unicodeHistory')
  if (savedHistory) {
    history.value = JSON.parse(savedHistory)
  }
})

// 监听输入变化
watch(inputText, () => {
  if (inputText.value) {
    updateCharInfo(inputText.value)
  }
})
</script>

<style scoped>
.unicode-container {
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
.char-info-card {
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