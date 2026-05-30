import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/HomeView.vue'
import Login from '../views/LoginView.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true } // 需要登录
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫（实现：关闭网页再打开必须重新登录）
router.beforeEach((to, from, next) => {
  // 从 sessionStorage 取 token（关闭网页自动清空）
  const token = sessionStorage.getItem('token')

  // 需要登录，但没有 token → 跳登录页
  if (to.meta.requiresAuth && !token) {
    next('/login')
  }
  // 已经登录，还想访问 login 页 → 重定向回首页
  else if (to.path === '/login' && token) {
    next('/')
  }
  // 正常放行
  else {
    next()
  }
})

export default router
