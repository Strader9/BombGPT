<template>
  <div class="app-root">
    <!-- 首次进入网页的 Logo 加载动画 -->
    <div
      v-if="showSplash"
      class="splash-screen"
    >
      <div class="splash-logo-wrap">
        <img
          src="/bbg-logo.png"
          alt="BBG Logo"
          class="splash-logo"
        />

        <div class="splash-loading">
          <span></span>
        </div>

        <div class="splash-text">
          校园生活百事通正在启动...
        </div>
      </div>
    </div>

    <!-- 正常页面 -->
    <router-view v-show="!showSplash" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'

const showSplash = ref(true)

onMounted(() => {
  // 每次刷新页面都会播放一次。如果你想一个浏览器会话只播放一次，我后面也可以帮你改。
  setTimeout(() => {
    showSplash.value = false
  }, 1600)
})
</script>

<style>
html,
body,
#app {
  margin: 0;
  padding: 0;
  min-height: 100%;
}

.app-root {
  min-height: 100vh;
}

/* ================= Logo 启动动画 ================= */
.splash-screen {
  position: fixed;
  inset: 0;
  z-index: 99999;
  background:
    radial-gradient(circle at center, rgba(255, 255, 255, 0.06), transparent 42%),
    #1f1f1d;
  display: flex;
  align-items: center;
  justify-content: center;
}

.splash-logo-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: splashFloat 1.6s ease both;
}

.splash-logo {
  width: 240px;
  max-width: 58vw;
  object-fit: contain;
  filter: drop-shadow(0 18px 36px rgba(0, 0, 0, 0.35));
  animation: logoPop 1.1s ease both;
}

.splash-loading {
  width: 220px;
  height: 5px;
  margin-top: 34px;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  overflow: hidden;
}

.splash-loading span {
  display: block;
  width: 100%;
  height: 100%;
  background: #fff4dc;
  border-radius: 999px;
  transform-origin: left center;
  animation: loadingBar 1.35s ease forwards;
}

.splash-text {
  margin-top: 16px;
  color: rgba(255, 244, 220, 0.86);
  font-size: 15px;
  letter-spacing: 1px;
}

@keyframes logoPop {
  0% {
    opacity: 0;
    transform: scale(0.82) translateY(18px);
  }

  60% {
    opacity: 1;
    transform: scale(1.04) translateY(0);
  }

  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes loadingBar {
  0% {
    transform: scaleX(0);
  }

  100% {
    transform: scaleX(1);
  }
}

@keyframes splashFloat {
  0% {
    opacity: 0;
  }

  20% {
    opacity: 1;
  }

  85% {
    opacity: 1;
  }

  100% {
    opacity: 0.98;
  }
}

/* =========================
   BBG Logo 深色统一主题
   主色取 logo 背景色，辅色取 logo 字体奶白色
========================= */

:root {
  --bbg-bg: #22211d;
  --bbg-bg-deep: #1b1a17;
  --bbg-panel: #2b2a25;
  --bbg-panel-light: #343229;
  --bbg-cream: #fff2d4;
  --bbg-cream-soft: #f4dfb5;
  --bbg-text: #fff7e6;
  --bbg-text-dark: #22211d;
  --bbg-muted: #c9bfa9;
  --bbg-border: rgba(255, 242, 212, 0.16);
  --bbg-shadow: rgba(0, 0, 0, 0.32);
  --bbg-danger: #c4574f;
}

/* 全局背景 */
html,
body,
#app {
  background: var(--bbg-bg) !important;
  color: var(--bbg-text) !important;
}

/* =========================
   登录 / 注册 / 忘记密码页
========================= */

.form-page {
  background:
    radial-gradient(circle at center, rgba(255, 242, 212, 0.05), transparent 42%),
    var(--bbg-bg) !important;
}

.card {
  background: var(--bbg-panel) !important;
  color: var(--bbg-text) !important;
  border: 1px solid var(--bbg-border) !important;
  box-shadow: 0 18px 48px var(--bbg-shadow) !important;
}

.login-logo-box {
  background: transparent !important;
}

.login-logo {
  filter: drop-shadow(0 10px 24px rgba(0, 0, 0, 0.36)) !important;
}

/* Element Plus 输入框 */
.el-input__wrapper {
  background: var(--bbg-bg-deep) !important;
  border: 1px solid var(--bbg-border) !important;
  box-shadow: none !important;
}

.el-input__wrapper.is-focus {
  border-color: var(--bbg-cream) !important;
  box-shadow: 0 0 0 2px rgba(255, 242, 212, 0.16) !important;
}

.el-input__inner {
  color: var(--bbg-text) !important;
}

.el-input__inner::placeholder {
  color: var(--bbg-muted) !important;
}

.el-input__clear,
.el-input__password {
  color: var(--bbg-muted) !important;
}

/* Element Plus 主按钮 */
.el-button--primary {
  background: var(--bbg-cream) !important;
  border-color: var(--bbg-cream) !important;
  color: var(--bbg-text-dark) !important;
  font-weight: 700 !important;
}

.el-button--primary:hover {
  background: var(--bbg-cream-soft) !important;
  border-color: var(--bbg-cream-soft) !important;
}

.el-link {
  color: var(--bbg-cream) !important;
}

.el-link:hover {
  color: var(--bbg-cream-soft) !important;
}

/* =========================
   聊天页整体
========================= */

.chat-layout {
  background: var(--bbg-bg) !important;
  color: var(--bbg-text) !important;
}

.sidebar {
  background: var(--bbg-bg-deep) !important;
  border-right: 1px solid var(--bbg-border) !important;
  color: var(--bbg-text) !important;
}

.sidebar-logo-box {
  background: transparent !important;
}

.sidebar-logo {
  filter: drop-shadow(0 10px 24px rgba(0, 0, 0, 0.35)) !important;
}

/* 新对话按钮 */
.new-chat-btn {
  background: transparent !important;
  color: var(--bbg-cream) !important;
  border: 1px solid var(--bbg-cream) !important;
}

.new-chat-btn:hover {
  background: rgba(255, 242, 212, 0.12) !important;
}

/* 历史记录 */
.history-title {
  color: var(--bbg-muted) !important;
}

.history-item {
  color: var(--bbg-text) !important;
  background: transparent !important;
}

.history-item:hover {
  background: rgba(255, 242, 212, 0.10) !important;
}

.history-item.active {
  background: rgba(255, 242, 212, 0.16) !important;
  color: var(--bbg-cream) !important;
}

.history-name {
  color: inherit !important;
}

.history-action-btn {
  background: rgba(255, 242, 212, 0.12) !important;
  color: var(--bbg-cream) !important;
}

.history-action-btn:hover {
  background: rgba(255, 242, 212, 0.22) !important;
}

/* 左侧功能菜单 */
.menu-item {
  color: var(--bbg-text) !important;
}

.menu-item:hover {
  background: rgba(255, 242, 212, 0.10) !important;
  color: var(--bbg-cream) !important;
}

.menu-item.active {
  background: rgba(255, 242, 212, 0.16) !important;
  color: var(--bbg-cream) !important;
  border: 1px solid rgba(255, 242, 212, 0.52) !important;
}

.logout {
  color: var(--bbg-muted) !important;
}

/* 聊天主区域 */
.chat-main {
  background: var(--bbg-bg) !important;
  color: var(--bbg-text) !important;
}

.chat-header {
  background: var(--bbg-bg) !important;
  border-bottom: 1px solid var(--bbg-border) !important;
  color: var(--bbg-cream) !important;
}

.message-list {
  background: var(--bbg-bg) !important;
}

/* AI 气泡 */
.message-row.assistant .message-bubble {
  background: var(--bbg-panel) !important;
  color: var(--bbg-text) !important;
  border: 1px solid var(--bbg-border) !important;
  box-shadow: 0 8px 26px rgba(0, 0, 0, 0.16) !important;
}

/* 用户气泡 */
.message-row.user .message-bubble {
  background: var(--bbg-cream) !important;
  color: var(--bbg-text-dark) !important;
  font-weight: 600 !important;
}

/* 输入区 */
.input-area {
  background: var(--bbg-bg-deep) !important;
  border-top: 1px solid var(--bbg-border) !important;
}

.input-box {
  background: var(--bbg-panel) !important;
  color: var(--bbg-text) !important;
  border: 2px solid rgba(255, 242, 212, 0.58) !important;
}

.input-box::placeholder {
  color: var(--bbg-muted) !important;
}

.input-box:focus {
  border-color: var(--bbg-cream) !important;
  box-shadow: 0 0 0 2px rgba(255, 242, 212, 0.14) !important;
}

.send-btn {
  background: var(--bbg-cream) !important;
  color: var(--bbg-text-dark) !important;
  font-weight: 700 !important;
}

.send-btn:hover {
  background: var(--bbg-cream-soft) !important;
}

.send-btn:disabled {
  background: #6f6a5d !important;
  color: #d8cfbb !important;
}

/* =========================
   反馈弹窗 / 我的反馈弹窗 / 重命名弹窗
========================= */

.feedback-mask,
.dialog-mask {
  background: rgba(0, 0, 0, 0.58) !important;
}

.feedback-dialog,
.my-feedback-dialog,
.rename-dialog,
.reply-dialog,
.knowledge-dialog {
  background: var(--bbg-panel) !important;
  color: var(--bbg-text) !important;
  border: 1px solid var(--bbg-border) !important;
  box-shadow: 0 18px 56px rgba(0, 0, 0, 0.38) !important;
}

.feedback-dialog h3,
.my-feedback-dialog h3,
.rename-dialog h3,
.reply-dialog h3,
.knowledge-dialog h3 {
  color: var(--bbg-cream) !important;
}

.feedback-tip,
.my-feedback-empty,
.history-empty,
.empty {
  color: var(--bbg-muted) !important;
}

.feedback-textarea,
.reply-textarea,
.form-textarea,
.rename-input,
.form-input {
  background: var(--bbg-bg-deep) !important;
  color: var(--bbg-text) !important;
  border: 1px solid var(--bbg-border) !important;
}

.feedback-textarea::placeholder,
.reply-textarea::placeholder,
.form-textarea::placeholder,
.rename-input::placeholder,
.form-input::placeholder {
  color: var(--bbg-muted) !important;
}

.feedback-textarea:focus,
.reply-textarea:focus,
.form-textarea:focus,
.rename-input:focus,
.form-input:focus {
  border-color: var(--bbg-cream) !important;
}

.cancel-btn {
  background: rgba(255, 242, 212, 0.10) !important;
  color: var(--bbg-text) !important;
}

.cancel-btn:hover {
  background: rgba(255, 242, 212, 0.18) !important;
}

.submit-btn,
.refresh-small-btn {
  background: var(--bbg-cream) !important;
  color: var(--bbg-text-dark) !important;
  font-weight: 700 !important;
}

.submit-btn:hover,
.refresh-small-btn:hover {
  background: var(--bbg-cream-soft) !important;
}

.my-feedback-item,
.original-feedback {
  background: var(--bbg-bg-deep) !important;
  border: 1px solid var(--bbg-border) !important;
}

/* =========================
   管理端
========================= */

.admin-page {
  background: var(--bbg-bg) !important;
  color: var(--bbg-text) !important;
}

.admin-sidebar {
  background: var(--bbg-bg-deep) !important;
  color: var(--bbg-text) !important;
  border-right: 1px solid var(--bbg-border) !important;
}

.admin-logo-box {
  background: transparent !important;
}

.admin-logo {
  filter: drop-shadow(0 10px 24px rgba(0, 0, 0, 0.35)) !important;
}

.admin-menu {
  color: var(--bbg-text) !important;
}

.admin-menu:hover,
.admin-menu.active {
  background: rgba(255, 242, 212, 0.16) !important;
  color: var(--bbg-cream) !important;
}

.admin-main {
  background: var(--bbg-bg) !important;
  color: var(--bbg-text) !important;
}

.admin-header {
  background: var(--bbg-bg) !important;
  border-bottom: 1px solid var(--bbg-border) !important;
  color: var(--bbg-cream) !important;
}

.admin-content {
  background: var(--bbg-bg) !important;
}

.data-table {
  background: var(--bbg-panel) !important;
  color: var(--bbg-text) !important;
  border: 1px solid var(--bbg-border) !important;
}

.data-table th {
  background: var(--bbg-panel-light) !important;
  color: var(--bbg-cream) !important;
}

.data-table td {
  border-bottom: 1px solid var(--bbg-border) !important;
  color: var(--bbg-text) !important;
}

/* 管理端按钮 */
.refresh-btn,
.add-btn,
.search-btn,
.page-btn,
.reply-btn,
.edit-btn {
  background: var(--bbg-cream) !important;
  color: var(--bbg-text-dark) !important;
  font-weight: 700 !important;
}

.refresh-btn:hover,
.add-btn:hover,
.search-btn:hover,
.page-btn:hover,
.reply-btn:hover,
.edit-btn:hover {
  background: var(--bbg-cream-soft) !important;
}

.reset-btn {
  background: rgba(255, 242, 212, 0.12) !important;
  color: var(--bbg-text) !important;
}

.reset-btn:hover {
  background: rgba(255, 242, 212, 0.20) !important;
}

.delete-btn {
  background: var(--bbg-danger) !important;
  color: #fff !important;
}

.delete-btn:hover {
  background: #a83f39 !important;
}

.knowledge-search-input {
  background: var(--bbg-bg-deep) !important;
  color: var(--bbg-text) !important;
  border: 1px solid var(--bbg-border) !important;
}

.knowledge-search-input::placeholder {
  color: var(--bbg-muted) !important;
}

.knowledge-search-input:focus {
  border-color: var(--bbg-cream) !important;
}

.total-count,
.page-info {
  color: var(--bbg-muted) !important;
}

/* 状态标签 */
.status.replied {
  background: rgba(255, 242, 212, 0.16) !important;
  color: var(--bbg-cream) !important;
}

.status.pending {
  background: rgba(196, 87, 79, 0.18) !important;
  color: #ffaaa2 !important;
}

/* =========================
   滚动条统一
========================= */

::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

::-webkit-scrollbar-track {
  background: var(--bbg-bg-deep);
}

::-webkit-scrollbar-thumb {
  background: rgba(255, 242, 212, 0.26);
  border-radius: 999px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 242, 212, 0.42);
}

/* =========================
   BBG Logo 循环动画
========================= */

/* 所有页面 logo 默认循环动画 */
.splash-logo,
.login-logo,
.sidebar-logo,
.admin-logo {
  animation:
    bbgLogoFloat 3.2s ease-in-out infinite,
    bbgLogoGlow 2.6s ease-in-out infinite;
  transform-origin: center center;
}

/* 启动页 logo 更明显一点 */
.splash-logo {
  animation:
    bbgLogoEntry 0.9s ease-out both,
    bbgLogoFloat 3.2s ease-in-out 0.9s infinite,
    bbgLogoGlow 2.6s ease-in-out 0.9s infinite;
}

/* 登录页 logo 中等强度 */
.login-logo {
  animation:
    bbgLogoFloat 3.4s ease-in-out infinite,
    bbgLogoGlow 2.8s ease-in-out infinite;
}

/* 左上角小 logo 轻一点，避免太晃 */
.sidebar-logo,
.admin-logo {
  animation:
    bbgLogoFloatSmall 3.8s ease-in-out infinite,
    bbgLogoGlowSoft 3s ease-in-out infinite;
}

/* 首次进入弹出 */
@keyframes bbgLogoEntry {
  0% {
    opacity: 0;
    transform: scale(0.78) translateY(22px);
    filter:
      drop-shadow(0 0 0 rgba(255, 242, 212, 0))
      drop-shadow(0 18px 36px rgba(0, 0, 0, 0.35));
  }

  60% {
    opacity: 1;
    transform: scale(1.06) translateY(0);
  }

  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 大 logo 呼吸悬浮 */
@keyframes bbgLogoFloat {
  0% {
    transform: translateY(0) scale(1);
  }

  50% {
    transform: translateY(-8px) scale(1.025);
  }

  100% {
    transform: translateY(0) scale(1);
  }
}

/* 小 logo 呼吸悬浮 */
@keyframes bbgLogoFloatSmall {
  0% {
    transform: translateY(0) scale(1);
  }

  50% {
    transform: translateY(-3px) scale(1.015);
  }

  100% {
    transform: translateY(0) scale(1);
  }
}

/* 大 logo 光晕 */
@keyframes bbgLogoGlow {
  0% {
    filter:
      drop-shadow(0 0 8px rgba(255, 242, 212, 0.22))
      drop-shadow(0 18px 36px rgba(0, 0, 0, 0.35));
  }

  50% {
    filter:
      drop-shadow(0 0 22px rgba(255, 242, 212, 0.52))
      drop-shadow(0 20px 42px rgba(0, 0, 0, 0.42));
  }

  100% {
    filter:
      drop-shadow(0 0 8px rgba(255, 242, 212, 0.22))
      drop-shadow(0 18px 36px rgba(0, 0, 0, 0.35));
  }
}

/* 小 logo 柔和光晕 */
@keyframes bbgLogoGlowSoft {
  0% {
    filter:
      drop-shadow(0 0 5px rgba(255, 242, 212, 0.16))
      drop-shadow(0 8px 18px rgba(0, 0, 0, 0.28));
  }

  50% {
    filter:
      drop-shadow(0 0 13px rgba(255, 242, 212, 0.35))
      drop-shadow(0 10px 22px rgba(0, 0, 0, 0.34));
  }

  100% {
    filter:
      drop-shadow(0 0 5px rgba(255, 242, 212, 0.16))
      drop-shadow(0 8px 18px rgba(0, 0, 0, 0.28));
  }
}

</style>


