<template>
  <div class="form-page">
    <div class="card">
      <h2>用户注册</h2>
      <el-input v-model="username" placeholder="用户名" />
      <el-input v-model="email" placeholder="邮箱" style="margin-top:10px" />
      <el-input v-model="password" type="password" placeholder="密码" style="margin-top:10px" />
      <el-input v-model="adminCode" placeholder="管理员验证码（选填）" style="margin-top:10px" />
      <el-button type="primary" @click="reg" style="margin-top:15px">注册</el-button>
      <div style="margin-top:10px">
        <el-link @click="$router.push('/login')">去登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
const router = useRouter()
const username = ref('')
const email = ref('')
const password = ref('')
const adminCode = ref('')

const reg = async () => {
  const res = await request.post('/user/register', {
    username: username.value,
    email: email.value,
    password: password.value,
    adminCode: adminCode.value
  })
  alert(res.data.msg)
  if (res.data.code === 200) router.push('/login')
}
</script>

<style scoped>
.form-page { display: flex; height: 100vh; align-items: center; justify-content: center; background:#f7f8fa; }
.card { background:#fff; padding:30px; border-radius:10px; width:350px; }
</style>
