<template>
  <div class="about-us">
    <!-- Hero Section -->
    <div class="hero-section">
      <h1>关于我们</h1>
      <p class="subtitle">一群充满激情的年轻开发者</p>
    </div>

    <!-- Company Introduction -->
    <section class="company-intro">
      <div class="container">
        <h2>团队介绍</h2>
        <div class="content">
          <div class="text">
            <p>我们是一支充满活力和创造力的年轻开发团队，专注于为用户提供优质的技术解决方案。虽然我们刚刚起步，但我们拥有无限的热情和坚定的信念。</p>
            <p>我们的团队由五位专业的技术人员组成，涵盖了前端、后端、App开发、小程序开发和网络技术等多个领域。我们相信，通过团队协作和持续学习，我们能够为客户创造价值。</p>
          </div>
          <div class="stats">
            <div class="stat-item">
              <h3>5</h3>
              <p>核心成员</p>
            </div>
            <div class="stat-item">
              <h3>4+</h3>
              <p>技术领域</p>
            </div>
            <div class="stat-item">
              <h3>100%</h3>
              <p>团队热情</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Team Section -->
    <section v-if="isLoggedIn" class="team-section">
      <div class="container">
        <h2>团队成员</h2>
        <div class="team-grid">
          <div class="team-member" v-for="member in teamMembers" :key="member.id">
            <div class="member-image">
              <img :src="member.image" :alt="member.name">
            </div>
            <div class="member-info">
              <h3>{{ member.name }}</h3>
              <p class="position">{{ member.position }}</p>
              <p class="description">{{ member.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Culture Section -->
    <section class="culture-section">
      <div class="container">
        <h2>团队理念</h2>
        <div class="culture-grid">
          <div class="culture-item" v-for="culture in cultures" :key="culture.id">
            <div class="icon">
              <i :class="culture.icon"></i>
            </div>
            <h3>{{ culture.title }}</h3>
            <p>{{ culture.description }}</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import a from '@/assets/Data/song.jpg'
import b from '@/assets/Data/jun.jpg'
import c from '@/assets/Data/long.jpg'
import d from '@/assets/Data/ke.jpg'
import e from '@/assets/Data/hao.jpg'

const store = useStore()

// 判断是否登录
const isLoggedIn = computed(() => {
  return store.state.token && store.state.token !== 'null' && store.state.token !== '';
});

const teamMembers = ref([
  {
    id: 1,
    name: '赵青松',
    position: '前端开发',
    description: '专注于前端技术开发，致力于打造流畅的用户体验和精美的界面设计。',
    image: a
  },
  {
    id: 2,
    name: '王嘉俊',
    position: '后端开发',
    description: '负责后端架构设计和开发，确保系统的稳定性和安全性。',
    image: b
  },
  {
    id: 3,
    name: '郑永龙',
    position: 'App开发',
    description: '专注于移动应用开发，致力于为用户提供便捷的移动端解决方案。',
    image: c
  },
  {
    id: 4,
    name: '李柯',
    position: '微信小程序开发',
    description: '专注于微信小程序开发，为用户提供轻量级的应用体验。',
    image: d
  },
  {
    id: 5,
    name: '张君昊',
    position: '网络技术',
    description: '负责网络架构和运维，确保系统的稳定运行和安全性。',
    image: e
  }
])

const cultures = ref([
  {
    id: 1,
    title: '持续学习',
    description: '保持对新技术的好奇心，不断学习和进步。',
    icon: 'fas fa-book'
  },
  {
    id: 2,
    title: '团队协作',
    description: '相互支持，共同成长，打造高效团队。',
    icon: 'fas fa-users'
  },
  {
    id: 3,
    title: '追求品质',
    description: '注重代码质量，追求卓越的用户体验。',
    icon: 'fas fa-star'
  }
])
</script>

<style scoped>
.about-us {
  color: #333;
}

.hero-section {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
  padding: 100px 0;
  text-align: center;
}

.hero-section h1 {
  font-size: 3.5rem;
  margin-bottom: 1rem;
}

.subtitle {
  font-size: 1.5rem;
  opacity: 0.9;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

section {
  padding: 80px 0;
}

h2 {
  font-size: 2.5rem;
  text-align: center;
  margin-bottom: 50px;
  color: #1f2937;
}

/* Company Introduction Styles */
.company-intro {
  background-color: #f9fafb;
}

.content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 50px;
  align-items: center;
}

.text p {
  margin-bottom: 20px;
  line-height: 1.6;
  font-size: 1.1rem;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

.stat-item {
  text-align: center;
  padding: 30px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-5px);
}

.stat-item h3 {
  font-size: 2.5rem;
  color: #4f46e5;
  margin-bottom: 10px;
}

/* Team Section Styles */
.team-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.team-member {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.team-member:hover {
  transform: translateY(-5px);
}

.member-image img {
  width: 100%;
  height: 300px;
  object-fit: cover;
}

.member-info {
  padding: 20px;
}

.member-info h3 {
  font-size: 1.5rem;
  margin-bottom: 5px;
}

.position {
  color: #4f46e5;
  font-weight: 500;
  margin-bottom: 10px;
}

/* Culture Section Styles */
.culture-section {
  background-color: #f9fafb;
}

.culture-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.culture-item {
  text-align: center;
  padding: 30px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.icon {
  font-size: 2.5rem;
  color: #4f46e5;
  margin-bottom: 20px;
}

.culture-item h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
}

@media (max-width: 768px) {
  .content {
    grid-template-columns: 1fr;
  }

  .stats {
    grid-template-columns: 1fr;
  }

  .hero-section h1 {
    font-size: 2.5rem;
  }
}
</style>