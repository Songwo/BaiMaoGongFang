<template>
  <div class="settings-container">
    <el-card class="settings-card">
      <template #header>
        <div class="settings-header">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </div>
      </template>
      <div class="theme-switch">
        <span>主题模式：</span>
        <el-switch
          v-model="isDark"
          active-text="暗色模式"
          inactive-text="亮色模式"
          @change="toggleTheme"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Setting } from '@element-plus/icons-vue'

const isDark = ref(false)

// 主题切换
const toggleTheme = () => {
  if (isDark.value) {
    document.documentElement.classList.add('dark-theme')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.classList.remove('dark-theme')
    localStorage.setItem('theme', 'light')
  }
}

onMounted(() => {
  // 初始化主题
  const theme = localStorage.getItem('theme')
  if (theme === 'dark') {
    isDark.value = true
    document.documentElement.classList.add('dark-theme')
  } else {
    isDark.value = false
    document.documentElement.classList.remove('dark-theme')
  }
})
</script>

<style scoped>
.settings-container {
  padding: 30px 20px;
  max-width: 500px;
  margin: 0 auto;
}
.settings-card {
  border-radius: 10px;
}
.settings-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
}
.theme-switch {
  margin-top: 30px;
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 16px;
}
</style>

<!-- 暗色主题全局样式，可放在App.vue或index.css中 -->
<style>
.dark-theme {
  --el-bg-color: #181818;
  --el-bg-color-overlay: #232323;
  --el-text-color-primary: #e5eaf3;
  --el-text-color-regular: #bfcbd9;
  --el-border-color: #444;
  --el-card-bg-color: #232323;
  --el-menu-bg-color: #232323;
  --el-menu-text-color: #bfcbd9;
  --el-menu-active-color: #409EFF;
  background: #181818 !important;
  color: #e5eaf3 !important;
}
.dark-theme .el-card {
  background: var(--el-card-bg-color) !important;
  color: var(--el-text-color-primary) !important;
}
.dark-theme .el-menu {
  background: var(--el-menu-bg-color) !important;
  color: var(--el-menu-text-color) !important;
}
.dark-theme .el-menu-item.is-active {
  color: var(--el-menu-active-color) !important;
}
.dark-theme .el-breadcrumb__inner {
  color: #bfcbd9 !important;
}
</style> 