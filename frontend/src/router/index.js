import { createRouter, createWebHistory } from 'vue-router'

import Home from '../views/HomeView.vue'
import Login from '../views/LoginView.vue'
import Register from '../views/RegisterView.vue'
import Forget from '../views/ForgetView.vue'
import Chat from '../views/ChatView.vue'

const routes = [
  // 打开根路径时，直接进入聊天页
  {
    path: '/',
    redirect: '/chat'
  },

  // 登录页
  {
    path: '/login',
    name: 'Login',
    component: Login
  },

  // 注册页
  {
    path: '/register',
    name: 'Register',
    component: Register
  },

  // 忘记密码页
  {
    path: '/forget',
    name: 'Forget',
    component: Forget
  },

  // AI聊天页
  {
    path: '/chat',
    name: 'Chat',
    component: Chat,
    meta: {
      requiresAuth: true
    }
  },

  // 原来的首页 / 知识库页面
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: {
      requiresAuth: true
    }
  },

  // 管理员页面
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/AdminView.vue'),
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  const role = sessionStorage.getItem('role')

  // 不需要登录就能访问的页面
  const publicPages = ['/login', '/register', '/forget']

  // 1. 未登录：只能进登录、注册、忘记密码
  if (!token && !publicPages.includes(to.path)) {
    return next('/login')
  }

  // 2. 已登录：访问登录页时自动跳到聊天页
  if (token && to.path === '/login') {
    return next('/chat')
  }

  // 3. 普通用户不能进管理员页面
  if (to.meta.requiresAdmin && role !== 'ADMIN') {
    alert('无权限')
    return next('/chat')
  }

  // 4. 其他情况放行
  next()
})

export default router
