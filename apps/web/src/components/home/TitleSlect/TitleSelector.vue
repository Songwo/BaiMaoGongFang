<template>
  <el-card class="title-card glass-effect">
    <template #header>
      <div class="card-header">
        <el-icon><Trophy /></el-icon>
        <span>用户称号</span>
      </div>
    </template>

    <div class="title-content">
      <div class="current-title">
        <span class="label">当前称号：</span>
        <el-tag :type="currentTitle.type" effect="dark" class="title-tag">
          {{ currentTitle.name }}
        </el-tag>
      </div>

      <div class="title-list">
        <div class="title-list-header">
          <span>可选称号</span>
          <el-button type="primary" size="small" @click="refreshTitles">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
        <div class="title-grid">
          <div 
            v-for="title in availableTitles" 
            :key="title.id"
            class="title-item"
            :class="{ 'selected': title.id === currentTitle.id }"
            @click="selectTitle(title)"
          >
            <el-tag :type="title.type" effect="dark" class="title-tag">
              {{ title.name }}
            </el-tag>
            <div class="title-info">
              <span class="title-desc">{{ title.description }}</span>
              <span class="title-requirement">要求：{{ title.requirement }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { Trophy, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import ToUrl from '@/api/api'
import store from '@/store'

// 接收父组件的props
const props = defineProps({
  honoraryTitle: {
    type: Object,
    required: true
  }
})

// 可选称号列表
const availableTitles = computed(() => {
  const typeMap = {
    '安全新秀': 'success',
    '漏洞侦探': 'warning',
    '攻防入门者': 'danger',
    '中级分析师': 'info',
    '加密解码手': 'success',
    '脚本小能手': 'warning',
    '红队先锋': 'danger',
    '蓝队守护者': 'info',
    '逆向专家': 'success',
    '网络安全大师': 'danger'
  }
  const descMap = {
    '安全新秀': '初识网络安全，掌握基础概念与工具',
    '漏洞侦探': '能发现常见漏洞，如 XSS、SQL 注入等',
    '攻防入门者': '完成基础靶场训练，对攻防有初步理解',
    '中级分析师': '掌握常见漏洞利用与溯源分析能力',
    '加密解码手': '能够处理常见的对称与非对称加密破解',
    '脚本小能手': '具备一定的 Python/JS 脚本编写能力用于安全测试',
    '红队先锋': '具备红队渗透测试经验，能模拟真实攻击',
    '蓝队守护者': '掌握防御策略、日志审计和威胁检测能力',
    '逆向专家': '熟练进行二进制分析与恶意代码逆向',
    '网络安全大师': '具备全面网络安全能力，善于攻防对抗与体系建设'
  }
  const reqMap = {
    '安全新秀': '100积分',
    '漏洞侦探': '120积分',
    '攻防入门者': '80积分',
    '中级分析师': '200积分',
    '加密解码手': '220积分',
    '脚本小能手': '240积分',
    '红队先锋': '140积分',
    '蓝队守护者': '160积分',
    '逆向专家': '200积分',
    '网络安全大师': '80积分'
  }
  const honoraryTitle = props.honoraryTitle || {}
  return Object.keys(honoraryTitle).map(key => ({
    id: key,
    name: key,
    type: typeMap[key] || 'info',
    description: descMap[key] || '',
    requirement: reqMap[key] || ''
  }))
})

// 当前称号
const currentTitle = computed(() => {
  const honoraryTitle = props.honoraryTitle || {}
  const selectedTitle = Object.entries(honoraryTitle).find(([_, value]) => value === 1)
  
  if (selectedTitle) {
    const title = availableTitles.value.find(t => t.id === selectedTitle[0])
    if (title) {
      return title
    }
  }
  return {
    id: '',
    name: '未设置',
    type: 'info',
    description: '',
    requirement: ''
  }
})

// 选择称号
const selectTitle = async (title) => {
  try {
    // console.log('Selecting title:', title)
    const response = await axios.get(`${ToUrl.url}/user/selectHonoraryTitle`, {
      params: { HonoraryTitle: title.id },
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    
    if (response.data.code === 200) {
      // 更新本地状态
      const honoraryTitle = { ...props.honoraryTitle }
      Object.keys(honoraryTitle).forEach(key => {
        honoraryTitle[key] = key === title.id ? 1 : 0
      })
      // console.log('Updated honoraryTitle:', honoraryTitle)
      // 触发父组件更新
      emit('update:honoraryTitle', honoraryTitle)
      ElMessage.success('称号设置成功')
    } else {
      ElMessage.error(response.data.message || '设置称号失败')
    }
  } catch (error) {
    // console.error('Error selecting title:', error)
    ElMessage.error('设置称号失败')
  }
}

// 定义 emit
const emit = defineEmits(['update:honoraryTitle'])

// 刷新
const refreshTitles = () => {
  // No need to update currentTitleDisplay as it's handled by computed
}
</script>

<style scoped>
.title-card {
  margin-bottom: 20px;
  border-radius: 15px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.title-content {
  padding: 20px;
}

.current-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

.label {
  color: #fff;
  font-weight: 500;
}

.title-list {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 15px;
}

.title-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  color: #fff;
  font-weight: 500;
}

.title-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.title-item {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.title-item:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.15);
}

.title-item.selected {
  background: rgba(64, 158, 255, 0.2);
  border: 1px solid rgba(64, 158, 255, 0.5);
}

.title-tag {
  margin-bottom: 10px;
}

.title-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.title-desc {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.title-requirement {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

:deep(.el-button) {
  color: #fff;
}

@media (max-width: 768px) {
  .title-grid {
    grid-template-columns: 1fr;
  }
}
</style> 