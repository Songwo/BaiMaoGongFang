<template>
  <el-card class="post-management">
    <template #header>
      <div class="card-header">
        <span>帖子管理</span>
        <el-button type="primary" size="small" @click="handleAddPost">新增帖子</el-button>
      </div>
    </template>

    <el-table :data="posts" stripe>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="username" label="作者" />
      <el-table-column prop="section" label="类别" />
      <el-table-column prop="timestamp" label="上传时间">
        <template #default="scope">
          {{ formatTime(scope.row.timestamp) }}
        </template>
      </el-table-column>
      <el-table-column label="评论管理" width="120">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="showComments(scope.row)">管理评论</el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="previewPost(scope.row)">预览</el-button>
          <el-button link type="primary" size="small" @click="editPost(scope.row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="deletePost(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 帖子对话框 -->
    <el-dialog v-model="addPostDialogVisible" title="新增帖子" width="800px">
      <el-form :model="newPost" :rules="postRules" ref="newPostFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="newPost.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="分类" prop="section">
          <el-select v-model="newPost.section" placeholder="请选择分类">
            <el-option v-for="section in sections" :key="section" :label="section" :value="section" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="newPost.content" type="textarea" :rows="8" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addPostDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNewPost">提交</el-button>
      </template>
    </el-dialog>

    <!-- 评论管理对话框 -->
    <el-dialog v-model="commentDialogVisible" title="评论管理">
      <el-table :data="currentComments" stripe>
        <el-table-column prop="content" label="内容">
          <template #default="scope">
            <el-input v-if="editingComment && editingComment.id === scope.row.id" v-model="editingComment.content"
              size="small" />
            <span v-else>{{ scope.row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="作者" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="startEditComment(scope.row)">
              编辑
            </el-button>
            <el-button v-if="editingComment && editingComment.id === scope.row.id" link type="success" size="small"
              @click="saveEditedComment">
              保存
            </el-button>
            <el-button link type="danger" size="small" @click="deleteComment(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="commentDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 帖子展示 / 编辑框 -->
    <el-dialog v-model="postEdit" :title="editMode ? '编辑帖子' : '帖子预览'" width="600px">
      <el-input type="textarea" :rows="8" placeholder="请输入内容" v-model="post.content" class="post-input"
        :disabled="!editMode">
      </el-input>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="postEdit = false">关闭</el-button>
          <el-button v-if="editMode" type="primary" @click="savePost(post)">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import store from '@/store'
import ToUrl from '@/api/api'

// 帖子数据
const posts = ref([])
// 新增帖子相关状态
const addPostDialogVisible = ref(false)
const newPostFormRef = ref(null)
const newPost = ref({
  title: '',
  section: '',
  content: '',
})
const commentDialogVisible = ref(false)
const postEdit = ref(false)
const editMode = ref(false)
const post = ref({})         // 当前选中的帖子
// 评论管理
const currentComments = ref([])

const xava = ref("");  // 从 store 获取头像路径
xava.value=store.state.avatar;

// 分类选项
const sections = ref(['安全漏洞讨论', '网络攻击案例', '安全工具推荐', '行业动态'])

// 表单验证规则
const postRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  section: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 时间格式化函数
const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 打开新增帖子对话框
const handleAddPost = () => {
  newPost.value = { title: '', section: '', content: '' } // 清空表单
  addPostDialogVisible.value = true
}

//获取全部帖子数据
const loadPost = async () => {
  try {
    const response = await axios.get(ToUrl.url+'/post/findAll', {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    // console.log(response.data.data)
    // 确保数据是数组
    if (Array.isArray(response.data.data)) {
      posts.value = response.data.data
    } else {
      console.error("错误：返回数据不是数组", response.data)
      posts.value = [] // 防止 Element Plus 报错
    }
  } catch (error) {
    ElMessage.error("数据加载失败" + error)
  }
}
// 提交新增帖子
const submitNewPost = async () => {
  try {
    // 验证表单
    await newPostFormRef.value.validate();
    const requestUrl = ToUrl.url+`/admin/insertPost`;
    // 发送新增帖子请求
    const response = await axios.post(requestUrl, {
      title: newPost.value.title,
      username: '官方帖子',
      authorId: store.state.id,
      section: newPost.value.section,
      content: newPost.value.content,
      avatar: xava.value
    }, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    });

    if (response.data.code === 200) {
      loadPost();
      ElMessage.success('帖子添加成功');
      addPostDialogVisible.value = false;
    } else {
      ElMessage.error(response.data.msg || '帖子添加失败');
    }
  } catch (error) {
    console.error('提交帖子失败:', error);
    ElMessage.error('表单验证失败，请检查输入');
  }
};


// 预览帖子
const previewPost = (selectedPost) => {
  post.value = { ...selectedPost }  // 复制帖子数据
  editMode.value = false            // 进入预览模式
  postEdit.value = true
}

// 编辑帖子
const editPost = (selectedPost) => {
  post.value = { ...selectedPost }  // 复制帖子数据
  editMode.value = true             // 进入编辑模式
  postEdit.value = true
}

const savePost = async (newPost) => {
  if (!newPost.content.trim()) {
    ElMessage.warning('帖子内容不能为空！');
    return;
  }

  try {
    const requestUrl =ToUrl.url + `/admin/UpdatePost`;
    // 发送 PUT 请求
    const response = await axios.put(
      requestUrl,
      {
        id: newPost.id,
        content: newPost.content,
        authorId: store.state.id
      },
      {
        headers: {
          Authorization: `Bearer ${store.state.token}`
        }
      }
    );

    // 判断后端是否成功更新
    if (response.data.code === 200) {
      ElMessage.success('帖子已保存');
      loadPost(); // 重新加载帖子数据，保证前端和后端同步
      postEdit.value = false; // 只有成功后才隐藏编辑框
    } else {
      ElMessage.error(response.data.msg || '帖子更新失败');
    }
  } catch (error) {
    console.error("更新帖子失败:", error);
    ElMessage.error(`更新失败: ${error.response?.data?.msg || error.message}`);
  }
};


// 删除帖子
const deletePost = async (selectedPost) => {
  try {
    const requestUrl = ToUrl.url+`/admin/deletePost`;
    // 发送 PUT 请求
    const response = await axios.put(
      requestUrl,
      {
        id: selectedPost.id,
      },
      {
        headers: {
          Authorization: `Bearer ${store.state.token}`
        }
      }
    );
    loadPost();
    ElMessage.success('帖子已删除')
  } catch (error) {
    ElMessage.error("删除失败：" + error)
  }
}

//保存评论
const editingComment = ref(null); // 存储当前正在编辑的评论

// 开始编辑评论
const startEditComment = (comment) => {
  editingComment.value = { ...comment }; // 复制评论数据
};

// 保存编辑的评论
const saveEditedComment = async () => {
  if (!editingComment.value.content.trim()) {
    ElMessage.warning('评论内容不能为空！');
    return;
  }

  try {
    const requestUrl = ToUrl.url+`/admin/UpdateComment`;
    const response = await axios.put(requestUrl, {
      id: editingComment.value.id,
      content: editingComment.value.content,
      username:editingComment.value.username
    }, {
      headers: { Authorization: `Bearer ${store.state.token}` }
    });

    if (response.data.code === 200) {
      ElMessage.success('评论已更新');
      // 重新加载评论列表
      showComments({ id: editingComment.value.postId });
      editingComment.value = null; // 清空编辑状态
    } else {
      ElMessage.error(response.data.msg || '评论更新失败');
    }
  } catch (error) {
    console.error("更新评论失败:", error);
    ElMessage.error(`更新失败: ${error.response?.data?.msg || error.message}`);
  }
};

//评论删除
const deleteComment = async (comment) => {
  try {
    const requestUrl = ToUrl.url+`/admin/deleteComment`;
    const response = await axios.put(requestUrl, {
      id: comment.id 
    },{
      headers: {
        Authorization: `Bearer ${store.state.token}`
      }
    });

    if (response.data.code === 200) {
      ElMessage.success('评论已删除');
      showComments({ id: comment.postId }); // 重新加载该帖子的评论
    } else {
      ElMessage.error(response.data.msg || '评论删除失败');
    }
  } catch (error) {
    console.error("删除评论失败:", error);
    ElMessage.error(`删除失败: ${error.response?.data?.msg || error.message}`);
  }
};

const showComments = async (selectedPost) => {
  try {
    const response = await axios.get(ToUrl.url+`/comments/find/${selectedPost.id}`, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    });
    currentComments.value = response.data.data;
    editingComment.value = null; // 取消编辑状态
  } catch (error) {
    ElMessage.error("评论数据加载失败:" + error);
  }
  commentDialogVisible.value = true;
};

onMounted(() => {
  loadPost();
})
</script>

<style scoped>
.post-input {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
