<template>
  <el-card class="reward-management">
    <template #header>
      <div class="card-header">
        <span>奖励管理</span>
      </div>
    </template>

    <!-- 奖励内容展示 -->
    <div v-if="reward" class="reward-content">
      <el-input
        v-if="isEditing"
        v-model="editableReward.context"
        type="textarea"
        :rows="30"
        placeholder="请输入奖励内容"
      />
      <div v-else class="content-display">
        <p>{{ reward.context }}</p>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="actions">
      <el-button v-if="!isEditing" type="primary" @click="startEditing">
        编辑
      </el-button>
      <el-button v-if="isEditing" type="success" @click="saveReward">
        保存
      </el-button>
      <el-button v-if="isEditing" @click="cancelEditing">
        取消
      </el-button>
      <el-button type="danger" @click="deleteReward" :disabled="!reward">
        删除
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import store from '@/store'
import ToUrl from '@/api/api'

// 奖励数据
const reward = ref(null) // 存储奖励对象 { _id, name, context }
const editableReward = ref({ id: '', name: '', context: '' }) // 可编辑的数据
const isEditing = ref(false)

// 加载奖励数据
const loadReward = async () => {
  try {
    const res = await axios.get(ToUrl.url + '/admin/findAllReward', {
      headers: {
        Authorization: `Bearer ${store.state.token}`,
      },
    })
    if (res.data.data.length > 0) {
      reward.value = res.data.data[0] // 直接存储对象
    }
  } catch (error) {
    ElMessage.error('奖励加载失败')
  }
}

// 开始编辑
const startEditing = () => {
  if (reward.value) {
    editableReward.value = { ...reward.value } // 确保是完整对象
    isEditing.value = true
  }
}

// 保存奖励
const saveReward = async () => {
  if (!editableReward.value.context.trim()) {
    ElMessage.warning('奖励内容不能为空')
    return
  }

  try {
    await axios.put(ToUrl.url + `/admin/UpdateReward`, {
      id: editableReward.value.id,
      name: editableReward.value.name,
      context: editableReward.value.context,
    }, {
      headers: {
        Authorization: `Bearer ${store.state.token}`,
      },
    })

    reward.value = { ...editableReward.value }
    isEditing.value = false
    ElMessage.success('奖励已保存')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 取消编辑
const cancelEditing = () => {
  isEditing.value = false
}

// 删除奖励
const deleteReward = async () => {
  try {
    await ElMessageBox.confirm('确定要删除该奖励吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await axios.delete(ToUrl.url + `/admin/deleteReward/${reward.value._id}`, {
      headers: {
        Authorization: `Bearer ${store.state.token}`,
      },
    })
    reward.value = null
    ElMessage.success('奖励已删除')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadReward()
})
</script>

<style scoped>
.reward-management {
  max-width: 800px;
  margin: 0 auto;
}

.reward-content {
  margin-bottom: 20px;
}

.content-display {
  white-space: pre-wrap;
  line-height: 1.6;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>