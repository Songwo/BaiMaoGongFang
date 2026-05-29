<template>
    <el-card class="video-management">
        <template #header>
            <div class="card-header">
                <span>视频管理</span>
                <div class="search-container">
                    <el-input v-model="searchKeyword" placeholder="搜索视频名称" clearable
                        style="width: 200px; margin-right: 10px" />
                    <el-select v-model="selectedCategory" placeholder="全部分类" clearable style="width: 150px">
                        <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label"
                            :value="item.value" />
                    </el-select>
                </div>
            </div>
        </template>

        <div class="upload-section">
            <!-- 选择分类 -->
            <el-select v-model="selectedUploadCategory" placeholder="选择分类" style="margin-right: 10px; width: 150px">
                <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>

            <!-- 题目 -->
            <el-input v-model="uploadTitle" placeholder="请输入视频标题" style="margin-right: 10px; width: 200px" />

            <!-- 描述 -->
            <el-input v-model="uploadDescription" placeholder="请输入视频描述" style="margin-right: 10px; width: 250px" />

            <!-- 视频上传 -->
            <el-upload class="upload-demo" multiple :limit="5" accept="video/*" :file-list="fileList" :http-request="handleUpload" :before-upload="beforeUpload">
                <el-button type="primary" class="upload_btn">点击上传</el-button>
                <template #tip>
                    <div class="el-upload__tip">
                        <span style="color: #f56c6c" v-if="!selectedUploadCategory || !uploadTitle || !uploadDescription">
                            请填写完整信息（分类、标题、描述）
                        </span>
                        <span v-else>支持 MP4/AVI 格式，大小不超过 500MB</span>
                    </div>
                </template>
            </el-upload>
        </div>

        <el-table :data="filteredVideos" stripe class="video-table">
            <el-table-column label="视频名称">
                <template #default="scope">
                    <el-button link type="primary" @click="handlePreview(scope.row)">
                        {{ scope.row.title }}
                    </el-button>
                </template>
            </el-table-column>
            <el-table-column prop="description" label="视频描述" width="180" />
            <el-table-column prop="categories" label="分类" width="120">
                <template #default="scope">
                    {{ formatCategory(scope.row.categories) }}
                </template>
            </el-table-column>
            <el-table-column prop="createTime" label="上传时间" width="180" >
                <template #default="scope">
                    {{ formatTime(scope.row.createTime) }}
                </template>
            </el-table-column>

            <el-table-column label="操作" width="150">
                <template #default="scope">
                    <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                    <el-button link type="danger" size="small" @click="deleteVideo(scope.row)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 编辑对话框 -->
        <el-dialog v-model="editDialogVisible" title="编辑视频信息">
            <el-form :model="currentVideo" label-width="80px">
                <el-form-item label="视频名称">
                    <el-input v-model="currentVideo.title" />
                </el-form-item>
                <el-form-item label="分类">
                    <el-select v-model="currentVideo.categories" placeholder="请选择分类">
                        <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label"
                            :value="item.value" />
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button type="primary" @click="saveEdit">保存</el-button>
                <el-button @click="editDialogVisible = false">取消</el-button>
            </template>
        </el-dialog>

        <!-- 视频预览对话框 -->
        <el-dialog v-model="previewVisible" title="视频预览" width="60%" destroy-on-close>
            <video :key="previewUrl" 
             ref="videoPlayer" 
             controls 
             style="width: 100%" 
             :src="previewUrl"
             crossorigin="anonymous"></video>
        </el-dialog>
    </el-card>
</template>

<script setup>
import { ref, computed,nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import store from '@/store'
import ToUrl from '@/api/api'
// 视频分类选项
const categoryOptions = [
    { value: '视频类1', label: '教学类' },
    { value: '视频类2', label: '娱乐类' },
    { value: '视频类3', label: '科技类' },
    { value: '视频类4', label: '教育类' },
    { value: '视频类5', label: '安全类' },
    { value: '视频类6', label: '其他' }
]
const videos = ref([])
// 搜索相关
const searchKeyword = ref('')
const selectedCategory = ref('')
// 编辑相关
const editDialogVisible = ref(false)
const currentVideo = ref({})
const selectedUploadCategory = ref(null) // 选择的分类
const fileList = ref([]) // 上传的文件列表
// 新增预览相关状态
const previewVisible = ref(false)
const previewUrl = ref('')
const videoPlayer = ref(null);
// 上传相关
const uploadTitle = ref('') // 视频标题
const uploadDescription = ref('') // 视频描述

//加载视频
const loadVideo=async ()=>{
    try{
        const response=await axios(ToUrl.url+"/admin/findAllVideo",{
            headers :{ 
                Authorization: `Bearer ${store.state.token}`,
                'Content-Type': 'application/json'
            }
        })
        videos.value=response.data.data;
        // console.log(response.data.data)
    }catch(error){
        ElMessage.error("视频加载失败"+error);
    }
}
// 视频上传前的检查
const beforeUpload = (file) => {
    // 1. 检查是否填写完整信息
    if (!selectedUploadCategory.value || !uploadTitle.value || !uploadDescription.value) {
        ElMessage.error('请填写完整信息（分类、标题、描述）后再上传')
        return false
    }

    // 2. 检查文件类型
    const isVideo = file.type.startsWith('video/')
    if (!isVideo) {
        ElMessage.error('只能上传视频文件 (MP4/AVI 格式)')
        return false
    }

    // 3. 检查文件大小
    const isLt500M = file.size / 1024 / 1024 < 500
    if (!isLt500M) {
        ElMessage.error('视频大小不能超过 500MB')
        return false
    }

    return true
}
// 处理上传
const handleUpload = async (param) => {
    const formData = new FormData()
    formData.append('video', param.file) // 添加视频文件
    formData.append('categories', selectedUploadCategory.value) // 分类
    formData.append('title', uploadTitle.value) // 题目
    formData.append('description', uploadDescription.value) // 描述
    formData.append('createTime', new Date().toISOString() ) // 当前时间

    try {
        const response = await axios.post(ToUrl.url+'/admin/upload', formData, {
            headers: { Authorization: `Bearer ${store.state.token}` }
        })

        if (response.data.message=="上传成功") {
            ElMessage.success('上传成功')
            fileList.value.push({ name: param.file.name, url: response.data.url })

            // 上传成功后清空输入框
            selectedUploadCategory.value = ''
            uploadTitle.value = ''
            uploadDescription.value = ''
            loadVideo();
        } else {
            ElMessage.error(response.data.message || '上传失败')
        }
    } catch (error) {
        ElMessage.error('上传出错: ' + error.message)
    }
}
// 计算属性处理过滤
const filteredVideos = computed(() => {
    return videos.value.filter(video => {
        const nameMatch = video.title.toLowerCase().includes(searchKeyword.value.toLowerCase())
        const categoryMatch = selectedCategory.value ? video.categories === selectedCategory.value : true
        return nameMatch && categoryMatch
    })
})
// 分类格式化
const formatCategory = category => {
    const found = categoryOptions.find(item => item.value === category)
    return found ? found.label : '未知分类'
}
//时间格式化
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}
// 编辑处理
const handleEdit = video => {
    currentVideo.value = { ...video }
    editDialogVisible.value = true
}
const saveEdit = async () => {
    try {
        const response = await axios.put(ToUrl.url + "/admin/UpdateVideo", {
            id: currentVideo.value.id,
            categories: currentVideo.value.categories,
            title: currentVideo.value.title,
            url: currentVideo.value.url,
            description: currentVideo.value.description,
            createTime: new Date().toISOString() // 获取当前时间
        }, {
            headers: { 
                Authorization: `Bearer ${store.state.token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.data.code === 200) {
            ElMessage.success("视频信息更新成功");

            // 更新本地视频列表
            const index = videos.value.findIndex(v => v.id === currentVideo.value.id);
            if (index !== -1) {
                videos.value[index] = { ...currentVideo.value, createTime: Date.now() };
            }

            editDialogVisible.value = false; // 关闭编辑对话框
        } else {
            ElMessage.error(response.data.message || "更新失败");
        }
    } catch (error) {
        ElMessage.error("更新出错: " + error.message);
    }
};


//预览视频
const handlePreview = (video) => {
    previewUrl.value = video.url;
    previewVisible.value = true;
    // console.log(video.url);
    // console.log(previewUrl);
    nextTick(() => {
        if (videoPlayer.value) {
            videoPlayer.value.load();
        }
    });
}
//删除视频
const deleteVideo = async(video) => {
    try{
        const response=await axios.put(ToUrl.url+"/admin/deleteVideo",{
            id:video.id
        },{
            headers :{ 
                Authorization: `Bearer ${store.state.token}`,
                'Content-Type': 'application/json'
            }
        })
        // console.log(response.data.data);
        ElMessage.success("删除成功")
        const index = videos.value.findIndex(v => v.id === video.id)
        if (index !== -1) {
            videos.value.splice(index, 1)
        }
    }catch(error){
        ElMessage.error("删除失败:"+error)
    }
}
//加载视频
loadVideo();

</script>

<style>
.video-table {
    margin-top: 20px;
}

.search-container {
    display: flex;
    align-items: center;
    margin-top: 10px;
}

.upload-section {
    display: flex;
    align-items: center;
    gap: 10px;
}
.upload_btn{
    margin-top: 30px;
}
</style>