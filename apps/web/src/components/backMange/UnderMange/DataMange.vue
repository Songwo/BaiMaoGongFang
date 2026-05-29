<template>
    <div class="data-statistics">
      <el-row :gutter="20">
        <!-- 柱状图 -->
        <el-col :span="12">
          <el-card>
            <div ref="scoreChart" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div ref="accuracyChart" style="height: 300px"></div>
          </el-card>
        </el-col>
  
        <!-- 饼图 -->
        <el-col :span="12">
          <el-card>
            <div ref="videoPieChart" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div ref="postPieChart" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div ref="commentChart" style="height: 300px"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import * as echarts from 'echarts'
  import axios from 'axios'
  import store from '@/store'
  import { ElMessage } from 'element-plus'
  import ToUrl from '@/api/api'
  
  // 图表引用
  const scoreChart = ref(null)
  const accuracyChart = ref(null)
  const videoPieChart = ref(null)
  const postPieChart = ref(null)
  const commentChart = ref(null)
  
  // 数据存储
  const videoCategories = ref([])
  const postData = ref([])
  const commentData = ref([])
  const questionData=ref([])
  const scoreData = ref([])
  const accuracyData = ref([])
  
  // API 请求
  const loadChartData = async () => {
    try {
      const headers = { 
        Authorization: `Bearer ${store.state.token}`,
        'Content-Type': 'application/json'
      }
  
      // 并行请求所有数据
      const [
        videoRes,
        postRes,
        questionRes,
        scoreRes,
        accuracyRes
      ] = await Promise.all([
        axios.get(ToUrl.url+'/admin/VideoCount', { headers }),
        axios.get(ToUrl.url+'/admin/PostCount', { headers }),
        axios.get(ToUrl.url+'/admin/questionCount', { headers }),
        axios.get(ToUrl.url+'/admin/findAllTotalScore', { headers }),
        axios.get(ToUrl.url+'/admin/score', { headers })
      ])
  
      // 处理响应数据
      videoCategories.value = videoRes.data?.data || []
      postData.value = postRes.data?.data || []
      questionData.value = questionRes.data?.data || []
      scoreData.value = scoreRes.data?.data || []
      accuracyData.value = accuracyRes?.data || []
      // console.log(accuracyChart.value);
      // 确保DOM更新后初始化图表
      setTimeout(initCharts, 100)
    } catch (error) {
      console.error("数据加载失败:", error)
      ElMessage.error('数据加载失败，请检查网络连接')
    }
  }
  
  // 初始化图表
  const initCharts = () => {
    try {
      // 分数柱状图
      if (scoreChart.value && scoreData.value.length) {
        const scoreInstance = echarts.init(scoreChart.value)
        scoreInstance.setOption({
          title: { text: '用户分数分布' },
          tooltip: { trigger: 'axis' },
          xAxis: { 
            type: 'category',
            data: scoreData.value.map(user => user.username),
            axisLabel: { rotate: 45 }
          },
          yAxis: { type: 'value' },
          series: [{
            name: '分数',
            type: 'bar',
            data: scoreData.value.map(user => user.totalScore),
            itemStyle: { color: '#5470c6' }
          }]
        })
      }
  
      // 正确率柱状图
      if (accuracyChart.value && accuracyData.value.length) {
        const accuracyInstance = echarts.init(accuracyChart.value)
        accuracyInstance.setOption({
          title: { text: '答题正确率' },
          tooltip: { trigger: 'axis' },
          xAxis: {
            type: 'category',
            data: accuracyData.value.map(item => item.username),
            axisLabel: { rotate: 45 }
          },
          yAxis: { 
            type: 'value',
            max: 100,
            axisLabel: { formatter: '{value}%' }
          },
          series: [{
            name: '正确率',
            type: 'bar',
            data: accuracyData.value.map(item => item.accuracy*100),
            itemStyle: { color: '#91cc75' }
          }]
        })
      }
    // 处理数据，将其转换为饼图所需的格式
    const transformedVideoData = videoCategories.value.map(item => ({
        name: item.categories,
        value: item.count
    }))
    const transformedPostData = postData.value.map(item => ({
        name: item.section,
        value: item.count
    }))
    const transformedQuestionData = questionData.value.map(item => ({
        name: item.category,
        value: item.count
    }))

// 初始化饼图的函数
const initPieChart = (dom, data, title) => {
    if (!dom || !data.length) return
    
    const chart = echarts.init(dom)
    chart.setOption({
        title: { text: title, left: 'center' },
        tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        series: [{
            name: title,
            type: 'pie',
            radius: '55%',
            data: data,
            emphasis: {
                itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            }
        }]
    })
}

    // 使用处理后的数据渲染饼图
    initPieChart(videoPieChart.value, transformedVideoData, '视频分类统计')
    initPieChart(postPieChart.value, transformedPostData, '帖子分类分布')
    initPieChart(commentChart.value, transformedQuestionData, '题目分类分布')
  
    } catch (error) {
      console.error("图表初始化失败:", error)
      ElMessage.error('图表渲染失败')
    }
  }
  
  // 生命周期钩子
  onMounted(() => {
    loadChartData()
    // 窗口大小变化时重置图表
    window.addEventListener('resize', () => {
      echarts.getInstanceByDom(scoreChart.value)?.resize()
      echarts.getInstanceByDom(accuracyChart.value)?.resize()
      echarts.getInstanceByDom(videoPieChart.value)?.resize()
      echarts.getInstanceByDom(postPieChart.value)?.resize()
      echarts.getInstanceByDom(commentChart.value)?.resize()
    })
  })
  </script>
  
  <style scoped>
  .el-row {
    margin-bottom: 20px;
  }
  
  /* 优化图表容器样式 */
  .el-card {
    margin-bottom: 20px;
    transition: box-shadow 0.3s;
  }
  
  .el-card:hover {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.15);
  }
  </style>