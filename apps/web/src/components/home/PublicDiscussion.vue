<template>
  <div class="public-discussion">
    <div class="discussion-header">
      <h1>公共讨论区</h1>
      <div class="header-actions">
        <el-button type="success" @click="showCreateRoomDialog" :icon="Plus">创建房间</el-button>
        <el-button type="primary" @click="createNewTopic" :icon="Plus">发起新话题</el-button>
      </div>
    </div>

    <!-- 话题分类标签 -->
    <div class="topic-categories">
      <el-tag
        v-for="category in categories"
        :key="category.value"
        :type="category.type"
        effect="dark"
        class="category-tag"
        @click="selectCategory(category.value)"
        :class="{ active: selectedCategory === category.value }"
        :data-category="category.value"
      >
        <el-icon class="tag-icon"><component :is="category.icon" /></el-icon>
        {{ category.label }}
        <span class="room-count" v-if="category.roomCount">({{ category.roomCount }})</span>
      </el-tag>
    </div>

    <!-- 房间列表 -->
    <div class="rooms-list" v-if="selectedCategory !== 'all'">
      <el-card v-for="room in filteredRooms" :key="room.id" class="room-card" :class="{ active: selectedRoom?.id === room.id }">
        <div class="room-info" @click="selectRoom(room)">
          <div class="room-header">
            <h3>{{ room.name }}</h3>
            <el-tag size="small" :type="room.type">{{ room.category }}</el-tag>
          </div>
          <div class="room-meta">
            <span class="online-count">
              <el-icon><User /></el-icon>
              {{ room.onlineCount }}人在线
            </span>
            <span class="message-count">
              <el-icon><ChatDotRound /></el-icon>
              {{ room.messageCount }}条消息
            </span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-container" v-if="selectedRoom" :class="{ 'room-expanded': selectedRoom }">
      <div class="chat-header">
        <div class="room-title">
          <h2>{{ selectedRoom.name }}</h2>
          <div class="room-stats">
            <span class="stat-item">
              <el-icon><User /></el-icon>
              {{ selectedRoom.onlineCount }}人在线
            </span>
            <span class="stat-item">
              <el-icon><ChatDotRound /></el-icon>
              {{ selectedRoom.messageCount }}条消息
            </span>
            <el-tag size="small" :type="selectedRoom.type">{{ selectedRoom.category }}</el-tag>
          </div>
        </div>
        <div class="room-actions">
          <el-button link @click="toggleRoomInfo">
            <el-icon><InfoFilled /></el-icon>
            房间信息
          </el-button>
          <el-button link @click="leaveRoom" v-if="isRoomCreator">
            <el-icon><Delete /></el-icon>
            解散房间
          </el-button>
          <el-button link class="exit-button" @click="exitRoom">
            <el-icon><Close /></el-icon>
            退出房间
          </el-button>
        </div>
      </div>

      <div class="chat-messages" ref="chatMessages">
        <div v-for="(message, index) in messages" :key="index" 
             class="message" 
             :class="{ 
               'message-self': message.userId === store.state.id,
               'system-message': message.type === 'join' || message.type === 'leave'
             }">
          <!-- Avatar Section -->
          <div class="message-avatar" v-if="message.type !== 'join' && message.type !== 'leave'">
            <el-avatar :size="32" :src="message.avatar" />
          </div>
          <!-- Message Content Area -->
          <div class="message-content-area" :class="{ 'system-content': message.type === 'join' || message.type === 'leave' }">
            <div class="message-header" v-if="message.type !== 'join' && message.type !== 'leave'">
              <span class="username">{{ message.username }}</span>
              <span class="time">{{ formatTime(message.timestamp) }}</span>
            </div>
            <div class="message-content" :class="{ 'system-content': message.type === 'join' || message.type === 'leave' }">
              {{ message.content }}
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <div class="input-toolbar">
          <div class="toolbar-left">
            <el-tooltip content="上传图片" placement="top">
              <el-button link @click="showFeatureComingSoon('图片上传')">
                <el-icon><Picture /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="上传视频" placement="top">
              <el-button link @click="showFeatureComingSoon('视频上传')">
                <el-icon><VideoCamera /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="上传文件" placement="top">
              <el-button link @click="showFeatureComingSoon('文件上传')">
                <el-icon><Folder /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
          <div class="toolbar-right">
            <el-tooltip content="表情" placement="top">
              <el-button link @click="toggleEmojiPicker">😊</el-button>
            </el-tooltip>
          </div>
        </div>

        <!-- Emoji Picker Panel -->
        <div v-if="emojiPickerVisible" class="emoji-picker">
          <div class="emoji-categories">
            <span
              v-for="category in categorizedEmojis"
              :key="category.name"
              class="emoji-category-label"
              :class="{ active: activeEmojiCategory === category.name }"
              @click="scrollToCategory(category.name)"
            >{{ category.icon }}</span>
          </div>
          <div class="emoji-list-container">
            <div
              v-for="category in categorizedEmojis"
              :key="category.name"
              :ref="(el) => { if (el) emojiCategoryRefs[category.name] = el; }"
              class="emoji-category"
            >
              <h4>{{ category.name }}</h4>
              <span
                v-for="emoji in category.emojis"
                :key="emoji"
                @click="insertEmoji(emoji)"
                class="emoji-item"
              >{{ emoji }}</span>
            </div>
          </div>
        </div>

        <el-input
          v-model="newMessage"
          type="textarea"
          :rows="3"
          placeholder="输入消息..."
          @keyup.enter.ctrl="sendMessage"
          ref="messageInputRef"
        />
        <div class="input-actions">
          <span class="tip">按 Ctrl + Enter 发送</span>
          <el-button type="primary" @click="sendMessage" :disabled="!newMessage.trim()">
            <el-icon><Position /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </div>

    <!-- 创建房间对话框 -->
    <el-dialog
      v-model="createRoomDialogVisible"
      title="创建新房间"
      width="500px"
    >
      <el-form :model="newRoom" label-width="80px" :rules="roomRules" ref="roomForm">
        <el-form-item label="房间名称" prop="name">
          <el-input v-model="newRoom.name" placeholder="请输入房间名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="newRoom.category" placeholder="请选择分类">
            <el-option
              v-for="category in categories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="newRoom.description"
            type="textarea"
            :rows="3"
            placeholder="请输入房间描述"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="newRoom.password"
            type="password"
            placeholder="可选：设置房间密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createRoomDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createRoom">创建</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 房间信息抽屉 -->
    <el-drawer
      v-model="roomInfoVisible"
      title="房间信息"
      direction="rtl"
      size="300px"
    >
      <div class="room-info-content" v-if="selectedRoom">
        <h3>{{ selectedRoom.name }}</h3>
        <p class="description">{{ selectedRoom.description }}</p>
        <div class="info-section">
          <h4>在线成员 ({{ selectedRoom.onlineCount }})</h4>
          <div class="member-list">
            <div v-for="member in selectedRoom.members" :key="member.id" class="member-item">
              <el-avatar :size="24" :src="member.avatar" />
              <span>{{ member.username }}</span>
              <el-tag size="small" v-if="member.isCreator">创建者</el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 密码输入对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="加入房间"
      width="400px"
      :close-on-click-modal="false"
      class="password-dialog"
    >
      <div class="room-preview" v-if="pendingRoom">
        <div class="room-preview-header">
          <h3>{{ pendingRoom.title }}</h3>
          <el-tag :type="getCategoryType(pendingRoom.category)" size="small">
            {{ getCategoryLabel(pendingRoom.category) }}
          </el-tag>
        </div>
        <div class="room-stats">
          <div class="stat-item">
            <el-icon><User /></el-icon>
            <span>{{ pendingRoom.onlineCount }}人在线</span>
          </div>
          <div class="stat-item">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ pendingRoom.messageCount }}条消息</span>
          </div>
        </div>
      </div>
      <el-form :model="passwordForm" label-width="80px" class="password-form">
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="passwordForm.password"
            type="password"
            placeholder="请输入房间密码"
            show-password
            @keyup.enter="confirmPassword"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmPassword" :loading="joiningRoom">
            <el-icon><Position /></el-icon>
            加入房间
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, markRaw } from 'vue'
import { 
  Plus, 
  User, 
  InfoFilled, 
  Delete, 
  Close,
  ChatDotRound,
  Monitor,
  Lock,
  Warning,
  Tools,
  Share,
  Picture,
  VideoCamera,
  Folder,
  Position
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { useStore } from 'vuex'
import ToUrl from '@/api/api'
import request from '@/axios'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const store = useStore()
const currentUserId = computed(() => store.state.user)
const currentUserAvatar = computed(() => `${ToUrl.url}/${store.state.avatar}`)

// WebSocket 相关方法
let ws = null
let reconnectAttempts = 0
const MAX_RECONNECT_ATTEMPTS = 5 // 最大重连次数
const RECONNECT_INTERVAL = 3000 // 重连间隔时间（毫秒）
const wsUrl = 'ws://localhost:8080' // 直接指定 WebSocket 服务器地址

// 分类数据
const categories = ref([
  { label: '技术讨论', value: 'tech', type: 'success', icon: markRaw(Monitor), roomCount: 0 },
  { label: '安全讨论', value: 'security', type: 'warning', icon: markRaw(Lock), roomCount: 0 },
  { label: '漏洞分析', value: 'vulnerability', type: 'danger', icon: markRaw(Warning), roomCount: 0 },
  { label: '工具分享', value: 'tools', type: 'info', icon: markRaw(Tools), roomCount: 0 },
  { label: '经验分享', value: 'experience', type: 'primary', icon: markRaw(Share), roomCount: 0 }
])

// 状态变量
const selectedCategory = ref('tech')
const selectedRoom = ref(null)
const messages = ref([])
const newMessage = ref('')
const chatMessages = ref(null)
const createRoomDialogVisible = ref(false)
const roomInfoVisible = ref(false)
const passwordDialogVisible = ref(false)
const passwordForm = ref({
  password: ''
})
const pendingRoom = ref(null)

// 房间列表
const rooms = ref([])

// 新建房间表单
const newRoom = ref({
  name: '',
  category: '',
  description: '',
  password: ''
})

// 表单验证规则
const roomRules = {
  name: [
    { required: true, message: '请输入房间名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入房间描述', trigger: 'blur' },
    { min: 5, max: 100, message: '长度在 5 到 100 个字符', trigger: 'blur' }
  ]
}

// 获取分类显示名称
const getCategoryLabel = (category) => {
  const categoryMap = {
    'tech': '技术讨论',
    'security': '安全讨论',
    'vulnerability': '漏洞分析',
    'tools': '工具分享',
    'experience': '经验分享'
  }
  return categoryMap[category] || category
}

// 获取分类对应的类型
const getCategoryType = (category) => {
  const categoryMap = {
    'tech': 'success',
    'security': 'warning',
    'vulnerability': 'danger',
    'tools': 'info',
    'experience': 'primary',
    '技术讨论': 'success',
    '安全讨论': 'warning',
    '漏洞分析': 'danger',
    '工具分享': 'info',
    '经验分享': 'primary'
  }
  return categoryMap[category] || 'info'
}

// 计算属性：根据分类筛选房间
const filteredRooms = computed(() => {
  const selectedCategoryLabel = getCategoryLabel(selectedCategory.value)
  const filtered = rooms.value.filter(room => {
    const roomCategoryLabel = getCategoryLabel(room.category)
    return roomCategoryLabel === selectedCategoryLabel
  })
  return filtered
})

// 判断是否为房间创建者
const isRoomCreator = computed(() => {
  if (!selectedRoom.value) return false
  return false
})

// 加载房间列表
const loadRooms = async () => {
  try {
    const response = await request.get('/bmgfChat/rooms/chatAllroom')
    rooms.value = response.data.map(room => {
      const roomData = {
        id: room.id,
        name: room.title,
        category: room.category,
        hasPassword: room.needPassword,
        onlineCount: room.onlineCount || 0,
        messageCount: room.messageCount || 0,
        type: getCategoryType(room.category),
        expireAt: room.expireAt
      }
      return roomData
    })
    await loadCategorySummary()
  } catch (error) {
    console.error('加载房间列表失败:', error)
    ElMessage.error('加载房间列表失败')
  }
}

// 加载分类统计
const loadCategorySummary = async () => {
  try {
    const response = await request.get('/bmgfChat/rooms/category/summary')
    const summary = response.data
    
    // 更新分类的房间数量
    categories.value = categories.value.map(category => {
      const categorySummary = summary.find(item => item.category === category.value)
      return {
        ...category,
        roomCount: categorySummary ? categorySummary.count : 0
      }
    })
  } catch (error) {
    console.error('加载分类统计失败:', error)
  }
}

// 方法
const showCreateRoomDialog = () => {
  createRoomDialogVisible.value = true
}

const createRoom = async () => {
  try {
    const roomData = {
      title: newRoom.value.name,
      category: newRoom.value.category,
      password: newRoom.value.password || null,
      expireAt: '2030-12-31T23:59:59' // 设置默认过期时间
    }
    
    await request.post('/bmgfChat/rooms', roomData)
    
  ElMessage.success('房间创建成功！')
  createRoomDialogVisible.value = false
    await loadRooms()
  } catch (error) {
    ElMessage.error('创建房间失败：' + (error.response?.data || error.message))
  }
}

const selectRoom = async (room) => {
  if (selectedRoom.value?.id === room.id) return
  
  try {
    const response = await request.get(`/bmgfChat/rooms/${room.id}`)
    const roomData = response.data
    
    // 更新房间列表中的实时数据
    const roomIndex = rooms.value.findIndex(r => r.id === room.id)
    if (roomIndex !== -1) {
      rooms.value[roomIndex].onlineCount = roomData.onlineCount || 0
      rooms.value[roomIndex].messageCount = roomData.messageCount || 0
    }
    
    // 如果需要密码且不是当前用户创建的房间
    if (roomData.hasPassword) {
      pendingRoom.value = roomData
      passwordDialogVisible.value = true
      return
    }
    
    // 不需要密码，直接加入
    await joinRoom(roomData)
  } catch (error) {
    ElMessage.error('获取房间详情失败')
  }
}

const toggleRoomInfo = () => {
  roomInfoVisible.value = !roomInfoVisible.value
}

const leaveRoom = async () => {
  try {
    await ElMessageBox.confirm('确定要解散该房间吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
    })
    
    await request.delete(`/bmgfChat/rooms/${selectedRoom.value.id}`)
    
    ElMessage.success('房间已解散')
    selectedRoom.value = null
    await loadRooms()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('解散房间失败：' + (error.response?.data || error.message))
    }
  }
}

// 心跳检测相关
let heartbeatInterval = null

// 添加心跳检测
const startHeartbeat = () => {
  // 清除可能存在的旧定时器
  if (heartbeatInterval) {
    clearInterval(heartbeatInterval)
  }

  heartbeatInterval = setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      // 在当前活动的WebSocket连接上发送心跳消息
      ws.send(JSON.stringify({ type: 'heartbeat' }))
    }
  }, 30000) // 每30秒发送一次心跳
}

// 创建全局WebSocket连接 (用于房间总览)
const createGlobalWebSocket = () => {
  if (ws) {
    // 如果已经有连接（可能是聊天连接），先关闭
    if (ws.url.includes('/ws/chat')) {
       ws.close()
       ws = null // 清空ws，避免影响后续判断
    }
  }

  // 创建新的 WebSocket 连接，添加 token 和 userId
  const token = store.state.token
  const userId = store.state.id
  const overviewSocket = new WebSocket(`${wsUrl}/ws/overview?token=${token}&userId=${userId}`)

  // 设置事件处理器
  overviewSocket.onopen = () => {
    // 连接成功
  }

  overviewSocket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)

      // 处理房间信息更新
      if (data.type === 'roomOverviewUpdate' && Array.isArray(data.rooms)) {
        // 遍历接收到的房间列表，更新本地数据
        data.rooms.forEach(updatedRoom => {
          const roomIndex = rooms.value.findIndex(r => r.id === updatedRoom.id)
          if (roomIndex !== -1) {
            // 只更新在线人数和消息数量
            rooms.value[roomIndex].onlineCount = updatedRoom.onlineCount !== undefined ? updatedRoom.onlineCount : rooms.value[roomIndex].onlineCount
            rooms.value[roomIndex].messageCount = updatedRoom.messageCount !== undefined ? updatedRoom.messageCount : rooms.value[roomIndex].messageCount

            // 如果当前选中的房间是这个房间，也更新选中房间的统计信息
            if (selectedRoom.value?.id === updatedRoom.id) {
              selectedRoom.value.onlineCount = rooms.value[roomIndex].onlineCount
              selectedRoom.value.messageCount = rooms.value[roomIndex].messageCount
            }
          }
        })
      } else if (data.type === 'roomInfo') {
         // 处理单个房间信息更新
         const roomIndex = rooms.value.findIndex(r => r.id === data.roomId)
         if (roomIndex !== -1) {
           rooms.value[roomIndex].onlineCount = data.onlineCount !== undefined ? data.onlineCount : rooms.value[roomIndex].onlineCount
           rooms.value[roomIndex].messageCount = data.messageCount !== undefined ? data.messageCount : rooms.value[roomIndex].messageCount

           if (selectedRoom.value?.id === data.roomId) {
              selectedRoom.value.onlineCount = rooms.value[roomIndex].onlineCount
              selectedRoom.value.messageCount = rooms.value[roomIndex].messageCount
           }
         }
      }
    } catch (error) {
      // 解析消息失败
    }
  }

  overviewSocket.onerror = (error) => {
    // WebSocket错误
  }

  overviewSocket.onclose = (event) => {
    // WebSocket连接关闭
  }
}

// 加入房间时创建聊天WebSocket连接
const connectChatWebSocket = (roomId) => {
  // 如果已经有全局连接，先关闭（因为现在要建立聊天连接）
  if (ws && ws.url.includes('/ws/overview')) {
    ws.close()
    ws = null
  }
  
  // 如果已经有聊天连接到同一个房间，不做处理
  if (ws && ws.url.includes(`/ws/chat?roomId=${roomId}`) && ws.readyState === WebSocket.OPEN) {
     return ws
  }
  
  // 如果有连接到不同房间的聊天连接，先关闭
   if (ws && ws.url.includes('/ws/chat')) {
     ws.close()
     ws = null
   }

  // 创建新的 WebSocket 连接，需要房间ID、token和userId
  const token = store.state.token
  const userId = store.state.id
  const chatWs = new WebSocket(`${wsUrl}/ws/chat?roomId=${roomId}&token=${token}&userId=${userId}`)

  chatWs.onopen = () => {
    reconnectAttempts = 0 // 重置重连次数
    startHeartbeat() // 启动心跳检测
    
    // 发送加入消息
    chatWs.send(JSON.stringify({
      type: 'join', // 加入消息类型
      roomId: roomId,
      senderId: store.state.id,
      content: '加入了聊天室',
      timestamp: new Date().toISOString()
    }))
  }

  chatWs.onmessage = (event) => {
    try {
      // 检查消息是否为空
      if (!event.data || event.data.trim() === '') {
        console.warn('收到空消息');
        return;
      }

      let data;
      try {
        data = JSON.parse(event.data);
      } catch (parseError) {
        console.warn('消息解析失败:', event.data);
        return;
      }

      // 忽略心跳消息
      if (data.type === 'heartbeat') {
        return;
      }

      // 检查并更新房间统计信息
      if (data.onlineCount !== undefined && data.messageCount !== undefined) {
        if (selectedRoom.value) {
          selectedRoom.value.onlineCount = data.onlineCount;
          selectedRoom.value.messageCount = data.messageCount;
        }
      }

      // 处理不同类型的消息
      if (!data.type) {
        // 如果没有type字段，检查是否是聊天消息
        if (data.senderId && data.content !== undefined) {
          messages.value.push({
            userId: data.senderId,
            username: data.senderName || data.senderId,
            content: data.content,
            timestamp: new Date(data.timestamp || Date.now()),
            avatar: data.senderAvatarUrl ? `${ToUrl.url}/${data.senderAvatarUrl}` : ''
          });
          scrollToBottom();
        }
        return;
      }

      switch (data.type) {
        case 'roomInfo':
          // 处理房间信息更新
          break;

        case 'timer':
          // 处理房间倒计时消息
          if (selectedRoom.value && data.minutesLeft !== undefined && data.minutesLeft <= 0) {
            ElMessage.warning('房间已过期，即将关闭');
            setTimeout(() => {
              exitRoom();
            }, 3000); // 3秒后关闭房间
          }
          break;

        case 'join':
        case 'leave':
          // 处理加入/离开消息
          if (data.senderName) {
            messages.value.push({
              type: data.type,
              content: `${data.senderName} ${data.type === 'join' ? '加入了' : '离开了'}聊天室`,
              timestamp: new Date(data.timestamp || Date.now())
            });
            scrollToBottom();
          }
          break;

        default:
          // 处理其他类型的消息
          console.warn('未知消息类型:', data.type, data);
          break;
      }

    } catch (error) {
      console.error('处理消息失败:', error, '原始消息:', event.data);
    }
  }

  chatWs.onerror = (error) => {
    ElMessage.error('聊天连接出错，请刷新页面重试')
    // 错误发生时，尝试关闭连接以便触发onclose进行重连
    if (chatWs && chatWs.readyState !== WebSocket.CLOSED && chatWs.readyState !== WebSocket.CLOSING) {
        chatWs.close()
    }
  }

  chatWs.onclose = (event) => {
    // 清除心跳定时器
    if (heartbeatInterval) {
      clearInterval(heartbeatInterval)
      heartbeatInterval = null
    }

    // 正常关闭或页面卸载时不重连
    if (event.code === 1000 || event.code === 1001) {
      return
    }
    
    // 只有当selectedRoom还有值且不是正常关闭时才尝试重连聊天连接
    if (selectedRoom.value && selectedRoom.value.id && event.code !== 1000 && event.code !== 1001 && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
      reconnectAttempts++
      
      // 指数退避延迟
      const delay = RECONNECT_INTERVAL * Math.pow(2, reconnectAttempts - 1)
      setTimeout(() => {
        if (selectedRoom.value && selectedRoom.value.id) {  // 再次检查，确保在延迟期间值仍然存在
          connectChatWebSocket(selectedRoom.value.id) // 尝试重连当前房间
        }
      }, delay)
    } else if (selectedRoom.value) {
       ElMessage.error('聊天服务器连接失败，请尝试重新进入房间或刷新页面')
       messages.value = [];
    }
  }

  return chatWs
}

// 加入房间
const joinRoom = async (roomData) => {
  try {
    // 转换数据格式
    selectedRoom.value = {
      id: roomData.id,
      name: roomData.title,
      category: roomData.category,
      hasPassword: roomData.needPassword,
      onlineCount: roomData.onlineCount || 0,
      messageCount: roomData.messageCount || 0,
      type: getCategoryType(roomData.category)
    }
    
    messages.value = [] // 清空消息列表，等待WebSocket发送历史消息
    
    // 连接聊天WebSocket
    ws = connectChatWebSocket(roomData.id)
    
    // 禁止背景滚动
    preventBackgroundScroll()
    
  } catch (error) {
    console.error('加入房间失败:', error)
    ElMessage.error('加入房间失败：' + (error.response?.data || error.message))
  }
}

// 发送消息
const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedRoom.value || !ws || ws.readyState !== WebSocket.OPEN) return

  const message = {
    roomId: selectedRoom.value.id,
    senderId: store.state.id,
    content: newMessage.value,
    timestamp: new Date().toISOString()
  }

  ws.send(JSON.stringify(message))
  newMessage.value = ''
}

const selectCategory = async (category) => {
  if (category === selectedCategory.value) return
  // console.log('Selecting category:', category)
  selectedCategory.value = category
  selectedRoom.value = null
  messages.value = []
}

const formatTime = (time) => {
  return dayjs(time).fromNow()
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatMessages.value) {
    chatMessages.value.scrollTop = chatMessages.value.scrollHeight
  }
}

// 退出房间
const exitRoom = () => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    // 发送离开消息
    ws.send(JSON.stringify({
      type: 'leave', // 离开消息类型
      roomId: selectedRoom.value.id,
      senderId: store.state.id,
      content: '离开了聊天室',
      timestamp: new Date().toISOString()
    }))
     // 关闭连接
    ws.close(1000, 'User left room') // 使用正常关闭代码1000
  }
   // 清理状态
  selectedRoom.value = null
  messages.value = []
  
  // 清除心跳定时器
  if (heartbeatInterval) {
      clearInterval(heartbeatInterval)
      heartbeatInterval = null
    }
    
  // 恢复背景滚动
  restoreBackgroundScroll()
    
  // 退出房间后，重新建立全局overview连接，用于刷新列表统计
  createGlobalWebSocket()
}

// 确认密码
const confirmPassword = async () => {
  if (!pendingRoom.value) return
  
  try {
    const response = await request.post(
      `/bmgfChat/rooms/${pendingRoom.value.id}/join`,
      { password: passwordForm.value.password }
    )
    
    if (response.data === 'joined') {
      ElMessage.success('成功加入房间')
      passwordDialogVisible.value = false
      passwordForm.value.password = ''
      await joinRoom(pendingRoom.value)
    } else {
      ElMessage.error('密码错误')
    }
  } catch (error) {
    ElMessage.error('加入房间失败：' + (error.response?.data || error.message))
  }
}

// 创建新话题
const createNewTopic = () => {
  ElMessage.info('新话题功能开发中...')
}

// --- Emoji  ---
const emojiPickerVisible = ref(false);
const messageInputRef = ref(null); // 输入框引用
const emojiCategoryRefs = ref({}); // 表情分类引用
const activeEmojiCategory = ref('常用'); // 当前选中的表情分类

const categorizedEmojis = ref([
  {
    name: '常用',
    icon: '😊',
    emojis: ['😊', '😂', '😍', '👍', '🙏', '🎉', '😢', '👋', '🔥', '❤️']
  },
  {
    name: '笑脸和表情',
    icon: '😀',
    emojis: ['😀', '😃', '😄', '😅', '😆', '😇', '😈', '😉', '😊', '😋', '😎', '😏', '😒', '😞', '😔', '😟', '😠', '😡', '😢', '😭', '😱', '😖', '😰', '😥', '😯', '😲', '😳', '😴', '😵', '😬', '🤨', '🧐', '🤔', '🤫', '🤭', '🤥', '🤤', '🤪', '😜', '😝', '😛', '🤑']
  },
  {
    name: '人物',
    icon: '👨‍💻',
    emojis: ['👶', '👧', '👦', '👩', '👨', '👵', '👴', '👲', '👳', '👷‍♀️', '👷‍♂️', '💂‍♀️', '💂‍♂️', '🕵️‍♀️', '🕵️‍♂️', '👩‍⚕️', '👨‍⚕️', '👩‍🎓', '👨‍🎓', '👩‍🏫', '👨‍🏫', '👩‍⚖️', '👨‍⚖️', '👩‍🌾', '👨‍🌾', '👩‍🍳', '👨‍🍳', '👩‍🔧', '👨‍🔧', '👩‍🏭', '👨‍🏭', '👩‍💼', '👨‍💼', '👩‍🔬', '👨‍🔬', '👩‍💻', '👨‍💻', '👩‍🎤', '👨‍🎤', '👩‍🎨', '👨‍🎨', '👩‍✈️', '👨‍✈️', '👩‍🚀', '👨‍🚀', '👩‍🚒', '👨‍🚒', '👸', '🤴', '👰‍♀️', '👰‍♂️', '🤵‍♀️', '🤵‍♂️', '👼', '🎅', '🤶', '🦸‍♀️', '🦸‍♂️', '🦹‍♀️', '🦹‍♂️', '🧙‍♀️', '🧙‍♂️', '🧚‍♀️', '🧚‍♂️', '🧛‍♀️', '🧛‍♂️', '🧜‍♀️', '🧜‍♂️', '🧝‍♀️', '🧝‍♂️', '🧞‍♀️', '🧞‍♂️', '🧟‍♀️', '🧟‍♂️', '🗣️', '👤', '👥', '🫂', '🚶‍♀️', '🚶‍♂️', '🏃‍♀️', '🏃‍♂️', '🧍‍♀️', '🧍‍♂️', '👫', '👬', '👭', '💑', '👪', '👨‍👦', '👨‍👩‍👦', '👨‍👩‍👧‍👦', '👨‍👩‍👦‍👦', '👨‍👩‍👧‍👧', '👨‍👦', '👨‍👦‍👦', '👨‍👧', '👨‍👧‍👦', '👨‍👧‍👧', '👩‍👦', '👩‍👦‍👦', '👩‍👧', '👩‍👧‍👦', '👩‍👧‍👧', '🏳️‍⚧️', '⚧️']
  },
   {
    name: '动物和自然',
    icon: '🐶',
    emojis: ['🐶', '🐱', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐨', '🐯', '🦁', '🐮', '🐷', '🐸', '🐵', '🐔', '🐧', '🐦', '🐤', '🦆', '🦅', '🦉', '🦇', '🐺', '🐗', '🐴', '🦄', '🐝', '🐛', '🦋', '🐌', '🐞', '🐜', '🦟', '🦗', '🕷️', '🦂', '🐢', '🐍', '🦎', '🦖', '🦕', '🐙', '🦑', '🦐', '🦞', '🦀', '🐡', '🐠', '🐟', '🐳', '🐬', '🦈', '🐊', '🐅', '🐆', '🦓', '🦍', '🦧', '🐘', '🦛', '🦏', '🐪', '🐫', '🦒', '🐃', '🐂', '🐄', '🐎', '🐖', '🐏', '🐑', '🐐', '🦌', '🐕', '🐩', '🐈', '🐓', '🦃', '🦢', '🕊️', '🐇', '🐿️', '🦨', '🦡', '🦫', '🦦', '🦥', '🐁', '🐀', '🐾', '🌳', '🌲', '🌴', '🌵', '🌾', '🌿', '🍀', '🍁', '🍂', '🍃', '💐', '🌸', '🌹', '🥀', '🌺', '🌻', '🌼', '🌷', '🌱', '🌲', '🌳', '🌴', '🌵', '🌾', '🌿', '🍀', '🍁', '🍂', '🍃', '🍇', '🍈', '🍉', '🍊', '🍋', '🍌', '🍍', '🥭', '🍎', '🍏', '🍐', '🍑', '🍒', '🍓', '🥝', '🍅', '🥥', '🥑', '🍆', '🥔', '🥕', '🌽', '🌶️', '🥒', '🥬', '🥦', '🧄', '🧅', '🍄']
  },
  {
    name: '食物和饮料',
    icon: '🍔',
    emojis: ['🍔', '🍟', '🍕', '🌮', '🌯', '🍿', '🧂', '🥫', '🍱', '🍘', '🍙', '🍚', '🍛', '🍜', '🍝', '🍠', '🥟', '🍤', '🍣', '🍥', '🥮', '🍢', '🍡', '🍧', '🍨', '🍦', '🥧', '🧁', '🍰', '🎂', '🍮', '🍬', '🍭', '🍫', '🍩', '🍪', '🌰', '🥜', '🍯', '🥛', '🍼', '☕', '🍵', '🍶', '🍾', '🍷', '🍸', '🍹', '🍺', '🍻', '🥂', '🥃', '🥤', '🧋', '🧊', '🥢', '🍽️', '🍴', '🥄', '🔪', '🏺']
  },
  {
    name: '符号',
    icon: '❤️',
    emojis: ['❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '✨', '💢', '💥', '💫', '💦', '💨', '💩', '💯', '🔥', '💡', '💥', '💫', '💦', '💨', '💩', '💯', '🔔', '🔕', '📣', '📢', '👁️‍🗨️', '💬', '💭', '🗯️', '♠️', '♣️', '♥️', '♦️', '🃏', '🎴', '通']
  },
]);

const toggleEmojiPicker = () => {
  emojiPickerVisible.value = !emojiPickerVisible.value;
  if (emojiPickerVisible.value) {
    nextTick(() => {
      messageInputRef.value?.focus();
      activeEmojiCategory.value = categorizedEmojis.value[0]?.name || '';
      const emojiListContainer = document.querySelector('.emoji-list-container');
      if (emojiListContainer) {
        emojiListContainer.scrollTop = 0;
      }
    });
  }
};

const insertEmoji = (emoji) => {
  const input = messageInputRef.value?.textarea;
  if (!input) return;

  const start = input.selectionStart;
  const end = input.selectionEnd;

  newMessage.value = newMessage.value.substring(0, start) +
                     emoji +
                     newMessage.value.substring(end);

  nextTick(() => {
    input.selectionStart = input.selectionEnd = start + emoji.length;
    input.focus();
  });

  emojiPickerVisible.value = false;
};

const scrollToCategory = (categoryName) => {
  const categoryElement = emojiCategoryRefs.value[categoryName];
  if (categoryElement) {
    categoryElement.scrollIntoView({ behavior: 'smooth' });
    activeEmojiCategory.value = categoryName;
  }
};


const emojiListContainer = ref(null);

onMounted(() => {

  emojiListContainer.value = document.querySelector('.emoji-list-container');
  if (emojiListContainer.value) {
    emojiListContainer.value.addEventListener('scroll', handleEmojiListScroll);
  }
});

onUnmounted(() => {

  if (emojiListContainer.value) {
    emojiListContainer.value.removeEventListener('scroll', handleEmojiListScroll);
  }
});

const handleEmojiListScroll = () => {
  if (!emojiListContainer.value) return;

  const containerTop = emojiListContainer.value.getBoundingClientRect().top;


  for (const category of categorizedEmojis.value) {
    const element = emojiCategoryRefs.value[category.name];
    if (element) {
      const elementTop = element.getBoundingClientRect().top;

      if (elementTop <= containerTop + 20) {
        activeEmojiCategory.value = category.name;

        break;
      }
    }
  }
};


const preventBackgroundScroll = () => {
  document.body.style.overflow = 'hidden';
}

const restoreBackgroundScroll = () => {
  document.body.style.overflow = '';
}

onMounted(async () => {
  await loadRooms()
  createGlobalWebSocket()
})

onUnmounted(() => {
  if (ws) {
    ws.close(1000, 'Component unmounted')
    ws = null
  }
  if (heartbeatInterval) {
    clearInterval(heartbeatInterval)
    heartbeatInterval = null
  }
  // 确保恢复背景滚动
  restoreBackgroundScroll()
})

// 添加新功能提示方法
const showFeatureComingSoon = (feature) => {
  ElMessage({
    message: `${feature}功能正在开发中，敬请期待！`,
    type: 'info',
    duration: 2000
  })
}
</script>

<style scoped>
.public-discussion {
  max-width: 100%;
  margin: 0;
  padding: 20px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

.discussion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 0;
  border-bottom: 1px solid #e4e7ed;
  background:linear-gradient(135deg, #7c3aed, #b5a0f4);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.header-actions {
  display: flex;
  gap: 12px;
  margin-right: 20px;
}

.discussion-header h1 {
  margin: 0;
  font-size: 32px;
  color: #ffffff;
  font-weight: 600;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
  margin-left: 20px;
  position: relative;
  padding-left: 15px;
}

.discussion-header h1::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 70%;
  background: #409EFF;
  border-radius: 2px;
}

.header-actions .el-button {
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.header-actions .el-button--success {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
}

.header-actions .el-button--primary {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
}

.topic-categories {
  margin: 24px 0;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.category-tag {
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 10px 20px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 6px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  color: #606266;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.category-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
  color: #409EFF;
}

.category-tag.active {
  background: #409EFF;
  color: #ffffff;
  border-color: #409EFF;
  transform: scale(1.05);
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.tag-icon {
  font-size: 16px;
  opacity: 0.9;
}

.category-tag:hover .tag-icon {
  opacity: 1;
}

.category-tag.active .tag-icon {
  opacity: 1;
  color: #ffffff;
}

.room-count {
  font-size: 12px;
  opacity: 0.8;
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 4px;
}

.category-tag.active .room-count {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

/* 为不同分类设置不同的边框颜色 */
.category-tag[data-category="tech"] {
  border-left: 3px solid #67c23a;
}

.category-tag[data-category="security"] {
  border-left: 3px solid #e6a23c;
}

.category-tag[data-category="vulnerability"] {
  border-left: 3px solid #f56c6c;
}

.category-tag[data-category="tools"] {
  border-left: 3px solid #909399;
}

.category-tag[data-category="experience"] {
  border-left: 3px solid #409EFF;
}

/* 选中状态下的边框颜色 */
.category-tag.active[data-category="tech"] {
  border-left-color: #67c23a;
}

.category-tag.active[data-category="security"] {
  border-left-color: #e6a23c;
}

.category-tag.active[data-category="vulnerability"] {
  border-left-color: #f56c6c;
}

.category-tag.active[data-category="tools"] {
  border-left-color: #909399;
}

.category-tag.active[data-category="experience"] {
  border-left-color: #409EFF;
}

.rooms-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
  padding: 16px;

  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.room-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  background: #ffffff;
  overflow: hidden;
}

.room-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
}

.room-card.active {
  border: 2px solid #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.room-info {
  padding: 16px;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.room-header h3 {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
  font-weight: 600;
}

.room-meta {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 13px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.room-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}

.room-meta .el-icon {
  font-size: 16px;
  color: #409EFF;
}

.chat-container {
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform-origin: center;
  opacity: 0;
  transform: scale(0.95);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
}

.room-expanded {
  opacity: 1;
  transform: scale(1);
}

.chat-header {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #1a1a1a 0%, #2c3e50 100%);
  position: relative;
  z-index: 1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.room-title {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.room-title h2 {
  margin: 0;
  font-size: 24px;
  color: #ffffff;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

.room-stats {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 14px;
  color: #e4e7ed;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e4e7ed;
  font-size: 14px;
}

.room-actions {
  display: flex;
  gap: 16px;
}

.room-actions .el-button {
  color: #ffffff;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.room-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.exit-button {
  color: #f56c6c !important;
  background: rgba(245, 108, 108, 0.1) !important;
  border-color: rgba(245, 108, 108, 0.2) !important;
}

.exit-button:hover {
  background: rgba(245, 108, 108, 0.2) !important;
}

.chat-messages {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background: #f5f7fa;
  scroll-behavior: smooth;
}

.message {
  max-width: 70%;
  align-self: flex-start;
  display: flex;
  gap: 12px;
  animation: messageSlideIn 0.3s ease-out;
  align-items: flex-start;
  padding: 0 20px;
}

.message-self {
  align-self: flex-end;
  flex-direction: row-reverse;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-avatar {
  flex-shrink: 0;
}

.message-content-area {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
  color: #909399;
}

.message-content {
  background: #ffffff;
  padding: 14px;
  border-radius: 12px;
  color: #2c3e50;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
  border-radius: 18px 18px 18px 4px;
  font-size: 14px;
  line-height: 1.6;
}

.message-self .message-content {
  background: #409EFF;
  color: #ffffff;
  border-radius: 18px 18px 4px 18px;
}

.chat-input {
  padding: 20px;
  border-top: 1px solid #e4e7ed;
  background: #ffffff;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.toolbar-left, .toolbar-right {
  display: flex;
  gap: 12px;
}

.toolbar-left .el-button, .toolbar-right .el-button {
  font-size: 20px;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
  color: #606266;
}

.toolbar-left .el-button:hover, .toolbar-right .el-button:hover {
  background-color: #f0f2f5;
  transform: scale(1.1);
}

.chat-input .el-textarea__inner {
  border-radius: 12px;
  padding: 14px;
  font-size: 14px;
  line-height: 1.6;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
  resize: none;
  background: #f5f7fa;
}

.chat-input .el-textarea__inner:focus {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding: 0 4px;
}

.tip {
  color: #909399;
  font-size: 13px;
}

.input-actions .el-button--primary {
  padding: 12px 28px;
  font-size: 15px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.input-actions .el-button--primary:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.2);
}

.system-message {
  justify-content: center;
  margin: 12px 0;
  max-width: 100%;
  padding: 0 20px;
}

.system-content {
  background: rgba(0, 0, 0, 0.7) !important;
  color: #ffffff !important;
  font-size: 13px;
  padding: 8px 20px;
  border-radius: 20px;
  text-align: center;
  max-width: fit-content;
  margin: 0 auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.emoji-picker {
   position: absolute;
   bottom: 55px;
   right: 0;
   background: #fff;
   border: 1px solid #ebeef5;
   border-radius: 8px;
   padding: 10px;
   box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
   display: flex;
   z-index: 10;
   width: 400px; /* 增加宽度 */
   height: 300px; /* 增加高度 */
}

.emoji-categories {
  width: 60px; /* 增加分类宽度 */
  border-right: 1px solid #ebeef5;
  padding-right: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
}

.emoji-category-label {
  cursor: pointer;
  font-size: 24px; /* 增加图标大小 */
  text-align: center;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.emoji-category-label:hover {
  background-color: #f0f2f5;
}

.emoji-category-label.active {
  background-color: #e0e0e0;
  font-weight: bold;
}

.emoji-list-container {
  flex-grow: 1;
  padding-left: 12px;
  overflow-y: auto;
}

.emoji-category h4 {
  margin: 0 0 12px 0;
  color: #2c3e50;
  font-size: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.emoji-item {
   cursor: pointer;
   font-size: 24px; /* 增加表情大小 */
   padding: 8px;
   transition: transform 0.2s ease;
   text-align: center;
}

.emoji-item:hover {
   transform: scale(1.2);
   background-color: #f0f2f5;
   border-radius: 4px;
}

.room-info-content {
  padding: 20px;
}

.room-info-content h3 {
  margin: 0 0 12px 0;
  color: #2c3e50;
}

.description {
  color: #606266;
  margin-bottom: 24px;
  line-height: 1.6;
}

.info-section {
  margin-top: 24px;
}

.info-section h4 {
  margin: 0 0 12px 0;
  color: #2c3e50;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .chat-container {
    top: 50px;
  }

  .chat-container::before {
    height: 50px;
  }

  .chat-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .room-stats {
    flex-wrap: wrap;
  }

  .room-actions {
    width: 100%;
    justify-content: space-between;
  }

  .message {
    max-width: 90%;
  }
}

/* 密码输入对话框样式 */
.password-dialog :deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.password-dialog :deep(.el-dialog__header) {
  margin: 0;
  padding: 20px;
  background: linear-gradient(135deg, #7c3aed, #b5a0f4);
  color: #ffffff;
}

.password-dialog :deep(.el-dialog__title) {
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
}

.password-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #ffffff;
}

.password-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.room-preview {
  padding: 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
}

.room-preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.room-preview-header h3 {
  margin: 0;
  font-size: 16px;
  color: #2c3e50;
  font-weight: 600;
}

.room-stats {
  display: flex;
  gap: 16px;
}

.room-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.room-stats .el-icon {
  color: #7c3aed;
}

.password-form {
  padding: 20px;
}

.password-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  border-radius: 8px;
  padding: 0 12px;
  transition: all 0.3s ease;
}

.password-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #7c3aed inset;
}

.password-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #7c3aed inset;
}

.password-form :deep(.el-input__prefix) {
  color: #7c3aed;
}

.dialog-footer {
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #e4e7ed;
}

.dialog-footer .el-button {
  padding: 10px 20px;
  font-size: 14px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.dialog-footer .el-button:not(.el-button--primary) {
  color: #000000;
  border-color: #dcdfe6;
}

.dialog-footer .el-button:not(.el-button--primary):hover {
  color: #000000;
  border-color: #c6c8cc;
  background-color: #f5f7fa;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #7c3aed, #b5a0f4);
  border: none;
  display: flex;
  align-items: center;
  gap: 6px;
}
</style> 