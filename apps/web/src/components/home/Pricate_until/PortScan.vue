<template>
  <div class="port-scan-container">
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
    <h1 class="title">端口扫描工具</h1>

    <!-- 扫描配置区域 -->
    <el-card class="scan-config">
      <template #header>
        <div class="card-header">
          <span>扫描配置</span>
        </div>
      </template>
      <el-form :model="scanForm" label-width="120px">
        <el-form-item label="目标地址">
          <el-input v-model="scanForm.target" placeholder="请输入IP地址或域名" />
        </el-form-item>
        <el-form-item label="端口范围">
          <el-input-number v-model="scanForm.startPort" :min="1" :max="65535" />
          <span class="port-range-separator">-</span>
          <el-input-number v-model="scanForm.endPort" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="扫描类型">
          <el-radio-group v-model="scanForm.scanType">
            <el-radio label="quick">快速扫描</el-radio>
            <el-radio label="full">完整扫描</el-radio>
            <el-radio label="custom">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="超时时间(ms)">
          <el-input-number v-model="scanForm.timeout" :min="100" :max="5000" :step="100" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="startScan" :loading="scanning">
            {{ scanning ? '扫描中...' : '开始扫描' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 扫描结果区域 -->
    <el-card class="scan-results" v-if="scanResults.length > 0">
      <template #header>
        <div class="card-header">
          <span>扫描结果</span>
          <div class="header-actions">
            <el-switch
              v-model="showAllPorts"
              active-text="显示所有端口"
              inactive-text="仅显示开放端口"
              style="margin-right: 16px"
            />
            <el-button type="primary" @click="exportResults">导出结果</el-button>
          </div>
        </div>
      </template>
      <el-table :data="filteredResults" style="width: 100%">
        <el-table-column prop="port" label="端口" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '开放' ? 'success' : 'danger'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="service" label="服务" />
        <el-table-column prop="banner" label="Banner信息" />
        <el-table-column prop="responseTime" label="响应时间(ms)" width="120" />
      </el-table>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row" v-if="scanResults.length > 0">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">总扫描端口</div>
          <div class="stat-value">{{ totalPorts }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">开放端口</div>
          <div class="stat-value">{{ openPorts }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">扫描耗时</div>
          <div class="stat-value">{{ scanTime }}s</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-title">平均响应时间</div>
          <div class="stat-value">{{ avgResponseTime }}ms</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import ToUrl from '@/api/api.ts'
import store from '@/store'

const router = useRouter()
const scanning = ref(false)
const scanTime = ref(0)
const avgResponseTime = ref(0)
const showAllPorts = ref(false)

const scanForm = ref({
  target: '',
  startPort: 1,
  endPort: 1024,
  scanType: 'quick',
  timeout: 1000
})

const scanResults = ref([])

const totalPorts = computed(() => scanResults.value.length)
const openPorts = computed(() => scanResults.value.filter(r => r.status === '开放').length)

const filteredResults = computed(() => {
  if (showAllPorts.value) {
    return scanResults.value
  }
  return scanResults.value.filter(r => r.status === '开放')
})

const goBack = () => {
  router.push('/bmgf/private')
}

const resetForm = () => {
  scanForm.value = {
    target: '',
    startPort: 1,
    endPort: 1024,
    scanType: 'quick',
    timeout: 1000
  }
  scanResults.value = []
}

const startScan = async () => {
  if (!scanForm.value.target) {
    ElMessage.warning('请输入目标地址')
    return
  }

  scanning.value = true
  try {
    const response = await axios.post(ToUrl.url + '/api/portscan/scan', scanForm.value, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    
    const data = response.data
    scanResults.value = data.results
    scanTime.value = (data.scanTime / 1000).toFixed(2)
    avgResponseTime.value = data.avgResponseTime
    
    ElMessage.success('扫描完成')
  } catch (error) {
    console.error('扫描失败:', error)
    ElMessage.error('扫描失败：' + (error.response?.data?.message || error.message))
  } finally {
    scanning.value = false
  }
}

const exportResults = () => {
  const data = scanResults.value.map(r => ({
    端口: r.port,
    状态: r.status,
    服务: r.service,
    Banner信息: r.banner,
    响应时间: r.responseTime + 'ms'
  }))
  
  const csv = 'data:text/csv;charset=utf-8,' + 
    Object.keys(data[0]).join(',') + '\n' +
    data.map(row => Object.values(row).join(',')).join('\n')
  
  const encodedUri = encodeURI(csv)
  const link = document.createElement('a')
  link.setAttribute('href', encodedUri)
  link.setAttribute('download', '端口扫描结果.csv')
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
</script>

<style scoped>
.port-scan-container {
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

.scan-config {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.port-range-separator {
  margin: 0 10px;
  color: #606266;
}

.scan-results {
  margin-bottom: 30px;
}

.stats-row {
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

:deep(.el-card__header) {
  background-color: #f5f7fa;
  padding: 15px 20px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-input-number) {
  width: 120px;
}

.header-actions {
  display: flex;
  align-items: center;
}
</style> 