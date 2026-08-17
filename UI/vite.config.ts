import path from "node:path"
import { defineConfig, loadEnv } from "vite"
import react from "@vitejs/plugin-react"
import tailwindcss from "@tailwindcss/vite"

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "")
  // Backend Spring Boot mặc định chạy ở 8080 (api/src/main/resources/application.yaml).
  const target = env.VITE_API_PROXY_TARGET ?? "http://localhost:8080"

  return {
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
    server: {
      port: 5173,
      proxy: {
        // Proxy thay cho CORS: request đi cùng origin với dev server nên backend
        // không cần khai CorsConfigurationSource.
        "/api": { target, changeOrigin: true },
        // Ảnh sản phẩm được Spring serve tĩnh qua /uploads/** (WebMvcConfig).
        "/uploads": { target, changeOrigin: true },
      },
    },
  }
})
