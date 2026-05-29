<template>
  <div class="course-container">
    <!-- 添加加载状态显示 -->
    <div v-if="loadingStates.questions" class="loading-overlay">
      <el-loading :fullscreen="true" text="正在加载题库..."></el-loading>
    </div>

    <!-- 左侧导航栏 -->
    <div class="course-sidebar">
      <div class="sidebar-header">
        <h3>课程分类</h3>
      </div>
      <el-menu :default-active="activeCategory" @select="handleCategorySelect" class="category-menu" accordion>
        <el-menu-item index="exam-records" @click="showExamRecords">
          <el-icon><Document /></el-icon>
          <span>考试记录</span>
        </el-menu-item>
        <el-sub-menu v-for="category in categories" :key="category.id" :index="category.id.toString()">
          <template #title>
            <span class="menu-title">
              <el-icon><Folder /></el-icon>
              {{ category.name }}
            </span>
          </template>
          <el-menu-item v-for="subcategory in category.subcategories" :key="subcategory.id"
            :index="subcategory.id.toString()">
            <el-icon><Document /></el-icon>
            {{ subcategory.name }}
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="course-main">
      <!-- 欢迎页面 -->
      <div v-if="!showRecords && !currentExam && !activeCategory" class="welcome-page">
        <div class="welcome-content">
          <h1>欢迎来到答题测试</h1>
          <p class="welcome-description">在这里你可以学习知识，测验自己的分数，提高自己的能力</p>
          
          <div class="welcome-features">
            <div class="feature-item">
              <el-icon><Document /></el-icon>
              <div class="feature-text">
                <h3>丰富的题库资源</h3>
                <p>涵盖多个领域的精选题目，满足不同学习需求</p>
              </div>
            </div>
            <div class="feature-item">
              <el-icon><Trophy /></el-icon>
              <div class="feature-text">
                <h3>实时成绩反馈</h3>
                <p>即时查看答题结果，了解自己的学习进度</p>
              </div>
            </div>
            <div class="feature-item">
              <el-icon><DataLine /></el-icon>
              <div class="feature-text">
                <h3>学习进度追踪</h3>
                <p>记录学习历程，见证能力提升</p>
              </div>
            </div>
          </div>

          <div class="welcome-stats">
            <div class="stat-item">
              <div class="stat-number">1000+</div>
              <div class="stat-label">精选题目</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">10+</div>
              <div class="stat-label">知识领域</div>
            </div>
          </div>

          <div class="welcome-tips">
            <h3>学习建议</h3>
            <ul>
              <li>从基础题目开始，循序渐进提升难度</li>
              <li>定期复习已完成的题目，巩固知识点</li>
              <li>关注错题分析，针对性提高</li>
            </ul>
          </div>

          <div class="welcome-action">
            <el-button type="primary" size="large" @click="startFirstCategory">
              开始学习
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 考试记录视图 -->
      <div v-else-if="showRecords" class="course-content">
        <div class="content-header">
          <h2>考试记录</h2>
          <div class="search-bar">
            <el-input placeholder="搜索考试记录..." prefix-icon="Search" v-model="recordSearchQuery" />
          </div>
        </div>
        
        <div class="records-list" v-loading="loadingStates.records">
          <el-table :data="filteredRecords" style="width: 100%" v-loading="loading">
            <el-table-column prop="examTitle" label="考试名称" min-width="200" />
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="得分" width="100">
              <template #default="scope">
                <span :class="getScoreClass(scope.row.totalScore)">{{ scope.row.totalScore }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="accuracy" label="正确率" width="120">
              <template #default="scope">
                {{ scope.row.accuracy.toFixed(1) }}%
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" link @click="viewRecordDetail(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 考试详情对话框 -->
        <el-dialog
          v-model="detailDialogVisible"
          title="考试详情"
          width="70%"
          :before-close="handleDetailDialogClose"
          class="exam-detail-dialog"
        >
          <div v-if="currentRecord" class="record-detail">
            <div class="detail-header">
              <h3>{{ currentRecord.examTitle }}</h3>
              <div class="detail-meta">
                <div class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  <span>提交时间：{{ formatDate(currentRecord.submitTime) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Trophy /></el-icon>
                  <span>总分：<span class="score">{{ currentRecord.totalScore }}</span></span>
                </div>
                <div class="meta-item">
                  <el-icon><DataLine /></el-icon>
                  <span>正确率：<span class="accuracy">{{ currentRecord.accuracy.toFixed(1) }}%</span></span>
                </div>
              </div>
            </div>

            <div class="detail-content">
              <div v-for="(result, index) in currentRecord.questionResults" :key="index" class="question-result">
                <div class="question-header">
                  <div class="question-info">
                    <span class="question-number">第{{ index + 1 }}题</span>
                    <span class="question-title">{{ result.questionTitle }}</span>
                  </div>
                  <el-tag :type="result.correct ? 'success' : 'danger'" effect="dark" class="result-tag">
                    {{ result.correct ? '正确' : '错误' }}
                  </el-tag>
                </div>
                <div class="answer-info">
                  <div class="answer-item">
                    <span class="label">您的答案：</span>
                    <span :class="['value', result.correct ? 'correct' : 'incorrect']">
                      {{ result.userAnswer }}
                    </span>
                  </div>
                  <div class="answer-item">
                    <span class="label">正确答案：</span>
                    <span class="value correct">{{ result.correctAnswer }}</span>
                  </div>
                  <div class="answer-item">
                    <span class="label">得分：</span>
                    <span class="value score">{{ result.score }}</span>
                  </div>
                  <div class="answer-item explanation" v-if="result.explanation">
                    <span class="label">解析：</span>
                    <span class="value">{{ result.explanation }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-dialog>
      </div>

      <!-- 试卷列表视图 -->
      <div v-else-if="!currentExam" class="course-content">
        <div class="content-header">
          <h2>测试题库</h2>
          <div class="search-bar">
            <el-input placeholder="搜索测试..." prefix-icon="Search" />
          </div>
        </div>
        
        <div class="exam-grid" v-loading="loadingStates.exams">
          <div v-for="exam in filteredExams" :key="exam.id" 
               class="exam-card" 
               :class="{ 'disabled-card': exam.isSubmitted }" 
               @click="startExam(exam)">
            <div class="card-cover">
              <el-image :src="getRandomCourseImage()" fit="cover" />
              <div class="card-tag" v-if="exam.isSubmitted">
                <el-tag type="success" effect="dark">已完成</el-tag>
              </div>
            </div>
            <div class="card-content">
              <h3>{{ exam.title }}</h3>
              <p>{{ exam.description }}</p>
              <div class="card-meta">
                <span><el-icon><Timer /></el-icon> {{ exam.duration }}分钟</span>
                <span><el-icon><Document /></el-icon> {{ exam.questionCount }}题</span>
              </div>
              <div class="card-footer">
                <el-button type="primary" plain>开始测试</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 考试界面 -->
      <div v-else class="exam-interface">
        <div class="exam-header">
          <div class="header-left">
            <el-button @click="cancelExam" class="back-button">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <h2>{{ currentExam.title }}</h2>
          </div>
          <div class="timer" v-if="!currentExam?.isSubmitted">
            <el-icon><Clock /></el-icon>
            <span>剩余时间: {{ formattedTime }}</span>
          </div>
        </div>

        <!-- 答题部分 - 只在未提交时显示 -->
        <div v-if="!currentExam?.isSubmitted" class="exam-content">
          <el-form :model="answers" ref="examForm">
            <div v-for="(question, index) in currentExam.questions" :key="question.id" class="question-item">
              <div class="question-header">
                <span class="question-number">第{{ index + 1 }}题</span>
                <h4>{{ question.title }}</h4>
              </div>
              <el-form-item :prop="`${question.id}.value`" :rules="[{ validator: validateAnswer(question) }]">
                <el-radio-group v-if="question.type === 'single'" v-model="answers[question.id].value">
                  <el-radio v-for="option in question.options" :key="option.value" :label="option.value">
                    <span class="option-content">{{ option.label }}</span>
                  </el-radio>
                </el-radio-group>
                <el-checkbox-group v-if="question.type === 'multiple'" v-model="answers[question.id].value">
                  <el-checkbox v-for="option in question.options" :key="option.value" :label="option.value">
                    <span class="option-content">{{ option.label }}</span>
                  </el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </div>
          </el-form>
        </div>

        <!-- 结果展示 - 只在提交后显示 -->
        <div v-if="currentExam?.isSubmitted" class="exam-results">
          <div class="result-header">
            <el-alert :title="`本次得分: ${scoreInfo.scoreDelta}`" type="success" show-icon />
          </div>
          
          <div class="result-content">
            <div v-for="question in currentExam.questions" :key="question.id" class="result-item">
              <div class="result-question">
                <h4 :class="getResultClass(question.id)">
                  {{ question.title }}
                  <span class="result-tag">{{ getResultTag(question.id) }}</span>
                </h4>
                
                <div class="answer-section">
                  <div class="correct-answer">
                    <span class="label">正确答案:</span>
                    <span class="value">{{ resultDetails[question.id]?.message || '未获取到答案' }}</span>
                    <span class="score-delta" :class="getScoreClass(resultDetails[question.id]?.score || 0)">
                      {{ resultDetails[question.id]?.score ? `+${resultDetails[question.id].score}` : '0' }}
                    </span>
                  </div>
                  
                  <div class="user-answer">
                    <span class="label">您的答案:</span>
                    <span class="value">{{ formatUserAnswer(question.id) || "未作答" }}</span>
                  </div>
                </div>

                <div class="result-message" v-if="resultDetails[question.id]?.message && resultDetails[question.id]?.correct">
                  {{ resultDetails[question.id].message }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 提交按钮 - 只在未提交时显示 -->
        <div v-if="!currentExam?.isSubmitted" class="exam-footer">
          <el-button type="primary" size="large" @click="submitExam">提交试卷</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Folder, Document, Timer, ArrowLeft, Search, Calendar, Trophy, DataLine, ArrowRight } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import axios from 'axios'
import ToUrl from '@/api/api'

const store = useStore()

// 响应式数据
const activeCategory = ref('')
const currentExam = ref(null)
const timeLeft = ref(0)
const answers = ref({})
const timer = ref(null)
const questions = ref([]) // 存储原始题目数据
const categories = ref([]) // 动态生成的分类结构
const resultDetails = ref({})
const scoreInfo = ref({
  scoreDelta: 0,
  totalScore: 0
})
const exams = ref([]) // 改为ref而不是computed
const examForm = ref(null)

// 考试记录相关
const showRecords = ref(false)
const recordSearchQuery = ref('')
const loading = ref(false)

// 考试详情相关
const detailDialogVisible = ref(false)
const currentRecord = ref(null)

// 修改缓存相关的响应式变量
const questionCache = ref(new Map());
const examCache = ref(new Map());

const loadingStates = ref({
  questions: false,
  exams: false,
  records: false
});

const fetchWithRetry = async (url, options, maxRetries = 3) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await axios.get(url, options);
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
      ElMessage.warning(`请求失败，正在进行第${i + 2}次重试...`);
    }
  }
};

// 修改获取试题数据的方法
const fetchQuestions = async () => {
  try {
    loadingStates.value.questions = true;
    // 检查缓存
    const cacheKey = 'all_questions';
    if (questionCache.value.has(cacheKey)) {
      questions.value = questionCache.value.get(cacheKey);
      generateCategories();
      return;
    }

    // 使用新的接口一次性获取100道题目，带重试机制
    const response = await fetchWithRetry(ToUrl.url+'/api/more_questions', {
      headers: { Authorization: `Bearer ${store.state.token}` }
    });

    if (response.data.code === 200) {
      questions.value = response.data.data;
      // 存入缓存
      questionCache.value.set(cacheKey, response.data.data);
      generateCategories();
    }
  } catch (err) {
    ElMessage.error('试题加载失败: ' + err.message);
  } finally {
    loadingStates.value.questions = false;
  }
}

//获取分类方法
const fetchCategories = async () => {
  try {
    const response = await axios.get(ToUrl.url+'/api/get_challenge', {
      headers: { Authorization: `Bearer ${store.state.token}` }
    })

    if (response.data.code === 200 && response.data.data) {
      processCategories(response.data.data)
    } else {
      throw new Error('获取分类数据失败')
    }
  } catch (err) {
    console.error('分类加载失败:', err)
    ElMessage.error('分类加载失败: ' + err.message)
  }
}

// 分类数据处理方法
const processCategories = (data) => {
  try {
    // 转换分类数据结构
    const mainCategories = Object.entries(data)
      .filter(([key, value]) => key.startsWith('challengel') && value !== null)
      .map(([key, value]) => ({
        id: key,
        name: value || key, // 如果value为null，使用key作为name
        subcategories: [
          {
            id: `${key}-basic`,
            name: `${value || key}基础测试`,
            type: 'basic'
          },
          {
            id: `${key}-advanced`,
            name: `${value || key}进阶测试`,
            type: 'advanced'
          }
        ]
      }));

    categories.value = mainCategories;

    if (categories.value.length > 0) {
      activeCategory.value = categories.value[0].subcategories[0].id;
    }
  } catch (error) {
    console.error('处理分类数据失败:', error);
    ElMessage.error('处理分类数据失败，请刷新页面重试');
  }
};

// 动态生成分类结构
const generateCategories = () => {
  const categoryMap = new Map()

  questions.value.forEach(question => {
    const processedQuestion = {
      ...question,
      type: 'single',
      // 将选项转换为字母形式
      options: question.options.map((text, index) => ({
        value: String.fromCharCode(65 + index), // 0->A, 1->B...
        label: `${String.fromCharCode(65 + index)}. ${text}`
      })),
      // 转换正确答案为字母形式
      answer: String.fromCharCode(65 + question.options.findIndex(opt => opt === question.answer))
    }

    if (!categoryMap.has(question.category)) {
      categoryMap.set(question.category, {
        id: question.category,
        name: question.category,
        questions: [],
        subcategories: [{ id: 'default', name: '全部题目' }]
      })
    }
    categoryMap.get(question.category).questions.push(processedQuestion)
  })

  categories.value = Array.from(categoryMap.values())
  if (categories.value.length > 0) {
    activeCategory.value = categories.value[0].id
  }
}
// 题目处理方法
const processQuestion = (question) => ({
  ...question,
  type: 'single',
  options: question.options.map((text, i) => ({
    value: String.fromCharCode(65 + i),
    label: `${String.fromCharCode(65 + i)}. ${text}`
  })),
  answer: String.fromCharCode(65 + question.options.indexOf(question.answer))
})

// 计算属性
const filteredExams = computed(() => {
  return exams.value.filter(exam =>
    exam.categoryId === activeCategory.value.split('-')[0] && // 匹配分类ID
    !exam.isSubmitted
  )
})

// 答案校验逻辑
const allQuestionsAnswered = computed(() => {
  if (!currentExam.value) return false
  return currentExam.value.questions.every(question => {
    const answer = answers.value[question.id]?.value
    // 修改验证逻辑，确保答案不为空
    return answer !== undefined && answer !== null && answer !== ''
  })
})

// 提交方法
async function submitExam() {
  try {
    if (!examForm.value) {
      ElMessage.error('表单未初始化');
      return;
    }
    
    // 验证表单
    try {
      await examForm.value.validate();
    } catch (validationError) {
      console.error('表单验证失败:', validationError);
      ElMessage.warning('请完成所有必答题');
      return;
    }

    // 检查答案是否完整
    if (!allQuestionsAnswered.value) {
      ElMessage.warning('请完成所有题目后再提交');
      return;
    }

    // 计算考试用时并格式化为整数分钟数
    const costTimeMinutes = Math.floor((currentExam.value.duration * 60 - timeLeft.value) / 60);

    // 准备提交数据
    const submitData = {
      examId: currentExam.value?.id,
      answers: {},
      costTime: costTimeMinutes // 直接使用整数分钟数
    };

    // 处理答案数据
    Object.entries(answers.value).forEach(([questionId, answer]) => {
      if (answer && answer.value !== undefined) {
        submitData.answers[questionId] = answer.value;
      }
    });

    console.log('准备提交的数据:', submitData);

    // 检查数据完整性
    if (!submitData.examId) {
      ElMessage.error('考试ID不存在');
      return;
    }

    if (Object.keys(submitData.answers).length === 0) {
      ElMessage.error('没有可提交的答案');
      return;
    }

    const response = await axios.post(ToUrl.url+'/api/submit', submitData, {
      headers: { 
        Authorization: `Bearer ${store.state.token}`,
        'Content-Type': 'application/json'
      }
    });

    if (response.data.code === 200) {
      const result = response.data.data;
      
      // 更新分数信息
      scoreInfo.value = {
        scoreDelta: result.scoreDelta || 0,
        totalScore: result.totalScore || 0
      };

      // 更新详细结果
      if (result.details) {
        result.details.forEach(detail => {
          if (detail && detail.questionId) {
            resultDetails.value[detail.questionId] = {
              correct: detail.correct,
              score: detail.score || 0, // 确保有分数值
              message: detail.explanation,
              userAnswer: detail.userAnswer,
              correctAnswer: detail.correctAnswer
            };
          }
        });
      }

      // 更新考试状态
      const examIndex = exams.value.findIndex(e => e.id === currentExam.value.id);
      if (examIndex !== -1) {
        exams.value[examIndex].isSubmitted = true;
      }

      currentExam.value.isSubmitted = true;
      
      // 显示提交成功消息
      ElMessage({
        type: 'success',
        message: `提交成功！本次得分：${result.scoreDelta}，总分：${result.totalScore}`,
        duration: 5000
      });
    } else {
      throw new Error(response.data.message || '提交失败');
    }
  } catch (err) {
    console.error('提交错误:', err);
    ElMessage.error(err.message || '提交失败，请稍后重试');
  }
}

// 在onMounted中调整调用顺序
onMounted(async () => {
  await fetchQuestions();  // 先获取题目
  await fetchCategories(); // 再获取分类（分类处理需要题目数据）
  linkQuestionsToCategories(); // 最后关联题目到分类
  // 移除自动加载第一个分类的代码，让欢迎页面显示
  activeCategory.value = ''; // 确保初始状态为空
});

const formattedTime = computed(() => {
  const minutes = Math.floor(timeLeft.value / 60)
  const seconds = timeLeft.value % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
})

// 方法
const handleCategorySelect = async (subCategoryId) => {
  if (subCategoryId === 'exam-records') {
    showExamRecords();
    return;
  }

  showRecords.value = false;
  activeCategory.value = subCategoryId; // 设置当前选中的分类

  try {
    loadingStates.value.exams = true;
    const loadingInstance = ElMessage({
      type: 'info',
      message: '正在加载题目...',
      duration: 0,
      icon: Clock,
    });

    const [categoryId, type] = subCategoryId.split('-');
    const category = categories.value.find(c => c.id === categoryId);

    if (!category || !category.name) {
      ElMessage.error('分类信息不完整，请刷新页面重试');
      return;
    }

    // 检查缓存
    const cacheKey = `${categoryId}-${type}`;
    if (examCache.value.has(cacheKey)) {
      exams.value = examCache.value.get(cacheKey);
      activeCategory.value = subCategoryId;
      loadingInstance.close();
      return;
    }

    // 从已加载的题目中筛选符合分类和难度的题目
    const filteredQuestions = questions.value.filter(q => 
      q.category === category.name && 
      q.difficulty === (type === 'basic' ? 1 : 2)
    );

    // 如果筛选后的题目不足，则请求新题目
    if (filteredQuestions.length < 10) {
      const response = await fetchWithRetry(ToUrl.url+'/api/more_questions', {
        params: {
          category: category.name,
          difficulty: type === 'basic' ? 1 : 2
        },
        headers: { 
          Authorization: `Bearer ${store.state.token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.data.code === 200) {
        const newQuestions = response.data.data;
        // 将新题目添加到题库
        questions.value = [...questions.value, ...newQuestions];
        // 更新筛选后的题目列表
        filteredQuestions.push(...newQuestions);
        // 更新缓存
        questionCache.value.set('all_questions', questions.value);
      }
    }

    // 处理题目数据
    const processedQuestions = filteredQuestions.map(q => ({
      ...q,
      id: q.id,
      type: 'single',
      options: q.options.map((text, i) => ({
        value: String.fromCharCode(65 + i),
        label: `${String.fromCharCode(65 + i)}. ${text}`
      })),
      answer: String.fromCharCode(65 + q.options.indexOf(q.answer))
    }));

    // 创建考试卡片
    const newExams = [];
    const questionsPerExam = 10;
    const totalExams = Math.min(10, Math.floor(processedQuestions.length / questionsPerExam));

    for (let i = 0; i < totalExams; i++) {
      const startIndex = i * questionsPerExam;
      const endIndex = startIndex + questionsPerExam;
      const examQuestions = processedQuestions.slice(startIndex, endIndex);

      newExams.push({
        id: `${subCategoryId}-card-${i + 1}`,
        categoryId: categoryId,
        title: `${category.name} - ${type === 'basic' ? '基础' : '进阶'}测试 ${i + 1}`,
        description: `${type === 'basic' ? '基础' : '进阶'}测试题库 - 第${i + 1}套`,
        duration: 60,
        questionCount: examQuestions.length,
        isSubmitted: false,
        questions: examQuestions
      });
    }

    // 更新exams数组并缓存
    exams.value = newExams;
    examCache.value.set(cacheKey, newExams);
    activeCategory.value = subCategoryId;
    
    if (newExams.length === 0) {
      ElMessage.warning('没有可用的题目，您可能已经完成了所有题目');
    } else {
      ElMessage.success(`成功加载${newExams.length}套试题，每套${questionsPerExam}道题目`);
      // 预加载下一个分类
      preloadNextCategory();
    }

    loadingInstance.close();
  } catch (error) {
    console.error('题目加载失败:', error);
    ElMessage.error('题目加载失败: ' + (error.response?.data?.message || error.message));
  } finally {
    loadingStates.value.exams = false;
  }
};

// 开始考试逻辑
const startExam = (exam) => {
  // 重置考试记录显示状态
  showRecords.value = false;
  
  if (exam.questions.length === 0) {
    ElMessage.warning('请先加载题目');
    return;
  }
  currentExam.value = {
    ...exam,
    questions: exam.questions.map(q => ({
      ...q,
      id: q.id // 直接使用原始ID
    }))
  };
  timeLeft.value = exam.duration * 60;
  initializeAnswers();
  startTimer();
};

function initializeAnswers() {
  answers.value = currentExam.value.questions.reduce((acc, question) => {
    acc[question.id] = {
      value: '',  // 初始化为空字符串
      questionId: question.id,
      timestamp: Date.now()
    }
    return acc
  }, {})
}

function validateAnswer(question) {
  return (_, value, callback) => {
    console.log('Validating answer for question:', question.id, 'Value:', value);
    const answer = answers.value[question.id]?.value;
    console.log('Current answer in answers object:', answer);
    
    if (!answer || (Array.isArray(answer) && answer.length === 0)) {
      callback(new Error('请完成此题作答'));
      return;
    }
    callback();
  }
}

function startTimer() {
  timer.value = setInterval(() => {
    if (timeLeft.value <= 0) {
      submitExam()
      return
    }
    timeLeft.value--
  }, 1000)
}

function cancelExam() {
  // 如果已经提交，直接返回
  if (currentExam.value?.isSubmitted) {
    clearInterval(timer.value)
    currentExam.value = null
    return
  }

  // 未提交时显示确认提示
  ElMessageBox.confirm(
    '确认要退出考试吗？退出后答题记录将不会保存。',
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      clearInterval(timer.value)
      currentExam.value = null
      ElMessage({
        type: 'info',
        message: '已退出考试'
      })
    })
    .catch(() => {
      // 用户点击取消，不做任何操作
    })
}

// 修改结果展示方法
const formatCorrectAnswer = (question) => {
  if (!question.options) return question.answer;
  const correct = question.options.find(opt => opt.value === question.answer);
  return correct ? `${question.answer}. ${correct.label.split('. ')[1]}` : question.answer;
}

const formatUserAnswer = (questionId) => {
  const result = resultDetails.value[questionId];
  if (!result) return '未作答';
  
  const question = currentExam.value.questions.find(q => q.id === questionId);
  if (!question || !question.options) return result.userAnswer;

  const option = question.options.find(o => o.value === result.userAnswer);
  return option ? `${result.userAnswer}. ${option.label.split('. ')[1]}` : result.userAnswer;
}

// 修改获取结果标签方法
const getResultTag = (questionId) => {
  const result = resultDetails.value[questionId];
  if (!result) return '题目无效';
  return result.correct ? '正确' : '错误';
}

// 修改获取结果类名方法
const getResultClass = (questionId) => {
  const result = resultDetails.value[questionId];
  if (!result) return 'invalid-question';
  return result.correct ? 'correct' : 'incorrect';
}

// 题目关联方法
const linkQuestionsToCategories = () => {
  categories.value.forEach(category => {
    // 筛选本分类题目
    const categoryQuestions = questions.value
      .filter(q => q.category === category.name)
      .map(processQuestion)

    // 分配到主分类
    category.questions = categoryQuestions

    // 分配到子分类（示例：平均分配）
    category.subcategories.forEach((sub, index) => {
      const chunkSize = Math.ceil(categoryQuestions.length / category.subcategories.length)
      sub.questions = categoryQuestions.slice(
        index * chunkSize,
        (index + 1) * chunkSize
      )
    })
  })
}

// 随机课程图片方法
const getRandomCourseImage = () => {
  const images = [
    'https://images.unsplash.com/photo-1501504905252-473c47e087f8',
    'https://images.unsplash.com/photo-1516321318423-f06f85e504b3',
    'https://images.unsplash.com/photo-1516321497487-e288fb19713f',
    'https://images.unsplash.com/photo-1516321318423-f06f85e504b3'
  ];
  
  // 预加载图片
  images.forEach(url => {
    const img = new Image();
    img.src = url;
  });
  
  return images[Math.floor(Math.random() * images.length)];
}

// 添加清理缓存的方法
const clearCache = () => {
  questionCache.value.clear();
  examCache.value.clear();
};

// 在组件卸载时清理缓存
onUnmounted(() => {
  clearInterval(timer.value);
  clearCache();
});

// 获取考试记录的方法
async function fetchUserExamRecords() {
  try {
    const response = await axios.get(ToUrl.url+'/api/exam-records', {
      headers: { 
        Authorization: `Bearer ${store.state.token}`,
        'Content-Type': 'application/json'
      }
    });

    if (response.data.code === 200) {
      const records = response.data.data;
      // 处理考试记录数据
      console.log('考试记录:', records);
      return records;
    }
  } catch (error) {
    console.error('获取考试记录失败:', error);
    ElMessage.error('获取考试记录失败');
  }
  return [];
}

// 可以添加一个显示考试记录的组件
const examRecords = ref([]);

// 在组件挂载时获取记录
onMounted(async () => {
  examRecords.value = await fetchUserExamRecords();
});

// 格式化日期
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 修改获取分数样式方法
const getScoreClass = (score) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 显示考试记录
const showExamRecords = async () => {
  showRecords.value = true;
  currentExam.value = null;
  loadingStates.value.records = true;
  try {
    const response = await fetchWithRetry(ToUrl.url+'/api/exam-records', {
      headers: { 
        Authorization: `Bearer ${store.state.token}`,
        'Content-Type': 'application/json'
      }
    });

    if (response.data.code === 200) {
      examRecords.value = response.data.data;
    } else {
      throw new Error(response.data.message || '获取考试记录失败');
    }
  } catch (error) {
    console.error('获取考试记录失败:', error);
    ElMessage.error('获取考试记录失败: ' + error.message);
  } finally {
    loadingStates.value.records = false;
  }
};

// 过滤考试记录
const filteredRecords = computed(() => {
  if (!recordSearchQuery.value) return examRecords.value
  const query = recordSearchQuery.value.toLowerCase()
  return examRecords.value.filter(record => 
    record.examTitle.toLowerCase().includes(query) ||
    formatDate(record.submitTime).toLowerCase().includes(query)
  )
})

// 查看记录详情
const viewRecordDetail = async (record) => {
  try {
    const response = await axios.get(`${ToUrl.url}/api/exam-records/${record.id}`, {
      headers: { 
        Authorization: `Bearer ${store.state.token}`,
        'Content-Type': 'application/json'
      }
    });

    if (response.data.code === 200) {
      currentRecord.value = response.data.data;
      detailDialogVisible.value = true;
    } else {
      throw new Error(response.data.message || '获取考试详情失败');
    }
  } catch (error) {
    console.error('获取考试详情失败:', error);
    ElMessage.error('获取考试详情失败: ' + error.message);
  }
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  detailDialogVisible.value = false;
  currentRecord.value = null;
}

// 格式化答案显示
const formatAnswer = (answer) => {
  if (!answer) return '未作答';
  return answer;
}

// 添加刷新题库的方法
const refreshQuestions = async () => {
  try {
    const response = await axios.get(ToUrl.url+'/api/more_questions', {
      headers: { Authorization: `Bearer ${store.state.token}` }
    });

    if (response.data.code === 200) {
      questions.value = response.data.data;
      // 更新缓存
      questionCache.value.set('all_questions', response.data.data);
      // 清除试卷缓存，因为题库已更新
      examCache.value.clear();
    }
  } catch (error) {
    console.error('刷新题库失败:', error);
  }
};

// 定期刷新题库
let refreshTimer = null;
onMounted(() => {
  // 每10分钟刷新一次题库
  refreshTimer = setInterval(refreshQuestions, 10 * 60 * 1000);
});

const preloadNextCategory = async () => {
  const nextCategory = getNextCategory();
  if (nextCategory) {
    const nextSubCategory = nextCategory.subcategories[0].id;
    try {
      loadingStates.value.exams = true;
      await handleCategorySelect(nextSubCategory);
    } catch (error) {
      console.error('预加载失败:', error);
    } finally {
      loadingStates.value.exams = false;
    }
  }
};

// 获取下一个分类
const getNextCategory = () => {
  if (!categories.value.length) return null;
  
  const currentIndex = categories.value.findIndex(c => c.id === activeCategory.value.split('-')[0]);
  if (currentIndex === -1 || currentIndex === categories.value.length - 1) {
    return categories.value[0];
  }
  return categories.value[currentIndex + 1];
};

// 添加开始第一个分类的方法
const startFirstCategory = () => {
  if (categories.value.length > 0 && categories.value[0].subcategories.length > 0) {
    const firstSubCategory = categories.value[0].subcategories[0].id;
    handleCategorySelect(firstSubCategory);
  }
};
</script>

<style lang="scss" scoped>
.course-container {
  display: flex;
  height: 100vh;
  background-color: transparent;
}

.course-sidebar {
  width: 280px;
  background: transparent;
  border-right: 1px solid rgba(228, 231, 237, 0.5);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  padding: 20px 0;

  .sidebar-header {
    padding: 0 20px 20px;
    border-bottom: 1px solid rgba(228, 231, 237, 0.5);
    
    h3 {
      margin: 0;
      color: #ffffff;
      font-size: 18px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
      
      &::before {
        content: '';
        display: block;
        width: 4px;
        height: 16px;
        background: #8b5cf6;
        border-radius: 2px;
      }
    }
  }

  .category-menu {
    border-right: none;
    background: transparent !important;
    
    // 基础菜单项样式
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      height: 44px;
      line-height: 44px;
      padding: 0 20px;
      color: #ffffff;
      font-size: 14px;
      background: transparent !important;
      
      .menu-title {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .el-icon {
          font-size: 16px;
          color: rgba(255, 255, 255, 0.7);
        }
      }
      
      &:hover {
        background: rgba(0, 0, 0, 0.5) !important;
        color: #ffffff;
        
        .el-icon {
          color: #ffffff;
        }
      }
    }
    
    // 激活状态样式
    :deep(.el-menu-item.is-active) {
      background: rgba(0, 0, 0, 0.3) !important;
      color: #ffffff;
      font-weight: 500;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        height: 100%;
        width: 4px;
        background: #8b5cf6;
      }
      
      .el-icon {
        color: #ffffff;
      }
    }
    
    // 子菜单样式
    :deep(.el-sub-menu) {
      // 子菜单标题
      .el-sub-menu__title {
        background: transparent !important;
        
        &:hover {
          background: rgba(0, 0, 0, 0.5) !important;
        }
      }
      
      // 子菜单项
      .el-menu-item {
        padding-left: 40px;
        height: 40px;
        line-height: 40px;
        font-size: 13px;
        color: #ffffff;
        background: transparent !important;
        
        &:hover {
          color: #ffffff;
          background: rgba(0, 0, 0, 0.5) !important;
        }
        
        &.is-active {
          color: #ffffff;
          background: rgba(0, 0, 0, 0.3) !important;
        }
        
        &::before {
          display: none;
        }
      }
      
      // 子菜单容器
      :deep(.el-menu) {
        background: transparent !important;
        border-radius: 4px;
        margin: 4px 0;
        padding: 4px 0;
      }
      
      // 内联子菜单
      :deep(.el-menu--inline) {
        background-color: transparent !important;
      }
      
      // 弹出式子菜单
      :deep(.el-menu--popup) {
        background: rgba(0, 0, 0, 0.3) !important;
        backdrop-filter: blur(10px);
        border: none;
        border-radius: 8px;
        padding: 4px;
        
        .el-menu-item {
          color: #ffffff;
          border-radius: 4px;
          margin: 2px 0;
          
          &:hover {
            color: #ffffff;
            background: rgba(0, 0, 0, 0.5) !important;
          }
          
          &.is-active {
            background: rgba(0, 0, 0, 0.3) !important;
          }
        }
      }
    }
    
    // 展开状态样式
    :deep(.el-sub-menu.is-opened) {
      > .el-sub-menu__title {
        color: #ffffff;
        background: rgba(0, 0, 0, 0.5) !important;
        
        .el-icon {
          color: #ffffff;
        }
      }
    }
  }
}

// 添加全局样式覆盖
:deep(.el-menu) {
  background-color: transparent !important;
}

:deep(.el-menu--popup) {
  background-color: rgba(0, 0, 0, 0.3) !important;
}

:deep(.el-menu--inline) {
  background-color: transparent !important;
}

:deep(.el-sub-menu .el-menu) {
  background-color: transparent !important;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  background-color: transparent !important;
}

:deep(.el-menu-item.is-active) {
  background-color: rgba(0, 0, 0, 0.3) !important;
}

:deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  background-color: rgba(0, 0, 0, 0.5) !important;
}

.result-message{
  color: black;
}

.course-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  &::-webkit-scrollbar {
    display: none;
  }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.course-content {
  .content-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    h2 {
      margin: 0;
      color: #ffffff;
      font-size: 24px;
      font-weight: 600;
    }
    
    .search-bar {
      width: 300px;
    }
  }
}

.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.exam-card {
  background: transparent;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid rgba(228, 231, 237, 0.5);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
  
  &.disabled-card {
    opacity: 0.7;
    cursor: not-allowed;
  }
  
  .card-cover {
    position: relative;
    height: 160px;
    
    .el-image {
      width: 100%;
      height: 100%;
    }
    
    .card-tag {
      position: absolute;
      top: 12px;
      right: 12px;
    }
  }
  
  .card-content {
    padding: 20px;
    background: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(10px);
    
    h3 {
      margin: 0 0 12px;
      font-size: 18px;
      color: #ffffff;
    }
    
    p {
      color: rgba(255, 255, 255, 0.8);
      margin-bottom: 16px;
      line-height: 1.5;
    }
    
    .card-meta {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
      color: rgba(255, 255, 255, 0.7);
      font-size: 14px;
      
      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
    
    .card-footer {
      text-align: center;
      
      .el-button {
        background: rgba(139, 92, 246, 0.8);
        border: none;
        color: #ffffff;
        padding: 8px 24px;
        font-size: 14px;
        border-radius: 6px;
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(139, 92, 246, 1);
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
        }
        
        &:active {
          transform: translateY(0);
        }
      }
    }
  }
}

.exam-interface {
  background: transparent;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 24px;
  
  .exam-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
    padding-bottom: 16px;
    border-bottom: 1px solid rgba(228, 231, 237, 0.5);
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .back-button {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 8px 16px;
        font-size: 14px;
        border: none;
        background: transparent;
        color: #ffffff;
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(255, 255, 255, 0.1);
          border-radius: 4px;
        }
        
        .el-icon {
          font-size: 16px;
        }
      }
      
      h2 {
        margin: 0;
        font-size: 24px;
        color: #ffffff;
      }
    }
    
    .timer {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      background: rgba(139, 92, 246, 0.1);
      border-radius: 20px;
      color: #ffffff;
      font-weight: 500;
    }
  }
  
  .question-item {
    margin-bottom: 32px;
    padding: 24px;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    border: 1px solid rgba(228, 231, 237, 0.5);
    
    .question-header {
      margin-bottom: 20px;
      
      .question-number {
        display: inline-block;
        padding: 4px 12px;
        background: #e4e7ed;
        border-radius: 12px;
        font-size: 14px;
        color: #6b7280;
        margin-bottom: 8px;
      }
      
      h4 {
        margin: 0;
        font-size: 18px;
        color: #2c3e50;
      }
    }
    
    .option-content {
      display: inline-block;
      padding: 8px 16px;
      background: rgba(255, 255, 255, 0.9);
      border-radius: 8px;
      margin-left: 8px;
      border: 1px solid rgba(228, 231, 237, 0.5);
    }
  }
  
  .exam-footer {
    margin-top: 40px;
    text-align: center;
  }
}

.exam-results {
  margin-top: 20px;
  
  .result-header {
    margin-bottom: 24px;
    
    .el-alert {
      background: rgba(103, 194, 58, 0.1);
      border: 1px solid rgba(103, 194, 58, 0.2);
      
      :deep(.el-alert__title) {
        font-size: 18px;
        font-weight: 600;
      }
    }
  }
  
  .result-content {
    .result-item {
      margin-bottom: 24px;
      padding: 20px;
      background: rgba(255, 255, 255, 0.8);
      backdrop-filter: blur(10px);
      border-radius: 12px;
      border: 1px solid rgba(228, 231, 237, 0.5);
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      
      .result-question {
        h4 {
          margin: 0 0 16px;
          font-size: 18px;
          color: #2c3e50;
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          &.correct {
            color: #67c23a;
          }
          
          &.incorrect {
            color: #f56c6c;
          }
        }
        
        .result-tag {
          padding: 4px 12px;
          border-radius: 12px;
          font-size: 14px;
          font-weight: 500;
          
          .correct & {
            background: #f0f9eb;
            color: #67c23a;
          }
          
          .incorrect & {
            background: #fee2e2;
            color: #f56c6c;
          }
        }
      }
    }
  }
}

:deep(.el-radio),
:deep(.el-checkbox) {
  margin-right: 16px;
  margin-bottom: 12px;
  
  &.is-checked {
    .option-content {
      background: #f0e6ff;
      color: #8b5cf6;
    }
  }
}

:deep(.el-radio__input.is-checked .el-radio__inner),
:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #8b5cf6;
  border-color: #8b5cf6;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__error) {
  color: #ef4444;
}

.records-list {
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.score-excellent {
  color: #67C23A;
  font-weight: bold;
}

.score-pass {
  color: #E6A23C;
  font-weight: bold;
}

.score-fail {
  color: #F56C6C;
  font-weight: bold;
}

.record-detail {
  .detail-header {
    margin-bottom: 32px;
    padding-bottom: 24px;
    border-bottom: 1px solid rgba(228, 231, 237, 0.5);

    h3 {
      margin: 0 0 16px;
      font-size: 24px;
      color: #2c3e50;
      font-weight: 600;
    }

    .detail-meta {
      display: flex;
      gap: 32px;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #606266;
        font-size: 14px;
        
        .el-icon {
          font-size: 18px;
          color: #8b5cf6;
        }
        
        .score {
          color: #67c23a;
          font-weight: 600;
          font-size: 16px;
        }
        
        .accuracy {
          color: #409eff;
          font-weight: 600;
          font-size: 16px;
        }
      }
    }
  }

  .detail-content {
    .question-result {
      margin-bottom: 24px;
      padding: 20px;
      background: #f8f9fa;
      border-radius: 12px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      }

      .question-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 16px;
        
        .question-info {
          flex: 1;
          margin-right: 16px;
          
          .question-number {
            display: inline-block;
            padding: 4px 12px;
            background: #e4e7ed;
            border-radius: 12px;
            font-size: 14px;
            color: #6b7280;
            margin-bottom: 8px;
          }
          
          .question-title {
            display: block;
            font-size: 16px;
            color: #2c3e50;
            line-height: 1.5;
          }
        }
        
        .result-tag {
          font-size: 14px;
          padding: 6px 12px;
          border-radius: 6px;
        }
      }

      .answer-info {
        .answer-item {
          display: flex;
          margin-bottom: 12px;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          .label {
            width: 80px;
            color: #606266;
            font-weight: 500;
          }
          
          .value {
            flex: 1;
            color: #2c3e50;
            
            &.correct {
              color: #67c23a;
              font-weight: 500;
            }
            
            &.incorrect {
              color: #f56c6c;
              font-weight: 500;
            }
            
            &.score {
              color: #8b5cf6;
              font-weight: 600;
            }
          }
          
          &.explanation {
            margin-top: 16px;
            padding-top: 16px;
            border-top: 1px dashed rgba(228, 231, 237, 0.5);
            
            .value {
              color: #606266;
              font-style: italic;
              line-height: 1.6;
            }
          }
        }
      }
    }
  }
}

:deep(.exam-detail-dialog) {
  .el-dialog {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    
    .el-dialog__header {
      margin: 0;
      padding: 20px 24px;
      border-bottom: 1px solid rgba(228, 231, 237, 0.5);
      
      .el-dialog__title {
        font-size: 20px;
        font-weight: 600;
        color: #2c3e50;
      }
    }
    
    .el-dialog__body {
      padding: 24px;
    }
  }
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.score-delta {
  margin-left: auto;
  font-weight: 600;
  font-size: 16px;
  
  &.score-excellent {
    color: #67C23A;
  }
  
  &.score-pass {
    color: #E6A23C;
  }
  
  &.score-fail {
    color: #F56C6C;
  }
}

.answer-section {
  margin: 16px 0;
  
  .correct-answer,
  .user-answer {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    
    .label {
      color: #6b7280;
      font-weight: 500;
      min-width: 80px;
    }
    
    .value {
      flex: 1;
      color: #2c3e50;
    }
  }
}

.welcome-page {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  height: calc(100vh - 120px);
  padding: 0;
  margin-left: 20px;
  overflow: hidden;
  
  .welcome-content {
    max-width: 1000px;
    width: 100%;
    text-align: left;
    padding: 40px;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border-radius: 24px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    
    h1 {
      font-size: 38px;
      color: #ffffff;
      margin-bottom: 20px;
      font-weight: 600;
    }
    
    .welcome-description {
      font-size: 17px;
      color: #ffffff;
      margin-bottom: 32px;
      line-height: 1.5;
      max-width: 800px;
    }
    
    .welcome-features {
      display: flex;
      flex-direction: column;
      gap: 24px;
      margin-bottom: 32px;
      
      .feature-item {
        display: flex;
        align-items: flex-start;
        gap: 16px;
        
        .el-icon {
          font-size: 36px;
          color: #ffffff;
          background: rgba(139, 92, 246, 0.2);
          padding: 10px;
          border-radius: 12px;
        }
        
        .feature-text {
          h3 {
            font-size: 18px;
            color: #ffffff;
            margin-bottom: 4px;
          }
          
          p {
            font-size: 14px;
            color: #ffffff;
            line-height: 1.4;
          }
        }
      }
    }

    .welcome-stats {
      display: flex;
      justify-content: center;
      gap: 100px;
      margin-bottom: 32px;
      padding: 24px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 16px;
      
      .stat-item {
        text-align: center;
        
        .stat-number {
          font-size: 28px;
          color: #ffffff;
          font-weight: 600;
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #ffffff;
        }
      }
    }

    .welcome-tips {
      margin-bottom: 32px;
      
      h3 {
        font-size: 18px;
        color: #ffffff;
        margin-bottom: 16px;
      }
      
      ul {
        list-style: none;
        padding: 0;
        
        li {
          font-size: 14px;
          color: #ffffff;
          margin-bottom: 10px;
          padding-left: 18px;
          position: relative;
          
          &::before {
            content: '•';
            position: absolute;
            left: 0;
            color: #ffffff;
            font-size: 16px;
          }
        }
      }
    }
    
    .welcome-action {
      .el-button {
        padding: 12px 32px;
        font-size: 15px;
        background: rgba(139, 92, 246, 0.8);
        border: none;
        border-radius: 12px;
        
        &:hover {
          background: rgba(139, 92, 246, 1);
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
        }
        
        .el-icon--right {
          margin-left: 8px;
        }
      }
    }
  }
}
</style>