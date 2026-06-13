<template>
  <div class="form-page">
    <div class="card">
      <div class="login-logo-box">
        <img
          src="/bbg-logo.png"
          alt="BBG Logo"
          class="login-logo"
        />
      </div>

      <el-input
        ref="usernameInput"
        v-model="username"
        placeholder="用户名"
        clearable
        @keydown.enter.prevent="focusPassword"
      />

      <el-input
        ref="passwordInput"
        v-model="password"
        type="password"
        placeholder="密码"
        show-password
        clearable
        style="margin-top:10px"
        @keydown.enter.prevent="login"
      />

      <el-button
        type="primary"
        :loading="loading"
        style="margin-top:15px;width:100%;"
        @click="login"
      >
        {{ loading ? '登录中...' : '登录' }}
      </el-button>

      <div class="link-row">
        <el-link @click="$router.push('/register')">
          注册
        </el-link>

        <el-link @click="$router.push('/forget')">
          忘记密码
        </el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const username = ref('')
const password = ref('')
const loading = ref(false)

const usernameInput = ref(null)
const passwordInput = ref(null)

const router = useRouter()

onMounted(() => {
  nextTick(() => {
    usernameInput.value?.focus?.()
  })
})

const focusPassword = () => {
  passwordInput.value?.focus?.()
}

const login = async () => {
  const usernameValue = username.value.trim()
  const passwordValue = password.value.trim()

  if (!usernameValue) {
    alert('请输入用户名')
    usernameInput.value?.focus?.()
    return
  }

  if (!passwordValue) {
    alert('请输入密码')
    passwordInput.value?.focus?.()
    return
  }

  if (loading.value) {
    return
  }

  loading.value = true

  try {
    const res = await request.post('/user/login', {
      username: usernameValue,
      password: passwordValue
    })

    console.log('后端返回：', res.data)

    if (String(res.data.code) === '200') {
      const data = res.data.data || {}

      sessionStorage.setItem('token', data.token || '')
      sessionStorage.setItem('role', data.role || '')
      sessionStorage.setItem('username', data.username || usernameValue)

      router.push('/chat')
    } else {
      alert('登录失败：' + (res.data.msg || '用户名或密码错误'))
    }
  } catch (err) {
    console.error('请求异常：', err)
    alert('网络错误或后端异常，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form-page {
  display: flex;
  height: 100vh;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
}

.card {
  background: #fff;
  padding: 30px;
  border-radius: 14px;
  width: 350px;
  box-sizing: border-box;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.08);
}

.login-logo-box {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.login-logo {
  width: 180px;
  max-width: 80%;
  object-fit: contain;
}

.link-row {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
}
</style>
