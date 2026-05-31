<template>
  <div style="display: flex; height: 100vh; margin: 0; padding: 0;">
    <!-- 左侧侧边栏：带悬停边框 + 点击切换 -->
    <aside style="width: 200px; background: #f5f7fa; border-right: 1px solid #eee;">
      <div style="text-align: center; font-size: 18px; font-weight: bold; padding: 20px 0;">
        校园功能
      </div>

      <!-- 菜单列表 -->
      <div class="sidebar-menu">
        <div
          class="menu-item"
          :class="{ active: currentMenu === 'chat' }"
          @click="switchMenu('chat')"
        >
          💬 智能问答
        </div>
        <div
          class="menu-item"
          :class="{ active: currentMenu === 'canteen' }"
          @click="switchMenu('canteen')"
        >
          🍚 食堂餐饮
        </div>
        <div
          class="menu-item"
          :class="{ active: currentMenu === 'library' }"
          @click="switchMenu('library')"
        >
          📚 图书馆服务
        </div>

        <!-- 管理员入口，仅管理员可见 -->
        <div
          v-if="isAdmin"
          class="menu-item admin-item"
          @click="goToAdmin"
        >
          🔐 管理后台
        </div>

        <!-- 退出登录 -->
        <div class="menu-item logout-item" @click="logout">
          🚪 退出登录
        </div>
      </div>
    </aside>

    <!-- 右侧主内容区：根据点击切换 -->
    <main style="flex: 1; display: flex; flex-direction: column;">
      <!-- 智能问答页 -->
      <div v-if="currentMenu === 'chat'" class="chat-box">
        <div style="text-align: center; font-size: 20px; font-weight: bold; padding: 20px; border-bottom: 1px solid #eee;">
          校园智能助手
        </div>
        <div class="chat-messages" ref="chatBox">
          <div style="background: #fff; padding: 10px 15px; border-radius: 10px; max-width: 70%;">
            你好！我是校园助手
          </div>
          <div
            v-for="(msg, index) in messages"
            :key="index"
            style="padding: 10px 15px; border-radius: 10px; max-width: 70%; margin-bottom: 15px;"
            :style="{
              background: msg.type === 'user' ? '#1890ff' : '#fff',
              color: msg.type === 'user' ? '#fff' : '#333',
              marginLeft: msg.type === 'user' ? 'auto' : '0'
            }"
          >
            {{ msg.content }}
          </div>
        </div>
        <div style="display: flex; padding: 20px; border-top: 1px solid #eee; background: #fff;">
          <input
            v-model="inputText"
            @keyup.enter="sendMessage"
            style="flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 5px; margin-right: 10px;"
            placeholder="输入问题..."
          />
          <button
            @click="sendMessage"
            style="padding: 10px 20px; background: #1890ff; color: #fff; border: none; border-radius: 5px; cursor: pointer;"
          >
            发送
          </button>
        </div>
      </div>

      <!-- 食堂餐饮页（空页面，后续可扩展） -->
      <div v-else-if="currentMenu === 'canteen'" class="empty-page">
        <h2>🍚 食堂餐饮</h2>
        <p>功能开发中，敬请期待~</p>
      </div>

      <!-- 图书馆服务页（空页面，后续可扩展） -->
      <div v-else-if="currentMenu === 'library'" class="empty-page">
        <h2>📚 图书馆服务</h2>
        <p>功能开发中，敬请期待~</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const chatBox = ref(null)
const inputText = ref('')
const messages = ref([])

// 当前选中的菜单
const currentMenu = ref('chat')
// 角色判断
const role = ref(sessionStorage.getItem('role'))
const isAdmin = computed(() => role.value === 'ADMIN')

// 切换菜单
const switchMenu = (menu) => {
  currentMenu.value = menu
}

// 跳转到管理后台
const goToAdmin = () => {
  router.push('/admin')
}

// 退出登录
const logout = () => {
  sessionStorage.clear()
  router.push('/login')
}

// 发送消息
const sendMessage = () => {
  if (!inputText.value.trim()) return
  messages.value.push({ type: 'user', content: inputText.value })

  // 模拟回复（可以替换成你的真实接口）
  setTimeout(() => {
    messages.value.push({
      type: 'bot',
      content: inputText.value === '时间' ? '校园开放时间：周一至周日 6:00-22:00' : '抱歉，知识库暂无相关答案。'
    })
    nextTick(() => {
      chatBox.value.scrollTop = chatBox.value.scrollHeight
    })
  }, 500)

  inputText.value = ''
}
</script>

<style scoped>
/* 菜单通用样式 */
.sidebar-menu {
  padding: 10px 0;
}
.menu-item {
  padding: 12px 20px;
  cursor: pointer;
  margin: 4px 10px;
  border-radius: 6px;
  transition: all 0.2s;
  color: #555;
}
/* 悬停边框高亮 */
.menu-item:hover {
  border: 1px solid #1890ff;
  background-color: #e6f7ff;
  color: #1890ff;
}
/* 选中状态样式 */
.menu-item.active {
  border: 1px solid #1890ff;
  background-color: #e6f7ff;
  color: #1890ff;
}
/* 管理员入口样式 */
.admin-item {
  color: #ff4d4d;
  border-top: 1px solid #eee;
  margin-top: 20px;
}
.admin-item:hover {
  border-color: #ff4d4d;
  background-color: #fff1f0;
  color: #ff4d4d;
}
/* 退出登录样式 */
.logout-item {
  color: #999;
}
.logout-item:hover {
  border-color: #999;
  background-color: #f5f5f5;
  color: #333;
}

/* 聊天页样式 */
.chat-box {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #fafafa;
}

/* 空页面样式 */
.empty-page {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
}
</style>
