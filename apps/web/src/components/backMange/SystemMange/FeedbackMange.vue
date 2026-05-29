<template>
  <div class="feedback-container">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card v-for="(stat, key) in feedbackStats" :key="key" class="stat-card" shadow="hover">
        <template #header>
          <div class="stat-header">
            <el-icon :size="24" :color="getStatusColor(key)">
              <component :is="getStatusIcon(key)" />
            </el-icon>
            <span class="stat-title">{{ getStatusText(key) }}</span>
          </div>
        </template>
        <div class="stat-value">{{ stat }}</div>
      </el-card>
    </div>

    <!-- 筛选和操作栏 -->
    <div class="filter-bar">
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleStatusChange">
        <el-option label="全部" value="" />
        <el-option label="待处理" value="PENDING" />
        <el-option label="处理中" value="PROCESSING" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已拒绝" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 反馈列表 -->
    <el-card class="feedback-list" shadow="hover">
      <el-table :data="feedbacks" style="width: 100%" v-loading="loading" @row-click="handleRowClick">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button 
                type="primary" 
                size="small" 
                @click.stop="handleStatusUpdate(row)"
                :disabled="row.status === 'COMPLETED'"
              >
                更新状态
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click.stop="handleDelete(row)"
              >
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 状态更新对话框 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="更新反馈状态"
      width="30%"
    >
      <el-form :model="statusForm" label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="statusForm.status" placeholder="请选择状态">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStatusUpdate">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="反馈详情"
      width="50%"
    >
      <div v-if="currentFeedback" class="feedback-detail">
        <div class="detail-item">
          <span class="label">ID：</span>
          <span class="value">{{ currentFeedback.id }}</span>
        </div>
        <div class="detail-item">
          <span class="label">标题：</span>
          <span class="value">{{ currentFeedback.title }}</span>
        </div>
        <div class="detail-item">
          <span class="label">内容：</span>
          <div class="value content">{{ currentFeedback.content }}</div>
        </div>
        <div class="detail-item">
          <span class="label">状态：</span>
          <el-tag :type="getStatusType(currentFeedback.status)">
            {{ getStatusText(currentFeedback.status) }}
          </el-tag>
        </div>
        <div class="detail-item">
          <span class="label">创建时间：</span>
          <span class="value">{{ formatDate(currentFeedback.createTime) }}</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="handleStatusUpdate(currentFeedback)"
            :disabled="currentFeedback?.status === 'COMPLETED'"
          >
            更新状态
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Warning, Clock, Check, Close } from '@element-plus/icons-vue'
import axios from '@/axios'
import ToUrl from '@/api/api'

// 数据状态
const feedbacks = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')
const feedbackStats = ref({})
const statusDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentFeedback = ref(null)
const statusForm = ref({
  id: '',
  status: ''
})

// 获取反馈列表
const fetchFeedbacks = async () => {
  loading.value = true
  try {
    const url = statusFilter.value
      ? `/api/admin/feedback/status/${statusFilter.value}`
      : '/api/admin/feedback'
    
    const response = await axios.get(url, {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    
    feedbacks.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取反馈列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取反馈统计
const fetchFeedbackStats = async () => {
  try {
    const response = await axios.get('/api/admin/feedback/stats')
    feedbackStats.value = response.data
  } catch (error) {
    ElMessage.error('获取反馈统计失败')
    console.error(error)
  }
}

// 更新反馈状态
const handleStatusUpdate = (row) => {
  statusForm.value = {
    id: row.id,
    status: row.status
  }
  statusDialogVisible.value = true
}

const submitStatusUpdate = async () => {
  try {
    await axios.put(`/api/admin/feedback/${statusForm.value.id}/status`, null, {
      params: {
        status: statusForm.value.status
      }
    })
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    refreshData()
  } catch (error) {
    ElMessage.error('状态更新失败')
    console.error(error)
  }
}

// 删除反馈
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除这条反馈吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await axios.delete(`/api/admin/feedback/${row.id}`)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  })
}

// 刷新数据
const refreshData = () => {
  fetchFeedbacks()
  fetchFeedbackStats()
}

// 状态筛选变化
const handleStatusChange = () => {
  currentPage.value = 1
  fetchFeedbacks()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchFeedbacks()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchFeedbacks()
}

// 处理行点击
const handleRowClick = (row) => {
  currentFeedback.value = row
  detailDialogVisible.value = true
}

// 工具函数
const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'PROCESSING': 'primary',
    'COMPLETED': 'success',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'REJECTED': '已拒绝'
  }
  return texts[status] || status
}

const getStatusColor = (status) => {
  const colors = {
    'PENDING': '#E6A23C',
    'PROCESSING': '#409EFF',
    'COMPLETED': '#67C23A',
    'REJECTED': '#F56C6C'
  }
  return colors[status] || '#909399'
}

const getStatusIcon = (status) => {
  const icons = {
    'PENDING': Warning,
    'PROCESSING': Clock,
    'COMPLETED': Check,
    'REJECTED': Close
  }
  return icons[status] || Warning
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 初始化
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.feedback-container {
  padding: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-title {
  font-size: 16px;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  text-align: center;
  margin-top: 10px;
}

.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.feedback-list {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-card__header) {
  padding: 15px;
  border-bottom: 1px solid #EBEEF5;
}

:deep(.el-table) {
  margin-top: 10px;
}

:deep(.el-tag) {
  text-align: center;
  min-width: 80px;
}

.feedback-detail {
  padding: 20px;
}

.detail-item {
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
}

.detail-item .label {
  font-weight: bold;
  min-width: 80px;
  color: #606266;
}

.detail-item .value {
  flex: 1;
  color: #303133;
}

.detail-item .content {
  white-space: pre-wrap;
  line-height: 1.6;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-top: 5px;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style> 