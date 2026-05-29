<template>
  <div class="manage-container">
    <!-- 公告管理 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="showAnnouncementDialog">发布公告</el-button>
        </div>
      </template>
      
      <el-table :data="announcements" style="width: 100%">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="type" label="类型">
          <template #default="scope">
            <el-tag :type="scope.row.type">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tag" label="标签" />
        <el-table-column prop="createTime" label="发布时间">
          <template #default="scope">
            {{ new Date(scope.row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editAnnouncement(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteAnnouncement(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 更新日志管理 -->
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>更新日志管理</span>
          <el-button type="primary" @click="showChangelogDialog">发布更新</el-button>
        </div>
      </template>
      
      <el-table :data="changelogs" style="width: 100%">
        <el-table-column prop="version" label="版本号" />
        <el-table-column prop="type" label="类型">
          <template #default="scope">
            <el-tag :type="getChangelogType(scope.row.type)">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="更新内容" />
        <el-table-column prop="createTime" label="发布时间">
          <template #default="scope">
            {{ new Date(scope.row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editChangelog(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteChangelog(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 公告编辑对话框 -->
    <el-dialog
      v-model="announcementDialogVisible"
      :title="isEdit ? '编辑公告' : '发布公告'"
      width="50%"
    >
      <el-form :model="announcementForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="announcementForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="announcementForm.type">
            <el-option label="重要" value="danger" />
            <el-option label="警告" value="warning" />
            <el-option label="信息" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="announcementForm.tag" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="announcementDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAnnouncement">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 更新日志编辑对话框 -->
    <el-dialog
      v-model="changelogDialogVisible"
      :title="isEdit ? '编辑更新日志' : '发布更新日志'"
      width="50%"
    >
      <el-form :model="changelogForm" label-width="80px">
        <el-form-item label="版本号">
          <el-input v-model="changelogForm.version" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="changelogForm.type">
            <el-option label="新功能" value="feature" />
            <el-option label="修复" value="fix" />
            <el-option label="改进" value="improvement" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="changelogForm.content"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="changelogDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveChangelog">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/announcement'
import { getChangelogs, createChangelog, updateChangelog, deleteChangelog } from '@/api/changelog'
import store from '@/store'

// 数据
const announcements = ref([])
const changelogs = ref([])
const announcementDialogVisible = ref(false)
const changelogDialogVisible = ref(false)
const isEdit = ref(false)

// 表单数据
const announcementForm = ref({
  title: '',
  content: '',
  type: 'info',
  tag: ''
})

const changelogForm = ref({
  version: '',
  type: 'feature',
  content: ''
})

// 初始化加载
onMounted(() => {
  loadAnnouncements()
  loadChangelogs()
})

// 加载公告
const loadAnnouncements = async () => {
  try {
    const res = await getAnnouncements()
    announcements.value = res.data
  } catch (error) {
    ElMessage.error('加载公告失败')
  }
}

// 加载更新日志
const loadChangelogs = async () => {
  try {
    const res = await getChangelogs()
    changelogs.value = res.data
  } catch (error) {
    ElMessage.error('加载更新日志失败')
  }
}

// 显示公告对话框
const showAnnouncementDialog = () => {
  isEdit.value = false
  announcementForm.value = {
    title: '',
    content: '',
    type: 'info',
    tag: ''
  }
  announcementDialogVisible.value = true
}

// 显示更新日志对话框
const showChangelogDialog = () => {
  isEdit.value = false
  changelogForm.value = {
    version: '',
    type: 'feature',
    content: ''
  }
  changelogDialogVisible.value = true
}

// 编辑公告
const editAnnouncement = (row) => {
  isEdit.value = true
  announcementForm.value = { ...row }
  announcementDialogVisible.value = true
}

// 编辑更新日志
const editChangelog = (row) => {
  isEdit.value = true
  changelogForm.value = { ...row }
  changelogDialogVisible.value = true
}

// 保存公告
const saveAnnouncement = async () => {
  try {
    if (isEdit.value) {
      await updateAnnouncement(announcementForm.value)
      ElMessage.success('更新公告成功')
    } else {
      await createAnnouncement(announcementForm.value)
      ElMessage.success('发布公告成功')
    }
    announcementDialogVisible.value = false
    loadAnnouncements()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 保存更新日志
const saveChangelog = async () => {
  try {
    if (isEdit.value) {
      await updateChangelog(changelogForm.value)
      ElMessage.success('更新日志成功')
    } else {
      await createChangelog(changelogForm.value)
      ElMessage.success('发布更新日志成功')
    }
    changelogDialogVisible.value = false
    loadChangelogs()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除公告
const deleteAnnouncement = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      type: 'warning'
    })
    await deleteAnnouncement(row.id)
    ElMessage.success('删除成功')
    loadAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 删除更新日志
const deleteChangelog = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该更新日志吗？', '提示', {
      type: 'warning'
    })
    await deleteChangelog(row.id)
    ElMessage.success('删除成功')
    loadChangelogs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 获取更新日志类型对应的标签类型
const getChangelogType = (type) => {
  switch (type) {
    case 'feature':
      return 'success'
    case 'fix':
      return 'warning'
    case 'improvement':
      return 'info'
    default:
      return 'info'
  }
}
</script>

<style scoped>
.manage-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 