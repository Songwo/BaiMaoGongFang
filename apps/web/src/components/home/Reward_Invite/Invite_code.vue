<template>
  <el-dialog
    v-model="dialogVisible"
    title="邀请好友"
    width="30%"
  >
    <div class="invite-container">
      <!-- 生成邀请码部分 -->
      <div class="generate-section">
        <h3>我的邀请码</h3>
        <div class="code-display" v-if="myInviteCode">
          <el-input v-model="myInviteCode" readonly>
            <template #append>
              <el-button @click="copyInviteCode">复制</el-button>
            </template>
          </el-input>
        </div>
        <el-button type="primary" @click="generateInviteCode" v-else>
          生成邀请码
        </el-button>
      </div>

      <el-divider>或</el-divider>

      <!-- 输入邀请码部分 -->
      <div class="input-section">
        <h3>输入好友邀请码</h3>
        <el-form>
          <el-form-item label="邀请码">
            <el-input v-model="inviteCode" placeholder="请输入好友的邀请码" />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitInviteCode">确认</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import ToUrl from '@/api/api'
import store from '@/store'

const dialogVisible = ref(false)
const inviteCode = ref('')
const myInviteCode = ref('')

const emit = defineEmits(['invite-success'])

// 生成邀请码
const generateInviteCode = async () => {
  try {
    const { data } = await axios.get(ToUrl.url + '/user/generateInviteCode', {
      headers: { Authorization: `Bearer ${store.state.token}` }
    })
    if (data.code === 200) {
      myInviteCode.value = data.data
      ElMessage.success('邀请码生成成功！')
    } else {
      ElMessage.error(data.msg || '生成邀请码失败')
    }
  } catch (err) {
    ElMessage.error('生成邀请码失败，请稍后重试')
    console.error(err)
  }
}

// 复制邀请码
const copyInviteCode = () => {
  if (myInviteCode.value) {
    navigator.clipboard.writeText(myInviteCode.value)
      .then(() => {
        ElMessage.success('邀请码已复制到剪贴板')
      })
      .catch(() => {
        ElMessage.error('复制失败，请手动复制')
      })
  }
}

const submitInviteCode = async () => {
  if (!inviteCode.value) {
    ElMessage.warning('请输入邀请码')
    return
  }

  try {
    const { data } = await axios.post(ToUrl.url + '/user/verifyInviteCode', {
      inviteCode: inviteCode.value,
      username: store.state.user
    }, {
      headers: { Authorization: `Bearer ${store.state.token}` }
    })
    
    if (data.code === 200) {
      ElMessage.success('邀请成功！')
      dialogVisible.value = false
      inviteCode.value = '' // 清空输入
      emit('invite-success') // 触发成功事件
    } else {
      ElMessage.error(data.msg || '邀请失败')
    }
  } catch (err) {
    ElMessage.error('邀请失败，请稍后重试')
    console.error(err)
  }
}

defineExpose({
  dialogVisible
})
</script>

<style scoped>
.invite-container {
  padding: 20px 0;
}

.generate-section,
.input-section {
  text-align: center;
  margin-bottom: 20px;
}

.generate-section h3,
.input-section h3 {
  margin-bottom: 16px;
  color: #333;
}

.code-display {
  margin: 16px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

:deep(.el-dialog__title) {
  color: #000;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #000;
}

:deep(.el-divider__text) {
  background-color: #fff;
  color: #909399;
}
</style>
