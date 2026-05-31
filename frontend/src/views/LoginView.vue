<template>
  <div class="form-page">
    <div class="card">
      <h2>校园生活百事通</h2>
      <el-input v-model="username" placeholder="用户名" />
      <el-input v-model="password" type="password" placeholder="密码" style="margin-top:10px" />
      <el-button type="primary" @click="login" style="margin-top:15px">登录</el-button>
      <div style="margin-top:10px;display:flex;justify-content:space-between">
        <el-link @click="$router.push('/register')">注册</el-link>
        <el-link @click="$router.push('/forget')">忘记密码</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const username = ref('')
const password = ref('')
const router = useRouter()

const login = async () => {
  try {
    const res = await axios.post('http://localhost:8080/user/login', {
      username: username.value,
      password: password.value
    })

    console.log('后端返回：', res.data)

    // 关键：后端 code 是字符串 "200"，必须写 === "200"
    if (res.data.code === "200") {
      sessionStorage.setItem('token', res.data.data.token)
      sessionStorage.setItem('role', res.data.data.role)
      sessionStorage.setItem('username', res.data.data.username)

      console.log('登录成功，准备跳转到 /chat')
      router.push('/chat')
    } else {
      alert('登录失败：' + res.data.msg)
    }
  } catch (err) {
    console.error('请求异常：', err)
    alert('网络错误或后端异常，请重试')
  }
}
</script>

<style scoped>
.form-page { display: flex; height: 100vh; align-items: center; justify-content: center; background:#f7f8fa; }
.card { background:#fff; padding:30px; border-radius:10px; width:350px; }
</style>
