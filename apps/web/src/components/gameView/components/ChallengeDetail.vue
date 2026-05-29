<template>
  <el-dialog
    v-model="dialogVisible"
    :title="challenge.title"
    width="80%"
    destroy-on-close
    class="challenge-detail-dialog"
    @close="$emit('close')"
  >
    <div class="challenge-detail">
      <div class="challenge-header">
        <div class="challenge-meta">
          <el-tag :type="getDifficultyType(challenge.difficulty)" size="medium" class="difficulty-tag">
            {{ getDifficultyStars(challenge.difficulty) }}
          </el-tag>
          <span class="challenge-status" :class="{ 'completed': challenge.completed }">
            <el-icon v-if="challenge.completed"><Check /></el-icon>
            {{ challenge.completed ? '已完成' : '未完成' }}
          </span>
        </div>
      </div>
      
      <el-divider content-position="left">挑战信息</el-divider>
      
      <el-row :gutter="20" class="challenge-content">
        <el-col :span="16">
          <div class="detail-section description-section">
            <h3>
              <el-icon><Document /></el-icon>
              挑战描述
            </h3>
            <p>{{ challenge.description }}</p>
          </div>
          
          <div class="detail-section task-section">
            <h3>
              <el-icon><Flag /></el-icon>
              任务目标
            </h3>
            <p>{{ challenge.task }}</p>
          </div>
          
          <div class="detail-section tutorial-section">
            <h3>
              <el-icon><Guide /></el-icon>
              解题指引
            </h3>
            <div class="tutorial-steps">
              <div v-for="(step, index) in getTutorialSteps(challenge)" :key="index" class="tutorial-step">
                <div class="step-number">{{ index + 1 }}</div>
                <div class="step-content">{{ step }}</div>
              </div>
            </div>
          </div>
          
          <div v-if="challenge.completed" class="detail-section completion-section">
            <h3>
              <el-icon><Trophy /></el-icon>
              完成信息
            </h3>
            <div class="completion-details">
              <p><strong>完成时间：</strong> {{ formatTime(challenge.completionTime) }}</p>
              <p><strong>得分：</strong> {{ challenge.score }}/100</p>
            </div>
          </div>
        </el-col>
        
        <el-col :span="8">
          <div class="detail-section action-section">
            <div v-if="isRunning" class="running-info">
              <div class="status-badge running">正在运行</div>
              <p class="time-remaining">
                剩余时间: {{ formatTimeRemaining(runningLabInfo?.remainingSeconds) }}
              </p>
            </div>
            
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="large" 
                :disabled="(!challenge.unlocked || challenge.completed || isOtherLabRunning) && !isRunning"
                @click="$emit('start-challenge', challenge)"
              >
                {{ isRunning ? '进入靶场' : '启动靶场' }}
              </el-button>
              
              <el-button 
                type="success" 
                size="large" 
                :disabled="!isRunning || challenge.completed"
                @click="$emit('verify-flag', challenge)"
              >
                验证FLAG
              </el-button>
            </div>
            
            <div v-if="isRunning" class="lab-url">
              <el-link :href="challenge.labUrl" target="_blank" type="primary">
                {{ challenge.labUrl }}
              </el-link>
            </div>
            
            <div class="vuln-overview">
              <h3>
                <el-icon><InfoFilled /></el-icon>
                漏洞概述
              </h3>
              
              <div v-if="isVIP" class="vuln-details">
                <div v-for="(step, index) in challenge.vulnSteps" :key="index" class="vuln-step">
                  <div class="step-icon">{{ step.icon }}</div>
                  <div class="step-content">
                    <h4>{{ step.title }}</h4>
                    <p>{{ step.content }}</p>
                  </div>
                </div>
              </div>
              
              <div v-else class="vip-upgrade">
                <p>升级VIP账户可查看漏洞原理和利用方法</p>
                <el-button type="warning" size="small">升级VIP</el-button>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Check, Document, Flag, Guide, Trophy, InfoFilled } from '@element-plus/icons-vue'

const props = defineProps({
  challenge: {
    type: Object,
    required: true
  },
  visible: {
    type: Boolean,
    default: false
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

const emit = defineEmits(['close', 'start-challenge', 'verify-flag'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => {
    if (!value) emit('close')
  }
})

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

// 获取教程步骤
const getTutorialSteps = (challenge) => {
  return [
    '了解漏洞的基本原理和攻击方式',
    '分析目标系统的安全机制和漏洞点',
    '尝试构造有效的攻击载荷',
    '利用漏洞获取系统权限',
    '获取并提交正确的FLAG'
  ]
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return 'N/A'
  return new Date(time).toLocaleString()
}

// 格式化剩余时间
const formatTimeRemaining = (ms) => {
  if (!ms) return '0分0秒'
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}分${remainingSeconds}秒`
}
</script>

<style scoped>
/* Override el-dialog styles for dark theme */
:deep(.el-dialog) {
  background: rgba(30, 30, 30, 0.95);
  border-radius: 10px;
  box-shadow: 0 0 30px rgba(0, 0, 0, 0.5);
}

:deep(.el-dialog__title) {
  color: #ffffff;
  font-size: 20px;
  font-weight: bold;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.7);
}

:deep(.el-divider) {
  background-color: rgba(255, 255, 255, 0.1);
}

:deep(.el-divider__text) {
  background-color: rgba(30, 30, 30, 0.95);
  color: rgba(255, 255, 255, 0.7);
}

/* 确保按钮内的文字可见 */
:deep(.el-button--primary) {
  background-color: #409eff;
  border-color: #409eff;
  color: #ffffff;
}

:deep(.el-button--success) {
  background-color: #67c23a;
  border-color: #67c23a;
  color: #ffffff;
}

:deep(.el-button--warning) {
  background-color: #e6a23c;
  border-color: #e6a23c;
  color: #ffffff;
}

:deep(.el-button--danger) {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: #ffffff;
}

:deep(.el-link) {
  color: #409eff;
}

:deep(.el-link:hover) {
  color: #66b1ff;
}

/* 输入框样式调整 */
:deep(.el-input__inner) {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

:deep(.el-input__placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

.challenge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.challenge-meta {
  display: flex;
  align-items: center;
  gap: 15px;
}

.difficulty-tag {
  font-size: 14px;
}

.challenge-status {
  display: flex;
  align-items: center;
  gap: 5px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.challenge-status.completed {
  color: #27ae60;
}

.challenge-content {
  margin-top: 20px;
}

.detail-section {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.detail-section h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #ffffff;
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-section p {
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
}

.tutorial-steps {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.tutorial-step {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.step-number {
  width: 26px;
  height: 26px;
  background: rgba(85, 36, 173, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
  color: rgba(255, 255, 255, 0.8);
}

.running-info {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.status-badge {
  padding: 6px 14px;
  border-radius: 20px;
  color: white;
  font-size: 14px;
  font-weight: bold;
}

.status-badge.running {
  background-color: #2980b9;
}

.time-remaining {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.action-buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 20px;
}

.action-buttons .el-button {
  flex: 1;
  min-width: 120px;
}

.lab-url {
  margin-bottom: 20px;
  text-align: center;
  word-break: break-all;
}

.vuln-details {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.vuln-step {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 15px;
  transition: all 0.3s ease;
}

.vuln-step:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateX(5px);
}

.step-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.step-content h4 {
  margin: 0 0 5px 0;
  color: #ffffff;
  font-size: 16px;
}

.step-content p {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.vip-upgrade {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  border: 1px dashed rgba(255, 255, 255, 0.2);
}

.vip-upgrade p {
  margin: 0 0 15px 0;
  color: rgba(255, 255, 255, 0.8);
}

.completion-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.completion-details p {
  margin: 0;
}

/* Responsive styles */
@media (max-width: 768px) {
  .challenge-content {
    display: flex;
    flex-direction: column;
  }
  
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 0 auto;
  }
  
  /* 在小屏幕上保持按钮垂直排列 */
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style> 