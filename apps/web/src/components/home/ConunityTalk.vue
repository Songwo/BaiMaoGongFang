<template>
  <div class="forum-container">
    <!-- 头部 -->
    <el-card class="header-card">
      <div class="forum-header">
        <div class="forum-title-area">
          <h1>网络安全交流社区</h1>
          <p class="subtitle">与技术同行，与安全共生</p>
        </div>
        <div class="stats-area">
          <div class="stat-item">
            <el-icon><Document /></el-icon>
            <div class="stat-info">
              <span class="stat-count">{{ posts.length }}</span>
              <span class="stat-label">帖子总数</span>
            </div>
          </div>
          <div class="stat-item">
            <el-icon><User /></el-icon>
            <div class="stat-info">
              <span class="stat-count">{{ getUniqueAuthors() }}</span>
              <span class="stat-label">活跃用户</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 主体内容 -->
    <el-row :gutter="24" class="main-content">
      <!-- 帖子列表 -->
      <el-col :xs="24" :sm="16" class="post-column">
        <el-card shadow="hover">
          <template #header>
            <div class="filter-bar">
              <div class="filter-controls">
                <el-select v-model="filterSection" placeholder="全部板块" @change="fetchPosts">
                  <el-option label="全部板块" value="" />
                  <el-option v-for="section in forumSections" :key="section.title" :label="section.title"
                    :value="section.title" />
                </el-select>
                <el-select v-model="sortOption" placeholder="排序方式" @change="applySorting">
                  <el-option label="最新发布" value="newest" />
                  <el-option label="最多回复" value="mostReplies" />
                  <el-option label="最早发布" value="oldest" />
                </el-select>
                <el-input 
                  v-model="searchQuery" 
                  placeholder="搜索帖子" 
                  prefix-icon="Search"
                  clearable
                  @input="filterPostsDebounced"
                  class="search-input"
                />
              </div>
              <el-button v-if="store.state.token" type="primary" @click="openPostDialog">
                <el-icon class="mr-5"><Plus /></el-icon>新建帖子
              </el-button>
            </div>
          </template>

          <div v-loading="loadingPosts" class="post-list">
            <transition-group name="post-list">
              <el-card v-for="post in displayedPosts" :key="post.id" class="post-card" shadow="hover" @click="selectPost(post)">
                <div class="post-content">
                  <div class="post-cover" v-if="post.coverImage">
                    <img :src="ToUrl.url + '/' + post.coverImage" :alt="post.title" loading="lazy" />
                  </div>
                  <div class="post-info">
                    <div class="post-header">
                      <el-tag :color="getSectionColor(post.section)" effect="dark" class="section-tag">
                        {{ post.section }}
                      </el-tag>
                      <h3 class="post-title">{{ post.title }}</h3>
                    </div>
                    <p class="post-excerpt" v-html="renderMarkdown(truncateContent(post.content))"></p>
                    <div class="post-meta">
                      <div class="meta-item">
                        <el-avatar :size="24" :src="ToUrl.url + '/' + post.avatar" loading="lazy" />
                        <span class="author">{{ post.username }}</span>
                        <el-tag 
                          :type="getPostTitle(post).type" 
                          effect="dark" 
                          size="small" 
                          class="user-title"
                        >
                          {{ getPostTitle(post).name }}
                        </el-tag>
                      </div>
                      <div class="meta-item">
                        <el-icon><Clock /></el-icon>
                        <span>{{ formatTime(post.timestamp) }}</span>
                      </div>
                      <div class="meta-item">
                        <el-icon><ChatLineRound /></el-icon>
                        <span>{{ post.replyCount }} 条回复</span>
                      </div>
                    </div>
                    <el-button type="primary" size="small" class="detail-btn" @click.stop="openDetailDialog(post)">
                      查看详情
                    </el-button>
                  </div>
                </div>
              </el-card>
            </transition-group>

            <!-- 未登录用户的提示卡片 -->
            <div v-if="!store.state.token" class="more-content-prompt">
              <el-divider>
                <el-icon><InfoFilled /></el-icon>
                更多精彩内容
              </el-divider>
              <p class="prompt-text">登录后可以查看更多帖子和完整内容</p>
              <el-button type="primary" @click="router.push('/bmgf/login')">立即登录</el-button>
            </div>

            <el-pagination
              v-if="filteredPosts.length > postsPerPage"
              :current-page="currentPage"
              :page-size="postsPerPage"
              :total="filteredPosts.length"
              layout="prev, pager, next"
              @current-change="handlePageChange"
              class="pagination"
            />
            <el-empty v-if="displayedPosts.length === 0" description="暂无相关帖子" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧边栏 -->
      <el-col :xs="24" :sm="8" class="sidebar-column">
        <!-- 热门帖子 -->
        <el-card class="trending-card" shadow="hover">
          <template #header>
            <div class="trending-header">
              <h3><el-icon><Star /></el-icon> 热门帖子</h3>
            </div>
          </template>
          <div class="trending-list">
            <div v-for="(post, index) in trendingPosts" :key="post.id" class="trending-item" @click="selectPost(post)">
              <div class="trending-rank">{{ index + 1 }}</div>
              <div class="trending-content">
                <div class="trending-title">{{ post.title }}</div>
                <div class="trending-meta">
                  <span>{{ post.replyCount }} 回复</span>
                  <span>{{ formatTime(post.timestamp) }}</span>
                </div>
              </div>
            </div>
            <el-empty v-if="trendingPosts.length === 0" description="暂无热门帖子" :image-size="60" />
          </div>
        </el-card>

        <!-- 用户板块 -->
        <el-card v-if="store.state.token" class="user-card" shadow="hover">
          <template #header>
            <div class="user-header">
              <h3><el-icon><User /></el-icon> 用户信息</h3>
            </div>
          </template>
          <div class="user-info">
            <el-avatar :size="64" :src="ToUrl.url+'/'+store.state.avatar" />
            <div class="user-details">
              <div class="user-name">
                {{ store.state.user }}
                <el-tag 
                  v-if="currentTitle.name !== '未设置'" 
                  :type="currentTitle.type" 
                  effect="dark" 
                  size="small" 
                  class="user-title"
                >
                  {{ currentTitle.name }}
                </el-tag>
                <el-tag 
                  v-else 
                  type="info" 
                  effect="dark" 
                  size="small" 
                  class="user-title"
                >
                  未拥有称号
                </el-tag>
              </div>
              <div class="user-stats">
                <div class="stat">
                  <div class="stat-value">{{ userPostCount }}</div>
                  <div class="stat-label">帖子</div>
                </div>
                <div class="stat">
                  <div class="stat-value">{{ userCommentCount }}</div>
                  <div class="stat-label">评论</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <transition name="slide-fade">
          <el-card v-if="selectedPost" 
            class="detail-card" 
            shadow="hover" 
            v-loading="loadingComments"
          >
            <template #header>
              <div class="detail-header">
                <h3>帖子详情</h3>
                <el-icon class="close-icon" @click.stop="selectedPost = null"><Close /></el-icon>
              </div>
            </template>

            <!-- 帖子内容 -->
            <div class="post-detail">
              <div class="post-cover" v-if="selectedPost?.coverImage">
                <img :src="ToUrl.url + '/' + selectedPost.coverImage" :alt="selectedPost.title" loading="lazy" />
              </div>
              <div class="detail-meta">
                <el-avatar :size="32" :src="ToUrl.url + '/' + selectedPost.avatar" loading="lazy" />
                <div class="meta-info">
                  <span class="author">{{ selectedPost.username }}</span>
                  <span class="time">{{ formatTime(selectedPost.timestamp) }}</span>
                </div>
              </div>
              <el-divider />
              <div class="content-text" v-html="renderMarkdown(selectedPost.content)"></div>
            </div>

            <!-- 评论区 -->
            <div class="comment-section">
              <h4 class="comment-title">评论（{{ selectedPost.comments?.length || 0 }}）</h4>
              
              <!-- 未登录用户的评论区提示 -->
              <div v-if="!store.state.token" class="comment-login-prompt">
                <el-empty description="登录后查看完整评论">
                  <template #image>
                    <el-icon :size="48"><ChatDotSquare /></el-icon>
                  </template>
                  <template #extra>
                    <div class="prompt-actions">
                      <p class="prompt-text">参与讨论，分享你的见解</p>
                      <el-button type="primary" @click="router.push('/bmgf/login')">去登录</el-button>
                    </div>
                  </template>
                </el-empty>
              </div>

              <!-- 登录用户可见的评论列表 -->
              <div v-else>
                <div class="comment-list">
                  <el-scrollbar height="300px">
                    <div 
                      v-for="comment in selectedPost.comments" 
                      :key="comment.id" 
                      class="comment-item"
                    >
                      <el-avatar :size="32" :src="ToUrl.url+'/'+comment.avatar" loading="lazy" />
                      <div class="comment-content">
                        <div class="comment-header">
                          <span class="author">{{ comment.username }}</span>
                          <el-tag 
                            :type="getPostTitle(comment).type" 
                            effect="dark" 
                            size="small" 
                            class="user-title"
                          >
                            {{ getPostTitle(comment).name }}
                          </el-tag>
                          <span class="time">{{ formatTime(comment.timestamp) }}</span>
                        </div>
                        <p class="comment-text">{{ comment.content }}</p>
                      </div>
                    </div>
                    <el-empty 
                      v-if="!selectedPost.comments?.length" 
                      description="暂无评论" 
                      :image-size="80"
                    />
                  </el-scrollbar>
                </div>

                <!-- 评论输入框 -->
                <div class="comment-editor">
                  <el-input
                    v-model="newComment.content"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入您的评论..."
                    resize="none"
                  />
                  <div class="editor-actions">
                    <el-button 
                      type="primary" 
                      :loading="postingComment" 
                      @click="submitComment"
                    >
                      发表评论
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 空状态 -->
          <el-card v-else class="empty-card" shadow="hover">
            <el-empty description="选择帖子查看详情" />
          </el-card>
        </transition>
      </el-col>
    </el-row>

    <!-- 新建帖子对话框 -->
    <el-dialog v-model="showPostDialog" title="新建帖子" width="90%">
      <el-form :model="newPost" :rules="postRules" label-width="80px" ref="postForm">
        <el-form-item label="封面" prop="coverImage">
          <el-upload
            class="cover-uploader"
            :action="ToUrl.url + '/upload/post-cover'"
            :headers="{ 'Authorization': `Bearer ${store.state.token}` }"
            :show-file-list="false"
            :on-success="(res) => {
              newPost.coverImage = res.data; 
              console.log(newPost.coverImage);
            }"
            :before-upload="(file) => {
              const isImage = file.type.startsWith('image/')
              if (!isImage) {
                ElMessage.error('请上传图片文件')
                return false
              }
              return true
            }"
          >
            <img v-if="newPost.coverImage" :src="ToUrl.url + '/' + newPost.coverImage" class="cover-image" @click.stop="previewImage" />
            <div v-else class="cover-uploader-icon">
              <el-icon><Plus /></el-icon>
              <div class="upload-text">点击上传封面</div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="标题" required prop="title">
          <el-input v-model="newPost.title" />
        </el-form-item>
        <el-form-item label="板块" required prop="section">
          <el-select v-model="newPost.section">
            <el-option v-for="section in forumSections" :key="section.title" :label="section.title"
              :value="section.title" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" required prop="content">
          <div class="editor-container">
            <MdEditor
              v-model="mdContent"
              :toolbars="mdConfig.toolbars"
              @onUploadImg="onUploadImg"
              preview-theme="github"
              code-theme="atom-one-dark"
              :showCodeRowNumber="true"
              style="height: 500px"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" :loading="postingPost" @click="submitPost">
          立即发布
        </el-button>
      </template>
    </el-dialog>
     <!-- 帖子详情弹窗 -->
  <el-dialog 
    v-model="detailVisible" 
    width="1200px"
    class="post-detail-dialog"
    @closed="selectedPost = null"
    :show-close="true"
    :title="selectedPost?.title"
  >
    <div v-loading="loadingComments" class="dialog-content">
      <!-- 帖子内容 -->
      <div class="post-content">
        <div class="post-cover" v-if="selectedPost?.coverImage">
          <img :src="ToUrl.url + '/' + selectedPost.coverImage" :alt="selectedPost.title" loading="lazy" />
        </div>
        <div class="post-meta">
          <el-avatar :size="32" :src="ToUrl.url+'/'+selectedPost.avatar" loading="lazy" />
          <div class="meta-info">
            <span class="author">{{ selectedPost.username }}</span>
            <span class="time">{{ formatTime(selectedPost.timestamp) }}</span>
          </div>
        </div>
        <el-divider />
        <div class="content-text" v-html="renderMarkdown(selectedPost.content)"></div>
      </div>

      <!-- 评论区 -->
      <div class="comment-wrapper">
        <h4 class="comment-title">评论（{{ selectedPost.comments?.length || 0 }}）</h4>
        
        <!-- 未登录用户的评论区提示 -->
        <div v-if="!store.state.token" class="comment-login-prompt">
          <el-empty description="登录后查看完整评论">
            <template #image>
              <el-icon :size="48"><ChatDotSquare /></el-icon>
            </template>
            <template #extra>
              <div class="prompt-actions">
                <p class="prompt-text">参与讨论，分享你的见解</p>
                <el-button type="primary" @click="router.push('/bmgf/login')">去登录</el-button>
              </div>
            </template>
          </el-empty>
        </div>

        <!-- 登录用户可见的评论列表 -->
        <div v-else>
          <div class="comment-list">
            <el-scrollbar height="300px">
              <div 
                v-for="comment in selectedPost.comments" 
                :key="comment.id" 
                class="comment-item"
              >
                <el-avatar :size="32" :src="ToUrl.url+'/'+comment.avatar" loading="lazy" />
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="author">{{ comment.username }}</span>
                    <el-tag 
                      :type="getPostTitle(comment).type" 
                      effect="dark" 
                      size="small" 
                      class="user-title"
                    >
                      {{ getPostTitle(comment).name }}
                    </el-tag>
                    <span class="time">{{ formatTime(comment.timestamp) }}</span>
                  </div>
                  <p class="comment-text">{{ comment.content }}</p>
                </div>
              </div>
              <el-empty 
                v-if="!selectedPost.comments?.length" 
                description="暂无评论" 
                :image-size="80"
              />
            </el-scrollbar>
          </div>

          <!-- 评论输入框 -->
          <div class="comment-editor">
            <el-input
              v-model="newComment.content"
              type="textarea"
              :rows="3"
              placeholder="请输入您的评论..."
              resize="none"
            />
            <div class="editor-actions">
              <el-button 
                type="primary" 
                :loading="postingComment" 
                @click="submitComment"
              >
                发表评论
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watchEffect, watch } from 'vue'
import {
  ElCard, ElButton, ElSelect, ElOption, ElTag,
  ElAvatar, ElIcon, ElEmpty, ElMessage, ElPagination, ElInput
} from 'element-plus'
import { 
  Clock, ChatLineRound, Close, Star, Document, 
  User, Plus, Search, InfoFilled, ChatDotSquare
} from '@element-plus/icons-vue'
import axios from 'axios'
import ToUrl from '@/api/api'
import '@/assets/styles/forum-theme.css'
import 'md-editor-v3/lib/style.css'
import { MdEditor } from 'md-editor-v3'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'
import { ElImageViewer } from 'element-plus'
import { useRouter } from 'vue-router'
import { debounce } from 'lodash-es'
import { useStore } from 'vuex'

const router = useRouter();
const store = useStore();

// 论坛板块
const forumSections = [
  { title: '安全漏洞讨论', description: '讨论各种网络安全漏洞及解决方案' },
  { title: '网络攻击案例', description: '分享并分析网络攻击的实际案例' },
  { title: '网络工具推荐', description: '分享并讨论网络安全工具的使用心得' },
  { title: '行业动态', description: '最新网络安全行业资讯分享' }
]

// 帖子相关状态
const posts = ref([])
const selectedPost = ref(null)
const filterSection = ref('')
const loadingPosts = ref(false)
const loadingComments = ref(false)
const postForm = ref(null)
// 评论相关状态
const newComment = ref({
  content: '',
  images: [],
  code: null
})
const postingComment = ref(false)
// 新建帖子相关状态
const showPostDialog = ref(false)
const newPost = ref({
  authorId: store.state.id,
  username: store.state.user,
  title: '',
  section: '',
  content: '',
  avatar: store.state.avatar,
  coverImage: ''
})
const postingPost = ref(false)

// 新增状态
const detailVisible = ref(false)
const sortOption = ref('newest')
const searchQuery = ref('')
const currentPage = ref(1)
const postsPerPage = 5
const userPostCount = ref(0)
const userCommentCount = ref(0)

// 评论相关状态
const showCodeEditor = ref(false)
const codeLanguage = ref('javascript')

const supportedLanguages = [
  { value: 'javascript', label: 'JavaScript' },
  { value: 'python', label: 'Python' },
  { value: 'java', label: 'Java' },
  { value: 'cpp', label: 'C++' },
  { value: 'csharp', label: 'C#' },
  { value: 'php', label: 'PHP' },
  { value: 'ruby', label: 'Ruby' },
  { value: 'swift', label: 'Swift' },
  { value: 'go', label: 'Go' },
  { value: 'rust', label: 'Rust' },
  { value: 'sql', label: 'SQL' },
  { value: 'html', label: 'HTML' },
  { value: 'css', label: 'CSS' },
  { value: 'xml', label: 'XML' },
  { value: 'json', label: 'JSON' }
]

// 添加 Markdown 编辑器配置
const mdContent = ref('')
const mdConfig = {
  toolbars: [
    'bold', 'underline', 'italic', 'strikethrough', '|',
    'title', 'sub', 'sup', 'quote', 'unordered', 'ordered', '|',
    'link', 'image', 'table', 'code', 'codeBlock', '|',
    'preview', 'fullscreen', '|',
    'undo', 'redo', '|',
    'save'
  ]
}

// 配置 marked 选项
marked.setOptions({
  breaks: true,
  gfm: true,
  headerIds: false,
  mangle: false,
  sanitize: false,
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (err) {}
    }
    return hljs.highlightAuto(code).value
  }
})

// 添加缓存机制
const markdownCache = new Map()

const renderMarkdown = (content) => {
  try {
    // 检查缓存
    if (markdownCache.has(content)) {
      return markdownCache.get(content)
    }

    let processedContent = content
    if (!store.state.token && content.length > 200) {
      processedContent = content.slice(0, 200) + '\n\n**... 登录后查看完整内容**'
    }

    const rendered = marked.parse(processedContent)
    // 存入缓存
    markdownCache.set(content, rendered)
    return rendered
  } catch (error) {
    console.error('Markdown rendering error:', error)
    return content
  }
}

// 图片上传处理函数
const onUploadImg = async (files, callback) => {
  const res = []
  for (const file of files) {
    try {
      const formData = new FormData()
      formData.append('file', file)
      const response = await axios.post(ToUrl.url + '/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'Authorization': `Bearer ${store.state.token}`
        }
      })
      res.push(ToUrl.url + '/' + response.data.data)
    } catch (error) {
      ElMessage.error('图片上传失败')
    }
  }
  callback(res)
}

// 打开详情方法
const openDetailDialog = async (post) => {
  if (!store.state.token && post.content.length > 200) {
    ElMessage.warning('登录后可查看完整内容');
    router.push('/bmgf/login');
    return;
  }
  
  selectedPost.value = post;
  detailVisible.value = true;
  
  // 获取评论
  if (post.replyCount > 0) {
    loadingComments.value = true;
    try {
      const response = await axios.get(ToUrl.url + `/comments/find/${post.id}`);
      // 确保每个评论都包含正确的称号信息
      selectedPost.value.comments = (response.data.data || []).map(comment => ({
        ...comment,
        honoraryTitle: comment.authorId === store.state.id ? store.state.honoraryTitle : comment.honoraryTitle
      }));
      
      // 未登录用户只显示部分评论
      if (!store.state.token) {
        selectedPost.value.comments = selectedPost.value.comments.slice(0, 3);
      }
    } catch (error) {
      ElMessage.error('获取评论失败');
    } finally {
      loadingComments.value = false;
    }
  }
}

// 获取帖子方法增强
const fetchPosts = async () => {
  try {
    loadingPosts.value = true;
    let response;
    
    const headers = store.state.token ? { 'Authorization': `Bearer ${store.state.token}` } : {};
    
    if (filterSection.value) {
      // 按板块查询
      response = await axios.get(ToUrl.url + '/post/findBySection', {
        params: { section: filterSection.value },
        headers
      });
    } else {
      // 查询所有
      response = await axios.get(ToUrl.url + '/post/findAll', {
        headers
      });
    }
    
    // 确保每个帖子都包含用户的称号信息
    posts.value = response.data.data.map(post => ({
      ...post,
      honoraryTitle: post.authorId === store.state.id ? store.state.honoraryTitle : post.honoraryTitle
    }));
    
    // 只有登录后才获取用户统计信息
    if (store.state.token) {
      fetchUserStats();
    }
  } catch (error) {
    ElMessage.error('获取帖子列表失败');
  } finally {
    loadingPosts.value = false;
  }
}

// 获取用户统计信息
const fetchUserStats = async () => {
  try {
    const response = await axios.get(ToUrl.url + `/api/stats/${store.state.id}`, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    });
    if (response.data.code === 200) {
      const stats = response.data.data;
      userPostCount.value = stats.postCount;
      userCommentCount.value = stats.commentCount;
    }
  } catch (error) {
    console.error('获取用户统计信息失败', error);
    ElMessage.error('获取用户统计信息失败');
  }
}

// 搜索过滤
const filterPosts = async () => {
  if (!searchQuery.value.trim()) {
    fetchPosts();
    return;
  }

  try {
    loadingPosts.value = true;
    const response = await axios.get(ToUrl.url + '/post/search', {
      params: { keyword: searchQuery.value },
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    });
    posts.value = response.data.data;
  } catch (error) {
    ElMessage.error('搜索失败');
  } finally {
    loadingPosts.value = false;
  }
}

// 排序功能
const applySorting = () => {
  currentPage.value = 1
}

// 列表过滤计算属性增强
const filteredPosts = computed(() => {
  if (!posts.value || posts.value.length === 0) return []
  
  let filtered = posts.value
  
  // 板块过滤
  if (filterSection.value) {
    filtered = filtered.filter(post => post.section === filterSection.value)
  }
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(post => 
      post.title.toLowerCase().includes(query) || 
      post.content.toLowerCase().includes(query) ||
      post.username.toLowerCase().includes(query)
    )
  }
  
  // 排序
  filtered = filtered.sort((a, b) => {
    if (sortOption.value === 'newest') {
      return new Date(b.timestamp) - new Date(a.timestamp)
    } else if (sortOption.value === 'oldest') {
      return new Date(a.timestamp) - new Date(b.timestamp)
    } else if (sortOption.value === 'mostReplies') {
      return b.replyCount - a.replyCount
    }
    return 0
  })

  // 未登录用户只显示部分帖子
  if (!store.state.token) {
    filtered = filtered.slice(0, 5) // 只显示前5条帖子
  }
  
  return filtered
})

// 分页后的帖子
const displayedPosts = computed(() => {
  const startIndex = (currentPage.value - 1) * postsPerPage
  return filteredPosts.value.slice(startIndex, startIndex + postsPerPage)
})

// 获取热门帖子
const fetchHotPosts = async () => {
  try {
    const headers = store.state.token ? { 'Authorization': `Bearer ${store.state.token}` } : {};
    const response = await axios.get(ToUrl.url + '/post/hot', {
      params: { limit: 5 },
      headers
    });
    return response.data.data;
  } catch (error) {
    console.error('获取热门帖子失败', error);
    return [];
  }
}

// trendingPosts计算属性
const trendingPosts = ref([]);

// 在mounted中调用获取热门帖子
onMounted(async () => {
  fetchPosts();
  trendingPosts.value = await fetchHotPosts();
});

// 获取独立作者数
const getUniqueAuthors = () => {
  if (!posts.value || posts.value.length === 0) return 0
  const uniqueAuthors = new Set(posts.value.map(post => post.authorId))
  return uniqueAuthors.size
}

// 修改后的时间格式化方法
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

// selectPost方法
const selectPost = async (post) => {
  try {
    const headers = store.state.token ? { 'Authorization': `Bearer ${store.state.token}` } : {};
    const response = await axios.get(ToUrl.url + `/post/findById`, {
      params: { id: post.id },
      headers
    });
    selectedPost.value = response.data.data;
    
    // 确保帖子包含用户的称号信息
    selectedPost.value.honoraryTitle = selectedPost.value.authorId === store.state.id ? 
      store.state.honoraryTitle : selectedPost.value.honoraryTitle;
    
    // 获取评论
    if (post.replyCount > 0) {
      loadingComments.value = true;
      try {
        const commentsResponse = await axios.get(ToUrl.url + `/comments/find/${post.id}`, {
          headers
        });
        // 确保每个评论都包含用户的称号信息
        selectedPost.value.comments = (commentsResponse.data.data || []).map(comment => ({
          ...comment,
          honoraryTitle: comment.authorId === store.state.id ? store.state.honoraryTitle : comment.honoraryTitle
        }));
      } catch (error) {
        ElMessage.error('获取评论失败');
      } finally {
        loadingComments.value = false;
      }
    } else {
      selectedPost.value.comments = [];
    }
  } catch (error) {
    ElMessage.error('获取帖子详情失败');
  }
};

// 处理代码高亮
const highlightCode = (code, language) => {
  try {
    return hljs.highlight(code, { language }).value
  } catch (error) {
    return hljs.highlightAuto(code).value
  }
}

// 提交评论
const submitComment = async () => {
  try {
    // 检查登录状态
    if (!store.state.token) {
      ElMessage.warning('请先登录后再发表评论');
      router.push('/bmgf/login');
      return;
    }

    if (!selectedPost.value?.id) {
      ElMessage.warning('请先选择要评论的帖子')
      return
    }

    if (!newComment.value.content.trim()) {
      ElMessage.warning('评论内容不能为空')
      return
    }

    postingComment.value = true
    
    // 获取当前选中的称号
    const titleData = store.state.honoraryTitle || {}
    const selectedTitle = Object.entries(titleData).find(([_, value]) => value === 1)
    const honoraryTitle = selectedTitle ? selectedTitle[0] : '未拥有称号'
    
    const commentData = {
      authorId: store.state.id,
      username: store.state.user,
      postId: selectedPost.value.id,
      content: newComment.value.content,
      avatar: store.state.avatar,
      timestamp: new Date().toISOString(),
      honoraryTitle: store.state.honoraryTitle // 直接使用 store 中的称号数据
    }

    const res = await axios.post(ToUrl.url + '/comments/insert', commentData, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })

    if (res.data.code === 200) {
      // 更新本地数据
      if (!selectedPost.value.comments) {
        selectedPost.value.comments = []
      }
      
      // 使用返回的数据或本地构建的数据
      const commentResponse = {
        ...commentData,
        id: res.data.data?.id || Date.now().toString(), // 如果后端没有返回id，使用时间戳作为临时id
      }
      
      selectedPost.value.comments.unshift(commentResponse)
      selectedPost.value.replyCount = (selectedPost.value.replyCount || 0) + 1
      
      // 重置评论表单
      newComment.value = {
        content: '',
        images: [],
        code: null
      }
      
      // 重新获取用户统计信息
      fetchUserStats()
      
      ElMessage.success('评论发表成功')
    } else {
      throw new Error(res.data.message || '评论发表失败')
    }
  } catch (error) {
    console.error('评论发表失败:', error)
    ElMessage.error(error.message || '评论发表失败')
  } finally {
    postingComment.value = false
  }
}

// 新建帖子
const openPostDialog = () => {
  if (!store.state.token) {
    ElMessage.warning('请先登录后再发布帖子');
    router.push('/bmgf/login');
    return;
  }
  showPostDialog.value = true
}
// 在setup()中添加验证规则
const postRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 50, message: '长度在5到50个字符', trigger: 'blur' }
  ],
  section: [
    { required: true, message: '请选择板块', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 10, message: '内容至少10个字符', trigger: 'blur' }
  ]
}

// 处理封面上传
const handleCoverUpload = async (file) => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    const response = await axios.post(ToUrl.url + '/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    newPost.value.coverImage = response.data.data
    return true
  } catch (error) {
    ElMessage.error('封面上传失败')
    return false
  }
}

const handleEditorCreated = (editor) => {
  editorRef.value = editor
}

const submitPost = async () => {
  try {
    postingPost.value = true;
    
    // 检查必填字段
    if (!newPost.value.title.trim()) {
      ElMessage.warning('请输入标题');
      return;
    }
    
    if (!newPost.value.section) {
      ElMessage.warning('请选择板块');
      return;
    }
    
    if (!mdContent.value.trim()) {
      ElMessage.warning('请输入内容');
      return;
    }
    
    // 使用 Markdown 内容
    newPost.value.content = mdContent.value;
    
    const res = await axios.post(ToUrl.url + '/post/insertPost', newPost.value, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    });
    
    if (res.data.code === 200) {
      const newPostData = {
        ...res.data.data,
        comments: []
      };
      posts.value.unshift(newPostData);
      selectPost(newPostData);
      showPostDialog.value = false;
      
      // 重置表单
      newPost.value = { 
        title: '', 
        section: '', 
        content: '',
        coverImage: '',
        authorId: store.state.id,
        username: store.state.user,
        avatar: store.state.avatar
      };
      mdContent.value = '';
      
      // 重新获取用户统计信息
      fetchUserStats();
      
      ElMessage.success('帖子发布成功');
    } else {
      throw new Error(res.data.message || '发布失败');
    }
  } catch (error) {
    console.error('发布失败:', error);
    ElMessage.error(error.message || '发布失败，请检查网络连接');
  } finally {
    postingPost.value = false;
  }
}

// 工具函数
const truncateContent = (text) => {
  return text.length > 100 ? text.slice(0, 100) + '...' : text
}

const getSectionColor = (section) => {
  const index = forumSections.findIndex(s => s.title === section)
  const colors = ['#7c3aed', '#8b5cf6', '#a78bfa', '#c4b5fd']
  return colors[index % colors.length]
}

// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// previewImage 方法
const previewImage = () => {
  if (newPost.value.coverImage) {
    const imageUrl = ToUrl.url + '/' + newPost.value.coverImage;
    ElImageViewer.createVNode({
      urlList: [imageUrl],
      initialIndex: 0
    });
  }
}

onMounted(() => {
  fetchPosts()
})

const filterPostsDebounced = debounce(filterPosts, 300)

// 称号相关的响应式数据
const currentTitle = computed(() => {
  const titleData = store.state.honoraryTitle || {}
  const selectedTitle = Object.entries(titleData).find(([_, value]) => value === 1)
  
  if (selectedTitle) {
    const typeMap = {
      '安全新秀': 'success',
      '漏洞侦探': 'warning',
      '攻防入门者': 'danger',
      '中级分析师': 'info',
      '加密解码手': 'success',
      '脚本小能手': 'warning',
      '红队先锋': 'danger',
      '蓝队守护者': 'info',
      '逆向专家': 'success',
      '网络安全大师': 'danger',
      '新星白帽': 'success',
      '安全先锋': 'warning',
      '攻防大师': 'danger',
      '漏洞猎人': 'info',
      '渗透之眼': 'info'
    }
    return {
      id: selectedTitle[0],
      name: selectedTitle[0],
      type: typeMap[selectedTitle[0]] || 'info',
      description: '称号描述',
      requirement: '称号要求'
    }
  }
  return {
    id: '',
    name: '未设置',
    type: 'info',
    description: '',
    requirement: ''
  }
})

const getTitleType = (honoraryTitle) => {
  if (!honoraryTitle) return 'info'
  
  const selectedTitle = Object.entries(honoraryTitle).find(([_, value]) => value === 1)
  if (!selectedTitle) return 'info'
  
  const typeMap = {
    '安全新秀': 'success',
    '漏洞侦探': 'warning',
    '攻防入门者': 'danger',
    '中级分析师': 'info',
    '加密解码手': 'success',
    '脚本小能手': 'warning',
    '红队先锋': 'danger',
    '蓝队守护者': 'info',
    '逆向专家': 'success',
    '网络安全大师': 'danger'
  }
  return typeMap[selectedTitle[0]] || 'info'
}

// 添加帖子称号计算属性
const getPostTitle = (post) => {
  const titleData = post.honoraryTitle || {}
  const selectedTitle = Object.entries(titleData).find(([_, value]) => value === 1)
  
  if (selectedTitle) {
    const typeMap = {
      '安全新秀': 'success',
      '漏洞侦探': 'warning',
      '攻防入门者': 'danger',
      '中级分析师': 'info',
      '加密解码手': 'success',
      '脚本小能手': 'warning',
      '红队先锋': 'danger',
      '蓝队守护者': 'info',
      '逆向专家': 'success',
      '网络安全大师': 'danger'
    }
    return {
      name: selectedTitle[0],
      type: typeMap[selectedTitle[0]] || 'info'
    }
  }
  return {
    name: '未拥有称号',
    type: 'info'
  }
}

// 添加评论称号计算属性
const getCommentTitle = (comment) => {
  const titleData = comment.honoraryTitle || {}
  const selectedTitle = Object.entries(titleData).find(([_, value]) => value === 1)
  
  if (selectedTitle) {
    const typeMap = {
      '安全新秀': 'success',
      '漏洞侦探': 'warning',
      '攻防入门者': 'danger',
      '中级分析师': 'info',
      '加密解码手': 'success',
      '脚本小能手': 'warning',
      '红队先锋': 'danger',
      '蓝队守护者': 'info',
      '逆向专家': 'success',
      '网络安全大师': 'danger'
    }
    return {
      name: selectedTitle[0],
      type: typeMap[selectedTitle[0]] || 'info'
    }
  }
  return {
    name: '未拥有称号',
    type: 'info'
  }
}
</script>

<style lang="scss" scoped>
.forum-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;

  .header-card {
    margin-bottom: 24px;
    background: linear-gradient(135deg, #7c3aed, #b5a0f4);
    border: none;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

    :deep(.el-card__header) {
      padding: 30px 40px;
    }

    .forum-header {
      color: white;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .forum-title-area {
        h1 {
          margin: 0;
          font-size: 2.2rem;
          text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.2);
        }

        .subtitle {
          margin: 10px 0 0;
          opacity: 0.9;
          font-size: 1.1rem;
        }
      }

      .stats-area {
        display: flex;
        gap: 24px;
        
        .stat-item {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .el-icon {
            font-size: 2rem;
            opacity: 0.9;
          }
          
          .stat-info {
            display: flex;
            flex-direction: column;
            
            .stat-count {
              font-size: 1.5rem;
              font-weight: bold;
            }
            
            .stat-label {
              font-size: 0.9rem;
              opacity: 0.9;
            }
          }
        }
      }
    }
  }

  .main-content {
    margin-top: 20px;

    .post-column {
      margin-bottom: 24px;
    }

    .filter-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      flex-wrap: wrap;
      gap: 12px;
      
      .filter-controls {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
        
        .el-select {
          width: 140px;
        }
        
        .search-input {
          width: 200px;
        }
      }
    }
  }

  .post-list {
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
              
              .user-title {
                margin-left: 4px;
                font-size: 12px;
                height: 20px;
                line-height: 18px;
                padding: 0 6px;
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
    
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
  }
  
  .trending-card, .user-card {
    margin-bottom: 20px;
    border-radius: 8px;
    overflow: hidden;
    
    .trending-header, .user-header {
      display: flex;
      align-items: center;
      
      h3 {
        margin: 0;
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 1.1rem;
      }
    }
    
    .trending-list {
      .trending-item {
        display: flex;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        transition: all 0.2s;
        
        &:hover {
          background-color: #f9f9f9;
        }
        
        &:last-child {
          border-bottom: none;
        }
        
        .trending-rank {
          flex-shrink: 0;
          width: 24px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: #7c3aed;
          color: white;
          border-radius: 50%;
          font-weight: bold;
          font-size: 0.8rem;
          margin-right: 12px;
        }
        
        .trending-content {
          flex: 1;
          min-width: 0;
          
          .trending-title {
            font-weight: 500;
            margin-bottom: 4px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          
          .trending-meta {
            display: flex;
            justify-content: space-between;
            font-size: 0.8rem;
            color: #999;
          }
        }
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      padding: 16px 0;
      
      .el-avatar {
        border: 3px solid #f0f0f0;
        margin-right: 16px;
      }
      
      .user-details {
        flex: 1;
        
        .user-name {
          font-size: 1.2rem;
          font-weight: 600;
          margin-bottom: 8px;
        }
        
        .user-stats {
          display: flex;
          gap: 24px;
          
          .stat {
            text-align: center;
            
            .stat-value {
              font-size: 1.1rem;
              font-weight: 600;
              color: #7c3aed;
            }
            
            .stat-label {
              font-size: 0.8rem;
              color: #999;
            }
          }
        }
      }
    }
  }

  .detail-card {
    position: sticky;
    top: 20px;
    max-height: calc(100vh - 120px);
    overflow-y: auto;

    :deep(.el-card__header) {
      padding: 15px 20px;
      border-bottom: 1px solid #eee;
    }

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }

      .close-icon {
        font-size: 18px;
        color: #666;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          color: #7c3aed;
          transform: scale(1.1);
        }
      }
    }

    .post-detail {
      .post-cover {
        margin-bottom: 16px;
        border-radius: 8px;
        overflow: hidden;
        height: 200px;

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
      .detail-title {
        color: #333333 !important;
        margin: 0 0 16px;
      }

      .detail-meta {
        display: flex;
        align-items: center;
        gap: 12px;

        .meta-info {
          display: flex;
          flex-direction: column;

          .author {
            font-weight: 500;
          }

          .time {
            color: #999;
            font-size: 0.9em;
          }
        }
      }

      .detail-content {
        line-height: 1.8;
        color: #333333;
        
        :deep(h1, h2, h3, h4, h5, h6) {
          margin: 1.5em 0 0.5em;
          font-weight: 600;
          line-height: 1.25;
        }
        
        :deep(p) {
          margin: 1em 0;
        }
        
        :deep(ul, ol) {
          padding-left: 2em;
          margin: 1em 0;
        }
        
        :deep(code) {
          background-color: #f6f8fa;
          padding: 0.2em 0.4em;
          border-radius: 3px;
          font-family: 'Fira Code', monospace;
        }
        
        :deep(pre) {
          background-color: #282c34;
          padding: 16px;
          border-radius: 6px;
          overflow: auto;
          
          code {
            background-color: transparent;
            padding: 0;
            color: #abb2bf;
          }
        }
        
        :deep(blockquote) {
          margin: 1em 0;
          padding: 0 1em;
          color: #666;
          border-left: 0.25em solid #dfe2e5;
        }
        
        :deep(table) {
          border-collapse: collapse;
          width: 100%;
          margin: 1em 0;
          
          th, td {
            border: 1px solid #dfe2e5;
            padding: 6px 13px;
          }
          
          th {
            background-color: #f6f8fa;
          }
        }
        
        :deep(img) {
          max-width: 100%;
          border-radius: 4px;
          margin: 1em 0;
        }
      }
    }

    .comment-section {
      margin-top: 32px;

      .comment-title {
        color: #333333 !important;
        margin-bottom: 20px;
      }

      .comment-list {
        .comment-item {
          display: flex;
          gap: 12px;
          padding: 16px 0;
          border-bottom: 1px solid #eee;

          .comment-content {
            flex: 1;

            .comment-header {
              display: flex;
              gap: 8px;
              align-items: center;
              margin-bottom: 4px;

              .author {
                font-weight: 500;
                color: #333333 !important;
              }

              .user-title {
                font-size: 12px;
                height: 20px;
                line-height: 18px;
                padding: 0 6px;
              }

              .time {
                color: #999;
                font-size: 0.8em;
              }
            }

            .comment-body {
              .comment-text {
                margin: 0;
                color: #333333 !important;
                line-height: 1.6;
              }

              .comment-images {
                display: flex;
                flex-wrap: wrap;
                gap: 8px;
                margin-bottom: 12px;

                .comment-image {
                  width: 120px;
                  height: 120px;
                  border-radius: 4px;
                  cursor: pointer;
                }
              }

              .code-block {
                margin: 12px 0;
                background: #282c34;
                border-radius: 6px;
                overflow: hidden;

                .code-header {
                  padding: 8px 16px;
                  background: rgba(255, 255, 255, 0.05);
                  
                  .language {
                    color: #abb2bf;
                    font-size: 0.9em;
                    text-transform: uppercase;
                  }
                }

                pre {
                  margin: 0;
                  padding: 16px;
                  
                  code {
                    font-family: 'Fira Code', monospace;
                    font-size: 0.9em;
                    line-height: 1.5;
                  }
                }
              }
            }
          }
        }
      }

      .comment-editor {
        margin-top: 24px;

        .editor-toolbar {
          margin: 12px 0;
          display: flex;
          gap: 12px;
          align-items: center;
        }

        .code-editor {
          margin-top: 12px;
          border: 1px solid #eee;
          border-radius: 4px;
          padding: 12px;

          .code-toolbar {
            margin-bottom: 12px;
          }
        }

        .editor-actions {
          margin-top: 12px;
          text-align: right;
        }
      }
    }
  }

  .empty-card {
    height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.post-detail-dialog {
  :deep(.el-dialog__header) {
    padding: 20px;
    border-bottom: 1px solid #eee;
    margin-right: 0;
  }

  :deep(.el-dialog__title) {
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }

  :deep(.el-dialog__headerbtn) {
    top: 20px;
  }

  :deep(.el-button--default) {
    color: #333333 !important;
  }

  .dialog-content {
    display: flex;
    flex-direction: column;
    height: 70vh;
    background-color: #ffffff;
    overflow-y: auto;
    padding: 20px;
  }

  .post-content {
    .post-cover {
      margin: -20px -20px 20px;
      height: 300px;
      border-radius: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    .post-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;

      .meta-info {
        display: flex;
        flex-direction: column;
        
        .author {
          font-weight: 500;
          color: #333333 !important;
        }
        
        .time {
          color: #999;
          font-size: 0.9em;
        }
      }
    }

    .content-text {
      line-height: 1.8;
      color: #333333;
      
      :deep(h1, h2, h3, h4, h5, h6) {
        margin: 1.5em 0 0.5em;
        font-weight: 600;
        line-height: 1.25;
      }
      
      :deep(p) {
        margin: 1em 0;
      }
      
      :deep(ul, ol) {
        padding-left: 2em;
        margin: 1em 0;
      }
      
      :deep(code) {
        background-color: #f6f8fa;
        padding: 0.2em 0.4em;
        border-radius: 3px;
        font-family: 'Fira Code', monospace;
      }
      
      :deep(pre) {
        background-color: #282c34;
        padding: 16px;
        border-radius: 6px;
        overflow: auto;
        
        code {
          background-color: transparent;
          padding: 0;
          color: #abb2bf;
        }
      }
      
      :deep(blockquote) {
        margin: 1em 0;
        padding: 0 1em;
        color: #666;
        border-left: 0.25em solid #dfe2e5;
      }
      
      :deep(table) {
        border-collapse: collapse;
        width: 100%;
        margin: 1em 0;
        
        th, td {
          border: 1px solid #dfe2e5;
          padding: 6px 13px;
        }
        
        th {
          background-color: #f6f8fa;
        }
      }
      
      :deep(img) {
        max-width: 100%;
        border-radius: 4px;
        margin: 1em 0;
      }
    }
  }

  .comment-wrapper {
    margin-top: 24px;
    flex: 1;
    display: flex;
    flex-direction: column;

    .comment-title {
      color: #333333 !important;
      margin: 0 0 16px;
    }

    .comment-list {
      flex: 1;
      margin-bottom: 20px;

      .comment-item {
        display: flex;
        gap: 12px;
        padding: 12px 0;
        border-bottom: 1px solid #eee;

        &:last-child {
          border-bottom: none;
        }

        .comment-content {
          flex: 1;

          .comment-header {
            display: flex;
            gap: 8px;
            align-items: center;
            margin-bottom: 4px;

            .author {
              font-weight: 500;
              color: #333333 !important;
            }

            .user-title {
              font-size: 12px;
              height: 20px;
              line-height: 18px;
              padding: 0 6px;
            }

            .time {
              color: #999;
              font-size: 0.8em;
            }
          }

          .comment-body {
            .comment-text {
              margin: 0;
              color: #333333 !important;
              line-height: 1.6;
            }

            .comment-images {
              display: flex;
              flex-wrap: wrap;
              gap: 8px;
              margin-bottom: 12px;

              .comment-image {
                width: 120px;
                height: 120px;
                border-radius: 4px;
                cursor: pointer;
              }
            }

            .code-block {
              margin: 12px 0;
              background: #282c34;
              border-radius: 6px;
              overflow: hidden;

              .code-header {
                padding: 8px 16px;
                background: rgba(255, 255, 255, 0.05);
                
                .language {
                  color: #abb2bf;
                  font-size: 0.9em;
                  text-transform: uppercase;
                }
              }

              pre {
                margin: 0;
                padding: 16px;
                
                code {
                  font-family: 'Fira Code', monospace;
                  font-size: 0.9em;
                  line-height: 1.5;
                }
              }
            }
          }
        }
      }
    }

    .comment-editor {
      border-top: 1px solid #eee;
      padding-top: 16px;

      .editor-actions {
        margin-top: 12px;
        text-align: right;
      }
    }
  }
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;

  .detail-btn {
    margin-left: auto;
    padding: 6px 12px;
    font-weight: 500;
  }
}

// 添加弹窗动画
:deep(.el-dialog) {
  transition: all 0.3s ease;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #eee;
}

// 过渡动画
.post-list-enter-active,
.post-list-leave-active {
  transition: all 0.3s ease;
}

.post-list-enter-from,
.post-list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}

// 覆盖Element主题色
:deep(.el-button--primary) {
  background-color: #7c3aed !important;
  border-color: #7c3aed !important;
  color: #ffffff !important;
  font-weight: 500;

  &:hover {
    background-color: #8b5cf6 !important;
    border-color: #8b5cf6 !important;
    color: #ffffff !important;
  }
}

:deep(.el-button--text) {
  color: #7c3aed !important;
  font-weight: 500;

  &:hover {
    color: #8b5cf6 !important;
  }
}

:deep(.el-button--info) {
  color: #7c3aed !important;
  font-weight: 500;

  &:hover {
    color: #8b5cf6 !important;
  }
}

:deep(.el-button--default) {
  --el-button-text-color: #333333 !important;
  --el-button-border-color: #7c3aed !important;
  --el-button-bg-color: #ffffff !important;
  --el-button-hover-text-color: #333333 !important;
  --el-button-hover-border-color: #8b5cf6 !important;
  --el-button-hover-bg-color: #f5f3ff !important;
  font-weight: 500;
}

.editor-actions {
  margin-top: 12px;
  text-align: right;

  .el-button {
    font-weight: 500;
  }
}

.cover-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    width: 300px;
    height: 180px;
    display: flex;
    justify-content: center;
    align-items: center;
    
    &:hover {
      border-color: #7c3aed;
    }
  }
  
  .cover-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .cover-uploader-icon {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #8c939d;
    
    .el-icon {
      font-size: 28px;
      margin-bottom: 8px;
    }
    
    .upload-text {
      font-size: 14px;
    }
  }
}

.cover-preview {
  margin-top: 12px;
  text-align: center;
  
  .preview-image {
    width: 300px;
    height: 180px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: scale(1.02);
    }
  }
  
  .preview-tip {
    margin-top: 8px;
    color: #666;
    font-size: 12px;
  }
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  width: 100%;

  :deep(.md-editor) {
    border: none;
    width: 100%;
  }

  :deep(.md-editor-preview) {
    background-color: #fff;
  }

  :deep(.md-editor-fullscreen) {
    position: fixed;
    top: 60px; /* 导航栏高度 */
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 2000;
    background: #fff;
  }
}

.content-text {
  line-height: 1.8;
  color: #333333;
  
  :deep(h1, h2, h3, h4, h5, h6) {
    margin: 1.5em 0 0.5em;
    font-weight: 600;
    line-height: 1.25;
  }
  
  :deep(p) {
    margin: 1em 0;
  }
  
  :deep(ul, ol) {
    padding-left: 2em;
    margin: 1em 0;
  }
  
  :deep(code) {
    background-color: #f6f8fa;
    padding: 0.2em 0.4em;
    border-radius: 3px;
    font-family: 'Fira Code', monospace;
  }
  
  :deep(pre) {
    background-color: #282c34;
    padding: 16px;
    border-radius: 6px;
    overflow: auto;
    
    code {
      background-color: transparent;
      padding: 0;
      color: #abb2bf;
    }
  }
  
  :deep(blockquote) {
    margin: 1em 0;
    padding: 0 1em;
    color: #666;
    border-left: 0.25em solid #dfe2e5;
  }
  
  :deep(table) {
    border-collapse: collapse;
    width: 100%;
    margin: 1em 0;
    
    th, td {
      border: 1px solid #dfe2e5;
      padding: 6px 13px;
    }
    
    th {
      background-color: #f6f8fa;
    }
  }
  
  :deep(img) {
    max-width: 100%;
    border-radius: 4px;
    margin: 1em 0;
  }
}

.login-prompt-card {
  grid-column: 1 / -1;
  margin-top: 1rem;
  padding: 2rem;
  text-align: center;
  background: linear-gradient(to bottom, #fefefe, #f8f9fa);
  border: 1px dashed #dfe6ee;
}

.login-prompt {
  margin-top: 1.5rem;
  padding: 2rem;
  text-align: center;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px dashed #dfe6ee;
}

:deep(.el-empty__description) {
  margin-top: 0.5rem;
  color: #666;
  font-size: 0.95rem;
}

:deep(.el-empty__bottom) {
  margin-top: 1rem;
}

.more-content-prompt {
  grid-column: 1 / -1;
  text-align: center;
  padding: 2rem 1rem;
  margin-top: 1rem;
  background: linear-gradient(to bottom, #fefefe, #f8f9fa);
  border: 1px dashed #dfe6ee;
  border-radius: 8px;

  .el-divider {
    margin: 1rem 0;
    
    :deep(.el-divider__text) {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #666;
      font-size: 1.1rem;
      font-weight: 600;
      
      .el-icon {
        font-size: 1.3rem;
        color: #409EFF;
      }
    }
  }

  .prompt-text {
    color: #409EFF;
    margin: 1rem 0;
    font-size: 1.1rem;
    font-weight: 500;
    background: linear-gradient(to right, #409EFF, #67C23A);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: shimmer 2s infinite;
    text-shadow: 0 0 1px rgba(0,0,0,0.1);
  }
}

.comment-login-prompt {
  padding: 3rem 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px dashed #dfe6ee;

  :deep(.el-empty__image) {
    .el-icon {
      color: #409EFF;
      font-size: 3rem;
    }
  }

  :deep(.el-empty__description) {
    color: #409EFF;
    font-size: 1.1rem;
    font-weight: 500;
    margin: 1rem 0;
  }

  .prompt-actions {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;

    .prompt-text {
      color: #409EFF;
      font-size: 1rem;
      font-weight: 500;
      margin: 0;
      background: linear-gradient(to right, #409EFF, #67C23A);
      -webkit-background-clip: text;
      background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }
}

@keyframes shimmer {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.8;
  }
  100% {
    opacity: 1;
  }
}

// 修改帖子内容中的登录提示样式
:deep(.content-text) {
  p:last-child {
    strong {
      display: block;
      margin-top: 1rem;
      padding: 0.5rem;
      color: #409EFF;
      font-size: 1.1rem;
      text-align: center;
      background: #ecf5ff;
      border-radius: 4px;
      border: 1px dashed #409EFF;
      animation: shimmer 2s infinite;
    }
  }
}
</style>