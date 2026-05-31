<template>
  <div class="form-page">
    <div class="card">
      <h2>找回密码</h2>
      <el-input v-model="email" placeholder="邮箱" />
      <div style="display:flex;gap:10px;margin-top:10px">
        <el-input v-model="code" placeholder="验证码" style="flex:1" />
        <el-button @click="send">发送</el-button>
      </div>
      <el-input v-model="password" type="password" placeholder="新密码" style="margin-top:10px" />
      <el-button type="primary" @click="reset" style="margin-top:15px">重置</el-button>
      <el-link @click="$router.push('/login')" style="margin-top:10px">返回登录</el-link>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
const router = useRouter()
const email = ref('')
const code = ref('')
const password = ref('')

const send = async () => {
  await request.post('/user/send-code', { email: email.value })
  alert('发送成功')
}
const reset = async () => {
  const res = await request.post('/user/reset-pwd', {
    email: email.value, code: code.value, password: password.value
  })
  alert(res.data.msg)
  if (res.data.code === 200) router.push('/login')
}
</script>

<style scoped>
.form-page { display: flex; height: 100vh; align-items: center; justify-content: center; background:#f7f8fa; }
.card { background:#fff; padding:30px; border-radius:10px; width:350px; }
</style>
