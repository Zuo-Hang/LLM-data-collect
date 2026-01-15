import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      // LLM API 代理到 local-llm-client (8081)
      '/api/llm': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      // OCR API 代理到 ocr-service-python (8082)
      '/api/ocr': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        rewrite: (path) => {
          // OCR 服务的路径映射：
          // /api/ocr/health -> /health
          // /api/ocr/recognize -> /api/ocr/recognize
          // /api/ocr/recognize-path -> /api/ocr/recognize-path
          if (path === '/api/ocr/health') {
            return '/health'
          }
          // 其他路径保持不变（OCR 服务的其他接口路径是 /api/ocr/xxx）
          return path
        },
      },
      // 其他 API 代理到主项目 (8080)
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
})

