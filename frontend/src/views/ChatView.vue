<template>
  <div class="chat-layout">
    <!-- 左侧功能栏 -->
    <aside class="sidebar">
      <h2 class="sidebar-title">校园功能</h2>

      <!-- 新对话 -->
      <div
        class="new-chat-btn"
        @click="startNewConversation"
      >
        ＋ 新对话
      </div>

      <!-- 历史记录 -->
      <div class="history-title">
        历史记录
      </div>

      <div class="history-list">
        <div
          v-if="conversationLoading"
          class="history-empty"
        >
          加载中...
        </div>

        <div
          v-else-if="conversationList.length === 0"
          class="history-empty"
        >
          暂无历史对话
        </div>

        <template v-else>
          <div
            v-for="item in conversationList"
            :key="item.id"
            class="history-item"
            :class="{ active: currentConversationId === item.id }"
            @click="loadConversationMessages(item)"
          >
            <span class="history-name">
              {{ item.title || '新对话' }}
            </span>

            <div class="history-actions">
              <button
                class="history-action-btn"
                title="重命名"
                @click.stop="renameConversationItem(item)"
              >
                ✎
              </button>

              <button
                class="history-action-btn delete"
                title="删除"
                @click.stop="deleteConversationItem(item.id)"
              >
                ×
              </button>
            </div>
          </div>
        </template>
      </div>

      <!-- 功能菜单 -->
      <div class="menu-bottom">
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
          class="menu-item"
          @click="openFeedback"
        >
          💡 提交想法
        </div>

        <div
          class="menu-item"
          @click="openMyFeedback"
        >
          📨 我的反馈
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
      </div>
    </aside>

    <!-- 右侧聊天区域 -->
    <main class="chat-main">
      <header class="chat-header">
        {{ currentTitle }}
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
          <!-- AI 回复：Markdown 渲染 -->
          <div
            v-if="msg.role === 'assistant'"
            class="message-bubble markdown-body"
            v-html="renderMarkdown(msg.content)"
          ></div>

          <!-- 用户消息：普通文本 -->
          <div
            v-else
            class="message-bubble"
          >
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

    <!-- 提交想法弹窗 -->
    <div
      v-if="feedbackVisible"
      class="feedback-mask"
    >
      <div class="feedback-dialog">
        <h3>提交想法</h3>

        <p class="feedback-tip">
          你可以提交建议、问题反馈、希望新增的校园功能，管理员会在后台查看并回复。
        </p>

        <textarea
          v-model="feedbackContent"
          class="feedback-textarea"
          placeholder="请输入你的想法，例如：希望增加校园地图功能"
        ></textarea>

        <div class="feedback-actions">
          <button
            class="cancel-btn"
            @click="closeFeedback"
          >
            取消
          </button>

          <button
            class="submit-btn"
            :disabled="feedbackLoading"
            @click="submitIdea"
          >
            {{ feedbackLoading ? '提交中...' : '提交' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 我的反馈弹窗 -->
    <div
      v-if="myFeedbackVisible"
      class="feedback-mask"
    >
      <div class="my-feedback-dialog">
        <h3>我的反馈</h3>

        <div class="my-feedback-toolbar">
          <button
            class="refresh-small-btn"
            @click="loadMyFeedback"
          >
            刷新
          </button>
        </div>

        <div
          v-if="myFeedbackLoading"
          class="my-feedback-empty"
        >
          加载中...
        </div>

        <div
          v-else-if="myFeedbackList.length === 0"
          class="my-feedback-empty"
        >
          暂无反馈记录
        </div>

        <div
          v-else
          class="my-feedback-list"
        >
          <div
            v-for="item in myFeedbackList"
            :key="item.id"
            class="my-feedback-item"
          >
            <div class="my-feedback-row">
              <strong>我的反馈：</strong>
              <span>{{ item.content }}</span>
            </div>

            <div class="my-feedback-row">
              <strong>状态：</strong>
              <span
                class="feedback-status"
                :class="item.status === 'REPLIED' ? 'replied' : 'pending'"
              >
                {{ item.status === 'REPLIED' ? '管理员已回复' : '等待管理员回复' }}
              </span>
            </div>

            <div class="my-feedback-row">
              <strong>提交时间：</strong>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>

            <div class="my-feedback-row">
              <strong>管理员回复：</strong>
              <span>{{ item.reply || '暂无回复' }}</span>
            </div>

            <div
              v-if="item.replyTime"
              class="my-feedback-row"
            >
              <strong>回复时间：</strong>
              <span>{{ formatTime(item.replyTime) }}</span>
            </div>
          </div>
        </div>

        <div class="feedback-actions">
          <button
            class="cancel-btn"
            @click="closeMyFeedback"
          >
            关闭
          </button>
        </div>
      </div>
    </div>

    <!-- 重命名历史对话弹窗 -->
    <div
      v-if="renameDialogVisible"
      class="feedback-mask"
    >
      <div class="rename-dialog">
        <h3>重命名对话</h3>

        <input
          v-model="renameTitle"
          class="rename-input"
          placeholder="请输入新的对话名称"
          @keyup.enter="submitRenameConversation"
        />

        <div class="feedback-actions">
          <button
            class="cancel-btn"
            @click="closeRenameDialog"
          >
            取消
          </button>

          <button
            class="submit-btn"
            @click="submitRenameConversation"
          >
            确认
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import MarkdownIt from 'markdown-it'
import { useRouter } from 'vue-router'
import { chatWithAi } from '../api/ai'
import { submitFeedback, getMyFeedback } from '../api/feedback'
import {
  createConversation,
  getConversationList,
  getMessageList,
  saveMessage,
  updateConversationTitle,
  deleteConversation
} from '../api/memory'

const router = useRouter()

const role = sessionStorage.getItem('role')
const username = sessionStorage.getItem('username') || ''

const question = ref('')
const loading = ref(false)
const messageListRef = ref(null)

const currentConversationId = ref(null)
const currentTitle = ref('新对话')
const conversationList = ref([])
const conversationLoading = ref(false)

// ====================== Markdown 渲染 ======================
const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true
})

const renderMarkdown = (content) => {
  return md.render(content || '')
}

// ====================== 重命名弹窗 ======================
const renameDialogVisible = ref(false)
const renameTitle = ref('')
const renameTarget = ref(null)

const getDefaultMessages = () => [
  {
    role: 'assistant',
    content: '你好！我是校园助手，你可以问我食堂、宿舍、校历、图书馆、办事流程等校园问题。'
  }
]

const messages = ref(getDefaultMessages())

// ====================== 聊天记忆 ======================

const loadConversationList = async (autoLoadLatest = false) => {
  if (!username) {
    return
  }

  conversationLoading.value = true

  try {
    const res = await getConversationList(username)

    if (res.data.code === '200' || res.data.code === 200) {
      conversationList.value = res.data.data || []

      if (autoLoadLatest && conversationList.value.length > 0) {
        await loadConversationMessages(conversationList.value[0])
      }
    } else {
      alert(res.data.msg || '获取历史对话失败')
    }
  } catch (error) {
    console.error('获取历史对话失败：', error)
    alert('获取历史对话失败，请检查后端 /memory 接口')
  } finally {
    conversationLoading.value = false
  }
}

const startNewConversation = () => {
  currentConversationId.value = null
  currentTitle.value = '新对话'
  question.value = ''
  messages.value = getDefaultMessages()
}

const createConversationByQuestion = async (firstQuestion) => {
  const title = firstQuestion.length > 20
    ? firstQuestion.slice(0, 20) + '...'
    : firstQuestion

  const res = await createConversation(username, title)

  if (res.data.code === '200' || res.data.code === 200) {
    currentConversationId.value = res.data.data.id
    currentTitle.value = title
    await loadConversationList(false)
  } else {
    throw new Error(res.data.msg || '创建对话失败')
  }
}

const loadConversationMessages = async (conversation) => {
  if (!conversation || !conversation.id) {
    return
  }

  currentConversationId.value = conversation.id
  currentTitle.value = conversation.title || '新对话'

  try {
    const res = await getMessageList(conversation.id, username)

    if (res.data.code === '200' || res.data.code === 200) {
      const list = res.data.data || []

      if (list.length === 0) {
        messages.value = getDefaultMessages()
      } else {
        messages.value = list.map(item => ({
          role: item.role,
          content: item.content
        }))
      }

      await scrollToBottom()
    } else {
      alert(res.data.msg || '加载聊天记录失败')
    }
  } catch (error) {
    console.error('加载聊天记录失败：', error)
    alert('加载聊天记录失败，请检查后端接口')
  }
}

const saveChatMessage = async (roleValue, contentValue) => {
  if (!currentConversationId.value || !username) {
    return
  }

  await saveMessage({
    conversationId: currentConversationId.value,
    username,
    role: roleValue,
    content: contentValue
  })
}

const renameConversationItem = (item) => {
  renameTarget.value = item
  renameTitle.value = item.title || '新对话'
  renameDialogVisible.value = true
}

const closeRenameDialog = () => {
  renameDialogVisible.value = false
  renameTarget.value = null
  renameTitle.value = ''
}

const submitRenameConversation = async () => {
  if (!renameTarget.value) {
    alert('未选择对话')
    return
  }

  const title = renameTitle.value.trim()

  if (!title) {
    alert('对话名称不能为空')
    return
  }

  try {
    const res = await updateConversationTitle(
      renameTarget.value.id,
      username,
      title
    )

    if (res.data.code === '200' || res.data.code === 200) {
      if (currentConversationId.value === renameTarget.value.id) {
        currentTitle.value = title
      }

      closeRenameDialog()
      await loadConversationList(false)
    } else {
      alert(res.data.msg || '重命名失败')
    }
  } catch (error) {
    console.error('重命名历史对话失败：', error)
    alert('重命名失败，请检查后端接口')
  }
}

const deleteConversationItem = async (id) => {
  const ok = window.confirm('确定删除这条历史对话吗？')

  if (!ok) {
    return
  }

  try {
    const res = await deleteConversation(id, username)

    if (res.data.code === '200' || res.data.code === 200) {
      if (currentConversationId.value === id) {
        startNewConversation()
      }

      await loadConversationList(false)
    } else {
      alert(res.data.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除历史对话失败：', error)
    alert('删除失败，请检查后端接口')
  }
}

// ====================== AI 聊天 ======================

const scrollToBottom = async () => {
  await nextTick()

  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const sendMessage = async () => {
  const text = question.value.trim()

  if (!text || loading.value) {
    return
  }

  if (!username) {
    alert('未获取到用户名，请重新登录')
    return
  }

  loading.value = true

  let assistantIndex = -1

  try {
    if (!currentConversationId.value) {
      await createConversationByQuestion(text)
    }

    const historyContext = messages.value
      .filter(item => item.content !== '正在思考中...')
      .slice(-8)
      .map(item => {
        if (item.role === 'user') {
          return '用户：' + item.content
        }

        return '助手：' + item.content
      })
      .join('\n')

    const questionWithContext = historyContext
      ? `以下是当前对话的历史上下文：\n${historyContext}\n\n用户现在的问题是：\n${text}\n\n请结合上下文回答用户现在的问题。`
      : text

    messages.value.push({
      role: 'user',
      content: text
    })

    question.value = ''

    messages.value.push({
      role: 'assistant',
      content: '正在思考中...'
    })

    assistantIndex = messages.value.length - 1

    await scrollToBottom()

    try {
      await saveChatMessage('user', text)
    } catch (error) {
      console.error('保存用户消息失败：', error)
    }

    const res = await chatWithAi(questionWithContext)
    const answer = res.data.answer || 'AI 没有返回答案'

    messages.value[assistantIndex] = {
      role: 'assistant',
      content: answer
    }

    try {
      await saveChatMessage('assistant', answer)
    } catch (error) {
      console.error('保存AI回复失败：', error)
    }

    await loadConversationList(false)
  } catch (error) {
    console.error('AI接口请求失败：', error)

    const errorMsg = '请求失败，请检查后端是否启动、/ai/chat 是否加入白名单、Ollama 是否正在运行。'

    if (assistantIndex !== -1) {
      messages.value[assistantIndex] = {
        role: 'assistant',
        content: errorMsg
      }

      try {
        await saveChatMessage('assistant', errorMsg)
      } catch (saveError) {
        console.error('保存错误消息失败：', saveError)
      }
    } else {
      alert(error.message || '发送失败')
    }
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

// ====================== 提交想法弹窗 ======================
const feedbackVisible = ref(false)
const feedbackContent = ref('')
const feedbackLoading = ref(false)

const openFeedback = () => {
  feedbackVisible.value = true
}

const closeFeedback = () => {
  feedbackVisible.value = false
}

const submitIdea = async () => {
  const content = feedbackContent.value.trim()

  if (!content) {
    alert('请输入反馈内容')
    return
  }

  feedbackLoading.value = true

  try {
    const res = await submitFeedback({
      username: username || '匿名用户',
      content
    })

    if (res.data.code === '200' || res.data.code === 200) {
      alert('提交成功，管理员会尽快处理')
      feedbackContent.value = ''
      feedbackVisible.value = false
    } else {
      alert(res.data.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交反馈失败：', error)
    alert('提交失败，请检查后端是否启动，或 /feedback/submit 是否加入白名单')
  } finally {
    feedbackLoading.value = false
  }
}

// ====================== 我的反馈弹窗 ======================
const myFeedbackVisible = ref(false)
const myFeedbackList = ref([])
const myFeedbackLoading = ref(false)

const openMyFeedback = async () => {
  myFeedbackVisible.value = true
  await loadMyFeedback()
}

const closeMyFeedback = () => {
  myFeedbackVisible.value = false
}

const loadMyFeedback = async () => {
  myFeedbackLoading.value = true

  try {
    if (!username) {
      alert('未获取到用户名，请重新登录')
      return
    }

    const res = await getMyFeedback(username)

    if (res.data.code === '200' || res.data.code === 200) {
      myFeedbackList.value = res.data.data || []
    } else {
      alert(res.data.msg || '获取反馈失败')
    }
  } catch (error) {
    console.error('获取我的反馈失败：', error)
    alert('获取我的反馈失败，请检查登录状态或后端接口')
  } finally {
    myFeedbackLoading.value = false
  }
}

// ====================== 页面跳转 ======================

const goChat = () => {
  router.push('/chat')
}

const goHome = () => {
  router.push('/home')
}

const goAdmin = () => {
  router.push('/admin').catch(() => {
    window.location.href = '/admin'
  })
}

const logout = () => {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('role')
  sessionStorage.removeItem('username')

  router.push('/login')
}

const formatTime = (time) => {
  if (!time) {
    return '-'
  }

  return String(time).replace('T', ' ').slice(0, 19)
}

onMounted(() => {
  loadConversationList(true)
})
</script>

<style scoped>
.chat-layout {
  height: 100vh;
  display: flex;
  background: #f7f7f8;
}

/* 左侧栏 */
.sidebar {
  width: 260px;
  background: #f3f6fa;
  border-right: 1px solid #e5e7eb;
  padding: 20px 12px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  text-align: center;
  margin: 0 0 18px;
  font-size: 22px;
  color: #111827;
}

.new-chat-btn {
  height: 42px;
  border-radius: 8px;
  border: 1px solid #409eff;
  background: #e8f2ff;
  color: #1677ff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-bottom: 16px;
  font-size: 15px;
}

.new-chat-btn:hover {
  background: #d9ebff;
}

.history-title {
  font-size: 14px;
  color: #6b7280;
  margin: 0 6px 8px;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
  margin-bottom: 12px;
}

.history-empty {
  color: #9ca3af;
  font-size: 14px;
  padding: 10px 8px;
}

.history-item {
  height: 40px;
  border-radius: 8px;
  padding: 0 8px 0 12px;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  color: #374151;
  font-size: 14px;
}

.history-item:hover {
  background: #e8f2ff;
}

.history-item.active {
  background: #e8f2ff;
  color: #1677ff;
}

.history-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.history-actions {
  display: none;
  align-items: center;
  gap: 4px;
  margin-left: 6px;
}

.history-item:hover .history-actions {
  display: flex;
}

.history-action-btn {
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #9ca3af;
  cursor: pointer;
  font-size: 14px;
  line-height: 22px;
  padding: 0;
}

.history-action-btn:hover {
  background: #dbeafe;
  color: #1677ff;
}

.history-action-btn.delete:hover {
  color: #ef4444;
}

.menu-bottom {
  border-top: 1px solid #e5e7eb;
  padding-top: 12px;
}

.menu-item {
  padding: 12px 16px;
  margin-bottom: 10px;
  border-radius: 8px;
  cursor: pointer;
  color: #374151;
  font-size: 15px;
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
  font-size: 23px;
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

/* 通用弹窗遮罩 */
.feedback-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

/* 提交想法弹窗 */
.feedback-dialog {
  width: 460px;
  background: #ffffff;
  border-radius: 14px;
  padding: 24px;
  box-sizing: border-box;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}

.feedback-dialog h3 {
  margin: 0 0 10px;
  font-size: 20px;
  color: #111827;
}

.feedback-tip {
  margin: 0 0 14px;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.6;
}

.feedback-textarea {
  width: 100%;
  height: 150px;
  resize: none;
  border: 1px solid #dcdfe6;
  border-radius: 10px;
  padding: 12px;
  box-sizing: border-box;
  font-size: 15px;
  line-height: 1.6;
  outline: none;
}

.feedback-textarea:focus {
  border-color: #409eff;
}

.feedback-actions {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn,
.submit-btn {
  min-width: 82px;
  height: 38px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-size: 14px;
}

.cancel-btn {
  background: #f3f4f6;
  color: #333;
}

.cancel-btn:hover {
  background: #e5e7eb;
}

.submit-btn {
  background: #1677ff;
  color: #fff;
}

.submit-btn:hover {
  background: #0f66d8;
}

.submit-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

/* 我的反馈弹窗 */
.my-feedback-dialog {
  width: 620px;
  max-height: 75vh;
  overflow-y: auto;
  background: #ffffff;
  border-radius: 14px;
  padding: 24px;
  box-sizing: border-box;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}

.my-feedback-dialog h3 {
  margin: 0 0 14px;
  font-size: 20px;
  color: #111827;
}

.my-feedback-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.refresh-small-btn {
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 8px;
  background: #1677ff;
  color: #fff;
  cursor: pointer;
}

.refresh-small-btn:hover {
  background: #0f66d8;
}

.my-feedback-empty {
  padding: 30px 0;
  text-align: center;
  color: #6b7280;
}

.my-feedback-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.my-feedback-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px;
  background: #f9fafb;
}

.my-feedback-row {
  margin-bottom: 8px;
  line-height: 1.7;
  color: #374151;
  word-break: break-word;
}

.my-feedback-row strong {
  color: #111827;
}

.feedback-status {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 13px;
}

.feedback-status.pending {
  background: #fff7ed;
  color: #ea580c;
}

.feedback-status.replied {
  background: #ecfdf5;
  color: #059669;
}

/* 重命名弹窗 */
.rename-dialog {
  width: 420px;
  background: #ffffff;
  border-radius: 14px;
  padding: 24px;
  box-sizing: border-box;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}

.rename-dialog h3 {
  margin: 0 0 16px;
  font-size: 20px;
  color: #111827;
}

.rename-input {
  width: 100%;
  height: 42px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 0 12px;
  box-sizing: border-box;
  font-size: 15px;
  outline: none;
}

.rename-input:focus {
  border-color: #409eff;
}

/* Markdown 渲染样式 */
.message-row.assistant .markdown-body {
  white-space: normal;
}

.markdown-body :deep(p) {
  margin: 0 0 10px;
  line-height: 1.8;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(strong) {
  font-weight: 700;
  color: #111827;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 8px 0 12px 22px;
  padding-left: 18px;
}

.markdown-body :deep(li) {
  margin-bottom: 6px;
  line-height: 1.8;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin: 14px 0 10px;
  font-weight: 700;
  color: #111827;
  line-height: 1.5;
}

.markdown-body :deep(h1) {
  font-size: 24px;
}

.markdown-body :deep(h2) {
  font-size: 22px;
}

.markdown-body :deep(h3) {
  font-size: 20px;
}

.markdown-body :deep(h4) {
  font-size: 18px;
}

.markdown-body :deep(code) {
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 5px;
  font-size: 14px;
  color: #dc2626;
}

.markdown-body :deep(pre) {
  background: #f3f4f6;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 10px 0;
}

.markdown-body :deep(pre code) {
  background: transparent;
  padding: 0;
  color: #111827;
}

.markdown-body :deep(blockquote) {
  margin: 10px 0;
  padding: 8px 12px;
  border-left: 4px solid #d1d5db;
  background: #f9fafb;
  color: #4b5563;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid #e5e7eb;
  margin: 16px 0;
}
</style>

