<template>
  <div class="trend-container">
    <el-card class="trend-card">
      <template #header>
        <div class="card-header">
          <span>网站访问趋势分析</span>
          <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
            <el-radio-button label="week">近一周</el-radio-button>
            <el-radio-button label="month">近一月</el-radio-button>
            <el-radio-button label="year">近一年</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      
      <div class="chart-container">
        <div ref="trafficChart" class="chart"></div>
      </div>
    </el-card>

    <el-card class="trend-card">
      <template #header>
        <div class="card-header">
          <span>用户活跃度分析</span>
          <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
            <el-radio-button label="week">近一周</el-radio-button>
            <el-radio-button label="month">近一月</el-radio-button>
            <el-radio-button label="year">近一年</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      
      <div class="chart-container">
        <div ref="userChart" class="chart"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import ToUrl from '@/api/api'
import store from '@/store'

const trafficChart = ref(null)
const userChart = ref(null)
const timeRange = ref('week')
let trafficChartInstance = null
let userChartInstance = null

// 初始化图表
const initCharts = () => {
  if (trafficChart.value) {
    trafficChartInstance = echarts.init(trafficChart.value)
  }
  if (userChart.value) {
    userChartInstance = echarts.init(userChart.value)
  }
  window.addEventListener('resize', handleResize)
}

// 处理窗口大小变化
const handleResize = () => {
  trafficChartInstance?.resize()
  userChartInstance?.resize()
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const token = store.state.token
    const response = await axios.get(`${ToUrl.url}/api/stats`, {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      params: {
        timeRange: timeRange.value
      }
    })
    const data = response.data
    
    // 生成预测数据
    const predictedData = generatePredictions(data)
    
    // 更新访问量趋势图
    updateTrafficChart(data, predictedData)
    // 更新用户活跃度图
    updateUserChart(data, predictedData)
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 生成预测数据
const generatePredictions = (data) => {
  const lastValue = data.visits || 0
  const lastUserValue = data.users || 0
  
  // 简单的线性预测
  const predictedVisits = []
  const predictedUsers = []
  const dates = []
  
  for (let i = 1; i <= 7; i++) {
    const nextDate = new Date()
    nextDate.setDate(nextDate.getDate() + i)
    dates.push(nextDate.toLocaleDateString())
    
    // 基于历史数据的简单预测
    predictedVisits.push(Math.round(lastValue * (1 + 0.1 * i)))
    predictedUsers.push(Math.round(lastUserValue * (1 + 0.05 * i)))
  }
  
  return {
    dates,
    predictedVisits,
    predictedUsers
  }
}

// 更新访问量趋势图
const updateTrafficChart = (data, predictedData) => {
  const option = {
    title: {
      text: '网站访问量趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>{a}: {c} 次'
    },
    legend: {
      data: ['实际访问量', '预测访问量'],
      bottom: 0
    },
    xAxis: {
      type: 'category',
      data: [...data.dates || [], ...predictedData.dates]
    },
    yAxis: {
      type: 'value',
      name: '访问量'
    },
    series: [
      {
        name: '实际访问量',
        type: 'line',
        data: [...(data.visitsHistory || []), null],
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '预测访问量',
        type: 'line',
        data: [null, ...predictedData.predictedVisits],
        smooth: true,
        lineStyle: {
          type: 'dashed'
        },
        itemStyle: {
          color: '#67C23A'
        }
      }
    ]
  }
  trafficChartInstance?.setOption(option)
}

// 更新用户活跃度图
const updateUserChart = (data, predictedData) => {
  const option = {
    title: {
      text: '用户活跃度趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>{a}: {c} 人'
    },
    legend: {
      data: ['实际用户数', '预测用户数'],
      bottom: 0
    },
    xAxis: {
      type: 'category',
      data: [...data.dates || [], ...predictedData.dates]
    },
    yAxis: {
      type: 'value',
      name: '用户数'
    },
    series: [
      {
        name: '实际用户数',
        type: 'line',
        data: [...(data.usersHistory || []), null],
        smooth: true,
        itemStyle: {
          color: '#E6A23C'
        }
      },
      {
        name: '预测用户数',
        type: 'line',
        data: [null, ...predictedData.predictedUsers],
        smooth: true,
        lineStyle: {
          type: 'dashed'
        },
        itemStyle: {
          color: '#F56C6C'
        }
      }
    ]
  }
  userChartInstance?.setOption(option)
}

// 处理时间范围变化
const handleTimeRangeChange = () => {
  fetchStats()
}

onMounted(() => {
  initCharts()
  fetchStats()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trafficChartInstance?.dispose()
  userChartInstance?.dispose()
})
</script>

<style scoped>
.trend-container {
  padding: 20px;
}

.trend-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 400px;
  width: 100%;
}

.chart {
  height: 100%;
  width: 100%;
}
</style> 