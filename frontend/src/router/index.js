import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/HomeView.vue'
import Login from '../views/LoginView.vue'
import Register from '../views/RegisterView.vue'
import Forget from '../views/ForgetView.vue'
import Chat from '../views/ChatView.vue'

const routes = [
  { path: '/', name: 'Home', component: Home, meta: { requiresAuth: true } },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/forget', component: Forget },
  { path: '/chat', component: Chat, meta: { requiresAuth: true } },
  { path: '/admin',name: 'Admin',component: () => import('../views/AdminView.vue'),meta: { requiresAuth: true, requiresAdmin: true }},
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  const role = sessionStorage.getItem('role')

  // 1. 未登录：只能进登录/注册，其他跳登录
  if (!token && to.path !== '/login' && to.path !== '/register') {
    return next('/login')
  }

  // 2. 已登录：访问登录页自动跳到聊天页
  if (token && to.path === '/login') {
    return next('/chat')
  }

  // 3. 普通用户不能进 /admin 开头的页面
  if (to.path.startsWith('/admin') && role !== 'ADMIN') {
    alert('无权限')
    return next('/chat')
  }

  // 其他情况放行
  next()
})

export default router
