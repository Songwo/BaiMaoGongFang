<template>
    <div class="base64-tool-container">
        <!-- 返回按钮 -->
        <el-button class="back-btn" type="text" @click="$router.go(-1)">
            <el-icon :size="24" class="icon">
                <ArrowLeft />
            </el-icon>返回
        </el-button>

        <!-- 工具主体 -->
        <div class="tool-content">
            <h1 class="title">Base64 编解码工具</h1>

            <!-- 输入区域 -->
            <el-input v-model="inputText" type="textarea" :rows="5" :style="{ fontSize: '18px' }"
                placeholder="请输入要编码或解码的内容" class="input-area" resize="none"></el-input>

            <!-- 操作选择 -->
            <div class="operation-group">
                <el-select v-model="operation" placeholder="请选择操作" class="operation-select">
                    <el-option label="Base64 编码" value="encode" />
                    <el-option label="Base64 解码" value="decode" />
                </el-select>

                <el-button type="primary" @click="handleProcess" class="process-btn">
                    {{ processing ? '处理中...' : '立即处理' }}
                </el-button>
            </div>

            <!-- 结果展示 -->
            <el-input v-model="result" type="textarea" :rows="5" readonly placeholder="处理结果将显示在这里" class="result-area"
                :style="{ fontSize: '18px' }"></el-input>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { ArrowLeft } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import store from '@/store';
import ToUrl from '@/api/api';

const inputText = ref('');
const operation = ref('encode');
const result = ref('');
const processing = ref(false);

const handleProcess = async () => {
    if (!inputText.value) {
        ElMessage.warning('请输入要处理的内容');
        return;
    }
    const op = ref({
        url: "",
        data: ""
    });
    processing.value = true;
    if (operation.value === 'encode') {
        op.value.url = "/encode";
        op.value.data = "编码成功";
    } else {
        op.value.url = "/decode";
        op.value.data = "解码成功";
    }

    try {
        const res = await axios.get(ToUrl.url + "/cms" + op.value.url, {
            params: { input: inputText.value },
            headers: {
                'Authorization': `Bearer ${store.state.token}`
            }
        });
        result.value = res.data?.result || '处理失败，请检查输入内容格式';
        ElMessage.success(op.value.data);
    } catch (error) {
        ElMessage.error('处理失败，请检查输入内容格式');                 
        result.value = '';
    } finally {
        processing.value = false;
    }
};
</script>

<style scoped>
.base64-tool-container {
    min-height: 100vh;
    padding: 2rem;
    position: relative;
}

.back-btn {
    position: absolute;
    left: 440px;
    top: 75px;
    padding: 0.5rem;
    transition: all 0.3s ease;
    color: #2b382b;
    &:hover {
        transform: translateX(-3px);

        .icon {
            color: var(--el-color-primary);
        }
    }
}

.tool-content {
    max-width: 800px;
    margin: 0 auto;
    padding: 2rem;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.title {
    text-align: center;
    color: #2c3e50;
    margin-bottom: 2rem;
    font-size: 1.8rem;
}

.input-area,
.result-area {
    margin-bottom: 1.5rem;

    :deep(.el-textarea__inner) {
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.9rem;
    }
}

.operation-group {
    display: flex;
    gap: 1rem;
    margin-bottom: 2rem;
}

.operation-select {
    flex: 1;
}

.process-btn {
    flex-shrink: 0;
    width: 120px;
    transition: all 0.3s ease;

    &:hover {
        transform: translateY(-2px);
        box-shadow: 0 2px 8px rgba(var(--el-color-primary-rgb), 0.3);
    }
}

@media (max-width: 768px) {
    .tool-content {
        padding: 1rem;
    }

    .operation-group {
        flex-direction: column;
    }

    .process-btn {
        width: 100%;
    }
}
</style>