<template>
  <div style="width:300px;margin:100px auto">
    <h2>校园生活百事通</h2>

    <el-input
      v-model="username"
      placeholder="请输入用户名"
      style="margin-bottom:10px"
    />

    <el-input
      v-model="password"
      type="password"
      placeholder="请输入密码"
      style="margin-bottom:10px"
    />

    <el-button type="primary" @click="login">
      登录
    </el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const username = ref('')
const password = ref('')
const router = useRouter()

const login = async () => {
  try {
    // 1. 向后端发送登录请求
    const res = await request.post('/api/login', {
      username: username.value,
      password: password.value
    })

    // 2. 登录成功，保存token到sessionStorage（关闭网页自动失效）
    if (res.data.code === 200) {
      sessionStorage.setItem('token', res.data.token)
      alert('登录成功！')
      // 3. 跳转到首页
      router.push('/')
    } else {
      alert(res.data.msg || '登录失败：用户名或密码错误')
    }
  } catch (err) {
    console.error(err)
    alert('登录失败：请检查后端是否启动，或账号密码是否正确')
  }
}
</script>
