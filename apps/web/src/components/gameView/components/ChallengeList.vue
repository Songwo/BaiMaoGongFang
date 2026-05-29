<template>
  <div class="challenge-list-container">
    <div class="challenge-filters">
      <div class="filter-section">
        <!-- 使用简化的下拉菜单 -->
        <el-select v-model="statusFilter" placeholder="完成状态" clearable class="status-filter">
          <el-option label="全部状态" value="" />
          <el-option label="已完成" value="completed" />
          <el-option label="未完成" value="uncompleted" />
        </el-select>
      </div>
      
      <div class="sort-section">
        <el-radio-group v-model="sortOption" size="small">
          <el-radio-button label="difficulty">按难度排序</el-radio-button>
          <el-radio-button label="title">按名称排序</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    
    <div class="challenges-grid">
      <el-empty v-if="filteredChallenges.length === 0" description="没有找到符合条件的挑战" />
      
      <div 
        v-for="challenge in filteredChallenges" 
        :key="challenge.id" 
        class="challenge-grid-item"
      >
        <challenge-card 
          :challenge="challenge" 
          :is-VIP="isVIP"
          :running-lab-info="runningLabInfo"
          @click="openChallengeDetail(challenge)"
          @start-challenge="$emit('start-challenge', challenge)"
          @verify-flag="$emit('verify-flag', challenge)"
        />
      </div>
    </div>
    
    <!-- 详情对话框 -->
    <challenge-detail
      v-if="selectedChallenge"
      :challenge="selectedChallenge"
      :is-VIP="isVIP"
      :running-lab-info="runningLabInfo"
      :visible="dialogVisible"
      @close="dialogVisible = false"
      @start-challenge="$emit('start-challenge', selectedChallenge)"
      @verify-flag="$emit('verify-flag', selectedChallenge)"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import ChallengeCard from './ChallengeCard.vue'
import ChallengeDetail from './ChallengeDetail.vue'

const props = defineProps({
  challenges: {
    type: Array,
    required: true
  },
  categories: {
    type: Array,
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

const emit = defineEmits(['start-challenge', 'verify-flag'])

// 搜索和过滤
const statusFilter = ref('')
const sortOption = ref('difficulty')

// 详情对话框
const dialogVisible = ref(false)
const selectedChallenge = ref(null)

// 过滤挑战
const filteredChallenges = computed(() => {
  let result = [...props.challenges]
  
  // 状态过滤
  if (statusFilter.value === 'completed') {
    result = result.filter(challenge => challenge.completed)
  } else if (statusFilter.value === 'uncompleted') {
    result = result.filter(challenge => !challenge.completed)
  }
  
  // 排序
  if (sortOption.value === 'difficulty') {
    result.sort((a, b) => a.difficulty - b.difficulty)
  } else if (sortOption.value === 'title') {
    result.sort((a, b) => a.title.localeCompare(b.title))
  }
  
  return result
})

// 打开挑战详情
const openChallengeDetail = (challenge) => {
  // 如果挑战未解锁，则不打开详情对话框
  if (!challenge.unlocked) {
    return
  }
  
  selectedChallenge.value = challenge
  dialogVisible.value = true
}
</script>

<style scoped>
.challenge-list-container {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.challenge-filters {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.status-filter {
  width: 150px;
}

.challenges-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.challenge-grid-item {
  transition: all 0.3s ease;
}

.challenge-grid-item:hover {
  transform: translateY(-5px);
}

/* Override ElementPlus styles for dark theme */
:deep(.el-input__inner) {
  background-color: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  border-color: rgba(255, 255, 255, 0.2);
}

:deep(.el-input__prefix) {
  color: rgba(255, 255, 255, 0.7);
}

:deep(.el-select .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.1);
  box-shadow: none;
}

:deep(.el-select .el-input__inner) {
  color: #ffffff;
}

:deep(.el-radio-button__inner) {
  background-color: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  border-color: rgba(255, 255, 255, 0.2);
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #5524ad;
  color: #ffffff;
  border-color: #5524ad;
  box-shadow: -1px 0 0 0 #5524ad;
}

/* Responsive styles */
@media (max-width: 768px) {
  .challenge-filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-section {
    flex-direction: column;
  }
  
  .challenges-grid {
    grid-template-columns: 1fr;
  }
}
</style> 