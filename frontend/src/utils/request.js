import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('token')

    // 关键：跳过 ngrok 免费版浏览器提示页
    config.headers['ngrok-skip-browser-warning'] = 'true'

    if (token) {
      config.headers.token = token
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response
  },
  error => {
    console.error('接口请求失败：', error)

    if (error.response && error.response.status === 401) {
      alert('登录状态失效或无权限，请重新登录')
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('role')
      sessionStorage.removeItem('username')
      window.location.href = '/login'
    }

    return Promise.reject(error)
  }
)

export default request
