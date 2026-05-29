<template>
  <div class="subdomain-container">
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
    <h1 class="title">子域名查询工具</h1>

    <!-- 功能介绍 -->
    <el-card class="intro-card">
      <template #header>
        <div class="card-header">
          <span>功能介绍</span>
        </div>
      </template>
      <p class="intro-text">
        子域名查询工具可以帮助您快速发现目标域名的所有子域名。
        通过多种查询方式，包括DNS查询、搜索引擎、证书查询等，全面收集子域名信息。
      </p>
    </el-card>

    <!-- 配置区域 -->
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span>查询配置</span>
        </div>
      </template>
      <div class="config-content">
        <el-form :model="config" label-width="100px">
          <el-form-item label="查询方式">
            <el-checkbox-group v-model="config.methods">
              <el-checkbox label="dns">DNS查询</el-checkbox>
              <el-checkbox label="search">搜索引擎</el-checkbox>
              <el-checkbox label="cert">证书查询</el-checkbox>
              <el-checkbox label="brute">暴力破解</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="超时时间">
            <el-input-number v-model="config.timeout" :min="1" :max="30" />
            <span class="unit">秒</span>
          </el-form-item>
          <el-form-item label="并发数">
            <el-input-number v-model="config.concurrent" :min="1" :max="10" />
          </el-form-item>
          <el-form-item label="字典文件">
            <el-upload
              class="upload-demo"
              action="#"
              :auto-upload="false"
              :on-change="handleFileChange"
              :limit="1"
            >
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持txt格式的字典文件，每行一个子域名
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 输入区域 -->
    <el-card class="input-card">
      <template #header>
        <div class="card-header">
          <span>目标域名</span>
          <div class="header-actions">
            <el-button type="primary" @click="clearInput">清空</el-button>
            <el-button type="success" @click="importTargets">导入</el-button>
          </div>
        </div>
      </template>
      <el-input
        v-model="targetDomain"
        type="textarea"
        :rows="4"
        placeholder="请输入目标域名，多个域名请换行输入..."
        resize="none"
      />
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button-group>
        <el-button type="primary" @click="startQuery" :loading="querying">
          <el-icon><Search /></el-icon>
          开始查询
        </el-button>
        <el-button type="warning" @click="stopQuery" :disabled="!querying">
          <el-icon><Close /></el-icon>
          停止查询
        </el-button>
        <el-button type="success" @click="exportResults">
          <el-icon><Download /></el-icon>
          导出结果
        </el-button>
      </el-button-group>
    </div>

    <!-- 查询结果 -->
    <el-card class="result-card">
      <template #header>
        <div class="card-header">
          <span>查询结果</span>
          <div class="header-actions">
            <el-button type="primary" @click="clearResults">清空结果</el-button>
          </div>
        </div>
      </template>
      <el-table :data="queryResults" style="width: 100%" v-loading="querying">
        <el-table-column prop="subdomain" label="子域名" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="网站标题" show-overflow-tooltip />
        <el-table-column prop="server" label="服务器" width="150" />
        <el-table-column prop="queryTime" label="查询时间" width="180" />
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
        <el-table-column prop="domain" label="目标域名" show-overflow-tooltip />
        <el-table-column prop="subdomainCount" label="子域名数量" width="120" />
        <el-table-column prop="queryTime" label="查询时间" width="180" />
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
const targetDomain = ref('')
const querying = ref(false)
const queryResults = ref([])
const history = ref([])

const config = ref({
  methods: ['dns', 'search', 'cert'],
  timeout: 10,
  concurrent: 3,
  dictionary: null
})

// 返回主页
const goBack = () => {
  router.push('/bmgf/private')
}

// 清空输入
const clearInput = () => {
  targetDomain.value = ''
}

// 导入目标
const importTargets = () => {
  // 实现导入功能
  ElMessage.info('导入功能即将推出')
}

// 处理文件上传
const handleFileChange = (file) => {
  if (file) {
    config.value.dictionary = file.raw
    ElMessage.success('字典文件已上传')
  }
}

// 开始查询
const startQuery = () => {
  if (!targetDomain.value.trim()) {
    ElMessage.warning('请输入目标域名')
    return
  }
  querying.value = true
  // 模拟查询过程
  setTimeout(() => {
    const domains = targetDomain.value.split('\n').filter(domain => domain.trim())
    domains.forEach(domain => {
      const result = {
        subdomain: `test.${domain.trim()}`,
        ip: '192.168.1.1',
        status: '200',
        title: 'Test Website',
        server: 'nginx/1.18.0',
        queryTime: new Date().toLocaleString()
      }
      queryResults.value.push(result)
      addToHistory(domain.trim())
    })
    querying.value = false
    ElMessage.success('查询完成')
  }, 2000)
}

// 停止查询
const stopQuery = () => {
  querying.value = false
  ElMessage.info('查询已停止')
}

// 导出结果
const exportResults = () => {
  if (queryResults.value.length === 0) {
    ElMessage.warning('没有可导出的结果')
    return
  }
  const blob = new Blob([JSON.stringify(queryResults.value, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'subdomain_query_results.json'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('结果已导出')
}

// 清空结果
const clearResults = () => {
  queryResults.value = []
  ElMessage.success('结果已清空')
}

// 获取状态标签类型
const getStatusType = (status) => {
  if (status === '200') return 'success'
  if (status === '404') return 'danger'
  return 'warning'
}

// 查看详情
const viewDetails = (row) => {
  // 实现详情查看功能
  ElMessage.info('详情功能即将推出')
}

// 删除结果
const deleteResult = (row) => {
  const index = queryResults.value.indexOf(row)
  if (index > -1) {
    queryResults.value.splice(index, 1)
    ElMessage.success('已删除结果')
  }
}

// 添加历史记录
const addToHistory = (domain) => {
  history.value.unshift({
    domain: domain,
    subdomainCount: queryResults.value.filter(r => r.subdomain.endsWith(domain)).length,
    queryTime: new Date().toLocaleString()
  })
  // 限制历史记录数量
  if (history.value.length > 10) {
    history.value.pop()
  }
  // 保存到本地存储
  localStorage.setItem('subdomainHistory', JSON.stringify(history.value))
}

// 清空历史记录
const clearHistory = () => {
  history.value = []
  localStorage.removeItem('subdomainHistory')
  ElMessage.success('历史记录已清空')
}

// 使用历史记录项
const useHistoryItem = (item) => {
  targetDomain.value = item.domain
}

// 删除历史记录项
const deleteHistoryItem = (item) => {
  const index = history.value.indexOf(item)
  if (index > -1) {
    history.value.splice(index, 1)
    localStorage.setItem('subdomainHistory', JSON.stringify(history.value))
    ElMessage.success('已删除历史记录')
  }
}

// 统计信息
const statistics = computed(() => [
  { label: '已查询域名', value: queryResults.value.length },
  { label: '活跃子域名', value: queryResults.value.filter(r => r.status === '200').length },
  { label: '未知子域名', value: queryResults.value.filter(r => r.status === '404').length },
  { label: '总子域名', value: queryResults.value.length }
])

// 加载历史记录
onMounted(() => {
  const savedHistory = localStorage.getItem('subdomainHistory')
  if (savedHistory) {
    history.value = JSON.parse(savedHistory)
  }
})
</script>

<style scoped>
.subdomain-container {
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

:deep(.el-upload__tip) {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style> 