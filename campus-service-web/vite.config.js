import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(() => {
  const allowedHosts = (process.env.VITE_ALLOWED_HOSTS || '')
    .split(',')
    .map(host => host.trim())
    .filter(Boolean)

  return {
    plugins: [vue()],
    server: {
      host: process.env.VITE_DEV_HOST || '0.0.0.0',
      port: Number(process.env.VITE_DEV_PORT || 5173),
      allowedHosts: allowedHosts.length ? allowedHosts : undefined,
      proxy: {
        '/api': {
          target: process.env.VITE_API_PROXY_TARGET || 'http://127.0.0.1:8080',
          changeOrigin: true,
          rewrite: path => path.replace(/^\/api/, '')
        }
      }
    }
  }
})
