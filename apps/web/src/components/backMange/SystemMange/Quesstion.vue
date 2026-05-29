<template>
  <el-card class="question-management">
    <template #header>
      <div class="card-header">
        <span>问题管理</span>
        <el-button type="primary" size="small" @click="addQuestion">新增问题</el-button>
      </div>
    </template>

    <el-table :data="questions" stripe>
      <el-table-column prop="title" label="问题标题" />
      <el-table-column prop="category" label="分类" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="editQuestion(scope.row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="deleteQuestion(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 问题编辑对话框 -->
    <el-dialog v-model="questionDialogVisible" :title="`${isEdit ? '编辑' : '新增'}问题`" width="600px">
      <el-form :model="currentQuestion" label-width="100px">
        <el-form-item label="问题标题" prop="title">
          <el-input v-model="currentQuestion.title" placeholder="请输入问题标题" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="currentQuestion.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="选项" prop="options">
          <el-input v-for="(option, index) in currentQuestion.options" :key="index"
            v-model="currentQuestion.options[index]" :placeholder="`选项 ${String.fromCharCode(65 + index)}`"
            class="mb-2" />
        </el-form-item>
        <el-form-item label="正确答案" prop="answer">
          <el-select v-model="currentQuestion.answer" placeholder="请选择正确答案">
            <el-option v-for="(option, index) in currentQuestion.options" :key="index"
              :label="String.fromCharCode(65 + index)" :value="String.fromCharCode(65 + index)" />
          </el-select>
        </el-form-item>
        <el-form-item label="解析" prop="explanation">
          <el-input v-model="currentQuestion.explanation" type="textarea" :rows="3" placeholder="请输入解析" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="questionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import store from '@/store'
import ToUrl from '@/api/api'

// 问题数据
const questions = ref([])

// 当前操作问题
const currentQuestion = ref({
  title: '',
  category: '',
  options: ['', '', '', ''],
  answer: '',
  explanation: ''
})
const isEdit = ref(false) // 是否为编辑模式
const questionDialogVisible = ref(false)

// 加载问题数据
const loadQuestions = async () => {
  try {
    const res = await axios.get(ToUrl.url + '/admin/findAllQuestion', {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    questions.value = res.data.data
  } catch (error) {
    ElMessage.error('问题加载失败')
  }
}

// 打开新增/编辑问题对话框
const addQuestion = () => {
  currentQuestion.value = {
    title: '',
    category: '',
    options: ['', '', '', ''],
    answer: '',
    explanation: ''
  }
  isEdit.value = false
  questionDialogVisible.value = true
}

const editQuestion = (question) => {
  currentQuestion.value = { ...question }
  isEdit.value = true
  questionDialogVisible.value = true
}

// 保存问题
const saveQuestion = async () => {
  try {
    const headers = {
      Authorization: `Bearer ${store.state.token}`,
      'Content-Type': 'application/json'
    };
    if (isEdit.value) {
      // 编辑问题
      await axios.put(
        ToUrl.url + `/admin/UpdateQuestion`,
        currentQuestion.value,
        { headers }
      );
    } else {
      // 新增问题
      await axios.post(
        ToUrl.url + '/admin/insertQuestion',
        currentQuestion.value,
        { headers }
      );
    }
    ElMessage.success('保存成功');
    questionDialogVisible.value = false;
    loadQuestions(); // 重新加载问题列表
  } catch (error) {
    ElMessage.error('保存失败');
    console.error(error); // 打印错误信息，便于调试
  }
};

// 删除问题
const deleteQuestion = async (aid) => {
  try {
    const response = await axios.put(ToUrl.url + "/admin/deleteQuestion",
      { id: aid },
      {
        headers: {
          'Authorization': `Bearer ${store.state.token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    ElMessage.success('删除成功');
    loadQuestions(); // 重新加载问题列表
  } catch (error) {
    ElMessage.error('删除失败');
    console.error(error); // 打印错误信息，便于调试
  }
};

onMounted(() => {
  loadQuestions()
})
</script>

<style scoped>
.mb-2 {
  margin-bottom: 0.5rem;
}
</style>