<template>
    <div class="ip-search-container">
        <el-card class="search-box" shadow="always">
            <div class="title-wrapper">
                <h1 class="gradient-title">IP Information Query</h1>
            </div>
            <!-- 返回按钮 -->
            <el-button class="back-btn" type="text" @click="$router.go(-1)">
                <el-icon :size="24" class="icon">
                    <ArrowLeft />
                </el-icon>返回
            </el-button>
            <el-form @submit.prevent="handleQuery">
                <el-input v-model="ipAddress" placeholder="Enter IP address" clearable :prefix-icon="Search"
                    class="custom-input" @keyup.enter="handleQuery">
                    <template #append>
                        <el-button type="primary" :icon="Search" :loading="loading" @click="handleQuery"
                            class="search-btn">
                            Query
                        </el-button>
                    </template>
                </el-input>
            </el-form>

            <transition name="el-zoom-in-top">
                <el-card v-if="resultVisible" class="result-card">
                    <el-descriptions title="Query Result" column=1>
                        <el-descriptions-item label="Country">
                            <el-tag type="success">{{ ipInfo.country }}</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="City">
                            <el-tag type="info">{{ ipInfo.city }}</el-tag>
                        </el-descriptions-item>
                        <el-descriptions-item label="ISP">
                            <el-tag type="warning">{{ ipInfo.isp }}</el-tag>
                        </el-descriptions-item>
                    </el-descriptions>
                </el-card>
            </transition>
        </el-card>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import store from '@/store'
import ToUrl from '@/api/api'

const ipAddress = ref('')
const loading = ref(false)
const resultVisible = ref(false)
const ipInfo = ref({
    country: '',
    city: '',
    isp: ''
})

const validateIP = (ip) => {
    const pattern = /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/
    return pattern.test(ip)
}

const handleQuery = async () => {
    if (!validateIP(ipAddress.value)) {
        ElMessage.error('Please enter a valid IP address')
        return
    }
    try {
        loading.value = true

        const response = await axios.get(ToUrl.url + "/cms/lookup", {
            params: { ip: ipAddress.value },
            headers: {
                Authorization: `Bearer ${store.state.token}`,
                'Content-Type': 'application/json'
            }
        }
        )
        
        ipInfo.value = response.data
        resultVisible.value = true
    } catch (error) {
        ElMessage.error('Failed to fetch IP information')
        console.error('API Error:', error)
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.ip-search-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 90vh;

}

.search-box {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border: none;
    border-radius: 15px;
    width: 500px;
    padding: 30px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.title-wrapper {
    text-align: center;
    margin-bottom: 30px;
}

.gradient-title {
    background: linear-gradient(45deg, #ffffff, #00d2ff);
    -webkit-background-clip: text;
    -moz-background-clip: text; /* Firefox */
    -ms-background-clip: text; /* Microsoft Edge (older versions) */
    -o-background-clip: text; /* Opera */
    background-clip: text; /* Standard property */
    -webkit-text-fill-color: transparent;
    font-size: 24px;
    font-weight: bold;
}

.custom-input :deep(.el-input__inner) {
    background: rgba(255, 255, 255, 0.9);
    border: none;
    border-radius: 25px;
    height: 50px;
    padding-left: 20px;
    font-size: 16px;
}

.search-btn {
    height: 50px;
    border-radius: 0 25px 25px 0;
    padding: 0 30px;
}

.result-card {
    margin-top: 20px;
    background: rgba(255, 255, 255, 0.9);
    border: none;
    border-radius: 10px;
}

.el-tag {
    font-size: 14px;
    padding: 8px 15px;
    border-radius: 15px;
}

.back-btn {
    position: absolute;
    left: 30px;
    top: 55px;
    padding: 0.5rem;
    transition: all 0.3s ease;
    color: #2b382b;

    &:hover {
        transform: translateX(-3px);

        .icon {
            color:#658a8a;
        }
    }
}
</style>