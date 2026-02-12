import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  preview: {
    host: true,
    port: 3000,
    allowedHosts: [
      'wolves-game-front.onrender.com'
    ]
  }
})
//hola
  /*
   ─────────────────────────────────────────────
   🔴 CONFIGURACIÓN LOCAL (BACKEND EN LOCAL)
   👉 Úsala SOLO si arrancas IntelliJ en :8080
   👉 Para activrrla: descomenta este bloque
   ─────────────────────────────────────────────

  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/game': 'http://localhost:8080',
      '/players': 'http://localhost:8080',
      '/questions': 'http://localhost:8080',
      '/me': 'http://localhost:8080',
    },
  },

  */

  /*
   ─────────────────────────────────────────────
   🟢 CONFIGURACIÓN ACTIVA (RENDER)
   👉 No hace falta server ni proxy
   👉 Las llamadas van a VITE_API_URL
   👉 Definido en .env
   ─────────────────────────────────────────────
  */
