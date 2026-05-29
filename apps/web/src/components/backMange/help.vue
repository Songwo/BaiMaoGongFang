<template>
  <div class="help-container">
    <el-card class="help-card">
      <template #header>
        <div class="card-header">
          <h2>帮助中心</h2>
        </div>
      </template>

      <div class="team-section">
        <h3>团队负责人</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="门店负责人">
            赵青松
          </el-descriptions-item>
          <el-descriptions-item label="前端开发">
            赵青松
          </el-descriptions-item>
          <el-descriptions-item label="后端开发">
            王嘉俊
          </el-descriptions-item>
          <el-descriptions-item label="微信小程序">
            李柯
          </el-descriptions-item>
          <el-descriptions-item label="App端">
            郑永龙
          </el-descriptions-item>
          <el-descriptions-item label="网络安全主题架构师">
            张君昊
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="contact-section">
        <h3>联系方式</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="邮箱">
            <el-link type="primary" href="mailto:zhaoqsnyah@gmail.com">
              zhaoqsnyah@gmail.com
            </el-link>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="resources-section">
        <h3>帮助资料</h3>
        <el-collapse>
          <el-collapse-item title="系统使用指南" name="1">
            <div class="collapse-content">
              <p>1. 登录系统后，您可以在左侧菜单栏选择不同的功能模块</p>
              <p>2. 每个模块都有相应的操作指南和帮助文档</p>
              <p>3. 如遇到问题，请联系相关技术负责人</p>
            </div>
          </el-collapse-item>
          <el-collapse-item title="常见问题" name="2">
            <div class="collapse-content">
              <p>1. 如何重置密码？</p>
              <p>2. 如何导出数据？</p>
              <p>3. 系统维护时间？</p>
            </div>
          </el-collapse-item>
          <el-collapse-item title="技术支持" name="3">
            <div class="collapse-content">
              <p>前端问题请联系：赵青松</p>
              <p>后端问题请联系：王嘉俊</p>
              <p>微信小程序问题请联系：李柯</p>
              <p>App端问题请联系：郑永龙</p>
              <p>网络安全相关问题请联系：张君昊</p>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-card>
  </div>
</template>

<script setup>
// 组件逻辑
</script>

<style scoped>
.help-container {
  padding: 20px;
  height: 100%;
}

.help-card {
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

.team-section,
.contact-section,
.resources-section {
  margin-bottom: 30px;
  padding: 0 20px;
}

h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 20px;
  font-weight: 500;
}

.collapse-content {
  padding: 10px;
  line-height: 1.8;
}

.collapse-content p {
  margin: 5px 0;
  color: #606266;
  font-size: 15px;
}

:deep(.el-descriptions__label) {
  width: 150px;
  background-color: #f5f7fa;
  font-weight: 500;
}

:deep(.el-descriptions__content) {
  padding: 12px 20px;
  font-size: 15px;
}

:deep(.el-collapse-item__header) {
  font-size: 16px;
  font-weight: 500;
  padding: 0 20px;
}

:deep(.el-collapse-item__content) {
  padding: 20px;
}

:deep(.el-card__body) {
  height: calc(100% - 60px);
  overflow-y: auto;
}

:deep(.el-card__header) {
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}
</style> 