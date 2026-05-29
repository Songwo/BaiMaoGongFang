<template>
  <div class="feedback-container">
    <div class="feedback-header">
      <h1>意见反馈</h1>
      <p>您的每一条建议都是我们进步的动力</p>
    </div>

    <div class="feedback-content">
      <el-form
        ref="feedbackForm"
        :model="feedbackData"
        :rules="rules"
        label-position="top"
        class="feedback-form"
      >
        <!-- 反馈类型 -->
        <el-form-item label="反馈类型" prop="type">
          <el-select
            v-model="feedbackData.type"
            placeholder="请选择反馈类型"
            class="feedback-select"
          >
            <el-option label="功能建议" value="feature" />
            <el-option label="问题反馈" value="bug" />
            <el-option label="使用咨询" value="question" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>

        <!-- 标题 -->
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="feedbackData.title"
            placeholder="请简要描述您的反馈"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <!-- 详细描述 -->
        <el-form-item label="详细描述" prop="content">
          <el-input
            v-model="feedbackData.content"
            type="textarea"
            :rows="6"
            placeholder="请详细描述您的反馈内容，包括：&#10;1. 您遇到的具体问题&#10;2. 问题发生的步骤&#10;3. 您的建议或期望"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <!-- 联系方式 -->
        <el-form-item label="联系方式" prop="contact">
          <el-input
            v-model="feedbackData.contact"
            placeholder="请留下您的邮箱或手机号，方便我们联系您"
          />
        </el-form-item>

        <!-- 截图上传 -->
        <el-form-item label="相关截图（选填）">
          <el-upload
            class="feedback-upload"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="3"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="upload-tip">
                支持jpg、png格式，最多上传3张图片
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            @click="submitFeedback"
            class="submit-button"
          >
            提交反馈
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/axios'

const feedbackForm = ref(null)
const submitting = ref(false)

// 反馈数据
const feedbackData = reactive({
  type: '',
  title: '',
  content: '',
  contact: '',
  files: []
})

// 表单验证规则
const rules = {
  type: [
    { required: true, message: '请选择反馈类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入反馈标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度在 5 到 50 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 10, max: 1000, message: '描述长度在 10 到 1000 个字符', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$|^1[3-9]\d{9}$/, message: '请输入正确的邮箱或手机号', trigger: 'blur' }
  ]
}

// 处理文件上传
const handleFileChange = (file) => {
  feedbackData.files.push(file)
}

// 提交反馈
const submitFeedback = async () => {
  if (!feedbackForm.value) return

  try {
    await feedbackForm.value.validate()
    submitting.value = true

    // 创建FormData对象
    const formData = new FormData()
    formData.append('type', feedbackData.type)
    formData.append('title', feedbackData.title)
    formData.append('content', feedbackData.content)
    formData.append('contact', feedbackData.contact)
    
    // 添加文件
    feedbackData.files.forEach((file, index) => {
      formData.append('files', file.raw)
    })

    // 发送到后端API
    const response = await request({
      url: '/api/feedback',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.data.success) {
      ElMessage.success('反馈提交成功！感谢您的建议')
      // 重置表单
      feedbackForm.value.resetFields()
      feedbackData.files = []
    } else {
      ElMessage.error(response.data.message || '提交失败，请稍后重试')
    }
  } catch (error) {
    console.error('提交反馈失败:', error)
    ElMessage.error(error.response?.data?.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.feedback-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
}

.feedback-header {
  text-align: center;
  margin-bottom: 40px;
  color: white;
}

.feedback-header h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
}

.feedback-header p {
  font-size: 1.1em;
  opacity: 0.8;
}

.feedback-content {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.feedback-form {
  max-width: 600px;
  margin: 0 auto;
}

:deep(.el-form-item__label) {
  color: white;
  font-size: 1.1em;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  color: #333;
}

:deep(.el-textarea__inner::placeholder) {
  color: #999;
}

:deep(.el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05);
}

:deep(.el-select-dropdown__item) {
  color: #333;
}

.feedback-select {
  width: 100%;
}

.feedback-upload {
  width: 100%;
}

.upload-tip {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9em;
  margin-top: 8px;
}

:deep(.el-upload--picture-card) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px dashed rgba(255, 255, 255, 0.2);
}

:deep(.el-upload--picture-card:hover) {
  border-color: #4CAF50;
}

.submit-button {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 12px 30px;
  font-size: 1.1em;
  transition: all 0.3s ease;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

@media (max-width: 768px) {
  .feedback-container {
    padding: 20px 15px;
  }

  .feedback-header h1 {
    font-size: 2em;
  }

  .feedback-content {
    padding: 20px;
  }

  :deep(.el-form-item__label) {
    font-size: 1em;
  }
}
</style> 