<template>
  <div class="cms-container">
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
    <h1 class="title">CMS指纹识别工具</h1>

    <!-- 功能介绍 -->
    <el-card class="intro-card">
      <template #header>
        <div class="card-header">
          <span>功能介绍</span>
        </div>
      </template>
      <p class="intro-text">
        CMS指纹识别工具可以帮助您快速识别网站使用的CMS系统。
        通过分析网站的特定特征，如文件路径、响应头、HTML特征等，准确识别出网站使用的CMS类型和版本。
      </p>
    </el-card>

    <!-- 配置区域 -->
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>扫描配置</span>
        </div>
      </template>
      <div class="config-content">
        <el-form :model="config" label-width="100px">
          <el-form-item label="扫描方式">
            <el-radio-group v-model="config.scanType">
              <el-radio label="quick">快速扫描</el-radio>
              <el-radio label="deep">深度扫描</el-radio>
              <el-radio label="custom">自定义扫描</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="超时时间">
            <el-input-number v-model="config.timeout" :min="1" :max="30" />
            <span class="unit">秒</span>
          </el-form-item>
          <el-form-item label="并发数">
            <el-input-number v-model="config.concurrent" :min="1" :max="10" />
          </el-form-item>
          <el-form-item label="扫描特征">
            <el-checkbox-group v-model="config.features">
              <el-checkbox label="headers">响应头</el-checkbox>
              <el-checkbox label="html">HTML特征</el-checkbox>
              <el-checkbox label="files">文件路径</el-checkbox>
              <el-checkbox label="cookies">Cookies</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 输入区域 -->
    <el-card class="input-card">
      <template #header>
        <div class="card-header">
          <span>目标网站</span>
          <div class="header-actions">
            <el-button type="primary" @click="clearInput">清空</el-button>
            <el-button type="success" @click="importTargets">导入</el-button>
          </div>
        </div>
      </template>
      <el-input
        v-model="targetUrl"
        type="textarea"
        :rows="4"
        placeholder="请输入目标网站URL，多个URL请换行输入..."
        resize="none"
      />
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button-group>
        <el-button type="primary" @click="startScan" :loading="scanning">
          <el-icon><Search /></el-icon>
          开始扫描
        </el-button>
        <el-button type="warning" @click="stopScan" :disabled="!scanning">
          <el-icon><Close /></el-icon>
          停止扫描
        </el-button>
        <el-button type="success" @click="exportResults">
          <el-icon><Download /></el-icon>
          导出结果
        </el-button>
      </el-button-group>
    </div>

    <!-- 扫描结果 -->
    <el-card class="result-card">
      <template #header>
        <div class="card-header">
          <span>扫描结果</span>
          <div class="header-actions">
            <el-button type="primary" @click="clearResults">清空结果</el-button>
          </div>
        </div>
      </template>
      <el-table :data="scanResults" style="width: 100%" v-loading="scanning">
        <el-table-column prop="url" label="目标网站" show-overflow-tooltip />
        <el-table-column prop="cms" label="CMS类型" width="150" />
        <el-table-column prop="version" label="版本" width="120" />
        <el-table-column prop="confidence" label="可信度" width="100">
          <template #default="{ row }">
            <el-tag :type="getConfidenceType(row.confidence)">
              {{ row.confidence }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="features" label="匹配特征" show-overflow-tooltip />
        <el-table-column prop="scanTime" label="扫描时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="text" @click="viewDetails(row)">详情</el-button>
            <el-button type="text" @click="deleteResult(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 统计信息 -->
    <el-card class="stats-card">
      <template #header>
        <div class="card-header">
          <span>统计信息</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="6" v-for="stat in statistics" :key="stat.label">
          <div class="stat-item">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-col>
      </el-row>
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
        <el-table-column prop="url" label="目标网站" show-overflow-tooltip />
        <el-table-column prop="cms" label="CMS类型" width="150" />
        <el-table-column prop="scanTime" label="扫描时间" width="180" />
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Search, Close, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const targetUrl = ref('')
const scanning = ref(false)
const scanResults = ref([])
const history = ref([])

const config = ref({
  scanType: 'quick',
  timeout: 10,
  concurrent: 3,
  features: ['headers', 'html', 'files']
})

// 返回主页
const goBack = () => {
  router.push('/bmgf/private')
}

// 清空输入
const clearInput = () => {
  targetUrl.value = ''
}

// 导入目标
const importTargets = () => {
  // 实现导入功能
  ElMessage.info('导入功能即将推出')
}

// 开始扫描
const startScan = () => {
  if (!targetUrl.value.trim()) {
    ElMessage.warning('请输入目标网站URL')
    return
  }
  scanning.value = true
  // 模拟扫描过程
  setTimeout(() => {
    const urls = targetUrl.value.split('\n').filter(url => url.trim())
    urls.forEach(url => {
      const result = {
        url: url.trim(),
        cms: 'WordPress',
        version: '5.8.2',
        confidence: Math.floor(Math.random() * 30) + 70,
        features: 'wp-content, wp-includes, wp-login.php',
        scanTime: new Date().toLocaleString()
      }
      scanResults.value.push(result)
      addToHistory(result)
    })
    scanning.value = false
    ElMessage.success('扫描完成')
  }, 2000)
}

// 停止扫描
const stopScan = () => {
  scanning.value = false
  ElMessage.info('扫描已停止')
}

// 导出结果
const exportResults = () => {
  if (scanResults.value.length === 0) {
    ElMessage.warning('没有可导出的结果')
    return
  }
  const blob = new Blob([JSON.stringify(scanResults.value, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'cms_scan_results.json'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('结果已导出')
}

// 清空结果
const clearResults = () => {
  scanResults.value = []
  ElMessage.success('结果已清空')
}

// 获取可信度标签类型
const getConfidenceType = (confidence) => {
  if (confidence >= 90) return 'success'
  if (confidence >= 70) return 'warning'
  return 'danger'
}

// 查看详情
const viewDetails = (row) => {
  // 实现详情查看功能
  ElMessage.info('详情功能即将推出')
}

// 删除结果
const deleteResult = (row) => {
  const index = scanResults.value.indexOf(row)
  if (index > -1) {
    scanResults.value.splice(index, 1)
    ElMessage.success('已删除结果')
  }
}

// 添加历史记录
const addToHistory = (result) => {
  history.value.unshift({
    url: result.url,
    cms: result.cms,
    scanTime: result.scanTime
  })
  // 限制历史记录数量
  if (history.value.length > 10) {
    history.value.pop()
  }
  // 保存到本地存储
  localStorage.setItem('cmsHistory', JSON.stringify(history.value))
}

// 清空历史记录
const clearHistory = () => {
  history.value = []
  localStorage.removeItem('cmsHistory')
  ElMessage.success('历史记录已清空')
}

// 使用历史记录项
const useHistoryItem = (item) => {
  targetUrl.value = item.url
}

// 删除历史记录项
const deleteHistoryItem = (item) => {
  const index = history.value.indexOf(item)
  if (index > -1) {
    history.value.splice(index, 1)
    localStorage.setItem('cmsHistory', JSON.stringify(history.value))
    ElMessage.success('已删除历史记录')
  }
}

// 统计信息
const statistics = computed(() => [
  { label: '已扫描网站', value: scanResults.value.length },
  { label: 'WordPress', value: scanResults.value.filter(r => r.cms === 'WordPress').length },
  { label: 'Joomla', value: scanResults.value.filter(r => r.cms === 'Joomla').length },
  { label: 'Drupal', value: scanResults.value.filter(r => r.cms === 'Drupal').length }
])

// 加载历史记录
onMounted(() => {
  const savedHistory = localStorage.getItem('cmsHistory')
  if (savedHistory) {
    history.value = JSON.parse(savedHistory)
  }
})
</script>

<style scoped>
.cms-container {
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

.unit {
  margin-left: 10px;
  color: #909399;
}

.input-card,
.result-card,
.stats-card {
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

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
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
