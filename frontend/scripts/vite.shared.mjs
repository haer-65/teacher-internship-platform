import path from 'node:path';
import { fileURLToPath } from 'node:url';
import vue from '@vitejs/plugin-vue';
import { loadEnv } from 'vite';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
export const frontendRoot = path.resolve(scriptDir, '..');

export function createFrontendViteConfig(mode = 'development') {
  const env = loadEnv(mode, frontendRoot, '');

  return {
    root: frontendRoot,
    resolve: {
      preserveSymlinks: true,
      alias: {
        '@': path.resolve(frontendRoot, 'src')
      }
    },
    plugins: [vue()],
    server: {
      host: '0.0.0.0',
      port: 5173,
      proxy: {
        '/api': {
          target: env.VITE_BACKEND_TARGET || 'http://localhost:8080',
          changeOrigin: true
        }
      }
    }
  };
}
