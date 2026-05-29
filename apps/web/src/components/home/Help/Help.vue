<template>
  <div class="help-container">
    <div class="help-header">
      <h1>帮助中心</h1>
      <p>在这里您可以找到常见问题的解答和使用指南</p>
    </div>

    <div class="help-content">
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索问题..."
          prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
      </div>

      <!-- FAQ分类 -->
      <div class="faq-categories">
        <el-tabs v-model="activeCategory" class="faq-tabs">
          <el-tab-pane label="常见问题" name="general">
            <el-collapse v-model="activeNames">
              <el-collapse-item title="如何开始使用白帽工坊？" name="1">
                <div class="faq-answer">
                  <p>1. 注册并登录您的账号</p>
                  <p>2. 浏览首页了解平台功能</p>
                  <p>3. 选择您感兴趣的学习路径开始学习</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="如何参与实战训练？" name="2">
                <div class="faq-answer">
                  <p>1. 确保您已完成基础课程学习</p>
                  <p>2. 在"靶场实战"中选择适合的难度级别</p>
                  <p>3. 按照提示完成训练任务</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="如何获取积分和奖励？" name="3">
                <div class="faq-answer">
                  <p>1. 完成每日任务</p>
                  <p>2. 参与答题测试</p>
                  <p>3. 在社区分享经验</p>
                  <p>4. 完成实战训练</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-tab-pane>

          <el-tab-pane label="账号相关" name="account">
            <el-collapse v-model="activeNames">
              <el-collapse-item title="如何修改密码？" name="4">
                <div class="faq-answer">
                  <p>1. 登录您的账号</p>
                  <p>2. 进入个人中心</p>
                  <p>3. 点击"修改密码"选项</p>
                  <p>4. 按照提示完成密码修改</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="忘记密码怎么办？" name="5">
                <div class="faq-answer">
                  <p>1. 在登录页面点击"忘记密码"</p>
                  <p>2. 输入注册邮箱</p>
                  <p>3. 按照邮件提示重置密码</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-tab-pane>

          <el-tab-pane label="学习指南" name="learning">
            <el-collapse v-model="activeNames">
              <el-collapse-item title="推荐的学习路径" name="6">
                <div class="faq-answer">
                  <p>1. 网络安全基础</p>
                  <p>2. Web安全入门</p>
                  <p>3. 渗透测试基础</p>
                  <p>4. 漏洞挖掘实践</p>
                </div>
              </el-collapse-item>
              <el-collapse-item title="如何有效利用学习资源？" name="7">
                <div class="faq-answer">
                  <p>1. 制定学习计划</p>
                  <p>2. 结合视频课程和实践</p>
                  <p>3. 参与社区讨论</p>
                  <p>4. 定期复习和总结</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 联系支持 -->
      <div class="contact-support">
        <h3>没有找到您需要的答案？</h3>
        <el-button type="primary" @click="goToFeedback">提交反馈</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const searchQuery = ref('')
const activeCategory = ref('general')
const activeNames = ref(['1'])

const handleSearch = (value) => {
  // 实现搜索逻辑
  console.log('Searching for:', value)
}

const goToFeedback = () => {
  router.push('/bmgf/feedback')
}
</script>

<style scoped>
.help-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
}

.help-header {
  text-align: center;
  margin-bottom: 40px;
  color: white;
}

.help-header h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
}

.help-header p {
  font-size: 1.1em;
  opacity: 0.8;
}

.help-content {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.search-box {
  max-width: 600px;
  margin: 0 auto 30px;
}

.faq-categories {
  margin-bottom: 40px;
}

:deep(.el-tabs__nav) {
  background: transparent;
}

:deep(.el-tabs__item) {
  color: white;
  font-size: 1.1em;
}

:deep(.el-tabs__item.is-active) {
  color: #4CAF50;
}

:deep(.el-collapse-item__header) {
  color: white;
  font-size: 1.1em;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  margin-bottom: 10px;
}

:deep(.el-collapse-item__content) {
  color: #333;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 20px;
}

.faq-answer {
  line-height: 1.8;
  color: #333;
}

.faq-answer p {
  margin: 8px 0;
  color: #333;
}

.contact-support {
  text-align: center;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.contact-support h3 {
  color: white;
  margin-bottom: 20px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 12px 30px;
  font-size: 1.1em;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

@media (max-width: 768px) {
  .help-container {
    padding: 20px 15px;
  }

  .help-header h1 {
    font-size: 2em;
  }

  .help-content {
    padding: 20px;
  }

  :deep(.el-tabs__item) {
    font-size: 1em;
  }
}
</style> 