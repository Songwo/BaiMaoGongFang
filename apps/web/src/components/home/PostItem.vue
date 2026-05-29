<template>
  <el-card class="post-card" shadow="hover" @click="selectPost">
    <div class="post-content">
      <div class="post-cover" v-if="source.coverImage">
        <img v-lazy="ToUrl.url + '/' + source.coverImage" :alt="source.title" />
      </div>
      <div class="post-info">
        <div class="post-header">
          <el-tag :color="getSectionColor(source.section)" effect="dark" class="section-tag">
            {{ source.section }}
          </el-tag>
          <h3 class="post-title">{{ source.title }}</h3>
        </div>
        <p class="post-excerpt" v-html="renderMarkdown(truncateContent(source.content))"></p>
        <div class="post-meta">
          <div class="meta-item">
            <el-avatar :size="24" :src="ToUrl.url + '/' + source.avatar" />
            <span class="author">{{ source.username }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Clock /></el-icon>
            <span>{{ formatTime(source.timestamp) }}</span>
          </div>
          <div class="meta-item">
            <el-icon><ChatLineRound /></el-icon>
            <span>{{ source.replyCount }} 条回复</span>
          </div>
        </div>
        <el-button type="primary" size="small" class="detail-btn" @click.stop="openDetailDialog">
          查看详情
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import { Clock, ChatLineRound } from '@element-plus/icons-vue'
import ToUrl from '@/api/api'
import * as marked from 'marked'

const props = defineProps({
  source: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['select', 'openDetail'])

const selectPost = () => {
  emit('select', props.source)
}

const openDetailDialog = () => {
  emit('openDetail', props.source)
}

const truncateContent = (text) => {
  return text.length > 100 ? text.slice(0, 100) + '...' : text
}

const getSectionColor = (section) => {
  const colors = ['#7c3aed', '#8b5cf6', '#a78bfa', '#c4b5fd']
  const index = section.length % colors.length
  return colors[index]
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

const renderMarkdown = (content) => {
  try {
    if (!content) return ''
    return marked.marked(content, {
      breaks: true,
      gfm: true,
      sanitize: false
    })
  } catch (error) {
    console.error('Markdown rendering error:', error)
    return content
  }
}
</script>

<style lang="scss" scoped>
.post-card {
  margin-bottom: 16px;
  transition: all 0.3s;
  cursor: pointer;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  }
  
  .post-content {
    display: flex;
    gap: 20px;
    align-items: center;
    
    .post-cover {
      flex-shrink: 0;
      width: 200px;
      height: 150px;
      border-radius: 8px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s ease;
        
        &:hover {
          transform: scale(1.05);
        }
      }
    }
    
    .post-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      
      .post-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;
        
        .post-title {
          margin: 0;
          font-size: 1.2rem;
          color: #333;
        }
      }
      
      .post-excerpt {
        color: #666;
        line-height: 1.6;
        margin: 0 0 12px;
        flex: 1;
      }
      
      .post-meta {
        display: flex;
        gap: 20px;
        margin-bottom: 12px;
        align-items: center;
        
        .meta-item {
          display: flex;
          align-items: center;
          gap: 8px;
          color: #666;
          font-size: 14px;
          
          .el-avatar {
            margin-right: 4px;
          }
          
          .author {
            font-weight: 500;
            color: #333;
          }
          
          .el-icon {
            font-size: 16px;
          }
        }
      }
      
      .detail-btn {
        align-self: flex-end;
      }
    }
  }
}
</style> 