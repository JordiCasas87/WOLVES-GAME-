/*
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/game': 'http://localhost:8080',
      '/players': 'http://localhost:8080',
      '/questions': 'http://localhost:8080',
      '/me': 'http://localhost:8080',
    },
  },
})
*/

import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
})
  

  
