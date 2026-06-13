import { createRouter, createWebHistory } from 'vue-router'

import Login from '../views/LoginView.vue'
import Register from '../views/RegisterView.vue'
import Forget from '../views/ForgetView.vue'
import Chat from '../views/ChatView.vue'

const routes = [
  {
    path: '/',
    redirect: '/chat'
  },
  {
    path: '/login',
    component: Login
  },
  {
    path: '/register',
    component: Register
  },
  {
    path: '/forget',
    component: Forget
  },
  {
    path: '/chat',
    component: Chat,
    meta: {
      requiresAuth: true
    }
  },
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

router.beforeEach((to) => {
  const token = sessionStorage.getItem('token')
  const role = sessionStorage.getItem('role')

  // 未登录：只能去登录、注册、忘记密码
  if (!token && to.path !== '/login' && to.path !== '/register' && to.path !== '/forget') {
    return '/login'
  }

  // 已登录：访问登录页时自动进入聊天页
  if (token && to.path === '/login') {
    return '/chat'
  }

  // 普通用户不能进入管理端
  if (to.meta.requiresAdmin && role !== 'ADMIN') {
    alert('无权限')
    return '/chat'
  }

  return true
})

export default router
