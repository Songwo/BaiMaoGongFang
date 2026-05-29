<template>
    <div class="admin-page">
      <h2>👮 管理员正在查看评论</h2>
      <p>如果评论中有恶意脚本，管理员就会中招。</p>
  
      <div class="comments-list">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="comment-item"
          v-html="comment.content"
        ></div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import axios from 'axios'
  import { ElMessage } from 'element-plus'
  
  const comments = ref([])
  
  const fetchComments = async () => {
    try {
      const res = await axios.get('http://localhost:8086/admin/visit')
      comments.value = res.data.data || []
    } catch (err) {
      ElMessage.error('加载评论失败：' + err.message)
    }
  }
  
  onMounted(() => {
    fetchComments()
  })
  </script>
  
  <style scoped>
  .admin-page {
    max-width: 800px;
    margin: 40px auto;
    padding: 20px;
    background: #f9f9f9;
    border-radius: 10px;
  }
  
  .comments-list {
    margin-top: 20px;
  }
  
  .comment-item {
    background-color: #fff;
    padding: 15px;
    margin-bottom: 10px;
    border-left: 4px solid #409EFF;
    border-radius: 6px;
    word-break: break-word;
  }
  </style>
  