<template>
  <el-container class="personal-container">
    <!-- 头部 -->
    <el-header class="header">
      <div class="header-content">
        <el-button 
          class="back-btn" 
          type="text" 
          @click="$router.go(-1)"
        >
          <el-icon :size="24" class="icon">
            <ArrowLeft />
          </el-icon>
          返回
        </el-button>

        <div class="user-id">
          <el-icon><User /></el-icon>
          <span>用户ID: {{ userInfo.id }}</span>
        </div>
      </div>
    </el-header>

    <!-- 主体内容 -->
    <el-main>
      <el-row :gutter="30">
        <!-- 左侧信息栏 -->
        <el-col :xs="24" :sm="16" :md="18">
          <el-card class="info-card glass-effect">
            <template #header>
              <div class="card-header">
                <div class="avatar-container">
                  <el-avatar 
                    :size="80" 
                    :src="ToUrl.url+'/'+userInfo.avatar" 
                    @click="handleAvatarClick"
                    class="avatar-hover"
                  />
                  <div class="avatar-hint">点击更换头像</div>
                  <input
                    type="file"
                    ref="avatarInput"
                    hidden
                    accept="image/*"
                    @change="uploadAvatar"
                  />
                </div>
                <div class="user-info">
                  <div class="user-name">
                    {{ userInfo.username }}
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
                  <div class="user-email">{{ userInfo.email }}</div>
                </div>
              </div>
            </template>

            <div v-if="isEditing">
              <!-- 编辑状态：显示可编辑的表单 -->
              <el-form :model="editForm" ref="form" label-width="80px" class="edit-form">
                <el-form-item label="用户名">
                  <el-input v-model="editForm.username" placeholder="请输入用户名">
                    <template #prefix>
                      <el-icon><User /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="电子邮箱">
                  <el-input v-model="editForm.email" placeholder="请输入电子邮箱">
                    <template #prefix>
                      <el-icon><Message /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-form>

              <div class="form-actions">
                <el-button @click="saveChanges" type="primary" class="action-button">
                  <el-icon><Check /></el-icon>保存
                </el-button>
                <el-button @click="handleCancelEdit" class="action-button cancel-button">
                  <el-icon><Close /></el-icon>取消
                </el-button>
              </div>
            </div>

            <!-- 非编辑状态：显示用户信息 -->
            <div v-else>
              <el-descriptions :column="1" border class="user-descriptions">
                <el-descriptions-item label="用户名">
                  <el-icon><User /></el-icon>
                  {{ userInfo.username }}
                </el-descriptions-item>
                <el-descriptions-item label="电子邮箱">
                  <el-icon><Message /></el-icon>
                  {{ userInfo.email }}
                  <el-tag 
                    v-if="userInfo.emailVerified" 
                    size="small" 
                    effect="dark" 
                    type="success" 
                    class="verified-tag"
                  >已验证</el-tag>
                  <el-tag 
                    v-else 
                    size="small" 
                    effect="dark" 
                    type="danger" 
                    class="verified-tag"
                  >未验证</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="注册时间">
                  <el-icon><Calendar /></el-icon>
                  {{ formatTime(userInfo.createTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="最后修改时间">
                  <el-icon><Timer /></el-icon>
                  {{ formatTime(userInfo.updateTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="个人积分">
                  <el-icon><Star /></el-icon>
                  {{ userInfo.totalScore }}
                </el-descriptions-item>
              </el-descriptions>
              <el-button @click="handleEditProfile" type="primary" class="edit-button">
                <el-icon><Edit /></el-icon>编辑资料
              </el-button>
            </div>
          </el-card>

          <!-- 称号选择卡片 -->
          <TitleSelector 
            :honorary-title="userInfo.honoraryTitle" 
            @update:honorary-title="async (newTitle) => {
              console.log('Updating honoraryTitle:', newTitle);
              userInfo.honoraryTitle = newTitle;
              // 重新加载用户信息以确保数据同步
              await userMinemes();
            }"
          />

          <!-- 学习报告卡片 -->
          <el-card class="learning-report-card glass-effect">
            <template #header>
              <div class="card-header">
                <el-icon><Document /></el-icon>
                <span>学习报告</span>
              </div>
            </template>

            <div class="learning-stats">
              <div class="stat-row">
                <div class="stat-item">
                  <div class="stat-value">{{ learningStats.totalTime || 0 }}</div>
                  <div class="stat-label">总学习时长(小时)</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ learningStats.completedCourses || 0 }}</div>
                  <div class="stat-label">已完成课程</div>
                </div>
              </div>
              <div class="stat-row">
                <div class="stat-item">
                  <div class="stat-value">{{ learningStats.averageScore || 0 }}%</div>
                  <div class="stat-label">平均得分</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ learningStats.continuousDays || 0 }}</div>
                  <div class="stat-label">连续学习天数</div>
                </div>
              </div>
            </div>

            <div class="learning-analysis">
              <h3>学习情况分析</h3>
              <div class="analysis-content">
                <p>{{ learningAnalysis }}</p>
              </div>
            </div>

            <div class="learning-suggestions">
              <h3>学习建议</h3>
              <ul class="suggestions-list">
                <li v-for="(suggestion, index) in learningSuggestions" :key="index">
                  <el-icon><Check /></el-icon>
                  {{ suggestion }}
                </li>
              </ul>
            </div>

            <div class="report-actions">
              <el-button type="primary" @click="generateReport" class="generate-report-btn">
                <el-icon><Download /></el-icon>
                生成详细报告
              </el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧安全设置 -->
        <el-col :xs="24" :sm="8" :md="6">
          <el-card class="security-card glass-effect">
            <template #header>
              <div class="security-title">
                <el-icon><Lock /></el-icon>
                安全设置
              </div>
            </template>

            <div class="security-items">
              <div class="security-item">
                <div class="security-item-header">
                  <el-icon><Message /></el-icon>
                  <span>邮箱验证</span>
                </div>
                <div class="security-item-content">
                  <el-tag v-if="userInfo.emailVerified" type="success" effect="dark">已验证</el-tag>
                  <el-tag v-else type="danger" effect="dark">未验证</el-tag>
                  <el-button 
                    v-if="!userInfo.emailVerified" 
                    @click="handleVerifyEmail" 
                    size="small" 
                    type="primary"
                    :disabled="isVerifying || cooldown > 0"
                  >
                    {{ cooldown > 0 ? `请等待 ${cooldown} 秒` : '验证邮箱' }}
                  </el-button>
                </div>
              </div>

              <div class="security-item">
                <div class="security-item-header">
                  <el-icon><Lock /></el-icon>
                  <span>登录密码</span>
                </div>
                <div class="security-item-content">
                  <el-button @click="handleChangePassword" type="primary" size="small">
                    修改密码
                  </el-button>
                </div>
              </div>

              <div class="security-item">
                <div class="security-item-header">
                  <el-icon><User /></el-icon>
                  <span>账户绑定</span>
                </div>
                <div class="security-item-content">
                  <el-button @click="handleBindAccount" type="primary" size="small">
                    管理绑定
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 添加新的统计卡片 -->
          <el-card class="stats-card glass-effect">
            <template #header>
              <div class="stats-title">
                <el-icon><DataLine /></el-icon>
                数据统计
              </div>
            </template>
            <div class="stats-content">
              <div class="stat-item">
                <div class="stat-value">{{ userInfo.totalScore }}</div>
                <div class="stat-label">总积分</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">0</div>
                <div class="stat-label">收藏数</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">0</div>
                <div class="stat-label">关注数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>

    <!-- 修改密码对话框 -->
    <el-dialog 
      title="修改密码" 
      v-model="passwordDialogVisible" 
      width="30%"
      class="custom-dialog"
    >
      <el-form label-width="100px" class="password-form">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input 
            v-model="passwordForm.currentPassword" 
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelPasswordChange" class="dialog-cancel-btn">取消</el-button>
          <el-button type="primary" @click="savePasswordChanges" class="dialog-confirm-btn">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 账户绑定管理对话框 -->
    <el-dialog 
      title="账户绑定管理" 
      v-model="accountBindingDialogVisible" 
      width="40%"
      class="custom-dialog"
    >
      <div v-if="bindedAccounts.length === 0" class="no-accounts">
        <el-empty description="您尚未绑定任何账户" />
      </div>
      <div v-else class="accounts-grid">
        <el-row :gutter="20">
          <el-col :span="12" v-for="(account, index) in bindedAccounts" :key="index">
            <el-card class="account-card">
              <div class="account-info">
                <el-icon :size="24"><Platform /></el-icon>
                <span>{{ account.name }}</span>
              </div>
              <el-button @click="unbindAccount(account)" type="danger" size="small">
                解绑
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="accountBindingDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue'
import { Message, Lock, User, ArrowLeft, Calendar, Timer, Star, Edit, Close, DataLine, Platform, Document, Download, Check } from '@element-plus/icons-vue'
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElButton, ElRow, ElCol, ElCard, ElTag } from 'element-plus';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router'
import axios from 'axios';
import ToUrl from '@/api/api';
import TitleSelector from './TitleSlect/TitleSelector.vue'

const router=useRouter();

// 使用 vuex 获取用户信息
const store = useStore()

// 模拟用户数据
const userInfo = ref({
  // 用户基本信息
  id: "",
  username: "",
  email: "",
  password: "",
  avatar: '',
  emailVerified: false,
  roles: [],
  
  // 时间信息
  createTime: "",
  updateTime: "",
  
  // 积分和统计信息
  totalScore: 0,
  correctCount: 0,
  totalAttempts: 0,
  ActivityPoints: 0,
  
  // 称号信息
  honoraryTitle: {},
  
  // 签到信息
  lastCheckInDate: "",
  consecutiveCheckInDays: 0,
  
  // 题目和实验信息
  completedQuestions: [],
  vulnScores: {},
  activeLabs: [],
  completedLabs: []
})

// 学习统计数据
const learningStats = ref({
  totalTime: 0,
  completedCourses: 0,
  averageScore: 0,
  continuousDays: 0
})

// 学习分析
const learningAnalysis = ref('')

// 学习建议
const learningSuggestions = ref([])

const passwordDialogVisible = ref(false);
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const accountBindingDialogVisible = ref(false);
const bindedAccounts = ref([]); // 假设从后端获取已绑定账户信息

// 使用 ref 来获取文件选择框
const avatarInput = ref(null);

// 触发文件选择框点击
const handleAvatarClick = () => {
  avatarInput.value.click();  // 点击文件选择框
};

// 上传头像
const uploadAvatar = async (e) => {
  const file = e.target.files[0];
  // console.log(111111111);
  if (!file) return;
  // console.log(22222222222);
  if(file.size > 500000) alert('太大');
  // 快速校验
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件');
    return;
  }
  // console.log(333333333);
  try {
    const formData = new FormData();
    formData.append('file', file);
    // console.log(444444444444);
    const res = await axios.post(ToUrl.url+`/user/avatar`, formData, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`,
        'Content-Type': 'multipart/form-data'
      }
    });
    // console.log(5555555555555);
    // console.log("aaaaaaaaaaaaaaaaa",res.data.data);
    // 更新头像
    userInfo.value.avatar = res.data.data;  // 更新图片路径
    await store.dispatch('setAvatar',res.data.data);
    // console.log("头像"+store.state.avatar);
    // console.log("路径如下:"+userInfo.value.avatar);
    ElMessage.success('头像更新成功');
  } catch (err) {
    ElMessage.error('上传失败');
  }
};
const handleChangePassword = () => {
  passwordDialogVisible.value = true;
};

//加载用户信息
const userMinemes = async () => {
  if (store.state.user === "") {
    ElMessage.error("未登录");
    return;
  }
  try {
    const response = await axios.get(ToUrl.url+`/user/mes/${store.state.user}`, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    });
    console.log('User info response:', response.data.data);
    
    // 检查 honoraryTitle 字段
    if (!response.data.data.honoraryTitle) {
      console.warn('honoraryTitle field is missing or null');
      response.data.data.honoraryTitle = {};
    } else {
      console.log('honoraryTitle data:', response.data.data.honoraryTitle);
    }
    
    userInfo.value = response.data.data;
    console.log('Updated userInfo:', userInfo.value);

    // 确保 emailVerified 字段被正确设置
    userInfo.value.emailVerified = response.data.data.emailVerified || false;

    // 更新 store 中的称号信息
    store.commit('setHonoraryTitle', response.data.data.honoraryTitle || {});

    editForm.value = {
      id:userInfo.value.id,
      username: userInfo.value.username,
      email: userInfo.value.email,
    }; 

    ElMessage.success("加载成功");
  } catch (error) {
    console.error('Error loading user info:', error);
    ElMessage.error("加载失败");
  }
};

// 生成学习报告
const generateReport = async () => {
  try {
    // 这里应该调用后端API获取详细的学习报告数据
    const response = await axios.get(ToUrl.url+`/user/learning-report/${store.state.user}`, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    });
    
    // 更新学习统计数据
    learningStats.value = response.data.data.stats;
    learningAnalysis.value = response.data.data.analysis;
    learningSuggestions.value = response.data.data.suggestions;
    
    // 生成可下载的报告
    const reportContent = generateReportContent();
    downloadReport(reportContent);
    
    ElMessage.success('报告生成成功');
  } catch (error) {
    ElMessage.error('报告生成失败');
  }
};

// 生成报告内容
const generateReportContent = () => {
  const report = {
    title: `${userInfo.value.username}的学习报告`,
    date: new Date().toLocaleDateString(),
    stats: learningStats.value,
    analysis: learningAnalysis.value,
    suggestions: learningSuggestions.value
  };
  
  return JSON.stringify(report, null, 2);
};

// 下载报告
const downloadReport = (content) => {
  const blob = new Blob([content], { type: 'application/json' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${userInfo.value.username}_学习报告_${new Date().toLocaleDateString()}.json`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};

// 加载学习数据
const loadLearningData = async () => {
  try {
    const response = await axios.get(ToUrl.url+`/user/learning-stats/${store.state.user}`, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    });
    
    learningStats.value = response.data.data.stats;
    learningAnalysis.value = response.data.data.analysis;
    learningSuggestions.value = response.data.data.suggestions;
  } catch (error) {
    console.error('加载学习数据失败:', error);
  }
};

// 称号相关的响应式数据
const currentTitle = computed(() => {
  console.log('Computing currentTitle with userInfo:', userInfo.value);
  const titleData = userInfo.value.honoraryTitle || {}
  console.log('Title data:', titleData);
  
  const selectedTitle = Object.entries(titleData).find(([_, value]) => value === 1)
  console.log('Selected title:', selectedTitle);
  
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
    const result = {
      id: selectedTitle[0],
      name: selectedTitle[0],
      type: typeMap[selectedTitle[0]] || 'info',
      description: '称号描述',
      requirement: '称号要求'
    }
    console.log('Returning title result:', result);
    return result;
  }
  
  console.log('No title selected, returning default');
  return {
    id: '',
    name: '未设置',
    type: 'info',
    description: '',
    requirement: ''
  }
})

// 在组件挂载时加载用户信息和学习数据
onMounted(() => {
  userMinemes();
  loadLearningData();
});

// 在组件卸载前清理数据
onBeforeUnmount(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer);
  }
})

// 修改密码
const savePasswordChanges = async () => {
  const { currentPassword, newPassword, confirmPassword } = passwordForm.value;
  
  // 验证密码为空
  if (!currentPassword || !newPassword || !confirmPassword) {
    ElMessage.error("密码不能为空");
    return;
  }
  // 验证新密码与确认密码一致性
  if (newPassword !== confirmPassword) {
    ElMessage.error("新密码与确认密码不一致");
    return;
  }
  try {
    await axios.put(ToUrl.url+"/user/changepwd", {
      id: store.state.id,
      currentPassword,
      newPassword
    }, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    });
    ElMessage.success("密码修改成功");
    passwordDialogVisible.value = false;
    // 清空密码表单
    passwordForm.value.currentPassword = '';
    passwordForm.value.newPassword = '';
    passwordForm.value.confirmPassword = '';
  } catch (error) {
    ElMessage.error("密码修改失败");
  }
};

// 取消修改密码
const handleCancelPasswordChange = () => {
  passwordDialogVisible.value = false;
  // 清空密码表单
  passwordForm.value.currentPassword = '';
  passwordForm.value.newPassword = '';
  passwordForm.value.confirmPassword = '';
};

// 编辑表单的数据模型
const editForm = ref({
  id:userInfo.id,
  username: userInfo.username,
  email: userInfo.email,
})

// 控制编辑模式
const isEditing = ref(false)

// 格式化时间
const formatTime = (timeString) => {
  return new Date(timeString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

// 进入编辑模式
const handleEditProfile = () => {
  isEditing.value = true
}

// 取消编辑模式
const handleCancelEdit = () => {
  userMinemes();
  isEditing.value = false
}
// 账户绑定管理
const handleBindAccount = () => {
  accountBindingDialogVisible.value = true;
};

const unbindAccount = (account) => {
  // 解绑账户的逻辑
  ElMessage.success(`已解绑账户: ${account.name}`);
};

// 密码确认验证
const confirmPasswordValidator = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('确认密码与新密码不一致'));
  } else {
    callback();
  }
};

// 修改保存信息的逻辑
const saveChanges = async () => {
  if (editForm.value.username === "" || editForm.value.email === "" ||
    (editForm.value.username === userInfo.value.username && editForm.value.email === userInfo.value.email)) {
    ElMessage.error("用户名或邮箱不能为空，或者未更改任何数据");
  } else {
    try {
      const response = await axios.put(ToUrl.url+"/user/update", {
        id: editForm.value.id,
        username: editForm.value.username,
        email: editForm.value.email
      }, {
        headers: {
          'Authorization': `Bearer ${store.state.token}`
        }
      });

      // 更新用户信息
      store.commit('setUser', editForm.value.username);
      userMinemes();  // 重新加载用户信息

      ElMessage.success('信息保存成功');
      isEditing.value = false;  // 退出编辑状态
    } catch (error) {
      ElMessage.error("信息保存失败");
    }
  }
};

// 添加冷却时间相关的响应式变量
const cooldown = ref(0);
const isVerifying = ref(false);
let cooldownTimer = null;

// 修改验证邮箱的处理函数
const handleVerifyEmail = async () => {
  if (isVerifying.value || cooldown.value > 0) return;
  
  try {
    isVerifying.value = true;
    const token = store.state.token;
    if (!token) {
      ElMessage.error('未登录或登录已过期');
      return;
    }
    
    await axios.post(ToUrl.url + '/user/send-verification', {}, {
      headers: {
        'Authorization': `Bearer ${token.trim()}`
      }
    });
    
    ElMessage.success('验证邮件已发送，请查收邮箱');
    
    // 设置5分钟冷却时间
    cooldown.value = 300; // 5分钟 = 300秒
    cooldownTimer = setInterval(() => {
      cooldown.value--;
      if (cooldown.value <= 0) {
        clearInterval(cooldownTimer);
        cooldownTimer = null;
      }
    }, 1000);
    
  } catch (error) {
    console.error('发送验证邮件失败:', error);
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录');
    } else {
      ElMessage.error(error.response?.data?.message || '发送验证邮件失败');
    }
  } finally {
    isVerifying.value = false;
  }
};

</script>

<style scoped>
.personal-container {
  min-height: 100vh;
  background: transparent;
  padding: 20px;
}

.header {
  background: transparent;
  padding: 0;
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #fff;
  transition: all 0.3s ease;
}

.back-btn:hover {
  transform: translateX(-5px);
  color: rgba(255, 255, 255, 0.8);
}

.user-id {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #fff;
}

.glass-effect {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.info-card {
  margin-bottom: 20px;
  border-radius: 15px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.avatar-container {
  position: relative;
  text-align: center;
}

.avatar-hint {
  position: absolute;
  bottom: -25px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #fff;
  background: rgba(0, 0, 0, 0.7);
  padding: 4px 8px;
  border-radius: 4px;
  white-space: nowrap;
  opacity: 0;
  transition: all 0.3s ease;
  pointer-events: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.avatar-container:hover .avatar-hint {
  opacity: 1;
  bottom: -30px;
}

.avatar-hover {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.avatar-hover:hover {
  transform: scale(1.05);
  border-color: var(--el-color-primary);
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.user-name {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-title {
  font-size: 12px;
  height: 20px;
  line-height: 18px;
  padding: 0 8px;
}

.user-email {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.user-descriptions {
  margin-top: 20px;
}

/* 添加表格样式 */
:deep(.el-descriptions__body) {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  border-radius: 10px;
}

:deep(.el-descriptions__cell) {
  background: transparent !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: #fff !important;
}

:deep(.el-descriptions__label) {
  color: rgba(255, 255, 255, 0.8) !important;
  font-weight: 500;
}

:deep(.el-descriptions__content) {
  color: #fff !important;
}

:deep(.el-descriptions__content .el-icon) {
  color: #fff !important;
  margin-right: 8px;
}

:deep(.el-descriptions__content .el-tag) {
  margin-left: 8px;
}

:deep(.el-descriptions__content span) {
  color: #fff !important;
}

:deep(.el-descriptions__content div) {
  color: #fff !important;
}

.verified-tag {
  margin-left: 10px;
  font-size: 12px;
  height: 20px;
  line-height: 18px;
  padding: 0 8px;
}

:deep(.el-tag--success) {
  background-color: rgba(103, 194, 58, 0.2);
  border-color: rgba(103, 194, 58, 0.3);
  color: #67C23A;
}

:deep(.el-tag--danger) {
  background-color: rgba(237, 80, 80, 0.603);
  border-color: rgba(224, 134, 134, 0.3);
  color: #f0eaea;
}

.edit-button {
  margin-top: 20px;
  width: 100%;
}

.security-card {
  margin-bottom: 20px;
  border-radius: 15px;
}

.security-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.security-item {
  padding: 15px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
}

.security-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-weight: 500;
  color: #fff;
}

.security-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}

.stats-card {
  border-radius: 15px;
}

.stats-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.stats-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  padding: 15px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 5px;
}

.custom-dialog {
  border-radius: 15px;
}

.password-form {
  padding: 20px;
}

.dialog-footer {
  padding: 20px;
  text-align: right;
}

.no-accounts {
  padding: 40px 0;
}

.accounts-grid {
  padding: 20px 0;
}

.account-card {
  margin-bottom: 15px;
  border-radius: 10px;
}

.account-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 5px;
}

@media (max-width: 768px) {
  .personal-container {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-content {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 修改按钮文字颜色 */
:deep(.el-button) {
  color: #fff;
}

:deep(.el-button--primary) {
  color: #fff;
}

:deep(.el-button--danger) {
  color: #fff;
}

:deep(.el-button--text) {
  color: #fff;
}

:deep(.el-button--text:hover) {
  color: rgba(255, 255, 255, 0.8);
}

/* 表格样式 */
:deep(.el-descriptions__body) {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  border-radius: 10px;
}

:deep(.el-descriptions__cell) {
  background: transparent !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: #fff !important;
}

:deep(.el-descriptions__label) {
  color: rgba(255, 255, 255, 0.8) !important;
  font-weight: 500;
}

:deep(.el-descriptions__content) {
  color: #fff !important;
}

:deep(.el-descriptions__content .el-icon) {
  color: #fff !important;
  margin-right: 8px;
}

:deep(.el-descriptions__content .el-tag) {
  margin-left: 8px;
}

:deep(.el-descriptions__content span) {
  color: #fff !important;
}

:deep(.el-descriptions__content div) {
  color: #fff !important;
}

/* 输入框文字颜色 */
:deep(.el-input__inner) {
  color: #fff;
}

:deep(.el-input__prefix) {
  color: rgba(255, 255, 255, 0.8);
}

/* 对话框标题和内容 */
:deep(.el-dialog__title) {
  color: #fff;
}

:deep(.el-dialog__body) {
  color: #fff;
}

/* 编辑表单样式 */
.edit-form {
  padding: 20px;
}

:deep(.el-form-item__label) {
  color: #fff !important;
  font-weight: 500;
}

:deep(.el-form-item__content) {
  margin-left: 120px !important;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-input__inner) {
  color: #fff;
}

:deep(.el-input__prefix) {
  color: rgba(255, 255, 255, 0.8);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding: 0 20px;
}

/* 取消按钮样式 */
.cancel-button {
  color: #000000 !important;
  background: #ffffff !important;
  border: 1px solid #dcdfe6 !important;
}

.cancel-button:hover {
  background: #f5f7fa !important;
  border-color: #dcdfe6 !important;
  color: #000000 !important;
}

.cancel-button:active {
  background: #f5f7fa !important;
  border-color: #dcdfe6 !important;
  color: #000000 !important;
}

/* 密码弹窗按钮样式 */
.dialog-cancel-btn {
  color: #000000 !important;
  background: #ffffff !important;
  border: 1px solid #dcdfe6 !important;
}

.dialog-cancel-btn:hover {
  background: #f5f7fa !important;
  border-color: #dcdfe6 !important;
  color: #000000 !important;
}

.dialog-cancel-btn:active {
  background: #f5f7fa !important;
  border-color: #dcdfe6 !important;
  color: #000000 !important;
}

/* 密码弹窗样式 */
:deep(.el-dialog) {
  background: #ffffff !important;
}

:deep(.el-dialog__title) {
  color: #000000 !important;
}

:deep(.el-dialog__body) {
  color: #000000 !important;
}

:deep(.el-form-item__label) {
  color: #000000 !important;
}

:deep(.el-input__inner) {
  color: #000000 !important;
  background: #ffffff !important;
}

:deep(.el-input__wrapper) {
  background: #ffffff !important;
}

/* 学习报告卡片样式 */
.learning-report-card {
  margin-top: 20px;
  border-radius: 15px;
}

.learning-report-card .card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.learning-stats {
  padding: 20px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.stat-row:last-child {
  margin-bottom: 0;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 15px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  margin: 0 10px;
}

.stat-item:first-child {
  margin-left: 0;
}

.stat-item:last-child {
  margin-right: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.learning-analysis,
.learning-suggestions {
  padding: 20px;
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

.learning-analysis h3,
.learning-suggestions h3 {
  color: #fff;
  font-size: 16px;
  margin-bottom: 15px;
}

.analysis-content {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

.suggestions-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestions-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.9);
}

.suggestions-list li:last-child {
  margin-bottom: 0;
}

.suggestions-list .el-icon {
  color: var(--el-color-success);
}

.report-actions {
  padding: 20px;
  text-align: center;
}

.generate-report-btn {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .stat-row {
    flex-direction: column;
  }
  
  .stat-item {
    margin: 0 0 10px 0;
  }
  
  .stat-item:last-child {
    margin-bottom: 0;
  }
}

/* 添加禁用按钮的样式 */
:deep(.el-button.is-disabled) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.5) !important;
  cursor: not-allowed;
}

:deep(.el-button.is-disabled:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.5) !important;
}
</style>