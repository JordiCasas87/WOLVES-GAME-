import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Auth endpoints live under /api
      '/api': 'http://localhost:8080',

      // These are protected by JWT; proxy here avoids CORS in dev.
      '/game': 'http://localhost:8080',
      '/players': 'http://localhost:8080',
      '/questions': 'http://localhost:8080',
      '/me': 'http://localhost:8080',
    },
  },
})
