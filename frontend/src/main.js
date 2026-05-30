import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 1. 导入路由实例
import router from './router'

// 2. 链式调用，按顺序注册 ElementPlus 和 路由
createApp(App)
  .use(ElementPlus)
  .use(router) // 关键：注册路由
  .mount('#app')
