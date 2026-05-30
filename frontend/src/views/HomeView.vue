<template>
  <div class="home-page">
    <!-- 右上角 退出按钮（原生HTML按钮，无需Element UI） -->
    <button class="logout-btn" @click="handleLogout">
      退出登录
    </button>

    <!-- 热门文章 TOP10 -->
    <div class="hot-list">
      <h2>🔥 热门文章 TOP10</h2>
      <div
        v-for="(item, index) in hotList"
        :key="item.id"
        class="hot-item"
      >
        <span class="rank">{{ index + 1 }}</span>
        <span class="title">{{ item.title }}</span>
        <span class="view-count">阅读 {{ item.viewCount }}</span>
      </div>
    </div>

    <!-- 分类列表 -->
    <div class="category-list">
      <h2>校园分类</h2>
      <div
        v-for="item in categoryList"
        :key="item.id"
        class="category-item"
        :class="{ active: selectedCategoryId === item.id }"
        @click="selectCategory(item.id)"
      >
        {{ item.name }}
      </div>
    </div>

    <!-- 知识库列表 -->
    <div class="knowledge-list">
      <h2>知识库文章</h2>
      <div
        v-for="item in knowledgeList"
        :key="item.id"
        class="knowledge-item"
      >
        <h3>{{ item.title }}</h3>
        <p>{{ item.content }}</p>
        <div class="meta">阅读 {{ item.viewCount }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCategoryList } from '../api/category'
import { getKnowledgeList, getKnowledgeByCategory, getHotKnowledge } from '../api/knowledge'

const router = useRouter()

// 分类数据
const categoryList = ref([])
const loadCategory = async () => {
  const res = await getCategoryList()
  categoryList.value = res.data
}

// 知识库数据
const knowledgeList = ref([])
const loadKnowledge = async (categoryId = null) => {
  let res
  if (categoryId) {
    res = await getKnowledgeByCategory(categoryId)
  } else {
    res = await getKnowledgeList()
  }
  knowledgeList.value = res.data
}

// 热门文章
const hotList = ref([])
const loadHot = async () => {
  try {
    const res = await getHotKnowledge()
    hotList.value = res.data
  } catch (e) {
    console.error("热门文章接口请求失败：", e)
    hotList.value = []
  }
}

// 当前选中分类
const selectedCategoryId = ref(null)
const selectCategory = (id) => {
  selectedCategoryId.value = selectedCategoryId.value === id ? null : id
  loadKnowledge(selectedCategoryId.value)
}

// ====================== 退出登录 ======================
const handleLogout = () => {
  // 清除登录状态（关闭网页就失效）
  sessionStorage.removeItem('token')
  // 跳回登录页
  router.push('/login')
}

// 页面加载
onMounted(() => {
  loadCategory()
  loadKnowledge()
  loadHot()
})
</script>

<style scoped>
.home-page {
  padding: 20px;
}

/* 退出按钮样式 */
.logout-btn {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 8px 16px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  z-index: 999;
}
.logout-btn:hover {
  background-color: #f78989;
}

.hot-list,
.category-list,
.knowledge-list {
  margin-bottom: 30px;
}

/* 热门文章样式 */
.hot-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 6px;
  margin: 8px 0;
}
.hot-item .rank {
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  margin-right: 10px;
  font-size: 12px;
}
.hot-item .title {
  flex: 1;
}
.hot-item .view-count {
  color: #666;
  font-size: 14px;
}

.category-item {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}
.category-item.active {
  color: #409eff;
  font-weight: bold;
}

.knowledge-item {
  margin: 15px 0;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
}
.meta {
  margin-top: 8px;
  color: #666;
  font-size: 14px;
}
</style>
