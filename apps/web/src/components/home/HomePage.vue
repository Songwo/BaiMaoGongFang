<template>
    <div class="homepage">
      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 左侧轮播图 -->
        <div class="left-section">
          <div class="carousel-container">
            <el-carousel :interval="5000" arrow="always" height="600px">
              <el-carousel-item v-for="(item, index) in carouselImages" :key="index">
                <img :src="item" alt="carousel image" class="carousel-image"/>
              </el-carousel-item>
            </el-carousel>
          </div>
        </div>

        <!-- 右侧新闻区域 -->
        <div class="right-section">
          <div class="news-container">
            <h2>网络安全动态</h2>
            <div class="news-list">
              <div class="news-item" v-for="(news, index) in securityNews" :key="index">
                <div class="news-header">
                  <span class="news-date">{{ news.date }}</span>
                  <span class="news-tag" :class="news.type">{{ news.type }}</span>
                </div>
                <h3>{{ news.title }}</h3>
                <p>{{ news.summary }}</p>
                <router-link :to="'/bmgf/news/' + news.id" class="read-more">阅读更多</router-link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 首页欢迎语 -->
      <div class="welcome-message" v-if="showWelcome">
        <div class="close-button" @click="showWelcome = false">
          <el-icon><Close /></el-icon>
        </div>
        <h1>
          <img src="/src/assets/logo/logo/信息.png" alt="carousel image" class="logo-image"/>
          欢迎来到网络攻防安全学习平台
        </h1>
        <p>从这里开始，探索网络安全的世界</p>
      </div> 

      <!-- 网络安全重要性 -->
      <div class="cybersecurity-importance">
        <div class="importance-content">
          <div class="importance-text">
            <h2>守护数字中国，共筑网络安全长城</h2>
            <div class="importance-points">
              <div class="point">
                <el-icon><Flag /></el-icon>
                <div class="point-content">
                  <h3>国家战略需求</h3>
                  <p>网络安全是国家安全的重要组成部分，是数字中国建设的基础保障</p>
                </div>
              </div>
              <div class="point">
                <el-icon><Connection /></el-icon>
                <div class="point-content">
                  <h3>数字经济发展</h3>
                  <p>网络安全人才是推动数字经济高质量发展的核心力量</p>
                </div>
              </div>
              <div class="point">
                <el-icon><Lock /></el-icon>
                <div class="point-content">
                  <h3>关键基础设施</h3>
                  <p>保护国家关键信息基础设施，维护国家安全和公共利益</p>
                </div>
              </div>
            </div>
            <div class="call-to-action">
              <p>加入我们，成为守护数字中国的中坚力量</p>
              <el-button type="primary" class="join-button">立即开始学习</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 人才发展趋势图 -->
      <div class="talent-growth">
        <h2>我国网络安全人才发展趋势</h2>
        <div class="chart-container" ref="talentChartRef"></div>
      </div>

      <!-- 项目资源链接 -->
      <div class="project-links">
        <h2>项目资源</h2>
        <div class="links-grid">
          <a href="http://www.wacyg.fun" target="_blank" class="link-card">
            <el-icon><Monitor /></el-icon>
            <h3>在线体验平台</h3>
            <p>访问我们的在线学习平台</p>
          </a>
          <a href="https://blog.csdn.net/zhao9585/article/details/147677821" target="_blank" class="link-card">
            <el-icon><Reading /></el-icon>
            <h3>配套教程</h3>
            <p>CSDN详细教程文档</p>
          </a>
          <a href="https://github.com/Songwo/BaiMaoGongFang.git" target="_blank" class="link-card">
            <el-icon><Link /></el-icon>
            <h3>GitHub源码</h3>
            <p>获取项目完整源代码</p>
          </a>
          <a href="https://juejin.cn/post/7499050342774718518" target="_blank" class="link-card">
            <el-icon><Document /></el-icon>
            <h3>掘金专栏</h3>
            <p>技术文章与经验分享</p>
          </a>
          <a href="https://zhuanlan.zhihu.com/p/1902005033681290221" target="_blank" class="link-card">
            <el-icon><ChatDotRound /></el-icon>
            <h3>知乎专栏</h3>
            <p>深度技术讨论</p>
          </a>
          <a href="https://www.jianshu.com/p/717bf5dca0ac" target="_blank" class="link-card">
            <el-icon><Edit /></el-icon>
            <h3>简书文章</h3>
            <p>项目详细说明</p>
          </a>
        </div>
      </div>

      <!-- 特色功能区块 -->
      <div class="features-section">
        <div class="feature-card">
          <el-icon><Monitor /></el-icon>
          <h3>实时攻防演练</h3>
          <p>提供真实的攻防环境，让学习更贴近实战</p>
        </div>
        <div class="feature-card">
          <el-icon><Reading /></el-icon>
          <h3>系统化课程</h3>
          <p>从入门到进阶的完整学习体系</p>
        </div>
        <div class="feature-card">
          <el-icon><Trophy /></el-icon>
          <h3>技能认证</h3>
          <p>完成课程可获得专业认证证书</p>
        </div>
        <div class="feature-card">
          <el-icon><User /></el-icon>
          <h3>专家指导</h3>
          <p>行业专家一对一指导，答疑解惑</p>
        </div>
      </div>

      <!-- 热门课程 -->
      <div class="courses-section">
        <h2>热门课程</h2>
        <div class="course-grid">
          <div class="course-card" v-for="(course, index) in hotCourses" :key="index">
            <img :src="course.image" :alt="course.title">
            <div class="course-info">
              <h3>{{ course.title }}</h3>
              <p>{{ course.description }}</p>
              <div class="course-meta">
                <span>{{ course.level }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 学习路径 -->
      <div class="learning-path">
        <h2>学习路径</h2>
        <div class="path-timeline">
          <div class="path-step" v-for="(step, index) in learningPath" :key="index">
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-content">
              <h3>{{ step.title }}</h3>
              <p>{{ step.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { Monitor, Reading, Trophy, User, Link, Document, ChatDotRound, Edit, Flag, Connection, Lock, Close } from '@element-plus/icons-vue'
  import * as echarts from 'echarts'
  import { useRouter } from 'vue-router';
  import axios from 'axios';
  import ToUrl from '@/api/api';
  
// 使用 import 导入图片
import homePage1 from '@/assets/homepage2.jpg';
import homePage2 from '@/assets/homepage1.jpg';
import homePage3 from '@/assets/homepage3.jpg';
import homePage4 from '@/assets/homepage4.jpg';
import homePage5 from '@/assets/homepage5.jpg';

const carouselImages = ref([homePage1, homePage2, homePage3,homePage4,homePage5]);

// 热门课程数据
const hotCourses = ref([
  {
    title: 'Web安全基础',
    description: '学习Web应用安全基础知识，掌握常见漏洞原理与防御方法',
    image: homePage1,
    students: 1234,
    level: '入门'
  },
  {
    title: '渗透测试实战',
    description: '系统学习渗透测试方法论，掌握各类工具的使用技巧',
    image: homePage2,
    students: 856,
    level: '进阶'
  },
  {
    title: '安全开发实践',
    description: '学习安全开发规范，掌握代码审计与漏洞修复技术',
    image: homePage3,
    students: 567,
    level: '高级'
  }
]);

// 学习路径数据
const learningPath = ref([
  {
    title: '基础知识',
    description: '计算机网络、操作系统、编程语言等基础知识'
  },
  {
    title: '安全基础',
    description: '密码学、安全协议、安全架构等安全基础知识'
  },
  {
    title: '攻防技术',
    description: '渗透测试、漏洞利用、安全防护等实战技能'
  },
  {
    title: '高级主题',
    description: '安全开发、安全运维、应急响应等专业领域'
  }
]);

const router = useRouter();
const securityNews = ref([]);

onMounted(async () => {
  try {
    const response = await axios.get(`${ToUrl.url}/api/news`);
    securityNews.value = response.data.map(news => ({
      id: news.id,
      title: news.title,
      summary: news.summary,
      type: news.type,
      date: new Date(news.createTime).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    }));
  } catch (error) {
    console.error('Error fetching news:', error);
  }
});

const goToNewsDetail = (newsId) => {
  router.push(`/news/${newsId}`);
};

// 控制欢迎语显示
const showWelcome = ref(true);

// 人才发展趋势图
const talentChartRef = ref(null);

onMounted(() => {
  const chart = echarts.init(talentChartRef.value);
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['2019', '2020', '2021', '2022', '2023', '2024'],
      axisLine: {
        lineStyle: {
          color: '#fff'
        }
      },
      axisLabel: {
        color: '#fff'
      }
    },
    yAxis: {
      type: 'value',
      name: '人才缺口（万人）',
      nameTextStyle: {
        color: '#fff'
      },
      axisLine: {
        lineStyle: {
          color: '#fff'
        }
      },
      axisLabel: {
        color: '#fff'
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.1)'
        }
      }
    },
    series: [
      {
        name: '人才缺口',
        type: 'line',
        smooth: true,
        data: [140, 150, 170, 190, 210, 230],
        itemStyle: {
          color: '#d0ffdc'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(208, 255, 220, 0.3)'
            },
            {
              offset: 1,
              color: 'rgba(208, 255, 220, 0.1)'
            }
          ])
        },
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3
        }
      }
    ]
  };

  chart.setOption(option);

  // 响应式调整
  window.addEventListener('resize', () => {
    chart.resize();
  });
});
  </script>
  
  <style scoped>
  .homepage {
  font-family: 'Arial', sans-serif;
  color: #fff;
  min-height: calc(100vh - 60px);
  position: relative;
  padding: 20px;
}

.main-content {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
}

.left-section {
  flex: 1.5;
}

.right-section {
  flex: 1;
}

.carousel-container {
  width: 100%;
  height: 500px;
  margin-top: 10px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.welcome-message {
  text-align: center;
  margin-top: 40px;
  position: relative;
  z-index: 2;
  background: rgba(0, 0, 0, 0.5);
  padding: 20px;
  border-radius: 10px;
  backdrop-filter: blur(5px);
}

.close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  transition: all 0.3s;
}

.close-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.close-button .el-icon {
  font-size: 20px;
  color: #ffffff;
}

.welcome-message h1 {
  font-size: 3rem;
  font-weight: 700;
  color: #d0ffdc;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.logo-image {
  width: 90px;
  height: auto;
  margin: 0;
  position: static;
}
  
  .carousel-container {
    width: 100%;
    height: 500px;
    margin-top: 10px;
    border-radius: 10px;
    overflow: hidden;
    object-fit: cover;
  }
  
  .carousel-image {
    width: 100%;
    height: 500px;
    object-fit: cover;
  }
  
  .welcome-message p {
    position: relative;
    top: 0px;
    left: 40px;
    font-size: 1.5rem;
    color: #ffffff;
    margin-bottom: 20px;
  }

  .project-links {
    margin: 40px 0;
    padding: 20px;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 10px;
  }

  .links-grid {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 20px;
  }

  .link-card {
    background: rgba(0, 0, 0, 0.4);
    padding: 20px;
    border-radius: 8px;
    text-align: center;
    transition: transform 0.3s;
  }

  .link-card:hover {
    transform: translateY(-5px);
  }

  .link-card .el-icon {
    font-size: 40px;
    color: #d0ffdc;
    margin-bottom: 15px;
  }

  .link-card h3 {
    color: #d0ffdc;
    margin: 10px 0;
  }

  .link-card p {
    color: #ffffff;
    font-size: 0.9rem;
  }

  .features-section {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin: 40px 0;
    padding: 0 20px;
  }

  .feature-card {
    background: rgba(0, 0, 0, 0.6);
    padding: 20px;
    border-radius: 10px;
    text-align: center;
    transition: transform 0.3s;
  }

  .feature-card:hover {
    transform: translateY(-5px);
  }

  .feature-card .el-icon {
    font-size: 40px;
    color: #d0ffdc;
    margin-bottom: 15px;
  }

  .feature-card h3 {
    color: #d0ffdc;
    margin: 10px 0;
  }

  .feature-card p {
    color: #ffffff;
    font-size: 0.9rem;
  }

  .courses-section, .learning-path, .news-section {
    margin: 40px 0;
    padding: 20px;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 10px;
  }

  h2 {
    color: #d0ffdc;
    text-align: center;
    margin-bottom: 30px;
    font-size: 2rem;
  }

  .course-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }

  .course-card {
    background: rgba(0, 0, 0, 0.4);
    border-radius: 8px;
    overflow: hidden;
    transition: transform 0.3s;
  }

  .course-card:hover {
    transform: translateY(-5px);
  }

  .course-card img {
    width: 100%;
    height: 160px;
    object-fit: cover;
  }

  .course-info {
    padding: 15px;
  }

  .course-info h3 {
    color: #d0ffdc;
    margin-bottom: 10px;
  }

  .course-info p {
    color: #ffffff;
    font-size: 0.9rem;
    margin-bottom: 10px;
  }

  .course-meta {
    display: flex;
    justify-content: space-between;
    color: #a0a0a0;
    font-size: 0.8rem;
  }

  .path-timeline {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .path-step {
    display: flex;
    align-items: flex-start;
    gap: 20px;
  }

  .step-number {
    width: 40px;
    height: 40px;
    background: #d0ffdc;
    color: #000;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
  }

  .step-content {
    flex: 1;
  }

  .step-content h3 {
    color: #d0ffdc;
    margin-bottom: 5px;
  }

  .step-content p {
    color: #ffffff;
  }

  .news-container {
    background: rgba(0, 0, 0, 0.6);
    border-radius: 10px;
    padding: 20px;
    height: 500px;
    margin-top: 10px;
    overflow-y: auto;
    scrollbar-width: thin;
    scrollbar-color: rgba(208, 255, 220, 0.3) transparent;
  }

  .news-container h2 {
    color: #d0ffdc;
    margin-bottom: 20px;
    font-size: 1.8rem;
    text-align: left;
  }

  .news-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .news-item {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    padding: 20px;
    margin-bottom: 20px;
    transition: all 0.3s ease;
  }

  .news-item:hover {
    transform: translateY(-5px);
    background: rgba(255, 255, 255, 0.15);
  }

  .news-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .news-date {
    color: rgba(255, 255, 255, 0.6);
    font-size: 0.9rem;
  }

  .news-tag {
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 0.8rem;
  }

  .news-tag.warning {
    background: #e6a23c;
    color: #fff;
  }

  .news-tag.danger {
    background: #f56c6c;
    color: #fff;
  }

  .news-tag.info {
    background: #909399;
    color: #fff;
  }

  .news-tag.success {
    background: #67c23a;
    color: #fff;
  }

  .news-item h3 {
    color: #fff;
    margin-bottom: 10px;
    font-size: 1.2rem;
  }

  .news-item p {
    color: rgba(255, 255, 255, 0.8);
    margin-bottom: 15px;
    line-height: 1.6;
  }

  .read-more {
    color: #409EFF;
    text-decoration: none;
    cursor: pointer;
    transition: color 0.3s;
  }

  .read-more:hover {
    color: #79bbff;
    text-decoration: underline;
  }

  /* 自定义滚动条样式 - Webkit (Chrome, Safari, Edge) */
  .news-container::-webkit-scrollbar {
    width: 6px;
  }

  .news-container::-webkit-scrollbar-track {
    background: transparent;
  }

  .news-container::-webkit-scrollbar-thumb {
    background-color: rgba(208, 255, 220, 0.3);
    border-radius: 3px;
  }

  .news-container::-webkit-scrollbar-thumb:hover {
    background-color: rgba(208, 255, 220, 0.5);
  }

  .cybersecurity-importance {
    margin: 40px 0;
    padding: 40px 20px;
    background: linear-gradient(135deg, rgba(0, 0, 0, 0.8) 0%, rgba(0, 0, 0, 0.6) 100%);
    border-radius: 15px;
    position: relative;
    overflow: hidden;
  }

  .cybersecurity-importance::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('/src/assets/homepage1.jpg') center/cover;
    opacity: 0.1;
    z-index: 0;
  }

  .importance-content {
    position: relative;
    z-index: 1;
    max-width: 1200px;
    margin: 0 auto;
  }

  .importance-text h2 {
    color: #d0ffdc;
    font-size: 2.2rem;
    text-align: center;
    margin-bottom: 40px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }

  .importance-points {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 30px;
    margin-bottom: 40px;
  }

  .point {
    display: flex;
    align-items: flex-start;
    gap: 15px;
    padding: 20px;
    background: rgba(0, 0, 0, 0.4);
    border-radius: 10px;
    transition: transform 0.3s;
  }

  .point:hover {
    transform: translateY(-5px);
  }

  .point .el-icon {
    font-size: 2rem;
    color: #d0ffdc;
    flex-shrink: 0;
  }

  .point-content h3 {
    color: #d0ffdc;
    margin-bottom: 10px;
    font-size: 1.2rem;
  }

  .point-content p {
    color: #ffffff;
    font-size: 0.95rem;
    line-height: 1.6;
  }

  .call-to-action {
    text-align: center;
    margin-top: 40px;
  }

  .call-to-action p {
    color: #ffffff;
    font-size: 1.2rem;
    margin-bottom: 20px;
  }

  .join-button {
    background: #d0ffdc;
    color: #000;
    border: none;
    padding: 12px 30px;
    font-size: 1.1rem;
    border-radius: 25px;
    transition: all 0.3s;
  }

  .join-button:hover {
    background: #b3ffc4;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(208, 255, 220, 0.3);
  }

  .talent-growth {
    margin: 40px 0;
    padding: 40px 20px;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 15px;
  }

  .talent-growth h2 {
    color: #d0ffdc;
    text-align: center;
    margin-bottom: 30px;
    font-size: 2rem;
  }

  .chart-container {
    width: 100%;
    height: 400px;
    margin: 0 auto;
  }
  </style>
  