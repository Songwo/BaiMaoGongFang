<template>
  <div class="challenge-container">
    <div class="challenge-header">
      <img :src="logoUrl" alt="Logo" class="logo" />
      <h2>SQL注入靶场</h2>
    </div>

    <el-card class="challenge-card">
      <template #header>
        <div class="card-header">
          <h3>靶场说明</h3>
          <el-tag type="success">难度：★☆☆☆☆</el-tag>
        </div>
      </template>

      <div class="challenge-content">
        <div class="description-section">
          <p>这是一个SQL注入靶场，用于学习和实践SQL注入技术。靶场模拟了一个简单的用户系统，你的目标是获取第二个用户（ID=2）的密码。</p>
          <div class="tips">
            <h4>提示：</h4>
            <ul>
              <li>尝试使用单引号(')来闭合SQL语句</li>
              <li>使用OR 1=1来绕过登录验证</li>
              <li>使用UNION SELECT来获取其他用户的数据</li>
              <li>注意观察错误信息，它们可能包含有用的信息</li>
            </ul>
          </div>
        </div>

        <div class="login-section">
          <h4>用户系统</h4>
          <el-form :model="loginForm" class="login-form">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogin">登录</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-if="loginResult" class="result-section">
          <h4>查询结果</h4>
          <div :class="['result-message', loginResult.success ? 'success' : 'error']">
            {{ loginResult.message }}
          </div>
          <div v-if="loginResult.data" class="result-data">
            <pre>{{ JSON.stringify(loginResult.data, null, 2) }}</pre>
          </div>
        </div>

        <div class="target-section">
          <h4>目标</h4>
          <p>获取ID=2用户的密码</p>
          <p class="hint">提示：尝试使用UNION SELECT语句来获取其他用户的信息</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import store from '@/store'
import { ElMessage } from 'element-plus'

const backendUrl = store.state.backendUrl
const logoUrl = ref('/src/assets/logo/logo/信息.png')
const loginForm = ref({
  username: '',
  password: ''
})
const loginResult = ref(null)

const handleLogin = async () => {
  if(!backendUrl){
    ElMessage.error('请先登录或检查后端地址')
    return
  }
  try {
    const response = await axios.post(backendUrl+'/login', {
      username: loginForm.value.username,
      password: loginForm.value.password
    })

    if(response.data.success === false){
      loginResult.value = {
        success: false,
        message: response.data.error || '登录失败',
        data: response.data
      }
      return
    }

    loginResult.value = {
      success: true,
      message: '登录成功！',
      data: response.data
    }
  } catch (error) {
    loginResult.value = {
      success: false,
      message: error.response?.data?.message || '登录失败，请检查输入',
      data: error.response?.data
    }
  }
}
</script>

<style scoped>
.challenge-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.challenge-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.logo {
  height: 50px;
  width: auto;
}

.challenge-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.challenge-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #333;
}

.challenge-content {
  padding: 20px 0;
}

.description-section {
  margin-bottom: 30px;
}

.description-section p {
  color: #666;
  line-height: 1.6;
}

.tips {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-top: 20px;
}

.tips h4 {
  color: #333;
  margin-bottom: 10px;
}

.tips ul {
  padding-left: 20px;
  color: #666;
}

.tips li {
  margin-bottom: 8px;
}

.login-section {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.login-section h4 {
  color: #333;
  margin-bottom: 20px;
}

.login-form {
  max-width: 400px;
}

.result-section {
  margin-top: 20px;
  padding: 20px;
  border-radius: 8px;
  background-color: #f8f9fa;
}

.result-section h4 {
  color: #333;
  margin-bottom: 15px;
}

.result-message {
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.result-message.success {
  background-color: #f0f9eb;
  color: #67c23a;
}

.result-message.error {
  background-color: #fef0f0;
  color: #f56c6c;
}

.result-data {
  background-color: #1e1e1e;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
}

.result-data pre {
  color: #fff;
  margin: 0;
  font-family: monospace;
}

.target-section {
  margin-top: 20px;
  padding: 20px;
  border-radius: 8px;
  background-color: #f8f9fa;
}

.target-section h4 {
  color: #333;
  margin-bottom: 15px;
}

.target-section p {
  color: #666;
  line-height: 1.6;
}

.hint {
  color: #909399;
  font-size: 0.875em;
}
</style> 