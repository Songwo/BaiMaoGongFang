<template>
  <div 
    class="challenge-card" 
    :class="{ 
      'is-completed': challenge.completed,
      'is-unlocked': challenge.unlocked,
      'is-locked': !challenge.unlocked,
      'is-running': isRunning
    }"
    @click="handleCardClick"
  >
    <div class="card-badges">
      <div v-if="challenge.completed" class="badge completed">
        <el-icon><Check /></el-icon>
      </div>
      <div v-if="isRunning" class="badge running">
        <el-icon><Timer /></el-icon>
      </div>
      <div v-if="!challenge.unlocked" class="badge locked">
        <el-icon><Lock /></el-icon>
      </div>
    </div>
    
    <div v-if="challenge.completed" class="completion-badge">
      已通关
    </div>
    
    <div class="card-header">
      <div class="difficulty">
        <el-tag :type="getDifficultyType(challenge.difficulty)" size="small">
          {{ getDifficultyStars(challenge.difficulty) }}
        </el-tag>
      </div>
    </div>
    
    <div class="card-content">
      <h3 class="challenge-title">{{ challenge.title }}</h3>
      <p class="challenge-description">{{ truncateDescription(challenge.description) }}</p>
    </div>
    
    <div class="card-footer">
      <el-button 
        v-if="challenge.unlocked && !challenge.completed" 
        type="primary" 
        size="small"
        :disabled="isOtherLabRunning && !isRunning"
        @click.stop="$emit('start-challenge', challenge)"
      >
        {{ isRunning ? '进入靶场' : '启动靶场' }}
      </el-button>
      
      <el-button 
        v-if="isRunning && !challenge.completed" 
        type="success" 
        size="small"
        @click.stop="$emit('verify-flag', challenge)"
      >
        验证FLAG
      </el-button>
      
      <div v-if="challenge.completed" class="completion-info">
        <el-icon><Trophy /></el-icon>
        <span>已通关</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Check, Lock, Trophy, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  challenge: {
    type: Object,
    required: true
  },
  isVIP: {
    type: Boolean,
    default: false
  },
  runningLabInfo: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['click', 'start-challenge', 'verify-flag'])

// 处理卡片点击，如果未解锁则阻止点击并提示
const handleCardClick = () => {
  if (!props.challenge.unlocked) {
    ElMessage.warning('该靶场尚未解锁，请先完成前面的挑战')
    return
  }
  
  emit('click')
}

// 是否是正在运行的靶场
const isRunning = computed(() => {
  return props.runningLabInfo && 
         props.runningLabInfo.challengeId === props.challenge.id &&
         props.challenge.labUrl
})

// 是否有其他靶场正在运行
const isOtherLabRunning = computed(() => {
  return props.runningLabInfo && 
         props.runningLabInfo.challengeId !== props.challenge.id
})

// 根据难度返回星星数量
const getDifficultyStars = (difficulty) => {
  return '★'.repeat(difficulty)
}

// 根据难度返回标签类型
const getDifficultyType = (difficulty) => {
  if (difficulty === 1) return 'success'
  if (difficulty === 2) return 'warning'
  if (difficulty === 3) return 'danger'
  return 'info'
}

// 截断描述文本
const truncateDescription = (text) => {
  if (!text) return ''
  return text.length > 60 ? text.substring(0, 60) + '...' : text
}
</script>

<style scoped>
.challenge-card {
  height: 230px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  padding: 15px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.challenge-card:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.challenge-card.is-completed {
  background: linear-gradient(135deg, rgba(39, 174, 96, 0.1), rgba(85, 36, 173, 0.1));
  border-color: rgba(39, 174, 96, 0.3);
}

.challenge-card.is-running {
  background: linear-gradient(135deg, rgba(41, 128, 185, 0.1), rgba(85, 36, 173, 0.1));
  border-color: rgba(41, 128, 185, 0.3);
  animation: pulse 2s infinite;
}

.challenge-card.is-locked {
  background: rgba(100, 100, 100, 0.15);
  border-color: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.4);
  cursor: not-allowed;
  filter: grayscale(90%);
  position: relative;
}

.challenge-card.is-locked::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  border-radius: 10px;
  z-index: 1;
}

.challenge-card.is-locked::after {
  content: '🔒 未解锁';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #ffffff;
  font-size: 18px;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.7);
  background-color: rgba(100, 100, 100, 0.7);
  padding: 8px 16px;
  border-radius: 4px;
  z-index: 2;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(41, 128, 185, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(41, 128, 185, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(41, 128, 185, 0);
  }
}

.card-badges {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  gap: 5px;
}

.badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
}

.badge.completed {
  background-color: #27ae60;
}

.badge.running {
  background-color: #2980b9;
}

.badge.locked {
  background-color: #7f8c8d;
}

.card-header {
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  flex: 1;
  overflow: hidden;
}

.challenge-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #ffffff;
  line-height: 1.3;
}

.challenge-description {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.4;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-footer {
  margin-top: 15px;
  display: flex;
  justify-content: flex-start;
  gap: 10px;
  align-items: center;
}

.completion-info {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #27ae60;
  font-size: 16px;
  font-weight: bold;
  background: rgba(39, 174, 96, 0.1);
  padding: 5px 10px;
  border-radius: 4px;
  border: 1px solid rgba(39, 174, 96, 0.3);
}

.completion-info .el-icon {
  font-size: 18px;
}

.completion-info span {
  font-weight: bold;
}

.completion-badge {
  position: absolute;
  top: 30px;
  right: -15px;
  background: #27ae60;
  color: white;
  font-weight: bold;
  padding: 5px 40px;
  transform: rotate(45deg);
  font-size: 14px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  z-index: 2;
}

/* Make element plus button more compact */
:deep(.el-button--small) {
  padding: 6px 12px;
  font-size: 12px;
}
</style> 