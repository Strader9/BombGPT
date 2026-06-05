<template>
  <div class="admin-page">
    <!-- 左侧栏 -->
    <aside class="admin-sidebar">
      <h2>管理端</h2>

      <div
        class="admin-menu"
        :class="{ active: activeMenu === 'feedback' }"
        @click="activeMenu = 'feedback'"
      >
        用户反馈
      </div>

      <div
        class="admin-menu"
        :class="{ active: activeMenu === 'knowledge' }"
        @click="activeMenu = 'knowledge'"
      >
        知识库管理
      </div>

      <div
        class="admin-menu"
        @click="goChat"
      >
        返回客户端
      </div>

      <div
        class="admin-menu logout"
        @click="logout"
      >
        退出登录
      </div>
    </aside>

    <!-- 右侧主内容 -->
    <main class="admin-main">
      <header class="admin-header">
        {{ activeMenu === 'feedback' ? '用户反馈管理' : '知识库管理' }}
      </header>

      <!-- ================= 用户反馈管理 ================= -->
      <section
        v-if="activeMenu === 'feedback'"
        class="admin-content"
      >
        <div class="toolbar">
          <button
            class="refresh-btn"
            @click="loadFeedback"
          >
            刷新反馈
          </button>
        </div>

        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户</th>
              <th>反馈内容</th>
              <th>状态</th>
              <th>提交时间</th>
              <th>管理员回复</th>
              <th>回复时间</th>
              <th>操作</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="item in feedbackList"
              :key="item.id"
            >
              <td>{{ item.id }}</td>
              <td>{{ item.username || '匿名用户' }}</td>
              <td class="content-cell">{{ item.content }}</td>
              <td>
                <span
                  class="status"
                  :class="item.status === 'REPLIED' ? 'replied' : 'pending'"
                >
                  {{ item.status === 'REPLIED' ? '已回复' : '未回复' }}
                </span>
              </td>
              <td>{{ formatTime(item.createTime) }}</td>
              <td class="content-cell">{{ item.reply || '暂无回复' }}</td>
              <td>{{ item.replyTime ? formatTime(item.replyTime) : '-' }}</td>
              <td>
                <button
                  class="reply-btn"
                  @click="openReplyDialog(item)"
                >
                  回复
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <div
          v-if="feedbackList.length === 0"
          class="empty"
        >
          暂无用户反馈
        </div>
      </section>

      <!-- ================= 知识库管理 ================= -->
      <section
        v-if="activeMenu === 'knowledge'"
        class="admin-content"
      >
        <div class="toolbar">
          <button
            class="refresh-btn"
            @click="loadKnowledge"
          >
            刷新知识库
          </button>

          <button
            class="add-btn"
            @click="openAddKnowledgeDialog"
          >
            新增问答
          </button>
        </div>

        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>分类ID</th>
              <th>问题</th>
              <th>答案</th>
              <th>关键词</th>
              <th>浏览量</th>
              <th>状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="item in knowledgeList"
              :key="item.id"
            >
              <td>{{ item.id }}</td>
              <td>{{ item.categoryId }}</td>
              <td class="content-cell">{{ item.question }}</td>
              <td class="content-cell">{{ item.answer }}</td>
              <td>{{ item.keywords || '-' }}</td>
              <td>{{ item.viewCount || 0 }}</td>
              <td>
                <span
                  class="status"
                  :class="item.status === 1 ? 'replied' : 'pending'"
                >
                  {{ item.status === 1 ? '启用' : '停用' }}
                </span>
              </td>
              <td>{{ formatTime(item.updateTime) }}</td>
              <td>
                <button
                  class="edit-btn"
                  @click="openEditKnowledgeDialog(item)"
                >
                  修改
                </button>

                <button
                  class="delete-btn"
                  @click="deleteKnowledgeItem(item.id)"
                >
                  删除
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <div
          v-if="knowledgeList.length === 0"
          class="empty"
        >
          暂无知识库内容
        </div>
      </section>
    </main>

    <!-- ================= 回复反馈弹窗 ================= -->
    <div
      v-if="replyDialogVisible"
      class="dialog-mask"
    >
      <div class="reply-dialog">
        <h3>回复用户反馈</h3>

        <div class="original-feedback">
          <strong>用户反馈：</strong>
          <p>{{ currentFeedback?.content }}</p>
        </div>

        <textarea
          v-model="replyContent"
          class="reply-textarea"
          placeholder="请输入回复内容"
        ></textarea>

        <div class="dialog-actions">
          <button
            class="cancel-btn"
            @click="closeReplyDialog"
          >
            取消
          </button>

          <button
            class="submit-btn"
            :disabled="replyLoading"
            @click="submitReply"
          >
            {{ replyLoading ? '提交中...' : '提交回复' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ================= 新增/修改知识库弹窗 ================= -->
    <div
      v-if="knowledgeDialogVisible"
      class="dialog-mask"
    >
      <div class="knowledge-dialog">
        <h3>{{ knowledgeDialogMode === 'add' ? '新增知识库问答' : '修改知识库问答' }}</h3>

        <div class="form-row">
          <label>分类ID</label>
          <input
            v-model.number="knowledgeForm.categoryId"
            class="form-input"
            type="number"
            placeholder="例如：1"
          />
        </div>

        <div class="form-row">
          <label>问题</label>
          <input
            v-model="knowledgeForm.question"
            class="form-input"
            placeholder="请输入问题"
          />
        </div>

        <div class="form-row">
          <label>答案</label>
          <textarea
            v-model="knowledgeForm.answer"
            class="form-textarea"
            placeholder="请输入答案"
          ></textarea>
        </div>

        <div class="form-row">
          <label>关键词</label>
          <input
            v-model="knowledgeForm.keywords"
            class="form-input"
            placeholder="例如：食堂,饭堂,餐厅"
          />
        </div>

        <div class="form-row">
          <label>状态</label>
          <select
            v-model.number="knowledgeForm.status"
            class="form-input"
          >
            <option :value="1">启用</option>
            <option :value="0">停用</option>
          </select>
        </div>

        <div class="form-row">
          <label>管理员备注</label>
          <input
            v-model="knowledgeForm.adminNote"
            class="form-input"
            placeholder="可不填"
          />
        </div>

        <div class="dialog-actions">
          <button
            class="cancel-btn"
            @click="closeKnowledgeDialog"
          >
            取消
          </button>

          <button
            class="submit-btn"
            :disabled="knowledgeLoading"
            @click="submitKnowledge"
          >
            {{ knowledgeLoading ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getAllFeedback, replyFeedback } from '../api/feedback'
import {
  getAdminKnowledgeList,
  addKnowledge,
  updateKnowledge,
  deleteKnowledge
} from '../api/knowledgeAdmin'

const router = useRouter()

const activeMenu = ref('feedback')

// ====================== 用户反馈管理 ======================
const feedbackList = ref([])
const replyDialogVisible = ref(false)
const currentFeedback = ref(null)
const replyContent = ref('')
const replyLoading = ref(false)

const loadFeedback = async () => {
  try {
    const res = await getAllFeedback()

    if (res.data.code === '200' || res.data.code === 200) {
      feedbackList.value = res.data.data || []
    } else {
      alert(res.data.msg || '获取反馈失败')
    }
  } catch (error) {
    console.error('获取反馈失败：', error)
    alert('获取反馈失败，请检查管理员权限或后端接口')
  }
}

const openReplyDialog = (item) => {
  currentFeedback.value = item
  replyContent.value = item.reply || ''
  replyDialogVisible.value = true
}

const closeReplyDialog = () => {
  replyDialogVisible.value = false
  currentFeedback.value = null
  replyContent.value = ''
}

const submitReply = async () => {
  const reply = replyContent.value.trim()

  if (!reply) {
    alert('请输入回复内容')
    return
  }

  if (!currentFeedback.value) {
    alert('未选择反馈')
    return
  }

  replyLoading.value = true

  try {
    const res = await replyFeedback(currentFeedback.value.id, reply)

    if (res.data.code === '200' || res.data.code === 200) {
      alert('回复成功')
      closeReplyDialog()
      loadFeedback()
    } else {
      alert(res.data.msg || '回复失败')
    }
  } catch (error) {
    console.error('回复失败：', error)
    alert('回复失败，请检查管理员权限或后端接口')
  } finally {
    replyLoading.value = false
  }
}

// ====================== 知识库管理 ======================
const knowledgeList = ref([])
const knowledgeDialogVisible = ref(false)
const knowledgeDialogMode = ref('add')
const knowledgeLoading = ref(false)

const emptyKnowledgeForm = () => ({
  id: null,
  categoryId: 1,
  question: '',
  answer: '',
  keywords: '',
  status: 1,
  adminNote: ''
})

const knowledgeForm = ref(emptyKnowledgeForm())

const loadKnowledge = async () => {
  try {
    const res = await getAdminKnowledgeList()

    if (res.data.code === '200' || res.data.code === 200) {
      knowledgeList.value = res.data.data || []
    } else {
      alert(res.data.msg || '获取知识库失败')
    }
  } catch (error) {
    console.error('获取知识库失败：', error)
    alert('获取知识库失败，请检查管理员权限或后端接口')
  }
}

const openAddKnowledgeDialog = () => {
  knowledgeDialogMode.value = 'add'
  knowledgeForm.value = emptyKnowledgeForm()
  knowledgeDialogVisible.value = true
}

const openEditKnowledgeDialog = (item) => {
  knowledgeDialogMode.value = 'edit'

  knowledgeForm.value = {
    id: item.id,
    categoryId: item.categoryId || 1,
    question: item.question || '',
    answer: item.answer || '',
    keywords: item.keywords || '',
    status: item.status === 0 ? 0 : 1,
    adminNote: item.adminNote || ''
  }

  knowledgeDialogVisible.value = true
}

const closeKnowledgeDialog = () => {
  knowledgeDialogVisible.value = false
  knowledgeForm.value = emptyKnowledgeForm()
}

const submitKnowledge = async () => {
  if (!knowledgeForm.value.question.trim()) {
    alert('问题不能为空')
    return
  }

  if (!knowledgeForm.value.answer.trim()) {
    alert('答案不能为空')
    return
  }

  knowledgeLoading.value = true

  try {
    let res

    if (knowledgeDialogMode.value === 'add') {
      res = await addKnowledge(knowledgeForm.value)
    } else {
      res = await updateKnowledge(knowledgeForm.value)
    }

    if (res.data.code === '200' || res.data.code === 200) {
      alert(knowledgeDialogMode.value === 'add' ? '新增成功' : '修改成功')
      closeKnowledgeDialog()
      loadKnowledge()
    } else {
      alert(res.data.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存知识库失败：', error)
    alert('保存失败，请检查后端接口或管理员权限')
  } finally {
    knowledgeLoading.value = false
  }
}

const deleteKnowledgeItem = async (id) => {
  const ok = window.confirm('确定要删除这条知识库内容吗？删除后不可恢复。')

  if (!ok) {
    return
  }

  try {
    const res = await deleteKnowledge(id)

    if (res.data.code === '200' || res.data.code === 200) {
      alert('删除成功')
      loadKnowledge()
    } else {
      alert(res.data.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除知识库失败：', error)
    alert('删除失败，请检查后端接口或管理员权限')
  }
}

// 切换到知识库管理时自动加载知识库
watch(activeMenu, (value) => {
  if (value === 'knowledge') {
    loadKnowledge()
  }

  if (value === 'feedback') {
    loadFeedback()
  }
})

// ====================== 公共方法 ======================
const formatTime = (time) => {
  if (!time) {
    return '-'
  }

  return String(time).replace('T', ' ').slice(0, 19)
}

const goChat = () => {
  router.push('/chat')
}

const logout = () => {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('role')
  sessionStorage.removeItem('username')
  router.push('/login')
}

onMounted(() => {
  loadFeedback()
})
</script>

<style scoped>
.admin-page {
  height: 100vh;
  display: flex;
  background: #f7f8fa;
}

.admin-sidebar {
  width: 240px;
  background: #111827;
  color: #fff;
  padding: 24px 16px;
  box-sizing: border-box;
}

.admin-sidebar h2 {
  text-align: center;
  margin-bottom: 30px;
}

.admin-menu {
  padding: 14px 16px;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  color: #d1d5db;
}

.admin-menu:hover,
.admin-menu.active {
  background: #2563eb;
  color: #fff;
}

.logout {
  margin-top: 30px;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.admin-header {
  height: 70px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  padding-left: 28px;
  font-size: 24px;
  font-weight: bold;
}

.admin-content {
  padding: 24px;
  overflow: auto;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.refresh-btn,
.add-btn {
  height: 38px;
  padding: 0 18px;
  border: none;
  border-radius: 8px;
  color: #fff;
  cursor: pointer;
}

.refresh-btn {
  background: #1677ff;
}

.add-btn {
  background: #10b981;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
}

.data-table th,
.data-table td {
  border-bottom: 1px solid #e5e7eb;
  padding: 12px;
  text-align: left;
  font-size: 14px;
  vertical-align: top;
}

.data-table th {
  background: #f3f4f6;
  color: #374151;
}

.content-cell {
  max-width: 320px;
  white-space: pre-wrap;
  word-break: break-word;
}

.status {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 13px;
}

.status.pending {
  background: #fff7ed;
  color: #ea580c;
}

.status.replied {
  background: #ecfdf5;
  color: #059669;
}

.reply-btn,
.edit-btn,
.delete-btn {
  height: 32px;
  padding: 0 12px;
  border: none;
  border-radius: 6px;
  color: #fff;
  cursor: pointer;
  margin-right: 6px;
}

.reply-btn {
  background: #10b981;
}

.edit-btn {
  background: #1677ff;
}

.delete-btn {
  background: #ef4444;
}

.empty {
  margin-top: 30px;
  color: #6b7280;
  text-align: center;
}

/* 弹窗 */
.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.reply-dialog,
.knowledge-dialog {
  width: 560px;
  max-height: 80vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  box-sizing: border-box;
}

.reply-dialog h3,
.knowledge-dialog h3 {
  margin: 0 0 16px;
}

.original-feedback {
  background: #f9fafb;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 14px;
}

.original-feedback p {
  margin: 8px 0 0;
  line-height: 1.6;
}

.reply-textarea,
.form-textarea {
  width: 100%;
  height: 140px;
  resize: none;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 12px;
  box-sizing: border-box;
  font-size: 15px;
  outline: none;
}

.reply-textarea:focus,
.form-textarea:focus,
.form-input:focus {
  border-color: #1677ff;
}

.dialog-actions {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn,
.submit-btn {
  height: 38px;
  min-width: 88px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.cancel-btn {
  background: #f3f4f6;
  color: #333;
}

.submit-btn {
  background: #1677ff;
  color: #fff;
}

.submit-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

/* 表单 */
.form-row {
  margin-bottom: 14px;
}

.form-row label {
  display: block;
  margin-bottom: 6px;
  color: #374151;
  font-weight: 600;
}

.form-input {
  width: 100%;
  height: 40px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 0 12px;
  box-sizing: border-box;
  font-size: 14px;
  outline: none;
}
</style>
