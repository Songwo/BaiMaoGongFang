<template>
    <!-- 视频分类管理卡片 -->
    <el-card class="category-management mt-4">
        <template #header>
            <div class="card-header">
                <span>视频分类管理</span>
            </div>
        </template>

        <!-- 视频分类表格 -->
        <el-table :data="videoCategories" stripe>
            <!-- 动态生成列 -->
            <el-table-column v-for="(_, index) in challengeFields" :key="index" 
                :prop="`challengel${index + 1}`" :label="`分类 ${index + 1}`" />
            <el-table-column label="操作" width="150">
                <template #default="scope">
                    <el-button link type="primary" size="small"
                        @click="editCategory('video', scope.row)">编辑</el-button>
                    <el-button link type="danger" size="small"
                        @click="deleteCategory('video', scope.row._id)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-button type="primary" class="mt-4" @click="addCategory('video')">新增视频分类</el-button>
    </el-card>

    <!-- 问题分类管理卡片 -->
    <el-card class="category-management">
        <template #header>
            <div class="card-header">
                <span>问题分类管理</span>
            </div>
        </template>

        <!-- 问题分类表格 -->
        <el-table :data="questionCategories" stripe>
            <!-- 动态生成列 -->
            <el-table-column v-for="(_, index) in challengeFields" :key="index" 
                :prop="`challengel${index + 1}`" :label="`分类 ${index + 1}`" />
            <el-table-column label="操作" width="150">
                <template #default="scope">
                    <el-button link type="primary" size="small"
                        @click="editCategory('question', scope.row)">编辑</el-button>
                    <el-button link type="danger" size="small"
                        @click="deleteCategory('question', scope.row._id)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-button type="primary" class="mt-4" @click="addCategory('question')">新增问题分类</el-button>
    </el-card>

    <!-- 分类编辑对话框 -->
    <el-dialog v-model="categoryDialogVisible"
        :title="`${isEdit ? '编辑' : '新增'}${activeTab === 'question' ? '问题' : '视频'}分类`">
        <el-form :model="currentCategory" label-width="100px">
            <!-- 动态生成表单项 -->
            <el-form-item v-for="(_, index) in challengeFields" :key="index" 
                :label="`分类 ${index + 1}`" :prop="`challengel${index + 1}`">
                <el-input v-model="currentCategory[`challengel${index + 1}`]" 
                    :placeholder="`请输入分类 ${index + 1}`" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="categoryDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveCategory">保存</el-button>
        </template>
    </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import store from '@/store'
import ToUrl from '@/api/api'

// 分类数据
const videoCategories = ref([]) // 视频分类
const questionCategories = ref([]) // 问题分类

// 当前操作分类
const currentCategory = ref({})
const isEdit = ref(false) // 是否为编辑模式
const categoryDialogVisible = ref(false)
const activeTab = ref('video') // 当前激活的标签页

// 定义 challenge 字段的数量（假设最多有 6 个）
const challengeFields = Array.from({ length: 6 }, (_, i) => `challengel${i + 1}`);

// 加载分类数据
const loadCategories = async () => {
    const headers = { 
        Authorization: `Bearer ${store.state.token}`,
        'Content-Type': 'application/json'
    };
    try {
        // 视频分类
        const videoRes = await axios.get(ToUrl.url+'/user/get_category', { headers });
        videoCategories.value = videoRes.data.data || [];

        // 问题分类
        const quesRes = await axios.get(ToUrl.url+'/api/get_challenge', { headers });
        const questionData = quesRes.data.data;

        // 将对象转换为数组，提取所有 challengel 属性
        const categories = [];
        if (questionData) {
            categories.push({
                id: questionData.id, // 使用共同 ID
                ...Object.fromEntries(
                    Object.entries(questionData).filter(([key]) => key.startsWith('challengel')))
            });
        }
        questionCategories.value = categories;
    } catch (error) {
        ElMessage.error('分类加载失败');
    }
}

// 打开新增/编辑分类对话框
const addCategory = (type) => {
    currentCategory.value = Object.fromEntries(challengeFields.map(field => [field, '']));
    isEdit.value = false;
    activeTab.value = type;
    categoryDialogVisible.value = true;
}

const editCategory = (type, category) => {
    currentCategory.value = { ...category };
    isEdit.value = true;
    activeTab.value = type;
    categoryDialogVisible.value = true;
}

// 保存分类
const saveCategory = async () => {
    try {
        const headers = { 
            Authorization: `Bearer ${store.state.token}`,
            'Content-Type': 'application/json'
        };

        challengeFields.forEach(field => {
            if (!currentCategory.value[field]) {
                currentCategory.value[field] = ''; // 确保所有字段存在
            }
        });

        let apiUrl = activeTab.value === 'video'
            ? `${ToUrl.url}/admin/UpdateVideoCategory`
            : `${ToUrl.url}/admin/UpdateCategory`;

        const method = isEdit.value ? 'put' : 'post';

        await axios({
            method: method,
            url: apiUrl,
            data: { id: currentCategory.value.id, ...currentCategory.value },
            headers: headers
        });

        ElMessage.success('保存成功');
        categoryDialogVisible.value = false;
        loadCategories();
    } catch (error) {
        ElMessage.error('保存失败: ' + (error.response?.data?.message || error.message));
    }
};

onMounted(() => {
    loadCategories();
});
</script>

<style scoped>
.mt-4 {
    margin-top: 1rem;
}
</style>