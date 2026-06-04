<template>
  <div class="chat-layout">
    <!-- 左侧功能栏 -->
    <aside class="sidebar">
      <h2 class="sidebar-title">校园功能</h2>

      <div
        class="menu-item active"
        @click="goChat"
      >
        💬 智能问答
      </div>

      <div
        class="menu-item"
        @click="goHome"
      >
        🍚 食堂餐饮
      </div>

      <div
        class="menu-item"
        @click="goHome"
      >
        📚 图书馆服务
      </div>

      <div
        v-if="role === 'ADMIN'"
        class="menu-item"
        @click="goAdmin"
      >
        🛠 管理端
      </div>

      <div
        class="menu-item logout"
        @click="logout"
      >
        🚪 退出登录
      </div>
    </aside>

    <!-- 右侧聊天区域 -->
    <main class="chat-main">
      <header class="chat-header">
        校园智能助手
      </header>

      <section
        ref="messageListRef"
        class="message-list"
      >
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="message-row"
          :class="msg.role"
        >
          <div class="message-bubble">
            {{ msg.content }}
          </div>
        </div>
      </section>

      <footer class="input-area">
        <input
          v-model="question"
          class="input-box"
          placeholder="输入问题..."
          @keyup.enter="sendMessage"
        />

        <button
          class="send-btn"
          :disabled="loading"
          @click="sendMessage"
        >
          {{ loading ? '思考中...' : '发送' }}
        </button>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { chatWithAi } from '../api/ai'

// 路由对象
const router = useRouter()

// 当前登录用户角色
const role = sessionStorage.getItem('role')

// 用户输入的问题
const question = ref('')

// 是否正在等待 AI 返回
const loading = ref(false)

// 聊天记录区域
const messageListRef = ref(null)

// 页面初始化消息
const messages = ref([
  {
    role: 'assistant',
    content: '你好！我是校园助手，你可以问我食堂、宿舍、校历、图书馆、办事流程等校园问题。'
  }
])

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()

  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// 发送消息
const sendMessage = async () => {
  const text = question.value.trim()

  if (!text || loading.value) {
    return
  }

  // 1. 先把用户问题显示到页面
  messages.value.push({
    role: 'user',
    content: text
  })

  // 2. 清空输入框
  question.value = ''

  // 3. 显示 AI 正在思考
  messages.value.push({
    role: 'assistant',
    content: '正在思考中...'
  })

  loading.value = true
  await scrollToBottom()

  try {
    // 4. 调用后端 /ai/chat 接口
    const res = await chatWithAi(text)

    // 5. 把最后一条“正在思考中...”替换成 AI 回答
    messages.value[messages.value.length - 1] = {
      role: 'assistant',
      content: res.data.answer || 'AI 没有返回答案'
    }
  } catch (error) {
    console.error('AI接口请求失败：', error)

    messages.value[messages.value.length - 1] = {
      role: 'assistant',
      content: '请求失败，请检查后端是否启动、/ai/chat 是否加入白名单、Ollama 是否正在运行。'
    }
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

// 跳转聊天页
const goChat = () => {
  router.push('/chat')
}

// 跳转原来的知识库首页
const goHome = () => {
  router.push('/home')
}

// 跳转管理端
const goAdmin = () => {
  router.push('/admin')
}

// 退出登录
const logout = () => {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('role')
  sessionStorage.removeItem('username')

  router.push('/login')
}
</script>

<style scoped>
.chat-layout {
  height: 100vh;
  display: flex;
  background: #f7f7f8;
}

/* 左侧栏 */
.sidebar {
  width: 240px;
  background: #f3f6fa;
  border-right: 1px solid #e5e7eb;
  padding: 24px 14px;
  box-sizing: border-box;
}

.sidebar-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 22px;
  color: #111827;
}

.menu-item {
  padding: 14px 18px;
  margin-bottom: 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #374151;
  font-size: 16px;
}

.menu-item:hover {
  background: #e8f2ff;
  color: #1677ff;
}

.menu-item.active {
  background: #e8f2ff;
  color: #1677ff;
  border: 1px solid #409eff;
}

.logout {
  margin-top: 24px;
  color: #888;
}

/* 右侧聊天区 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.chat-header {
  height: 72px;
  border-bottom: 1px solid #eeeeee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: #111827;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 28px 28px 120px;
  box-sizing: border-box;
  background: #fafafa;
}

.message-row {
  display: flex;
  margin-bottom: 20px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 70%;
  padding: 14px 18px;
  border-radius: 12px;
  line-height: 1.8;
  font-size: 16px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-row.assistant .message-bubble {
  background: #ffffff;
  color: #111827;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.message-row.user .message-bubble {
  background: #1e90ff;
  color: #ffffff;
}

/* 输入区 */
.input-area {
  height: 86px;
  border-top: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 16px 28px;
  display: flex;
  gap: 14px;
  box-sizing: border-box;
}

.input-box {
  flex: 1;
  height: 50px;
  border: 2px solid #f0a000;
  border-radius: 6px;
  padding: 0 14px;
  font-size: 16px;
  outline: none;
}

.input-box:focus {
  border-color: #409eff;
}

.send-btn {
  width: 90px;
  height: 50px;
  border: none;
  border-radius: 6px;
  background: #1e90ff;
  color: #ffffff;
  font-size: 16px;
  cursor: pointer;
}

.send-btn:hover {
  background: #1677d2;
}

.send-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}
</style>
