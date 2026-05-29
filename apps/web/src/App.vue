<!-- App.vue -->
<template>
  <div>
    <router-view></router-view>
    <LearningGuide/>
  </div>
</template>

<script>
import LearningGuide from './components/LearningGuide.vue'

export default {
  name: 'App',
  components: {
    LearningGuide
  }
}
</script>

<style>
:root {
  /* 主题色覆盖 - 使用优雅紫色系配色 */
  --el-color-primary: #7c3aed !important;
  --el-color-primary-light-3: #8b5cf6 !important;
  --el-color-primary-light-5: #a78bfa !important;
  --el-color-primary-light-7: #c4b5fd !important;
  --el-color-primary-light-8: #ddd6fe !important;
  --el-color-primary-light-9: #f5f3ff !important;
  --el-color-primary-dark-2: #5b21b6 !important;
  
  /* 菜单项选中颜色覆盖 */
  --el-menu-active-color: #e2ffe2 !important;
  --el-menu-hover-text-color: #9d6cf1 !important;
  --el-menu-hover-bg-color: rgba(124, 58, 237, 0.1) !important;
}

/* 为选中的菜单项增加更明显的样式 */
.el-menu--horizontal > .el-menu-item.is-active,
.el-menu--horizontal > .el-sub-menu.is-active .el-sub-menu__title {
  border-bottom: 2px solid var(--el-color-primary) !important;
  font-weight: bold;
  background-color: rgba(218, 197, 255, 0.15) !important;
}
</style>