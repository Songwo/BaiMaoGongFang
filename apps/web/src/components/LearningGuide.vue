<template>
  <div class="learning-guide" v-if="!isAdminPage">
    <el-dialog
      v-model="showNewUserGuide"
      title="欢迎来到学习之旅"
      width="60%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      class="welcome-dialog"
    >
      <div class="guide-content">
        <div class="welcome-header">
          <div class="title-container">
            <img src="@/assets/logo/logo/信息.png" alt="Logo" class="welcome-logo" />
            <h2 class="welcome-title">开启您的安全学习之旅</h2>
          </div>
          <p class="welcome-subtitle">为了给您提供更好的学习体验，请先完成以下问卷</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" class="profile-form">
          <el-form-item label="您的技术等级" prop="skillLevel">
            <el-select 
              v-model="form.skillLevel" 
              placeholder="请选择您的技术等级" 
              class="custom-select"
            >
              <el-option label="初学者" value="beginner">
                <div class="option-content">
                  <span class="option-icon">🌱</span>
                  <span class="option-label">初学者</span>
                  <span class="option-desc">刚开始接触安全领域</span>
                </div>
              </el-option>
              <el-option label="中级" value="intermediate">
                <div class="option-content">
                  <span class="option-icon">🌿</span>
                  <span class="option-label">中级</span>
                  <span class="option-desc">有一定安全基础</span>
                </div>
              </el-option>
              <el-option label="高级" value="advanced">
                <div class="option-content">
                  <span class="option-icon">🌳</span>
                  <span class="option-label">高级</span>
                  <span class="option-desc">具备丰富安全经验</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="您感兴趣的领域" prop="interests">
            <el-select 
              v-model="form.interests" 
              multiple 
              :collapse-tags="false"
              placeholder="请选择您感兴趣的领域" 
              class="custom-select"
            >
              <el-option 
                v-for="item in interestOptions" 
                :key="item.value" 
                :label="item.label" 
                :value="item.value"
              >
                <div class="interest-option">
                  <span class="interest-icon">{{ getInterestIcon(item.value) }}</span>
                  <span class="interest-label">{{ item.label }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="您的学习目标" prop="learningGoal">
            <el-input 
              type="textarea" 
              v-model="form.learningGoal" 
              :rows="3" 
              placeholder="请输入您的学习目标，例如：掌握Web安全基础知识，能够独立完成简单的安全测试..."
              class="custom-textarea"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <div class="button-group">
            <el-button 
              type="primary" 
              @click="submitProfile"
              class="submit-button"
            >
              开始学习之旅
            </el-button>
            <el-button 
              @click="handleRemindLater"
              class="remind-later-button"
            >
              稍后提醒
            </el-button>
          </div>
          <div class="footer-slogan">
            <p class="slogan-text gradient-text">共建中国数字化长城</p>
            <p class="slogan-call gradient-text">加入我们，共同守护网络安全</p>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount, h } from 'vue';
import { useStore } from 'vuex';
import { ElMessage, ElNotification } from 'element-plus';
import axios from 'axios';
import request from '@/axios/index';
import { useRouter, useRoute } from 'vue-router';

// 访问 Vuex Store
const store = useStore();
const router = useRouter();
const route = useRoute();

// 判断是否在管理员页面
const isAdminPage = computed(() => {
  return route.path.includes('/backMange');
});

// 添加判断是否为App下载页面的计算属性
const isAppDownloadPage = computed(() => {
  return route.path.includes('/root/app');
});

// 响应式状态
const showNewUserGuide = ref(false);
const form = reactive({
  skillLevel: '',
  interests: [],
  learningGoal: ''
});
const recommendationTimer = ref(null);

// 模板引用
const formRef = ref(null);

// 静态数据
const rules = {
  skillLevel: [
    { required: true, message: '请选择您的技术等级', trigger: 'change' }
  ],
  interests: [
    { required: true, message: '请选择您感兴趣的领域', trigger: 'change' }
  ],
  learningGoal: [
    { required: true, message: '请输入您的学习目标', trigger: 'blur' }
  ]
};

const interestOptions = [
  { value: 'web', label: 'Web安全' },
  { value: 'mobile', label: '移动安全' },
  { value: 'system', label: '系统安全' },
  { value: 'crypto', label: '密码学' },
  { value: 'reverse', label: '逆向工程' }
];

// 计算属性
const isLoggedIn = computed(() => {
  const stu = store.state.token;
  // console.log("stu:111111111111"+stu);
  return !!stu;
});

const isOrdinaryUser = computed(() => {
  return isLoggedIn.value && !(store.state.roles || []).includes('ROLE_ADMIN');
});

// 添加缓存相关的响应式变量
const recommendationsCache = ref(new Map());
const userProfileCache = ref(null);

// 修改获取推荐的方法
const getRecommendations = async () => {
  try {
    // 检查缓存
    const cacheKey = `recommendations_${store.state.id}`;
    if (recommendationsCache.value.has(cacheKey)) {
      return recommendationsCache.value.get(cacheKey);
    }

    const response = await request.get(`/api/recommendations/user/${store.state.id}`);
    const recommendations = response.data;
    if (recommendations && recommendations.length > 0) {
      const processedRecommendations = recommendations.map(rec => ({
        ...rec,
        description: rec.description || '这是一个适合您当前水平的学习内容，点击开始学习吧！'
      }));
      
      // 存入缓存
      recommendationsCache.value.set(cacheKey, processedRecommendations);
      return processedRecommendations;
    }
    return [];
  } catch (error) {
    console.error('获取推荐失败:', error);
    ElMessage.error('获取推荐内容失败');
    return [];
  }
};

const recordUserBehavior = async (behavior) => {
  try {
    await request.post('/api/recommendations/behavior', {
      userId: store.state.id,
      contentType: behavior.contentType, // VIDEO, QUIZ, VULNERABILITY
      contentId: behavior.contentId,
      interactionType: behavior.interactionType, // WATCH, ANSWER, SEARCH
      interactionTime: new Date(),
      duration: behavior.duration,
      score: behavior.score,
      category: behavior.category
    });
  } catch (error) {
    console.error('记录用户行为失败:', error);
  }
};

const getRecommendationUrl = (recommendation) => {
  switch (recommendation.contentType) {
    case 'VIDEO':
      return '/root/atack';
    case 'QUIZ':
      return '/root/Question';
    case 'VULNERABILITY':
      return '/root/game';
    default:
      return '/root/home';
  }
};

const formatDuration = (seconds) => {
  if (!seconds) return '';
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  return `${hours > 0 ? hours + '小时' : ''}${minutes}分钟`;
};

const showRecommendation = async () => {
  try {
    // 如果是App下载页面，不显示推荐
    if (isAppDownloadPage.value) {
      return;
    }

    // 检查是否应该显示推荐
    const lastRecommendationTime = localStorage.getItem('lastRecommendationTime');
    const now = Date.now();
    if (lastRecommendationTime && now - parseInt(lastRecommendationTime) < 60000) {
      return; // 如果距离上次推荐不到1分钟，不显示新的推荐
    }

    const recommendations = await getRecommendations();
    if (recommendations && recommendations.length > 0) {
      const randomRecommendation = recommendations[Math.floor(Math.random() * recommendations.length)];
      
      // 获取内容类型对应的图标和描述
      const contentTypeInfo = {
        VIDEO: { icon: '🎥', text: '视频课程' },
        QUIZ: { icon: '📝', text: '知识测试' },
        VULNERABILITY: { icon: '🎮', text: '实战练习' }
      }[randomRecommendation.contentType] || { icon: '📚', text: '学习内容' };

      // 获取难度等级对应的文本
      const difficultyText = {
        1: '入门级',
        2: '中级',
        3: '高级'
      }[randomRecommendation.difficultyLevel] || '';

      // 构建标签文本
      const tagsText = randomRecommendation.tags?.join(' · ') || '';
      
      // 构建时长或题目数量信息
      const contentInfo = randomRecommendation.contentType === 'VIDEO' 
        ? `时长：${formatDuration(randomRecommendation.metadata?.duration)}`
        : randomRecommendation.contentType === 'QUIZ'
          ? `题目数：${randomRecommendation.metadata?.questionCount || 0}题`
          : '';

      // 创建通知实例
      const notification = ElNotification({
        title: '学习推荐',
        dangerouslyUseHTMLString: true,
        message: `
          <div style="margin-top: 10px">
            <p style="font-size: 16px; margin-bottom: 10px">根据您的学习进度，我们为您推荐：</p>
            <div style="background: #f0f9ff; padding: 15px; border-radius: 8px; margin-bottom: 10px">
              <div style="display: flex; align-items: center; margin-bottom: 8px">
                <span style="font-size: 20px; margin-right: 8px">${contentTypeInfo.icon}</span>
                <span style="color: #409EFF; font-weight: 500">${contentTypeInfo.text}</span>
                ${difficultyText ? `<span style="margin-left: 8px; color: #67C23A; font-size: 12px; background: #f0f9eb; padding: 2px 8px; border-radius: 4px">${difficultyText}</span>` : ''}
              </div>
              <h3 style="color: #303133; margin: 0 0 10px 0; font-size: 16px">${randomRecommendation.title}</h3>
              <p style="color: #606266; margin: 0 0 8px 0; font-size: 14px">${randomRecommendation.description}</p>
              ${tagsText ? `<p style="color: #909399; margin: 0 0 8px 0; font-size: 12px">${tagsText}</p>` : ''}
              ${contentInfo ? `<p style="color: #909399; margin: 0; font-size: 12px">${contentInfo}</p>` : ''}
            </div>
            <div style="margin-top: 15px">
              <button 
                onclick="
                  localStorage.setItem('lastRecommendationTime', '${Date.now()}');
                  window.location.href='${getRecommendationUrl(randomRecommendation)}';
                  document.querySelector('.el-notification').remove();
                "
                style="
                  width: 100%;
                  height: 40px;
                  font-size: 16px;
                  font-weight: 500;
                  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                  border: none;
                  border-radius: 8px;
                  color: white;
                  cursor: pointer;
                  transition: all 0.3s ease;
                "
                onmouseover="this.style.transform='translateY(-2px)';this.style.boxShadow='0 4px 12px rgba(102, 126, 234, 0.4)'"
                onmouseout="this.style.transform='translateY(0)';this.style.boxShadow='none'"
              >
                立即学习
              </button>
            </div>
          </div>
        `,
        type: 'info',
        duration: 0,
        position: 'bottom-left',
        showClose: true,
        customClass: 'recommendation-notification'
      });
    }
  } catch (error) {
    console.error('显示推荐失败:', error);
  }
};

const startRecommendationTimer = () => {
  if (recommendationTimer.value) {
    clearInterval(recommendationTimer.value);
  }

  // 每2分钟检查一次是否应该显示推荐
  recommendationTimer.value = setInterval(() => {
    showRecommendation();
  },2 * 60 * 1000);

  // 立即显示一次推荐
  showRecommendation();
};

// 修改检查用户资料的方法
const checkUserProfile = async () => {
  try {
    // 检查缓存
    if (userProfileCache.value) {
      if (!userProfileCache.value) {
        showNewUserGuide.value = true;
      } else {
        await recordUserBehavior({
          contentType: 'PROFILE',
          contentId: 'profile_view',
          interactionType: 'VIEW',
          duration: 0,
          score: null,
          category: userProfileCache.value.interests[0]
        });
        startRecommendationTimer();
      }
      return;
    }

    // 如果没有ID，先获取用户信息
    if (!store.state.id) {
      try {
        const responseId = await request.get('/api/user/profile');
        if (responseId.data.code === 200) {
          const { id, avatar } = responseId.data.data;
          await store.dispatch('setId', id);
          await store.dispatch('setAvatar', avatar);
        } else {
          console.error('获取用户信息失败:', responseId.data.msg);
          return;
        }
      } catch (err) {
        console.error('重新获取用户信息失败:', err);
        return;
      }
    }

    // 从后端获取用户资料
    const response = await request.get(`/api/user/profile/${store.state.id}`);
    const userProfile = response.data;
    
    // 更新缓存
    userProfileCache.value = userProfile;
    
    if (!userProfile) {
      showNewUserGuide.value = true;
    } else {
      await recordUserBehavior({
        contentType: 'PROFILE',
        contentId: 'profile_view',
        interactionType: 'VIEW',
        duration: 0,
        score: null,
        category: userProfile.interests[0]
      });
      startRecommendationTimer();
    }
  } catch (error) {
    console.error('获取用户资料失败:', error);
    ElMessage.error('获取用户资料失败');
  }
};

// 修改提交资料的方法
const submitProfile = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let skillLevel;
        switch (form.skillLevel) {
          case 'beginner':
            skillLevel = 1;
            break;
          case 'intermediate':
            skillLevel = 2;
            break;
          case 'advanced':
            skillLevel = 3;
            break;
          default:
            skillLevel = 1;
        }

        const profileData = {
          userId: store.state.id,
          skillLevel: skillLevel,
          interests: form.interests,
          learningPreferences: {
            goal: form.learningGoal
          }
        };

        // 提交用户资料到后端
        await request.post('/api/user/profile', profileData);
        
        // 更新缓存
        userProfileCache.value = profileData;
        
        // 清除推荐缓存，因为用户资料已更新
        recommendationsCache.value.clear();
        
        await recordUserBehavior({
          contentType: 'PROFILE',
          contentId: 'initial_profile',
          interactionType: 'SUBMIT',
          duration: 0,
          score: 1.0,
          category: form.interests[0]
        });

        showNewUserGuide.value = false;
        startRecommendationTimer();
        ElMessage.success('个人信息已保存');
      } catch (error) {
        console.error('保存用户资料失败:', error);
        ElMessage.error('保存失败，请重试');
      }
    }
  });
};

// 添加清理缓存的方法
const clearCache = () => {
  recommendationsCache.value.clear();
  userProfileCache.value = null;
};

// 添加稍后提醒的处理函数
const handleRemindLater = () => {
  showNewUserGuide.value = false;
  setTimeout(() => {
    // 只在普通用户且已登录时弹出
    if (isOrdinaryUser.value) {
      showNewUserGuide.value = true;
    }
  }, 30 * 60 * 1000);
};

// 监听登录状态和角色变化
watch([isLoggedIn, () => store.state.roles], ([login, roles]) => {
  if (login && !(roles || []).includes('ROLE_ADMIN')) {
    checkUserProfile();
  } else {
    // 退出登录或切换为管理员时，关闭弹窗和清除定时器
    showNewUserGuide.value = false;
    if (recommendationTimer.value) {
      clearInterval(recommendationTimer.value);
      recommendationTimer.value = null;
    }
    localStorage.removeItem('lastRecommendationTime');
  }
});

// 生命周期钩子
onMounted(() => {
  if (isLoggedIn.value) {
    checkUserProfile();
  }
});

onBeforeUnmount(() => {
  if (recommendationTimer.value) {
    clearInterval(recommendationTimer.value);
    recommendationTimer.value = null;
  }
  clearCache();
});

// 添加获取兴趣领域图标的函数
const getInterestIcon = (value) => {
  const icons = {
    web: '🌐',
    mobile: '📱',
    system: '💻',
    crypto: '🔐',
    reverse: '🔄'
  };
  return icons[value] || '📚';
};
</script>

<style scoped>
.learning-guide {
  position: fixed;
  z-index: 1000;
}

.welcome-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.welcome-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 0;
  margin: 0;
  border-bottom: none;
}

.welcome-dialog :deep(.el-dialog__title) {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.guide-content {
  padding: 24px;
}

.welcome-header {
  text-align: center;
  margin-bottom: 32px;
}

.title-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 12px;
}

.welcome-logo {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.welcome-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-subtitle {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.profile-form {
  max-width: 600px;
  margin: 0 auto;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
  text-align: right;
  padding-right: 20px;
  line-height: 32px;
  width: 140px !important;
}

.profile-form :deep(.el-form-item__content) {
  line-height: 32px;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.custom-select {
  width: 100%;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.option-icon {
  font-size: 20px;
}

.option-label {
  font-weight: 500;
  color: #303133;
}

.option-desc {
  color: #909399;
  font-size: 13px;
  margin-left: auto;
}

.interest-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.interest-icon {
  font-size: 18px;
}

.interest-label {
  font-weight: 500;
}

.custom-textarea :deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.6;
  padding: 12px;
  border-radius: 8px;
  border-color: #dcdfe6;
  transition: all 0.3s;
}

.custom-textarea :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.dialog-footer {
  text-align: center;
  padding: 8px 24px 16px;
  display: flex;
  flex-direction: column;
  margin-top: -20px;
  align-items: center;
  gap: 12px;
}

.button-group {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: -8px;
}

.footer-slogan {
  margin-top: 4px;
  text-align: center;
}

.slogan-text,
.slogan-call {
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.slogan-text {
  font-style: italic;
  font-size: 14px;
}

.slogan-call {
  font-weight: bold;
  margin: 4px 0 0 0;
  font-size: 12px;
}

.submit-button {
  width: 160px;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 22px;
  transition: all 0.3s ease;
}

.remind-later-button {
  width: 120px;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  background: transparent;
  border: 1px solid #dcdfe6;
  border-radius: 22px;
  color: #606266;
  transition: all 0.3s ease;
}

.remind-later-button:hover {
  color: #409EFF;
  border-color: #409EFF;
  background: rgba(64, 158, 255, 0.1);
}

:deep(.el-select-dropdown__item) {
  padding: 8px 12px;
}

:deep(.el-select-dropdown__item.selected) {
  background-color: #f0f9ff;
  color: #409EFF;
}

:deep(.el-select-dropdown__item.selected .option-desc) {
  color: #409EFF;
}

:deep(.el-tag) {
  background-color: #f0f9ff;
  border-color: #e6f1ff;
  color: #409EFF;
  border-radius: 4px;
  padding: 0 8px;
  height: 24px;
  line-height: 22px;
}

:deep(.el-tag .el-tag__close) {
  color: #409EFF;
  background-color: transparent;
}

:deep(.el-tag .el-tag__close:hover) {
  background-color: #409EFF;
  color: white;
}

:deep(.el-select__tags) {
  margin: 0;
  padding: 0 8px;
  flex-wrap: wrap;
  gap: 4px;
}

:deep(.el-select__input) {
  margin: 0;
  height: 24px;
  line-height: 24px;
}

:deep(.el-tag) {
  margin: 2px;
  height: 24px;
  line-height: 22px;
  padding: 0 8px;
}
</style>