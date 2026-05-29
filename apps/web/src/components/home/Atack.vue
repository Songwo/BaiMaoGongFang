<template>
  <div class="video-container">
    <!-- Header with title and search bar -->
    <div class="header">
      <h2>视频探索</h2>
      <el-input
        v-model="searchQuery"
        placeholder="搜索视频标题或描述..."
        clearable
        @clear="handleSearch"
        @input="handleSearch"
        prefix-icon="el-icon-search"
        class="search-input"
      />
    </div>
    <!-- Main content layout -->
    <el-row :gutter="24">
      <!-- Left category menu -->
      <el-col :span="4" class="category-column">
        <div class="category-menu-container">
          <h3 class="category-title">分类浏览</h3>
          <el-menu
            :default-active="activeCategory"
            @select="handleCategoryChange"
            class="category-menu"
            background-color="transparent"
            text-color="#f0f0f0"
            active-text-color="#409EFF"
          >
            <el-menu-item index="all">
              <el-icon><Grid /></el-icon>
              <span>全部视频</span>
            </el-menu-item>
            <el-menu-item
              v-for="category in categories"
              :key="category"
              :index="category"
            >
              <el-icon><Folder /></el-icon>
              <span>{{ category }}</span>
            </el-menu-item>
             <el-empty v-if="!categories || categories.length === 0" description="暂无分类" :image-size="50"></el-empty>
          </el-menu>
        </div>
      </el-col>

      <!-- Right video list -->
      <el-col :span="20" class="video-list-column">
        <!-- Video card grid -->
        <el-row :gutter="20">
          <el-col
            v-if="filteredVideos.length > 0"
            v-for="video in filteredVideos"
            :key="video.id"
            :xs="24" :sm="12" :md="8" :lg="6"
            class="video-item-col"
          >
            <el-card
              :body-style="{ padding: '0px' }"
              shadow="hover"
              class="video-card"
            >
              <div class="video-thumbnail">
                <video
                  controls
                  :poster="getVideoPoster(video)"
                  class="video-player"
                  preload="metadata"
                  @error="handleVideoError"
                >
                  <source :src="getVideoUrl(video)" type="video/mp4">
                  您的浏览器不支持 HTML5 video 标签。
                </video>
                 <!-- <div class=\"play-overlay\"><el-icon><VideoPlay /></el-icon></div> -->
              </div>
              <div class="video-details">
                <h4 class="video-title" :title="video.title">{{ video.title }}</h4>
                <p class="video-description">{{ video.description || '暂无描述' }}</p>
                <div class="video-info">
                  <el-tag size="small" type="info">{{ video.categories || '未分类' }}</el-tag>
                  <span class="video-meta"></span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col v-else class="full-width-col">
             <el-empty description="暂无相关视频"></el-empty>
          </el-col>
        </el-row>

        <!-- Pagination component -->
        <div class="pagination-container" v-if="pagination.total > pagination.size">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[8, 12, 16]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
            hide-on-single-page
          />
        </div>
      </el-col>
    </el-row>

    <!-- Related Videos Section -->
    <div class="related-videos-section">
      <h3 class="section-title">推荐视频</h3>
      <el-row :gutter="20">
        <el-col
          v-if="relatedVideos.length > 0"
          v-for="relatedVideo in relatedVideos"
          :key="'related-' + relatedVideo.id"
          :xs="24" :sm="12" :md="8" :lg="6"
          class="video-item-col"
        >
          <el-card
            :body-style="{ padding: '0px' }"
            shadow="hover"
            class="video-card related-card"
          >
            <div class="video-thumbnail">
              <video
                controls
                :poster="getVideoPoster(relatedVideo)"
                class="video-player"
                preload="metadata"
                 @error="handleVideoError"
              >
                <source :src="getVideoUrl(relatedVideo)" type="video/mp4">
                您的浏览器不支持 HTML5 video 标签。
              </video>
            </div>
            <div class="video-details">
              <h4 class="video-title" :title="relatedVideo.title">{{ relatedVideo.title }}</h4>
            </div>
          </el-card>
        </el-col>
        <el-col v-else class="full-width-col">
          <el-empty description="暂无推荐视频"></el-empty>
        </el-col>
      </el-row>
    </div>

    <!-- Introduction Section -->
    <div class="introduction-section">
      <h3 class="section-title intro-title">关于本站</h3>
      <p>欢迎访问我们的网络安全视频中心。这里汇集了关于网络攻防技术、安全意识培养、最新漏洞分析及行业动态的专业视频资源。我们致力于为您提供前沿、实用的网络安全知识，共同构建更安全的网络空间。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElEmpty } from 'element-plus' // Import ElEmpty
import { Grid, Folder, VideoPlay } from '@element-plus/icons-vue'
import axios from 'axios'
import store from '@/store' // 假设使用 Vuex store 获取 token
import ToUrl from '@/api/api' // 假设这里导出包含基础 URL 的对象

// 导入封面图片
import poster1 from '@/assets/VedioFm/poster1.jpg'
import poster2 from '@/assets/VedioFm/poster2.jpg'
import poster3 from '@/assets/VedioFm/poster3.jpg'
import poster4 from '@/assets/VedioFm/poster4.jpg'
import poster5 from '@/assets/VedioFm/poster5.jpg'

// --- 配置 ---
const DEFAULT_POSTERS = [
  poster1,
  poster2,
  poster3,
  poster4,
  poster5
]; // 定义默认封面图数组
const DEFAULT_POSTER = poster1; // 使用导入的图片作为默认封面
const BASE_AVATAR_URL = ToUrl.url ? ToUrl.url + '/avatar' : ''; // 封面图基础 URL

// --- 响应式数据 ---
const activeCategory = ref('all')
const categories = ref([]) // 从 API 获取的实际分类
const videoList = ref([])
const searchQuery = ref('')
const relatedVideos = ref([]) // 推荐视频数据

// --- 分页 ---
const pagination = reactive({
  current: 1,
  size: 8, // 默认每页数量
  total: 0
})

// --- 计算属性 ---
// 基于搜索查询的客户端过滤
const filteredVideos = computed(() => {
  // 确保 videoList 是数组
  const list = Array.isArray(videoList.value) ? videoList.value : [];
  if (!searchQuery.value) {
    return list;
  }
  const lowerCaseQuery = searchQuery.value.toLowerCase();
  return list.filter(video =>
    (video && video.title && video.title.toLowerCase().includes(lowerCaseQuery)) ||
    (video && video.description && video.description.toLowerCase().includes(lowerCaseQuery))
  );
});

const getVideoPoster = (video) => {
  if (!video) return DEFAULT_POSTER;

  const imagePath = video.imageurl || '';
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://') || !BASE_AVATAR_URL) {
      return imagePath || getRandomPoster(); // 如果没有指定封面，使用随机封面
  }
  return `${BASE_AVATAR_URL}${imagePath.startsWith('/') ? imagePath : '/' + imagePath}`; // 拼接 Base URL
};

const getRandomPoster = () => {
  const randomIndex = Math.floor(Math.random() * DEFAULT_POSTERS.length);
  return DEFAULT_POSTERS[randomIndex];
};

const getVideoUrl = (video) => {
  return video?.url || '#'; // 如果 URL 不存在，返回 '#'
};

const handleVideoError = (event) => {
    console.error("视频加载错误:", event.target.error);
    ElMessage.warning('部分视频资源加载失败');
};


// --- API 调用 ---
const loadVideos = async () => {
  // console.log(`加载视频: 分类=${activeCategory.value}, 页码=${pagination.current}, 每页=${pagination.size}`);
  try {
    const endpoint = activeCategory.value !== 'all'
      ? '/user/video_Category' // 特定分类接口
      : '/user/videos';       // 所有视频接口

    const params = {
      page: pagination.current,
      size: pagination.size,
      // 仅在分类不是 'all' 时发送 category 参数
      ...(activeCategory.value !== 'all' && { category: activeCategory.value })
    };

    // 获取 token
    const token = store.state.token || '';
    if (!token) {
        ElMessage.error('用户未登录或 Token 无效');
        videoList.value = [];
        pagination.total = 0;
        return; // 如果没有 token，则不继续请求
    }

    const { data } = await axios.get(ToUrl.url + endpoint, {
      params,
      headers: {
        'Authorization': `Bearer ${token}`,
      }
    });

    if (data && data.content && Array.isArray(data.content)) {
      // 确保每个视频对象都包含必要的属性
      videoList.value = data.content.map(v => ({
        id: v?.id ?? Math.random(), // 如果 id 不存在，提供一个随机值作为 key
        title: v?.title || '无标题',
        description: v?.description || '',
        url: v?.url, // 确保这是正确的字段名
        imageurl: v?.imageurl, // 确保这是正确的字段名
        categories: v?.categories || '未分类',
      }));
      pagination.total = data.totalElements || 0;
       // console.log(`视频加载成功: ${videoList.value.length} 条, 总计: ${pagination.total}`);
    } else {
      console.warn('视频响应中未找到内容或格式不正确:', data);
      videoList.value = [];
      pagination.total = 0;
    }
  } catch (error) {
    console.error('视频加载失败:', error);
    const errorMsg = error.response?.data?.message || error.message || '未知网络错误';
    ElMessage.error(`视频加载失败: ${errorMsg}`);
    videoList.value = []; // 出错时清空列表
    pagination.total = 0;
  }
}

const loadCategories = async () => {
  try {
    // 获取 token
    const token = store.state.token || '';
     if (!token) {
        console.warn('无法加载分类：用户未登录');
        categories.value = [];
        return; // 如果没有 token，则不继续请求
    }
    const { data } = await axios.get(ToUrl.url + '/user/get_category', {
      headers: {
        'Authorization': `Bearer ${token}`,
      }
    });
    // 检查 data 和 data.data 是否存在且格式符合预期
    if (data && data.data) {
       categories.value = processCategories(data.data);
       // console.log('分类加载成功:', categories.value);
    } else {
      console.warn('分类数据为空或格式不正确:', data);
      categories.value = [];
    }
  } catch (error) {
     console.error('分类加载失败:', error);
     const errorMsg = error.response?.data?.message || error.message || '未知网络错误';
     ElMessage.error(`分类加载失败: ${errorMsg}`);
     categories.value = []; // 出错时清空分类
  }
}

// 处理分类数据 
const processCategories = (data) => {
  if (!Array.isArray(data) || data.length === 0) {
    return [];
  }
   const firstItem = data[0];
   if (typeof firstItem === 'object' && firstItem !== null) {
       // 过滤掉 'id' 或其他非分类字段
       return Object.keys(firstItem)
                  .filter(key => key !== 'id' /* 在此添加其他需要排除的字段 */)
                  .map(key => firstItem[key])
                  .filter(value => typeof value === 'string' && value.trim() !== ''); // 确保是有效字符串
   }
   return []; // 如果格式不符，返回空数组
};


// --- 事件处理 ---
const handleSearch = () => {

  // console.log('触发搜索:', searchQuery.value);
  // 当搜索查询改变时，重置分页到第一页
  pagination.current = 1;
};

const handleCategoryChange = (selectedCategory) => {
  // console.log('分类更改为:', selectedCategory);
  if (activeCategory.value === selectedCategory) return; // 防止重复加载
  activeCategory.value = selectedCategory;
  pagination.current = 1; // 切换分类时重置到第一页
  loadVideos(); // 加载新分类的视频
}

const handleSizeChange = (newSize) => {
  // console.log('每页显示数量更改为:', newSize);
  pagination.size = newSize;
  pagination.current = 1; // 更改每页数量时重置到第一页
  loadVideos();
}

const handleCurrentChange = (newPage) => {
  // console.log('页码更改为:', newPage);
  pagination.current = newPage;
  loadVideos();
}

// --- 生命周期钩子 ---
onMounted(() => {
  // console.log('Atack 组件已挂载');
  // 加载初始数据
  loadCategories();
  loadVideos();
});

// 监听 videoList 变化，更新推荐视频
watch(videoList, (newVideoList) => {
  if (Array.isArray(newVideoList) && newVideoList.length > 0) {
    // 克隆并打乱数组顺序
    const shuffled = [...newVideoList].sort(() => 0.5 - Math.random());
    // 最多选取 4 个，或者数组本身的长度（如果不足 4 个）
    const count = Math.min(4, shuffled.length);
    relatedVideos.value = shuffled.slice(0, count);
    // console.log(`推荐视频已从当前列表更新: ${relatedVideos.value.length} 条`);
  } else {
    relatedVideos.value = []; // 如果主列表为空，清空推荐列表
  }
}, { deep: true }); // deep: true 确保列表内部对象变化也能触发（虽然这里可能非必需）

// 监听 token 变化，如果用户在组件挂载期间登录/登出
watch(() => store?.state?.token, (newToken, oldToken) => {
  if (newToken && newToken !== oldToken) {
    // console.log('检测到 Token 变化，重新加载数据...');
    loadCategories();
    loadVideos();
  } else if (!newToken && oldToken) {
     // console.log('检测到用户登出，清空数据...');
     videoList.value = [];
     categories.value = [];
     relatedVideos.value = [];
     pagination.total = 0;
     pagination.current = 1;
  }
}, { immediate: false }); // immediate: false 避免挂载时重复加载

</script>

<style scoped>
/* --- 通用容器 --- */
.video-container {
  padding: 25px 30px;
  background: transparent; /* 改为透明背景 */
  border-radius: 12px;
  color: #333;
  min-height: calc(100vh - 100px); /* 确保有最小高度 */
}

/* --- 头部 --- */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2); /* 调整分割线透明度 */
}

.header h2 {
  color: #fff;
  font-weight: 600;
  font-size: 1.8em;
  margin: 0;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.2);
}

.search-input {
  max-width: 350px;
}
:deep(.search-input .el-input__inner) {
  border-radius: 20px;
  background-color: rgba(255, 255, 255, 0.95); /* 更不透明一点 */
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
}
:deep(.search-input .el-input__prefix) {
  color: #769fcd; /* 匹配背景色调 */
}

.category-menu-container {
  background-color: rgba(0, 0, 0, 0.06); /* 非常淡的背景 */
  padding: 20px 15px;
  border-radius: 8px;
}

.category-title {
  color: #f8f9fa; /* 浅灰色，对比度更好 */
  font-size: 1.15em;
  font-weight: 500;
  margin: 0 0 18px 0;
  padding-left: 10px;
  border-left: 4px solid #5dade2; /* 柔和的蓝色边框 */
}

.category-menu {
  border-right: none;
}

.category-menu .el-menu-item {
  border-radius: 6px;
  margin-bottom: 6px;
  padding: 0 15px !important; /* 强制内边距 */
  height: 45px;
  line-height: 45px;
  color: #e9ecef; /* 菜单项文字颜色 */
  transition: background-color 0.2s ease, color 0.2s ease;
}
.category-menu .el-menu-item span {
  margin-left: 10px; /* 图标和文字间距 */
}
.category-menu .el-menu-item .el-icon {
    color: #adb5bd; /* 图标颜色 */
    transition: color 0.2s ease;
}

.category-menu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff; /* 悬停文字变白 */
}
.category-menu .el-menu-item:hover .el-icon {
    color: #fff; /* 悬停图标变白 */
}


.category-menu .el-menu-item.is-active {
  background-color: #5dade2 !important; /* 激活背景色 */
  color: #fff !important;
  font-weight: 500;
}
.category-menu .el-menu-item.is-active .el-icon {
    color: #fff !important; /* 激活图标颜色 */
}

/* 分类为空状态 */
.category-menu .el-empty {
    padding: 10px 0;
    background: transparent;
}
:deep(.category-menu .el-empty__description p) {
    color: #adb5bd;
    font-size: 0.9em;
}

.video-item-col {
  margin-bottom: 24px;
}

.video-card {
  border-radius: 10px;
  overflow: hidden;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  display: flex;
  flex-direction: column;
  height: 100%; /* 使同一行的卡片有相同高度的潜力 */
  background-color: #ffffff;
  border: 1px solid #dee2e6; /* 更柔和的边框 */
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.06);
}

.video-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.video-thumbnail {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* 16:9 宽高比 */
  background-color: #e9ecef; /* 占位背景 */
  overflow: hidden;
}

.video-player {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  border: none;
  background-color: #000; /* 视频加载时的背景色 */
}

.video-details {
  padding: 15px 18px; /* 调整内边距 */
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.video-title {
  margin: 0 0 8px 0;
  font-size: 1.0em;
  font-weight: 600;
  color: #343a40; /* 深灰色标题 */
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 2.8em; /* 预留两行空间 */
  cursor: default;
}

.video-description {
  font-size: 0.85em;
  color: #6c757d; /* 中灰色描述 */
  margin-bottom: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 3em; /* 预留两行空间 */
  flex-grow: 1; /* 允许描述推开底部信息 */
}

.video-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto; /* 推到底部 */
  padding-top: 10px;
  border-top: 1px solid #f1f3f5; /* 非常淡的分割线 */
  font-size: 0.8em;
  color: #adb5bd; /* 浅灰色元信息 */
}
.video-info .el-tag {
    background-color: #e9ecef; /* 标签背景 */
    color: #6c757d; /* 标签文字 */
    border-color: transparent;
}

/* --- 分页 --- */
.pagination-container {
  margin-top: 35px; /* 增加上边距 */
  display: flex;
  justify-content: center;
  padding: 15px 0;
}
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled)) {
    background-color: #fff;
    color: #6c757d;
    font-weight: 500;
}
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
    color: #5dade2;
}
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
    background-color: #5dade2;
    color: #fff;
}
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next) {
    background-color: #fff;
    color: #6c757d;
}


/* --- 区块样式 --- */
.section-title {
  color: #fff;
  margin-bottom: 25px; /* 增加下边距 */
  font-weight: 600;
  font-size: 1.5em; /* 稍大一点 */
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
}

.related-videos-section, .introduction-section {
  margin-top: 45px;
  padding: 30px;
  background-color: rgba(0, 0, 0, 0.05); /* 降低背景透明度 */
  border-radius: 10px;
}

.related-card .video-details {
  padding: 12px 15px;
}

.related-card .video-title {
  font-size: 0.95em;
  min-height: 2.6em;
}
.related-card .video-description {
   display: none; /* 推荐卡片不显示描述 */
}

/* --- 简介区块 --- */
.introduction-section {
  background-color: rgba(255, 255, 255, 0.8); /* 降低背景透明度 */
  color: #343a40;
}
.introduction-section .intro-title {
 color: #495057; /* 简介标题颜色 */
 border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.introduction-section p {
  line-height: 1.7;
  font-size: 0.95em;
  color: #495057; /* 简介文字颜色 */
}

/* --- 空状态和全宽 --- */
.full-width-col {
  width: 100%;
}
.el-empty {
  padding: 40px 0;
  background-color: rgba(255, 255, 255, 0.75); /* 半透明背景 */
  border-radius: 8px;
}
:deep(.el-empty__description p) {
 color: #6c757d; /* 空状态描述文字颜色 */
}


/* --- 响应式调整 --- */
@media (max-width: 1200px) {
    .category-column {
       flex: 0 0 20%; /* 调整分类宽度 */
       max-width: 20%;
    }
    .video-list-column {
       flex: 0 0 80%; /* 调整视频列表宽度 */
       max-width: 80%;
    }
}


@media (max-width: 992px) {
    .category-column {
      flex: 0 0 100%; /* 小屏幕上分类占满整行 */
      max-width: 100%;
      margin-bottom: 25px;
    }
     .video-list-column {
       flex: 0 0 100%; /* 视频列表也占满整行 */
       max-width: 100%;
     }
     .category-menu-container {
        max-height: 350px; /* 限制小屏幕上的高度 */
        overflow-y: auto;
     }
}


@media (max-width: 768px) {
  .video-container {
    padding: 20px 15px;
  }
  .header {
    flex-direction: column;
    align-items: stretch;
  }
  .header h2 {
    margin-bottom: 15px;
    text-align: center;
    font-size: 1.6em; /* 调整标题大小 */
  }
  .search-input {
    max-width: 100%;
  }
  .section-title {
    font-size: 1.3em;
  }
   .video-details {
      padding: 12px 15px;
   }
   .video-title {
      font-size: 0.95em;
   }
    .video-description {
      font-size: 0.8em;
   }
}

/* --- 滚动条样式 (可选) --- */
.category-menu-container::-webkit-scrollbar {
  width: 6px;
}
.category-menu-container::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.5);
  border-radius: 3px;
}
.category-menu-container::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}
</style>
