<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>

      <el-table :data="userList" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="150" />
        
        <el-table-column label="头像" width="120"> 
          <template #default="{ row }">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :on-change="(file) => handleAvatarChange(file, row)"
              :disabled="!editableUsers[row.id]"
            >
              <img v-if="row.avatar" :src="ToUrl.url + '/' + row.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </template>
        </el-table-column>

        <el-table-column label="分数" width="180">
          <template #default="{ row }">
            <el-input-number v-model="row.totalScore" :min="0" :disabled="!editableUsers[row.id]" />
          </template>
        </el-table-column>

        <el-table-column label="密码" width="220">
          <template #default="{ row }">
            <el-input v-model="row.password" :disabled="!editableUsers[row.id]" />
          </template>
        </el-table-column>
        
        <el-table-column label="邮箱" width="300">
          <template #default="{ row }">
            <el-input v-model="row.email" :disabled="!editableUsers[row.id]" style="padding-right: 20px;" />
          </template>
        </el-table-column>

        <el-table-column label="角色" width="150">
          <template #default="{ row }">
            <el-select v-model="row.roles[0]" :disabled="!editableUsers[row.id]" placeholder="选择角色">
              <el-option label="管理员" value="ROLE_ADMIN" />
              <el-option label="普通用户" value="ROLE_USER" />
              <el-option label="VIP用户" value="ROLE_VIP" />
            </el-select>
          </template>
        </el-table-column>
    
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="!editableUsers[row.id]" type="primary" @click="editUser(row)" class="white-text">编辑</el-button>
            <el-button v-else type="success" @click="saveUser(row)" class="white-text">保存</el-button>
            <el-button type="danger" @click="deleteUser(row.id)" class="white-text">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import axios from 'axios'
import store from '@/store'
import ToUrl from '@/api/api'

// 模拟用户列表数据
const userList = ref([])

// 记录每个用户是否可编辑(不能动态绑定单个值)
const editableUsers = reactive({})

//加载用户数据
const loadUser = async () => {
  try {
    const response = await axios.get(ToUrl.url + `/admin/findAllUser`, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    // console.log(response.data.data);
    userList.value = response.data.data;
  } catch (error) {
    ElMessage.error("数据加载失败" + error);
  }
}

// 编辑用户
const editUser = (user) => {
  editableUsers[user.id] = true
}

// 保存用户数据
const saveUser = async (user) => {
  try {
    const response = await axios.put(
      ToUrl.url + `/admin/UpdateUser`,
      {
        id: user.id,
        username: user.username,
        avatar: user.avatar,
        totalScore: user.totalScore,
        password: user.password,
        email: user.email,
        roles: [user.roles[0]]  // 保持roles为数组格式
      },
      {
        headers: { Authorization: `Bearer ${store.state.token}` }
      }
    );

    if (response.data.code === 200) {
      ElMessage.success(`用户 ${user.username} 信息已更新`);
      editableUsers[user.id] = false; // 取消编辑模式
    } else {
      ElMessage.error(response.data.msg || '用户信息更新失败');
    }
  } catch (error) {
    console.error("用户更新失败:", error);
    ElMessage.error(`更新失败: ${error.response?.data?.msg || error.message}`);
  }
};

//上传头像
const handleAvatarChange = async (file, user) => {
  const formData = new FormData();
  formData.append('file', file.raw);
  formData.append('id', user.id);

  try {
    const response = await axios.post(
      ToUrl.url + `/admin/UpdateAvatar`,
      formData,
      {
        headers: {
          'Authorization': `Bearer ${store.state.token}`,
          'Content-Type': 'multipart/form-data'
        }
      }
    );

    if (response.data.code === 200) {
      // 成功上传头像后，强制更新用户头像
      user.avatar = response.data.data;
      // console.log(response.data.data);
      userList.value = [...userList.value]; // 触发 Vue 响应式更新
      ElMessage.success('头像上传成功');
    } else {
      ElMessage.error('头像上传失败');
    }
  } catch (error) {
    console.error('头像上传失败:', error);
    ElMessage.error(`上传失败: ${error.response?.data?.msg || error.message}`);
  }
};


const deleteUser = async (aid) => {
  try {
    const response = await axios.put(ToUrl.url + "/admin/deleteUser", 
      { id: aid }, 
      {
        headers: {
          'Authorization': `Bearer ${store.state.token}`,
          'Content-Type': 'application/json' 
        }
      }
    );
    if (response.data.code === 200) {
      userList.value = userList.value.filter(user => user.id !== aid);
      ElMessage.success('用户已删除');
    } else {
      ElMessage.error(response.data.msg || '删除失败');
    }
  } catch (error) {
    console.error('删除失败:', error);
    ElMessage.error(`删除失败: ${error.response?.data?.msg || error.message}`);
  }
};

onMounted(() => {
  loadUser();
})
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 60px;
  height: 60px;
  display: block;
  border-radius: 50%;
}

.avatar-uploader-icon {
  font-size: 20px;
  color: #8c939d;
}

/* 添加按钮文字颜色样式 */
.white-text {
  color: white !important;
}

/* 确保下拉框文字颜色正常 */
:deep(.el-select) {
  color: #333;
}
</style>