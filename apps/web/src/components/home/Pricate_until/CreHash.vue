<template>
  <div class="hash-generator-container">
    <el-card class="hash-generator-card" @click="handleCardClick">
      <!-- 返回按钮 -->
      <el-button class="back-btn" type="text" @click="$router.go(-1)">
                <el-icon :size="24" class="icon">
                    <ArrowLeft />
                </el-icon>返回
            </el-button>
      <h2>哈希值生成工具</h2>
      <el-form @submit.prevent="generateHash">
        <el-form-item label="输入文本">
          <el-input v-model="inputText" type="textarea" placeholder="请输入文本" rows="10" :style="{ fontSize: '18px' }"/>
        </el-form-item>
        <el-form-item label="输出格式">
          <el-radio-group v-model="outputFormat">
            <el-radio label="hex">十六进制</el-radio>
            <el-radio label="base64">Base64</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="generateHash">生成 SHA-256 哈希</el-button>
        </el-form-item>
      </el-form>
      <el-card v-if="hashResult" class="result-card">
        <p><strong>哈希值 ({{ outputFormat === 'hex' ? '十六进制' : 'Base64' }})：</strong></p>
        <pre>{{ hashResult }}</pre>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const inputText = ref('')
const hashResult = ref('')
const outputFormat = ref('hex')
const loading = ref(false)

// 工具函数：转换 ArrayBuffer 为十六进制字符串
const bufferToHex = (buffer) => {
  return Array.from(new Uint8Array(buffer)).map((b) => b.toString(16).padStart(2, '0')).join('')
}

// 工具函数：转换 ArrayBuffer 为 Base64
const bufferToBase64 = (buffer) => {
  const binary = String.fromCharCode(...new Uint8Array(buffer))
  return btoa(binary)
}

const generateHash = async () => {
  if (!inputText.value) {
    ElMessage.error('请输入文本')
    return
  }
  loading.value = true
  try {
    const encoder = new TextEncoder()
    const data = encoder.encode(inputText.value)
    const hashBuffer = await crypto.subtle.digest('SHA-256', data)

    hashResult.value = outputFormat.value === 'hex' ? bufferToHex(hashBuffer) : bufferToBase64(hashBuffer)
  } catch (error) {
    ElMessage.error('哈希生成失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleCardClick = () => {}
</script>

<style scoped>
.hash-generator-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 90vh;
  background-color: transparent;
}

.hash-generator-card {
  width: 600px;
  padding: 20px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  &.active {
    border: 2px solid #1d4932;
    background-color: #f8faf9;
  }
}

.result-card {
  margin-top: 20px;
  padding: 15px;
  border-radius: 5px;
  background: rgba(255, 255, 255, 0.2);
}

pre {
  white-space: pre-wrap;
  word-break: break-word;
  color: #2b382b;
  font-weight: 500;
  font-size: 16px;
}

:deep(.el-form-item__label) {
  color: #2b382b;
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

:deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.2);
  color: #2b382b;
  font-weight: 500;
}

:deep(.el-input__inner::placeholder) {
  color: #658a8a;
  opacity: 0.7;
}

.back-btn {
    position: absolute;
    left: 20px;
    top: 35px;
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

:deep(.el-radio) {
    color: #2b382b;
    font-weight: 500;
    
    .el-radio__label {
        color: #2b382b;
    }
    
    &.is-checked {
        .el-radio__label {
            color: #658a8a;
        }
    }
}
</style>