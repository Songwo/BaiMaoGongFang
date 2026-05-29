<template>
  <div class="vuln-details-section">
    <h3>
      <el-icon><Warning /></el-icon>
      Log4Shell漏洞详情
    </h3>
    <div class="vuln-content">
      <h4>漏洞原理</h4>
      <p>Log4Shell(CVE-2021-44228)是Apache Log4j2中的一个远程代码执行漏洞。该漏洞允许攻击者通过JNDI注入执行任意代码，影响范围广泛，危害严重。</p>
      
      <h4>漏洞特征</h4>
      <ul>
        <li>影响Log4j2 2.0-beta9到2.14.1版本</li>
        <li>通过JNDI注入实现远程代码执行</li>
        <li>攻击者可以完全控制目标系统</li>
        <li>利用门槛低，危害性高</li>
      </ul>

      <h4>防御措施</h4>
      <ul>
        <li>升级Log4j2到安全版本(2.15.0及以上)</li>
        <li>禁用JNDI查找功能</li>
        <li>限制JNDI访问范围</li>
        <li>使用WAF等安全设备进行防护</li>
      </ul>

      <h4>检测方法</h4>
      <ul>
        <li>检查Log4j2版本</li>
        <li>监控异常JNDI请求</li>
        <li>分析日志中的可疑字符串</li>
        <li>使用漏洞扫描工具</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { Warning } from '@element-plus/icons-vue'
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