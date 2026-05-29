<template>
  <div class="document-container">
    <el-card class="document-card">
      <template #header>
        <div class="card-header">
          <h2>文档中心</h2>
        </div>
      </template>

      <div class="document-grid">
        <!-- CSDN文章 -->
        <el-card class="document-item" shadow="hover" @click="openLink('https://blog.csdn.net/zhao9585/article/details/147677821')">
          <div class="platform-tag csdn">
            <el-icon><Monitor /></el-icon>
            CSDN
          </div>
          <h3>白帽工坊（BaiMaoGongFang）- 全栈网络安全学习平台</h3>
          <p>详细介绍白帽工坊项目的功能亮点和技术栈，包括实战CTF靶场、系统化课程、安全工具集等核心功能。</p>
        </el-card>

        <!-- GitHub仓库 -->
        <el-card class="document-item" shadow="hover" @click="openLink('https://github.com/Songwo/BaiMaoGongFang.git')">
          <div class="platform-tag github">
            <el-icon><Link /></el-icon>
            GitHub
          </div>
          <h3>白帽工坊项目源码</h3>
          <p>白帽工坊项目的完整源代码，包含前端、后端、小程序和App端的所有代码。</p>
        </el-card>

        <!-- 掘金文章 -->
        <el-card class="document-item" shadow="hover" @click="openLink('https://juejin.cn/post/7499050342774718518')">
          <div class="platform-tag juejin">
            <el-icon><Reading /></el-icon>
            掘金
          </div>
          <h3>白帽工坊技术分享</h3>
          <p>分享白帽工坊项目的技术实现细节和开发经验。</p>
        </el-card>

        <!-- 知乎文章 -->
        <el-card class="document-item" shadow="hover" @click="openLink('https://zhuanlan.zhihu.com/p/1902005033681290221')">
          <div class="platform-tag zhihu">
            <el-icon><ChatDotRound /></el-icon>
            知乎
          </div>
          <h3>白帽工坊项目介绍</h3>
          <p>在知乎平台分享的白帽工坊项目介绍和技术讨论。</p>
        </el-card>

        <!-- B站视频 -->
        <el-card class="document-item" shadow="hover" @click="openLink('https://www.bilibili.com/video/BV1s4VHzHEmx?vd_source=09a7101b90da6426ff2769ff953efeb2')">
          <div class="platform-tag bilibili">
            <el-icon><VideoPlay /></el-icon>
            Bilibili
          </div>
          <h3>网络安全靶场平台演示</h3>
          <p>白帽工坊网络安全靶场平台的演示视频，包含功能展示和操作说明。</p>
        </el-card>

        <!-- 简书文章 -->
        <el-card class="document-item" shadow="hover" @click="openLink('https://www.jianshu.com/p/717bf5dca0ac')">
          <div class="platform-tag jianshu">
            <el-icon><Notebook /></el-icon>
            简书
          </div>
          <h3>白帽工坊开发笔记</h3>
          <p>记录白帽工坊项目开发过程中的技术笔记和经验总结。</p>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {
  Monitor,
  Link,
  Reading,
  ChatDotRound,
  VideoPlay,
  Notebook
} from '@element-plus/icons-vue'

const openLink = (url) => {
  window.open(url, '_blank')
}
</script>

<style scoped>
.document-container {
  padding: 20px;
  height: 100%;
}

.document-card {
  width: 100%;
  height: 100%;
  margin: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.document-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  padding: 20px;
}

.document-item {
  position: relative;
  height: 220px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: all 0.3s ease;
  cursor: pointer;
  padding: 20px;
}

.document-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.document-item h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #303133;
  line-height: 1.4;
  padding-right: 80px;
}

.document-item p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  padding-right: 20px;
}

.platform-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 4px;
}

.platform-tag .el-icon {
  font-size: 14px;
}

.platform-tag.csdn {
  background-color: #fc5531;
}

.platform-tag.github {
  background-color: #24292e;
}

.platform-tag.juejin {
  background-color: #1e80ff;
}

.platform-tag.zhihu {
  background-color: #0084ff;
}

.platform-tag.bilibili {
  background-color: #fb7299;
}

.platform-tag.jianshu {
  background-color: #ea6f5a;
}

:deep(.el-card__body) {
  height: calc(100% - 60px);
  padding: 0;
}

:deep(.el-card__header) {
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}
</style> 