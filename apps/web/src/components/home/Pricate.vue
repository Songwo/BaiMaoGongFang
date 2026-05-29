<template>
  <div class="container">
    <h1 class="title">Network Security Tools</h1>
    
    <!-- 介绍部分 -->
    <div class="intro-section">
      <p class="intro-text">
        专业的网络安全工具集合，为您提供全方位的网络安全解决方案。
        从CMS识别到密码强度检测，从IP查询到哈希值生成，一站式满足您的安全需求。
      </p>
    </div>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-section">
      <el-col :span="6" v-for="stat in statistics" :key="stat.label">
        <div class="stat-card">
          <Icon :icon="stat.icon" class="stat-icon" />
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 搜索和分类 -->
    <div class="search-section">
      <el-input
        v-model="searchQuery"
        placeholder="搜索安全工具..."
        class="search-input"
        :prefix-icon="Search"
      />
      <el-radio-group v-model="selectedCategory" class="category-group">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="scan">扫描工具</el-radio-button>
        <el-radio-button label="encode">编码工具</el-radio-button>
        <el-radio-button label="analysis">分析工具</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 热门工具 -->
    <div class="hot-tools">
      <h2>热门工具</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="tool in hotTools" :key="tool.name">
          <el-card class="tool-card hot">
            <div class="hot-badge">热门</div>
            <Icon :icon="tool.icon" class="tool-icon" />
            <h3>{{ tool.name }}</h3>
            <p>{{ tool.description }}</p>
            <el-button type="primary" @click="goToTool(tool.link)">查看</el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 工具列表 -->
    <div class="tools-section">
      <h2>全部工具</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="tool in filteredTools" :key="tool.name">
          <el-card class="tool-card">
            <Icon :icon="tool.icon" class="tool-icon" />
            <h3>{{ tool.name }}</h3>
            <p>{{ tool.description }}</p>
            <el-button type="primary" @click="goToTool(tool.link)">查看</el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 代码示例分区 -->
    <div class="code-examples-section">
      <h2>加解密代码示例</h2>
      <el-row :gutter="20">
        <el-col :span="8" v-for="example in codeExamples" :key="example.name">
          <el-card class="code-card">
            <div class="code-header">
              <Icon :icon="example.icon" class="code-icon" />
              <h3>{{ example.name }}</h3>
            </div>
            <p class="code-description">{{ example.description }}</p>
            <div class="code-languages">
              <el-tag v-for="lang in example.languages" :key="lang" class="language-tag">
                {{ lang }}
              </el-tag>
            </div>
            <div class="code-actions">
              <el-button type="primary" @click="downloadCode(example)">
                <el-icon><Download /></el-icon>
                下载代码
              </el-button>
              <el-button type="success" @click="viewCode(example)">
                <el-icon><View /></el-icon>
                查看代码
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 添加路由出口 -->
    <router-view></router-view>
  </div>
</template>

<script setup name="Private">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { Search, Download, View } from '@element-plus/icons-vue'
import axios from 'axios'
import ToUrl from '@/api/api.ts'
import store from '@/store'
import { ElMessage } from 'element-plus'

const searchQuery = ref('')
const router = useRouter()
const selectedCategory = ref('all')
const stats = ref({
  dailyVisits: 0,
  userCount: 0
})

// 格式化数字显示
const formatNumber = (num) => {
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k+'
  }
  return num.toString()
}

// 更新统计数据
const updateStats = async () => {
  try {
    const response = await axios.get(ToUrl.url+'/api/stats', {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    stats.value = response.data
  } catch (error) {
    console.error('Failed to fetch stats:', error)
    // 设置默认值，确保界面不会崩溃
    if (!stats.value.dailyVisits && !stats.value.userCount) {
      stats.value = { dailyVisits: 0, userCount: 0 }
    }
  }
}

// 记录访问
const recordVisit = async () => {
  try {
    await axios.post(ToUrl.url+'/api/stats/visits', {}, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    await updateStats() // 更新统计数据
  } catch (error) {
    console.error('Failed to record visit:', error)
    // 错误时不重试，继续执行
  }
}

// 定时更新统计数据
let statsInterval= null

onMounted(() => {
  // 加载数据
  updateStats() //更新用户和访问数量
  recordVisit() //增加访问量
  increaseUserCount() //增加用户
  
  // 每30秒更新一次数据
  statsInterval = window.setInterval(updateStats, 30000)
})

onUnmounted(() => {
  if (statsInterval) {
    clearInterval(statsInterval)
  }
})

const statistics = computed(() => [
  { label: '工具总数', value: '4+', icon: 'mdi:tools' },
  { label: '日访问量', value: formatNumber(stats.value.dailyVisits), icon: 'mdi:eye' },
  { label: '用户数量', value: formatNumber(stats.value.userCount), icon: 'mdi:account-group' },
  { label: '安全评分', value: '98%', icon: 'mdi:shield-check' }
])

const hotTools = ref([
  { 
    name: 'CMS', 
    description: '在线CMS指纹识别工具', 
    icon: 'mdi:shield-search',
    link: '/bmgf/tools/cms' 
  },
  { 
    name: '子域名查询', 
    description: '在线子域名查询工具', 
    icon: 'mdi:domain',
    link: '/bmgf/tools/subdomain' 
  },
  { 
    name: 'IP查询', 
    description: '在线IP信息查询工具', 
    icon: 'mdi:ip-network',
    link: '/bmgf/tools/ip-finder' 
  },
  { 
    name: '密码强度检测', 
    description: '密码强度检测工具', 
    icon: 'mdi:shield-lock',
    link: '/bmgf/tools/password-check' 
  }
])

const tools = ref([
  { 
    name: 'CMS', 
    description: '在线CMS指纹识别工具', 
    icon: 'mdi:shield-search',
    link: '/bmgf/tools/cms',
    category: 'scan'
  },
  { 
    name: '子域名查询', 
    description: '在线子域名查询工具', 
    icon: 'mdi:domain',
    link: '/bmgf/tools/subdomain',
    category: 'scan'
  },
  { 
    name: 'base64编码', 
    description: '在线Base64解码工具', 
    icon: 'mdi:code-brackets',
    link: '/bmgf/tools/base64',
    category: 'encode'
  },
  { 
    name: 'IP查询', 
    description: '在线IP信息查询工具', 
    icon: 'mdi:ip-network',
    link: '/bmgf/tools/ip-finder',
    category: 'analysis'
  },
  { 
    name: '哈希值', 
    description: '在线哈希值生成工具', 
    icon: 'mdi:function-variant',
    link: '/bmgf/tools/hash',
    category: 'encode'
  },
  { 
    name: '密码强度检测工具', 
    description: '密码强度检测工具', 
    icon: 'mdi:shield-lock',
    link: '/bmgf/tools/password-check',
    category: 'analysis'
  },
  {
    name: '端口扫描工具',
    description: '在线端口扫描工具',
    icon: 'mdi:network-outline',
    link: '/bmgf/tools/port-scan',
    category: 'scan'
  },
  {
    name: 'URL编码工具',
    description: '在线URL编码工具',
    icon: 'mdi:link-variant',
    link: '/bmgf/tools/url-encode',
    category: 'encode'
  },
  {
    name: '凯撒密码工具',
    description: '在线凯撒密码工具',
    icon: 'mdi:lock-outline',
    link: '/bmgf/tools/caesar',
    category: 'encode'
  },
  {
    name: 'Unicode编码工具',
    description: '在线Unicode编码工具',
    icon: 'mdi:unicode',
    link: '/bmgf/tools/unicode',
    category: 'encode'
  }
])

const filteredTools = computed(() => {
  return tools.value.filter(tool => {
    const matchesSearch = tool.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                         tool.description.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesCategory = selectedCategory.value === 'all' || tool.category === selectedCategory.value
    return matchesSearch && matchesCategory
  })
})

const goToTool = (url) => {
  const token = store.state.token;
  if (!token || token === 'null' || token === '') {
    ElMessage.warning('请先登录后使用此功能');
    router.push('/');
    return;
  }
  router.push(url);
}

const increaseUserCount = async () => {
  try {
    await axios.post(ToUrl.url+'/api/stats/users', {}, {
      headers: { 'Authorization': `Bearer ${store.state.token}` }
    })
    await updateStats() // 更新统计数据
  } catch (error) {
    console.error('Failed to increase user count:', error)
  }
}

// 代码示例数据
const codeExamples = ref([
  {
    name: 'AES加密',
    description: '高级加密标准(AES)实现，支持128/192/256位密钥',
    icon: 'mdi:lock',
    languages: ['Java', 'Python', 'JavaScript'],
    code: {
      Java: `import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESEncryption {
    private static final String ALGORITHM = "AES";
    
    public static String encrypt(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    public static String decrypt(String encryptedData, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }
}`,
      Python: `from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad
import base64

def encrypt(data, key):
    cipher = AES.new(key.encode(), AES.MODE_CBC)
    ct_bytes = cipher.encrypt(pad(data.encode(), AES.block_size))
    iv = base64.b64encode(cipher.iv).decode('utf-8')
    ct = base64.b64encode(ct_bytes).decode('utf-8')
    return iv + ':' + ct

def decrypt(encrypted_data, key):
    iv, ct = encrypted_data.split(':')
    iv = base64.b64decode(iv)
    ct = base64.b64decode(ct)
    cipher = AES.new(key.encode(), AES.MODE_CBC, iv)
    pt = unpad(cipher.decrypt(ct), AES.block_size)
    return pt.decode()`,
      JavaScript: `const crypto = require('crypto');

function encrypt(text, key) {
    const iv = crypto.randomBytes(16);
    const cipher = crypto.createCipheriv('aes-256-cbc', Buffer.from(key), iv);
    let encrypted = cipher.update(text);
    encrypted = Buffer.concat([encrypted, cipher.final()]);
    return iv.toString('hex') + ':' + encrypted.toString('hex');
}

function decrypt(text, key) {
    const [ivHex, encryptedHex] = text.split(':');
    const iv = Buffer.from(ivHex, 'hex');
    const encrypted = Buffer.from(encryptedHex, 'hex');
    const decipher = crypto.createDecipheriv('aes-256-cbc', Buffer.from(key), iv);
    let decrypted = decipher.update(encrypted);
    decrypted = Buffer.concat([decrypted, decipher.final()]);
    return decrypted.toString();
}`
    }
  },
  {
    name: 'RSA加密',
    description: 'RSA非对称加密实现，支持密钥生成和加解密',
    icon: 'mdi:key-variant',
    languages: ['Java', 'Python', 'JavaScript'],
    code: {
      Java: `import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RSAEncryption {
    private static final String ALGORITHM = "RSA";
    
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
        generator.initialize(2048);
        return generator.generateKeyPair();
    }
    
    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    public static String decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }
}`,
      Python: `from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_OAEP
import base64

def generate_keys():
    key = RSA.generate(2048)
    private_key = key.export_key()
    public_key = key.publickey().export_key()
    return private_key, public_key

def encrypt(data, public_key):
    key = RSA.import_key(public_key)
    cipher = PKCS1_OAEP.new(key)
    encrypted = cipher.encrypt(data.encode())
    return base64.b64encode(encrypted).decode()

def decrypt(encrypted_data, private_key):
    key = RSA.import_key(private_key)
    cipher = PKCS1_OAEP.new(key)
    decrypted = cipher.decrypt(base64.b64decode(encrypted_data))
    return decrypted.decode()`,
      JavaScript: `const crypto = require('crypto');

function generateKeyPair() {
    return crypto.generateKeyPairSync('rsa', {
        modulusLength: 2048,
        publicKeyEncoding: {
            type: 'spki',
            format: 'pem'
        },
        privateKeyEncoding: {
            type: 'pkcs8',
            format: 'pem'
        }
    });
}

function encrypt(text, publicKey) {
    const buffer = Buffer.from(text, 'utf8');
    const encrypted = crypto.publicEncrypt(publicKey, buffer);
    return encrypted.toString('base64');
}

function decrypt(encrypted, privateKey) {
    const buffer = Buffer.from(encrypted, 'base64');
    const decrypted = crypto.privateDecrypt(privateKey, buffer);
    return decrypted.toString('utf8');
}`
    }
  },
  {
    name: 'MD5哈希',
    description: 'MD5消息摘要算法实现，用于数据完整性校验',
    icon: 'mdi:function-variant',
    languages: ['Java', 'Python', 'JavaScript'],
    code: {
      Java: `import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Hash {
    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}`,
      Python: `import hashlib

def md5_hash(text):
    return hashlib.md5(text.encode()).hexdigest()`,
      JavaScript: `const crypto = require('crypto');

function md5Hash(text) {
    return crypto.createHash('md5').update(text).digest('hex');
}`
    }
  }
])

// 下载代码
const downloadCode = (example) => {
  const blob = new Blob([JSON.stringify(example.code, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${example.name}_code_examples.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('代码示例已下载')
}

// 查看代码
const viewCode = (example) => {
  // 这里可以打开一个对话框显示代码
  ElMessage.info('代码查看功能即将推出')
}
</script>

<style scoped>
.container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.title {
  text-align: center;
  font-size: 36px;
  font-weight: 600;
  margin-bottom: 40px;
  background: linear-gradient(45deg, #edd0ff, #e465fb);
  -webkit-background-clip: text;
  -moz-background-clip: text;
  -ms-background-clip: text;
  -o-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.intro-section {
  text-align: center;
  margin-bottom: 40px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 10px;
}

.intro-text {
  font-size: 16px;
  color: #606266;
  line-height: 1.6;
}

.stats-section {
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 24px;
  color: #9c27b0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.search-section {
  margin-bottom: 30px;
  display: flex;
  gap: 20px;
  align-items: center;
}

.search-input {
  width: 300px;
}

.category-group {
  margin-left: auto;
}

.hot-tools {
  margin-bottom: 40px;
}

.hot-tools h2 {
  margin-bottom: 20px;
  color: #303133;
}

.tool-card {
  background-color: #ffffff;
  padding: 20px;
  text-align: center;
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin: 10px;
  border: 1px solid #ebeef5;
  position: relative;
  overflow: visible;
}

.tool-card.hot {
  border: 2px solid #e6a23c;
}

.hot-badge {
  position: absolute;
  top: -10px;
  right: -10px;
  background: #e6a23c;
  color: white;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  z-index: 1;
}

.tool-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.tool-icon {
  width: 80px;
  height: 80px;
  color: #9c27b0;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.tool-card:hover .tool-icon {
  transform: rotate(10deg) scale(1.1);
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.15));
}

h3 {
  color: #303133;
  margin: 10px 0;
  font-size: 18px;
}

p {
  color: #606266;
  font-size: 14px;
  min-height: 40px;
  margin-bottom: 15px;
}

.tools-section h2 {
  margin-bottom: 20px;
  color: #303133;
}

.el-row {
  margin-top: 40px;
  justify-content: center;
}

.tool-card :deep(.el-button) {
  background-color: white;
  color: #9c27b0;
  border: 1px solid #9c27b0;
}

.tool-card :deep(.el-button:hover) {
  background-color: #9c27b0;
  color: white;
}

.code-examples-section {
  margin-top: 40px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 10px;
}

.code-examples-section h2 {
  text-align: center;
  color: #303133;
  margin-bottom: 30px;
}

.code-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.code-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.code-icon {
  font-size: 24px;
  color: #9c27b0;
}

.code-description {
  color: #606266;
  margin-bottom: 15px;
  flex-grow: 1;
}

.code-languages {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.language-tag {
  background-color: #f0f2f5;
  border: none;
}

.code-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>