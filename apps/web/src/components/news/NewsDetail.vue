<template>
  <div class="news-detail-container">
    <div class="news-header">
      <el-button 
        class="back-btn" 
        type="text" 
        @click="$router.go(-1)"
      >
        <el-icon :size="24" class="icon">
          <ArrowLeft />
        </el-icon>
        返回
      </el-button>
    </div>

    <div class="news-content glass-effect">
      <div class="news-meta">
        <span class="news-date">{{ news.date }}</span>
        <el-tag :type="news.type" effect="dark" class="news-tag">{{ news.type }}</el-tag>
      </div>
      
      <h1 class="news-title">{{ news.title }}</h1>
      
      <div class="news-body">
        <p class="news-summary">{{ news.summary }}</p>
        
        <div class="news-full-content" v-if="news.fullContent">
          <h2>详细内容</h2>
          <p>{{ news.fullContent }}</p>
        </div>

        <div class="news-impact" v-if="news.impact">
          <h2>影响范围</h2>
          <p>{{ news.impact }}</p>
        </div>

        <div class="news-solution" v-if="news.solution">
          <h2>解决方案</h2>
          <p>{{ news.solution }}</p>
        </div>

        <div class="news-references" v-if="news.references && news.references.length > 0">
          <h2>参考资料</h2>
          <ul>
            <li v-for="(ref, index) in news.references" :key="index">
              <a :href="ref.url" target="_blank">{{ ref.title }}</a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import ToUrl from '@/api/api';

const route = useRoute();
const news = ref({
  date: '',
  title: '',
  type: '',
  summary: '',
  fullContent: '',
  impact: '',
  solution: '',
  references: []
});

onMounted(async () => {
  try {
    const newsId = route.params.id;
    const response = await axios.get(`${ToUrl.url}/api/news/${newsId}`);
    const newsData = response.data;
    
    // 转换日期格式
    const createTime = new Date(newsData.createTime);
    const formattedDate = createTime.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });

    news.value = {
      date: formattedDate,
      title: newsData.title,
      type: newsData.type,
      summary: newsData.summary,
      fullContent: newsData.fullContent,
      impact: newsData.impact,
      solution: newsData.solution,
      references: newsData.references || []
    };
  } catch (error) {
    ElMessage.error('获取新闻详情失败');
    console.error('Error fetching news:', error);
  }
});
</script>

<style scoped>
.news-detail-container {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.news-header {
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

.glass-effect {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.news-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.news-date {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
}

.news-tag {
  font-size: 0.8rem;
}

.news-title {
  color: #fff;
  font-size: 2rem;
  margin-bottom: 20px;
  line-height: 1.4;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.news-body {
  color: #fff;
}

.news-summary {
  font-size: 1.1rem;
  line-height: 1.6;
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  backdrop-filter: blur(5px);
}

.news-full-content,
.news-impact,
.news-solution,
.news-references {
  margin-bottom: 30px;
}

h2 {
  color: #fff;
  font-size: 1.5rem;
  margin-bottom: 15px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

p {
  line-height: 1.8;
  margin-bottom: 15px;
  white-space: pre-line;
}

.news-references ul {
  list-style: none;
  padding: 0;
}

.news-references li {
  margin-bottom: 10px;
}

.news-references a {
  color: #fff;
  text-decoration: none;
  transition: all 0.3s;
  padding: 5px 10px;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.1);
}

.news-references a:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .news-detail-container {
    padding: 10px;
  }

  .news-title {
    font-size: 1.5rem;
  }

  .news-summary {
    font-size: 1rem;
  }
}
</style> 