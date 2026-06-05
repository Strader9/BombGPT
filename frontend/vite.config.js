import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],

  server: {
    host: '0.0.0.0',

    // 允许 Cloudflare 临时公网域名访问
    allowedHosts: [
      '.trycloudflare.com'
    ]
  }
})
