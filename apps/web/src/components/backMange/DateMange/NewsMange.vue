<template>
  <div class="news-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>新闻管理</span>
          <el-button type="primary" @click="showNewsDialog">发布新闻</el-button>
        </div>
      </template>

      <el-table :data="newsList" style="width: 100%">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="type" label="类型">
          <template #default="scope">
            <el-tag :type="scope.row.type">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间">
          <template #default="scope">
            {{ new Date(scope.row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editNews(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteNews(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新闻编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑新闻' : '发布新闻'"
      width="70%"
    >
      <el-form :model="newsForm" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="newsForm.title" placeholder="请输入新闻标题" />
        </el-form-item>
        
        <el-form-item label="类型" required>
          <el-select v-model="newsForm.type" placeholder="请选择新闻类型">
            <el-option label="危险" value="danger" />
            <el-option label="警告" value="warning" />
            <el-option label="信息" value="info" />
            <el-option label="成功" value="success" />
          </el-select>
        </el-form-item>

        <el-form-item label="摘要" required>
          <el-input
            v-model="newsForm.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入新闻摘要"
          />
        </el-form-item>

        <el-form-item label="详细内容" required>
          <el-input
            v-model="newsForm.fullContent"
            type="textarea"
            :rows="6"
            placeholder="请输入新闻详细内容"
          />
        </el-form-item>

        <el-form-item label="影响范围">
          <el-input
            v-model="newsForm.impact"
            type="textarea"
            :rows="4"
            placeholder="请输入影响范围"
          />
        </el-form-item>

        <el-form-item label="解决方案">
          <el-input
            v-model="newsForm.solution"
            type="textarea"
            :rows="4"
            placeholder="请输入解决方案"
          />
        </el-form-item>

        <el-form-item label="参考资料">
          <div v-for="(ref, index) in newsForm.references" :key="index" class="reference-item">
            <el-input v-model="ref.title" placeholder="标题" style="width: 200px" />
            <el-input v-model="ref.url" placeholder="URL" style="width: 400px" />
            <el-button type="danger" @click="removeReference(index)">删除</el-button>
          </div>
          <el-button type="primary" @click="addReference">添加参考</el-button>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNews">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import store from '@/store'
import ToUrl from '@/api/api'

const newsList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const newsForm = ref({
  title: '',
  type: '',
  summary: '',
  fullContent: '',
  impact: '',
  solution: '',
  references: []
})

// 加载新闻列表
const loadNews = async () => {
  try {
    const response = await axios.get(`${ToUrl.url}/api/news`, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    newsList.value = response.data
  } catch (error) {
    ElMessage.error('加载新闻列表失败')
  }
}

// 显示新闻对话框
const showNewsDialog = () => {
  isEdit.value = false
  newsForm.value = {
    title: '',
    type: '',
    summary: '',
    fullContent: '',
    impact: '',
    solution: '',
    references: []
  }
  dialogVisible.value = true
}

// 编辑新闻
const editNews = (news) => {
  isEdit.value = true
  newsForm.value = { ...news }
  dialogVisible.value = true
}

// 添加参考资料
const addReference = () => {
  newsForm.value.references.push({ title: '', url: '' })
}

// 删除参考资料
const removeReference = (index) => {
  newsForm.value.references.splice(index, 1)
}

// 保存新闻
const saveNews = async () => {
  try {
    if (!newsForm.value.title || !newsForm.value.type || !newsForm.value.summary || !newsForm.value.fullContent) {
      ElMessage.warning('请填写必填字段')
      return
    }

    const url = isEdit.value 
      ? `${ToUrl.url}/api/news/${newsForm.value.id}`
      : `${ToUrl.url}/api/news`

    const method = isEdit.value ? 'put' : 'post'

    const response = await axios[method](url, newsForm.value, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    ElMessage.success(isEdit.value ? '新闻更新成功' : '新闻发布成功')
    dialogVisible.value = false
    loadNews()
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  }
}

// 删除新闻
const deleteNews = async (news) => {
  try {
    await ElMessageBox.confirm('确定要删除这条新闻吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await axios.delete(`${ToUrl.url}/api/news/${news.id}`, {
      headers: {
        'Authorization': `Bearer ${store.state.token}`
      }
    })
    ElMessage.success('新闻删除成功')
    loadNews()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

onMounted(() => {
  loadNews()
})
</script>

<style scoped>
.news-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reference-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

.reference-item .el-input {
  flex: 1;
}

.reference-item .el-button {
  flex-shrink: 0;
  margin-left: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-dialog__body) {
  padding-top: 20px;
}

:deep(.el-button) {
  color: #fff;
}

:deep(.el-button--text) {
  color: #409EFF;
}

:deep(.el-button--text:hover) {
  color: #66b1ff;
}
</style> 