import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

export default defineConfig({
  plugins: [svelte()],
  server: {
    proxy: {
      '/api': 'http://newhanchat:8080',  // Docker service name
      '/ws': {
        target: 'ws://newhanchat:8080',  // WebSocket proxy
        ws: true
      }
    }
  },
  preview: {  // For production preview server
    port: 4173,
    strictPort: true,
    proxy: {
      '/api': 'http://newhanchat:8080',
      '/ws': {
        target: 'ws://newhanchat:8080',
        ws: true
      }
    }
  }
})