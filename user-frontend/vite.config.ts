/**
 * Vite配置文件
 */
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [
      vue({
        script: {
          defineModel: true,
          propsDestructure: true
        }
      }),
      vueDevTools()
    ],

    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
        '~': fileURLToPath(new URL('./node_modules', import.meta.url))
      }
    },

    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `
            @use "@/styles/variables" as *;
            @use "@/styles/mixins" as *;
          `,
          charset: false
        }
      },
      devSourcemap: mode === 'development'
    },

    server: {
      host: '0.0.0.0',
      port: parseInt(env.VITE_DEV_PORT) || 3000,
      cors: true,
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080/api',
          changeOrigin: true,
          secure: false
            }
        }
    },

    build: {
      target: 'es2015',
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: mode === 'development',
      minify: mode === 'production' ? 'terser' : false,
      rollupOptions: {
        output: {
          chunkFileNames: 'assets/js/[name]-[hash].js',
          entryFileNames: 'assets/js/[name]-[hash].js',
          assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia'],
            elementPlus: ['element-plus', '@element-plus/icons-vue'],
            utils: ['axios', 'dayjs']
          }
        }
      },
      chunkSizeWarningLimit: 1000
    },

    define: {
      __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: 'false',
      __VUE_PROD_DEVTOOLS__: 'false'
    },

    optimizeDeps: {
      include: [
        'vue',
        'vue-router',
        'pinia',
        'element-plus',
        '@element-plus/icons-vue',
        'axios'
      ]
    },

    esbuild: {
      drop: mode === 'production' ? ['console', 'debugger'] : []
    }
  }
})
