<template>
  <div class="vuln-details-section">
    <h3>
      <el-icon><Connection /></el-icon>
      SSRF漏洞详情
    </h3>
    <div class="vuln-content">
      <h4>漏洞原理</h4>
      <p>SSRF(Server-Side Request Forgery)是一种由攻击者构造请求，由服务端发起请求的安全漏洞。攻击者可以利用该漏洞使服务器向任意地址发起请求，从而获取内网信息或执行未授权操作。</p>
      
      <h4>常见利用方式</h4>
      <ul>
        <li>探测内网端口开放情况</li>
        <li>访问内网Web应用</li>
        <li>利用内网服务漏洞</li>
        <li>读取本地文件</li>
      </ul>

      <h4>防御措施</h4>
      <ul>
        <li>限制请求的目标地址范围</li>
        <li>禁用不需要的协议（如file://、dict://等）</li>
        <li>使用白名单验证请求URL</li>
        <li>对请求进行严格的参数验证</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { Connection } from '@element-plus/icons-vue'
</script>

<style scoped>
.vuln-details-section {
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.vuln-content {
  margin-top: 15px;
}

.vuln-content h4 {
  color: #ffffff;
  margin: 15px 0 10px 0;
}

.vuln-content p {
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  margin-bottom: 15px;
}

.vuln-content ul {
  list-style-type: disc;
  padding-left: 20px;
  margin: 10px 0;
}

.vuln-content li {
  color: rgba(255, 255, 255, 0.8);
  margin: 5px 0;
}
</style> 