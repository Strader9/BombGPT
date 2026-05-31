<template>
  <div class="detail-page" style="padding: 20px;">
    <h1>{{ categoryName }}</h1>
    <hr>
    <div v-if="loading">加载中...</div>
    <div v-else-if="articles.length === 0">暂无文章</div>
    <div v-else class="article-list">
      <div v-for="article in articles" :key="article.id" class="article-item">
        <h3>{{ article.title }}</h3>
        <p>{{ article.content }}</p>
        <small>阅读：{{ article.viewCount }}</small>
      </div>
    </div>
    <button @click="goBack" style="margin-top: 20px; padding: 8px 16px;">返回首页</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const categoryName = ref('')
const articles = ref([])
const loading = ref(true)

onMounted(async () => {
  // 从URL参数里拿到分类ID
  const categoryId = route.params.id
  // 从后端获取该分类下的文章
  try {
    const res = await request.get(`/knowledge/list?categoryId=${categoryId}`)
    articles.value = res.data
    // 也可以顺便获取分类名，这里先用简单的
    categoryName.value = route.params.name || '分类详情'
  } catch (e) {
    console.error(e)
    articles.value = []
  } finally {
    loading.value = false
  }
})

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.article-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  margin: 15px 0;
}
</style>
