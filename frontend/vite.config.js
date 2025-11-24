import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import path from 'path';
import { fileURLToPath } from 'url';

// Recreate __dirname in ESM
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@components': path.resolve(__dirname, './src/components'),
      '@assets': path.resolve(__dirname, './src/assets'),
      '@pages': path.resolve(__dirname, './src/pages'),
      '@hooks': path.resolve(__dirname, './src/hooks'),
      '@context': path.resolve(__dirname, './src/context'),
      '@utils': path.resolve(__dirname, './src/utils')
    }
  },
  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api/products': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
      },
      '/api/orders': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
      },
      '/api/logs': {
        target: 'http://localhost:8084',
        changeOrigin: true,
        secure: false,
      }
    }
  },
  build: {
    target: 'esnext',
    sourcemap: true
  },
  esbuild: false, // ensures Rolldown (not esbuild) handles bundling
  define: {
    global: 'window',
  }
});
